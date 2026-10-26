package com.shnewbs.deepprimal;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.shnewbs.deepprimal.worldgen.ColumnSurface;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(DeepPrimal.ID)
public final class DeepPrimal {
  public static final String ID = "deepprimal";
  public static final Logger LOGGER = LogUtils.getLogger();
  public static final DeferredRegister<TicketType> TICKETS =
      DeferredRegister.create(Registries.TICKET_TYPE, ID);
  public static final DeferredHolder<TicketType, TicketType> PROBE_TICKET =
      TICKETS.register(
          "worldgen_probe", () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING));
  public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITIES =
      DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, ID);

  public DeepPrimal(IEventBus modBus, ModContainer container) {
    com.shnewbs.deepprimal.diagnostics.LocalDashboard.metadata(
        container.getModInfo().getVersion().toString(),
        net.neoforged.fml.ModList.get().getMods().stream()
            .map(m -> java.util.Map.of("id", m.getModId(), "version", m.getVersion().toString()))
            .toList());
    container.registerConfig(
        ModConfig.Type.STARTUP,
        com.shnewbs.deepprimal.diagnostics.DiagnosticsConfig.SPEC,
        "deepprimal-diagnostics.toml");
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.diagnostics.ServerTelemetry());
    container.registerConfig(
        ModConfig.Type.STARTUP, WorldgenConfig.SPEC, "deepprimal-worldgen.toml");
    container.registerConfig(
        ModConfig.Type.SERVER,
        com.shnewbs.deepprimal.water.RiverConfig.SPEC,
        "deepprimal-rivers.toml");
    com.shnewbs.deepprimal.water.RiverWater.BLOCKS.register(modBus);
    com.shnewbs.deepprimal.water.RiverWater.ITEMS.register(modBus);
    com.shnewbs.deepprimal.water.WeatherHydrology.ATTACHMENTS.register(modBus);
    modBus.addListener(com.shnewbs.deepprimal.water.WaterPort::register);
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.water.SwimmingEffort());
    modBus.addListener(WorldgenConfig::packs);
    modBus.addListener(com.shnewbs.deepprimal.render.HorizonService::register);
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.render.HorizonService());
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.water.WaterEnvironment());
    container.registerConfig(ModConfig.Type.SERVER,
        com.shnewbs.deepprimal.engine.HydrologyConfig.SPEC, "deepprimal-hydrology.toml");
    container.registerConfig(ModConfig.Type.STARTUP,
        com.shnewbs.deepprimal.engine.TerrainConfig.SPEC, "deepprimal/worldgen/terrain.toml");
    container.registerConfig(ModConfig.Type.SERVER, com.shnewbs.deepprimal.engine.StabilityRuntime.SPEC, "deepprimal/hazards/stability.toml");
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.engine.StabilityRuntime());
    com.shnewbs.deepprimal.engine.EngineRegistry.register(modBus);
    com.shnewbs.deepprimal.engine.GroundMaterials.register(modBus);
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.engine.HydrologyRuntime());
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.engine.WaterBorderWake());
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.engine.WaterRuntime.Events());
    NeoForge.EVENT_BUS.register(new com.shnewbs.deepprimal.engine.WaterInteriorWake());
    TICKETS.register(modBus);
    DENSITIES.register("column_surface", () -> ColumnSurface.MAP_CODEC);
    DENSITIES.register(
        "waterway_height", () -> com.shnewbs.deepprimal.worldgen.WaterwayHeight.MAP_CODEC);
    DENSITIES.register("ocean_height", () -> com.shnewbs.deepprimal.worldgen.OceanHeight.MAP_CODEC);
    DENSITIES.register(modBus);
    NeoForge.EVENT_BUS.register(new WorldgenProbe(container.getModInfo().getVersion().toString()));
    NeoForge.EVENT_BUS.register(new NoVanillaXp());
    container.registerConfig(ModConfig.Type.SERVER, com.shnewbs.deepprimal.engine.EngineRuntimeConfig.SPEC, "deepprimal/engine/runtime.toml");
    NeoForge.EVENT_BUS.register(new EngineCoreModule());
    LOGGER.info(
        "Deep Primal terrain/liquid engine registered; saved generators preserved. Block assets are"
            + " temporary palettes.");
  }
}
