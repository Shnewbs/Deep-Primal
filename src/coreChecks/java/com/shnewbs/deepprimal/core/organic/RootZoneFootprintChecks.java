package com.shnewbs.deepprimal.core.organic;

import java.util.Random;

/** Isolated deterministic checks; run with Java 21+ against the production class. */
public final class RootZoneFootprintChecks {
    private static int checks;
    private static void eq(double a, double b) {
        if (Math.abs(a - b) > 1e-12) throw new AssertionError(a + " != " + b);
        checks++;
    }
    private static void reject(Runnable r) {
        try { r.run(); throw new AssertionError("accepted invalid input"); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    public static void main(String[] args) {
        var d = new RootZoneFootprint.Definition(1, "deepprimal:oak", 8, 4, .75);
        var top = new RootZoneFootprint.Horizon(0, 2, 1);
        var bottom = new RootZoneFootprint.Horizon(2, 4, 0);
        eq(RootZoneFootprint.access(d, top, bottom), .75);
        eq(RootZoneFootprint.coverage(d, 0), 1);
        eq(RootZoneFootprint.coverage(d, 4), .25);
        eq(RootZoneFootprint.coverage(d, 8), 0);
        eq(RootZoneFootprint.coverage(d, 100), 0);
        reject(() -> new RootZoneFootprint.Definition(2, "deepprimal:oak", 8, 4, .5));
        reject(() -> new RootZoneFootprint.Horizon(2, 1, .5));
        reject(() -> RootZoneFootprint.access(d,
                new RootZoneFootprint.Horizon(0, 3, 1),
                new RootZoneFootprint.Horizon(2, 4, 1)));
        reject(() -> RootZoneFootprint.coverage(d, Double.NaN));
        var random = new Random(1123);
        for (int i = 0; i < 100_000; i++) {
            double a = random.nextDouble(), b = random.nextDouble();
            var h1 = new RootZoneFootprint.Horizon(0, 2, a);
            var h2 = new RootZoneFootprint.Horizon(2, 4, b);
            eq(RootZoneFootprint.access(d, h1, h2), .75 * a + .25 * b);
            eq(RootZoneFootprint.access(d, h1, h2), RootZoneFootprint.access(d, h1, h2));
            double c = RootZoneFootprint.coverage(d, random.nextDouble() * 16);
            if (c < 0 || c > 1) throw new AssertionError("coverage bounds");
            checks++;
        }
        System.out.println("ROOT_ZONE_PASS assertions=" + checks);
    }
}
