package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

/** Real item atlas and entity PIP captures, including cached atlas reuse and resource reload. */
final class GuiOrientationChecks {
  private static int stage, ticks;
  private static volatile boolean prepared, reloaded, captured;

  static void tick(Minecraft mc) {
    ticks++;
    if (stage == 0) {
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend"))
          && !RenderSystem.getDevice().getBackendName().equals("Vulkan"))
        throw new AssertionError("Orientation fixture fell back from Vulkan");
      mc.options.hideGui = false;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var s = mc.getSingleplayerServer();
                for (String cmd :
                    new String[] {"gamemode survival @a", "time set noon", "weather clear"})
                  s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), cmd);
                for (var p : s.getPlayerList().getPlayers()) {
                  p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                  p.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                  Item[] items = {
                    Items.TORCH,
                    Items.DIAMOND_AXE,
                    Items.CRAFTING_TABLE,
                    Items.OAK_STAIRS,
                    Items.POPPY,
                    Items.WATER_BUCKET,
                    Items.DIAMOND_PICKAXE,
                    Items.SHIELD,
                    Items.CHEST
                  };
                  for (int i = 0; i < 36; i++)
                    p.getInventory().setItem(i, new ItemStack(items[i % items.length]));
                  p.containerMenu.broadcastChanges();
                }
                prepared = true;
              });
      stage = 1;
      ticks = 0;
    } else if (stage == 1 && prepared && ticks > 60) {
      mc.setScreen(new InventoryScreen(mc.player));
      stage = 2;
      ticks = 0;
    } else if ((stage == 2 || stage == 4) && ticks > 45 && mc.getOverlay() == null) {
      Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
      out.toFile().mkdirs();
      captured = false;
      Screenshot.grab(
          out.toFile(),
          stage == 2 ? "inventory.png" : "inventory-reloaded.png",
          mc.getMainRenderTarget(),
          1,
          m -> captured = true);
      stage++;
      ticks = 0;
    } else if (stage == 3 && captured && ticks > 10) {
      mc.reloadResourcePacks().thenRun(() -> reloaded = true);
      stage = 4;
      ticks = 0;
    } else if (stage == 5 && captured && reloaded && ticks > 20) {
      String result =
          "GUI_ORIENTATION_NATIVE_CAPTURE backend="
              + RenderSystem.getDevice().getBackendName()
              + " itemAtlas=true entityPip=true resourceReload=true views=2\n";
      try {
        Files.writeString(
            Path.of(System.getProperty("deepprimal.validationCapture"), "orientation-result.txt"),
            result);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      System.out.print(result);
      stage = 6;
      mc.setScreen(null);
      mc.disconnectWithSavingScreen();
      mc.stop();
    }
  }
}
