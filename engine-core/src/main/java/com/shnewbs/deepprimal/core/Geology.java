package com.shnewbs.deepprimal.core;

/** Seeded folded strata, faults, intrusions, finite host-dependent deposits and soil horizons. */
public final class Geology {
  public enum Rock { GRANITE, ANDESITE, DIORITE, BASALT, TUFF, SANDSTONE, CALCITE, DEEPSLATE }
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
    double permeability = province == 0 ? .65 : province == 1 ? .16 : .30;
    if (Math.abs(faultField) < .07) permeability = Math.max(permeability, .8);
    return new Column(fold, fault, province, intrusion, soil, permeability,
        x - cx, z - cz, cy, (int) (noise.hash(gx, gz, 1813) * 8), 3 + noise.hash(gx, gz, 1814) * 5);
  }
}
