# Renderer Jam 68 — bounded LOD apply-turn counter

Scope: renderer-only increment for bounded LOD streaming observability. No engine-builder, water-schedule, automation, port, or unrelated source changes.

Implementation: LodDiskIndexService.Diagnostics now exposes applyTurns, a lifetime counter for owner-thread PendingApply.advance(...) turns. It counts bounded apply invocations without filesystem work, GPU work, or changing existing mutation semantics.

Implementation commit: dc914d3351707ed28c0cea763212a9801ac5f851

Tests: source-only structural check passed. Java25/full Gradle/NeoForge, native Vulkan/OpenGL/GPU, packaged JAR/release not run per owner override.

Next repair: use applyTurns with pendingApplyRemaining, appliedAdds, appliedRemovals, appliedSheds, and lifecycle counters during owner-side camera travel/edit-churn baselines.
