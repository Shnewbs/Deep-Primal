package com.shnewbs.deepprimal.core;

import java.util.*;

final class WaterBudgetChecks {
  private static void check(boolean ok, String what) { if (!ok) throw new AssertionError(what); }
  static void run() {
    droughtReserve();
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
    for (double surface : new double[] {-511.9375, 96, 288.25, 2047.9375})
      for (long reserve : new long[] {Aquifer.STOP - 256, Aquifer.STOP, Aquifer.STOP + 256,
          Aquifer.START, Aquifer.CAPACITY}) {
        var perched = new Aquifer(new WaterAccount.Parcel(reserve, 0, 0),
            WaterAccount.Parcel.EMPTY, 30, true, 0);
        double datum = Aquifer.naturalDatum(surface);
        double expected = surface + 8 * (reserve - Aquifer.STOP) / (double) Aquifer.CAPACITY;
        check(perched.head(datum) == expected, "Natural source head uses its own planned elevation");
        check((perched.dischargeUnits(datum, surface, 16, 4) > 0) == (reserve > Aquifer.STOP),
            "Natural source ceases at its finite drought reserve");
        check(perched.dischargeUnits(datum, expected, 16, 4) == 0,
            "Natural source backwater blocks output");
      }
    for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      try {
        Aquifer.naturalDatum(bad);
        throw new AssertionError("Nonfinite natural head accepted");
      } catch (IllegalArgumentException expected) {}
    }
    System.out.println("NATURAL_AQUIFER_HEAD_PASS elevations=4 reserves=5 droughtReserve=true backwater=true");
    check(Aquifer.baseflowUnits(64) == 80 && Aquifer.baseflowUnits(0) == 32
        && Aquifer.baseflowUnits(-5) == 32 && Aquifer.baseflowUnits(4096) == 5173
        && Aquifer.baseflowUnits(256) > Aquifer.baseflowUnits(128), "Regional natural baseflow sizing");
    System.out.println("NATURAL_BASEFLOW_QUOTA_PASS default=80 floor=32 monotonic=true");
    weatherCycles();
    System.out.println("WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true");
  }

  private static void droughtReserve() {
    double datum = Aquifer.naturalDatum(288.25);
    for (long remaining : new long[] {0, 128, 256, 384, 512, 768}) {
      var a = new Aquifer(new WaterAccount.Parcel(Aquifer.STOP + remaining, 8191, 16381),
          WaterAccount.Parcel.EMPTY, Aquifer.WET_STEPS, true, 0, Aquifer.WET_STEPS);
      // A drained pool below the planned stage still has positive pressure at the drought reserve.
      int out = a.dischargeUnits(datum, 286, 16, 16);
      check(out <= remaining / WaterAccount.SCALE,
          "Deficit-cell output spent the drought reserve: remaining=" + remaining + " units=" + out);
      a.ground.take(out * WaterAccount.SCALE);
      check(a.ground.water() >= Aquifer.STOP, "Outlet crossed the drought reserve within one interval");
      if (a.ground.water() == Aquifer.STOP)
        check(!a.canSupply(true) && !a.canSupply(false),
            "Supply stayed available at the drought reserve before the next step");
      check(a.dischargeUnits(datum, 286, 16, 16) == 0,
          "Repeated outlet visit spent the sub-unit reserve remainder");
    }
    System.out.println("AQUIFER_DROUGHT_RESERVE_PASS cases=6 deficitCell=true immediateStop=true subUnitRemainder=true");
  }

  private static void weatherCycles() {
    int scenarios = 0, droughtStops = 0, restarts = 0, reconstructed = 0;
    long steps = 0;
    for (int rainRate : new int[] {0, 64, 4096}) {
      for (int loadedColumns : new int[] {256, 4096}) {
        var a = new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY * 7 / 10, 131071, 524287),
            WaterAccount.Parcel.EMPTY, Aquifer.WET_STEPS, true, 0);
        var atmosphere = new WaterAccount(WaterAccount.MAX, new WaterAccount.Parcel(1L << 32, 0, 0));
        var downstream = new WaterAccount(WaterAccount.MAX, new WaterAccount.Parcel(65536, 917503, 1048573));
        long[] initial = totals(a.ground, a.snow, atmosphere, downstream);
        int quota = Aquifer.baseflowUnits(rainRate);
        long tick = 0;
        boolean stopped = false, restarted = false;
        for (int interval = 0; interval < 30000; interval++) {
          tick += 20;
          check(a.step(tick) && !a.step(tick), "Environmental interval ran twice");
          // First exhaust the seeded reserve. Then exercise three rain/snow/melt cycles,
          // each with 900 wet and 4,800 dry intervals, followed by another long drought.
          int cycle = interval - 12000;
          boolean wet = cycle >= 0 && cycle < 17100 && cycle % 5700 < 900;
          boolean snowing = wet && cycle / 5700 == 1;
          if (wet)
            WaterAccount.move(atmosphere, snowing ? a.snow : a.ground,
                (long) rainRate * loadedColumns / 2);
          else a.ground.evaporateInto(atmosphere, 64);
          if (!snowing) WaterAccount.move(a.snow, a.ground, 4096);
          if (interval < 12000 && !a.canSupply(true)) stopped = true;
          if (stopped && a.canSupply(true)) restarted = true;
          long before = a.ground.water();
          int delivered = 0;
          for (int outlet = 0; outlet < 512 && delivered < quota; outlet++) {
            if (!a.canSupply(true)) break;
            int out = a.dischargeUnits(Aquifer.naturalDatum(288.25), 286, 16, quota - delivered);
            if (out == 0) break;
            long moved = WaterAccount.move(a.ground, downstream, (long) out * WaterAccount.SCALE);
            check(moved == (long) out * WaterAccount.SCALE, "Paid source delivery was not accepted");
            delivered += out;
          }
          check(delivered <= quota && before - a.ground.water() == (long) delivered * WaterAccount.SCALE,
              "Natural interval exceeded its quota or debited unaccepted water");
          check(delivered == 0 || a.ground.water() >= Aquifer.STOP, "Multi-outlet drought reserve overshoot");
          check(a.ground.water() > Aquifer.STOP
                  || a.dischargeUnits(Aquifer.naturalDatum(288.25), 284, 16, 16) == 0,
              "Deep deficit bypassed drought shutdown");
          check(Arrays.equals(initial, totals(a.ground, a.snow, atmosphere, downstream)),
              "Weather cycle lost water, salt or sediment at interval " + interval);
          if (interval % 997 == 0) {
            long lastTick = a.lastTick();
            var copy = new Aquifer(a.ground.snapshot(), a.snow.snapshot(), a.wetSteps(),
                a.active(), lastTick, a.dynamicWetSteps());
            check(copy.canSupply(true) == a.canSupply(true) && copy.canSupply(false) == a.canSupply(false)
                && !copy.step(lastTick), "Reconstruction changed supply or duplicated a step");
            a = copy;
            reconstructed++;
          }
          steps++;
        }
        check(stopped, "Long drought did not stop natural supply");
        check(restarted == (rainRate != 0), "Storm restart did not match available precipitation");
        droughtStops++;
        if (restarted) restarts++;
        scenarios++;
      }
    }
    System.out.println("AQUIFER_WEATHER_CYCLE_PASS scenarios=" + scenarios + " intervals=" + steps
        + " droughtStops=" + droughtStops + " stormRestarts=" + restarts + " reconstructed=" + reconstructed
        + " exactWaterSaltSediment=true rainSnowMelt=true partialCatchments=true");
  }

  private static long[] totals(WaterAccount... accounts) {
    long[] total = new long[3];
    for (var account : accounts) {
      var p = account.snapshot();
      total[0] += p.water(); total[1] += p.salt(); total[2] += p.sediment();
    }
    return total;
  }
}
