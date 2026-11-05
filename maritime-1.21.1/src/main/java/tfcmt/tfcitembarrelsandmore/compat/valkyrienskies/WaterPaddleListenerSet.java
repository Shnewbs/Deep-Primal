package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddlePhysicsListener;

public final class WaterPaddleListenerSet
implements ShipPhysicsListener {
    private final Map<BlockPos, WaterPaddlePhysicsListener> paddles = new ConcurrentHashMap<BlockPos, WaterPaddlePhysicsListener>();

    public void put(BlockPos worldPos, WaterPaddlePhysicsListener listener) {
        this.paddles.put(worldPos, listener);
    }

    public void remove(BlockPos worldPos) {
        this.paddles.remove(worldPos);
    }

    public WaterPaddlePhysicsListener get(BlockPos worldPos) {
        return this.paddles.get(worldPos);
    }

    public Collection<WaterPaddlePhysicsListener> values() {
        return this.paddles.values();
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel) {
        this.physTick(physShip, physLevel, 0.05);
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel, double delta) {
        for (WaterPaddlePhysicsListener listener : this.paddles.values()) {
            listener.physTick(physShip, physLevel, delta);
        }
    }
}

