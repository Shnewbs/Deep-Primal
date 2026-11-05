package com.shnewbs.deepprimal.engine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Explicit version-3 V8 surface contract. No native material placement is
 * enabled by saving this policy. The recorded fingerprint detects future
 * accidental reinterpretation of the frozen 40-biome cover palette.
 *
 * <p>Climate is still supplied by the caller at absolute world coordinates;
 * the saved source identifier is an integration contract, not proof that a
 * native climate field has been wired or persisted in existing worlds.
 */
public record V8SurfacePolicy(int version, int paletteRevision, int fieldRevision,
    int climateRevision, String paletteFingerprint, String climateSource) {
  public static final int VERSION = 1;
  public static final int CLIMATE_REVISION = 1;
  public static final String CLIMATE_SOURCE = "external-absolute-world-v1";

  /** The original preview cover mapping is frozen only under revision 1. */
  public static final V8SurfacePolicy FROZEN_R1 = new V8SurfacePolicy(
      VERSION, EcotoneSurfaceField.PALETTE_REVISION, EcotoneSurfaceField.FIELD_REVISION,
      CLIMATE_REVISION, fingerprint(EcotoneSurfaceField.PREVIEW_R1), CLIMATE_SOURCE);

  private record Wire(int version, int paletteRevision, int fieldRevision,
      int climateRevision, String paletteFingerprint, String climateSource) {}

  private static final Codec<Wire> WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(Wire::version),
      Codec.INT.fieldOf("palette_revision").forGetter(Wire::paletteRevision),
      Codec.INT.fieldOf("field_revision").forGetter(Wire::fieldRevision),
      Codec.INT.fieldOf("climate_revision").forGetter(Wire::climateRevision),
      Codec.STRING.fieldOf("palette_fingerprint").forGetter(Wire::paletteFingerprint),
      Codec.STRING.fieldOf("climate_source").forGetter(Wire::climateSource)
  ).apply(i, Wire::new));

  private static final Set<String> FIELDS = Set.of(
      "version", "palette_revision", "field_revision", "climate_revision",
      "palette_fingerprint", "climate_source");

  private static final Codec<V8SurfacePolicy> VALUE_CODEC = WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8SurfacePolicy(w.version, w.paletteRevision,
          w.fieldRevision, w.climateRevision, w.paletteFingerprint, w.climateSource));
    } catch (IllegalArgumentException | NullPointerException failure) {
      return DataResult.error(() -> "V8 surface policy: " + failure.getMessage());
    }
  }, p -> new Wire(p.version, p.paletteRevision, p.fieldRevision,
      p.climateRevision, p.paletteFingerprint, p.climateSource));

  /** No optional defaults or ignored keys: a typo must never reshape a saved world. */
  public static final Codec<V8SurfacePolicy> CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<V8SurfacePolicy, T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input).flatMap(fields -> {
        Set<String> seen = new HashSet<>();
        var entries = fields.entries().iterator();
        int count = 0;
        while (entries.hasNext()) {
          if (++count > FIELDS.size())
            return DataResult.error(() -> "too many V8 surface policy fields");
          var entry = entries.next();
          var key = ops.getStringValue(entry.getFirst()).result().orElse(null);
          if (key == null || !FIELDS.contains(key) || !seen.add(key))
            return DataResult.error(() -> "invalid or duplicate V8 surface policy field");
        }
        if (count != FIELDS.size() || !seen.equals(FIELDS))
          return DataResult.error(() -> "incomplete V8 surface policy");
        return VALUE_CODEC.decode(ops, input);
      });
    }
    @Override
    public <T> DataResult<T> encode(V8SurfacePolicy policy, DynamicOps<T> ops, T prefix) {
      return VALUE_CODEC.encode(policy, ops, prefix);
    }
  };

  public V8SurfacePolicy {
    if (version != VERSION || paletteRevision != EcotoneSurfaceField.PALETTE_REVISION
        || fieldRevision != EcotoneSurfaceField.FIELD_REVISION
        || climateRevision != CLIMATE_REVISION)
      throw new IllegalArgumentException("unsupported V8 surface policy revision");
    if (!CLIMATE_SOURCE.equals(Objects.requireNonNull(climateSource, "climateSource")))
      throw new IllegalArgumentException("unknown V8 climate source contract");
    if (!fingerprint(EcotoneSurfaceField.PREVIEW_R1).equals(
        Objects.requireNonNull(paletteFingerprint, "paletteFingerprint")))
      throw new IllegalArgumentException("saved palette fingerprint differs from compiled revision");
  }

  /** Only matching saved versions may use this exact immutable palette. */
  public EcotoneSurfaceField.Palette frozenPalette() {
    return EcotoneSurfaceField.PREVIEW_R1;
  }

  /** Stable FNV-1a of all five raw IEEE-754 cover fractions in biome-id order. */
  public static String fingerprint(EcotoneSurfaceField.Palette palette) {
    Objects.requireNonNull(palette, "palette");
    long hash = 0xcbf29ce484222325L;
    for (int id = 0; id < EcotoneSurfaceField.BIOMES; id++) {
      var cover = palette.get(id);
      for (double value : new double[]{cover.grass(), cover.podzol(), cover.snow(),
          cover.vegetation(), cover.soilFactor()}) {
        long bits = Double.doubleToLongBits(value);
        for (int byteIndex = 0; byteIndex < 8; byteIndex++) {
          hash = (hash ^ ((bits >>> (8 * byteIndex)) & 255L)) * 0x100000001b3L;
        }
      }
    }
    String hex = Long.toUnsignedString(hash, 16);
    return "0".repeat(16 - hex.length()) + hex;
  }
}
