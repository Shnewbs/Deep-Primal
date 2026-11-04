package com.shnewbs.deepprimal.core.organic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pure, bounded regional admission. The caller owns authoritative time and persistent cursor. */
public final class OrganicRegionBudget {
    public static final int SCHEMA = 1;
    private OrganicRegionBudget() {}

    /** Region identity and cursor are durable host state, never inferred from wall-clock time. */
    public record Cursor(int schema, long nextIndex) {
        public Cursor {
            if (schema != SCHEMA || nextIndex < 0) throw new IllegalArgumentException("invalid cursor");
        }
    }

    /** Snapshot IDs must be unique and in stable ascending order for deterministic replay. */
    public static Result admit(List<String> stablePatchIds, Cursor cursor, int budget) {
        Objects.requireNonNull(stablePatchIds, "stablePatchIds");
        Objects.requireNonNull(cursor, "cursor");
        if (budget < 0) throw new IllegalArgumentException("negative budget");
        String previous = null;
        for (String id : stablePatchIds) {
            if (id == null || id.isBlank() || (previous != null && previous.compareTo(id) >= 0))
                throw new IllegalArgumentException("patch IDs must be nonempty, unique and sorted");
            previous = id;
        }
        int count = stablePatchIds.size();
        if (count == 0 || budget == 0) return new Result(List.of(), cursor);
        int start = (int) (cursor.nextIndex() % count);
        int take = Math.min(count, budget);
        ArrayList<String> selected = new ArrayList<>(take);
        for (int i = 0; i < take; i++) selected.add(stablePatchIds.get((start + i) % count));
        // Cursor advances by admitted work only. Modulo prevents overflow even near Long.MAX_VALUE.
        long next = (start + (long) take) % count;
        return new Result(List.copyOf(selected), new Cursor(SCHEMA, next));
    }

    public record Result(List<String> admitted, Cursor nextCursor) {
        public Result {
            admitted = List.copyOf(Objects.requireNonNull(admitted, "admitted"));
            Objects.requireNonNull(nextCursor, "nextCursor");
        }
    }
}
