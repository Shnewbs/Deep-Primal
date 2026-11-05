# Renderer jam 51 — DLSS memory-binding provenance contract

This jam adds a native-free, allocation-free contract helper for the four Vulkan
DLSS resources: scene color, scene depth, motion vectors, and output. It rejects
zero/overflowing bindings and overlapping ranges within the same VkDeviceMemory
allocation, while allowing adjacent ranges and distinct allocations.

The helper is deliberately **not wired into NGX dispatch yet** because the current
JNI evaluation API receives only VkImage/VkImageView handles and format arrays. A
future renderer integration must pass authoritative binding provenance from the
Vulkan allocation owner before enabling DLSS. This jam therefore does not claim
active DLSS, GPU pixels, or memory safety in the live renderer.

The source change is confined to renderer-vulkan native DLSS contract/test files
and this dedicated evidence path. No LOD, water, terrain, settings, GUI, or
other scheduler files are modified.
