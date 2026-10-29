package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Opt-in, saved-profile-bound V8 drainage planning over V8's own pre-hydrology
 * height channel. Reuses the versioned WatershedRegion priority-flood, lake,
 * channel and outlet planner, rather than projecting V7 river geometry onto V8.
 *
 * <p>All region origins and climate inputs use absolute world coordinates.
 * This supplies an actual deterministic *planned* routed-water observation for
 * V8 material exclusion, not a native fluid initializer or a volume ledger.
 * V8 density/chunk integration, cross-region outlet continuity and Minecraft
 * world/save/graphics acceptance must be completed before native activation.
 * The class never loads chunks, touches blocks or changes physical water.
 */
public final class V8RoutedWaterSource implements V8WaterDomainAdmission.WaterSource {
  /** Increment to change the authoring fingerprint when routing semantics change. */
  public static final int ROUTE_REVISION = 1;
  private static final int CELL = TerrainEngine.CELL;
  private static final int STEP = WatershedRegion.STEP;
  private static final int SIDE = WatershedRegion.SIDE;
  private static final int MAX_RESIDENT_REGIONS = 2;
  private static final String FINGERPRINT_DOMAIN = "deepprimal-v8-routed-regions-v1";

  /** Planned ground cut and water stage, not a physical fluid quantity. */
  public record RouteColumn(double originalGround, double plannedGround,
      V8WaterDomainAdmission.WaterKind kind, double plannedStage,
      double flowX, double flowZ, double speed) {
    public RouteColumn {
      Objects.requireNonNull(kind, "water kind");
      if (!Double.isFinite(originalGround) || !Double.isFinite(plannedGround))
        throw new IllegalArgumentException("nonfinite routed ground");
      if (kind == V8WaterDomainAdmission.WaterKind.DRY
          ? !Double.isNaN(plannedStage)
          : !Double.isFinite(plannedStage))
        throw new IllegalArgumentException("invalid routed stage");
    }
    public V8WaterDomainAdmission.WaterDatum datum() {
      return new V8WaterDomainAdmission.WaterDatum(kind, plannedStage);
    }
  }

  private static final class Region {
    final int originX, originZ;
    final double[] raw;
    final boolean[] connectedSea;
    final WatershedRegion waterways;

    Region(int ox, int oz, DensityGraph graph, TerrainEngine savedClimate, int channelRevision) {
      originX = ox;
      originZ = oz;
      raw = new double[SIDE * SIDE];
      connectedSea = new boolean[raw.length];
      DensityGraph.Sampler sampler = graph.newSampler();
      for (int z = 0; z < SIDE; z++)
        for (int x = 0; x < SIDE; x++) {
          double height = sampler.channel("height", (double) ox + x * STEP,
              V8WaterDomainAdmission.SEA_PLANE, (double) oz + z * STEP);
          if (!Double.isFinite(height))
            throw new IllegalArgumentException("nonfinite V8 routing height");
          raw[z * SIDE + x] = height;
        }
      // Reproduce the watershed's orthogonal sea-connectivity seed on its
      // exact V8 height lattice. Elevation alone cannot flood an inland basin.
      ArrayDeque<Integer> queue = new ArrayDeque<>();
      for (int z = 0; z < SIDE; z++)
        for (int x = 0; x < SIDE; x++) {
          int i = z * SIDE + x;
          if ((x == 0 || x == SIDE - 1 || z == 0 || z == SIDE - 1)
              && raw[i] <= V8WaterDomainAdmission.SEA_PLANE) {
            connectedSea[i] = true;
            queue.addLast(i);
          }
        }
      while (!queue.isEmpty()) {
        int i = queue.removeFirst(), x = i % SIDE, z = i / SIDE;
        for (int d = 0; d < 4; d++) {
          int nx = x + (d == 0 ? -1 : d == 1 ? 1 : 0);
          int nz = z + (d == 2 ? -1 : d == 3 ? 1 : 0);
          if (nx < 0 || nz < 0 || nx >= SIDE || nz >= SIDE) continue;
          int j = nz * SIDE + nx;
          if (connectedSea[j] || raw[j] > V8WaterDomainAdmission.SEA_PLANE) continue;
          connectedSea[j] = true;
          queue.addLast(j);
        }
      }
      // WatershedRegion uses off-lattice terrain probes for bank and oxbow
      // validation; never round those samples to this 32-block grid. Only
      // its initial drainage nodes can safely reuse the precomputed heights.
      waterways = new WatershedRegion(ox, oz,
          (x, z) -> {
            int at = latticeIndex(x, z, ox, oz);
            return at >= 0 ? raw[at] : sampler.channel("height", x,
                V8WaterDomainAdmission.SEA_PLANE, z);
          }, savedClimate::rainfall,
          true, channelRevision >= 1,
          channelRevision >= 2 ? savedClimate.noise : null,
          channelRevision >= 3, channelRevision >= 4, channelRevision >= 5);
    }

