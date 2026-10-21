package com.shnewbs.deepprimal.render;

import com.shnewbs.deepprimal.render.network.LodWorld;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class LodIdentity {
  private static final Map<MinecraftServer, String> IDS = new WeakHashMap<>();

  private static String id(MinecraftServer server) {
    return IDS.computeIfAbsent(
        server,
        s -> {
          Path p = s.getWorldPath(LevelResource.ROOT).resolve("data/deepprimal-world-id.txt");
          try {
            if (Files.exists(p)) return UUID.fromString(Files.readString(p).trim()).toString();
            String id = UUID.randomUUID().toString();
            Files.createDirectories(p.getParent());
            Files.writeString(p, id, StandardOpenOption.CREATE_NEW);
            return id;
          } catch (Exception e) {
            com.shnewbs.deepprimal.DeepPrimal.LOGGER.warn(
                "Persistent LOD identity unavailable; using session identity", e);
            return UUID.randomUUID().toString();
          }
        });
  }

  @SubscribeEvent
  public void login(PlayerEvent.PlayerLoggedInEvent e) {
    if (e.getEntity() instanceof ServerPlayer p)
      PacketDistributor.sendToPlayer(p, new LodWorld(id(p.level().getServer())));
  }

  @SubscribeEvent
  public void stopped(ServerStoppedEvent e) {
    IDS.remove(e.getServer());
  }
}
