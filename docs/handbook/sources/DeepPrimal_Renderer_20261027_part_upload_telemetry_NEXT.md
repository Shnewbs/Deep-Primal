# Dedicated renderer backlog continuation

1. Validate `lodGpuPartCpuTiming`, `lodGpuPartSizeTiming`, `lodGpuPartOverCpuBudget`, and `lodGpuPartFailed` at pinned Vulkan + OpenGL scenes, record named hardware and frame/GPU timestamps separately. Measure warm/cold camera, travel, edits, sea/river transparency, native handoff, reload, resize, world reset and budget stress.
2. If measured spikes correlate with indivisible part sizes, add bounded quad-preserving part subdivision and paired timing/VRAM evidence; otherwise prefer worker re-use, fewer allocations and real GPU draw/submission profiling.
3. Audit saturated LOD disk-key discovery and near-native handoff, including no stale edit reuse. Keep world generation, server tracking and unseen-region authority separate.
4. Distinguish requested/effective full detail, vertical, LOD and simulation budgets in persistent UI; do not increase host-tracker limits by slider alone.
5. Preserve shipped FSR1 EASU/RCAS; build temporal inputs, NGX device extension/dispatch/lifetime and RTX acceptance before activating DLSS; frame generation remains a separate feature.

Owner runs complete Gradle/Minecraft/GPU builds until explicitly changing the October 27 source-only handoff. Renderer jam self-audit is not the independent engine auditor.
