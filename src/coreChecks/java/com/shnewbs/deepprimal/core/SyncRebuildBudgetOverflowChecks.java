package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.Random;

/** Differential and adversarial checks for native synchronous rebuild admission. */
public final class SyncRebuildBudgetOverflowChecks {
    private static long checks;
    private static final class Oracle {
        private BigInteger count = BigInteger.ZERO;
        private BigInteger elapsed = BigInteger.ZERO;
        private int maximum;
        private long limit;
        void reset(int maximum, long limit) {
            this.maximum = maximum;
            this.limit = limit;
            count = BigInteger.ZERO;
            elapsed = BigInteger.ZERO;
        }
        void completed(long duration) {
            count = count.add(BigInteger.ONE);
            if (duration > 0) elapsed = elapsed.add(BigInteger.valueOf(duration));
        }
        boolean allows(boolean edit) {
            return edit || count.compareTo(BigInteger.valueOf(maximum)) < 0
                && elapsed.compareTo(BigInteger.valueOf(limit)) < 0;
        }
    }
    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError("check " + checks + ": " + label);
    }
    public static void main(String[] args) throws Exception {
        SyncRebuildBudget budget = new SyncRebuildBudget();
        Oracle oracle = new Oracle();
        budget.reset(4, Long.MAX_VALUE);
        budget.completed(Long.MAX_VALUE - 3);
        budget.completed(20);
        check(!budget.allows(false), "elapsed overflow reopened ordinary native builds");
        check(budget.allows(true), "player edit must bypass exhausted budget");
        budget.reset(3, 1_500_000);
        check(budget.allows(false), "new frame must reset budget");
        budget.completed(-10);
        check(budget.allows(false), "negative duration must not consume time");
        budget.completed(1_500_000);
        check(!budget.allows(false), "exact time cap");
        budget.reset(2, 1_500_000);
        budget.completed(500_000);
        check(budget.allows(false), "ordinary first completion");
        budget.completed(500_000);
        check(!budget.allows(false), "count cap");
        budget.reset(0, 0);
        check(!budget.allows(false) && budget.allows(true), "disabled budget and player edit");
        // Reach the 2^31 completion boundary without a multi-billion-call test.
        budget.reset(Integer.MAX_VALUE, Long.MAX_VALUE);
        var countField = SyncRebuildBudget.class.getDeclaredField("count");
        countField.setAccessible(true);
        countField.setInt(budget, Integer.MAX_VALUE);
        budget.completed(0);
        check(!budget.allows(false), "count overflow reopened ordinary builds");
        check(budget.allows(true), "count saturation must preserve edit bypass");
        Random random = new Random(0x51B0D632L);
        for (int sequence = 0; sequence < 300; sequence++) {
            int maximum = switch (sequence % 7) {
                case 0 -> Integer.MAX_VALUE;
                case 1 -> 0;
                case 2 -> -1;
                default -> 1 + random.nextInt(1000);
            };
            long limit = switch (sequence % 8) {
                case 0 -> Long.MAX_VALUE;
                case 1 -> Long.MAX_VALUE - 1;
                case 2 -> 0;
                case 3 -> -1;
                default -> random.nextInt(2_000_000);
            };
            budget.reset(maximum, limit);
            oracle.reset(maximum, limit);
            for (int step = 0; step < 500; step++) {
                long duration = switch (random.nextInt(12)) {
                    case 0 -> Long.MAX_VALUE;
                    case 1 -> Long.MAX_VALUE - random.nextInt(100);
                    case 2 -> Long.MIN_VALUE;
                    case 3 -> -random.nextInt(1000);
                    default -> random.nextInt(2_000_000);
                };
                if (random.nextInt(7) == 0) {
                    int resetMax = random.nextInt(200);
                    long resetLimit = random.nextInt(3_000_000);
                    budget.reset(resetMax, resetLimit);
                    oracle.reset(resetMax, resetLimit);
                } else {
                    budget.completed(duration);
                    oracle.completed(duration);
                }
                check(budget.allows(false) == oracle.allows(false), "ordinary oracle seq=" + sequence + " step=" + step);
                check(budget.allows(true) == oracle.allows(true), "player-edit oracle");
            }
        }
        System.out.println("SYNC_REBUILD_OVERFLOW_PASS checks=" + checks);
    }
    private SyncRebuildBudgetOverflowChecks() {}
}
