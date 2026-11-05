package com.shnewbs.deepprimal.engine;

import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Loaded-only, fair section scans activate unsupported water inside generated/saved chunks. */
public final class WaterInteriorWake {
  private static final int CELLS_PER_TICK = 4096, QUANTUM = 256;
  private final WeakHashMap<ServerLevel, LinkedHashMap<Long, Cursor>> levels = new WeakHashMap<>();

  private static final class Cursor {
    int section, cell;

    Cursor(int section) {
      this.section = section;
    }
  }

  public void enqueue(ServerLevel level, int x, int z) {
    long key = ((long) x << 32) | (z & 0xffffffffL);
    levels
        .computeIfAbsent(level, unused -> new LinkedHashMap<>())
        .putIfAbsent(key, new Cursor(level.getSectionsCount() - 1));
  }

  @SubscribeEvent
  public void loaded(ChunkEvent.Load event) {
    if (event.getLevel() instanceof ServerLevel level
        && event.getChunk() instanceof LevelChunk
        && level.getChunkSource().getGenerator() instanceof EngineChunkGenerator) {
      var pos = event.getChunk().getPos();
      enqueue(level, pos.x(), pos.z());
    }
  }

  @SubscribeEvent
  public void tick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel level) step(level);
  }

  public void step(ServerLevel level) {
    var jobs = levels.get(level);
    if (jobs == null) return;
    int remaining = CELLS_PER_TICK;
    long end = System.nanoTime() + 1_000_000;
    var pos = new BlockPos.MutableBlockPos();
    while (!jobs.isEmpty() && remaining > 0 && System.nanoTime() < end) {
      var entry = jobs.pollFirstEntry();
      long key = entry.getKey();
      Cursor cursor = entry.getValue();
      int x = (int) (key >> 32), z = (int) key;
      // This lookup may pump callbacks. Keep no iterators and never request a missing chunk.
      var raw = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
      if (!(raw instanceof LevelChunk chunk)) {
        remaining--;
        continue;
      }
      int quantum = QUANTUM;
      while (cursor.section >= 0 && quantum > 0 && remaining > 0 && System.nanoTime() < end) {
        var section = chunk.getSection(cursor.section);
        if (cursor.cell == 0 && !section.maybeHas(s -> s.is(EngineRegistry.WATER_BLOCK.get()))) {
          cursor.section--;
          quantum--;
          remaining--;
          continue;
        }
        int cell = cursor.cell++, localX = cell & 15, localZ = (cell >> 4) & 15;
        int localY = 15 - (cell >> 8);
        var state = section.getBlockState(localX, localY, localZ);
        if (state.is(EngineRegistry.WATER_BLOCK.get())) {
          pos.set(x * 16 + localX, level.getMinY() + cursor.section * 16 + localY, z * 16 + localZ);
          if (WaterRuntime.needsWake(level, pos)) WaterRuntime.activate(level, pos);
        }
        if (cursor.cell == 4096) {
          cursor.cell = 0;
          cursor.section--;
        }
        quantum--;
        remaining--;
      }
      if (cursor.section >= 0) jobs.putIfAbsent(key, cursor);
    }
  }

  @SubscribeEvent
  public void chunkUnloaded(ChunkEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel level) {
      var jobs = levels.get(level);
      var pos = event.getChunk().getPos();
      if (jobs != null) jobs.remove(((long) pos.x() << 32) | (pos.z() & 0xffffffffL));
    }
  }

  @SubscribeEvent
  public void unloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel level) levels.remove(level);
  }

  @SubscribeEvent
  public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
    levels.clear();
  }
}
