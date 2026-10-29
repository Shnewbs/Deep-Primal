package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Immutable post-materialization observation for explicit V8-r6 vegetation.
 * The original placement plan is a deterministic query, NOT proof that every
 * candidate survived native setters and synchronous callbacks. This receipt
 * reads actual final chunk states after the heightmap pass, within the caller's
 * existing prewrite rollback scope; it never writes or relocates a plant.
 *
 * <p>It describes the just-completed in-memory generation transaction only.
 * Later player edits, cross-chunk effects, lighting and disk durability require
 * their own authority and cannot be inferred from these observations.
 */
public final class V8VegetationNativeReceipt {
  private static final int SIDE = 16;
  private final long[] realizedWords;
  private final int plannedCount;
  private final int realizedCount;

  private V8VegetationNativeReceipt(long[] words, int plannedCount, int realizedCount) {
    this.realizedWords = words.clone();
    this.plannedCount = plannedCount;
    this.realizedCount = realizedCount;
  }

  /** Total deterministic candidates before native writes or callback vetoes. */
  public int plannedCount() { return plannedCount; }
  /** Count still supported and matching the intended species at read-back. */
  public int realizedCount() { return realizedCount; }
  public int skippedCount() { return plannedCount - realizedCount; }

  public boolean realized(int localX, int localZ) {
    if (localX < 0 || localX >= SIDE || localZ < 0 || localZ >= SIDE)
      throw new IndexOutOfBoundsException("V8 vegetation receipt local column");
    int index = localZ * SIDE + localX;
    return (realizedWords[index >>> 6] & (1L << (index & 63))) != 0;
  }

  /** Defensive copy; neither callers nor future captures can mutate this receipt. */
  public long[] realizedWords() { return realizedWords.clone(); }

  /**
   * Called only after raw, ground and foliage setters and final heightmap
   * priming succeed. A read failure aborts the enclosing native transaction
   * through its existing rollback path, rather than fabricating a receipt.
   *
   * <p>Only planned sites are read; 256 columns is a fixed upper bound. The
   * expected topsoil is the same preflight value used during the native pass.
   * A species write that was rejected or overwritten by a callback is absent.
   */
  public static V8VegetationNativeReceipt capture(ChunkAccess chunk,
      V8VegetationColumnPlan plants, V8EcotoneColumnPlan columns,
      BlockState expectedGrass, BlockState expectedPodzol,
      BlockState shortGrass, BlockState fern) {
    Objects.requireNonNull(chunk, "V8 receipt chunk");
    Objects.requireNonNull(plants, "V8 saved placement plan");
    Objects.requireNonNull(columns, "V8 saved surface preflight");
    int minY = chunk.getMinY();
    int maxY = Math.addExact(minY, chunk.getHeight());
    int originX = chunk.getPos().getMinBlockX();
    int originZ = chunk.getPos().getMinBlockZ();
    long[] words = new long[4];
    int observed = 0;
    int candidateCount = 0;
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        var placement = plants.placement(x, z);
        if (placement == null) continue;
        candidateCount++;
        var column = columns.column(x, z);
        if (column == null || column.cover().snowPatch()) continue;
        BlockState support = switch (column.cover().cover()) {
          case BARE -> null;
          case GRASS -> expectedGrass;
          case PODZOL -> expectedPodzol;
        };
        if (support == null || support.isAir()) continue;
        int y = placement.y();
        if (y <= minY + 2 || y >= maxY || column.topY() != y - 1) continue;
        int worldX = Math.addExact(originX, x);
        int worldZ = Math.addExact(originZ, z);
        var beneath = pos.set(worldX, y - 1, worldZ);
        BlockState actualSupport = Objects.requireNonNull(chunk.getBlockState(beneath),
            "V8 realized plant support");
        if (!support.equals(actualSupport) || !actualSupport.getFluidState().isEmpty()
            || !actualSupport.isFaceSturdy(chunk, beneath, Direction.UP)) continue;
        BlockState species = Objects.requireNonNull(
            placement.plant() == V8VegetationColumnPlan.Plant.FERN ? fern : shortGrass,
            "V8 planned plant species state");
        BlockState actualPlant = Objects.requireNonNull(
            chunk.getBlockState(pos.set(worldX, y, worldZ)), "V8 realized plant state");
        if (!species.equals(actualPlant) || !actualPlant.getFluidState().isEmpty()) continue;
        int bit = z * SIDE + x;
        words[bit >>> 6] |= 1L << (bit & 63);
        observed++;
      }
    if (candidateCount != plants.count())
      throw new IllegalStateException("V8 vegetation plan candidate count changed");
    return new V8VegetationNativeReceipt(words, candidateCount, observed);
  }
}
