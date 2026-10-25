package net.vulkanmod.deepprimal.upscaling;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.engine.VkRenderPass;
import net.vulkanmod.render.engine.VkCommandEncoder;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.PipelineConfig;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.util.VkResult;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkImage;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageView;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkFormat;
import org.lwjgl.vulkan.VkImageUsageFlags;
import org.lwjgl.vulkan.VkImageAspectFlags;
import org.lwjgl.vulkan.VkImageLayout;
import org.lwjgl.vulkan.VkMemoryPropertyFlags;
import org.lwjgl.vulkan.VkSampleCountFlags;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import java.util.Optional;

/**
 * Upscaling pass for FSR (FidelityFX Super Resolution) and DLSS (Deep Learning Super Sampling).
 * This provides a Vulkan-native implementation that integrates with the Deep Primal renderer.
 */
public final class UpscalingPass {
    private static final String FSR_SHADER_PATH = "deepprimal_upscaling/fsr_upscale";
    private static final String DLSS_SHADER_PATH = "deepprimal_upscaling/dlss_upscale";

    private final VkGpuDevice device;
    private final int renderWidth;
    private final int renderHeight;
    private final int displayWidth;
    private final int displayHeight;

    private GraphicsPipeline upscalingPipeline;
    private VkGpuTexture upscaledColorTexture;
    private GpuTextureView upscaledColorView;
    private Framebuffer upscalingFramebuffer;
    private RenderPass upscalingRenderPass;

    private UpscalingMode currentMode = UpscalingMode.NONE;
    private FsrQuality fsrQuality = FsrQuality.BALANCED;
    private DlssQuality dlssQuality = DlssQuality.BALANCED;
    private boolean frameGenerationEnabled = false;

    // FSR constants from AMD FidelityFX SDK
    private static final float[] FSR_SCALE_FACTORS = {1.3f, 1.5f, 1.7f, 2.0f, 3.0f};
    private static final float[] FSR_RCP_SCALE_FACTORS = {1.0f/1.3f, 1.0f/1.5f, 1.0f/1.7f, 1.0f/2.0f, 1.0f/3.0f};

    // DLSS constants from NVIDIA NGX SDK
    private static final float[] DLSS_SCALE_FACTORS = {1.0f, 3.0f, 2.0f, 1.7f, 1.5f, 1.3f};
    private static final float[] DLSS_RCP_SCALE_FACTORS = {1.0f, 1.0f/3.0f, 1.0f/2.0f, 1.0f/1.7f, 1.0f/1.5f, 1.0f/1.3f};

    public UpscalingPass(int displayWidth, int displayHeight) {
        this.device = VkGpuDevice.current();
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;

        // Calculate render resolution based on default quality
        this.renderWidth = displayWidth;
        this.renderHeight = displayHeight;
    }

    public void initialize(UpscalingMode mode, FsrQuality fsrQuality, DlssQuality dlssQuality, boolean frameGeneration) {
        this.currentMode = mode;
        this.fsrQuality = fsrQuality;
        this.dlssQuality = dlssQuality;
        this.frameGenerationEnabled = frameGeneration;

        if (mode == UpscalingMode.NONE) {
            return;
        }

        // Calculate render resolution based on upscaling quality
        float scaleFactor = getScaleFactor();
        this.renderWidth = Math.max(1, (int) (displayWidth / scaleFactor));
        this.renderHeight = Math.max(1, (int) (displayHeight / scaleFactor));

        createUpscalingResources();
        createUpscalingPipeline();
    }

    private float getScaleFactor() {
        if (currentMode == UpscalingMode.FSR) {
            return FSR_SCALE_FACTORS[fsrQuality.ordinal()];
        } else if (currentMode == UpscalingMode.DLSS) {
            return DLSS_SCALE_FACTORS[dlssQuality.ordinal()];
        }
        return 1.0f;
    }

