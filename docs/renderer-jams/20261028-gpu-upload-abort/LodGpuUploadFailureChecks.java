package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Owner-build-independent Java 21 tests for the exact production rollback helper. */
public final class LodGpuUploadFailureChecks {
  private static long assertions;
  private static void check(boolean value) {
    assertions++;
    if (!value) throw new AssertionError("GPU rollback assertion " + assertions);
  }
  public static void main(String[] args) {
    RuntimeException uploadError = new RuntimeException("device upload failed");
    List<Integer> closes = new ArrayList<>();
    long remaining = LodGpuUploadFailure.unwind(1000, 625,
        () -> { for (int i = 0; i < 3; i++) closes.add(i); }, uploadError);
    check(remaining == 375);
    check(closes.equals(List.of(0, 1, 2)));
    check(uploadError.getSuppressed().length == 0);

    RuntimeException cleanupError = new RuntimeException("buffer close failed");
    RuntimeException mainError = new RuntimeException("upload failed");
    remaining = LodGpuUploadFailure.unwind(420, 120, () -> { throw cleanupError; }, mainError);
    check(remaining == 300);
    check(mainError.getSuppressed().length == 1);
    check(mainError.getSuppressed()[0] == cleanupError);
    check(mainError.getCause() == null);
    Error fatalCleanup = new AssertionError("driver failure on close");
    mainError = new RuntimeException("upload failed 2");
    remaining = LodGpuUploadFailure.unwind(300, 300, () -> { throw fatalCleanup; }, mainError);
    check(remaining == 0);
    check(mainError.getSuppressed()[0] == fatalCleanup);

    RuntimeException accounting = new RuntimeException("accounting");
    remaining = LodGpuUploadFailure.unwind(99, 100, () -> closes.add(9), accounting);
    check(remaining == 0);
    check(closes.contains(9));
    check(accounting.getSuppressed().length == 1);
    check(accounting.getSuppressed()[0] instanceof IllegalStateException);
    accounting = new RuntimeException("negative");
    remaining = LodGpuUploadFailure.unwind(-1, 0, () -> closes.add(10), accounting);
    check(remaining == 0);
    check(accounting.getSuppressed().length == 1);

    Random random = new Random(20261028);
    for (int i = 0; i < 150_000; i++) {
      long resident = random.nextLong() & Long.MAX_VALUE;
      long accepted = (random.nextLong() & Long.MAX_VALUE) % (resident + (resident == Long.MAX_VALUE ? 0 : 1));
      if (resident == Long.MAX_VALUE) accepted = Math.min(accepted, resident);
      final int[] count = {0};
      RuntimeException failure = new RuntimeException();
      long result = LodGpuUploadFailure.unwind(resident, accepted, () -> count[0]++, failure);
      check(result == resident - accepted);
      check(count[0] == 1);
      check(failure.getSuppressed().length == 0);
    }
    System.out.println("LOD_GPU_ABORT_PASS assertions=" + assertions);
  }
}
