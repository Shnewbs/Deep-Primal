package com.shnewbs.deepprimal.lod;

import com.mojang.logging.LogUtils;
import com.shnewbs.deepprimal.lod.network.LodWorld;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

/** Common networking/identity owner; dedicated servers never link client rendering classes. */
@Mod("deepprimal_lod")
public final class LodModule {
  public static final Logger LOGGER = LogUtils.getLogger();

  public LodModule(IEventBus modBus) {
    modBus.addListener(LodModule::payloads);
    NeoForge.EVENT_BUS.register(new LodIdentity());
  }

  private static void payloads(RegisterPayloadHandlersEvent event) {
    // Preserve the existing channel and version for alpha.18 peers and cache identity.
    event.registrar("3").optional().playToClient(LodWorld.TYPE, LodWorld.CODEC);
  }
}
