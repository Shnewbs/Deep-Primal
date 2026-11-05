# Renderer backlog after 20261028-disk-nearest

1. Replace all-in-one worker directory enumeration with a thread-owned resumable cursor, per-turn entry cap and interruption cleanup. On camera travel, rescan and reconcile nearest 8,192 keys while retaining saved-revision/edit protection and deferred capacity entries.
2. Owner native fixed-seed camera/travel/teleport/edit/reopen/negative seam/transparent-water and full-queue (8,192+) LOD acceptance, including visible page coverage, with a packaged Java25 build.
3. Same-world default/experimental-32KiB A/B: CPU cull/upload/submit and full CPU/GPU frame p50/p95/p99, fence/VRAM and page commit/drop/draw counts. Do not enable split by default without verified advantage.
4. Profile actual mesh/culling/submission bottlenecks before merging or changing indirect GPU paths; preserve light/material/UV/water semantics and Vulkan/OpenGL fallback.
5. Finish temporal depth/motion/jitter/input and legally packaged native NGX/FSR prerequisite gates without advertising a nonfunctional DLSS setting as working.

Renderer author self-grade 4.75/10 (previous 4.67), independent engine score 5.0/10 carried. Other schedules untouched. Owner retains complete Gradle/Minecraft/GPU/JAR validation.
