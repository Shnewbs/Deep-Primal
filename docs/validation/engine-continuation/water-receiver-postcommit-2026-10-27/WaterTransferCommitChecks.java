package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.engine.WaterTransferCommit.Outcome;
import java.util.Random;

/** Dependency-free test of the production postwrite decision policy. */
public final class WaterTransferCommitChecks {
  private static long checks;
  private static void eq(Object actual, Object wanted, String label) {
    checks++;
    if (!wanted.equals(actual))
      throw new AssertionError(label + " actual=" + actual + " wanted=" + wanted);
  }
  private static void yes(boolean condition, String label) {
    checks++;
    if (!condition) throw new AssertionError(label);
  }
  private static Outcome reference(boolean wrote, boolean source, boolean old, boolean applied) {
    if (!source || (!old && !applied)) return Outcome.LOST_OWNERSHIP;
    if (wrote && applied) return Outcome.COMMIT;
    if (old) return Outcome.ROLLBACK_SOURCE;
    return Outcome.ROLLBACK_BOTH;
  }
  private static void scenario(int mutation, boolean wrote) {
    int source = 12, receiver = wrote ? 7 : 3;
    boolean loadedSource = true, loadedReceiver = true;
    switch (mutation) {
      case 0 -> { }                       // normal
      case 1 -> receiver = 3;              // callback reverted receiver
      case 2 -> receiver = 15;             // callback replaced receiver
      case 3 -> source = 9;                // callback replaced source
      case 4 -> loadedReceiver = false;    // unload receiver
      case 5 -> loadedSource = false;      // unload source
      case 6 -> { if (!wrote) receiver = 7; } // false setBlock after applying
      case 7 -> { if (wrote) receiver = 15; }
      default -> throw new AssertionError();
    }
    boolean ownA = loadedSource && source == 12;
    boolean beforeB = loadedReceiver && receiver == 3;
    boolean ownB = loadedReceiver && receiver == 7;
    Outcome result = WaterTransferCommit.afterReceiverWrite(wrote, ownA, beforeB, ownB);
    eq(result, reference(wrote, ownA, beforeB, ownB), "callback "+mutation+"/"+wrote);
    if (result == Outcome.COMMIT) {
      long waterA = 16 * 256L, waterB = 3 * 256L;
      long saltA = 240, saltB = 13, sedimentA = 120, sedimentB = 7;
      long movedSalt = saltA * 4 / 16, movedSediment = sedimentA * 4 / 16;
      yes(waterA - 4*256L + waterB + 4*256L == waterA + waterB, "water quantity");
      yes(saltA - movedSalt + saltB + movedSalt == saltA + saltB, "salt quantity");
      yes(sedimentA - movedSediment + sedimentB + movedSediment == sedimentA + sedimentB,
          "sediment quantity");
      yes(wrote && ownA && ownB, "success without two owners");
    } else if (result == Outcome.ROLLBACK_BOTH || result == Outcome.ROLLBACK_SOURCE) {
      yes(ownA && (beforeB || ownB), "rollback without authoritative owners");
      if (result == Outcome.ROLLBACK_BOTH) {
        yes(ownB, "receiver rollback without owner");
        receiver = 3;
      }
      source = 16;
      yes(source + receiver == 19, "rollback created or lost native units");
    } else {
      yes(!ownA || (!beforeB && !ownB), "ownership loss not fail-closed");
    }
  }
  public static void main(String[] args) {
    for (int bits = 0; bits < 16; bits++) {
      boolean wrote = (bits & 1) != 0, source = (bits & 2) != 0;
      boolean old = (bits & 4) != 0, applied = (bits & 8) != 0;
      eq(WaterTransferCommit.afterReceiverWrite(wrote,source,old,applied),
          reference(wrote,source,old,applied), "truth table "+bits);
    }
    eq(WaterTransferCommit.afterReceiverWrite(true,true,false,true),
        Outcome.COMMIT, "normal");
    eq(WaterTransferCommit.afterReceiverWrite(true,true,true,false),
        Outcome.ROLLBACK_SOURCE, "receiver reverted by callback");
    eq(WaterTransferCommit.afterReceiverWrite(false,true,false,true),
        Outcome.ROLLBACK_BOTH, "false receiver return after applying");
    eq(WaterTransferCommit.afterReceiverWrite(true,true,false,false),
        Outcome.LOST_OWNERSHIP, "foreign receiver");
    eq(WaterTransferCommit.afterReceiverWrite(true,false,false,true),
        Outcome.LOST_OWNERSHIP, "foreign source");
    for (int m=0;m<8;m++) for (boolean wrote : new boolean[]{false,true}) scenario(m,wrote);
    Random rnd = new Random(0x71b7c0deL);
    for (int i=0;i<200_000;i++) {
      boolean wrote=rnd.nextBoolean(), source=rnd.nextBoolean();
      boolean old=rnd.nextBoolean(), applied=rnd.nextBoolean();
      eq(WaterTransferCommit.afterReceiverWrite(wrote,source,old,applied),
          reference(wrote,source,old,applied), "random "+i);
    }
    System.out.println("WATER_POSTWRITE_POLICY_PASS checks="+checks+
        " scenarios=16 randomized=200000");
  }
}
