package com.shnewbs.deepprimal.core.organic;

import com.shnewbs.deepprimal.core.organic.FoundationsSoilPhysicsBridge.*;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;

/** Deterministic differential formula checks against Foundations Soil 0.1.0-beta. */
public final class FoundationsSoilPhysicsBridgeChecks {
  private static long checks;
  private static void check(boolean ok, String why) {
    checks++;
    if (!ok) throw new AssertionError(why);
  }
  private static void near(double a, double b, String why) {
    check(Math.abs(a - b) <= 1e-12, why + ": " + a + " != " + b);
  }
  private static void reject(Runnable r, String why) {
    boolean failed = false;
    try { r.run(); }
    catch (IllegalArgumentException | NullPointerException expected) { failed = true; }
    check(failed, why);
  }
  private static double unit(double x) { return Math.max(0, Math.min(1, x)); }

  /** Independent reference equations transcribed from Foundations Soil SoilPhysics/SoilProfile. */
  private static void reference(Profile p, Texture t, double hydration, Conditions got) {
    double holding = unit(.15 + .50 * t.silt() + .40 * t.clay() + .20 * p.organicMatter());
    double capacity = unit(.12 + .60 * holding + .12 * p.structure());
    double aeration = unit(.25 + .60 * t.sand() + .20 * p.structure()
        - .35 * p.compaction() - .20 * t.clay());
    double resistance = unit(.60 * p.compaction() + .25 * t.clay() + .15 * (1 - p.structure()));
    double drainage = unit(.55 * p.drainage() + .35 * t.sand() + .10 * p.structure()
        - .20 * t.clay() - .25 * p.compaction());
    double available = unit(hydration * (.60 + .60 * holding) - .12 * p.salinity());
    Moisture moisture = hydration >= .90 && drainage < .35 ? Moisture.WATERLOGGED
        : available < .18 ? Moisture.DRY : available < .35 ? Moisture.LIMITED
        : hydration >= .85 ? Moisture.WET : Moisture.OPTIMAL;
    double water = switch (moisture) {
      case DRY -> .65;
      case LIMITED -> .85;
      case WATERLOGGED -> .70;
      default -> 1.0;
    };
    double ph = 1.0 - Math.min(.45, Math.abs(p.pH() - 6.5) * .09);
    double access = Math.max(.35, Math.min(1.08, ph * (1 - p.compaction() * .35)
        * (.88 + p.biology() * .20) * (.88 + p.structure() * .12)));
    near(got.waterHolding(), holding, "holding");
    near(got.fieldCapacity(), capacity, "capacity");
    near(got.aeration(), aeration, "aeration");
    near(got.rootResistance(), resistance, "resistance");
    near(got.effectiveDrainage(), drainage, "drainage");
    near(got.availableWater(), available, "available water");
    check(got.moisture() == moisture, "moisture classification");
    near(got.nutrientAccess(), access, "nutrient access");
    near(got.responseModifier(), water * (1.0 - .15 * resistance), "physical response");
  }

  public static void main(String[] args) {
    Profile p = new Profile(.4, 6.5, .5, .2, .7, .6, .1);
    Texture loam = new Texture(.4, .4, .2);
    check(FoundationsSoilPhysicsBridge.SCHEMA_VERSION == 1, "schema");
    check(FoundationsSoilPhysicsBridge.evaluate(p, Optional.empty(), OptionalDouble.of(.5)).isEmpty(),
        "missing texture must not synthesize loam");
    check(FoundationsSoilPhysicsBridge.evaluate(p, Optional.of(loam), OptionalDouble.empty()).isEmpty(),
        "missing water owner reading must not synthesize hydration");
    check(FoundationsSoilPhysicsBridge.evaluate(p, Optional.empty(), OptionalDouble.empty()).isEmpty(),
        "missing sources stay missing");
    check(FoundationsSoilPhysicsBridge.evaluate(p, Optional.of(loam), OptionalDouble.of(.5))
        .orElseThrow().equals(FoundationsSoilPhysicsBridge.evaluate(p, loam, .5)), "optional parity");
    reject(() -> new Texture(0, 0, 0), "empty texture");
    reject(() -> new Texture(Double.NaN, 1, 1), "NaN texture");
    reject(() -> new Texture(Double.POSITIVE_INFINITY, 1, 1), "infinite texture");
    reject(() -> new Texture(-1, 1, 1), "negative texture");
    reject(() -> new Profile(.4, 10, .5, .2, .7, .6, .1), "pH range");
    reject(() -> new Profile(.4, 6.5, 1.1, .2, .7, .6, .1), "drainage range");
    reject(() -> new Profile(.4, 6.5, .5, .2, .7, Double.NaN, .1), "NaN biology");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, loam, -0.01), "negative hydration");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, loam, 1.01), "excess hydration");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, loam, Double.NaN), "NaN hydration");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, Optional.of(loam), OptionalDouble.of(Double.NaN)),
        "NaN optional hydration");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, (Texture) null, .5), "null texture");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(null, loam, .5), "null profile");
    reject(() -> FoundationsSoilPhysicsBridge.evaluate(p, null, OptionalDouble.empty()), "null optional");
    Texture scaled = new Texture(4, 4, 2);
    near(scaled.sand(), .4, "scaled sand");
    near(scaled.silt(), .4, "scaled silt");
    near(scaled.clay(), .2, "scaled clay");
    Texture huge = new Texture(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    near(huge.sand() + huge.silt() + huge.clay(), 1, "overflow-safe normalization");
    check(new Texture(.4, .4, .2).equals(loam), "stable normalized texture");
    reference(p, loam, .5, FoundationsSoilPhysicsBridge.evaluate(p, loam, .5));
    check(FoundationsSoilPhysicsBridge.evaluate(p, loam, 0).moisture() == Moisture.DRY,
        "dry classification");
    Profile clay = new Profile(.5, 6, .1, .6, .3, .5, .1);
    check(FoundationsSoilPhysicsBridge.evaluate(clay, new Texture(.1, .2, .7), .95)
        .moisture() == Moisture.WATERLOGGED, "waterlogged classification");

    Random rng = new Random(0xF0A1DA7102L);
    for (int i = 0; i < 50_000; i++) {
      Profile profile = new Profile(rng.nextDouble(), 3.5 + 6 * rng.nextDouble(),
          rng.nextDouble(), rng.nextDouble(), rng.nextDouble(), rng.nextDouble(), rng.nextDouble());
      Texture texture = new Texture(rng.nextDouble(), rng.nextDouble(), rng.nextDouble());
      double hydration = rng.nextDouble();
      Conditions first = FoundationsSoilPhysicsBridge.evaluate(profile, texture, hydration);
      reference(profile, texture, hydration, first);
      check(first.equals(FoundationsSoilPhysicsBridge.evaluate(profile, texture, hydration)),
          "deterministic replay");
      check(first.equals(FoundationsSoilPhysicsBridge.evaluate(profile,
          Optional.of(texture), OptionalDouble.of(hydration)).orElseThrow()), "optional replay");
    }
    System.out.println("FOUNDATIONS_SOIL_PHYSICS_BRIDGE_PASS checks=" + checks
        + " trials=50000 missingInputsFailClosed=true deterministic=true");
  }
  private FoundationsSoilPhysicsBridgeChecks() {}
}
