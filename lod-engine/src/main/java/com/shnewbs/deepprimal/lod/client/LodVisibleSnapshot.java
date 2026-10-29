package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Render-thread-only visibility scratch. Published render-state lists are immutable
 * snapshots: the temporary buffer may be recycled on the next extract event.
 * Always call finish in a finally block so GPU mesh references cannot be retained
 * by an unsuccessful extraction. Reset drops the array across worlds.
 */
final class LodVisibleSnapshot<T> {
  // Match the transform scratch retention ceiling; a one-off giant view must not pin its array.
  private static final int MAX_RETAINED_RECORDS = 32768;
  private ArrayList<T> scratch;
  private boolean collecting;

  ArrayList<T> begin() {
    if (collecting) throw new IllegalStateException("nested LOD visibility extraction");
    if (scratch == null) scratch = new ArrayList<>();
    else scratch.clear();
    collecting = true;
    return scratch;
  }

  List<T> snapshot() {
    if (!collecting) throw new IllegalStateException("no LOD visibility extraction");
    return List.copyOf(scratch);
  }

  void finish() {
    if (scratch != null) {
      int peakRecords = scratch.size();
      scratch.clear();
      if (peakRecords > MAX_RETAINED_RECORDS) scratch = null;
    }
    collecting = false;
  }

  void reset() {
    finish();
    scratch = null;
  }

  int retainedReferences() {
    return scratch == null ? 0 : scratch.size();
  }
}
