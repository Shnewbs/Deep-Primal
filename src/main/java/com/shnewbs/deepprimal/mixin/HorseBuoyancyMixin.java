package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
abstract class HorseBuoyancyMixin {
  @Inject(method="aiStep", at=@At("RETURN"))
  private void deepprimal$float(CallbackInfo ci) {
    var horse = (AbstractHorse)(Object)this;
    if (RiverConfig.HORSES.get() && horse.isVehicle() && horse.isInWater()) {
      var velocity = horse.getDeltaMovement();
      if (velocity.y < .04) horse.setDeltaMovement(velocity.x, Math.min(.04,velocity.y+.04),velocity.z);
    }
  }
}
