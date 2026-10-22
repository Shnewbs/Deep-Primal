package tfcmt.tfcitembarrelsandmore.client.renderer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class HoseRope {
    private static final int POINT_COUNT = 20;
    private static final double SLACK_FACTOR = 1.12;
    private static final double GRAVITY_PER_TICK = 0.03;
    private static final double DAMPING = 0.85;
    private static final int CONSTRAINT_PASSES = 4;
    private static final double RESNAP_DISTANCE = 8.0;
    private final Vec3[] points = new Vec3[20];
    private final Vec3[] prevPoints = new Vec3[20];
    private double segmentLength;

    HoseRope(Vec3 start, Vec3 end) {
        this.layStraight(start, end);
    }

    private void layStraight(Vec3 start, Vec3 end) {
        for (int i = 0; i < 20; ++i) {
            Vec3 p;
            double t = (double)i / 19.0;
            this.points[i] = p = HoseRope.lerp(start, end, t);
            this.prevPoints[i] = p;
        }
        this.segmentLength = start.distanceTo(end) * 1.12 / 19.0;
    }

    List<Vec3> snapshot() {
        ArrayList<Vec3> out = new ArrayList<Vec3>(20);
        for (Vec3 p : this.points) {
            out.add(p);
        }
        return out;
    }

    List<Vec3> step(Level level, Vec3 anchorStart, Vec3 anchorEnd) {
        if (anchorStart.distanceToSqr(this.points[0]) > 64.0 || anchorEnd.distanceToSqr(this.points[19]) > 64.0) {
            this.layStraight(anchorStart, anchorEnd);
        }
        this.points[0] = anchorStart;
        this.points[19] = anchorEnd;
        this.segmentLength = anchorStart.distanceTo(anchorEnd) * 1.12 / 19.0;
        this.integrate();
        for (int i = 0; i < 4; ++i) {
            this.satisfyConstraints();
        }
        this.resolveCollisions(level);
        return this.snapshot();
    }

    private void integrate() {
        for (int i = 1; i < 19; ++i) {
            Vec3 current = this.points[i];
            Vec3 velocity = current.subtract(this.prevPoints[i]).scale(0.85);
            Vec3 next = current.add(velocity).subtract(new Vec3(0.0, 0.03, 0.0));
            this.prevPoints[i] = current;
            this.points[i] = next;
        }
    }

    private void satisfyConstraints() {
        for (int i = 0; i < 19; ++i) {
            boolean bPinned;
            Vec3 b = this.points[i + 1];
            Vec3 a = this.points[i];
            Vec3 delta = b.subtract(a);
            double dist = delta.length();
            if (dist < 1.0E-6) continue;
            double diff = (dist - this.segmentLength) / dist;
            Vec3 correction = delta.scale(0.5 * diff);
            boolean aPinned = i == 0;
            boolean bl = bPinned = i + 1 == 19;
            if (!aPinned) {
                this.points[i] = a.add(correction);
            }
            if (bPinned) continue;
            this.points[i + 1] = b.subtract(correction);
        }
    }

    private void resolveCollisions(Level level) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 1; i < 19; ++i) {
            Vec3 p = this.points[i];
            int x = (int)Math.floor(p.x);
            int y = (int)Math.floor(p.y);
            int z = (int)Math.floor(p.z);
            cursor.set(x, y, z);
            BlockState state = level.getBlockState((BlockPos)cursor);
            if (!state.getCollisionShape((BlockGetter)level, (BlockPos)cursor).isEmpty()) {
                double restY = (double)y + 1.0 + 0.02;
                this.points[i] = new Vec3(p.x, restY, p.z);
                this.prevPoints[i] = new Vec3(this.prevPoints[i].x, restY, this.prevPoints[i].z);
                continue;
            }
            if (!level.getFluidState((BlockPos)cursor).is(FluidTags.WATER)) continue;
            this.points[i] = new Vec3(p.x, p.y + 0.04, p.z);
        }
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }
}

