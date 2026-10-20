package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class SwimmingMixin {
  @Inject(method="dismountsUnderwater", at=@At("HEAD"), cancellable=true)
  private void deepprimal$horse(CallbackInfoReturnable<Boolean> ci) {
    if ((Object)this instanceof AbstractHorse && RiverConfig.HORSES.get()) ci.setReturnValue(false);
  }
}
