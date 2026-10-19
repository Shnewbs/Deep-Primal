package com.shnewbs.deepprimal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.shnewbs.deepprimal.core.BoundedLru;
import com.shnewbs.deepprimal.core.TickWindow;
import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.performance.PerformanceConfig;
import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Bounded operator diagnostics. World access and ticket ownership stay on the server thread. */
public final class WorldgenProbe {
  private final TickWindow recentTicks = new TickWindow(256);
  private BoundedLru<SampleKey, Sample> samples = new BoundedLru<>(0);
  private Run run;
  private ExecutorService requests;
  private ExecutorService reports;
  private CompletableFuture<?> draining;
  private long tickStarted;
  private long lastTick;
  private final String version;

  public WorldgenProbe(String version) {
    this.version = version;
  }

  @SubscribeEvent
  public void commands(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("deepprimal")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("info")
                        .executes(
                            c -> {
                              var level = c.getSource().getLevel();
                              reply(
                                  c.getSource(),
                                  "Prototype: y="
                                      + level.getMinY()
                                      + ".."
                                      + level.getMaxY()
                                      + ", sea="
                                      + level.getSeaLevel()
                                      + ", seed="
                                      + level.getSeed()
                                      + ". Native aquifers/vegetation are placeholders; no"
                                      + " renewable groundwater yet.");
                              return 1;
                            }))
                .then(Commands.literal("perf").executes(c -> performance(c.getSource())))
                .then(
                    Commands.literal("sample")
                        .then(
                            Commands.argument(
                                    "x", IntegerArgumentType.integer(-29_000_000, 29_000_000))
                                .then(
                                    Commands.argument(
                                            "z",
                                            IntegerArgumentType.integer(-29_000_000, 29_000_000))
                                        .executes(
                                            c ->
                                                sample(
                                                    c.getSource(),
                                                    IntegerArgumentType.getInteger(c, "x"),
                                                    IntegerArgumentType.getInteger(c, "z"))))))
                .then(
                    Commands.literal("bench")
                        .then(
                            Commands.argument(
                                    "chunkX", IntegerArgumentType.integer(-1_800_000, 1_800_000))
                                .then(
                                    Commands.argument(
                                            "chunkZ",
                                            IntegerArgumentType.integer(-1_800_000, 1_800_000))
                                        .then(
                                            Commands.argument(
                                                    "radius", IntegerArgumentType.integer(0, 8))
                                                .executes(
                                                    c ->
                                                        start(
                                                            c.getSource(),
                                                            IntegerArgumentType.getInteger(
                                                                c, "chunkX"),
                                                            IntegerArgumentType.getInteger(
                                                                c, "chunkZ"),
                                                            IntegerArgumentType.getInteger(
                                                                c, "radius")))))))
                .then(
                    Commands.literal("cancel")
                        .executes(
                            c -> {
                              cancel();
                              reply(
                                  c.getSource(),
                                  "Benchmark stopped; any submitted engine request may finish.");
                              return 1;
                            })));
  }

  private int performance(CommandSourceStack source) {
    var ticks = recentTicks.snapshot();
    reply(
        source,
        String.format(
            Locale.ROOT,
            "PERF ticks=%d median=%.2fms p95=%.2fms max=%.2fms heapMiB=%d loadedChunks=%d"
                + " pendingTasks=%d sampleCache=%d/%d cacheHits=%d cacheMisses=%d benchmark=%s",
            ticks.count(),
            ticks.medianMs(),
            ticks.p95Ms(),
            ticks.maxMs(),
            heap() / 1048576,
            source.getLevel().getChunkSource().getLoadedChunksCount(),
            source.getLevel().getChunkSource().getPendingTasksCount(),
            samples.size(),
            samples.capacity(),
            samples.hits(),
            samples.misses(),
            run == null ? "idle" : run.next + "/" + run.xs.length));
    reply(
        source,
        "RUNTIME trackingCapacity="
            + DistanceRuntime.trackingCapacity()
            + " chunkSelections="
            + RuntimeMetrics.SEND_SELECTIONS.sum()
            + " maxSendBatch="
            + RuntimeMetrics.MAX_SEND_BATCH.get()
            + " candidates="
            + RuntimeMetrics.SEND_CANDIDATES.sum()
            + " selectionMillis="
            + RuntimeMetrics.SEND_NANOS.sum() / 1_000_000
            + " viewColumns="
            + RuntimeMetrics.VIEW_COLUMNS.sum()
            + " viewSectionsMoved="
            + RuntimeMetrics.VIEW_SECTIONS_MOVED.sum());
    return 1;
  }

  private int sample(CommandSourceStack source, int x, int z) {
    ServerLevel level = source.getLevel();
    int capacity = PerformanceConfig.SAMPLE_CACHE_ENTRIES.get();
    if (capacity != samples.capacity()) samples = new BoundedLru<>(capacity);
    SampleKey key = new SampleKey(level.dimension().identifier().toString(), level.getSeed(), x, z);
    Sample sample = samples.get(key);
    if (sample == null) {
      var router = level.getChunkSource().randomState().router();
      var context = new DensityFunction.SinglePointContext(x, 0, z);
      sample =
          new Sample(
              router.preliminarySurfaceLevel().compute(context),
              router.continents().compute(context));
      samples.put(key, sample);
    }
    String message =
        String.format(
            Locale.ROOT,
            "Sampler (%d,%d): preliminary surface=%.1f, continentalness=%.3f, vertical top=%d. No"
                + " chunk generated.",
            x,
            z,
            sample.surface,
            sample.land,
            level.getMaxY());
    reply(source, message);
    if (PerformanceConfig.LOG_SAMPLES.get()) DeepPrimal.LOGGER.info(message);
    return 1;
  }

  private int start(CommandSourceStack source, int x, int z, int radius) {
    if (radius > PerformanceConfig.MAX_BENCH_RADIUS.get()) {
      source.sendFailure(
          Component.literal("Radius exceeds maxBenchmarkRadius in deepprimal-performance.toml."));
      return 0;
    }
    if (run != null || (draining != null && !draining.isDone())) {
      source.sendFailure(
          Component.literal("A benchmark request is active; wait or use /deepprimal cancel."));
      return 0;
    }
    if (requests == null) requests = worker("deepprimal-chunk-request", 1);
    if (reports == null) reports = worker("deepprimal-report-writer", 4);
    run = new Run(source, x, z, radius, version);
    reply(
        source,
        "Benchmark started: "
            + run.xs.length
            + " FULL chunk requests, concurrency=1. Use untouched coordinates for cold terrain.");
    DeepPrimal.LOGGER.info("Benchmark started: {} requests", run.xs.length);
    return 1;
  }

  @SubscribeEvent
  public void beforeTick(ServerTickEvent.Pre event) {
    tickStarted = System.nanoTime();
  }

  @SubscribeEvent
  public void tick(ServerTickEvent.Post event) {
    long now = System.nanoTime();
    lastTick = Math.max(0, now - tickStarted);
    recentTicks.record(lastTick);
    Run current = run;
    if (current == null) return;
    current.ticks.record(lastTick);
    current.peakHeap = Math.max(current.peakHeap, heap());
    try {
      if (current.future != null) {
        if (!current.future.isDone()) {
          if (now - current.requestStarted
              > TimeUnit.SECONDS.toNanos(PerformanceConfig.REQUEST_TIMEOUT_SECONDS.get()))
            throw new IllegalStateException("Chunk request timed out; loading ticket released");
          return;
        }
        if (!current.future.getNow(null).isSuccess())
          throw new IllegalStateException("Engine returned an unavailable chunk");
        current.durations[current.next++] = now - current.requestStarted;
        current.release();
        current.future = null;
      }
      if (current.next == current.xs.length) {
        run = null;
        BenchmarkReport report = current.snapshot(now);
        // No filesystem operations or live world reads occur on the writer thread.
        reports.execute(
            () -> {
              try {
                report.write(Path.of("deepprimal-benchmarks"));
                String summary = report.summary();
                DeepPrimal.LOGGER.info(summary);
                event.getServer().execute(() -> reply(current.source, summary));
              } catch (Exception exception) {
                DeepPrimal.LOGGER.error("Benchmark report write failed", exception);
                event
                    .getServer()
                    .execute(
                        () ->
                            current.source.sendFailure(
                                Component.literal(
                                    "Benchmark completed, but writing its report failed. See the"
                                        + " server log.")));
              }
            });
        return;
      }
      int limit = PerformanceConfig.PAUSE_ABOVE_TICK_MS.get();
      if (limit > 0 && lastTick > TimeUnit.MILLISECONDS.toNanos(limit)) return;
      int x = current.xs[current.next], z = current.zs[current.next];
      current.requestStarted = System.nanoTime();
      current.ticket = new ChunkPos(x, z);
      current
          .level
          .getChunkSource()
          .addTicketWithRadius(DeepPrimal.PROBE_TICKET.get(), current.ticket, 0);
      current.future =
          CompletableFuture.supplyAsync(
                  () -> current.level.getChunkSource().getChunkFuture(x, z, ChunkStatus.FULL, true),
                  requests)
              .thenCompose(f -> f);
    } catch (Exception exception) {
      draining = current.future;
      current.release();
      run = null;
      DeepPrimal.LOGGER.error("Worldgen benchmark failed", exception);
      current.source.sendFailure(Component.literal("Benchmark failed: " + exception.getMessage()));
    }
  }

  private void cancel() {
    if (run != null) {
      draining = run.future;
      run.release();
      run = null;
    }
  }

  @SubscribeEvent
  public void reload(OnDatapackSyncEvent event) {
    samples.clear();
  }

  @SubscribeEvent
  public void stopping(ServerStoppingEvent event) {
    cancel();
  }

  @SubscribeEvent
  public void stopped(ServerStoppedEvent event) {
    cancel();
    samples.clear();
    recentTicks.clear();
    draining = null;
    if (requests != null) requests.shutdownNow();
    if (reports != null) {
      reports.shutdown();
      try {
        if (!reports.awaitTermination(5, TimeUnit.SECONDS)) reports.shutdownNow();
      } catch (InterruptedException interrupted) {
        reports.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
    requests = null;
    reports = null;
  }

  private static ExecutorService worker(String name, int queueSize) {
    var worker =
        new ThreadPoolExecutor(
            1,
            1,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueSize),
            task -> {
              var thread = new Thread(task, name);
              thread.setDaemon(true);
              return thread;
            },
            new ThreadPoolExecutor.AbortPolicy());
    worker.allowCoreThreadTimeOut(true);
    return worker;
  }

  private static long heap() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  private static long gc(boolean time) {
    return ManagementFactory.getGarbageCollectorMXBeans().stream()
        .mapToLong(bean -> Math.max(0, time ? bean.getCollectionTime() : bean.getCollectionCount()))
        .sum();
  }

  private static void reply(CommandSourceStack source, String message) {
    source.sendSuccess(() -> Component.literal(message), false);
  }

  private record SampleKey(String dimension, long seed, int x, int z) {}

  private record Sample(double surface, double land) {}

  private static final class Run {
    final CommandSourceStack source;
    final ServerLevel level;
    final int[] xs, zs;
    final long[] durations;
    final TickWindow ticks = new TickWindow(4096);
    final long started = System.nanoTime(), initialGc = gc(false), initialGcMillis = gc(true);
    long requestStarted, peakHeap = heap();
    int next;
    CompletableFuture<ChunkResult<ChunkAccess>> future;
    ChunkPos ticket;
    final String version;

    Run(CommandSourceStack source, int x, int z, int radius, String version) {
      this.version = version;
      this.source = source;
      level = source.getLevel();
      int count = (2 * radius + 1) * (2 * radius + 1);
      xs = new int[count];
      zs = new int[count];
      durations = new long[count];
      int index = 0;
      for (int dz = -radius; dz <= radius; dz++)
        for (int dx = -radius; dx <= radius; dx++) {
          xs[index] = x + dx;
          zs[index++] = z + dz;
        }
    }

    void release() {
      if (ticket != null) {
        level.getChunkSource().removeTicketWithRadius(DeepPrimal.PROBE_TICKET.get(), ticket, 0);
        ticket = null;
      }
    }

    BenchmarkReport snapshot(long completed) {
      return new BenchmarkReport(
          xs,
          zs,
          durations,
          (completed - started) / 1e9,
          heap(),
          peakHeap,
          level.getSeed(),
          level.dimension().identifier().toString(),
          level.getMinY(),
          level.getMaxY(),
          ticks.snapshot(),
          gc(false) - initialGc,
          gc(true) - initialGcMillis,
          version);
    }
  }
}
