package com.shnewbs.deepprimal.core.organic;

import java.util.Objects;

/**
 * Portable, immutable species requirements. This does not own soil, water, time or biomass.
 * All environmental values must come from authoritative host snapshots.
 */
public record SpeciesPhysiology(
        int schema,
        String id,
        Kind kind,
        double minimumTemperatureC,
        double optimumTemperatureC,
        double maximumTemperatureC,
        double minimumLight,
        double minimumMoisture,
        double maximumMoisture,
        double minimumRootAccess,
        int growingSeasonStartDay,
        int growingSeasonEndDay) {

    public enum Kind { ANNUAL, PERENNIAL, TREE, GRASS, FUNGUS }

    public SpeciesPhysiology {
        if (schema != 1) throw new IllegalArgumentException("unsupported species schema");
        Objects.requireNonNull(id, "id");
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("species id must be a stable namespaced identifier");
        Objects.requireNonNull(kind, "kind");
        finite(minimumTemperatureC);
        finite(optimumTemperatureC);
        finite(maximumTemperatureC);
        if (!(minimumTemperatureC < optimumTemperatureC && optimumTemperatureC < maximumTemperatureC))
            throw new IllegalArgumentException("temperature thresholds must be ordered");
        fraction(minimumLight);
        fraction(minimumMoisture);
        fraction(maximumMoisture);
        fraction(minimumRootAccess);
        if (minimumMoisture > maximumMoisture)
            throw new IllegalArgumentException("moisture thresholds must be ordered");
        if (growingSeasonStartDay < 1 || growingSeasonStartDay > 366
                || growingSeasonEndDay < 1 || growingSeasonEndDay > 366)
            throw new IllegalArgumentException("season day out of range");
    }

    /** A pure suitability score; zero means no admitted growth, never an automatic growth tick. */
    public double suitability(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        if (!seasonContains(environment.dayOfYear())) return 0.0;
        double t = environment.temperatureC();
        if (t <= minimumTemperatureC || t >= maximumTemperatureC) return 0.0;
        if (environment.light() < minimumLight
                || environment.moisture() < minimumMoisture
                || environment.moisture() > maximumMoisture
                || environment.rootAccess() < minimumRootAccess) return 0.0;
        double thermal = t <= optimumTemperatureC
                ? (t - minimumTemperatureC) / (optimumTemperatureC - minimumTemperatureC)
                : (maximumTemperatureC - t) / (maximumTemperatureC - optimumTemperatureC);
        return Math.max(0.0, Math.min(1.0, thermal));
    }

    private boolean seasonContains(int day) {
        return growingSeasonStartDay <= growingSeasonEndDay
                ? day >= growingSeasonStartDay && day <= growingSeasonEndDay
                : day >= growingSeasonStartDay || day <= growingSeasonEndDay;
    }

    public record Environment(int dayOfYear, double temperatureC, double light,
                              double moisture, double rootAccess) {
        public Environment {
            if (dayOfYear < 1 || dayOfYear > 366)
                throw new IllegalArgumentException("day out of range");
            finite(temperatureC);
            fraction(light);
            fraction(moisture);
            fraction(rootAccess);
        }
    }

    private static void fraction(double value) {
        finite(value);
        if (value < 0.0 || value > 1.0)
            throw new IllegalArgumentException("expected fraction in [0,1]");
    }

    private static void finite(double value) {
        if (!Double.isFinite(value))
            throw new IllegalArgumentException("nonfinite environmental value");
    }
}
