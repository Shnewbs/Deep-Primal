package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.worldgen.ColumnSurface;
import com.shnewbs.deepprimal.worldgen.SurfaceWaterAquifer;
import com.shnewbs.deepprimal.worldgen.WaterwayHeight;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Constructor-only selection: legacy settings keep their original aquifer instance. */
@Mixin(NoiseChunk.class)
abstract class WaterwayAquiferMixin {
  @Shadow @Final @Mutable private Aquifer aquifer;
  @Shadow @Final private DensityFunction preliminarySurfaceLevel;

  @Inject(method = "<init>", at = @At("RETURN"))
  private void deepprimal$surfaceWater(
      int cells,
      RandomState state,
      int x,
      int z,
      NoiseSettings noise,
      DensityFunctions.BeardifierOrMarker beardifier,
      NoiseGeneratorSettings settings,
      Aquifer.FluidPicker fluid,
      Blender blender,
      CallbackInfo ci) {
    if (preliminarySurfaceLevel instanceof ColumnSurface column && column.surfaceWater()) {
      WaterwayHeight[] routed = {null};
      state
          .router()
          .preliminarySurfaceLevel()
          .mapAll(
              f -> {
                if (f instanceof WaterwayHeight h && h.routed()) routed[0] = h;
                return f;
              });
      aquifer =
          new SurfaceWaterAquifer(
              aquifer, (NoiseChunk) (Object) this, settings.seaLevel(), routed[0], x, z);
    }
  }
}
