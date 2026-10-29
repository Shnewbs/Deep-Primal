# Renderer jam 43 — read-only author self-audit

**Exact scored source:** [`8f28f833caa259a0e59adac494a3aee8d750bc63`](https://github.com/Shnewbs/Deep-Primal/commit/8f28f833caa259a0e59adac494a3aee8d750bc63) (production Git blob `32ae24afbfa3c9c5d5f668ac860443f05fc336b2`); parent renderer claim `27620460fb51c06c9d13a034d8802c67d3ae7878`; observed unchanged `mod_version=0.0.1-alpha.29-dev.88-vegetation-receipts.1`.

This was a **read-only author self-audit after the code commit**, not an independent engine audit. The historical independent whole-engine average remains 5.0/10; no new whole-engine regrade or native performance acceptance.

## Inputs, scope and correctness

Reviewed actual current-main source; no `AGENTS.md`; `docs/ENGINE-WORK-STATE.json`, `docs/WATER-ENGINE-WORK-STATE.json`, LOD/drainage and graphics/upscaling architecture, renderer backlog/claims, prior jam42 and latest relevant independent engine review. The renderer-only claim fenced `LodPublishedBounds.java` and this jam's docs. No other worker's source, schedules, caches or generated outputs were altered.

Previous finding consumed: jam42 showed useful uniform-page metadata p50 reductions but inconsistent mixed-late p99; the two-back page-publication/fine-culling break-even still requires owner GPU timing. This jam targets redundant page-publication checks, not GPU acceptance.

- [`LodPublishedBounds.java` L55–68](https://github.com/Shnewbs/Deep-Primal/blob/8f28f833caa259a0e59adac494a3aee8d750bc63/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L55-L68) reuses previously validated **immutable object identity** for i-1/i-2, preserving false for invalid repeated objects. Invalid pages remain non-cullable by the coarse union, while later valid fine parts keep their own adjacency checks.
- [`LodPublishedBounds.java` L83–105](https://github.com/Shnewbs/Deep-Primal/blob/8f28f833caa259a0e59adac494a3aee8d750bc63/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L83-L105) omits a redundant representative equality check when a proven adjacent match implies uniformity. First mixed divergence still replays the validated prefix to retain exact signed-zero `Math.min/max` behavior.
- [Pinned independent parity oracle](LodImmutableAabbIdentityChecks.java) against [original source](LodPublishedBoundsPreIdentity.java) checks 12,000 deterministic randomized multilevel pages per flag configuration plus explicit null, invalid-order, NaN, repeated invalid, signed ±zero, A/B/A, negative-coordinate and finite uniform pages. Exact 64-bit union coordinates, identity markers and both masks are compared.

## Executed checks

Linux x86_64, OpenJDK `21.0.12.1`. The owner's Temurin JDK25 installation and archive were absent. `javac --release 21 -Xlint:all -Werror` **passed**. Two independent repeats of four broadphase × two-back startup combinations, including the prior two-back regression, **16 processes / 23,885,080 assertions passed**. Source/baseline/test/stub blobs **5/5 matched** the pinned commit. A deliberately broken build that marked a repeated invalid box valid **failed at assertion 15**, as desired. The new standalone two-stub source check also passed all four flag combinations.

Exact commands and raw logs are in the evidence archive. Native GPU/device pixels or Minecraft frame completion were **not** exercised.

## Paired synthetic Java CPU p50 (ns per 64 page publications)

| Geometry, 4 LOD levels × 64 parts | Run 1 old → new | Run 2 old → new | Effect |
|---|---:|---:|---|
| Uniform shared instance | 63,786 → 36,345 | 63,766 → 36,465 | 42.8–43.0% faster |
| Uniform equal values, separate AABBs | 118,188 → 98,398 | 118,298 → 98,308 | 16.7–16.9% faster |
| Alternating A/B shared instances | 118,239 → 100,431 | 124,177 → 103,136 | 15.1–16.9% faster |
| Mixed near-end divergence | 195,304 → 183,386 | 200,171 → 191,078 | 4.5–6.1% faster |
| All unique | 93,381 → 97,737 | 90,216 → 95,344 | **4.7–5.7% slower** |
| Alternating shared valid/invalid | 71,077 → 45,609 | 71,368 → 45,769 | 35.8–35.9% faster |
| All shared invalid | 10,035 → 11,558 | 10,095 → 11,658 | **15.2–15.5% slower** |

Mixed-late p99 worsened in one run (510,249→525,442 ns). Alternating p99 was inconsistent; other p95/p99 varied. These microbenchmarks exclude real CPU/GPU contention, allocations beyond the fixture, native frames and VRAM. No measured FPS improvement is claimed.

## Six equal-weight renderer-only scores

| Category | Score /10 | Pinned evidence / outstanding gate |
|---|---:|---|
| Distance/LOD correctness and useful coverage | **6.80 carried** | [Frozen page union and masks](https://github.com/Shnewbs/Deep-Primal/blob/8f28f833caa259a0e59adac494a3aee8d750bc63/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L55-L120); no new proven distant coverage |
| Mesh generation/reuse/scheduling | **6.95 carried** | Only CPU bounds metadata; no mesh worker/upload change |
| GPU culling/submission/memory | **4.55 carried** | No actual indirect, GPU timing, descriptor or VRAM result |
| Actual AMD FSR/NVIDIA DLSS | **5.00 carried** | Prior FSR1 spatial implementation, temporal FSR and active NGX/RTX unproven |
| Visual/backend/settings compatibility | **5.30 carried** | No changes to backend, water blend or persisted controls; native pixels untested |
| Validation and measured performance | **6.60 (+0.05)** | Pinned exact-source differential parity, negative mutation and paired isolated CPU measurements; mixed/unique tails unresolved |
| **Average** | **5.87 (+0.01)** | 35.20/6=5.8667; previous renderer jam42 displayed 5.86; independent engine grade separate |

**Unrun and not claimed:** Java25 full Gradle 9.2.1/NeoForge, JAR, outer/nested CRC/native payload, real Vulkan and OpenGL frames, transparent-water pixels, FSR1 visual output, camera/travel/edit CPU+GPU p50/p95/p99, RAM/VRAM, native save/reopen/resource reload/resize/world switch/shutdown, temporal FSR, actual RTX DLSS. Direct Git CLI failed github.com DNS; authenticated GitHub expected-parent commits and pinned blob checks succeeded. Owner's Oct27 override controls full builds.

**Next priority:** owner native Vulkan/OpenGL same-scene two-back ON/OFF, fixed/adaptive/disabled broadphase, FSR1/native and transparent waterfall captures. Check unique/mixed p95/p99 against saved CPU publication timing. If real workloads are mostly unique or tail regressions matter, gate or revert the identity fast path rather than optimizing more synthetic selectors.
