package com.shnewbs.deepprimal.core.organic;

import java.util.List;
import java.util.Random;

public final class OrganicRegionBudgetChecks {
    private static long checks;
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("check " + checks); }
    private static void rejects(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException | NullPointerException e) { rejected = true; }
        check(rejected);
    }
    public static void main(String[] args) {
        var ids = List.of("dp:a", "dp:b", "dp:c", "dp:d", "dp:e");
        var cursor = new OrganicRegionBudget.Cursor(1, 0);
        check(OrganicRegionBudget.admit(ids, cursor, 2).admitted().equals(List.of("dp:a", "dp:b")));
        for (int i = 0; i < 20; i++) {
            var result = OrganicRegionBudget.admit(ids, cursor, 2);
            check(result.admitted().size() == 2);
            check(result.admitted().equals(OrganicRegionBudget.admit(ids, cursor, 2).admitted()));
            cursor = result.nextCursor();
        }
        check(OrganicRegionBudget.admit(ids, new OrganicRegionBudget.Cursor(1, Long.MAX_VALUE), 99).admitted().size() == 5);
        check(OrganicRegionBudget.admit(ids, cursor, 0).nextCursor().equals(cursor));
        check(OrganicRegionBudget.admit(List.of(), cursor, 3).admitted().isEmpty());
        var fixedCursor = cursor;
        rejects(() -> OrganicRegionBudget.admit(ids, fixedCursor, -1));
        rejects(() -> OrganicRegionBudget.admit(List.of("b", "a"), fixedCursor, 1));
        rejects(() -> OrganicRegionBudget.admit(List.of("a", "a"), fixedCursor, 1));
        rejects(() -> OrganicRegionBudget.admit(List.of(""), fixedCursor, 1));
        rejects(() -> new OrganicRegionBudget.Cursor(2, 0));
        rejects(() -> new OrganicRegionBudget.Cursor(1, -1));
        Random rng = new Random(0x0A6A11C);
        for (int i = 0; i < 100_000; i++) {
            long index = rng.nextLong() & Long.MAX_VALUE;
            int budget = rng.nextInt(12);
            var c = new OrganicRegionBudget.Cursor(1, index);
            var r = OrganicRegionBudget.admit(ids, c, budget);
            check(r.admitted().size() == Math.min(5, budget));
            check(r.nextCursor().nextIndex() >= 0);
            check(r.admitted().equals(OrganicRegionBudget.admit(ids, c, budget).admitted()));
        }
        System.out.println("OrganicRegionBudget checks passed: " + checks);
    }
}
