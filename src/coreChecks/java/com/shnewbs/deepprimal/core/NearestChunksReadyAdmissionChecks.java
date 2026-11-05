package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/** Exact differential check for cheap readiness preflight in chunk sending. */
public final class NearestChunksReadyAdmissionChecks {
    private static final Random RANDOM = new Random(0x1300A11DL);
    private static long checks;
    private static long skippedReadinessProbes;

    private NearestChunksReadyAdmissionChecks() {}

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message + " at check " + checks);
    }

    private static long pack(int x, int z) {
        return Integer.toUnsignedLong(x) | (Integer.toUnsignedLong(z) << 32);
    }

    private static BigInteger squaredDistance(long position, int cx, int cz) {
        BigInteger dx = BigInteger.valueOf((int) position - (long) cx);
        BigInteger dz = BigInteger.valueOf((int) (position >>> 32) - (long) cz);
        return dx.multiply(dx).add(dz.multiply(dz));
    }

    private static Comparator<Long> ordering(int cx, int cz) {
        return Comparator.<Long, BigInteger>comparing(p -> squaredDistance(p, cx, cz))
            .thenComparingLong(Long::longValue);
    }

    private static void checkSorted(NearestChunks selection, PriorityQueue<Long> oracle,
                                    Comparator<Long> ordering) {
        selection.sort();
        List<Long> expected = new ArrayList<>(oracle);
        expected.sort(ordering);
        check(selection.size() == expected.size(), "selected count");
        for (int i = 0; i < expected.size(); i++) {
            check(selection.position(i) == expected.get(i), "selected position");
        }
    }

    private static void offerOracle(PriorityQueue<Long> oracle, long position,
                                    int limit, Comparator<Long> ordering) {
        if (oracle.size() < limit) oracle.offer(position);
        else if (ordering.compare(position, oracle.peek()) < 0) {
            oracle.poll();
            oracle.offer(position);
        }
    }

    private static void verify(int cx, int cz, int limit, List<Long> positions,
                               HashSet<Long> ready) {
        Comparator<Long> ordering = ordering(cx, cz);
        PriorityQueue<Long> oracle = new PriorityQueue<>(ordering.reversed());
        NearestChunks selection = new NearestChunks(limit);
        selection.reset(limit, cx, cz);
        for (long position : positions) {
            boolean expected = oracle.size() < limit || ordering.compare(position, oracle.peek()) < 0;
            boolean admitted = selection.wouldAccept(position);
            check(admitted == expected, "preflight matches exact ready-only oracle");
            // Model the mixin: probe readiness only for candidates that could
            // enter the bounded nearest-ready heap. Unready near chunks must
            // not consume slots or starve ready farther chunks.
            if (!admitted) skippedReadinessProbes++;
            if (admitted && ready.contains(position)) selection.offer(position);
            if (ready.contains(position)) offerOracle(oracle, position, limit, ordering);
        }
        checkSorted(selection, oracle, ordering);
        // A preflight after sorting must compare with the farthest selected
        // item, not the nearest item at index zero.
        for (long position : positions) {
            boolean expected = oracle.size() < limit || ordering.compare(position, oracle.peek()) < 0;
            check(selection.wouldAccept(position) == expected, "sorted preflight");
        }
        // The sorted-to-heap transition must preserve preflight semantics.
        long late = pack(cx, cz);
        boolean expected = oracle.size() < limit || ordering.compare(late, oracle.peek()) < 0;
        check(selection.wouldAccept(late) == expected, "late preflight");
        if (expected) selection.offer(late);
        offerOracle(oracle, late, limit, ordering);
        checkSorted(selection, oracle, ordering);
    }

    public static void main(String[] args) {
        verify(Integer.MIN_VALUE, Integer.MIN_VALUE, 2, List.of(
            pack(Integer.MAX_VALUE, Integer.MAX_VALUE),
            pack(Integer.MIN_VALUE + 1, Integer.MIN_VALUE),
            pack(Integer.MIN_VALUE, Integer.MIN_VALUE + 1),
            pack(Integer.MAX_VALUE, Integer.MIN_VALUE)),
            new HashSet<>(List.of(pack(Integer.MIN_VALUE + 1, Integer.MIN_VALUE),
                pack(Integer.MAX_VALUE, Integer.MIN_VALUE))));
        // A long prefix of unavailable nearby chunks cannot consume the quota.
        List<Long> starvation = new ArrayList<>();
        HashSet<Long> ready = new HashSet<>();
        for (int x = 0; x < 64; x++) starvation.add(pack(x, 0));
        for (int x = 64; x < 128; x++) {
            long p = pack(x, 0);
            starvation.add(p);
            ready.add(p);
        }
        verify(0, 0, 16, starvation, ready);
        for (int trial = 0; trial < 2_000; trial++) {
            int cx = RANDOM.nextInt(), cz = RANDOM.nextInt();
            int limit = 1 + RANDOM.nextInt(64);
            int count = 16 + RANDOM.nextInt(160);
            HashSet<Long> unique = new HashSet<>();
            while (unique.size() < count) {
                int x = switch (RANDOM.nextInt(6)) {
                    case 0 -> Integer.MIN_VALUE;
                    case 1 -> Integer.MAX_VALUE;
                    default -> RANDOM.nextInt();
                };
                int z = switch (RANDOM.nextInt(6)) {
                    case 0 -> Integer.MIN_VALUE;
                    case 1 -> Integer.MAX_VALUE;
                    default -> RANDOM.nextInt();
                };
                unique.add(pack(x, z));
            }
            List<Long> positions = new ArrayList<>(unique);
            ready = new HashSet<>();
            for (long p : positions) if (RANDOM.nextInt(4) != 0) ready.add(p);
            verify(cx, cz, limit, positions, ready);
        }
        check(skippedReadinessProbes > 0, "preflight must actually skip some expensive probes");
        System.out.println("PASS NearestChunks ready-admission checks=" + checks
            + " skippedSyntheticReadinessProbes=" + skippedReadinessProbes);
    }
}
