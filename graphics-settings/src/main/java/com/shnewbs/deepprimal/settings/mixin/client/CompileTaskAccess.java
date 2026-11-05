package com.shnewbs.deepprimal.settings.mixin.client;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionRenderDispatcher.RenderSection.CompileTask.class)
public interface CompileTaskAccess {
  @Accessor("isCompleted")
  AtomicBoolean deepprimal$completed();

  @Accessor("isCancelled")
  AtomicBoolean deepprimal$cancelled();
}
