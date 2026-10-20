package com.shnewbs.deepprimal.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.WaterDynamics;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Versioned offshore relief. Existing saved generators never acquire this node implicitly. */
public record OceanHeight(
    DensityFunction terrain, DensityFunction continents, DensityFunction trenches, int floor)
    implements DensityFunction.SimpleFunction {
  public static final MapCodec<OceanHeight> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("terrain")
                          .forGetter(OceanHeight::terrain),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("continents")
                          .forGetter(OceanHeight::continents),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("trenches")
                          .forGetter(OceanHeight::trenches),
                      Codec.intRange(-4096, 0).fieldOf("floor").forGetter(OceanHeight::floor))
                  .apply(i, OceanHeight::new));
  private static final KeyDispatchDataCodec<OceanHeight> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

  public double compute(FunctionContext p) {
    return WaterDynamics.oceanHeight(
        terrain.compute(p), continents.compute(p), trenches.compute(p), floor);
  }

  public DensityFunction mapAll(Visitor v) {
    return v.apply(
        new OceanHeight(terrain.mapAll(v), continents.mapAll(v), trenches.mapAll(v), floor));
  }

  public double minValue() {
    return floor + 40;
  }

  public double maxValue() {
    return Math.max(floor + 40, terrain.maxValue());
  }

  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC;
  }
}
