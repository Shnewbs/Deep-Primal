package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Loaded-only environmental accounting. No elapsed-time catch-up and no renewable player block. */
public final class HydrologyRuntime {
  private static final WeakHashMap<ServerLevel, Work> WORK = new WeakHashMap<>();
  private static int internalWrites;
  private static final class Cursor { int column; final long[] visited = new long[256]; }
  private static final class RegionWork {
    final double datum;
    long tick = Long.MIN_VALUE;
    int discharged;
    RegionWork(double datum) { this.datum = datum; }
  }
  private static final class Work {
    final LinkedHashMap<Long, Cursor> chunks = new LinkedHashMap<>();
    final HashMap<Long, RegionWork> regions = new HashMap<>();
  }
  public static boolean enabled(ServerLevel l) {
    return HydrologyConfig.ENABLED.get()
        && l.getChunkSource().getGenerator() instanceof EngineChunkGenerator g
        && (g.geography().revision >= 4 || HydrologyConfig.EXISTING_WORLDS.get());
  }
  public static boolean boundary(BlockState s) {
    return s.is(EngineRegistry.WATER_BLOCK.get()) && s.getValue(EngineWaterBlock.OCEAN);
  }
  public static boolean write(ServerLevel l, BlockPos p, BlockState s) {
    if (l.getBlockState(p).equals(s)) return true;
    internalWrites++;
    try { return l.setBlock(p, s, 2 | 16); }
    finally { internalWrites--; }
  }
  public static void externalChange(ServerLevel l, BlockPos p, BlockState before, BlockState after) {
    if (internalWrites != 0 || before == null || before == after) return;
    if (before.is(EngineRegistry.WATER_BLOCK.get()) &&
        (!after.is(EngineRegistry.WATER_BLOCK.get()) ||
            !before.getValue(EngineFluid.UNITS).equals(after.getValue(EngineFluid.UNITS)))) {
      // External commands/destructive block placement are explicit boundaries of the simulation.
      var d = HydrologyData.get(l);
      if (d.composition.remove(p.asLong()) != null) d.setDirty();
    }
  }
  public static WaterAccount.Parcel contents(ServerLevel l, BlockPos p, BlockState s) {
    long water = s.is(EngineRegistry.WATER_BLOCK.get()) ? s.getValue(EngineFluid.UNITS) * WaterAccount.SCALE : 0;
    var d = HydrologyData.get(l);
    var saved = d.composition.get(p.asLong());
    if (saved != null && saved.water() == water) return saved;
    return new WaterAccount.Parcel(water, boundary(s) ? water * WaterAccount.OCEAN_SALT : 0, 0);
  }
  private static void store(ServerLevel l, BlockPos p, WaterAccount.Parcel parcel) {
    var d = HydrologyData.get(l);
    WaterAccount.Parcel prior;
    if (parcel.water() == 0 || parcel.salt() == 0 && parcel.sediment() == 0 && !boundary(l.getBlockState(p))) {
      prior = d.composition.remove(p.asLong());
      if (prior != null) d.setDirty();
    } else {
      prior = d.composition.put(p.asLong(), parcel);
      if (!parcel.equals(prior)) d.setDirty();
    }
  }
  public static void transferred(ServerLevel l, BlockPos a, BlockPos b,
      WaterAccount.Parcel beforeA, WaterAccount.Parcel beforeB, int units, BlockState source) {
    var d = HydrologyData.get(l);
    var from = new WaterAccount(4096, beforeA);
    var to = new WaterAccount(4096, beforeB);
    if (WaterAccount.move(from, to, units * WaterAccount.SCALE) != units * WaterAccount.SCALE)
      throw new IllegalStateException("Committed fluid transfer exceeds parcel");
    store(l, a, from.snapshot()); store(l, b, to.snapshot());
    if (boundary(source) && enabled(l)) restoreOcean(l, a, source);
  }
  public static boolean restoreOcean(ServerLevel l, BlockPos p, BlockState anchor) {
    if (!boundary(anchor) || p.getY() != TerrainEngine.SEA - 1) return false;
    var old = l.getBlockState(p);
    if (!old.isAir() && !old.is(EngineRegistry.WATER_BLOCK.get())) return false;
    var d = HydrologyData.get(l);
    var water = new WaterAccount(4096, contents(l, p, old));
    long amount = Math.min(water.room(), d.ocean.water()) / WaterAccount.SCALE * WaterAccount.SCALE;
    if (amount == 0) return false;
    var addition = d.ocean.preview(amount);
    if (!water.add(addition)) return false;
    if (!write(l, p, anchor.setValue(EngineFluid.UNITS, (int) (water.water() / WaterAccount.SCALE)))) return false;
    d.ocean.take(amount); d.setDirty(); store(l, p, water.snapshot()); return true;
  }
  /** Ocean accepts only water that has reached sea level; a lake at that height is still finite. */
  public static boolean oceanSink(ServerLevel l, BlockPos from, BlockPos to) {
    if (!enabled(l) || to.getY() != TerrainEngine.SEA - 1) return false;
    var a = l.getBlockState(from); var b = l.getBlockState(to);
    if (!boundary(b) || boundary(a) || !a.is(EngineRegistry.WATER_BLOCK.get())) return false;
    int units = a.getValue(EngineFluid.UNITS);
    if (from.getY() + units / 16. < TerrainEngine.SEA) return false;
    var d = HydrologyData.get(l);
    int take = (int) Math.min(Math.min(4, units), d.ocean.room() / WaterAccount.SCALE);
    if (take == 0) return false;
    var account = new WaterAccount(4096, contents(l, from, a));
    var parcel = account.take(take * WaterAccount.SCALE);
    if (!write(l, from, units == take ? Blocks.AIR.defaultBlockState() : a.setValue(EngineFluid.UNITS, units - take))) return false;
    if (!d.ocean.add(parcel)) throw new IllegalStateException("Ocean reservation changed");
    d.setDirty();
    store(l, from, account.snapshot()); WaterRuntime.activate(l, from); return true;
  }
  /** Measured placement and pickup preserve chemistry, but never copy an ocean/source marker. */
  public static boolean place(ServerLevel l, BlockPos p, WaterAccount.Parcel parcel) {
    if (!l.hasChunkAt(p) || !l.getBlockState(p).isAir() || parcel.water() != 4096) return false;
    if (!write(l, p, EngineRegistry.WATER_BLOCK.get().defaultBlockState())) return false;
    store(l, p, parcel); WaterRuntime.activate(l, p); return true;
  }
  public static WaterAccount.Parcel pickup(ServerLevel l, BlockPos p) {
    var state = l.getBlockState(p);
    if (!state.is(EngineRegistry.WATER_BLOCK.get()) || state.getValue(EngineFluid.UNITS) != 16) return null;
    var parcel = contents(l, p, state);
    if (!write(l, p, Blocks.AIR.defaultBlockState())) return null;
    store(l, p, WaterAccount.Parcel.EMPTY);
    if (boundary(state) && enabled(l)) restoreOcean(l, p, state);
    WaterRuntime.activate(l, p); return parcel;
  }
  public static boolean emit(ServerLevel l, BlockPos p, Aquifer aquifer, double datum, int rate) {
    if (!l.isInWorldBounds(p) || !l.hasChunkAt(p)) return false;
    var old = l.getBlockState(p);
    if (!old.isAir() && !old.is(EngineRegistry.WATER_BLOCK.get())) return false;
    if (boundary(old)) return false;
    int units = old.isAir() ? 0 : old.getValue(EngineFluid.UNITS);
    int amount = aquifer.dischargeUnits(datum, p.getY() + units / 16., 16 - units, rate);
    if (amount == 0) return false;
    var addition = aquifer.ground.preview(amount * WaterAccount.SCALE);
    var water = new WaterAccount(4096, contents(l, p, old));
    if (!water.add(addition)) return false;
    var state = (old.isAir() ? EngineRegistry.WATER_BLOCK.get().defaultBlockState() : old)
        .setValue(EngineFluid.UNITS, units + amount);
    if (!write(l, p, state)) return false;
    aquifer.ground.take(addition.water()); HydrologyData.get(l).setDirty(); store(l, p, water.snapshot());
    WaterRuntime.activate(l, p); return true;
  }
  public static boolean infiltrate(ServerLevel l, BlockPos p, Aquifer aquifer, int rate) {
    var old = l.getBlockState(p);
    if (!old.is(EngineRegistry.WATER_BLOCK.get()) || boundary(old)) return false;
    int units = old.getValue(EngineFluid.UNITS);
    int amount = (int) Math.min(Math.min(units, rate), aquifer.ground.room() / WaterAccount.SCALE);
    if (amount <= 0) return false;
    var water = new WaterAccount(4096, contents(l, p, old));
    var parcel = water.take(amount * WaterAccount.SCALE);
    if (!write(l, p, units == amount ? Blocks.AIR.defaultBlockState() : old.setValue(EngineFluid.UNITS, units - amount))) return false;
    if (!aquifer.ground.add(parcel)) throw new IllegalStateException("Aquifer reservation changed");
    HydrologyData.get(l).setDirty();
    store(l, p, water.snapshot()); WaterRuntime.activate(l, p); return true;
  }
  private static double permeability(BlockState bed, Geology.Column g) {
    if (bed.is(Blocks.CLAY) || bed.is(Blocks.BEDROCK)) return 0;
    if (bed.is(Blocks.SAND) || bed.is(Blocks.GRAVEL)) return .9;
    if (bed.is(Blocks.DIRT) || bed.is(Blocks.GRASS_BLOCK)) return .5;
    if (bed.is(Blocks.STONE) || bed.is(Blocks.GRANITE) || bed.is(Blocks.ANDESITE)
        || bed.is(Blocks.DIORITE) || bed.is(Blocks.BASALT) || bed.is(Blocks.TUFF)
        || bed.is(Blocks.SANDSTONE) || bed.is(Blocks.CALCITE) || bed.is(Blocks.DEEPSLATE)) return g.permeability();
    return 0; // Constructed floors, glass, leaves and roofs are not recharge soils.
  }
  private static RegionWork region(Work work, TerrainEngine t, int x, int z) {
    long key = HydrologyData.key(x, z);
    return work.regions.computeIfAbsent(key, k -> {
      int ox = Math.floorDiv(x, 64) * 64, oz = Math.floorDiv(z, 64) * 64;
      double low = Double.POSITIVE_INFINITY;
      for (int dz = 8; dz < 64; dz += 16) for (int dx = 8; dx < 64; dx += 16)
        low = Math.min(low, t.column(ox + dx, oz + dz).ground());
      return new RegionWork(low + 1);
    });
  }
  private static void visit(ServerLevel l, Work work, int x, int z) {
    var t = ((EngineChunkGenerator) l.getChunkSource().getGenerator()).geography();
    var top = l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
    var p = top.below();
    // Ignore sparse plants/snow, but never tunnel through structures or a forest roof.
    for (int i = 0; i < 8 && l.getBlockState(p).canBeReplaced()
        && !l.getBlockState(p).is(EngineRegistry.WATER_BLOCK.get()); i++) p = p.below();
    var state = l.getBlockState(p);
    if (boundary(state)) { restoreOcean(l, p, state); return; }
    // One-time migration marks only a large, existing sea surface. Dry edited land stays dry.
    if (t.revision < 4 && p.getY() == TerrainEngine.SEA - 1 && t.ocean(x, z)
        && state.is(EngineRegistry.WATER_BLOCK.get()) && state.getValue(EngineFluid.UNITS) == 16) {
      boolean sea = true;
      for (Direction side : Direction.Plane.HORIZONTAL) {
        var n = p.relative(side, 4);
        if (!l.hasChunkAt(n) || WaterRuntime.units(l, n) != 16) { sea = false; break; }
      }
      if (sea) { write(l, p, state.setValue(EngineWaterBlock.OCEAN, true)); return; }
    }
    var sample = t.column(x, z);
    if (sample.water() == TerrainEngine.SEA && t.ocean(x, z)) return;
    var geology = new Geology(t.noise).column(x, z, sample);
    BlockPos bed = state.is(EngineRegistry.WATER_BLOCK.get()) ? p.below() : p;
    // Deep lakes infiltrate at their floor; finding it is strictly capped.
    for (int i = 0; i < 16 && l.getBlockState(bed).is(EngineRegistry.WATER_BLOCK.get()); i++) bed = bed.below();
    double permeable = permeability(l.getBlockState(bed), geology);
    if (permeable == 0) return;
    var d = HydrologyData.get(l);
    var aquifer = d.aquifer(x, z, sample.rain(), l.getGameTime());
    var regional = region(work, t, x, z);
    boolean environmentalStep = aquifer.step(l.getGameTime());
    if (environmentalStep) {
      regional.tick = l.getGameTime(); regional.discharged = 0;
      if (sample.temperature() > .25) WaterAccount.move(aquifer.snow, aquifer.ground, 4096);
      // Coarse ocean evaporation closes the circulation account; salt stays in the ocean.
      d.ocean.evaporateInto(d.atmosphere, Math.min(4096,
          Math.max(0, WaterAccount.MAX / 4 - d.atmosphere.water())));
      // Groundwater evapotranspiration returns pure water; constituents stay in the ground.
      if (!l.isRaining()) aquifer.ground.evaporateInto(d.atmosphere, 64);
      d.setDirty();
    }
    if (environmentalStep && l.isRaining() && l.canSeeSky(top)) {
      // Regional precipitation is independent of the number of source outlets or scan visits.
      // Only the currently loaded catchment contributes, once per fixed environmental step.
      int loadedColumns = 0;
      int regionChunkX = Math.floorDiv(x, 64) * 4, regionChunkZ = Math.floorDiv(z, 64) * 4;
      for (int cz = 0; cz < 4; cz++) for (int cx = 0; cx < 4; cx++)
        if (l.hasChunk(regionChunkX + cx, regionChunkZ + cz)) loadedColumns += 256;
      var kind = l.getBiome(top).value().getPrecipitationAt(top, l.getSeaLevel());
      var target = kind == net.minecraft.world.level.biome.Biome.Precipitation.SNOW ? aquifer.snow : aquifer.ground;
      if (kind != net.minecraft.world.level.biome.Biome.Precipitation.NONE)
        WaterAccount.move(d.atmosphere, target, (long) (HydrologyConfig.RAIN_QUANTA.get() * permeable * loadedColumns));
      d.setDirty();
    }
    // A higher surface recharges; a lower exposed outlet can discharge. Never both on one visit.
    double head = aquifer.head(regional.datum);
    if (state.is(EngineRegistry.WATER_BLOCK.get()) && p.getY() + state.getValue(EngineFluid.UNITS) / 16. > head + .5) {
      if (Math.floorMod(x * 31L + z * 17L + l.getGameTime() / 20, 8) < Math.ceil(permeable * 8))
        infiltrate(l, p, aquifer, 1);
    } else if (regional.discharged < 32) {
      var outlet = state.is(EngineRegistry.WATER_BLOCK.get()) ? p : p.above();
      boolean naturalWaterway = sample.wet() && outlet.getY() < sample.water();
      // Seeded groundwater supports existing waterways. A new dry-land outlet needs
      // sustained additional recharge, not merely visiting an untouched chunk.
      if (!aquifer.canSupply(naturalWaterway)) return;
      long before = aquifer.ground.water();
      emit(l, outlet, aquifer, regional.datum, Math.min(HydrologyConfig.SOURCE_RATE.get(), 32 - regional.discharged));
      regional.discharged += (int) ((before - aquifer.ground.water()) / WaterAccount.SCALE);
    }
  }
  @SubscribeEvent public void loaded(ChunkEvent.Load e) {
    if (e.getLevel() instanceof ServerLevel l && e.getChunk() instanceof LevelChunk
        && l.getChunkSource().getGenerator() instanceof EngineChunkGenerator)
      WORK.computeIfAbsent(l, k -> new Work()).chunks.putIfAbsent(e.getChunk().getPos().pack(), new Cursor());
  }
  @SubscribeEvent public void tick(LevelTickEvent.Post e) {
    if (!(e.getLevel() instanceof ServerLevel l) || !enabled(l)) return;
    var work = WORK.get(l); if (work == null) return;
    int limit = HydrologyConfig.COLUMNS.get();
    long deadline = System.nanoTime() + 2_000_000;
    for (int i = 0; i < limit && !work.chunks.isEmpty() && System.nanoTime() < deadline; i++) {
      var entry = work.chunks.pollFirstEntry();
      var cp = new net.minecraft.world.level.ChunkPos((int) (long) entry.getKey(), (int) (entry.getKey() >> 32));
      var chunk = l.getChunkSource().getChunk(cp.x(), cp.z(), ChunkStatus.FULL, false);
      if (!(chunk instanceof LevelChunk)) continue;
      var c = entry.getValue(); int col = c.column++ & 255;
      work.chunks.putIfAbsent(entry.getKey(), c);
      long now = l.getGameTime();
      if (now - c.visited[col] < 20 || now <= c.visited[col]) continue;
      c.visited[col] = now;
      // Multiplication by an odd number is a permutation, avoiding repeated strip sampling.
      int shuffled = col * 73 & 255;
      visit(l, work, cp.getMinBlockX() + (shuffled & 15), cp.getMinBlockZ() + (shuffled >> 4));
    }
  }
  @SubscribeEvent public void unloaded(ChunkEvent.Unload e) {
    if (!(e.getLevel() instanceof ServerLevel l)) return;
    var w = WORK.get(l); if (w == null) return;
    w.chunks.remove(e.getChunk().getPos().pack());
    // Cache follows loaded geography, not the lifetime exploration footprint.
    if (w.chunks.isEmpty()) w.regions.clear();
    else if (w.regions.size() > w.chunks.size()) w.regions.clear();
  }
  @SubscribeEvent public void levelUnloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload e) {
    if (e.getLevel() instanceof ServerLevel l) WORK.remove(l);
  }
  @SubscribeEvent public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e) { WORK.clear(); }
}
