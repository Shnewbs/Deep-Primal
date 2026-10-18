package com.shnewbs.deepprimal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.shnewbs.deepprimal.core.SampleStats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Operator-only diagnostic. One in-flight FULL chunk, bounded area, no future.join on server thread. */
public final class WorldgenProbe {
    private Run run;
    private ExecutorService requests;
    private CompletableFuture<?> draining;

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deepprimal")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("info").executes(c -> {
                var l = c.getSource().getLevel();
                say(c.getSource(), "Prototype: y=" + l.getMinY() + ".." + l.getMaxY()
                    + ", sea=" + l.getSeaLevel() + ", seed=" + l.getSeed()
                    + ". Vanilla aquifers/vegetation are placeholders; no renewable groundwater yet.");
                return 1;
            }))
            .then(Commands.literal("sample")
                .then(Commands.argument("x", IntegerArgumentType.integer(-29_000_000, 29_000_000))
                .then(Commands.argument("z", IntegerArgumentType.integer(-29_000_000, 29_000_000))
                .executes(c -> sample(c.getSource(), IntegerArgumentType.getInteger(c, "x"), IntegerArgumentType.getInteger(c, "z"))))))
            .then(Commands.literal("bench")
                .then(Commands.argument("chunkX", IntegerArgumentType.integer(-1_800_000, 1_800_000))
                .then(Commands.argument("chunkZ", IntegerArgumentType.integer(-1_800_000, 1_800_000))
                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 4))
                .executes(c -> start(c.getSource(), IntegerArgumentType.getInteger(c, "chunkX"),
                    IntegerArgumentType.getInteger(c, "chunkZ"), IntegerArgumentType.getInteger(c, "radius")))))))
            .then(Commands.literal("cancel").executes(c -> {
                // A submitted engine request cannot be safely killed; abandon accounting and submit no more.
                if (run != null) {
                    draining = run.future;
                    run.release();
                }
                run = null;
                say(c.getSource(), "Benchmark stopped; any already-submitted chunk may finish.");
                return 1;
            })));
    }

    private int sample(CommandSourceStack source, int x, int z) {
        ServerLevel level = source.getLevel();
        var router = level.getChunkSource().randomState().router();
        // Cheap sampler queries only: do not generate/load the target chunk.
        int top = level.getMaxY();
        double surface = router.preliminarySurfaceLevel().compute(new DensityFunction.SinglePointContext(x, 0, z));
        double land = router.continents().compute(new DensityFunction.SinglePointContext(x, 0, z));
        say(source, String.format(Locale.ROOT,
            "Sampler (%d,%d): preliminary surface=%.1f, continentalness=%.3f, vertical top=%d. No chunk generated.", x, z, surface, land, top));
        return 1;
    }

    private int start(CommandSourceStack source, int x, int z, int radius) {
        if (run != null || (draining != null && !draining.isDone())) {
            source.sendFailure(Component.literal("A benchmark is already running; use /deepprimal cancel."));
            return 0;
        }
        if (requests == null || requests.isShutdown()) {
            requests = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "deepprimal-chunk-request");
                thread.setDaemon(true);
                return thread;
            });
        }
        run = new Run(source, x, z, radius);
        say(source, "Benchmark started: " + run.coords.size()
            + " FULL chunk requests, concurrency=1. Includes existing chunks; use untouched coordinates for cold terrain.");
        return 1;
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (run == null) return;
        Run r = run;
        try {
            if (r.future != null) {
                if (!r.future.isDone()) return;
                if (!r.future.getNow(null).isSuccess()) throw new IllegalStateException("Engine returned an unavailable chunk");
                r.samples.add(System.nanoTime() - r.requestStarted);
                r.release();
                r.future = null;
                r.next++;
            }
            if (r.next == r.coords.size()) {
                run = null;
                r.finish();
                return;
            }
            int[] p = r.coords.get(r.next);
            r.requestStarted = System.nanoTime();
            r.activeTicket = new ChunkPos(p[0], p[1]);
            r.level.getChunkSource().addTicketWithRadius(DeepPrimal.PROBE_TICKET.get(), r.activeTicket, 0);
            // This engine API managed-blocks when invoked on its main thread. Its off-thread
            // branch dispatches safely to the engine's main-thread processor without waiting there.
            r.future = CompletableFuture.supplyAsync(() ->
                r.level.getChunkSource().getChunkFuture(p[0], p[1], ChunkStatus.FULL, true), requests)
                .thenCompose(future -> future);
        } catch (Exception ex) {
            r.release();
            run = null;
            DeepPrimal.LOGGER.error("Worldgen benchmark failed", ex);
            r.source.sendFailure(Component.literal("Benchmark failed: " + ex.getMessage()));
        }
    }

    @SubscribeEvent
    public void stopped(ServerStoppedEvent event) {
        if (run != null) run.release();
        run = null;
        draining = null;
        if (requests != null) requests.shutdownNow();
        requests = null;
    }

    private static void say(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
        DeepPrimal.LOGGER.info(message);
    }

    private static final class Run {
        final CommandSourceStack source;
        final ServerLevel level;
        final List<int[]> coords = new ArrayList<>();
        final List<Long> samples = new ArrayList<>();
        final long started = System.nanoTime();
        long requestStarted;
        int next;
        CompletableFuture<ChunkResult<ChunkAccess>> future;
        ChunkPos activeTicket;

        void release() {
            if (activeTicket != null) {
                level.getChunkSource().removeTicketWithRadius(DeepPrimal.PROBE_TICKET.get(), activeTicket, 0);
                activeTicket = null;
            }
        }

        Run(CommandSourceStack source, int x, int z, int radius) {
            this.source = source;
            level = source.getLevel();
            for (int dz = -radius; dz <= radius; dz++)
                for (int dx = -radius; dx <= radius; dx++) coords.add(new int[]{x + dx, z + dz});
        }

        void finish() throws Exception {
            SampleStats stats = SampleStats.of(samples.stream().mapToLong(Long::longValue).toArray());
            double seconds = (System.nanoTime() - started) / 1e9;
            long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            String summary = String.format(Locale.ROOT,
                "BENCH COMPLETE count=%d median=%.2fms p95=%.2fms p99=%.2fms max=%.2fms elapsed=%.2fs throughput=%.2f/s heapEndMiB=%d seed=%d dimension=%s",
                stats.count(), stats.medianMs(), stats.p95Ms(), stats.p99Ms(), stats.maxMs(), seconds,
                stats.count() / seconds, heap / 1048576, level.getSeed(), level.dimension().identifier());
            Path dir = Path.of("deepprimal-benchmarks");
            Files.createDirectories(dir);
            String stamp = Long.toString(System.currentTimeMillis());
            StringBuilder csv = new StringBuilder("chunk_x,chunk_z,request_to_ready_ms\n");
            for (int i = 0; i < samples.size(); i++) csv.append(coords.get(i)[0]).append(',')
                .append(coords.get(i)[1]).append(',').append(samples.get(i) / 1e6).append('\n');
            Files.writeString(dir.resolve(stamp + ".csv"), csv.toString());
            Files.writeString(dir.resolve(stamp + ".txt"), summary + "\n"
                + "Observational request latency includes tick polling, queueing, neighbor generation and disk I/O; not pure generator CPU.\n"
                + "Java=" + System.getProperty("java.version") + " processors=" + Runtime.getRuntime().availableProcessors()
                + " minY=" + level.getMinY() + " maxY=" + level.getMaxY() + " concurrency=1\n");
            say(source, summary);
        }
    }
}
