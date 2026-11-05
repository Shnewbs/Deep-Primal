package com.shnewbs.deepprimal.performance;

import com.shnewbs.deepprimal.core.MemoryDistancePolicy;
import net.minecraft.server.MinecraftServer;

/** Shared client/server column admission. Requested options remain available for review. */
public final class MemorySafety {
  public static boolean enabled() {
    return !PerformanceConfig.SPEC.isLoaded() || PerformanceConfig.MEMORY_GUARD.get();
  }

  public static int limit(int requested, int sections) {
    return enabled()
        ? Math.min(
            requested, MemoryDistancePolicy.renderLimit(Runtime.getRuntime().maxMemory(), sections))
        : requested;
  }

  public static int serverLimit(int requested, MinecraftServer server) {
    int sections = 0;
    for (var level : server.getAllLevels()) sections = Math.max(sections, level.getSectionsCount());
    return limit(requested, sections == 0 ? 64 : sections);
  }

  private MemorySafety() {}
}
