# Expanded replica — docs/renderer-jams/20261028-recovery-epochs/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-recovery-epochs/README.md) |
| Source identity | git: docs/renderer-jams/20261028-recovery-epochs/README.md |
| Preserved source SHA-256 | `c25e04c025af6b998bae98af67b78a005e89f95abe369583b9caca40b5d9a92d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Protects post-snapshot recovery ownership with bounded renewal epochs and prevents stale scan completion from discarding a re-enqueued key. Reconciliation counts inspected candidates and mutations toward its turn budget, and successful-empty directories gain stationary polling.

## Research expansion — gaps and details to deepen

- The epoch rule should join the wider world/device/source identity contract. Local renewal tokens protect queue ownership but do not alone prove old-world jobs, stale disk bytes, or GPU publications are rejected throughout the pipeline.

## Research expansion — proposed contracts

- Propagate immutable world-attachment and source-revision identity through scan, restore, mesh, upload, and publication. Preserve renewed keys under saturation, define overflow restart semantics, and link the later nonempty-refresh repair rather than treating this historical polling policy as complete.

## Research expansion — acceptance checks

- Retain renewal and empty-folder negative controls, then stress repeated remove/re-enqueue, overflow, retarget, world switch, and delayed completion in the actual client. Verify bounded worker retirement, authoritative edit precedence, and useful visible coverage.

## Research expansion — source anchors

- Renderer jam 20261028-recovery-epochs — renewed LOD ownership and fixed-camera discovery
- Production source changes

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-recovery-epochs — renewed LOD ownership and fixed-camera discovery

**Qualified source-only implementation:** [2931f6c7a839013c216f21386a62c72122042d83](https://github.com/Shnewbs/Deep-Primal/commit/2931f6c7a839013c216f21386a62c72122042d83). Inherited engine version `0.0.1-alpha.29-dev.48-v8materials.1`; no game version bump or owner testing JAR. The source commit parent is `3c1c3668bcf8a9c2489be35c0c311bc38bd84be5` (renderer-only claim), based on independent audit checkpoint `29137b5e6c19d5fcb4f42943ad6156f2d7841ff8`. The review and claim-release commit contains documentation only.

This jam consumes the [independent 07:31 UTC audit](../../engine-hourly-audits/2026-10-28T0731-5a23f28/REVIEW.md), which reproduced a queued recovery removed and re-enqueued during an 8192-key background directory scan, then incorrectly discarded by the older completion. The same audit confirmed an existing, successfully scanned empty directory would miss a later file at an unchanged camera. Prior [renderer jam](../20261028-disk-retry/REVIEW.md) self-graded 5.00/10.

## Production source changes

- [LodDiskIndexService.java, owner epochs](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L33-L65): records post-snapshot enqueue ownership in a map capped at 8192 keys; overflow aborts stale work and starts another bounded snapshot. Client-thread state and reset/cancel lifecycle are preserved.
- [Stale discard and near-page admission](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L142-L207): only discard a key if its ownership has not been renewed. If a protected owner consumes the original eviction slot, shed a lower-priority *unrenewed* ranked queued page while admitting the nearest new disk page. Every inspected candidate and queue mutation counts toward a 256-entry client-turn budget; the 8192-key cap remains intact.
- [Empty-folder polling](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L121-L139): a successfully scanned, truly empty saved-page set silently retries every 200 client ticks. Existing absent-directory and I/O-error exponential backoff 20→40→80→160→320 ticks is unchanged.
- [VoxelLodRenderer.java](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java): all four existing caller-side persisted-recovery enqueue/requeue paths now report ownership renewals to the index service. No mesh, GPU buffer, shader, water transport, geometry or worldgen method was modified.
- [LodDiskRecoveryEpochChecks.java](https://github.com/Shnewbs/Deep-Primal/blob/2931f6c7a839013c216f21386a62c72122042d83/docs/renderer-jams/20261028-recovery-epochs/LodDiskRecoveryEpochChecks.java): a committed real-filesystem Java regression covers post-start same-key requeue, multi-tick deferred apply, priority/cap preservation, and successful-empty directory rediscovery.

## Executed focused validation

Isolated Linux source-only checkout. OpenJDK 21.0.12.1 and owner-provided Temurin JDK 25.0.5+8, re-used read-only at its verified existing workspace path. Both compiled with `-Xlint:all -Werror`, and all tests ran `-ea -Xverify:all`. Exact product source Git blobs were read back after the guarded direct-`main` commit; JDK-tested service source blob is `bf6f8c59b6578253440b201bae616bc2b7022df0`, the unchanged scanner is `fc76905ab1f82ba49cc4495ab8b943127b2b8d21`, renderer caller is `29e528d8da2dc1f42c8b9890d3f78804b3a9f81c`, and committed test source is `d215cb9e3713c9466730e9c2476e29ef1ece34ac`.

```bash
find src tests -name '*.java' | sort > sources.txt
"$JAVA_HOME/bin/javac" --release 25 -Xlint:all -Werror -d classes25 @sources.txt
for test in LodDiskNearestScanChecks LodDiskIncrementalChecks LodDiskIndexServiceChecks LodDiskRetryChecks LodDiskRecoveryEpochChecks; do
 "$JAVA_HOME/bin/java" -ea -Xverify:all -cp classes25 "com.shnewbs.deepprimal.lod.client.$test"
done
/usr/bin/javac --release 21 -Xlint:all -Werror -d classes21 @sources.txt
```

Final JDK25 results: **9006 nearest + 8295 incremental + 537 service + 4218 retry + 789 new ownership/empty = 22845 assertions**. Final JDK21: **9006 + 8295 + 521 + 4218 + 787 = 22827 assertions**. Asynchronous timing changes the service/new-test assertion counts between passing runs. An extra unprivileged Java25 test passed 8 filesystem permission-recovery assertions. Two deliberately broken mutant programs fail the appropriate exact assertions: suppressed renewal reporting and disabled stable-empty polling. This is a functional result, not a measured game-frame speedup.

## Unrun/remaining gates

Per the owner's October 27 override, **full Java25 Gradle/NeoForge, JAR packaging, native Minecraft world save/reopen, GPU pixel/lifetime, AMD Windows and NVIDIA RTX DLSS hardware, FSR temporal/NGX integration, real FPS/frame p50/p95/p99, and other full-scene latency measurements were not run**. No new JAR or GitHub Release/Actions/CurseForge upload occurred. Supported FSR1 spatial mode and inactive temporal/DLSS providers remain unchanged. A nonempty, successfully scanned directory that later gains more files at the same camera still needs discovery policy and native acceptance.

Next: owner-operated native reopen/travel/edit + 8192-key saturation/fault tests on exact source; matched Vulkan/OpenGL frame and page-upload p50/p95/p99, draw/VRAM/transparent-water coverage; then temporal FSR/DLSS integration and effective-distance budgeting. The independent engine grade stays at its previously recorded 5.0/10, **not regraded here**.


<!-- END PRESERVED SOURCE -->
