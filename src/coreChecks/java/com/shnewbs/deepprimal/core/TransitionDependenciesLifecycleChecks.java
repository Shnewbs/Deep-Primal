package com.shnewbs.deepprimal.core;

import java.util.Random;

/** First-capture, reset, and identity differential tests for LOD transition invalidation. */
public final class TransitionDependenciesLifecycleChecks {
    private static long assertions;
    private TransitionDependenciesLifecycleChecks() {}

    private static void check(boolean ok, String reason) {
        assertions++;
        if (!ok) throw new AssertionError(reason + " at assertion " + assertions);
    }

    private static final class Reference {
        private boolean captured;
        private long x, z;
        private int radius;
        private Object[] sources = new Object[0];

        boolean capture(long nextX, long nextZ, int nextRadius, Object[] nextSources) {
            boolean changed = !captured || x != nextX || z != nextZ || radius != nextRadius
                || sources.length != nextSources.length;
            if (!changed) {
                for (int i = 0; i < nextSources.length; i++) {
                    if (sources[i] != nextSources[i]) { changed = true; break; }
                }
            }
            captured = true;
            x = nextX;
            z = nextZ;
            radius = nextRadius;
            sources = nextSources.clone();
            return changed;
        }

        void clear() { captured = false; sources = new Object[0]; }
    }

    private static void capture(TransitionDependencies actual, Reference expected,
                                long x, long z, int radius, Object... sources) {
        actual.begin(x, z, radius);
        for (Object source : sources) actual.include(source);
        check(actual.end() == expected.capture(x, z, radius, sources),
            "LOD dependency transition differs from reference");
    }

    public static void main(String[] args) {
        var actual = new TransitionDependencies(8);
        var reference = new Reference();
        Object a = new Object(), b = new Object(), c = new Object();
        // Previously the initial sentinel looked identical to this valid empty capture.
        capture(actual, reference, Long.MIN_VALUE, 0, 0);
        capture(actual, reference, Long.MIN_VALUE, 0, 0);
        capture(actual, reference, Long.MIN_VALUE, 0, 0, a);
        capture(actual, reference, Long.MIN_VALUE, 0, 0, a);
        capture(actual, reference, Long.MIN_VALUE, 0, 0, b);
        capture(actual, reference, Long.MIN_VALUE, 0, 0, b, c);
        capture(actual, reference, Long.MIN_VALUE, 0, 0, b);
        actual.clear(); reference.clear();
        capture(actual, reference, Long.MIN_VALUE, 0, 0);
        capture(actual, reference, Long.MIN_VALUE, 0, 0);
        var empty = new TransitionDependencies(0);
        var emptyReference = new Reference();
        capture(empty, emptyReference, Long.MIN_VALUE, 0, 0);
        empty.clear(); emptyReference.clear();
        capture(empty, emptyReference, Long.MIN_VALUE, 0, 0);

        Random random = new Random(0xDEEA184L);
        Object[] identities = {null, a, b, c, new Object(), new Object(), new Object()};
        long[] coordinates = {Long.MIN_VALUE, Long.MAX_VALUE, -512, -1, 0, 1, 512};
        for (int iteration = 0; iteration < 200_000; iteration++) {
            if (iteration % 137 == 0) { actual.clear(); reference.clear(); }
            long x = coordinates[random.nextInt(coordinates.length)];
            long z = coordinates[random.nextInt(coordinates.length)];
            int radius = random.nextInt(5);
            Object[] selected = new Object[random.nextInt(9)];
            for (int i = 0; i < selected.length; i++)
                selected[i] = identities[random.nextInt(identities.length)];
            capture(actual, reference, x, z, radius, selected);
            // Stable repeated captures must not report spurious invalidations.
            if ((iteration & 7) == 0) capture(actual, reference, x, z, radius, selected);
        }
        System.out.println("PASS TransitionDependencies lifecycle assertions=" + assertions);
    }
}
