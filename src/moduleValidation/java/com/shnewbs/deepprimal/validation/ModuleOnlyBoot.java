package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "deepprimal_validation", dist = Dist.CLIENT)
public final class ModuleOnlyBoot {
  private int ticks;

  public ModuleOnlyBoot() {
    NeoForge.EVENT_BUS.addListener(this::tick);
  }

  private void tick(ClientTickEvent.Post event) {
    var mc = Minecraft.getInstance();
    if (!(mc.screen instanceof TitleScreen) || ++ticks < 10) return;
    if (ModList.get().isLoaded("deepprimal")
        || !ModList.get().isLoaded("deepprimal_performance")
        || !ModList.get().isLoaded("deepprimal_graphics"))
      throw new AssertionError("Module isolation failed");
    System.out.println(
        "MODULE_ONLY_BOOT_PASS backend="
            + RenderSystem.getDevice().getBackendName()
            + " worldgen=false horizon=false performance=true graphicsSettings=true");
    mc.stop();
  }
}