    private void createUpscalingResources() {
        // Create upscaled color texture (render target at lower resolution)
        VkGpuTexture.ColorAttachmentInfo colorInfo = new VkGpuTexture.ColorAttachmentInfo(
            renderWidth, renderHeight, VkFormat.VK_FORMAT_R16G16B16A16_SFLOAT, 1);
        this.upscaledColorTexture = device.createColorAttachmentTexture(colorInfo);
        this.upscaledColorView = device.createTextureView(upscaledColorTexture);

        // Create depth texture for upscaling pass
        VkGpuTexture.DepthAttachmentInfo depthInfo = new VkGpuTexture.DepthAttachmentInfo(
            renderWidth, renderHeight, VkFormat.VK_FORMAT_D32_SFLOAT, 1);
        VkGpuTexture depthTexture = device.createDepthAttachmentTexture(depthInfo);

        // Create framebuffer for upscaling render pass
        this.upscalingFramebuffer = Framebuffer.builder()
            .width(renderWidth)
            .height(renderHeight)
            .layers(1)
            .withColorAttachment(upscaledColorTexture)
            .withDepthAttachment(depthTexture)
            .build();

        // Create render pass for upscaling
        RenderPass.Builder builder = RenderPass.builder(upscalingFramebuffer);
        builder.getColorAttachmentInfo().setFinalLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        builder.getColorAttachmentInfo().setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_STORE);
        builder.getDepthAttachmentInfo().setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
        this.upscalingRenderPass = builder.build();
    }

    private void createUpscalingPipeline() {
        String shaderPath = currentMode == UpscalingMode.FSR ? FSR_SHADER_PATH : DLSS_SHADER_PATH;

        PipelineConfig config = PipelineConfig.builder()
            .vertexShader(shaderPath + ".vert.spv")
            .fragmentShader(shaderPath + ".frag.spv")
            .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
            .cullMode(VK10.VK_CULL_MODE_NONE)
            .depthTestEnable(false)
            .depthWriteEnable(false)
            .build();

        this.upscalingPipeline = new GraphicsPipeline(config);
    }

    /**
     * Begin the upscaling render pass - renders scene at lower resolution.
     */
    public void beginUpscalingPass(VkCommandEncoder encoder) {
        if (currentMode == UpscalingMode.NONE) {
            return;
        }

        encoder.beginRenderPass(upscalingRenderPass, upscalingFramebuffer);
        encoder.setViewport(0, 0, renderWidth, renderHeight);
        encoder.setScissor(0, 0, renderWidth, renderHeight);
    }

    /**
     * End the upscaling render pass and perform the upscale to display resolution.
     */
    public void endUpscalingPassAndUpscale(VkCommandEncoder encoder, GpuTexture displayColorAttachment) {
        if (currentMode == UpscalingMode.NONE) {
            return;
        }

        encoder.endRenderPass();

        // Transition upscaled texture to shader read
        try (MemoryStack stack = MemoryStack.stackPush()) {
            upscaledColorTexture.transitionImageLayout(stack, encoder.getCommandBuffer(),
                VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }

        // Begin upscale pass to display resolution
        Framebuffer displayFramebuffer = Renderer.getInstance().getSwapChain();
        RenderPass displayRenderPass = Renderer.getInstance().getMainPass().getMainRenderPass(); // Need to expose this

        encoder.beginRenderPass(displayRenderPass, displayFramebuffer);
        encoder.setViewport(0, 0, displayWidth, displayHeight);
        encoder.setScissor(0, 0, displayWidth, displayHeight);

        // Bind upscaling pipeline
        PipelineState state = PipelineState.getCurrentPipelineState(displayRenderPass);
        long pipelineHandle = upscalingPipeline.getHandle(state);
        encoder.bindGraphicsPipeline(pipelineHandle);

        // Bind upscaled texture as input
        encoder.bindDescriptorSet(0, upscaledColorView); // Assuming descriptor set 0 for input texture

        // Push constants for upscaling parameters
        PushConstants pushConstants = upscalingPipeline.getPushConstants();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            float rcpScale = getRcpScaleFactor();
            pushConstants.update(
                stack.mallocFloat(4)
                    .put(0, rcpScale)
                    .put(1, (float) renderWidth)
                    .put(2, (float) renderHeight)
                    .put(3, currentMode == UpscalingMode.FSR ? 0.0f : 1.0f) // mode flag
            );
            encoder.pushConstants(pipelineHandle, pushConstants);
        }

        // Draw fullscreen triangle
        encoder.draw(3, 1, 0, 0);

        encoder.endRenderPass();
    }

    private float getRcpScaleFactor() {
        if (currentMode == UpscalingMode.FSR) {
            return FSR_RCP_SCALE_FACTORS[fsrQuality.ordinal()];
        } else if (currentMode == UpscalingMode.DLSS) {
            return DLSS_RCP_SCALE_FACTORS[dlssQuality.ordinal()];
        }
        return 1.0f;
    }

    public void onResize(int displayWidth, int displayHeight) {
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;

        if (currentMode != UpscalingMode.NONE) {
            float scaleFactor = getScaleFactor();
            int newRenderWidth = Math.max(1, (int) (displayWidth / scaleFactor));
            int newRenderHeight = Math.max(1, (int) (displayHeight / scaleFactor));

            if (newRenderWidth != renderWidth || newRenderHeight != renderHeight) {
                cleanup();
                initialize(currentMode, fsrQuality, dlssQuality, frameGenerationEnabled);
            }
        }
    }

    public void setMode(UpscalingMode mode) {
        if (mode != currentMode) {
            cleanup();
            initialize(mode, fsrQuality, dlssQuality, frameGenerationEnabled);
        }
    }

    public void setFsrQuality(FsrQuality quality) {
        if (quality != fsrQuality && currentMode == UpscalingMode.FSR) {
            cleanup();
            initialize(currentMode, quality, dlssQuality, frameGenerationEnabled);
        }
        this.fsrQuality = quality;
    }

    public void setDlssQuality(DlssQuality quality) {
        if (quality != dlssQuality && currentMode == UpscalingMode.DLSS) {
            cleanup();
            initialize(currentMode, fsrQuality, quality, frameGenerationEnabled);
        }
        this.dlssQuality = quality;
    }

    public void setFrameGeneration(boolean enabled) {
        this.frameGenerationEnabled = enabled;
        // Frame generation would require additional integration with FSR 3 / DLSS 3 SDKs
    }

    public void cleanup() {
        if (upscalingPipeline != null) {
            upscalingPipeline.cleanUp();
            upscalingPipeline = null;
        }
        if (upscalingFramebuffer != null) {
            upscalingFramebuffer.cleanUp(true);
            upscalingFramebuffer = null;
        }
        if (upscalingRenderPass != null) {
            upscalingRenderPass.cleanUp();
            upscalingRenderPass = null;
        }
        if (upscaledColorTexture != null) {
            upscaledColorTexture.close();
            upscaledColorTexture = null;
        }
        if (upscaledColorView != null) {
            upscaledColorView.close();
            upscaledColorView = null;
        }
    }

    public int getRenderWidth() { return renderWidth; }
    public int getRenderHeight() { return renderHeight; }
    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public UpscalingMode getCurrentMode() { return currentMode; }
    public FsrQuality getFsrQuality() { return fsrQuality; }
    public DlssQuality getDlssQuality() { return dlssQuality; }
    public boolean isFrameGenerationEnabled() { return frameGenerationEnabled; }
    public GpuTexture getUpscaledColorTexture() { return upscaledColorTexture; }
    public GpuTextureView getUpscaledColorView() { return upscaledColorView; }
    public Framebuffer getUpscalingFramebuffer() { return upscalingFramebuffer; }
    public RenderPass getUpscalingRenderPass() { return upscalingRenderPass; }

    public enum UpscalingMode {
        NONE,
        FSR,
        DLSS
    }

    public enum FsrQuality {
        ULTRA_QUALITY(0),
        QUALITY(1),
        BALANCED(2),
        PERFORMANCE(3),
        ULTRA_PERFORMANCE(4);

        private final int index;
        FsrQuality(int index) { this.index = index; }
        public int index() { return index; }
    }

    public enum DlssQuality {
        DLAA(0),
        ULTRA_PERFORMANCE(1),
        PERFORMANCE(2),
        BALANCED(3),
        QUALITY(4),
        ULTRA_QUALITY(5);

        private final int index;
        DlssQuality(int index) { this.index = index; }
        public int index() { return index; }
    }
}