package com.shnewbs.deepprimal.engine;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Resumes dormant finite water when a sealed, unloaded neighbor becomes available. */
public final class WaterBorderWake {
  private static final int MAX_JOBS = 256, CELLS_PER_TICK = 512;
  private final WeakHashMap<ServerLevel, LinkedHashMap<Edge, Job>> levels = new WeakHashMap<>();

  private record Edge(int x, int z, boolean east) {}

  private static final class Job {
    int cursor;
  }

  @SubscribeEvent
  public void loaded(ChunkEvent.Load event) {
    if (event.getLevel() instanceof ServerLevel level
        && event.getChunk() instanceof LevelChunk
        && level.getChunkSource().getGenerator() instanceof EngineChunkGenerator) {
      var p = event.getChunk().getPos();
      enqueue(level, p.x(), p.z());
    }
  }

  public void enqueue(ServerLevel level, int x, int z) {
    var jobs = levels.computeIfAbsent(level, k -> new LinkedHashMap<>());
    for (var edge :
        List.of(
            new Edge(x, z, true),
            new Edge(x - 1, z, true),
            new Edge(x, z, false),
            new Edge(x, z - 1, false))) jobs.putIfAbsent(edge, new Job());
    while (jobs.size() > MAX_JOBS) jobs.remove(jobs.keySet().iterator().next());
  }

  @SubscribeEvent
  public void tick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel level) step(level);
  }

  public void step(ServerLevel level) {
    var jobs = levels.get(level);
    if (jobs == null || jobs.isEmpty()) return;
    int remaining = CELLS_PER_TICK;
    long end = System.nanoTime() + 1_000_000;
    var a = new BlockPos.MutableBlockPos();
    var b = new BlockPos.MutableBlockPos();
    while (!jobs.isEmpty() && remaining > 0 && System.nanoTime() < end) {
      // Even non-loading lookups can pump chunk callbacks. Do not retain map iterators.
      var entry = jobs.firstEntry();
      var edge = entry.getKey();
      var job = entry.getValue();
      var first = level.getChunkSource().getChunk(edge.x, edge.z, ChunkStatus.FULL, false);
      var second =
          level
              .getChunkSource()
              .getChunk(
                  edge.x + (edge.east ? 1 : 0),
                  edge.z + (edge.east ? 0 : 1),
                  ChunkStatus.FULL,
                  false);
      if (!(first instanceof LevelChunk left) || !(second instanceof LevelChunk right)) {
        jobs.remove(edge, job);
        remaining--;
        continue;
      }
      int total = level.getHeight() * 16;
      while (job.cursor < total && remaining > 0 && System.nanoTime() < end) {
        int y = level.getMinY() + job.cursor / 16, along = job.cursor % 16;
        if ((job.cursor & 255) == 0) {
          int section = left.getSectionIndex(y);
          var ls = left.getSection(section);
          var rs = right.getSection(section);
          boolean lw = ls.maybeHas(s -> s.is(EngineRegistry.WATER_BLOCK.get()));
          boolean rw = rs.maybeHas(s -> s.is(EngineRegistry.WATER_BLOCK.get()));
          // Palette checks skip rock, air and unchanged, completely full ocean interiors.
          if ((!lw && !rw)
              || (ls.maybeHas(WaterBorderWake::notFullWater) == false
                  && rs.maybeHas(WaterBorderWake::notFullWater) == false)) {
            job.cursor += 256;
            remaining--;
            continue;
          }
        }
        a.set(edge.x * 16 + (edge.east ? 15 : along), y, edge.z * 16 + (edge.east ? along : 15));
        b.set(a.getX() + (edge.east ? 1 : 0), y, a.getZ() + (edge.east ? 0 : 1));
        // Read the already obtained chunks directly; a level lookup could reload a chunk
        // released by a callback during the preceding non-loading chunk lookups.
        var as = left.getBlockState(a);
        var bs = right.getBlockState(b);
        int au = as.is(EngineRegistry.WATER_BLOCK.get()) ? as.getValue(EngineFluid.UNITS) : 0;
        int bu = bs.is(EngineRegistry.WATER_BLOCK.get()) ? bs.getValue(EngineFluid.UNITS) : 0;
        if (au > bu + 1 && (bu > 0 || bs.isAir()))
          level.scheduleTick(a, EngineRegistry.WATER.get(), 2);
        if (bu > au + 1 && (au > 0 || as.isAir()))
          level.scheduleTick(b, EngineRegistry.WATER.get(), 2);
        job.cursor++;
        remaining--;
      }
      if (job.cursor >= total) jobs.remove(edge, job);
    }
  }

  private static boolean notFullWater(net.minecraft.world.level.block.state.BlockState s) {
    return !s.is(EngineRegistry.WATER_BLOCK.get()) || s.getValue(EngineFluid.UNITS) != 16;
  }

  @SubscribeEvent
  public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
    levels.clear();
  }
}
