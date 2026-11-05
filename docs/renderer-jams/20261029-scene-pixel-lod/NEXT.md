# Renderer jam 31 follow-up backlog

1. Owner Java25 Gradle/NeoForge and packaged native OpenGL/Vulkan identical-camera scenes: native 1080p, FSR1 Quality 720p scene, half-resolution spatial. Verify lodErrorViewportHeightPixels / lodErrorWorldTargetPresent at extraction and reset; compare LOD silhouettes, negative seams, near/full-detail handoff and transparent water pixels.
2. Capture named GPU/driver/scene/seed, camera/travel/edit, CPU/GPU frame p50/p95/p99, draw/descriptor count, upload/scheduling/culling, warm/cold RAM/VRAM and resident/pending/cache budgets; run fixed/adaptive/off page broadphase comparisons and record requested/effective full-detail, vertical, LOD and independent simulation distances.
3. If profiled, optimize mesh reuse, bounded worker/upload, actual supported GPU submission and fence-safe allocation. A synchronous drawMultipleIndexed API call is not GPU multi-draw indirect.
4. Preserve V1–V7, player edits, lighting, translucent water. Do not advertise unexplored remote terrain without an authoritative delivery contract and producer/network prerequisites.
5. Temporal FSR and real NVIDIA DLSS SR still need correct jitter, preserved world depth, moving-object motion vectors, temporal history, vendor dispatch, capabilities/runtime packaging/licensing. FSR1 is spatial; frame generation is separate.

Jam source [cf578b2e36fcdbde04320cc2564c5b1cd89004d6](https://github.com/Shnewbs/Deep-Primal/commit/cf578b2e36fcdbde04320cc2564c5b1cd89004d6), source-only renderer grade 5.74 (+0.01). Whole-engine grade unchanged. No owner Java25/GPU/JAR tests in this run.
