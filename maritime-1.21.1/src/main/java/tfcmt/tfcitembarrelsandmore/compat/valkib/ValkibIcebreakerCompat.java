package tfcmt.tfcitembarrelsandmore.compat.valkib;

import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;

public final class ValkibIcebreakerCompat {
    private static final TagKey<Block> ICEBREAKER_TAG = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "icebreaker"));
    private static final TagKey<Block> VALKIB_BREAKABLES_TAG = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("valkib", "breakables"));
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!((Boolean)TIBAMConfig.COMMON.icebreakerEnabled.get()).booleanValue()) {
            return;
        }
        if (++this.tickCounter < (Integer)TIBAMConfig.COMMON.icebreakerScanIntervalTicks.get()) {
            return;
        }
        this.tickCounter = 0;
        MinecraftServer server = event.getServer();
        double radius = (Double)TIBAMConfig.COMMON.icebreakerPlayerSearchRadius.get();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            HashSet<Long> scannedShipIds = new HashSet<Long>();
            for (ServerPlayer player : level.players()) {
                AABB searchArea = player.getBoundingBox().inflate(radius);
                for (Ship ship : VSCompat.getShipsIntersecting((Level)level, (AABB)searchArea)) {
                    if (!scannedShipIds.add(ship.getId())) continue;
                    this.scanShip(level, ship);
                }
            }
        }
    }

    private void scanShip(ServerLevel level, Ship ship) {
        AABBic voxelAABB = ship.getShipAABB();
        if (voxelAABB == null) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = voxelAABB.minX(); x <= voxelAABB.maxX(); ++x) {
            for (int y = voxelAABB.minY(); y <= voxelAABB.maxY(); ++y) {
                for (int z = voxelAABB.minZ(); z <= voxelAABB.maxZ(); ++z) {
                    pos.set(x, y, z);
                    if (!level.getBlockState((BlockPos)pos).is(ICEBREAKER_TAG)) continue;
                    this.breakNearbyIce(level, ship, (BlockPos)pos);
                }
            }
        }
    }

    private void breakNearbyIce(ServerLevel level, Ship ship, BlockPos shipPos) {
        org.joml.Vector3d worldVector = ship.getShipToWorld().transformPosition(new org.joml.Vector3d(shipPos.getX() + 0.5, shipPos.getY() + 0.5, shipPos.getZ() + 0.5));
            Vec3 worldCenter = new Vec3(worldVector.x, worldVector.y, worldVector.z);
        BlockPos center = new BlockPos(Mth.floor((double)worldCenter.x), Mth.floor((double)worldCenter.y), Mth.floor((double)worldCenter.z));
        int r = (Integer)TIBAMConfig.COMMON.icebreakerBreakRadius.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dy = -r; dy <= r; ++dy) {
                for (int dz = -r; dz <= r; ++dz) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.getBlockState((BlockPos)pos).is(VALKIB_BREAKABLES_TAG)) continue;
                    level.destroyBlock((BlockPos)pos, false);
                }
            }
        }
    }
}

