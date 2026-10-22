package tfcmt.tfcitembarrelsandmore.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import tfcmt.tfcitembarrelsandmore.common.BraceData;

public class BraceClientCache {
    private static final Map<BlockPos, BraceData> CACHE = new HashMap<BlockPos, BraceData>();

    public static void put(BlockPos pos, BraceData data) {
        CACHE.put(pos.immutable(), data);
    }

    public static void remove(BlockPos pos) {
        CACHE.remove(pos);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static BraceData get(BlockPos pos) {
        return CACHE.get(pos);
    }
}