    private static int latticeIndex(double x, double z, int ox, int oz) {
      int gx = (int) Math.round((x - ox) / STEP);
      int gz = (int) Math.round((z - oz) / STEP);
      if (gx < 0 || gx >= SIDE || gz < 0 || gz >= SIDE
          || x != (double) ox + gx * STEP || z != (double) oz + gz * STEP)
        return -1;
      return gz * SIDE + gx;
    }

    boolean oceanAt(int x, int z) {
      int gx = Math.floorDiv(x - originX, STEP);
      int gz = Math.floorDiv(z - originZ, STEP);
      if (gx < 0 || gx >= SIDE - 1 || gz < 0 || gz >= SIDE - 1)
        throw new IllegalArgumentException("ocean query outside V8 routing region");
      int i = gz * SIDE + gx;
      return connectedSea[i] || connectedSea[i + 1]
          || connectedSea[i + SIDE] || connectedSea[i + SIDE + 1];
    }
  }

  private final V8GeneratorSnapshot saved;
  private final DensityGraph graph;
  private final TerrainEngine climate;
  private final String fingerprint;
  private final LinkedHashMap<Long, Region> regions = new LinkedHashMap<>(4, .75f, true) {
    @Override protected boolean removeEldestEntry(Map.Entry<Long, Region> eldest) {
      return size() > MAX_RESIDENT_REGIONS;
    }
  };

  private V8RoutedWaterSource(V8GeneratorSnapshot saved, String fingerprint) {
    this.saved = saved;
    this.fingerprint = fingerprint;
    this.graph = saved.density().compile(saved.seed());
    this.climate = new TerrainEngine(saved.seed(), V8SavedClimateSource.REFERENCE_TERRAIN_REVISION,
        saved.climateBinding().savedOptions(), saved.climateBinding().channelRevision());
  }

  /** Pin a new r5 authoring profile to *this* algorithm, never rewrite older saves. */
  public static V8GeneratorSnapshot author(V8GeneratorSnapshot revision4) {
    Objects.requireNonNull(revision4, "saved revision 4");
    if (revision4.version() != V8GeneratorSnapshot.CLIMATE_VERSION)
      throw new IllegalArgumentException("routing authoring requires explicit V8 revision 4");
    validateHeightChannel(revision4.density());
    return revision4.withHydrologyBinding(V8HydrologyBinding.pinned(fingerprint(revision4)));
  }

  /** Open only an r5 profile whose routing fingerprint matches its actual inputs. */
  public static V8RoutedWaterSource open(V8GeneratorSnapshot revision5, long savedWorldSeed) {
    Objects.requireNonNull(revision5, "saved revision 5");
    if (revision5.version() != V8GeneratorSnapshot.HYDROLOGY_VERSION
        || revision5.seed() != savedWorldSeed
        || !V8SurfacePolicy.FROZEN_R1.equals(revision5.surfacePolicy()))
      throw new IllegalArgumentException("saved world/revision/surface policy mismatch");
    validateHeightChannel(revision5.density());
    String expected = fingerprint(revision5);
    if (!expected.equals(revision5.hydrologyBinding().sourceFingerprint()))
      throw new IllegalArgumentException("V8 routed source differs from saved provenance");
    return new V8RoutedWaterSource(revision5, expected);
  }

  /** Authority for the existing admitted V8 ecotone path; no arbitrary callback digest. */
  public V8WaterDomainAdmission.Authority authority() {
    return new V8WaterDomainAdmission.Authority(saved.seed(), saved.density(),
        saved.climateBinding(), V8WaterDomainAdmission.MASK_INTERFACE_REVISION,
        fingerprint, this);
  }

  public String sourceFingerprint() { return fingerprint; }
  public synchronized int cachedRegionCount() { return regions.size(); }

