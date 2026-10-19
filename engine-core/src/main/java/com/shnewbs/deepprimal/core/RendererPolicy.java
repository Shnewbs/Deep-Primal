package com.shnewbs.deepprimal.core;

import java.util.Set;

/** Renderer ownership is independent of server/worldgen optimizations. */
public final class RendererPolicy {
    public static final Set<String> REPLACEMENTS = Set.of("sodium", "embeddium", "rubidium", "vulkanmod", "nvidium", "iris", "oculus", "distanthorizons");
    public static boolean ownsRenderer(Set<String> installed) { return installed.stream().noneMatch(REPLACEMENTS::contains); }
    private RendererPolicy() {}
}
