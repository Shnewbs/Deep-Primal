# Deep Primal renderer-only open acceptance and jam backlog

Last scoped implementation source: [`2931f6c7a839013c216f21386a62c72122042d83`](https://github.com/Shnewbs/Deep-Primal/commit/2931f6c7a839013c216f21386a62c72122042d83); [self-audit](20261028-recovery-epochs/REVIEW.md) 5.08/10. Keep separate from general engine and finite-water schedules.

1. **Native persisted LOD recovery:** validate post-scan same-key recovery ownership, fixed-camera empty-directory rediscovery, save/reopen, edits, transient I/O, world exit/rejoin, 64-chunk retarget, 8192-key cap, near/full-detail handoff and water transparency on real Minecraft/Vulkan and GL. Source/JDK25 checks pass for newly protected ownership and 200-tick empty-directory polling; native/pixel acceptance remains open. Nonempty directories receiving new pages without camera movement are not yet guaranteed to be rediscovered.
2. **Matched Vulkan/OpenGL frame baselines:** p50/p95/p99 CPU/GPU frame, draw/descriptor count, mesh/rebuild/upload, VRAM/RAM, camera travel/edit, LOD+water opacity, warm/cold and named GPU/driver; compare ordinary vs opt-in 32KiB upload parts.
3. **Bounded mesh/streaming changes:** profile worker/cull/reuse, upload part splitting, resource lifetime under cap churn and stale edit cleanup before seeking real batching/indirect (never label many draws as indirect).
4. **Correct useful distances:** separate full-detail/vertical/LOD/simulation caps, requests/effective limits, host tracking/network safety, cache admission, authoritative unseen-terrain delivery.
5. **Upscaling integration:** preserve actual AMD FSR1, implement temporal inputs/history and a supported vendor provider for newer FSR; integrate valid NVIDIA DLSS Super Resolution Vulkan NGX dispatch only on supported hardware under license. Frame generation remains separate.
6. **Owner-operated qualification:** full Java25 Gradle/NeoForge JAR, archive/class checks, native Windows/Vulkan/OpenGL/reload/resize/save/shutdown and GPU footage/performance before artifact delivery/score escalation.

Do not promote source-only fixes to verified native coverage; do not alter other roles' schedules or owned files.
