package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

/** Optional Minecraft bridge. Physics listener API compatibility is validated separately. */
public final class VSCompat {
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();
    private static final AtomicBoolean REPORTED = new AtomicBoolean();
    private VSCompat() {}
    public static boolean isLoaded() {
        if (!ModList.get().isLoaded("valkyrienskies")) return false;
        boolean supported = VSCompat.class.getClassLoader().getResource("org/valkyrienskies/core/api/ships/ShipPhysicsListener.class") != null;
        if (!supported && REPORTED.compareAndSet(false, true)) tfcitembarrelsandmore.LOGGER.warn("Maritime ship integration disabled: this VS backend does not provide the required physics-listener API");
        return supported;
    }
    private static Object invoke(String name, Object... arguments) {
        if (!isLoaded()) return null;
        try {
            Method method = METHODS.get(name);
            if (method == null) {
                Class<?> bridge = Class.forName("org.valkyrienskies.mod.common.VSGameUtilsKt");
                for (Method candidate : bridge.getMethods()) {
                    if (!candidate.getName().equals(name) || candidate.getParameterCount() != arguments.length) continue;
                    boolean matches = true;
                    for (int i = 0; i < arguments.length; i++) if (arguments[i] != null && !candidate.getParameterTypes()[i].isInstance(arguments[i])) matches = false;
                    if (matches) { method = candidate; METHODS.put(name, candidate); break; }
                }
                if (method == null) throw new NoSuchMethodException(name);
            }
            return method.invoke(null, arguments);
        } catch (ReflectiveOperationException | LinkageError error) {
            if (REPORTED.compareAndSet(false, true)) tfcitembarrelsandmore.LOGGER.error("Maritime: installed VS Minecraft bridge is incompatible; ship integration requires validation", error);
            return null;
        }
    }
    public static LoadedServerShip getShipAt(Level level, BlockPos pos) {
        Object result = invoke("getLoadedShipManagingPos", level, pos);
        return result instanceof LoadedServerShip ship ? ship : null;
    }
    private static Ship shipAt(Level level, BlockPos pos) {
        Object result = invoke("getShipManagingPos", level, pos);
        return result instanceof Ship ship ? ship : null;
    }
    public static List<Ship> getShipsIntersecting(Level level, AABB bounds) {
        Object result = invoke("getShipsIntersecting", level, bounds);
        List<Ship> ships = new ArrayList<>();
        if (result instanceof Iterable<?> iterable) for (Object item : iterable) if (item instanceof Ship ship) ships.add(ship);
        return ships;
    }
    public static Vec3 getShipVelocity(Level level, BlockPos pos) {
        Ship ship = shipAt(level, pos);
        if (ship == null) return null;
        var velocity = ship.getVelocity();
        return new Vec3(velocity.x(), velocity.y(), velocity.z());
    }
    public static Vec3 worldDirectionToShipLocal(Level level, BlockPos pos, Vec3 vector) {
        Ship ship = shipAt(level, pos);
        if (ship == null) return null;
        var transformed = ship.getWorldToShip().transformDirection(new Vector3d(vector.x, vector.y, vector.z));
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }
    public static Long getShipId(Level level, BlockPos pos) { Ship ship = shipAt(level, pos); return ship == null ? null : ship.getId(); }
    public static Vec3 shipLocalToWorld(Level level, BlockPos pos, Vec3 vector) {
        Ship ship = shipAt(level, pos);
        if (ship == null) return vector;
        var transformed = ship.getShipToWorld().transformPosition(new Vector3d(vector.x, vector.y, vector.z));
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }
    public static Vec3 spawnPosToWorld(Level level, BlockPos pos, double x, double y, double z) {
        return isLoaded() ? shipLocalToWorld(level, pos, new Vec3(x, y, z)) : new Vec3(x, y, z);
    }
}
