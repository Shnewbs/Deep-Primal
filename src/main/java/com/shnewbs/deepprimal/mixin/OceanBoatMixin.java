package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.core.WaterDynamics;
import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Small buoyancy delta only: downstream propulsion remains the native fluid current. */
@Mixin(AbstractBoat.class)
abstract class OceanBoatMixin {
  @Inject(method = "floatBoat", at = @At("RETURN"))
  private void deepprimal$swell(CallbackInfo ci) {
    var boat = (AbstractBoat) (Object) this;
    if (!RiverConfig.OCEAN_MOTION.get()
        || !boat.isInWater()
        || !boat.level().getBiome(boat.blockPosition()).is(BiomeTags.IS_OCEAN)) return;
    double time = boat.level().getGameTime(), storm = boat.level().getRainLevel(1);
    double delta = 0;
    for (int corner = 0; corner < 4; corner++) {
      double x = boat.getX() + ((corner & 1) == 0 ? -.6 : .6);
      double z = boat.getZ() + ((corner & 2) == 0 ? -.6 : .6);
      delta +=
          (WaterDynamics.wave(x, z, time + 1, storm) - WaterDynamics.wave(x, z, time, storm)) * .1;
    }
    boat.setDeltaMovement(boat.getDeltaMovement().add(0, delta, 0));
  }
}
