package com.shnewbs.deepprimal.settings.mixin.client;

import com.shnewbs.deepprimal.core.SyncRebuildBudget;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import com.shnewbs.deepprimal.settings.VisualPerformance;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep native task creation, region snapshots, worker ownership and upload callbacks. */
@Mixin(LevelRenderer.class)
public abstract class ChunkRebuildMixin {
  @Unique private final SyncRebuildBudget deepprimal$rebuildBudget = new SyncRebuildBudget();

  @Inject(method = "compileSections", at = @At("HEAD"))
  private void deepprimal$resetBudget(Camera camera, CallbackInfo callback) {
    deepprimal$rebuildBudget.reset(
        ClientPerformanceConfig.SYNC_REBUILDS.get(),
        ClientPerformanceConfig.SYNC_REBUILD_MICROS.get() * 1000L);
  }

  @Redirect(
      method = "compileSections",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;rebuildSectionSync(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;Lnet/minecraft/client/renderer/chunk/RenderRegionCache;)V"))
  private void deepprimal$paceRebuild(
      SectionRenderDispatcher dispatcher,
      SectionRenderDispatcher.RenderSection section,
      RenderRegionCache cache) {
    boolean playerEdit = section.isDirtyFromPlayer();
    if (ClientPerformanceConfig.PACE_REBUILDS.get()
        && !deepprimal$rebuildBudget.allows(playerEdit)) {
      section.rebuildSectionAsync(cache);
      VisualPerformance.chunkRebuildsDeferred++;
      return;
    }
    long start = System.nanoTime();
    try {
      dispatcher.rebuildSectionSync(section, cache);
    } finally {
      long elapsed = System.nanoTime() - start;
      deepprimal$rebuildBudget.completed(elapsed);
      VisualPerformance.chunkSyncBuilds++;
      VisualPerformance.chunkSyncNanos += elapsed;
      if (playerEdit) VisualPerformance.chunkPlayerBuilds++;
    }
  }
}
