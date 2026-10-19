// Deep Primal Vulkan cache implementation. LGPL-3.0-only.
package net.vulkanmod.cache;

import net.neoforged.fml.loading.FMLPaths;

public final class BackendCaches {
  private static final java.nio.file.Path ROOT =
      FMLPaths.GAMEDIR.get().resolve("cache/deepprimal/vulkan");
  public static final BinaryCache SHADERS =
      new BinaryCache(ROOT.resolve("shaders"), 2 * 1024 * 1024, 48 * 1024 * 1024L, 1024);
  public static final BinaryCache PIPELINES =
      new BinaryCache(ROOT.resolve("pipelines"), 16 * 1024 * 1024, 32 * 1024 * 1024L, 8);

  private BackendCaches() {}
}
