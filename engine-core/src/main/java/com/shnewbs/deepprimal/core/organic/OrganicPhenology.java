package com.shnewbs.deepprimal.core.organic;

import java.util.Objects;

/** Pure one-day regional cohort phenology. Host owns time, soil, biomass and persistence. */
public final class OrganicPhenology {
    public static final int SCHEMA = 1;
    private OrganicPhenology() {}

    /** Versioned save state; lastDay is the authoritative monotonic calendar day. */
    public record Cohort(int schema, String speciesId, long lastDay,
                         long favorableDays, long stressfulDays, boolean dormant) {
        public Cohort {
            if (schema != SCHEMA || lastDay < 0 || favorableDays < 0 || stressfulDays < 0)
                throw new IllegalArgumentException("invalid cohort state");
            Objects.requireNonNull(speciesId, "speciesId");
            if (!speciesId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException("invalid species ID");
        }
    }

    /** Exactly one admitted day; no implicit unloaded-region catch-up or free biomass. */
    public static Transition advance(Cohort previous, SpeciesPhysiology species,
                                     SpeciesPhysiology.Environment environment, long calendarDay) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(environment, "environment");
        if (!previous.speciesId().equals(species.id()))
            throw new IllegalArgumentException("cohort species mismatch");
        if (calendarDay < previous.lastDay())
            throw new IllegalArgumentException("calendar rewound");
        if (calendarDay == previous.lastDay())
            return new Transition(previous, 0.0, false);
        if (previous.lastDay() == Long.MAX_VALUE || calendarDay != previous.lastDay() + 1)
            throw new IllegalArgumentException("unadmitted calendar gap");
        double suitability = species.suitability(environment);
        boolean favorable = suitability > 0.0;
        long good = favorable ? Math.addExact(previous.favorableDays(), 1) : previous.favorableDays();
        long bad = favorable ? previous.stressfulDays() : Math.addExact(previous.stressfulDays(), 1);
        return new Transition(new Cohort(SCHEMA, previous.speciesId(), calendarDay,
                good, bad, !favorable), suitability, true);
    }

    /** Suitability is only an input to a separately funded growth transaction. */
    public record Transition(Cohort next, double suitability, boolean advanced) {
        public Transition {
            Objects.requireNonNull(next, "next");
            if (!Double.isFinite(suitability) || suitability < 0.0 || suitability > 1.0)
                throw new IllegalArgumentException("invalid suitability");
        }
    }
}
