package com.shnewbs.deepprimal.core;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Engine-core module for NeoForge mod loading.
 * Registers configs and event handlers for the core engine.
 */
@Mod("deepprimal_core")
public final class EngineCoreModule {
    private static EngineCore engineCore;

    public EngineCoreModule(ModContainer container) {
        // Register core configs
        container.registerConfig(
            ModConfig.Type.STARTUP,
            TerrainConfig.SPEC,
            "deepprimal/worldgen/terrain.toml"
        );
        container.registerConfig(
            ModConfig.Type.SERVER,
            HydrologyConfig.SPEC,
            "deepprimal-hydrology.toml"
        );
        container.registerConfig(
            ModConfig.Type.SERVER,
            StabilityRuntime.SPEC,
            "deepprimal/hazards/stability.toml"
        );

        // Register engine registry
        EngineRegistry.register(container.getEventBus());
        GroundMaterials.register(container.getEventBus());

        // Register event handlers
        NeoForge.EVENT_BUS.register(new HydrologyRuntime());
        NeoForge.EVENT_BUS.register(new WaterRuntime.Events());
        NeoForge.EVENT_BUS.register(new StabilityRuntime());

        // Initialize engine core on server start
        container.getEventBus().addListener(this::onServerStarting);
        container.getEventBus().addListener(this::onServerStopping);
    }

    private void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        // Create engine core with server world seed
        long seed = event.getServer().getLevel(net.minecraft.resources.ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.withDefaultNamespace("overworld")))
            .getSeed();
        
        TerrainOptions options = TerrainOptions.fromConfig();
        
        engineCore = new EngineCore.Builder()
            .seed(seed)
            .revision(7)
            .options(options)
            .densityCacheMB(128)
            .meshCacheMB(256)
            .build();
        
        engineCore.start();
    }

    private void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        if (engineCore != null) {
            engineCore.stop();
            engineCore = null;
        }
    }

    public static EngineCore getEngineCore() {
        return engineCore;
    }
}