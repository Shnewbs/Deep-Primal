package com.shnewbs.deepprimal.render;

import com.shnewbs.deepprimal.core.RendererPolicy;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.fml.loading.FMLLoader;

/** Safe during mixin selection; no Minecraft client classes or renderer initialization. */
public final class RendererOwnership {
    private static final class Detection {
        static final Set<String> MODS = FMLLoader.getCurrent().getLoadingModList().getMods().stream()
            .map(m -> m.getModId()).collect(Collectors.toUnmodifiableSet());
        static final Set<String> REPLACEMENTS = MODS.stream().filter(RendererPolicy.REPLACEMENTS::contains)
            .collect(Collectors.toUnmodifiableSet());
        static final boolean OWNED = REPLACEMENTS.isEmpty() && !Boolean.getBoolean("deepprimal.renderer.disabled");
    }
    public static boolean installed(String id) { return Detection.MODS.contains(id); }
    public static boolean owned() { return Detection.OWNED; }
    public static String owner() { return owned() ? "Deep Primal" : Detection.REPLACEMENTS.isEmpty() ? "External / disabled" : String.join(", ", new java.util.TreeSet<>(Detection.REPLACEMENTS)); }
    private RendererOwnership() {}
}
