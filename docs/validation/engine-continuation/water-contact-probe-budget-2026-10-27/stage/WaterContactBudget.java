package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** One server-thread owner per level: finite admission and exact unordered contact residency.
 * No world references, elapsed-time catch-up, resizing or per-contact objects.
 */
public final class WaterContactBudget {
  public static final int DEFER = -1, DUPLICATE = 0, FIRST = 1;
  private final int visitLimit;
  private final int probeLimit;
  private final long[] first;
  private final long[] second;
  private final int[] stamps;
  private int generation;
  private int visits;
  private int contacts;
  private int probes;
  private long tick;
  private boolean initialized;

  public WaterContactBudget(int limit) {
    if (limit < 1 || limit > 65536)
      throw new IllegalArgumentException("Contact visit limit must be in [1,65536]");
    visitLimit = limit;
    // Bound all collision work over the whole tick, not just an individual lookup.
    probeLimit = 4 * limit;
    int capacity = 2;
    while (capacity < 2 * limit) capacity <<= 1;
    first = new long[capacity];
    second = new long[capacity];
    stamps = new int[capacity];
  }

  public void beginTick(long now) {
    if (initialized && tick == now) return;
    initialized = true;
    tick = now;
    visits = contacts = probes = 0;
    if (++generation == 0) {
      Arrays.fill(stamps, 0);
      generation = 1;
    }
  }

  /** Each attempted neighbor consumes an admission, including duplicates and unavailable cells. */
  public boolean tryVisit() {
    requireTick();
    if (visits == visitLimit) return false;
    visits++;
    return true;
  }

  /** Returns FIRST, DUPLICATE or DEFER. DEFER must be requeued for another tick.
   * Probes are charged for every examined bucket, even duplicates and failures.
   */
  public int contactStatus(long a, long b) {
    requireTick();
    if (a > b) { long swap = a; a = b; b = swap; }
    if (contacts == visitLimit) return DEFER;
    int mask = stamps.length - 1;
    int slot = (int) mix(a ^ Long.rotateLeft(mix(b), 29)) & mask;
    for (int local = 0; local < stamps.length; local++) {
      if (probes == probeLimit) return DEFER;
      probes++;
      if (stamps[slot] != generation) {
        first[slot] = a;
        second[slot] = b;
        stamps[slot] = generation;
        contacts++;
        return FIRST;
      }
      if (first[slot] == a && second[slot] == b) return DUPLICATE;
      slot = (slot + 1) & mask;
    }
    return DEFER;
  }

  /** Compatibility predicate. Runtime adapters must use contactStatus to retain DEFER work. */
  public boolean firstContact(long a, long b) { return contactStatus(a, b) == FIRST; }

  private static long mix(long value) {
    value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
    value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
    return value ^ (value >>> 31);
  }
  private void requireTick() {
    if (!initialized) throw new IllegalStateException("beginTick required before water contacts");
  }
  public int visits() { return visits; }
  public int retainedContacts() { return contacts; }
  public int probes() { return probes; }
  public int maxProbes() { return probeLimit; }
}
