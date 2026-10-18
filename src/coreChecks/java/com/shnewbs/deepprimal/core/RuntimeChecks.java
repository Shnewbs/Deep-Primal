package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public final class RuntimeChecks {
    public static void run() {
        for (int n = 2; n <= 96; n++) check(DistancePolicy.parse("" + n, 96).orElseThrow() == n, "distance parse");
        for (String text : new String[]{"", "0", "1", "97", "-2", "2.0", " 4", "9999999999", "Infinity", "2e2"})
            check(DistancePolicy.parse(text, 96).isEmpty(), "invalid distance: " + text);
        check(DistancePolicy.parse("33", 32).isEmpty(), "simulation limit");
        check(DistancePolicy.sectionSlots(32, 48) == 202800, "tall-world slot count");
        Random random = new Random(20261019L);
        NearestChunks selector = new NearestChunks(64);
        for (int test = 0; test < 3000; test++) {
            int centerX = random.nextInt(3_600_001) - 1_800_000;
            int centerZ = random.nextInt(3_600_001) - 1_800_000;
            int limit = 1 + random.nextInt(64), count = random.nextInt(1000);
            HashSet<Long> positions = new HashSet<>();
            while (positions.size() < count) positions.add(pack(random.nextInt(3_600_001) - 1_800_000, random.nextInt(3_600_001) - 1_800_000));
            selector.reset(limit, centerX, centerZ);
            for (long position : positions) selector.offer(position);
            selector.sort();
            Long[] reference = positions.toArray(Long[]::new);
            Arrays.sort(reference, Comparator.<Long>comparingLong(p -> distance(p, centerX, centerZ)).thenComparingLong(Long::longValue));
            check(selector.size() == Math.min(limit, count), "bounded nearest size");
            for (int i = 0; i < selector.size(); i++) check(selector.position(i) == reference[i], "nearest differential");
        }
        // Ties and coordinates crossing zero use a stable packed-position tie-break.
        selector.reset(2, 0, 0); selector.offer(pack(1, 0)); selector.offer(pack(-1, 0)); selector.offer(pack(0, 1)); selector.sort();
        check(selector.position(0) == pack(1, 0) && selector.position(1) == pack(-1, 0), "equal-distance ordering");
        int gridCases = 0;
        for (int radius : new int[]{2, 6, 32, 33, 96}) {
            int side = 2 * radius + 1;
            for (int trial = 0; trial < 100; trial++) {
                int camera = random.nextInt(3_600_001) - 1_800_000;
                HashSet<Integer> covered = new HashSet<>();
                for (int index = 0; index < side; index++) {
                    int actual = ViewGrid.coordinate(camera, radius, index);
                    check(actual >= camera - radius && actual <= camera + radius, "grid range");
                    check(Math.floorMod(actual, side) == index, "grid ring index");
                    covered.add(actual); gridCases++;
                }
                check(covered.size() == side, "grid covers each coordinate once");
            }
        }
        System.out.println("PASS: 3000 nearest-queue differential cases, distance validation, " + gridCases + " ring-grid coordinates.");
    }
    private static long pack(int x, int z) { return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32); }
    private static long distance(long p, int x, int z) {
        long dx = (int)p - (long)x, dz = (int)(p >>> 32) - (long)z;
        return dx * dx + dz * dz;
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
