package com.shnewbs.deepprimal.settings.mixin.client;

import com.shnewbs.deepprimal.settings.VisualPerformance;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Finish a coherent snapshot before compiling the latest non-player changes. */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class SectionRebuildMixin {
  @Unique private SectionRenderDispatcher.RenderSection.CompileTask deepprimal$building;
  @Unique private boolean deepprimal$keepDirty;

  @Inject(method = "rebuildSectionAsync", at = @At("HEAD"), cancellable = true)
  private void deepprimal$coalesce(RenderRegionCache cache, CallbackInfo ci) {
    if (deepprimal$building == null
        || ((SectionRenderDispatcher.RenderSection) (Object) this).isDirtyFromPlayer()) return;
    var task = (CompileTaskAccess) deepprimal$building;
    if (!task.deepprimal$completed().get() && !task.deepprimal$cancelled().get()) {
      deepprimal$keepDirty = true;
      VisualPerformance.chunkRebuildsCoalesced++;
      ci.cancel();
    }
  }

  @Inject(method = "setNotDirty", at = @At("HEAD"), cancellable = true)
  private void deepprimal$retainChanges(CallbackInfo ci) {
    if (deepprimal$keepDirty) {
      deepprimal$keepDirty = false;
      ci.cancel();
    }
  }

  @Inject(method = "createCompileTask", at = @At("RETURN"))
  private void deepprimal$track(
      RenderRegionCache cache,
      CallbackInfoReturnable<SectionRenderDispatcher.RenderSection.CompileTask> ci) {
    deepprimal$building = ci.getReturnValue();
  }

  @Inject(method = "compileSync", at = @At("RETURN"))
  private void deepprimal$syncFinished(RenderRegionCache cache, CallbackInfo ci) {
    deepprimal$building = null;
  }

  @Inject(method = "cancelTasks", at = @At("HEAD"))
  private void deepprimal$cancelled(CallbackInfo ci) {
    deepprimal$building = null;
    deepprimal$keepDirty = false;
  }
}
