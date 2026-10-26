package net.vulkanmod.deepprimal.upscaling;
import com.mojang.blaze3d.systems.RenderSystem;
/** Called only from the Vulkan renderer's main-target allocation hooks. */
public final class UpscalingIntegration {
  public static int renderDimension(int display){RenderSystem.assertOnRenderThread();return UpscalingManager.getInstance().renderDimension(display);}
  private UpscalingIntegration(){}
}
