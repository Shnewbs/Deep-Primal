package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.core.EngineBoatBuoyancy;
import com.shnewbs.deepprimal.core.WaterDynamics;
import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Small buoyancy delta only: downstream propulsion remains the native fluid current. */
@Mixin(AbstractBoat.class)
abstract class OceanBoatMixin {
  @Inject(method = "getStatus", at = @At("RETURN"), cancellable = true)
  private void deepprimal$floatInEngineWater(
      CallbackInfoReturnable<AbstractBoat.Status> ci) {
    var boat = (AbstractBoat) (Object) this;
    var status = ci.getReturnValue();
    boolean underwater = status == AbstractBoat.Status.UNDER_WATER;
    boolean underFlowingWater = status == AbstractBoat.Status.UNDER_FLOWING_WATER;
    if ((underwater || underFlowingWater)
        && EngineBoatBuoyancy.shouldUseSurfaceBuoyancy(
            deepprimal$underEngineWater(boat), underwater, underFlowingWater))
      ci.setReturnValue(AbstractBoat.Status.IN_WATER);
  }

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

  private static boolean deepprimal$underEngineWater(AbstractBoat boat) {
    AABB box = boat.getBoundingBox();
    int x0 = Mth.floor(box.minX), x1 = Mth.ceil(box.maxX);
    int y0 = Mth.floor(box.maxY), y1 = Mth.ceil(box.maxY + .001);
    int z0 = Mth.floor(box.minZ), z1 = Mth.ceil(box.maxZ);
    var pos = new BlockPos.MutableBlockPos();
    for (int x = x0; x < x1; x++)
      for (int y = y0; y < y1; y++)
        for (int z = z0; z < z1; z++) {
          pos.set(x, y, z);
          if (boat.level().hasChunkAt(pos)
              && boat.level().getFluidState(pos).getType() instanceof EngineFluid) return true;
        }
    return false;
  }
}
