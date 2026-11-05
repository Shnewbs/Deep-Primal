package com.shnewbs.deepprimal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Opt-in, finite, observational receiver traces from a watershed perimeter to
 * the FIRST downstream perimeter cell. This is graph geometry, not runoff,
 * physical water, a spill certificate or a global ocean claim.
 *
 * <p>Interior paths share memoized exits. The work is O(side^2) with bounded
 * scratch storage and never reads a chunk, a neighboring region or an ocean
 * level. Legacy generation need not call this helper.
 */
public final class WatershedInteriorPortRoutes {
  private WatershedInteriorPortRoutes() {}

  public record Route(int fromX, int fromZ, int toX, int toZ,
                      long firstReceiverKey, double crest, int hops) {
    public Route {
      long from = WatershedOutletTopology.key(fromX, fromZ);
      long to = WatershedOutletTopology.key(toX, toZ);
      WatershedOutletTopology.key((int) (firstReceiverKey >> 32),
          (int) firstReceiverKey);
      if (from == to || !Double.isFinite(crest) || hops < 2)
        throw new IllegalArgumentException("invalid interior watershed route");
    }
  }

  /**
   * Source and receiver arrays are the already-computed SAME region. The grid
   * is a signed-coordinate, STEP-spaced square; the region exporter verifies
   * its own authoritative parent ordering before invoking this method.
   */
  public static List<Route> trace(int originX, int originZ, int side, int step,
      double[] ground, double[] stage, int[] parent) {
    if (side < 3 || side > 1025 || step != WatershedOutletTopology.STEP
        || (long) side * side > Integer.MAX_VALUE
        || Math.floorMod(originX, step) != 0 || Math.floorMod(originZ, step) != 0)
      throw new IllegalArgumentException("invalid watershed route grid");
    int count = side * side;
    if (ground == null || stage == null || parent == null || ground.length != count
        || stage.length != count || parent.length != count)
      throw new IllegalArgumentException("incomplete watershed route arrays");
    long lastX = (long) originX + (long) (side - 1) * step;
    long lastZ = (long) originZ + (long) (side - 1) * step;
    if (lastX > Integer.MAX_VALUE || lastZ > Integer.MAX_VALUE)
      throw new IllegalArgumentException("watershed route crosses world int limit");

    // Validate ALL edges before tracing any source: a valid-looking boundary
    // cannot conceal a malformed interior receiver or cyclic parent graph.
    for (int i = 0; i < count; i++) {
      int p = parent[i];
      if (!Double.isFinite(ground[i]) || !Double.isFinite(stage[i]) || p < -1 || p >= count)
        throw new IllegalArgumentException("invalid watershed interior node");
      if (p < 0) continue;
      int dx = Math.abs(i % side - p % side);
      int dz = Math.abs(i / side - p / side);
      if (dx > 1 || dz > 1 || dx + dz == 0 || stage[p] > stage[i] + 1e-7)
        throw new IllegalArgumentException("invalid watershed interior receiver");
    }

    // A valid boundary trace does not prove the entire receiver graph is safe:
    // disconnected interior loops and boundary -> interior -> same-boundary
    // loops otherwise vanish as empty output. Verify the COMPLETE functional
    // parent graph, once, before memoizing or accepting any region port. This
    // iterative three-state DFS costs O(side^2) time/scratch; it does not depend
    // on caller ordering, recurse, or inspect another region.
    byte[] pending = new byte[count]; // 0 unseen, 1 on path, 2 globally verified
    int[] trail = new int[count];
    for (int start = 0; start < count; start++) {
      if (pending[start] != 0) continue;
      int at = start, depth = 0;
      while (at >= 0 && pending[at] == 0) {
        pending[at] = 1;
        trail[depth++] = at;
        at = parent[at];
      }
      if (at >= 0 && pending[at] == 1)
        throw new IllegalArgumentException("cyclic watershed receiver graph");
      while (depth > 0) pending[trail[--depth]] = 2;
    }
    // Reuse the same bounded arrays for local path verification/memoization.
    Arrays.fill(pending, (byte) 0);
    int[] destination = new int[count];
    int[] hops = new int[count];
    double[] crest = new double[count];
    Arrays.fill(destination, -2); // -2 unknown; -1 terminal non-port.
    for (int i = 0; i < count; i++) if (boundary(i, side)) {
      destination[i] = i;
      crest[i] = ground[i];
    }
    ArrayList<Route> routes = new ArrayList<>(4 * (side - 1));
    for (int a = 0; a < count; a++) {
      if (!boundary(a, side)) continue;
      int first = parent[a];
      if (first < 0 || boundary(first, side)) continue;
      int at = first, depth = 0;
      while (at >= 0 && destination[at] == -2) {
        if (pending[at] != 0 || depth >= count)
          throw new IllegalArgumentException("cyclic watershed interior receiver chain");
        pending[at] = 1;
        trail[depth++] = at;
        at = parent[at];
      }
      while (depth > 0) {
        int i = trail[--depth], p = parent[i];
        if (p < 0 || destination[p] < 0) {
          destination[i] = -1;
        } else {
          destination[i] = destination[p];
          hops[i] = Math.addExact(hops[p], 1);
          crest[i] = Math.max(ground[i], crest[p]);
        }
        pending[i] = 0;
      }
      int to = destination[first];
      if (to < 0 || to == a) continue;
      int countHops = Math.addExact(hops[first], 1);
      int fromX = world(originX, a % side, step);
      int fromZ = world(originZ, a / side, step);
      int toX = world(originX, to % side, step);
      int toZ = world(originZ, to / side, step);
      long firstKey = WatershedOutletTopology.key(
          world(originX, first % side, step), world(originZ, first / side, step));
      routes.add(new Route(fromX, fromZ, toX, toZ, firstKey,
          Math.max(ground[a], crest[first]), countHops));
    }
    return List.copyOf(routes);
  }

  private static int world(int origin, int index, int step) {
    return Math.toIntExact((long) origin + (long) index * step);
  }

  private static boolean boundary(int i, int side) {
    int x = i % side, z = i / side;
    return x == 0 || z == 0 || x == side - 1 || z == side - 1;
  }
}
