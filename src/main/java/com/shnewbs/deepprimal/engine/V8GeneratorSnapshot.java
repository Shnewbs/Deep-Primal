package com.shnewbs.deepprimal.engine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable V8 generator snapshot for explicit preview/save-profile integration.
 * Not registered as a chunk-generator codec; V1-V7 saves and defaults are unchanged.
 * A signed decimal string keeps the complete 64-bit generation seed across JSON.
 */
public record V8GeneratorSnapshot(
    int version, long seed, int minY, int height,
    DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blendPolicy,
    V8SurfacePolicy surfacePolicy, V8ClimateBinding climateBinding,
    V8HydrologyBinding hydrologyBinding, V8VegetationPolicy vegetationPolicy) {
  /** Frozen six-field legacy V8 snapshot format. Never rewrite these existing saves. */
  public static final int VERSION = 1;
  /** New explicit seven-field snapshot format; no silent upgrade of previous saves. */
  public static final int BLENDED_VERSION = 2;
  /** New eight-field revision pins surface cover and climate-input provenance. */
  public static final int SURFACE_VERSION = 3;
  /** Explicit reference-climate and hydrology identity for new V8 snapshots. */
  public static final int CLIMATE_VERSION = 4;
  /** Explicit ten-field saved hydrology observation provenance. No native routing activation. */
  public static final int HYDROLOGY_VERSION = 5;
  /** Explicit r6 freezes new vegetation placement. All earlier saved snapshots remain intact. */
  public static final int VEGETATION_VERSION = 6;

  /** Retain the ten-argument revision-5 source API. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground,
      V8BlendPolicy blendPolicy, V8SurfacePolicy surfacePolicy,
      V8ClimateBinding climateBinding, V8HydrologyBinding hydrologyBinding) {
    this(version, seed, minY, height, density, ground, blendPolicy, surfacePolicy,
        climateBinding, hydrologyBinding, null);
  }

  /** Retain the exact nine-argument revision-4 constructor and wire shape. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground,
      V8BlendPolicy blendPolicy, V8SurfacePolicy surfacePolicy,
      V8ClimateBinding climateBinding) {
    this(version, seed, minY, height, density, ground, blendPolicy,
        surfacePolicy, climateBinding, null);
  }

  /** Preserve every existing eight-argument revision-3 constructor and callsite. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground,
      V8BlendPolicy blendPolicy, V8SurfacePolicy surfacePolicy) {
    this(version, seed, minY, height, density, ground, blendPolicy, surfacePolicy, null);
  }

  /** Retain source/API compatibility with every existing six-argument constructor. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground) {
    this(requireLegacyVersion(version), seed, minY, height, density, ground, V8BlendPolicy.HARD, null);
  }

  private static int requireLegacyVersion(int version) {
    if (version != VERSION)
      throw new IllegalArgumentException("six-argument V8 constructor is legacy-only");
    return version;
  }

  /** Preserve the exact seven-argument revision-2 source API and wire shape. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blendPolicy) {
    this(requireBlendVersion(version), seed, minY, height, density, ground, blendPolicy, null);
  }

  private static int requireBlendVersion(int version) {
    if (version != BLENDED_VERSION)
      throw new IllegalArgumentException("seven-argument V8 constructor is revision-2-only");
    return version;
  }

  /**
   * Explicitly select a revision-2 blend policy from a legacy/revision-2 profile.
   * Revision 3 freezes surface provenance and revision 4 also freezes climate;
   * converting either through this API would silently discard saved fields.
   * Do not rewrite existing saved profiles or reinterpret newer revisions.
   */
  public V8GeneratorSnapshot withBlendPolicy(V8BlendPolicy policy) {
    if (version != VERSION && version != BLENDED_VERSION)
      throw new IllegalStateException(
          "blend policy may only be selected from V8 revision 1 or 2; "
          + "later saved revisions cannot be downgraded");
    return new V8GeneratorSnapshot(BLENDED_VERSION, seed, minY, height,
        density, ground, Objects.requireNonNull(policy, "policy"));
  }

  /** Upgrade an explicit rev2 preview without changing its saved output in-place. */
  public V8GeneratorSnapshot withSurfacePolicy(V8SurfacePolicy policy) {
    if (version != BLENDED_VERSION)
      throw new IllegalStateException("surface policy may only be selected from V8 revision 2");
    return new V8GeneratorSnapshot(SURFACE_VERSION, seed, minY, height,
        density, ground, blendPolicy, Objects.requireNonNull(policy, "policy"));
  }

  /**
   * Create a NEW, explicit V8 revision-3 authoring profile with the relief-2
   * density recipe frozen into its saved graph. Never upgrade an existing
   * snapshot implicitly: its exact serialized density nodes remain binding.
   * This does not register V8 as the active Minecraft generator.
   */
  public static V8GeneratorSnapshot newReliefV2(long seed, int minY, int height,
      GroundSurfaceProfile ground, V8BlendPolicy blendPolicy,
      V8SurfacePolicy surfacePolicy, boolean floatingFeatures) {
    return new V8GeneratorSnapshot(SURFACE_VERSION, seed, minY, height,
        TerrainShapeV8.definitionReliefV2(floatingFeatures),
        Objects.requireNonNull(ground, "ground"),
        Objects.requireNonNull(blendPolicy, "blendPolicy"),
        Objects.requireNonNull(surfacePolicy, "surfacePolicy"));
  }

  /**
   * New opt-in V8 recipe with an explicit coastal sea-plane guard. Revision-3
   * snapshot envelopes already persist every graph node, so this does not
   * rewrite V8 relief V2 or legacy saves. Native activation still requires
   * ocean-mask, routed water, border and saved-world compatibility evidence.
   */
  public static V8GeneratorSnapshot newReliefV3(long seed, int minY, int height,
      GroundSurfaceProfile ground, V8BlendPolicy blendPolicy,
      V8SurfacePolicy surfacePolicy, boolean floatingFeatures) {
    return new V8GeneratorSnapshot(SURFACE_VERSION, seed, minY, height,
        TerrainShapeV8.definitionReliefV3(floatingFeatures),
        Objects.requireNonNull(ground, "ground"),
        Objects.requireNonNull(blendPolicy, "blendPolicy"),
        Objects.requireNonNull(surfacePolicy, "surfacePolicy"));
  }

  /**
   * Explicit new-world authoring selection for the coastal-height-safe V8
   * relief V4 graph. Existing revisions and native generator registration are
   * unchanged; the snapshot persists the entire selected graph verbatim.
   */
  public static V8GeneratorSnapshot newReliefV4(long seed, int minY, int height,
      GroundSurfaceProfile ground, V8BlendPolicy blendPolicy,
      V8SurfacePolicy surfacePolicy, boolean floatingFeatures) {
    return new V8GeneratorSnapshot(SURFACE_VERSION, seed, minY, height,
        TerrainShapeV8.definitionReliefV4(floatingFeatures),
        Objects.requireNonNull(ground, "ground"),
        Objects.requireNonNull(blendPolicy, "blendPolicy"),
        Objects.requireNonNull(surfacePolicy, "surfacePolicy"));
  }

  /** New explicit revision, never an in-place reinterpretation of existing r3 saves. */
  public V8GeneratorSnapshot withClimateBinding(V8ClimateBinding binding) {
    if (version != SURFACE_VERSION)
      throw new IllegalStateException("climate binding may only be selected from V8 revision 3");
    return new V8GeneratorSnapshot(CLIMATE_VERSION, seed, minY, height,
        density, ground, blendPolicy, surfacePolicy,
        Objects.requireNonNull(binding, "binding"));
  }

  /** A new opt-in saved revision; never overwrite V8-r4 or alter earlier graph bytes. */
  public V8GeneratorSnapshot withHydrologyBinding(V8HydrologyBinding binding) {
    if (version != CLIMATE_VERSION)
      throw new IllegalStateException("hydrology binding may only be selected from V8 revision 4");
    return new V8GeneratorSnapshot(HYDROLOGY_VERSION, seed, minY, height,
        density, ground, blendPolicy, surfacePolicy, climateBinding,
        Objects.requireNonNull(binding, "binding"));
  }

  /** Explicit new authoring only: refuse silent migration of already saved worlds. */
  public V8GeneratorSnapshot withVegetationPolicy(V8VegetationPolicy policy) {
    if (version != HYDROLOGY_VERSION)
      throw new IllegalStateException("V8 vegetation may only be authored from revision 5");
    return new V8GeneratorSnapshot(VEGETATION_VERSION, seed, minY, height,
        density, ground, blendPolicy, surfacePolicy, climateBinding,
        hydrologyBinding, Objects.requireNonNull(policy, "vegetation policy"));
  }

  private static final Codec<Long> SEED = Codec.STRING.comapFlatMap(text -> {
    if (!text.matches("0|-?[1-9][0-9]*"))
      return DataResult.error(() -> "seed must be a canonical signed 64-bit decimal string");
    try {
      return DataResult.success(Long.parseLong(text));
    } catch (NumberFormatException failure) {
      return DataResult.error(() -> "seed must fit signed 64-bit integer range");
    }
  }, value -> Long.toString(value));

  private record WireLegacy(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground) {}
  private record WireBlend(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blend) {}

  private record WireSurface(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blend,
      V8SurfacePolicy surface) {}

  private record WireClimate(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blend,
      V8SurfacePolicy surface, V8ClimateBinding climateBinding) {}

  private record WireHydrology(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blend,
      V8SurfacePolicy surface, V8ClimateBinding climateBinding,
      V8HydrologyBinding hydrologyBinding) {}

  private record WireVegetation(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blend,
      V8SurfacePolicy surface, V8ClimateBinding climateBinding,
      V8HydrologyBinding hydrologyBinding, V8VegetationPolicy vegetationPolicy) {}

  /** Keep the six-field legacy codec and exact field order intact. */
  private static final Codec<WireLegacy> LEGACY_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireLegacy::version),
      SEED.fieldOf("seed").forGetter(WireLegacy::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireLegacy::minY),
      Codec.INT.fieldOf("height").forGetter(WireLegacy::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireLegacy::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireLegacy::ground)
  ).apply(i, WireLegacy::new));

  /** A seventh required field is legal only for explicitly selected V8 revision 2. */
  private static final Codec<WireBlend> BLEND_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireBlend::version),
      SEED.fieldOf("seed").forGetter(WireBlend::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireBlend::minY),
      Codec.INT.fieldOf("height").forGetter(WireBlend::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireBlend::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireBlend::ground),
      V8BlendPolicy.CODEC.fieldOf("blend").forGetter(WireBlend::blend)
  ).apply(i, WireBlend::new));

  /** Every saved surface policy field is mandatory, revisioned independently of climate widths. */
  private static final Codec<WireSurface> SURFACE_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireSurface::version),
      SEED.fieldOf("seed").forGetter(WireSurface::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireSurface::minY),
      Codec.INT.fieldOf("height").forGetter(WireSurface::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireSurface::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireSurface::ground),
      V8BlendPolicy.CODEC.fieldOf("blend").forGetter(WireSurface::blend),
      V8SurfacePolicy.CODEC.fieldOf("surface").forGetter(WireSurface::surface)
  ).apply(i, WireSurface::new));

  private static final Codec<WireClimate> CLIMATE_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireClimate::version),
      SEED.fieldOf("seed").forGetter(WireClimate::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireClimate::minY),
      Codec.INT.fieldOf("height").forGetter(WireClimate::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireClimate::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireClimate::ground),
      V8BlendPolicy.CODEC.fieldOf("blend").forGetter(WireClimate::blend),
      V8SurfacePolicy.CODEC.fieldOf("surface").forGetter(WireClimate::surface),
      V8ClimateBinding.CODEC.fieldOf("climate_binding").forGetter(WireClimate::climateBinding)
  ).apply(i, WireClimate::new));

  /** Only explicitly selected revision 5 has a mandatory tenth hydrology field. */
  private static final Codec<WireHydrology> HYDROLOGY_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireHydrology::version),
      SEED.fieldOf("seed").forGetter(WireHydrology::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireHydrology::minY),
      Codec.INT.fieldOf("height").forGetter(WireHydrology::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireHydrology::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireHydrology::ground),
      V8BlendPolicy.CODEC.fieldOf("blend").forGetter(WireHydrology::blend),
      V8SurfacePolicy.CODEC.fieldOf("surface").forGetter(WireHydrology::surface),
      V8ClimateBinding.CODEC.fieldOf("climate_binding").forGetter(WireHydrology::climateBinding),
      V8HydrologyBinding.CODEC.fieldOf("hydrology_binding").forGetter(WireHydrology::hydrologyBinding)
  ).apply(i, WireHydrology::new));

  /** The exact eleven mandatory fields are written only for saved revision 6. */
  private static final Codec<WireVegetation> VEGETATION_WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(WireVegetation::version),
      SEED.fieldOf("seed").forGetter(WireVegetation::seed),
      Codec.INT.fieldOf("min_y").forGetter(WireVegetation::minY),
      Codec.INT.fieldOf("height").forGetter(WireVegetation::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(WireVegetation::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(WireVegetation::ground),
      V8BlendPolicy.CODEC.fieldOf("blend").forGetter(WireVegetation::blend),
      V8SurfacePolicy.CODEC.fieldOf("surface").forGetter(WireVegetation::surface),
      V8ClimateBinding.CODEC.fieldOf("climate_binding").forGetter(WireVegetation::climateBinding),
      V8HydrologyBinding.CODEC.fieldOf("hydrology_binding").forGetter(WireVegetation::hydrologyBinding),
      V8VegetationPolicy.CODEC.fieldOf("vegetation_policy").forGetter(WireVegetation::vegetationPolicy)
  ).apply(i, WireVegetation::new));

  private static final Set<String> LEGACY_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground");
  private static final Set<String> BLEND_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend");

  private static final Set<String> SURFACE_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend", "surface");

  private static final Set<String> CLIMATE_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend", "surface",
      "climate_binding");

  private static final Set<String> HYDROLOGY_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend", "surface",
      "climate_binding", "hydrology_binding");
  private static final Set<String> VEGETATION_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend", "surface",
      "climate_binding", "hydrology_binding", "vegetation_policy");

  private static final Codec<V8GeneratorSnapshot> LEGACY_VALUE_CODEC = LEGACY_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "legacy V8 snapshot: " + failure.getMessage());
    }
  }, s -> new WireLegacy(s.version, s.seed, s.minY, s.height, s.density, s.ground));

  private static final Codec<V8GeneratorSnapshot> BLEND_VALUE_CODEC = BLEND_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground, w.blend));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "blended V8 snapshot: " + failure.getMessage());
    }
  }, s -> new WireBlend(s.version, s.seed, s.minY, s.height, s.density, s.ground, s.blendPolicy));

  private static final Codec<V8GeneratorSnapshot> SURFACE_VALUE_CODEC = SURFACE_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground, w.blend, w.surface));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "surface V8 snapshot: " + failure.getMessage());
    }
  }, s -> new WireSurface(s.version, s.seed, s.minY, s.height, s.density,
      s.ground, s.blendPolicy, s.surfacePolicy));

  private static final Codec<V8GeneratorSnapshot> CLIMATE_VALUE_CODEC = CLIMATE_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground, w.blend,
          w.surface, w.climateBinding));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "climate-bound V8 snapshot: " + failure.getMessage());
    }
  }, s -> new WireClimate(s.version, s.seed, s.minY, s.height, s.density,
      s.ground, s.blendPolicy, s.surfacePolicy, s.climateBinding));

  private static final Codec<V8GeneratorSnapshot> HYDROLOGY_VALUE_CODEC = HYDROLOGY_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground,
          w.blend, w.surface, w.climateBinding, w.hydrologyBinding));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "hydrology-bound V8 snapshot: " + failure.getMessage());
    }
  }, s -> new WireHydrology(s.version, s.seed, s.minY, s.height, s.density,
      s.ground, s.blendPolicy, s.surfacePolicy, s.climateBinding, s.hydrologyBinding));

  private static final Codec<V8GeneratorSnapshot> VEGETATION_VALUE_CODEC = VEGETATION_WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground,
          w.blend, w.surface, w.climateBinding, w.hydrologyBinding, w.vegetationPolicy));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "vegetated V8 snapshot: " + failure.getMessage());
    }
  }, v -> new WireVegetation(v.version, v.seed, v.minY, v.height, v.density,
      v.ground, v.blendPolicy, v.surfacePolicy, v.climateBinding,
      v.hydrologyBinding, v.vegetationPolicy));

  /**
   * Decode the exact saved version envelope. Legacy six-field saves stay six fields;
   * absence of a blend field means hard classification, never default soft widths.
   * The explicit seven/eight-field revisions reject unknown, missing and duplicate keys.
   */
  public static final Codec<V8GeneratorSnapshot> CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<V8GeneratorSnapshot, T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input).flatMap(fields -> {
        var entries = fields.entries().iterator();
        Set<String> seen = new HashSet<>();
        Integer version = null;
        int count = 0;
        while (entries.hasNext()) {
          if (++count > VEGETATION_FIELDS.size())
            return DataResult.error(() -> "too many V8 snapshot root fields");
          var member = entries.next();
          String key = ops.getStringValue(member.getFirst()).result().orElse(null);
          if (key == null || !VEGETATION_FIELDS.contains(key) || !seen.add(key))
            return DataResult.error(() -> "unexpected or duplicate V8 snapshot root field");
          if ("version".equals(key)) {
            version = Codec.INT.parse(ops, member.getSecond()).result().orElse(null);
            if (version == null)
              return DataResult.error(() -> "V8 snapshot version must be an integer");
          }
        }
        Set<String> expected = version != null && version == VERSION ? LEGACY_FIELDS
            : version != null && version == BLENDED_VERSION ? BLEND_FIELDS
            : version != null && version == SURFACE_VERSION ? SURFACE_FIELDS
            : version != null && version == CLIMATE_VERSION ? CLIMATE_FIELDS
            : version != null && version == HYDROLOGY_VERSION ? HYDROLOGY_FIELDS
            : version != null && version == VEGETATION_VERSION ? VEGETATION_FIELDS : null;
        if (expected == null)
          return DataResult.error(() -> "unsupported or missing V8 snapshot version");
        if (count != expected.size() || !seen.equals(expected))
          return DataResult.error(() -> "incomplete or invalid V8 snapshot version envelope");
        return version == VERSION ? LEGACY_VALUE_CODEC.decode(ops, input)
            : version == BLENDED_VERSION ? BLEND_VALUE_CODEC.decode(ops, input)
            : version == SURFACE_VERSION ? SURFACE_VALUE_CODEC.decode(ops, input)
            : version == CLIMATE_VERSION ? CLIMATE_VALUE_CODEC.decode(ops, input)
            : version == HYDROLOGY_VERSION ? HYDROLOGY_VALUE_CODEC.decode(ops, input)
            : VEGETATION_VALUE_CODEC.decode(ops, input);
      });
    }

    @Override
    public <T> DataResult<T> encode(V8GeneratorSnapshot snapshot, DynamicOps<T> ops, T prefix) {
      return snapshot.version == VERSION ? LEGACY_VALUE_CODEC.encode(snapshot, ops, prefix)
          : snapshot.version == BLENDED_VERSION ? BLEND_VALUE_CODEC.encode(snapshot, ops, prefix)
          : snapshot.version == SURFACE_VERSION ? SURFACE_VALUE_CODEC.encode(snapshot, ops, prefix)
          : snapshot.version == CLIMATE_VERSION ? CLIMATE_VALUE_CODEC.encode(snapshot, ops, prefix)
          : snapshot.version == HYDROLOGY_VERSION ? HYDROLOGY_VALUE_CODEC.encode(snapshot, ops, prefix)
          : VEGETATION_VALUE_CODEC.encode(snapshot, ops, prefix);
    }
  };

  public V8GeneratorSnapshot {
    if (version != VERSION && version != BLENDED_VERSION
        && version != SURFACE_VERSION && version != CLIMATE_VERSION
        && version != HYDROLOGY_VERSION && version != VEGETATION_VERSION)
      throw new IllegalArgumentException("unsupported V8 generator snapshot version: " + version);
    Objects.requireNonNull(blendPolicy, "blendPolicy");
    if (version == VERSION && !V8BlendPolicy.HARD.equals(blendPolicy))
      throw new IllegalArgumentException("legacy V8 snapshot cannot silently enable soft blending");
    if (version >= SURFACE_VERSION && surfacePolicy == null)
      throw new IllegalArgumentException("V8 revision 3 requires frozen surface provenance");
    if (version < SURFACE_VERSION && surfacePolicy != null)
      throw new IllegalArgumentException("older V8 revisions cannot silently gain surface settings");
    if (version >= CLIMATE_VERSION && climateBinding == null)
      throw new IllegalArgumentException("V8 revision 4 requires frozen reference climate binding");
    if (version < CLIMATE_VERSION && climateBinding != null)
      throw new IllegalArgumentException("older V8 revisions cannot silently gain climate binding");
    if (version >= HYDROLOGY_VERSION && hydrologyBinding == null)
      throw new IllegalArgumentException("V8 revision 5 requires frozen hydrology identity");
    if (version < HYDROLOGY_VERSION && hydrologyBinding != null)
      throw new IllegalArgumentException("older V8 revisions cannot silently gain hydrology identity");
    if (version == VEGETATION_VERSION && vegetationPolicy == null)
      throw new IllegalArgumentException("V8 revision 6 needs a saved vegetation policy");
    if (version != VEGETATION_VERSION && vegetationPolicy != null)
      throw new IllegalArgumentException("previous V8 versions cannot silently enable vegetation");
    if (version == VEGETATION_VERSION && !V8SurfacePolicy.FROZEN_R1.equals(surfacePolicy))
      throw new IllegalArgumentException("V8 vegetation requires frozen R1 surface policy");
    if (height < 1 || height > DensityChunk.MAX_HEIGHT)
      throw new IllegalArgumentException("V8 height must be within 1.." + DensityChunk.MAX_HEIGHT);
    long highest = (long) minY + height - 1;
    if (minY < -DensityGraph.WORLD_LIMIT || highest > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("V8 vertical bounds outside density graph coordinates");
    Objects.requireNonNull(density, "density");
    Objects.requireNonNull(ground, "ground");
    // Check the complete immutable graph and material channel before the snapshot can be saved.
    var graph = density.compile(seed);
    graph.channel(ground.channel(), 0, 0, 0);
  }

  /** Reject another world's geometry before any native block write. */
  public void requireHeight(int actualMinY, int actualHeight) {
    if (minY != actualMinY || height != actualHeight)
      throw new IllegalArgumentException("V8 saved profile height mismatch: expected "
          + minY + "/" + height + ", received " + actualMinY + "/" + actualHeight);
  }

  /** Compile once per saved generator/session, never independently per chunk. */
  public Prepared prepare() {
    var graph = density.compile(seed);
    return new Prepared(this, ground.compile(seed, graph));
  }

  /**
   * Provenance-bound compiled geometry; a caller cannot combine one saved
   * world's metadata with another world's compiled density/material fields.
   */
  public static final class Prepared {
    private final V8GeneratorSnapshot snapshot;
    private final GroundSurfaceProfile.Compiled surface;
    private final EcotoneSurfaceField ecotoneSurface;
    private Prepared(V8GeneratorSnapshot snapshot, GroundSurfaceProfile.Compiled surface) {
      this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
      this.surface = Objects.requireNonNull(surface, "surface");
      this.ecotoneSurface = snapshot.version() >= BLENDED_VERSION
          ? new EcotoneSurfaceField(snapshot.seed()) : null;
    }
    public V8GeneratorSnapshot snapshot() { return snapshot; }
    public GroundSurfaceProfile.Compiled surface() { return surface; }
    /**
     * Explicit V8-r2/r3 surface preview: consume saved climate widths and all 40
     * continuous biome weights at ABSOLUTE coordinates. Never called by the
     * legacy/native default generator; does not mutate a chunk or water state.
     * Native placement and palette-persistence acceptance remain separate.
     */
    public EcotoneSurfaceField.Sample previewEcotoneSurface(int worldX, int worldZ,
        EcotoneBlend.Climate climate, EcotoneBlend.Domain domain) {
      if (ecotoneSurface == null)
        throw new IllegalStateException("legacy V8 snapshots have no blended surface policy");
      if (snapshot.blendPolicy().version() != EcotoneBlend.POLICY_VERSION)
        throw new IllegalStateException("unsupported V8 ecotone policy revision");
      var frozen = snapshot.version() >= SURFACE_VERSION
          ? snapshot.surfacePolicy().frozenPalette() : EcotoneSurfaceField.PREVIEW_R1;
      return ecotoneSurface.sample(worldX, worldZ, domain,
          EcotoneBlend.sample(climate, domain, snapshot.blendPolicy().widths()), frozen);
    }

    public void requireHeight(int actualMinY, int actualHeight) {
      snapshot.requireHeight(actualMinY, actualHeight);
    }
  }
}
