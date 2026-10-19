package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.client.VisualPerformance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityDistanceMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void deepprimal$visualRange(Entity entity, Frustum frustum, double x, double y, double z,
                                      CallbackInfoReturnable<Boolean> callback) {
        int radius = ClientPerformanceConfig.ENTITY_LIMIT.get();
        if (radius == 0 || entity instanceof Player || Minecraft.getInstance().shouldEntityAppearGlowing(entity)
            || entity instanceof Leashable leashable && leashable.getLeashHolder() != null) return;
        if (VisualPerformance.outside(entity.getBoundingBox(), x, y, z, radius)) {
            VisualPerformance.entitiesSkipped++;
            callback.setReturnValue(false);
        }
    }
}
