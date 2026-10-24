package com.shnewbs.deepprimal.core;

import static com.shnewbs.deepprimal.core.TerrainNoise.*;

import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Immutable drainage, lake and channel geometry. No chunk/world reads or mutable simulation. */
public final class WatershedRegion {
  public static final int STEP = 32, SIDE = TerrainEngine.CELL / STEP + 1;
  private static final int N = SIDE * SIDE, TILE = 64, TILES = TerrainEngine.CELL / TILE;
  private static final double STREAM_AREA = 18_000;
  private static final int[] ORTHOGONAL = {-SIDE, SIDE, -1, 1};
  private final double ox, oz;
  private final boolean broadChannels;
  private final double[] raw = new double[N],
      flood = new double[N],
      area = new double[N],
      stage = new double[N];
  private final boolean[] ocean = new boolean[N], lakeHalo = new boolean[N];
  private final int[] parent = new int[N], order = new int[N], lakeAt = new int[N];
  private final Lake[] lakes;
  private final Segment[] segments;
  private final int[][] index;
  private final int streams;
  private volatile boolean[] supportedTunnels;

  public record Surface(
      double ground, double water, double sediment, double flowX, double flowZ, double speed) {}

  public record Probe(
      double x, double z, double stage, double radius, double area, boolean source) {}

  public record LakeProbe(
      double x,
      double z,
      double level,
      double area,
      double outletX,
      double outletZ,
      double minX,
      double minZ,
      double maxX,
      double maxZ) {}

  public record Statistics(
      int reaches, int headwaters, int lakes, int segments, long estimatedBytes) {}

  private static final class Lake {
    final int id;
    int outlet = -1, lowest;
    double min = Double.POSITIVE_INFINITY, spill, level = Double.NaN;
    int cells, x0 = SIDE, z0 = SIDE, x1, z1;

    Lake(int id) {
      this.id = id;
    }
  }

  private record Segment(
      double ax,
      double az,
      double bx,
      double bz,
      double sa,
      double sb,
      double ra,
      double rb,
      double aa,
      double ab,
      int sill,
      double crest,
      boolean source) {}

