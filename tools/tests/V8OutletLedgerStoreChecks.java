package com.shnewbs.deepprimal.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Actual local filesystem/CAS checks. NOT Minecraft world persistence. */
public final class V8OutletLedgerStoreChecks {
  private static long checks;
  private V8OutletLedgerStoreChecks() {}
  private static void check(boolean ok, String label) {
    checks++;
    if (!ok) throw new AssertionError(label);
  }
  private interface Action { void run() throws Exception; }
  private static void rejected(Class<? extends Throwable> expected, Action action) {
    boolean caught = false;
    try { action.run(); }
    catch (Throwable error) {
      if (!expected.isInstance(error)) throw new AssertionError("unexpected failure", error);
      caught = true;
    }
    check(caught, "expected rejection: " + expected.getSimpleName());
  }
  private static long key(int x, int z) { return V8ConservedOutletLedger.port(x, z); }
  private static V8ConservedOutletLedger.Snapshot initial() {
    long a = key(-64, -32), b = key(-32, -32), c = key(0, -32);
    Map<Long, V8ConservedOutletLedger.Balance> balances = new HashMap<>();
    balances.put(a, new V8ConservedOutletLedger.Balance(500, 300, 200, 1000));
    balances.put(b, new V8ConservedOutletLedger.Balance(100, 70, 40, 1000));
    balances.put(c, new V8ConservedOutletLedger.Balance(200, 50, 20, 1000));
    return V8ConservedOutletLedger.open(balances, Map.of(a, b, b, c));
  }
  private static V8ConservedOutletLedger.Transfer t(long sequence, long water) {
    return new V8ConservedOutletLedger.Transfer(key(-64,-32), key(-32,-32), sequence,
        water, water / 2, water / 4);
  }
  private static long[] totals(V8ConservedOutletLedger.Snapshot snap) {
    long water=0, salt=0, sediment=0;
    for (var b : snap.accounts().values()) {
      water += b.water(); salt += b.salt(); sediment += b.sediment();
    }
    return new long[]{water, salt, sediment};
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("one scratch directory required");
    Path root = Path.of(args[0]);
    Files.createDirectories(root);
    Path file = root.resolve("outlet.snapshot");
    var starting = initial();
    long[] conserved = totals(starting);
    rejected(IOException.class, () -> V8OutletLedgerStore.open(file));
    try (var store = V8OutletLedgerStore.create(file, starting)) {
      check(store.generation() == 1, "initial generation");
      check(store.snapshot().equals(starting), "initial canonical state");
      rejected(IOException.class, () -> V8OutletLedgerStore.create(file, starting));
      rejected(IOException.class, () -> V8OutletLedgerStore.open(file));
      long expected = store.generation();
      // The ledger's two candidates from the same source are both individually
      // valid; only the persistent generation CAS may choose the winning one.
      var candidateA = V8ConservedOutletLedger.apply(starting, t(1, 12));
      var candidateB = V8ConservedOutletLedger.apply(starting, t(1, 16));
      check(!candidateA.snapshot().equals(candidateB.snapshot()), "candidate forks reproduced");
      CountDownLatch fire = new CountDownLatch(1);
      AtomicInteger accepted = new AtomicInteger();
      AtomicInteger stale = new AtomicInteger();
      AtomicReference<Throwable> failure = new AtomicReference<>();
      Thread[] threads = new Thread[2];
      for (int i=0;i<2;i++) {
        final int n=i;
        threads[i]=new Thread(() -> {
          try {
            fire.await();
            store.transact(expected, List.of(t(1, n==0?12:16)));
            accepted.incrementAndGet();
          } catch (IllegalStateException okay) { stale.incrementAndGet(); }
          catch (Throwable error) { failure.compareAndSet(null,error); }
        });
        threads[i].start();
      }
      fire.countDown();
      for (Thread thread : threads) thread.join();
      if (failure.get()!=null) throw new AssertionError("concurrent writer", failure.get());
      check(accepted.get()==1 && stale.get()==1, "single winning generation");
      check(store.generation()==2, "one generation committed");
      check(Arrays.equals(totals(store.snapshot()),conserved),"conservation after CAS");
      var receipt = store.snapshot().lastReceipts().get(key(-64,-32));
      var exact = store.transact(2, List.of(receipt));
      check(exact.generation()==2 && exact.result().applied()==0
          && exact.result().replayed()==1, "exact replay no disk advancement");
      rejected(IllegalArgumentException.class, () -> store.transact(2, List.of(t(1, 24))));
      rejected(IllegalArgumentException.class, () -> store.transact(2, List.of(t(3, 24))));
      check(store.generation()==2, "failed transfer not published");
      for (int i=2;i<=40;i++) {
        long g=store.generation();
        long amount=i%3+1;
        var commit=store.transact(g,List.of(t(i,amount)));
        check(commit.generation()==g+1 && commit.result().applied()==1, "monotone commit");
        check(Arrays.equals(totals(store.snapshot()),conserved), "triple conservation");
      }
    }
    try (var reopened = V8OutletLedgerStore.open(file)) {
      check(reopened.generation()==41,"reopened commit generation");
      check(Arrays.equals(totals(reopened.snapshot()), conserved), "reopened conservation");
      check(reopened.snapshot().lastReceipts().get(key(-64,-32)).sequence()==40,
          "receipt durable");
      var exact=reopened.transact(41,List.of(t(40,40%3+1)));
      check(exact.generation()==41 && exact.result().replayed()==1, "restart replay idempotent");
      rejected(IllegalArgumentException.class,()->reopened.transact(41,List.of(t(39,2))));
      rejected(IllegalStateException.class,()->reopened.transact(40,List.of(t(41,2))));
      Path pending = file.resolveSibling("outlet.snapshot.pending");
      Files.write(pending,new byte[]{1,2,3}); // abandoned unacknowledged scratch write
      check(reopened.generation()==41,"pending does not change accepted image");
      check(Files.exists(pending), "store does not silently consume pending data");
    }
    try (var reopened = V8OutletLedgerStore.open(file)) {
      check(reopened.generation()==41,"ignore stale pre-rename pending on reopen");
      var result = reopened.transact(41,List.of(t(41,2)));
      check(result.generation()==42,"atomic replace pending after reopen");
    }
    byte[] valid=Files.readAllBytes(file);
    byte[] invalid=valid.clone();
    invalid[39]^=0x01;
    Files.write(file,invalid);
    rejected(IOException.class,()->V8OutletLedgerStore.open(file));
    Files.write(file,valid);
    try(var store=V8OutletLedgerStore.open(file)) {
      check(store.generation()==42,"restore exact valid frame");
      // The OS file lock does not protect against *nonparticipant* writers.
      // Detect a malicious/uncoordinated replacement, poison, require reopen.
      Files.write(file,invalid);
      rejected(IOException.class,()->store.transact(42,List.of(t(42,2))));
      rejected(IllegalStateException.class,store::generation);
    }
    Files.write(file,valid);
    try(var restored=V8OutletLedgerStore.open(file)) {
      check(restored.generation()==42,"poison recovery requires disk reopening");
      check(Arrays.equals(totals(restored.snapshot()),conserved),"post-recovery totals");
    }
    // Even a validly checksummed empty snapshot is a valid durable authority.
    Path empty=root.resolve("empty.snapshot");
    try(var store=V8OutletLedgerStore.create(empty,V8ConservedOutletLedger.open(Map.of(),Map.of()))) {
      check(store.generation()==1,"empty store created");
    }
    try(var reopened=V8OutletLedgerStore.open(empty)) {
      check(reopened.snapshot().accounts().isEmpty(),"empty store reopens");
    }
    // Deleting a checkpoint must never reconstruct an imaginary initial balance.
    Files.delete(empty);
    rejected(IOException.class,()->V8OutletLedgerStore.open(empty));
    System.out.println("V8_OUTLET_STORE_PASS assertions="+checks+
        " generation=42 persistedTriplet="+Arrays.toString(conserved));
  }
}
