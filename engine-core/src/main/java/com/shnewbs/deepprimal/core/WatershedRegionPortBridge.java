package com.shnewbs.deepprimal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Conservative, explicit-call-only adapter between actual watershed boundary
 * samples and the versioned outlet-topology solver. This is a planning draft,
 * NOT physical water, a native V8 integration, or a complete global flood.
 *
 * <p>Shared signed-coordinate grid nodes are owned once, and mismatched saved
 * terrain heights fail closed instead of producing a seam. Missing adjacent
 * regions are retained as unknown frontiers: a finite sample cannot prove an
 * enclosed lake. Ocean anchors MUST be independently supplied by a verified
 * saved ocean-domain policy and must agree with every sampled watershed.
 *
 * <p>Port runoff is deliberately zero: `area[]` in a WatershedRegion is
 * cumulative rainfall/catchment and summing it across boundary nodes would
 * create fictitious volume. Exact cross-region discharge still needs an
 * authority that assigns each physical input exactly once.
 */
public final class WatershedRegionPortBridge {
  private WatershedRegionPortBridge() {}

  /** An immutable seam draft. Solving this DOES NOT change live water. */
  public record Draft(List<WatershedOutletTopology.Port> ports,
      List<WatershedOutletTopology.Link> links,
      int sharedCoordinates, int unresolvedFrontiers) {
    public Draft {
      ports = List.copyOf(ports);
      links = List.copyOf(links);
    }
    public WatershedOutletTopology.Solution solve(double seaStage) {
      return WatershedOutletTopology.solve(ports, links, seaStage);
    }
  }

  private static final class Sample {
    final int x, z;
    final double floor;
    boolean allOcean;
    int copies;
    Sample(WatershedRegion.BoundaryNode n) {
      x = n.x(); z = n.z(); floor = n.ground(); allOcean = n.ocean();
      copies = 1;
    }
    void merge(WatershedRegion.BoundaryNode n) {
      if (Double.doubleToLongBits(floor) != Double.doubleToLongBits(n.ground()))
        throw new IllegalArgumentException("different saved terrain heights at watershed seam");
      copies++;
      allOcean &= n.ocean();
    }
  }

  private record Seam(long a, long b) {}

