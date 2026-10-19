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
    private net.minecraft.client.gui.screens.Screen acceptedWarning;
    private String waitingScreen;
    private String pendingCapture;
    private long pendingAt;
    public ClientCapture() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (Boolean.getBoolean("deepprimal.validationQueueStartup")) {
            try { QueueStartupChecks.tick(mc); } catch (Exception e) { throw new RuntimeException(e); }
            return;
        }
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
        if (mc.level == null || mc.player == null) {
            if (mc.screen != null && !mc.screen.getClass().getName().equals(waitingScreen)) {
                waitingScreen = mc.screen.getClass().getName(); System.out.println("VALIDATION_WAITING_SCREEN " + waitingScreen);
            }
            if (Boolean.getBoolean("deepprimal.validationAcceptWorldWarning") && mc.screen != null
                && acceptedWarning != mc.screen
                && (mc.screen instanceof net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen
                    || mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen)) {
                for (var child : mc.screen.children()) if (child instanceof net.minecraft.client.gui.components.Button button
                    && (button.getMessage().getString().equals("Proceed") || button.getMessage().getString().equals("I know what I'm doing!"))) {
                    acceptedWarning = mc.screen; button.onPress(null); break;
                }
            }
            return;
        }
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
            if (action.equals("renderer-checks")) {
                RendererClientChecks.run(mc); Files.writeString(dir.resolve("renderer-checks.done"), "pass", StandardCharsets.UTF_8); return;
            }
            if (action.startsWith("backend-check=")) {
                String backend = com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName();
                if (!backend.equals(action.substring(14))) throw new AssertionError("Wrong backend " + backend);
                System.out.println("BACKEND_SELECTION_PASS backend="+backend); return;
            }
            if (action.equals("vulkan-checks")) { GpuBackendChecks.run(mc); return; }
            if (action.equals("scene-near")) {
                var server = mc.getSingleplayerServer();
                server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "tp @a -1221 180 243 135 5"));
                mc.setScreen(null); return;
            }
            if (action.equals("reload-check")) {
                mc.reloadResourcePacks().thenRun(() -> System.out.println("RESOURCE_RELOAD_PASS")); return;
            }
            if (action.equals("resize-check")) {
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().handle(), 1000, 650); return;
            }
            if (action.equals("scene-checks")) {
                var server = mc.getSingleplayerServer();
                server.execute(() -> {
                    var source = server.createCommandSourceStack();
                    for (String command : new String[]{"gamemode spectator @a", "time set noon", "weather clear", "tp @a -1221 440 243 135 18"})
                        server.getCommands().performPrefixedCommand(source, command);
                }); mc.setScreen(null); return;
            }
            if (action.equals("performance-checks")) { PerformanceClientChecks.run(mc); return; }
            if (action.equals("rebuild-stress")) {
                mc.options.prioritizeChunkUpdates().set(net.minecraft.client.PrioritizeChunkUpdates.NEARBY);
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.PACE_REBUILDS.set(true);
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.SYNC_REBUILDS.set(1);
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.SYNC_REBUILD_MICROS.set(250);
                mc.setScreen(null); return;
            }
            if (action.equals("player-rebuild")) {
                var view = ((com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor)mc.levelRenderer).deepprimal$viewArea();
                var center = net.minecraft.core.SectionPos.of(mc.gameRenderer.getMainCamera().position());
                for (var section : view.sections) {
                    long node = section.getSectionNode();
                    if (Math.abs(net.minecraft.core.SectionPos.x(node)-center.x())<=1
                        && Math.abs(net.minecraft.core.SectionPos.y(node)-center.y())<=1
                        && Math.abs(net.minecraft.core.SectionPos.z(node)-center.z())<=1) section.setDirty(true);
                }
                return;
            }
            if (action.equals("memory-guard-checks")) {
                int requested = mc.options.renderDistance().get();
                mc.options.renderDistance().set(96);
                int limit = com.shnewbs.deepprimal.MemorySafety.limit(96, mc.level.getSectionsCount());
                if (mc.options.renderDistance().get()!=96 || mc.options.getEffectiveRenderDistance()>limit)
                    throw new AssertionError("Memory guard did not preserve request/bound effective distance");
                mc.options.renderDistance().set(requested);
                System.out.println("MEMORY_GUARD_CHECKS_PASS requested=96 memoryLimit="+limit+" effective="+mc.options.getEffectiveRenderDistance());
                return;
            }
            if (action.equals("integrated-memory-checks")) {
                var server = mc.getSingleplayerServer();
                if (server == null) throw new AssertionError("No integrated server");
                int limit = com.shnewbs.deepprimal.MemorySafety.serverLimit(96, server);
                int view = server.getPlayerList().getViewDistance();
                int simulation = server.getPlayerList().getSimulationDistance();
                if (mc.options.renderDistance().get()!=96 || mc.options.simulationDistance().get()!=16
                    || view>limit || simulation>limit || mc.options.getEffectiveRenderDistance()>limit)
                    throw new AssertionError("Unbounded integrated distances: " + view + "/" + simulation + " limit="+limit);
                System.out.println("INTEGRATED_MEMORY_GUARD_PASS requested=96 simulationRequested=16 view="+view+" simulation="+simulation+" limit="+limit);
                return;
            }
            if (action.equals("reuse-samples")) {
                net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new com.shnewbs.deepprimal.render.network.HorizonRequest(
                    Long.MIN_VALUE, mc.level.dimension().identifier(), Math.floorDiv(mc.player.getBlockX(),512), Math.floorDiv(mc.player.getBlockZ(),512), 2)); return;
            }
            if (action.startsWith("batching=")) { com.shnewbs.deepprimal.client.ClientPerformanceConfig.BATCHED_DRAWS.set(Boolean.parseBoolean(action.substring(9))); mc.setScreen(null); return; }
            if (action.startsWith("background=")) { com.shnewbs.deepprimal.client.ClientPerformanceConfig.UNFOCUSED_FPS.set(Integer.parseInt(action.substring(11))); com.shnewbs.deepprimal.client.ClientPerformanceConfig.PAUSE_BACKGROUND.set(true); mc.setScreen(null); return; }
            if (action.startsWith("performance-page=")) {
                mc.setScreen(new com.shnewbs.deepprimal.client.HorizonSettingsScreen(null));
                for (int i = 0; i < Integer.parseInt(action.substring(17)); i++) {
                    var next = mc.screen.children().stream().filter(net.minecraft.client.gui.components.Button.class::isInstance).map(net.minecraft.client.gui.components.Button.class::cast).filter(b -> b.getMessage().getString().equals("›")).findFirst().orElseThrow(); next.onPress(null);
                }
                pendingCapture = "performance-page-" + action.substring(17); pendingAt = System.nanoTime(); return;
            }
            if (action.equals("performance")) { mc.setScreen(new com.shnewbs.deepprimal.client.HorizonSettingsScreen(null)); pendingCapture=action; pendingAt=System.nanoTime(); return; }
            if (action.startsWith("memory=")) { com.shnewbs.deepprimal.client.ClientPerformanceConfig.HORIZON_MEMORY_MIB.set(Integer.parseInt(action.substring(7))); mc.setScreen(null); return; }
            if (action.startsWith("adaptive=")) { com.shnewbs.deepprimal.client.ClientPerformanceConfig.ADAPTIVE_WORK.set(Boolean.parseBoolean(action.substring(9))); mc.setScreen(null); return; }
            if (action.startsWith("horizon=")) {
                com.shnewbs.deepprimal.client.ClientPerformanceConfig.FAR_RENDER_DISTANCE.set(Integer.parseInt(action.substring(8)));
                mc.setScreen(null); return;
            }
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
