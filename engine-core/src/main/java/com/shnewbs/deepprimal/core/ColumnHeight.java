package com.shnewbs.deepprimal.core;

/** Exact top-cell solution for a height-minus-Y terrain density. No vertical search. */
public final class ColumnHeight {
    private ColumnHeight() {}

    public static int surfaceY(double height, int lower, int upper, int step) {
        if (step <= 0 || upper < lower || !Double.isFinite(height)) {
            throw new IllegalArgumentException("Invalid column geometry");
        }
        // floorDiv(upper, step) * step can fall below Integer.MIN_VALUE.
        // Keep the aligned cell coordinate wide until it is clamped to lower.
        long first = Math.floorDiv((long) upper, step) * step;
        if (height > first) return (int) Math.max((long) lower, first);
        if (height <= lower) return lower;
        // Find the greatest integer strictly below height *before* aligning to
        // a cell. Dividing a tiny positive height by step can underflow to zero,
        // incorrectly skipping the zero cell (or another grid boundary).
        long below = (long) Math.ceil(height) - 1L;
        long candidate = Math.floorDiv(below, step) * step;
        return (int) Math.max((long) lower, Math.min(first, candidate));
    }
}
