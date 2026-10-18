package com.shnewbs.deepprimal;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;

@Mod(DeepPrimal.ID)
public final class DeepPrimal {
    public static final String ID = "deepprimal";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<TicketType> TICKETS = DeferredRegister.create(Registries.TICKET_TYPE, ID);
    public static final DeferredHolder<TicketType, TicketType> PROBE_TICKET = TICKETS.register("worldgen_probe",
        () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING));

    public DeepPrimal(IEventBus modBus) {
        TICKETS.register(modBus);
        NeoForge.EVENT_BUS.register(new WorldgenProbe());
        NeoForge.EVENT_BUS.register(new NoVanillaXp());
        LOGGER.info("Deep Primal worldgen prototype loaded: vanilla placeholders, disposable test worlds.");
    }
}
