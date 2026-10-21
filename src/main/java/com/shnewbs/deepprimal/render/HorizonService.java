package com.shnewbs.deepprimal.render;

import com.shnewbs.deepprimal.core.ByteLru;
import com.shnewbs.deepprimal.core.FarTerrain;
import com.shnewbs.deepprimal.core.HorizonGeometry;
import com.shnewbs.deepprimal.core.HorizonSamples;
import com.shnewbs.deepprimal.core.WorkBudget;
import com.shnewbs.deepprimal.render.network.HorizonRequest;
import com.shnewbs.deepprimal.render.network.HorizonTile;
import com.shnewbs.deepprimal.worldgen.ColumnSurface;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Cooperative server-thread sampling; no chunk tickets and no unsafe worker access to density
 * state.
 */
public final class HorizonService {
  private static final ArrayDeque<Job> JOBS = new ArrayDeque<>();
  private static final Map<UUID, Long> LAST = new HashMap<>();
  private static final Map<net.minecraft.resources.Identifier, java.util.Optional<DensityFunction>>
      SOURCES = new HashMap<>();
  private static final ByteLru<TileKey, HorizonSamples> CACHE =
      new ByteLru<>(8 * 1048576L, FarTerrain.MAX_TILES, HorizonSamples::payloadBytes);
  private static final WorkBudget BUDGET = new WorkBudget();
  private static long tickStart;
  public static long samples,
      completed,
      sharedRequests,
      sentBytes,
      samplingNanos,
      cancelled,
      reusedSamples;

  public static int cachedTiles() {
    return CACHE.size();
  }

  public static long cacheHits() {
    return CACHE.hits();
  }

  public static long cacheBytes() {
    return CACHE.bytes();
  }

  public static double workFraction() {
    return BUDGET.fraction();
  }

  public static void register(RegisterPayloadHandlersEvent event) {
    // Different sample counts require a new optional channel version; old peers decline safely.
    var registrar = event.registrar("3").optional();
    registrar.playToServer(
        HorizonRequest.TYPE, HorizonRequest.CODEC, (p, c) -> request(p, (ServerPlayer) c.player()));
    registrar.playToClient(HorizonTile.TYPE, HorizonTile.CODEC);
  }

  private static boolean relevant(Waiter w) {
    return !w.player.hasDisconnected()
        && w.player.level().dimension().identifier().equals(w.request.dimension())
        && FarTerrain.inRange(
            w.request.x(),
            w.request.z(),
            w.player.getX(),
            w.player.getZ(),
            FarTerrain.MAX_DISTANCE);
  }

  private static void send(Waiter w, HorizonSamples data) {
    if (!relevant(w)) return;
    var p = w.request;
    if (data.detail() != p.detail()) {
      TileKey key = new TileKey(p.dimension(), p.x(), p.z(), p.detail());
      HorizonSamples reduced = CACHE.get(key);
      if (reduced == null) {
        reduced = data.forDetail(p.detail());
        reusedSamples += reduced.heights().length;
        CACHE.put(key, reduced);
      }
      data = reduced;
    }
    PacketDistributor.sendToPlayer(
        w.player,
        new HorizonTile(
            p.session(),
            p.dimension(),
            p.x(),
            p.z(),
            p.detail(),
            true,
            data.heights(),
            data.colors()));
    sentBytes += data.heights().length * 6L;
  }

  private static void request(HorizonRequest p, ServerPlayer player) {
    var level = player.level();
    long now = level.getServer().getTickCount();
    if (p.detail() < 0
        || p.detail() > 2
        || !level.dimension().identifier().equals(p.dimension())
        || Math.abs((long) p.x()) > 58000
        || Math.abs((long) p.z()) > 58000
        || !FarTerrain.inRange(p.x(), p.z(), player.getX(), player.getZ(), FarTerrain.MAX_DISTANCE)
        || now - LAST.getOrDefault(player.getUUID(), now - 4) < 4) return;
    // A new client session supersedes its old waiter without disturbing other players.
    JOBS.removeIf(
        job -> {
          job.waiters.removeIf(w -> w.player == player && w.request.session() != p.session());
          if (!job.waiters.isEmpty()) return false;
          cancelled++;
          return true;
        });
    // One waiter per player, even when several players share a job.
    for (Job job : JOBS) for (Waiter pending : job.waiters) if (pending.player == player) return;
    LAST.put(player.getUUID(), now);
    TileKey key = new TileKey(p.dimension(), p.x(), p.z(), p.detail());
    Waiter waiter = new Waiter(player, p);
    HorizonSamples cached = CACHE.get(key);
    if (cached == null)
      for (int finer = p.detail() - 1; finer >= 0; finer--) {
        var available = CACHE.get(new TileKey(p.dimension(), p.x(), p.z(), finer));
        if (available != null) {
          cached = available.forDetail(p.detail());
          reusedSamples += cached.heights().length;
          CACHE.put(key, cached);
          break;
        }
      }
    if (cached != null) {
      send(waiter, cached);
      return;
    }
    for (Job job : JOBS)
      if (job.key.dimension.equals(key.dimension)
          && job.key.x == key.x
          && job.key.z == key.z
          && job.key.detail <= key.detail) {
        if (job.waiters.size() < 32) {
          job.waiters.add(waiter);
          sharedRequests++;
        }
        return;
      }
    if (JOBS.size() >= 32) return;
    var source =
        SOURCES.computeIfAbsent(
            p.dimension(),
            dimension -> {
              ColumnSurface[] found = new ColumnSurface[1];
              level
                  .getChunkSource()
                  .randomState()
                  .router()
                  .preliminarySurfaceLevel()
                  .mapAll(
                      f -> {
                        if (f instanceof ColumnSurface column) found[0] = column;
                        return f;
                      });
              return found[0] == null
                  ? java.util.Optional.empty()
                  : java.util.Optional.of(found[0].height());
            });
    if (source.isEmpty()) {
      PacketDistributor.sendToPlayer(
          player,
          new HorizonTile(
              p.session(),
              p.dimension(),
              p.x(),
              p.z(),
              p.detail(),
              false,
              new short[0],
              new int[0]));
      return;
    }
    JOBS.add(new Job(key, waiter, source.get(), level));
  }

