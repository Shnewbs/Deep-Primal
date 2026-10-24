package com.shnewbs.deepprimal.core;

/** V7 canopy occupancy follows the selected biome, with a coherent local density patch. */
public final class Vegetation {
  public static double treeChance(int biome,double density) {
    double base=switch(biome) {
      case 1,4,14,15,16,24 -> .72;
      case 10,11,13 -> .90;
      case 12 -> .42;
      case 22 -> .50;
      case 6,28 -> .55;
      case 8 -> .12;
      case 0 -> .04;
      case 17 -> .35;
      default -> 0;
    };
    return base*(.65+.35*Math.clamp(density,0,1));
  }
  private Vegetation() {}
}
