# Renderer Jam 66 — pending bounded-apply remainder

This renderer-only jam extends the existing read-only LodDiskIndexService diagnostics snapshot with the exact number of queued discard/add work items still remaining in the bounded owner-thread apply.

## Production change

- `LodDiskIndexService.Diagnostics` now includes `pendingApplyRemaining`.
- `PendingApply.remaining()` reports the exact outstanding discard-plus-add entries after the current bounded turn.
- No filesystem work, queue mutation, mesh generation, GPU submission, water simulation, backend dispatch, or settings behavior changed.

## Scope exclusions

No terrain-generation redesign, finite-water simulation/chemistry, GUI redesign, ports, standalone-game work, or FSR/DLSS implementation was added.

## Validation

Focused source-level readback only. Owner-side Java25/full Gradle/NeoForge, native Vulkan/OpenGL, GPU timing/VRAM, water/lifecycle, packaged JAR, and release checks remain unrun.
