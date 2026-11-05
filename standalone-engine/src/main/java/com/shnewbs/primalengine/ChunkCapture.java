package com.shnewbs.primalengine;

import com.shnewbs.primalengine.lod.ColumnarChunk;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Incremental snapshot of a caller-owned loaded chunk; invoke only on its owning game thread. */
public final class ChunkCapture {
  private final ChunkAccess chunk;
  private final ColumnarChunk.Builder builder;
  private final Map<BlockState, Integer> ids = new HashMap<>();
  private final List<String> palette = new ArrayList<>();
  private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
  private final int[] column;
  private int y;

  public ChunkCapture(ChunkAccess chunk, long revision) {
    this.chunk = chunk;
    column = new int[chunk.getHeight()];
    builder =
        new ColumnarChunk.Builder(
            chunk.getPos().x(), chunk.getPos().z(), chunk.getMinY(), chunk.getHeight(), revision);
  }

  public boolean step(int maximumBlockReads) {
    if (maximumBlockReads < 1) throw new IllegalArgumentException("Snapshot work budget");
    while (maximumBlockReads-- > 0 && builder.completedColumns() < 256) {
      int c = builder.completedColumns();
      var state =
          chunk.getBlockState(
              pos.set(
                  chunk.getPos().getMinBlockX() + c % 16,
                  chunk.getMinY() + y,
                  chunk.getPos().getMinBlockZ() + c / 16));
      column[y++] =
          ids.computeIfAbsent(
              state,
              s -> {
                palette.add(s.toString());
                return palette.size() - 1;
              });
      if (y == column.length) {
        builder.column(column);
        y = 0;
      }
    }
    return builder.completedColumns() == 256;
  }

  public ColumnarChunk finish() {
    return builder.finish(palette);
  }
}
