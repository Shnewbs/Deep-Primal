package com.shnewbs.deepprimal.core.organic;

import java.util.Random;

public final class OrganicPhenologyChecks {
    private static long checks;
    private static void check(boolean ok) { checks++; if (!ok) throw new AssertionError("check " + checks); }
    private static void rejects(Runnable task) {
        boolean rejected = false;
        try { task.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected);
    }
    public static void main(String[] args) {
        var species = new SpeciesPhysiology(1, "deepprimal:oak", SpeciesPhysiology.Kind.TREE,
                -10, 20, 40, 0.2, 0.1, 0.9, 0.1, 80, 300);
        var good = new SpeciesPhysiology.Environment(150, 20, 0.8, 0.5, 0.8);
        var cold = new SpeciesPhysiology.Environment(350, -5, 0.8, 0.5, 0.8);
        var initial = new OrganicPhenology.Cohort(1, species.id(), 0, 0, 0, true);
        var once = OrganicPhenology.advance(initial, species, good, 1);
        check(once.advanced() && once.suitability() == 1.0 && !once.next().dormant());
        check(once.next().favorableDays() == 1 && once.next().stressfulDays() == 0);
        check(OrganicPhenology.advance(once.next(), species, good, 1).next().equals(once.next()));
        rejects(() -> OrganicPhenology.advance(initial, species, good, 2));
        rejects(() -> OrganicPhenology.advance(once.next(), species, good, 0));
        rejects(() -> OrganicPhenology.advance(initial, new SpeciesPhysiology(1, "deepprimal:pine", SpeciesPhysiology.Kind.TREE,
                -10, 20, 40, 0.2, 0.1, 0.9, 0.1, 80, 300), good, 1));
        var random = new Random(20261105L);
        var state = initial;
        for (int i = 1; i <= 100000; i++) {
            var env = random.nextBoolean() ? good : cold;
            var next = OrganicPhenology.advance(state, species, env, i);
            check(next.advanced());
            check(next.next().lastDay() == i);
            check(next.next().favorableDays() + next.next().stressfulDays() == i);
            check(next.next().dormant() == (next.suitability() == 0));
            check(OrganicPhenology.advance(state, species, env, i).equals(next));
            state = next.next();
        }
        check(state.favorableDays() + state.stressfulDays() == 100000);
        System.out.println("ORGANIC_PHENOLOGY_PASS assertions=" + checks);
    }
}
