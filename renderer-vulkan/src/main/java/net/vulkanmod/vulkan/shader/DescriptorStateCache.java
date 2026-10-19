// Deep Primal Vulkan descriptor reuse. LGPL-3.0-only.
package net.vulkanmod.vulkan.shader;

import java.util.Arrays;

/**
 * Hashes accelerate lookup; complete snapshots decide equality. Retained arrays are reused each
 * frame.
 */
public final class DescriptorStateCache {
  public final long[] state;
  private final int[] slots = new int[64];
  private long[][] snapshots = new long[16][];

  public DescriptorStateCache(int width) {
    state = new long[width];
    clear();
  }

  private int slot() {
    return Arrays.hashCode(state) & 63;
  }

  public int find() {
    int index = slots[slot()];
    return index >= 0 && Arrays.equals(state, snapshots[index]) ? index : -1;
  }

  public void remember(int index) {
    if (index >= snapshots.length)
      snapshots = Arrays.copyOf(snapshots, Math.max(index + 1, snapshots.length * 2));
    if (snapshots[index] == null) snapshots[index] = new long[state.length];
    System.arraycopy(state, 0, snapshots[index], 0, state.length);
    slots[slot()] = index;
  }

  public void clear() {
    Arrays.fill(slots, -1);
  }
}
