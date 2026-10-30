# Renderer jam 59 — duplicate-key fast path

Renderer-only LOD discovery optimization. No engine, water, automation, port, or unrelated work changed.

## Implementation
In `LodDiskNearestScan.Selection.accept(int,int)`, the packed page key is now probed against `retained` before distance arithmetic and `RankedKey` allocation. Periodic saved-page rediscovery commonly encounters already-retained keys; duplicates now exit through one hash probe. Ranking, tie-breaks, MAX_KEYS admission, water ordering, Vulkan/OpenGL paths, and FSR/DLSS behavior are unchanged.

Pinned implementation commit: https://github.com/Shnewbs/Deep-Primal/commit/d9093ff9966f73cb9e9fba77a08e7be954070ca0

## Validation
- Current `main` re-read before edit and after integration.
- Production file update applied with current blob SHA guard.
- Source-only scope review completed.
- Full Java25/Gradle/NeoForge, native Vulkan/OpenGL, GPU timings/VRAM, water/lifecycle, FSR/DLSS dispatch, and packaged JAR/release gates were not run under the owner override.
