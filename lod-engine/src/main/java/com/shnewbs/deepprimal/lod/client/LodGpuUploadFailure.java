package com.shnewbs.deepprimal.lod.client;

import java.util.Objects;

/**
 * Exceptional GPU-upload rollback. The owner clears its pending reference before calling
 * this helper, so a failing close cannot cause a later frame to reuse freed buffers.
 * Every accepted resource is included in the committed byte ledger before any fallible
 * post-append diagnostics. This does not recover a failed GPU device or retry a page.
 */
final class LodGpuUploadFailure {
  private LodGpuUploadFailure() {}

  static long unwind(long residentBytes, long acceptedBytes, Runnable release,
      Throwable originalFailure) {
    Objects.requireNonNull(release, "release");
    Objects.requireNonNull(originalFailure, "originalFailure");
    try {
      release.run();
    } catch (RuntimeException | Error cleanupFailure) {
      if (cleanupFailure != originalFailure) originalFailure.addSuppressed(cleanupFailure);
    }
    if (residentBytes < 0 || acceptedBytes < 0 || acceptedBytes > residentBytes) {
      originalFailure.addSuppressed(new IllegalStateException(
          "inconsistent LOD pending GPU allocation accounting"));
      return 0;
    }
    return residentBytes - acceptedBytes;
  }
}
