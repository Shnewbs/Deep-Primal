package com.shnewbs.deepprimal.settings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
@Mod(value="deepprimal_graphics",dist=Dist.CLIENT)
public final class GraphicsSettingsModule {
 public GraphicsSettingsModule(ModContainer container) {
  container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,ClientPerformanceConfig.SPEC,"deepprimal-client-performance.toml");
  container.registerExtensionPoint(IConfigScreenFactory.class,(IConfigScreenFactory)(mod,parent)->new PerformanceSettingsScreen(parent));
 }
}
