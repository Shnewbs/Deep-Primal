# Renderer jam 50 — value-equal LOD page fast path

Source-only renderer increment on current main.

## Implementation

LodPublishedBounds.populate now detects pages whose independently allocated AABBs are value-equal, not just identity-shared. It validates the representative and remaining parts once, reuses the representative as the conservative page marker, and builds the existing adjacent-repeat mask without union min/max reductions.

This is limited to resident LOD publication metadata. Geometry, draw order, transparent-water behavior, Vulkan/OpenGL backend code, settings persistence, and DLSS/FSR dispatch are unchanged.

## Validation

Owner override remains authoritative: no full Java25/Gradle/NeoForge/native/GPU/JAR claims are made in this run. The source change is intentionally small and read-only audited after integration.

## Remaining gates

Need an isolated Java 21/25 compile of LodPublishedBounds with the real module dependencies, synthetic equal-vs-mixed publication timing, and owner Vulkan/OpenGL checks before any performance grade increase.
