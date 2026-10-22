package tfcmt.tfcitembarrelsandmore.common;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.AirHoseItem;
import tfcmt.tfcitembarrelsandmore.common.items.DiveSuitArmorItem;

public class TIBAMDiveSuitEvents {
    private static final int EFFECT_DURATION_TICKS = 30;

    @SubscribeEvent
    public void onPlayerTickAir(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        if (!TIBAMDiveSuitEvents.isWearingFullDiveSuit(player)) {
            return;
        }
        if (!player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        AirCompressorBlockEntity compressor = TIBAMDiveSuitEvents.findConnectedCompressor(player);
        if (compressor == null) {
            return;
        }
        int wanted = (Integer)TIBAMConfig.SERVER.diveSuitAirPerSecond.get();
        int drained = compressor.drainAir(wanted);
        if (drained <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false, true));
    }

    private static boolean isWearingFullDiveSuit(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).getItem() instanceof DiveSuitArmorItem) continue;
            return false;
        }
        return true;
    }

    @Nullable
    private static AirCompressorBlockEntity findConnectedCompressor(Player player) {
        Level level = player.level();
        Set<GlobalPos> linked = AirCompressorBlockEntity.getLinkedCompressors(player.getUUID());
        for (GlobalPos pos : linked) {
            AirCompressorBlockEntity be;
            BlockEntity blockEntity;
            if (!pos.dimension().equals((Object)level.dimension()) || !level.isLoaded(pos.pos()) || !((blockEntity = level.getBlockEntity(pos.pos())) instanceof AirCompressorBlockEntity) || !(be = (AirCompressorBlockEntity)blockEntity).isPlayerConnected(player)) continue;
            return be;
        }
        return null;
    }

    @SubscribeEvent
    public void onPlayerTickTether(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        AirCompressorBlockEntity compressor = TIBAMDiveSuitEvents.findLinkedCompressorWithHose(player);
        if (compressor == null) {
            return;
        }
        int range = AirHoseItem.getRange(compressor.getTotalHoseCount());
        if (range <= 0) {
            return;
        }
        Vec3 hosePort = compressor.hosePortPos();
        Vec3 pos = player.position();
        Vec3 outward = pos.subtract(hosePort);
        double dist = outward.length();
        if (dist <= (double)range) {
            return;
        }
        Vec3 unit = outward.scale(1.0 / dist);
        Vec3 clamped = hosePort.add(unit.scale((double)range));
        Vec3 vel = player.getDeltaMovement();
        double outwardSpeed = vel.dot(unit);
        if (outwardSpeed > 0.0) {
            player.setDeltaMovement(vel.subtract(unit.scale(outwardSpeed)));
        }
        serverPlayer.connection.teleport(clamped.x, clamped.y, clamped.z, player.getYRot(), player.getXRot());
    }

    @Nullable
    private static AirCompressorBlockEntity findLinkedCompressorWithHose(Player player) {
        Level level = player.level();
        Set<GlobalPos> linked = AirCompressorBlockEntity.getLinkedCompressors(player.getUUID());
        for (GlobalPos pos : linked) {
            BlockEntity blockEntity;
            if (!pos.dimension().equals((Object)level.dimension()) || !level.isLoaded(pos.pos()) || !((blockEntity = level.getBlockEntity(pos.pos())) instanceof AirCompressorBlockEntity)) continue;
            AirCompressorBlockEntity be = (AirCompressorBlockEntity)blockEntity;
            return be;
        }
        return null;
    }
}

