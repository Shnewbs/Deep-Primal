# Dev130 — bounded ready-chunk admission before native readiness lookup

Date: 2026-10-31 UTC. Pinned source before claim: `b4442bc7c574e4bfaa2dd2810dbcf50805eb197e`. Scoped writer claim: `ec5c6bf7d8b071ab17d561d0211f042d8440ece8`. Independent audit consumed: [Dev129](../../../engine-hourly-audits/2026-10-31T1330-ddcf22c5/REVIEW.md), which found no additional source defect but flagged repeated `ChunkMap.getChunkToSend` readiness probes and unmeasured heavy-queue cost.

## Change

The sender still selects only *ready* chunks, sorts nearest-first and rechecks selected chunks before removing them from `pendingChunks`. Before probing readiness for each pending candidate, it now calls `NearestChunks.wouldAccept(position)`. When the bounded ready heap is full and the candidate cannot displace its farthest ready member, the expensive native lookup is skipped. An unavailable near candidate does **not** occupy a heap slot, so farther ready chunks are still eligible.

`wouldAccept` is a side-effect-free exact 65-bit squared-distance preflight, preserving the existing signed packed-position tie break. Before sorting, the farthest element is the heap root; after sorting, it is the final array element. The existing `offer` semantics, post-sort heap rebuild, and selection order are unchanged. The hot-path preflight has no allocation. It does recompute arithmetic for admitted candidates, so a net CPU improvement is not claimed without native profiling.

Changes are confined to `NearestChunks.java`, `PlayerChunkSenderMixin.java`, a new dependency-free `NearestChunksReadyAdmissionChecks.java` wired into `CoreChecks`, and this evidence. No terrain profile, water mass/chemistry, saved-world format, renderer, LOD, upscaler, or art changes.

## Executed local validation

OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`:

- Existing exact 65-bit nearest-heap differential regression: **224,254 assertions passed**.
- New BigInteger/PriorityQueue readiness-admission oracle: **498,393 assertions passed** across 2,000 randomized cases, signed extremes, unavailable-near starvation, sorted preflight, late offers and exact tie ordering. It skipped **59,760 synthetic readiness probes** (not an in-game speed measurement).
- Actual proposed mixin compiled with isolated Minecraft, Mixin, fastutil and metrics API doubles; reflective injection tests: **33,503 checks passed** across 500 randomized scenarios. The synthetic map observed **51,068 avoided lookups** relative to unconditional prefilter probing. Actual native readiness behavior and scheduling are not measured.
- Three independently compiled broken `wouldAccept` controls were rejected: always true, always false and broken 65-bit carry. Rejections were Java `AssertionError` from the new differential test.
- Production Git blob identities verified against the exact locally compiled files: `NearestChunks.java` = `148f6f02679d3e42ee1c3a00b1c57d2604dd3068`, `PlayerChunkSenderMixin.java` = `fa9b04316134a283a17cd406f9b7614263deb9b2`, and new test = `e6ff5536c4e66dabd93f16410880433c2a0d3b0b`.

## Acceptance boundaries

These are isolated Java21 and synthetic mixin tests, **not** a full Gradle/NeoForge build. This runtime has no complete local checkout, verified Java25 toolchain, dependency cache, working Git CLI DNS, native Minecraft server/client, GPU acceptance, or verified replacement JAR. Do not delete the last successful owner JAR. GitHub Actions were not used. Independent full-engine grade remains **50/100 (5.0/10)** without a new regrade.

Next acceptance: Java25 full `coreChecks` and packaged mod; native chunk-send `SEND_NANOS` and throughput under large pending queues (including high readiness churn), server save/reopen and crash recovery, finite-water conservation, V8 terrain borders and Vulkan/LOD frame-time checks.
