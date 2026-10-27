package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.UpscalingPolicy;
import com.shnewbs.deepprimal.settings.PrimalGraphicsScreen;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.UpscalingConfig;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Real menu/config regression checks; included only in the visual-validation source set. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class UpscalingMenuChecks {
  private boolean finished;
  private long began;

  public UpscalingMenuChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static Button button(Minecraft mc, String label) {
    return mc.screen.children().stream()
        .filter(Button.class::isInstance)
        .map(Button.class::cast)
        .filter(b -> b.getMessage().getString().replace("› ", "").equals(label))
        .reduce((first, last) -> last)
        .orElseThrow(() -> new AssertionError("Missing button: " + label));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> values(Screen screen) throws ReflectiveOperationException {
    var field = PrimalGraphicsScreen.class.getDeclaredField("values");
    field.setAccessible(true);
    return (Map<String, String>) field.get(screen);
  }

  private static String text(Screen screen, String method) throws ReflectiveOperationException {
    Method accessor = PrimalGraphicsScreen.class.getDeclaredMethod(method);
    accessor.setAccessible(true);
    return (String) accessor.invoke(screen);
  }

  private static boolean enabledField(Minecraft mc, String label) {
    return mc.screen.children().stream()
        .filter(EditBox.class::isInstance)
        .map(EditBox.class::cast)
        .filter(f -> f.getMessage().getString().equals(label))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing field: " + label))
        .active;
  }

  private static void verify(Minecraft mc) throws Exception {
    var oldMode = UpscalingConfig.MODE.get();
    var oldQuality = UpscalingConfig.QUALITY.get();
    double oldScale = UpscalingConfig.RENDER_SCALE.get();
    double oldSharpness = UpscalingConfig.SHARPNESS.get();
    Screen parent = mc.screen;
    try {
      UpscalingConfig.MODE.set(UpscalingPolicy.Mode.NONE);
      UpscalingConfig.QUALITY.set(UpscalingPolicy.Quality.QUALITY);
      UpscalingConfig.RENDER_SCALE.set(0.75);
      UpscalingConfig.SHARPNESS.set(0.4);
      UpscalingConfig.SPEC.save();
      UpscalingConfig.applyChanges();
      mc.setScreen(new PrimalGraphicsScreen(parent));
      var screen = mc.screen;
      screen.resize(900, 480);
      button(mc, "Upscaling").onPress(null);
      for (String key : new String[] {
        "upscaler", "upscalingQuality", "upscalingScale", "upscalingSharpness",
        "upscalingFrameGeneration"
      }) require(values(screen).containsKey(key), "Upscaling control exists: " + key);
      require(!button(mc, "Quality").active, "Native disables irrelevant quality");
      require(!button(mc, "Unavailable").active, "Frame generation cannot be enabled");
      require(!enabledField(mc, "Custom render scale (%)"), "Native disables custom scale");
      button(mc, "Native").onPress(null);
      require(enabledField(mc, "Custom render scale (%)"), "Spatial enables custom scale");
      button(mc, "Spatial").onPress(null);
      require(enabledField(mc, "FSR sharpness (%)"), "FSR enables sharpening");
      require(!enabledField(mc, "Custom render scale (%)"), "FSR preset owns scale");
      button(mc, "FSR 1 (Spatial)").onPress(null);
      require(!enabledField(mc, "FSR sharpness (%)"), "DLSS does not expose FSR sharpening");
      require(UpscalingConfig.MODE.get() == UpscalingPolicy.Mode.NONE, "Method remains staged");
      require(text(screen, "upscalingStatus").contains("Staged: NVIDIA DLSS"),
          "Staged and active methods are distinguished");
      require(!text(screen, "upscalingReason").isBlank(), "Unavailable DLSS has a visible reason");
      var staged = values(screen);
      staged.put("upscalingQuality", Integer.toString(UpscalingPolicy.Quality.BALANCED.ordinal()));
      staged.put("upscalingScale", "63");
      staged.put("upscalingSharpness", "37");
      button(mc, "Graphics API").onPress(null);
      screen.resize(340, 240);
      button(mc, "Upscaling").onPress(null);
      require(values(screen).get("upscalingSharpness").equals("37"),
          "Tab change and resize preserve unsaved settings");
      Button tab = button(mc, "Upscaling");
      require(tab.getY() + tab.getHeight() < button(mc, "Apply").getY(),
          "Seventh tab fits above Apply at small menu size");
      button(mc, "Apply").onPress(null);
      require(UpscalingConfig.MODE.get() == UpscalingPolicy.Mode.DLSS, "DLSS preference saves");
      require(UpscalingConfig.QUALITY.get() == UpscalingPolicy.Quality.BALANCED, "Quality saves");
      require(Math.abs(UpscalingConfig.RENDER_SCALE.get() - 0.63) < 1e-9, "Custom scale saves");
      require(Math.abs(UpscalingConfig.SHARPNESS.get() - 0.37) < 1e-9, "Sharpness saves");
      require(UpscalingConfig.getActiveMode() != UpscalingPolicy.Mode.DLSS,
          "Missing temporal provider cannot claim active DLSS");
      require(UpscalingConfig.activeScale() == 1.0, "Unavailable DLSS retains native resolution");
      String persisted = Files.readString(FMLPaths.CONFIGDIR.get().resolve("deepprimal/graphics/upscaling.toml"));
      require(persisted.contains("DLSS") && persisted.contains("BALANCED")
              && persisted.contains("0.63") && persisted.contains("0.37"),
          "Apply persists all controls to the registered config file");
      values(screen).put("upscaler", Integer.toString(UpscalingPolicy.Mode.FSR.ordinal()));
      values(screen).put("upscalingScale", "92");
      button(mc, "Revert").onPress(null);
      require(values(screen).get("upscaler").equals(Integer.toString(UpscalingPolicy.Mode.DLSS.ordinal()))
              && values(screen).get("upscalingScale").equals("63"),
          "Revert reloads applied upscaling preferences");
      values(screen).put("upscalingSharpness", "99");
      screen.onClose();
      require(mc.screen == parent, "Closing returns to parent");
      mc.setScreen(new PrimalGraphicsScreen(parent));
      button(mc, "Upscaling").onPress(null);
      require(values(mc.screen).get("upscalingSharpness").equals("37"),
          "Closing discards unapplied changes and reopening uses persisted preferences");
      require(text(mc.screen, "upscalingStatus").startsWith("Saved: NVIDIA DLSS"),
          "Reopening reports saved preference separately from active fallback");
    } finally {
      UpscalingConfig.MODE.set(oldMode);
      UpscalingConfig.QUALITY.set(oldQuality);
      UpscalingConfig.RENDER_SCALE.set(oldScale);
      UpscalingConfig.SHARPNESS.set(oldSharpness);
      UpscalingConfig.SPEC.save();
      UpscalingConfig.applyChanges();
      mc.setScreen(parent);
    }
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (finished || !Boolean.getBoolean("deepprimal.validationUpscaling")) return;
    var mc = Minecraft.getInstance();
    if (began == 0) began = System.nanoTime();
    if (System.nanoTime() - began > 120_000_000_000L)
      throw new AssertionError("Upscaling menu checks timed out");
    if (mc.getOverlay() != null) return;
    if (mc.screen instanceof AccessibilityOnboardingScreen) {
      mc.screen.onClose();
      return;
    }
    if (!(mc.screen instanceof TitleScreen)) return;
    try {
      require(RendererOwnership.owned(), "Upscaling test requires Deep Primal renderer ownership");
      verify(mc);
      String result = "UPSCALING_MENU_PASS methods=4 staged=true apply=true revert=true persistence=true"
          + " resize=true nativeFallback=true frameGenerationUnavailable=true\n";
      String output = System.getProperty("deepprimal.validationCapture");
      if (output != null) {
        Path directory = Path.of(output);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("upscaling-menu-result.txt"), result);
      }
      System.out.print(result);
      finished = true;
      mc.stop();
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }
}
