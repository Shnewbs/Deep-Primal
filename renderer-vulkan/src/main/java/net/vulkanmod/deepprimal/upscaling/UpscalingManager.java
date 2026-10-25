package net.vulkanmod.deepprimal.upscaling;

import com.shnewbs.deepprimal.settings.UpscalingConfig;
import com.shnewbs.deepprimal.settings.RendererPreferences;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.render.engine.VkCommandEncoder;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.PipelineConfig;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.util.VkResult;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFormat;
import org.lwjgl.vulkan.VkImageLayout;
import org.lwjgl.vulkan.VkImageUsageFlags;
import org.lwjgl.vulkan.VkSampleCountFlags;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import java.util.Optional;

/**
 * Manages upscaling (FSR/DLSS) for the Deep Primal Vulkan renderer.
 * Integrates with UpscalingConfig and handles render resolution scaling.
 */
public final class UpscalingManager {
    private static UpscalingManager INSTANCE;

    private final VkGpuDevice device;
    private int displayWidth;
    private int displayHeight;
    private int renderWidth;
    private int renderHeight;

    private UpscalingConfig.UpscalingMode currentMode = UpscalingConfig.UpscalingMode.FSR;
    private UpscalingConfig.FsrQuality fsrQuality = UpscalingConfig.FsrQuality.BALANCED;
    private UpscalingConfig.DlssQuality dlssQuality = UpscalingConfig.DlssQuality.BALANCED;
    private boolean frameGenerationEnabled = false;
    private boolean initialized = false;

    // Render resources
    private GpuTexture renderColorTexture;
    private GpuTextureView renderColorView;
    private GpuTexture renderDepthTexture;
    private GpuTextureView renderDepthView;
    private Framebuffer renderFramebuffer;
    private net.vulkanmod.vulkan.framebuffer.RenderPass renderPass;

    // Upscale pipeline
    private GraphicsPipeline upscalePipeline;

    private UpscalingManager() {
        this.device = VkGpuDevice.current();
        this.displayWidth = 1920;
        this.displayHeight = 1080;
        updateRenderResolution();
    }

