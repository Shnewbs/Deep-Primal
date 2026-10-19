// Deep Primal Vulkan descriptor reuse. LGPL-3.0-only.
package net.vulkanmod.vulkan.shader;

import java.nio.IntBuffer;
import java.util.Arrays;

/** A new command buffer, including a flush/restart, invalidates all remembered bindings. */
public final class DescriptorBindingState {
  private long layout, set;
  private int bindPoint = -1, count = -1;
  private int[] offsets = new int[8];

  public boolean changed(long layout, long set, int bindPoint, IntBuffer values) {
    int size = values.remaining(), start = values.position();
    boolean same =
        this.layout == layout && this.set == set && this.bindPoint == bindPoint && count == size;
    if (same) {
      for (int i = 0; i < size; i++)
        if (offsets[i] != values.get(start + i)) {
          same = false;
          break;
        }
      if (same) return false;
    }
    if (size > offsets.length) offsets = Arrays.copyOf(offsets, size);
    for (int i = 0; i < size; i++) offsets[i] = values.get(start + i);
    this.layout = layout;
    this.set = set;
    this.bindPoint = bindPoint;
    count = size;
    return true;
  }

  public void reset() {
    layout = set = 0;
    bindPoint = count = -1;
  }
}
