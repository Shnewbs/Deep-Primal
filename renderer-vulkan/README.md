# Deep Primal Vulkan adapter

LGPL-3.0-only adaptation of VulkanMod 0.6.8+26.1.2 by Collateral and contributors. See LICENSE, COPYING, UPSTREAM.json and [integration documentation](../docs/VULKAN-BACKPORT.md).

Build this module as part of the root Java 25/NeoForge build: `./gradlew build`. The resulting nested adapter JAR remains separately replaceable/rebuildable. Its sources are under `src/main/java`; source shaders and startup/mixin metadata are under `src/main/resources`. The module bundles LWJGL Vulkan/shaderc/VMA bindings and their required platform natives, not LWJGL core/GLFW or Minecraft classes.

The native Minecraft chunk mesher is retained. This directory excludes Fabric API/Loader and VulkanMod's custom terrain renderer, its configuration screens and its unrelated gameplay/mixin optimizations. Authored bootstrap, recovery, copy/query/stencil and native API adaptations are included with the matching reconstructed LGPL backend source.
