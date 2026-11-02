package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Overflow must invalidate an incomplete dependency capture before retry. */
public final class TransitionDependenciesOverflowRecoveryChecks {
    private static long assertions;
    private TransitionDependenciesOverflowRecoveryChecks() {}

    private static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message + " at assertion " + assertions);
    }

    private static void overflow(TransitionDependencies tracker, Object... sources) {
        boolean threw = false;
        try {
            for (Object source : sources) tracker.include(source);
        } catch (IllegalStateException expected) {
            threw = true;
        }
        check(threw, "Overflow was not rejected");
    }

    public static void main(String[] args) {
        TransitionDependencies zero = new TransitionDependencies(0);
        zero.begin(11, 12, 13);
        overflow(zero, new Object());
        zero.begin(11, 12, 13);
        check(zero.end(), "Failed initial capture suppressed retry invalidation");
        zero.begin(11, 12, 13);
        check(!zero.end(), "Stable zero-capacity capture rebuilt");

        Object a = new Object(), b = new Object(), c = new Object();
        TransitionDependencies one = new TransitionDependencies(1);
        one.begin(5, 6, 7);
        one.include(a);
        check(one.end(), "First capture must invalidate");
        one.begin(5, 6, 7);
        overflow(one, b, c);
        one.begin(5, 6, 7);
        one.include(b);
        check(one.end(), "Failed partial overwrite suppressed retry invalidation");
        one.begin(5, 6, 7);
        one.include(b);
        check(!one.end(), "Stable capture after retry rebuilt");

        Random rng = new Random(0xDEEA185L);
        Object[] ids = {a, b, c, null};
        for (int i = 0; i < 100_000; i++) {
            long x = rng.nextLong(), z = rng.nextLong();
            int radius = rng.nextInt(100);
            Object selected = ids[rng.nextInt(ids.length)];
            one.begin(x, z, radius);
            one.include(selected);
            check(one.end(), "New coordinates must invalidate");
            one.begin(x, z, radius);
            overflow(one, selected, ids[rng.nextInt(ids.length)]);
            one.begin(x, z, radius);
            one.include(selected);
            check(one.end(), "Overflow recovery must conservatively invalidate");
            one.begin(x, z, radius);
            one.include(selected);
            check(!one.end(), "Stable post-recovery dependency rebuilt");
        }
        System.out.println("PASS TransitionDependencies overflow recovery assertions=" + assertions);
    }
}
