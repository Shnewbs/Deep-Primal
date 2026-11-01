package com.shnewbs.deepprimal.core.organic;

import com.shnewbs.deepprimal.core.organic.SoilNutrientLedger.Nutrients;
import java.util.Random;

/** Dependency-free soil pool invariants; executable without NeoForge. */
public final class SoilNutrientLedgerChecks {
  private static long checks;
  private static void check(boolean ok, String reason) {
    checks++;
    if (!ok) throw new AssertionError(reason);
  }
  private static void reject(Runnable action, String reason) {
    boolean rejected = false;
    try { action.run(); }
    catch (IllegalArgumentException | ArithmeticException | NullPointerException expected) {
      rejected = true;
    }
    check(rejected, reason);
  }
  private static void same(Nutrients a, Nutrients b, String label) {
    check(a.equals(b), label + " expected=" + a + " actual=" + b);
  }
  private static Nutrients total(SoilNutrientLedger ledger) {
    return ledger.mineral().plus(ledger.detritus());
  }
  public static void main(String[] args) {
    var initial = SoilNutrientLedger.EMPTY;
    check(initial.schemaVersion() == 1 && initial.revision() == 0, "empty identity");
    same(initial.creditMineral(Nutrients.ZERO).mineral(), Nutrients.ZERO, "zero deposit");
    check(initial.creditMineral(Nutrients.ZERO) == initial, "zero input is no-op");
    reject(() -> new Nutrients(-1, 0, 0), "negative component");
    reject(() -> new SoilNutrientLedger(2, 0, Nutrients.ZERO, Nutrients.ZERO), "future schema");
    reject(() -> new SoilNutrientLedger(1, -1, Nutrients.ZERO, Nutrients.ZERO), "negative revision");
    reject(() -> initial.creditMineral(null), "null credit");
    reject(() -> initial.mineralize(null), "null mineralization");
    reject(() -> new Nutrients(Long.MAX_VALUE, 0, 0).plus(new Nutrients(1, 0, 0)), "overflow");
    reject(() -> new Nutrients(1, 0, 0).minus(new Nutrients(2, 0, 0)), "overdraw");
    reject(() -> new SoilNutrientLedger(1, Long.MAX_VALUE, Nutrients.ZERO, Nutrients.ZERO)
        .creditMineral(new Nutrients(1, 0, 0)), "revision overflow");
    reject(() -> new SoilNutrientLedger(1, 3, new Nutrients(Long.MAX_VALUE, 0, 0),
        new Nutrients(1, 0, 0)).mineralize(new Nutrients(1, 0, 0)), "mineral overflow");

    var rng = new Random(0x0A64A1C5L);
    for (int i = 0; i < 100_000; i++) {
      var minerals = new Nutrients(rng.nextInt(1000), rng.nextInt(1000), rng.nextInt(1000));
      var litter = new Nutrients(rng.nextInt(1000), rng.nextInt(1000), rng.nextInt(1000));
      var s = new SoilNutrientLedger(1, rng.nextInt(10_000), minerals, litter);
      var original = total(s);
      var demand = new Nutrients(rng.nextInt(1500), rng.nextInt(1500), rng.nextInt(1500));
      var mineralized = s.mineralize(demand);
      same(total(mineralized), original, "mineralization conservation");
      var expectedMineralized = litter.limitedTo(demand);
      same(mineralized.mineral(), minerals.plus(expectedMineralized), "mineralization paid amount");
      same(mineralized.detritus(), litter.minus(expectedMineralized), "mineralization detritus debit");
      var crop = mineralized.uptake(demand);
      same(crop.amount(), mineralized.mineral().limitedTo(demand), "exact uptake amount");
      same(total(crop.ledger()).plus(crop.amount()), original, "uptake conservation");
      var drainage = crop.ledger().leach(demand);
      same(drainage.amount(), crop.ledger().mineral().limitedTo(demand), "exact drainage amount");
      same(total(drainage.ledger()).plus(crop.amount()).plus(drainage.amount()),
          original, "drainage conservation");
      check(crop.amount().nitrogen() <= demand.nitrogen(), "bounded N uptake");
      check(crop.amount().phosphorus() <= demand.phosphorus(), "bounded P uptake");
      check(crop.amount().potassium() <= demand.potassium(), "bounded K uptake");
      check(drainage.ledger().revision() >= s.revision(), "monotone revision");
      check(s.mineral().equals(minerals) && s.detritus().equals(litter), "immutable source");
      var external = new Nutrients(3, 4, 5);
      same(total(s.creditDetritus(external)), original.plus(external), "paid litter credit");
      same(total(s.creditMineral(external)), original.plus(external), "paid mineral credit");
      var dry = new SoilNutrientLedger(1, s.revision(), Nutrients.ZERO, litter);
      check(dry.uptake(demand).ledger() == dry, "empty mineral uptake no-op");
    }
    System.out.println("ORGANIC_SOIL_LEDGER_PASS checks=" + checks
        + " trials=100000 conservation=true overflowRejected=true");
  }
  private SoilNutrientLedgerChecks() {}
}
