package com.shnewbs.deepprimal.performance.mixin;

import com.shnewbs.deepprimal.core.NearestChunks;
import com.shnewbs.deepprimal.performance.PerformanceConfig;
import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {
  @Shadow @Final private LongSet pendingChunks;
  @Shadow private float batchQuota;
  @Unique private final NearestChunks deepprimal$nearest = new NearestChunks(64);

  @Inject(method = "collectChunksToSend", at = @At("HEAD"), cancellable = true)
  private void deepprimal$boundedSelection(
      ChunkMap map, ChunkPos center, CallbackInfoReturnable<List<LevelChunk>> callback) {
    if (!PerformanceConfig.OPTIMIZE_CHUNK_SENDS.get()) return;
    long started = System.nanoTime();
    int limit = Math.min((int) batchQuota, PerformanceConfig.MAX_CHUNK_SENDS.get());
    if (limit < 1) {
      callback.setReturnValue(List.of());
      return;
    }
    deepprimal$nearest.reset(limit, center.x(), center.z());
    var iterator = pendingChunks.iterator();
    // Only ready chunks compete for the bounded send quota. Selecting pending-but-
    // unavailable near chunks first can otherwise starve ready farther chunks.
    while (iterator.hasNext()) {
      long position = iterator.nextLong();
      if (map.getChunkToSend(position) != null) deepprimal$nearest.offer(position);
    }
    deepprimal$nearest.sort();
    var ready = new ArrayList<LevelChunk>(deepprimal$nearest.size());
    int candidates = pendingChunks.size();
    for (int i = 0; i < deepprimal$nearest.size(); i++) {
      long position = deepprimal$nearest.position(i);
      LevelChunk chunk = map.getChunkToSend(position);
      if (chunk != null) {
        ready.add(chunk);
        pendingChunks.remove(position);
      }
    }
    RuntimeMetrics.MAX_SEND_BATCH.accumulateAndGet(ready.size(), Math::max);
    RuntimeMetrics.SEND_SELECTIONS.increment();
    RuntimeMetrics.SEND_CANDIDATES.add(candidates);
    RuntimeMetrics.SEND_NANOS.add(System.nanoTime() - started);
    callback.setReturnValue(ready);
  }
}
