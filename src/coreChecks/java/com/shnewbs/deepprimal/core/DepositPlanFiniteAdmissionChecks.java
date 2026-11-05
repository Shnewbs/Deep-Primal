package com.shnewbs.deepprimal.core;

import java.util.List;
import java.util.Set;

/** Fail-closed deposit body elevation admission without changing finite V1-V7 output. */
public final class DepositPlanFiniteAdmissionChecks {
  private static long assertions;
  private static void need(boolean ok, String why) {
    assertions++;
    if (!ok) throw new AssertionError(why + " assertion=" + assertions);
  }
  private static final Geology.Column ROCK = new Geology.Column(
      0, 0, 0, 0, 2, .5, 0, 0, 0, 0, 3);

  private static DepositPlan plan(TerrainNoise noise, DepositPlan.Shape shape,
      DepositPlan.Reference reference) {
    return new DepositPlan(noise, List.of(new DepositPlan.Definition(
        "test:finite_body", shape, reference, 32, 1, 1, 5, 5, 72, 16, 72,
        Set.of(Geology.Rock.values()), List.of(new DepositPlan.Content(7, 1)))));
  }

  public static void main(String[] args) {
    long digest = 0xcbf29ce484222325L;
    long finiteCases = 0;
    var noise = new TerrainNoise(0x166F1A17L);
    for (var shape : DepositPlan.Shape.values()) {
      for (var reference : DepositPlan.Reference.values()) {
        var plan = plan(noise, shape, reference);
        for (int ox = -48; ox <= 48; ox += 16) {
          for (int oz = -48; oz <= 48; oz += 16) {
            var chunk = plan.chunk(ox, oz, (x, z) -> 250 + Math.sin(x * .01) * 12);
            need(chunk.candidates() > 0, "finite candidates must exist");
            digest = (digest ^ chunk.candidates()) * 0x100000001b3L;
            for (int dx = 0; dx < 16; dx += 3) {
              for (int dz = 0; dz < 16; dz += 3) {
                var col = chunk.column(ox + dx, oz + dz, ROCK, 250);
                for (int y = -16; y <= 256; y += 8) {
                  int actual = col.at(y);
                  need(actual == -1 || actual == 7, "finite deposit material");
                  digest = (digest ^ (actual + 2)) * 0x100000001b3L;
                  finiteCases++;
                }
              }
            }
          }
        }
      }
    }
    need(digest == 0x869f403583530ee1L,
        "finite deposit geometry and contents must retain original output");
    System.out.println("DEPOSIT_FINITE_PARITY hash=" + Long.toUnsignedString(digest, 16)
        + " finiteCases=" + finiteCases);
    // A nonfinite depth callback previously generated NaN-centered bodies that
    // cast to y=0 and bypassed every distance/grade rejection through NaN.
    long invalidCases = 0;
    for (var shape : DepositPlan.Shape.values()) {
      var depth = plan(noise, shape, DepositPlan.Reference.DEPTH);
      for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
          Double.NEGATIVE_INFINITY}) {
        for (int ox = -48; ox <= 48; ox += 16) {
          for (int oz = -48; oz <= 48; oz += 16) {
            var chunk = depth.chunk(ox, oz, (x, z) -> bad);
            need(chunk.candidates() == 0, "nonfinite depth must not compile bodies");
            for (int dx = 0; dx < 16; dx += 5) {
              for (int dz = 0; dz < 16; dz += 5) {
                need(chunk.at(ox + dx, 0, oz + dz, ROCK, 250) == -1,
                    "nonfinite depth must not mint ore at y=0");
                invalidCases++;
              }
            }
          }
        }
      }
    }
    // Absolute and geological references do not depend on terrain height and
    // must retain their candidates when the unused callback is malformed.
    for (var reference : new DepositPlan.Reference[] {
        DepositPlan.Reference.ABSOLUTE, DepositPlan.Reference.GEOLOGICAL}) {
      var plan = plan(noise, DepositPlan.Shape.VEIN, reference);
      int baseline = plan.chunk(0, 0, (x, z) -> 250).candidates();
      for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
          Double.NEGATIVE_INFINITY})
        need(plan.chunk(0, 0, (x, z) -> bad).candidates() == baseline,
            "unused surface callback must not alter " + reference);
    }
    System.out.println("DEPOSIT_FINITE_ADMISSION_PASS assertions=" + assertions
        + " invalidSamples=" + invalidCases + " finiteCases=" + finiteCases);
  }
  private DepositPlanFiniteAdmissionChecks() {}
}
