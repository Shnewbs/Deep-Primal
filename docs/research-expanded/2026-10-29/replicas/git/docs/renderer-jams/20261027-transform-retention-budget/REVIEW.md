# Expanded replica — docs/renderer-jams/20261027-transform-retention-budget/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-transform-retention-budget/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-transform-retention-budget/REVIEW.md |
| Preserved source SHA-256 | `ab719010566373af8e291b3ef6efaa6b8394dcdce3ef624fe84c2373fcd72825` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Bounds transform reuse by both four slots and 32,768 retained elements, using overflow-safe admission, oldest eviction, oversized bypass, factory validation, and full reset. Exact draw counts and synchronous uniform submission remain unchanged. The review explicitly reports source-only evidence and no GPU, heap, or complete-frame timing acceptance.

## Research expansion — gaps and details to deepen

- An element ceiling is a structural proxy rather than a measured heap-byte bound. Oversized bypass can deliberately increase allocation frequency, so the policy needs the observed visible-size distribution and retained-memory/frame tradeoff.

## Research expansion — proposed contracts

- Add measured element/object byte estimates separately from the fixed logical limit, plus oversized request rate, cache-hit distribution, and retained peak. Preserve factory-before-eviction and no-retention-change failure semantics.

## Research expansion — acceptance checks

- Retain the 625,529 checks and rejected mutation; exercise alternating shapes above and below the budget in the real client. Measure factory allocation, retained heap, GC pauses, and native frame tails with identical visibility and settings.

## Research expansion — source anchors

- Deep Primal renderer jam: bounded LOD transform retention — self-audit
- Implemented production change

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal renderer jam: bounded LOD transform retention — self-audit

