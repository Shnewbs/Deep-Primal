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
    for (int entry : quads) for (int slot = 0; slot < 9; slot++) {
      if (slot != 4 && mask(entry, slot % 3 - 1, slot / 3 - 1) != 0)
        offsets[slot + 1]++;
    }
    offsets[0] = 10;
    for (int slot = 0; slot < 9; slot++) offsets[slot + 1] = Math.addExact(offsets[slot + 1], offsets[slot]);
    int[] entries = new int[offsets[9]];
    System.arraycopy(offsets, 0, entries, 0, 10);
    for (int entry : quads) for (int slot = 0; slot < 9; slot++) {
      if (slot != 4 && mask(entry, slot % 3 - 1, slot / 3 - 1) != 0)
        entries[offsets[slot]++] = entry;
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
