package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.SectionPos;
import org.lwjgl.system.MemoryUtil;

/** Reads real GPU vertices after staggered admission of the two sides of a cliff ledge. */
final class WaterCliffContinuityChecks {
  private record Readback(GpuBuffer data, int scenario, long time, int y, long uploaded) {}

  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var result = type.getDeclaredField(name);
    result.setAccessible(true);
    return result;
  }

  private static WaterPatchMesher.Snapshot snapshot(int origin, int upper, int amount) {
    byte[] water = new byte[WaterPatchMesher.CELLS], solid = new byte[water.length];
    int[] lights = new int[water.length], colors = new int[256];
    Arrays.fill(lights, 0x00f000f0);
    Arrays.fill(colors, 0xffffffff);
    for (int z = -1; z <= 16; z++) for (int x = -1; x <= 16; x++) {
      int surface = x < 8 ? upper : upper - 1;
      for (int y = -1; y <= 16; y++) {
        int at = WaterPatchMesher.index(x, y, z);
        if (origin + y < surface) solid[at] = 1;
        else if (origin + y == surface) water[at] = (byte) amount;
      }
    }
    return new WaterPatchMesher.Snapshot(water, solid, lights, colors);
  }

  private static void readable(Object page, Class<?> pageType) throws ReflectiveOperationException {
    var gpu = (WaterGpuMesh) field(pageType, "gpu").get(page);
    var vertices = field(WaterGpuMesh.class, "vertices");
    var old = (GpuBuffer) vertices.get(gpu);
    byte[] accepted = (byte[]) field(pageType, "presentation").get(page);
    var data = MemoryUtil.memAlloc(accepted.length);
    try {
      data.put(accepted).flip();
      vertices.set(gpu, RenderSystem.getDevice().createBuffer(() -> "Readable cliff vertices",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, data));
    } finally {
      MemoryUtil.memFree(data);
    }
    old.close();
  }

  static CompletableFuture<Void> run(Path output) throws ReflectiveOperationException {
    var result = new CompletableFuture<Void>();
    var reads = new ArrayList<Readback>();
    var type = AnimatedWaterRenderer.class;
    var ctor = type.getDeclaredConstructor(); ctor.setAccessible(true);
    var renderer = ctor.newInstance();
    var pt = Class.forName(type.getName() + "$Page");
    var pc = pt.getDeclaredConstructor(long.class); pc.setAccessible(true);
    var rt = Class.forName(type.getName() + "$Ready");
    var rc = rt.getDeclaredConstructor(long.class, long.class, pt,
        WaterPatchMesher.Snapshot.class, WaterPatchMesher.Mesh.class); rc.setAccessible(true);
    var upload = type.getDeclaredMethod("upload", rt); upload.setAccessible(true);
    var sync = type.getDeclaredMethod("synchronizeVisibleHeights", List.class); sync.setAccessible(true);
    boolean queued = false;
    int scenario = 0;
    try {
      for (int upper : new int[] {-16, 0, 16})
        for (boolean smooth : new boolean[] {false, true})
          for (int first : new int[] {0, 1}) {
            renderer.reset();
            long session = field(type, "session").getLong(renderer), revision = 0;
            @SuppressWarnings("unchecked")
            var pages = (Map<Long, Object>) field(type, "pages").get(renderer);
            Object[] pair = {pc.newInstance(SectionPos.asLong(0, (upper - 16) >> 4, 0)),
                pc.newInstance(SectionPos.asLong(0, upper >> 4, 0))};
            var prior = new WaterPatchMesher.Snapshot[2];
            for (int n = 0; n < 2; n++) {
              int y = field(pt, "y").getInt(pair[n]);
              pages.put(field(pt, "key").getLong(pair[n]), pair[n]);
              prior[n] = snapshot(y, upper, 8);
              field(pt, "changed").setBoolean(pair[n], false);
              field(type, "clock").setLong(renderer, 0);
              upload.invoke(renderer, rc.newInstance(session, ++revision, pair[n], prior[n],
                  WaterPatchMesher.build(prior[n], prior[n], smooth)));
              readable(pair[n], pt);
            }
            for (long time : new long[] {0, 100, 125, 150, 175, 200, 250, 300, 325, 350, 375, 400, 450}) {
              field(type, "clock").setLong(renderer, time);
              if (time == 100 || time == 150 || time == 300 || time == 350) {
                int n = time == 100 || time == 300 ? first : 1 - first;
                int amount = time < 300 ? 16 : 8;
                int y = field(pt, "y").getInt(pair[n]);
                var next = snapshot(y, upper, amount);
                upload.invoke(renderer, rc.newInstance(session, ++revision, pair[n], next,
                    WaterPatchMesher.build(next, prior[n], smooth)));
                prior[n] = next;
              }
              sync.invoke(renderer, Arrays.asList(pair));
              for (Object p : pair) {
                var gpu = (WaterGpuMesh) field(pt, "gpu").get(p);
                var gpuSource = (GpuBuffer) field(WaterGpuMesh.class, "vertices").get(gpu);
                int size = ((byte[]) field(pt, "presentation").get(p)).length;
                var read = RenderSystem.getDevice().createBuffer(() -> "Cliff edge readback",
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, size);
                reads.add(new Readback(read, scenario, time, field(pt, "y").getInt(p),
                    field(pt, "uploaded").getLong(p)));
                RenderSystem.getDevice().createCommandEncoder().copyToBuffer(gpuSource.slice(0, size), read.slice());
              }
            }
            scenario++;
          }
      final int scenarios = scenario;
      RenderSystem.queueFencedTask(() -> {
        int comparisons = 0, witnesses = 0;
        float maximumGap = 0;
        var previous = new HashMap<String, Float>();
        try {
          for (var read : reads) {
            int found = 0;
            try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(read.data, true, false)) {
              var data = mapped.data().order(ByteOrder.nativeOrder());
              for (int p = 0; p < data.limit(); p += 36) {
                if (!WaterCornerPacket.packed(data, p) || data.getFloat(p) != 8
                    || data.getFloat(p + 8) != 8) continue;
                int age = (byte) data.getShort(p + 26);
                float t = Math.clamp((read.time - read.uploaded - age) / 100f, 0, 1);
                float height = read.y + data.getFloat(p + 4) + data.getShort(p + 24) / 16384f * (1 - t);
                if (read.time == 0 || read.time == 250 || read.time == 450) {
                  float expected = -16 + (read.scenario / 4) * 16
                      + (read.time == 250 ? .5f : 1 / 256f);
                  if (Math.abs(height - expected) > .0001f)
                    throw new AssertionError("Cliff surface did not reach its authoritative endpoint");
                }
                String key = read.scenario + ":" + read.time;
                Float before = previous.putIfAbsent(key, height);
                if (before != null) {
                  maximumGap = Math.max(maximumGap, Math.abs(before - height));
                  comparisons++;
                  if (Math.abs(before - height) > 1 / 16384f + .000001f)
                    throw new AssertionError("Asynchronous cliff seam: scenario=" + read.scenario
                        + " time=" + read.time + " first=" + before + " other=" + height
                        + " gap=" + Math.abs(before - height));
                }
                found++; witnesses++;
              }
            }
            if (found == 0) throw new AssertionError("No GPU cliff corner witnesses");
          }
          String receipt = "WATER_CLIFF_GPU_CONTINUITY_PASS scenarios=" + scenarios + " readbacks=" + reads.size()
              + " comparisons=" + comparisons + " witnesses=" + witnesses + " maximumGap=" + maximumGap
              + " framesPerScenario=13 skewMillis=50 signedPages=3 uploadOrders=2 smoothModes=2\n";
          Files.createDirectories(output);
          Files.writeString(output.resolve("water-cliff-continuity.txt"), receipt);
          System.out.print(receipt);
          result.complete(null);
        } catch (Exception | AssertionError failure) {
          result.completeExceptionally(failure);
          throw new RuntimeException("Cliff water continuity validation failed", failure);
        } finally {
          for (var read : reads) read.data.close();
        }
      });
      queued = true;
    } finally {
      renderer.reset();
      ((ExecutorService) field(type, "workers").get(renderer)).shutdownNow();
      if (!queued) for (var read : reads) read.data.close();
    }
    return result;
  }

  private WaterCliffContinuityChecks() {}
}
