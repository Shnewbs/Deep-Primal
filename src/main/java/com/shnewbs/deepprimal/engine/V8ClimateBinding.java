package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.shnewbs.deepprimal.core.TerrainOptions;
import java.util.Locale;
import java.util.Objects;

/**
 * Saved V8-r4 reference-climate identity. The complete seven-field V7 terrain
 * options are encoded in a canonical bit-exact key rather than re-reading
 * mutable creation defaults on reopen. A binding never edits physical water.
 * This does NOT assert that the V7 ocean/river mask matches V8 geometry.
 */
public record V8ClimateBinding(int version, int terrainRevision,
    int channelRevision, TerrainOptions savedOptions) {
  public static final int VERSION = 1;
  public static final int REFERENCE_TERRAIN_REVISION = 7;
  public static final Codec<V8ClimateBinding> CODEC = Codec.STRING.comapFlatMap(key -> {
    try {
      return DataResult.success(decodeKey(key));
    } catch (IllegalArgumentException failure) {
      return DataResult.error(() -> "V8 climate binding: " + failure.getMessage());
    }
  }, V8ClimateBinding::key);

  public V8ClimateBinding {
    if (version != VERSION || terrainRevision != REFERENCE_TERRAIN_REVISION
        || channelRevision < 0 || channelRevision > 5)
      throw new IllegalArgumentException("unsupported climate/reference/channel revision");
    Objects.requireNonNull(savedOptions, "savedOptions");
  }

  public static V8ClimateBinding fromSavedV7(TerrainOptions options, int channelRevision) {
    return new V8ClimateBinding(VERSION, REFERENCE_TERRAIN_REVISION,
        channelRevision, Objects.requireNonNull(options, "options"));
  }

  /** Canonical, architecture-independent signed-double bit identity. */
  public String key() {
    return String.format(Locale.ROOT, "%d:%d:%d:%d%d%d%d%d:%016x:%016x",
        version, terrainRevision, channelRevision,
        savedOptions.caves() ? 1 : 0, savedOptions.riverTunnels() ? 1 : 0,
        savedOptions.dunes() ? 1 : 0, savedOptions.glaciers() ? 1 : 0,
        savedOptions.debris() ? 1 : 0,
        Double.doubleToLongBits(savedOptions.mountainScale()),
        Double.doubleToLongBits(savedOptions.polarDistance()));
  }

  private static V8ClimateBinding decodeKey(String key) {
    if (key == null || !key.matches("1:7:[0-5]:[01]{5}:[0-9a-f]{16}:[0-9a-f]{16}"))
      throw new IllegalArgumentException("noncanonical reference-climate key");
    String[] fields = key.split(":", -1);
    String bools = fields[3];
    long mountainBits = Long.parseUnsignedLong(fields[4], 16);
    long polarBits = Long.parseUnsignedLong(fields[5], 16);
    TerrainOptions options = new TerrainOptions(bools.charAt(0) == '1',
        bools.charAt(1) == '1', bools.charAt(2) == '1',
        bools.charAt(3) == '1', bools.charAt(4) == '1',
        Double.longBitsToDouble(mountainBits), Double.longBitsToDouble(polarBits));
    V8ClimateBinding result = new V8ClimateBinding(VERSION, REFERENCE_TERRAIN_REVISION,
        Integer.parseInt(fields[2]), options);
    if (!result.key().equals(key))
      throw new IllegalArgumentException("reference climate key cannot roundtrip exactly");
    return result;
  }
}
