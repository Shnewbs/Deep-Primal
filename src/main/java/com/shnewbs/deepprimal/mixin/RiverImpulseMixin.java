package com.shnewbs.deepprimal.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.shnewbs.deepprimal.water.RiverConfig;
import com.shnewbs.deepprimal.water.RiverWaterBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Native non-player normalization erases river speed. Preserve the same sampled mean as players. */
@Mixin(targets="net.minecraft.world.entity.EntityFluidInteraction$Tracker")
abstract class RiverImpulseMixin {
  @Shadow private Vec3 accumulatedCurrent;
  @Shadow private int currentCount;

  @ModifyExpressionValue(method="applyCurrentTo", at=@At(value="INVOKE",
      target="Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;", ordinal=0))
  private Vec3 deepprimal$speed(Vec3 normalized, @Local(argsOnly=true) Entity entity) {
    if (currentCount > 0 && RiverConfig.CURRENTS.get()
        && entity.level().getBlockState(entity.blockPosition()).getBlock() instanceof RiverWaterBlock)
      return accumulatedCurrent.scale(1.0/currentCount);
    return normalized;
  }
}
