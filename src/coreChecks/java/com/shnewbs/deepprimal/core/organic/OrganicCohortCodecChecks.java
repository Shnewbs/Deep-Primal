package com.shnewbs.deepprimal.core.organic;

import java.util.*;

/** Isolated regression: java com.shnewbs.deepprimal.core.organic.OrganicCohortCodecChecks */
public final class OrganicCohortCodecChecks {
    static long checks;
    static void yes(boolean value) {
        checks++;
        if (!value) throw new AssertionError("check " + checks);
    }
    static void rejects(Runnable r) {
        checks++;
        try { r.run(); throw new AssertionError("accepted invalid input"); }
        catch (IllegalArgumentException expected) {}
    }
    public static void main(String[] args) {
        Random random = new Random(0xBADC0FFEE0DL);
        for (int trial = 0; trial < 10000; trial++) {
            int count = random.nextInt(24);
            List<OrganicCohortCodec.Entry> entries = new ArrayList<>();
            for (int i = 0; i < count; i++)
                entries.add(new OrganicCohortCodec.Entry(i * 7L - 1000,
                        new OrganicPhenology.Cohort(1, "deepprimal:oak",
                                random.nextInt(100000), random.nextInt(1000),
                                random.nextInt(1000), random.nextBoolean())));
            byte[] payload = OrganicCohortCodec.encode(entries);
            yes(OrganicCohortCodec.decode(payload).equals(entries));
            yes(Arrays.equals(OrganicCohortCodec.encode(OrganicCohortCodec.decode(payload)), payload));
            byte[] truncated = Arrays.copyOf(payload, payload.length - 1);
            rejects(() -> OrganicCohortCodec.decode(truncated));
            byte[] trailing = Arrays.copyOf(payload, payload.length + 1);
            rejects(() -> OrganicCohortCodec.decode(trailing));
        }
        var cohort = new OrganicPhenology.Cohort(1, "deepprimal:oak", 1, 1, 0, false);
        rejects(() -> OrganicCohortCodec.encode(List.of(
                new OrganicCohortCodec.Entry(2, cohort), new OrganicCohortCodec.Entry(2, cohort))));
        rejects(() -> OrganicCohortCodec.encode(List.of(
                new OrganicCohortCodec.Entry(3, cohort), new OrganicCohortCodec.Entry(2, cohort))));
        rejects(() -> OrganicCohortCodec.decode(new byte[300000]));
        byte[] bad = OrganicCohortCodec.encode(List.of(new OrganicCohortCodec.Entry(1, cohort)));
        bad[7] = 2;
        rejects(() -> OrganicCohortCodec.decode(bad));
        System.out.println("ORGANIC_COHORT_CODEC_PASS assertions=" + checks);
    }
}
