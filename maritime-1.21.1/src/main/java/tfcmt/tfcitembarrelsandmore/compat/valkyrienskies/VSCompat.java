package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public final class VSCompat {
    public static boolean isLoaded() {
        return ModList.get().isLoaded("valkyrienskies");
    }

    public static LoadedServerShip getShipAt(Level level, BlockPos pos) {
        LoadedServerShip serverShip;
        LoadedShip ship = VSGameUtilsKt.getLoadedShipManagingPos((Level)level, (Vec3i)pos);
        return ship instanceof LoadedServerShip ? (serverShip = (LoadedServerShip)ship) : null;
    }

    public static Vec3 getShipVelocity(Level level, BlockPos pos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        if (ship == null) {
            return null;
        }
        Vector3dc v = ship.getVelocity();
        return new Vec3(v.x(), v.y(), v.z());
    }

    public static Vec3 worldDirectionToShipLocal(Level level, BlockPos pos, Vec3 worldDir) {
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        if (ship == null) {
            return null;
        }
        Vector3d local = ship.getWorldToShip().transformDirection(new Vector3d(worldDir.x, worldDir.y, worldDir.z));
        return new Vec3(local.x, local.y, local.z);
    }

    public static Long getShipId(Level level, BlockPos pos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        return ship != null ? Long.valueOf(ship.getId()) : null;
    }

    public static Vec3 shipLocalToWorld(Level level, BlockPos pos, Vec3 shipLocalPos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        return ship != null ? VSGameUtilsKt.toWorldCoordinates((Ship)ship, (Vec3)shipLocalPos) : shipLocalPos;
    }

    public static Vec3 spawnPosToWorld(Level level, BlockPos anchor, double x, double y, double z) {
        if (!VSCompat.isLoaded()) {
            return new Vec3(x, y, z);
        }
        return VSCompat.shipLocalToWorld(level, anchor, new Vec3(x, y, z));
    }

    private VSCompat() {
    }
}

