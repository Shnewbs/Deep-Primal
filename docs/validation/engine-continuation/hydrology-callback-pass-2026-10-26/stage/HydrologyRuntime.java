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
  private static final int REGIONAL_DISCHARGE_UNITS = 32;
  private static final class Cursor {
    int column;
    final long[] visited = new long[256];
    final double[] streamSurface = new double[256];
    final int[] streamOutletY = new int[256];
    final boolean[] streamCandidate = new boolean[256];
    final long[] replenished = new long[256];
    { Arrays.fill(replenished, Long.MIN_VALUE); }
    final int[] streams = new int[256];
    int streamCount, streamNext, surveyed;
    /** Keeps planned columns ordered headwater-first; call after {@link #plan}. */
    void candidate(int column, boolean wet) {
      if (streamCandidate[column] == wet) return;
      streamCandidate[column] = wet;
      if (wet) {
        int i = streamCount++;
        for (; i > 0 && streamSurface[streams[i - 1]] < streamSurface[column]; i--) streams[i] = streams[i - 1];
        streams[i] = column;
        return;
      }
      for (int i = 0; i < streamCount; i++) if (streams[i] == column) {
        System.arraycopy(streams, i + 1, streams, i, --streamCount - i); return;
      }
    }
    double headwater() { return streamCount == 0 ? Double.NEGATIVE_INFINITY : streamSurface[streams[0]]; }
  }
  private static final class RegionWork {
    final double datum;
    long tick = Long.MIN_VALUE;
    long precipitation = Long.MIN_VALUE;
    // Cache reconstruction cannot grant another allowance inside an existing aquifer step.
    // Fail closed until aquifer.step authorizes the next environmental interval.
    int discharged = REGIONAL_DISCHARGE_UNITS;
    // Planned waterways draw on the region's long-run baseflow, separately from new dry springs.
    int natural = 1 << 16; // fail closed above any configurable baseflow quota
    RegionWork(double datum) { this.datum = datum; }
  }
  private static final class Work {
    final LinkedHashMap<Long, Cursor> chunks = new LinkedHashMap<>();
    final StreamSourceIndex<Cursor> streamChunks = new StreamSourceIndex<>();
    final LinkedHashSet<Long> survey = new LinkedHashSet<>();
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
  public record Transfer(WaterAccount.Parcel from, WaterAccount.Parcel to) {}
  /** Prepare exact volume and constituent results before any native block writes. */
  public static Transfer prepareTransfer(WaterAccount.Parcel beforeA,
      WaterAccount.Parcel beforeB, int units) {
    if (units <= 0 || units > 16) return null;
    var from = new WaterAccount(4096, beforeA);
    var to = new WaterAccount(4096, beforeB);
    if (WaterAccount.move(from, to, units * WaterAccount.SCALE) != units * WaterAccount.SCALE)
      return null;
    return new Transfer(from.snapshot(), to.snapshot());
  }
  public static void transferred(ServerLevel l, BlockPos a, BlockPos b,
      Transfer transfer, BlockState source) {
    store(l, a, transfer.from()); store(l, b, transfer.to());
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
    if (!d.ocean.canAccept(parcel)) return false;
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
    if (!aquifer.ground.canAccept(parcel)) return false;
    if (!write(l, p, units == amount ? Blocks.AIR.defaultBlockState() : old.setValue(EngineFluid.UNITS, units - amount))) return false;
    if (!aquifer.ground.add(parcel)) throw new IllegalStateException("Aquifer reservation changed");
    HydrologyData.get(l).setDirty();
    store(l, p, water.snapshot()); WaterRuntime.activate(l, p); return true;
  }
  /** One actual solid block becomes one finite water block; no source or reservoir is copied. */
  public static boolean meltIce(ServerLevel l,BlockPos p,double temperature) {
    if(temperature<=.25||l.getChunkSource().getChunkNow(Math.floorDiv(p.getX(),16),Math.floorDiv(p.getZ(),16))==null)return false;
    var state=l.getBlockState(p);
    if(!(state.is(Blocks.ICE)||state.is(Blocks.PACKED_ICE)||state.is(Blocks.BLUE_ICE)||state.is(Blocks.SNOW_BLOCK)))return false;
    if(l.getHeight(Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ())>p.getY()+1)return false;
    if(!write(l,p,EngineRegistry.WATER_BLOCK.get().defaultBlockState()))return false;
    store(l,p,new WaterAccount.Parcel(4096,0,0));WaterRuntime.activate(l,p);return true;
  }
  private static double permeability(BlockState bed, TerrainNoise noise, int x, int z) {
    if (bed.getBlock() instanceof GroundMaterials.SoilBlock soil) return soil.texture().permeability();
    if (bed.is(Blocks.CLAY) || bed.is(Blocks.BEDROCK)) return 0;
    if (bed.is(Blocks.SAND) || bed.is(Blocks.GRAVEL)) return .9;
    if (bed.is(Blocks.DIRT) || bed.is(Blocks.GRASS_BLOCK)) return .5;
    if (bed.is(Blocks.STONE) || bed.is(Blocks.GRANITE) || bed.is(Blocks.ANDESITE)
        || bed.is(Blocks.DIORITE) || bed.is(Blocks.BASALT) || bed.is(Blocks.TUFF)
        || bed.is(Blocks.SANDSTONE) || bed.is(Blocks.CALCITE) || bed.is(Blocks.DEEPSLATE))
      return new Geology(noise).permeability(x, z);
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
  /** One representative column per loaded chunk, weighted by its 256-column footprint. */
  private static void precipitation(ServerLevel l, TerrainEngine t, int x, int z,
      HydrologyData data, Aquifer aquifer) {
    long rain = 0, snow = 0;
    int chunkX = Math.floorDiv(x, 64) * 4, chunkZ = Math.floorDiv(z, 64) * 4;
    for (int cz = 0; cz < 4; cz++) for (int cx = 0; cx < 4; cx++) {
      if (l.getChunkSource().getChunkNow(chunkX + cx, chunkZ + cz) == null) continue;
      int sx = (chunkX + cx) * 16 + 8, sz = (chunkZ + cz) * 16 + 8;
      var top = l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(sx, 0, sz));
      if (!l.canSeeSky(top)) continue;
      var p = top.below();
      for (int i = 0; i < 8 && l.getBlockState(p).canBeReplaced()
          && !l.getBlockState(p).is(EngineRegistry.WATER_BLOCK.get()); i++) p = p.below();
      var state = l.getBlockState(p);
      if (boundary(state)) continue;
      var bed = p;
      for (int i = 0; i < 16 && l.getBlockState(bed).is(EngineRegistry.WATER_BLOCK.get()); i++)
        bed = bed.below();
      var bedState = l.getBlockState(bed);
      // Reject common roofs before doing any terrain/geology work.
      if (bedState.is(Blocks.BEDROCK)) continue;
      boolean ice = t.revision >= 6 && (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE)
          || state.is(Blocks.BLUE_ICE) || state.is(Blocks.SNOW_BLOCK));
      double permeable = ice ? new Geology(t.noise).permeability(sx, sz)
          : permeability(bedState, t.noise, sx, sz);
      if (permeable <= 0) continue;
      var kind = l.getBiome(top).value().getPrecipitationAt(top, l.getSeaLevel());
      long amount = (long) (HydrologyConfig.RAIN_QUANTA.get() * permeable * 256);
      if (kind == net.minecraft.world.level.biome.Biome.Precipitation.SNOW) snow += amount;
      else if (kind == net.minecraft.world.level.biome.Biome.Precipitation.RAIN) rain += amount;
    }
    WaterAccount.distribute(data.atmosphere, aquifer.ground, aquifer.snow, rain, snow);
    data.setDirty();
  }
  private static void visit(ServerLevel l, Work work, Cursor cursor, int column,
      TerrainEngine t, int x, int z) {
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
    if (sample.water() == TerrainEngine.SEA && t.ocean(x, z)) {
      cursor.candidate(column, false);
      return;
    }
    if (sample.wet()) plan(cursor, column, sample.water());
    cursor.candidate(column, sample.wet());
    BlockPos bed = state.is(EngineRegistry.WATER_BLOCK.get()) ? p.below() : p;
    // Deep lakes infiltrate at their floor; finding it is strictly capped.
    for (int i = 0; i < 16 && l.getBlockState(bed).is(EngineRegistry.WATER_BLOCK.get()); i++) bed = bed.below();
    boolean ice=t.revision>=6 && (state.is(Blocks.ICE)||state.is(Blocks.PACKED_ICE)||state.is(Blocks.BLUE_ICE)||state.is(Blocks.SNOW_BLOCK));
    double permeable = ice ? new Geology(t.noise).permeability(x, z)
        : permeability(l.getBlockState(bed), t.noise, x, z);
    var d = HydrologyData.get(l);
    // A sealed column cannot seed groundwater, but must not freeze an existing regional clock.
    var aquifer = permeable == 0 ? d.aquifers.get(HydrologyData.key(x, z))
        : d.aquifer(x, z, sample.rain(), l.getGameTime());
    if (aquifer == null) return;
    var regional = region(work, t, x, z);
    boolean environmentalStep = aquifer.step(l.getGameTime());
    if (environmentalStep) {
      regional.tick = l.getGameTime(); regional.discharged = 0; regional.natural = 0;
      double seasonal=sample.temperature()+(t.revision>=6?.18*Math.sin(2*Math.PI*Math.floorMod(l.getGameTime(),24000L*96)/(24000.*96)):0);
      if (seasonal > .25) WaterAccount.move(aquifer.snow, aquifer.ground, 4096);
      if(ice)meltIce(l,p,seasonal);
      // Coarse ocean evaporation closes the circulation account; salt stays in the ocean.
      d.ocean.evaporateInto(d.atmosphere, Math.min(4096,
          Math.max(0, WaterAccount.MAX / 4 - d.atmosphere.water())));
      // Groundwater evapotranspiration returns pure water; constituents stay in the ground.
      if (!l.isRaining()) aquifer.ground.evaporateInto(d.atmosphere, 64);
      d.setDirty();
    }
    if (regional.tick != Long.MIN_VALUE && regional.precipitation != regional.tick
        && l.isRaining()) {
      precipitation(l, t, x, z, d, aquifer);
      regional.precipitation = regional.tick;
    }
    if (permeable == 0) return;
    if(ice)return; // Solid ice is never treated as an outlet/recharge block after the one-time melt.
    // A higher surface recharges; a lower exposed outlet can discharge. Never both on one visit.
    // Perched upstream reaches do not share the lowest terrain head of the whole region.
    double datum = sample.wet() ? Aquifer.naturalDatum(sample.water()) : regional.datum;
    double head = aquifer.head(datum);
    if (state.is(EngineRegistry.WATER_BLOCK.get())
        && p.getY() + state.getValue(EngineFluid.UNITS) / 16. > head + .5) {
      if (Math.floorMod(x * 31L + z * 17L + l.getGameTime() / 20, 8) < Math.ceil(permeable * 8))
        infiltrate(l, p, aquifer, 1);
    } else if (regional.discharged < REGIONAL_DISCHARGE_UNITS) {
      var outlet = state.is(EngineRegistry.WATER_BLOCK.get()) ? p : p.above();
      boolean naturalWaterway = sample.wet() && outlet.getY() < sample.water();
      if (naturalWaterway) return;
      // Seeded groundwater supports existing waterways. A new dry-land outlet needs
      // sustained additional recharge, not merely visiting an untouched chunk.
      if (!aquifer.canSupply(false)) return;
      int outletUnits = WaterRuntime.units(l, outlet);
      int sourceCapacity = 16 - outletUnits;
      if (sourceCapacity <= 0) return;
      long before = aquifer.ground.water();
      emit(l, outlet, aquifer, regional.datum, Math.min(sourceCapacity,
          Math.min(HydrologyConfig.SOURCE_RATE.get(), REGIONAL_DISCHARGE_UNITS - regional.discharged)));
      regional.discharged += (int) ((before - aquifer.ground.water()) / WaterAccount.SCALE);
    }
  }
  private static void plan(Cursor cursor, int column, double surface) {
    cursor.streamSurface[column] = surface;
    cursor.streamOutletY[column] = (int) Math.ceil(surface) - 1;
  }
  /** Finds generated waterway columns soon after load; only visible engine water is sampled. */
  private static void survey(ServerLevel l, Work work, TerrainEngine t, long deadline) {
    while (!work.survey.isEmpty() && System.nanoTime() - deadline < 0) {
      long key = work.survey.removeFirst();
      var c = work.chunks.get(key);
      if (c == null) continue;
      var cp = new net.minecraft.world.level.ChunkPos((int) key, (int) (key >> 32));
      if (!(l.getChunkSource().getChunk(cp.x(), cp.z(), ChunkStatus.FULL, false) instanceof LevelChunk)) {
        work.survey.addLast(key); return;
      }
      for (; c.surveyed < 256 && System.nanoTime() - deadline < 0; c.surveyed++) {
        int col = c.surveyed, shuffled = col * 73 & 255;
        int x = cp.getMinBlockX() + (shuffled & 15), z = cp.getMinBlockZ() + (shuffled >> 4);
        var p = l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).below();
        for (int i = 0; i < 8 && l.getBlockState(p).canBeReplaced()
            && !l.getBlockState(p).is(EngineRegistry.WATER_BLOCK.get()); i++) p = p.below();
        var state = l.getBlockState(p);
        if (!state.is(EngineRegistry.WATER_BLOCK.get()) || boundary(state) || c.streamCandidate[col]) continue;
        var sample = t.column(x, z);
        if (!sample.wet() || sample.water() == TerrainEngine.SEA && t.ocean(x, z)) continue;
        plan(c, col, sample.water());
        c.candidate(col, true);
      }
      if (c.surveyed == 256 || work.streamChunks.contains(key)) refreshSource(work, key, c);
      if (c.surveyed < 256) { work.survey.addFirst(key); return; }
    }
  }
  private static void refreshSource(Work work, long key, Cursor cursor) {
    if (work.chunks.get(key) != cursor) {
      work.streamChunks.remove(key, cursor);
      return;
    }
    if (cursor.streamCount == 0) work.streamChunks.deactivate(key);
    else work.streamChunks.put(key, cursor, cursor.headwater());
  }
  private static void replenishStream(ServerLevel l, Work work, TerrainEngine t,
      Cursor cursor, int column, int x, int z) {
    if (!cursor.streamCandidate[column]) return;
    long now = l.getGameTime(), last = cursor.replenished[column];
    // At most one top-up per planned column per tick.
    if (last == now) return;
    var regional = region(work, t, x, z);
    int quota = Aquifer.baseflowUnits(HydrologyConfig.RAIN_QUANTA.get());
    // The interval's finite baseflow is released linearly across its 20 ticks instead of as a
    // single headwater slug, which travelled down steep reaches as a wave and dried the bed
    // between pulses. The per-interval total and its debit are unchanged.
    int allowance = streamAllowance(quota, regional.tick, now);
    if (regional.natural >= allowance) return;
    cursor.replenished[column] = now;
    var outlet = new BlockPos(x, cursor.streamOutletY[column], z);
    if (!l.isInWorldBounds(outlet) || !l.hasChunkAt(outlet)) return;
    var state = l.getBlockState(outlet);
    if ((!state.isAir() && !state.is(EngineRegistry.WATER_BLOCK.get())) || boundary(state)) return;
    var data = HydrologyData.get(l);
    var aquifer = data.aquifers.get(HydrologyData.key(x, z));
    if (aquifer == null || !aquifer.canSupply(true)) return;
    int units = WaterRuntime.units(l, outlet);
    int sourceCapacity =
        StreamSourceCapacity.remainingUnits(cursor.streamSurface[column], outlet.getY(), units);
    if (sourceCapacity <= 0) return;
    // Pay into the actual deficit cell. Cells below the outlet lie wholly under the planned
    // surface, so filling them can never raise the reach above its generated stage.
    var target = outlet;
    for (int i = 0; units == 0 && i < 2; i++) {
      var below = target.below();
      var b = l.getBlockState(below);
      if (boundary(b) || !(b.isAir() || b.is(EngineRegistry.WATER_BLOCK.get()) && b.getValue(EngineFluid.UNITS) < 16)) break;
      target = below; sourceCapacity = 16 - WaterRuntime.units(l, below);
      if (!b.isAir()) break;
    }
    long before = aquifer.ground.water();
    // Natural springs are limited by aquifer head pressure, planned capacity and the regional
    // baseflow quota; the fixed SOURCE_RATE only meters dry-land outlets.
    emit(l, target, aquifer, Aquifer.naturalDatum(cursor.streamSurface[column]),
        Math.min(sourceCapacity, allowance - regional.natural));
    regional.natural += (int) ((before - aquifer.ground.water()) / WaterAccount.SCALE);
  }
  /** Cumulative baseflow released by tick {@code now} of the interval that began at {@code start}. */
  static int streamAllowance(int quota, long start, long now) {
    if (start == Long.MIN_VALUE || now < start) return quota;
    // With ordered signed ticks, negative subtraction means elapsed-time overflow.
    // Such an interval is already complete; never turn it into a negative quota.
    long elapsed = now - start;
    long phase = elapsed < 0 ? 19 : Math.min(19, elapsed);
    return (int) (quota * (phase + 1) / 20);
  }
  @SubscribeEvent public void loaded(ChunkEvent.Load e) {
    if (e.getLevel() instanceof ServerLevel l && e.getChunk() instanceof LevelChunk
        && l.getChunkSource().getGenerator() instanceof EngineChunkGenerator) {
      var work = WORK.computeIfAbsent(l, k -> new Work());
      long key = e.getChunk().getPos().pack();
      if (work.chunks.putIfAbsent(key, new Cursor()) == null) work.survey.add(key);
    }
  }
  @SubscribeEvent public void tick(LevelTickEvent.Post e) {
    if (!(e.getLevel() instanceof ServerLevel l) || !enabled(l)) return;
    var work = WORK.get(l); if (work == null) return;
    var t = ((EngineChunkGenerator) l.getChunkSource().getGenerator()).geography();
    int limit = HydrologyConfig.COLUMNS.get();
    long start = System.nanoTime(), deadline = start + 2_000_000;
    survey(l, work, t, start + 1_000_000);
    // Preserve admission time for regional recharge/accounting after the source pass.
    long streamDeadline = limit > 0 ? deadline - 500_000 : deadline;
    // Planned waterways get their own bounded cadence; scan breadth must not starve a source.
    // The finite regional quota is offered headwater-first: springs feed the head of a reach and
    // lower columns are fed by flow, so downstream deficits must not consume the headwater share.
    // Lifecycle updates maintain headwater order; no whole-index scan/copy/sort per tick.
    // Production cleanup occurred after survey: expire dry registration priority at this boundary.
    work.streamChunks.beginPass();
    var order = work.streamChunks.iterator();
    while (System.nanoTime() - streamDeadline < 0 && order.hasNext()) {
      var entry = order.next();
      var c = entry.getValue();
      var cp = new net.minecraft.world.level.ChunkPos((int) (long) entry.getKey(), (int) (entry.getKey() >> 32));
      var sourceChunk = l.getChunkSource().getChunk(cp.x(), cp.z(), ChunkStatus.FULL, false);
      // Native callbacks may unload/replace a source. Finish the current transaction only;
      // do not continue an obsolete cursor or a structurally changed TreeSet traversal.
      if (!order.valid()) break;
      if (!(sourceChunk instanceof LevelChunk)) {
        forgetChunk(work, entry.getKey());
        continue;
      }
      for (int s = 0; s < c.streamCount && order.valid() && System.nanoTime() - streamDeadline < 0; s++) {
        int col = c.streams[s], shuffled = col * 73 & 255;
        replenishStream(l, work, t, c, col, cp.getMinBlockX() + (shuffled & 15), cp.getMinBlockZ() + (shuffled >> 4));
      }
    }
    for (int i = 0; i < limit && !work.chunks.isEmpty() && System.nanoTime() - deadline < 0; i++) {
      var entry = work.chunks.pollFirstEntry();
      var cp = new net.minecraft.world.level.ChunkPos((int) (long) entry.getKey(), (int) (entry.getKey() >> 32));
      var chunk = l.getChunkSource().getChunk(cp.x(), cp.z(), ChunkStatus.FULL, false);
      if (!(chunk instanceof LevelChunk)) {
        forgetChunk(work, entry.getKey());
        continue;
      }
      var c = entry.getValue();
      work.chunks.putIfAbsent(entry.getKey(), c);
      long now = l.getGameTime();
      // Rotate allocation order each aquifer interval; 73 is coprime with 256.
      int scanOffset = (int) ((now / 20 * 73L) & 255);
      int col = (c.column++ + scanOffset) & 255;
      // Multiplication by an odd number is a permutation, avoiding repeated strip sampling.
      int shuffled = col * 73 & 255;
      int x = cp.getMinBlockX() + (shuffled & 15);
      int z = cp.getMinBlockZ() + (shuffled >> 4);
      if (now > c.visited[col] && now - c.visited[col] >= 20) {
        c.visited[col] = now;
        visit(l, work, c, col, t, x, z);
        refreshSource(work, entry.getKey(), c);
      }
    }
  }
  @SubscribeEvent public void unloaded(ChunkEvent.Unload e) {
    if (!(e.getLevel() instanceof ServerLevel l)) return;
    var w = WORK.get(l); if (w == null) return;
    forgetChunk(w, e.getChunk().getPos().pack());
  }
  /** Share invalidation with the no-longer-FULL path, which may precede an unload event. */
  private static void forgetChunk(Work w, long key) {
    w.chunks.remove(key);
    w.streamChunks.remove(key);
    w.survey.remove(key);
    // Cache follows loaded geography, not the lifetime exploration footprint.
    if (w.chunks.isEmpty()) w.regions.clear();
    else if (w.regions.size() > w.chunks.size()) w.regions.clear();
  }
  @SubscribeEvent public void levelUnloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload e) {
    if (e.getLevel() instanceof ServerLevel l) WORK.remove(l);
  }
  @SubscribeEvent public void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e) { WORK.clear(); }
}
