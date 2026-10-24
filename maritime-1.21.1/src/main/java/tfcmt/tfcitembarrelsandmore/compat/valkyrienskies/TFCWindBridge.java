package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import java.util.HashSet;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;

public final class TFCWindBridge {
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!((Boolean)TIBAMConfig.COMMON.windBridgeEnabled.get()).booleanValue()) {
            return;
        }
        if (++this.tickCounter < (Integer)TIBAMConfig.COMMON.windBridgeIntervalTicks.get()) {
            return;
        }
        this.tickCounter = 0;
        MinecraftServer server = event.getServer();
        double speedScale = (Double)TIBAMConfig.COMMON.windBridgeSpeedScale.get();
        double radius = (Double)TIBAMConfig.COMMON.windBridgePlayerSearchRadius.get();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            HashSet<Long> handledShipIds = new HashSet<Long>();
            for (ServerPlayer player : level.players()) {
                AABB searchArea = player.getBoundingBox().inflate(radius);
                for (Ship ship : VSCompat.getShipsIntersecting((Level)level, (AABB)searchArea)) {
                    LoadedServerShip loadedShip;
                    if (!(ship instanceof LoadedServerShip) || !handledShipIds.add((loadedShip = (LoadedServerShip)ship).getId())) continue;
                    this.applyWindTo(level, loadedShip, speedScale);
                }
            }
        }
    }

    private void applyWindTo(ServerLevel level, LoadedServerShip ship, double speedScale) {
        if (ship.getDragController() == null) {
            return;
        }
        Vector3dc posInWorld = ship.getTransform().getPositionInWorld();
        BlockPos samplePos = new BlockPos(Mth.floor((double)posInWorld.x()), Mth.floor((double)posInWorld.y()), Mth.floor((double)posInWorld.z()));
        Vec2 tfcWind = Climate.get(level).getWind(level, samplePos);
        double magnitude = Math.sqrt(tfcWind.x * tfcWind.x + tfcWind.y * tfcWind.y);
        if (magnitude < 1.0E-4) {
            ship.getDragController().setWindSpeed(0.0, "tfc_item_barrels_and_more");
            return;
        }
        double dirX = (double)tfcWind.x / magnitude;
        double dirZ = (double)tfcWind.y / magnitude;
        ship.getDragController().setWindDirection(dirX, 0.0, dirZ, "tfc_item_barrels_and_more");
        ship.getDragController().setWindSpeed(magnitude * speedScale, "tfc_item_barrels_and_more");
    }
}

