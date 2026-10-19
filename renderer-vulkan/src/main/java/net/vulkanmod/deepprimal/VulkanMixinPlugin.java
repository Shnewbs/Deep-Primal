package net.vulkanmod.deepprimal;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
public final class VulkanMixinPlugin implements IMixinConfigPlugin {
    public boolean shouldApplyMixin(String target, String mixin) { return Startup.enabled(); }
    public void onLoad(String name) {}
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> own, Set<String> other) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
