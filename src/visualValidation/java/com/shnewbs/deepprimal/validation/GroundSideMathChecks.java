package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.GroundSideSpriteSource;

final class GroundSideMathChecks {
  static void run() {
    int checked = 0;
    for (int luminance = 0; luminance < 256; luminance++)
      for (int weight = 0; weight < 256; weight++)
        for (int surface : new int[] {0, 0xffffff, 0x3a9141, 0x69602b})
          for (int soil : new int[] {0xb6a080, 0x794a2c, 0x32302a}) {
            int result = GroundSideSpriteSource.compose(
                GroundSideSpriteSource.pack(luminance, weight, soil), surface);
            for (int shift = 0; shift <= 16; shift += 8) {
              double expected = luminance * (weight * ((surface >> shift) & 255)
                  + (255 - weight) * ((soil >> shift) & 255)) / 65025.0;
              if (Math.abs(((result >> shift) & 255) - expected) > 1.5)
                throw new AssertionError("Dual-tint coefficient changed luminance/mask blend");
              checked++;
            }
            if ((result >>> 24) != 255) throw new AssertionError("Non-opaque dual-tint output");
          }
    System.out.println("GROUND_SIDE_MATH_PASS channelChecks=" + checked
        + " luminances=256 weights=256 surfaces=4 soils=3 maxError=1.5/255 opaque=true");
  }

  private GroundSideMathChecks() {}
}
