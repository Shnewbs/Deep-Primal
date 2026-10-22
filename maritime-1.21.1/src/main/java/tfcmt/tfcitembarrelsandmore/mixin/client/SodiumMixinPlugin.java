package tfcmt.tfcitembarrelsandmore.mixin.client;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class SodiumMixinPlugin
implements IMixinConfigPlugin {
    private static final boolean SDL_PRESENT;

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return SDL_PRESENT;
    }

    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    static {
        String classResourcePath = "toni/sodiumdynamiclights/SodiumDynamicLights.class";
        SDL_PRESENT = SodiumMixinPlugin.class.getClassLoader().getResource(classResourcePath) != null;
    }
}

