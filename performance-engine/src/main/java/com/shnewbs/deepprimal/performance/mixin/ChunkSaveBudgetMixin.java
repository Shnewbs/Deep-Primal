package com.shnewbs.deepprimal.performance.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.shnewbs.deepprimal.performance.PerformanceConfig;
import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bound snapshots plus encoding/writes, including pause/shutdown saves that bypass eager-save
 * limits.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkSaveBudgetMixin {
  @Shadow @Final private AtomicInteger activeChunkWrites;
  @Shadow @Final private BlockableEventLoop<Runnable> mainThreadExecutor;

  @ModifyConstant(method = "saveChunksEagerly", constant = @Constant(intValue = 128))
  private int deepprimal$eagerLimit(int nativeLimit) {
    return PerformanceConfig.BOUND_SAVES.get() ? PerformanceConfig.SAVE_WRITES.get() : nativeLimit;
  }

  @Inject(method = "save", at = @At("HEAD"))
  private void deepprimal$admitSave(ChunkAccess chunk, CallbackInfoReturnable<Boolean> callback) {
    if (PerformanceConfig.BOUND_SAVES.get()
        && activeChunkWrites.get() >= PerformanceConfig.SAVE_WRITES.get()) {
      RuntimeMetrics.SAVE_BACKPRESSURE.increment();
      // Wait before copyOf/tryMarkSaved. Native completion releases the slot; no save is dropped.
      mainThreadExecutor.managedBlock(
          () -> activeChunkWrites.get() < PerformanceConfig.SAVE_WRITES.get());
    }
  }

  @Inject(
      method = "save",
      at =
          @At(
              value = "INVOKE",
              target = "Ljava/util/concurrent/atomic/AtomicInteger;incrementAndGet()I",
              shift = At.Shift.AFTER))
  private void deepprimal$writeAcquired(
      ChunkAccess chunk,
      CallbackInfoReturnable<Boolean> callback,
      @Share("snapshotAcquired") LocalBooleanRef acquired) {
    acquired.set(true);
  }

  @Inject(
      method = "save",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ljava/util/concurrent/CompletableFuture;handle(Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;",
              shift = At.Shift.AFTER))
  private void deepprimal$writeHandedOff(
      ChunkAccess chunk,
      CallbackInfoReturnable<Boolean> callback,
      @Share("snapshotAcquired") LocalBooleanRef acquired) {
    acquired.set(false);
  }

  @Inject(
      method = "save",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/MinecraftServer;reportChunkSaveFailure(Ljava/lang/Throwable;Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Lnet/minecraft/world/level/ChunkPos;)V"))
  private void deepprimal$failedSnapshot(
      ChunkAccess chunk,
      CallbackInfoReturnable<Boolean> callback,
      @Share("snapshotAcquired") LocalBooleanRef acquired) {
    // Native code increments before copyOf; an exception before the future exists must release that
    // slot.
    if (acquired.get()) {
      activeChunkWrites.decrementAndGet();
      acquired.set(false);
    }
  }

  @Inject(method = "save", at = @At("RETURN"))
  private void deepprimal$saveWatermark(
      ChunkAccess chunk, CallbackInfoReturnable<Boolean> callback) {
    RuntimeMetrics.MAX_PENDING_WRITES.accumulateAndGet(activeChunkWrites.get(), Math::max);
  }
}
