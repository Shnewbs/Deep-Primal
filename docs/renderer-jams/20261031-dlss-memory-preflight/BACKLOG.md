# Renderer jam 70 follow-up backlog

1. **P0 owner build:** Java25 compile of production DLSS provider, contract and test; full Gradle/NeoForge and existing nativeChecks. No JAR claimed here.
2. **P0 real provenance:** renderer allocation owner must supply actual VkDeviceMemory, offset and size for color/depth/motion/output. Reject absent/overlapping ranges before JNI; validate against Vulkan binding calls, not just caller metadata.
3. **P0 temporal renderer:** full-scene depth and moving-object motion vectors, jitter, layouts/barriers, fence-safe lifetimes, resize/reload/world switch and RTX NGX output.
4. **P1 matched baselines:** named hardware Vulkan/OpenGL p50/p95/p99, CPU/GPU, culling/rebuild/upload, draw/descriptor, RAM/VRAM, water and LOD on/off, travel/edit and cold/warm.
5. **P1 LOD:** owner Java25 compile of jam69's repaired disk index and its focused test; native travel/edit/reopen correctness.
