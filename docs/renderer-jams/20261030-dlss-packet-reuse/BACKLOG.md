# Jam 47 next renderer-only priorities

1. **P0 hardware:** owner Vulkan/OpenGL screenshots and CPU/GPU p50/p95/p99, RAM/VRAM and water pixels for LOD two-back on/off, FSR1/native, camera travel/edit and world/resize/reload; detect mixed-page tails. Capture actual hardware/API/driver and saved setting budgets.
2. **P0 DLSS correctness:** the reused Java packet does not validate disjoint VkDeviceMemory, temporal world-depth, camera plus moving-object motion vectors or submission barriers. Implement those resource lifetimes and capability gates before claiming NGX is active. Keep native fallback honest.
3. **P1 FSR:** FSR1 spatial shipped; temporal FSR3/4 and RTX DLSS remain gated by explicit vendor SDK licensing/package/runtime/GPU tests. Frame generation remains separate.
4. **P1 distance:** verify actual unseen-terrain delivery, persisted requested/effective caps and host tracker admission before increasing useful distances. Do not modify water simulation, terrain generation or other feature claims.
5. Owner performs Java25/full Gradle/Minecraft/GPU/JAR; do not label source-evidence ZIP a testable JAR.
