package com.shnewbs.primalengine.core;

import java.util.*;

/** Version-one geography: whole-continent drainage planned before individual chunks exist. */
public final class ContinentalWorld {
  public static final int SEA = 96, MIN_Y = -128, HEIGHT = 768, CELL = 8192;
  private static final int STEP = 64, SIDE = 129, COUNT = SIDE * SIDE;

  public record Sample(
      double ground,
      double water,
      double flowX,
      double flowZ,
      double rain,
      double temperature,
      boolean ocean) {}

  private record Island(int x, int z, double cx, double cz, double rx, double rz) {}

  private final SeedField field;
  private final LinkedHashMap<Long, Drainage> plans = new LinkedHashMap<>(16, .75f, true);

  public ContinentalWorld(long seed) {
    field = new SeedField(seed);
  }

  private Island island(int x, int z) {
    return new Island(
        x,
        z,
        x * (double) CELL + (field.at(x, z, 1) - .5) * 800,
        z * (double) CELL + (field.at(x, z, 2) - .5) * 800,
        2400 + field.at(x, z, 3) * 700,
        2200 + field.at(x, z, 4) * 800);
  }

  private double mask(Island i, double x, double z) {
    double xx = (x - i.cx) / i.rx, zz = (z - i.cz) / i.rz;
    return 1 - Math.hypot(xx, zz) + field.detail(x, z, 1200, 20) * .13;
  }

  private Island nearest(double x, double z) {
    int ix = (int) Math.floor(x / CELL + .5), iz = (int) Math.floor(z / CELL + .5);
    Island best = null;
    double value = -Double.MAX_VALUE;
    for (int dz = -1; dz <= 1; dz++)
      for (int dx = -1; dx <= 1; dx++) {
        Island i = island(ix + dx, iz + dz);
        double m = mask(i, x, z);
        if (m > value) {
          best = i;
          value = m;
        }
      }
    return best;
  }

  private double raw(Island i, double x, double z) {
    double m = mask(i, x, z);
    if (m < 0) return SEA - 8 + Math.max(-104, m * 140);
    double foothill = 100 * SeedField.smooth(m / .6);
    double ridge = Math.pow(1 - Math.abs(field.detail(x, z, 1450, 40)), 4);
    double mountain = 200 * ridge * SeedField.smooth((m - .16) / .45);
    return SEA
        - 8
        + Math.min(m, .12) * 90
        + foothill
        + mountain
        + field.detail(x, z, 210, 70) * 8 * SeedField.smooth(m / .1);
  }

  public double uncarved(double x, double z) {
    return raw(nearest(x, z), x, z);
  }

  private synchronized Drainage plan(Island i) {
    long key = ((long) i.x << 32) ^ (i.z & 0xffffffffL);
    Drainage d = plans.get(key);
    if (d == null) {
      d = new Drainage(i);
      plans.put(key, d);
      while (plans.size() > 12) plans.remove(plans.keySet().iterator().next());
    }
    return d;
  }

  public Sample sample(double x, double z) {
    Island i = nearest(x, z);
    double ground = raw(i, x, z);
    double rain = Math.clamp(.65 + field.detail(x, z, 4000, 90) * .5, .15, 1);
    double temperature =
        Math.clamp(
            .55 + field.detail(x, z, 9000, 100) * .4 - Math.max(0, ground - 180) / 800, 0, 1);
    if (ground < SEA) return new Sample(ground, SEA, 0, 0, rain, temperature, true);
    Drainage d = plan(i);
    int gx = (int) Math.floor((x - d.ox) / STEP), gz = (int) Math.floor((z - d.oz) / STEP);
    double water = Double.NaN, fx = 0, fz = 0, best = Double.MAX_VALUE;
    for (int dz = -2; dz <= 2; dz++)
      for (int dx = -2; dx <= 2; dx++) {
        int xx = gx + dx, zz = gz + dz;
        if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
        int a = zz * SIDE + xx, b = d.parent[a];
        if (b < 0 || d.area[a] < 160_000 || d.surface[a] < SEA + .1) continue;
        double ax = d.ox + xx * STEP, az = d.oz + zz * STEP;
        double vx = (b % SIDE - xx) * STEP, vz = (b / SIDE - zz) * STEP;
        double t = Math.clamp(((x - ax) * vx + (z - az) * vz) / (vx * vx + vz * vz), 0, 1);
        double distance = Math.hypot(x - ax - vx * t, z - az - vz * t);
        double width = Math.clamp(2 + Math.sqrt(d.area[a]) / 550, 3, 18);
        double surface = SeedField.lerp(d.surface[a], d.surface[b], t) - .15;
        double depth = Math.clamp(width * .23, 1.1, 4);
        double valley = 1 - SeedField.smooth((distance - width) / (12 + width));
        if (valley > 0) ground = Math.min(ground, SeedField.lerp(ground, surface - depth, valley));
        if (distance < width && distance / width < best) {
          best = distance / width;
          water = surface;
          fx = vx / Math.hypot(vx, vz);
          fz = vz / Math.hypot(vx, vz);
        }
      }
    return new Sample(ground, water, fx, fz, rain, temperature, false);
  }

  public synchronized int cachedContinents() {
    return plans.size();
  }

  public void verifyDrainage(double x, double z) {
    Drainage d = plan(nearest(x, z));
    for (int a = 0; a < COUNT; a++) {
      int b = d.parent[a];
      if (b >= 0 && (d.order[b] >= d.order[a] || d.surface[b] > d.surface[a] + 1e-9))
        throw new AssertionError("Drainage cycle or uphill edge");
    }
  }

  private final class Drainage {
    final double ox, oz;
    final double[] surface = new double[COUNT], area = new double[COUNT];
    final int[] parent = new int[COUNT], order = new int[COUNT];

    Drainage(Island island) {
      ox = island.cx - CELL / 2.;
      oz = island.cz - CELL / 2.;
      Arrays.fill(parent, -1);
      Arrays.fill(order, -1);
      var queue =
          new PriorityQueue<Integer>(
              Comparator.<Integer>comparingDouble(a -> surface[a]).thenComparingInt(a -> a));
      boolean[] seen = new boolean[COUNT];
      int[] sorted = new int[COUNT];
      for (int z = 0; z < SIDE; z++)
        for (int x = 0; x < SIDE; x++) {
          int a = z * SIDE + x;
          double wx = ox + x * STEP, wz = oz + z * STEP;
          surface[a] = raw(island, wx, wz);
          area[a] = STEP * STEP * Math.clamp(.65 + field.detail(wx, wz, 4000, 90) * .5, .15, 1);
          if (surface[a] <= SEA || x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1) {
            seen[a] = true;
            queue.add(a);
          }
        }
      int count = 0;
      while (!queue.isEmpty()) {
        int a = queue.remove();
        sorted[count] = a;
        order[a] = count++;
        int ax = a % SIDE, az = a / SIDE;
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int x = ax + dx, z = az + dz;
            if (x < 0 || z < 0 || x >= SIDE || z >= SIDE) continue;
            int b = z * SIDE + x;
            if (seen[b]) continue;
            seen[b] = true;
            parent[b] = a;
            surface[b] = Math.max(surface[b], surface[a] + .002);
            queue.add(b);
          }
      }
      for (int n = COUNT - 1; n >= 0; n--) {
        int a = sorted[n], b = parent[a];
        if (b >= 0) area[b] += area[a];
      }
    }
  }
}
