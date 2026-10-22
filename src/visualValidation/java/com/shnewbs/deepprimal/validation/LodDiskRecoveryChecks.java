package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.lod.*;
import com.shnewbs.deepprimal.settings.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;

/** Isolated renderer instance, real disk/worker/GPU; never changes authoritative chunks. */
final class LodDiskRecoveryChecks {
  private static Field field(Class<?> type, String name) throws Exception {
    var f = type.getDeclaredField(name); f.setAccessible(true); return f;
  }

  static void run(Minecraft mc, Path output) throws Exception {
    String backend = RenderSystem.getDevice().getBackendName();
    if (!backend.equals("Vulkan")) throw new AssertionError("Actual Vulkan required: " + backend);
    var type = Class.forName("com.shnewbs.deepprimal.lod.client.VoxelLodRenderer");
    System.out.println("VALIDATION_CLASS_ORIGIN class=" + type.getName()
        + " location=" + type.getProtectionDomain().getCodeSource().getLocation());
    var constructor = type.getDeclaredConstructor(); constructor.setAccessible(true);
    Object renderer = constructor.newInstance();
    var start = type.getDeclaredMethod("startDiskJob"); start.setAccessible(true);
    var upload = type.getDeclaredMethod("processUploads"); upload.setAccessible(true);
    var reset = type.getMethod("reset");
    var worker = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
    var oldPosition = mc.player.position();
    int radius = LodConfig.RADIUS.get(); boolean persist = LodConfig.PERSIST.get();
    boolean pause = ClientPerformanceConfig.PAUSE_BACKGROUND.get();
    long diskReads = 0, uploaded = 0, retained = 0; boolean restored = false;
    try {
      int x = mc.player.chunkPosition().x() + 96, z = mc.player.chunkPosition().z();
      long key = ((long) x << 32) ^ (z & 0xffffffffL);
      var store = new LodStore(output.resolve("isolated-disk-cache"));
      int[] offsets = new int[257], runs = new int[256 * 3];
      for (int c = 0; c < 256; c++) {
        offsets[c] = c * 3; runs[c * 3] = 192; runs[c * 3 + 1] = 193;
      }
      offsets[256] = runs.length;
      store.write(new ColumnarChunk(x, z, 192, 1, 7,
          List.of("ff777777|minecraft:stone"), offsets, runs));
      field(type, "worker").set(renderer, worker);
      field(type, "store").set(renderer, store);
      @SuppressWarnings("unchecked") var disk = (ArrayDeque<Long>) field(type, "disk").get(renderer);
      disk.addLast(key);
      LodConfig.RADIUS.set(16); LodConfig.PERSIST.set(true);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      if ((Boolean) start.invoke(renderer)) throw new AssertionError("Out-of-range disk read");
      retained = disk.size();
      boolean started;
      try {
        mc.player.setPos(x * 16. + .5, oldPosition.y, z * 16. + .5);
        started = (Boolean) start.invoke(renderer);
      } finally { mc.player.setPos(oldPosition); }
      System.out.println("LOD_DISK_RECOVERY_OBSERVED retainedAfterOutside=" + retained
          + " startedAfterMove=" + started);
      if (retained != 1 || !started)
        throw new AssertionError("Out-of-range cache must remain eligible after a camera move");
      var future = (Future<?>) field(type, "future").get(renderer);
      Object ready = future.get(5, TimeUnit.SECONDS);
      var pendingType = Class.forName(type.getName() + "$PendingGpu");
      var pendingConstructor = pendingType.getDeclaredConstructor(ready.getClass());
      pendingConstructor.setAccessible(true);
      field(type, "pendingGpu").set(renderer, pendingConstructor.newInstance(ready));
      for (int turn = 0; turn < 16 && field(type, "pendingGpu").get(renderer) != null; turn++)
        upload.invoke(renderer);
      diskReads = type.getField("diskReads").getLong(renderer);
      uploaded = type.getField("uploadedChunks").getLong(renderer);
      restored = ((Map<?, ?>) field(type, "cache").get(renderer)).containsKey(key);
      if (diskReads != 1 || uploaded != 1 || !restored
          || type.getField("residentBytes").getLong(renderer) <= 0)
        throw new AssertionError("Disk page did not complete real GPU upload");
    } finally {
      mc.player.setPos(oldPosition);
      LodConfig.RADIUS.set(radius); LodConfig.PERSIST.set(persist);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(pause);
      reset.invoke(renderer); worker.shutdownNow();
    }
    if (type.getField("residentBytes").getLong(renderer) != 0)
      throw new AssertionError("Isolated GPU pages were not released");
    String record = "LOD_DISK_RECOVERY_NATIVE_PASS backend=" + backend
        + " retainedAfterOutside=" + retained + " diskReads=" + diskReads
        + " uploadedChunks=" + uploaded + " restored=" + restored
        + " isolatedGpuReleased=true scope=isolatedRendererRealStoreGpu\n";
    Files.writeString(output.resolve("lod-disk-recovery.txt"), record);
    System.out.print(record);
  }

  private LodDiskRecoveryChecks() {}
}
