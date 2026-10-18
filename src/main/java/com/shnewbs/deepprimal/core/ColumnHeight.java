package com.shnewbs.deepprimal.core;

/** Exact top-cell solution for a height-minus-Y terrain density. No vertical search. */
public final class ColumnHeight {
    private ColumnHeight() {}

    public static int surfaceY(double height, int lower, int upper, int step) {
        if (step <= 0 || upper < lower || !Double.isFinite(height)) {
            throw new IllegalArgumentException("Invalid column geometry");
        }
        int first = Math.floorDiv(upper, step) * step;
        if (height > first) return Math.max(lower, first);
        if (height <= lower) return lower;
        // Density is strictly positive below the surface: an exact cell boundary belongs below it.
        int candidate = (int) ((Math.ceil(height / step) - 1.0) * step);
        return Math.max(lower, Math.min(first, candidate));
    }
}