  @SubscribeEvent
  public void pre(ServerTickEvent.Pre event) {
    tickStart = System.nanoTime();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    long start = System.nanoTime();
    var runtime = Runtime.getRuntime();
    BUDGET.observe(
        (start - tickStart) / 1e6,
        45,
        (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory());
    long end = start + (long) (1_000_000 * BUDGET.fraction());
    int work = 0;
    while (!JOBS.isEmpty() && work < 64 && System.nanoTime() < end) {
      Job job = JOBS.removeFirst();
      job.waiters.removeIf(w -> !relevant(w));
      if (job.waiters.isEmpty() || start - job.created > 120_000_000_000L) {
        cancelled++;
        continue;
      }
      int cells = HorizonGeometry.cells(job.key.detail),
          width = cells + 1,
          step = FarTerrain.TILE_BLOCKS / cells;
      int i = job.next++, x = job.key.x * FarTerrain.TILE_BLOCKS + i % width * step;
      int z = job.key.z * FarTerrain.TILE_BLOCKS + i / width * step;
      var river = job.waterway == null ? null : job.waterway.sample(x, z);
      int h =
          (int)
              Math.clamp(
                  Math.floor(
                      river == null
                          ? job.height.compute(new DensityFunction.SinglePointContext(x, 0, z))
                          : river.height()),
                  job.minY + 8,
                  job.maxY - 16);
      int water = river == null ? job.seaLevel : (int) Math.floor(river.waterLevel());
      job.heights[i] = (short) Math.max(water, h);
      var biome =
          job.level
              .getChunkSource()
              .getGenerator()
              .getBiomeSource()
              .getNoiseBiome(
                  net.minecraft.core.QuartPos.fromBlock(x),
                  net.minecraft.core.QuartPos.fromBlock(h),
                  net.minecraft.core.QuartPos.fromBlock(z),
                  job.level.getChunkSource().randomState().sampler())
              .value();
      int color =
          com.shnewbs.deepprimal.core.WaterDynamics.palette(
              biome.getWaterColor(), biome.getBaseTemperature(), biome.hasPrecipitation() ? 1 : 0);
      job.colors[i] =
          h < water
              ? 0xf9000000 | color
              : h < job.seaLevel + 4
                  ? 0xffb9af80
                  : h > 360
                      ? 0xffdee2e1
                      : h > 224 ? 0xff747c6b : 0xff000000 | biome.getGrassColor(x, z);
      work++;
      samples++;
      if (job.next == job.heights.length) {
        completed++;
        var data = new HorizonSamples(job.key.detail, job.heights, job.colors);
        CACHE.put(job.key, data);
        job.waiters.forEach(w -> send(w, data));
      } else JOBS.addLast(job); // Sample-level fairness under one global budget.
    }
    samplingNanos += System.nanoTime() - start;
    if (event.getServer().getTickCount() % 200 == 0)
      LAST.keySet().removeIf(id -> event.getServer().getPlayerList().getPlayer(id) == null);
  }

  @SubscribeEvent
  public void reload(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
    if (event.getPlayer() == null) {
      JOBS.clear();
      SOURCES.clear();
      CACHE.clear();
    }
  }

  @SubscribeEvent
  public void stopped(ServerStoppedEvent event) {
    JOBS.clear();
    LAST.clear();
    SOURCES.clear();
    CACHE.clear();
    BUDGET.reset();
    tickStart = 0;
    samples =
        completed = sharedRequests = sentBytes = samplingNanos = cancelled = reusedSamples = 0;
  }

  private record TileKey(net.minecraft.resources.Identifier dimension, int x, int z, int detail) {}

  private record Waiter(ServerPlayer player, HorizonRequest request) {}

  public static int pending() {
    return JOBS.size();
  }

  private static final class Job {
    final net.minecraft.server.level.ServerLevel level;
    final com.shnewbs.deepprimal.worldgen.WaterwayHeight waterway;
    final TileKey key;
    final ArrayList<Waiter> waiters = new ArrayList<>();
    final DensityFunction height;
    final short[] heights;
    final int[] colors;
    final int minY, maxY, seaLevel;
    final long created = System.nanoTime();
    int next;

    Job(
        TileKey key,
        Waiter waiter,
        DensityFunction height,
        net.minecraft.server.level.ServerLevel level) {
      this.level = level;
      com.shnewbs.deepprimal.worldgen.WaterwayHeight[] found = {null};
      height.mapAll(
          f -> {
            if (f instanceof com.shnewbs.deepprimal.worldgen.WaterwayHeight w && w.routed())
              found[0] = w;
            return f;
          });
      waterway = found[0];
      this.key = key;
      this.height = height;
      minY = level.getMinY();
      maxY = level.getMaxY();
      seaLevel = level.getSeaLevel();
      waiters.add(waiter);
      heights = new short[HorizonGeometry.points(key.detail)];
      colors = new int[heights.length];
    }
  }
}
