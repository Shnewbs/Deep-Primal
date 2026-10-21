package com.shnewbs.deepprimal.lod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Persistent world identity prevents reusing a multiplayer cache after the server resets its world.
 */
public record LodWorld(String identity) implements CustomPacketPayload {
  public static final Type<LodWorld> TYPE =
      new Type<>(Identifier.fromNamespaceAndPath("deepprimal", "lod_world"));
  public static final StreamCodec<RegistryFriendlyByteBuf, LodWorld> CODEC =
      StreamCodec.of((b, p) -> b.writeUtf(p.identity, 64), b -> new LodWorld(b.readUtf(64)));

  public Type<LodWorld> type() {
    return TYPE;
  }
}
