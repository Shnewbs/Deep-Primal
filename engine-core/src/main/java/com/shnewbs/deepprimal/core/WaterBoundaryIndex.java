package com.shnewbs.deepprimal.core;

/** Immutable neighbor-facing lists of packed top-quad offsets/cells, in original GPU order.
 * Each boundary cell belongs to one side, or two sides and their diagonal at a page corner.
 * At most three stored entries per input; the center bucket is always empty. */
public final class WaterBoundaryIndex {
  public static final WaterBoundaryIndex EMPTY = new WaterBoundaryIndex(new int[10]);
  private final int[] entries;

  private WaterBoundaryIndex(int[] entries) { this.entries = entries; }

  public static WaterBoundaryIndex of(int[] quads) {
    if (quads.length == 0) return EMPTY;
    int[] offsets = new int[10];
    // Only cells on a page edge can contribute to a neighbor. Classifying each
    // cell once avoids 9 mask evaluations per quad in each of the two passes.
    // Slots retain row-major neighbor order: NW,N,NE,W,center,E,SW,S,SE.
    for (int entry : quads) {
      int x = entry & 15, z = (entry >>> 4) & 15;
      if (z == 0) {
        if (x == 0) offsets[1]++; // NW
        offsets[2]++; // N
        if (x == 15) offsets[3]++; // NE
      }
      if (x == 0) offsets[4]++; // W
      if (x == 15) offsets[6]++; // E
      if (z == 15) {
        if (x == 0) offsets[7]++; // SW
        offsets[8]++; // S
        if (x == 15) offsets[9]++; // SE
      }
    }
    offsets[0] = 10;
    for (int slot = 0; slot < 9; slot++)
      offsets[slot + 1] = Math.addExact(offsets[slot + 1], offsets[slot]);
    int[] entries = new int[offsets[9]];
    System.arraycopy(offsets, 0, entries, 0, 10);
    // Append to each bucket in original input order, preserving GPU ordering.
    for (int entry : quads) {
      int x = entry & 15, z = (entry >>> 4) & 15;
      if (z == 0) {
        if (x == 0) entries[offsets[0]++] = entry;
        entries[offsets[1]++] = entry;
        if (x == 15) entries[offsets[2]++] = entry;
      }
      if (x == 0) entries[offsets[3]++] = entry;
      if (x == 15) entries[offsets[5]++] = entry;
      if (z == 15) {
        if (x == 0) entries[offsets[6]++] = entry;
        entries[offsets[7]++] = entry;
        if (x == 15) entries[offsets[8]++] = entry;
      }
    }
    return new WaterBoundaryIndex(entries);
  }

  private static int slot(int dx, int dz) {
    if (dx < -1 || dx > 1 || dz < -1 || dz > 1 || (dx == 0 && dz == 0))
      throw new IllegalArgumentException("Expected an adjacent horizontal page");
    return (dz + 1) * 3 + dx + 1;
  }

  public int begin(int dx, int dz) { return entries[slot(dx, dz)]; }
  public int end(int dx, int dz) { return entries[slot(dx, dz) + 1]; }
  public int entry(int position) { return entries[position]; }
  public int retainedBytes() { return entries.length * Integer.BYTES; }

  /** Local shared-corner mask for a neighbor at dx/dz relative to this page. */
  public static int mask(int entry, int dx, int dz) {
    int x = entry & 15, z = (entry >>> 4) & 15;
    int xs = dx == 0 ? 15 : dx < 0 ? (x == 0 ? 5 : 0) : (x == 15 ? 10 : 0);
    int zs = dz == 0 ? 15 : dz < 0 ? (z == 0 ? 3 : 0) : (z == 15 ? 12 : 0);
    return xs & zs;
  }
}
