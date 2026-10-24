package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;

public record BleederRatePacket(BlockPos pos, int rate) implements CustomPacketPayload {
    public static final Type<BleederRatePacket> TYPE = new Type<>(TIBAMNetwork.id("rate"));
    public static final StreamCodec<FriendlyByteBuf, BleederRatePacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeBlockPos(packet.pos()); buf.writeVarInt(packet.rate()); },
        buf -> new BleederRatePacket(buf.readBlockPos(), buf.readVarInt()));
    @Override public Type<BleederRatePacket> type() { return TYPE; }
    public static void handle(BleederRatePacket packet, IPayloadContext context) {
        BlockEntity be = TIBAMNetwork.getControlledBlock(context, packet.pos());
        int value = Math.clamp(packet.rate(), 0, 10);
        if (be instanceof AirCompressorBlockEntity compressor) compressor.setBleederRate(value);
    }
}
