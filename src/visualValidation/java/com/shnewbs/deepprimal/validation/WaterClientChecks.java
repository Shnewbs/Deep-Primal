package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.SnowFoliage;
import com.shnewbs.deepprimal.client.WaterColors;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import com.shnewbs.deepprimal.settings.WaterVisualSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

final class WaterClientChecks {
  static void run(Minecraft mc) {
    boolean original = WaterVisualConfig.COLORS.get();
    var screen = new WaterVisualSettingsScreen(null);
    mc.setScreen(screen);
    button(screen, "Water colors:").onPress(null);
    if (WaterVisualConfig.COLORS.get() != original)
      throw new AssertionError("Staged toggle wrote early");
    var fields =
        screen.children().stream()
            .filter(EditBox.class::isInstance)
            .map(EditBox.class::cast)
            .toList();
    fields.get(0).setValue("129");
    if (button(screen, "Apply").active) throw new AssertionError("Particle overflow accepted");
    fields.get(0).setValue("24");
    fields.get(1).setValue("65");
    if (button(screen, "Apply").active) throw new AssertionError("Wave radius overflow accepted");
    fields.get(1).setValue("64");
    if (!button(screen, "Apply").active) throw new AssertionError("Valid water settings rejected");
    screen.onClose();
    if (WaterVisualConfig.COLORS.get() != original)
      throw new AssertionError("Cancel wrote settings");
    for (var block :
        new net.minecraft.world.level.block.Block[] {
          Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.OAK_LEAVES
        }) {
      var sources = mc.getBlockColors().getTintSources(block.defaultBlockState());
      if (sources.isEmpty() || !(sources.getFirst() instanceof SnowFoliage))
        throw new AssertionError("Leaf palette was not decorated: " + block);
    }
    int previous = 0;
    for (int x = -17; x <= 17; x++) {
      int color = WaterColors.blended(mc.level, new BlockPos(x, 144, -3));
      if ((color >>> 24) != 255) throw new AssertionError("Water alpha is not opaque");
      if (x > -17)
        for (int shift : new int[] {0, 8, 16})
          if (Math.abs((color >> shift & 255) - (previous >> shift & 255)) > 18)
            throw new AssertionError("Water tint discontinuity");
      previous = color;
    }
    mc.setScreen(null);
    System.out.println(
        "WATER_CLIENT_PASS stagedToggles=true cancel=true inputBounds=true frostPalettes=3"
            + " opaqueTints=35 negativeJoinContinuity=true");
  }

  static void surface() throws Exception {
    var instance = com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE;
    var field = instance.getClass().getDeclaredField("patch");
    field.setAccessible(true);
    var patch = field.get(instance);
    if (patch == null) throw new AssertionError("Loaded ocean wave patch was not created");
    var bytes = patch.getClass().getDeclaredField("byteSize");
    bytes.setAccessible(true);
    if (bytes.getLong(patch) > 256 * 1024)
      throw new AssertionError("Ocean patch exceeds bounded budget");
    System.out.println("OCEAN_SURFACE_PASS bytes=" + bytes.getLong(patch));
  }

  private static Button button(net.minecraft.client.gui.screens.Screen screen, String prefix) {
    return screen.children().stream()
        .filter(Button.class::isInstance)
        .map(Button.class::cast)
        .filter(b -> b.getMessage().getString().startsWith(prefix))
        .findFirst()
        .orElseThrow();
  }

  private WaterClientChecks() {}
}
