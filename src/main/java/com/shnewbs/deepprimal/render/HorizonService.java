package com.shnewbs.deepprimal.render;

import com.shnewbs.deepprimal.core.FarTerrain;
import com.shnewbs.deepprimal.render.network.HorizonRequest;
import com.shnewbs.deepprimal.render.network.HorizonTile;
import com.shnewbs.deepprimal.worldgen.ColumnSurface;
import java.util.ArrayDeque;
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

/** Server-thread density sampling with a global time/work budget. Never requests a chunk. */
public final class HorizonService {
    private static final ArrayDeque<Job> JOBS = new ArrayDeque<>();
    private static final Map<UUID, Long> LAST = new HashMap<>();
    private static final Map<net.minecraft.resources.Identifier, java.util.Optional<DensityFunction>> SOURCES = new HashMap<>();
    private static final com.shnewbs.deepprimal.core.BoundedLru<TileKey, TileData> CACHE = new com.shnewbs.deepprimal.core.BoundedLru<>(512);
    public static long samples, completed;
    public static int cachedTiles() { return CACHE.size(); }
    public static long cacheHits() { return CACHE.hits(); }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToServer(HorizonRequest.TYPE, HorizonRequest.CODEC, (p, c) -> request(p, (ServerPlayer)c.player()));
        registrar.playToClient(HorizonTile.TYPE, HorizonTile.CODEC);
    }
    private static void request(HorizonRequest p, ServerPlayer player) {
        var level = player.level(); long now = level.getGameTime();
        if (!level.dimension().identifier().equals(p.dimension()) || Math.abs((long)p.x()) > 58000 || Math.abs((long)p.z()) > 58000
            || !FarTerrain.inRange(p.x(), p.z(), player.getX(), player.getZ(), FarTerrain.MAX_DISTANCE)
            || JOBS.size() >= 32 || now - LAST.getOrDefault(player.getUUID(), now - 4) < 4
            || JOBS.stream().anyMatch(j -> j.player == player)) return;
        LAST.put(player.getUUID(), now);
        TileKey key = new TileKey(p.dimension(), p.x(), p.z());
        TileData cached = CACHE.get(key);
        if (cached != null) {
            PacketDistributor.sendToPlayer(player, new HorizonTile(p.session(), p.dimension(), p.x(), p.z(), true, cached.heights, cached.colors)); return;
        }
        var source = SOURCES.computeIfAbsent(p.dimension(), dimension -> {
            ColumnSurface[] found = new ColumnSurface[1];
            level.getChunkSource().randomState().router().preliminarySurfaceLevel()
                .mapAll(f -> { if (f instanceof ColumnSurface column) found[0] = column; return f; });
            return found[0] == null ? java.util.Optional.empty() : java.util.Optional.of(found[0].height());
        });
        if (source.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new HorizonTile(p.session(), p.dimension(), p.x(), p.z(), false, new short[0], new int[0])); return;
        }
        JOBS.add(new Job(player, p, source.get()));
    }
    @SubscribeEvent public void tick(ServerTickEvent.Post event) {
        long end = System.nanoTime() + 1_000_000; int work = 0;
        while (!JOBS.isEmpty() && work < 64 && System.nanoTime() < end) {
            Job job = JOBS.peek();
            if (job.player.hasDisconnected() || !job.player.level().dimension().identifier().equals(job.request.dimension())) { JOBS.remove(); continue; }
            int i = job.next++, x = job.request.x() * FarTerrain.TILE_BLOCKS + i % 9 * FarTerrain.STEP;
            int z = job.request.z() * FarTerrain.TILE_BLOCKS + i / 9 * FarTerrain.STEP;
            double raw = job.height.compute(new DensityFunction.SinglePointContext(x, 0, z));
            int h = (int)Math.clamp(Math.floor(raw), job.player.level().getMinY() + 8, job.player.level().getMaxY() - 16);
            job.heights[i] = (short)Math.max(job.player.level().getSeaLevel(), h);
            job.colors[i] = h < 96 ? 0xff355d80 : h < 100 ? 0xffb9af80 : h > 360 ? 0xffdee2e1 : h > 224 ? 0xff747c6b : 0xff66824b;
            work++; samples++;
            if (job.next == FarTerrain.POINTS) {
                JOBS.remove(); completed++;
                var p = job.request;
                CACHE.put(new TileKey(p.dimension(), p.x(), p.z()), new TileData(job.heights, job.colors));
                PacketDistributor.sendToPlayer(job.player, new HorizonTile(p.session(), p.dimension(), p.x(), p.z(), true, job.heights, job.colors));
            }
        }
        // Offline entries do not accumulate across logins; active jobs own at most 32 player references.
        if (event.getServer().getTickCount() % 200 == 0) LAST.keySet().removeIf(id -> event.getServer().getPlayerList().getPlayer(id) == null);
    }
    @SubscribeEvent public void reload(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) { JOBS.clear(); SOURCES.clear(); CACHE.clear(); }
    }
    @SubscribeEvent public void stopped(ServerStoppedEvent event) { JOBS.clear(); LAST.clear(); SOURCES.clear(); CACHE.clear(); samples = completed = 0; }
    private record TileKey(net.minecraft.resources.Identifier dimension, int x, int z) {}
    private record TileData(short[] heights, int[] colors) {}
    public static int pending() { return JOBS.size(); }
    private static final class Job {
        final ServerPlayer player; final HorizonRequest request; final DensityFunction height;
        final short[] heights = new short[FarTerrain.POINTS]; final int[] colors = new int[FarTerrain.POINTS]; int next;
        Job(ServerPlayer player, HorizonRequest request, DensityFunction height) { this.player = player; this.request = request; this.height = height; }
    }
}
