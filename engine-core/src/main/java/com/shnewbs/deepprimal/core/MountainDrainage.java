package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.DoubleBinaryOperator;

/** Bounded, deterministic catchments. A priority flood finds valley routes to existing water. */
public final class MountainDrainage {
  public static final int CELL = 64, SIZE = 4096, SIDE = SIZE / CELL + 1;
  private static final int COUNT = SIDE * SIDE, CACHE_SIZE = 32;
  private final DoubleBinaryOperator terrain;
  private final double seaLevel;
  private final boolean contained, naturalSteps;
  private static final int STATIONS = 17;
  private static final int WAYS = 4, SETS = CACHE_SIZE / WAYS;
  private final AtomicReferenceArray<Region[]> cache = new AtomicReferenceArray<>(SETS);
  private final Object[] locks = new Object[SETS];

  public MountainDrainage(DoubleBinaryOperator terrain, double seaLevel) {
    this(terrain, seaLevel, false);
  }

  public MountainDrainage(DoubleBinaryOperator terrain, double seaLevel, boolean contained) {
    this(terrain, seaLevel, contained, false);
  }

  public MountainDrainage(
      DoubleBinaryOperator terrain, double seaLevel, boolean contained, boolean naturalSteps) {
    this.terrain = terrain;
    this.seaLevel = seaLevel;
    this.contained = contained;
    this.naturalSteps = naturalSteps;
    Arrays.setAll(locks, i -> new Object());
  }

