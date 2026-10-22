package tfcmt.tfcitembarrelsandmore.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfcmt.tfcitembarrelsandmore.common.BraceData;

public final class TIBAMNetwork {
    private TIBAMNetwork() {}
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", path); }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.21.1-1");
        registrar.playToServer(BleederRatePacket.TYPE, BleederRatePacket.STREAM_CODEC, BleederRatePacket::handle);
        registrar.playToServer(BleederTierPacket.TYPE, BleederTierPacket.STREAM_CODEC, BleederTierPacket::handle);
        registrar.playToServer(ThrottlePacket.TYPE, ThrottlePacket.STREAM_CODEC, ThrottlePacket::handle);
        registrar.playToServer(PrimedRecipePacket.TYPE, PrimedRecipePacket.STREAM_CODEC, PrimedRecipePacket::handle);
        registrar.playToClient(BraceDataPacket.TYPE, BraceDataPacket.STREAM_CODEC, BraceDataPacket::handle);
        registrar.playToClient(TripleEngineSpeedPacket.TYPE, TripleEngineSpeedPacket.STREAM_CODEC, TripleEngineSpeedPacket::handle);
    }
    static BlockEntity getControlledBlock(IPayloadContext context, BlockPos pos) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
            || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64
            || !player.serverLevel().hasChunkAt(pos)) return null;
        return player.serverLevel().getBlockEntity(pos);
    }
    public static void syncBrace(BlockPos pos, BraceData data, ServerLevel level) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), new BraceDataPacket(pos, data));
    }
    public static void removeBrace(BlockPos pos, ServerLevel level) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), new BraceDataPacket(pos));
    }
    public static void setBleederRate(BlockPos pos, int rate) { PacketDistributor.sendToServer(new BleederRatePacket(pos, rate)); }
    public static void setBleederTier(BlockPos pos, int tier) { PacketDistributor.sendToServer(new BleederTierPacket(pos, tier)); }
    public static void setThrottle(BlockPos pos, int throttle) { PacketDistributor.sendToServer(new ThrottlePacket(pos, throttle)); }
    public static void syncTripleEngineSpeed(BlockPos pos, float speed, ServerLevel level) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), new TripleEngineSpeedPacket(pos, speed));
    }
    public static void setPrimedRecipe(BlockPos pos, ResourceLocation id) { PacketDistributor.sendToServer(new PrimedRecipePacket(pos, id)); }
}