**Role:** dedicated renderer author, NOT independent engine auditor.
**Read-only production snapshot for this review:** [a571287cb7b39f4dd366b764281ad138b9c37a4c](https://github.com/Shnewbs/Deep-Primal/commit/a571287cb7b39f4dd366b764281ad138b9c37a4c). At that commit `gradle.properties` has `mod_version=0.0.1-alpha.29-dev.37-heightlex.1`. The renderer work did **not** change that shared version or produce a JAR.
**Claim parent:** d35371745c5fdfe63c0f6d40efc92ee290591585. Claim commit `45b2137`; implementation commit `26a33927`; refined final commit `a571287cb7b39f4dd366b764281ad138b9c37a4c`. Intervening builder commits were preserved via fresh expected-parent updates, with no forced push.
**Previous renderer report consumed:** [transform-array-reuse](../20261027-transform-array-reuse/REVIEW.md) at production `100e91f1`, especially the unresolved four-slot, **unbounded retained element count**. The later independent [dev36 audit](../../engine-hourly-audits/2026-10-27T2025-73867fa/REVIEW.md) made the same observation. Its hydrology-guard validator finding is assigned to the engine builder, not to this renderer jam.

## Implemented production change

- [ReusableTransformArrays.java lines 17–81](https://github.com/Shnewbs/Deep-Primal/blob/a571287cb7b39f4dd366b764281ad138b9c37a4c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java#L17-L81): preserve exact-length arrays and FIFO reuse, but bound both slots (4 in renderer) and **sum of retained Java transform elements (32,768)**. Overflow-safe remaining-budget admission, oldest eviction, oversized-bypass without evicting hot entries, validating the factory before eviction, and zeroing all retained references on clear.
- [VoxelLodRenderer.java lines 82–98](https://github.com/Shnewbs/Deep-Primal/blob/a571287cb7b39f4dd366b764281ad138b9c37a4c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L82-L98): explicitly configure four retained variants and a 32,768-element retention budget. [Exact draw count is still used for synchronous uniform submission](https://github.com/Shnewbs/Deep-Primal/blob/a571287cb7b39f4dd366b764281ad138b9c37a4c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L699-L718); [reset still clears the cache](https://github.com/Shnewbs/Deep-Primal/blob/a571287cb7b39f4dd366b764281ad138b9c37a4c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L778-L793).
- [TransformRetentionBudgetChecks.java lines 1–117](https://github.com/Shnewbs/Deep-Primal/blob/a571287cb7b39f4dd366b764281ad138b9c37a4c/docs/renderer-jams/20261027-transform-retention-budget/TransformRetentionBudgetChecks.java): new standalone, non-runtime Java25 oracle validating circular FIFO behavior, 150,000 deterministic count changes, slot/element limits, exact lengths, factory failure, oversized bypass, zero-length, world reset, and 20,000 hot frames.
- No water/terrain simulation, geography V1–V7, saved profiles, shaders, art, Vulkan/OpenGL code, FSR1, DLSS, other feature scopes or root builder state was edited.

## Actual focused execution

Isolated Linux workspace, **Eclipse Temurin 25.0.5+8** (`java -version`, `javac -version`). Exact final Git blobs compiled locally match source file `94237337b506a3df4b32dfba5bd5a705bb17ed53` and fixture `cd6fa4b87ce9262f681670054d4c657654cd9805`. Production integration blob `13d9bc84db79536927c0866fdf5698edeb478a58` was source-reviewed, **not compiled against NeoForge**.

```sh
javac --release 25 -g -d classes ReusableTransformArrays.java TransformRetentionBudgetChecks.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.TransformRetentionBudgetChecks
```

**PASS:** `TRANSFORM_RETENTION_BUDGET_PASS checks=625529 trackedElementLimit=31 oracleOperations=150000 warmBaselineArrays=20000 warmCandidateArrays=2 gpuOrHeapTimingClaim=false`. The 31-element limit exercises eviction; the separate real renderer allocation uses 32,768.
**Mutation control:** remove the `retainedElements += length` statement in a private copy; same Java25 compile succeeds and run rejects it at `AssertionError: initial budget` (exit 1). Mutation was never committed.

**Matched synthetic tradeoff**, four recurring visibility counts over 400 requests, same baseline/candidate independent JVM classpath:

| Four count shapes | Old retained / factory calls | New retained / factory calls |
|---|---:|---:|
| 3,000 / 4,000 / 5,000 / 6,000 | 18,000 elements / 4 | 18,000 / 4 |
| 10,000 / 11,000 / 12,000 / 13,000 | 46,000 elements / 4 | 25,000 / 400 |

These are retained **array element counts**, not measured heap bytes or GPU memory. The large-case churn is a real performance tradeoff and must be checked against actual visible-part distributions before retuning or increasing the cap. No measured p50/p95/p99 FPS, heap residency, GPU timing or driver resource claim.

## Six equal focused grades (/10; conservative current-self review)

| Category | Prior | This review | Actual basis |
|---|---:|---:|---|
| Distance/LOD correctness and useful coverage | 4 | 4 | Existing [signed-distance](../20261027-signed-distance/REVIEW.md) and explored-only contract unchanged; no full native traveled coverage |
| Mesh generation, reuse and scheduling | 6 | 6 | Exact arrays reused under budget; no mesher, worker or staging scheduler change |
| GPU culling, submission and memory | 4 | 4 | Bounded Java retention and exact submission source; no GPU heap/driver, pixels, cull/dispatched indirect or staging measurements |
| Actual AMD FSR / NVIDIA DLSS | 5 | 5 | Existing FSR1 integration unchanged; temporal NGX path/input acceptance and real RTX execution missing |
| Visual, backend and settings compatibility | 5 | 5 | No change to water transparency/settings/backend APIs, but no real API resize/world/reload frame validation |
| Validation and measured performance | 4 | 4 | 625,529 focused Java25 assertions and a rejected mutation; no whole-client/hardware frame measurements |
| **Total / average** | **28/60 = 4.67** | **28/60 = 4.67** | **Change 0.00**; no fresh independent full engine regrade |

The **separate** last accepted whole-engine external grade stays **50/100 = 5.0/10**; this self-review cannot replace the ten-category independent engine audit.

## Not run / blockers / continuation

Owner's October 27 directive reserves full Gradle/NeoForge, Minecraft/GPU builds and releases to the owner. **Not run:** full module/build, game/native LOD save/reopen, world-switch/resize/shutdown, archive CRC and packaged class identity, Windows/AMD8060S OpenGL/Vulkan pixel checks, actual RTX/DLSS, measured JVM allocations/heap/VRAM, GPU frame p50/p95/p99, water transparent ordering. **No JAR or GitHub/CurseForge release was produced.**

Next: measure actual visible-array length histogram and retained Java heap against p50/p95/p99 frame tails in cold/warm camera/travel/edit on the owner's AMD Vulkan/OpenGL system, especially >32,768 total alternating shapes; profile mesh upload and allocation queues; then implement GPU upload reuse and validated temporal motion/depth/jitter integration for DLSS. Keep renderer work independent of engine/hydrology writer.


<!-- END PRESERVED SOURCE -->
