package tfcmt.tfcitembarrelsandmore.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.RotationNetworkManager;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class RotationNetworkWalker {
    private RotationNetworkWalker() {
    }

    public static List<SinkNode> findSinks(Level level, BlockPos startPos, long networkId) {
        RotationNetworkManager manager = RotationNetworkManager.get((Level)level);
        ArrayList<SinkNode> sinks = new ArrayList<SinkNode>();
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        visited.add(startPos);
        queue.add(startPos);
        while (!queue.isEmpty()) {
            BlockPos pos = (BlockPos)queue.poll();
            Node node = manager.getNode(pos);
            if (node == null || node.network() != networkId) continue;
            if (node instanceof SinkNode) {
                SinkNode sinkNode = (SinkNode)node;
                if (!pos.equals((Object)startPos)) {
                    sinks.add(sinkNode);
                }
            }
            for (Direction dir : node.connections()) {
                BlockPos neighborPos = pos.relative(dir);
                if (!visited.add(neighborPos)) continue;
                queue.add(neighborPos);
            }
        }
        return sinks;
    }
}