  /** One pre-hydrology V8 graph, routed from its frozen absolute-coordinate height. */
  public synchronized RouteColumn plan(int worldX, int worldZ) {
    int ox = Math.multiplyExact(Math.floorDiv(worldX, CELL), CELL);
    int oz = Math.multiplyExact(Math.floorDiv(worldZ, CELL), CELL);
    if (ox < -DensityGraph.WORLD_LIMIT || oz < -DensityGraph.WORLD_LIMIT
        || (long) ox + CELL > DensityGraph.WORLD_LIMIT
        || (long) oz + CELL > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("entire watershed footprint must fit V8 graph domain");
    long key = ((long) ox << 32) ^ (oz & 0xffffffffL);
    Region region = regions.get(key);
    if (region == null) {
      region = new Region(ox, oz, graph, climate, saved.climateBinding().channelRevision());
      regions.put(key, region);
    }
    double original = graph.channel("height", worldX,
        V8WaterDomainAdmission.SEA_PLANE, worldZ);
    WatershedRegion.Surface routed = region.waterways.sample(worldX, worldZ, original);
    boolean wet = Double.isFinite(routed.water());
    V8WaterDomainAdmission.WaterKind kind;
    if (!wet) kind = V8WaterDomainAdmission.WaterKind.DRY;
    else if (routed.water() == V8WaterDomainAdmission.SEA_PLANE
        && original <= V8WaterDomainAdmission.SEA_PLANE && region.oceanAt(worldX, worldZ))
      kind = V8WaterDomainAdmission.WaterKind.OCEAN;
    else if (routed.speed() > 0) kind = V8WaterDomainAdmission.WaterKind.RIVER;
    else kind = V8WaterDomainAdmission.WaterKind.LAKE;
    return new RouteColumn(original, routed.ground(), kind,
        wet ? routed.water() : Double.NaN, routed.flowX(), routed.flowZ(), routed.speed());
  }

  @Override public V8WaterDomainAdmission.WaterDatum sample(int x, int z) {
    return plan(x, z).datum();
  }

  /** Reject arbitrary saved V8 definitions where height depends on Y/3D noise. */
  private static void validateHeightChannel(DensityGraph.Definition def) {
    Map<String, DensityGraph.Node> nodes = new LinkedHashMap<>();
    for (var n : def.nodes()) nodes.put(n.id(), n);
    if (!nodes.containsKey("height"))
      throw new IllegalArgumentException("routed V8 graph requires a height channel");
    inspectHeight("height", nodes, new java.util.HashSet<>());
  }

  private static void inspectHeight(String id, Map<String, DensityGraph.Node> nodes,
      java.util.Set<String> visited) {
    if (!visited.add(id)) return;
    var n = Objects.requireNonNull(nodes.get(id), "missing height dependency");
    if (n.op() == DensityGraph.Op.Y || n.op() == DensityGraph.Op.NOISE_3D)
      throw new IllegalArgumentException("V8 routed height must be Y-independent");
    for (String input : n.inputs()) inspectHeight(input, nodes, visited);
  }

  /** Architecture-neutral hash of routing semantics and exact saved graph/climate inputs. */
  public static String fingerprint(V8GeneratorSnapshot profile) {
    Objects.requireNonNull(profile, "profile");
    if (profile.version() != V8GeneratorSnapshot.CLIMATE_VERSION
        && profile.version() != V8GeneratorSnapshot.HYDROLOGY_VERSION)
      throw new IllegalArgumentException("fingerprint requires saved V8 climate binding");
    validateHeightChannel(profile.density());
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      writeString(out, FINGERPRINT_DOMAIN);
      out.writeInt(ROUTE_REVISION);
      out.writeLong(profile.seed());
      out.writeInt(profile.minY());
      out.writeInt(profile.height());
      writeString(out, profile.climateBinding().key());
      var definition = profile.density();
      out.writeInt(definition.version());
      writeString(out, definition.root());
      out.writeInt(definition.nodes().size());
      for (var n : definition.nodes()) {
        writeString(out, n.id());
        writeString(out, n.op().name());
        out.writeInt(n.inputs().size());
        for (String input : n.inputs()) writeString(out, input);
        out.writeInt(n.parameters().size());
        for (double v : n.parameters()) out.writeLong(Double.doubleToLongBits(v));
        NoiseSpec noise = n.noise();
        out.writeBoolean(noise != null);
        if (noise != null) {
          out.writeLong(Double.doubleToLongBits(noise.frequencyX()));
          out.writeLong(Double.doubleToLongBits(noise.frequencyY()));
          out.writeLong(Double.doubleToLongBits(noise.frequencyZ()));
          out.writeInt(noise.octaves());
          out.writeLong(Double.doubleToLongBits(noise.lacunarity()));
          out.writeLong(Double.doubleToLongBits(noise.gain()));
          out.writeLong(noise.salt());
        }
      }
      out.flush();
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray());
      return java.util.HexFormat.of().formatHex(digest);
    } catch (IOException | NoSuchAlgorithmException failure) {
      throw new IllegalStateException("SHA-256 routing provenance unavailable", failure);
    }
  }

  private static void writeString(DataOutputStream out, String value) throws IOException {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    out.writeInt(bytes.length);
    out.write(bytes);
  }
}
