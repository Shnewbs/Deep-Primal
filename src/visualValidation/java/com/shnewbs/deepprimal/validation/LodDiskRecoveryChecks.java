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
    int memory = LodConfig.MEMORY.get();
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
      field(type, "root").set(renderer, output.resolve("isolated-disk-cache"));
      @SuppressWarnings("unchecked") var disk = (Collection<Long>) field(type, "disk").get(renderer);
      disk.add(key);
      LodConfig.RADIUS.set(16); LodConfig.PERSIST.set(true); LodConfig.MEMORY.set(4);
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
      if (!((Map<?, ?>) field(type, "cache").get(renderer)).containsKey(key))
        throw new AssertionError("Initial persisted page must reach GPU before eviction");
      // Entry-count pressure calls the same production eviction path as payload pressure.
      var evict = type.getDeclaredMethod("evictToFit", long.class, int.class, Long.class,
          net.minecraft.world.phys.Vec3.class); evict.setAccessible(true);
      field(type, "future").set(renderer, null);
      field(type, "workingKey").set(renderer, null);
      for (int cycle = 0; cycle < 4; cycle++) {
        long beforeBytes = type.getField("residentBytes").getLong(renderer);
        long reservation = (cycle % 2 == 0) ? 0L : 4L * 1048576 - beforeBytes + 1;
        evict.invoke(renderer, reservation, cycle % 2 == 0 ? 0 : 4096, null, oldPosition);
        boolean indexed = disk.contains(key);
        System.out.println("LOD_EVICTION_RECOVERY_OBSERVED cycle=" + cycle
            + " indexed=" + indexed + " resident=" + type.getField("residentBytes").getLong(renderer));
        if (!indexed || type.getField("residentBytes").getLong(renderer) != 0)
          throw new AssertionError("Evicted persisted page must remain scheduled for recovery");
        if ((Boolean) start.invoke(renderer)) throw new AssertionError("Evicted far page read outside radius");
        try {
          mc.player.setPos(x * 16. + .5, oldPosition.y, z * 16. + .5);
          if (!(Boolean) start.invoke(renderer)) throw new AssertionError("Evicted page cannot restart in range");
        } finally { mc.player.setPos(oldPosition); }
        future = (Future<?>) field(type, "future").get(renderer);
        ready = future.get(5, TimeUnit.SECONDS);
        field(type, "pendingGpu").set(renderer, pendingConstructor.newInstance(ready));
        field(type, "future").set(renderer, null);
        field(type, "workingKey").set(renderer, null);
        for (int turn = 0; turn < 16 && field(type, "pendingGpu").get(renderer) != null; turn++)
          upload.invoke(renderer);
        if (!((Map<?, ?>) field(type, "cache").get(renderer)).containsKey(key))
          throw new AssertionError("Evicted page did not regain GPU residency");
      }
      // Real GPU ballast fills the 4 MiB payload budget; releasing it must wake the same view.
      long pageBytes = type.getField("residentBytes").getLong(renderer);
      long ballastBytes = 4L * 1048576 - Math.max(1, pageBytes / 2);
      if (pageBytes <= 0 || ballastBytes + pageBytes <= 4L * 1048576)
        throw new AssertionError("Pressure fixture must exceed the budget with its target page");
      try {
        mc.player.setPos(x * 16. + .5, oldPosition.y, z * 16. + .5);
        evict.invoke(renderer, ballastBytes, 4095, null, mc.player.position().add(32, 0, 0));
        var ballastBuffer = RenderSystem.getDevice().createBuffer(() -> "LOD recovery pressure ballast",
            com.mojang.blaze3d.buffers.GpuBuffer.USAGE_VERTEX, ballastBytes);
        boolean cacheOwnsBallast = false;
        try {
          var ballast = new com.shnewbs.deepprimal.graphics.TerrainGpuMesh(x + 1, z, 192, 193,
              ballastBuffer, null, 3, 0, 0, new short[0]);
          var entryType = Class.forName(type.getName() + "$Entry");
          var entryConstructor = entryType.getDeclaredConstructor(long.class, List.class, long.class,
              int[].class, int.class, boolean.class); entryConstructor.setAccessible(true);
          @SuppressWarnings("unchecked") var cache = (Map<Long, Object>) field(type, "cache").get(renderer);
          Object entry = entryConstructor.newInstance(0L,
              List.of(List.of(ballast), List.of(), List.of()), ballast.byteSize, new int[] {0}, 193, false);
          cache.put(key + (1L << 32), entry); cacheOwnsBallast = true;
          type.getField("residentBytes").setLong(renderer, ballast.byteSize);
          if (ballast.byteSize != ballastBytes || ballastBytes > 4L * 1048576)
            throw new AssertionError("Actual GPU payload accounting differs from ballast allocation");
          long beforeReads = type.getField("diskReads").getLong(renderer);
          for (int turn = 0; turn < 16; turn++)
            if ((Boolean) start.invoke(renderer)) throw new AssertionError("Fixed-view read/upload churn under pressure");
          if (type.getField("diskReads").getLong(renderer) != beforeReads)
            throw new AssertionError("Deferred page performed disk work without capacity");
          evict.invoke(renderer, 0L, 0, null, mc.player.position());
          if (type.getField("residentBytes").getLong(renderer) != 0)
            throw new AssertionError("Closing the real ballast did not free all GPU payload");
          boolean recovered = (Boolean) start.invoke(renderer);
          System.out.println("LOD_CAPACITY_RECOVERY_OBSERVED pageBytes=" + pageBytes
              + " ballastBytes=" + ballastBytes + " fixedView=true recovered=" + recovered);
          if (!recovered) throw new AssertionError("Freed GPU capacity must wake a deferred page without camera or budget changes");
        } finally { if (!cacheOwnsBallast) ballastBuffer.close(); }
      } finally { mc.player.setPos(oldPosition); }
      future = (Future<?>) field(type, "future").get(renderer);
      ready = future.get(5, TimeUnit.SECONDS);
      field(type, "pendingGpu").set(renderer, pendingConstructor.newInstance(ready));
      field(type, "future").set(renderer, null); field(type, "workingKey").set(renderer, null);
      for (int turn = 0; turn < 16 && field(type, "pendingGpu").get(renderer) != null; turn++) upload.invoke(renderer);
      System.out.println("LOD_FIXED_VIEW_CAPACITY_PASS idleCalls=16 noDiskWorkUnderPressure=true freedCapacityRecovery=true actualGpuBallast=true");
      diskReads = type.getField("diskReads").getLong(renderer);
      uploaded = type.getField("uploadedChunks").getLong(renderer);
      restored = ((Map<?, ?>) field(type, "cache").get(renderer)).containsKey(key);
      if (diskReads != 6 || uploaded != 6 || !restored
          || type.getField("residentBytes").getLong(renderer) <= 0)
        throw new AssertionError("Disk page did not complete real GPU upload");
      // Do not resurrect persisted data over an edit, or advertise an unwritten snapshot.
      @SuppressWarnings("unchecked") var latest = (Map<Long, Long>) field(type, "latest").get(renderer);
      @SuppressWarnings("unchecked") var dirty = (Map<Long, Long>) field(type, "dirty").get(renderer);
      var revisionAccessor = ready.getClass().getDeclaredMethod("revision"); revisionAccessor.setAccessible(true);
      long revision = (Long) revisionAccessor.invoke(ready);
      for (String control : List.of("dirty", "newer-revision", "not-persisted", "persistence-disabled")) {
        latest.clear(); dirty.clear(); disk.clear();
        Object controlReady = ready;
        if (control.equals("not-persisted")) {
          var components = ready.getClass().getRecordComponents();
          var parameterTypes = java.util.Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
          var values = new Object[components.length];
          for (int i = 0; i < components.length; i++) {
            var accessor = components[i].getAccessor(); accessor.setAccessible(true); values[i] = accessor.invoke(ready);
          }
          values[values.length - 1] = false;
          var readyConstructor = ready.getClass().getDeclaredConstructor(parameterTypes); readyConstructor.setAccessible(true);
          controlReady = readyConstructor.newInstance(values);
        }
        if (!control.equals("dirty")) {
          field(type, "pendingGpu").set(renderer, pendingConstructor.newInstance(controlReady));
          for (int turn = 0; turn < 16 && field(type, "pendingGpu").get(renderer) != null; turn++)
            upload.invoke(renderer);
        }
        if (control.equals("dirty")) dirty.put(key, revision + 1);
        if (control.equals("newer-revision")) latest.put(key, revision + 1);
        if (control.equals("persistence-disabled")) LodConfig.PERSIST.set(false);
        evict.invoke(renderer, 0L, 0, null, oldPosition);
        LodConfig.PERSIST.set(true);
        if (disk.contains(key) || type.getField("residentBytes").getLong(renderer) != 0)
          throw new AssertionError("Invalid recovery admission: " + control);
        System.out.println("LOD_EVICTION_ADMISSION_PASS control=" + control);
      }
      // Simulate dirty-capacity displacement after an edit to an already queued disk key.
      latest.clear(); dirty.clear(); disk.clear(); disk.add(key); latest.put(key, revision + 1);
      var forget = type.getDeclaredMethod("forgetUnused", long.class); forget.setAccessible(true);
      forget.invoke(renderer, key);
      if (latest.getOrDefault(key, -1L) != revision + 1)
        throw new AssertionError("Queued edit revision was forgotten after dirty displacement");
      try {
        mc.player.setPos(x * 16. + .5, oldPosition.y, z * 16. + .5);
        if (!(Boolean) start.invoke(renderer)) throw new AssertionError("Queued stale read not scheduled for validation");
      } finally { mc.player.setPos(oldPosition); }
      var staleFuture = (Future<?>) field(type, "future").get(renderer);
      Object staleReady = staleFuture.get(5, TimeUnit.SECONDS);
      var obsolete = type.getDeclaredMethod("obsolete", staleReady.getClass()); obsolete.setAccessible(true);
      if (!(Boolean) obsolete.invoke(renderer, staleReady)) throw new AssertionError("Stale disk revision accepted over edit");
      field(type, "pendingGpu").set(renderer, pendingConstructor.newInstance(staleReady));
      upload.invoke(renderer);
      field(type, "future").set(renderer, null); field(type, "workingKey").set(renderer, null);
      forget.invoke(renderer, key);
      if (field(type, "pendingGpu").get(renderer) != null || latest.containsKey(key)
          || type.getField("residentBytes").getLong(renderer) != 0)
        throw new AssertionError("Rejected stale restoration retained resources or metadata");
      System.out.println("LOD_QUEUED_EDIT_GUARD_PASS dirtyDisplacement=true staleReadRejected=true metadataReleased=true");
      // Exercise the actual immutable native-chunk capture, worker and successful store write.
      latest.clear(); dirty.clear(); disk.clear();
      field(type, "level").set(renderer, mc.level);
      int nativeX = mc.player.chunkPosition().x(), nativeZ = mc.player.chunkPosition().z();
      type.getMethod("dirty", int.class, int.class).invoke(renderer, nativeX, nativeZ);
      var tick = type.getMethod("tick", net.neoforged.neoforge.client.event.ClientTickEvent.Post.class);
      for (int turn = 0; turn < 64 && field(type, "future").get(renderer) == null; turn++)
        tick.invoke(renderer, (Object) null);
      var captureFuture = (Future<?>) field(type, "future").get(renderer);
      if (captureFuture == null) throw new AssertionError("Real loaded chunk capture was not scheduled");
      Object capturedReady = captureFuture.get(10, TimeUnit.SECONDS);
      for (String component : List.of("captured", "persisted")) {
        var accessor = capturedReady.getClass().getDeclaredMethod(component); accessor.setAccessible(true);
        if (!(Boolean) accessor.invoke(capturedReady)) throw new AssertionError("Capture provenance missing: " + component);
      }
      var capturedRevision = capturedReady.getClass().getDeclaredMethod("revision"); capturedRevision.setAccessible(true);
      if (store.read(nativeX, nativeZ).revision != (Long) capturedRevision.invoke(capturedReady))
        throw new AssertionError("Captured revision did not reach the real store");
      System.out.println("LOD_CAPTURE_PERSISTENCE_PASS realNativeChunk=true storedRevisionMatches=true");
    } finally {
      mc.player.setPos(oldPosition);
      LodConfig.RADIUS.set(radius); LodConfig.PERSIST.set(persist); LodConfig.MEMORY.set(memory);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(pause);
      reset.invoke(renderer); worker.shutdownNow();
    }
    if (type.getField("residentBytes").getLong(renderer) != 0)
      throw new AssertionError("Isolated GPU pages were not released");
    String record = "LOD_DISK_RECOVERY_NATIVE_PASS backend=" + backend
        + " retainedAfterOutside=" + retained + " diskReads=" + diskReads
        + " uploadedChunks=" + uploaded + " restored=" + restored
        + " evictionCycles=4 fixedViewIdleCalls=16 freedCapacityRecovery=true actualGpuBallast=true editPersistenceControls=4 nativeCapturePersisted=true queuedEditGuard=true isolatedGpuReleased=true scope=isolatedRendererRealStoreGpu\n";
    Files.writeString(output.resolve("lod-disk-recovery.txt"), record);
    System.out.print(record);
  }

  private LodDiskRecoveryChecks() {}
}
