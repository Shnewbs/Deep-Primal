package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bounded, absolute-coordinate V8 marine connectivity admission. Region edges
 * are NEVER independently marine seeds. The opt-in routing v2 policy uses
 * explicit deep-continental marine cores, then joins 32-block water lattice
 * components across neighboring 8192-block watershed regions. A closed basin
 * crossing a region edge therefore does not become ocean simply because its
 * low cells touch that edge.
 *
 * <p>This is conservative sampled-mask admission, not a continuous physical
 * shoreline proof or a replacement for the existing WatershedRegion routing.
 * Uncertain or work-cap-exceeded components are NOT certified as ocean. There
 * is no native generation, block mutation, water volume or chemical transport.
 * Native coast/river/lake and old-new-border acceptance remains mandatory.
 */
public final class V8OceanConnectivity {
  public static final int POLICY_REVISION = 2;
  static final int STEP = WatershedRegion.STEP;
  static final int SIDE = TerrainEngine.CELL / STEP;
  private static final int CELL = TerrainEngine.CELL;
  private static final double SEA = V8WaterDomainAdmission.SEA_PLANE;
  private static final double MARINE_CONTINENTAL_MAX = -0.25;
  private static final double MARINE_HEIGHT_MAX = SEA - 24;
  private static final int MAX_CACHED_REGIONS = 4;
  private static final int MAX_SEARCH_REGIONS = 8;
  private static final int MAX_COMPONENTS_SEARCHED = 96;

  /** An exact world-coordinate height/continentalness observation. */
  public record Point(double height, double continentalness) {
    public Point {
      if (!Double.isFinite(height) || !Double.isFinite(continentalness))
        throw new IllegalArgumentException("nonfinite V8 marine sample");
    }
  }

  @FunctionalInterface
  public interface Sampler { Point sample(int worldX, int worldZ); }

  private record ComponentRef(int originX, int originZ, int label) {}
  private record Gateway(int nextX, int nextZ) {}
  private static final class Component {
    boolean marineCore;
    final List<Gateway> edges = new ArrayList<>();
  }

  private static final class Region {
    final int originX, originZ;
    final int[] labels = new int[SIDE * SIDE];
    final List<Component> components = new ArrayList<>();

    Region(int ox, int oz, Sampler sampler) {
      originX = ox;
      originZ = oz;
      Arrays.fill(labels, -1);
      byte[] wet = new byte[labels.length];
      byte[] marine = new byte[labels.length];
      for (int z = 0; z < SIDE; z++)
        for (int x = 0; x < SIDE; x++) {
          int i = z * SIDE + x;
          Point p = Objects.requireNonNull(sampler.sample(ox + x * STEP, oz + z * STEP),
              "V8 marine lattice sample");
          if (p.height() <= SEA) {
            wet[i] = 1;
            if (p.height() <= MARINE_HEIGHT_MAX
                && p.continentalness() <= MARINE_CONTINENTAL_MAX)
              marine[i] = 1;
          }
        }
      int[] queue = new int[labels.length];
      for (int i = 0; i < labels.length; i++) {
        if (wet[i] == 0 || labels[i] >= 0) continue;
        Component component = new Component();
        int id = components.size();
        components.add(component);
        int start = 0, end = 0;
        queue[end++] = i;
        labels[i] = id;
        while (start < end) {
          int at = queue[start++];
          int x = at % SIDE, z = at / SIDE;
          if (marine[at] != 0) component.marineCore = true;
          for (int d = 0; d < 4; d++) {
            int nx = x + (d == 0 ? -1 : d == 1 ? 1 : 0);
            int nz = z + (d == 2 ? -1 : d == 3 ? 1 : 0);
            if (nx < 0 || nx >= SIDE || nz < 0 || nz >= SIDE) {
              component.edges.add(new Gateway(ox + nx * STEP, oz + nz * STEP));
              continue;
            }
            int n = nz * SIDE + nx;
            if (wet[n] != 0 && labels[n] < 0) {
              labels[n] = id;
              queue[end++] = n;
            }
          }
        }
      }
    }

    int label(int worldX, int worldZ) {
      int x = (worldX - originX) / STEP;
      int z = (worldZ - originZ) / STEP;
      return labels[z * SIDE + x];
    }
  }

