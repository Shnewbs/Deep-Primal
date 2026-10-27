package com.shnewbs.deepprimal.engine;

/**
 * Pure decision boundary for a two-cell native finite-water write. Calls that
 * read authoritative loaded cells and execute rollback stay in WaterRuntime.
 * A lost owner is an error, never permission to restore a player's newer edit.
 */
public final class WaterTransferCommit {
  private WaterTransferCommit() {}

  public enum Outcome { COMMIT, ROLLBACK_SOURCE, ROLLBACK_BOTH, LOST_OWNERSHIP }

  /**
   * Classify the *post-receiver-write* state. "receiverBefore" means its
   * original value is still present; "receiverApplied" means we still own the
   * new value. Those predicates must be probed from loaded native cells.
   */
  public static Outcome afterReceiverWrite(boolean wroteReceiver,
      boolean sourceOwned, boolean receiverBefore, boolean receiverApplied) {
    if (!sourceOwned || (!receiverBefore && !receiverApplied))
      return Outcome.LOST_OWNERSHIP;
    if (wroteReceiver && receiverApplied) return Outcome.COMMIT;
    if (receiverBefore) return Outcome.ROLLBACK_SOURCE;
    return Outcome.ROLLBACK_BOTH;
  }
}
