package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Callback mutation of a future cell must not become the saved prewrite state. */
public final class V8CallbackUndoChecks {
  private static long assertions;
  private static void ok(boolean condition, String reason) {
    assertions++;
    if (!condition) throw new AssertionError(reason);
  }
  private static final class CallbackChunk extends ChunkAccess {
    final ChunkPos pos = new ChunkPos(2, -4);
    final BlockState[] values = new BlockState[256 * 32];
    final BlockState[] originals;
    final Heightmap surface = new Heightmap(-8);
    final Heightmap floor = new Heightmap(-8);
    long setters, reads;
    boolean callback, failed;
    int readFailAt = -1;
    CallbackChunk() {
      for (int i = 0; i < values.length; i++)
        values[i] = new BlockState("original_" + (i % 11));
      originals = values.clone();
      Heightmap.primeHeightmaps(this, java.util.EnumSet.of(
          Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    }
    @Override public ChunkPos getPos() { return pos; }
    @Override public int getMinY() { return -8; }
    @Override public int getHeight() { return 32; }
    @Override public BlockState getBlockState(int x, int y, int z) {
      reads++;
      if (reads == readFailAt) throw new IllegalStateException("prewrite snapshot read failed");
      return values[((y + 8) << 8) | (z << 4) | x];
    }
    @Override public void setBlockState(BlockPos p, BlockState state) {
      int x = p.getX() - pos.getMinBlockX(), z = p.getZ() - pos.getMinBlockZ();
      int index = ((p.getY() + 8) << 8) | (z << 4) | x;
      values[index] = state;
      setters++;
      if (!callback && setters == 1) {
        callback = true;
        // This cell is in a later section, not yet directly visited.
        values[(28 << 8) | 1] = new BlockState("callback_sentinel");
      }
      if (!failed && setters == 1000) {
        failed = true;
        throw new IllegalStateException("native setter failed after callback");
      }
    }
    @Override public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
      return type == Heightmap.Types.WORLD_SURFACE_WG ? surface : floor;
    }
    void assertOriginal(String scenario) {
      for (int i = 0; i < values.length; i++)
        ok(originals[i].equals(values[i]), scenario + " changed cell " + i);
    }
  }
  private static DensityGraph graph() {
    return new Definition(VERSION, "d", List.of(
        Node.operation("y", Op.Y), Node.constant("neg", -1),
        Node.operation("ny", Op.MULTIPLY, "y", "neg"),
        Node.constant("top", 8), Node.operation("d", Op.ADD, "top", "ny")))
        .compile(943L);
  }
  public static void main(String[] args) {
    var profile = new GroundSurfaceProfile.Compiled(graph(), 3);
    var c = new CallbackChunk();
    try {
      DensityChunkMaterializer.fillPlannedSurface(profile, c, 16);
      throw new AssertionError("expected setter failure");
    } catch (IllegalStateException e) {
      ok(e.getMessage().contains("native setter failed"), "root failure preserved");
    }
    ok(c.callback && c.failed, "future-cell callback and postwrite fault both ran");
    c.assertOriginal("callback case");

    var preflight = new CallbackChunk();
    preflight.readFailAt = (int) preflight.reads + 500;
    try {
      DensityChunkMaterializer.fillPlannedSurface(profile, preflight, 16);
      throw new AssertionError("expected prewrite read failure");
    } catch (IllegalStateException e) {
      ok(e.getMessage().contains("snapshot read failed"), "read failure preserved");
    }
    ok(preflight.setters == 0, "snapshot read fault caused no writes");
    preflight.assertOriginal("snapshot read case");
    System.out.println("V8_CALLBACK_UNDO_PASS assertions=" + assertions
        + " callback_mutated_future_cell_restored=true prewrite_failure_no_writes=true"
        + " native_minecraft=false");
  }
}
