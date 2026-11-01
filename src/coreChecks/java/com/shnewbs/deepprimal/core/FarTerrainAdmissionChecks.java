package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Isolated, dependency-free horizon tile admission and signed coordinate regression. */
public final class FarTerrainAdmissionChecks {
    private static long checks;
    private FarTerrainAdmissionChecks() {}

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static boolean oracle(int tx, int tz, double x, double z, int chunks) {
        if (chunks < 0 || !Double.isFinite(x) || !Double.isFinite(z)) return false;
        // Independent nearest-point-to-rectangle construction, not the production
        // max-of-two-signed-edge-distances expression.
        double left = (long) tx * FarTerrain.TILE_BLOCKS;
        double top = (long) tz * FarTerrain.TILE_BLOCKS;
        double nearestX = Math.min(Math.max(x, left), left + FarTerrain.TILE_BLOCKS);
        double nearestZ = Math.min(Math.max(z, top), top + FarTerrain.TILE_BLOCKS);
        return Math.hypot(x - nearestX, z - nearestZ) <= (double) chunks * 16.0;
    }

    public static void main(String[] args) {
        for (int chunks : new int[] {-1, -256, Integer.MIN_VALUE}) {
            check(!FarTerrain.inRange(0, 0, 0, 0, chunks), "negative range accepted: " + chunks);
            check(!FarTerrain.inRange(-1, -1, 0, 0, chunks), "negative range accepted at signed tile");
        }
        for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            check(!FarTerrain.inRange(0, 0, invalid, 0, 512), "invalid x accepted");
            check(!FarTerrain.inRange(0, 0, 0, invalid, 512), "invalid z accepted");
        }
        check(FarTerrain.inRange(0, 0, 0, 0, 0), "zero-radius tile interior");
        check(FarTerrain.inRange(-1, -1, 0, 0, 0), "zero-radius shared corner");
        check(!FarTerrain.inRange(1, 0, 0, 0, 0), "zero-radius distant tile");
        check(FarTerrain.inRange(1, 0, 496, 0, 1), "16-block exact threshold");
        check(!FarTerrain.inRange(1, 0, Math.nextDown(496), 0, 1), "outside exact threshold");
        Random random = new Random(0xD148F00DL);
        for (int i = 0; i < 300_000; i++) {
            int tx = random.nextInt(1001) - 500;
            int tz = random.nextInt(1001) - 500;
            double x = (random.nextInt(1_000_001) - 500_000) / 4.0;
            double z = (random.nextInt(1_000_001) - 500_000) / 4.0;
            int chunks = random.nextInt(513);
            check(FarTerrain.inRange(tx, tz, x, z, chunks) == oracle(tx, tz, x, z, chunks),
                "finite parity: " + tx + "," + tz + "," + x + "," + z + "," + chunks);
        }
        for (int tx : new int[] {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}) {
            for (int tz : new int[] {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}) {
                long key = FarTerrain.key(tx, tz);
                check(FarTerrain.x(key) == tx && FarTerrain.z(key) == tz, "tile key roundtrip");
                double x = (long) tx * FarTerrain.TILE_BLOCKS + 256.0;
                double z = (long) tz * FarTerrain.TILE_BLOCKS + 256.0;
                check(FarTerrain.inRange(tx, tz, x, z, 0), "extreme tile interior");
            }
        }
        System.out.println("FAR_TERRAIN_ADMISSION_PASS checks=" + checks);
    }
}
