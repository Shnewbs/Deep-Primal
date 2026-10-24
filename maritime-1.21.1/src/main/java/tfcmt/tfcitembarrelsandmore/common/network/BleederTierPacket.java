package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;

public record BleederTierPacket(BlockPos pos, int tier) implements CustomPacketPayload {
    public static final Type<BleederTierPacket> TYPE = new Type<>(TIBAMNetwork.id("tier"));
    public static final StreamCodec<FriendlyByteBuf, BleederTierPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeBlockPos(packet.pos()); buf.writeVarInt(packet.tier()); },
        buf -> new BleederTierPacket(buf.readBlockPos(), buf.readVarInt()));
    @Override public Type<BleederTierPacket> type() { return TYPE; }
    public static void handle(BleederTierPacket packet, IPayloadContext context) {
        BlockEntity be = TIBAMNetwork.getControlledBlock(context, packet.pos());
        int value = Math.clamp(packet.tier(), 0, 4);
        if (be instanceof SinglePistonEngineBlockEntity engine) engine.setBleederTier(value);
        else if (be instanceof BoilerTankBlockEntity tank) tank.setBleederTier(value);
    }
}
