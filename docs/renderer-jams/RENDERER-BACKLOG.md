# Deep Primal renderer-only open acceptance and jam backlog

Last scoped implementation source: [`3478cf5d8f44fcc0cdcced2576f12704732f44f0`](https://github.com/Shnewbs/Deep-Primal/commit/3478cf5d8f44fcc0cdcced2576f12704732f44f0); [renderer self-audit](20261028-nonempty-refresh/REVIEW.md) **5.17/10**. Keep separate from general engine and finite-water schedules. Full Gradle/Minecraft/GPU builds remain owner-operated.

1. **Native persisted LOD recovery:** Java25/Java21 filesystem tests now cover post-snapshot ownership, successful-empty/missing-folder recovery and **400-client-tick successful-nonempty directory rediscovery at a stationary camera**; [20261028-nonempty-refresh](20261028-nonempty-refresh/README.md). Retain 256-entry/turn and 8192-key bounds. Native save/reopen/teleport/edit, 64-chunk retarget, water transparency, extreme-directory overhead and actual GPU-visible coverage remain open. Do not claim unexplored terrain delivery or frame-time improvement from source checks.
2. **Matched Vulkan/OpenGL frame baselines:** p50/p95/p99 CPU/GPU frame, draw/descriptor count, mesh/rebuild/upload, VRAM/RAM, camera travel/edit, LOD+water opacity, warm/cold and named GPU/driver; compare ordinary vs opt-in 32KiB upload parts.
3. **Bounded mesh/streaming changes:** profile worker/cull/reuse, upload part splitting, resource lifetime under cap churn and stale edit cleanup before seeking real batching/indirect (never label many draws as indirect).
4. **Correct useful distances:** separate full-detail/vertical/LOD/simulation caps, requests/effective limits, host tracking/network safety, cache admission, authoritative unseen-terrain delivery.
5. **Upscaling integration:** preserve actual AMD FSR1, implement temporal inputs/history and a supported vendor provider for newer FSR; integrate valid NVIDIA DLSS Super Resolution Vulkan NGX dispatch only on supported hardware under license. Frame generation remains separate.
6. **Owner-operated qualification:** full Java25 Gradle/NeoForge JAR, archive/class checks, native Windows/Vulkan/OpenGL/reload/resize/save/shutdown and GPU footage/performance before artifact delivery/score escalation.

Do not promote source-only fixes to verified native coverage; do not alter other roles' schedules or owned files.

