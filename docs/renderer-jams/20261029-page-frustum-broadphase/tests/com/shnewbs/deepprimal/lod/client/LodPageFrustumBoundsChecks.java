package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Deterministic CPU-only proof of conservative page rejection and draw-order parity. */
public final class LodPageFrustumBoundsChecks {
  private static long assertions;
  private static volatile long sink;

  private static void check(boolean condition, String what) {
    assertions++;
    if (!condition) throw new AssertionError(what + " at assertion " + assertions);
  }

  private record Plane(double x, double y, double z, double offset) {}
  private static final class Frustum {
    final Plane[] planes;
    long checks;
    Frustum(Plane... planes) { this.planes = planes; }
    boolean visible(AABB a) {
      checks++;
      if (a == null) return false;
      for (Plane p : planes) {
        double furthest = p.x >= 0 ? a.maxX : a.minX;
        furthest *= p.x;
        furthest += p.y * (p.y >= 0 ? a.maxY : a.minY);
        furthest += p.z * (p.z >= 0 ? a.maxZ : a.minZ);
        if (furthest < p.offset) return false;
      }
      return true;
    }
  }

  private static Frustum view(double x, double z, double width) {
    return new Frustum(new Plane(1, 0, 0, x - width),
        new Plane(-1, 0, 0, -x - width), new Plane(0, 0, 1, z - width),
        new Plane(0, 0, -1, -z - width), new Plane(0, 1, 0, -500),
        new Plane(0, -1, 0, -1000), new Plane(1, 0, 1, x + z - width * 1.5));
  }

