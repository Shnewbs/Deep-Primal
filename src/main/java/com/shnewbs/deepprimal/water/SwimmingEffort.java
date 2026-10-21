package com.shnewbs.deepprimal.water;

import com.shnewbs.deepprimal.core.SwimmingState;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class SwimmingEffort {
  private final WeakHashMap<ServerPlayer, SwimmingState> states = new WeakHashMap<>();

  @SubscribeEvent
  public void tick(PlayerTickEvent.Post event) {
    if (!(event.getEntity() instanceof ServerPlayer player)
        || player.isCreative()
        || player.isSpectator()
        || !player.isAlive()) return;
    if (!RiverConfig.SWIM_STAMINA.get() && RiverConfig.TREADING_OFFSET.get() == 0) {
      states.remove(player);
      return;
    }
    boolean swimming =
        player.isInWater() && !player.onGround() && !player.isPassenger() && !player.onClimbable();
    var state = states.computeIfAbsent(player, unused -> new SwimmingState());
    boolean exhausted =
        state.tick(RiverConfig.SWIM_STAMINA.get() && swimming, RiverConfig.SWIM_SECONDS.get() * 20);
    if (!swimming) return;
    if (RiverConfig.SWIM_STAMINA.get() && player.tickCount % 20 == 0) {
      player.causeFoodExhaustion(.05f);
      player.sendSystemMessage(
          net.minecraft.network.chat.Component.translatable(
              "deepprimal.swimming.remaining",
              state.remaining(RiverConfig.SWIM_SECONDS.get() * 20) / 20),
          true);
    }
    double sink = exhausted ? .035 : RiverConfig.TREADING_OFFSET.get();
    if (sink > 0 && player.tickCount % 5 == 0 && !player.hasEffect(MobEffects.CONDUIT_POWER)) {
      player.setDeltaMovement(player.getDeltaMovement().add(0, -sink * 5, 0));
      player.hurtMarked = true;
    }
  }
}
