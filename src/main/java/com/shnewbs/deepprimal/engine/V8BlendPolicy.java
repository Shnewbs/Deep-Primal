package com.shnewbs.deepprimal.engine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable, explicitly saved climate/ecotone policy for new V8 world profiles.
 * Version 1 of this policy describes widths, not landform geometry or water levels.
 * Missing policy in a legacy V8 snapshot is hard and never enabled implicitly.
 */
public record V8BlendPolicy(int version, EcotoneBlend.Widths widths) {
  public static final int VERSION = EcotoneBlend.POLICY_VERSION;
  public static final V8BlendPolicy HARD = new V8BlendPolicy(VERSION, EcotoneBlend.Widths.HARD);

  private record Wire(int version, double temperature, double rainfall,
      double height, double slope, double patch) {}

  private static final Codec<Wire> WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(Wire::version),
      Codec.DOUBLE.fieldOf("temperature").forGetter(Wire::temperature),
      Codec.DOUBLE.fieldOf("rainfall").forGetter(Wire::rainfall),
      Codec.DOUBLE.fieldOf("height").forGetter(Wire::height),
      Codec.DOUBLE.fieldOf("slope").forGetter(Wire::slope),
      Codec.DOUBLE.fieldOf("patch").forGetter(Wire::patch)
  ).apply(i, Wire::new));

  private static final Set<String> FIELDS = Set.of(
      "version", "temperature", "rainfall", "height", "slope", "patch");

  private static final Codec<V8BlendPolicy> VALUE_CODEC = WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8BlendPolicy(w.version,
          new EcotoneBlend.Widths(w.temperature, w.rainfall, w.height, w.slope, w.patch)));
    } catch (IllegalArgumentException | NullPointerException failure) {
      return DataResult.error(() -> "V8 blend policy: " + failure.getMessage());
    }
  }, p -> new Wire(p.version, p.widths.temperature(), p.widths.rainfall(),
      p.widths.height(), p.widths.slope(), p.widths.patch()));

  /** Strict nested envelope prevents a typo silently defaulting a saved width. */
  public static final Codec<V8BlendPolicy> CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<V8BlendPolicy, T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input).flatMap(fields -> {
        Set<String> seen = new HashSet<>();
        var entries = fields.entries().iterator();
        int count = 0;
        while (entries.hasNext()) {
          if (++count > FIELDS.size())
            return DataResult.error(() -> "too many V8 blend policy fields");
          var entry = entries.next();
          var key = ops.getStringValue(entry.getFirst()).result().orElse(null);
          if (key == null || !FIELDS.contains(key) || !seen.add(key))
            return DataResult.error(() -> "invalid V8 blend policy field");
        }
        if (count != FIELDS.size() || !seen.equals(FIELDS))
          return DataResult.error(() -> "incomplete V8 blend policy");
        return VALUE_CODEC.decode(ops, input);
      });
    }

    @Override
    public <T> DataResult<T> encode(V8BlendPolicy policy, DynamicOps<T> ops, T prefix) {
      return VALUE_CODEC.encode(policy, ops, prefix);
    }
  };

  public V8BlendPolicy {
    if (version != VERSION)
      throw new IllegalArgumentException("unsupported V8 blend policy version: " + version);
    Objects.requireNonNull(widths, "widths");
  }

  public boolean hasSoftEdges() {
    return !EcotoneBlend.Widths.HARD.equals(widths);
  }

  /**
   * Resolve the saved policy without silently enabling blending in legacy
   * profiles. Legacy V8-r1 profiles deliberately preserve hard behavior when
   * the policy field is absent; every newer revision must carry an explicit
   * versioned policy and fails closed when it is missing or incompatible.
   */
  public static V8BlendPolicy effectiveForSnapshot(int snapshotVersion,
      V8BlendPolicy savedPolicy) {
    if (snapshotVersion == 1) return savedPolicy == null ? HARD : savedPolicy;
    if (snapshotVersion < 1)
      throw new IllegalArgumentException("unsupported V8 snapshot version: " + snapshotVersion);
    if (savedPolicy == null)
      throw new IllegalArgumentException("explicit V8 blend policy required for saved revision "
          + snapshotVersion);
    if (savedPolicy.version() != VERSION)
      throw new IllegalArgumentException("incompatible V8 blend policy version");
    return savedPolicy;
  }

  /** Fail-closed admission for a persisted non-legacy V8 profile. */
  public void requireForSnapshotVersion(int snapshotVersion) {
    if (snapshotVersion <= 1)
      throw new IllegalArgumentException("legacy V8 snapshots do not require an explicit policy");
    if (version != VERSION)
      throw new IllegalArgumentException("incompatible V8 blend policy version");
  }
}
