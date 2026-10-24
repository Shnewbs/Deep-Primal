package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.lwjgl.system.MemoryUtil;

/** Actual extraction entry point with controlled synchronous retirement delay; never a latency benchmark. */
final class WaterExtractionDeadlineChecks {
  private static Path output;
  private static boolean expectShared, pending;
  private record Read(byte[] cpu, GpuBuffer gpu) {}
  private static final class Pending implements Future<Object> {
    int probes;
    public boolean isDone() { probes++; return false; }
    public boolean isCancelled() { return false; }
    public boolean cancel(boolean interrupt) { return true; }
    public Object get() { throw new AssertionError("Pending future must not be consumed"); }
    public Object get(long timeout, TimeUnit unit) { return get(); }
  }

  static void start(Path dir, boolean check) {
    if (pending) throw new IllegalStateException("Deadline probe already active");
    output = dir; expectShared = check; pending = true;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  @SuppressWarnings("unchecked")
  public void extract(ExtractLevelRenderStateEvent event) throws Exception {
    if (!pending) return;
    pending = false;
    var mc = Minecraft.getInstance();
    var rows = new ArrayList<Map<String, Object>>(); var reads = new ArrayList<Read>();
    var type = AnimatedWaterRenderer.class; var pageType = Class.forName(type.getName() + "$Page");
    var constructor = type.getDeclaredConstructor(); constructor.setAccessible(true);
    var pageConstructor = pageType.getDeclaredConstructor(long.class); pageConstructor.setAccessible(true);
    var vertices = WaterPublicationFailureChecks.field(WaterGpuMesh.class, "vertices");
    ContextKey<List<?>> visible = (ContextKey<List<?>>) WaterPublicationFailureChecks.field(type, "VISIBLE").get(null);
    List<?> previousVisible = event.getRenderState().getRenderData(visible);
    try {
      for (int delayMillis : new int[]{8, 12, 20}) {
        Object renderer = constructor.newInstance();
        Object device = RenderSystem.getDevice();
        Field backend = Arrays.stream(device.getClass().getDeclaredFields())
            .filter(f -> f.getType().isInterface() && f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();
        backend.setAccessible(true); Object original = backend.get(device);
        try {
          int x = mc.player.blockPosition().getX() >> 4, y = mc.player.blockPosition().getY() >> 4, z = mc.player.blockPosition().getZ() >> 4;
          long key = SectionPos.asLong(x, y, z); Object page = pageConstructor.newInstance(key);
          var data = MemoryUtil.memCalloc(144).order(ByteOrder.nativeOrder());
          WaterGpuMesh mesh;
          try {
            long pairs = 104L * (1 + 289 + 83521 + 24137569L);
            for (int p = 0; p < 144; p += 36) WaterCornerPacket.write(data, p, pairs, 0, 0, 0, 0, true);
            mesh = new WaterGpuMesh(data, 4); ((GpuBuffer) vertices.get(mesh)).close();
            vertices.set(mesh, RenderSystem.getDevice().createBuffer(() -> "Deadline readable page",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, data));
            byte[] cpu = new byte[144]; data.get(0, cpu);
            WaterPublicationFailureChecks.field(pageType, "presentation").set(page, cpu);
          } finally { MemoryUtil.memFree(data); }
          WaterPublicationFailureChecks.field(pageType, "gpu").set(page, mesh);
          ((Map<Long, Object>) WaterPublicationFailureChecks.field(type, "pages").get(renderer)).put(key, page);
          ((Set<Long>) WaterPublicationFailureChecks.field(type, "retireQueue").get(renderer)).add(key);
          WaterPublicationFailureChecks.field(type, "residentBytes").setLong(renderer, 144);
          WaterPublicationFailureChecks.field(type, "clockNanos").setLong(renderer, 201_000_000L);
          WaterPublicationFailureChecks.field(type, "level").set(renderer, mc.level);
          var futures = new ArrayList<Pending>();
          var work = (List<Future<?>>) WaterPublicationFailureChecks.field(type, "work").get(renderer);
          for (int n = 0; n < 4; n++) { var future = new Pending(); futures.add(future); work.add(future); }
          GpuBuffer target = (GpuBuffer) vertices.get(mesh); long[] writes = {0}, delayNanos = {0};
          Object proxy = Proxy.newProxyInstance(backend.getType().getClassLoader(), new Class<?>[]{backend.getType()}, (o, m, args) -> {
            Object encoder = invoke(m, original, args);
            if (!m.getName().equals("createCommandEncoder")) return encoder;
            return Proxy.newProxyInstance(m.getReturnType().getClassLoader(), new Class<?>[]{m.getReturnType()}, (e, op, a) -> {
              if (op.getName().equals("writeToBuffer") && a[0] instanceof GpuBufferSlice slice && slice.buffer() == target) {
                writes[0]++; long begin = System.nanoTime(); Thread.sleep(delayMillis); delayNanos[0] += System.nanoTime() - begin;
              }
              return invoke(op, encoder, a);
            });
          });
          long begin = System.nanoTime();
          backend.set(device, proxy);
          try { ((AnimatedWaterRenderer) renderer).extract(event); }
          finally { backend.set(device, original); event.getRenderState().setRenderData(visible, previousVisible); }
          long elapsed = System.nanoTime() - begin;
          int probes = futures.stream().mapToInt(f -> f.probes).sum();
          if (writes[0] != 1 || delayNanos[0] < 7_000_000L) throw new AssertionError("Controlled retirement delay did not execute");
          byte[] cpu = ((byte[]) WaterPublicationFailureChecks.field(pageType, "presentation").get(page)).clone();
          var read = RenderSystem.getDevice().createBuffer(() -> "Deadline readback", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, 144);
          reads.add(new Read(cpu, read)); RenderSystem.getDevice().createCommandEncoder().copyToBuffer(target.slice(0, 144), read.slice());
          var row = new LinkedHashMap<String, Object>(); row.put("delayMillis", delayMillis); row.put("observedDelayNanos", delayNanos[0]); row.put("extractNanos", elapsed);
          row.put("retirementWrites", writes[0]); row.put("pendingFutureProbesAfterRetirement", probes); rows.add(row);
        } finally {
          backend.set(device, original); event.getRenderState().setRenderData(visible, previousVisible);
          type.getMethod("reset").invoke(renderer); ((ExecutorService) WaterPublicationFailureChecks.field(type, "workers").get(renderer)).shutdownNow();
        }
      }
    } catch (Exception | Error e) { for (var read : reads) read.gpu.close(); throw e; }
    final boolean check = expectShared; final Path dir = output;
    RenderSystem.queueFencedTask(() -> {
      try {
        long bad = 0; var hashes = new ArrayList<Map<String, Object>>(); var raw = new java.io.ByteArrayOutputStream();
        for (var read : reads) {
          byte[] gpu = new byte[144]; try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(read.gpu, true, false)) { mapped.data().get(0, gpu); }
          for (int n = 0; n < 144; n++) if (gpu[n] != read.cpu[n]) bad++;
          raw.writeBytes(read.cpu); raw.writeBytes(gpu);
          hashes.add(Map.of("bytes", 144, "cpuSha256", WaterPublicationFailureChecks.sha(read.cpu), "gpuSha256", WaterPublicationFailureChecks.sha(gpu)));
        }
        boolean shared = rows.stream().allMatch(r -> ((Integer) r.get("pendingFutureProbesAfterRetirement")) == 0);
        if (bad != 0) throw new AssertionError("Deadline probe GPU data differs");
        var result = new LinkedHashMap<String, Object>(); result.put("sharedBudget", shared); result.put("expectedShared", check); result.put("cases", rows);
        result.put("readbacks", hashes); result.put("differentBytes", bad);
        try (var input = WaterExtractionDeadlineChecks.class.getResourceAsStream("WaterExtractionDeadlineChecks.class")) { result.put("fixtureClassSha256", WaterPublicationFailureChecks.sha(input.readAllBytes())); }
        result.put("scope", "Real production extraction with injected8/12/20ms synchronous retirement delay and four pending futures. Admission-counter regression, not ordinary performance or a hard total extraction-time bound. Visible render state restored; private pages reset; no world edits.");
        Files.write(dir.resolve("water-deadline-readbacks.bin"), raw.toByteArray());
        Files.writeString(dir.resolve("water-deadline-result.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
        System.out.println("WATER_DEADLINE_RESULT sharedBudget=" + shared + " cases=3 differentBytes=0 expectedShared=" + check);
        if (check && !shared) throw new AssertionError("Optional work received fresh budget after over-budget retirement");
        if (check) System.out.println("WATER_DEADLINE_NATIVE_PASS cases=3 pendingFutureProbes=0 retirementPreserved=true");
      } catch (Exception e) { throw new RuntimeException(e); }
      finally { for (var read : reads) read.gpu.close(); }
    });
  }

  private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
    try { return method.invoke(target, args); } catch (InvocationTargetException e) { throw e.getCause(); }
  }
}
