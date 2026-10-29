package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import java.util.Objects;

/**
 * Opt-in bridge from a caller-supplied, saved V7 terrain profile to V8 ecotone
 * preflight. It does not register V8 or reinterpret existing V8 snapshots.
 *
 * <p>Its hydrological exclusion mask comes from the actual saved V7 continental
 * and watershed algorithms. It conservatively protects still lakes as well as
 * moving rivers and oceans: the surface-material planner may only paint LAND.
 * V8 geometry can differ from V7, so this is a reference input for new-world
 * experiments, not an accepted V8 water/ocean mask or a native migration.
 */
public final class V8SavedClimateSource implements V8EcotoneColumnPlan.ClimateSource {
  public static final int REFERENCE_TERRAIN_REVISION = 7;
  private static final int MIN_CHANNEL_REVISION = 0;
  private static final int MAX_CHANNEL_REVISION = 5;
  private static final double PATCH_SCALE = 1100.0;
  private static final long PATCH_SALT = 3130L;

  private final TerrainEngine reference;
  private final TerrainOptions savedOptions;
  private final int savedChannelRevision;

  private V8SavedClimateSource(long seed, TerrainOptions savedOptions, int channelRevision) {
    this.savedOptions = savedOptions;
    this.savedChannelRevision = channelRevision;
    // This engine is privately owned; do not use a mutable live-world engine or
    // inherit someone else's cached watershed state during parallel generation.
    this.reference = new TerrainEngine(seed, REFERENCE_TERRAIN_REVISION,
        savedOptions, channelRevision);
  }

  /**
   * The two inputs MUST be read from the same saved world's generation settings.
   * In particular, never use TerrainConfig.creationProfile() on an existing save.
   * A separate native gate must bind a recorded V8-specific climate/ocean policy
   * before a V8 codec is registered; the V7 reference remains explicit-call-only.
   */
  public static V8SavedClimateSource fromSavedV7(V8GeneratorSnapshot v8,
      TerrainProfile savedV7Profile, long savedWorldSeed,
      int savedTerrainRevision, int savedChannelRevision) {
    Objects.requireNonNull(v8, "V8 snapshot");
    Objects.requireNonNull(savedV7Profile, "saved V7 profile");
    if (v8.version() != V8GeneratorSnapshot.SURFACE_VERSION
        || !V8SurfacePolicy.FROZEN_R1.equals(v8.surfacePolicy()))
      throw new IllegalArgumentException("V8 revision-3 frozen surface policy required");
    if (v8.seed() != savedWorldSeed)
      throw new IllegalArgumentException("saved V7 and V8 world seeds differ");
    if (savedTerrainRevision != REFERENCE_TERRAIN_REVISION)
      throw new IllegalArgumentException("reference climate requires saved V7 geography");
    if (savedChannelRevision < MIN_CHANNEL_REVISION
        || savedChannelRevision > MAX_CHANNEL_REVISION)
      throw new IllegalArgumentException("invalid saved V7 channel revision");
    return new V8SavedClimateSource(v8.seed(),
        Objects.requireNonNull(savedV7Profile.options(), "saved terrain options"),
        savedChannelRevision);
  }

  /** Reopen only the V8-r4 climate/reference binding stored in the V8 snapshot. */
  public static V8SavedClimateSource fromSavedV8(V8GeneratorSnapshot snapshot,
      long savedWorldSeed) {
    Objects.requireNonNull(snapshot, "V8 snapshot");
    if ((snapshot.version() != V8GeneratorSnapshot.CLIMATE_VERSION
        && snapshot.version() != V8GeneratorSnapshot.HYDROLOGY_VERSION
        && snapshot.version() != V8GeneratorSnapshot.VEGETATION_VERSION)
        || !V8SurfacePolicy.FROZEN_R1.equals(snapshot.surfacePolicy()))
      throw new IllegalArgumentException("explicit frozen V8 revision-4 climate binding required");
    if (snapshot.seed() != savedWorldSeed)
      throw new IllegalArgumentException("saved world seed differs from V8 climate binding");
    var binding = Objects.requireNonNull(snapshot.climateBinding(), "climate binding");
    return new V8SavedClimateSource(snapshot.seed(), binding.savedOptions(),
        binding.channelRevision());
  }

  public TerrainOptions savedOptions() { return savedOptions; }
  public int savedChannelRevision() { return savedChannelRevision; }

  /**
   * Complete one immutable climate+domain observation before returning it to
   * V8EcotoneColumnPlan. The reference's bounded caches are internally mutable;
   * serializing access avoids races when callers share this adapter. For high
   * throughput, use a distinct adapter per independent planning worker.
   */
  @Override
  public synchronized V8EcotoneColumnPlan.ClimateDatum sample(
      int worldX, int topY, int worldZ) {
    double x = worldX, z = worldZ;
    TerrainEngine.Sample water = reference.sample(x, z);
    boolean ocean = reference.ocean(x, z);
    EcotoneBlend.Domain domain = ocean ? EcotoneBlend.Domain.OCEAN
        : water.wet() ? EcotoneBlend.Domain.RIVER : EcotoneBlend.Domain.LAND;

    // The V8 planned surface altitude is authoritative for altitude/snow
    // thresholds, while these continuous climate fields and water exclusions
    // use the pinned reference terrain algorithm at absolute coordinates.
    var climate = new EcotoneBlend.Climate(topY,
        reference.temperature(x, z, topY), water.rain(), water.slope(),
        reference.noise.field(x, z, PATCH_SCALE, PATCH_SALT));
    return new V8EcotoneColumnPlan.ClimateDatum(climate, domain);
  }
}
