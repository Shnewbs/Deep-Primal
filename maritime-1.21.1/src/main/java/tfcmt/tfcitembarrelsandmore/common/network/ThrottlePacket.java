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

public record ThrottlePacket(BlockPos pos, int throttle) implements CustomPacketPayload {
    public static final Type<ThrottlePacket> TYPE = new Type<>(TIBAMNetwork.id("throttle"));
    public static final StreamCodec<FriendlyByteBuf, ThrottlePacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeBlockPos(packet.pos()); buf.writeVarInt(packet.throttle()); },
        buf -> new ThrottlePacket(buf.readBlockPos(), buf.readVarInt()));
    @Override public Type<ThrottlePacket> type() { return TYPE; }
    public static void handle(ThrottlePacket packet, IPayloadContext context) {
        BlockEntity be = TIBAMNetwork.getControlledBlock(context, packet.pos());
        int value = Math.clamp(packet.throttle(), 0, 100);
        if (be instanceof SinglePistonEngineBlockEntity engine) engine.setThrottle(value);
        else if (be instanceof BoilerTankBlockEntity tank) tank.setThrottle(value);
    }
}
