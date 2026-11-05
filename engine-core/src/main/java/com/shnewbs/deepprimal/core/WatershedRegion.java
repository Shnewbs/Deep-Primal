package com.shnewbs.deepprimal.core;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Immutable drainage, lake and channel geometry. No chunk/world reads or mutable simulation. */
public final class WatershedRegion {
  public static final int STEP = 32, SIDE = TerrainEngine.CELL / STEP + 1;
  private static final int N = SIDE * SIDE, TILE = 64, TILES = TerrainEngine.CELL / TILE;
  private static final double STREAM_AREA = 18_000;
  private static final int[] ORTHOGONAL = {-SIDE, SIDE, -1, 1};
  /** Channel revision 2: trunk reaches with at least this catchment keep a graded bed. */
  private static final double TRUNK_AREA = 400_000, GRADED_SLOPE = 22.1, GREAT_AREA = 1_200_000;
  /** Channel revision 3: only great trunks on a low coastal plain build a delta fan. */
  private static final double DELTA_AREA = 1_800_000, DELTA_MAX_STAGE = TerrainEngine.SEA + 6;
  private static final int DELTA_LIMIT = 6, DELTA_BRANCHES = 3;
  /** Channel revision 4: abandoned meander loops beside flat, unconfined lowland trunks. */
  private static final double OXBOW_AREA = 160_000, OXBOW_GRADE = .0035;
  private static final int OXBOW_LIMIT = 24;
  private final double ox, oz;
  private final boolean broadChannels, cascadeBeds, earthRivers, riverDeltas, floodplainLakes,
      budgetedDistributaries, deepChannels, deepWaterBodies;
  private final Oxbow[] oxbows;
  private final int[][] oxbowIndex;
  private final Distributary[] distributaries;
  private final TerrainNoise riverNoise;
  private final double[] incision = new double[N], chainage = new double[N], braiding = new double[N];
  private final double[] raw = new double[N],
      flood = new double[N],
      area = new double[N],
      stage = new double[N];
  private final boolean[] ocean = new boolean[N], lakeHalo = new boolean[N];
  private final int[] parent = new int[N], order = new int[N], lakeAt = new int[N];
  private final Lake[] lakes;
  private final Segment[] segments;
  private final int[][] index;
  private final Delta[] deltas;
  private final int[][] deltaIndex;
  private final int streams;
  private volatile boolean[] supportedTunnels;

  /** Explicit cross-region sea admission for new V8 priority-flood policies only. */
  @FunctionalInterface public interface OceanBoundary {
    boolean isOceanBoundary(double worldX, double worldZ);
  }

  public record Surface(
      double ground, double water, double sediment, double flowX, double flowZ, double speed) {}

  public record Probe(
      double x, double z, double stage, double radius, double area, boolean source) {}

  public record LakeProbe(
      double x,
      double z,
      double level,
      double area,
      double outletX,
      double outletZ,
      double minX,
      double minZ,
      double maxX,
      double maxZ) {}

  public record Statistics(
      int reaches, int headwaters, int lakes, int segments, long estimatedBytes) {}

  /** A planned distributary fan; {@code seaward} points from the mouth toward the ocean. */
  public record DeltaProbe(
      double mouthX,
      double mouthZ,
      double seawardX,
      double seawardZ,
      double stage,
      double reach,
      double halfWidth,
      double trunkRadius,
      int branches) {}

  private record Delta(
      double mouthX,
      double mouthZ,
      double inlandX,
      double inlandZ,
      double acrossX,
      double acrossZ,
      double stage,
      double reach,
      double halfWidth,
      double trunkRadius) {}

  private record DeltaHit(double ground, double weight) {}

  /**
   * A planned distributary branch and its share of the trunk discharge. Shares across one fan sum
   * to one, and the branch width follows sqrt(share) hydraulic geometry.
   */
  private record Distributary(
      int fan,
      int branch,
      double share,
      double startRadius,
      double endRadius,
      double forkX,
      double forkZ,
      double midX,
      double midZ,
      double endX,
      double endZ) {}

  /** A distributary branch's planned discharge share and geometry. */
  public record DistributaryProbe(
      int fan,
      int branch,
      double share,
      boolean budgeted,
      double startRadius,
      double midRadius,
      double endRadius,
      double forkX,
      double forkZ,
      double midX,
      double midZ,
      double endX,
      double endZ) {}

  /** A cut-off meander loop: a still-water crescent whose tips point back toward the river. */
  public record OxbowProbe(
      double x, double z, double level, double radius, double halfWidth, double riverStage) {}

  /**
   * {@code (ux,uz)} points from the loop centre through the middle of the arc (away from the
   * river); the arc spans {@code halfSpan} radians either side of it.
   */
  private record Oxbow(
      double cx,
      double cz,
      double ux,
      double uz,
      double radius,
      double halfWidth,
      double halfSpan,
      double level,
      double depth,
      double riverStage) {}

  private static final class Lake {
    final int id;
    int outlet = -1, lowest;
    double min = Double.POSITIVE_INFINITY, spill, level = Double.NaN, bathymetryDepth;
    int cells, x0 = SIDE, z0 = SIDE, x1, z1;

    Lake(int id) {
      this.id = id;
    }
  }

  private record Segment(
      double ax,
      double az,
      double bx,
      double bz,
      double sa,
      double sb,
      double ra,
      double rb,
      double aa,
      double ab,
      int sill,
      double crest,
      boolean source,
      double ca,
      double cb,
      double wa,
      double wb,
      double ka,
      double kb,
      double reach,
      boolean delta) {
    Segment(double ax, double az, double bx, double bz, double sa, double sb, double ra,
        double rb, double aa, double ab, int sill, double crest, boolean source) {
      this(ax, az, bx, bz, sa, sb, ra, rb, aa, ab, sill, crest, source, 0, 0, 0, 0, 0, 0, 0, false);
    }
  }

