# Deep Primal renderer-only open acceptance and jam backlog

Last scoped implementation source: [`a5b29c00c66b243490cee9b8c4ae1e17f732f6a4`](https://github.com/Shnewbs/Deep-Primal/commit/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4); [self-audit](20261028-disk-retry/REVIEW.md) 5.00/10. Keep separate from general engine and finite-water schedules.

1. **Native persisted LOD recovery:** create/reopen, missing folder later created, transient I/O, world exit/rejoin, 64-chunk retarget, 8192-key cap and edit-revision guards; confirm page pixels and nearby/full-detail handoff. A successfully scanned empty directory is not independently rediscovered at a fixed camera.
2. **Matched Vulkan/OpenGL frame baselines:** p50/p95/p99 CPU/GPU frame, draw/descriptor count, mesh/rebuild/upload, VRAM/RAM, camera travel/edit, LOD+water opacity, warm/cold and named GPU/driver; compare ordinary vs opt-in 32KiB upload parts.
3. **Bounded mesh/streaming changes:** profile worker/cull/reuse, upload part splitting, resource lifetime under cap churn and stale edit cleanup before seeking real batching/indirect (never label many draws as indirect).
4. **Correct useful distances:** separate full-detail/vertical/LOD/simulation caps, requests/effective limits, host tracking/network safety, cache admission, authoritative unseen-terrain delivery.
5. **Upscaling integration:** preserve actual AMD FSR1, implement temporal inputs/history and a supported vendor provider for newer FSR; integrate valid NVIDIA DLSS Super Resolution Vulkan NGX dispatch only on supported hardware under license. Frame generation remains separate.
6. **Owner-operated qualification:** full Java25 Gradle/NeoForge JAR, archive/class checks, native Windows/Vulkan/OpenGL/reload/resize/save/shutdown and GPU footage/performance before artifact delivery/score escalation.

Do not promote source-only fixes to verified native coverage; do not alter other roles' schedules or owned files.
