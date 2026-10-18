package com.shnewbs.deepprimal.render.network;

import com.shnewbs.deepprimal.core.FarTerrain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HorizonTile(long session, Identifier dimension, int x, int z, boolean supported, short[] heights, int[] colors) implements CustomPacketPayload {
    public static final Type<HorizonTile> TYPE = new Type<>(Identifier.fromNamespaceAndPath("deepprimal", "horizon_tile"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HorizonTile> CODEC = StreamCodec.of((b, p) -> {
        b.writeLong(p.session); b.writeIdentifier(p.dimension); b.writeInt(p.x); b.writeInt(p.z); b.writeBoolean(p.supported);
        if (p.supported) for (int i = 0; i < FarTerrain.POINTS; i++) { b.writeShort(p.heights[i]); b.writeInt(p.colors[i]); }
    }, b -> {
        long s = b.readLong(); var dim = b.readIdentifier(); int x = b.readInt(), z = b.readInt(); boolean supported = b.readBoolean();
        short[] heights = new short[supported ? FarTerrain.POINTS : 0]; int[] colors = new int[heights.length];
        for (int i = 0; i < heights.length; i++) { heights[i] = b.readShort(); colors[i] = b.readInt(); }
        return new HorizonTile(s, dim, x, z, supported, heights, colors);
    });
    @Override public Type<HorizonTile> type() { return TYPE; }
}
