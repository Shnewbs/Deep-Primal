package com.shnewbs.deepprimal.lod.client;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.shnewbs.deepprimal.core.lod.*;
import com.shnewbs.deepprimal.core.RollingTiming;
import com.shnewbs.deepprimal.graphics.GpuLighting;
import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Explored-chunk LOD: immutable copies, worker reduction/storage, render-thread GPU ownership. */
public final class VoxelLodRenderer {
  private static final int MAX_DISK_KEYS = 8192;
  // Opt-in developer comparison; default zero preserves the existing GPU parts and draw count.
  private static final int EXPERIMENTAL_PART_UPLOAD_KIB =
      Math.max(0, Math.min(256,
          Integer.getInteger("deepprimal.lod.experimentalPartUploadKiB", 0)));
  public static final boolean TELEMETRY = Boolean.getBoolean("deepprimal.lod.telemetry");
  public final RollingTiming cullTiming = new RollingTiming(240);
  public final RollingTiming uploadTiming = new RollingTiming(240);
  private final LodGpuUploadProbe gpuPartProbe = new LodGpuUploadProbe(240);
  private final LodPageUploadProbe pageUploadProbe = new LodPageUploadProbe(240);
  // Opt-in CPU stage metrics: never inferred to be GPU/driver frame completion.
  private final LodSubmissionProbe submissionProbe = new LodSubmissionProbe(240);
  public final RollingTiming submitTiming = new RollingTiming(240);
  public volatile long lastUploadBytes, pendingMeshBudgetBytes;
  public volatile int testedParts, rejectedParts, submittedParts, submittedApiBatches;
  public static final VoxelLodRenderer INSTANCE = new VoxelLodRenderer();
  private static final ContextKey<List<TerrainGpuMesh>> VISIBLE =
      new ContextKey<>(Identifier.fromNamespaceAndPath("deepprimal", "voxel_lod"));
  private static final RenderPipeline PIPELINE =
      RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "voxel_lod"))
          .withVertexShader(Identifier.fromNamespaceAndPath("deepprimal_graphics", "core/terrain"))
          .withFragmentShader(Identifier.fromNamespaceAndPath("deepprimal_lod", "core/voxel_lod"))
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.TRIANGLES)
          .withDepthStencilState(DepthStencilState.DEFAULT)
          .withSampler("NativeLight")
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withCull(false)
          .build();

  private record Entry(
      long revision, List<List<TerrainGpuMesh>> levels, long bytes, int[] selected, int top,
      boolean persisted)
      implements AutoCloseable {
    public void close() {
      closeGpuMeshes(levels);
    }
  }

  private record Ready(
      long session, long key, long revision, LodMesher.Levels levels, boolean captured,
      boolean persisted) {}

  // Reused only within a synchronous render pass: at most 4,096 draw references
  // per API submission, independent of the number of visible LOD mesh parts.
  private final BoundedIndexedDrawBatches<com.mojang.blaze3d.systems.RenderPass.Draw<Void>>
      indexedDrawBatches = new BoundedIndexedDrawBatches<>(4096);
  // Recycle only the transient extraction collector; render-state snapshots remain immutable.
  private final LodVisibleSnapshot<TerrainGpuMesh> visibleSnapshots = new LodVisibleSnapshot<>();
  private final LinkedHashMap<Long, Entry> cache = new LinkedHashMap<>(256, .75f, true);
  // One bounded primitive victim selector per active world; no arrays allocated per eviction.
  private LodEvictionHeap evictionHeap;
  private final LinkedHashMap<Long, Long> dirty = new LinkedHashMap<>();
  private final HashMap<Long, Long> latest = new HashMap<>();
  private final LinkedHashSet<Long> disk = new LinkedHashSet<>();
  private record DeferredView(long cameraChunk, int memoryMiB, long bytes) {}
  private final HashMap<Long, DeferredView> deferredDisk = new HashMap<>();
  private ThreadPoolExecutor worker;
  private Future<Ready> future;
  private Long workingKey;
  private volatile boolean persistenceFailed;
  private PendingGpu pendingGpu;
  private int ticks, captureBurst;
  private DeliveredChunkScan deliveredScan;
  public volatile int discoveryVisited, discoveryTotal;
  public volatile boolean discoveryComplete;
  private final Matrix4f drawMatrix = new Matrix4f(), textureIdentity = new Matrix4f();
  private final Vector4f white = new Vector4f(1, 1, 1, 1);
  // Bound retained CPU transform instances as well as count variants; a huge
  // transient visible set still renders but cannot pin unbounded Java objects.
  private final ReusableTransformArrays<net.minecraft.client.renderer.DynamicUniforms.Transform>
      transformArrays = new ReusableTransformArrays<>(4, 32768);
  private final java.util.function.IntFunction<
      net.minecraft.client.renderer.DynamicUniforms.Transform[]> transformFactory =
      length -> {
        var array = new net.minecraft.client.renderer.DynamicUniforms.Transform[length];
        for (int i = 0; i < length; i++)
          array[i] =
              new net.minecraft.client.renderer.DynamicUniforms.Transform(
                  drawMatrix, white, new Vector3f(), textureIdentity);
        return array;
      };

  // Sampled by LodClient diagnostics on the client render thread.
  ReusableTransformArrays.Snapshot transformStats() {
    return transformArrays.snapshot();
  }

  LodGpuUploadProbe.Snapshot uploadPartStats() {
    return gpuPartProbe.snapshot();
  }

  LodPageUploadProbe.Snapshot uploadPageStats() {
    return pageUploadProbe.snapshot();
  }

  LodSubmissionProbe.Snapshot submissionStageStats() {
    return submissionProbe.snapshot();
  }

  private static final class PendingGpu {
    final Ready ready;
    final long admittedAtNanos;
    int uploadFrames;
    final List<List<TerrainGpuMesh>> levels = new ArrayList<>();
    // Accepted buffers never change their allocated size: use a committed, constant-time
    // total rather than walking every previously uploaded part on each render frame.
    final PendingGpuBytes accepted = new PendingGpuBytes();
    int detail, part;

    PendingGpu(Ready ready) {
      this.ready = ready;
      this.admittedAtNanos = TELEMETRY ? System.nanoTime() : 0;
      for (int i = 0; i < 3; i++) levels.add(new ArrayList<>());
    }

    long bytes() {
      return accepted.bytes();
    }

    void addGpu(int detail, TerrainGpuMesh gpu) {
      accepted.append(levels.get(detail), gpu, gpu.byteSize);
    }

    void close() {
      closeGpuMeshes(levels);
    }
  }

  /** Close every owned GPU buffer, even when one teardown operation fails. */
  private static void closeGpuMeshes(List<List<TerrainGpuMesh>> levels) {
    Throwable first = null;
    for (var meshes : levels) {
      for (var mesh : meshes) {
        try {
          mesh.close();
        } catch (RuntimeException | Error failure) {
          if (first == null) first = failure;
          else if (failure != first) first.addSuppressed(failure);
        }
      }
    }
    if (first instanceof Error error) throw error;
    if (first instanceof RuntimeException failure) throw failure;
  }

  private final LodDiskIndexService diskIndex = new LodDiskIndexService();
  private ClientLevel level;
  private String worldId;
  private LodStore store;
  private Path root;
  private long session, revision = System.currentTimeMillis() << 16;
  public volatile long capturedChunks,
      uploadedChunks,
      diskReads,
      residentBytes,
      drawnTriangles,
      staleResults;
  public volatile int visibleChunks, queuedChunks, visibleSpan;
  // Successful first-part uploads above the soft per-frame byte cap.
  public volatile long oversizedFirstUploads, oversizedFirstUploadBytes;
  public volatile String status = "Waiting for world";

  public void worldIdentity(String id) {
    if (!id.equals(worldId)) {
      worldId = id;
      reset();
    }
  }

  public static boolean active() {
    return RendererOwnership.owned() && LodConfig.ENABLED.get() && INSTANCE.level != null;
  }

  public long snapshotRevision(int x, int z) {
    Entry e = cache.get(key(x, z));
    return e == null ? -1 : e.revision;
  }

  public static int visibleDistance() {
    return active() ? INSTANCE.visibleSpan : 0;
  }

  public static int distance() {
    return active() ? LodConfig.RADIUS.get() : 0;
  }

  public static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static int x(long k) {
    return (int) (k >> 32);
  }

  private static int z(long k) {
    return (int) k;
  }

  public void dirty(int x, int z) {
    if (!RendererOwnership.owned() || !LodConfig.ENABLED.get()) return;
    long k = key(x, z), r = ++revision;
    dirty.put(k, r);
    latest.put(k, r);
    if (dirty.size() > 4096) {
      long first = dirty.keySet().iterator().next();
      dirty.remove(first);
      forgetUnused(first);
    }
  }

  @SubscribeEvent
  public void loaded(ChunkEvent.Load e) {
    if (e.getLevel() instanceof ClientLevel l) {
      var p = e.getChunk().getPos();
      Minecraft.getInstance()
          .execute(
              () -> {
                if (l == Minecraft.getInstance().level) dirty(p.x(), p.z());
              });
    }
  }

  private void attach(ClientLevel next) {
    reset();
    level = next;
    if (next == null) return;
    worker =
        new ThreadPoolExecutor(
            1,
            1,
            20,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2),
            r -> {
              Thread t = new Thread(r, "Deep Primal voxel LOD");
              t.setDaemon(true);
              t.setPriority(Thread.MIN_PRIORITY);
              return t;
            });
    if (worldId != null) {
      try {
        var mc = Minecraft.getInstance();
        String endpoint =
            mc.getSingleplayerServer() != null
                ? mc.getSingleplayerServer()
                    .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .toAbsolutePath()
                    .normalize()
                    .toString()
                : mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "local-session";
        String token =
            HexFormat.of()
                .formatHex(
                    java.security.MessageDigest.getInstance("SHA-256")
                        .digest(
                            (worldId + "|" + endpoint + "|" + next.dimension().identifier())
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .substring(0, 32);
        root = Minecraft.getInstance().gameDirectory.toPath().resolve("deepprimal-lod/v3/" + token);
        store = new LodStore(root);
      } catch (java.security.NoSuchAlgorithmException e) {
        throw new AssertionError(e);
      }
    }
    // Already-delivered chunks are discovered incrementally on the owning client thread.

  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post e) {
    var mc = Minecraft.getInstance();
    if (!RendererOwnership.owned() || !LodConfig.ENABLED.get()) {
      if (level != null || worker != null) reset();
      if (mc.level == null) worldId = null;
      status = "Disabled / delegated";
      return;
    }
    if (mc.level != level) attach(mc.level);
    if (level == null) return;
    if (worker == null) attach(mc.level);
    if (mc.player == null) return;
    var position = mc.player.chunkPosition();
    // Snapshot the camera on the client thread: the filesystem worker must not
    // read mutable player state and must not choose files around world origin.
    if (diskIndex.tick(root, position.x(), position.z(), disk,
        LodConfig.PERSIST.get() && store != null,
        k -> { deferredDisk.remove(k); forgetUnused(k); }))
      status = "Cache index unavailable";
    int radius = Math.min(com.shnewbs.deepprimal.core.DistancePolicy.MAX_RENDER,
        mc.options.getEffectiveRenderDistance());
    // On effective-radius changes, completed scans or substantial travel, rescan the new
    // native footprint while preserving the existing bounded per-tick admission below.
    if (deliveredScan == null || deliveredScan.shouldRetarget(position.x(), position.z(), radius))
      deliveredScan = new DeliveredChunkScan(position.x(), position.z(), radius);
    int discoveryBudget = ClientPerformanceConfig.PAUSE_BACKGROUND.get() && !mc.isWindowActive()
        ? 0 : 128;
    deliveredScan.drain(discoveryBudget, (x, z) -> {
      // Discovery must not displace live edits or flood the existing bounded dirty queue.
      long k = key(x, z);
      if (dirty.containsKey(k)) return true;
      if (dirty.size() >= 512) return false;
      // Native rediscovery must supersede disk work, including in-flight results/uploads.
      if (level.hasChunk(x, z)) dirty(x, z);
      return true;
    });
    discoveryVisited = deliveredScan.visited();
    discoveryTotal = deliveredScan.total();
    discoveryComplete = deliveredScan.done();
    queuedChunks = dirty.size();
    if (future != null
        || pendingGpu != null
        || ++ticks % (int) Math.ceil(LodConfig.CAPTURES.get() / ClientWorkPacer.fraction()) != 0)
      return;
    if (ClientPerformanceConfig.PAUSE_BACKGROUND.get() && !mc.isWindowActive()) return;
    if (captureBurst >= 3 && startDiskJob()) return;
    long start = System.nanoTime();
    int count = 0;
    while (!dirty.isEmpty() && count++ < 1 && System.nanoTime() - start < 2_000_000) {
      // Camera-near edits must not wait behind an entire distant dirty queue.
      // One of eight successful captures remains oldest-first for starvation freedom.
      long k = LodCapturePriority.choose(
          dirty, position.x(), position.z(), (capturedChunks & 7L) == 7L);
      long rev = dirty.remove(k);
      var chunk = level.getChunkSource().getChunk(x(k), z(k), ChunkStatus.FULL, false);
      if (chunk == null || chunk instanceof net.minecraft.world.level.chunk.EmptyLevelChunk) {
        if (!cache.containsKey(k)) latest.remove(k);
        continue;
      }
      NativeLodCapture copy = new NativeLodCapture(level, chunk, rev);
      long epoch = session;
      LodStore writer = store;
      long bytes = LodConfig.DISK.get() * 1048576L;
      boolean persist = LodConfig.PERSIST.get() && !persistenceFailed,
          pruneNow = capturedChunks % 64 == 0;
      Path folder = root;
      workingKey = k;
      future =
          worker.submit(
              () -> {
                ColumnarChunk page = copy.build();
                boolean persisted = false;
                if (persist && writer != null) {
                  try {
                    writer.write(page);
                    persisted = true;
                    if (pruneNow) prune(folder, bytes);
                  } catch (java.io.IOException failure) {
                    persistenceFailed = true;
                    com.shnewbs.deepprimal.lod.LodModule.LOGGER.warn(
                        "LOD persistence disabled for this session; rendering continues", failure);
                  }
                }
                return new Ready(epoch, k, rev,
                    LodMeshPartSplitter.split(LodMesher.build(page), EXPERIMENTAL_PART_UPLOAD_KIB),
                    true, persisted);
              });
      capturedChunks++;
      captureBurst++;
      status = "Capturing real chunks";
      return;
    }
    if (startDiskJob()) return;
    status = "Ready — explored chunks";
  }

  /** Keep cache restoration moving even when flowing water continuously dirties native chunks. */
  private boolean startDiskJob() {
    if (!LodConfig.PERSIST.get()) {
      // Release queued edit guards with the same per-call key bound as disk selection.
      for (int remaining = Math.min(64, disk.size()); remaining > 0; remaining--) {
        long k = disk.removeFirst();
        deferredDisk.remove(k);
        forgetUnused(k);
      }
      return false;
    }
    // A later camera move can bring an indexed page into range. Keep those keys,
    // but inspect each at most once per call and retain the existing 64-key bound.
    int remaining = Math.min(64, disk.size());
    while (!disk.isEmpty() && remaining-- > 0) {
      long k = disk.removeFirst();
      Entry displayed = cache.get(k);
      if ((displayed != null
              && latest.getOrDefault(k, displayed.revision) <= displayed.revision)
          || dirty.containsKey(k)) {
        deferredDisk.remove(k);
        continue;
      }
      var deferred = deferredDisk.get(k);
      if (deferred != null) {
        var player = Minecraft.getInstance().player;
        if (player == null
            || (deferred.cameraChunk == key(player.chunkPosition().x(), player.chunkPosition().z())
                && deferred.memoryMiB == LodConfig.MEMORY.get()
                && (deferred.bytes > LodConfig.MEMORY.get() * 1048576L - residentBytes
                    || cache.size() >= 4096))) {
          disk.addLast(k);
          diskIndex.recoveryQueued(k);
          continue;
        }
        deferredDisk.remove(k);
      }
      if (!inRange(k)) {
        disk.addLast(k);
        diskIndex.recoveryQueued(k);
        continue;
      }
      LodStore reader = store;
      if (reader == null) break;
      long epoch = session;
      workingKey = k;
      future =
          worker.submit(
              () -> {
                ColumnarChunk page = reader.read(x(k), z(k));
                return new Ready(epoch, k, page.revision,
                    LodMeshPartSplitter.split(LodMesher.build(page), EXPERIMENTAL_PART_UPLOAD_KIB),
                    false, true);
              });
      diskReads++;
      status = "Loading persistent terrain";
      captureBurst = 0;
      return true;
    }
    return false;
  }

  private boolean inRange(long k) {
    var p = Minecraft.getInstance().player;
    if (p == null) return false;
    return LodChunkDistance.chebyshev(
        x(k), z(k), p.chunkPosition().x(), p.chunkPosition().z()) <= LodConfig.RADIUS.get();
  }

  private static void prune(Path folder, long budget) throws java.io.IOException {
    if (folder == null) return;
    var files = new ArrayList<Path>();
    long bytes = 0;
    try (var stream = Files.newDirectoryStream(folder, "*.plod")) {
      for (Path p : stream) {
        files.add(p);
        bytes += Files.size(p);
      }
    }
    if (bytes <= budget) return;
    files.sort(
        Comparator.comparingLong(
            p -> {
              try {
                return Files.getLastModifiedTime(p).toMillis();
              } catch (java.io.IOException e) {
                return 0;
              }
            }));
    for (Path p : files) {
      if (bytes <= budget) break;
      long size = Files.size(p);
      Files.deleteIfExists(p);
      bytes -= size;
    }
  }

  @SubscribeEvent
  public void extract(ExtractLevelRenderStateEvent event) {
    if (!active()) return;
    if (future != null && future.isDone()) {
      try {
        Ready r = future.get();
        if (obsolete(r)) {
          staleResults++;
        } else if (r.levels.bytes() <= LodConfig.MEMORY.get() * 1048576L) {
          long limit = LodConfig.MEMORY.get() * 1048576L;
          evictToFit(r.levels.bytes(), 4095, r.key, event.getCamera().position());
          if (residentBytes + r.levels.bytes() <= limit) pendingGpu = new PendingGpu(r);
          else deferPersisted(r);
        } else {
          deferPersisted(r);
        }
      } catch (InterruptedException failure) {
        Thread.currentThread().interrupt();
      } catch (ExecutionException | CancellationException failure) {
        status = "LOD job failed; terrain kept";
        com.shnewbs.deepprimal.lod.LodModule.LOGGER.warn("LOD job failed", failure);
      } finally {
        future = null;
        Long completed = workingKey;
        workingKey = null;
        if (completed != null) forgetUnused(completed);
      }
    }
    processUploads();
    evictToFit(0, 4096, null, event.getCamera().position());
    long cullStarted = TELEMETRY ? System.nanoTime() : 0;
    int tested = 0, rejected = 0;
    var camera = event.getCamera().position();
    var visible = visibleSnapshots.begin();
    try {
    int chunks = 0, span = 0;
    int cx = Math.floorDiv((int) Math.floor(camera.x), 16),
        cz = Math.floorDiv((int) Math.floor(camera.z), 16),
        near = Minecraft.getInstance().options.getEffectiveRenderDistance();
    double focal =
        Minecraft.getInstance().getWindow().getHeight()
            / (2 * Math.tan(Math.toRadians(Minecraft.getInstance().options.fov().get()) / 2));
    for (var item : cache.entrySet()) {
      long k = item.getKey();
      long ringDistance = LodChunkDistance.chebyshev(x(k), z(k), cx, cz);
      if (ringDistance > LodConfig.RADIUS.get()) continue;
      // Keep coverage until the native section at the page's top is compiled and visible.
      Entry entry = item.getValue();
      var finest = entry.levels.get(0);
      if (finest.isEmpty()) continue;
      int top = entry.top;
      if (ringDistance <= near
          && event
              .getLevelRenderer()
              .isSectionCompiledAndVisible(
                  new net.minecraft.core.BlockPos(x(k) * 16 + 8, top - 1, z(k) * 16 + 8))) continue;
      double dist = Math.max(1, Math.hypot(x(k) * 16. + 8 - camera.x, z(k) * 16. + 8 - camera.z));
      int d = entry.selected[0];
      while (d < 2 && (1 << (d + 1)) * focal / dist < LodConfig.ERROR.get() * .85) d++;
      while (d > 0 && (1 << d) * focal / dist > LodConfig.ERROR.get() * 1.15) d--;
      entry.selected[0] = d;
      boolean seen = false;
      for (TerrainGpuMesh part : entry.levels.get(d)) {
        if (TELEMETRY) tested++;
        if (event.getFrustum().isVisible(part.bounds)) {
          visible.add(part);
          seen = true;
        } else if (TELEMETRY) rejected++;
      }
      if (seen) {
        chunks++;
        span = Math.max(span, (int) ringDistance + 1);
      }
    }
    if (TELEMETRY) {
      testedParts = tested;
      rejectedParts = rejected;
      // Prospective GPU budget, not the larger Java-array heap footprint.
      pendingMeshBudgetBytes = pendingGpu == null ? 0 : pendingGpu.ready.levels.bytes();
      cullTiming.record(System.nanoTime() - cullStarted);
    }
    visibleChunks = chunks;
    visibleSpan = Math.min(LodConfig.RADIUS.get(), span);
    event.getRenderState().setRenderData(VISIBLE, visibleSnapshots.snapshot());
    } finally {
      // Render state owns a detached immutable copy. Never pin meshes after an aborted extract.
      visibleSnapshots.finish();
    }
  }

  private boolean obsolete(Ready r) {
    if (r.session != session) return true;
    Entry displayed = cache.get(r.key);
    if (displayed != null && displayed.revision > r.revision) return true;
    // A coherent live snapshot may finish while another edit is queued. Discarding it on
    // every fluid tick would prevent any image from completing. The newer dirty revision
    // stays queued. Disk data must still lose to edits already observed in this session.
    return !r.captured && latest.getOrDefault(r.key, r.revision) != r.revision;
  }

  private void forgetUnused(long k) {
    if (!cache.containsKey(k)
        && !disk.contains(k)
        && !dirty.containsKey(k)
        && (workingKey == null || workingKey != k)
        && (pendingGpu == null || pendingGpu.ready.key != k)) latest.remove(k);
  }

  private void evictToFit(
      long additional, int entries, Long keep, net.minecraft.world.phys.Vec3 camera) {
    long limit = LodConfig.MEMORY.get() * 1048576L;
    if (residentBytes + additional <= limit && cache.size() <= entries) return;
    // Build the stable farthest-first victim heap in O(resident pages), then
    // remove only as many candidates as the current byte/entry limits require.
    // Preserve keep-last, equal-distance access order and all cleanup semantics.
    if (evictionHeap == null) evictionHeap = new LodEvictionHeap(4096);
    evictionHeap.reset(cache.keySet(), keep, camera.x, camera.z);
    // Continue all required evictions and persisted recovery even if releasing one
    // superseded GPU allocation fails. Surface the first failure after bookkeeping.
    LodGpuResetCleanup closeFailures = new LodGpuResetCleanup();
    while (evictionHeap.hasNext()) {
      long k = evictionHeap.next();
      if (residentBytes + additional <= limit && cache.size() <= entries) break;
      Entry old = cache.remove(k);
      residentBytes -= old.bytes;
      closeFailures.release(old::close);
      // Revisit only a persisted revision that no observed edit has superseded.
      // Ordered uniqueness prevents duplicate restores; retain the index memory bound.
      if (old.persisted && store != null && LodConfig.PERSIST.get()
          && !dirty.containsKey(k) && latest.getOrDefault(k, old.revision) == old.revision
          && disk.size() < MAX_DISK_KEYS) {
        disk.add(k);
        diskIndex.recoveryQueued(k);
        // Avoid a fixed-view eviction/read/upload loop. Reconsider after movement or rebudgeting.
        var player = Minecraft.getInstance().player;
        long view = player != null ? key(player.chunkPosition().x(), player.chunkPosition().z())
            : key(Math.floorDiv((int) Math.floor(camera.x), 16),
                Math.floorDiv((int) Math.floor(camera.z), 16));
        deferredDisk.put(k, new DeferredView(view, LodConfig.MEMORY.get(), old.bytes));
      }
      forgetUnused(k);
    }
    closeFailures.throwIfFailed();
  }

  private void processUploads() {
    lastUploadBytes = 0;
    if (pendingGpu == null) return;
    long started = TELEMETRY ? System.nanoTime() : 0;
    try {
      processUploadWork();
    } catch (RuntimeException | Error failure) {
      // Do not retry a partially uploaded page after a failed device operation.
      // Cache ownership was already transferred if a page was fully committed.
      PendingGpu aborted = pendingGpu;
      pendingGpu = null;
      if (aborted != null) {
        residentBytes = LodGpuUploadFailure.unwind(residentBytes, aborted.bytes(),
            aborted::close, failure);
        forgetUnused(aborted.ready.key);
        status = "LOD GPU upload failed; terrain kept";
      }
      throw failure;
    } finally {
      if (TELEMETRY) uploadTiming.record(System.nanoTime() - started);
    }
  }

  private void processUploadWork() {
    PendingGpu p = pendingGpu;
    if (p == null) return;
    if (TELEMETRY && p.uploadFrames < Integer.MAX_VALUE) p.uploadFrames++;
    if (p.ready.levels.bytes() + residentBytes - p.bytes() > LodConfig.MEMORY.get() * 1048576L) {
      if (TELEMETRY) pageUploadProbe.dropped(LodPageUploadProbe.Drop.MEMORY);
      deferPersisted(p.ready);
      residentBytes -= p.bytes();
      pendingGpu = null;
      try { p.close(); }
      finally { forgetUnused(p.ready.key); }
      return;
    }
    if (obsolete(p.ready)) {
      if (TELEMETRY) pageUploadProbe.dropped(LodPageUploadProbe.Drop.STALE);
      residentBytes -= p.bytes();
      pendingGpu = null;
      try { p.close(); }
      finally { forgetUnused(p.ready.key); }
      staleResults++;
      return;
    }
    if (ClientPerformanceConfig.PAUSE_BACKGROUND.get() && !Minecraft.getInstance().isWindowActive())
      return;
    long used = 0,
        start = System.nanoTime(),
        limit = ClientPerformanceConfig.UPLOAD_KIB.get() * 1024L;
    int uploaded = 0;
    int maxUploads = Math.max(1, (int) (LodConfig.UPLOADS.get() * ClientWorkPacer.fraction()));
    while (p.detail < 3) {
      var meshes = p.ready.levels.level(p.detail);
      if (p.part == meshes.size()) {
        p.detail++;
        p.part = 0;
        continue;
      }
      var mesh = meshes.get(p.part);
      long partBytes = mesh.gpuBytes();
      boolean expired = System.nanoTime() - start
          > ClientPerformanceConfig.UPLOAD_MICROS.get() * 1000L;
      if (!UploadWorkBudget.mayStart(uploaded, maxUploads, used, partBytes, limit, expired))
        break;
      boolean oversizedFirst = UploadWorkBudget.oversizedFirst(uploaded, partBytes, limit);
      // Optional CPU-side upload probe: measures packing/device submission, not GPU completion.
      long partStarted = TELEMETRY ? System.nanoTime() : 0;
      TerrainGpuMesh gpu;
      try {
        gpu = TerrainGpuMesh.upload(x(p.ready.key), z(p.ready.key), p.detail, mesh);
      } catch (RuntimeException | Error error) {
        if (TELEMETRY) gpuPartProbe.failed(System.nanoTime() - partStarted);
        throw error;
      }
      try {
        gpu.bounds =
            new AABB(
                x(p.ready.key) * 16.,
                mesh.minimum(),
                z(p.ready.key) * 16.,
                x(p.ready.key) * 16. + 16,
                mesh.maximum() + 1,
                z(p.ready.key) * 16. + 16);
        p.addGpu(p.detail, gpu);
      } catch (RuntimeException | Error error) {
        // The newly allocated buffers have not entered the accepted residency ledger.
        // Do not leak them when bounds/admission fails after successful GPU allocation.
        try {
          gpu.close();
        } catch (RuntimeException | Error closeFailure) {
          error.addSuppressed(closeFailure);
        }
        if (TELEMETRY) gpuPartProbe.failed(System.nanoTime() - partStarted);
        throw error;
      }
      // Both ledgers must advance before any fallible diagnostic callback.
      residentBytes += gpu.byteSize;
      if (TELEMETRY)
        gpuPartProbe.accepted(System.nanoTime() - partStarted, gpu.byteSize,
            ClientPerformanceConfig.UPLOAD_MICROS.get() * 1000L);
      p.part++;
      uploaded++;
      used += gpu.byteSize;
      if (oversizedFirst) {
        oversizedFirstUploads++;
        oversizedFirstUploadBytes += gpu.byteSize;
      }
      if (TELEMETRY) lastUploadBytes = used;
    }
    if (p.detail == 3) {
      Entry replacement =
          new Entry(
              p.ready.revision,
              p.levels.stream().map(List::copyOf).toList(),
              p.bytes(),
              new int[] {0},
              p.ready.levels.fine().stream().mapToInt(m -> m.maximum()).max().orElse(0),
              p.ready.persisted);
      Entry old = cache.put(p.ready.key, replacement);
      // Transfer ownership before closing a superseded page: an old close
      // failure must never trigger rollback of the newly cached GPU buffers.
      pendingGpu = null;
      if (old != null) {
        residentBytes -= old.bytes;
        old.close();
      }
      uploadedChunks++;
      // Count only a page that actually entered the cache, after replacement cleanup.
      if (TELEMETRY)
        pageUploadProbe.committed(System.nanoTime() - p.admittedAtNanos,
            p.uploadFrames, p.bytes());
    }
  }

  /** Keep completed disk-backed work recoverable when a live budget change rejects its upload. */
  private void deferPersisted(Ready r) {
    if (r.session != session || !r.persisted || store == null || !LodConfig.PERSIST.get()
        || dirty.containsKey(r.key) || latest.getOrDefault(r.key, r.revision) != r.revision
        || (!disk.contains(r.key) && disk.size() >= MAX_DISK_KEYS)) return;
    Entry displayed = cache.get(r.key);
    if (displayed != null && displayed.revision >= r.revision) return;
    disk.add(r.key);
    diskIndex.recoveryQueued(r.key);
    latest.putIfAbsent(r.key, r.revision);
    var player = Minecraft.getInstance().player;
    long view = player == null ? 0 : key(player.chunkPosition().x(), player.chunkPosition().z());
    deferredDisk.put(r.key, new DeferredView(view, LodConfig.MEMORY.get(), r.levels.bytes()));
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent.AfterOpaqueBlocks event) {
    submittedParts = submittedApiBatches = 0;
    if (!active()) return;
    var parts = event.getLevelRenderState().getRenderData(VISIBLE);
    if (parts == null || parts.isEmpty()) return;
    long submitStarted = TELEMETRY ? System.nanoTime() : 0;
    boolean transformsWritten = false;
    try {
      var target = Minecraft.getInstance().getMainRenderTarget();
      var camera = event.getLevelRenderState().cameraRenderState.pos;
      var transformPool = transformArrays.acquire(parts.size(), transformFactory);
      drawMatrix.set(event.getModelViewMatrix());
      for (int i = 0; i < parts.size(); i++) {
        var p = parts.get(i);
        ((Vector3f) transformPool[i].modelOffset())
            .set((float) (p.x * 16. - camera.x), (float) -camera.y, (float) (p.z * 16. - camera.z));
      }
      var slices = RenderSystem.getDynamicUniforms().writeTransforms(transformPool);
      transformsWritten = true;
      long transformsReadyAt = TELEMETRY ? System.nanoTime() : 0;
      int returnedDrawRecords = 0, returnedDrawCalls = 0;
      boolean renderPassCompleted = false;
      // Publish cumulative diagnostics once per pass, not once per visible mesh part.
      // Counts only successful draw API calls, even if a later submission fails.
      long completedTriangles = 0;
      try {
        try (var pass =
          RenderSystem.getDevice()
              .createCommandEncoder()
              .createRenderPass(
                  () -> "Deep Primal real-chunk LOD",
                  target.getColorTextureView(),
                  OptionalInt.empty(),
                  target.getDepthTextureView(),
                  OptionalDouble.empty())) {
        pass.setPipeline(PIPELINE);
        RenderSystem.bindDefaultUniforms(pass);
        GpuLighting.bind(pass);
        if (ClientPerformanceConfig.BATCHED_DRAWS.get()) {
          int index = 0;
          while (index < parts.size()) {
            int take = Math.min(4096, parts.size() - index);
            var batch = indexedDrawBatches.begin(take);
            long batchTriangles = 0;
            try {
              for (int i = 0; i < take; i++) {
                var part = parts.get(index);
                part.transformSlice = slices[index];
                batch.add(part.indexedDraw);
                batchTriangles += part.triangles();
                index++;
              }
              pass.drawMultipleIndexed(
                  batch, null, VertexFormat.IndexType.SHORT,
                  java.util.Set.of("DynamicTransforms"), null);
              completedTriangles += batchTriangles;
              if (TELEMETRY) {
                returnedDrawRecords += take;
                returnedDrawCalls++;
              }
              submittedApiBatches++;
            } finally {
              indexedDrawBatches.end();
            }
          }
        } else
          for (int i = 0; i < parts.size(); i++) {
            pass.setUniform("DynamicTransforms", slices[i]);
            parts.get(i).draw(pass);
            completedTriangles += parts.get(i).triangles();
            if (TELEMETRY) {
              returnedDrawRecords++;
              returnedDrawCalls++;
            }
          }
        }
        renderPassCompleted = true;
      } finally {
        drawnTriangles += completedTriangles;
        if (TELEMETRY) {
          long finishedAt = System.nanoTime();
          submissionProbe.finished(transformsReadyAt - submitStarted,
              finishedAt - transformsReadyAt, finishedAt - submitStarted,
              returnedDrawRecords, returnedDrawCalls, renderPassCompleted);
        }
      }
      if (TELEMETRY) {
        submittedParts = parts.size();
        submitTiming.record(System.nanoTime() - submitStarted);
      }
    } catch (RuntimeException | Error failure) {
      // Pre-draw failures were previously absent from LOD CPU tail statistics.
      // Keep draw-pass failures on the existing finally path; never double count.
      if (TELEMETRY && !transformsWritten)
        submissionProbe.failedBeforeDraw(System.nanoTime() - submitStarted);
      throw failure;
    }
  }

  @SubscribeEvent
  public void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
    reset();
    worldId = null;
  }

  @SubscribeEvent
  public void shutdown(net.neoforged.neoforge.event.GameShuttingDownEvent e) {
    reset();
    worldId = null;
  }

  public void reset() {
    deliveredScan = null;
    discoveryVisited = discoveryTotal = 0;
    discoveryComplete = false;
    cullTiming.reset();
    uploadTiming.reset();
    submitTiming.reset();
    submissionProbe.reset();
    gpuPartProbe.reset();
    pageUploadProbe.reset();
    lastUploadBytes = pendingMeshBudgetBytes = 0;
    drawnTriangles = 0; // Per-world submission totals must not leak into the next world.
    oversizedFirstUploads = oversizedFirstUploadBytes = 0;
    testedParts = rejectedParts = submittedParts = submittedApiBatches = 0;
    session++;
    // Relinquish pending ownership before any fallible GPU close, then attempt
    // every other cached close even if one buffer teardown fails.
    LodGpuResetCleanup closeFailures = new LodGpuResetCleanup();
    PendingGpu abandoned = pendingGpu;
    pendingGpu = null;
    if (abandoned != null) closeFailures.release(abandoned::close);
    // Revoke asynchronous owners before cancellation/shutdown. A failing
    // cancellation, disk-index reset or worker shutdown must not skip cached
    // GPU releases or leave stale world references behind.
    Future<Ready> abandonedFuture = future;
    future = null;
    if (abandonedFuture != null)
      closeFailures.release(() -> abandonedFuture.cancel(true));
    closeFailures.release(diskIndex::reset);
    ThreadPoolExecutor abandonedWorker = worker;
    worker = null;
    if (abandonedWorker != null)
      closeFailures.release(abandonedWorker::shutdownNow);
    workingKey = null;
    captureBurst = 0;
    persistenceFailed = false;
    for (Entry entry : cache.values()) closeFailures.release(entry::close);
    cache.clear();
    evictionHeap = null; // Drop retained arrays on world switch/logout/shutdown.
    indexedDrawBatches.reset();
    visibleSnapshots.reset();
    transformArrays.clear();
    dirty.clear();
    latest.clear();
    disk.clear();
    deferredDisk.clear();
    residentBytes = 0;
    visibleSpan = visibleChunks = queuedChunks = 0;
    store = null;
    root = null;
    level = null;
    // Complete state clearing before surfacing any GPU resource-close failure.
    closeFailures.throwIfFailed();
  }

  private VoxelLodRenderer() {}
}
