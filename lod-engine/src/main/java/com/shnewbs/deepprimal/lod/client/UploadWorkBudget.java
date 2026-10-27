package com.shnewbs.deepprimal.lod.client;

/**
 * Indivisible GPU-mesh upload admission. A pending part cannot be split across frames:
 * admit one part even if it exceeds the soft per-frame byte/time budget, then apply
 * both budgets to any additional parts. The caller must first enforce the separate
 * hard resident-memory and session/revision limits on the complete pending page.
 *
 * <p>This only governs whether a GPU upload may begin. It neither queues nor
 * owns native buffers, and it never relaxes the retained GPU-memory ceiling.
 */
final class UploadWorkBudget {
  private UploadWorkBudget() {}

  static boolean mayStart(int uploadedParts, int maxParts, long usedBytes,
      long nextBytes, long byteBudget, boolean deadlineExpired) {
    if (uploadedParts < 0 || maxParts < 1 || usedBytes < 0 || nextBytes < 0 || byteBudget < 0)
      throw new IllegalArgumentException("invalid upload work budget");
    if (uploadedParts >= maxParts) return false;
    // Progress guarantee for an indivisible part. Subsequent parts wait for a new frame.
    if (uploadedParts == 0) return true;
    // Subtraction avoids overflow in (usedBytes + nextBytes) for large inputs.
    return !deadlineExpired && usedBytes <= byteBudget
        && nextBytes <= byteBudget - usedBytes;
  }

  static boolean oversizedFirst(int uploadedParts, long nextBytes, long byteBudget) {
    return uploadedParts == 0 && nextBytes > byteBudget;
  }
}
