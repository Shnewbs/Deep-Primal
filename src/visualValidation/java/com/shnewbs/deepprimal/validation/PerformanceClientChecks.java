package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.client.HorizonSettingsScreen;
import com.shnewbs.deepprimal.client.VisualPerformance;
import com.shnewbs.deepprimal.client.render.HorizonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.joml.Matrix4f;

/** Disposable visual-only objects verify that the live hooks run before expensive extraction. */
public final class PerformanceClientChecks {
    public static void run(Minecraft mc) {
        var camera = mc.gameRenderer.getMainCamera().position();
        var frustum = new Frustum(new Matrix4f(), new Matrix4f());
        int entities = ClientPerformanceConfig.ENTITY_LIMIT.get();
        int blocks = ClientPerformanceConfig.BLOCK_ENTITY_LIMIT.get();
        int particles = ClientPerformanceConfig.PARTICLE_LIMIT.get();
        int spawns = ClientPerformanceConfig.PARTICLE_SPAWNS.get();
        try {
            ClientPerformanceConfig.ENTITY_LIMIT.set(32);
            var entity = new ArmorStand(mc.level, camera.x + 200, camera.y, camera.z) { public void synchronizedGlow() { setSharedFlag(6, true); } };
            long before = VisualPerformance.entitiesSkipped;
            if (mc.getEntityRenderDispatcher().shouldRender(entity, frustum, camera.x, camera.y, camera.z)
                || VisualPerformance.entitiesSkipped != before + 1) throw new AssertionError("Entity range hook missing");
            entity.synchronizedGlow();
            mc.getEntityRenderDispatcher().shouldRender(entity, frustum, camera.x, camera.y, camera.z);
            if (VisualPerformance.entitiesSkipped != before + 1) throw new AssertionError("Glowing entity was clipped");
            ClientPerformanceConfig.BLOCK_ENTITY_LIMIT.set(16);
            var chest = new ChestBlockEntity(BlockPos.containing(camera.x + 120, camera.y, camera.z), Blocks.CHEST.defaultBlockState());
            chest.setLevel(mc.level);
            long priorBlocks = VisualPerformance.blockEntitiesSkipped;
            if (mc.getBlockEntityRenderDispatcher().tryExtractRenderState(chest, 0, null, null) != null
                || VisualPerformance.blockEntitiesSkipped != priorBlocks + 1) throw new AssertionError("Block-entity range hook missing");
            ClientPerformanceConfig.PARTICLE_LIMIT.set(16);
            long priorParticles = VisualPerformance.particlesSkipped;
            Particle testParticle = new Particle(mc.level, camera.x + 100, camera.y, camera.z) {
                @Override public ParticleRenderType getGroup() { return ParticleRenderType.SINGLE_QUADS; }
            };
            mc.particleEngine.add(testParticle);
            if (VisualPerformance.particlesSkipped != priorParticles + 1) throw new AssertionError("Particle admission hook missing");
            ClientPerformanceConfig.PARTICLE_SPAWNS.set(1); VisualPerformance.newParticleTick();
            if (!VisualPerformance.admitParticle() || VisualPerformance.admitParticle()) throw new AssertionError("Particle tick cap missing");
            var renderer = HorizonRenderer.INSTANCE;
            if (renderer.maxUploadsPerFrame > ClientPerformanceConfig.UPLOAD_LEVELS.get()
                || renderer.maxUploadBytesPerFrame > ClientPerformanceConfig.UPLOAD_KIB.get() * 1024L) throw new AssertionError("Upload budget exceeded");
            var screen = new HorizonSettingsScreen(null); mc.setScreen(screen);
            for (int page = 0; page < 3; page++) button(mc, "›").onPress(null);
            button(mc, "Speed — reduced visual detail").onPress(null);
            if (ClientPerformanceConfig.ENTITY_LIMIT.get() != 32) throw new AssertionError("Profile changed config before Apply");
            screen.onClose();
            System.out.println("PERFORMANCE_CONTROLS_PASS indexedUploads=" + renderer.maxUploadsPerFrame
                + " maxUploadBytes=" + renderer.maxUploadBytesPerFrame + " entity=true glowExempt=true blockEntity=true particles=true profilesStaged=true");
        } finally {
            ClientPerformanceConfig.ENTITY_LIMIT.set(entities); ClientPerformanceConfig.BLOCK_ENTITY_LIMIT.set(blocks);
            ClientPerformanceConfig.PARTICLE_LIMIT.set(particles); ClientPerformanceConfig.PARTICLE_SPAWNS.set(spawns);
            VisualPerformance.newParticleTick(); mc.setScreen(null);
        }
    }
    private static Button button(Minecraft mc, String label) {
        return mc.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
            .filter(b -> b.getMessage().getString().equals(label)).findFirst().orElseThrow();
    }
    private PerformanceClientChecks() {}
}
