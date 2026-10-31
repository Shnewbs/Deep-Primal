package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/** Differential tests against an exact 65-bit squared-distance oracle. */
public final class NearestChunksOverflowChecks {
    private static long assertions;
    private static final Random RANDOM = new Random(0x1280C0DEL);

    private static long pack(int x, int z) {
        return Integer.toUnsignedLong(x) | (Integer.toUnsignedLong(z) << 32);
    }

    private static BigInteger distance(long position, int centerX, int centerZ) {
        BigInteger dx = BigInteger.valueOf((int)position - (long)centerX);
        BigInteger dz = BigInteger.valueOf((int)(position >>> 32) - (long)centerZ);
        return dx.multiply(dx).add(dz.multiply(dz));
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message + " at check " + assertions);
    }

    private static void verify(int cx, int cz, int capacity, List<Long> positions) {
        NearestChunks nearest = new NearestChunks(capacity);
        nearest.reset(capacity, cx, cz);
        for (long position : positions) nearest.offer(position);
        nearest.sort();
        List<Long> expected = new ArrayList<>(positions);
        expected.sort(Comparator.<Long, BigInteger>comparing(p -> distance(p, cx, cz))
            .thenComparingLong(Long::longValue));
        int count = Math.min(capacity, positions.size());
        check(nearest.size() == count, "selection count");
        for (int i = 0; i < count; i++)
            check(nearest.position(i) == expected.get(i), "distance or tie order");
    }

    public static void main(String[] args) {
        // Distances above 2^63 and 2^64 used to wrap and outrank near chunks.
        verify(Integer.MIN_VALUE, Integer.MIN_VALUE, 2, List.of(
            pack(Integer.MAX_VALUE, Integer.MAX_VALUE),
            pack(Integer.MIN_VALUE + 1, Integer.MIN_VALUE),
            pack(Integer.MAX_VALUE, Integer.MIN_VALUE),
            pack(Integer.MIN_VALUE, Integer.MIN_VALUE + 1)));
        verify(Integer.MAX_VALUE, Integer.MAX_VALUE, 3, List.of(
            pack(Integer.MIN_VALUE, Integer.MIN_VALUE),
            pack(Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
            pack(Integer.MIN_VALUE, Integer.MAX_VALUE),
            pack(Integer.MAX_VALUE, Integer.MAX_VALUE - 1)));
        for (int trial = 0; trial < 2_000; trial++) {
            int cx = RANDOM.nextInt(), cz = RANDOM.nextInt();
            int capacity = 1 + RANDOM.nextInt(64);
            int count = RANDOM.nextInt(140);
            HashSet<Long> unique = new HashSet<>();
            while (unique.size() < count) {
                int x = switch (RANDOM.nextInt(5)) {
                    case 0 -> Integer.MIN_VALUE;
                    case 1 -> Integer.MAX_VALUE;
                    default -> RANDOM.nextInt();
                };
                int z = switch (RANDOM.nextInt(5)) {
                    case 0 -> Integer.MIN_VALUE;
                    case 1 -> Integer.MAX_VALUE;
                    default -> RANDOM.nextInt();
                };
                unique.add(pack(x, z));
            }
            verify(cx, cz, capacity, new ArrayList<>(unique));
        }
        System.out.println("PASS NearestChunks exact unsigned 65-bit differential checks: " + assertions);
    }
}
