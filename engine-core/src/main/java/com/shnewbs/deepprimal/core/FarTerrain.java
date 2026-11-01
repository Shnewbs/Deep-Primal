package com.shnewbs.deepprimal.core;

/** A bounded multi-resolution horizon: no chunk tickets, full voxel columns or simulation. */
public final class FarTerrain {
    public static final int MAX_DISTANCE = 512, TILE_BLOCKS = 512, MAX_TILES = 1200;
    public static long key(int x, int z) { return (long)x << 32 | z & 0xffffffffL; }
    public static int x(long key) { return (int)(key >> 32); }
    public static int z(long key) { return (int)key; }
    public static boolean inRange(int tx, int tz, double x, double z, int chunks) {
        // Negative radii would become positive when squared; invalid camera
        // coordinates must never admit distant tiles. Fail closed at the boundary.
        if (chunks < 0 || !Double.isFinite(x) || !Double.isFinite(z)) return false;
        double dx = Math.max(Math.max(tx * (double)TILE_BLOCKS - x, x - (tx + 1.0) * TILE_BLOCKS), 0);
        double dz = Math.max(Math.max(tz * (double)TILE_BLOCKS - z, z - (tz + 1.0) * TILE_BLOCKS), 0);
        return dx * dx + dz * dz <= (double)chunks * chunks * 256;
    }
    private FarTerrain() {}
}
