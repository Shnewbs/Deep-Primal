package com.shnewbs.deepprimal.core;

/** Seeded folded strata, faults, intrusions, finite host-dependent deposits and soil horizons. */
public final class Geology {
  public enum Rock { GRANITE, ANDESITE, DIORITE, BASALT, TUFF, SANDSTONE, CALCITE, DEEPSLATE }
  public enum Lithology { SEDIMENTARY, IGNEOUS, METAMORPHIC }
  public enum Ore { NONE, COAL, COPPER, IRON, GOLD, REDSTONE, LAPIS, DIAMOND, EMERALD }
  public record Column(double bedding, double fault, int province, double intrusion,
      int soilDepth, double permeability, double veinX, double veinZ, double veinY,
      int veinType, double veinRadius) {
    public Rock rock(int y) {
      if (intrusion > .80 && y < bedding + 45) return Rock.GRANITE;
      if (intrusion < -.84 && y < bedding + 70) return Rock.BASALT;
      int layer = Math.floorMod((int) Math.floor((y + bedding + fault) / 23), 5);
      if (y < -48 && layer > 1) return Rock.DEEPSLATE;
      return switch (province) {
        case 0 -> switch (layer) { case 0, 1 -> Rock.SANDSTONE; case 2 -> Rock.CALCITE; default -> Rock.TUFF; };
        case 1 -> switch (layer) { case 0, 1 -> Rock.ANDESITE; case 2 -> Rock.BASALT; default -> Rock.TUFF; };
        default -> switch (layer) { case 0, 1 -> Rock.GRANITE; case 2 -> Rock.DIORITE; default -> Rock.CALCITE; };
      };
    }
    public Ore ore(int y, int bed) {
      return oreFrom(veinX, veinZ, veinY, veinType, veinRadius, y, bed);
    }
    // Shared exact predicate for a separately versioned bounded deposit field.
    Ore oreFrom(double offsetX, double offsetZ, double centerY, int type, double radius,
        int y, int bed) {
      if (y >= bed - 5) return Ore.NONE;
      double dx = offsetX / (radius * 1.7), dz = offsetZ / radius,
          dy = (y - centerY) / (radius * .45);
      if (dx * dx + dy * dy + dz * dz > 1) return Ore.NONE;
      Rock host = rock(y);
      return switch (type) {
        case 0 -> host == Rock.SANDSTONE || host == Rock.TUFF ? Ore.COAL : Ore.NONE;
        case 1 -> host == Rock.ANDESITE || host == Rock.CALCITE ? Ore.COPPER : Ore.NONE;
        case 2 -> Ore.IRON;
        case 3 -> host == Rock.GRANITE || host == Rock.DIORITE ? Ore.GOLD : Ore.NONE;
        case 4 -> host == Rock.BASALT || host == Rock.GRANITE ? Ore.REDSTONE : Ore.NONE;
        case 5 -> host == Rock.CALCITE ? Ore.LAPIS : Ore.NONE;
        case 6 -> host == Rock.BASALT || host == Rock.DEEPSLATE ? Ore.DIAMOND : Ore.NONE;
        default -> host == Rock.CALCITE || host == Rock.GRANITE ? Ore.EMERALD : Ore.NONE;
      };
    }
  }
  private final TerrainNoise noise;
  public Geology(TerrainNoise noise) { this.noise = noise; }
  /** Broad surface-rock province; existing Minecraft blocks are visual proxies for each family. */
  public Lithology lithology(Column column) {
    return switch (column.province()) {
      case 0 -> Lithology.SEDIMENTARY;
      case 1 -> Lithology.IGNEOUS;
      default -> Lithology.METAMORPHIC;
    };
  }

  /** Revision-4 outcrops are limited to steep highlands and their shallow rock face. */
  public static boolean isMountainOutcrop(int surfaceY, double slope, int y) {
    return Double.isFinite(slope) && surfaceY >= 150 && slope > 1.3
        && y <= surfaceY && surfaceY - y <= 96;
  }

  /**
   * Returns true if the given position is on a mountain slope where visible
   * lithological banding should be rendered. This enables the "stone layer"
   * effect on mountain flanks where igneous, sedimentary, and metamorphic
   * strata are exposed.
   */
  public static boolean isMountainStrataZone(int surfaceY, double slope, int y) {
      return Double.isFinite(slope) && surfaceY >= 130 && slope > 0.6
          && y <= surfaceY && surfaceY - y <= 150;
  }

  /**
   * Material-revision-3 outcrop palette. It perturbs bedding laterally and
   * varies band thickness and facies; the frozen host-rock function remains
   * authoritative for caves, ores, permeability, and older material revisions.
   * Bedding and facies noise are amortized across vertical blocks.
   */
  public final class OutcropColumn {
    private final int x, z;
    private final double thickness, beddingOffset;
    private final Lithology lithology;
    private final Column host;
    private final boolean preserveIntrusions;
    private int cachedBand = Integer.MIN_VALUE;
    private Rock cachedRock;

