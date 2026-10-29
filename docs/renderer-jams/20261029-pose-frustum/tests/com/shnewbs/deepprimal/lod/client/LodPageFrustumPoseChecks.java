package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Deterministic opt-in camera-pose resampling and unchanged fine-visibility parity. */
public final class LodPageFrustumPoseChecks {
  private static long assertions;
  private static void check(boolean okay, String message) {
    assertions++;
    if (!okay) throw new AssertionError(message + " assertion=" + assertions);
  }

  private static void trainSkip(LodPageFrustumAdaptiveGate gate,
      double x, double y, double z, float yaw, float pitch) {
    check(gate.shouldTestPages(x, y, z, yaw, pitch), "sample first");
    gate.completed(true, 64, 0);
  }

  private static boolean visible(AABB b) {
    return b.minX < 32 && b.maxX > -32 && b.minZ < 32 && b.maxZ > -32;
  }

  private static List<Integer> selected(List<TerrainGpuMesh> parts, AABB box, boolean coarse) {
    var out = new ArrayList<Integer>();
    if (coarse && box != null && !visible(box)) return out;
    for (int i = 0; i < parts.size(); i++)
      if (visible(parts.get(i).bounds)) out.add(i);
    return out;
  }

  public static void main(String[] args) {
    boolean enabled = LodPageFrustumAdaptiveGate.ENABLED;
    var gate = new LodPageFrustumAdaptiveGate();
    trainSkip(gate, -50, 64, 30, 359, 10);
    check(gate.shouldTestPages(-50, 64, 30, -1, 10) == !enabled,
        "yaw wraparound not movement");
    if (enabled) {
      check(!gate.shouldTestPages(-50, 64, 30, 6.9f, 10), "subthreshold yaw");
      check(!gate.shouldTestPages(-50, 64, 30, 7f, 10), "yaw boundary");
      check(gate.shouldTestPages(-50, 64, 30, 7.01f, 10), "yaw beyond eight degrees");
      gate.completed(true, 64, 0);
      check(gate.shouldTestPages(-50, 64, 30, 7.01f, 10) == false,
          "stable camera skips again");
    }
    gate.reset();
    trainSkip(gate, 10, 30, 20, 0, 0);
    check(gate.shouldTestPages(10, 30, 20, 0, 8.01f), "pitch forces resample");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(14, 30, 20, 0, 8.01f) == !enabled,
        "four-block boundary remains skipped");
    check(gate.shouldTestPages(14.01, 30, 20, 0, 8.01f),
        "translation beyond four blocks resamples");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(14.01, 30, 24.01, 0, 8.01f),
        "z axis translation forces resample");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(14.01, 34.01, 24.01, 0, 8.01f),
        "vertical camera translation forces resample");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(Double.NaN, 34.01, 24.01, 0, 8.01f),
        "invalid position forces conservative coarse sample");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(14.01, 34.01, 24.01, 0, 8.01f),
        "recover from invalid sample");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(14.01, 34.01, 24.01, Float.NaN, 0),
        "invalid angle forces conservative sample");

    // An aborted candidate pose cannot become the last completed sample.
    gate.reset();
    check(gate.shouldTestPages(0, 64, 0, 0, 0), "aborted first sample");
    check(gate.shouldTestPages(10, 64, 0, 180, 0), "aborted pose cannot suppress");
    gate.completed(true, 64, 0);
    check(gate.shouldTestPages(15, 64, 0, 180, 0),
        "first successful sample is authoritative");
    gate.reset();
    check(gate.shouldTestPages(0, 64, 0, 0, 0), "reset drops pose");

    // Moving-camera parity across negative/positive page locations and
    // arbitrary fine-part arrangements. The helper only changes *when* the
    // conservative coarse test runs; it cannot select different mesh parts.
    var random = new Random(0x502ECA11L);
    gate.reset();
    long forcedOnTurn = 0, coarseRejects = 0, selectedParts = 0;
    for (int frame = 0; frame < 180; frame++) {
      float yaw = frame % 9 == 0 ? 145f : 0f;
      double x = frame % 15 < 4 ? 45 : -45;
      boolean coarse = gate.shouldTestPages(x, 64, -21, yaw, -5);
      if (frame % 9 == 0 && coarse) forcedOnTurn++;
      int tested = 0, avoided = 0;
      for (int page = 0; page < 128; page++) {
        int n = 2 + random.nextInt(5);
        var parts = new ArrayList<TerrainGpuMesh>();
        for (int p = 0; p < n; p++) {
          double px = random.nextInt(260) - 130;
          double pz = random.nextInt(260) - 130;
          parts.add(new TerrainGpuMesh(new AABB(px, -10, pz, px + 6, 30, pz + 6)));
        }
        AABB union = LodPageFrustumBounds.build(List.of(parts))[0];
        var before = selected(parts, union, false);
        var after = selected(parts, union, coarse);
        check(before.equals(after), "ordered native-visible parts unchanged");
        selectedParts += after.size();
        if (coarse && union != null) {
          tested++;
          if (!visible(union)) {
            avoided += parts.size();
            coarseRejects++;
          }
        }
      }
      gate.completed(coarse, tested, avoided);
    }
    check(forcedOnTurn >= 20, "turn sampling permitted");
    check(coarseRejects > 0 && selectedParts > 0, "mixed visible and rejected samples");
    System.out.println("LOD_POSE_FRUSTUM_PASS adaptive=" + enabled
        + " assertions=" + assertions + " turnSamples=" + forcedOnTurn
        + " pageRejects=" + coarseRejects + " selectedParts=" + selectedParts);
  }
}
