package net.vulkanmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Entity previews, maps and oversized items sample Vulkan offscreen images top to bottom. */
@Mixin(PictureInPictureRenderer.class)
public abstract class GuiPictureOriginMixin {
  @ModifyArgs(
      method = "blitTexture",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"))
  private void deepprimal$pictureOrigin(Args args) {
    if (!"Vulkan".equals(RenderSystem.getDevice().getBackendName())) return;
    args.set(9, 0.0F);
    args.set(10, 1.0F);
  }
}