  public Sample sample(int x, int z, double original) {
    Region r = region(Math.floorDiv(x, SIZE), Math.floorDiv(z, SIZE));
    int gx = Math.floorDiv(x - r.x, CELL), gz = Math.floorDiv(z - r.z, CELL);
    double height = original, surface = seaLevel, strength = 0, flowX = 0, flowZ = 0, speed = 1;
    for (int j = Math.max(1, gz - 2); j <= Math.min(SIDE - 2, gz + 2); j++) {
      for (int i = Math.max(1, gx - 2); i <= Math.min(SIDE - 2, gx + 2); i++) {
        int child = j * SIDE + i, parent = r.parent[child];
        if (parent < 0 || r.width[child] <= 0) continue;
        double ax = r.x + r.localX[child], az = r.z + r.localZ[child];
        double dx = r.localX[parent] - r.localX[child], dz = r.localZ[parent] - r.localZ[child];
        double t = Math.clamp(((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz), 0, 1);
        double distance = Math.hypot(x - ax - t * dx, z - az - t * dz);
        double width =
            r.width[child] + t * (Math.max(r.width[child], r.width[parent]) - r.width[child]);
        if (naturalSteps && r.headwater[child] && r.lakes[child] == 0) {
          // A spring grows into its catchment instead of starting as a full-width waterfall.
          width = Math.max(2, width * (.25 + .75 * smooth(t, 0, 1)));
        }
        double weight = 1 - smooth(distance, width, width + 12);
        if (weight <= 0 && !(naturalSteps && r.lakes[child] > 0)) continue;
        double water = r.water[child] + t * (r.water[parent] - r.water[child]);
        if (r.profiles != null) {
          double station = t * (STATIONS - 1);
          int a = Math.min(STATIONS - 2, (int) station);
          float[] profile = r.profiles[child];
          water = Math.min(water, profile[a] + (station - a) * (profile[a + 1] - profile[a]));
        }
        if (naturalSteps && r.lakes[child] > 0) {
          double sourceDistance = Math.hypot(x - ax, z - az);
          double poolWeight = 1 - smooth(sourceDistance, r.lakes[child] * .65, r.lakes[child]);
          double poolBed = r.water[child] - 3.25;
          double poolHeight = original - poolWeight * Math.max(0, original - poolBed);
          if (poolWeight > 0 && poolHeight < height) {
            height = poolHeight;
            surface = r.water[child];
            strength = poolWeight;
            double length = Math.hypot(dx, dz);
            flowX = dx / length;
            flowZ = dz / length;
            speed = .35;
          }
        }
        if (weight <= 0) continue;
        double bed =
            water
                - (naturalSteps
                    ? 1.25 + Math.min(2.25, width * .12)
                    : 3 + Math.min(4, width * .18));
        double carved = original - weight * Math.max(0, original - bed);
        if (carved < height || (carved == height && weight > strength)) {
          height = carved;
          surface = water;
          strength = weight;
          double length = Math.hypot(dx, dz);
          flowX = dx / length;
          flowZ = dz / length;
          speed =
              WaterDynamics.speed(
                  (r.water[child] - r.water[parent]) / length, r.area[child], width);
        }
      }
    }
    return new Sample(height, surface, strength, flowX, flowZ, speed);
  }

  private static double smooth(double value, double low, double high) {
    double t = Math.clamp((value - low) / (high - low), 0, 1);
    return t * t * (3 - 2 * t);
  }

  public Region region(int rx, int rz) {
    long key = ((long) rx << 32) ^ (rz & 0xffffffffL);
    int slot = (int) mix(key) & (SETS - 1);
    Region[] bucket = cache.get(slot);
    Region r = find(bucket, rx, rz);
    if (r != null) return r;
    synchronized (locks[slot]) {
      bucket = cache.get(slot);
      r = find(bucket, rx, rz);
      if (r != null) return r;
      r = build(rx, rz);
      Region[] next = new Region[Math.min(WAYS, bucket == null ? 1 : bucket.length + 1)];
      next[0] = r;
      if (bucket != null) System.arraycopy(bucket, 0, next, 1, next.length - 1);
      cache.set(slot, next);
      return r;
    }
  }

  private static Region find(Region[] bucket, int rx, int rz) {
    if (bucket != null) for (Region r : bucket) if (r.rx == rx && r.rz == rz) return r;
    return null;
  }

  private Region build(int rx, int rz) {
    int ox = Math.multiplyExact(rx, SIZE), oz = Math.multiplyExact(rz, SIZE);
    double[] h = new double[COUNT], flood = new double[COUNT];
    float[] water = new float[COUNT], width = new float[COUNT];
    int[] parent = new int[COUNT], order = new int[COUNT], area = new int[COUNT];
    short[] localX = new short[COUNT], localZ = new short[COUNT];
    Arrays.fill(parent, -2);
    Arrays.fill(flood, Double.POSITIVE_INFINITY);
    PriorityQueue<Node> queue =
        new PriorityQueue<>(Comparator.comparingDouble(Node::level).thenComparingInt(Node::index));
    int fallback = -1;
    for (int z = 1; z < SIDE - 1; z++)
      for (int x = 1; x < SIDE - 1; x++) {
        int n = z * SIDE + x;
        long hash = mix(((long) (rx * (SIDE - 1) + x) << 32) ^ (rz * (SIDE - 1) + z));
        localX[n] = (short) (x * CELL + (int) Math.floorMod(hash, 25) - 12);
        localZ[n] = (short) (z * CELL + (int) Math.floorMod(hash >>> 8, 25) - 12);
        h[n] = terrain.applyAsDouble(ox + localX[n], oz + localZ[n]);
        if (!Double.isFinite(h[n])) throw new IllegalArgumentException("Non-finite terrain");
        area[n] = 1;
        if (h[n] < seaLevel - 1) {
          if (fallback < 0 || h[n] < h[fallback]) fallback = n;
          // A river should keep a direction after reaching sea level. Seed
          // deep water and wet region exits, rather than ending every reach
          // at the first shallow river node. An inland basin uses a real wet
          // minimum when it has no such exit; this is not a global watershed.
          if (contained
              && h[n] >= seaLevel - 20
              && x != 1
              && z != 1
              && x != SIDE - 2
              && z != SIDE - 2) continue;
          parent[n] = -1;
          flood[n] = seaLevel;
          queue.add(new Node(n, seaLevel));
        }
      }
    if (queue.isEmpty() && fallback >= 0) {
      parent[fallback] = -1;
      flood[fallback] = seaLevel;
      queue.add(new Node(fallback, seaLevel));
    }
    int length = 0;
    while (!queue.isEmpty()) {
      Node node = queue.remove();
      int n = node.index;
      if (node.level != flood[n]) continue;
      order[length++] = n;
      int x = n % SIDE, z = n / SIDE;
      for (int dz = -1; dz <= 1; dz++)
        for (int dx = -1; dx <= 1; dx++) {
          if ((dx | dz) == 0
              || x + dx < 1
              || z + dz < 1
              || x + dx >= SIDE - 1
              || z + dz >= SIDE - 1) continue;
          int next = (z + dz) * SIDE + x + dx;
          // First discovery is final for this minimum-spill priority flood.
          if (parent[next] != -2) continue;
          parent[next] = n;
          flood[next] = Math.max(h[next], node.level);
          queue.add(new Node(next, flood[next]));
        }
    }
    // Parents are visited first. Accumulate catchment area and lower downstream
    // profiles to breach intervening saddles rather than fill entire hillsides.
    for (int k = length - 1; k >= 0; k--) {
      int n = order[k], p = parent[n];
      water[n] = (float) Math.max(seaLevel, h[n] - 2);
      if (p >= 0) area[p] += area[n];
    }
    for (int k = length - 1; k >= 0; k--) {
      int n = order[k], p = parent[n];
      if (p >= 0 && area[n] >= 10) water[p] = Math.min(water[p], water[n]);
    }
    for (int k = 0; k < length; k++) {
      int n = order[k];
      if (parent[n] >= 0 && area[n] >= 10 && (contained || h[n] >= seaLevel)) {
        width[n] = (float) Math.min(16, 3.5 + Math.sqrt(area[n]) * .22);
      }
    }
    // Only retain channels whose complete downstream path is selected or reaches
    // an existing wet outlet; a catchment with no wet outlet creates no channels.
    float[][] profiles = contained ? new float[COUNT][] : null;
    if (contained) {
      // Resolve the waterline against the bed and both banks along each reach,
      // not just at widely spaced routing nodes. Running minima retain downhill
      // terraces and breach saddles without suspending water over lower valleys.
      for (int k = length - 1; k >= 0; k--) {
        int n = order[k], p = parent[n];
        if (p < 0 || width[n] <= 0) continue;
        double dx = localX[p] - localX[n], dz = localZ[p] - localZ[n];
        double distance = Math.hypot(dx, dz);
        float[] profile = new float[STATIONS];
        double last = water[n];
        for (int s = 0; s < STATIONS; s++) {
          double t = s / (double) (STATIONS - 1);
          double x = ox + localX[n] + t * dx, z = oz + localZ[n] + t * dz;
          double bank = width[n] + t * (Math.max(width[n], width[p]) - width[n]) + 4;
          double ceiling = terrain.applyAsDouble(x, z);
          for (int side = -1; side <= 1; side += 2)
            ceiling =
                Math.min(
                    ceiling,
                    terrain.applyAsDouble(
                        x - side * bank * dz / distance, z + side * bank * dx / distance));
          last = Math.min(last, Math.max(seaLevel, ceiling - 2));
          profile[s] = (float) last;
        }
        profiles[n] = profile;
        water[n] = profile[0];
        water[p] = Math.min(water[p], profile[STATIONS - 1]);
      }
    }
    boolean[] headwater = new boolean[COUNT];
    float[] lakes = new float[COUNT];
    if (naturalSteps) {
      for (int k = 0; k < length; k++) headwater[order[k]] = width[order[k]] > 0;
      for (int k = 0; k < length; k++) {
        int n = order[k], p = parent[n];
        if (width[n] > 0 && p >= 0) headwater[p] = false;
      }
    }
    if (naturalSteps) {
      for (int k = 0; k < length; k++) {
        int n = order[k], p = parent[n];
        if (!headwater[n] || area[n] < 16 || water[n] <= seaLevel + 8) continue;
        double radius = Math.min(24, 10 + Math.sqrt(area[n]));
        double bank = Double.POSITIVE_INFINITY;
        for (int angle = 0; angle < 16; angle++) {
          double a = angle * Math.PI / 8;
          bank =
              Math.min(
                  bank,
                  terrain.applyAsDouble(
                      ox + localX[n] + radius * Math.cos(a),
                      oz + localZ[n] + radius * Math.sin(a)));
        }
        double lake = Math.min(water[n], bank - 1);
        if (lake < water[p] || lake < water[n] - 8) continue;
        lakes[n] = (float) radius;
        water[n] = (float) lake;
        if (profiles != null && profiles[n] != null)
          for (int station = 0; station < STATIONS; station++)
            profiles[n][station] = Math.min(profiles[n][station], (float) lake);
      }
    }
    return new Region(
        rx, rz, ox, oz, parent, water, width, h, area, order, length, localX, localZ, profiles,
        headwater, lakes);
  }

  private static long mix(long value) {
    value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
    value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
    return value ^ (value >>> 31);
  }

  public record Sample(
      double height,
      double waterLevel,
      double strength,
      double flowX,
      double flowZ,
      double speed) {}

  private record Node(int index, double level) {}

  /** Immutable data exposed to generation checks; arrays remain private. */
  public static final class Region {
    private final int rx, rz, x, z;
    private final int[] parent, area, order;
    private final float[] water, width;
    private final double[] terrain;
    private final int length;
    private final short[] localX, localZ;
    private final float[][] profiles;
    private final boolean[] headwater;
    private final float[] lakes;

    private Region(
        int rx,
        int rz,
        int x,
        int z,
        int[] parent,
        float[] water,
        float[] width,
        double[] terrain,
        int[] area,
        int[] order,
        int length,
        short[] localX,
        short[] localZ,
        float[][] profiles,
        boolean[] headwater,
        float[] lakes) {
      this.rx = rx;
      this.rz = rz;
      this.x = x;
      this.z = z;
      this.parent = parent;
      this.water = water;
      this.width = width;
      this.terrain = terrain;
      this.area = area;
      this.order = order;
      this.length = length;
      this.localX = localX;
      this.localZ = localZ;
      this.profiles = profiles;
      this.headwater = headwater;
      this.lakes = lakes;
    }

    public boolean headwater(int node) {
      return headwater[node];
    }

    public double lakeRadius(int node) {
      return lakes[node];
    }

    public int nodeCount() {
      return length;
    }

    public int node(int orderIndex) {
      return order[orderIndex];
    }

    public int parent(int node) {
      return parent[node];
    }

    public double water(int node) {
      return water[node];
    }

    public double terrain(int node) {
      return terrain[node];
    }

    public double width(int node) {
      return width[node];
    }

    public int area(int node) {
      return area[node];
    }

    public int x(int node) {
      return x + localX[node];
    }

    public int z(int node) {
      return z + localZ[node];
    }
  }
}
