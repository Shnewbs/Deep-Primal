package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Extreme signed camera/window differential oracle for native vertical section wrapping. */
public final class VerticalRangeOverflowChecks {
    private static long checks;
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message + " at check=" + checks);
        checks++;
    }
    private static int expectedCount(int sections, int radius) {
        return radius == 0 ? sections : (int)Math.min((long)sections, 2L * radius + 1L);
    }
    private static int expectedLow(int camera, int min, int sections, int radius) {
        int count = expectedCount(sections, radius);
        long lo = radius == 0 ? min : Math.max((long)min,
            Math.min((long)camera - radius, (long)min + sections - count));
        return Math.toIntExact(lo);
    }
    public static void main(String[] args) {
        // Keep the window smaller than the world: a full-height window would mask camera underflow.
        // Previously camera MIN_VALUE - 96 wrapped to a positive Y, selecting the top of the world.
        check(VerticalRange.low(Integer.MIN_VALUE, -64, 512, 96) == -64,
            "underflow camera selects top instead of bottom");
        // Previously slot - low overflowed and produced an incorrect section for negative low.
        check(VerticalRange.coordinate(Integer.MIN_VALUE, 63, Integer.MAX_VALUE) ==
            (int)((long)Integer.MIN_VALUE + Math.floorMod((long)Integer.MAX_VALUE - Integer.MIN_VALUE, 63)),
            "signed section ring overflow");
        check(VerticalRange.count(128, Integer.MAX_VALUE) == 128, "radius multiplication overflow");
        Random rng = new Random(0xDEE0134L);
        int[] cameras = {Integer.MIN_VALUE, Integer.MIN_VALUE+1, -1000, -65, -1, 0,
            1, 63, 64, 1000, Integer.MAX_VALUE-1, Integer.MAX_VALUE};
        int[] minimums = {Integer.MIN_VALUE, -4096, -64, 0, 4096, Integer.MAX_VALUE-512};
        int[] radii = {0, 1, 2, 8, 32, 96, 127, 1024, Integer.MAX_VALUE};
        for (int min: minimums) for (int sections: new int[]{1, 2, 3, 64, 128, 256, 512}) {
            if ((long)min + sections - 1 > Integer.MAX_VALUE) continue;
            for (int radius: radii) for (int camera: cameras) verify(camera,min,sections,radius,rng);
        }
        for (int i=0; i<200_000; i++) {
            int min = rng.nextInt();
            int sections = 1+rng.nextInt(1024);
            if ((long)min + sections - 1 > Integer.MAX_VALUE) continue;
            int camera = rng.nextInt();
            int radius = switch(rng.nextInt(8)) {
                case 0 -> 0; case 1 -> 1; case 2 -> 96;
                case 3 -> Integer.MAX_VALUE; default -> rng.nextInt(10000);
            };
            verify(camera,min,sections,radius,rng);
        }
        for (int[] invalid: new int[][]{{0,1},{-1,1},{1,-1}}) {
            try { VerticalRange.count(invalid[0], invalid[1]); throw new AssertionError("invalid window accepted"); }
            catch (IllegalArgumentException ok) { checks++; }
        }
        try { VerticalRange.low(0, Integer.MAX_VALUE, 2, 1); throw new AssertionError("unrepresentable world accepted"); }
        catch (IllegalArgumentException ok) { checks++; }
        try { VerticalRange.coordinate(0, 0, 0); throw new AssertionError("zero ring accepted"); }
        catch (IllegalArgumentException ok) { checks++; }
        System.out.println("VERTICAL_RANGE_OVERFLOW_PASS checks="+checks);
    }
    private static void verify(int camera, int min, int sections, int radius, Random rng) {
        int count = expectedCount(sections, radius);
        int low = expectedLow(camera,min,sections,radius);
        check(VerticalRange.count(sections,radius) == count,"count");
        check(VerticalRange.low(camera,min,sections,radius) == low,"low");
        check((long)low >= min && (long)low+count <= (long)min+sections,"bounds");
        check(VerticalRange.visibilityRadius(sections,radius) == (radius==0 ? sections : Math.min(sections,radius)),"visibility");
        for (int j=0; j<3; j++) {
            int slot = switch(j) {case 0 -> rng.nextInt(); case 1 -> Integer.MIN_VALUE; default -> Integer.MAX_VALUE;};
            int expected = Math.toIntExact((long)low + Math.floorMod((long)slot-low, count));
            check(VerticalRange.coordinate(low,count,slot) == expected,"ring");
        }
    }
}
