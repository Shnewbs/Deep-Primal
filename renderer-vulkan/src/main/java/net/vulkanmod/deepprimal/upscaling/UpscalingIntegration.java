package net.vulkanmod.deepprimal.upscaling;
import com.mojang.blaze3d.systems.RenderSystem;
/** Legacy entry point. Scene scaling is now owned by the common graphics module. */
public final class UpscalingIntegration {
  public static int renderDimension(int display){RenderSystem.assertOnRenderThread();return display;}
  private UpscalingIntegration(){}
}

