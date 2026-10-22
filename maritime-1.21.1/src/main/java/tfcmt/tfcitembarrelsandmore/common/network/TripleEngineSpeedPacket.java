package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TripleEngineSpeedPacket(BlockPos pos, float speed) implements CustomPacketPayload {
    public static final Type<TripleEngineSpeedPacket> TYPE = new Type<>(TIBAMNetwork.id("engine_speed"));
    public static final StreamCodec<FriendlyByteBuf, TripleEngineSpeedPacket> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> { buf.writeBlockPos(p.pos()); buf.writeFloat(p.speed()); },
        buf -> new TripleEngineSpeedPacket(buf.readBlockPos(), buf.readFloat()));
    @Override public Type<TripleEngineSpeedPacket> type() { return TYPE; }
    public static void handle(TripleEngineSpeedPacket packet, IPayloadContext context) {
        tfcmt.tfcitembarrelsandmore.client.MaritimeClientPayloads.handle(packet);
    }
}
