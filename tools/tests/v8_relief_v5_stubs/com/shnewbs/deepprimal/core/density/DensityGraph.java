package com.shnewbs.deepprimal.core.density;

import java.util.List;
import java.util.Objects;

/** API double: only the graph schema used by V8ReliefJoinV5. Not the production VM. */
public final class DensityGraph {
  private DensityGraph() {}
  public enum Op { CONSTANT, ADD, MULTIPLY, SMOOTHSTEP, NOISE_2D }
  public record NoiseSpec(double xFrequency, double yFrequency, double zFrequency,
                          int octaves, double lacunarity, double gain, long salt) {}
  public record Node(String id, Op op, List<String> inputs,
                     List<Double> parameters, NoiseSpec noise) {
    public Node {
      Objects.requireNonNull(id); Objects.requireNonNull(op);
      inputs = List.copyOf(inputs); parameters = List.copyOf(parameters);
    }
    public static Node noise(String id, NoiseSpec noise, boolean horizontal) {
      if (!horizontal) throw new IllegalArgumentException("only horizontal noise here");
      return new Node(id, Op.NOISE_2D, List.of(), List.of(), noise);
    }
    public static Node constant(String id, double v) {
      return new Node(id, Op.CONSTANT, List.of(), List.of(v), null);
    }
    public static Node operation(String id, Op op, String... inputs) {
      return new Node(id, op, List.of(inputs), List.of(), null);
    }
    public static Node bounded(String id, Op op, String input, double low, double high) {
      if (!(low < high)) throw new IllegalArgumentException("invalid bounds");
      return new Node(id, op, List.of(input), List.of(low, high), null);
    }
  }
}
