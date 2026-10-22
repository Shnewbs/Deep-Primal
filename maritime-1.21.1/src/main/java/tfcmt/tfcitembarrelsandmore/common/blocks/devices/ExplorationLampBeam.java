package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;

public final class ExplorationLampBeam {
    private static final int BEAM_LENGTH = 3;
    private static final Map<GlobalPos, GlobalPos> OWNED_LIGHTS = new ConcurrentHashMap<GlobalPos, GlobalPos>();

    private ExplorationLampBeam() {
    }

    public static void sync(ServerLevel level, BlockPos lampPos, BlockState newLampState) {
        ExplorationLampBeam.removeOwnedBeam(level, lampPos);
        if (!ExplorationLampBeam.qualifiesForBeam(newLampState)) {
            return;
        }
        Direction facing = (Direction)newLampState.getValue((Property)ExplorationLampBlock.FACING);
        BlockPos target = ExplorationLampBeam.resolveBeamTarget(level, lampPos, facing);
        if (target == null) {
            return;
        }
        ExplorationLampBeam.placeGhostLight(level, target, lampPos);
    }

    public static void onLampRemoved(ServerLevel level, BlockPos lampPos) {
        ExplorationLampBeam.removeOwnedBeam(level, lampPos);
    }

    private static void removeOwnedBeam(ServerLevel level, BlockPos lampPos) {
        GlobalPos lampGlobal = GlobalPos.of((ResourceKey)level.dimension(), (BlockPos)lampPos);
        GlobalPos owned = null;
        for (Map.Entry<GlobalPos, GlobalPos> entry : OWNED_LIGHTS.entrySet()) {
            if (!entry.getValue().equals((Object)lampGlobal)) continue;
            owned = entry.getKey();
            break;
        }
        if (owned == null) {
            return;
        }
        OWNED_LIGHTS.remove(owned);
        ExplorationLampBeam.removeGhostLightIfPresent(level, owned.pos());
    }

    private static boolean qualifiesForBeam(BlockState lampState) {
        return (Boolean)lampState.getValue((Property)ExplorationLampBlock.LIT) != false && lampState.getValue(ExplorationLampBlock.MODE) == ExplorationLampBlockEntity.Mode.SEARCH;
    }

    private static BlockPos resolveBeamTarget(ServerLevel level, BlockPos lampPos, Direction facing) {
        BlockPos lastClear = null;
        for (int d = 1; d <= 3; ++d) {
            BlockPos cell = lampPos.relative(facing, d);
            if (!ExplorationLampBeam.isClearForLight(level, cell)) {
                BlockPos above = cell.above();
                if (ExplorationLampBeam.isClearForLight(level, above)) {
                    return above;
                }
                return lastClear;
            }
            lastClear = cell;
        }
        return lastClear;
    }

    private static boolean isClearForLight(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.LIGHT);
    }

    private static void placeGhostLight(ServerLevel level, BlockPos target, BlockPos lampPos) {
        BlockState existing = level.getBlockState(target);
        if (!existing.isAir() && !existing.is(Blocks.LIGHT)) {
            return;
        }
        BlockState lightState = (BlockState)((BlockState)Blocks.LIGHT.defaultBlockState().setValue((Property)BlockStateProperties.LEVEL, (Comparable)Integer.valueOf(15))).setValue((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(false));
        level.setBlock(target, lightState, 3);
        OWNED_LIGHTS.put(GlobalPos.of((ResourceKey)level.dimension(), (BlockPos)target), GlobalPos.of((ResourceKey)level.dimension(), (BlockPos)lampPos));
    }

    private static void removeGhostLightIfPresent(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}

