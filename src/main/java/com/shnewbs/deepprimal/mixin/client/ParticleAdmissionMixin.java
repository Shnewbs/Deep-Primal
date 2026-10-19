package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.client.VisualPerformance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleAdmissionMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void deepprimal$newTick(CallbackInfo callback) { VisualPerformance.newParticleTick(); }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void deepprimal$admit(Particle particle, CallbackInfo callback) {
        if (ClientPerformanceConfig.PARTICLE_LIMIT.get() == 0 && ClientPerformanceConfig.PARTICLE_SPAWNS.get() == 0) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var group = particle.getGroup();
        if (group == ParticleRenderType.NO_RENDER || group == ParticleRenderType.ITEM_PICKUP || group == ParticleRenderType.ELDER_GUARDIANS) return;
        var camera = mc.gameRenderer.getMainCamera().position();
        if (VisualPerformance.outside(particle.getBoundingBox(), camera.x, camera.y, camera.z,
            ClientPerformanceConfig.PARTICLE_LIMIT.get()) || !VisualPerformance.admitParticle()) {
            VisualPerformance.particlesSkipped++;
            callback.cancel();
        }
    }
}
