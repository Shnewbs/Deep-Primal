package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.DistanceManager;

/** Short actual 96-chunk single-player session on the crash seed, not a full-scene memory stress test. */
public final class QueueStartupChecks {
    private static int started, stage, changed;
    private static long entered;
    private static String waitingScreen;
    private static net.minecraft.client.gui.screens.Screen acknowledged;
    public static void tick(Minecraft mc) throws Exception {
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
            String current = mc.screen == null ? "none" : mc.screen.getClass().getName() + " " + mc.screen.getTitle().getString();
            if (!current.equals(waitingScreen)) {
                waitingScreen = current;
                System.out.println("QUEUE_LIVE_WAITING screen=" + current);
            }
            // This opt-in harness opens only its disposable test save.
            if (mc.screen != acknowledged && mc.screen instanceof BackupConfirmScreen && mc.screen.getTitle().getString()
                .equals(Component.translatable("selectWorld.backupQuestion.experimental").getString())) {
                String skip = Component.translatable("selectWorld.backupJoinSkipButton").getString();
                for (var child : mc.screen.children())
                    if (child instanceof Button button && button.getMessage().getString().equals(skip)) {
                        acknowledged = mc.screen;
                        button.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0)); break;
                    }
            }
            return;
        }
        var server = mc.getSingleplayerServer();
        if (entered == 0) {
            entered = System.nanoTime(); started = server.getTickCount();
            check(DistanceRuntime.trackingCapacity() == 96, "integrated tracking capacity");
            check(mc.options.renderDistance().get() == 96, "requested render distance");
            var view = ((LevelRendererAccessor)mc.levelRenderer).deepprimal$viewArea();
            check(view.getViewDistance() == 96, "actual native render area");
            var chunkMapField = server.overworld().getChunkSource().getClass().getDeclaredField("chunkMap"); chunkMapField.setAccessible(true);
            var manager = ((ChunkMap)chunkMapField.get(server.overworld().getChunkSource())).getDistanceManager();
            var dispatcherField = DistanceManager.class.getDeclaredField("ticketDispatcher"); dispatcherField.setAccessible(true);
            var queueField = ChunkTaskDispatcher.class.getDeclaredField("queue"); queueField.setAccessible(true);
            var queue = queueField.get(dispatcherField.get(manager));
            var buckets = ChunkTaskPriorityQueue.class.getDeclaredField("queuesPerPriority"); buckets.setAccessible(true);
            check(((List<?>)buckets.get(queue)).size() == 99, "live player-ticket queue capacity");
            System.out.println("QUEUE_LIVE_STARTED render=96 tracker=96 buckets=99 sections=" + view.sections.length);
        }
        int ticks = server.getTickCount() - started;
        if (System.nanoTime() - entered > 150_000_000_000L) throw new AssertionError("96-chunk session timed out");
        if (stage == 0 && ticks >= 60) {
            server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "execute as @a at @s run tp @s ~64 ~ ~64"));
            stage = 1; changed = server.getTickCount();
            System.out.println("QUEUE_LIVE_MOVED ticks=" + ticks);
        } else if (stage == 1 && server.getTickCount() - changed >= 60) {
            mc.options.renderDistance().set(8); stage = 2; changed = server.getTickCount();
            System.out.println("QUEUE_LIVE_REDUCED requested=8 tracker=" + DistanceRuntime.trackingCapacity());
        } else if (stage == 2 && server.getTickCount() - changed >= 80) {
            check(server.getPlayerList().getViewDistance() == 8, "live server distance reduction");
            check(DistanceRuntime.trackingCapacity() == 96, "session capacity remains allocated during release");
            System.out.println("QUEUE_LIVE_PASS ticks=" + ticks + " loaded=" + server.overworld().getChunkSource().getLoadedChunksCount()
                + " heapMiB=" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
            mc.stop(); stage = 3;
        }
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
