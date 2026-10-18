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
    public static final DeferredRegister<TicketType> TICKETS = DeferredRegister.create(Registries.TICKET_TYPE, ID);
    public static final DeferredHolder<TicketType, TicketType> PROBE_TICKET = TICKETS.register("worldgen_probe",
        () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING));
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITIES =
        DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, ID);

    public DeepPrimal(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, PerformanceConfig.SPEC, "deepprimal-performance.toml");
        TICKETS.register(modBus);
        DENSITIES.register("column_surface", () -> ColumnSurface.MAP_CODEC);
        DENSITIES.register(modBus);
        NeoForge.EVENT_BUS.register(new WorldgenProbe(container.getModInfo().getVersion().toString()));
        NeoForge.EVENT_BUS.register(new NoVanillaXp());
        LOGGER.info("Deep Primal worldgen prototype loaded: vanilla placeholders, disposable test worlds.");
    }
}
