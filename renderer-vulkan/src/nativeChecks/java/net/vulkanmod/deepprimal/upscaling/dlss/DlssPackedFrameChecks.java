package net.vulkanmod.deepprimal.upscaling.dlss;

import java.util.Arrays;

/** Pure-Java packet parity; neither executes native NGX nor certifies GPU performance. */
public final class DlssPackedFrameChecks {
  private static long assertions;

  public static void main(String[] args) {
    DlssPackedFrame packed = new DlssPackedFrame();
    long[] images = packed.images;
    int[] formats = packed.formats;
    for (int n = 0; n < 100_000; n++) {
      long base = 1 + 16L * n;
      DlssFrame frame = new DlssFrame(1,
          new DlssFrame.Image(base, base + 1, 37, 1280, 720),
          new DlssFrame.Image(base + 2, base + 3, 126, 1280, 720),
          new DlssFrame.Image(base + 4, base + 5, n % 2 == 0 ? 83 : 103, 1280, 720),
          new DlssFrame.Image(base + 6, base + 7, n % 2 == 0 ? 37 : 44, 1920, 1080),
          .25f, -.25f, 1, 1, 16.6f, 1, false);
      packed.fill(frame);
      check(packed.images == images && packed.formats == formats,
          "JNI arrays must be session-owned and reused");
      check(Arrays.equals(images, new long[]{base, base + 1, base + 2, base + 3,
          base + 4, base + 5, base + 6, base + 7}), "JNI image/view order");
      check(Arrays.equals(formats, new int[]{37, 126, n % 2 == 0 ? 83 : 103,
          n % 2 == 0 ? 37 : 44}), "JNI format order");
    }
    // Test another packet to establish the arrays are per session, not global shared scratch.
    DlssPackedFrame other = new DlssPackedFrame();
    check(other.images != images && other.formats != formats, "no cross-session sharing");
    check(other.images.length == 8 && other.formats.length == 4, "JNI fixed ABI lengths");
    System.out.println("DLSS_PACKET_PASS assertions=" + assertions + " gpu=false");
  }

  private static void check(boolean valid, String message) {
    if (!valid) throw new AssertionError(message);
    assertions++;
  }

  private DlssPackedFrameChecks() {}
}
