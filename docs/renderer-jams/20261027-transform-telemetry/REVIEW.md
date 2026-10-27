# Deep Primal dedicated renderer jam — transform workload telemetry (self-audit)

**Jam:** `20261027-transform-telemetry`, **candidate production snapshot:** [`c28f5776b2bbafda1c839d7c7369199452b1b933`](https://github.com/Shnewbs/Deep-Primal/commit/c28f5776b2bbafda1c839d7c7369199452b1b933). This is a **read-only renderer-author self-audit**, not a new independent engine audit, and no production code was edited during this review. Source parent `1613a21cfd58fec83c56013e9176f2a8424f9d81`; scoped claim `731e313e6eb337cae01e4225427822add90954ca`. The common source version at this snapshot remains `0.0.1-alpha.29-dev.37-heightlex.1`; the renderer did not touch shared `gradle.properties` while the engine builder owns separate current changes.

## Consumed last renderer findings, concrete implementation

The preceding [bounded-transform self-audit](../20261027-transform-retention-budget/REVIEW.md) documented **400 array factories / 400 synthetic large-shape requests**, with a 32,768-element ceiling vs 4 factories before the budget, and explicitly called for an actual visible-array length histogram and heap study. The newest independent [dev37 engine report](../../engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md) concerns hydrology guard dominance and remains assigned to the engine builder; renderer did not touch those files.

- [ReusableTransformArrays.java L23–35](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java#L23-L35), [L42–86](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java#L42-L86), [L91–127](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java#L91-L127): count successful real-array requests, cache hits, factory creations, oversized bypasses and FIFO evictions, plus current/peak retained elements, last/peak draw count and five fixed histogram bins. The five long counters use preallocated primitive fields; no per-draw map, string or new snapshot allocation. Invalid factories do not produce successful acquisition counters; clear resets all counters and references.
- [VoxelLodRenderer.java L90–103](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L90-L103): expose a snapshot without changing the exact-length GPU submit path or retention limit.
- [LodClient.java L66–83](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L66-L83): expose counters through the existing local diagnostic map. The client dashboard currently samples that map with frame/context/API metrics; these are **render-thread Java array counters**, not GPU timings/VRAM measurements, and not a claim that a camera/scene run was performed.
- [TransformTelemetryChecks.java L1–118](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/docs/renderer-jams/20261027-transform-telemetry/TransformTelemetryChecks.java#L1-L118): committed stand-alone, non-runtime regression oracle.

Changed files: three LOD Java production files plus the above dedicated test, claim and evidence documentation. **Unchanged:** terrain V1–V7, profiles, player edits, water/chemistry, near/native mesh scheduling, shaders, transparent-water ordering, graphics-settings, Vulkan backend, FSR1/DLSS paths, resource packs, Themes and art. No versioned JAR or release.

## Exact focused test evidence

Environment: isolated Linux, system Java/Javac `21.0.12.1`. Owner-provided Java25 archive and extracted toolchain are **not present in this execution runtime**; direct shell GitHub DNS resolution failed. The GitHub connector read current main, ownership and the pinned files. The helper and test files compiled locally have the identical Git blobs `e84f12bdcf4dffc17421faa8fd34f24bb30226bd` and `96c98afd482e3ab3367ff207eabb1d0c09c5ca52` that are committed.

```sh
javac --release 21 -d classes ReusableTransformArrays.java TransformTelemetryChecks.java TransformRetentionBudgetChecks.java
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.TransformTelemetryChecks
java -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.TransformRetentionBudgetChecks
```

- **PASS 724,271 assertions** (120,000 random requests; 5 draw-count bins; exact hits, allocations, over-budget bypass, eviction and bounded peak accounting; unchanged two-array warm reuse after 10,000 frames; factory error and world-reset checks).
- **PASS 625,529 prior bounded-cache assertions** against the modified helper (150,000 original FIFO/budget requests; 20,000 warm-frame factory-control counts unchanged).
- **EXPECTED REJECTION:** isolated mutated helper removes hit recording; `javac --release 21` succeeds, `java -Xverify:all` exits 1 with `AssertionError: hit/miss/oversized counters drift 39`. The mutant is excluded from source/JAR.
- Evidence ZIP (workspace-only, no JAR): SHA-256 `ad11f11843d1569d8e780fcf8aa3ae039affb370efca1aa32c464c5fba74f70b`, 9,455 bytes. Retains exact tested helper/fixture, original regression fixture, logs and excluded mutation.

**Matched before/after measurements:** old/current renderer still generate two arrays for 20,000 alternating counts 200/201 in synthetic Java, with the same 32,768-element cap. This jam adds instrumented counters rather than claiming an FPS/allocation reduction. No native visible-count distribution or heap/VRAM measurement was made. New successful-request counters are cumulative since world reset; compare deltas for camera/travel/edit workloads.

## Six equal renderer grades (/10; unchanged relative to prior)

| Category | Prior | This jam | Pinned evidence and missing gates |
|---|---:|---:|---|
| Distance/LOD correctness and usable coverage | 4 | 4 | Existing [VoxelLodRenderer selection](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L458-L495) unchanged; no traveled/native seam, edited-surface or unexplored-data acceptance |
| Mesh generation/reuse/scheduling | 6 | 6 | [Transform helper](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/ReusableTransformArrays.java#L42-L86) retained exact factory reuse and bounds, but no mesh/upload algorithm improvement in this jam |
| GPU culling/submission/memory | 4 | 4 | [Diagnostics](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L66-L83) expose Java work only; no real GPU draw/descriptor/VRAM/time acceptance |
| Actual AMD FSR/NVIDIA DLSS | 5 | 5 | Existing [upscaling state](../../GRAPHICS-UPSCALING-WORK-STATE.json) records FSR1 and partial NGX, not actual RTX temporal output; unchanged |
| Visual/backend/settings compatibility | 5 | 5 | No water/shader/backend changes; no compiled native world/reload/resize/Vulkan/OpenGL comparison |
| Validation and measured performance | 4 | 4 | [Committed focused fixture](https://github.com/Shnewbs/Deep-Primal/blob/c28f5776b2bbafda1c839d7c7369199452b1b933/docs/renderer-jams/20261027-transform-telemetry/TransformTelemetryChecks.java) + Java21 executions; no packaged client/native GPU or real FPS p50/p95/p99 |
| **Total / average** | **28/60 = 4.67/10** | **28/60 = 4.67/10** | **Delta 0.00**; focused author self-audit only |

The last fully accepted independent **whole-engine** result remains **50/100 = 5.0/10**, carried; renderer grades do not alter that rubric.

## Concurrency, unrun gates and handoff

At scoped acquisition root work-state was checkpointed and water foreground claim released; renderer's prior claim had been released. The original builder/auditor IDs queried from the prompt were stale/disabled in task records; the newer engine builder was confirmed **in progress** at 21:59 UTC and the newer independent auditor completed its 21:30 run. The root writer lease became active until 22:25 UTC while this renderer jam was being finalized; the renderer's source paths and report are disjoint from its hydrology/guard repair. Commits use fresh head tree and guarded expected-SHA refs, never force-push. No shared checkout/cache or heavy timed builds were used.

**Not run:** full Java25/Gradle 9.2.1/module/NeoForge, compiled client integration, Windows AMD8060S/Vulkan and OpenGL pixels or water transparency, RTX DLSS, actual CPU/GPU p50/p95/p99, measured heap/VRAM, native game reload/resize/world-switch/save/reopen/shutdown, packaged JAR CRC/nested identities. The owner expressly handles full builds. No successful JAR, package qualification or publication was claimed.

**Next priority:** use the new counters against camera/travel/edit scenes (warm/cold) on named AMD/OpenGL/Vulkan hardware with matching frame and upload/culling times; measure large visible shape churn before changing retention policy. Continue bounded GPU upload staging and real jitter/motion/depth NGX prerequisites only after matched graphics gates. Keep this renderer schedule distinct and enabled.