  public WatershedRegion(
      double ox, double oz, DoubleBinaryOperator height, DoubleBinaryOperator rain) {
    this(ox,oz,height,rain,false);
  }
  public WatershedRegion(
      double ox, double oz, DoubleBinaryOperator height, DoubleBinaryOperator rain, boolean broadChannels) {
    this.broadChannels=broadChannels;
    this.ox = ox;
    this.oz = oz;
    Arrays.fill(parent, -1);
    Arrays.fill(order, -1);
    boolean[] seen = new boolean[N];
    int[] sorted = new int[N];
    var queue =
        new PriorityQueue<Integer>(
            Comparator.<Integer>comparingDouble(a -> flood[a]).thenComparingInt(a -> a));
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        raw[a] = height.applyAsDouble(ox + x * STEP, oz + z * STEP);
        flood[a] = Math.max(TerrainEngine.SEA, raw[a]);
        area[a] = STEP * STEP * rain.applyAsDouble(ox + x * STEP, oz + z * STEP);
      }
    // Elevation alone does not make an inland depression part of the ocean.
    var sea = new ArrayDeque<Integer>();
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        if ((x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1)
            && raw[a] <= TerrainEngine.SEA
            && !ocean[a]) {
          ocean[a] = true;
          sea.add(a);
        }
      }
    while (!sea.isEmpty()) {
      int a = sea.removeFirst(), x = a % SIDE, z = a / SIDE;
      seen[a] = true;
      stage[a] = TerrainEngine.SEA;
      area[a] = 0;
      for (int d : ORTHOGONAL) {
        int b = a + d;
        if (b < 0
            || b >= N
            || Math.abs(x - b % SIDE) + Math.abs(z - b / SIDE) != 1
            || ocean[b]
            || raw[b] > TerrainEngine.SEA) continue;
        ocean[b] = true;
        sea.add(b);
      }
    }
    // Only coast/boundary land enters the heap; ocean interiors do no routing work.
    for (int z = 0; z < SIDE; z++)
      for (int x = 0; x < SIDE; x++) {
        int a = z * SIDE + x;
        if (seen[a]) continue;
        int seaReceiver = -1;
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx >= 0 && zz >= 0 && xx < SIDE && zz < SIDE && ocean[zz * SIDE + xx])
              seaReceiver = zz * SIDE + xx;
          }
        if (seaReceiver >= 0 || x == 0 || z == 0 || x == SIDE - 1 || z == SIDE - 1) {
          seen[a] = true;
          parent[a] = seaReceiver;
          queue.add(a);
        }
      }
    int count = 0;
    while (!queue.isEmpty()) {
      int a = queue.remove();
      sorted[count] = a;
      order[a] = count++;
      int x = a % SIDE, z = a / SIDE;
      for (int dz = -1; dz <= 1; dz++)
        for (int dx = -1; dx <= 1; dx++) {
          int xx = x + dx, zz = z + dz;
          if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
          int b = zz * SIDE + xx;
          if (seen[b]) continue;
          seen[b] = true;
          parent[b] = a;
          flood[b] = Math.max(raw[b], flood[a]);
          queue.add(b);
        }
    }
    int[] child = new int[N];
    Arrays.fill(child, -1);
    for (int n = count - 1; n >= 0; n--) {
      int a = sorted[n], b = parent[a];
      if (b >= 0) {
        area[b] += area[a];
        if (child[b] < 0 || area[a] > area[child[b]]) child[b] = a;
      }
    }
    var bodies = new ArrayList<Lake>();
    bodies.add(null);
    var search = new ArrayDeque<Integer>();
    for (int a = 0; a < N; a++) {
      if (lakeAt[a] != 0 || flood[a] - raw[a] < .01 || ocean[a]) continue;
      var lake = new Lake(bodies.size());
      lake.spill = flood[a];
      bodies.add(lake);
      lakeAt[a] = lake.id;
      search.add(a);
      while (!search.isEmpty()) {
        int b = search.removeFirst();
        lake.cells++;
        if (raw[b] < lake.min) {
          lake.min = raw[b];
          lake.lowest = b;
        }
        int x = b % SIDE, z = b / SIDE;
        lake.x0 = Math.min(lake.x0, x);
        lake.z0 = Math.min(lake.z0, z);
        lake.x1 = Math.max(lake.x1, x);
        lake.z1 = Math.max(lake.z1, z);
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx < 0 || zz < 0 || xx >= SIDE || zz >= SIDE) continue;
            int c = zz * SIDE + xx;
            if (lakeAt[c] == 0
                && !ocean[c]
                && flood[c] - raw[c] >= .01
                && Math.abs(flood[c] - lake.spill) < 1e-7) {
              lakeAt[c] = lake.id;
              search.add(c);
            }
          }
      }
    }
    lakes = bodies.toArray(Lake[]::new);
    for (int n = 0; n < count; n++) {
      int a = sorted[n], b = parent[a];
      double downstream = b < 0 ? Math.max(TerrainEngine.SEA, raw[a] - .15) : stage[b];
      stage[a] = Math.max(downstream, Math.floor(flood[a] * 16) / 16);
      if (lakeAt[a] > 0) {
        var lake = lakes[lakeAt[a]];
        if (Double.isNaN(lake.level)) {
          lake.outlet = a;
          lake.level =
              lake.cells >= 3 && lake.spill - lake.min >= 1.5
                  ? Math.floor(lake.spill * 16) / 16
                  : Double.NEGATIVE_INFINITY;
        }
        if (Double.isFinite(lake.level)) stage[a] = Math.max(downstream, lake.level);
      }
    }
    for (int a = 0; a < N; a++)
      if (lakeAt[a] > 0 && Double.isFinite(lakes[lakeAt[a]].level)) {
        int x = a % SIDE, z = a / SIDE;
        for (int dz = -2; dz <= 2; dz++)
          for (int dx = -2; dx <= 2; dx++) {
            int xx = x + dx, zz = z + dz;
            if (xx >= 0 && zz >= 0 && xx < SIDE && zz < SIDE) lakeHalo[zz * SIDE + xx] = true;
          }
      }
    var lines = new ArrayList<Segment>();
    int heads = 0;
    for (int a = 0; a < N; a++) {
      int b = parent[a];
      if (b < 0 || area[a] < STREAM_AREA || ocean[a]) continue;
      boolean source = child[a] < 0 || area[child[a]] < STREAM_AREA;
      if (source) heads++;
      int up = child[a], down = parent[b];
      double ax = px(a), az = pz(a), bx = px(b), bz = pz(b), len = Math.hypot(bx - ax, bz - az);
      double tx = up < 0 ? bx - ax : bx - px(up), tz = up < 0 ? bz - az : bz - pz(up);
      double ux = down < 0 ? bx - ax : px(down) - ax, uz = down < 0 ? bz - az : pz(down) - az;
      double tl = Math.max(1, Math.hypot(tx, tz)), ul = Math.max(1, Math.hypot(ux, uz));
      double c1x = ax + tx / tl * len * .28, c1z = az + tz / tl * len * .28;
      double c2x = bx - ux / ul * len * .28, c2z = bz - uz / ul * len * .28;
      double lastX = ax, lastZ = az;
      int sill =
          lakeAt[a] > 0 && lakes[lakeAt[a]].outlet == a && Double.isFinite(lakes[lakeAt[a]].level)
              ? lakeAt[a]
              : 0;
      for (int k = 1; k <= 8; k++) {
        double t = k / 8., s = 1 - t, before = (k - 1) / 8.;
        double x = s * s * s * ax + 3 * s * s * t * c1x + 3 * s * t * t * c2x + t * t * t * bx;
        double z = s * s * s * az + 3 * s * s * t * c1z + 3 * s * t * t * c2z + t * t * t * bz;
        lines.add(
            new Segment(
                lastX,
                lastZ,
                x,
                z,
                lerp(stage[a], stage[b], before),
                lerp(stage[a], stage[b], t),
                radius(lerp(area[a], area[b], before), source, before),
                radius(lerp(area[a], area[b], t), source, t),
                lerp(area[a], area[b], before),
                lerp(area[a], area[b], t),
                sill != 0 && k >= 5 && k <= 7 ? sill : 0,
                sill == 0 ? 0 : Math.floor(lakes[sill].level),
                source));
        lastX = x;
        lastZ = z;
      }
    }
    streams = heads;
    segments = lines.toArray(Segment[]::new);
    @SuppressWarnings("unchecked")
    ArrayList<Integer>[] buckets = new ArrayList[TILES * TILES];
    for (int n = 0; n < segments.length; n++) {
      var s = segments[n];
      double margin = Math.max(s.ra, s.rb) + 36;
      int x0 = tile(Math.min(s.ax, s.bx) - margin, ox),
          x1 = tile(Math.max(s.ax, s.bx) + margin, ox);
      int z0 = tile(Math.min(s.az, s.bz) - margin, oz),
          z1 = tile(Math.max(s.az, s.bz) + margin, oz);
      for (int z = z0; z <= z1; z++)
        for (int x = x0; x <= x1; x++) {
          int i = z * TILES + x;
          if (buckets[i] == null) buckets[i] = new ArrayList<>();
          buckets[i].add(n);
        }
    }
    index = new int[buckets.length][];
    for (int n = 0; n < index.length; n++)
      if (buckets[n] != null) index[n] = buckets[n].stream().mapToInt(Integer::intValue).toArray();
  }

  private double radius(double area, boolean source, double t) {
    if(broadChannels) {
      double r=Math.clamp(1.4+Math.sqrt(area)/185,2.1,32);
      return source?lerp(1.3,r,smooth(t*2)):r;
    }
    double r = Math.clamp(.7 + Math.sqrt(area) / 410, 1.05, 26);
    return source ? lerp(.9, r, smooth(t * 2)) : r;
  }

  private double px(int a) {
    return ox + (a % SIDE) * STEP;
  }

  private double pz(int a) {
    return oz + (a / SIDE) * STEP;
  }

  private static int tile(double p, double origin) {
    return Math.clamp((int) Math.floor((p - origin) / TILE), 0, TILES - 1);
  }

  private double lakeWater(double x, double z) {
    int gx = Math.clamp((int) Math.floor((x - ox) / STEP), 0, SIDE - 2),
        gz = Math.clamp((int) Math.floor((z - oz) / STEP), 0, SIDE - 2);
    double u = Math.clamp((x - ox) / STEP - gx, 0, 1), v = Math.clamp((z - oz) / STEP - gz, 0, 1);
    int a = gz * SIDE + gx;
    double level = Double.NaN;
    for (int n = 0; n < 4; n++) {
      int c = a + (n & 1) + (n >>> 1) * SIDE;
      if (lakeAt[c] > 0 && Double.isFinite(lakes[lakeAt[c]].level))
        level =
            Double.isNaN(level) ? lakes[lakeAt[c]].level : Math.min(level, lakes[lakeAt[c]].level);
    }
    if (!Double.isFinite(level)) return Double.NaN;
    double coarse = lerp(lerp(raw[a], raw[a + 1], u), lerp(raw[a + SIDE], raw[a + SIDE + 1], u), v);
    return coarse < level ? level : Double.NaN;
  }

  /** V6-only roof footprint. A whole cross-section must have rock cover before its valley cut is suppressed. */
  public boolean tunnelSupport(double x,double z,double uncut,DoubleBinaryOperator height) {
    boolean[] eligible=supportedTunnels;
    if(eligible==null)synchronized(this){
      eligible=supportedTunnels;
      if(eligible==null){
        eligible=new boolean[segments.length];
        for(int id=0;id<segments.length;id++) {
          var s=segments[id];double vx=s.bx-s.ax,vz=s.bz-s.az,len=Math.hypot(vx,vz);
          if(Math.min(s.sa,s.sb)<=TerrainEngine.SEA+8||height.applyAsDouble((s.ax+s.bx)*.5,(s.az+s.bz)*.5)-(s.sa+s.sb)*.5<9)continue;
          boolean covered=true;
          for(int n=0;n<3&&covered;n++) {
            double t=n*.5,cx=lerp(s.ax,s.bx,t),cz=lerp(s.az,s.bz,t),r=lerp(s.ra,s.rb,t)+2,stage=lerp(s.sa,s.sb,t);
            for(int side=-1;side<=1;side++)if(height.applyAsDouble(cx-vz/len*r*side,cz+vx/len*r*side)<stage+7){covered=false;break;}
          }
          eligible[id]=covered;
        }
        supportedTunnels=eligible;
      }
    }
    var candidates=index[tile(z,oz)*TILES+tile(x,ox)];if(candidates==null)return false;
    for(int id:candidates)if(eligible[id]) {
      var s=segments[id];double vx=s.bx-s.ax,vz=s.bz-s.az,len=vx*vx+vz*vz;
      double t=Math.clamp(((x-s.ax)*vx+(z-s.az)*vz)/len,0,1),r=lerp(s.ra,s.rb,t),stage=lerp(s.sa,s.sb,t);
      double bank=Math.clamp(5+r*.4+Math.sqrt(Math.max(0,uncut-stage))*2,5,24);
      if(Math.hypot(x-s.ax-vx*t,z-s.az-vz*t)<r+bank+1.5)return true;
    }
    return false;
  }

  public Surface sample(double x, double z, double uncut) {
    int gx = Math.clamp((int) Math.floor((x - ox) / STEP), 0, SIDE - 2),
        gz = Math.clamp((int) Math.floor((z - oz) / STEP), 0, SIDE - 2);
    double u = Math.clamp((x - ox) / STEP - gx, 0, 1), v = Math.clamp((z - oz) / STEP - gz, 0, 1);
    int a = gz * SIDE + gx;
    boolean seaNear = ocean[a] || ocean[a + 1] || ocean[a + SIDE] || ocean[a + SIDE + 1];
    if (uncut < TerrainEngine.SEA && seaNear)
      return new Surface(Math.min(uncut, TerrainEngine.SEA - 1.001), TerrainEngine.SEA, 1, 0, 0, 0);
    double water = Double.NaN, ground = uncut, sediment = 0, fx = 0, fz = 0, speed = 0;
    double sill = Double.NEGATIVE_INFINITY,
        joinedBed = Double.POSITIVE_INFINITY,
        bankFloor = Double.NEGATIVE_INFINITY;
    double lakeLevel = Double.NaN;
    for (int corner = 0; corner < 4; corner++) {
      int c = a + (corner & 1) + (corner >>> 1) * SIDE;
      if (lakeAt[c] > 0 && Double.isFinite(lakes[lakeAt[c]].level))
        lakeLevel =
            Double.isNaN(lakeLevel)
                ? lakes[lakeAt[c]].level
                : Math.min(lakeLevel, lakes[lakeAt[c]].level);
    }
    if (Double.isFinite(lakeLevel)) {
      double coarse =
          lerp(lerp(raw[a], raw[a + 1], u), lerp(raw[a + SIDE], raw[a + SIDE + 1], u), v);
      double detail = smooth((coarse - lakeLevel - 1) / 4);
      ground = lerp(coarse, uncut, detail);
      sediment = 1 - detail;
      double shoreStep =
          Math.max(
                  Math.max(
                      Math.abs(raw[a + 1] - raw[a]), Math.abs(raw[a + SIDE + 1] - raw[a + SIDE])),
                  Math.max(
                      Math.abs(raw[a + SIDE] - raw[a]), Math.abs(raw[a + SIDE + 1] - raw[a + 1])))
              / STEP;
      if (coarse < lakeLevel + shoreStep * 1.01)
        joinedBed = Math.min(joinedBed, Math.floor(lakeLevel) - 1.001);
      if (coarse >= lakeLevel && coarse < lakeLevel + shoreStep * 1.5)
        bankFloor = Math.max(bankFloor, lakeLevel + .35);
      if (coarse < lakeLevel) water = lakeLevel;
    }
    if (!Double.isFinite(water) && lakeHalo[a]) {
      for (int n = 0; n < 4; n++) {
        double neighbour =
            lakeWater(x + (n == 0 ? -1 : n == 1 ? 1 : 0), z + (n == 2 ? -1 : n == 3 ? 1 : 0));
        if (Double.isFinite(neighbour)) bankFloor = Math.max(bankFloor, neighbour + .35);
      }
    }
    int[] candidates = index[tile(z, oz) * TILES + tile(x, ox)];
    if (candidates != null)
      for (int id : candidates) {
        var s = segments[id];
        double vx = s.bx - s.ax, vz = s.bz - s.az, len = vx * vx + vz * vz;
        double t = Math.clamp(((x - s.ax) * vx + (z - s.az) * vz) / len, 0, 1);
        double dist = Math.hypot(x - s.ax - vx * t, z - s.az - vz * t), r = lerp(s.ra, s.rb, t);
        if (dist > r + 36) continue;
        double level = lerp(s.sa, s.sb, t),
            depth = Math.clamp(1.35 + r * .17, 1.5, 6),
            bank = Math.clamp(5 + r * .4 + Math.sqrt(Math.max(0, uncut - level)) * 2, 5, 24);
        if (dist < r + 1.01) {
          double grade = Math.max(0, (s.sa - s.sb) / Math.sqrt(len));
          joinedBed = Math.min(joinedBed, Math.floor(level - grade) - 1.001);
        }
        double grade = Math.max(0, (s.sa - s.sb) / Math.sqrt(len)),
            guard = level + .35 + grade * 1.5;
        if (dist < r + 1.5) bankFloor = Math.max(bankFloor, guard);
        else if (dist < r + bank + 1.5 && uncut < guard)
          bankFloor = Math.max(bankFloor, lerp(guard, uncut, smooth((dist - r - 1.5) / bank)));
        if (dist < r) {
          double edge = Math.clamp(dist / r, 0, 1);
          ground = Math.min(ground, level - depth + (depth - .3) * Math.pow(edge, 3));
          if (dist < Math.max(.8, r * .7)) {
            ground = Math.min(ground, Math.floor(level - grade) - 1.001);
            if (seaNear && uncut < TerrainEngine.SEA + 1)
              ground = Math.min(ground, TerrainEngine.SEA - 1.001);
          }
          if (Double.isNaN(water) || level < water) {
            water = level;
            double l = Math.sqrt(len);
            fx = vx / l;
            fz = vz / l;
            speed =
                Math.clamp(
                    .08 + Math.sqrt(Math.max(0, (s.sa - s.sb) / l)) * .9 + r * .006, .08, .85);
          }
          if (s.sill > 0) sill = Math.max(sill, s.crest - 1 + .001);
        } else {
          double blend = smooth((dist - r) / bank);
          ground = Math.min(ground, lerp(level + .35, uncut, blend));
        }
        sediment = Math.max(sediment, 1 - smooth((dist - r) / (bank + 8)));
      }
    if (Double.isFinite(water)) {
      water = Math.rint(Math.max(TerrainEngine.SEA, water) * 16) / 16;
      ground = Math.min(Math.min(ground, joinedBed), Math.ceil(water) - 1.001);
      if (Double.isFinite(sill))
        ground = Math.max(ground, Math.min(sill, Math.ceil(water) - 1.001));
    } else if (Double.isFinite(bankFloor)) ground = Math.max(ground, bankFloor);
    return new Surface(ground, water, sediment, fx, fz, speed);
  }

  public List<Probe> probes() {
    var out = new ArrayList<Probe>();
    for (int i = 0; i < segments.length; i += 8) {
      var s = segments[i];
      out.add(new Probe(s.ax, s.az, s.sa, s.ra, s.aa, s.source));
    }
    return List.copyOf(out);
  }

  public List<Probe> centerline() {
    var out = new ArrayList<Probe>();
    for (var s : segments) {
      out.add(new Probe(s.ax, s.az, s.sa, s.ra, s.aa, s.source));
      out.add(new Probe(s.bx, s.bz, s.sb, s.rb, s.ab, s.source));
    }
    return List.copyOf(out);
  }

  public List<LakeProbe> lakeProbes() {
    var out = new ArrayList<LakeProbe>();
    for (int i = 1; i < lakes.length; i++) {
      var l = lakes[i];
      if (Double.isFinite(l.level))
        out.add(
            new LakeProbe(
                px(l.lowest),
                pz(l.lowest),
                l.level,
                l.cells * (double) STEP * STEP,
                px(l.outlet),
                pz(l.outlet),
                ox + (l.x0 - 1) * STEP,
                oz + (l.z0 - 1) * STEP,
                ox + (l.x1 + 1) * STEP,
                oz + (l.z1 + 1) * STEP));
    }
    return List.copyOf(out);
  }

  public Statistics statistics() {
    long refs = 0;
    for (var bucket : index) if (bucket != null) refs += bucket.length;
    return new Statistics(
        segments.length / 8,
        streams,
        lakeProbes().size(),
        segments.length,
        N * (8L * 4 + 4 * 3 + 2) + segments.length * 112L + index.length * 8L + refs * 4);
  }

  public void verify() {
    for (int a = 0; a < N; a++) {
      if (ocean[a] && lakeAt[a] != 0)
        throw new AssertionError("Connected ocean entered a lake component");
      int b = parent[a];
      if (b >= 0 && order[a] >= 0 && (order[b] >= order[a] || stage[b] > stage[a] + 1e-7))
        throw new AssertionError("Watershed routing/head order");
    }
  }
}
