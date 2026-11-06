package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Real Create World menu regression, including KubeJS after an unsupported saved generator. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class WorldCreationChecks {
  private int stage, ticks;
  private long started;
  private net.minecraft.client.gui.screens.Screen accepted;
  private final StringBuilder record = new StringBuilder();

  public WorldCreationChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static void press(Minecraft mc, String name) {
    for (var child : mc.screen.children())
      if (child instanceof Button b && b.getMessage().getString().equals(name)) {
        b.onPress(null);
        return;
      }
    throw new AssertionError("Missing button " + name + " on " + mc.screen.getClass());
  }

  private Path output() {
    return Path.of(System.getProperty("deepprimal.validationCapture"));
  }

  private void report(String line) throws Exception {
    record.append(line).append('\n');
    Files.createDirectories(output());
    Files.writeString(output().resolve("world-creation.txt"), record);
    System.out.println(line);
  }

  private void prepareBrokenWorld(Minecraft mc) throws Exception {
    Path world = mc.gameDirectory.toPath().resolve("saves/missing-engine");
    Path file = WorldGenSettings.TYPE.id().withSuffix(".dat").resolveAgainst(world.resolve("data"));
    require(Files.isRegularFile(file), "Disposable saved-world fixture present: " + file);
    CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.defaultQuota());
    CompoundTag overworld =
        root.getCompoundOrEmpty("data")
            .getCompoundOrEmpty("dimensions")
            .getCompoundOrEmpty("minecraft:overworld");
    overworld.putString("type", "primalengine:continental");
    var generator = overworld.getCompoundOrEmpty("generator");
    generator.putString("type", "primalengine:continental");
    generator.getCompoundOrEmpty("biome_source").putString("type", "primalengine:regional");
    NbtIo.writeCompressed(root, file);
  }

  private void openForm(Minecraft mc, String mode) throws Exception {
    long prep = System.nanoTime();
    CreateWorldScreen.openFresh(mc, () -> mc.setScreen(new TitleScreen()));
    require(mc.screen instanceof CreateWorldScreen, "Actual Create World screen opens");
    var screen = (CreateWorldScreen) mc.screen;
    require(
        screen.getUiState().getSettings().selectedDimensions().overworld()
            instanceof EngineChunkGenerator,
        "Own generator selected by native creation UI");
    if (net.neoforged.fml.ModList.get().isLoaded("kubejs")) {
      var type = Class.forName("dev.latvian.mods.kubejs.util.RegistryAccessContainer");
      var context = (RegistryAccess) type.getField("current").get(null);
      require(context.lookup(Registries.ITEM).isPresent(), "KubeJS static item registry restored");
      var ops =
          (com.mojang.serialization.DynamicOps<com.google.gson.JsonElement>)
              type.getMethod("json").invoke(context);
      require(
          ItemStack.CODEC.encodeStart(ops, Items.STONE.getDefaultInstance()).isSuccess(),
          "KubeJS registry serialization context works");
      report("KUBEJS_ITEM_LOOKUP_AND_CODEC_PASS");
    }
    report(
        "WORLD_CREATION_FORM_PASS mode=" + mode + " prepareMs=" + (System.nanoTime() - prep) / 1e6);
    var ui = screen.getUiState();
    ui.setName(System.getProperty("deepprimal.validationCreationName", "Alpha19-engine-check"));
    ui.setSeed("20261020");
    ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
    ui.setAllowCommands(true);
    ticks = 0;
    stage = 2;
  }

  private void acceptWarning(Minecraft mc) {
    if (mc.level == null
        && mc.screen != accepted
        && (mc.screen instanceof ConfirmExperimentalFeaturesScreen
            || mc.screen instanceof BackupConfirmScreen
            || mc.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen)) {
      accepted = mc.screen;
      for (var child : mc.screen.children())
        if (child instanceof Button b
            && (b.getMessage().getString().equals("Yes")
                || b.getMessage().getString().equals("Proceed")
                || b.getMessage().getString().equals("I know what I'm doing!"))) {
          b.onPress(null);
          break;
        }
    }
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("deepprimal.validationWorldCreation")) return;
    var mc = Minecraft.getInstance();
    try {
      if (stage == 9) return;
      if (stage == 8) { EngineSceneChecks.tick(Minecraft.getInstance()); return; }
      if (stage == 4 && mc.screen != null && mc.screen.getClass().getSimpleName().contains("Error"))
        throw new AssertionError(
            "Unexpected error screen during world creation: " + mc.screen.getClass());
      if (started == 0) started = System.nanoTime();
      if (System.nanoTime() - started > 240_000_000_000L)
        throw new AssertionError("World creation timed out, stage " + stage);
      if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
        stage = 1;
        String mode = System.getProperty("deepprimal.validationCreationMode", "fresh");
        if (mode.equals("failed")) {
          prepareBrokenWorld(mc);
          stage = 10;
          mc.createWorldOpenFlows()
              .openWorld("missing-engine", () -> mc.setScreen(new TitleScreen()));
          return;
        } else if (mode.equals("context")) {
          Class<?> type = Class.forName("dev.latvian.mods.kubejs.util.RegistryAccessContainer");
          Object empty =
              type.getConstructor(RegistryAccess.Frozen.class).newInstance(RegistryAccess.EMPTY);
          type.getField("current").set(null, empty);
          report("DYNAMIC_ONLY_KUBEJS_CONTEXT_SET");
        }
        openForm(mc, mode);
        return;
      }
      if (stage == 10) {
        if (mc.screen != null
            && mc.screen.getClass().getSimpleName().equals("DatapackLoadFailureScreen")) {
          report("UNSUPPORTED_EXPERIMENT_WORLD_REJECTED_PASS");
          stage = 11;
          press(mc, "Back");
          accepted = null;
          return;
        }
        acceptWarning(mc);
        return;
      }
      if (stage == 11 && mc.getOverlay() == null) {
        stage = 1;
        openForm(mc, "failed");
        return;
      }
      if (stage == 2 && ++ticks > 20) {
        Screenshot.grab(
            output().toFile(),
            "creation-form.png",
            mc.getMainRenderTarget(),
            1,
            m -> System.out.println("WORLD_CREATION_FORM_CAPTURED"));
        ticks = 0;
        stage = 3;
        return;
      }
      if (stage == 3 && ++ticks > 20) {
        stage = 4;
        ticks = 0;
        press(mc, "Create New World");
        return;
      }
      if (stage == 4 && mc.screen != accepted && ticks++ % 60 == 0)
        report("CREATION_SCREEN " + (mc.screen == null ? "null" : mc.screen.getClass().getName()));
      if (stage == 4) acceptWarning(mc);
      if (stage == 4
          && mc.level != null
          && mc.player != null
          && mc.screen == null
          && ++ticks > 80) {
        require(
            mc.getSingleplayerServer().overworld().getChunkSource().getGenerator()
                instanceof EngineChunkGenerator,
            "UI-created world uses our engine");
        var generator =
            (EngineChunkGenerator)
                mc.getSingleplayerServer().overworld().getChunkSource().getGenerator();
        require(generator.geography().revision == 7, "Fresh creation selects V7 geography");
        var level=mc.getSingleplayerServer().overworld();
        require(level.getSeed()==20261020L,"Entered seed is the saved world seed");
        var expected=new com.shnewbs.deepprimal.core.TerrainEngine(level.getSeed(),7,generator.terrainProfile().options(),generator.channelRevision());
        require(generator.geography().sample(341,-217).equals(expected.sample(341,-217)),"UI world binds the actual saved seed");
        report(
            "WORLD_CREATION_JOIN_PASS revision=7 seed=20261020 backend="
                + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName());
        StartupLifecycleChecks.observe("creation-joined", output());
        if (Boolean.getBoolean("deepprimal.validationOrganicTerrain")) {
          require(generator.channelRevision()==10, "New world selects terrain-derived channels");
          require(generator.getBiomeSource().possibleBiomes().size()==40, "Complete owned biome palette");
          require(generator.getBiomeSource().possibleBiomes().stream().allMatch(
              b->b.unwrapKey().orElseThrow().identifier().getNamespace().equals("deepprimal")),
              "All native possible biomes belong to the mod");
          require(mc.level.getBiome(mc.player.blockPosition()).unwrapKey().orElseThrow()
              .identifier().getNamespace().equals("deepprimal"), "Actual joined biome belongs to the mod");
          report("ORGANIC_NATIVE_WORLD_PASS channelRevision=10 ownedBiomes=40 seed=20261020");
          stage=8;
          return;
        }
        mc.disconnectWithSavingScreen();
        StartupLifecycleChecks.observe("creation-disconnected", output());
        report("WORLD_CREATION_PASS");
        stage = 9;
        mc.stop();
      }
    } catch (Throwable failure) {
      try {
        report("WORLD_CREATION_FAIL " + failure);
      } catch (Exception ignored) {
      }
      stage = 9;
      throw new RuntimeException(failure);
    }
  }
}


