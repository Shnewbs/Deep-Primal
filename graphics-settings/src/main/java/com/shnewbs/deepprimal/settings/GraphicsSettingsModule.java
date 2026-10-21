package com.shnewbs.deepprimal.settings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "deepprimal_graphics", dist = Dist.CLIENT)
public final class GraphicsSettingsModule {
  public GraphicsSettingsModule(ModContainer container) {
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.CLIENT,
        ClientPerformanceConfig.SPEC,
        "deepprimal-client-performance.toml");
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.CLIENT,
        WaterVisualConfig.SPEC,
        "deepprimal-water-visuals.toml");
    container.registerConfig(
        net.neoforged.fml.config.ModConfig.Type.CLIENT, LodConfig.SPEC, "deepprimal-lod.toml");
    container.registerExtensionPoint(
        IConfigScreenFactory.class,
        (IConfigScreenFactory) (mod, parent) -> new PrimalGraphicsScreen(parent));
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
        GraphicsSettingsModule::openScreen);
  }

  private static void openScreen(net.neoforged.neoforge.client.event.ScreenEvent.Opening event) {
    if (RendererOwnership.owned()
        && event.getNewScreen()
            instanceof net.minecraft.client.gui.screens.options.VideoSettingsScreen screen) {
      var parent =
          ((com.shnewbs.deepprimal.settings.mixin.client.OptionsParentAccess) screen)
              .deepprimal$parent();
      event.setNewScreen(new PrimalGraphicsScreen(parent));
    }
  }
}
