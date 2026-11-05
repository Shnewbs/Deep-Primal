package com.shnewbs.deepprimal.core.organic;

import java.util.Random;

public final class SpeciesPhysiologyChecks {
    private static long checks;
    private static void yes(boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("species physiology assertion " + checks);
    }
    private static void invalid(Runnable runnable) {
        try { runnable.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("invalid input accepted");
    }
    public static void main(String[] args) {
        SpeciesPhysiology tree = new SpeciesPhysiology(1, "deepprimal:oak",
                SpeciesPhysiology.Kind.TREE, -5, 20, 40, .2, .3, .9, .4, 300, 90);
        yes(tree.suitability(new SpeciesPhysiology.Environment(10, 20, .8, .6, .7)) == 1.0);
        yes(tree.suitability(new SpeciesPhysiology.Environment(150, 20, .8, .6, .7)) == 0.0);
        yes(tree.suitability(new SpeciesPhysiology.Environment(10, 20, .8, .2, .7)) == 0.0);
        yes(tree.suitability(new SpeciesPhysiology.Environment(10, 20, .8, .6, .2)) == 0.0);
        invalid(() -> new SpeciesPhysiology(2, "deepprimal:oak", SpeciesPhysiology.Kind.TREE,
                -5, 20, 40, .2, .3, .9, .4, 1, 366));
        invalid(() -> new SpeciesPhysiology(1, "INVALID", SpeciesPhysiology.Kind.TREE,
                -5, 20, 40, .2, .3, .9, .4, 1, 366));
        invalid(() -> new SpeciesPhysiology(1, "deepprimal:oak", SpeciesPhysiology.Kind.TREE,
                -5, 20, 40, Double.NaN, .3, .9, .4, 1, 366));
        invalid(() -> new SpeciesPhysiology.Environment(0, 20, .5, .5, .5));
        Random random = new Random(0x51EEDL);
        for (int i = 0; i < 100000; i++) {
            int day = 1 + random.nextInt(366);
            double temperature = -10 + random.nextDouble() * 60;
            double light = random.nextDouble();
            double moisture = random.nextDouble();
            double roots = random.nextDouble();
            var snapshot = new SpeciesPhysiology.Environment(day, temperature, light, moisture, roots);
            double first = tree.suitability(snapshot);
            double replay = tree.suitability(snapshot);
            yes(Double.doubleToLongBits(first) == Double.doubleToLongBits(replay));
            yes(first >= 0.0 && first <= 1.0);
        }
        System.out.println("SpeciesPhysiologyChecks passed: " + checks);
    }
}
