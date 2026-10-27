package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Single-owner tick budget and exact unordered contact set with fixed residency.
 * No world references, elapsed-time catch-up, resizing or per-contact objects.
 */
public final class WaterContactBudget {
  private final int limit;
  private final long[] first;
  private final long[] second;
  private final int[] stamps;
  private int generation;
  private int visits;
  private int contacts;
  private long tick;
  private boolean initialized;

  public WaterContactBudget(int visitLimit) {
    if (visitLimit < 1 || visitLimit > 65536)
      throw new IllegalArgumentException("Contact visit limit must be in [1,65536]");
    limit = visitLimit;
    int capacity = 2;
    while (capacity < 2 * visitLimit) capacity <<= 1;
    first = new long[capacity];
    second = new long[capacity];
    stamps = new int[capacity];
  }

  /** A changed tick starts one budget, including backward or wrapping clocks. */
  public void beginTick(long now) {
    if (initialized && tick == now) return;
    initialized = true;
    tick = now;
    visits = 0;
    contacts = 0;
    // A stamp is reused only after all 2^32 values. Zero is reserved for empty slots.
    if (++generation == 0) {
      Arrays.fill(stamps, 0);
      generation = 1;
    }
  }

  /** Count every neighbor admission, including dry, unloaded and duplicate contacts. */
  public boolean tryVisit() {
    requireTick();
    if (visits == limit) return false;
    visits++;
    return true;
  }

  /** True once per exact unordered pair per tick; callers separately charge tryVisit.
   * The independent contact cap also prevents accidental callers from growing residency.
   * At most table capacity probes occur, even for hostile colliding inputs.
   */
  public boolean firstContact(long a, long b) {
    requireTick();
    if (a > b) { long swap = a; a = b; b = swap; }
    if (contacts == limit) return false;
    int mask = stamps.length - 1;
    int slot = (int) mix(a ^ Long.rotateLeft(mix(b), 29)) & mask;
    for (int probes = 0; probes < stamps.length; probes++) {
      if (stamps[slot] != generation) {
        first[slot] = a;
        second[slot] = b;
        stamps[slot] = generation;
        contacts++;
        return true;
      }
      if (first[slot] == a && second[slot] == b) return false;
      slot = (slot + 1) & mask;
    }
    // The <= 1/2 load cap makes this unreachable; fail closed if an invariant breaks.
    return false;
  }

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
}
