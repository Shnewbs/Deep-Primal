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

public class WaterPaddlePhysicsListener
implements ShipPhysicsListener {
    private final BlockPos worldPos;
    private final Vector3d localAxis;
    private volatile double currentSignedRpm = 0.0;
    private boolean reversed = false;
    private double effectiveThrust = 0.0;
    private volatile boolean propellersPresent = false;
    private volatile double thrustMultiplier = 1.0;

    public WaterPaddlePhysicsListener(BlockPos worldPos, Direction.Axis shaftAxis) {
        this.worldPos = worldPos;
        this.localAxis = shaftAxis == Direction.Axis.X ? new Vector3d(1.0, 0.0, 0.0) : new Vector3d(0.0, 0.0, 1.0);
    }

    public void setCurrentSignedRpm(double rpm) {
        this.currentSignedRpm = rpm;
    }

    public void setPropellersPresent(boolean present) {
        this.propellersPresent = present;
    }

    public void setThrustMultiplier(double multiplier) {
        this.thrustMultiplier = multiplier;
    }

    public double getThrustMultiplier() {
        return this.thrustMultiplier;
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel) {
        this.physTick(physShip, physLevel, 0.05);
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel, double delta) {
        double taperMultiplier;
        double signedRpm = this.currentSignedRpm;
        double rpm = Math.abs(signedRpm);
        double multiplier = this.thrustMultiplier;
        double rawThrustNewtons = TIBAMConfig.COMMON.paddleThrustNewtons(rpm) * multiplier;
        if (rawThrustNewtons > 0.0) {
            this.reversed = signedRpm < 0.0;
        }
        double peakThrust = TIBAMConfig.COMMON.paddlePeakThrustNewtons() * multiplier;
        if (rawThrustNewtons > this.effectiveThrust) {
            double spinupSeconds = this.propellersPresent ? (Double)TIBAMConfig.COMMON.paddleSpinupWithPropellersSeconds.get() : (Double)TIBAMConfig.COMMON.paddleSpinupSeconds.get();
            double maxIncreasePerTick = peakThrust / spinupSeconds * delta;
            this.effectiveThrust = Math.min(rawThrustNewtons, this.effectiveThrust + maxIncreasePerTick);
        } else if (rawThrustNewtons < this.effectiveThrust) {
            double spindownSeconds = (Double)TIBAMConfig.COMMON.propulsionSpindownSeconds.get();
            double maxDecreasePerTick = peakThrust / spindownSeconds * delta;
            this.effectiveThrust = Math.max(rawThrustNewtons, this.effectiveThrust - maxDecreasePerTick);
        }
        if (this.effectiveThrust <= 0.0) {
            return;
        }
        BodyKinematics kinematics = physShip.getKinematics();
        Matrix4dc shipToWorld = kinematics.getToWorld();
        Vector3d localThrust = new Vector3d((Vector3dc)this.localAxis).cross((Vector3dc)new Vector3d(0.0, -1.0, 0.0));
        if (this.reversed) {
            localThrust.negate();
        }
        Vector3d forceDirection = shipToWorld.transformDirection(localThrust);
        forceDirection.normalize();
        double maxSpeed = (Double)TIBAMConfig.COMMON.maxShipSpeedMps.get();
        double taperFraction = (Double)TIBAMConfig.COMMON.speedTaperFraction.get();
        double speedAlongThrust = kinematics.getVelocity().dot((Vector3dc)forceDirection);
        if (speedAlongThrust >= maxSpeed) {
            taperMultiplier = 0.0;
        } else {
            double taperBand = maxSpeed * taperFraction;
            double taperStart = maxSpeed - taperBand;
            double d = taperMultiplier = speedAlongThrust <= taperStart || taperBand <= 0.0 ? 1.0 : (maxSpeed - speedAlongThrust) / taperBand;
        }
        if (taperMultiplier <= 0.0) {
            return;
        }
        Vector3d force = forceDirection.mul(this.effectiveThrust * taperMultiplier, new Vector3d());
        Vector3d posInWorld = shipToWorld.transformPosition(new Vector3d((double)this.worldPos.getX() + 0.5, (double)this.worldPos.getY() + 0.5, (double)this.worldPos.getZ() + 0.5));
        physShip.applyWorldForce((Vector3dc)force, (Vector3dc)posInWorld);
    }
}

