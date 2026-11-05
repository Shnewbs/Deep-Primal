package com.shnewbs.deepprimal.validation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.BitSet;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Conservative connected-component scan of actual generated rock, dev runtime only. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class TerrainServerChecks {
    public TerrainServerChecks() { NeoForge.EVENT_BUS.register(this); }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rockscan").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer())
                .executes(context -> {
                    int cx = IntegerArgumentType.getInteger(context, "x"), cz = IntegerArgumentType.getInteger(context, "z");
                    var level = context.getSource().getLevel();
                    final int side = 48, bottom = 192, layers = 448, plane = side * side, size = plane * layers;
                    var rock = new BitSet(size); var connected = new BitSet(size); int[] queue = new int[size];
                    var pos = new BlockPos.MutableBlockPos(); int total = 0, tail = 0;
                    for (int y = 0; y < layers; y++) for (int z = 0; z < side; z++) for (int x = 0; x < side; x++) {
                        var state = level.getBlockState(pos.set(cx - side / 2 + x, bottom + y, cz - side / 2 + z));
                        if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !state.is(Blocks.SNOW_BLOCK)) continue;
                        int index = y * plane + z * side + x; rock.set(index); total++;
                        if (x == 0 || x == side - 1 || z == 0 || z == side - 1 || y == 0 || y == layers - 1) {
                            connected.set(index); queue[tail++] = index;
                        }
                    }
                    flood(rock, connected, queue, tail, side, layers);
                    var floating = (BitSet)rock.clone(); floating.andNot(connected);
                    int floatingBlocks = floating.cardinality(), components = 0, largest = 0;
                    while (!floating.isEmpty()) {
                        int first = floating.nextSetBit(0); var seen = new BitSet(size); seen.set(first); queue[0] = first;
                        int count = flood(floating, seen, queue, 1, side, layers);
                        largest = Math.max(largest, count); components++; floating.andNot(seen);
                    }
                    String message = "ROCKSCAN x=" + cx + " z=" + cz + " solid=" + total + " floatingBlocks=" + floatingBlocks
                        + " floatingComponents=" + components + " largest=" + largest;
                    context.getSource().sendSuccess(() -> Component.literal(message), false); return 1;
                }))));
    }
    private static int flood(BitSet rock, BitSet seen, int[] queue, int tail, int side, int layers) {
        int plane = side * side;
        for (int head = 0; head < tail; head++) {
            int p = queue[head], y = p / plane, z = (p % plane) / side, x = p % side;
            if (x > 0) tail = add(p - 1, rock, seen, queue, tail);
            if (x + 1 < side) tail = add(p + 1, rock, seen, queue, tail);
            if (z > 0) tail = add(p - side, rock, seen, queue, tail);
            if (z + 1 < side) tail = add(p + side, rock, seen, queue, tail);
            if (y > 0) tail = add(p - plane, rock, seen, queue, tail);
            if (y + 1 < layers) tail = add(p + plane, rock, seen, queue, tail);
        }
        return tail;
    }
    private static int add(int p, BitSet rock, BitSet seen, int[] queue, int tail) {
        if (rock.get(p) && !seen.get(p)) { seen.set(p); queue[tail++] = p; }
        return tail;
    }
}
