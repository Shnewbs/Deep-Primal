package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent reference-model and malformed-telemetry checks for the work pacer. */
public final class WorkBudgetTelemetryChecks {
    private static long checks;
    private WorkBudgetTelemetryChecks() {}

    private static void check(boolean ok, String reason) {
        checks++;
        if (!ok) throw new AssertionError(reason + " at check " + checks);
    }

    private static final class Oracle {
        double fraction = 1;
        int stable;
        void observe(double elapsed, double target, double heap) {
            boolean valid = Double.isFinite(elapsed) && elapsed >= 0
                && Double.isFinite(target) && target > 0
                && Double.isFinite(heap) && heap >= 0 && heap <= 1;
            if (!valid) { stable = 0; return; }
            if (elapsed > target * 1.15 || heap > 0.85) {
                fraction = Math.max(0.125, fraction * 0.75);
                stable = 0;
            } else if (elapsed <= target * 1.05 && heap < 0.75) {
                if (++stable >= 60) {
                    fraction = Math.min(1, fraction + 0.025);
                    stable = 0;
                }
            }
        }
    }

    public static void main(String[] args) {
        WorkBudget budget = new WorkBudget();
        for (int i = 0; i < 100; i++) budget.observe(50, 16.67, 0.5);
        check(budget.fraction() == 0.125, "backoff floor");
        for (int i = 0; i < 59; i++) budget.observe(16, 16.67, 0.5);
        double before = budget.fraction();
        // Each invalid sample must reset recovery hysteresis and leave budget unchanged.
        double[][] invalid = {
            {Double.NaN, 16.67, 0.5}, {Double.POSITIVE_INFINITY, 16.67, 0.5},
            {Double.NEGATIVE_INFINITY, 16.67, 0.5}, {-1, 16.67, 0.5},
            {16, Double.NaN, 0.5}, {16, Double.POSITIVE_INFINITY, 0.5},
            {16, Double.NEGATIVE_INFINITY, 0.5}, {16, 0, 0.5}, {16, -1, 0.5},
            {16, 16.67, Double.NaN}, {16, 16.67, Double.POSITIVE_INFINITY},
            {16, 16.67, Double.NEGATIVE_INFINITY}, {16, 16.67, -0.01},
            {16, 16.67, 1.01}
        };
        for (double[] sample : invalid) {
            budget.observe(sample[0], sample[1], sample[2]);
            check(budget.fraction() == before, "invalid sample changed fraction");
            budget.observe(16, 16.67, 0.5);
            check(budget.fraction() == before, "invalid sample failed to reset recovery");
            for (int i = 0; i < 58; i++) budget.observe(16, 16.67, 0.5);
        }
        budget.observe(16, 16.67, 0.5);
        check(budget.fraction() > before, "60 valid frames recover");
        budget.reset();
        check(budget.fraction() == 1, "reset");
        Random random = new Random(0x131A11DL);
        Oracle oracle = new Oracle();
        for (int i = 0; i < 150_000; i++) {
            double elapsed = switch (random.nextInt(30)) {
                case 0 -> Double.NaN;
                case 1 -> Double.POSITIVE_INFINITY;
                case 2 -> -1;
                default -> random.nextDouble(0, 50);
            };
            double target = switch (random.nextInt(30)) {
                case 0 -> Double.NaN;
                case 1 -> Double.POSITIVE_INFINITY;
                case 2 -> 0;
                default -> random.nextDouble(0.01, 35);
            };
            double heap = switch (random.nextInt(30)) {
                case 0 -> Double.NaN;
                case 1 -> -0.01;
                case 2 -> 1.01;
                default -> random.nextDouble();
            };
            budget.observe(elapsed, target, heap);
            oracle.observe(elapsed, target, heap);
            check(Double.doubleToLongBits(budget.fraction()) ==
                  Double.doubleToLongBits(oracle.fraction), "reference model parity");
            check(budget.fraction() >= 0.125 && budget.fraction() <= 1,
                  "budget bounded");
        }
        System.out.println("PASS WorkBudget telemetry checks=" + checks);
    }
}
