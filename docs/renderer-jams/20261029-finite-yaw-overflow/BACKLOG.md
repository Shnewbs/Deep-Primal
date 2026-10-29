# Renderer jam29 follow-up backlog (renderer-only)

1. **Owner native build gate:** Java25 Gradle/NeoForge compile and packaged identity verification; source-only Jam29 artifact is not a JAR.
2. **Actual GPU baselines:** matched Vulkan/OpenGL fixed/opt-in adaptive/disabled page-frustum trials, ordinary and extreme turns, camera travel/edit, water transparency and world native handoff. Capture CPU/GPU p50/p95/p99, draw/descriptor counts, upload/rebuild, RAM/VRAM, cache/resident/pending.
3. **Performance concern to evaluate:** this jam's CPU *angle arithmetic* fixture ran 29–34% slower despite retaining normal-angle fast path. Sample realistic game extraction frequencies before additional CPU culling changes; experiment remains default-off. Do not call isolated component timings FPS.
4. **Functional acceptance:** resource reload, resize, world switch, save/reopen, shutdown, edit invalidation, signed seams, transparent-water ordering and LOD transitions.
5. **Next feature after measurements:** bounded mesh/submission and streaming optimizations, requested vs effective full-detail/vertical/LOD controls with host safety; temporal FSR and DLSS only once actual vendor provider dispatch and temporal inputs are validated on supported hardware. Frame generation is later.
6. Keep this scope distinct from terrain/water builder, independent auditor and unrelated GUI/ports/standalone work. Preserve current `main` history and released claims.
