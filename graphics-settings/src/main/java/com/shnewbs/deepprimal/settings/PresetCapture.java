package com.shnewbs.deepprimal.settings;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

/** Preview native preset choices in our staged model without changing live options. */
public final class PresetCapture {
  private static final ThreadLocal<Map<OptionInstance<?>, Object>> PLAN = new ThreadLocal<>();

  public static Map<OptionInstance<?>, Object> plan(GraphicsPreset preset, Minecraft minecraft) {
    if (PLAN.get() != null) throw new IllegalStateException("Nested graphics preset preview");
    var result = new IdentityHashMap<OptionInstance<?>, Object>();
    PLAN.set(result);
    try {
      preset.apply(minecraft);
      return result;
    } finally {
      PLAN.remove();
    }
  }

  public static boolean capture(OptionInstance<?> option, Object value) {
    var plan = PLAN.get();
    if (plan == null) return false;
    plan.put(option, value);
    return true;
  }

  private PresetCapture() {}
}
