package com.shnewbs.deepprimal.lod.client;

import java.util.Objects;

/**
 * Render-thread, best-effort disposal during LOD world reset or cache eviction.
 * A single failing GPU close must not prevent closing later buffers or completing
 * owner bookkeeping. The first failure is rethrown after all releases were tried.
 * This is resource cleanup, not device-loss recovery.
 */
final class LodGpuResetCleanup {
  private Throwable firstFailure;

  void release(Runnable closer) {
    Objects.requireNonNull(closer, "closer");
    try {
      closer.run();
    } catch (RuntimeException | Error failure) {
      if (firstFailure == null) firstFailure = failure;
      else if (failure != firstFailure) firstFailure.addSuppressed(failure);
    }
  }

  void throwIfFailed() {
    if (firstFailure instanceof Error error) throw error;
    if (firstFailure instanceof RuntimeException failure) throw failure;
  }
}