  /**
   * Assemble a bounded, reproducible draft from actual region boundary nodes.
   * Ocean keys are an external, explicitly verified saved-profile input; empty
   * means NO ocean certification, never elevation-based inference.
   */
  public static Draft assemble(Collection<WatershedRegion.BoundarySnapshot> snapshots,
      Set<Long> certifiedOceanKeys) {
    Objects.requireNonNull(snapshots, "snapshots");
    Objects.requireNonNull(certifiedOceanKeys, "certifiedOceanKeys");
    if (snapshots.isEmpty() || snapshots.size() > 12)
      throw new IllegalArgumentException("regional watershed admission budget (1..12)");

    TreeMap<Long, Sample> samples = new TreeMap<>();
    HashSet<Long> seenOrigins = new HashSet<>();
    Map<Seam, Double> links = new HashMap<>();
    int expectedBoundaryCount = 4 * (WatershedRegion.SIDE - 1);
    for (WatershedRegion.BoundarySnapshot snapshot : snapshots) {
      Objects.requireNonNull(snapshot, "snapshot");
      int ox = snapshot.originX(), oz = snapshot.originZ();
      if (Math.floorMod(ox, TerrainEngine.CELL) != 0
          || Math.floorMod(oz, TerrainEngine.CELL) != 0
          || !seenOrigins.add(((long) ox << 32) | (oz & 0xffffffffL)))
        throw new IllegalArgumentException("unaligned or repeated watershed origin");
      if (snapshot.nodes().size() != expectedBoundaryCount)
        throw new IllegalArgumentException("incomplete watershed boundary snapshot");
      long endX = (long) ox + TerrainEngine.CELL;
      long endZ = (long) oz + TerrainEngine.CELL;
      HashMap<Long, WatershedRegion.BoundaryNode> local = new HashMap<>();
      for (var node : snapshot.nodes()) {
        Objects.requireNonNull(node, "boundary node");
        int x = node.x(), z = node.z();
        long xx = x, zz = z;
        if (xx < ox || xx > endX || zz < oz || zz > endZ
            || !(xx == ox || xx == endX || zz == oz || zz == endZ)
            || Math.floorMod((long) x - ox, WatershedOutletTopology.STEP) != 0
            || Math.floorMod((long) z - oz, WatershedOutletTopology.STEP) != 0
            || !Double.isFinite(node.ground())
            || !Double.isFinite(node.floodedElevation())
            || !Double.isFinite(node.plannedStage())
            || !Double.isFinite(node.catchmentArea())
            || node.catchmentArea() < 0)
          throw new IllegalArgumentException("invalid watershed border measurement");
        long key = WatershedOutletTopology.key(x, z);
        if (local.putIfAbsent(key, node) != null)
          throw new IllegalArgumentException("repeated boundary node in one region");
        Sample sample = samples.get(key);
        if (sample == null) samples.put(key, new Sample(node));
        else sample.merge(node);
      }
      int step = WatershedOutletTopology.STEP;
      // Four actual 32-block perimeter walks. No interior shortcuts are
      // invented; their true minimum-spill connections remain an open gate.
      for (int i = 0; i < WatershedRegion.SIDE - 1; i++) {
        long offset = (long) i * step;
        join(local, links, (long) ox + offset, oz, (long) ox + offset + step, oz);
        join(local, links, (long) ox + offset, endZ, (long) ox + offset + step, endZ);
        join(local, links, ox, (long) oz + offset, ox, (long) oz + offset + step);
        join(local, links, endX, (long) oz + offset, endX, (long) oz + offset + step);
      }
    }
    if (samples.size() > WatershedOutletTopology.MAX_PORTS
        || links.size() > WatershedOutletTopology.MAX_LINKS)
      throw new IllegalArgumentException("watershed topology draft exceeds admission budget");
    for (Long key : certifiedOceanKeys) if (!samples.containsKey(key))
      throw new IllegalArgumentException("ocean certificate for unobserved port");

    int shared = 0, unresolved = 0;
    List<WatershedOutletTopology.Port> ports = new ArrayList<>(samples.size());
    for (var entry : samples.entrySet()) {
      Sample s = entry.getValue();
      int expectedCopies = (Math.floorMod(s.x, TerrainEngine.CELL) == 0 ? 2 : 1)
          * (Math.floorMod(s.z, TerrainEngine.CELL) == 0 ? 2 : 1);
      if (s.copies > expectedCopies)
        throw new IllegalArgumentException("overlapping watershed ownership");
      boolean unknown = s.copies != expectedCopies;
      if (unknown) unresolved++;
      if (s.copies > 1) shared++;
      boolean certified = certifiedOceanKeys.contains(entry.getKey());
      if (certified && (!s.allOcean || unknown || s.floor > TerrainEngine.SEA))
        throw new IllegalArgumentException("uncorroborated saved ocean anchor");
      ports.add(new WatershedOutletTopology.Port(s.x, s.z, s.floor, 0L,
          certified, unknown));
    }
    List<WatershedOutletTopology.Link> edges = new ArrayList<>(links.size());
    links.entrySet().stream().sorted(Comparator.comparingLong((Map.Entry<Seam, Double> e)
        -> e.getKey().a()).thenComparingLong(e -> e.getKey().b()))
        .forEach(e -> edges.add(new WatershedOutletTopology.Link(
            (int) (e.getKey().a() >> 32), (int) e.getKey().a(),
            (int) (e.getKey().b() >> 32), (int) e.getKey().b(),
            e.getValue())));
    return new Draft(ports, edges, shared, unresolved);
  }

  private static void join(Map<Long, WatershedRegion.BoundaryNode> local,
      Map<Seam, Double> links, long x1, long z1, long x2, long z2) {
    if (x1 < Integer.MIN_VALUE || x1 > Integer.MAX_VALUE
        || x2 < Integer.MIN_VALUE || x2 > Integer.MAX_VALUE
        || z1 < Integer.MIN_VALUE || z1 > Integer.MAX_VALUE
        || z2 < Integer.MIN_VALUE || z2 > Integer.MAX_VALUE)
      throw new IllegalArgumentException("watershed region crosses integer world range");
    long first = WatershedOutletTopology.key((int) x1, (int) z1);
    long second = WatershedOutletTopology.key((int) x2, (int) z2);
    var a = local.get(first);
    var b = local.get(second);
    if (a == null || b == null)
      throw new IllegalArgumentException("missing perimeter walk node");
    Seam edge = new Seam(Math.min(first, second), Math.max(first, second));
    double sill = Math.max(a.ground(), b.ground());
    Double previous = links.putIfAbsent(edge, sill);
    if (previous != null && Double.doubleToLongBits(previous) != Double.doubleToLongBits(sill))
      throw new IllegalArgumentException("opposing watershed sill disagrees");
  }
}
