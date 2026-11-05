package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;

/**
 * Explicit V8-r5 saved provenance for a separately supplied routed-water
 * observer. This only freezes an externally verified routing source digest;
 * it does not implement or independently authenticate that source, create
 * water, or register V8 in the native generator.
 *
 * <p>Never infer a fingerprint from mutable creation defaults. Derive it from
 * a canonical pinned routing-source specification/parameters and require the
 * same source on reopen. Validation and cross-region hydrology are separate.
 */
public record V8HydrologyBinding(int version, int maskInterfaceRevision,
    String sourceFingerprint) {
  public static final int VERSION = 1;
  public static final int MASK_INTERFACE_REVISION = 1;
  public static final Codec<V8HydrologyBinding> CODEC = Codec.STRING.comapFlatMap(key -> {
    try {
      return DataResult.success(decodeKey(key));
    } catch (IllegalArgumentException failure) {
      return DataResult.error(() -> "V8 hydrology binding: " + failure.getMessage());
    }
  }, V8HydrologyBinding::key);

  public V8HydrologyBinding {
    if (version != VERSION || maskInterfaceRevision != MASK_INTERFACE_REVISION)
      throw new IllegalArgumentException("unsupported V8 hydrology provenance revision");
    Objects.requireNonNull(sourceFingerprint, "source fingerprint");
    if (!sourceFingerprint.matches("[0-9a-f]{64}")
        || sourceFingerprint.equals("0".repeat(64)))
      throw new IllegalArgumentException("routing fingerprint must be a nonzero lowercase SHA-256");
  }

  public static V8HydrologyBinding pinned(String sourceSha256) {
    return new V8HydrologyBinding(VERSION, MASK_INTERFACE_REVISION, sourceSha256);
  }

  public String key() {
    return version + ":" + maskInterfaceRevision + ":" + sourceFingerprint;
  }

  private static V8HydrologyBinding decodeKey(String key) {
    if (key == null || !key.matches("1:1:[0-9a-f]{64}"))
      throw new IllegalArgumentException("noncanonical V8 hydrology key");
    return pinned(key.substring(4));
  }
}
