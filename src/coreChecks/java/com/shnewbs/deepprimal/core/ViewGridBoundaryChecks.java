package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;

/** Signed section-grid boundaries and independent exact ring oracle. */
public final class ViewGridBoundaryChecks {
    private static long checks;
    private ViewGridBoundaryChecks() {}

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message + " (check " + checks + ")");
    }

    private static int oracle(int camera, int radius, int index) {
        if (radius < 0) throw new IllegalArgumentException("negative radius");
        BigInteger low = BigInteger.valueOf(camera).subtract(BigInteger.valueOf(radius));
        BigInteger high = BigInteger.valueOf(camera).add(BigInteger.valueOf(radius));
        if (low.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0
                || high.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
            throw new IllegalArgumentException("unrepresentable window");
        BigInteger width = BigInteger.valueOf(radius).multiply(BigInteger.TWO).add(BigInteger.ONE);
        BigInteger offset = BigInteger.valueOf(index).subtract(low).mod(width);
        return low.add(offset).intValueExact();
    }

    private static void compare(int camera, int radius, int index) {
        try {
            int expected = oracle(camera, radius, index);
            int actual = ViewGrid.coordinate(camera, radius, index);
            check(actual == expected, "ring coordinate: " + camera + "/" + radius + "/" + index);
            check((long) actual >= (long) camera - radius
                    && (long) actual <= (long) camera + radius, "inside window");
            check(Math.floorMod((long) actual, 2L * radius + 1)
                    == Math.floorMod((long) index, 2L * radius + 1), "slot identity");
        } catch (IllegalArgumentException expected) {
            try {
                ViewGrid.coordinate(camera, radius, index);
                throw new AssertionError("invalid window accepted: " + camera + "/" + radius);
            } catch (IllegalArgumentException ok) {
                check(true, "invalid rejected");
            }
        }
    }

    public static void main(String[] args) {
        // The original int subtraction wraps even though this entire window is representable.
        compare(Integer.MIN_VALUE + 1, 1, 0);
        compare(Integer.MAX_VALUE - 1, 1, Integer.MAX_VALUE);
        // A window outside the signed section domain must not wrap to a remote chunk.
        compare(Integer.MIN_VALUE, 1, 0);
        for (int camera : new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
                Integer.MIN_VALUE + 2, -1_800_000, -1, 0, 1, 1_800_000,
                Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE}) {
            for (int radius : new int[] {-1, 0, 1, 2, 32, 125, 1_073_741_823, 1_073_741_824, Integer.MAX_VALUE}) {
                for (int index : new int[] {Integer.MIN_VALUE, -100, -1, 0, 1, 2, 31, 125, 1024, Integer.MAX_VALUE})
                    compare(camera, radius, index);
            }
        }
        Random rng = new Random(20261031L);
        for (int trial = 0; trial < 120_000; trial++) {
            int camera = rng.nextInt();
            int radius = switch (trial % 5) {
                case 0 -> rng.nextInt(126);
                case 1 -> rng.nextInt(1_000_000);
                case 2 -> rng.nextInt(Integer.MAX_VALUE);
                case 3 -> Integer.MAX_VALUE;
                default -> rng.nextInt(5);
            };
            compare(camera, radius, rng.nextInt());
        }
        for (int radius : new int[] {0, 1, 2, 32, 125}) {
            for (int camera : new int[] {-1_800_000, -125, 0, 125, 1_800_000}) {
                int side = 2 * radius + 1;
                HashSet<Integer> seen = new HashSet<>();
                for (int index = 0; index < side; index++) {
                    int result = ViewGrid.coordinate(camera, radius, index);
                    check(seen.add(result), "unique slot");
                    check(Math.floorMod(result, side) == index, "ring slot");
                }
                check(seen.size() == side, "complete ring");
            }
        }
        System.out.println("VIEW_GRID_BOUNDARY_PASS checks=" + checks);
    }
}