  private static List<TerrainGpuMesh> meshes(int n, Random r, double x, double z) {
    List<TerrainGpuMesh> parts = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      double bx = x + r.nextDouble() * 16, bz = z + r.nextDouble() * 16;
      double minY = r.nextInt(420) - 80, height = r.nextDouble() * 120 + 1;
      parts.add(new TerrainGpuMesh(new AABB(bx, minY, bz,
          bx + r.nextDouble() * 16, minY + height, bz + r.nextDouble() * 16)));
    }
    return parts;
  }

  private static void oneLevel(List<TerrainGpuMesh> parts, AABB broad, Frustum f) {
    List<Integer> before = new ArrayList<>(), after = new ArrayList<>();
    for (int i = 0; i < parts.size(); i++) if (f.visible(parts.get(i).bounds)) before.add(i);
    long priorChecks = f.checks;
    boolean needsParts = broad == null || f.visible(broad);
    long selectedChecks = f.checks;
    if (needsParts) for (int i = 0; i < parts.size(); i++)
      if (f.visible(parts.get(i).bounds)) after.add(i);
    check(before.equals(after), "frustum parity (including material/draw order)");
    check(selectedChecks - priorChecks == (broad == null ? 0 : 1), "one cached broadphase test");
    if (broad != null) {
      for (TerrainGpuMesh mesh : parts) {
        AABB b = mesh.bounds;
        check(b.minX >= broad.minX && b.minY >= broad.minY && b.minZ >= broad.minZ
            && b.maxX <= broad.maxX && b.maxY <= broad.maxY
            && b.maxZ <= broad.maxZ, "world-space containment");
      }
    }
  }

  private static void deterministic() {
    Random r = new Random(0x1A25C0A4L);
    for (int trial = 0; trial < 12000; trial++) {
      List<List<TerrainGpuMesh>> levels = new ArrayList<>();
      int[] counts = {r.nextInt(11), r.nextInt(11), r.nextInt(11)};
      double x = (r.nextInt(2000) - 1000) * 16.0;
      double z = (r.nextInt(2000) - 1000) * 16.0;
      for (int d = 0; d < counts.length; d++) levels.add(meshes(counts[d], r, x, z));
      List<List<TerrainGpuMesh>> original = new ArrayList<>();
      for (List<TerrainGpuMesh> layer : levels) original.add(List.copyOf(layer));
      AABB[] broad = LodPageFrustumBounds.build(levels);
      check(broad.length == 3, "three independent levels");
      for (int d = 0; d < levels.size(); d++) {
        check(broad[d] == null == (counts[d] <= 1), "singleton/empty unbounded");
        // Includes very far views, near handoff, negative coordinates and edge-touching.
        oneLevel(levels.get(d), broad[d], view(x + (trial % 7) * 50,
            z + (trial % 13) * 35, 5 + (trial % 5) * 37));
        check(levels.get(d).equals(original.get(d)), "no source-list mutation");
      }
    }
    // Critical false-negative mutant: page bound from first part alone misses the second.
    AABB offscreen = new AABB(500, 0, 500, 501, 1, 501);
    AABB visible = new AABB(-1, 0, -1, 1, 1, 1);
    List<TerrainGpuMesh> pair = List.of(new TerrainGpuMesh(offscreen),
        new TerrainGpuMesh(visible));
    AABB[] pairBounds = LodPageFrustumBounds.build(List.of(pair));
    check(pairBounds[0] != null && view(0, 0, 20).visible(pairBounds[0]),
        "union retains non-first visible part");
    oneLevel(pair, pairBounds[0], view(0, 0, 20));
    // Fail-safe for damaged/non-finite bounds: preserve original per-part checks.
    for (AABB broken : Arrays.asList(null, new AABB(Double.NaN, 0, 0, 1, 1, 1),
        new AABB(2, 0, 0, 1, 1, 1),
        new AABB(0, 0, 0, Double.POSITIVE_INFINITY, 1, 1))) {
      AABB[] invalid = LodPageFrustumBounds.build(List.of(List.of(
          new TerrainGpuMesh(new AABB(-1, 0, -1, 1, 1, 1)),
          new TerrainGpuMesh(broken))));
      check(invalid[0] == null, "malformed bound must disable coarse rejection");
    }
  }

  private static long drawFixture(List<List<TerrainGpuMesh>> pages, AABB[][] bounds,
      Frustum f, boolean coarse) {
    long start = System.nanoTime(), accepted = 0;
    for (int p = 0; p < pages.size(); p++) {
      var parts = pages.get(p);
      AABB merged = bounds[p][0];
      if (coarse && merged != null && !f.visible(merged)) continue;
      for (TerrainGpuMesh part : parts) if (f.visible(part.bounds)) accepted++;
    }
    sink = accepted;
    return System.nanoTime() - start;
  }

  private static long median(long[] times) {
    Arrays.sort(times);
    return times[times.length / 2];
  }

  private static void pairedCpu() {
    Random r = new Random(3542);
    List<List<TerrainGpuMesh>> pages = new ArrayList<>();
    AABB[][] bounds = new AABB[4096][];
    for (int i = 0; i < bounds.length; i++) {
      double x = (i % 64 - 32) * 16., z = (i / 64 - 32) * 16.;
      List<TerrainGpuMesh> page = meshes(6, r, x, z);
      pages.add(page);
      bounds[i] = LodPageFrustumBounds.build(List.of(page));
    }
    for (int i = 0; i < 100; i++) {
      drawFixture(pages, bounds, view(0, 0, 140), false);
      drawFixture(pages, bounds, view(0, 0, 140), true);
    }
    long[] oldTimes = new long[301], newTimes = new long[301];
    long oldCalls = 0, newCalls = 0;
    for (int i = 0; i < 301; i++) {
      Frustum old = view(0, 0, 140), fast = view(0, 0, 140);
      if ((i & 1) == 0) {
        oldTimes[i] = drawFixture(pages, bounds, old, false);
        newTimes[i] = drawFixture(pages, bounds, fast, true);
      } else {
        newTimes[i] = drawFixture(pages, bounds, fast, true);
        oldTimes[i] = drawFixture(pages, bounds, old, false);
      }
      oldCalls += old.checks; newCalls += fast.checks;
      check(newCalls <= oldCalls, "coarse tests must not exceed fine checks in this fixture");
    }
    System.out.println("LOD_PAGE_FRUSTUM_PASS assertions=" + assertions
        + " pages=4096 partsPerPage=6 pairedSamples=301"
        + " baselineMedianNanos=" + median(oldTimes)
        + " candidateMedianNanos=" + median(newTimes)
        + " baselineFrustumCalls=" + oldCalls
        + " candidateFrustumCalls=" + newCalls
        + " callsAvoided=" + (oldCalls - newCalls));
  }

  public static void main(String[] args) {
    deterministic();
    pairedCpu();
  }
}