  private final Sampler sampler;
  private final LinkedHashMap<Long, Region> cache = new LinkedHashMap<>(8, .75f, true) {
    @Override protected boolean removeEldestEntry(Map.Entry<Long, Region> eldest) {
      return size() > MAX_CACHED_REGIONS;
    }
  };

  /** A compiled saved V8 graph supplies the ONLY allowed observation source. */
  public V8OceanConnectivity(DensityGraph graph) {
    Objects.requireNonNull(graph, "saved V8 graph");
    DensityGraph.Sampler density = graph.newSampler();
    this.sampler = (x, z) -> new Point(
        density.channel("height", x, SEA, z),
        density.channel("continentalness", x, SEA, z));
  }

  /** Test-only injection for bounded component/seam fixtures; no random state. */
  V8OceanConnectivity(Sampler sampler) {
    this.sampler = Objects.requireNonNull(sampler, "marine sampler");
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private Region region(int nodeX, int nodeZ, Map<Long, Region> visit) {
    int ox = Math.multiplyExact(Math.floorDiv(nodeX, CELL), CELL);
    int oz = Math.multiplyExact(Math.floorDiv(nodeZ, CELL), CELL);
    if (ox < -DensityGraph.WORLD_LIMIT || oz < -DensityGraph.WORLD_LIMIT
        || (long) ox + CELL > DensityGraph.WORLD_LIMIT
        || (long) oz + CELL > DensityGraph.WORLD_LIMIT)
      return null;
    long key = key(ox, oz);
    Region page = visit.get(key);
    if (page != null) return page;
    if (visit.size() >= MAX_SEARCH_REGIONS) return null;
    page = cache.get(key);
    if (page == null) {
      page = new Region(ox, oz, sampler);
      cache.put(key, page);
    }
    visit.put(key, page);
    return page;
  }

  private ComponentRef at(int nodeX, int nodeZ, Map<Long, Region> visit) {
    Region page = region(nodeX, nodeZ, visit);
    if (page == null) return null;
    int label = page.label(nodeX, nodeZ);
    return label < 0 ? null : new ComponentRef(page.originX, page.originZ, label);
  }

  private boolean reachesMarineCore(ComponentRef first, Map<Long, Region> visit) {
    ArrayDeque<ComponentRef> queue = new ArrayDeque<>();
    Set<ComponentRef> seen = new HashSet<>();
    queue.add(first);
    seen.add(first);
    while (!queue.isEmpty()) {
      ComponentRef cursor = queue.removeFirst();
      Region page = visit.get(key(cursor.originX(), cursor.originZ()));
      if (page == null) throw new IllegalStateException("missing bounded ocean region");
      Component component = page.components.get(cursor.label());
      if (component.marineCore) return true;
      for (Gateway gate : component.edges) {
        ComponentRef next = at(gate.nextX(), gate.nextZ(), visit);
        if (next == null || seen.contains(next)) continue;
        if (seen.size() >= MAX_COMPONENTS_SEARCHED) return false;
        seen.add(next);
        queue.addLast(next);
      }
    }
    return false;
  }

  /**
   * A four-corner, connected marine-core witness. Missing, dry and unresolved
   * corners fail CLOSED; no local region boundary grants ocean authority.
   */
  public synchronized boolean isMarineAt(int worldX, int worldZ) {
    int lowX = Math.floorDiv(worldX, STEP) * STEP;
    int lowZ = Math.floorDiv(worldZ, STEP) * STEP;
    long highX = (long) lowX + STEP;
    long highZ = (long) lowZ + STEP;
    if (lowX < -DensityGraph.WORLD_LIMIT || lowZ < -DensityGraph.WORLD_LIMIT
        || highX > DensityGraph.WORLD_LIMIT || highZ > DensityGraph.WORLD_LIMIT)
      return false;
    Map<Long, Region> visit = new HashMap<>();
    Set<ComponentRef> proven = new HashSet<>();
    for (int z = 0; z < 2; z++)
      for (int x = 0; x < 2; x++) {
        int nodeX = x == 0 ? lowX : (int) highX;
        int nodeZ = z == 0 ? lowZ : (int) highZ;
        ComponentRef component = at(nodeX, nodeZ, visit);
        if (component == null) return false;
        if (!proven.contains(component) && !reachesMarineCore(component, visit)) return false;
        proven.add(component);
      }
    return true;
  }

  public synchronized int cachedRegionCount() { return cache.size(); }
}
