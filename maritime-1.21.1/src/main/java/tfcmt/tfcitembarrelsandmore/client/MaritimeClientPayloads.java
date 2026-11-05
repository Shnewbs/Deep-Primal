package tfcmt.tfcitembarrelsandmore.client;

import net.minecraft.client.Minecraft;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.network.BraceDataPacket;
import tfcmt.tfcitembarrelsandmore.common.network.TripleEngineSpeedPacket;

public final class MaritimeClientPayloads {
    private MaritimeClientPayloads() {}
    public static void handle(BraceDataPacket packet) {
        if (packet.braceType().isEmpty()) BraceClientCache.remove(packet.pos());
        else BraceClientCache.put(packet.pos(), new BraceData(packet.braceType(), packet.variant(), packet.mod()));
        var mc = Minecraft.getInstance();
        if (mc.levelRenderer != null) mc.levelRenderer.setSectionDirty(packet.pos().getX() >> 4, packet.pos().getY() >> 4, packet.pos().getZ() >> 4);
    }
    public static void handle(TripleEngineSpeedPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !Float.isFinite(packet.speed())) return;
        var blockEntity = level.getBlockEntity(packet.pos());
        if (blockEntity instanceof TripleExpansionEngineBlockEntity engine) engine.setClientSpeed(packet.speed());
        else if (blockEntity instanceof LargeSinglePistonEngineBlockEntity engine) engine.setClientSpeed(packet.speed());
    }
}
