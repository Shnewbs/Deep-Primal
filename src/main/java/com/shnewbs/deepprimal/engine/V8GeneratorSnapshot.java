package com.shnewbs.deepprimal.engine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
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
    DensityGraph.Definition density, GroundSurfaceProfile ground, V8BlendPolicy blendPolicy) {
  /** Frozen six-field legacy V8 snapshot format. Never rewrite these existing saves. */
  public static final int VERSION = 1;
  /** New explicit seven-field snapshot format; no silent upgrade of previous saves. */
  public static final int BLENDED_VERSION = 2;

  /** Retain source/API compatibility with every existing six-argument constructor. */
  public V8GeneratorSnapshot(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground) {
    this(requireLegacyVersion(version), seed, minY, height, density, ground, V8BlendPolicy.HARD);
  }

  private static int requireLegacyVersion(int version) {
    if (version != VERSION)
      throw new IllegalArgumentException("six-argument V8 constructor is legacy-only");
    return version;
  }

  /** Opt in to a saved policy by constructing a new explicit V8 snapshot revision. */
  public V8GeneratorSnapshot withBlendPolicy(V8BlendPolicy policy) {
    return new V8GeneratorSnapshot(BLENDED_VERSION, seed, minY, height,
        density, ground, Objects.requireNonNull(policy, "policy"));
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

  private static final Set<String> LEGACY_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground");
  private static final Set<String> BLEND_FIELDS = Set.of(
      "version", "seed", "min_y", "height", "density", "ground", "blend");

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

  /**
   * Decode the exact saved version envelope. Legacy six-field saves stay six fields;
   * absence of a blend field means hard classification, never default soft widths.
   * The explicit seven-field revision rejects unknown, missing and duplicate root keys.
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
          if (++count > BLEND_FIELDS.size())
            return DataResult.error(() -> "too many V8 snapshot root fields");
          var member = entries.next();
          String key = ops.getStringValue(member.getFirst()).result().orElse(null);
          if (key == null || !BLEND_FIELDS.contains(key) || !seen.add(key))
            return DataResult.error(() -> "unexpected or duplicate V8 snapshot root field");
          if ("version".equals(key)) {
            version = Codec.INT.parse(ops, member.getSecond()).result().orElse(null);
            if (version == null)
              return DataResult.error(() -> "V8 snapshot version must be an integer");
          }
        }
        Set<String> expected = version != null && version == VERSION
            ? LEGACY_FIELDS : version != null && version == BLENDED_VERSION ? BLEND_FIELDS : null;
        if (expected == null)
          return DataResult.error(() -> "unsupported or missing V8 snapshot version");
        if (count != expected.size() || !seen.equals(expected))
          return DataResult.error(() -> "incomplete or invalid V8 snapshot version envelope");
        return version == VERSION
            ? LEGACY_VALUE_CODEC.decode(ops, input) : BLEND_VALUE_CODEC.decode(ops, input);
      });
    }

    @Override
    public <T> DataResult<T> encode(V8GeneratorSnapshot snapshot, DynamicOps<T> ops, T prefix) {
      return snapshot.version == VERSION
          ? LEGACY_VALUE_CODEC.encode(snapshot, ops, prefix)
          : BLEND_VALUE_CODEC.encode(snapshot, ops, prefix);
    }
  };

  public V8GeneratorSnapshot {
    if (version != VERSION && version != BLENDED_VERSION)
      throw new IllegalArgumentException("unsupported V8 generator snapshot version: " + version);
    Objects.requireNonNull(blendPolicy, "blendPolicy");
    if (version == VERSION && !V8BlendPolicy.HARD.equals(blendPolicy))
      throw new IllegalArgumentException("legacy V8 snapshot cannot silently enable soft blending");
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
    private Prepared(V8GeneratorSnapshot snapshot, GroundSurfaceProfile.Compiled surface) {
      this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
      this.surface = Objects.requireNonNull(surface, "surface");
    }
    public V8GeneratorSnapshot snapshot() { return snapshot; }
    public GroundSurfaceProfile.Compiled surface() { return surface; }
    public void requireHeight(int actualMinY, int actualHeight) {
      snapshot.requireHeight(actualMinY, actualHeight);
    }
  }
}
