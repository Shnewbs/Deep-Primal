package com.shnewbs.deepprimal.settings;

import com.shnewbs.deepprimal.core.VisualBudget;
import net.minecraft.world.phys.AABB;

/** Scalar counters, updated on the client thread. No per-object visibility cache. */
public final class VisualPerformance {
    public static long entitiesSkipped, blockEntitiesSkipped, particlesSkipped;
    public static long chunkSyncBuilds, chunkSyncNanos, chunkRebuildsDeferred, chunkPlayerBuilds;
    private static int admittedParticles;
    public static void newParticleTick() { admittedParticles = 0; }
    public static boolean admitParticle() {
        int maximum = ClientPerformanceConfig.PARTICLE_SPAWNS.get();
        if (maximum == 0) return true;
        if (admittedParticles >= maximum) return false;
        admittedParticles++;
        return true;
    }
    public static boolean outside(AABB box, double x, double y, double z, int radius) {
        return VisualBudget.outside(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, x, y, z, radius);
    }
    private VisualPerformance() {}
}
