package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;

public record PrimedRecipePacket(BlockPos pos, ResourceLocation recipeId) implements CustomPacketPayload {
    public static final Type<PrimedRecipePacket> TYPE = new Type<>(TIBAMNetwork.id("primed_recipe"));
    public static final StreamCodec<FriendlyByteBuf, PrimedRecipePacket> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> { buf.writeBlockPos(p.pos()); buf.writeNullable(p.recipeId(), FriendlyByteBuf::writeResourceLocation); },
        buf -> new PrimedRecipePacket(buf.readBlockPos(), buf.readNullable(FriendlyByteBuf::readResourceLocation)));
    @Override public Type<PrimedRecipePacket> type() { return TYPE; }
    public static void handle(PrimedRecipePacket packet, IPayloadContext context) {
        if (TIBAMNetwork.getControlledBlock(context, packet.pos()) instanceof MechanicalPowerHammerBlockEntity hammer) {
            hammer.setPrimedRecipeId(packet.recipeId());
        }
    }
}
