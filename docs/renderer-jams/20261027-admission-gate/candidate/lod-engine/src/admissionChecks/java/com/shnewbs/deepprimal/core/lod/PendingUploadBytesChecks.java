package com.shnewbs.deepprimal.core.lod;

import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import com.sun.management.ThreadMXBean;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.*;
import sun.misc.Unsafe;

/** CPU admission accounting only. Uninitialized mesh holders contain no GPU resources. */
public final class PendingUploadBytesChecks {
  private static volatile long sink;
  private static final Unsafe unsafe;
  static {
    try {
      var field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (Unsafe) field.get(null);
    } catch (ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
  }
  private static TerrainGpuMesh holder(long bytes) throws Exception {
    var mesh = (TerrainGpuMesh) unsafe.allocateInstance(TerrainGpuMesh.class);
    mesh.byteSize = bytes;
    return mesh;
  }
  private static long oracle(List<List<TerrainGpuMesh>> levels) {
    long sum = 0;
    for (var level : levels) for (var mesh : level) sum = Math.addExact(sum, mesh.byteSize);
    return sum;
  }
  private static void check(MethodHandle query, List<List<TerrainGpuMesh>> levels) throws Throwable {
    long actual = (long) query.invokeExact();
    if (actual != oracle(levels)) throw new AssertionError("Wrong resident bytes: " + actual);
  }
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Throwable {
    LodReductionChecks.main(args);
    var type = Class.forName("com.shnewbs.deepprimal.lod.client.VoxelLodRenderer$PendingGpu");
    Path actual = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
    if (!actual.equals(Path.of(args[0]).toRealPath())) throw new AssertionError("Wrong product class " + actual);
    var constructor = type.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    Object pending = constructor.newInstance(new Object[] {null});
    var field = type.getDeclaredField("levels"); field.setAccessible(true);
    var levels = (List<List<TerrainGpuMesh>>) field.get(pending);
    var method = type.getDeclaredMethod("bytes"); method.setAccessible(true);
    MethodHandle query = MethodHandles.lookup().unreflect(method).bindTo(pending);
    if (!(levels instanceof ArrayList) || levels.size() != 3) throw new AssertionError("Private layout changed");
    for (var level : levels) if (!(level instanceof ArrayList)) throw new AssertionError("Private part layout changed");
    check(query, levels);
    int checks = 1;
    var bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    if (!bean.isThreadAllocatedMemorySupported()) throw new AssertionError("Allocation counter unavailable");
    bean.setThreadAllocatedMemoryEnabled(true);
    long thread = Thread.currentThread().threadId();
    for (int kind = 0; kind < 5; kind++) {
      var meshes = LodMesher.build(LodReductionChecks.scene(kind, -64));
      var signature = LodReductionChecks.signature(List.of(meshes.fine(), meshes.medium(), meshes.coarse()));
      for (var level : levels) level.clear();
      for (int detail = 0; detail < 3; detail++) {
        for (var mesh : meshes.level(detail)) {
          levels.get(detail).add(holder(mesh.gpuBytes()));
          check(query, levels); checks++;
        }
      }
      if ((long) query.invokeExact() != meshes.bytes()) throw new AssertionError("Mesher admission disagrees");
      long expected = oracle(levels);
      for (int i = 0; i < 100_000; i++) sink = (long) query.invokeExact();
      int batches = 128, perBatch = 1024;
      long[] timings = new long[batches];
      long allocated = 0;
      for (int batch = 0; batch < batches; batch++) {
        long before = bean.getThreadAllocatedBytes(thread), start = System.nanoTime(), total = 0;
        for (int i = 0; i < perBatch; i++) total += (long) query.invokeExact();
        timings[batch] = System.nanoTime() - start;
        allocated += bean.getThreadAllocatedBytes(thread) - before;
        if (total != expected * perBatch) throw new AssertionError("Measured query changed");
        sink = total;
      }
      Arrays.sort(timings);
      int parts = levels.stream().mapToInt(List::size).sum();
      System.out.printf(Locale.ROOT,
          "MEASURE scene=%d parts=%d bytes=%d allocationBytesPerQuery=%.6f batchMeanNsP50=%.3f batchMeanNsP95=%.3f batchMeanNsP99=%.3f queries=%d signature=%s%n",
          kind, parts, expected, allocated / (double)(batches * perBatch),
          timings[63] / (double)perBatch, timings[121] / (double)perBatch, timings[126] / (double)perBatch,
          batches * perBatch, signature);
      if (!signature.equals(LodReductionChecks.signature(List.of(meshes.fine(), meshes.medium(), meshes.coarse()))))
        throw new AssertionError("Query mutated geometry");
      // Admission must reflect partial progress and current values, rather than a stale cached total.
      for (int detail = 0; detail < 3; detail++) {
        levels.get(detail).clear(); check(query, levels); checks++;
        levels.get(detail).add(holder(3_000_000_000L + detail)); check(query, levels); checks++;
        levels.get(detail).get(0).byteSize += 700; check(query, levels); checks++;
      }
    }
    var random = new Random(876521);
    for (int i = 0; i < 10_000; i++) {
      var level = levels.get(random.nextInt(3));
      if (level.size() > 64 || random.nextInt(5) == 0) level.clear();
      else if (!level.isEmpty() && random.nextBoolean()) level.remove(random.nextInt(level.size()));
      else level.add(holder(random.nextLong(1L << 33)));
      check(query, levels); checks++;
    }
    for (var level : levels) level.clear(); check(query, levels); checks++;
    System.out.println("PENDING_UPLOAD_BYTES_PASS checks=" + checks + " realMesherScenes=5 privateProductClass=" + actual
        + " actualGpuUpload=false methodHandleInvocation=true");
  }
}
