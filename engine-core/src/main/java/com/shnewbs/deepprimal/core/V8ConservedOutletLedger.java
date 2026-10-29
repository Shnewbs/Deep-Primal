package com.shnewbs.deepprimal.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bounded, explicit-call-only authority for transfers between *already funded*
 * V8 watershed accounts. Transfers never derive water from catchment area or
 * ocean stage: all three conserved quantities are removed from one account and
 * credited to another in the same immutable commit. No liquid rendering heights
 * or geology are modified here.
 *
 * <p>A native owner must persist the ENTIRE resulting Snapshot atomically in one
 * world-owned journal before acknowledging a commit. Persisting separate
 * chunk/account copies cannot provide the same crash guarantees. Caller-supplied
 * receivers are an authorization contract, NOT proof of global marine drainage.
 * This helper neither activates V8 generation nor performs Minecraft I/O.
 */
public final class V8ConservedOutletLedger {
  private V8ConservedOutletLedger() {}
  public static final int POLICY_VERSION = 1;
  public static final int MAX_ACCOUNTS = 16_384;
  public static final int MAX_BATCH = 4_096;

  public record Balance(long water, long salt, long sediment, long capacity) {
    public Balance {
      if (water < 0 || salt < 0 || sediment < 0 || capacity < 0 || water > capacity)
        throw new IllegalArgumentException("invalid physical outlet balance");
    }
    Balance debit(Transfer t) {
      if (water < t.water || salt < t.salt || sediment < t.sediment)
        throw new IllegalArgumentException("outlet source overdraft");
      return new Balance(water - t.water, salt - t.salt,
          sediment - t.sediment, capacity);
    }
    Balance credit(Transfer t) {
      long w = Math.addExact(water, t.water);
      long s = Math.addExact(salt, t.salt);
      long d = Math.addExact(sediment, t.sediment);
      if (w > capacity) throw new IllegalArgumentException("outlet receiver at capacity");
      return new Balance(w, s, d, capacity);
    }
  }

  /** Sequence 1 starts each source's persistent, strictly ordered history. */
  public record Transfer(long source, long receiver, long sequence,
                         long water, long salt, long sediment) {
    public Transfer {
      requireKey(source); requireKey(receiver);
      if (source == receiver || sequence <= 0 || water <= 0 || salt < 0 || sediment < 0)
        throw new IllegalArgumentException("invalid physical outlet transfer");
    }
  }

  /**
   * Entire durable checkpoint. lastReceipts is part of the saved state:
   * forgetting it on reload permits a second physical debit for the same event.
   * Only one retained receipt per source is needed because older sequence
   * numbers are rejected rather than silently interpreted as replay.
   */
  public record Snapshot(int version, Map<Long, Balance> accounts,
                         Map<Long, Long> receivers,
                         Map<Long, Transfer> lastReceipts) {
    public Snapshot {
      if (version != POLICY_VERSION) throw new IllegalArgumentException("outlet ledger policy revision");
      accounts = Map.copyOf(Objects.requireNonNull(accounts, "accounts"));
      receivers = Map.copyOf(Objects.requireNonNull(receivers, "receivers"));
      lastReceipts = Map.copyOf(Objects.requireNonNull(lastReceipts, "lastReceipts"));
      if (accounts.size() > MAX_ACCOUNTS || receivers.size() > MAX_ACCOUNTS
          || lastReceipts.size() > MAX_ACCOUNTS)
        throw new IllegalArgumentException("outlet ledger maximum residency exceeded");
      for (var entry : accounts.entrySet()) {
        requireKey(entry.getKey()); Objects.requireNonNull(entry.getValue(), "balance");
      }
      for (var entry : receivers.entrySet()) {
        requireKey(entry.getKey()); requireKey(entry.getValue());
        if (entry.getKey().equals(entry.getValue())
            || !accounts.containsKey(entry.getKey()) || !accounts.containsKey(entry.getValue()))
          throw new IllegalArgumentException("uncertified or missing outlet receiver");
      }
      // Every account has at most one downstream receiver. Reject any cycle,
      // including a loop in an otherwise disconnected loaded region.
      Set<Long> done = new HashSet<>();
      for (long start : receivers.keySet()) {
        if (done.contains(start)) continue;
        Set<Long> path = new HashSet<>();
        long current = start;
        while (receivers.containsKey(current) && !done.contains(current)) {
          if (!path.add(current)) throw new IllegalArgumentException("cyclic outlet authority");
          current = receivers.get(current);
        }
        done.addAll(path);
      }
      for (var entry : lastReceipts.entrySet()) {
        requireKey(entry.getKey());
        Transfer t = Objects.requireNonNull(entry.getValue(), "receipt");
        if (entry.getKey().longValue() != t.source()
            || !accounts.containsKey(t.source()) || !accounts.containsKey(t.receiver())
            || !Objects.equals(receivers.get(t.source()), t.receiver()))
          throw new IllegalArgumentException("receipt outside authoritative outlet graph");
      }
    }
  }

