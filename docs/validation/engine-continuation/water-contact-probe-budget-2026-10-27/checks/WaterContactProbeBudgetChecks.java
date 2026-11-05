package com.shnewbs.deepprimal.core;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
public final class WaterContactProbeBudgetChecks {
  private static long cases;
  private static void ok(boolean good, String reason) { cases++; if (!good) throw new AssertionError(reason); }
  private static void checkRandom() {
    var rng = new Random(20261027);
    for (int limit : new int[] {1, 2, 7, 31, 128, 2048}) {
      var budget = new WaterContactBudget(limit);
      for (int tick = 0; tick < 200; tick++) {
        budget.beginTick(tick);
        var seen = new HashSet<String>();
        for (int n = 0; n < limit * 2; n++) {
          long a = rng.nextLong(), b = rng.nextLong();
          if (n % 7 == 0) { a = -1; b = Long.MIN_VALUE; }
          String key = Math.min(a,b)+":"+Math.max(a,b);
          boolean seenBefore = seen.contains(key);
          int status = budget.contactStatus(a,b);
          if (status == WaterContactBudget.FIRST) {
            ok(!seenBefore && seen.size()<limit,"duplicate or over-cap insertion");
            seen.add(key);
          } else if (status == WaterContactBudget.DUPLICATE) {
            ok(seenBefore,"false duplicate");
          } else {
            ok(status == WaterContactBudget.DEFER,"invalid status");
          }
          ok(budget.probes() <= budget.maxProbes(),"unbounded probing");
        }
      }
    }
  }
  private static void collisions() throws Exception {
    var budget = new WaterContactBudget(128);
    var mix=WaterContactBudget.class.getDeclaredMethod("mix", long.class);
    mix.setAccessible(true);
    var collisions = new long[128]; int found=0;
    for(long val=1;found<collisions.length;val++) {
      long k=(long)mix.invoke(null,val);
      long hash=(long)mix.invoke(null,Long.rotateLeft(k,29));
      if((hash&255)==255)collisions[found++]=val;
    }
    budget.beginTick(0);
    int accepted=0, deferred=0;
    for(long b:collisions) {
      int s=budget.contactStatus(0,b);
      if(s==WaterContactBudget.FIRST)accepted++;
      else if(s==WaterContactBudget.DEFER)deferred++;
      else throw new AssertionError("unexpected duplicate");
    }
    ok(accepted>0&&deferred>0,"adversarial chain was not deferred");
    ok(budget.probes()==budget.maxProbes(),"all collision probes accounted");
    long formerlyDeferred=collisions[accepted];
    budget.beginTick(1);
    ok(budget.contactStatus(0,formerlyDeferred)==WaterContactBudget.FIRST,"deferred retry next tick");
    ok(budget.visits()==0&&budget.probes()>0,"new epoch resets admission");
    budget.beginTick(1);
    ok(budget.contactStatus(formerlyDeferred,0)==WaterContactBudget.DUPLICATE,"unordered pair identity");
    budget.beginTick(Long.MIN_VALUE);
    ok(budget.contactStatus(0,formerlyDeferred)==WaterContactBudget.FIRST,"backward signed clock resets");
    var gen=WaterContactBudget.class.getDeclaredField("generation");gen.setAccessible(true);
    gen.setInt(budget,-1);budget.beginTick(Long.MAX_VALUE);
    ok(gen.getInt(budget)==1,"stamp zero never used");
    ok(budget.contactStatus(0,formerlyDeferred)==WaterContactBudget.FIRST,"stamp wrap forgets old keys");
  }
  public static void main(String[] args) throws Exception {checkRandom();collisions();System.out.println("CONTACT_PROBE_BUDGET_PASS cases="+cases);}
}
