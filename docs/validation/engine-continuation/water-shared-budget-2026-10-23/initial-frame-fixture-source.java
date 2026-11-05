package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.minecraft.core.SectionPos;
import org.lwjgl.system.MemoryUtil;

/** Opt-in private residency fixture. One production retirement call per distinct rendered frame. */
final class WaterFrameRetirementChecks {
  private static Job active;
  private static long frame;

  static void start(Path output, boolean moving) throws Exception {
    if (active != null) throw new IllegalStateException("Frame retirement fixture already active");
    Job job = new Job(output, moving);
    active = job;
    try { job.setup(); }
    catch (Exception | Error e) { active = null; job.close(); throw e; }
  }

  @SubscribeEvent
  public void before(RenderFrameEvent.Pre event) {
    long id = ++frame, now = System.nanoTime();
    Job job = active;
    if (job == null || !job.ready || job.readQueued) return;
    try {
      if (job.settle++ < 5) return;
      if (job.last != null) job.last.put("frameIntervalNanos", now - job.last.get("preNanos"));
      if (job.samples.size() == Job.TURNS) { job.finish(); return; }
      job.turn(id, now);
    } catch (Exception | Error e) { active = null; job.close(); throw new RuntimeException(e); }
  }

  @SubscribeEvent
  public void after(RenderFrameEvent.Post event) {
    Job job = active;
    if (job != null && job.last != null && job.last.get("frame") == frame)
      job.last.put("frameCpuNanos", System.nanoTime() - job.last.get("preNanos"));
  }

  private static Map<String, Long> memory() {
    var result = new LinkedHashMap<String, Long>();
    var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    result.put("heapUsedBytes", heap.getUsed()); result.put("heapCommittedBytes", heap.getCommitted());
    for (var pool : ManagementFactory.getPlatformMXBeans(java.lang.management.BufferPoolMXBean.class)) {
      result.put(pool.getName() + "PoolUsedBytes", pool.getMemoryUsed());
      result.put(pool.getName() + "PoolCapacityBytes", pool.getTotalCapacity());
    }
    return result;
  }

