package com.shnewbs.deepprimal.core;

/** Fast backoff, slow recovery for optional background work. */
public final class WorkBudget {
    private double fraction = 1;
    private int stable;
    public void observe(double elapsedMs, double targetMs, double heapFraction) {
        if (!Double.isFinite(elapsedMs) || targetMs <= 0) return;
        if (elapsedMs > targetMs * 1.15 || heapFraction > 0.85) {
            fraction = Math.max(0.125, fraction * 0.75); stable = 0;
        } else if (elapsedMs <= targetMs * 1.05 && heapFraction < 0.75 && ++stable >= 60) {
            fraction = Math.min(1, fraction + 0.025); stable = 0;
        }
    }
    public double fraction() { return fraction; }
    public void reset() { fraction = 1; stable = 0; }
}
