package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;

public class PropellerPhysicsListener
implements ShipPhysicsListener {
    private final BlockPos worldPos;
    private final Direction facing;
    private final PropellerType type;
    private volatile double currentRpm = 0.0;
    private boolean reversed = false;
    private double effectiveThrust = 0.0;

    public PropellerPhysicsListener(BlockPos worldPos, Direction facing, PropellerType type) {
        this.worldPos = worldPos;
        this.facing = facing;
        this.type = type;
    }

    public PropellerType getType() {
        return this.type;
    }

    public void setCurrentRpm(double rpm) {
        this.currentRpm = rpm;
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel) {
        this.physTick(physShip, physLevel, 0.05);
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel, double delta) {
        double taperMultiplier;
        double rawThrustNewtons;
        double signedRpm = this.currentRpm;
        double rpm = Math.abs(signedRpm);
        double d = rawThrustNewtons = this.type == PropellerType.BRASS ? TIBAMConfig.COMMON.brassThrustNewtons(rpm) : TIBAMConfig.COMMON.copperThrustNewtons(rpm);
        if (rawThrustNewtons > 0.0) {
            boolean bl = this.reversed = signedRpm < 0.0;
        }
        if (rawThrustNewtons >= this.effectiveThrust) {
            this.effectiveThrust = rawThrustNewtons;
        } else {
            double peakThrust = this.type == PropellerType.BRASS ? TIBAMConfig.COMMON.brassPeakThrustNewtons() : TIBAMConfig.COMMON.copperPeakThrustNewtons();
            double spindownSeconds = (Double)TIBAMConfig.COMMON.propulsionSpindownSeconds.get();
            double maxDecreasePerTick = peakThrust / spindownSeconds * delta;
            this.effectiveThrust = Math.max(rawThrustNewtons, this.effectiveThrust - maxDecreasePerTick);
        }
        if (this.effectiveThrust <= 0.0) {
            return;
        }
        BodyKinematics kinematics = physShip.getKinematics();
        Matrix4dc shipToWorld = kinematics.getToWorld();
        Direction thrustFacing = this.reversed ? this.facing.getOpposite() : this.facing;
        Vector3d forceDirection = shipToWorld.transformDirection(new Vector3d((double)thrustFacing.getStepX(), (double)thrustFacing.getStepY(), (double)thrustFacing.getStepZ()));
        forceDirection.normalize();
        double maxSpeed = (Double)TIBAMConfig.COMMON.maxShipSpeedMps.get();
        double taperFraction = (Double)TIBAMConfig.COMMON.speedTaperFraction.get();
        double speedAlongThrust = kinematics.getVelocity().dot((Vector3dc)forceDirection);
        if (speedAlongThrust >= maxSpeed) {
            taperMultiplier = 0.0;
        } else {
            double taperBand = maxSpeed * taperFraction;
            double taperStart = maxSpeed - taperBand;
            double d2 = taperMultiplier = speedAlongThrust <= taperStart || taperBand <= 0.0 ? 1.0 : (maxSpeed - speedAlongThrust) / taperBand;
        }
        if (taperMultiplier <= 0.0) {
            return;
        }
        Vector3d force = forceDirection.mul(this.effectiveThrust * taperMultiplier, new Vector3d());
        Vector3d posInWorld = shipToWorld.transformPosition(new Vector3d((double)this.worldPos.getX() + 0.5, (double)this.worldPos.getY() + 0.5, (double)this.worldPos.getZ() + 0.5));
        physShip.applyWorldForce((Vector3dc)force, (Vector3dc)posInWorld);
    }

    public static enum PropellerType {
        COPPER,
        BRASS;

    }
}

