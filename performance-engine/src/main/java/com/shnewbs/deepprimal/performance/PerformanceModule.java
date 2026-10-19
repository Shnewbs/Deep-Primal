package com.shnewbs.deepprimal.performance;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
@Mod("deepprimal_performance")
public final class PerformanceModule {
 public PerformanceModule(ModContainer container) {
  container.registerConfig(ModConfig.Type.COMMON,PerformanceConfig.SPEC,"deepprimal-performance.toml");
  NeoForge.EVENT_BUS.register(new DistanceRuntime());
 }
}
