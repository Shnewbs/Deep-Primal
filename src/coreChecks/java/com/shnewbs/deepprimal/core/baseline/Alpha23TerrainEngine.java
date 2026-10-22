package com.shnewbs.deepprimal.core.baseline;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

import com.shnewbs.deepprimal.core.TerrainNoise;
import java.util.*;

/** Original versioned continental planner. Immutable drainage results are shared between chunks. */
public final class Alpha23TerrainEngine {
  public static final int SEA = 96, CELL = 8192, STEP = 64, SIDE = 129, COUNT = SIDE * SIDE;

  public record Sample(
      double ground,
      double water,
      double rain,
      double temperature,
      double slope,
      double sediment,
      double flowX,
      double flowZ,
      double speed,
      int rock) {
    public boolean wet() {
      return Double.isFinite(water);
    }
  }

  private record Land(
      int x, int z, double cx, double cz, double rx, double rz, double cos, double sin) {}

  private record Location(Land land, double mask) {}

  public final TerrainNoise noise;
  public final int revision;
  private final java.util.concurrent.ConcurrentHashMap<Long, Land> shapes =
      new java.util.concurrent.ConcurrentHashMap<>();
  private final LinkedHashMap<Long, Drainage> cache = new LinkedHashMap<>(16, .75f, true);

  public Alpha23TerrainEngine(long seed) {
    this(seed, 1);
  }

  public Alpha23TerrainEngine(long seed, int revision) {
    if (revision < 1 || revision > 2) throw new IllegalArgumentException("Terrain revision");
    this.revision = revision;
    noise = new TerrainNoise(seed);
  }

  private Land land(int x, int z) {
    long key = ((long) x << 32) ^ (z & 0xffffffffL);
    Land existing = shapes.get(key);
    if (existing != null) return existing;
    if (shapes.size() >= 128) {
      var it = shapes.keySet().iterator();
      if (it.hasNext()) shapes.remove(it.next());
    }
    Land created = createLand(x, z);
    Land prior = shapes.putIfAbsent(key, created);
    return prior == null ? created : prior;
  }

  private Land createLand(int x, int z) {
    double angle = noise.hash(x, z, 5) * Math.PI;
    return new Land(
        x,
        z,
        x * (double) CELL + (noise.hash(x, z, 1) - .5) * 650,
        z * (double) CELL + (noise.hash(x, z, 2) - .5) * 650,
        2550 + noise.hash(x, z, 3) * 650,
        2350 + noise.hash(x, z, 4) * 700,
        Math.cos(angle),
        Math.sin(angle));
  }

  private double mask(Land i, double x, double z) {
    double wx = x + noise.field(x, z, 1700, 21) * 350 + noise.field(x, z, 430, 25) * 65;
    double wz = z + noise.field(x, z, 1700, 29) * 350 + noise.field(x, z, 430, 31) * 65;
    return mask(i, wx, wz, noise.field(x, z, 270, 37) * .035);
  }

  private double mask(Land i, double wx, double wz, double coast) {
    double dx = wx - i.cx, dz = wz - i.cz, c = i.cos, s = i.sin;
    double nx = (dx * c - dz * s) / i.rx, nz = (dx * s + dz * c) / i.rz;
    // Connected overlapping provinces produce bays and peninsulas rather than circular islands.
    double core = 1 - Math.hypot(nx / .80, nz / .67);
    double north = (1 - Math.hypot((nx - .42) / .51, (nz + .25) / .55)) * .7;
    double south = (1 - Math.hypot((nx + .45) / .50, (nz - .25) / .49)) * .7;
    double island = (1 - Math.hypot((nx - .90) / .13, (nz - .28) / .18)) * .35;
    return Math.max(Math.max(core, north), Math.max(south, island)) + coast;
  }

