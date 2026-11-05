# Renderer jam 14 — resource-safe LOD world reset

Jam `renderer-20261028T1800-gpu-reset-failure-cleanup`, source [`96f368e8e212db211376c111a9bef148f9d1f67f`](https://github.com/Shnewbs/Deep-Primal/commit/96f368e8e212db211376c111a9bef148f9d1f67f), version `0.0.1-alpha.29-dev.59-ecotone-query.1`.

The existing LOD `reset()` could abandon pending and cached GPU buffers when a close operation threw. It now relinquishes pending ownership before closing, tries every cache entry, clears state, then propagates the first close failure (with later failures suppressed). No geometry, water, shader, native backend, settings, worldgen or unrelated file changed.

**Isolated Java21 strict build + 44,444 assertions passed**, mutation test failed as expected. No Java25/full Gradle/native GPU/client/JAR under October 27 owner-build override. See [read-only self-audit](REVIEW.md), [receipt](RECEIPT.json), and [claim](CLAIM.json). Renderer score **5.51/10 (+0.03)**; separate independent engine score remains historical 5.0/10. No frame-time/FPS/VRAM credit.
