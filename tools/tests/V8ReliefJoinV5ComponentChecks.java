package com.shnewbs.deepprimal.core.density;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Executes exact new builder nodes with an explicitly synthetic noise API double. */
public final class V8ReliefJoinV5ComponentChecks {
  private static long assertions;
  private static void require(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message);
  }

  private static double smooth(double value, double lo, double hi) {
    double t = Math.max(0, Math.min(1, (value - lo)/(hi - lo)));
    return t*t*(3 - 2*t);
  }

  /** Non-production deterministic, smooth noise stand-in; validates graph wiring only. */
  private static double noise(DensityGraph.NoiseSpec s, double x, double z) {
    return Math.sin(x*s.xFrequency()*2.3 + s.salt()*.001)*
        Math.cos(z*s.zFrequency()*2.1 + s.salt()*.001);
  }

  private static double evaluate(List<DensityGraph.Node> nodes, int x, int z,
                                 double continentalness, double base) {
    Map<String, Double> values = new HashMap<>();
    values.put("continentalness", continentalness);
    values.put("negative_one", -1.0);
    values.put("one", 1.0);
    values.put("relief_before_canyon", base);
    for (DensityGraph.Node n : nodes) {
      var in = n.inputs();
      double v = switch (n.op()) {
        case CONSTANT -> n.parameters().getFirst();
        case NOISE_2D -> noise(n.noise(), x, z);
        case ADD -> values.get(in.get(0)) + values.get(in.get(1));
        case MULTIPLY -> values.get(in.get(0)) * values.get(in.get(1));
        case SMOOTHSTEP -> smooth(values.get(in.getFirst()),
            n.parameters().get(0), n.parameters().get(1));
      };
      require(Double.isFinite(v), "nonfinite join stage " + n.id());
      values.put(n.id(), v);
    }
    double window = values.get("relief5_join_window");
    require(window >= 0 && window <= 1, "join weight outside [0,1]");
    require(Math.abs(values.get(V8ReliefJoinV5.OUTPUT) - base) <= 11.000001,
        "join exceeded frozen 11-block perturbation");
    return values.get(V8ReliefJoinV5.OUTPUT);
  }

  private static void topology(List<DensityGraph.Node> nodes) {
    require(V8ReliefJoinV5.RECIPE_REVISION == 5, "explicit recipe version");
    require(nodes.size() == 14, "unexpected graph schema size");
    require(V8ReliefJoinV5.OUTPUT.equals(nodes.getLast().id()), "output id changed");
    Set<String> available = new HashSet<>(Set.of(
        "continentalness", "negative_one", "one", "relief_before_canyon"));
    for (var n : nodes) {
      require(available.add(n.id()), "duplicate id " + n.id());
      for (String id : n.inputs()) require(available.contains(id), "forward/missing ref " + id);
      if (n.op() == DensityGraph.Op.NOISE_2D) {
        require(n.noise() != null && n.noise().octaves() == 3,
            "unfrozen noise sample structure");
      }
    }
    require(nodes.stream().filter(n->n.op() == DensityGraph.Op.NOISE_2D).count() == 2,
        "expected only two bounded smooth 2D noise sources");
  }

  public static void main(String[] args) {
    List<DensityGraph.Node> nodes = V8ReliefJoinV5.nodes();
    topology(nodes);
    long changed = 0, zeroAtExtremes = 0, seams = 0;
    for (int x=-512; x<=512; x+=16) {
      for (int z=-512; z<=512; z+=16) {
        double c = .38 + .42*Math.sin(x/502.0)*Math.cos(z/479.0);
        double base=96 + 120*c + 8*Math.sin(x/147.0 + z/199.0);
        double value = evaluate(nodes,x,z,c,base);
        double repeat = evaluate(V8ReliefJoinV5.nodes(),x,z,c,base);
        require(Double.doubleToRawLongBits(value)==Double.doubleToRawLongBits(repeat),
            "non-deterministic absolute world-column graph");
        if (Math.abs(value-base)>1e-8) changed++;
        double near=evaluate(nodes,x+1,z,c,base);
        require(Math.abs(near-value)<.5,"join creates single-block impulse");
        seams++;
      }
    }
    for (double c:new double[]{-1,-.3,-.1,1.1,1.5}) {
      for(int x=-1024;x<=1024;x+=128) {
        double out=evaluate(nodes,x,0,c,96);
        require(Double.doubleToRawLongBits(out)==Double.doubleToRawLongBits(96),
            "far lowland or highland must preserve baseline exactly");
        zeroAtExtremes++;
      }
    }
    require(changed>500,"join disabled over foothill band");
    require(zeroAtExtremes==85,"extreme comparison corpus incomplete");
    System.out.println("RELIEF5_JOIN_COMPONENT_PASS assertions="+assertions+
        " changed="+changed+" seams="+seams+" exactBaseExtremes="+zeroAtExtremes+
        " compiledProductionJoin=true coreVmStub=true native=false");
  }
}
