package com.shnewbs.deepprimal.core;

import java.util.*;

final class WaterBudgetChecks {
  private static void check(boolean ok, String what) { if (!ok) throw new AssertionError(what); }
  static void run() {
    var random = new Random(26);
    WaterAccount[] stores = new WaterAccount[12];
    long water = 0, salt = 0, sediment = 0;
    for (int i = 0; i < stores.length; i++) {
      var p = new WaterAccount.Parcel(80000 + i * 311, i * 23, i * 701);
      stores[i] = new WaterAccount(120000, p);
      water += p.water(); salt += p.salt(); sediment += p.sediment();
    }
    for (int i = 0; i < 200000; i++) {
      int a = random.nextInt(stores.length), b = random.nextInt(stores.length);
      if ((i & 7) == 0) stores[a].evaporateInto(stores[b], random.nextInt(500));
      else WaterAccount.move(stores[a], stores[b], random.nextInt(5000));
    }
    long w = 0, s = 0, d = 0;
    for (var a : stores) { var p = a.snapshot(); w += p.water(); s += p.salt(); d += p.sediment(); }
    check(w == water && s == salt && d == sediment, "Loops, splits, evaporation conserve exact totals");
    var full = new WaterAccount(1, new WaterAccount.Parcel(1, 7, 9));
    var source = stores[0]; var before = source.snapshot();
    check(WaterAccount.move(source, full, 4096) == 0 && source.snapshot().equals(before), "Blocked destination retains water");
    var huge = new WaterAccount(WaterAccount.MAX, new WaterAccount.Parcel(WaterAccount.MAX / 2, WaterAccount.MAX * 100, 9));
    var half = huge.take(WaterAccount.MAX / 4);
    check(half.salt() == WaterAccount.MAX * 50 && half.water() == WaterAccount.MAX / 4, "Overflow-safe ocean chemistry");
    var a = new Aquifer(new WaterAccount.Parcel(Aquifer.START, 0, 0), WaterAccount.Parcel.EMPTY, 0, false, 0);
    for (int i = 1; i < Aquifer.WET_STEPS; i++) { a.step(i * 20); check(!a.active(), "No instant spring activation"); }
    a.step(600); check(a.active(), "Sustained recharge activates spring");
    check(a.dischargeUnits(100, 99, 16, 4) == 0, "Backpressure blocks output above head");
    check(a.dischargeUnits(100, 90, 0, 4) == 0, "Solid/full destination blocks output");
    long initial = a.ground.water();
    for (int i = 0; i < 100000 && a.active(); i++) {
      int out = a.dischargeUnits(100, 90, 16, 4);
      a.ground.take(out * WaterAccount.SCALE); a.step(620L + i * 20L);
    }
    check(!a.active() && a.ground.water() <= Aquifer.STOP && a.ground.water() < initial, "Finite drought dries spring");
    a.ground.add(new WaterAccount.Parcel(Aquifer.START, 0, 0));
    a.step(a.lastTick() + 20L * 1000000);
    check(!a.active() && a.wetSteps() == 1, "No offline recharge/activation backlog");
    for (int i = 0; i < 29; i++) a.step(a.lastTick() + 20);
    check(a.active(), "Environmental refill revives dried groundwater");
    var seeded = new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY * 7 / 10,0,0), WaterAccount.Parcel.EMPTY,30,true,0);
    for(int i=1;i<=300;i++) seeded.step(i*20);
    check(seeded.canSupply(true) && !seeded.canSupply(false), "Unrecharged dry land cannot become a spring merely with time or chunk visits");
    seeded.ground.add(new WaterAccount.Parcel(Aquifer.CAPACITY/5,0,0));
    for(int i=0;i<29;i++) {seeded.step(seeded.lastTick()+20);check(!seeded.canSupply(false), "New outlet must wait for sustained high groundwater");}
    seeded.step(seeded.lastTick()+20); check(seeded.canSupply(false), "Sustained recharge permits an outlet anywhere with head");
    seeded.ground.take(Aquifer.CAPACITY);seeded.step(seeded.lastTick()+20);
    check(!seeded.canSupply(false), "Depleted dynamic outlet stops");
    System.out.println("WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true");
  }
}
