package com.shnewbs.deepprimal.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class V8ConservedOutletLedgerChecks {
  private static long checks;
  private static void ok(boolean condition) {
    checks++;
    if (!condition) throw new AssertionError("failed assertion " + checks);
  }
  private static void rejects(Runnable task) {
    boolean failed = false;
    try { task.run(); } catch (IllegalArgumentException | ArithmeticException expected) { failed = true; }
    ok(failed);
  }
  private static long[] totals(V8ConservedOutletLedger.Snapshot s) {
    long[] t = new long[3];
    for (var b : s.accounts().values()) {
      t[0] = Math.addExact(t[0], b.water());
      t[1] = Math.addExact(t[1], b.salt());
      t[2] = Math.addExact(t[2], b.sediment());
    }
    return t;
  }
  private static void sameTotals(long[] left, long[] right) {
    for (int i = 0; i < 3; i++) ok(left[i] == right[i]);
  }
  public static void main(String[] args) {
    long a = V8ConservedOutletLedger.port(-8192, 32);
    long b = V8ConservedOutletLedger.port(-8160, 32);
    long c = V8ConservedOutletLedger.port(-8128, 32);
    var ledger = V8ConservedOutletLedger.open(Map.of(
        a, new V8ConservedOutletLedger.Balance(50, 30, 20, 100),
        b, new V8ConservedOutletLedger.Balance(10, 5, 4, 100),
        c, new V8ConservedOutletLedger.Balance(0, 0, 0, 100)), Map.of(a, b, b, c));
    var t = new V8ConservedOutletLedger.Transfer(a, b, 1, 7, 4, 2);
    long[] original = totals(ledger);
    var once = V8ConservedOutletLedger.apply(ledger, t);
    ok(once.applied() == 1 && once.replayed() == 0);
    sameTotals(original, totals(once.snapshot()));
    ok(once.snapshot().accounts().get(a).water() == 43);
    ok(once.snapshot().accounts().get(b).water() == 17);
    V8ConservedOutletLedger.Result duplicate;
    try {
      duplicate = V8ConservedOutletLedger.apply(once.snapshot(), t);
    } catch (IllegalArgumentException unexpected) {
      throw new AssertionError("An exact committed transfer replay must be accepted", unexpected);
    }
    ok(duplicate.applied() == 0 && duplicate.replayed() == 1);
    ok(duplicate.snapshot() == once.snapshot());
    // Persist/reopen all maps, including the last receipt, before replaying.
    var reopened = new V8ConservedOutletLedger.Snapshot(
        V8ConservedOutletLedger.POLICY_VERSION, once.snapshot().accounts(),
        once.snapshot().receivers(), once.snapshot().lastReceipts());
    ok(V8ConservedOutletLedger.apply(reopened, t).applied() == 0);
    sameTotals(original, totals(reopened));
    rejects(() -> V8ConservedOutletLedger.apply(reopened,
        new V8ConservedOutletLedger.Transfer(a, b, 1, 8, 4, 2)));
    rejects(() -> V8ConservedOutletLedger.apply(reopened,
        new V8ConservedOutletLedger.Transfer(a, b, 3, 1, 0, 0)));
    rejects(() -> V8ConservedOutletLedger.apply(reopened,
        new V8ConservedOutletLedger.Transfer(a, c, 2, 1, 0, 0)));
    rejects(() -> V8ConservedOutletLedger.apply(reopened,
        new V8ConservedOutletLedger.Transfer(a, b, 2, 50, 0, 0)));
    rejects(() -> V8ConservedOutletLedger.apply(reopened,
        new V8ConservedOutletLedger.Transfer(a, b, 2, 1, 99, 0)));
    rejects(() -> V8ConservedOutletLedger.open(Map.of(
        a, new V8ConservedOutletLedger.Balance(0, 0, 0, 0),
        b, new V8ConservedOutletLedger.Balance(0, 0, 0, 0)), Map.of(a, b, b, a)));
    rejects(() -> V8ConservedOutletLedger.port(-8191, 32));
    rejects(() -> new V8ConservedOutletLedger.Balance(101, 0, 0, 100));
    rejects(() -> new V8ConservedOutletLedger.Transfer(a, a, 1, 1, 0, 0));
    rejects(() -> V8ConservedOutletLedger.applyBatch(reopened, List.of(
        new V8ConservedOutletLedger.Transfer(a, b, 2, 1, 1, 1),
        new V8ConservedOutletLedger.Transfer(a, b, 4, 1, 0, 0))));
    sameTotals(original, totals(reopened));
    rejects(() -> new V8ConservedOutletLedger.Snapshot(
        V8ConservedOutletLedger.POLICY_VERSION, reopened.accounts(),
        reopened.receivers(), Map.of(b, t)));
    rejects(() -> V8ConservedOutletLedger.open(Map.of(
        a, new V8ConservedOutletLedger.Balance(1, 0, 0, 1)), Map.of(a, b)));
    // Overflow may not wrap constituent balances or capacity checks.
    var overflow = V8ConservedOutletLedger.open(Map.of(
        a, new V8ConservedOutletLedger.Balance(10, 1, 1, 10),
        b, new V8ConservedOutletLedger.Balance(1, Long.MAX_VALUE, 0, 10)), Map.of(a, b));
    rejects(() -> V8ConservedOutletLedger.apply(overflow,
        new V8ConservedOutletLedger.Transfer(a, b, 1, 1, 1, 0)));

    // Randomized long-lived, exactly-conserved signed-region transects.
    var ports = new long[12];
    var accounts = new HashMap<Long, V8ConservedOutletLedger.Balance>();
    var receivers = new HashMap<Long, Long>();
    for (int i = 0; i < ports.length; i++) {
      ports[i] = V8ConservedOutletLedger.port(-16384 + i * 32, -8192 + (i / 3) * 32);
      accounts.put(ports[i], new V8ConservedOutletLedger.Balance(5000, 5000, 5000, 1_000_000));
      if (i > 0) receivers.put(ports[i], ports[i - 1]);
    }
    var random = new Random(20261029);
    var state = V8ConservedOutletLedger.open(accounts, receivers);
    long[] expected = totals(state);
    long[] seq = new long[12];
    for (int attempt = 0; attempt < 10_000; attempt++) {
      int i = 1 + random.nextInt(11);
      var source = state.accounts().get(ports[i]);
      long amount = Math.min(source.water(), 1 + random.nextInt(5));
      if (amount == 0) continue;
      long salt = Math.min(source.salt(), random.nextInt(4));
      long sediment = Math.min(source.sediment(), random.nextInt(4));
      var event = new V8ConservedOutletLedger.Transfer(ports[i], ports[i-1],
          ++seq[i], amount, salt, sediment);
      var result = V8ConservedOutletLedger.applyBatch(state, List.of(event, event));
      ok(result.applied() == 1 && result.replayed() == 1);
      state = result.snapshot();
      if (attempt % 97 == 0) {
        state = new V8ConservedOutletLedger.Snapshot(1,
            new HashMap<>(state.accounts()), new HashMap<>(state.receivers()),
            new HashMap<>(state.lastReceipts()));
        ok(V8ConservedOutletLedger.apply(state, event).replayed() == 1);
      }
      sameTotals(expected, totals(state));
    }
    ok(state.lastReceipts().size() <= 11);
    ok(state.accounts().size() == 12);
    System.out.println("V8_OUTLET_LEDGER_PASS assertions=" + checks + " accounts="
        + state.accounts().size() + " receipts=" + state.lastReceipts().size());
  }
}
