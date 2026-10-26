package net.vulkanmod.mixin;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.vulkanmod.deepprimal.upscaling.UpscalingIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(RenderTarget.class)
public abstract class MainTargetResizeMixin {
  @ModifyVariable(method="resize",at=@At("HEAD"),argsOnly=true,ordinal=0)
  private int deepprimal$width(int width){return (Object)this instanceof MainTarget?UpscalingIntegration.renderDimension(width):width;}
  @ModifyVariable(method="resize",at=@At("HEAD"),argsOnly=true,ordinal=1)
  private int deepprimal$height(int height){return (Object)this instanceof MainTarget?UpscalingIntegration.renderDimension(height):height;}
}
