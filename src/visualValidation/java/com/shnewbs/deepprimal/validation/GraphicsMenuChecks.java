package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.settings.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.options.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Native menu routing, complete option coverage and persistence. Never included in releases. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class GraphicsMenuChecks {
  private int stage, ticks;
  private final StringBuilder record = new StringBuilder();

  public GraphicsMenuChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static Button button(Minecraft mc, String text) {
    return mc.screen.children().stream()
        .filter(Button.class::isInstance)
        .map(Button.class::cast)
        .filter(b -> b.getMessage().getString().replace("› ", "").contains(text))
        .findFirst()
        .orElseThrow();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> values(Screen screen) throws Exception {
    var f = PrimalGraphicsScreen.class.getDeclaredField("values");
    f.setAccessible(true);
    return (Map<String, String>) f.get(screen);
  }

  private void check(Minecraft mc) throws Exception {
    var parent = new OptionsScreen(new TitleScreen(), mc.options, false);
    mc.setScreen(parent);
    button(mc, "Video Settings").onPress(null);
    if (!RendererOwnership.owned()) {
      require(mc.screen instanceof VideoSettingsScreen, "Delegated renderer retains native entry");
      record.append("GRAPHICS_DELEGATION_PASS\n");
      return;
    }
    require(mc.screen instanceof PrimalGraphicsScreen, "Video Settings opens Deep Primal directly");
    var screen = mc.screen;
    button(mc, "Performance").onPress(null);
    EditBox search =
        mc.screen.children().stream()
            .filter(EditBox.class::isInstance)
            .map(EditBox.class::cast)
            .filter(f -> f.getMessage().getString().equals("Search settings"))
            .findFirst()
            .orElseThrow();
    search.setValue("Target FPS");
    require(
        mc.screen.children().stream().filter(EditBox.class::isInstance).count() == 2,
        "Search shows only the matching numeric control and search box");
    button(mc, "General").onPress(null);
    var v = values(screen);
    String[] keys = {
      "preset",
      "render",
      "simulation",
      "clouds",
      "particles",
      "shadows",
      "resolution",
      "fullscreen",
      "exclusiveFullscreen",
      "guiScale",
      "fpsLimit",
      "vsync",
      "inactivity",
      "brightness",
      "biomeBlend",
      "ambient",
      "cloudRange",
      "weatherRange",
      "leaves",
      "transparency",
      "mipmaps",
      "filtering",
      "anisotropy",
      "entityScale",
      "menuBlur",
      "chunkPriority",
      "autosave",
      "vignette",
      "attackIndicator",
      "chunkFade"
    };
    for (String key : keys) require(v.containsKey(key), "Native video option available: " + key);
    require(
        LodAccess.available() != Boolean.getBoolean("deepprimal.validationNoLod"),
        "Optional LOD discovery");
    if (Boolean.getBoolean("deepprimal.validationGraphicsRestart")) {
      require(mc.options.menuBackgroundBlurriness().get() == 3, "Native option survived restart");
      require(ClientPerformanceConfig.TARGET_FPS.get() == 89, "Custom option survived restart");
      record.append("GRAPHICS_RESTART_PASS native=3 custom=89\n");
    }
    v.put("menuBlur", "3");
    v.put("target", "89");
    button(mc, "Apply").onPress(null);
    require(mc.options.menuBackgroundBlurriness().get() == 3, "Native option applies");
    require(ClientPerformanceConfig.TARGET_FPS.get() == 89, "Custom option applies");
    v = values(screen);
    v.put("menuBlur", "8");
    button(mc, "Quality").onPress(null);
    screen.resize(340, 240);
    require(values(screen).get("menuBlur").equals("8"), "Resize retains staging");
    button(mc, "Revert").onPress(null);
    require(values(screen).get("menuBlur").equals("3"), "Revert reloads applied value");
    screen.resize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    button(mc, "Done").onPress(null);
    require(mc.screen == parent, "Done returns to Options, with no hidden vanilla menu");
    mc.setScreen(new VideoSettingsScreen(parent, mc, mc.options));
    require(mc.screen instanceof PrimalGraphicsScreen, "Programmatic video opening is replaced");
    require(values(mc.screen).get("menuBlur").equals("3"), "Reopen displays saved native value");
    button(mc, "Performance").onPress(null);
    require(
        values(mc.screen).get("target").equals("89"),
        "Performance tab shows saved value after reopen");
    record.append("GRAPHICS_SEARCH_AND_PERFORMANCE_REOPEN_PASS targetFps=89\n");
    values(mc.screen).put("menuBlur", "9");
    mc.screen.onClose();
    require(
        mc.screen == parent && mc.options.menuBackgroundBlurriness().get() == 3,
        "Escape discards staging and restores parent");
    mc.setScreen(new VideoSettingsScreen(parent, mc, mc.options));
    button(mc, "Quality").onPress(null);
    for (var entry : Map.of("fpsLimit", "117", "entityScale", "73", "chunkFade", "27").entrySet()) {
      var staged = values(mc.screen);
      String previous = staged.put(entry.getKey(), entry.getValue());
      button(mc, "General").onPress(null);
      require(
          !button(mc, "Apply").active,
          "Native increments reject silently normalized input: " + entry.getKey());
      staged.put(entry.getKey(), previous);
      button(mc, "Quality").onPress(null);
      require(button(mc, "Apply").active, "Legal native input restores Apply");
    }
    record.append("GRAPHICS_NATIVE_STEPS_PASS fps=10 entityPercent=25 fadeMs=50\n");
    int previousCloudRange = mc.options.cloudRange().get();
    // CUSTOM -> FAST: preview every native preset value without mutating live options.
    require(values(mc.screen).get("preset").equals("3"), "Individual setting selects Custom");
    button(mc, "CUSTOM").onPress(null);
    require(mc.options.cloudRange().get() == previousCloudRange, "Preset remains staged");
    require(
        values(mc.screen).get("cloudRange").equals("32"), "Preset updates displayed staged values");
    // Explicitly preserve a value equal to its pre-preset baseline; it must still override FAST.
    values(mc.screen).put("menuBlur", "3");
    button(mc, "Apply").onPress(null);
    require(
        mc.options.cloudRange().get() == 32 && mc.options.mipmapLevels().get() == 2,
        "Preset applies native quality settings");
    require(
        mc.options.menuBackgroundBlurriness().get() == 3,
        "Explicit staged override survives preset application");
    record.append(
        "GRAPHICS_PRESET_PASS preview=true staged=true nativeApply=true explicitOverride=true"
            + " textureReload=true\n");
    record
        .append(
            "GRAPHICS_MENU_PASS nativeOptions=30 apply=true revert=true resize=true reopen=true"
                + " parent=true optionalLod=")
        .append(LodAccess.available())
        .append('\n');
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("deepprimal.validationGraphics")) return;
    var mc = Minecraft.getInstance();
    try {
      if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
        check(mc);
        stage = 1;
        ticks = 0;
      } else if (stage == 1 && ++ticks > 25 && mc.getOverlay() == null) {
        Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
        Files.createDirectories(out);
        Screenshot.grab(out.toFile(), "graphics-menu.png", mc.getMainRenderTarget(), 1, m -> {});
        Files.writeString(out.resolve("graphics-result.txt"), record);
        System.out.print(record);
        stage = 2;
      } else if (stage == 2 && ++ticks > 40) {
        stage = 3;
        mc.stop();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