  /** Only successful caller-persisted snapshots can be treated as committed. */
  public record Result(Snapshot snapshot, int applied, int replayed) {}

  public static Snapshot open(Map<Long, Balance> accounts, Map<Long, Long> receivers) {
    return new Snapshot(POLICY_VERSION, accounts, receivers, Map.of());
  }

  public static Result apply(Snapshot snapshot, Transfer transfer) {
    return applyBatch(snapshot, List.of(transfer));
  }

  /**
   * A bounded atomic batch; rejection returns no candidate state. The caller
   * must serialize/publish the complete returned snapshot in one transaction.
   */
  public static Result applyBatch(Snapshot snapshot, Collection<Transfer> requests) {
    Objects.requireNonNull(snapshot, "snapshot");
    Objects.requireNonNull(requests, "requests");
    if (requests.size() > MAX_BATCH) throw new IllegalArgumentException("outlet batch too large");
    Map<Long, Balance> balances = new HashMap<>(snapshot.accounts());
    Map<Long, Transfer> receipts = new HashMap<>(snapshot.lastReceipts());
    int applied = 0, replayed = 0;
    for (Transfer transfer : requests) {
      Transfer t = Objects.requireNonNull(transfer, "transfer");
      if (!Objects.equals(snapshot.receivers().get(t.source()), t.receiver()))
        throw new IllegalArgumentException("outlet transfer is not an authorized receiver edge");
      Transfer last = receipts.get(t.source());
      if (last != null && t.sequence() <= last.sequence()) {
        if (last.equals(t)) { replayed++; continue; }
        throw new IllegalArgumentException("conflicting or stale outlet transfer sequence");
      }
      long expected = last == null ? 1 : Math.addExact(last.sequence(), 1);
      if (t.sequence() != expected)
        throw new IllegalArgumentException("out-of-order outlet transfer sequence");
      Balance source = Objects.requireNonNull(balances.get(t.source()), "source account");
      Balance destination = Objects.requireNonNull(balances.get(t.receiver()), "receiver account");
      Balance debit = source.debit(t), credit = destination.credit(t);
      balances.put(t.source(), debit);
      balances.put(t.receiver(), credit);
      receipts.put(t.source(), t);
      applied++;
    }
    if (applied == 0) return new Result(snapshot, 0, replayed);
    return new Result(new Snapshot(POLICY_VERSION, balances, snapshot.receivers(), receipts),
        applied, replayed);
  }

  /** Existing watershed port identity; signed coordinates must be 32-aligned. */
  public static long port(int x, int z) { return WatershedOutletTopology.key(x, z); }

  private static void requireKey(long key) {
    int x = (int) (key >> 32), z = (int) key;
    if (WatershedOutletTopology.key(x, z) != key)
      throw new IllegalArgumentException("invalid outlet port key");
  }
}
