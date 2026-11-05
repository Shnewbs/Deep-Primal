package com.shnewbs.deepprimal.core.density;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Explicit new-generation V8 border admission. A caller must supply four
 * authoritative, persisted-neighbor observations; UNKNOWN and existing chunks
 * without a matching saved-profile identity fail closed. This is deliberately
 * NOT a cross-version terrain blender or a neighbor chunk loader.
 *
 * <p>No legacy generation, saved graph, existing chunk or physical water is
 * modified. A later native owner must prove observation freshness and durable
 * chunk-revision provenance before using this optional gate in production.
 */
public final class V8GenerationBorderAdmission {
  private V8GenerationBorderAdmission() {}

  public enum Direction { NORTH, EAST, SOUTH, WEST }
  public enum State { UNGENERATED, GENERATED, UNKNOWN }

  public record Neighbor(Direction direction, int chunkX, int chunkZ,
                         State state, long savedProfileFingerprint) {
    public Neighbor {
      Objects.requireNonNull(direction, "direction");
      Objects.requireNonNull(state, "state");
    }
  }

  /** The witness is immutable; no implicit lookup or chunk generation occurs. */
  public record Witness(int chunkX, int chunkZ, long savedProfileFingerprint,
                        List<Neighbor> neighbors) {
    public Witness {
      if (savedProfileFingerprint == 0L)
        throw new IllegalArgumentException("missing saved V8 profile identity");
      neighbors = List.copyOf(Objects.requireNonNull(neighbors, "neighbors"));
      if (neighbors.size() != Direction.values().length)
        throw new IllegalArgumentException("all four neighbor observations required");
    }
  }

  /**
   * Refuse unknown, legacy, edited or mismatched neighbors before ANY native
   * density sample or block write. A generated neighbor must match the exact
   * saved profile; an ungenerated neighbor must have no profile identity.
   * At integer chunk-coordinate overflow there is no valid four-side witness.
   */
  public static void requireCompatible(Witness witness, int actualChunkX,
                                       int actualChunkZ, long expectedProfileFingerprint) {
    Objects.requireNonNull(witness, "witness");
    if (expectedProfileFingerprint == 0L
        || witness.savedProfileFingerprint() != expectedProfileFingerprint
        || witness.chunkX() != actualChunkX || witness.chunkZ() != actualChunkZ)
      throw new IllegalArgumentException("V8 border owner/profile mismatch");

    EnumSet<Direction> seen = EnumSet.noneOf(Direction.class);
    for (Neighbor neighbor : witness.neighbors()) {
      if (!seen.add(neighbor.direction()))
        throw new IllegalArgumentException("duplicate V8 border direction");
      int expectedX, expectedZ;
      try {
        expectedX = Math.addExact(actualChunkX, switch (neighbor.direction()) {
          case EAST -> 1;
          case WEST -> -1;
          default -> 0;
        });
        expectedZ = Math.addExact(actualChunkZ, switch (neighbor.direction()) {
          case SOUTH -> 1;
          case NORTH -> -1;
          default -> 0;
        });
      } catch (ArithmeticException overflow) {
        throw new IllegalArgumentException("V8 border coordinate overflow", overflow);
      }
      if (neighbor.chunkX() != expectedX || neighbor.chunkZ() != expectedZ)
        throw new IllegalArgumentException("V8 border coordinate mismatch");
      if (neighbor.state() == State.UNKNOWN)
        throw new IllegalArgumentException("unobserved V8 generation border");
      if (neighbor.state() == State.GENERATED
          && neighbor.savedProfileFingerprint() != expectedProfileFingerprint)
        throw new IllegalArgumentException("incompatible generated V8 border");
      if (neighbor.state() == State.UNGENERATED
          && neighbor.savedProfileFingerprint() != 0L)
        throw new IllegalArgumentException("ungenerated border has saved identity");
    }
    if (seen.size() != Direction.values().length)
      throw new IllegalArgumentException("incomplete V8 border observations");
  }
}
