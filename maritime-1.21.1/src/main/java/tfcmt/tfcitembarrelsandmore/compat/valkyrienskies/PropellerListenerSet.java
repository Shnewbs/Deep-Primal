package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerPhysicsListener;

public final class PropellerListenerSet
implements ShipPhysicsListener {
    private final Map<BlockPos, PropellerPhysicsListener> propellers = new ConcurrentHashMap<BlockPos, PropellerPhysicsListener>();

    public void put(BlockPos worldPos, PropellerPhysicsListener listener) {
        this.propellers.put(worldPos, listener);
    }

    public void remove(BlockPos worldPos) {
        this.propellers.remove(worldPos);
    }

    public PropellerPhysicsListener get(BlockPos worldPos) {
        return this.propellers.get(worldPos);
    }

    public boolean isEmpty() {
        return this.propellers.isEmpty();
    }

    public Collection<PropellerPhysicsListener> values() {
        return this.propellers.values();
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel) {
        this.physTick(physShip, physLevel, 0.05);
    }

    public void physTick(PhysShip physShip, PhysLevel physLevel, double delta) {
        for (PropellerPhysicsListener listener : this.propellers.values()) {
            listener.physTick(physShip, physLevel, delta);
        }
    }
}

