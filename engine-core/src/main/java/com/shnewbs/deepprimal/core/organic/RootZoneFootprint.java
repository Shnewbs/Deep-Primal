package com.shnewbs.deepprimal.core.organic;

import java.util.Objects;

/** Pure aggregated root-zone suitability. The host owns soil, water, species and saves. */
public final class RootZoneFootprint {
    public static final int SCHEMA = 1;
    private RootZoneFootprint() {}

    /** Distances are blocks; depths are positive distances below the soil surface. */
    public record Definition(int schema, String speciesId, double radiusBlocks,
                             double depthBlocks, double surfaceFraction) {
        public Definition {
            if (schema != SCHEMA) throw new IllegalArgumentException("unsupported schema");
            Objects.requireNonNull(speciesId, "speciesId");
            if (!speciesId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException("invalid species ID");
            if (!Double.isFinite(radiusBlocks) || radiusBlocks <= 0 || radiusBlocks > 128
                    || !Double.isFinite(depthBlocks) || depthBlocks <= 0 || depthBlocks > 64
                    || !Double.isFinite(surfaceFraction) || surfaceFraction < 0 || surfaceFraction > 1)
                throw new IllegalArgumentException("invalid root dimensions or allocation");
        }
    }

    /** Read-only horizon snapshot from the authoritative soil owner. */
    public record Horizon(double topDepth, double bottomDepth, double access) {
        public Horizon {
            if (!Double.isFinite(topDepth) || !Double.isFinite(bottomDepth)
                    || topDepth < 0 || bottomDepth <= topDepth
                    || !Double.isFinite(access) || access < 0 || access > 1)
                throw new IllegalArgumentException("invalid horizon");
        }
    }

    /**
     * Two nonoverlapping horizons; missing depth is inaccessible. The upper half
     * of the root profile receives surfaceFraction of demand. This is never
     * a nutrient or water withdrawal and cannot credit resources.
     */
    public static double access(Definition roots, Horizon upper, Horizon lower) {
        Objects.requireNonNull(roots, "roots");
        Objects.requireNonNull(upper, "upper");
        Objects.requireNonNull(lower, "lower");
        if (upper.bottomDepth() > lower.topDepth())
            throw new IllegalArgumentException("overlapping horizons");
        double half = roots.depthBlocks() * 0.5;
        double surface = weighted(upper, 0, half) + weighted(lower, 0, half);
        double deep = weighted(upper, half, roots.depthBlocks())
                + weighted(lower, half, roots.depthBlocks());
        return roots.surfaceFraction() * surface / half
                + (1 - roots.surfaceFraction()) * deep / half;
    }

    private static double weighted(Horizon h, double start, double end) {
        return Math.max(0, Math.min(end, h.bottomDepth()) - Math.max(start, h.topDepth()))
                * h.access();
    }

    /** Horizontal coverage weight without per-root entities or random draws. */
    public static double coverage(Definition roots, double distanceBlocks) {
        Objects.requireNonNull(roots, "roots");
        if (!Double.isFinite(distanceBlocks) || distanceBlocks < 0)
            throw new IllegalArgumentException("invalid distance");
        double normalized = Math.min(1, distanceBlocks / roots.radiusBlocks());
        return (1 - normalized) * (1 - normalized);
    }
}
