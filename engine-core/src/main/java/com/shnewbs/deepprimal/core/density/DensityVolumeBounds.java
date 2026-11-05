package com.shnewbs.deepprimal.core.density;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A conservative occupancy preflight for experimental V8 density jobs.
 *
 * <p>This is deliberately separate from {@link DensityGrid}: interpolated lattice values
 * cannot establish solid/empty topology. A definite answer from this class holds for
 * every coordinate within the specified closed axis-aligned box; UNCERTAIN requires an
 * authoritative exact density sample. It does not modify or select saved terrain versions.
 *
 * <p>Bounds use outward-rounded interval arithmetic. Noise is bounded by the noise
 * contract, never by just its sampled corners. Warps widen the source coordinate box.
 * The recursion/work ceiling fails open to UNCERTAIN rather than using an unsafe guess.
 */
public final class DensityVolumeBounds {
  public enum Occupancy { SOLID, EMPTY, UNCERTAIN }

  private static final int MAX_VISITS = 4096;
  private static final Range UNKNOWN = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  private static final Range UNIT = new Range(-1.001, 1.001);
  private static final Range SMOOTH = new Range(-Double.MIN_VALUE, Math.nextUp(1.0));
  private final String root;
  private final Map<String, DensityGraph.Node> nodes;
  private final int coordinateMask;

  /** The supplied definition must already have passed the graph compiler's validation. */
  public DensityVolumeBounds(DensityGraph graph) {
    Objects.requireNonNull(graph, "graph");
    DensityGraph.Definition definition = graph.definition();
    root = definition.root();
    var indexed = new HashMap<String, DensityGraph.Node>();
    for (var node : definition.nodes()) {
      if (indexed.putIfAbsent(node.id(), node) != null)
        throw new IllegalArgumentException("duplicate graph node: " + node.id());
    }
    if (!indexed.containsKey(root)) throw new IllegalArgumentException("missing graph root");
    nodes = Map.copyOf(indexed);
    coordinateMask = coordinateMask(root, new HashMap<>());
  }

  /**
   * Conservative source-coordinate dependency bitset (X=1, Y=2, Z=4).
   * Unused axes cannot affect the density value; a WARP includes every
   * displacement input as well as its source, even when that overestimates.
   */
  public int coordinateMask() { return coordinateMask; }

  private int coordinateMask(String id, Map<String, Integer> memo) {
    Integer cached = memo.get(id);
    if (cached != null) return cached;
    DensityGraph.Node node = nodes.get(id);
    int mask = switch (node.op()) {
      case CONSTANT -> 0;
      case X -> 1;
      case Y -> 2;
      case Z -> 4;
      case NOISE_2D -> 5;
      case NOISE_3D -> 7;
      default -> {
        int all = 0;
        for (String input : node.inputs()) all |= coordinateMask(input, memo);
        yield all;
      }
    };
    memo.put(id, mask);
    return mask;
  }

  /** Classifies all continuous positions within a closed box, not only its corners. */
  public Occupancy classify(double minX, double minY, double minZ,
                            double maxX, double maxY, double maxZ) {
    Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
    checkBox(box);
    Range result = bound(root, box, new int[1]);
    if (result.low > 0) return Occupancy.SOLID;
    if (result.high <= 0) return Occupancy.EMPTY;
    return Occupancy.UNCERTAIN;
  }

  /** Convenience for a positive-size integer voxel group with inclusive end coordinates. */
  public Occupancy classifyCells(int x, int y, int z, int countX, int countY, int countZ) {
    if (countX < 1 || countY < 1 || countZ < 1 ||
        countX > 64 || countY > 64 || countZ > 64)
      throw new IllegalArgumentException("cell group must have 1..64 cells per axis");
    return classify(x, y, z, (long)x + countX - 1L, (long)y + countY - 1L,
                    (long)z + countZ - 1L);
  }

  private static boolean validCoordinate(double value) {
    return Double.isFinite(value) && Math.abs(value) <= DensityGraph.WORLD_LIMIT;
  }

  private static void checkBox(Box b) {
    // Called for every occupancy probe: validate scalars without a temporary array.
    if (!validCoordinate(b.minX) || !validCoordinate(b.minY) || !validCoordinate(b.minZ)
        || !validCoordinate(b.maxX) || !validCoordinate(b.maxY) || !validCoordinate(b.maxZ))
      throw new IllegalArgumentException("box coordinate outside density world bounds");
    if (b.minX > b.maxX || b.minY > b.maxY || b.minZ > b.maxZ)
      throw new IllegalArgumentException("inverted density box");
  }

