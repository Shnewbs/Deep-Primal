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

  private static void checkBox(Box b) {
    for (double value : new double[]{b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ})
      if (!Double.isFinite(value) || Math.abs(value) > DensityGraph.WORLD_LIMIT)
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
    double[] p={a.low*b.low,a.low*b.high,a.high*b.low,a.high*b.high};
    double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY;
    for(double v:p) {
      if(Double.isNaN(v))return UNKNOWN;
      lo=Math.min(lo,v);hi=Math.max(hi,v);
    }
    return outward(lo,hi);
  }

  private static Range outward(double lo, double hi) {
    if(Double.isNaN(lo)||Double.isNaN(hi)||lo>hi)return UNKNOWN;
    return new Range(Math.nextDown(lo),Math.nextUp(hi));
  }

  private record Box(double minX,double minY,double minZ,
                     double maxX,double maxY,double maxZ) {}
  private record Range(double low,double high) {}
}
