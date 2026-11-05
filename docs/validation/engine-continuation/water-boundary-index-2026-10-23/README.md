# R42 — index water boundary quads by neighbor direction

2026-10-23. Staged on `work/engine-after-alpha24`; parent `92b45b09930b067f09ccbe041069037121f2dca8`. This is a continuation checkpoint. Engine/performance acceptance remains incomplete; public production sources, main and supplied installers are unchanged.

## Reproduction and change

Source audit found that each of eight horizontal neighbors traversed its complete top-boundary list, including quads that cannot share a corner with the incoming page. A known-coordinate full-section boundary fixture has960quads: the prior traversal visits7680entries across eight neighbor routes; the direction index visits1088, with identical relevant corner masks. This is a dependency-free work-count reproduction, not a real-scene latency measurement.

The independently authored immutable `WaterBoundaryIndex` stores neighbor-facing entries in original GPU order. Side cells belong to one bucket; page-corner cells belong to two sides and their diagonal. The renderer selects the opposite neighbor bucket and computes its local shared-corner mask. It avoids processing unrelated boundary quads while retaining packet arithmetic, local clocks, GPU grouping and accepted-range cleanup/rejection order. The index is built before GPU publication, assigned after success, and dropped on release/reset. The range-commit and retirement methods are byte-identical to R41; vertex/shader/layout, geometry, water quantity, solver and frozenV1/V2/V3 geography are unchanged.

The complete586-input snapshot contains24staged files:6production/18validation. Since R41, two production and three private validation files change; the other581prior inputs are identical. The new core helper and checks are added, CoreChecks registers them, and the private boundary fixture is strengthened. Fresh R41 controls use exactly the same strengthened fixture source/class, adapting its reflected field assignment to each index representation.

## Fresh verification

- Two full25-task builds pass: R41 product control and candidate. Candidate core checks add688129absolute-coordinate/stable-order/immutability/storage-bound cases, all eight directions and large positive/negative origins. Existing210000packet and210000bulk cases remain passing, alongside retained terrain/watershed/module checks.
- Five software-native sessions pass: cold batch-only R41 diagnostic, full-suite R41 control, development Vulkan, immutable packaged Vulkan and OpenGL fallback. Each boundary session has256cases/512paired CPU-GPU reads. The three candidate sessions total768cases/1536paired reads; every after-call/after-retry packet matches both fresh controls and an independent Python packet/rejection oracle. Coverage includes all eight directions, unrelated quads, scattered/contiguous layouts, large coordinates, original262080-byte maximum batches, rejected first/second writes and idempotence.
- The three candidate suites also pass48admission cases/432paired reads without budget-denied resident loss,15336seam pixel comparisons/30672draws,48synchronous publication-rejection cases/192reads,nine extraction-deadline cases and129distinct rendered-frame retirement turns per session. A fresh full-suite R41 control passes these same controls separately. R40 admission and older publication baselines are historical replay, not new native runs.
- Explicit saved disconnect, crash delta0, Vulkan validationErrors0, zero tracked resources and shutdown continuation pass. Warnings remain visible in raw logs. Seven closed-launcher/build raw/sealed console identities pass. Package audit checks6archive CRCs,412compiled class identities and5nested/standalone identities; private validation/compatibility-agent code is absent. The packaged launch verifies the audited outer package is unchanged. Runtime renderer hashes differ from R41 and match current candidate compiled bytes across all three candidate sessions; preserved disassembly confirms calls to the direction index.
- Three lake screenshots were manually inspected. Water, banks and terrain render without an error screen. Static captures, differing Vulkan/GL dimensions and live phase/load state are not complete temporal-flicker, API-parity, visible-cap-pressure or distant-LOD acceptance.

## Performance and memory limits

| Private whole-boundary observation | Maximum normal call, ms |
| --- | ---: |
| R41 full-suite Vulkan control | 2.537825 |
| Candidate development Vulkan | 3.108334 |
| Candidate packaged Vulkan | 2.439478 |
| Candidate OpenGL | 1.233415 |

The standalone cold batch-only control reaches112.745399ms; it is retained as a diagnostic, not the primary latency comparison. Even the full-suite controls are single-run route observations with host timing/order and scene variation. Candidate results are mixed; no robust latency improvement percentage or API comparison follows. The2ms target remains unmet. Candidate retirement maxima are5.895935ms development Vulkan,17.416660ms packaged Vulkan and4.719695ms GL. Range counts/payloads do not shrink, and GPU/whole-neighbor work remains synchronous.

The index adds construction CPU and retained memory. Its int-array payload is bounded by40bytes plus12bytes per input boundary quad, versus4bytes per prior flat-list entry. At the nominal32MiB/2048-page caps, the loose raw-index bound is2878112bytes, an increment bound of1946048bytes. These bounds exclude array/object headers, temporary construction arrays and total process/GPU peak memory; they are not residency acceptance. The full-frame private fixture bypasses ordinary discovery/admission/eviction and does not measure visible-scene cap churn. Index-construction CPU and workload-matched total upload/memory acceptance remain open.

## Next implementation and outstanding gates

This removes irrelevant traversal; it does not implement cross-frame publication. Next implement bounded/resumable scattered/cohort updates with explicit visibility, target supersession, edit/unload/reset invalidation and retry rules. Simply yielding partway through a shared corner cohort can delay neighboring targets, so complete corner/pixel/rejection regressions must survive. Then match actual visible-scene travel/edit/cap churn and sustained streams/oceans before promotion.

Keep distant rectangular horizon/LOD cutoffs, spatial/coarser coverage, mixed transparency, automatic retry/whole-cohort and allocation/device-failure atomicity, ownerGPU/FPS and comparative market evidence open. Prior stale-class/empty-descriptor, marker/reload/atlas and save failures retain their unresolved causes. No new build/native assertion failures occurred; no speculative workaround is added. Renewable/finite budgets, metered headwaters, compatible river/ocean constituent mixing without duplicate volume, bounded pressure/drain continuation and geology remain under the requirement register; catalog/species stays deferred. Solver substeps are not independent60Hz.

## Restoration and evidence

```
python scripts/restore_snapshot.py EVIDENCE_DIR NEW_ISOLATED_PROJECT
python scripts/replay_checkpoint.py EVIDENCE_DIR NEW_REPLAY_DIR
```

Destinations must not exist. Restoration checks all586source/archive hashes before writing. `baseline-overlay.json` and its584-input manifest reconstruct the fresh control from the candidate. Replay checks raw GPU/pixel data, console identities and controls without a new build/native run or new timing. Captures use lossless content-addressed compaction with every logical path/hash retained. The same pinned JDK25/JDK21/Gradle9.2.1, Mesa25.2.8software driver and Xvfb1.9 are used sequentially. Software checks provide no owner-GPU guarantee;73older missing originals remain missing. No installer is released.

Offline replay passes: 971physical capture/build/historical-admission archive members and20926logical capture paths are hash-checked, alongside586candidate inputs and584reconstructed control inputs. No build/native execution or new timing is performed by replay.
