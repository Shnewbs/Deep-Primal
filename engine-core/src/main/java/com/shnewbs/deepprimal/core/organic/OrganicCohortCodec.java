package com.shnewbs.deepprimal.core.organic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Canonical, bounded save payload for a region's admitted phenology cohorts. */
public final class OrganicCohortCodec {
    public static final int SCHEMA = 1;
    public static final int MAX_COHORTS = 4096;
    public static final int MAX_BYTES = 262144;
    private static final int MAGIC = 0x4f524743; // ORGC
    private OrganicCohortCodec() {}

    public record Entry(long patchId, OrganicPhenology.Cohort cohort) {
        public Entry { Objects.requireNonNull(cohort, "cohort"); }
    }

    /** Reject duplicates and unsorted patch IDs rather than silently overwriting state. */
    public static byte[] encode(List<Entry> entries) {
        Objects.requireNonNull(entries, "entries");
        if (entries.size() > MAX_COHORTS) throw new IllegalArgumentException("too many cohorts");
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buffer);
            out.writeInt(MAGIC);
            out.writeInt(SCHEMA);
            out.writeInt(entries.size());
            long previous = 0;
            boolean first = true;
            for (Entry entry : entries) {
                Objects.requireNonNull(entry, "entry");
                if (!first && entry.patchId() <= previous) throw new IllegalArgumentException("patch IDs must increase");
                first = false;
                previous = entry.patchId();
                OrganicPhenology.Cohort c = entry.cohort();
                byte[] id = c.speciesId().getBytes(StandardCharsets.US_ASCII);
                if (id.length == 0 || id.length > 128) throw new IllegalArgumentException("species ID too long");
                out.writeLong(entry.patchId());
                out.writeShort(id.length);
                out.write(id);
                out.writeInt(c.schema());
                out.writeLong(c.lastDay());
                out.writeLong(c.favorableDays());
                out.writeLong(c.stressfulDays());
                out.writeBoolean(c.dormant());
                if (buffer.size() > MAX_BYTES) throw new IllegalArgumentException("payload too large");
            }
            out.flush();
            return buffer.toByteArray();
        } catch (IOException impossible) { throw new AssertionError(impossible); }
    }

    /** Strict decoding: no trailing data, oversized payload, invalid ID or partial save accepted. */
    public static List<Entry> decode(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length < 12 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("invalid payload size");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            if (in.readInt() != MAGIC || in.readInt() != SCHEMA) throw new IllegalArgumentException("unknown cohort format");
            int count = in.readInt();
            if (count < 0 || count > MAX_COHORTS) throw new IllegalArgumentException("invalid cohort count");
            List<Entry> result = new ArrayList<>(count);
            long previous = 0;
            for (int i = 0; i < count; i++) {
                long patchId = in.readLong();
                if (i > 0 && patchId <= previous) throw new IllegalArgumentException("unsorted patch IDs");
                previous = patchId;
                int len = in.readUnsignedShort();
                if (len == 0 || len > 128) throw new IllegalArgumentException("invalid species ID size");
                byte[] id = new byte[len];
                in.readFully(id);
                for (byte b : id) if (b < 0x21 || b > 0x7e) throw new IllegalArgumentException("non-ASCII ID");
                OrganicPhenology.Cohort cohort = new OrganicPhenology.Cohort(
                        in.readInt(), new String(id, StandardCharsets.US_ASCII),
                        in.readLong(), in.readLong(), in.readLong(), in.readBoolean());
                result.add(new Entry(patchId, cohort));
            }
            if (in.available() != 0) throw new IllegalArgumentException("trailing payload data");
            return List.copyOf(result);
        } catch (IOException ex) { throw new IllegalArgumentException("truncated or invalid cohort payload", ex); }
    }
}
