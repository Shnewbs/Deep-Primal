package net.vulkanmod.deepprimal.upscaling.dlss;

import java.util.Random;

/** CPU-only Java parity for the pre-JNI memory provenance contract. */
public final class DlssMemoryBindingContractChecks {
  private static long assertions;

  private static void check(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message);
  }

  private static boolean reference(DlssFrame.MemoryBinding[] values) {
    for (var value : values) if (!value.available()) return false;
    for (int a = 0; a < values.length; ++a) {
      for (int b = a + 1; b < values.length; ++b) {
        var first = values[a];
        var second = values[b];
        if (first.memory() == second.memory()
            && first.offset() < second.offset() + second.size()
            && second.offset() < first.offset() + first.size()) return false;
      }
    }
    return true;
  }

  private static boolean accepted(DlssFrame.MemoryBinding[] values) {
    try {
      DlssMemoryBindingContract.validate(values[0], values[1], values[2], values[3]);
      return true;
    } catch (IllegalArgumentException expected) {
      return false;
    }
  }

  public static void main(String[] args) {
    var clean = new DlssFrame.MemoryBinding[]{
        new DlssFrame.MemoryBinding(10, 0, 64),
        new DlssFrame.MemoryBinding(10, 64, 64),
        new DlssFrame.MemoryBinding(10, 128, 64),
        new DlssFrame.MemoryBinding(11, 0, 64)};
    check(accepted(clean), "touching and separate allocations");
    for (int a = 0; a < 4; ++a) {
      for (int b = a + 1; b < 4; ++b) {
        var copy = clean.clone();
        copy[b] = new DlssFrame.MemoryBinding(copy[a].memory(), copy[a].offset(), copy[a].size());
        check(!accepted(copy), "all six overlap pairs must fail");
      }
    }
    var missing = clean.clone();
    missing[2] = DlssFrame.MemoryBinding.unavailable();
    check(!accepted(missing), "unavailable provenance");
    var negative = clean.clone();
    negative[0] = new DlssFrame.MemoryBinding(10, -1, 1);
    check(!accepted(negative), "negative offset");
    var overflow = clean.clone();
    overflow[0] = new DlssFrame.MemoryBinding(10, Long.MAX_VALUE - 2, 4);
    check(!accepted(overflow), "signed range overflow");
    var negativeOpaqueHandle = clean.clone();
    negativeOpaqueHandle[0] = new DlssFrame.MemoryBinding(Long.MIN_VALUE, 0, 64);
    check(accepted(negativeOpaqueHandle), "nonzero opaque native handles may be signed");

    var frame = new DlssFrame(99,
        new DlssFrame.Image(1, 2, 37, 1280, 720, clean[0]),
        new DlssFrame.Image(3, 4, 126, 1280, 720, clean[1]),
        new DlssFrame.Image(5, 6, 83, 1280, 720, clean[2]),
        new DlssFrame.Image(7, 8, 37, 1920, 1080, clean[3]),
        .25f, -.25f, 1, 1, 16.6f, 1, false);
    DlssMemoryBindingContract.validate(frame);
    check(true, "valid frame integration");
    var legacy = new DlssFrame(99,
        new DlssFrame.Image(1, 2, 37, 1280, 720),
        new DlssFrame.Image(3, 4, 126, 1280, 720),
        new DlssFrame.Image(5, 6, 83, 1280, 720),
        new DlssFrame.Image(7, 8, 37, 1920, 1080),
        .25f, -.25f, 1, 1, 16.6f, 1, false);
    try {
      DlssMemoryBindingContract.validate(legacy);
      throw new AssertionError("legacy frame with unknown bindings accepted");
    } catch (IllegalArgumentException expected) {
      check(true, "legacy metadata fails closed before JNI");
    }

    Random random = new Random(0xD155A11A);
    for (int n = 0; n < 200_000; ++n) {
      DlssFrame.MemoryBinding[] sample = new DlssFrame.MemoryBinding[4];
      for (int i = 0; i < sample.length; ++i)
        sample[i] = new DlssFrame.MemoryBinding(random.nextInt(6) - 1,
            random.nextInt(257) - 1, random.nextInt(129));
      check(accepted(sample) == reference(sample), "randomized reference parity");
    }
    System.out.println("DLSS_MEMORY_PREFLIGHT_PASS assertions=" + assertions + " gpu=false");
  }

  private DlssMemoryBindingContractChecks() {}
}
