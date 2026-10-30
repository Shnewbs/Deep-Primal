# Renderer jam 52 — DLSS memory provenance handoff

## Scope
Renderer-only. This jam consumes jam 51's finding that the native contract could validate a memory-range helper but had no authoritative provenance inputs.

## Implementation
- DlssFrame.Image now carries optional MemoryBinding provenance with a legacy constructor that marks provenance unavailable.
- DlssPackedFrame reuses a fixed 12-long packet for four (VkDeviceMemory, offset, size) triples.
- DlssNative.evaluate and DlssVulkanProvider forward the provenance packet without per-frame allocation.
- deepprimal_dlss.cpp rejects missing/overflowing/overlapping bindings before NGX resource creation/evaluation.

Provenance-unavailable frames remain rejected by the native path; DLSS is not advertised as active.

## Validation
Local native-free fixture compiled and executed:
g++ -std=c++17 -Wall -Wextra -Werror -pedantic ... && ./check
Result: PASS assertions=200006.

Not run: Java 25/Gradle/NeoForge, production JNI/NGX build, Vulkan/OpenGL client, GPU pixels/frame time/VRAM, water/lifecycle, packaging/JAR/release.