  private Location locate(double x, double z) {
    double wx = x + noise.field(x, z, 1700, 21) * 350 + noise.field(x, z, 430, 25) * 65;
    double wz = z + noise.field(x, z, 1700, 29) * 350 + noise.field(x, z, 430, 31) * 65;
    double coast = noise.field(x, z, 270, 37) * .035;
    int cx = (int) Math.floor(x / CELL + .5), cz = (int) Math.floor(z / CELL + .5);
    Land best = null;
    double score = -Double.MAX_VALUE;
    for (int dz = -1; dz <= 1; dz++)
      for (int dx = -1; dx <= 1; dx++) {
        Land l = land(cx + dx, cz + dz);
        double m = mask(l, wx, wz, coast);
        if (m > score) {
          score = m;
          best = l;
        }
      }
    return new Location(best, score);
  }

  public double rainfall(double x, double z) {
    return Math.clamp(.57 + noise.field(x, z, 3100, 81) * .55, .08, 1);
  }

  public double forest(double x, double z) {
    return Math.clamp(
        .46 + noise.field(x, z, 240, 95) * .7 + noise.field(x, z, 1100, 99) * .35, 0, 1);
  }

  public double raw(double x, double z) {
    return raw(locate(x, z).mask, x, z);
  }

  private double raw(Land l, double x, double z) {
    return raw(mask(l, x, z), x, z);
  }

  private double raw(double m, double x, double z) {
    if (m < 0)
      return SEA
          - 5
          - 42 * smooth(-m / .12)
          - 85 * smooth((-m - .12) / .55)
          + noise.field(x, z, 270, 33) * 13;
    double inland = smooth(m / .22), plateau = 22 + noise.field(x, z, 1100, 35) * 28;
    double province = smooth((noise.field(x, z, 2200, 39) + .30) / .65);
    double warpX = x + noise.field(x, z, 430, 44) * 130,
        warpZ = z + noise.field(x, z, 430, 48) * 130;
    double ridge = 1 - Math.abs(noise.field(warpX, warpZ, 780, 51));
    double peaks = Math.pow(ridge, 3) * province * (110 + noise.field(x, z, 1800, 57) * 80);
    double hills =
        noise.field(x, z, 240, 61) * 24
            + noise.field(x, z, 65, 65) * 6
            + noise.noise(x, z, 19, 70) * 1.3;
    return SEA - 3 + inland * (plateau + peaks + hills) + Math.min(m, .15) * 30;
  }

  private synchronized Drainage plan(Land l) {
    long key = ((long) l.x << 32) ^ (l.z & 0xffffffffL);
    Drainage d = cache.get(key);
    if (d == null) {
      d = new Drainage(l);
      cache.put(key, d);
      while (cache.size() > 12) cache.remove(cache.keySet().iterator().next());
    }
    return d;
  }

  public synchronized int cachedRegions() {
    return cache.size();
  }

  public int biome(double x, double z) {
    double h = raw(x, z),
        r = rainfall(x, z),
        t = .62 + noise.field(x, z, 6500, 85) * .40 - Math.max(0, h - 160) / 420;
    if (h < SEA) return 3;
    if (h > 245) return 7;
    if (t < .20) return 5;
    if (t < .36) return 4;
    if (r < .32) return 2;
    if (h < SEA + 10 && r > .77) return 6;
    return forest(x, z) > .45 && r > .5 ? 1 : 0;
  }

