package net.vulkanmod.deepprimal.upscaling.dlss;

/** Pure Java Vulkan-handle contract checks; does not load NVIDIA NGX or execute on a GPU. */
public final class DlssImageAliasChecks {
  private static int assertions;
  private static final DlssFrame.Image[] VALID = {
      new DlssFrame.Image(1, 2, 37, 1280, 720),
      new DlssFrame.Image(3, 4, 126, 1280, 720),
      new DlssFrame.Image(5, 6, 83, 1280, 720),
      new DlssFrame.Image(7, 8, 37, 1920, 1080)
  };

  public static void main(String[] args) {
    // Unaliased inputs, including a separate render-sized destination and non-default
    // temporal constants, remain accepted; this does not claim full-scene motion data.
    for (int i = 0; i < 1024; i++) {
      DlssFrame frame = frame(VALID);
      check(frame.color().image() == 1 && frame.depth().image() == 3
          && frame.motionVectors().view() == 6 && frame.output().view() == 8,
          "valid disjoint resources");
    }
    for (int first = 0; first < VALID.length; first++) {
      for (int second = first + 1; second < VALID.length; second++) {
        for (boolean reuseImage : new boolean[]{true, false}) {
          DlssFrame.Image[] collision = VALID.clone();
          DlssFrame.Image original = collision[second];
          collision[second] = new DlssFrame.Image(
              reuseImage ? collision[first].image() : original.image(),
              reuseImage ? original.view() : collision[first].view(),
              original.format(), original.width(), original.height());
          final int left = first, right = second;
          rejectsAlias(() -> frame(collision), "pair=" + left + ":" + right
              + " handle=" + (reuseImage ? "image" : "view"));
        }
      }
    }
    // Different views on the same image still collide, even with valid formats and sizes.
    DlssFrame.Image[] sameImageNewView = VALID.clone();
    sameImageNewView[3] = new DlssFrame.Image(1, 40, 37, 1920, 1080);
    rejectsAlias(() -> frame(sameImageNewView), "output with alternate view of color image");
    // Different image numbers but identical views also collide.
    DlssFrame.Image[] sameViewNewImage = VALID.clone();
    sameViewNewImage[3] = new DlssFrame.Image(40, 2, 37, 1920, 1080);
    rejectsAlias(() -> frame(sameViewNewImage), "output with identical color view");
    System.out.println("DLSS_IMAGE_ALIAS_PASS assertions=" + assertions
        + " pairs=6 colliding_handle_cases=14 native_gpu=false");
  }

  private static DlssFrame frame(DlssFrame.Image[] input) {
    return new DlssFrame(9, input[0], input[1], input[2], input[3],
        .25f, -.25f, 1f, 1f, 16.6f, 1f, true);
  }

  private static void rejectsAlias(Runnable action, String caseName) {
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      check(expected.getMessage().contains("distinct image/view handles"),
          "incorrect rejection reason: " + caseName + ": " + expected.getMessage());
      return;
    }
    throw new AssertionError("Aliased DLSS resources were accepted: " + caseName);
  }

  private static void check(boolean valid, String context) {
    if (!valid) throw new AssertionError(context);
    assertions++;
  }

  private DlssImageAliasChecks() {}
}