  private Range bound(String id, Box box, int[] visits) {
    if (++visits[0] > MAX_VISITS) return UNKNOWN;
    DensityGraph.Node node = nodes.get(id);
    if (node == null) return UNKNOWN; // Defensive; compiled graphs do not have missing inputs.
    var inputs = node.inputs();
    var parameters = node.parameters();
    return switch (node.op()) {
      case CONSTANT -> new Range(parameters.getFirst(), parameters.getFirst());
      case X -> new Range(box.minX, box.maxX);
      case Y -> new Range(box.minY, box.maxY);
      case Z -> new Range(box.minZ, box.maxZ);
      case NOISE_2D, NOISE_3D -> UNIT;
      case ADD -> add(bound(inputs.get(0),box,visits), bound(inputs.get(1),box,visits));
      case MULTIPLY -> multiply(bound(inputs.get(0),box,visits), bound(inputs.get(1),box,visits));
      case MIN -> {
        Range a=bound(inputs.get(0),box,visits),b=bound(inputs.get(1),box,visits);
        yield new Range(Math.min(a.low,b.low),Math.min(a.high,b.high));
      }
      case MAX -> {
        Range a=bound(inputs.get(0),box,visits),b=bound(inputs.get(1),box,visits);
        yield new Range(Math.max(a.low,b.low),Math.max(a.high,b.high));
      }
      case ABS -> {
        Range a=bound(inputs.get(0),box,visits);
        double low=a.low<=0 && a.high>=0?0:Math.min(Math.abs(a.low),Math.abs(a.high));
        yield outward(low,Math.max(Math.abs(a.low),Math.abs(a.high)));
      }
      case CLAMP -> {
        Range a=bound(inputs.get(0),box,visits);
        double min=parameters.get(0),max=parameters.get(1);
        yield new Range(Math.clamp(a.low,min,max),Math.clamp(a.high,min,max));
      }
      case SMOOTHSTEP -> SMOOTH; // Smoothstep always remains in [0, 1].
      case LERP -> {
        Range a=bound(inputs.get(0),box,visits),b=bound(inputs.get(1),box,visits);
        // The interpolation fraction is clamped to [0, 1]; convex-hull proof
        // works even when the fraction and endpoints are mutually dependent.
        yield outward(Math.min(a.low,b.low),Math.max(a.high,b.high));
      }
      case SEA_SIDE_BLEND -> {
        // Bounded conservative envelope, never sample only the box corners.
        // The sign-preserving transform is between historical/proposed
        // absolute sea distances and cannot exceed either extreme. Canyon
        // incision applies equally to both; no false definite occupancy.
        Range canyon=bound(inputs.get(2),box,visits);
        Range previous=add(bound(inputs.get(0),box,visits),canyon);
        Range candidate=add(bound(inputs.get(1),box,visits),canyon);
        double sea=parameters.get(0);
        double radius=Math.max(Math.max(Math.abs(previous.low-sea),Math.abs(previous.high-sea)),
            Math.max(Math.abs(candidate.low-sea),Math.abs(candidate.high-sea)));
        if(!Double.isFinite(radius)) yield UNKNOWN;
        if(previous.low>sea) yield outward(sea,sea+radius);
        if(previous.high<sea) yield outward(sea-radius,sea);
        yield outward(sea-radius,sea+radius);
      }
      case WARP -> {
        double amplitude=parameters.getFirst();
        Range dx=bound(inputs.get(1),box,visits);
        Range dy=bound(inputs.get(2),box,visits);
        Range dz=bound(inputs.get(3),box,visits);
        Range x=add(new Range(box.minX,box.maxX),multiply(new Range(amplitude,amplitude),dx));
        Range y=add(new Range(box.minY,box.maxY),multiply(new Range(amplitude,amplitude),dy));
        Range z=add(new Range(box.minZ,box.maxZ),multiply(new Range(amplitude,amplitude),dz));
        yield bound(inputs.get(0),new Box(x.low,y.low,z.low,x.high,y.high,z.high),visits);
      }
    };
  }

  private static Range add(Range a, Range b) {
    return outward(a.low+b.low,a.high+b.high);
  }

  private static Range multiply(Range a, Range b) {
    // Four interval corners, evaluated in the same order as the old array loop.
    // Avoid an array allocation for every MULTIPLY node in a V8 preflight.
    double p0=a.low*b.low,p1=a.low*b.high,p2=a.high*b.low,p3=a.high*b.high;
    if (Double.isNaN(p0) || Double.isNaN(p1) || Double.isNaN(p2) || Double.isNaN(p3))
      return UNKNOWN;
    return outward(Math.min(Math.min(p0,p1),Math.min(p2,p3)),
        Math.max(Math.max(p0,p1),Math.max(p2,p3)));
  }

  private static Range outward(double lo, double hi) {
    if(Double.isNaN(lo)||Double.isNaN(hi)||lo>hi)return UNKNOWN;
    return new Range(Math.nextDown(lo),Math.nextUp(hi));
  }

  private record Box(double minX,double minY,double minZ,
                     double maxX,double maxY,double maxZ) {}
  private record Range(double low,double high) {}
}
