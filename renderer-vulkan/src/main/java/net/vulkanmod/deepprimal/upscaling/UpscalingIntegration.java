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

/**
 * Integration layer between upscaling system and Vulkan renderer.
 * Handles render pass modification and frame submission.
 */
public final class UpscalingIntegration {
    private static UpscalingIntegration INSTANCE;

    private final UpscalingManager upscalingManager;
    private boolean enabled = false;
    private int lastDisplayWidth = 0;
    private int lastDisplayHeight = 0;

    private UpscalingIntegration() {
        this.upscalingManager = UpscalingManager.getInstance();
    }

    public static synchronized UpscalingIntegration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UpscalingIntegration();
        }
        return INSTANCE;
    }

    /**
     * Initialize upscaling integration.
     * Call during renderer initialization after device creation.
     */
    public void initialize() {
        if (RendererPreferences.requestedBackend() != RendererPreferences.Backend.Vulkan) {
            return;
        }

        UpscalingConfig.UpscalingMode mode = UpscalingConfig.getActiveMode();
        if (mode == null) {
            return;
        }

        this.enabled = true;
        upscalingManager.initialize();
    }

    /**
     * Called at the start of frame rendering.
     * Modifies the render target to use scaled resolution.
     */
    public void onFrameBegin(VkCommandEncoder encoder, int displayWidth, int displayHeight) {
        if (!enabled || !upscalingManager.isUpscalingActive()) {
            return;
        }

        // Check for resize
        if (displayWidth != lastDisplayWidth || displayHeight != lastDisplayHeight) {
            lastDisplayWidth = displayWidth;
            lastDisplayHeight = displayHeight;
            upscalingManager.onResize(displayWidth, displayHeight);
        }

        // Begin scaled render pass
        upscalingManager.beginScaledRender(encoder);
    }

    /**
     * Called at the end of frame rendering.
     * Performs upscale to display resolution.
     */
    public void onFrameEnd(VkCommandEncoder encoder) {
        if (!enabled || !upscalingManager.isUpscalingActive()) {
            return;
        }

        upscalingManager.endScaledRenderAndUpscale(encoder);
    }

    /**
     * Get the render dimensions (may be smaller than display if upscaling active).
     */
    public int getRenderWidth() {
        return enabled && upscalingManager.isUpscalingActive() 
            ? upscalingManager.getRenderWidth() 
            : lastDisplayWidth;
    }

    public int getRenderHeight() {
        return enabled && upscalingManager.isUpscalingActive()
            ? upscalingManager.getRenderHeight()
            : lastDisplayHeight;
    }

    /**
     * Check if upscaling is currently active.
     */
    public boolean isActive() {
        return enabled && upscalingManager.isUpscalingActive();
    }

    /**
     * Get current upscaling mode.
     */
    public UpscalingConfig.UpscalingMode getCurrentMode() {
        return upscalingManager.getCurrentMode();
    }

    /**
     * Get current quality setting.
     */
    public Object getCurrentQuality() {
        if (upscalingManager.getCurrentMode() == UpscalingConfig.UpscalingMode.FSR) {
            return upscalingManager.getFsrQuality();
        } else {
            return upscalingManager.getDlssQuality();
        }
    }

    /**
     * Change upscaling mode at runtime (requires frame rebuild).
     */
    public void setMode(UpscalingConfig.UpscalingMode mode) {
        upscalingManager.setMode(mode);
    }

    /**
     * Change quality at runtime.
     */
    public void setQuality(int qualityIndex) {
        if (upscalingManager.getCurrentMode() == UpscalingConfig.UpscalingMode.FSR) {
            upscalingManager.setFsrQuality(UpscalingConfig.FsrQuality.fromIndex(qualityIndex));
        } else {
            upscalingManager.setDlssQuality(UpscalingConfig.DlssQuality.fromIndex(qualityIndex));
        }
    }

    /**
     * Enable/disable frame generation.
     */
    public void setFrameGeneration(boolean enabled) {
        upscalingManager.setFrameGeneration(enabled);
    }

    /**
     * Shutdown upscaling integration.
     */
    public void shutdown() {
        upscalingManager.shutdown();
        enabled = false;
    }
}