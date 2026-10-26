package net.vulkanmod.mixin;
import com.mojang.blaze3d.pipeline.MainTarget;
import net.vulkanmod.deepprimal.upscaling.UpscalingIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(MainTarget.class)
public abstract class MainTargetScaleMixin {
  @ModifyVariable(method="createFrameBuffer",at=@At("HEAD"),argsOnly=true,ordinal=0)
  private int deepprimal$width(int width){return UpscalingIntegration.renderDimension(width);}
  @ModifyVariable(method="createFrameBuffer",at=@At("HEAD"),argsOnly=true,ordinal=1)
  private int deepprimal$height(int height){return UpscalingIntegration.renderDimension(height);}
}
