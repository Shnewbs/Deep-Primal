package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.minecraft.core.SectionPos;

/** Actual production upload/readback, including adjacent uncapped surface histories. */
final class WaterfallUploadChecks {
  private record Readback(GpuBuffer buffer, WaterPatchMesher.Mesh mesh, int pageY, int amount) {}

  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var result = type.getDeclaredField(name);
    result.setAccessible(true);
    return result;
  }

  private static WaterPatchMesher.Snapshot snapshot(int amount, int direction) {
    byte[] water = new byte[WaterPatchMesher.CELLS], solid = new byte[water.length];
    byte[] flow = new byte[water.length];
    int[] lights = new int[water.length], colors = new int[256];
    Arrays.fill(flow, (byte) direction);
    Arrays.fill(colors, 0xffffffff);
    Arrays.fill(lights, 0x00f000f0);
    for (int y = -1; y <= 16; y++) water[WaterPatchMesher.index(3, y, 3)] = (byte) amount;
    // This free surface shares a corner with a clipped wall in the adjacent column.
    water[WaterPatchMesher.index(4, 15, 3)] = (byte) amount;
    water[WaterPatchMesher.index(4, 6, 3)] = (byte) amount;
    // Supported one-block rock ledge, separate from the falling-column controls.
    for (int z = 6; z <= 11; z++)
      for (int x = 8; x <= 13; x++) {
        boolean high = x < 11;
        int surface = high ? 8 : 7;
        for (int y = -1; y < surface; y++) solid[WaterPatchMesher.index(x, y, z)] = 1;
        water[WaterPatchMesher.index(x, surface, z)] = (byte) amount;
      }
    return new WaterPatchMesher.Snapshot(water, solid, lights, colors, flow);
  }

  static CompletableFuture<Void> run(Path output) throws ReflectiveOperationException {
    var result = new CompletableFuture<Void>();
    var reads = new ArrayList<Readback>();
    var type = AnimatedWaterRenderer.class;
    var constructor = type.getDeclaredConstructor();
    constructor.setAccessible(true);
    var renderer = constructor.newInstance();
    var pageType = Class.forName(type.getName() + "$Page");
    var pageConstructor = pageType.getDeclaredConstructor(long.class);
    pageConstructor.setAccessible(true);
    var readyType = Class.forName(type.getName() + "$Ready");
    var readyConstructor = readyType.getDeclaredConstructor(long.class, long.class, pageType,
        WaterPatchMesher.Snapshot.class, WaterPatchMesher.Mesh.class);
    readyConstructor.setAccessible(true);
    var upload = type.getDeclaredMethod("upload", readyType);
    upload.setAccessible(true);
    boolean queued = false;
    try {
      for (boolean smooth : new boolean[] {false, true})
        for (int direction : new int[] {16, 0})
          for (int pageY : new int[] {-16, 0, 16}) {
            renderer.reset();
            long session = field(type, "session").getLong(renderer);
            long key = SectionPos.asLong(-1, pageY >> 4, -1);
            var page = pageConstructor.newInstance(key);
            if (field(pageType, "y").getInt(page) != pageY)
              throw new AssertionError("Waterfall fixture section origin differs");
            @SuppressWarnings("unchecked")
            var pages = (Map<Long, Object>) field(type, "pages").get(renderer);
            pages.put(key, page);
            var previous = snapshot(1, direction);
            long revision = 0;
            for (int amount : new int[] {1, 8, 16, 8, 1}) {
              var current = snapshot(amount, direction);
              var mesh = WaterPatchMesher.build(current, previous, smooth);
              field(type, "clock").setLong(renderer, revision * 25);
              field(pageType, "changed").setBoolean(page, false);
              upload.invoke(renderer, readyConstructor.newInstance(session, ++revision, page, current, mesh));
              var gpu = (WaterGpuMesh) field(pageType, "gpu").get(page);
              if (gpu == null) throw new AssertionError("Waterfall upload was not admitted");
              var vertices = field(WaterGpuMesh.class, "vertices");
              var original = (GpuBuffer) vertices.get(gpu);
              // Keep the production layout/capacity, adding only readback capability.
              if (revision == 1) {
                var readable = RenderSystem.getDevice().createBuffer(() -> "Readable waterfall vertices",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, gpu.bytes);
                original.close();
                vertices.set(gpu, readable);
                field(type, "clock").setLong(renderer, 0);
                upload.invoke(renderer, readyConstructor.newInstance(session, revision, page, current, mesh));
                original = readable;
              }
              var read = RenderSystem.getDevice().createBuffer(() -> "Waterfall readback",
                  GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, mesh.count() * 36L);
              reads.add(new Readback(read, mesh, pageY, amount));
              RenderSystem.getDevice().createCommandEncoder()
                  .copyToBuffer(original.slice(0, mesh.count() * 36L), read.slice());
              previous = current;
            }
          }
      RenderSystem.queueFencedTask(() -> {
        int wallVertices = 0, freeVertices = 0, ledgeVertices = 0;
        try {
          for (var read : reads)
            try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(read.buffer, true, false)) {
              var data = mapped.data().order(ByteOrder.nativeOrder());
              var mesh = read.mesh.vertices();
              float[] ledgeTarget = {Float.NaN, Float.NaN};
              float[] ledgeFrom = {Float.NaN, Float.NaN};
              for (int vertex = 0; vertex < read.mesh.count(); vertex++) {
                int source = vertex * WaterPatchMesher.STRIDE, target = vertex * 36;
                int cell = (int) mesh[source + 9], cy = cell >> 8;
                int face = WaterPatchMesher.face(mesh[source + 10]);
                float height = data.getFloat(target + 4);
                if (Math.abs(height - (mesh[source + 1] + mesh[source + 3])) > 0.00001)
                  throw new AssertionError("Upload changed waterfall mesh target");
                if ((cell & 15) == 3 && face >= 1 && face <= 4) {
                  float previousHeight = height + data.getShort(target + 24) / 16384F;
                  if (height > cy + 1.00001 || previousHeight > cy + 1.00001
                      || height < cy || previousHeight < cy)
                    throw new AssertionError("Animated waterfall wall overlaps its cell at pageY=" + read.pageY);
                  wallVertices++;
                } else if (face == 0 && height > cy + 1) freeVertices++;
                if (face == 0 && mesh[source] == 11 && mesh[source + 2] == 8
                    && (cy == 7 || cy == 8)) {
                  float expected = 8 + Math.max(1 / 256f, -.5f + read.amount / 16f);
                  if (Math.abs(height - expected) > .00001f)
                    throw new AssertionError("Rock ledge GPU target differs from shared edge");
                  int side = cy - 7;
                  ledgeTarget[side] = height;
                  ledgeFrom[side] = height + data.getShort(target + 24) / 16384f;
                  ledgeVertices++;
                }
              }
              if (!Float.isFinite(ledgeTarget[0]) || !Float.isFinite(ledgeTarget[1])
                  || Math.abs(ledgeTarget[0] - ledgeTarget[1]) > .00001f
                  || Math.abs(ledgeFrom[0] - ledgeFrom[1]) > .0001f)
                throw new AssertionError("Rock ledge GPU interpolation endpoints disconnect");
            }
          if (wallVertices == 0 || freeVertices == 0 || ledgeVertices == 0)
            throw new AssertionError("Waterfall controls did not exercise walls and uncapped shared surfaces");
          String record = "WATERFALL_UPLOAD_PASS samples=" + reads.size() + " wallVertices=" + wallVertices
              + " rockLedgeVertices=" + ledgeVertices + " uncappedSurfaceVertices=" + freeVertices
              + " smoothModes=2 directions=2 verticalPages=3 repeatedUpload=true scope=productionGpuReadback\n";
          Files.createDirectories(output);
          Files.writeString(output.resolve("waterfall-upload-pass.txt"), record);
          System.out.print(record);
          result.complete(null);
        } catch (Exception | AssertionError failure) {
          result.completeExceptionally(failure);
          throw new RuntimeException("Waterfall GPU upload validation failed", failure);
        } finally {
          for (var read : reads) read.buffer.close();
        }
      });
      queued = true;
    } finally {
      renderer.reset();
      ((ExecutorService) field(type, "workers").get(renderer)).shutdownNow();
      if (!queued) for (var read : reads) read.buffer.close();
    }
    return CompletableFuture.allOf(result, WaterCliffContinuityChecks.run(output));
  }

  private WaterfallUploadChecks() {}
}
