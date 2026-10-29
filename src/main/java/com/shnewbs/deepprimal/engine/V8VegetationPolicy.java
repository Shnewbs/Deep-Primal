package com.shnewbs.deepprimal.engine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;

/**
 * Mandatory frozen feature-placement choice for NEW explicit V8 revision-6
 * profiles. No default for missing fields, no upgrades of older V8 snapshots.
 * The existing revision-1 surface palette/field remains authoritative.
 */
public record V8VegetationPolicy(int version, int anchorSpacing, double densityScale) {
  public static final int VERSION = 1;
  public static final V8VegetationPolicy FROZEN_R1 = new V8VegetationPolicy(VERSION, 4, 1);
  private static final Set<String> FIELDS = Set.of("version", "anchor_spacing", "density_scale");

  private record Wire(int version, int anchorSpacing, double densityScale) {}
  private static final Codec<Wire> WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(Wire::version),
      Codec.INT.fieldOf("anchor_spacing").forGetter(Wire::anchorSpacing),
      Codec.DOUBLE.fieldOf("density_scale").forGetter(Wire::densityScale)
  ).apply(i, Wire::new));
  private static final Codec<V8VegetationPolicy> VALUE_CODEC = WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8VegetationPolicy(w.version, w.anchorSpacing, w.densityScale));
    } catch (IllegalArgumentException failure) {
      return DataResult.error(() -> "V8 vegetation policy: " + failure.getMessage());
    }
  }, p -> new Wire(p.version, p.anchorSpacing, p.densityScale));

  public static final Codec<V8VegetationPolicy> CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<V8VegetationPolicy,T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input).flatMap(fields -> {
        Set<String> seen = new HashSet<>();
        var entries = fields.entries().iterator();
        int n = 0;
        while (entries.hasNext()) {
          if (++n > FIELDS.size())
            return DataResult.error(() -> "too many V8 vegetation policy fields");
          String key = ops.getStringValue(entries.next().getFirst()).result().orElse(null);
          if (key == null || !FIELDS.contains(key) || !seen.add(key))
            return DataResult.error(() -> "unexpected or duplicate V8 vegetation policy field");
        }
        if (n != FIELDS.size() || !seen.equals(FIELDS))
          return DataResult.error(() -> "missing V8 vegetation policy field");
        return VALUE_CODEC.decode(ops, input);
      });
    }
    @Override
    public <T> DataResult<T> encode(V8VegetationPolicy value, DynamicOps<T> ops, T prefix) {
      return VALUE_CODEC.encode(value, ops, prefix);
    }
  };

  public V8VegetationPolicy {
    if (version != VERSION)
      throw new IllegalArgumentException("unsupported V8 vegetation revision");
    if (anchorSpacing != 4 && anchorSpacing != 8 && anchorSpacing != 16)
      throw new IllegalArgumentException("V8 vegetation anchor spacing must divide 16");
    if (!Double.isFinite(densityScale) || densityScale < 0 || densityScale > 1)
      throw new IllegalArgumentException("V8 vegetation density scale must be finite [0,1]");
  }
}
