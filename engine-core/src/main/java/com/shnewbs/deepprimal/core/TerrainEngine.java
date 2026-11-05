package com.shnewbs.deepprimal.core;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

import java.util.*;

/** Original versioned continental planner. Immutable drainage results are shared between chunks. */
public final class TerrainEngine {
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
    public Sample withSlope(double value) {
      return new Sample(
          ground, water, rain, temperature, value, sediment, flowX, flowZ, speed, rock);
    }

    public boolean wet() {
      return Double.isFinite(water);
    }
  }

  private record Land(
      int x, int z, double cx, double cz, double rx, double rz, double cos, double sin) {}

  private record Location(Land land, double mask) {}

  public final TerrainNoise noise;
  public final int revision;
  public final int channelRevision;
  public final TerrainOptions options;
  public final Landscape landscape;
  private final Landforms landforms;
  private final LinkedHashMap<Long, Sample> foundations = new LinkedHashMap<>();
  private record BiomeColumn(Sample sample,Geology.Column geology,int surface) {}
  private final LinkedHashMap<Long,BiomeColumn> biomeColumns=new LinkedHashMap<>();
  private final java.util.concurrent.ConcurrentHashMap<Long, Land> shapes =
      new java.util.concurrent.ConcurrentHashMap<>();
  private final LinkedHashMap<Long, Drainage> cache = new LinkedHashMap<>(16, .75f, true);

  private record Planned(long key, WatershedRegion region) {}

  private volatile Planned lastWatershed;
  private final LinkedHashMap<Long, WatershedRegion> watersheds =
      new LinkedHashMap<>(8, .75f, true);

  public TerrainEngine(long seed) {
    this(seed, 1);
  }

  public TerrainEngine(long seed, int revision) { this(seed, revision, TerrainOptions.DEFAULT); }

  public TerrainEngine(long seed, int revision, TerrainOptions options) { this(seed, revision, options, 0); }

  /** {@code channelRevision} 1-8 are saved V7 world options; 0 replays legacy worlds exactly. */
  public TerrainEngine(long seed, int revision, TerrainOptions options, int channelRevision) {
    if (revision < 1 || revision > 7) throw new IllegalArgumentException("Terrain revision");
    if (channelRevision < 0 || channelRevision > 8 || channelRevision != 0 && revision != 7)
      throw new IllegalArgumentException("Channel revision requires V7 geography");
    this.revision = revision;
    this.channelRevision = channelRevision;
    noise = new TerrainNoise(seed, revision >= 7);
    this.options = java.util.Objects.requireNonNull(options);
    landscape = new Landscape(noise, options, revision);
    landforms = new Landforms(noise, options);
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
    if (revision >= 7) return new Land(x,z,
        x*(double)CELL+(noise.hash(x,z,1)-.5)*900,
        z*(double)CELL+(noise.hash(x,z,2)-.5)*900,
        2200+noise.hash(x,z,3)*1500,2100+noise.hash(x,z,4)*1500,
        Math.cos(angle),Math.sin(angle));
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
    if (revision >= 7) {
      double width=.52+noise.hash(i.x,i.z,4300)*.28;
      double branch=.30+noise.hash(i.x,i.z,4301)*.25;
      double bend=(noise.hash(i.x,i.z,4302)-.5)*.7;
      double core=1-Math.hypot(nx/.80,nz/width);
      double a=(1-Math.hypot((nx-.40)/.53,(nz+bend)/branch))*.75;
      double b=(1-Math.hypot((nx+.48)/.48,(nz-bend)/(.75-branch)))*.75;
      // Keep varied lobes inside their own drainage domain, with an ocean margin.
      double envelope=1-Math.hypot(dx,dz)/2950;
      return Math.min(envelope,Math.max(core,Math.max(a,b)))+coast;
    }
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
    if (revision >= 6) return landscape.rain(x, z);
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
    double coastalDetail = 0;
    if (revision >= 3 && Math.abs(m) < .18)
      coastalDetail =
          (noise.field(x, z, 64, 73) * 2.2 + noise.field(x, z, 180, 77) * 4.5)
              * (1 - smooth(Math.abs(m) / .18));
    if (m < 0 && revision >= 3) {
      double shelf = smooth(-m / .12);
      return SEA
          - 3
          - 44 * shelf
          - 85 * smooth((-m - .12) / .55)
          + noise.field(x, z, 270, 33) * 13 * shelf
          + coastalDetail;
    }
    if (m < 0)
      return SEA
          - 5
          - 42 * smooth(-m / .12)
          - 85 * smooth((-m - .12) / .55)
          + noise.field(x, z, 270, 33) * 13;
    if (revision >= 7) {
      double inland=smooth(m/.24);
      double height=SEA-3+inland*landforms.relief(x,z)+Math.min(m,.15)*30+coastalDetail;
      return landscape.relief(x,z,height,inland);
    }
    double inland = smooth(m / .22), plateau = 22 + noise.field(x, z, 1100, 35) * 28;
    double province = smooth((noise.field(x, z, 2200, 39) + .30) / .65);
    double warpX = x + noise.field(x, z, 430, 44) * 130,
        warpZ = z + noise.field(x, z, 430, 48) * 130;
    double ridge = 1 - Math.abs(noise.field(warpX, warpZ, 780, 51));
    double peaks = Math.pow(ridge, 3) * province * (110 + noise.field(x, z, 1800, 57) * 80);
    if (revision >= 6) {
      double resistance = .75 + .25 * noise.field(x, z, 2300, 1803);
      peaks *= options.mountainScale() * resistance;
    }
    double hills =
        noise.field(x, z, 240, 61) * 24
            + noise.field(x, z, 65, 65) * 6
            + noise.noise(x, z, 19, 70) * 1.3;
    double ground = SEA - 3 + inland * (plateau + peaks + hills) + Math.min(m, .15) * 30;
    if (revision >= 6) return landscape.relief(x, z, ground + coastalDetail, inland);
    return revision >= 3 ? ground + coastalDetail : ground;
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

  private WatershedRegion watershed(Land l) {
    long key = ((long) l.x << 32) ^ (l.z & 0xffffffffL);
    Planned last = lastWatershed;
    return last != null && last.key == key ? last.region : loadWatershed(l, key);
  }

  private synchronized WatershedRegion loadWatershed(Land l, long key) {
    var d = watersheds.get(key);
    if (d == null) {
      d =
          new WatershedRegion(
              l.cx - CELL / 2., l.cz - CELL / 2., (x, z) -> raw(l, x, z), this::rainfall, revision >= 7, channelRevision >= 1,
              channelRevision >= 2 ? noise : null,
              channelRevision >= 3 && (channelRevision <= 6 || channelRevision == 8),
              channelRevision >= 4 && (channelRevision <= 6 || channelRevision == 8),
              channelRevision >= 5 && (channelRevision <= 6 || channelRevision == 8),
              null, null, channelRevision == 6 || channelRevision == 7 || channelRevision == 8,
              channelRevision == 8);
      watersheds.put(key, d);
      while (watersheds.size() > 6) watersheds.remove(watersheds.keySet().iterator().next());
    }
    lastWatershed = new Planned(key, d);
    return d;
  }

  public WatershedRegion waterways(double x, double z) {
    if (revision < 3) throw new IllegalStateException("Watershed geometry requires revision 3");
    return watershed(locate(x, z).land);
  }

  public synchronized int cachedRegions() {
    return revision >= 3 ? watersheds.size() : cache.size();
  }

  public int biome(double x, double z) {
    if (revision >= 6) return biomeColumn(x,z).surface;
    double h = raw(x, z),
        r = rainfall(x, z),
        t = revision >= 4 ? temperature(x, z, h) : .62 + noise.field(x, z, 6500, 85) * .40 - Math.max(0, h - 160) / 420;
    if (h < SEA) return 3;
    if (h > 245) return 7;
    if (t < .20) return 5;
    if (t < .36) return 4;
    if (r < .32) return 2;
    if (h < SEA + 10 && r > .77) return 6;
    return forest(x, z) > .45 && r > .5 ? 1 : 0;
  }

  public int biome(double x, double y, double z) {
    if (revision < 6) return biome(x,z);
    var column=biomeColumn(x,z);var s=column.sample;
    if (options.caves() && y < s.ground()-CaveNetwork.ROOF) {
      var rock = column.geology.rock((int)y);
      if (rock == Geology.Rock.CALCITE) return 39;
      if (s.rain()>.70 && s.temperature()>.4) return 38;
    }
    return column.surface;
  }
  private BiomeColumn biomeColumn(double x,double z) {
    boolean cacheable=x==(int)x&&z==(int)z;
    long key=((long)(int)x<<32)|((int)z&0xffffffffL);
    if(cacheable)synchronized(biomeColumns){var cached=biomeColumns.get(key);if(cached!=null)return cached;}
    var s=sample(x,z);var created=new BiomeColumn(s,new Geology(noise).column((int)x,(int)z,s),landscape.biome(x,z,s,ocean(x,z)));
    if(cacheable)synchronized(biomeColumns){if(biomeColumns.size()>=4096)biomeColumns.remove(biomeColumns.keySet().iterator().next());biomeColumns.put(key,created);}
    return created;
  }

  public Sample sample(double x, double z) {
    if (revision >= 6) {
      var s=column(x,z);
      return s.withSlope(Math.hypot(column(x+1,z).ground()-column(x-1,z).ground(),column(x,z+1).ground()-column(x,z-1).ground())*.5);
    }
    return revision >= 4 ? contained(x, z, true) : sample(x, z, true);
  }

  /** Bulk V3 callers derive shaped slope from a shared halo; legacy metadata remains exact. */
  public Sample column(double x, double z) {
    if (revision >= 6) {
      var s=hydraulicColumn(x,z);
      double uncut=raw(x,z);
      return landscape.surface(x,z,s,uncut,tunnelSupport(x,z,uncut));
    }
    return revision >= 4 ? contained(x, z, false) : sample(x, z, revision < 3);
  }

  private boolean tunnelSupport(double x,double z,double uncut) {
    return revision>=6&&options.riverTunnels()&&waterways(x,z).tunnelSupport(x,z,uncut,this::raw);
  }
  public boolean tunnel(double x,double z) {
    var h=hydraulicColumn(x,z);double uncut=raw(x,z);
    return landscape.tunnel(x,z,h,uncut)&&tunnelSupport(x,z,uncut);
  }
  public double surfaceWithoutIce(double x,double z) {
    var h=hydraulicColumn(x,z);double uncut=raw(x,z);
    return tunnelSupport(x,z,uncut)&&(!h.wet()||landscape.tunnel(x,z,h,uncut))?Math.max(uncut,h.ground()):h.ground();
  }
  public Sample hydraulicColumn(double x,double z) { return contained(x,z,false); }
  private Sample foundation(double x,double z) {
    if (revision < 6 || x!=Math.rint(x) || z!=Math.rint(z) || x<Integer.MIN_VALUE || x>Integer.MAX_VALUE || z<Integer.MIN_VALUE || z>Integer.MAX_VALUE)
      return sample(x,z,false);
    long key=((long)(int)x<<32)|((int)z&0xffffffffL);
    synchronized(foundations) { var old=foundations.get(key);if(old!=null)return old; }
    var result=sample(x,z,false);
    synchronized(foundations) {
      var old=foundations.get(key);if(old!=null)return old;
      if(foundations.size()>=8192)foundations.pollFirstEntry();
      foundations.put(key,result);
    }
    return result;
  }

  /** Bank tops must contain the actual discretized fluid, including diagonal bends. */
  private Sample contained(double x, double z, boolean gradient) {
    Sample s = revision >= 6 ? foundation(x,z) : sample(x, z, gradient);
    if (s.wet()) return s;
    double bank = s.ground();
    for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
      if (dx == 0 && dz == 0) continue;
      Sample n = revision >= 6 ? foundation(x+dx,z+dz) : sample(x + dx, z + dz, false);
      if (n.wet()) bank = Math.max(bank, Math.ceil(n.water()) - .001);
    }
    return bank == s.ground() ? s : new Sample(bank, s.water(), s.rain(), s.temperature(),
        s.slope(), s.sediment(), s.flowX(), s.flowZ(), s.speed(), s.rock());
  }

  /** This tests continental ocean geometry, never the elevation of an inland lake. */
  public boolean ocean(double x, double z) { return locate(x, z).mask < 0 && raw(x, z) < SEA; }

  public double temperature(double x, double z, double height) {
    if (revision >= 6) return landscape.temperature(x,z,height);
    double latitude = revision >= 4 ? .18 * Math.cos(z / 12000.) : 0;
    return Math.clamp(.62 + latitude + noise.field(x, z, 6500, 85) * .4
        - Math.max(0, height - 160) / 420, 0, 1);
  }

  private Sample sample(double x, double z, boolean gradient) {
    Location location = locate(x, z);
    Land l = location.land;
    double ground = raw(location.mask, x, z), uncut = ground, rain = rainfall(x, z);
    double temperature =
        temperature(x, z, ground);
    double slope =
        gradient
            ? Math.hypot(raw(l, x + 3, z) - raw(l, x - 3, z), raw(l, x, z + 3) - raw(l, x, z - 3))
                / 6
            : 0;
    int rock = (int) Math.floor((noise.field(x, z, 1500, 91) + 1) * 2.5) % 4;
    if (ground < SEA && (revision < 3 || location.mask < 0))
      return new Sample(
          revision >= 3 ? Math.min(ground, SEA - 1.001) : ground,
          SEA,
          rain,
          temperature,
          slope,
          1,
          0,
          0,
          0,
          rock);
    if (revision >= 3) {
      var v = watershed(l).sample(x, z, ground);
      return new Sample(
          v.ground(),
          v.water(),
          rain,
          temperature,
          slope,
          v.sediment(),
          v.flowX(),
          v.flowZ(),
          v.speed(),
          rock);
    }
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
    if (revision >= 3) {
      waterways(x, z).verify();
      return;
    }
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
