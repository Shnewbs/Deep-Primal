package com.shnewbs.deepprimal.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Java21 component and actual local-FS tests, not native Minecraft validation. */
public final class V8OutletAuthorityAdmissionChecks {
  private V8OutletAuthorityAdmissionChecks() {}
  private static long assertions;
  private static void check(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason);
  }
  private interface Attempt { void run() throws Exception; }
  private static void rejects(Attempt attempt) throws Exception {
    boolean rejected=false;
    try { attempt.run(); }
    catch (IllegalArgumentException | ArithmeticException | IOException expected) {
      rejected=true;
    }
    check(rejected, "expected fail-closed authority admission");
  }
  private static long key(int x, int z) { return WatershedOutletTopology.key(x,z); }
  private record Fixture(List<WatershedOutletTopology.Port> ports,
      List<WatershedOutletTopology.Link> links, Set<Long> roots,
      Map<Long, Long> authorized,
      Map<Long, V8ConservedOutletLedger.Balance> balances) {}
  private static Fixture fixture(int x, int z) {
    long a=key(x,z), b=key(x+32,z), c=key(x+64,z);
    return new Fixture(List.of(
        new WatershedOutletTopology.Port(x,z,112,0,false,false),
        new WatershedOutletTopology.Port(x+32,z,101,0,false,false),
        new WatershedOutletTopology.Port(x+64,z,90,0,true,false)),
        List.of(new WatershedOutletTopology.Link(x,z,x+32,z,111),
            new WatershedOutletTopology.Link(x+32,z,x+64,z,99)),
        Set.of(c), Map.of(a,b,b,c), Map.of(
            a,new V8ConservedOutletLedger.Balance(1200,100,50,1200),
            b,new V8ConservedOutletLedger.Balance(200,20,10,2000),
            c,new V8ConservedOutletLedger.Balance(300,200,70,100000)));
  }
  private static V8OutletAuthorityAdmission.Admission admit(Fixture f) {
    return V8OutletAuthorityAdmission.admit(f.ports(), f.links(), 96,
        f.roots(), f.authorized(), f.balances());
  }
  private static long[] totals(V8ConservedOutletLedger.Snapshot s) {
    long w=0, salt=0, sed=0;
    for (var b:s.accounts().values()) {
      w+=b.water(); salt+=b.salt(); sed+=b.sediment();
    }
    return new long[]{w,salt,sed};
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("scratch path required");
    var f=fixture(-64,-32);
    var admitted=admit(f);
    check(admitted.admittedPorts()==3 && admitted.admittedLinks()==2,"bounds");
    check(admitted.marineRoots().equals(f.roots()),"marine anchors retained");
    check(admitted.snapshot().receivers().equals(f.authorized()),"only independently witnessed routes");
    check(admitted.snapshot().lastReceipts().isEmpty(),"no fabricated receipts");
    check(java.util.Arrays.equals(totals(admitted.snapshot()),new long[]{1700,320,130}),
        "funded volume and constituents preserved");
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        Set.of(key(999*32,-32),key(0,-32)),f.authorized(),f.balances()));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        Set.of(key(-64,-32)),f.authorized(),f.balances()));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(), Map.of(key(-64,-32),key(-32,-32)),f.balances()));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(), Map.of(key(-64,-32),key(0,-32),key(-32,-32),key(0,-32)),f.balances()));
    var fundsPlus=new HashMap<>(f.balances());
    fundsPlus.put(key(128,0),new V8ConservedOutletLedger.Balance(1,0,0,1));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(),f.authorized(),fundsPlus));
    var fundsMissing=new HashMap<>(f.balances()); fundsMissing.remove(key(-64,-32));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(),f.authorized(),fundsMissing));
    var unknown=new ArrayList<>(f.ports()); unknown.set(0,
        new WatershedOutletTopology.Port(-64,-32,112,0,false,true));
    rejects(() -> V8OutletAuthorityAdmission.admit(unknown,f.links(),96,
        f.roots(),f.authorized(),f.balances()));
    var unknownOcean=new ArrayList<>(f.ports()); unknownOcean.set(2,
        new WatershedOutletTopology.Port(0,-32,90,0,true,true));
    rejects(() -> V8OutletAuthorityAdmission.admit(unknownOcean,f.links(),96,
        f.roots(),f.authorized(),f.balances()));
    // A solitary ocean anchor has no upstream unresolved component to mask this guard.
    rejects(() -> V8OutletAuthorityAdmission.admit(
        List.of(new WatershedOutletTopology.Port(0,-32,90,0,true,true)), List.of(),96,
        Set.of(key(0,-32)),Map.of(),Map.of(key(0,-32),f.balances().get(key(0,-32)))));
    var runoff=new ArrayList<>(f.ports()); runoff.set(0,
        new WatershedOutletTopology.Port(-64,-32,112,50,false,false));
    rejects(() -> V8OutletAuthorityAdmission.admit(runoff,f.links(),96,
        f.roots(),f.authorized(),f.balances()));
    var disconnected=new ArrayList<>(f.ports()); disconnected.add(
        new WatershedOutletTopology.Port(128,-32,120,0,false,false));
    var disconnectedFunds=new HashMap<>(f.balances());
    disconnectedFunds.put(key(128,-32),new V8ConservedOutletLedger.Balance(1,1,1,2));
    rejects(() -> V8OutletAuthorityAdmission.admit(disconnected,f.links(),96,
        f.roots(),f.authorized(),disconnectedFunds));
    var overflowFunds=new HashMap<>(f.balances());
    overflowFunds.put(key(-64,-32),new V8ConservedOutletLedger.Balance(Long.MAX_VALUE,100,50,Long.MAX_VALUE));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(),f.authorized(),overflowFunds));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        Set.of(),f.authorized(),f.balances()));
    rejects(() -> V8OutletAuthorityAdmission.admit(f.ports(),f.links(),96,
        f.roots(),Map.of(),f.balances()));

    // Roundtrip through the EXISTING real, world-bound durability mechanism.
    Path root=Path.of(args[0]); Files.createDirectories(root);
    Path journal=root.resolve("v8-outlet.snapshot");
    var world=new V8OutletLedgerStore.WorldBinding(UUID.fromString(
        "12345678-aaaa-4bcd-9999-0123456789ab"),"ab".repeat(32));
    try(var store=V8OutletAuthorityAdmission.createBound(journal,admitted,world)) {
      var move=new V8ConservedOutletLedger.Transfer(key(-64,-32),key(-32,-32),1,17,5,3);
      var moved=store.transact(1,List.of(move));
      check(moved.generation()==2 && moved.result().applied()==1,"durable movement");
      check(java.util.Arrays.equals(totals(moved.result().snapshot()),totals(admitted.snapshot())),
          "exact triple conservation");
      check(store.transact(2,List.of(move)).generation()==2,"idempotent replay");
      check(store.snapshot().lastReceipts().get(key(-64,-32)).equals(move),"receipt persisted");
    }
    try(var restored=V8OutletLedgerStore.openBound(journal,world)) {
      check(restored.generation()==2,"generation after restart");
      check(java.util.Arrays.equals(totals(restored.snapshot()),totals(admitted.snapshot())),
          "restart exact conservation");
      check(restored.snapshot().receivers().equals(f.authorized()),"restart route identity");
      var last=restored.snapshot().lastReceipts().get(key(-64,-32));
      check(restored.transact(2,List.of(last)).generation()==2,"restart replay no double debit");
    }
    rejects(() -> V8OutletLedgerStore.openBound(journal,new V8OutletLedgerStore.WorldBinding(
        UUID.fromString("12345678-aaaa-4bcd-9999-0123456789ac"),"ab".repeat(32))));

    // Stable signed-coordinate and input permutation replay without per-call RNG.
    for (int i=0;i<2048;i++) {
      int origin=(i-1024)*96;
      int z=((i*17)%4096-2048)*32;
      var t=fixture(origin,z);
      var shuffledP=new ArrayList<>(t.ports());
      var shuffledL=new ArrayList<>(t.links());
      Collections.reverse(shuffledP);
      Collections.reverse(shuffledL);
      var baseline=admit(t);
      var permutation=V8OutletAuthorityAdmission.admit(shuffledP,shuffledL,96,
          t.roots(),t.authorized(),t.balances());
      check(baseline.snapshot().equals(permutation.snapshot()),"input order independence");
      check(baseline.snapshot().receivers().equals(t.authorized()),"exact routed conservation");
      check(java.util.Arrays.equals(totals(baseline.snapshot()),new long[]{1700,320,130}),
          "signed-coordinate conserved values");
      check(baseline.admittedPorts()==3 && baseline.admittedLinks()==2,"bounded account set");
    }
    System.out.println("V8_OUTLET_AUTHORITY_PASS assertions="+assertions+" cases=2048");
  }
}
