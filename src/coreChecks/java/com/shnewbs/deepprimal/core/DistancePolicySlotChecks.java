package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.Random;

/** Independent arbitrary-precision oracle for section-slot accounting. */
public final class DistancePolicySlotChecks {
    private static int checks;
    private static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(int distance, int sections) {
        BigInteger side = BigInteger.valueOf(distance).multiply(BigInteger.TWO).add(BigInteger.ONE);
        BigInteger exact = side.multiply(side).multiply(BigInteger.valueOf(sections));
        long expected = exact.min(MAX).longValueExact();
        long actual = DistancePolicy.sectionSlots(distance, sections);
        check(actual == expected, "slots " + distance + "," + sections + ": " + actual + " != " + expected);
        check(actual >= 0, "negative slots");
    }

    private static void rejects(int distance, int sections) {
        try {
            DistancePolicy.sectionSlots(distance, sections);
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("negative window accepted: " + distance + "," + sections);
    }

    public static void main(String[] args) {
        int[] radii = {0, 1, 2, 32, 96, 125, 100_000, 1_000_000, 1_073_741_823, Integer.MAX_VALUE};
        int[] sections = {0, 1, 2, 24, 48, 64, 256, 65_535, 1_000_000, Integer.MAX_VALUE};
        for (int r : radii) for (int s : sections) expect(r, s);
        rejects(-1, 1);
        rejects(1, -1);
        rejects(Integer.MIN_VALUE, 0);
        rejects(0, Integer.MIN_VALUE);
        check(DistancePolicy.sectionSlots(32, 48) == 202_800, "normal parity");
        Random random = new Random(0xD1412026L);
        for (int i = 0; i < 200_000; i++) {
            int radius = random.nextInt(Integer.MAX_VALUE);
            int vertical = random.nextInt(Integer.MAX_VALUE);
            expect(radius, vertical);
        }
        System.out.println("DISTANCE_POLICY_SLOT_PASS checks=" + checks);
    }
}