    private OutcropColumn(int x, int z, Column column, boolean preserveIntrusions) {
      this.x = x;
      this.z = z;
      thickness = 26 + noise.field(x, z, 1900, 1885) * 4;
      beddingOffset = column.bedding() + column.fault()
          + noise.field(x, z, 560, 1881) * 15
          + noise.field(x, z, 170, 1882) * 4;
      lithology = lithology(column);
      host = column;
      this.preserveIntrusions = preserveIntrusions;
    }

    /** This sampler is intended to be owned by one generated column. */
    public Rock rock(int y) {
      if (preserveIntrusions) {
        if (host.intrusion() > .80 && y < host.bedding() + 45) return Rock.GRANITE;
        if (host.intrusion() < -.84 && y < host.bedding() + 70) return Rock.BASALT;
      }
      int band = (int) Math.floor((y + beddingOffset) / thickness);
      if (band != cachedBand) {
        double facies = noise.field(x, z, 920, 1883L + band * 37L);
        cachedRock = switch (lithology) {
          case SEDIMENTARY -> facies > .32 ? Rock.SANDSTONE
              : facies > -.48 ? Rock.CALCITE : Rock.TUFF;
          case IGNEOUS -> facies > .25 ? Rock.ANDESITE
              : facies > -.42 ? Rock.BASALT : Rock.TUFF;
          case METAMORPHIC -> facies > .25 ? Rock.DIORITE
              : facies > -.42 ? Rock.GRANITE : Rock.DEEPSLATE;
        };
        cachedBand = band;
      }
      return cachedRock;
    }
  }

  public OutcropColumn outcropColumn(int x, int z, Column column) {
    return new OutcropColumn(x, z, column, false);
  }

  /** Material revision 5 preserves the frozen intrusive granite and basalt bodies. */
  public OutcropColumn outcropColumnPreservingIntrusions(int x, int z, Column column) {
    return new OutcropColumn(x, z, column, true);
  }

  /**
   * Returns the rock type for visible mountain strata banding. This creates
   * distinct igneous, sedimentary, and metamorphic layers on mountain flanks
   * based on the lithology province, with thickness variation from noise.
   */
  public Rock mountainStrataRock(int x, int z, Column column, int y) {
    if (!isMountainStrataZone((int) Math.floor(column.bedding()), 1.0, y)) {
      return column.rock(y);
    }
    Lithology lith = lithology(column);
      double bandThickness = 16 + noise.field(x, z, 1900, 1885) * 8;  // Thinner bands for more visible layering
    double bandOffset = column.bedding() + column.fault()
          + noise.field(x, z, 560, 1881) * 15
          + noise.field(x, z, 170, 1882) * 4;
    int band = (int) Math.floor((y + bandOffset) / bandThickness);
    double facies = noise.field(x, z, 920, 1883L + band * 37L);
      // Add secondary facies for more variation within bands
      double facies2 = noise.field(x, z, 921, 1884L + band * 37L);
      return switch (lith) {
        case SEDIMENTARY -> facies > .4 ? Rock.SANDSTONE
            : facies > -.4 ? Rock.CALCITE : (facies2 > 0 ? Rock.TUFF : Rock.SANDSTONE);
        case IGNEOUS -> facies > .35 ? Rock.ANDESITE
            : facies > -.35 ? Rock.BASALT : (facies2 > 0 ? Rock.TUFF : Rock.ANDESITE);
        case METAMORPHIC -> facies > .35 ? Rock.DIORITE
            : facies > -.35 ? Rock.GRANITE : (facies2 > 0 ? Rock.DEEPSLATE : Rock.DIORITE);
      };
    }

  /** Exact hydraulic subset; no terrain, strata, deposit or soil-horizon evaluation. */
  public double permeability(int x, int z) {
    double faultField = noise.field(x, z, 1400, 1802);
    int province = Math.clamp((int) ((noise.field(x, z, 2300, 1803) + 1) * 1.5), 0, 2);
    return permeability(province, faultField);
  }
  private static double permeability(int province, double faultField) {
    double value = province == 0 ? .65 : province == 1 ? .16 : .30;
    return Math.abs(faultField) < .07 ? Math.max(value, .8) : value;
  }
  public Column column(int x, int z, TerrainEngine.Sample sample) {
    double fold = noise.field(x, z, 720, 1801) * 32 + Math.sin(x * .006 + z * .003) * 9;
    double faultField = noise.field(x, z, 1400, 1802);
    double fault = faultField >= 0 ? 18 : -18;
    int province = Math.clamp((int) ((noise.field(x, z, 2300, 1803) + 1) * 1.5), 0, 2);
    double intrusion = noise.field(x, z, 360, 1804);
    int soil = (int) Math.clamp(2 + sample.rain() * 3 + sample.sediment() * 3 - sample.slope() * 3, 0, 7);
    int gx = Math.floorDiv(x, 32), gz = Math.floorDiv(z, 32);
    double cx = gx * 32 + 8 + noise.hash(gx, gz, 1810) * 16,
        cz = gz * 32 + 8 + noise.hash(gx, gz, 1811) * 16;
    double cy = -96 + noise.hash(gx, gz, 1812) * 352;
    double permeability = permeability(province, faultField);
    return new Column(fold, fault, province, intrusion, soil, permeability,
        x - cx, z - cz, cy, (int) (noise.hash(gx, gz, 1813) * 8), 3 + noise.hash(gx, gz, 1814) * 5);
  }
}