  public Sample sample(double x, double z) {
    Location location = locate(x, z);
    Land l = location.land;
    double ground = raw(location.mask, x, z), uncut = ground, rain = rainfall(x, z);
    double temperature =
        Math.clamp(.62 + noise.field(x, z, 6500, 85) * .4 - Math.max(0, ground - 160) / 420, 0, 1);
    double slope =
        Math.hypot(raw(l, x + 3, z) - raw(l, x - 3, z), raw(l, x, z + 3) - raw(l, x, z - 3)) / 6;
    int rock = (int) Math.floor((noise.field(x, z, 1500, 91) + 1) * 2.5) % 4;
    if (ground < SEA) return new Sample(ground, SEA, rain, temperature, slope, 1, 0, 0, 0, rock);
    Drainage d = plan(l);
    int gx = (int) Math.floor((x - d.ox) / STEP), gz = (int) Math.floor((z - d.oz) / STEP);
    double water = Double.NaN, fx = 0, fz = 0, speed = 0, sediment = 0, best = Double.MAX_VALUE;
    for (int dz = -2; dz <= 2; dz++)
      for (int dx = -2; dx <= 2; dx++) {
        int xx = gx + dx, zz = gz + dz;
        if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
        int a = zz * SIDE + xx, b = d.parent[a];
        if (b < 0 || d.area[a] < (revision == 1 ? 60_000 : 18_000) || d.surface[a] < SEA + .2)
          continue;
        double[] c = d.curves[a];
        if (c == null) continue;
        double width = Math.clamp(1.7 + Math.sqrt(d.area[a]) / 530, 2.5, 22);
        for (int k = 0; k < 8; k++) {
          double ax = c[k * 2],
              az = c[k * 2 + 1],
              vx = c[k * 2 + 2] - ax,
              vz = c[k * 2 + 3] - az,
              len = vx * vx + vz * vz;
          double t = Math.clamp(((x - ax) * vx + (z - az) * vz) / len, 0, 1),
              dist = Math.hypot(x - ax - vx * t, z - az - vz * t);
          double along = (k + t) / 8;
          if (revision == 2) {
            // Width joins at shared graph nodes; small springs no longer start river-sized.
            double catchment = lerp(d.area[a], d.area[b], smooth(along));
            width = Math.clamp(.70 + Math.sqrt(catchment) / 410, 1.05, 26);
          }
          if (dist > width + 38) continue;
          double surface = Math.max(SEA, lerp(d.surface[a], d.surface[b], along) - .15);
          double depth =
              revision == 1
                  ? Math.clamp(1.2 + width * .20, 1.3, 5.5)
                  : Math.clamp(.55 + width * .19, .75, 6);
          double valley = 1 - smooth((dist - width) / (20 + width));
          if (revision == 1) ground = Math.min(ground, lerp(ground, surface - depth, valley));
          else {
            // Union cuts only: a second reach cannot raise a rim across a carved channel.
            double edge = Math.clamp(dist / width, 0, 1);
            double bed = surface - depth * (1 - Math.pow(edge, 4) * .95);
            double bank = smooth((dist - width) / (4 + width * .4));
            ground = Math.min(ground, lerp(bed, uncut, bank));
          }
          if (revision == 1 && dist >= width && dist < width + 9) {
            double bank = 1 - smooth((dist - width) / 9);
            ground = Math.max(ground, lerp(ground, surface + .35, bank));
          }
          sediment = Math.max(sediment, valley * (1 - Math.min(1, slope)));
          if (dist < width && dist / width < best) {
            best = dist / width;
            double edge = Math.pow(dist / width, 4);
            ground = Math.min(ground, surface - depth * (1 - edge * .95));
            if (ground < surface) {
              water = surface;
              fx = vx / Math.sqrt(len);
              fz = vz / Math.sqrt(len);
              double grade =
                  (d.surface[a] - d.surface[b])
                      / Math.max(1, Math.hypot(d.px[a] - d.px[b], d.pz[a] - d.pz[b]));
              speed = Math.clamp(.12 + Math.sqrt(Math.max(0, grade)) * .9 + width * .008, .12, .85);
            }
          }
        }
      }
    return new Sample(ground, water, rain, temperature, slope, sediment, fx, fz, speed, rock);
  }

  public boolean cave(double x, double y, double z, double surface, int minY) {
    if (y < minY + 12 || y > surface - 10 || y > 180) return false;
    double a = noise.volume(x, y, z, 54, 140), b = noise.volume(x, y, z, 62, 740);
    return a * a + b * b < .012;
  }

