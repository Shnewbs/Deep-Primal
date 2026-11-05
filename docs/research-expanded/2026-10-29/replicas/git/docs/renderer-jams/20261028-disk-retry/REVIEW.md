# Expanded replica — docs/renderer-jams/20261028-disk-retry/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-retry/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-disk-retry/REVIEW.md |
| Preserved source SHA-256 | `1a3523aeac394f6d0dd0df57fcc2f09c8edfdfbcd7d85a9f6e926774af254af6` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds same-camera retries after missing directories or transient scan I/O failures using bounded client-tick exponential backoff, with camera retarget bypass and deferred reconciliation first. Worker retirement remains nonblocking on the client thread. The Java21/Java25 regression closes the specific retry defect, not every discovery case.

## Research expansion — gaps and details to deepen

- The historical successful-empty and successful-nonempty discovery gaps were later addressed by separate refresh changes. Preserve that chronology while clarifying that a tick-based retry cadence cannot guarantee filesystem completion or wall-clock recovery.

## Research expansion — proposed contracts

- Model scan outcomes as missing, failed, successful-empty, successful-nonempty, cancelled, or stale, each with its own next eligible action. Record attempt age, backoff reason, owner generation, and retired-worker count without noisy missing-folder logging.

## Research expansion — acceptance checks

- Reproduce missing-folder creation and permission recovery at a stationary camera, then test later refresh policies together with reset and saturated reconciliation. Verify native visible recovery and bounded worker lifetime; source filesystem assertions do not establish GPU coverage.

## Research expansion — source anchors

- Renderer jam 20261028-disk-retry — read-only six-category self-audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-disk-retry — read-only six-category self-audit

**Exact reviewed source:** [`a5b29c00c66b243490cee9b8c4ae1e17f732f6a4`](https://github.com/Shnewbs/Deep-Primal/commit/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4), after final focused Java21/Java25 tests. This review was performed without code edits during scoring. It is *not* the separate independent whole-engine audit. Previous renderer jam: [20261028-disk-cursor](../20261028-disk-cursor/REVIEW.md), **4.92/10**. Independent auditor's [new 06:33 UTC finding](../../engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md) is consumed; that auditor's 5.0/10 whole-engine baseline is carried, not freshly regraded.

| Equal-weight renderer category | Current /10 | Exact pinned evidence and unresolved limitations |
|---|---:|---|
| Distance / LOD correctness and usable coverage | **5.5** | [Missing-directory probe](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L146-L156), [same-camera retry and deferred admission](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L38-L97), and [real filesystem test](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/docs/renderer-jams/20261028-disk-retry/LodDiskRetryRegression.java) close the 06:33 audit's observed retry failure. No native Minecraft frame, world reopen, or unexplored-terrain delivery proof. |
| Mesh generation / reuse / scheduling | **6.0 (carried)** | Existing 256-entry scan grants and 8192-key cap remain in [LodDiskNearestScan](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L16-L25); [worker-retirement ownership](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L126-L153) reduces reset overlap risk. No newly measured mesh throughput, draw count or native scheduling gain. |
| GPU culling / submission / memory | **4.0 (carried)** | [Only two LOD discovery Java sources changed](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/). GPU buffer, indirect draw, residency and device fence paths unchanged, not benchmarked. |
| Actual FSR / DLSS | **5.0 (carried)** | Existing [FSR1 spatial source and explicit DLSS inactive state](../../UPSCALING.md) unchanged. No temporal FSR or active DLSS evaluation; no frame-generation claim. |
| Visual/backend/settings compatibility | **5.0 (carried)** | No Vulkan/GL shader, material, UI, water transparency or setting code changed. [Retarget/reset behavior](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L38-L74) passes pure-Java controls, but native water ordering/reload/resize/world-switch pixels untested. |
| Validation and measured performance | **4.5 (carried)** | Exact committed [regression source](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/docs/renderer-jams/20261028-disk-retry/LodDiskRetryRegression.java) and extended source harness passed Java25 **22,061** and Java21 **22,091** focused assertions across final runs; separate unprivileged Java25 extended check passed 4,220. Old-source baseline fails expected assertion. No matched native frame p50/p95/p99, FPS, GPU VRAM/timing or Java25 Gradle/NeoForge. |
| **Average** | **5.00/10** | Sum **30.0/60**, **+0.08** from prior **4.92/10**, target strictly >8.5 unmet. |

**Read-only conclusion:** the existing persisted LOD discovery no longer permanently stops after a missing folder or recoverable directory I/O error; a bounded per-tick retry is implemented and verified in real filesystem JVM tests. Reset now tracks retired worker lifetime without waiting on the client thread. A stable directory scanned empty and populated later, and complete native world-view correctness, remain unaccepted. No scored evidence implies FPS increase, FSR temporal support, DLSS readiness, independently 60Hz simulation or engine completion. Full build/JAR are owner-operated and unrun.


<!-- END PRESERVED SOURCE -->
