# Scoped renderer backlog after jam 30

1. **Highest priority native evidence:** owner-run Java25 Gradle/Minecraft and matched Vulkan/OpenGL fixed/adaptive/disabled coarse page-frustum A/B, moving/turning cameras, all-visible and 80%-offscreen views; frame CPU/GPU p50/p95/p99, draw calls, VRAM/RAM and transparent-water pixel comparison. No native acceptance from source tests.
2. **Mesh/GPU throughput:** profile actual submission, descriptor/upload/scratch allocation, worker queue, fence-safe buffers and constrained meshoptimizer semantics before selecting a real bottleneck. The present broadphase is CPU culling, **not** GPU indirect.
3. **Useful coverage and streaming:** native edit invalidation, nearby/full-detail handoff, negative seams, cache bounds and authoritative unexplored distant tiles; do not merely lift sliders or host tracking limits.
4. **Temporal upscaling:** preserve real FSR1 spatial. Acquire valid jitter, motion vectors, preserved world depth, frame history and vendor runtime/extension/licensing requirements before activating temporal FSR or NVIDIA DLSS SR. Frame generation remains separate.
5. **Acceptance and owner builds:** packaged Java25 NeoForge/client lifecycle, water alpha/surface order, resource reload/resize/world-switch/save/reopen/shutdown, GPU captures; release only if owner authorizes a qualified artifact.

**Latest source checkpoint:** [jam30 `ab15e44cdad0a40966b6682f06b486031bb3aa5e`](https://github.com/Shnewbs/Deep-Primal/commit/ab15e44cdad0a40966b6682f06b486031bb3aa5e); source-only self-audit **5.73/10** (jam29 5.72); engine grade remains separate.