  public void verify(double x, double z) {
    Drainage d = plan(locate(x, z).land);
    for (int a = 0; a < COUNT; a++) {
      int b = d.parent[a];
      if (b >= 0 && (d.order[b] >= d.order[a] || d.surface[b] > d.surface[a]))
        throw new AssertionError("Drainage order");
    }
  }

  private final class Drainage {
    final double ox, oz;
    final double[] surface = new double[COUNT],
        area = new double[COUNT],
        px = new double[COUNT],
        pz = new double[COUNT];
    final int[] parent = new int[COUNT], order = new int[COUNT];
    final double[][] curves = new double[COUNT][];

    Drainage(Land l) {
      ox = l.cx - CELL / 2.;
      oz = l.cz - CELL / 2.;
      Arrays.fill(parent, -1);
      Arrays.fill(order, -1);
      boolean[] seen = new boolean[COUNT];
      int[] sorted = new int[COUNT];
      var queue =
          new PriorityQueue<Integer>(
              Comparator.<Integer>comparingDouble(a -> surface[a]).thenComparingInt(a -> a));
      for (int z = 0; z < SIDE; z++)
        for (int x = 0; x < SIDE; x++) {
          int a = z * SIDE + x;
          px[a] = ox + x * STEP + (noise.hash(x + l.x * SIDE, z + l.z * SIDE, 201) - .5) * 28;
          pz[a] = oz + z * STEP + (noise.hash(x + l.x * SIDE, z + l.z * SIDE, 202) - .5) * 28;
          surface[a] = raw(l, px[a], pz[a]);
          area[a] = STEP * STEP * rainfall(px[a], pz[a]);
          if (surface[a] <= SEA || x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1) {
            seen[a] = true;
            queue.add(a);
          }
        }
      int count = 0;
      while (!queue.isEmpty()) {
        int a = queue.remove();
        order[a] = count;
        sorted[count++] = a;
        int ax = a % SIDE, az = a / SIDE;
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int x = ax + dx, z = az + dz;
            if (x < 0 || z < 0 || x >= SIDE || z >= SIDE) continue;
            int b = z * SIDE + x;
            if (seen[b]) continue;
            seen[b] = true;
            parent[b] = a;
            surface[b] = Math.max(surface[b], surface[a] + .015);
            queue.add(b);
          }
      }
      int[] child = new int[COUNT];
      Arrays.fill(child, -1);
      for (int n = COUNT - 1; n >= 0; n--) {
        int a = sorted[n], b = parent[a];
        if (b >= 0) {
          area[b] += area[a];
          if (child[b] < 0 || area[a] > area[child[b]]) child[b] = a;
        }
      }
      for (int a = 0; a < COUNT; a++) {
        int b = parent[a];
        if (b < 0 || area[a] < 60_000) continue;
        int up = child[a], down = parent[b];
        double vx = px[b] - px[a], vz = pz[b] - pz[a], distance = Math.hypot(vx, vz);
        double tx = up < 0 ? vx : px[b] - px[up],
            tz = up < 0 ? vz : pz[b] - pz[up],
            tl = Math.hypot(tx, tz);
        double ux = down < 0 ? vx : px[down] - px[a],
            uz = down < 0 ? vz : pz[down] - pz[a],
            ul = Math.hypot(ux, uz);
        double c1x = px[a] + tx / tl * distance * .28, c1z = pz[a] + tz / tl * distance * .28;
        double c2x = px[b] - ux / ul * distance * .28, c2z = pz[b] - uz / ul * distance * .28;
        double[] c = curves[a] = new double[18];
        for (int k = 0; k <= 8; k++) {
          double t = k / 8., s = 1 - t;
          c[k * 2] =
              s * s * s * px[a] + 3 * s * s * t * c1x + 3 * s * t * t * c2x + t * t * t * px[b];
          c[k * 2 + 1] =
              s * s * s * pz[a] + 3 * s * s * t * c1z + 3 * s * t * t * c2z + t * t * t * pz[b];
        }
      }
    }
  }
}
