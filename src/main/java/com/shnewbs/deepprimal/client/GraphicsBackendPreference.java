package com.shnewbs.deepprimal.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;

/** Restart preference only; this class never initializes native graphics libraries. */
final class GraphicsBackendPreference {
    private static java.nio.file.Path file() { return FMLPaths.CONFIGDIR.get().resolve("deepprimal-renderer.properties"); }
    static boolean vulkan() {
        var settings = new Properties();
        if (Files.isRegularFile(file())) try (var input = Files.newInputStream(file())) { settings.load(input); }
        catch (IOException error) { com.mojang.logging.LogUtils.getLogger().warn("Cannot read graphics preference", error); }
        return "Vulkan".equalsIgnoreCase(settings.getProperty("backend", "OpenGL"));
    }
    static void save(boolean vulkan) throws IOException {
        var target = file(); Files.createDirectories(target.getParent());
        var temporary = Files.createTempFile(target.getParent(), "deepprimal-renderer-", ".tmp");
        try {
            Files.writeString(temporary, "backend=" + (vulkan ? "Vulkan" : "OpenGL") + "\n");
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException error) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
            Files.deleteIfExists(target.resolveSibling("deepprimal-vulkan-starting"));
        } finally { Files.deleteIfExists(temporary); }
    }
    private GraphicsBackendPreference() {}
}
