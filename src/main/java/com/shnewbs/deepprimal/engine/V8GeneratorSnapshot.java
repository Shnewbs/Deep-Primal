package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.Objects;

/**
 * Immutable V8 generator snapshot for explicit preview/save-profile integration.
 * Not registered as a chunk-generator codec; V1-V7 saves and defaults are unchanged.
 * A signed decimal string keeps the complete 64-bit generation seed across JSON.
 */
public record V8GeneratorSnapshot(
    int version, long seed, int minY, int height,
    DensityGraph.Definition density, GroundSurfaceProfile ground) {
  public static final int VERSION = 1;

  private static final Codec<Long> SEED = Codec.STRING.comapFlatMap(text -> {
    if (!text.matches("0|-?[1-9][0-9]*"))
      return DataResult.error(() -> "seed must be a canonical signed 64-bit decimal string");
    try {
      return DataResult.success(Long.parseLong(text));
    } catch (NumberFormatException failure) {
      return DataResult.error(() -> "seed must fit signed 64-bit integer range");
    }
  }, value -> Long.toString(value));

  private record Wire(int version, long seed, int minY, int height,
      DensityGraph.Definition density, GroundSurfaceProfile ground) {}

  private static final Codec<Wire> WIRE = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("version").forGetter(Wire::version),
      SEED.fieldOf("seed").forGetter(Wire::seed),
      Codec.INT.fieldOf("min_y").forGetter(Wire::minY),
      Codec.INT.fieldOf("height").forGetter(Wire::height),
      DensityGraphCodec.CODEC.fieldOf("density").forGetter(Wire::density),
      GroundSurfaceProfile.CODEC.fieldOf("ground").forGetter(Wire::ground)
  ).apply(i, Wire::new));

  /** Strict, inline full snapshot: invalid saved profiles return codec errors. */
  public static final Codec<V8GeneratorSnapshot> CODEC = WIRE.comapFlatMap(w -> {
    try {
      return DataResult.success(new V8GeneratorSnapshot(
          w.version, w.seed, w.minY, w.height, w.density, w.ground));
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException failure) {
      return DataResult.error(() -> "V8 generator snapshot: " + failure.getMessage());
    }
  }, s -> new Wire(s.version, s.seed, s.minY, s.height, s.density, s.ground));

  public V8GeneratorSnapshot {
    if (version != VERSION)
      throw new IllegalArgumentException("unsupported V8 generator snapshot version: " + version);
    if (height < 1 || height > DensityChunk.MAX_HEIGHT)
      throw new IllegalArgumentException("V8 height must be within 1.." + DensityChunk.MAX_HEIGHT);
    long highest = (long) minY + height - 1;
    if (minY < -DensityGraph.WORLD_LIMIT || highest > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("V8 vertical bounds outside density graph coordinates");
    Objects.requireNonNull(density, "density");
    Objects.requireNonNull(ground, "ground");
    // Check the complete immutable graph and material channel before the snapshot can be saved.
    var graph = density.compile(seed);
    graph.channel(ground.channel(), 0, 0, 0);
  }

  /** Reject another world's geometry before any native block write. */
  public void requireHeight(int actualMinY, int actualHeight) {
    if (minY != actualMinY || height != actualHeight)
      throw new IllegalArgumentException("V8 saved profile height mismatch: expected "
          + minY + "/" + height + ", received " + actualMinY + "/" + actualHeight);
  }

  /** Compile once per saved generator/session, never independently per chunk. */
  public Prepared prepare() {
    var graph = density.compile(seed);
    return new Prepared(this, ground.compile(seed, graph));
  }

  /**
   * Provenance-bound compiled geometry; a caller cannot combine one saved
   * world's metadata with another world's compiled density/material fields.
   */
  public static final class Prepared {
    private final V8GeneratorSnapshot snapshot;
    private final GroundSurfaceProfile.Compiled surface;
    private Prepared(V8GeneratorSnapshot snapshot, GroundSurfaceProfile.Compiled surface) {
      this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
      this.surface = Objects.requireNonNull(surface, "surface");
    }
    public V8GeneratorSnapshot snapshot() { return snapshot; }
    public GroundSurfaceProfile.Compiled surface() { return surface; }
    public void requireHeight(int actualMinY, int actualHeight) {
      snapshot.requireHeight(actualMinY, actualHeight);
    }
  }
}
