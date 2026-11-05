# Renderer jam 15 — reset-control teardown isolation

Implementation [`969abe129b94df7734afe61bc3b5d76f5155bf13`](https://github.com/Shnewbs/Deep-Primal/commit/969abe129b94df7734afe61bc3b5d76f5155bf13). Prevent exceptions from asynchronous capture cancellation, disk scan reset or executor shutdown from abandoning later cache GPU releases and stale-world cleanup. First failure remains visible; subsequent failures are suppressed. This is renderer-only.

**Java 21 isolated fault-injection checks:** 1,664 assertions passed for 128 masks × two throwable configurations; deleted-disk-reset negative check failed as intended. Source anchors verified on pinned implementation. The shared version is `0.0.1-alpha.29-dev.60-relief-v2.1` (not changed). **No Java25, full build, native GPU, FPS, VRAM or JAR acceptance.**

[Read-only six-category self-audit](REVIEW.md) **5.53/10 (+0.02)**. Independent engine review remains 5.0/10. [Test source](LodResetControlChecks.java); [receipt](RECEIPT.json); [released claim](CLAIM.json).
