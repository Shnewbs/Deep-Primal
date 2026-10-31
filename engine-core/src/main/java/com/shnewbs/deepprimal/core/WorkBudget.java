package com.shnewbs.deepprimal.core;

/** Fast backoff, slow recovery for optional background work. */
public final class WorkBudget {
    private double fraction = 1;
    private int stable;
    public void observe(double elapsedMs, double targetMs, double heapFraction) {
        // Invalid or out-of-range telemetry is not evidence of a stable frame.
        // In particular, an infinite target or a negative heap fraction must
        // never unlock background work after a run of apparently good frames.
        if (!Double.isFinite(elapsedMs) || elapsedMs < 0
                || !Double.isFinite(targetMs) || targetMs <= 0
                || !Double.isFinite(heapFraction) || heapFraction < 0 || heapFraction > 1) {
            stable = 0;
            return;
        }
        if (elapsedMs > targetMs * 1.15 || heapFraction > 0.85) {
            fraction = Math.max(0.125, fraction * 0.75); stable = 0;
        } else if (elapsedMs <= targetMs * 1.05 && heapFraction < 0.75) {
            if (++stable >= 60) {
                fraction = Math.min(1, fraction + 0.025); stable = 0;
            }
        } else {
            // Neutral frames are not healthy recovery frames; break the streak.
            stable = 0;
        }
    }
    public double fraction() { return fraction; }
    public void reset() { fraction = 1; stable = 0; }
}
