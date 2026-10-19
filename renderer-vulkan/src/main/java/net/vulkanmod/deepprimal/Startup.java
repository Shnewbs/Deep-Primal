package net.vulkanmod.deepprimal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** No graphics class initialization during mixin selection. */
public final class Startup {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path MARKER = com.shnewbs.deepprimal.settings.RendererPreferences.recoveryMarker();
    private static final boolean REQUESTED = requested();
    private static boolean armed;
    private static boolean complete;
    private static boolean fallbackWindow;
    private static boolean requested() {
        if (com.shnewbs.deepprimal.settings.RendererPreferences.requestedBackend() != com.shnewbs.deepprimal.settings.RendererPreferences.Backend.Vulkan) return false;
        if (Files.exists(MARKER) && !Boolean.getBoolean("deepprimal.vulkan.retry")) {
            LOGGER.warn("Previous Vulkan startup did not finish; recovering with OpenGL. Select Vulkan again to retry.");
            return false;
        }
        if (!com.shnewbs.deepprimal.settings.RendererOwnership.owned()) {
            LOGGER.warn("Another graphics replacement is installed; Deep Primal Vulkan is disabled.");
            return false;
        }
        return true;
    }
    public static boolean enabled() { return REQUESTED; }
    public static void arm() throws IOException {
        Files.createDirectories(MARKER.getParent()); Files.writeString(MARKER, "Vulkan startup in progress\n"); armed = true;
    }
    public static void firstFrame() {
        if (!armed || complete || !net.minecraft.client.Minecraft.getInstance().isGameLoadFinished()) return;
        try { Files.deleteIfExists(MARKER); complete = true; LOGGER.info("DEEPPRIMAL_VULKAN_FIRST_FRAME"); }
        catch (IOException error) { LOGGER.warn("Cannot clear Vulkan recovery marker", error); }
    }
    public static void fallback() {
        fallbackWindow = true;
        if (!armed) return;
        try { Files.deleteIfExists(MARKER); } catch (IOException error) { LOGGER.warn("Cannot clear Vulkan recovery marker", error); }
    }
    public static boolean consumeWindowFallback() {
        boolean result = fallbackWindow; fallbackWindow = false; return result;
    }
    private Startup() {}
}
