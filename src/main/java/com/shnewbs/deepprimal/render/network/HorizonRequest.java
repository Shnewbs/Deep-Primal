package com.shnewbs.deepprimal.render.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HorizonRequest(long session, Identifier dimension, int x, int z) implements CustomPacketPayload {
    public static final Type<HorizonRequest> TYPE = new Type<>(Identifier.fromNamespaceAndPath("deepprimal", "horizon_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HorizonRequest> CODEC = StreamCodec.of((b, p) -> {
        b.writeLong(p.session); b.writeIdentifier(p.dimension); b.writeInt(p.x); b.writeInt(p.z);
    }, b -> new HorizonRequest(b.readLong(), b.readIdentifier(), b.readInt(), b.readInt()));
    @Override public Type<HorizonRequest> type() { return TYPE; }
}
