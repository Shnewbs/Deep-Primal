package com.shnewbs.deepprimal.performance.mixin;

import com.shnewbs.deepprimal.performance.PerformanceConfig;
import net.minecraft.world.entity.Mob;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Reimplements 26.3's persistent-mob idle timer behavior; despawn decisions stay native. */
@Mixin(Mob.class)
public abstract class MobIdleMixin {
  @Redirect(
      method = "checkDespawn",
      at =
          @At(
              value = "FIELD",
              target = "Lnet/minecraft/world/entity/Mob;noActionTime:I",
              opcode = Opcodes.PUTFIELD))
  private void deepprimal$preserveFarIdleTime(Mob mob, int resetValue) {
    if (PerformanceConfig.PERSISTENT_IDLE.get()
        && (mob.isPersistenceRequired() || mob.requiresCustomPersistence())) {
      var player = mob.level().getNearestPlayer(mob, -1.0);
      int range = mob.getType().getCategory().getNoDespawnDistance();
      if (player == null || player.distanceToSqr(mob) >= (double) range * range) return;
    }
    mob.setNoActionTime(resetValue);
  }
}