    public static synchronized UpscalingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UpscalingManager();
        }
        return INSTANCE;
    }

    /**
     * Initialize upscaling based on current config.
     * Should be called after Vulkan device is created.
     */
    public void initialize() {
        if (initialized) return;

        // Only initialize if Vulkan backend is active
        if (RendererPreferences.requestedBackend() != RendererPreferences.Backend.Vulkan) {
            return;
        }

        UpscalingConfig.UpscalingMode mode = UpscalingConfig.getActiveMode();
        if (mode == null) {
            // Upscaling disabled
            this.currentMode = UpscalingConfig.UpscalingMode.FSR; // Default, but won't be used
            this.renderWidth = displayWidth;
            this.renderHeight = displayHeight;
            initialized = true;
            return;
        }

        this.currentMode = mode;
        this.fsrQuality = UpscalingConfig.FsrQuality.fromIndex(UpscalingConfig.FSR_QUALITY.get());
        this.dlssQuality = UpscalingConfig.DlssQuality.fromIndex(UpscalingConfig.DLSS_QUALITY.get());
        this.frameGenerationEnabled = UpscalingConfig.FRAME_GENERATION.get();

        updateRenderResolution();
        createRenderResources();
        createUpscalePipeline();

        initialized = true;
    }

    private void updateRenderResolution() {
        float scaleFactor = getScaleFactor();
        this.renderWidth = Math.max(1, (int) (displayWidth / scaleFactor));
        this.renderHeight = Math.max(1, (int) (displayHeight / scaleFactor));
    }

    private float getScaleFactor() {
        if (currentMode == UpscalingConfig.UpscalingMode.FSR) {
            return fsrQuality.scaleFactor();
        } else if (currentMode == UpscalingConfig.UpscalingMode.DLSS) {
            return dlssQuality.scaleFactor();
        }
        return 1.0f;
    }

    private void createRenderResources() {
        // Create render target at lower resolution
        VkGpuDevice.ColorAttachmentInfo colorInfo = new VkGpuDevice.ColorAttachmentInfo(
            renderWidth, renderHeight, VkFormat.VK_FORMAT_R16G16B16A16_SFLOAT, 1);
        this.renderColorTexture = device.createColorAttachmentTexture(colorInfo);
        this.renderColorView = device.createTextureView(renderColorTexture);

        // Create depth texture
        VkGpuDevice.DepthAttachmentInfo depthInfo = new VkGpuDevice.DepthAttachmentInfo(
            renderWidth, renderHeight, VkFormat.VK_FORMAT_D32_SFLOAT, 1);
        this.renderDepthTexture = device.createDepthAttachmentTexture(depthInfo);
        this.renderDepthView = device.createTextureView(renderDepthTexture);

        // Create framebuffer
        this.renderFramebuffer = Framebuffer.builder()
            .width(renderWidth)
            .height(renderHeight)
            .layers(1)
            .withColorAttachment(renderColorTexture)
            .withDepthAttachment(renderDepthTexture)
            .build();

        // Create render pass
        net.vulkanmod.vulkan.framebuffer.RenderPass.Builder builder = 
            net.vulkanmod.vulkan.framebuffer.RenderPass.builder(renderFramebuffer);
        builder.getColorAttachmentInfo().setFinalLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        builder.getColorAttachmentInfo().setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_STORE);
        builder.getDepthAttachmentInfo().setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
        this.renderPass = builder.build();
    }

    private void createUpscalePipeline() {
        String shaderPrefix = currentMode == UpscalingConfig.UpscalingMode.FSR 
            ? "deepprimal_upscaling/fsr_upscale"
            : "deepprimal_upscaling/dlss_upscale";

        PipelineConfig config = PipelineConfig.builder()
            .vertexShader(shaderPrefix + ".vert.spv")
            .fragmentShader(shaderPrefix + ".frag.spv")
            .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
            .cullMode(VK10.VK_CULL_MODE_NONE)
            .depthTestEnable(false)
            .depthWriteEnable(false)
            .build();

        this.upscalePipeline = new GraphicsPipeline(config);
    }

    /**
     * Begin rendering at the scaled resolution.
     * Call this instead of the normal main pass begin.
     */
    public void beginScaledRender(VkCommandEncoder encoder) {
        if (currentMode == UpscalingConfig.UpscalingMode.FSR || 
            currentMode == UpscalingConfig.UpscalingMode.DLSS) {
            encoder.beginRenderPass(renderPass, renderFramebuffer);
            encoder.setViewport(0, 0, renderWidth, renderHeight);
            encoder.setScissor(0, 0, renderWidth, renderHeight);
        }
    }

    /**
     * End scaled render and perform upscale to display resolution.
     * Call this after all scene rendering is complete.
     */
    public void endScaledRenderAndUpscale(VkCommandEncoder encoder) {
        if (currentMode == UpscalingConfig.UpscalingMode.FSR || 
            currentMode == UpscalingConfig.UpscalingMode.DLSS) {
            encoder.endRenderPass();

            // Transition render color texture to shader read
            try (MemoryStack stack = MemoryStack.stackPush()) {
                renderColorTexture.transitionImageLayout(stack, encoder.getCommandBuffer(),
                    VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            }

            // Get display framebuffer (swapchain)
            Framebuffer displayFramebuffer = Renderer.getInstance().getSwapChain();
            net.vulkanmod.vulkan.framebuffer.RenderPass displayRenderPass = 
                Renderer.getInstance().getMainPass().getMainRenderPass(); // Need accessor

            encoder.beginRenderPass(displayRenderPass, displayFramebuffer);
            encoder.setViewport(0, 0, displayWidth, displayHeight);
            encoder.setScissor(0, 0, displayWidth, displayHeight);

            // Bind upscale pipeline
            PipelineState state = PipelineState.getCurrentPipelineState(displayRenderPass);
            long pipelineHandle = upscalePipeline.getHandle(state);
            encoder.bindGraphicsPipeline(pipelineHandle);

            // Bind render color texture as input
            // This assumes descriptor set layout matches the shader
            encoder.bindDescriptorSet(0, renderColorView);

            // Push constants for upscaling parameters
            PushConstants pushConstants = upscalePipeline.getPushConstants();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                float rcpScale = getScaleFactor();
                pushConstants.update(
                    stack.mallocFloat(4)
                        .put(0, 1.0f / rcpScale)
                        .put(1, (float) renderWidth)
                        .put(2, (float) renderHeight)
                        .put(3, currentMode == UpscalingConfig.UpscalingMode.FSR ? 0.0f : 1.0f)
                );
                encoder.pushConstants(pipelineHandle, pushConstants);
            }

            // Draw fullscreen triangle
            encoder.draw(3, 1, 0, 0);

            encoder.endRenderPass();
        }
    }

    /**
     * Handle window resize.
     */
    public void onResize(int width, int height) {
        this.displayWidth = width;
        this.displayHeight = height;

        if (initialized && (currentMode == UpscalingConfig.UpscalingMode.FSR ||
                            currentMode == UpscalingConfig.UpscalingMode.DLSS)) {
            int newRenderWidth = Math.max(1, (int) (width / getScaleFactor()));
            int newRenderHeight = Math.max(1, (int) (height / getScaleFactor()));

            if (newRenderWidth != renderWidth || newRenderHeight != renderHeight) {
                cleanup();
                updateRenderResolution();
                createRenderResources();
            }
        }
    }

    /**
     * Update upscaling mode (requires reinitialization).
     */
    public void setMode(UpscalingConfig.UpscalingMode mode) {
        if (mode != currentMode) {
            cleanup();
            this.currentMode = mode;
            updateRenderResolution();
            createRenderResources();
            createUpscalePipeline();
        }
    }

    public void setFsrQuality(UpscalingConfig.FsrQuality quality) {
        if (quality != fsrQuality && currentMode == UpscalingConfig.UpscalingMode.FSR) {
            cleanup();
            this.fsrQuality = quality;
            updateRenderResolution();
            createRenderResources();
            createUpscalePipeline();
        }
        this.fsrQuality = quality;
    }

    public void setDlssQuality(UpscalingConfig.DlssQuality quality) {
        if (quality != dlssQuality && currentMode == UpscalingConfig.UpscalingMode.DLSS) {
            cleanup();
            this.dlssQuality = quality;
            updateRenderResolution();
            createRenderResources();
            createUpscalePipeline();
        }
        this.dlssQuality = quality;
    }

    public void setFrameGeneration(boolean enabled) {
        this.frameGenerationEnabled = enabled;
        // Frame generation would require additional SDK integration
    }

    private void cleanup() {
        if (upscalePipeline != null) {
            upscalePipeline.cleanUp();
            upscalePipeline = null;
        }
        if (renderFramebuffer != null) {
            renderFramebuffer.cleanUp(true);
            renderFramebuffer = null;
        }
        if (renderPass != null) {
            renderPass.cleanUp();
            renderPass = null;
        }
        if (renderColorTexture != null) {
            renderColorTexture.close();
            renderColorTexture = null;
        }
        if (renderColorView != null) {
            renderColorView.close();
            renderColorView = null;
        }
        if (renderDepthTexture != null) {
            renderDepthTexture.close();
            renderDepthTexture = null;
        }
        if (renderDepthView != null) {
            renderDepthView.close();
            renderDepthView = null;
        }
    }

    public int getRenderWidth() { return renderWidth; }
    public int getRenderHeight() { return renderHeight; }
    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public UpscalingConfig.UpscalingMode getCurrentMode() { return currentMode; }
    public UpscalingConfig.FsrQuality getFsrQuality() { return fsrQuality; }
    public UpscalingConfig.DlssQuality getDlssQuality() { return dlssQuality; }
    public boolean isFrameGenerationEnabled() { return frameGenerationEnabled; }
    public boolean isUpscalingActive() { 
        return initialized && (currentMode == UpscalingConfig.UpscalingMode.FSR ||
                               currentMode == UpscalingConfig.UpscalingMode.DLSS);
    }
    public GpuTexture getRenderColorTexture() { return renderColorTexture; }
    public GpuTextureView getRenderColorView() { return renderColorView; }
    public Framebuffer getRenderFramebuffer() { return renderFramebuffer; }
    public net.vulkanmod.vulkan.framebuffer.RenderPass getRenderPass() { return renderPass; }

    public void shutdown() {
        cleanup();
        initialized = false;
    }
}