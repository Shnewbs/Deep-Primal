package tfcmt.tfcitembarrelsandmore.common;

import java.util.HashMap;
import java.util.Map;
import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TollTier;

public final class SinkTollRegistry {
    private static Map<Block, TollTier> overrides;

    private SinkTollRegistry() {
    }

    private static Map<Block, TollTier> overrides() {
        if (overrides == null) {
            HashMap<Block, TollTier> map = new HashMap<Block, TollTier>();
            map.put((Block)TIBAMBlocks.MECHANICAL_POWER_HAMMER.get(), TollTier.MEDIUM);
            map.put((Block)TIBAMBlocks.PNEUMATIC_POWER_HAMMER.get(), TollTier.HEAVY);
            map.put((Block)TIBAMBlocks.TWO_BLADE_PROPELLER.get(), TollTier.MEDIUM);
            map.put((Block)TIBAMBlocks.FOUR_BLADE_PROPELLER.get(), TollTier.HEAVY);
            for (DeferredHolder<Block, Block> entry : TIBAMBlocks.WATER_PADDLES.values()) {
                map.put((Block)entry.get(), TollTier.HEAVY);
            }
            overrides = map;
        }
        return overrides;
    }

    public static TollTier getTier(Block block) {
        return SinkTollRegistry.overrides().getOrDefault(block, TollTier.LIGHT);
    }

    public static boolean isWorking(Level level, BlockPos pos, BlockEntity be, Node node) {
        if (be instanceof ITollableSink) {
            ITollableSink tollable = (ITollableSink)be;
            return tollable.isTollableWorking();
        }
        if (be instanceof QuernBlockEntity) {
            QuernBlockEntity quern = (QuernBlockEntity)be;
            return quern.isGrinding();
        }
        Rotation rotation = node.rotation();
        return rotation != null && rotation.speed() != 0.0f;
    }
}

