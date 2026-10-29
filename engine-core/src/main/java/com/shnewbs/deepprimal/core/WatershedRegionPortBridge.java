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
 * terrain heights, priority-flood stages, catchment, classification and
 * downstream receiver identity fail closed instead of producing a false seam.
 * A region boundary can have different independent priority floods; their
 * disagreement MUST be reconciled by an authoritative cross-region planner,
 * never silently merged by this observational adapter. Missing adjacent
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
    final WatershedRegion.BoundaryNode first;
    boolean allOcean;
    int copies;
    Sample(WatershedRegion.BoundaryNode n) {
      x = n.x(); z = n.z(); floor = n.ground(); first = n;
      allOcean = n.ocean();
      copies = 1;
    }
    void merge(WatershedRegion.BoundaryNode n) {
      if (Double.doubleToLongBits(floor) != Double.doubleToLongBits(n.ground()))
        throw new IllegalArgumentException("different saved terrain heights at watershed seam");
      // Both regions must agree on ALL observed routing quantities for one
      // absolute lattice node. Same ground is not a valid same-water proof.
      // Bit-exact admission avoids order-dependent first/last sample wins.
      if (Double.doubleToLongBits(first.floodedElevation())
              != Double.doubleToLongBits(n.floodedElevation())
          || Double.doubleToLongBits(first.plannedStage())
              != Double.doubleToLongBits(n.plannedStage()))
        throw new IllegalArgumentException("different priority-flood or stage at watershed seam");
      if (Double.doubleToLongBits(first.catchmentArea())
              != Double.doubleToLongBits(n.catchmentArea()))
        throw new IllegalArgumentException("different catchment accounting at watershed seam");
      if (first.ocean() != n.ocean() || first.lake() != n.lake())
        throw new IllegalArgumentException("different water classification at watershed seam");
      if (!Objects.equals(first.downstreamKey(), n.downstreamKey()))
        throw new IllegalArgumentException("different downstream receiver at watershed seam");
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
        // The production watershed's parent[] is an adjacent 8-neighbour
        // lattice index within this region. Reject forged/out-of-range edges
        // before a receiver can be used as a trusted cross-region observation.
        validateReceiver(node, ox, oz, endX, endZ);
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
      // A parent directly connecting two observed perimeter nodes is real
      // watershed topology, including diagonal corner crossings. Previously
      // the bridge supplied only artificial orthogonal perimeter walks.
      // Interior parents are NOT assumed to exit at another observed port:
      // those paths still require an authoritative interior export.
      for (var node : local.values()) {
        Long receiver = node.downstreamKey();
        if (receiver == null || !local.containsKey(receiver)) continue;
        var downstream = local.get(receiver);
        if (node.plannedStage() + 1e-7 < downstream.plannedStage())
          throw new IllegalArgumentException("inverted watershed receiver stage");
        join(local, links, node.x(), node.z(), downstream.x(), downstream.z());
      }
    }
    if (samples.size() > WatershedOutletTopology.MAX_PORTS
        || links.size() > WatershedOutletTopology.MAX_LINKS)
      throw new IllegalArgumentException("watershed topology draft exceeds admission budget");
    // Converting directed receivers into undirected spill edges must not erase
    // a cycle. Every observed absolute-coordinate parent must be acyclic even
    // when its stage is exactly equal to its receiver's stage.
    validateObservedReceiverForest(samples);
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

  /**
   * Explicitly incorporate receiver paths exported by the SAME production
   * watershed that supplied each perimeter snapshot. A path is a measured
   * interior connection, not a guessed straight line or a new water source.
   * The ordinary perimeter-only assemble() contract is preserved exactly.
   *
   * <p>A bridge can validate route endpoints/first receivers/stages but cannot
   * independently certify the hidden interior path. Only pass trusted output
   * from WatershedRegion.routingSnapshot(); no caller-authored route lists.
   */
  public static Draft assembleRouted(
      Collection<WatershedRegion.RoutingSnapshot> snapshots,
      Set<Long> certifiedOceanKeys) {
    Objects.requireNonNull(snapshots, "snapshots");
    List<WatershedRegion.BoundarySnapshot> boundaries = new ArrayList<>();
    for (var snapshot : snapshots) {
      Objects.requireNonNull(snapshot, "routing snapshot");
      boundaries.add(Objects.requireNonNull(snapshot.boundary(), "routing boundary"));
    }
    Draft base = assemble(boundaries, certifiedOceanKeys);
    TreeMap<Seam, Double> allLinks = new TreeMap<>(Comparator
        .comparingLong(Seam::a).thenComparingLong(Seam::b));
    for (var link : base.links()) {
      long a = WatershedOutletTopology.key(link.ax(), link.az());
      long b = WatershedOutletTopology.key(link.bx(), link.bz());
      allLinks.put(new Seam(Math.min(a, b), Math.max(a, b)), link.sill());
    }
    Map<Long, WatershedInteriorPortRoutes.Route> usedSources = new HashMap<>();
    for (var snapshot : snapshots) {
      var boundary = snapshot.boundary();
      int ox = boundary.originX(), oz = boundary.originZ();
      long ex = (long) ox + TerrainEngine.CELL;
      long ez = (long) oz + TerrainEngine.CELL;
      Map<Long, WatershedRegion.BoundaryNode> local = new HashMap<>();
      for (var node : boundary.nodes())
        local.put(WatershedOutletTopology.key(node.x(), node.z()), node);
      if (snapshot.routes().size() > boundary.nodes().size())
        throw new IllegalArgumentException("too many watershed interior routes");
      Set<Long> seenLocal = new HashSet<>();
      for (var route : snapshot.routes()) {
        Objects.requireNonNull(route, "interior route");
        long from = WatershedOutletTopology.key(route.fromX(), route.fromZ());
        long to = WatershedOutletTopology.key(route.toX(), route.toZ());
        var source = local.get(from);
        var target = local.get(to);
        int firstX = (int) (route.firstReceiverKey() >> 32);
        int firstZ = (int) route.firstReceiverKey();
        // A first receiver on the same perimeter is already handled by
        // assemble(). Only a real interior path warrants a new shortcut.
        boolean firstInterior = firstX > ox && (long) firstX < ex
            && firstZ > oz && (long) firstZ < ez;
        if (source == null || target == null || from == to || !firstInterior
            || !Objects.equals(source.downstreamKey(), route.firstReceiverKey())
            || route.hops() < 2 || route.hops() > WatershedRegion.SIDE * WatershedRegion.SIDE
            || route.crest() < Math.max(source.ground(), target.ground())
            || target.plannedStage() > source.plannedStage() + 1e-7
            || !seenLocal.add(from))
          throw new IllegalArgumentException("invalid interior-to-port receiver evidence");
        var previous = usedSources.putIfAbsent(from, route);
        if (previous != null && !previous.equals(route))
          throw new IllegalArgumentException("conflicting shared watershed interior route");
        Seam edge = new Seam(Math.min(from, to), Math.max(from, to));
        // Both perimeter and internal corridors are observed alternatives.
        // The smallest certified crest represents the lowest known pass.
        allLinks.merge(edge, route.crest(), Math::min);
      }
    }
    if (allLinks.size() > WatershedOutletTopology.MAX_LINKS)
      throw new IllegalArgumentException("interior routes exceed outlet link budget");
    List<WatershedOutletTopology.Link> links = new ArrayList<>(allLinks.size());
    for (var entry : allLinks.entrySet()) {
      long a = entry.getKey().a(), b = entry.getKey().b();
      links.add(new WatershedOutletTopology.Link((int) (a >> 32), (int) a,
          (int) (b >> 32), (int) b, entry.getValue()));
    }
    return new Draft(base.ports(), links, base.sharedCoordinates(),
        base.unresolvedFrontiers());
  }

  /**
   * Fail closed for directed cycles in the observed perimeter subgraph.
   * Missing interior nodes terminate the observable chain: only the actual
   * watershed producer can attest to the complete parent ordering. The
   * traversal is iterative and O(observed nodes), avoiding recursion depth
   * and relying on no hash-map iteration order for the returned draft.
   */
  private static void validateObservedReceiverForest(TreeMap<Long, Sample> samples) {
    HashSet<Long> settled = new HashSet<>();
    HashSet<Long> visiting = new HashSet<>();
    ArrayList<Long> trail = new ArrayList<>();
    for (Long start : samples.keySet()) {
      if (settled.contains(start)) continue;
      visiting.clear();
      trail.clear();
      Long at = start;
      while (at != null && samples.containsKey(at) && !settled.contains(at)) {
        if (!visiting.add(at))
          throw new IllegalArgumentException("cyclic watershed perimeter receiver graph");
        trail.add(at);
        at = samples.get(at).first.downstreamKey();
      }
      settled.addAll(trail);
    }
  }

  /** Only production-valid 8-neighbour parents within the source region. */
  private static void validateReceiver(WatershedRegion.BoundaryNode node,
      long ox, long oz, long maxX, long maxZ) {
    if (node.ocean() && node.lake())
      throw new IllegalArgumentException("ocean node cannot be an inland lake");
    if (node.ocean() && node.downstreamKey() != null)
      throw new IllegalArgumentException("ocean node cannot have a land receiver");
    if (node.downstreamKey() == null) return;
    long receiver = node.downstreamKey();
    int rx = (int) (receiver >> 32), rz = (int) receiver;
    long dx = Math.abs((long) rx - node.x());
    long dz = Math.abs((long) rz - node.z());
    if (rx < ox || rx > maxX || rz < oz || rz > maxZ
        || Math.floorMod(rx, WatershedOutletTopology.STEP) != 0
        || Math.floorMod(rz, WatershedOutletTopology.STEP) != 0
        || dx > WatershedOutletTopology.STEP || dz > WatershedOutletTopology.STEP
        || (dx == 0 && dz == 0))
      throw new IllegalArgumentException("invalid watershed downstream parent coordinate");
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
