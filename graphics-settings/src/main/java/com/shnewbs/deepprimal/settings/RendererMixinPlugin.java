package com.shnewbs.deepprimal.settings;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Yield before overlapping bytecode is transformed, not after competing renderers initialize. */
public final class RendererMixinPlugin implements IMixinConfigPlugin {
  @Override
  public boolean shouldApplyMixin(String target, String mixin) {
    if (mixin.endsWith("FrameLimitMixin")) return !RendererOwnership.installed("dynamic_fps");
    if (!mixin.contains(".mixin.client.") || mixin.endsWith("VideoSettingsMixin")) return true;
    return RendererOwnership.owned();
  }

  @Override
  public void onLoad(String packageName) {}

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public void acceptTargets(Set<String> own, Set<String> other) {}

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}

  @Override
  public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
