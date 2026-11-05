package com.shnewbs.deepprimal.water;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Optional gameplay policies. Pressure work is bounded and runs once per player per second. */
public final class WaterEnvironment {
  @SubscribeEvent
  public void sources(CreateFluidSourceEvent event) {
    if (FiniteWater.enabled() && event.getFluidState().is(FluidTags.WATER)) {
      event.setCanConvert(false);
      return;
    }
    if (!RiverConfig.FINITE_SOURCES.get() || !event.getFluidState().is(FluidTags.WATER)) return;
    boolean natural =
        event.getState().getBlock() instanceof RiverWaterBlock
            || event.getLevel().getBiome(event.getPos()).is(BiomeTags.IS_RIVER)
            || event.getLevel().getBiome(event.getPos()).is(BiomeTags.IS_OCEAN);
    event.setCanConvert(event.getVanillaResult() && RiverConfig.NATURAL_RENEWAL.get() && natural);
  }

  @SubscribeEvent
  public void pressure(PlayerTickEvent.Post event) {
    if (!(event.getEntity() instanceof ServerPlayer player)
        || player.tickCount % 20 != 0
        || !RiverConfig.PRESSURE.get()
        || player.isCreative()
        || player.isSpectator()
        || player.hasEffect(MobEffects.CONDUIT_POWER)) return;
    var level = player.level();
    var pos = BlockPos.containing(player.getEyePosition());
    int depth = columnDepth(level, pos);
    int free = RiverConfig.PRESSURE_DEPTH.get();
    int armor = 0;
    for (var slot : net.minecraft.world.entity.EquipmentSlot.values())
      if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR
          && !player.getItemBySlot(slot).isEmpty()) armor++;
    float protection = (float) Math.max(0, 1 - armor * RiverConfig.ARMOR_PROTECTION.get());
    if (depth > free) {
      if (RiverConfig.PRESSURE_EFFECTS.get()) {
        player.addEffect(
            new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.SLOWNESS, 40, depth > free + 64 ? 1 : 0));
        if (depth > free + 32)
          player.addEffect(
              new net.minecraft.world.effect.MobEffectInstance(MobEffects.MINING_FATIGUE, 40));
      }
      if (protection > 0)
        player.hurtServer(
            level,
            player.damageSources().drown(),
            Math.min(8, 1 + (depth - free) / 32f) * protection);
    }
  }

  public static int columnDepth(net.minecraft.world.level.Level level, BlockPos eyes) {
    var cursor = eyes.mutable();
    int depth = 0;
    while (depth < 512
        && cursor.getY() < level.getMaxY()
        && level.hasChunkAt(cursor)
        && level.getFluidState(cursor).is(FluidTags.WATER)) {
      depth++;
      cursor.move(net.minecraft.core.Direction.UP);
    }
    return depth;
  }
}
