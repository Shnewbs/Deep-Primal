package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfcmt.tfcitembarrelsandmore.common.BraceData;

public record BraceDataPacket(BlockPos pos, String braceType, String variant, String mod) implements CustomPacketPayload {
    public static final Type<BraceDataPacket> TYPE = new Type<>(TIBAMNetwork.id("brace_data"));
    public static final StreamCodec<FriendlyByteBuf, BraceDataPacket> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> { buf.writeBlockPos(p.pos()); buf.writeUtf(p.braceType(), 128); buf.writeUtf(p.variant(), 128); buf.writeUtf(p.mod(), 128); },
        buf -> new BraceDataPacket(buf.readBlockPos(), buf.readUtf(128), buf.readUtf(128), buf.readUtf(128)));
    public BraceDataPacket(BlockPos pos, BraceData data) { this(pos, data.type, data.variant, data.mod); }
    public BraceDataPacket(BlockPos pos) { this(pos, "", "", ""); }
    @Override public Type<BraceDataPacket> type() { return TYPE; }
    public static void handle(BraceDataPacket packet, IPayloadContext context) {
        tfcmt.tfcitembarrelsandmore.client.MaritimeClientPayloads.handle(packet);
    }
}
