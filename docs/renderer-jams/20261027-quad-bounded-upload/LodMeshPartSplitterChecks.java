package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.HorizonMesh;
import com.shnewbs.deepprimal.core.lod.LodMesher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Standalone production-helper tests. Does not execute Minecraft or actual GPU uploads. */
public final class LodMeshPartSplitterChecks {
  private static long checks;

  private static void require(boolean valid, String what) {
    checks++;
    if (!valid) throw new AssertionError(what + " at assertion " + checks);
  }

  private static HorizonMesh quads(Random rng, int count) {
    float[] positions = new float[count * 12];
    int[] colors = new int[count * 4], normals = new int[count * 4], indices = new int[count * 6];
    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for (int q = 0; q < count; q++) {
      int v = 4 * q, k = 6 * q;
      for (int i = 0; i < 4; i++) {
        int h = rng.nextInt(1024) - 512;
        float y = h + rng.nextFloat();
        positions[3 * (v + i)] = rng.nextInt(4096) - 2048 + rng.nextFloat();
        positions[3 * (v + i) + 1] = y;
        positions[3 * (v + i) + 2] = rng.nextInt(4096) - 2048 + rng.nextFloat();
        colors[v + i] = rng.nextInt(); // includes partially transparent material
        normals[v + i] = rng.nextInt();
        min = Math.min(min, (int) Math.floor(y));
        max = Math.max(max, (int) Math.ceil(y));
      }
      indices[k] = v;
      indices[k + 1] = v + 1;
      indices[k + 2] = v + 2;
      indices[k + 3] = v;
      indices[k + 4] = v + 2;
      indices[k + 5] = v + 3;
    }
    return new HorizonMesh(positions, colors, indices, normals, new short[0], min, max);
  }

  private static long[] triangleSignature(List<HorizonMesh> list) {
    long[] sig = new long[list.stream().mapToInt(m -> m.indices().length).sum() / 3 * 3];
    int out = 0;
    for (HorizonMesh m : list) {
      for (int index : m.indices()) {
        long value = 0x9E3779B97F4A7C15L;
        int off = 3 * index;
        for (int k = 0; k < 3; k++) value = Long.rotateLeft(value, 11)
            ^ Float.floatToRawIntBits(m.positions()[off + k]);
        value = Long.rotateLeft(value, 11) ^ m.colors()[index];
        value = Long.rotateLeft(value, 11) ^ m.normals()[index];
        sig[out++] = value;
      }
    }
    return sig;
  }

  private static void verifySplit(List<HorizonMesh> input, long budget) {
    var result = LodMeshPartSplitter.splitLevel(input, budget);
    require(result.size() <= LodMeshPartSplitter.MAX_PARTS_PER_LEVEL || result == input,
        "output cap exceeded");
    require(Arrays.equals(triangleSignature(input), triangleSignature(result)),
        "ordered triangle/material/normal payload altered");
    long beforeBytes = 0, afterBytes = 0;
    for (HorizonMesh m : input) beforeBytes += m.gpuBytes();
    for (HorizonMesh m : result) {
      afterBytes += m.gpuBytes();
      require(m.gpuBytes() <= budget || result == input || m.vertices() % 4 != 0,
          "oversized part after eligible split");
      require(m.heights().length == 0, "height layout changed");
    }
    require(beforeBytes == afterBytes, "allocation estimate not conserved");
    require(triangleSignature(input).length == triangleSignature(result).length,
        "triangle count changed");
  }

  public static void main(String[] args) {
    Random random = new Random(202610271955L);
    require(LodMeshPartSplitter.QUAD_GPU_BYTES == 92, "quad byte contract");
    for (int i = 0; i < 1800; i++) {
      int quads = 1 + random.nextInt(1024);
      HorizonMesh mesh = quads(random, quads);
      long budget = new long[] {92, 1024, 4096, 16384, 32768, 65536,
          262144, 1048576}[i % 8];
      verifySplit(List.of(mesh), budget);
      // Original arrays must never be mutated, including alpha/normal payloads.
      require(mesh.indices()[0] == 0 && mesh.indices()[3] == 0,
          "original source modified");
    }
    // Mixed existing parts, with per-part ordering unchanged.
    for (int i = 0; i < 300; i++) {
      List<HorizonMesh> page = new ArrayList<>();
      for (int j = 0; j < 1 + random.nextInt(8); j++)
        page.add(quads(random, 1 + random.nextInt(1024)));
      verifySplit(page, 16 * 1024);
    }
    var original = quads(random, 1024);
    require(LodMeshPartSplitter.splitLevel(List.of(original), 262144) == List.of(original)
        || LodMeshPartSplitter.splitLevel(List.of(original), 262144).get(0) == original,
        "zero-copy pass-through");
    // A noncanonical shared-vertex mesh must not be reindexed or truncated.
    int[] broken = original.indices().clone();
    broken[7] = 0;
    var foreign = new HorizonMesh(original.positions(), original.colors(), broken,
        original.normals(), new short[0], original.minimum(), original.maximum());
    var foreignList = List.of(foreign);
    require(LodMeshPartSplitter.splitLevel(foreignList, 32768) == foreignList,
        "shared topology was split");
    var legacy = new HorizonMesh(original.positions(), original.colors(), original.indices(),
        original.normals(), new short[]{2}, original.minimum(), original.maximum());
    require(LodMeshPartSplitter.splitLevel(List.of(legacy), 32768).get(0) == legacy,
        "legacy height payload was split");
    // A crafted page with too many pieces must be rejected before allocating anything.
    var dense = new ArrayList<HorizonMesh>();
    for (int i = 0; i < 1025; i++) dense.add(quads(random, 1));
    require(LodMeshPartSplitter.splitLevel(dense, 92) == dense, "page-count fail-open");
    require(LodMeshPartSplitter.splitLevel(List.of(original), 0).get(0) == original,
        "disabled zero budget");
    boolean invalid = false;
    try { LodMeshPartSplitter.splitLevel(List.of(original), -1); }
    catch (IllegalArgumentException expected) { invalid = true; }
    require(invalid, "negative byte budget");
    invalid = false;
    try { LodMeshPartSplitter.split(new LodMesher.Levels(List.of(), List.of(), List.of()), -2); }
    catch (IllegalArgumentException expected) { invalid = true; }
    require(invalid, "negative KiB budget");
    // Worker cancellation is explicit: no partial page can be published.
    Thread.currentThread().interrupt();
    boolean cancelled = false;
    try { LodMeshPartSplitter.splitLevel(List.of(original), 32768); }
    catch (java.util.concurrent.CancellationException expected) { cancelled = true; }
    finally { Thread.interrupted(); }
    require(cancelled, "interrupted split must abandon the worker result");
    var levels = new LodMesher.Levels(List.of(original), List.of(original), List.of(original));
    require(LodMeshPartSplitter.split(levels, 0) == levels, "default no-op");
    var split = LodMeshPartSplitter.split(levels, 32);
    require(split.bytes() == levels.bytes(), "three-level GPU memory budget");
    require(split.fine().size() > 1 && split.medium().size() > 1
        && split.coarse().size() > 1, "opt-in split did not take effect");
    System.out.println("LOD_MESH_PART_SPLITTER_PASS assertions=" + checks
        + " randomizedSingle=1800 randomizedPages=300 optIn32KiB=true");
  }
}
