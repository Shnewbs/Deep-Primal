package com.shnewbs.deepprimal.performance.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shnewbs.deepprimal.core.DistancePolicy;
import com.shnewbs.deepprimal.performance.DistanceRuntime;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Each queue keeps one capacity for allocation, reprioritization, and empty-state checks. */
@Mixin(ChunkTaskPriorityQueue.class)
public abstract class ChunkTaskPriorityQueueMixin {
  @Unique private int deepprimal$priorityCount;

  @ModifyExpressionValue(
      method = "<init>",
      at =
          @At(
              value = "FIELD",
              target = "Lnet/minecraft/server/level/ChunkTaskPriorityQueue;PRIORITY_LEVEL_COUNT:I"))
  private int deepprimal$allocatePriorities(int nativeCount) {
    if (deepprimal$priorityCount == 0)
      deepprimal$priorityCount =
          DistancePolicy.queuePriorityCount(nativeCount, DistanceRuntime.trackingCapacity());
    return deepprimal$priorityCount;
  }

  @ModifyExpressionValue(
      method = {"resortChunkTasks", "hasWork"},
      at =
          @At(
              value = "FIELD",
              target = "Lnet/minecraft/server/level/ChunkTaskPriorityQueue;PRIORITY_LEVEL_COUNT:I"))
  private int deepprimal$queueBounds(int nativeCount) {
    return deepprimal$priorityCount;
  }
}
