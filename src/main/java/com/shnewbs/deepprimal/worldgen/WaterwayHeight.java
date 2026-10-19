package com.shnewbs.deepprimal.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.MountainDrainage;
import com.shnewbs.deepprimal.core.WaterwayShape;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Seeded platform adapter. The enclosing flat_cache retains the native 5x5 column grid. */
public final class WaterwayHeight implements DensityFunction.SimpleFunction {
  private final DensityFunction terrain, continents, ridges, erosion, detail, tributaries;
  private final int seaLevel;
  private final boolean routed;
  private final MountainDrainage drainage;

  public WaterwayHeight(
      DensityFunction terrain,
      DensityFunction continents,
      DensityFunction ridges,
      DensityFunction erosion,
      DensityFunction detail,
      DensityFunction tributaries,
      int seaLevel,
      boolean routed) {
    this(terrain, continents, ridges, erosion, detail, tributaries, seaLevel, routed, null);
  }

  private WaterwayHeight(
      DensityFunction terrain,
      DensityFunction continents,
      DensityFunction ridges,
      DensityFunction erosion,
      DensityFunction detail,
      DensityFunction tributaries,
      int seaLevel,
      boolean routed,
      MountainDrainage drainage) {
    this.terrain = terrain;
    this.continents = continents;
    this.ridges = ridges;
    this.erosion = erosion;
    this.detail = detail;
    this.tributaries = tributaries;
    this.seaLevel = seaLevel;
    this.routed = routed;
    this.drainage = drainage;
  }

  public DensityFunction terrain() {
    return terrain;
  }

  public DensityFunction continents() {
    return continents;
  }

  public DensityFunction ridges() {
    return ridges;
  }

  public DensityFunction erosion() {
    return erosion;
  }

  public DensityFunction detail() {
    return detail;
  }

  public DensityFunction tributaries() {
    return tributaries;
  }

  public int seaLevel() {
    return seaLevel;
  }

  public boolean routed() {
    return routed;
  }

  public static final MapCodec<WaterwayHeight> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("terrain_height")
                          .forGetter(WaterwayHeight::terrain),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("continents")
                          .forGetter(WaterwayHeight::continents),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("ridges")
                          .forGetter(WaterwayHeight::ridges),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("erosion")
                          .forGetter(WaterwayHeight::erosion),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("detail")
                          .forGetter(WaterwayHeight::detail),
                      DensityFunction.HOLDER_HELPER_CODEC
                          .fieldOf("tributaries")
                          .forGetter(WaterwayHeight::tributaries),
                      Codec.intRange(-4096, 4096)
                          .fieldOf("sea_level")
                          .forGetter(WaterwayHeight::seaLevel),
                      Codec.BOOL.optionalFieldOf("routed", false).forGetter(WaterwayHeight::routed))
                  .apply(instance, WaterwayHeight::new));
  private static final KeyDispatchDataCodec<WaterwayHeight> CODEC =
      KeyDispatchDataCodec.of(MAP_CODEC);

  @Override
  public double compute(FunctionContext context) {
    if (routed) return sample(context.blockX(), context.blockZ()).height();
    return WaterwayShape.height(
        terrain.compute(context),
        continents.compute(context),
        ridges.compute(context),
        erosion.compute(context),
        detail.compute(context),
        tributaries.compute(context),
        seaLevel);
  }

  private double base(FunctionContext p) {
    double c = continents.compute(p);
    double coast = WaterwayShape.coastHeight(terrain.compute(p), c, erosion.compute(p), seaLevel);
    return WaterwayShape.riverHeight(
        coast, c, ridges.compute(p), seaLevel - 7 + 1.5 * Math.clamp(detail.compute(p), -1, 1));
  }

  public MountainDrainage.Sample sample(int x, int z) {
    if (drainage == null)
      throw new IllegalStateException("Routed terrain must be seeded before evaluation");
    var p = new DensityFunction.SinglePointContext(x, 0, z);
    return drainage.sample(x, z, base(p));
  }

  public MountainDrainage drainage() {
    return drainage;
  }

  private static boolean seeded(DensityFunction input) {
    boolean[] resolved = {true}, seen = {false};
    input.mapAll(
        new DensityFunction.Visitor() {
          public DensityFunction apply(DensityFunction f) {
            return f;
          }

          public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noise) {
            seen[0] = true;
            if (noise.noise() == null) resolved[0] = false;
            return noise;
          }
        });
    return seen[0] && resolved[0];
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    var mapped =
        new WaterwayHeight(
            terrain.mapAll(visitor),
            continents.mapAll(visitor),
            ridges.mapAll(visitor),
            erosion.mapAll(visitor),
            detail.mapAll(visitor),
            tributaries.mapAll(visitor),
            seaLevel,
            routed,
            drainage);
    if (routed
        && drainage == null
        && seeded(mapped.terrain)
        && seeded(mapped.continents)
        && seeded(mapped.ridges)
        && seeded(mapped.erosion)
        && seeded(mapped.detail)) {
      var raw = mapped;
      var network =
          new MountainDrainage(
              (x, z) -> raw.base(new DensityFunction.SinglePointContext((int) x, 0, (int) z)),
              seaLevel);
      mapped =
          new WaterwayHeight(
              mapped.terrain,
              mapped.continents,
              mapped.ridges,
              mapped.erosion,
              mapped.detail,
              mapped.tributaries,
              seaLevel,
              true,
              network);
    }
    return visitor.apply(mapped);
  }

  // Native visitors intern structurally equal density nodes. The cache is a
  // derived runtime detail and must not split otherwise identical seeded fields.
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof WaterwayHeight h
        && seaLevel == h.seaLevel
        && routed == h.routed
        && terrain.equals(h.terrain)
        && continents.equals(h.continents)
        && ridges.equals(h.ridges)
        && erosion.equals(h.erosion)
        && detail.equals(h.detail)
        && tributaries.equals(h.tributaries);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        terrain, continents, ridges, erosion, detail, tributaries, seaLevel, routed);
  }

  @Override
  public double minValue() {
    return Math.min(terrain.minValue(), seaLevel - 14.0);
  }

  @Override
  public double maxValue() {
    return Math.max(terrain.maxValue(), seaLevel + 23.0);
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC;
  }
}
