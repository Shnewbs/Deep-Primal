package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.core.WaterFlowField;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.core.WaterBoundaryIndex;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import com.shnewbs.deepprimal.core.WaterFlowTransition;
import com.shnewbs.deepprimal.core.WaterSurfaceTransition;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import com.shnewbs.deepprimal.settings.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Owns near liquid presentation. Authority and terrain meshes remain independent. */
public final class AnimatedWaterRenderer {
  public static final AnimatedWaterRenderer INSTANCE = new AnimatedWaterRenderer();
  private static volatile boolean owns;
  private static final long MAX_BYTES = 32L * 1024 * 1024;
  private static final int MAX_PAGES = 2048, MAX_HISTORY = 32768;
  private static final ContextKey<List<Page>> VISIBLE =
      new ContextKey<>(Identifier.fromNamespaceAndPath("deepprimal", "animated_water"));
  private static final RenderPipeline PIPELINE =
      RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
          .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "animated_water"))
          .withVertexShader(
              Identifier.fromNamespaceAndPath("deepprimal_graphics", "core/animated_water"))
          .withFragmentShader(
              Identifier.fromNamespaceAndPath("deepprimal_graphics", "core/animated_water"))
          .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
          .withSampler("Sampler0")
          .withSampler("Sampler2")
          .withDepthStencilState(DepthStencilState.DEFAULT)
          .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
          .withCull(false)
          .build();
  private final LinkedHashMap<Long, Page> pages = new LinkedHashMap<>();
  private final LinkedHashSet<Long> dirty = new LinkedHashSet<>();
  private final LinkedHashSet<Long> urgent = new LinkedHashSet<>();
  private final LinkedHashSet<Long> retireQueue = new LinkedHashSet<>();
  private static final int CORNER_RETRY_BYTES = 1024 * 1024, CORNER_RETRY_RANGES = 16;
  private static final int CORNER_RETRY_SLICE = 65_520;
  private final ArrayDeque<CornerRetry> cornerRetries = new ArrayDeque<>();
  private int cornerRetryBytes;
  private final LinkedHashSet<Long> fresh = new LinkedHashSet<>();
  // Fully hidden water needs no resident page. Remember it until geometry/halo data changes.
  private final LinkedHashSet<Long> hidden = new LinkedHashSet<>();
  private static final WaterPatchMesher.Snapshot EMPTY =
      new WaterPatchMesher.Snapshot(
          new byte[WaterPatchMesher.CELLS],
          new byte[WaterPatchMesher.CELLS],
          new int[WaterPatchMesher.CELLS],
          new int[256]);
  private final List<Long2ObjectLinkedOpenHashMap<WaterSurfaceTransition>>
      heights = new ArrayList<>(),
      alpha = new ArrayList<>();
  private final Long2ObjectLinkedOpenHashMap<WaterFlowTransition> flowDirections = new Long2ObjectLinkedOpenHashMap<>();
  private final Long2ObjectLinkedOpenHashMap<WaterFlowTransition> cornerDirections = new Long2ObjectLinkedOpenHashMap<>();
  private final ArrayList<Future<Ready>> work = new ArrayList<>();
  private final ExecutorService workers =
      Executors.newFixedThreadPool(
          2,
          r -> {
            var t = new Thread(r, "Deep Primal water mesh");
            t.setDaemon(true);
            return t;
          });
  private ClientLevel level;
  private boolean stopped;
  private long session, clock, clockNanos, lastFrame;
  private int discovery, captureTurn;
  public long residentBytes, uploads, captures, drawnPages, terrainInvalidationsAvoided, staleJobs;
  public long heightBoundaryWrites, heightBoundaryBytes, lastHeightBoundaryNanos, maxHeightBoundaryNanos;
  private final Matrix4f viewMatrix = new Matrix4f();
  private final Vector4f white = new Vector4f(1, 1, 1, 1);

  private static final class Page {
    final long key;
    final int x, y, z;
    final AABB bounds;
    final Matrix4f texture = new Matrix4f();
    final Vector3f offset = new Vector3f();
    DynamicUniforms.Transform transform;
    WaterPatchMesher.Snapshot snapshot;
    WaterGpuMesh gpu;
    // Accepted vertex bytes only: total retained payload cannot exceed the 32 MiB GPU cap.
    byte[] presentation;
    WaterBoundaryIndex boundaryQuads = WaterBoundaryIndex.EMPTY;
    int[] heightQuads = new int[0];
    int retireOffset, heightFailures;
    boolean retirePending;
    Map<Integer, WaterFaces> fading = Map.of();
    boolean changed = true, busy;
    long uploaded, used, cleanup, cornerGeneration;

    Page(long key) {
      this.key = key;
      x = SectionPos.x(key) * 16;
      y = SectionPos.y(key) * 16;
      z = SectionPos.z(key) * 16;
      // Shared surface corners can cross a layer boundary by less than one block.
      bounds = new AABB(x, y - 1, z, x + 16, y + 17, z + 16);
    }
  }

  private static final class CornerRetry {
    final Page page;
    final long generation, queuedAt, cleanup;
    final int start;
    final byte[] data, masks;
    int offset, failures;
    CornerRetry(Page page, int start, byte[] data, byte[] masks, long queuedAt, long cleanup) {
      this.page = page; this.generation = page.cornerGeneration; this.start = start;
      this.data = data; this.masks = masks; this.queuedAt = queuedAt; this.cleanup = cleanup;
    }
  }

  private record WaterFaces(float[] vertices, long revision) {}

  private record Ready(
      long session,
      long revision,
      Page page,
      WaterPatchMesher.Snapshot snapshot,
      WaterPatchMesher.Mesh mesh) {}

  private AnimatedWaterRenderer() {
    for (int i = 0; i < 8; i++) heights.add(new Long2ObjectLinkedOpenHashMap<>());
    for (int i = 0; i < 6; i++) alpha.add(new Long2ObjectLinkedOpenHashMap<>());
  }

  public static boolean ownsWater() {
    return owns;
  }

  public int pending() {
    return dirty.size() + urgent.size() + work.size() + cornerRetries.size();
  }

  public long animationTime() {
    return clock;
  }

  public float displayedHeight(int x, int y, int z) {
    var sample = heights.get(0).get(BlockPos.asLong(x, y, z));
    return sample == null ? Float.NaN : sample.at(clock);
  }

  private boolean loaded(int x, int z) {
    // ClientLevel.hasChunk() is unconditionally true. Query delivered storage without creating air.
    var chunk = level.getChunkSource().getChunk(x, z, false);
    return chunk != null && !(chunk instanceof EmptyLevelChunk);
  }

  private void release(Page page) {
    cancelCornerRetries(page, false);
    page.presentation = null;
    page.boundaryQuads = WaterBoundaryIndex.EMPTY;
    page.heightQuads = new int[0];
    retireQueue.remove(page.key);
    page.fading = Map.of();
    dirty.remove(page.key);
    urgent.remove(page.key);
    fresh.remove(page.key);
    if (page.gpu != null) {
      residentBytes -= page.gpu.bytes;
      page.gpu.close();
      page.gpu = null;
    }
  }

  private void chunkChanged(ClientLevel source, int x, int z, boolean removed) {
    if (!owns || source != level) return;
    hidden.removeIf(
        key -> Math.abs(SectionPos.x(key) - x) <= 1 && Math.abs(SectionPos.z(key) - z) <= 1);
    for (var it = pages.values().iterator(); it.hasNext(); ) {
      Page page = it.next();
      int dx = Math.abs((page.x >> 4) - x), dz = Math.abs((page.z >> 4) - z);
      if (dx > 1 || dz > 1) continue;
      if (removed && dx == 0 && dz == 0) {
        release(page);
        it.remove();
      } else {
        page.changed = true;
        urgent.add(page.key);
      }
    }
    if (removed) fresh.removeIf(key -> SectionPos.x(key) == x && SectionPos.z(key) == z);
  }

  @SubscribeEvent
  public void chunkLoaded(ChunkEvent.Load event) {
    if (event.getLevel() instanceof ClientLevel client) {
      var pos = event.getChunk().getPos();
      chunkChanged(client, pos.x(), pos.z(), false);
    }
  }

  @SubscribeEvent
  public void chunkUnloaded(ChunkEvent.Unload event) {
    if (event.getLevel() instanceof ClientLevel client) {
      var pos = event.getChunk().getPos();
      chunkChanged(client, pos.x(), pos.z(), true);
    }
  }

  public void changed(ClientLevel source, BlockPos p) {
    if (!owns || source != level) return;
    for (int dy = -1; dy <= 1; dy++)
      for (int dz = -1; dz <= 1; dz++)
        for (int dx = -1; dx <= 1; dx++) {
          long key =
              SectionPos.asLong((p.getX() + dx) >> 4, (p.getY() + dy) >> 4, (p.getZ() + dz) >> 4);
          invalidateHidden(key);
          Page page = pages.get(key);
          if (page != null) {
            page.changed = true;
            urgent.add(key);
          }
        }
  }

  private void invalidateHidden(long key) {
    if (hidden.remove(key))
      newlyWet(
          new BlockPos(
              SectionPos.x(key) * 16 + 8, SectionPos.y(key) * 16 + 8, SectionPos.z(key) * 16 + 8));
  }

  public void newlyWet(BlockPos p) {
    if (!owns) return;
    long key = SectionPos.asLong(p);
    hidden.remove(key);
    if (pages.containsKey(key)) return;
    fresh.add(key);
    while (fresh.size() > MAX_PAGES * 2) fresh.removeFirst();
    var mc = Minecraft.getInstance();
    if (mc.player != null
        && mc.player.distanceToSqr(p.getX(), p.getY(), p.getZ()) < 128 * 128
        && pages.size() < MAX_PAGES) {
      Page page = new Page(key);
      page.snapshot = EMPTY;
      pages.put(key, page);
      fresh.remove(key);
      urgent.add(key);
    }
  }

  public void sectionChanged(int x, int y, int z) {
    if (!owns) return;
    for (int dy = -1; dy <= 1; dy++)
      for (int dz = -1; dz <= 1; dz++)
        for (int dx = -1; dx <= 1; dx++) {
          long key = SectionPos.asLong(x + dx, y + dy, z + dz);
          invalidateHidden(key);
          var page = pages.get(key);
          if (page != null) {
            page.changed = true;
            urgent.add(page.key);
          }
        }
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (stopped) return;
    var mc = Minecraft.getInstance();
    boolean next =
        mc.level != null && RendererOwnership.owned() && WaterVisualConfig.INTERPOLATE.get();
    if (mc.level != level || next != owns) {
      boolean ownershipChanged = next != owns;
      reset();
      level = mc.level;
      owns = next;
      if (ownershipChanged && level != null) mc.levelRenderer.allChanged();
    }
  }

  @SubscribeEvent
  public void extract(ExtractLevelRenderStateEvent event) {
    if (stopped || !owns || level == null) return;
    var mc = Minecraft.getInstance();
    long now = System.nanoTime();
    if (lastFrame != 0 && !mc.isPaused()) clockNanos += Math.min(250_000_000, now - lastFrame);
    clock = clockNanos / 1_000_000;
    lastFrame = now;
    retirePresentation();
    retryCornerPublications();
    // Retirement consumes this frame's shared admission budget; expiry service is mandatory.
    long deadline = now + 2_000_000;
    int uploadedThisFrame = 0;
    for (var it = work.iterator();
        it.hasNext() && uploadedThisFrame < 2 && System.nanoTime() < deadline; ) {
      var future = it.next();
      if (!future.isDone()) continue;
      it.remove();
      try {
        Ready ready = future.get();
        Page page = ready.page;
        if (ready.session != session || pages.get(page.key) != page) {
          staleJobs++;
          continue;
        }
        page.busy = false;
        upload(ready);
        uploadedThisFrame++;
        if (page.changed) urgent.add(page.key);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (ExecutionException | CancellationException e) {
        throw new IllegalStateException("Water mesh worker failed", e);
      }
    }
    var camera = event.getCamera().position();
    var view = ((LevelRendererAccessor) mc.levelRenderer).deepprimal$viewArea();
    if (view != null)
      for (int n = 0;
          n < Math.min(512, view.sections.length) && System.nanoTime() < deadline;
          n++) {
        var section = view.sections[Math.floorMod(discovery++, view.sections.length)];
        long key = section.getSectionNode();
        if (pages.containsKey(key)
            || hidden.contains(key)
            || !event.getFrustum().isVisible(section.getBoundingBox())) continue;
        int x = SectionPos.x(key), y = SectionPos.y(key), z = SectionPos.z(key);
        if (y < level.getMinSectionY() || y >= level.getMaxSectionY()) continue;
        var chunk = level.getChunkSource().getChunk(x, z, false);
        if (chunk == null || chunk instanceof EmptyLevelChunk) continue;
        if (chunk
            .getSection(chunk.getSectionIndexFromSectionY(y))
            .maybeHas(s -> s.is(EngineRegistry.WATER_BLOCK.get()))) {
          Page p = new Page(key);
          if (pages.size() >= MAX_PAGES) evict(camera, p);
          if (pages.size() < MAX_PAGES) {
            if (fresh.remove(key)) p.snapshot = EMPTY;
            pages.put(key, p);
            dirty.add(key);
          }
        }
      }
    var visible = new ArrayList<Page>();
    for (var it = pages.values().iterator(); it.hasNext(); ) {
      Page page = it.next();
      if (!loaded(page.x >> 4, page.z >> 4)) {
        release(page);
        it.remove();
        continue;
      }
      if (event.getFrustum().isVisible(page.bounds)) {
        page.used = clock;
        if (page.gpu != null) visible.add(page);
        if (page.cleanup != 0 && clock >= page.cleanup) {
          page.cleanup = 0;
          page.changed = true;
          dirty.add(page.key);
        }
      }
    }
    // Existing dirty pages get turns in FIFO order; a newer update never cancels coherent work.
    int attempts = dirty.size() + urgent.size(), submitted = 0;
    while (attempts-- > 0
        && submitted < 2
        && work.size() < 4
        && (!dirty.isEmpty() || !urgent.isEmpty())
        && System.nanoTime() < deadline) {
      var queue =
          !urgent.isEmpty() && (dirty.isEmpty() || (++captureTurn & 7) != 0) ? urgent : dirty;
      long key = queue.removeFirst();
      Page page = pages.get(key);
      if (page == null) continue;
      if (!loaded(page.x >> 4, page.z >> 4)) {
        release(page);
        pages.remove(key);
        continue;
      }
      if (page.busy) {
        queue.add(key);
        continue;
      }
      if (!event.getFrustum().isVisible(page.bounds)) {
        queue.add(key);
        continue;
      }
      dirty.remove(key);
      urgent.remove(key);
      var snapshot = capture(page);
      var before = page.snapshot;
      long generation = session;
      boolean smooth = WaterVisualConfig.SMOOTH.get();
      page.changed = false;
      page.busy = true;
      long revision = ++captures;
      submitted++;
      work.add(
          workers.submit(
              () ->
                  new Ready(
                      generation,
                      revision,
                      page,
                      snapshot,
                      WaterPatchMesher.build(snapshot, before, smooth))));
    }
    synchronizeVisibleHeights(visible);
    visible.sort(Comparator.comparingDouble((Page p) -> p.bounds.distanceToSqr(camera)).reversed());
    event.getRenderState().setRenderData(VISIBLE, List.copyOf(visible));
  }

  /** Sample shared trajectories at this frame's clock instead of replaying a neighbor's
   * older upload clock. Horizontal borders and stepped surfaces share one trajectory.
   * Only indexed position/displacement bytes change; material and opacity stay intact. */
  private void synchronizeVisibleHeights(List<Page> visible) {
    long started = System.nanoTime();
    ByteBuffer transfer = null;
    var quad = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    try {
      pages:
      for (var p : visible) {
        if (p.gpu == null || p.presentation == null) continue;
        var accepted = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
        {
          int start = -1, end = 0;
          for (int heightQuad : p.heightQuads) {
            int offset = heightQuad * 144;
            quad.clear().put(p.presentation, offset, 144).flip();
            boolean changed = false;
            for (int v = 0; v < 4; v++) {
              int vertex = v * 36, source = offset + vertex;
              float x = accepted.getFloat(source), z = accepted.getFloat(source + 8);
              int x2 = Math.round(x * 2), z2 = Math.round(z * 2);
              int cy = Byte.toUnsignedInt(accepted.get(source + 35));
              var h = heights.get((x2 & 1) | ((z2 & 1) << 1)).get(
                  BlockPos.asLong(p.x + Math.floorDiv(x2, 2), p.y + cy, p.z + Math.floorDiv(z2, 2)));
              if (h == null) continue;
              h.advance(clock);
              int heightOffset = (byte) accepted.getShort(source + 26);
              float progress = Math.clamp((clock - p.uploaded - heightOffset) / 100f, 0, 1);
              float shown = accepted.getFloat(source + 4)
                  + accepted.getShort(source + 24) / 16384f * (1 - progress);
              float shared = cy + h.at(clock);
              if (Math.abs(shown - shared) <= .00001f) continue;
              quad.putFloat(vertex + 4, shared).putShort(vertex + 24, (short) 0);
              changed = true;
            }
            if (changed) {
              if (transfer == null)
                transfer = MemoryUtil.memAlloc(WaterCornerPacket.SLICE_BYTES).order(ByteOrder.nativeOrder());
              if (start >= 0 && (offset != end || transfer.remaining() < 144)) {
                if (!publishHeightRange(p, start, transfer)) continue pages;
                start = -1;
              }
              if (start < 0) start = offset;
              transfer.put(quad);end = offset + 144;
            } else if (start >= 0) {
              if (!publishHeightRange(p, start, transfer)) continue pages;
              start = -1;
            }
          }
          if (start >= 0 && !publishHeightRange(p, start, transfer)) continue pages;
        }
        p.heightFailures = 0;
      }
    } finally {
      if (transfer != null) MemoryUtil.memFree(transfer);
      lastHeightBoundaryNanos = System.nanoTime() - started;
      maxHeightBoundaryNanos = Math.max(maxHeightBoundaryNanos, lastHeightBoundaryNanos);
    }
  }

  private boolean publishHeightRange(Page p, int start, ByteBuffer transfer) {
    transfer.flip();
    int length = transfer.remaining();
    try {
      p.gpu.updateRange(start, transfer);
    } catch (RuntimeException failure) {
      if (++p.heightFailures >= 3)
        throw new IllegalStateException("Repeated shared water height upload failure", failure);
      com.mojang.logging.LogUtils.getLogger().warn(
          "Shared water height upload failed for page {}; retrying from live history next frame", p.key, failure);
      transfer.clear();
      return false;
    }
    transfer.get(0, p.presentation, start, length);
    heightBoundaryWrites++;heightBoundaryBytes += length;
    transfer.clear();
    return true;
  }

  private WaterPatchMesher.Snapshot capture(Page page) {
    byte[] units = new byte[WaterPatchMesher.CELLS],
        solid = new byte[units.length], flow = new byte[units.length];
    int[] light = new int[units.length], colors = new int[18 * 18];
    var chunks = new net.minecraft.world.level.chunk.LevelChunk[9];
    for (int dz = -1; dz <= 1; dz++)
      for (int dx = -1; dx <= 1; dx++) {
        var chunk = level.getChunkSource().getChunk((page.x >> 4) + dx, (page.z >> 4) + dz, false);
        if (chunk != null && !(chunk instanceof EmptyLevelChunk))
          chunks[(dz + 1) * 3 + dx + 1] = chunk;
      }
    var pos = new BlockPos.MutableBlockPos();
    for (int z = -1; z <= 16; z++)
      for (int x = -1; x <= 16; x++) {
        var chunk = chunks[(z < 0 ? 0 : z < 16 ? 1 : 2) * 3 + (x < 0 ? 0 : x < 16 ? 1 : 2)];
        for (int y = -1; y <= 16; y++) {
          int i = WaterPatchMesher.index(x, y, z);
          pos.set(page.x + x, page.y + y, page.z + z);
          if (chunk == null || !level.isInWorldBounds(pos)) {
            solid[i] = 1;
            continue;
          }
          var state = chunk.getBlockState(pos);
          if (state.is(EngineRegistry.WATER_BLOCK.get())) {
            units[i] = (byte) (int) state.getValue(EngineFluid.UNITS);
            flow[i] = (byte) (int) state.getValue(EngineFluid.FLOW);
            light[i] = LevelRenderer.getLightCoords(level, pos.above());
          } else if (state.isSolid()) solid[i] = 1;
        }
        if (x >= -1 && x <= 16 && z >= -1 && z <= 16) {
          pos.set(page.x + x, page.y + 8, page.z + z);
          colors[(z + 1) * 18 + x + 1] = BiomeColors.getAverageWaterColor(level, pos);
        }
      }
    return new WaterPatchMesher.Snapshot(units, solid, light, colors, flow);
  }

  private WaterSurfaceTransition transition(
      Long2ObjectLinkedOpenHashMap<WaterSurfaceTransition> map, long key, float old, float next) {
    WaterSurfaceTransition value = map.getAndMoveToLast(key);
    if (value == null) {
      value = new WaterSurfaceTransition(old, clock);
      map.put(key, value);
    }
    value.target(next, clock);
    while (map.size() > MAX_HISTORY) map.removeFirst();
    return value;
  }

  private static int faceKey(float[] values, int i) {
    return (int) values[i + 9] * 6 + WaterPatchMesher.face(values[i + 10]);
  }

  private boolean fading(Page page, int face) {
    int cell = face / 6;
    long key = BlockPos.asLong(page.x + (cell & 15), page.y + (cell >> 8), page.z + ((cell >> 4) & 15));
    var value = alpha.get(face % 6).get(key);
    return value != null && value.target() == 0 && value.at(clock) > 0;
  }

  private Map<Integer, WaterFaces> rememberFading(Page page, List<WaterFaces> parts) {
    var result = new LinkedHashMap<Integer, WaterFaces>();
    for (var faces : parts) {
      float[] values = faces.vertices;
      for (int i = 0; i < values.length; ) {
        int key = faceKey(values, i), end = i + WaterPatchMesher.STRIDE * 4;
        while (end < values.length && faceKey(values, end) == key)
          end += WaterPatchMesher.STRIDE * 4;
        if (values[i + 13] == 0 && fading(page, key))
          result.put(
              key,
              i == 0 && end == values.length
                  ? faces
                  : new WaterFaces(Arrays.copyOfRange(values, i, end), faces.revision));
        i = end;
      }
    }
    return result;
  }

  private WaterFlowTransition flow(long key, int direction, long revision, int previousDirection) {
    var value = flowDirections.getAndMoveToLast(key);
    if (value == null && (direction < 16 || previousDirection < 16)) {
      value = new WaterFlowTransition(previousDirection, clock);
      flowDirections.put(key, value);
    }
    if (value != null) value.target(direction, clock, revision);
    while (flowDirections.size() > MAX_HISTORY) flowDirections.removeFirst();
    return value;
  }

  private WaterFlowTransition cornerFlow(long key, int direction, long revision, int previous) {
    var value = cornerDirections.getAndMoveToLast(key);
    if (value == null && (direction < 16 || previous < 16)) {
      value = new WaterFlowTransition(previous, clock);
      cornerDirections.put(key, value);
    }
    if (value != null) value.target(direction, clock, revision);
    while (cornerDirections.size() > MAX_HISTORY) cornerDirections.removeFirst();
    return value;
  }

  private void upload(Ready ready) {
    Page p = ready.page;
    var parts = new ArrayList<WaterFaces>();
    parts.add(new WaterFaces(ready.mesh.vertices(), ready.revision));
    var present = new BitSet(4096 * 6);
    float[] incoming = ready.mesh.vertices();
    for (int i = 0; i < incoming.length; i += WaterPatchMesher.STRIDE * 4)
      present.set(faceKey(incoming, i));
    // A dry cell can disappear from worker snapshots while its last faces are still fading.
    // Retain only absent, still-dying faces; rewet/new geometry replaces them immediately.
    for (var entry : p.fading.entrySet())
      if (!present.get(entry.getKey()) && fading(p, entry.getKey())) parts.add(entry.getValue());
    int count = 0;
    for (var faces : parts) count += faces.vertices.length / WaterPatchMesher.STRIDE;
    if (count == 0) {
      // An edit during capture/meshing must get another turn before a negative result is cached.
      if (p.changed) return;
      // The previous snapshot may already be dry while its GPU faces are still fading.
      // Repeated empty captures must not cut that fade short or extend its deadline.
      if (p.gpu != null && clock < p.cleanup) return;
      release(p);
      pages.remove(p.key);
      hidden.add(p.key);
      while (hidden.size() > MAX_PAGES * 4) hidden.removeFirst();
      return;
    }
    long bytes = (long) count * 36;
    var camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
    boolean reuse = p.gpu != null && bytes <= p.gpu.bytes;
    if (!reuse && !reserveBytes(camera, p, bytes)) {
      p.changed = true;
      return;
    }
    ByteBuffer data = MemoryUtil.memAlloc((int) bytes).order(ByteOrder.nativeOrder());
    boolean animating = false, dyingFaces = false;
    int remaining = 0;
    var boundary = new IntArrayList();
    var heightQuads = new IntArrayList();
    WaterSurfaceTransition opacity = null;
    var atlas =
        (TextureAtlas)
            Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
    var still = atlas.getSprite(Identifier.withDefaultNamespace("block/water_still"));
    var flowing = atlas.getSprite(Identifier.withDefaultNamespace("block/water_flow"));
    try {
      for (var faces : parts) {
        float[] values = faces.vertices;
        int shownDirection = 16;
        WaterFlowTransition flowState = null;
        int cornerTargets = 0, cornerSources = 0, cornerAges = 0;
        for (int v = 0; v < values.length / WaterPatchMesher.STRIDE; v++) {
          int i = v * WaterPatchMesher.STRIDE;
          int cell = (int) values[i + 9];
          int cx = cell & 15, cz = (cell >> 4) & 15, cy = cell >> 8;
          int face = WaterPatchMesher.face(values[i + 10]);
          long cellKey = BlockPos.asLong(p.x + cx, p.y + cy, p.z + cz);
          if ((v & 3) == 0) {
            int direction = WaterPatchMesher.direction(values[i + 10]);
            // All faces share the cell's admitted material and original transition clock.
            // Unchanged still water stays sparse; the accepted snapshot seeds its first change.
            int previousDirection = p.snapshot != null && p.snapshot.water(cx, cy, cz) > 0
                ? p.snapshot.direction(cx, cy, cz) : direction;
            flowState = flow(cellKey, direction, faces.revision, previousDirection);
            shownDirection = flowState == null ? direction : flowState.target();
            if (flowState != null && flowState.queued(clock)) {
              animating = true;
              remaining = Math.max(remaining, WaterFlowTransition.MILLIS - flowState.age(clock));
            }
            if (face == 0) {
              cornerTargets = cornerSources = cornerAges = 0;
              for (int n = 0; n < 4; n++) {
                int x = cx + (n & 1), z = cz + (n >> 1);
                int directionAtCorner = WaterFlowField.direction(ready.snapshot, x, cy, z);
                int old = WaterFlowField.direction(p.snapshot, x, cy, z);
                if (directionAtCorner < 0) directionAtCorner = old < 0 ? direction : old;
                if (old < 0) old = directionAtCorner;
                var corner = cornerFlow(BlockPos.asLong(p.x + x, p.y + cy, p.z + z), directionAtCorner, faces.revision, old);
                int to = corner == null ? directionAtCorner : corner.target();
                int from = corner == null || !corner.active(clock) ? to : corner.source();
                int age = corner == null ? 100 : corner.age(clock);
                cornerTargets |= to << (n * 5); cornerSources |= from << (n * 5);
                cornerAges |= age << (n * 8);
                if (corner != null && corner.queued(clock)) {
                  animating = true;
                  remaining = Math.max(remaining, 100 - age);
                }
              }
            }
            opacity =
                transition(alpha.get(face), cellKey, values[i + 12], values[i + 13]);
            dyingFaces |= values[i + 13] == 0;
          }
          float target = values[i + 3], from = target;
          int heightLayer = cy, connection = 0;
          int offset = 0;
          if (values[i + 11] != 0) {
            int x2 = Math.round(values[i] * 2),
                z2 = Math.round(values[i + 2] * 2),
                part = (x2 & 1) | ((z2 & 1) << 1);
            if (face == 0) {
              connection = WaterPatchMesher.surfaceConnection(ready.snapshot, x2, cy, z2)
                  | WaterPatchMesher.surfaceConnection(p.snapshot, x2, cy, z2);
              // Retained drying faces can outlive both wet snapshots. Their endpoints
              // still identify a supported upper ledge. Padding stores the anchor only.
              int raised = (connection & 1) != 0 || Math.max(target, values[i + 4]) > 1 ? 1 : 0;
              heightLayer += raised;
            }
            long key =
                BlockPos.asLong(p.x + Math.floorDiv(x2, 2), p.y + heightLayer, p.z + Math.floorDiv(z2, 2));
            boolean wall = face >= 1 && face <= 4;
            if (wall) {
              // Clipped stacked walls and free surfaces can meet at the same world corner.
              // They need independent histories or the top packet re-extends the wall.
              part = 4 + Math.round(values[i] - cx) + 2 * Math.round(values[i + 2] - cz);
              key = cellKey;
            }
            var map = heights.get(part);
            var h = map.getAndMoveToLast(key);
            if (h == null) {
              h = new WaterSurfaceTransition(values[i + 4] - (heightLayer - cy), clock);
              map.put(key, h);
            }
            h.target(target - (heightLayer - cy), clock, faces.revision);
            target = h.target() + (heightLayer - cy);
            while (map.size() > (wall ? MAX_HISTORY / 4 : MAX_HISTORY)) map.removeFirst();
            from = h.from(clock) + (heightLayer - cy);
            offset = h.offset(clock);
            int heightRemaining = h.remaining(clock);
            if (heightRemaining > 0) {
              animating = true;
              remaining = Math.max(remaining, heightRemaining);
            }
          }
          if (Math.abs(from - target) > .0001) {
            animating = true;
            remaining = Math.max(remaining, WaterSurfaceTransition.MILLIS + offset);
          }
          if (Math.abs(opacity.from(clock) - opacity.target()) > .0001) {
            animating = true;
            remaining = Math.max(remaining, WaterSurfaceTransition.MILLIS + opacity.offset(clock));
          }
          int color = Float.floatToRawIntBits(values[i + 7]),
              light = Float.floatToRawIntBits(values[i + 8]);
          if ((v & 3) == 0 && face == 0 && (cx == 0 || cx == 15 || cz == 0 || cz == 15))
            boundary.add(((data.position() / 144) << 12) | cell);
          if (face == 0 && ((connection & 2) != 0 || heightLayer != cy
              || cx == 0 || cx == 15 || cz == 0 || cz == 15)) {
            int quad = data.position() / 144;
            if (heightQuads.isEmpty() || heightQuads.getInt(heightQuads.size() - 1) != quad)
              heightQuads.add(quad);
          }
          int vertexOffset = data.position();
          data.putFloat(values[i]).putFloat(values[i + 1] + target).putFloat(values[i + 2]);
          boolean blend = flowState != null && flowState.active(clock)
              && (face == 0 || (flowState.source() < 16) != (shownDirection < 16));
          // Opacity lives in Normal; active faces reuse constant color alpha for cell-local UVs.
          data.put((byte) (color >> 16)).put((byte) (color >> 8)).put((byte) color)
              .put((byte) (face == 0 || blend ? cx | (cz << 4) : 255));
          boolean flowingFace = shownDirection < 16;
          boolean side = face != 0;
          if (face == 0) {
            data.putFloat(WaterFlowField.targets(cornerTargets, (cornerAges >>> 24) & 255))
                .putFloat(WaterFlowField.sources(cornerSources, (cornerAges >>> 24) & 255));
          } else {
            var sprite = flowingFace ? flowing : still;
            float scale = flowingFace ? .5f : 1;
            float localU = values[i + 5] * (WaterPatchMesher.direction(values[i + 10]) < 16 ? 2 : 1);
            data.putFloat(sprite.getU(localU * scale)).putFloat(sprite.getV((1 - target) * scale));
          }
          data.putShort((short) Math.round(Math.clamp(from - target, -1, 1) * 16384))
              .putShort((short) (face == 0 ? WaterFlowField.heightClock(offset, cornerAges & 255) : offset));
          // Native light coordinates occupy the low byte; reserve bits 8/9 for side mode and bit 10 for periodic flow tops.
          int presentation = side ? 256 | (flowingFace ? 512 : 0) : 0;
          int source = blend ? flowState.source() << 11 : 0;
          int age = blend ? 32768 | (flowState.age(clock) << 8) : 0;
          data.putShort((short) ((light & 255) | (face == 0 ? ((cornerAges >>> 8) & 255) << 8 : presentation | source)))
              .putShort((short) (((light >>> 16) & 255) | (face == 0 ? ((cornerAges >>> 16) & 255) << 8 : age)));
          data.put((byte) Math.round(opacity.from(clock) * 127))
              .put((byte) Math.round(opacity.target() * 127))
              .put((byte) Math.clamp(opacity.offset(clock), -WaterSurfaceTransition.MILLIS, 0))
              .put((byte) heightLayer);
          if (face == 0) WaterCornerPacket.fromLegacy(data, vertexOffset, clock);
        }
      }
      data.flip();
      byte[] accepted = new byte[data.remaining()];
      data.duplicate().get(accepted);
      var boundaryIndex = WaterBoundaryIndex.of(boundary.toIntArray());
      if (reuse) {
        // Retain allocation capacity: a smaller update can grow back without another buffer.
        p.gpu.update(data, count);
      } else {
        var mesh = new WaterGpuMesh(data, count);
        if (p.gpu != null) {
          residentBytes -= p.gpu.bytes;
          p.gpu.close();
        }
        p.gpu = mesh;
        residentBytes += mesh.bytes;
      }
      // Retained CPU faces are a subset of the admitted GPU payload; failed admission changes neither.
      // Existing uniform storage carries sprite bounds; no vertex/uniform allocation growth.
      p.texture.m10(still.getV(1) - still.getV(0)).m11((flowing.getV(1) - flowing.getV(0)) * .5f);
      p.texture.m20(still.getV(0)).m21(flowing.getV(0));
      p.texture.m13(still.getU(0)).m22(still.getU(1) - still.getU(0));
      p.texture.m01(flowing.getU(0)).m02(flowing.getU(1) - flowing.getU(0));
      p.texture.m03(flowing.getV(0)).m12(flowing.getV(1) - flowing.getV(0));
      p.texture.m30(p.x).m31(p.z);
      p.fading = rememberFading(p, parts);
      p.snapshot = ready.snapshot;
      cancelCornerRetries(p, false);
      p.presentation = accepted;
      p.boundaryQuads = boundaryIndex;
      p.heightQuads = heightQuads.toIntArray();
      p.retireOffset = 0;
      p.retirePending = false;
      retireQueue.remove(p.key);
      retireQueue.add(p.key);
      p.texture.m23(clock & 65535);
      p.uploaded = clock;
      p.cleanup = animating || dyingFaces ? clock + Math.max(0, remaining) + 1 : 0;
      uploads++;
      synchronizeCornerNeighbors(p, true);
    } finally {
      MemoryUtil.memFree(data);
    }
  }

  /** Visit only neighbor-facing quads; contiguous changed quads share one bounded submission.
   * Publication remains synchronous: a range bound does not establish a frame-time bound. */
  private void synchronizeCornerNeighbors(Page admitted) {
    synchronizeCornerNeighbors(admitted, false);
  }

  /** Ordinary publication stays within one extraction; only rejected ranges are deferred. */
  private void synchronizeCornerNeighbors(Page admitted, boolean deferFailures) {
    ByteBuffer transfer = null;
    ByteBuffer quad = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    try {
      for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dz == 0) continue;
        Page p = pages.get(SectionPos.asLong((admitted.x >> 4) + dx, admitted.y >> 4, (admitted.z >> 4) + dz));
        if (p == null || p.gpu == null || p.presentation == null) continue;
        cancelCornerRetries(p, true);
        ByteBuffer accepted = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
        int start = -1, end = 0;
        long pendingCleanup = p.cleanup;
        // Relative to the resident page, the admitted page lies in the opposite direction.
        int endIndex = p.boundaryQuads.end(-dx, -dz);
        for (int index = p.boundaryQuads.begin(-dx, -dz); index < endIndex; index++) {
          int entry = p.boundaryQuads.entry(index);
          int shared = WaterBoundaryIndex.mask(entry, -dx, -dz);
          int at = (entry >>> 12) * 144, cell = entry & 4095;
          int cx = cell & 15, cz = (cell >> 4) & 15, y = p.y + (cell >> 8);
          int mask = 0, targets = 0, sources = 0, ages = 0;
          long cleanup = start >= 0 ? pendingCleanup : p.cleanup;
          for (int n = 0; n < 4; n++) {
            if ((shared & (1 << n)) == 0) continue;
            int x = p.x + cx + (n & 1), z = p.z + cz + (n >> 1);
            var c = cornerDirections.get(BlockPos.asLong(x, y, z));
            if (c == null) continue;
            int to = c.target(), from = c.active(clock) ? c.source() : to, age = c.age(clock);
            mask |= 1 << n;
            targets |= to << (n * 5); sources |= from << (n * 5); ages |= age << (n * 8);
            if (c.queued(clock)) {
              long next = clock + 100 - age + 1;
              cleanup = cleanup == 0 ? next : Math.min(cleanup, next);
            }
          }
          int changed = WaterCornerPacket.changedCorners(accepted, at, mask, targets, sources, ages, clock);
          if (changed != 0) {
            quad.clear().put(p.presentation, at, 144).flip();
            WaterCornerPacket.patchCorners(quad, 0, changed, targets, sources, ages, clock);
            if (transfer == null)
              transfer = MemoryUtil.memAlloc(WaterCornerPacket.SLICE_BYTES).order(ByteOrder.nativeOrder());
            // Never bridge an unchanged quad or exceed the existing range-byte bound.
            if (start >= 0 && (at != end || transfer.remaining() < 144)) {
              publishCornerRange(p, start, transfer, pendingCleanup, deferFailures);
              start = -1;
            }
            if (start < 0) start = at;
            transfer.put(quad);
            end = at + 144;
            pendingCleanup = cleanup;
          } else {
            if (start >= 0) {
              publishCornerRange(p, start, transfer, pendingCleanup, deferFailures);
              start = -1;
            }
            p.cleanup = cleanup;
          }
        }
        if (start >= 0) publishCornerRange(p, start, transfer, pendingCleanup, deferFailures);
      }
    } finally {
      if (transfer != null) MemoryUtil.memFree(transfer);
    }
  }

  /** Commit the accepted CPU mirror and deadline only after the entire GPU range succeeds. */
  private void publishCornerRange(Page p, int start, ByteBuffer transfer, long cleanup, boolean deferFailures) {
    transfer.flip();
    int length = transfer.remaining();
    try {
      p.gpu.updateRange(start, transfer);
    } catch (RuntimeException failure) {
      if (!deferFailures) throw failure;
      deferCornerRange(p, start, transfer.duplicate().position(0).limit(length), cleanup);
      transfer.clear();
      return;
    }
    transfer.get(0, p.presentation, start, length);
    p.cleanup = cleanup;
    armRetirement(p);
    transfer.clear();
  }

  /** A new material publication supersedes old failed data; remesh unaccepted earlier corners. */
  private void cancelCornerRetries(Page p, boolean remesh) {
    boolean removed = false;
    for (var it = cornerRetries.iterator(); it.hasNext();) {
      var retry = it.next();
      if (retry.page != p) continue;
      cornerRetryBytes -= retry.data.length;
      it.remove(); removed = true;
    }
    p.cornerGeneration++;
    if (removed && remesh) { p.changed = true; urgent.add(p.key); }
  }

  /** Bounded retained recovery data. Overflow schedules a fresh authoritative mesh instead. */
  private void deferCornerRange(Page p, int start, ByteBuffer transfer, long cleanup) {
    int length = transfer.remaining();
    if (cornerRetries.size() >= CORNER_RETRY_RANGES || length > CORNER_RETRY_BYTES - cornerRetryBytes) {
      p.changed = true; urgent.add(p.key);
      return;
    }
    byte[] data = new byte[length], masks = new byte[length / 144];
    transfer.get(0, data);
    var next = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
    var old = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
    for (int q = 0; q < length; q += 144) for (int n = 0; n < 4; n++) {
      if (WaterCornerPacket.target(next, q, n) != WaterCornerPacket.target(old, start + q, n)
          || WaterCornerPacket.source(next, q, n) != WaterCornerPacket.source(old, start + q, n)
          || WaterCornerPacket.start(next, q, n) != WaterCornerPacket.start(old, start + q, n))
        masks[q / 144] |= (byte)(1 << n);
    }
    cornerRetries.addLast(new CornerRetry(p, start, data, masks, clock, cleanup));
    cornerRetryBytes += length;
  }

  /** Mandatory fair recovery: <=16 stale headers, one write and65520bytes per rendered frame.
   * Rebuild only requested material corners over the latest accepted bytes, retaining retirement,
   * height, light and opacity. A completed transition becomes steady rather than reviving on wrap.
   * Three consecutive synchronous failures on a range remain fatal; async loss is not handled. */
  private void retryCornerPublications() {
    for (int headers = 0; headers < CORNER_RETRY_RANGES && !cornerRetries.isEmpty(); headers++) {
      var retry = cornerRetries.removeFirst(); Page p = retry.page;
      if (pages.get(p.key) != p || p.gpu == null || p.presentation == null
          || p.cornerGeneration != retry.generation) {
        cornerRetryBytes -= retry.data.length;
        continue;
      }
      int length = Math.min(CORNER_RETRY_SLICE, retry.data.length - retry.offset);
      ByteBuffer patch = MemoryUtil.memAlloc(length).order(ByteOrder.nativeOrder());
      try {
        patch.put(p.presentation, retry.start + retry.offset, length).flip();
        var queued = ByteBuffer.wrap(retry.data).order(ByteOrder.nativeOrder());
        for (int q = 0; q < length; q += 144) {
          int original = retry.offset + q;
          WaterCornerPacket.rebaseCorners(patch, q, queued, original,
              retry.masks[original / 144] & 15, retry.queuedAt, clock);
        }
        try {
          p.gpu.updateRange(retry.start + retry.offset, patch);
        } catch (RuntimeException failure) {
          if (++retry.failures >= 3) {
            cornerRetryBytes -= retry.data.length;
            throw new IllegalStateException("Water corner publication failed after three recovery attempts", failure);
          }
          cornerRetries.addLast(retry);
          return;
        }
        patch.get(0, p.presentation, retry.start + retry.offset, length);
        armRetirement(p);
        if (retry.cleanup != 0) p.cleanup = p.cleanup == 0 ? retry.cleanup : Math.min(p.cleanup, retry.cleanup);
        retry.offset += length; retry.failures = 0;
        if (retry.offset == retry.data.length) cornerRetryBytes -= retry.data.length;
        else cornerRetries.addLast(retry);
        return;
      } finally { MemoryUtil.memFree(patch); }
    }
  }

  /** Preserve cursor progress when a write lands behind it. Such data gets a second sweep;
   * resetting an active cursor on each publication could starve later parts of a large page. */
  private void armRetirement(Page p) {
    if (retireQueue.add(p.key)) { p.retireOffset = 0; p.retirePending = false; }
    else p.retirePending = true;
  }

  /** Guaranteed service before optional extraction: <=32 page headers/262080 bytes.
   * Remove fully steady pages. Live pairs and writes behind the cursor require another sweep.
   * A sweep still completes within193 frames at nominal caps, before16-bit clock wrap. */
  private void retirePresentation() {
    int budget = WaterCornerPacket.SLICE_BYTES, pagesVisited = 0;
    ByteBuffer transfer = null;
    try {
      while (budget >= 144 && pagesVisited < 32 && !retireQueue.isEmpty()) {
        long key = retireQueue.iterator().next(); Page p = pages.get(key);
        if (p == null || p.gpu == null || p.presentation == null) {
          retireQueue.remove(key); pagesVisited++; continue;
        }
        int length = Math.min(p.presentation.length - p.retireOffset, budget); length -= length % 144;
        int begin = p.retireOffset, end = begin + length;
        ByteBuffer accepted = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
        int state = WaterCornerPacket.retirementState(accepted, begin, end, clock);
        if ((state & WaterCornerPacket.RETIRE_EXPIRED) != 0) {
          if (transfer == null) transfer = MemoryUtil.memAlloc(WaterCornerPacket.SLICE_BYTES).order(ByteOrder.nativeOrder());
          transfer.clear().put(p.presentation, begin, length).flip();
          WaterCornerPacket.retire(transfer, 0, length, clock);
          p.gpu.updateRange(begin, transfer);
          transfer.get(0, p.presentation, begin, length);
        }
        // No cursor/pending/CPU commit follows a rejected GPU write.
        p.retirePending |= (state & WaterCornerPacket.RETIRE_LIVE) != 0;
        budget -= length; p.retireOffset = end;
        if (end == p.presentation.length) {
          boolean again = p.retirePending; p.retireOffset = 0; p.retirePending = false;
          retireQueue.remove(key);
          if (again) retireQueue.add(key);
          pagesVisited++;
        }
      }
    } finally { if (transfer != null) MemoryUtil.memFree(transfer); }
  }

  /** Plan a sufficient farthest-first set before releasing any covering allocation.
   * Discovery's separate page-count policy is unchanged. A denied byte reservation is read-only. */
  private boolean reserveBytes(net.minecraft.world.phys.Vec3 camera, Page keep, long bytes) {
    if (bytes > MAX_BYTES) return false;
    long required = residentBytes + bytes - MAX_BYTES;
    if (required <= 0) return true;
    double minimumDistance = keep.bounds.distanceToSqr(camera);
    var candidates = new ArrayList<Page>();
    long available = 0;
    for (Page page : pages.values()) {
      if (page == keep || page.busy || page.gpu == null
          || page.bounds.distanceToSqr(camera) <= minimumDistance) continue;
      candidates.add(page);
      available += page.gpu.bytes;
    }
    if (available < required) return false;
    // Stable sorting retains the prior insertion-order rule for equal-distance victims.
    candidates.sort(Comparator.comparingDouble((Page page) -> page.bounds.distanceToSqr(camera)).reversed());
    for (Page page : candidates) {
      long reclaimed = page.gpu.bytes;
      pages.remove(page.key);
      release(page);
      required -= reclaimed;
      if (required <= 0) return true;
    }
    throw new IllegalStateException("Water reservation accounting changed during admission");
  }

  private boolean evict(net.minecraft.world.phys.Vec3 camera, Page keep) {
    Page victim = null;
    // Discovery/upload of a farther page must not displace a closer covering mesh.
    // Busy farther pages are ineligible; wait rather than evict a closer idle page.
    double far = keep == null ? -1 : keep.bounds.distanceToSqr(camera);
    for (Page p : pages.values())
      if (p != keep && !p.busy) {
        double d = p.bounds.distanceToSqr(camera);
        if (d > far) {
          far = d;
          victim = p;
        }
      }
    if (victim == null) return false;
    pages.remove(victim.key);
    release(victim);
    return true;
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    if (!owns) return;
    var visible = event.getLevelRenderState().getRenderData(VISIBLE);
    if (visible == null || visible.isEmpty()) return;
    var mc = Minecraft.getInstance();
    var camera = event.getLevelRenderState().cameraRenderState.pos;
    var transforms = new DynamicUniforms.Transform[visible.size()];
    viewMatrix.set(event.getModelViewMatrix());
    for (int i = 0; i < visible.size(); i++) {
      Page p = visible.get(i);
      p.offset.set((float) (p.x - camera.x), (float) (p.y - camera.y), (float) (p.z - camera.z));
      float elapsed = Math.min(65536, clock - p.uploaded);
      p.texture.m00(elapsed).m23((clock & 65535) - elapsed);
      if (p.transform == null)
        p.transform = new DynamicUniforms.Transform(viewMatrix, white, p.offset, p.texture);
      transforms[i] = p.transform;
    }
    var slices = RenderSystem.getDynamicUniforms().writeTransforms(transforms);
    var target = mc.getMainRenderTarget();
    try (var pass =
        RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "Deep Primal independent water",
                target.getColorTextureView(),
                OptionalInt.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty())) {
      pass.setPipeline(PIPELINE);
      RenderSystem.bindDefaultUniforms(pass);
      var atlas = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
      pass.bindTexture(
          "Sampler2",
          mc.gameRenderer.lightmap(),
          RenderSystem.getSamplerCache()
              .getClampToEdge(com.mojang.blaze3d.textures.FilterMode.NEAREST));
      for (int i = 0; i < visible.size(); i++) {
        Page p = visible.get(i);
        if (p.gpu == null) continue;
        pass.setUniform("DynamicTransforms", slices[i]);
        p.gpu.draw(pass);
        drawnPages++;
      }
    }
  }

  public void reset() {
    session++;
    for (var f : work) f.cancel(true);
    work.clear();
    for (Page p : pages.values()) {
      p.fading = Map.of();
      p.presentation = null;
      p.boundaryQuads = WaterBoundaryIndex.EMPTY;
      if (p.gpu != null) {
        p.gpu.close();
        p.gpu = null;
      }
    }
    cornerRetries.clear();
    cornerRetryBytes = 0;
    pages.clear();
    dirty.clear();
    urgent.clear();
    fresh.clear();
    retireQueue.clear();
    hidden.clear();
    heights.forEach(Map::clear);
    alpha.forEach(Map::clear);
    flowDirections.clear();
    cornerDirections.clear();
    residentBytes = 0;
    discovery = 0;
    lastFrame = 0;
  }

  @SubscribeEvent
  public void logout(ClientPlayerNetworkEvent.LoggingOut event) {
    reset();
    owns = false;
    level = null;
  }

  @SubscribeEvent
  public void shutdown(net.neoforged.neoforge.event.GameShuttingDownEvent event) {
    stopped = true;
    owns = false;
    reset();
    workers.shutdownNow();
  }
}
