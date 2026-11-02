package com.shnewbs.deepprimal.core;

import java.util.*;

public final class WaterAnimationChecks {
  private static void require(boolean b, String message) {
    if (!b) throw new AssertionError(message);
  }

  private static WaterPatchMesher.Snapshot strip(int origin) {
    byte[] u = new byte[WaterPatchMesher.CELLS], s = new byte[u.length];
    int[] l = new int[u.length], c = new int[256];
    Arrays.fill(c, 0xff3f76e4);
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) {
        u[WaterPatchMesher.index(x, 0, z)] = (byte) (1 + Math.floorMod(origin + x + 2 * z, 16));
        s[WaterPatchMesher.index(x, -1, z)] = 1;
      }
    return new WaterPatchMesher.Snapshot(u, s, l, c);
  }

  private static WaterPatchMesher.Snapshot coloredStrip(int originX) {
    byte[] u = new byte[WaterPatchMesher.CELLS], s = new byte[u.length];
    int[] l = new int[u.length], c = new int[18 * 18];
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) {
        u[WaterPatchMesher.index(x, 0, z)] = 8;
        s[WaterPatchMesher.index(x, -1, z)] = 1;
        int worldX = originX + x;
        c[(z + 1) * 18 + x + 1] =
            ((worldX * 7 & 255) << 16) | ((z * 11 & 255) << 8) | ((worldX + z) & 255);
      }
    return new WaterPatchMesher.Snapshot(u, s, l, c);
  }

  public static void run() {
    Random random = new Random(211004);
    var ordered = new WaterSurfaceTransition(0, 0);
    ordered.target(1, 0, 20);
    ordered.target(.25f, 50, 19);
    require(
        ordered.target() == 1 && ordered.at(100) == 1,
        "stale neighboring jobs cannot reverse a shared edge");
    for (int i = 0; i < 10000; i++) {
      float a = random.nextFloat(), b = random.nextFloat(), c = random.nextFloat();
      long start = random.nextInt(1_000_000);
      var t = new WaterSurfaceTransition(a, start);
      t.target(b, start);
      int elapsed = random.nextInt(100);
      float displayed = t.at(start + elapsed);
      require(
          displayed >= Math.min(a, b) - .00001 && displayed <= Math.max(a, b) + .00001,
          "bounded interpolation");
      t.target(c, start + elapsed);
      require(Math.abs(t.at(start + elapsed) - displayed) < .000001, "retarget continuity");
      require(Math.abs(t.at(start + elapsed + 100) - c) < .000001, "settled target");
      t.target(c, start + elapsed + 1000);
      require(t.from(start + elapsed + 1000) == c, "unchanged samples do not replay motion");
    }
    float unit = 1 / 16f;
    var parcel = new WaterSurfaceTransition(.5f, 0);
    long version = 0;
    for (int n = 1; n <= 40; n++)
      parcel.target(n % 2 == 1 ? .5f + unit : .5f, n * 75L, ++version);
    float settled = parcel.at(40 * 75L + 100);
    require(Math.abs(settled - (.5f + unit / 2)) < .00001f, "parcel ping-pong settles at midpoint");
    for (int n = 41; n <= 60; n++) {
      parcel.target(n % 2 == 1 ? .5f + unit : .5f, n * 75L, ++version);
      require(Math.abs(parcel.at(n * 75L + 37) - settled) < .00001f, "settled parcel surface stays still");
    }
    var fill = new WaterSurfaceTransition(.25f, 0);
    for (int n = 1; n <= 8; n++) {
      fill.target(.25f + n * unit, n * 50L, n);
      require(fill.target() == .25f + n * unit, "monotonic fill is never held back");
    }
    var slow = new WaterSurfaceTransition(.5f, 0);
    slow.target(.5f + unit, 0, 1);
    slow.target(.5f, WaterSurfaceTransition.SETTLE_MILLIS + 1, 2);
    require(slow.target() == .5f, "slow genuine reversals are exact");
    var jump = new WaterSurfaceTransition(.5f, 0);
    jump.target(.75f, 0, 1);
    jump.target(.5f, 50, 2);
    require(jump.target() == .5f, "large reversals are exact");
    var stopped = new WaterSurfaceTransition(.5f, 0);
    stopped.target(.5f + unit, 50, 1);
    stopped.target(.5f, 100, 2);
    require(stopped.remaining(100) == 400, "midpoint schedules cleanup beyond initial interpolation");
    stopped.target(.5f, 300, 3);
    require(stopped.remaining(300) == 200, "unchanged recapture cannot extend hold cleanup");
    require(stopped.target() == .5f + unit / 2, "unchanged recapture retains active parcel smoothing");
    stopped.target(.5f, 500, 4);
    require(stopped.target() == .5f, "stopped flow releases the midpoint at its original deadline");
    require(Math.abs(stopped.at(500) - (.5f + unit / 2)) < .00001,
        "midpoint release remains visually continuous");
    require(stopped.at(600) == .5f, "stopped flow settles to the authoritative height");
    require(stopped.remaining(500) == 100 && stopped.remaining(600) == 0,
        "release interpolation completes without perpetual cleanup");
    int releases = 0;
    for (int level = 1; level < 16; level++)
      for (int direction : new int[] {-1, 1})
        for (int cadence : new int[] {25, 50, 100, 200, 400}) {
          float original = level / 16f, other = original + direction * unit;
          var transition = new WaterSurfaceTransition(original, 0);
          transition.target(other, cadence, 1);
          transition.target(original, 2L * cadence, 2);
          long deadline = 2L * cadence + WaterSurfaceTransition.SETTLE_MILLIS;
          float midpoint = (original + other) * .5f;
          for (int duplicate = 1; duplicate <= 3; duplicate++) {
            long at = 2L * cadence + duplicate * 100;
            transition.target(original, at, 2);
            require(transition.target() == midpoint && transition.remaining(at) == deadline - at,
                "duplicate shared-corner publications retain original expiry");
          }
          transition.target(other, deadline - 1, 1);
          require(transition.target() == midpoint, "stale page cannot restart hold");
          float displayed = transition.at(deadline);
          transition.target(original, deadline, 3);
          require(transition.at(deadline) == displayed, "expiry is C0 continuous");
          require(transition.target() == original && transition.at(deadline + 100) == original
              && transition.remaining(deadline + 100) == 0, "exact authoritative release");
          transition.target(original, deadline + 1000, 4);
          require(transition.remaining(deadline + 1000) == 0, "static height stays idle");
          releases++;
        }
    var opacity = new WaterSurfaceTransition(.5f, 0);
    var hiddenSource = new WaterSurfaceTransition(.5f, 0);
    hiddenSource.target(.5f + unit, 50, 1);
    hiddenSource.target(.5f, 100, 2);
    hiddenSource.advance(500);
    require(hiddenSource.target() == .5f && hiddenSource.at(500) == .5f + unit / 2,
        "visible neighbor services expiry without an upload from the hidden source page");
    hiddenSource.advance(600);
    require(hiddenSource.at(600) == .5f && hiddenSource.remaining(600) == 0,
        "frame-serviced hold returns exactly without changing raw sample history");
    opacity.target(.5f + unit, 50);
    opacity.target(.5f, 100);
    require(opacity.remaining(100) == 100 && opacity.at(200) == .5f,
        "unversioned opacity remains an ordinary 100 ms interpolation");
    var left = strip(0);
    var right = strip(16);
    var a = WaterPatchMesher.build(left, null, true);
    var b = WaterPatchMesher.build(right, null, true);
    Map<Float, Float> edge = new HashMap<>();
    for (int i = 0; i < a.vertices().length; i += WaterPatchMesher.STRIDE)
      if (a.vertices()[i] == 16 && a.vertices()[i + 10] == 0)
        edge.put(a.vertices()[i + 2], a.vertices()[i + 3]);
    int shared = 0;
    for (int i = 0; i < b.vertices().length; i += WaterPatchMesher.STRIDE)
      if (b.vertices()[i] == 0 && b.vertices()[i + 10] == 0) {
        float z = b.vertices()[i + 2];
        require(Math.abs(edge.get(z) - b.vertices()[i + 3]) < .000001, "shared page edge");
        shared++;
      }
    require(shared > 16, "multiple shared samples");
    var tintedLeft = WaterPatchMesher.build(coloredStrip(0), null, true);
    var tintedRight = WaterPatchMesher.build(coloredStrip(16), null, true);
    var leftEdge = new HashMap<Float, Integer>();
    for (int i = 0; i < tintedLeft.vertices().length; i += WaterPatchMesher.STRIDE)
      if (WaterPatchMesher.face(tintedLeft.vertices()[i + 10]) == 0
          && tintedLeft.vertices()[i] == 16)
        leftEdge.put(tintedLeft.vertices()[i + 2],
            Float.floatToRawIntBits(tintedLeft.vertices()[i + 7]));
    int tintedShared = 0;
    for (int i = 0; i < tintedRight.vertices().length; i += WaterPatchMesher.STRIDE)
      if (WaterPatchMesher.face(tintedRight.vertices()[i + 10]) == 0
          && tintedRight.vertices()[i] == 0) {
        float z = tintedRight.vertices()[i + 2];
        require(leftEdge.get(z) != null
            && leftEdge.get(z) == Float.floatToRawIntBits(tintedRight.vertices()[i + 7]),
            "halo-backed water colors match across adjacent page edges");
        tintedShared++;
      }
    require(tintedShared > 16, "tinted page edge has shared samples");
    byte[] empty = new byte[WaterPatchMesher.CELLS];
    var dry =
        new WaterPatchMesher.Snapshot(
            empty, new byte[empty.length], new int[empty.length], new int[256]);
    var fading = WaterPatchMesher.build(dry, left, true);
    require(fading.count() > 0, "drying geometry retained for fade");
    for (int i = 0; i < fading.vertices().length; i += WaterPatchMesher.STRIDE)
      require(fading.vertices()[i + 13] == 0, "dry geometry fades out");
    require(WaterPatchMesher.build(dry, dry, true).count() == 0, "settled dry geometry released");
    System.out.println(
        "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges="
            + shared
            + " dryFade=true immutableSnapshots=true parcelSettle=true exactReleaseCases=" + releases);
  }
}