  private static Map<String, Object> scene() throws Exception {
    var mc = net.minecraft.client.Minecraft.getInstance();
    var result = new LinkedHashMap<String, Object>();
    result.put("backend", RenderSystem.getDevice().getBackendName());
    result.put("player", List.of(mc.player.getX(), mc.player.getY(), mc.player.getZ(), (double) mc.player.getYRot(), (double) mc.player.getXRot()));
    var horizon = com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE;
    result.put("cachedLodTiles", horizon.cachedTiles); result.put("visibleLodTiles", horizon.visibleTiles); result.put("drawnLodTriangles", horizon.drawnTriangles);
    var water = (Map<?, ?>) WaterPublicationFailureChecks.field(AnimatedWaterRenderer.class, "pages").get(AnimatedWaterRenderer.INSTANCE);
    var keys = water.keySet().stream().map(Object::toString).sorted().toList();
    result.put("admittedWaterPages", keys.size()); result.put("admittedWaterKeySha256", WaterPublicationFailureChecks.sha(String.join(",", keys).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    result.put("animated", com.shnewbs.deepprimal.settings.WaterVisualConfig.INTERPOLATE.get());
    result.put("waves", com.shnewbs.deepprimal.settings.WaterVisualConfig.WAVES.get());
    result.put("lod", com.shnewbs.deepprimal.settings.LodConfig.ENABLED.get());
    return result;
  }

  private static final class Job implements AutoCloseable {
    static final int PAGES = 2048, TOTAL = (32 * 1024 * 1024 / 144) * 144;
    static final int TURNS = (TOTAL + WaterCornerPacket.SLICE_BYTES - 1) / WaterCornerPacket.SLICE_BYTES;
    final Path output;
    final boolean moving, vulkan;
    final Class<?> type = AnimatedWaterRenderer.class, pageType;
    final Object renderer, device = RenderSystem.getDevice(), original, proxy;
    final Method retire, getStaging, stagingUsed, stagingCapacity;
    final Field backend, vertices;
    final Map<Long, Object> pages;
    final Set<Long> queue;
    final List<Map<String, Long>> samples = new ArrayList<>();
    final List<Map<String, Object>> stagingEvents = new ArrayList<>();
    final List<GpuBuffer> readbacks = new ArrayList<>();
    final Map<String, Long> beforeMemory = memory();
    Map<String, Long> setupMemory, endMemory, last;
    Map<String, Object> sceneBefore, sceneAfter;
    long totalWrites, totalBytes, maxRange, wraps;
    int settle;
    boolean ready, readQueued, closed;

    @SuppressWarnings("unchecked")
    Job(Path output, boolean moving) throws Exception {
      this.output = output; this.moving = moving;
      pageType = Class.forName(type.getName() + "$Page");
      var constructor = type.getDeclaredConstructor(); constructor.setAccessible(true);
      renderer = constructor.newInstance();
      pages = (Map<Long, Object>) WaterPublicationFailureChecks.field(type, "pages").get(renderer);
      queue = (Set<Long>) WaterPublicationFailureChecks.field(type, "retireQueue").get(renderer);
      retire = type.getDeclaredMethod("retirePresentation"); retire.setAccessible(true);
      vertices = WaterPublicationFailureChecks.field(WaterGpuMesh.class, "vertices");
      backend = Arrays.stream(device.getClass().getDeclaredFields())
          .filter(f -> f.getType().isInterface() && f.getType().getSimpleName().equals("GpuDeviceBackend"))
          .findFirst().orElseThrow();
      backend.setAccessible(true); original = backend.get(device);
      vulkan = RenderSystem.getDevice().getBackendName().equals("Vulkan");
      if (vulkan) {
        getStaging = Class.forName("net.vulkanmod.vulkan.Vulkan").getMethod("getStagingBuffer");
        Object staging = getStaging.invoke(null);
        stagingUsed = staging.getClass().getMethod("getUsedBytes");
        stagingCapacity = staging.getClass().getMethod("getBufferSize");
      } else { getStaging = stagingUsed = stagingCapacity = null; }
      proxy = Proxy.newProxyInstance(backend.getType().getClassLoader(), new Class<?>[]{backend.getType()}, (o, m, args) -> {
        long started = System.nanoTime(); Object encoder = invoke(m, original, args);
        if (!m.getName().equals("createCommandEncoder")) return encoder;
        last.merge("backendEncoderNanos", System.nanoTime() - started, Long::sum);
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(), new Class<?>[]{m.getReturnType()}, (e, op, a) -> {
          if (!op.getName().equals("writeToBuffer") || !(a[0] instanceof GpuBufferSlice)) return invoke(op, encoder, a);
          int bytes = ((ByteBuffer) a[1]).remaining();
          last.merge("rangeWrites", 1L, Long::sum); last.merge("rangeBytes", (long) bytes, Long::sum);
          totalWrites++; totalBytes += bytes; maxRange = Math.max(maxRange, bytes);
          Object staging = vulkan ? getStaging.invoke(null) : null;
          long used = vulkan ? (long) stagingUsed.invoke(staging) : -1;
          long capacity = vulkan ? (long) stagingCapacity.invoke(staging) : -1;
          boolean wrap = vulkan && bytes > capacity - ((used + 3) & ~3L);
          if (wrap) wraps++;
          long begin = System.nanoTime();
          try { return invoke(op, encoder, a); }
          finally {
            long elapsed = System.nanoTime() - begin;
            last.merge("backendWriteNanos", elapsed, Long::sum);
            if (vulkan && (wrap || totalWrites <= 4)) {
              var row = new LinkedHashMap<String, Object>();
              row.put("frame", frame); row.put("write", totalWrites); row.put("bytes", bytes);
              row.put("stagingUsedBefore", used); row.put("stagingCapacity", capacity);
              row.put("predictedWrap", wrap); row.put("stagingUsedAfter", (long) stagingUsed.invoke(staging));
              row.put("backendNanos", elapsed); stagingEvents.add(row);
            }
          }
        });
      });
    }

    void setup() throws Exception {
      var constructor = pageType.getDeclaredConstructor(long.class); constructor.setAccessible(true);
      int quads = TOTAL / 144, base = quads / PAGES, extra = quads % PAGES;
      var data = MemoryUtil.memCalloc((base + 1) * 144).order(ByteOrder.nativeOrder());
      try {
        long pairs = (6 * 17L + (moving ? 2 : 6)) * (1 + 289 + 83521 + 24137569L);
        for (int p = 0; p < data.capacity(); p += 36) WaterCornerPacket.write(data, p, pairs, 0, 0, 0, 0, true);
        for (int n = 0; n < PAGES; n++) {
          int bytes = (base + (n < extra ? 1 : 0)) * 144;
          long key = SectionPos.asLong(n, 0, 0); Object page = constructor.newInstance(key);
          data.limit(144); var mesh = new WaterGpuMesh(data, 4); ((GpuBuffer) vertices.get(mesh)).close();
          data.limit(bytes); vertices.set(mesh, RenderSystem.getDevice().createBuffer(() -> "Frame retirement readable page",
              GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, data));
          WaterPublicationFailureChecks.field(WaterGpuMesh.class, "bytes").setLong(mesh, bytes);
          WaterPublicationFailureChecks.field(WaterGpuMesh.class, "indices").setInt(mesh, bytes / 144 * 6);
          if (mesh.bytes != bytes) throw new AssertionError("Private page capacity differs");
          byte[] cpu = new byte[bytes]; data.get(0, cpu);
          WaterPublicationFailureChecks.field(pageType, "gpu").set(page, mesh);
          WaterPublicationFailureChecks.field(pageType, "presentation").set(page, cpu);
          pages.put(key, page); queue.add(key);
        }
      } finally { MemoryUtil.memFree(data); }
      WaterPublicationFailureChecks.field(type, "residentBytes").setLong(renderer, TOTAL);
      RenderSystem.queueFencedTask(() -> { setupMemory = memory(); ready = true; });
    }

    void turn(long id, long now) throws Exception {
      if (samples.isEmpty()) sceneBefore = scene();
      last = new LinkedHashMap<>(); last.put("frame", id); last.put("preNanos", now);
      for (String key : List.of("rangeWrites", "rangeBytes", "backendWriteNanos", "backendEncoderNanos")) last.put(key, 0L);
      WaterPublicationFailureChecks.field(type, "clock").setLong(renderer, 201L + samples.size() * 250L);
      long started = System.nanoTime();
      backend.set(device, proxy);
      try { WaterPublicationFailureChecks.call(retire, renderer); }
      finally { backend.set(device, original); }
      last.put("retireCpuNanos", System.nanoTime() - started);
      if (last.get("rangeWrites") > 32 || last.get("rangeBytes") > WaterCornerPacket.SLICE_BYTES)
        throw new AssertionError("Per-frame retirement work bound exceeded");
      samples.add(last);
    }

    void finish() throws Exception {
      readQueued = true; endMemory = memory(); sceneAfter = scene();
      if (moving ? totalBytes != TOTAL : totalBytes != 0) throw new AssertionError("Retirement write accounting differs");
      var cpuPages = new ArrayList<byte[]>();
      for (Object page : pages.values()) {
        byte[] cpu = (byte[]) WaterPublicationFailureChecks.field(pageType, "presentation").get(page);
        var mesh = (WaterGpuMesh) WaterPublicationFailureChecks.field(pageType, "gpu").get(page);
        var read = RenderSystem.getDevice().createBuffer(() -> "Frame retirement readback", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, cpu.length);
        readbacks.add(read); cpuPages.add(cpu);
        RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer) vertices.get(mesh)).slice(0, cpu.length), read.slice());
      }
      RenderSystem.queueFencedTask(() -> {
        try {
          long bad = 0; var hashes = new ArrayList<Map<String, Object>>();
          for (int n = 0; n < readbacks.size(); n++) {
            byte[] cpu = cpuPages.get(n), gpu = new byte[cpu.length];
            try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(readbacks.get(n), true, false)) { mapped.data().get(0, gpu); }
            for (int i = 0; i < gpu.length; i++) if (gpu[i] != cpu[i]) bad++;
            hashes.add(Map.of("bytes", gpu.length, "cpuSha256", WaterPublicationFailureChecks.sha(cpu), "gpuSha256", WaterPublicationFailureChecks.sha(gpu)));
          }
          if (bad != 0) throw new AssertionError("Retired GPU bytes differ: " + bad);
          for (var row : samples) if (!row.containsKey("frameCpuNanos") || !row.containsKey("frameIntervalNanos")) throw new AssertionError("Incomplete frame measurements");
          var result = new LinkedHashMap<String, Object>();
          result.put("pass", true); result.put("moving", moving); result.put("pages", PAGES); result.put("residentBytes", TOTAL);
          result.put("turns", TURNS); result.put("rangeWrites", totalWrites); result.put("rangeBytes", totalBytes); result.put("maxRangeBytes", maxRange);
          result.put("differentBytes", bad); result.put("stagingWrapPredictions", wraps); result.put("stagingEvents", stagingEvents);
          result.put("samples", samples); result.put("readbacks", hashes);
          result.put("sceneBefore", sceneBefore); result.put("sceneAfter", sceneAfter);
          result.put("beforeSetupMemory", beforeMemory); result.put("afterSetupMemory", setupMemory); result.put("endSamplesMemory", endMemory);
          for (String key : List.of("retireCpuNanos", "frameCpuNanos", "frameIntervalNanos")) {
            long[] values = samples.stream().mapToLong(row -> row.get(key)).sorted().toArray();
            result.put(key + "Median", values[values.length / 2]); result.put(key + "P95", values[(int) Math.ceil(values.length * .95) - 1]); result.put(key + "Max", values[values.length - 1]);
          }
          try (var input = WaterFrameRetirementChecks.class.getResourceAsStream("WaterFrameRetirementChecks.class")) { result.put("fixtureClassSha256", WaterPublicationFailureChecks.sha(input.readAllBytes())); }
          result.put("scope", "One call per distinct rendered frame after setup fence and five-frame settle. Private reflected non-drawn pages bypass admission/eviction. Instrumented CPU phase and frame intervals include host work/waits/frame limiting; heap/direct-pool observations omit other native allocations. Controlled +250ms clock/frame is not independent60Hz. No churn/full-scene/ownerGPU/FPS or comparative acceptance.");
          Files.writeString(output.resolve("water-frame-retirement.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
          System.out.println("WATER_FRAME_RETIREMENT_PASS moving=" + moving + " pages=" + PAGES + " turns=" + TURNS + " rangeWrites=" + totalWrites + " bytes=" + totalBytes + " differentBytes=0 wraps=" + wraps);
        } catch (Exception e) { throw new RuntimeException(e); }
        finally { active = null; close(); }
      });
    }

    public void close() {
      if (closed) return; closed = true;
      for (GpuBuffer buffer : readbacks) buffer.close();
      try { backend.set(device, original); type.getMethod("reset").invoke(renderer); ((ExecutorService) WaterPublicationFailureChecks.field(type, "workers").get(renderer)).shutdownNow(); }
      catch (Exception e) { throw new RuntimeException(e); }
    }
  }

  private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
    try { return method.invoke(target, args); } catch (InvocationTargetException e) { throw e.getCause(); }
  }
}
