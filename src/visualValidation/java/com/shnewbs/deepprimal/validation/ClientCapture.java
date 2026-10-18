package com.shnewbs.deepprimal.validation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in development source set. Excluded from the distributed mod JAR. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class ClientCapture {
    private long entered;
    private boolean taken;
    private boolean checked;
    private long checkedAt;
    private String pendingCapture;
    private long pendingAt;
    public ClientCapture() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (Boolean.getBoolean("deepprimal.validationTerrain")) { terrain(mc); return; }
        if (mc.level == null || mc.player == null || taken) return;
        mc.options.hideGui = !Boolean.getBoolean("deepprimal.validationDistances");
        if (entered == 0) entered = System.nanoTime();
        if (System.nanoTime() - entered < 45_000_000_000L) return;
        if (Boolean.getBoolean("deepprimal.validationDistances") && !checked) {
            var center = mc.player.chunkPosition();
            if (!mc.level.getChunkSource().hasChunk(center.x() + 33, center.z())) {
                if (System.nanoTime() - entered > 150_000_000_000L) throw new AssertionError("Chunk at radius 33 was not delivered");
                return;
            }
            try { DistanceClientChecks.run(mc); }
            catch (Exception exception) { throw new RuntimeException(exception); }
            checked = true; checkedAt = System.nanoTime(); return;
        }
        if (Boolean.getBoolean("deepprimal.validationDistances") && System.nanoTime() - checkedAt < 5_000_000_000L) return;
        taken = true;
        File output = new File(System.getProperty("deepprimal.validationCapture"));
        output.mkdirs();
        Screenshot.grab(output, "terrain.png", mc.getMainRenderTarget(), 1, message -> {
            System.out.println("DEEPPRIMAL_CAPTURE_SAVED " + message.getString());
            mc.execute(mc::stop);
        });
    }

    private void terrain(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        Path dir = Path.of(System.getProperty("deepprimal.validationCapture"));
        if (pendingCapture != null && System.nanoTime() - pendingAt > 1_000_000_000L) {
            String name = pendingCapture; pendingCapture = null;
            Screenshot.grab(dir.toFile(), name + ".png", mc.getMainRenderTarget(), 1,
                message -> System.out.println("DEEPPRIMAL_CAPTURE_SAVED " + name));
        }
        Path request = dir.resolve("capture-request.txt");
        if (!Files.exists(request)) return;
        try {
            String action = Files.readString(request).trim(); Files.delete(request);
            Files.createDirectories(dir);
            if (action.equals("stop")) { mc.stop(); return; }
            if (action.equals("checks")) {
                TerrainClientChecks.run(mc); Files.writeString(dir.resolve("checks.done"), "pass", StandardCharsets.UTF_8); return;
            }
            if (action.startsWith("volume=")) {
                String[] values = action.substring(7).split(",");
                mc.options.renderDistance().set(Integer.parseInt(values[0]));
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(Integer.parseInt(values[1]));
                mc.levelRenderer.allChanged(); mc.setScreen(null); return;
            }
            if (action.equals("controls")) mc.setScreen(new com.shnewbs.deepprimal.client.DistanceScreen(null));
            else if (action.equals("world-creation")) {
                net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(mc, () -> {}, (a, b, c, d, e) -> false);
                var field = mc.screen.getClass().getDeclaredField("uiState"); field.setAccessible(true);
                var ui = (net.minecraft.client.gui.screens.worldselection.WorldCreationUiState)field.get(mc.screen);
                if (!ui.getWorldType().describePreset().getString().equals("Deep Primal")) throw new AssertionError("Wrong default world label");
                if (ui.getNormalPresetList().stream().anyMatch(p -> p.describePreset().getString().contains("legacy")))
                    throw new AssertionError("Comparison presets are still selectable");
                int height = ui.getSettings().selectedDimensions().dimensions()
                    .get(net.minecraft.world.level.dimension.LevelStem.OVERWORLD).type().value().height();
                int expected = com.shnewbs.deepprimal.WorldgenConfig.EXTRA_DEPTH.get() ? 1024 : 768;
                if (height != expected) throw new AssertionError("Wrong configured height " + height);
                System.out.println("WORLD_CREATION_CHECKS_PASS height=" + height + " label=Deep Primal");
                var tabs = mc.screen.getClass().getDeclaredField("tabNavigationBar"); tabs.setAccessible(true);
                ((net.minecraft.client.gui.components.tabs.TabNavigationBar)tabs.get(mc.screen)).selectTab(1, false);
            }
            else if (action.startsWith("vertical=")) {
                int radius = Integer.parseInt(action.substring(9));
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.set(radius);
                mc.levelRenderer.allChanged(); mc.setScreen(null); return;
            } else { mc.setScreen(null); mc.options.hideGui = true; }
            pendingCapture = action; pendingAt = System.nanoTime();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