  public WatershedRegion(
      double ox, double oz, DoubleBinaryOperator height, DoubleBinaryOperator rain) {
    this(ox,oz,height,rain,false);
  }
  public WatershedRegion(
      double ox, double oz, DoubleBinaryOperator height, DoubleBinaryOperator rain, boolean broadChannels) {
    this(ox,oz,height,rain,broadChannels,false);
  }
  /**
   * {@code cascadeBeds} (saved channel revision 1) keeps steep reaches shallow: a channel bed
   * never sits deeper than its designed cross-section plus one downstream step below the stage.
   * Without it, fine terrain gullies under a coarse-routed stage become deep finite troughs.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, null);
  }

  /**
   * A non-null {@code riverNoise} selects saved channel revision 2 (Earth-scaled rivers): graded
   * trunk beds that incise canyons (steady-state stream power, slope ~ area^-1/2), hydraulic
   * geometry that widens great lowland rivers and estuary mouths, node-consistent meanders and
   * braided reaches with emergent bars. Revisions 0/1 pass null and replay exactly.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, false);
  }

  /**
   * A non-null {@code riverNoise} selects saved channel revision 2 (Earth-scaled rivers). Adding
   * {@code riverDeltas} selects channel revision 3: great trunks that meet the sea on a low
   * coastal plain build a sediment fan with a small distributary network. Revisions 0/1/2 pass
   * {@code false} and replay exactly.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, riverDeltas, false);
  }

  /**
   * {@code floodplainLakes} selects channel revision 4: flat, unconfined lowland trunks leave
   * oxbow lakes on their floodplain. Revisions 0-3 pass {@code false} and replay exactly.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, riverDeltas,
        floodplainLakes, false);
  }

  /**
   * {@code budgetedDistributaries} selects channel revision 5: each delta fan splits its trunk's
   * discharge into conserved branch shares, and every branch is widened according to
   * sqrt(discharge) hydraulic geometry. Revisions 0-4 pass {@code false} and replay exactly.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes,
      boolean budgetedDistributaries) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, riverDeltas,
        floodplainLakes, budgetedDistributaries, null);
  }

  /**
   * Optional per-lattice-boundary ocean authority. Null freezes the V1-V7
   * legacy rule that any wet outer node seeds the regional priority flood.
   * A non-null source may deny an inland low basin's false edge-ocean seed.
   * It must be deterministic, saved-world bound, and must not read chunks.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes,
      boolean budgetedDistributaries, OceanBoundary admittedSea) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise,
        riverDeltas, floodplainLakes, budgetedDistributaries, admittedSea, null);
  }

  /**
   * Opt-in V8 ocean interior authority. Unlike the older seed-only callback,
   * every below-sea node reached by the ocean flood must pass this predicate.
   * The caller must bind both callbacks to one immutable saved graph/profile.
   * Null retains the exact historical unconstrained ocean interior traversal.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes,
      boolean budgetedDistributaries, OceanBoundary admittedSea,
      OceanBoundary admittedOceanInterior) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, riverDeltas,
        floodplainLakes, budgetedDistributaries, admittedSea, admittedOceanInterior, false);
  }

  /**
   * {@code deepChannels} selects a discharge-scaled thalweg for flowing
   * channels, excluding delta branches and lake basins. It can be enabled
   * independently of distributary planning.
   */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes,
      boolean budgetedDistributaries, OceanBoundary admittedSea,
      OceanBoundary admittedOceanInterior, boolean deepChannels) {
    this(ox, oz, height, rain, broadChannels, cascadeBeds, riverNoise, riverDeltas,
        floodplainLakes, budgetedDistributaries, admittedSea, admittedOceanInterior,
        deepChannels, false);
  }

  /** {@code deepWaterBodies} deepens still lakes and oxbows without changing their saved levels. */
  public WatershedRegion(double ox, double oz, DoubleBinaryOperator height,
      DoubleBinaryOperator rain, boolean broadChannels, boolean cascadeBeds,
      TerrainNoise riverNoise, boolean riverDeltas, boolean floodplainLakes,
      boolean budgetedDistributaries, OceanBoundary admittedSea,
      OceanBoundary admittedOceanInterior, boolean deepChannels, boolean deepWaterBodies) {
    if (admittedOceanInterior != null && admittedSea == null)
      throw new IllegalArgumentException("ocean interior authority requires seeded ocean boundary");
    this.floodplainLakes = floodplainLakes;
    this.budgetedDistributaries = budgetedDistributaries;
    this.deepChannels = deepChannels;
    this.deepWaterBodies = deepWaterBodies;
    if (floodplainLakes && !riverDeltas)
      throw new IllegalArgumentException("Oxbow lakes require channel revision 3");
    if (budgetedDistributaries && !riverDeltas)
      throw new IllegalArgumentException("Distributary budgets require channel revision 3");
    this.broadChannels=broadChannels;
    this.cascadeBeds=cascadeBeds;
    this.riverNoise=riverNoise;
    this.earthRivers=riverNoise!=null;
    this.riverDeltas=riverDeltas;
    if (earthRivers && !(broadChannels && cascadeBeds))
      throw new IllegalArgumentException("Earth rivers require V7 broad cascade channels");
    if (riverDeltas && !earthRivers)
      throw new IllegalArgumentException("River deltas require Earth rivers");
    this.ox = ox;
    this.oz = oz;
    Arrays.fill(parent, -1);
    Arrays.fill(order, -1);
    boolean[] seen = new boolean[N];
    int[] sorted = new int[N];
    var queue =
        new PriorityQueue<Integer>(
            Comparator.<Integer>comparingDouble(a -> flood[a]).thenComparingInt(a -> a));
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        raw[a] = height.applyAsDouble(ox + x * STEP, oz + z * STEP);
        // Historical V1-V7 freezes the sea floor into every priority value.
        // A new explicitly source-bound ocean authority must NOT raise an
        // inland depression to sea level before its real spill can be routed.
        // Actual cross-region spill/discharge remains a separate open gate.
        flood[a] = admittedSea == null ? Math.max(TerrainEngine.SEA, raw[a]) : raw[a];
        area[a] = STEP * STEP * rain.applyAsDouble(ox + x * STEP, oz + z * STEP);
      }
    // Elevation alone does not make an inland depression part of the ocean.
    var sea = new ArrayDeque<Integer>();
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        if ((x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1)
            && raw[a] <= TerrainEngine.SEA
            && (admittedSea == null
                || admittedSea.isOceanBoundary(ox + x * STEP, oz + z * STEP))
            && !ocean[a]) {
          ocean[a] = true;
          sea.add(a);
        }
      }
    while (!sea.isEmpty()) {
      int a = sea.removeFirst(), x = a % SIDE, z = a / SIDE;
      seen[a] = true;
      stage[a] = TerrainEngine.SEA;
      area[a] = 0;
      for (int d : ORTHOGONAL) {
        int b = a + d;
        if (b < 0
            || b >= N
            || Math.abs(x - b % SIDE) + Math.abs(z - b / SIDE) != 1
            || ocean[b]
            || raw[b] > TerrainEngine.SEA
            || (admittedOceanInterior != null
                && !admittedOceanInterior.isOceanBoundary(
                    ox + (b % SIDE) * STEP, oz + (b / SIDE) * STEP))) continue;
        ocean[b] = true;
        sea.add(b);
      }
    }
    // Only coast/boundary land enters the heap; ocean interiors do no routing work.
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        if (seen[a]) continue;
        int seaReceiver = -1;
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx >= 0 && zz >= 0 && xx < SIDE && zz < SIDE && ocean[zz * SIDE + xx])
              seaReceiver = zz * SIDE + xx;
          }
        if (seaReceiver >= 0 || x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1) {
          seen[a] = true;
          parent[a] = seaReceiver;
          queue.add(a);
        }
      }
    int count = 0;
    while (!queue.isEmpty()) {
      int a = queue.remove();
      sorted[count] = a;
      order[a] = count++;
      int x = a % SIDE, z = a / SIDE;
      for (int dz = -1; dz <= 1; dz++)
        for (int dx = -1; dx <= 1; dx++) {
          int xx = x + dx, zz = z + dz;
          if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
          int b = zz * SIDE + xx;
          if (seen[b]) continue;
          seen[b] = true;
          parent[b] = a;
          flood[b] = Math.max(raw[b], flood[a]);
          queue.add(b);
        }
    }
    int[] child = new int[N];
    Arrays.fill(child, -1);
    for (int n = count - 1; n >= 0; n--) {
      int a = sorted[n], b = parent[a];
      if (b >= 0) {
        area[b] += area[a];
        if (child[b] < 0 || area[a] > area[child[b]]) child[b] = a;
      }
    }
    var bodies = new ArrayList<Lake>();
    bodies.add(null);
    var search = new ArrayDeque<Integer>();
    for (int a = 0; a < N; a++) {
      if (lakeAt[a] != 0 || flood[a] - raw[a] < .01 || ocean[a]) continue;
      var lake = new Lake(bodies.size());
      lake.spill = flood[a];
      bodies.add(lake);
      lakeAt[a] = lake.id;
      search.add(a);
      while (!search.isEmpty()) {
        int b = search.removeFirst();
        lake.cells++;
        if (raw[b] < lake.min) {
          lake.min = raw[b];
          lake.lowest = b;
        }
        int x = b % SIDE, z = b / SIDE;
        lake.x0 = Math.min(lake.x0, x);
        lake.z0 = Math.min(lake.z0, z);
        lake.x1 = Math.max(lake.x1, x);
        lake.z1 = Math.max(lake.z1, z);
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
            int c = zz * SIDE + xx;
            if (lakeAt[c] == 0
                && !ocean[c]
                && flood[c] - raw[c] >= .01
                && Math.abs(flood[c] - lake.spill) < 1e-7) {
              lakeAt[c] = lake.id;
              search.add(c);
            }
          }
      }
    }
    lakes = bodies.toArray(Lake[]::new);
    for (int n = 0; n < count; n++) {
      int a = sorted[n], b = parent[a];
      // Legacy outer roots always drain to the sea datum. Under an explicit
      // ocean-boundary policy a denied wet edge is an inland root instead: it
      // must not inherit sea height merely because it touches the region edge.
      // Cross-region spill/receiver exchange is still a separate open gate.
      double downstream = b < 0
          ? (admittedSea == null ? Math.max(TerrainEngine.SEA, raw[a] - .15)
              : raw[a] - .15)
          : stage[b];
      stage[a] = Math.max(downstream, Math.floor(flood[a] * 16) / 16);
      if (lakeAt[a] > 0) {
        var lake = lakes[lakeAt[a]];
        if (Double.isNaN(lake.level)) {
          lake.outlet = a;
          lake.level =
              lake.cells >= 3 && lake.spill - lake.min >= 1.5
                  ? Math.floor(lake.spill * 16) / 16
                  : Double.NEGATIVE_INFINITY;
          if (Double.isFinite(lake.level))
            lake.bathymetryDepth =
                Math.clamp(6 + Math.sqrt(lake.cells) * 1.2 + (lake.spill - lake.min) * .15, 8, 35);
        }
        if (Double.isFinite(lake.level)) stage[a] = Math.max(downstream, lake.level);
      }
      if (earthRivers && b >= 0) {
        double d = Math.hypot(px(a) - px(b), pz(a) - pz(b));
        chainage[a] = chainage[b] + d;
        boolean pool = lakeAt[a] > 0 && Double.isFinite(lakes[lakeAt[a]].level);
        if (!pool && area[a] >= TRUNK_AREA) {
          // A trunk river keeps pace with uplift: its bed stays graded through rising ground.
          double graded =
              Math.floor((stage[b] + GRADED_SLOPE / Math.sqrt(area[a]) * d) * 16) / 16;
          if (graded < stage[a]) {
            double lowered = Math.max(downstream, graded);
            incision[a] = stage[a] - lowered;
            stage[a] = lowered;
          }
        }
      }
    }
    if (earthRivers)
      for (int a = 0; a < N; a++) {
        int b = parent[a];
        if (b < 0 || area[a] < STREAM_AREA || ocean[a]) continue;
        double grade = (stage[a] - stage[b]) / Math.hypot(px(a) - px(b), pz(a) - pz(b));
        double base = Math.clamp(1.4 + Math.sqrt(area[a]) / 185, 2.1, 32);
        // Braided reaches: moderate gradient, enough discharge, not confined in a canyon.
        braiding[a] =
            smooth((grade - .002) / .003)
                * (1 - smooth((grade - .04) / .03))
                * smooth((base - 3) / 3)
                * (1 - smooth(incision[a] / 3))
                * smooth((stage[a] - TerrainEngine.SEA - 3) / 3)
                * smooth((riverNoise.field(px(a), pz(a), 1400, 9131) + .25) / .3);
      }
    for (int a = 0; a < N; a++)
      if (lakeAt[a] > 0 && Double.isFinite(lakes[lakeAt[a]].level)) {
        int x = a % SIDE, z = a / SIDE;
        for (int dz = -2; dz <= 2; dz++)
          for (int dx = -2; dx <= 2; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx >= 0 && zz >= 0 && xx < SIDE && zz < SIDE) lakeHalo[zz * SIDE + xx] = true;
          }
      }
    var lines = new ArrayList<Segment>();
    deltas = riverDeltas ? planDeltas() : new Delta[0];
    int heads = 0;
    for (int a = 0; a < N; a++) {
      int b = parent[a];
      if (b < 0 || area[a] < STREAM_AREA || ocean[a]) continue;
      boolean source = child[a] < 0 || area[child[a]] < STREAM_AREA;
      if (source) heads++;
      int up = child[a], down = parent[b];
      double ax = px(a), az = pz(a), bx = px(b), bz = pz(b), len = Math.hypot(bx - ax, bz - az);
      double tx = up < 0 ? bx - ax : bx - px(up), tz = up < 0 ? bz - az : bz - pz(up);
      double ux = down < 0 ? bx - ax : px(down) - ax, uz = down < 0 ? bz - az : pz(down) - az;
      double tl = Math.max(1, Math.hypot(tx, tz)), ul = Math.max(1, Math.hypot(ux, uz));
      double c1x = ax + tx / tl * len * .28, c1z = az + tz / tl * len * .28;
      double c2x = bx - ux / ul * len * .28, c2z = bz - uz / ul * len * .28;
      double lastX = ax, lastZ = az;
      if (earthRivers) {
        lastX += warp(ax, az, area[a], stage[a], 0);
        lastZ += warp(ax, az, area[a], stage[a], 1);
      }
      int sill =
          lakeAt[a] > 0 && lakes[lakeAt[a]].outlet == a && Double.isFinite(lakes[lakeAt[a]].level)
              ? lakeAt[a]
              : 0;
      for (int k = 1; k <= 8; k++) {
        double t = k / 8., s = 1 - t, before = (k - 1) / 8.;
        double x = s * s * s * ax + 3 * s * s * t * c1x + 3 * s * t * t * c2x + t * t * t * bx;
        double z = s * s * s * az + 3 * s * s * t * c1z + 3 * s * t * t * c2z + t * t * t * bz;
        if (earthRivers) {
          // Shared nodes take identical displacements, so confluences stay joined.
          double wa = k == 8 ? area[b] : lerp(area[a], area[b], t);
          double ws = k == 8 ? stage[b] : lerp(stage[a], stage[b], t);
          double wx = warp(x, z, wa, ws, 0), wz = warp(x, z, wa, ws, 1);
          x += wx;
          z += wz;
          double ka = lerp(incision[a], incision[b], before), kb = lerp(incision[a], incision[b], t);
          double ca = lerp(chainage[a], chainage[b], before), cb = lerp(chainage[a], chainage[b], t);
          double ba = lerp(braiding[a], braiding[b], before), bb = lerp(braiding[a], braiding[b], t);
          double sa = lerp(stage[a], stage[b], before), sb = lerp(stage[a], stage[b], t);
          lines.add(
              new Segment(
                  lastX, lastZ, x, z, sa, sb,
                  earthRadius(lerp(area[a], area[b], before), sa, ba, source, before),
                  earthRadius(lerp(area[a], area[b], t), sb, bb, source, t),
                  lerp(area[a], area[b], before),
                  lerp(area[a], area[b], t),
                  sill != 0 && k >= 5 && k <= 7 ? sill : 0,
                  sill == 0 ? 0 : Math.floor(lakes[sill].level),
                  source, ca, cb, ba, bb, ka, kb, canyonReach(Math.max(ka, kb)), false));
          lastX = x;
          lastZ = z;
          continue;
        }
        lines.add(
            new Segment(
                lastX,
                lastZ,
                x,
                z,
                lerp(stage[a], stage[b], before),
                lerp(stage[a], stage[b], t),
                radius(lerp(area[a], area[b], before), source, before),
                radius(lerp(area[a], area[b], t), source, t),
                lerp(area[a], area[b], before),
                lerp(area[a], area[b], t),
                sill != 0 && k >= 5 && k <= 7 ? sill : 0,
                sill == 0 ? 0 : Math.floor(lakes[sill].level),
                source));
        lastX = x;
        lastZ = z;
      }
    }
    distributaries = addDistributaries(lines);
    streams = heads;
    segments = lines.toArray(Segment[]::new);
    @SuppressWarnings("unchecked")
    ArrayList<Integer>[] buckets = new ArrayList[TILES * TILES];
    for (int n = 0; n < segments.length; n++) {
      var s = segments[n];
      double margin = Math.max(s.ra, s.rb) + 36 + s.reach;
      int x0 = tile(Math.min(s.ax, s.bx) - margin, ox),
          x1 = tile(Math.max(s.ax, s.bx) + margin, ox);
      int z0 = tile(Math.min(s.az, s.bz) - margin, oz),
          z1 = tile(Math.max(s.az, s.bz) + margin, oz);
      for (int z = z0; z <= z1; z++)
        for (int x = x0; x <= x1; x++) {
          int i = z * TILES + x;
          if (buckets[i] == null) buckets[i] = new ArrayList<>();
          buckets[i].add(n);
        }
    }
    index = new int[buckets.length][];
    for (int n = 0; n < index.length; n++)
      if (buckets[n] != null) index[n] = buckets[n].stream().mapToInt(Integer::intValue).toArray();
    deltaIndex = indexDeltas();
    oxbows = floodplainLakes ? planOxbows(height) : new Oxbow[0];
    oxbowIndex = indexOxbows();
  }

  /**
   * Places at most {@link #OXBOW_LIMIT} abandoned loops beside flat, unincised, unbraided trunk
   * reaches. Every candidate is rejected unless its whole footprint plus bank band stays clear of
   * channels, deltas, lakes, the sea and the region edge, and its sampled rim is flat enough to
   * hold still water at least one block below the lowest rim point.
   */
  private Oxbow[] planOxbows(DoubleBinaryOperator height) {
    var planned = new ArrayList<Oxbow>();
    for (int i = 3; i < segments.length && planned.size() < OXBOW_LIMIT; i += 8) {
      var s = segments[i];
      if (s.delta || s.source || s.aa < OXBOW_AREA || Math.max(s.ka, s.kb) > .5
          || Math.max(s.wa, s.wb) > .05 || s.sill != 0)
        continue;
      double vx = s.bx - s.ax, vz = s.bz - s.az, len = Math.hypot(vx, vz);
      if (len < 1 || (s.sa - s.sb) / len > OXBOW_GRADE || s.sb < TerrainEngine.SEA + 1.5) continue;
      double px = (s.ax + s.bx) * .5, pz = (s.az + s.bz) * .5;
      if (riverNoise.field(px, pz, 700, 9161) < -.25) continue;
      int node = Math.clamp((int) Math.round((pz - oz) / STEP), 0, SIDE - 1) * SIDE
          + Math.clamp((int) Math.round((px - ox) / STEP), 0, SIDE - 1);
      if (lakeHalo[node] || lakeAt[node] > 0) continue;
      double tx = vx / len, tz = vz / len;
      double side = riverNoise.field(px, pz, 300, 9163) >= 0 ? 1 : -1;
      double r = (s.ra + s.rb) * .5;
      double radius = Math.clamp(r * 1.5 + 8, 14, 64), halfWidth = Math.clamp(r * .45, 3, 14);
      double bank = Math.clamp(5 + r * .4, 5, 24);
      // The loop turns slightly with the old flow, as real cut-offs rarely sit square to a reach.
      double turn = riverNoise.field(px, pz, 500, 9165) * .45;
      double nx = -tz * side, nz = tx * side;
      double ux = nx * Math.cos(turn) - nz * Math.sin(turn),
          uz = nx * Math.sin(turn) + nz * Math.cos(turn);
      double offset = r + bank + 8 + radius + halfWidth;
      double cx = px + nx * offset, cz = pz + nz * offset;
      double halfSpan = 1.75 + riverNoise.field(px, pz, 400, 9167) * .25;
      double outer = radius + halfWidth + 6;
      if (cx - outer < ox + 48 || cz - outer < oz + 48
          || cx + outer > ox + TerrainEngine.CELL - 48 || cz + outer > oz + TerrainEngine.CELL - 48)
        continue;
      boolean clear = true;
      for (var o : planned)
        if (Math.hypot(o.cx - cx, o.cz - cz) < o.radius + o.halfWidth + outer + 12) clear = false;
      double rim = Double.POSITIVE_INFINITY, high = Double.NEGATIVE_INFINITY;
      for (int k = 0; k <= 32 && clear; k++) {
        double phi = (k / 16. - 1) * (halfSpan + .25);
        double dx = ux * Math.cos(phi) - uz * Math.sin(phi), dz = ux * Math.sin(phi) + uz * Math.cos(phi);
        for (double ring : new double[] {radius - halfWidth - 3, radius, radius + halfWidth + 3}) {
          double x = cx + dx * ring, z = cz + dz * ring;
          int g = Math.clamp((int) Math.round((z - oz) / STEP), 0, SIDE - 1) * SIDE
              + Math.clamp((int) Math.round((x - ox) / STEP), 0, SIDE - 1);
          if (ocean[g] || lakeAt[g] > 0 || lakeHalo[g] || nearChannel(x, z, 6)
              || deltaSurface(x, z, height.applyAsDouble(x, z)) != null) {
            clear = false;
            break;
          }
          double h = height.applyAsDouble(x, z);
          high = Math.max(high, h);
          if (ring != radius) rim = Math.min(rim, h);
        }
      }
      if (!clear || high - rim > 8 || rim - s.sb > 9) continue;
      double level = Math.floor(Math.min(rim - 1.2, s.sb + 1.5));
      if (level < TerrainEngine.SEA + 1) continue;
      planned.add(
          new Oxbow(cx, cz, ux, uz, radius, halfWidth, halfSpan, level,
              deepWaterBodies
                  ? Math.clamp(halfWidth * .8, 3, 10)
                  : Math.clamp(halfWidth * .35, 1.4, 3.4),
              s.sb));
    }
    return planned.toArray(Oxbow[]::new);
  }

  /** True when {@code (x,z)} lies within a channel or its bank band plus {@code margin}. */
  private boolean nearChannel(double x, double z, double margin) {
    int[] ids = index[tile(z, oz) * TILES + tile(x, ox)];
    if (ids == null) return false;
    for (int id : ids) {
      var s = segments[id];
      double vx = s.bx - s.ax, vz = s.bz - s.az, len = vx * vx + vz * vz;
      double t = len == 0 ? 0 : Math.clamp(((x - s.ax) * vx + (z - s.az) * vz) / len, 0, 1);
      double r = lerp(s.ra, s.rb, t);
      if (Math.hypot(x - s.ax - vx * t, z - s.az - vz * t)
          < r + Math.clamp(5 + r * .4, 5, 24) + s.reach + margin)
        return true;
    }
    return false;
  }

  private int[][] indexOxbows() {
    int[][] out = new int[TILES * TILES][];
    for (int n = 0; n < oxbows.length; n++) {
      var o = oxbows[n];
      double extent = o.radius + o.halfWidth + 8;
      for (int z = tile(o.cz - extent, oz); z <= tile(o.cz + extent, oz); z++)
        for (int x = tile(o.cx - extent, ox); x <= tile(o.cx + extent, ox); x++) {
          int i = z * TILES + x;
          int[] old = out[i];
          out[i] = old == null ? new int[] {n} : Arrays.copyOf(old, old.length + 1);
          out[i][out[i].length - 1] = n;
        }
    }
    return out;
  }

  /**
   * Signed distance outside the crescent (negative inside) and the local half-width. Tips taper
   * to points, so the lake closes without square ends.
   */
  private static double[] oxbowDistance(Oxbow o, double x, double z) {
    double dx = x - o.cx, dz = z - o.cz, rho = Math.hypot(dx, dz);
    double along = dx * o.ux + dz * o.uz, across = -dx * o.uz + dz * o.ux;
    double phi = Math.atan2(across, along);
    double taper = Math.sqrt(Math.max(0, 1 - Math.pow(Math.abs(phi) / o.halfSpan, 4)));
    double half = o.halfWidth * taper;
    if (Math.abs(phi) <= o.halfSpan && half > .5) return new double[] {Math.abs(rho - o.radius) - half, half};
    double end = Math.copySign(o.halfSpan, phi);
    double ex = o.cx + o.radius * (o.ux * Math.cos(end) - o.uz * Math.sin(end));
    double ez = o.cz + o.radius * (o.ux * Math.sin(end) + o.uz * Math.cos(end));
    return new double[] {Math.max(0, Math.hypot(x - ex, z - ez) - .5), .5};
  }

  public List<OxbowProbe> oxbowProbes() {
    var out = new ArrayList<OxbowProbe>();
    for (var o : oxbows)
      out.add(new OxbowProbe(o.cx + o.ux * o.radius, o.cz + o.uz * o.radius, o.level, o.radius,
          o.halfWidth, o.riverStage));
    return List.copyOf(out);
  }

  /**
   * Great trunks that meet the ocean on a low coastal plain. The fan extends inland from the
   * mouth, so it only reworks existing land/shallow coast inside the existing drainage domain.
   */
  private Delta[] planDeltas() {
    var mouths = new ArrayList<Integer>();
    for (int a = 0; a < N; a++) {
      int b = parent[a];
      if (b < 0 || !ocean[b] || area[a] < DELTA_AREA || stage[a] > DELTA_MAX_STAGE) continue;
      mouths.add(a);
    }
    if (mouths.isEmpty()) return new Delta[0];
    mouths.sort(
        Comparator.<Integer>comparingDouble(a -> -area[a]).thenComparingInt(a -> a));
    if (mouths.size() > DELTA_LIMIT) mouths = new ArrayList<>(mouths.subList(0, DELTA_LIMIT));
    mouths.sort(Comparator.naturalOrder());
    var planned = new ArrayList<Delta>();
    for (int a : mouths) {
      int b = parent[a];
      double mx = px(a), mz = pz(a), sx = px(b), sz = pz(b);
      double length = Math.hypot(sx - mx, sz - mz);
      if (length < 1e-6) continue;
      double seaX = (sx - mx) / length, seaZ = (sz - mz) / length;
      double trunk = earthRadius(area[a], stage[a], 0, false, 1);
      double reach = Math.clamp(trunk * 1.1 + 24, 40, 170);
      double halfWidth = Math.clamp(trunk * 1.8 + reach * .55, 44, 220);
      planned.add(
          new Delta(
              mx,
              mz,
              -seaX,
              -seaZ,
              -seaZ,
              seaX,
              stage[a],
              reach,
              halfWidth,
              trunk));
    }
    return planned.toArray(Delta[]::new);
  }

  private int[][] indexDeltas() {
    if (deltas.length == 0) return new int[TILES * TILES][];
    @SuppressWarnings("unchecked")
    ArrayList<Integer>[] buckets = new ArrayList[TILES * TILES];
    for (int n = 0; n < deltas.length; n++) {
      var d = deltas[n];
      double extent = Math.max(d.reach, d.halfWidth) + 8;
      int x0 = tile(d.mouthX - extent, ox), x1 = tile(d.mouthX + extent, ox);
      int z0 = tile(d.mouthZ - extent, oz), z1 = tile(d.mouthZ + extent, oz);
      for (int z = z0; z <= z1; z++)
        for (int x = x0; x <= x1; x++) {
          int i = z * TILES + x;
          if (buckets[i] == null) buckets[i] = new ArrayList<>();
          buckets[i].add(n);
        }
    }
    int[][] out = new int[buckets.length][];
    for (int n = 0; n < out.length; n++)
      if (buckets[n] != null) out[n] = buckets[n].stream().mapToInt(Integer::intValue).toArray();
    return out;
  }

  /** Three distributary reaches per fan, from a fork on the trunk down to the shoreline. */
  private Distributary[] addDistributaries(List<Segment> lines) {
    var planned = new ArrayList<Distributary>();
    for (int fan = 0; fan < deltas.length; fan++) {
      var d = deltas[fan];
      double[] shares = budgetedDistributaries ? distributaryShares(d) : null;
      double fork = Math.min(d.reach * .45, 220);
      double base = Math.clamp(1.2 + d.trunkRadius * .04, 1.6, 4.6);
      double startStage = d.stage + .45 * (fork / d.reach), endStage = TerrainEngine.SEA + .30;
      for (int branch = 0; branch < DELTA_BRANCHES; branch++) {
        double share = shares == null ? 1. / DELTA_BRANCHES : shares[branch];
        // Wider branch, more discharge: width ~ sqrt(Q) about the capacity-fair split.
        double startRadius =
            budgetedDistributaries
                ? Math.clamp(base * Math.sqrt(share * DELTA_BRANCHES), 1.2, 6.4)
                : base;
        double endRadius = Math.max(1.1, startRadius * .6);
        double lateral =
            (DELTA_BRANCHES == 1 ? 0 : branch / (double) (DELTA_BRANCHES - 1) * 1.24 - .62)
                + riverNoise.field(d.mouthX, d.mouthZ, 640, 9181 + branch) * .10;
        double forkX = d.mouthX + d.inlandX * fork, forkZ = d.mouthZ + d.inlandZ * fork;
        double midX =
            d.mouthX + d.inlandX * fork * .45 + d.acrossX * lateral * d.halfWidth * .5;
        double midZ =
            d.mouthZ + d.inlandZ * fork * .45 + d.acrossZ * lateral * d.halfWidth * .5;
        double endX = d.mouthX + d.acrossX * lateral * d.halfWidth + d.inlandX * .02;
        double endZ = d.mouthZ + d.acrossZ * lateral * d.halfWidth + d.inlandZ * .02;
        double midStage = lerp(startStage, endStage, .5);
        double midRadius = (startRadius + endRadius) * .5;
        lines.add(
            new Segment(
                forkX, forkZ, midX, midZ, startStage, midStage, startRadius, midRadius,
                d.halfWidth * 40, d.halfWidth * 30, 0, 0, false,
                fork, fork * .55, 0, 0, 0, 0, 0, true));
        lines.add(
            new Segment(
                midX, midZ, endX, endZ, midStage, endStage, midRadius, endRadius,
                d.halfWidth * 30, d.halfWidth * 22, 0, 0, false,
                fork * .55, 0, 0, 0, 0, 0, 0, true));
        planned.add(
            new Distributary(
                fan, branch, share, startRadius, endRadius,
                forkX, forkZ, midX, midZ, endX, endZ));
      }
    }
    return planned.toArray(Distributary[]::new);
  }

  /**
   * Deterministic discharge split for a fan. The middle branch takes the largest share and the
   * side branches less; every share is positive and the set sums to one, so no branch gains water
   * the trunk does not carry.
   */
  private double[] distributaryShares(Delta d) {
    double[] w = new double[DELTA_BRANCHES];
    double sum = 0;
    for (int b = 0; b < DELTA_BRANCHES; b++) {
      double bias = 1 - .42 * Math.abs(b - (DELTA_BRANCHES - 1) * .5);
      w[b] = bias * (1 + .22 * riverNoise.field(d.mouthX, d.mouthZ, 512, 9277 + b));
      sum += w[b];
    }
    for (int b = 0; b < DELTA_BRANCHES; b++) w[b] /= sum;
    return w;
  }

  public List<DistributaryProbe> distributaryProbes() {
    var out = new ArrayList<DistributaryProbe>(distributaries.length);
    for (var d : distributaries)
      out.add(
          new DistributaryProbe(
              d.fan,
              d.branch,
              d.share,
              budgetedDistributaries,
              d.startRadius,
              (d.startRadius + d.endRadius) * .5,
              d.endRadius,
              d.forkX,
              d.forkZ,
              d.midX,
              d.midZ,
              d.endX,
              d.endZ));
    return List.copyOf(out);
  }

  /**
   * Depositional fan surface at a point, or {@code null} outside every delta (or where the fan
   * would not raise the ground). The surface slopes from sea level + 0.35 at the shoreline to the
   * mouth stage + 0.45 at the inland apex, so it only claims low coastal ground and shallows.
   */
  private DeltaHit deltaSurface(double x, double z, double uncut) {
    if (deltaIndex.length == 0) return null;
    int[] ids = deltaIndex[tile(z, oz) * TILES + tile(x, ox)];
    if (ids == null) return null;
    double raised = uncut, weight = 0;
    for (int id : ids) {
      var d = deltas[id];
      double dx = x - d.mouthX, dz = z - d.mouthZ;
      double u = (dx * d.inlandX + dz * d.inlandZ) / d.reach;
      if (u < -.12 || u > 1) continue;
      double v = (dx * d.acrossX + dz * d.acrossZ) / d.halfWidth;
      if (Math.abs(v) > 1) continue;
      double w = 1 - smooth((Math.abs(v) - .70) / .30);
      w *= smooth((u + .12) / .12);
      if (w <= 0) continue;
      double top = lerp(TerrainEngine.SEA + .35, d.stage + .45, smooth(u));
      double blended = lerp(uncut, Math.max(uncut, top), w);
      if (blended > raised) raised = blended;
      weight = Math.max(weight, w);
    }
    return raised > uncut + 1e-12 && weight > 0 ? new DeltaHit(raised, weight) : null;
  }

  private double radius(double area, boolean source, double t) {
    if(broadChannels) {
      double r=Math.clamp(1.4+Math.sqrt(area)/185,2.1,32);
      return source?lerp(1.3,r,smooth(t*2)):r;
    }
    double r = Math.clamp(.7 + Math.sqrt(area) / 410, 1.05, 26);
    return source ? lerp(.9, r, smooth(t * 2)) : r;
  }

  private double px(int a) {
    return ox + (a % SIDE) * STEP;
  }

  /**
   * Hydraulic geometry (width ~ discharge^1/2) with great-river and estuary widening. Great
   * lowland trunks reach 128-220 blocks; canyon and upland reaches stay comparatively narrow.
   */
  private static double earthRadius(
      double area, double stage, double braid, boolean source, double t) {
    double base = Math.clamp(1.4 + Math.sqrt(area) / 185, 2.1, 32);
    double great = smooth((area - GREAT_AREA) / 4_800_000);
    double lowland = 1 - smooth((stage - TerrainEngine.SEA - 4) / 40);
    double estuary = 1 + .7 * great * (1 - smooth((stage - TerrainEngine.SEA) / 2.5));
    double r = Math.min(112, base * (1 + 3.2 * great * lowland) * estuary * (1 + .6 * braid));
    return source ? lerp(1.3, r, smooth(t * 2)) : r;
  }

  /** Meander displacement from a shared vector field; wavelength grows with river size. */
  private double warp(double x, double z, double area, double stage, int axis) {
    double r = Math.clamp(1.4 + Math.sqrt(area) / 185, 2.1, 32);
    double fade = smooth((stage - TerrainEngine.SEA - .5) / 3);
    if (fade <= 0) return 0;
    double large = smooth((r - 3) / 20);
    return fade
        * ((1 - large) * Math.min(9, 2 + r * 1.4) * riverNoise.noise(x, z, 110, 9101 + axis)
            + large * Math.min(48, 8 + r * 1.6) * riverNoise.noise(x, z, 420, 9103 + axis));
  }

  /** Horizontal reach of terraced canyon walls beyond the channel. */
  private static double canyonReach(double incision) {
    return incision <= 2 ? 0 : Math.min(96, 20 + incision * 1.2);
  }

  /** Stepped cliff-and-bench wall height above the water at distance {@code d} from the bank. */
  private double canyonWall(double x, double z, double d, double relief, double incision) {
    double rise = 7 + 3 * riverNoise.field(x, z, 300, 9141);
    double u = d / (rise * .7) + .5;
    double step = Math.floor(u);
    double terraces = rise * (step + smooth((u - step - .5) / .5));
    double reach = canyonReach(incision);
    return terraces + Math.max(0, relief) * smooth((d - reach * .55) / (reach * .45));
  }

  private double pz(int a) {
    return oz + (a / SIDE) * STEP;
  }

  private static int tile(double p, double origin) {
    return Math.clamp((int) Math.floor((p - origin) / TILE), 0, TILES - 1);
  }

  private double lakeWater(double x, double z) {
    int gx = Math.clamp((int) Math.floor((x - ox) / STEP), 0, SIDE - 2),
        gz = Math.clamp((int) Math.floor((z - oz) / STEP), 0, SIDE - 2);
    double u = Math.clamp((x - ox) / STEP - gx, 0, 1), v = Math.clamp((z - oz) / STEP - gz, 0, 1);
    int a = gz * SIDE + gx;
    double level = Double.NaN;
    for (int n = 0; n < 4; n++) {
      int c = a + (n & 1) + (n >>> 1) * SIDE;
      if (lakeAt[c] > 0 && Double.isFinite(lakes[lakeAt[c]].level))
        level =
            Double.isNaN(level) ? lakes[lakeAt[c]].level : Math.min(level, lakes[lakeAt[c]].level);
    }
    if (!Double.isFinite(level)) return Double.NaN;
    double coarse = lerp(lerp(raw[a], raw[a + 1], u), lerp(raw[a + SIDE], raw[a + SIDE + 1], u), v);
    return coarse < level ? level : Double.NaN;
  }

  /** V6-only roof footprint. A whole cross-section must have rock cover before its valley cut is suppressed. */
  public boolean tunnelSupport(double x,double z,double uncut,DoubleBinaryOperator height) {
    boolean[] eligible=supportedTunnels;
    if(eligible==null)synchronized(this){
      eligible=supportedTunnels;
      if(eligible==null){
        eligible=new boolean[segments.length];
        for(int id=0;id<segments.length;id++) {
          var s=segments[id];double vx=s.bx-s.ax,vz=s.bz-s.az,len=Math.hypot(vx,vz);
          if(Math.max(s.ka,s.kb)>2)continue; // an incised canyon stays open to the sky
          if(Math.min(s.sa,s.sb)<=TerrainEngine.SEA+8||height.applyAsDouble((s.ax+s.bx)*.5,(s.az+s.bz)*.5)-(s.sa+s.sb)*.5<9)continue;
          boolean covered=true;
          for(int n=0;n<3&&covered;n++) {
            double t=n*.5,cx=lerp(s.ax,s.bx,t),cz=lerp(s.az,s.bz,t),r=lerp(s.ra,s.rb,t)+2,stage=lerp(s.sa,s.sb,t);
            for(int side=-1;side<=1;side++)if(height.applyAsDouble(cx-vz/len*r*side,cz+vx/len*r*side)<stage+7){covered=false;break;}
          }
          eligible[id]=covered;
        }
        supportedTunnels=eligible;
      }
    }
    var candidates=index[tile(z,oz)*TILES+tile(x,ox)];if(candidates==null)return false;
    if(earthRivers)for(int id:candidates) {
      var s=segments[id];if(s.reach<=0)continue;
      double vx=s.bx-s.ax,vz=s.bz-s.az,len=vx*vx+vz*vz;
      double t=Math.clamp(((x-s.ax)*vx+(z-s.az)*vz)/len,0,1);
      if(Math.hypot(x-s.ax-vx*t,z-s.az-vz*t)<lerp(s.ra,s.rb,t)+s.reach)return false;
    }
    for(int id:candidates)if(eligible[id]) {
      var s=segments[id];double vx=s.bx-s.ax,vz=s.bz-s.az,len=vx*vx+vz*vz;
      double t=Math.clamp(((x-s.ax)*vx+(z-s.az)*vz)/len,0,1),r=lerp(s.ra,s.rb,t),stage=lerp(s.sa,s.sb,t);
      double bank=Math.clamp(5+r*.4+Math.sqrt(Math.max(0,uncut-stage))*2,5,24);
      if(Math.hypot(x-s.ax-vx*t,z-s.az-vz*t)<r+bank+1.5)return true;
    }
    return false;
  }

  public Surface sample(double x, double z, double uncut) {
    int gx = Math.clamp((int) Math.floor((x - ox) / STEP), 0, SIDE - 2),
        gz = Math.clamp((int) Math.floor((z - oz) / STEP), 0, SIDE - 2);
    double u = Math.clamp((x - ox) / STEP - gx, 0, 1), v = Math.clamp((z - oz) / STEP - gz, 0, 1);
    int a = gz * SIDE + gx;
    DeltaHit delta = deltaSurface(x, z, uncut);
    double base = delta == null ? uncut : delta.ground();
    boolean seaNear = ocean[a] || ocean[a + 1] || ocean[a + SIDE] || ocean[a + SIDE + 1];
    if (base < TerrainEngine.SEA && seaNear)
      return new Surface(Math.min(base, TerrainEngine.SEA - 1.001), TerrainEngine.SEA, 1, 0, 0, 0);
    double water = Double.NaN, ground = base, sediment = delta == null ? 0 : delta.weight() * .95,
        fx = 0, fz = 0, speed = 0;
    double sill = Double.NEGATIVE_INFINITY,
        joinedBed = Double.POSITIVE_INFINITY,
        bankFloor = Double.NEGATIVE_INFINITY;
    double lakeLevel = Double.NaN, channelFloor = Double.NEGATIVE_INFINITY, channelStage = Double.NaN;
    double bar = Double.NEGATIVE_INFINITY;
    for (int corner = 0; corner < 4; corner++) {
      int c = a + (corner & 1) + (corner >>> 1) * SIDE;
      if (lakeAt[c] > 0 && Double.isFinite(lakes[lakeAt[c]].level))
        lakeLevel =
            Double.isNaN(lakeLevel)
                ? lakes[lakeAt[c]].level
                : Math.min(lakeLevel, lakes[lakeAt[c]].level);
    }
    if (Double.isFinite(lakeLevel)) {
      double coarse =
          lerp(lerp(raw[a], raw[a + 1], u), lerp(raw[a + SIDE], raw[a + SIDE + 1], u), v);
      double detail = smooth((coarse - lakeLevel - 1) / 4);
      ground = lerp(coarse, base, detail);
      if (deepWaterBodies && coarse < lakeLevel) {
        Lake lake = null;
        for (int corner = 0; corner < 4; corner++) {
          int id = lakeAt[a + (corner & 1) + (corner >>> 1) * SIDE];
          if (id > 0 && Double.isFinite(lakes[id].level) && lakes[id].level == lakeLevel) {
            lake = lakes[id];
            break;
          }
        }
        if (lake != null) {
          double basinRange = Math.max(1, lakeLevel - lake.min);
          double basinPosition = Math.clamp((coarse - lake.min) / basinRange, 0, 1);
          double bowl = 1 - smooth(basinPosition);
          ground = Math.min(ground, lakeLevel - lake.bathymetryDepth * bowl);
        }
      }
      sediment = 1 - detail;
      double shoreStep =
          Math.max(
                  Math.max(
                      Math.abs(raw[a + 1] - raw[a]), Math.abs(raw[a + SIDE + 1] - raw[a + SIDE])),
                  Math.max(
                      Math.abs(raw[a + SIDE] - raw[a]), Math.abs(raw[a + SIDE + 1] - raw[a + 1])))
              / STEP;
      if (coarse < lakeLevel + shoreStep * 1.01)
        joinedBed = Math.min(joinedBed, Math.floor(lakeLevel) - 1.001);
      if (coarse >= lakeLevel && coarse < lakeLevel + shoreStep * 1.5)
        bankFloor = Math.max(bankFloor, lakeLevel + .35);
      if (coarse < lakeLevel) water = lakeLevel;
    }
    if (!Double.isFinite(water) && lakeHalo[a]) {
      for (int n = 0; n < 4; n++) {
        double neighbour =
            lakeWater(x + (n == 0 ? -1 : n == 1 ? 1 : 0), z + (n == 2 ? -1 : n == 3 ? 1 : 0));
        if (Double.isFinite(neighbour)) bankFloor = Math.max(bankFloor, neighbour + .35);
      }
    }
    int[] candidates = index[tile(z, oz) * TILES + tile(x, ox)];
    if (candidates != null)
      for (int id : candidates) {
        var s = segments[id];
        double vx = s.bx - s.ax, vz = s.bz - s.az, len = vx * vx + vz * vz;
        double t = Math.clamp(((x - s.ax) * vx + (z - s.az) * vz) / len, 0, 1);
        double dist = Math.hypot(x - s.ax - vx * t, z - s.az - vz * t), r = lerp(s.ra, s.rb, t);
        if (dist > r + 36 + s.reach) continue;
        boolean deepen =
            deepChannels && !s.delta && delta == null && !Double.isFinite(lakeLevel) && !lakeHalo[a];
        double level = lerp(s.sa, s.sb, t),
            braid = earthRivers ? lerp(s.wa, s.wb, t) : 0,
            depth =
                earthRivers
                    ? (deepen
                        ? Math.clamp(1 + 1.6 * Math.sqrt(r), 2.5, 20)
                        : Math.clamp(.6 + 1.1 * Math.sqrt(r), 1.5, 8)) * (1 - .45 * braid)
                    : Math.clamp(1.35 + r * .17, 1.5, 6),
            bank = Math.clamp(5 + r * .4 + Math.sqrt(Math.max(0, base - level)) * 2, 5, 24);
        if (dist < r + 1.01) {
          double grade = Math.max(0, (s.sa - s.sb) / Math.sqrt(len));
          joinedBed = Math.min(joinedBed, Math.floor(level - grade) - 1.001);
        }
        double grade = Math.max(0, (s.sa - s.sb) / Math.sqrt(len)),
            guard = level + .35 + grade * 1.5;
        if (dist < r + 1.5) bankFloor = Math.max(bankFloor, guard);
        else if (dist < r + bank + 1.5 && base < guard)
          bankFloor = Math.max(bankFloor, lerp(guard, base, smooth((dist - r - 1.5) / bank)));
        if (dist < r) {
          double edge = Math.clamp(dist / r, 0, 1);
          ground = Math.min(ground, level - depth + (depth - .3) * Math.pow(edge, 3));
          if (dist < Math.max(.8, r * .7)) {
            ground = Math.min(ground, Math.floor(level - grade) - 1.001);
            if (seaNear && base < TerrainEngine.SEA + 1)
              ground = Math.min(ground, TerrainEngine.SEA - 1.001);
          }
          if (Double.isNaN(water) || level < water) {
            water = level;
            // Steep mountain creeks are shallow; flat rivers keep their full designed depth.
            // Revision 1 holds about the film its runtime baseflow sustains, so new creeks do not
            // surge and drain the generated volume before settling.
            channelFloor = level - depth / (1 + 4 * grade) - Math.min(grade, 1) * .5 - .25;
            channelStage = level;
            double l = Math.sqrt(len);
            fx = vx / l;
            fz = vz / l;
            speed =
                Math.clamp(
                    .08 + Math.sqrt(Math.max(0, (s.sa - s.sb) / l)) * .9
                        + (earthRivers ? Math.min(r, 12) : r) * .006,
                    .08,
                    .85);
            bar = Double.NEGATIVE_INFINITY;
            if (earthRivers && braid > .2 && dist < r * .82 && Double.isNaN(lakeLevel) && !lakeHalo[a]) {
              // Braid bars: flow-elongated emergent islands, continuous along the chainage.
              double along = lerp(s.ca, s.cb, t);
              double across = ((x - s.ax) * -vz + (z - s.az) * vx) / l;
              double field = riverNoise.noise(along / (r * 2.4), across / (r * .6), 1, 9151);
              bar = (field - (.55 - .35 * braid)) / .3;
              bar *= 1 - smooth((dist / r - .6) / .22);
            }
          }
          if (s.sill > 0) sill = Math.max(sill, s.crest - 1 + .001);
        } else {
          double blend = smooth((dist - r) / bank);
          double wall = lerp(level + .35, base, blend);
          double cut = earthRivers ? lerp(s.ka, s.kb, t) : 0;
          if (cut > 2) {
            double canyon =
                Math.min(base, level + .35 + canyonWall(x, z, dist - r, base - level - .35, cut));
            wall = lerp(wall, canyon, smooth((cut - 2) / 6));
          }
          ground = Math.min(ground, wall);
        }
        sediment =
            Math.max(
                sediment,
                (1 - smooth((dist - r) / (bank + 8)))
                    * (earthRivers ? 1 - smooth((lerp(s.ka, s.kb, t) - 2) / 6) * .8 : 1));
      }
    if (oxbows.length > 0 && !Double.isFinite(water) && delta == null) {
      int[] ids = oxbowIndex[tile(z, oz) * TILES + tile(x, ox)];
      if (ids != null)
        for (int id : ids) {
          var o = oxbows[id];
          double[] hit = oxbowDistance(o, x, z);
          double e = hit[0];
          if (e < 0) {
            // Still water: a silted, bowl-shaped bed, deepest along the old thalweg.
            double edge = Math.clamp(1 + e / hit[1], 0, 1);
            ground = Math.min(ground, o.level - o.depth + (o.depth - .3) * Math.pow(edge, 3));
            water = o.level;
            sediment = 1;
            fx = fz = speed = 0;
            break;
          }
          if (e < 6) {
            bankFloor = Math.max(bankFloor, lerp(o.level + .35, base, smooth((e - 1.5) / 4.5)));
            sediment = Math.max(sediment, 1 - smooth(e / 6));
          }
        }
    }
    if (Double.isFinite(water) && bar > 0) {
      // An emergent bar stays dry above the rounded stage; contained banks surround it.
      double stage = Math.rint(Math.max(TerrainEngine.SEA, water) * 16) / 16;
      return new Surface(
          Math.floor(stage) + .999 + (smooth(bar) > .65 ? 1 : 0), Double.NaN, 1, 0, 0, 0);
    }
    if (Double.isFinite(water)) {
      water = Math.rint(Math.max(TerrainEngine.SEA, water) * 16) / 16;
      ground = Math.min(ground, joinedBed);
      // A steep junction is a short fall onto the lower reach, not a pit below this stage.
      if (cascadeBeds && channelFloor > TerrainEngine.SEA - 1
          && (Double.isNaN(lakeLevel) || channelStage < lakeLevel))
        ground = Math.max(ground, channelFloor);
      ground = Math.min(ground, Math.ceil(water) - 1.001);
      if (Double.isFinite(sill))
        ground = Math.max(ground, Math.min(sill, Math.ceil(water) - 1.001));
    } else if (Double.isFinite(bankFloor)) ground = Math.max(ground, bankFloor);
    return new Surface(ground, water, sediment, fx, fz, speed);
  }

  public List<Probe> probes() {
    var out = new ArrayList<Probe>();
    for (int i = 0; i < segments.length; i += 8) {
      var s = segments[i];
      out.add(new Probe(s.ax, s.az, s.sa, s.ra, s.aa, s.source));
    }
    return List.copyOf(out);
  }

  public List<Probe> centerline() {
    var out = new ArrayList<Probe>();
    for (var s : segments) {
      out.add(new Probe(s.ax, s.az, s.sa, s.ra, s.aa, s.source));
      out.add(new Probe(s.bx, s.bz, s.sb, s.rb, s.ab, s.source));
    }
    return List.copyOf(out);
  }

  /** Distributary reach endpoints only; revision 3 fans expose three branches per mouth. */
  public List<Probe> deltaCenterline() {
    var out = new ArrayList<Probe>();
    for (var s : segments) {
      if (!s.delta) continue;
      out.add(new Probe(s.ax, s.az, s.sa, s.ra, s.aa, false));
      out.add(new Probe(s.bx, s.bz, s.sb, s.rb, s.ab, false));
    }
    return List.copyOf(out);
  }

  public List<DeltaProbe> deltaProbes() {
    var out = new ArrayList<DeltaProbe>(deltas.length);
    for (var d : deltas)
      out.add(
          new DeltaProbe(
              d.mouthX,
              d.mouthZ,
              -d.inlandX,
              -d.inlandZ,
              d.stage,
              d.reach,
              d.halfWidth,
              d.trunkRadius,
              DELTA_BRANCHES));
    return List.copyOf(out);
  }

  public List<LakeProbe> lakeProbes() {
    var out = new ArrayList<LakeProbe>();
    for (int i = 1; i < lakes.length; i++) {
      var l = lakes[i];
      if (Double.isFinite(l.level))
        out.add(
            new LakeProbe(
                px(l.lowest),
                pz(l.lowest),
                l.level,
                l.cells * (double) STEP * STEP,
                px(l.outlet),
                pz(l.outlet),
                ox + (l.x0 - 1) * STEP,
                oz + (l.z0 - 1) * STEP,
                ox + (l.x1 + 1) * STEP,
                oz + (l.z1 + 1) * STEP));
    }
    return List.copyOf(out);
  }

  public Statistics statistics() {
    long refs = 0;
    for (var bucket : index) if (bucket != null) refs += bucket.length;
    return new Statistics(
        segments.length / 8,
        streams,
        lakeProbes().size(),
        segments.length,
        N * (8L * 4 + 4 * 3 + 2 + (earthRivers ? 24 : 0))
            + segments.length * (earthRivers ? 176L : 112L)
            + index.length * 8L
            + refs * 4
            + deltas.length * 80L
            + deltaIndex.length * 8L);
  }

  public void verify() {
    for (int a = 0; a < N; a++) {
      if (ocean[a] && lakeAt[a] != 0)
        throw new AssertionError("Connected ocean entered a lake component");
      int b = parent[a];
      if (b >= 0 && order[a] >= 0 && (order[b] >= order[a] || stage[b] > stage[a] + 1e-7))
        throw new AssertionError("Watershed routing/head order");
    }
  }

  /** Bounded observational port receipt for new V8 cross-region outlet planning.
   * Not a live runoff ledger or a change to legacy geography. Every perimeter
   * lattice node is returned exactly once including signed corners. */
  public record BoundaryNode(int x, int z, double ground, double floodedElevation,
      double plannedStage, double catchmentArea, boolean ocean, boolean lake,
      Long downstreamKey) {}

  public record BoundarySnapshot(int originX, int originZ, List<BoundaryNode> nodes) {
    public BoundarySnapshot { nodes = List.copyOf(nodes); }
  }


  /** Explicit, trusted whole-region receiver path export for outlet planning. */
  public record RoutingSnapshot(BoundarySnapshot boundary,
      List<WatershedInteriorPortRoutes.Route> routes) {
    public RoutingSnapshot {
      Objects.requireNonNull(boundary, "boundary");
      routes = List.copyOf(routes);
    }
  }

  /**
   * Opt-in interior-to-perimeter receiver observations. The existing boundary
   * snapshot verifies the entire producer first. No water is moved, and a
   * routed path without an observed boundary exit remains unresolved.
   */
  public RoutingSnapshot routingSnapshot() {
    BoundarySnapshot boundary = boundarySnapshot();
    return new RoutingSnapshot(boundary, WatershedInteriorPortRoutes.trace(
        boundary.originX(), boundary.originZ(), SIDE, STEP, raw, stage, parent));
  }

  /** Explicit opt-in export; no construction or gameplay path calls this by default. */
  public BoundarySnapshot boundarySnapshot() {
    // The exporter is opt-in, but once invoked it must never hand an unverified
    // parent tree to the cross-region adapter. This checks all interior cells,
    // not only the perimeter observations available to the bridge.
    verify();
    if (!Double.isFinite(ox) || !Double.isFinite(oz)
        || ox != Math.rint(ox) || oz != Math.rint(oz)
        || ox < Integer.MIN_VALUE || ox > Integer.MAX_VALUE
        || oz < Integer.MIN_VALUE || oz > Integer.MAX_VALUE)
      throw new IllegalStateException("watershed origin cannot be represented exactly");
    int originX = (int) ox, originZ = (int) oz;
    if (Math.floorMod(originX, TerrainEngine.CELL) != 0
        || Math.floorMod(originZ, TerrainEngine.CELL) != 0)
      throw new IllegalStateException("watershed origin is not region-aligned");
    ArrayList<BoundaryNode> result = new ArrayList<>(4 * (SIDE - 1));
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        if (x != 0 && z != 0 && x != SIDE - 1 && z != SIDE - 1) continue;
        int i = z * SIDE + x, receiver = parent[i];
        long worldX = (long) originX + (long) x * STEP;
        long worldZ = (long) originZ + (long) z * STEP;
        if (worldX < Integer.MIN_VALUE || worldX > Integer.MAX_VALUE
            || worldZ < Integer.MIN_VALUE || worldZ > Integer.MAX_VALUE)
          throw new IllegalStateException("watershed boundary exceeds integer coordinates");
        Long downstream = null;
        if (receiver >= 0) {
          long nextX = (long) originX + (long) (receiver % SIDE) * STEP;
          long nextZ = (long) originZ + (long) (receiver / SIDE) * STEP;
          if (nextX < Integer.MIN_VALUE || nextX > Integer.MAX_VALUE
              || nextZ < Integer.MIN_VALUE || nextZ > Integer.MAX_VALUE)
            throw new IllegalStateException("watershed receiver exceeds integer coordinates");
          downstream = WatershedOutletTopology.key((int) nextX, (int) nextZ);
        }
        result.add(new BoundaryNode((int) worldX, (int) worldZ, raw[i], flood[i],
            stage[i], area[i], ocean[i], lakeAt[i] > 0, downstream));
      }
    return new BoundarySnapshot(originX, originZ, result);
  }

}
