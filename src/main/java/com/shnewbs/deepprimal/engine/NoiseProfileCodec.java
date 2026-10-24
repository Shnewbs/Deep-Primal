package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.datafixers.util.Either;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.Map;
import java.util.Set;

/** Strict stage-one JSON profile; not yet registered as or enabled by a terrain revision. */
public final class NoiseProfileCodec {
  private static final Set<String> KEYS = Set.of("frequency_x","frequency_y","frequency_z","octaves","lacunarity","gain","salt");
  public static final Codec<NoiseSpec> CODEC = Codec.unboundedMap(Codec.STRING,Codec.either(Codec.DOUBLE,Codec.STRING))
      .comapFlatMap(values -> {
        try {
          if (!KEYS.containsAll(values.keySet())) throw new IllegalArgumentException("Unknown noise profile keys");
          var d=NoiseSpec.DEFAULT;
          double octaves=number(values,"octaves",d.octaves());
          if (octaves!=Math.rint(octaves) || octaves<1 || octaves>8)
            throw new IllegalArgumentException("octaves must be an integer 1..8");
          // Decimal strings preserve every 64-bit salt without JSON double rounding/truncation.
          String salt=values.containsKey("salt")?values.get("salt").right().orElseThrow(
              ()->new IllegalArgumentException("salt must be a decimal string")):Long.toString(d.salt());
          return DataResult.success(new NoiseSpec(number(values,"frequency_x",d.frequencyX()),
              number(values,"frequency_y",d.frequencyY()),number(values,"frequency_z",d.frequencyZ()),
              (int)octaves,number(values,"lacunarity",d.lacunarity()),number(values,"gain",d.gain()),Long.parseLong(salt)));
        } catch (IllegalArgumentException error) { return DataResult.error(error::getMessage); }
      }, spec -> Map.of("frequency_x",Either.left(spec.frequencyX()),"frequency_y",Either.left(spec.frequencyY()),
          "frequency_z",Either.left(spec.frequencyZ()),"octaves",Either.left((double)spec.octaves()),
          "lacunarity",Either.left(spec.lacunarity()),"gain",Either.left(spec.gain()),"salt",Either.right(Long.toString(spec.salt()))));

  private static double number(Map<String,Either<Double,String>> values,String key,double fallback) {
    return values.containsKey(key)?values.get(key).left().orElseThrow(
        ()->new IllegalArgumentException(key+" must be a number")):fallback;
  }
  private NoiseProfileCodec() {}
}
