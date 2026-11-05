package com.shnewbs.deepprimal.settings;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;

/** Backend-neutral restart preference; never initializes native graphics libraries. */
public final class RendererPreferences {
  private static volatile String startupFallbackReason = "";

  /** Set by the optional adapter without loading any client or native graphics classes. */
  public static void reportStartupFallback(String reason) {
    startupFallbackReason = java.util.Objects.requireNonNull(reason);
  }

  public static String startupFallbackReason() {
    return startupFallbackReason;
  }

  public enum Backend {
    OpenGL,
    Vulkan
  }

  private static Path file() {
    return FMLPaths.CONFIGDIR.get().resolve("deepprimal-renderer.properties");
  }

  public static Path recoveryMarker() {
    return file().resolveSibling("deepprimal-vulkan-starting");
  }

  public static Backend savedBackend() {
    var values = new Properties();
    if (Files.isRegularFile(file()))
      try (var stream = Files.newInputStream(file())) {
        values.load(stream);
      } catch (IOException error) {
        com.mojang.logging.LogUtils.getLogger().warn("Cannot read graphics preference", error);
      }
    return "Vulkan".equalsIgnoreCase(values.getProperty("backend", "OpenGL"))
        ? Backend.Vulkan
        : Backend.OpenGL;
  }

  public static Backend requestedBackend() {
    String value = System.getProperty("deepprimal.graphicsBackend");
    return value == null
        ? savedBackend()
        : "Vulkan".equalsIgnoreCase(value) ? Backend.Vulkan : Backend.OpenGL;
  }

  public static void save(Backend backend) throws IOException {
    Path target = file();
    Files.createDirectories(target.getParent());
    Path temporary = Files.createTempFile(target.getParent(), "deepprimal-renderer-", ".tmp");
    try {
      Files.writeString(temporary, "backend=" + backend + "\n");
      try {
        Files.move(
            temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
      Files.deleteIfExists(recoveryMarker());
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private RendererPreferences() {}
}
