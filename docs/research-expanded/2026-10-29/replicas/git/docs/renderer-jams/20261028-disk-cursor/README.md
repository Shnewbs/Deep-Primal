# Expanded replica — docs/renderer-jams/20261028-disk-cursor/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-cursor/README.md) |
| Source identity | git: docs/renderer-jams/20261028-disk-cursor/README.md |
| Preserved source SHA-256 | `b5480123f8fafe561705046434de22f3951bbd3245c231f90a24fe1762e6957e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs immutable scan-result sorting and replaces monolithic persisted-page discovery with a worker-owned resumable cursor. It bounds worker entries and owner reconciliation at 256 per turn, retains 8,192 nearest keys, retargets, and tracks cooperative cancellation/retirement.

## Research expansion — gaps and details to deepen

- An entry bound is not an I/O deadline, and the immutable-result defect shows why producer/consumer container contracts matter. Native same-camera recovery, repeated world resets, and visible restoration still need integration evidence beyond scanner/service tests.

## Research expansion — proposed contracts

- Declare immutable result ownership, worker stream lifetime, cancellation generation, stale-result rejection, and owner-thread admission. Expose examined entries, queue mutations, oldest pending age, and discovery-to-visible progress; retain explored/delivered authority rather than manufacturing unknown terrain.

## Research expansion — acceptance checks

- Replay saturated directories, late nearest files, retarget, deletion, permission failures, and rapid world attachment with the actual client. Verify bounded mutation and no stale publication; collect filesystem tails, native coverage, GPU memory, and water images separately.

## Research expansion — source anchors

- Renderer jam 20261028-disk-cursor — persisted LOD discovery and admission
- Changed source

This original shares identical bytes with: [library:Renderer Jams/20261028-disk-cursor/README.md](../../../../project/Renderer-Jams/20261028-disk-cursor/README.md). Both source identities are retained.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-disk-cursor — persisted LOD discovery and admission

**Scope:** renderer-only (LOD metadata lookup and client queue), with the independent engine/water workstreams untouched. **Jam ID:** `20261028-disk-cursor`. **Engine version:** inherited; no `gradle.properties` or generator revision changes. **Source candidate:** [`004a436c7ba01b4662eb25299009b8c41ab9d075`](https://github.com/Shnewbs/Deep-Primal/commit/004a436c7ba01b4662eb25299009b8c41ab9d075), after original main `d96fd950de3fd61ff0c66a9fa27ab5e51d8e6147`, ownership claim `35bcb4aa84bda426ea5692f114e1af482a11fc3f`, first change `8bcde04a8c7ebfebbf0b656cdd3eea365a7058ba`. Pinned final source is checked after all fixes; documentation-only checkpoint can be newer.

## Consumed findings and reproduced defect

The independent [05:30 UTC engine audit](../../engine-hourly-audits/2026-10-28T0530-4cf0f1b/REVIEW.md) discovered a critical integration failure: `LodDiskNearestScan.scan()` returned `List.copyOf(...)`, but the renderer called `keys.sort()` on the immutable result inside a catch-all `Exception` handler, so completed disk scans never entered the persisted restoration queue. That is a source-level and independently executed Java reproduction, not evidence of a live Minecraft restore. The preceding [disk-nearest renderer self-audit](../20261028-disk-nearest/REVIEW.md) scored 4.75/10 and called for incremental indexing, retargeting and full-cap reconciliation. No other feature lease was touched.

## Changed source

- [`LodDiskNearestScan.java`](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java): separate low-priority worker cursor, max 256 directory or recovery entries per granted turn, bounded 8192 nearest keys, canonical filename parsing and signed-extreme exact distance ordering, no on-render-thread filesystem enumeration; cancellation closes the stream on its worker. Existing synchronous `scan()` is retained for tests/offline use only. Completed result is deliberately immutable.
- [`LodDiskIndexService.java`](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java): owner-thread admission via max 256 queue mutations per tick, camera retarget threshold 64 chunks, newly observed recovery priority, smaller/distant old queue entries displaced at capacity, and nonblocking cancellation/wait-for-retirement on the same service.
- [`VoxelLodRenderer.java`](https://github.com/Shnewbs/Deep-Primal/blob/004a436c7ba01b4662eb25299009b8c41ab9d075/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L279-L290): removes the immutable-list `sort()` consumer and one-time scan future; calls dedicated service with the existing edit/ownership guard and resets the service on world exit. Existing native capture, mesh upload, transparency, pixel shaders and water physics are unchanged.
- Three source-controlled production-excluded checks under this jam directory: `LodDiskIncrementalChecks.java`, `LodDiskIndexServiceChecks.java`, and `LodDiskIndexBench.java`. Existing previous `LodDiskNearestScanChecks.java` is reused without modification.

First candidate was rejected by a new CPU-only callback probe: one full-set reconstruction in the client callback could exceed 20 ms. The subsequent **final** code moves selection/discard computation to the worker and applies no more than 256 queue operations in each client tick. Only the final code is scored below.

## Executed source tests (isolated Linux x86_64)

Temurin 25.0.5+8 was verified locally (`java -version`, `javac -version`) and used with `JAVA_HOME` set explicitly; installed OpenJDK 21.0.12.1 was also tested. Commands ran using only Java sources, not NeoForge classes:

```bash
find src tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes21 @sources.txt
java -ea -Xverify:all -cp classes21 com.shnewbs.deepprimal.lod.client.LodDiskNearestScanChecks
java -ea -Xverify:all -cp classes21 com.shnewbs.deepprimal.lod.client.LodDiskIncrementalChecks
java -ea -Xverify:all -cp classes21 com.shnewbs.deepprimal.lod.client.LodDiskIndexServiceChecks
# Repeat with Temurin 25 and --release 25 -d classes25.
```

Both compilers exit 0. The inherited scanner checks pass 9006 assertions per JDK; new entry-bounded worker, sorting, signed extremes, cancellations and negative coordinates pass 8295 per JDK; integration service queue tests pass 522 (Java21) and 538 (Java25) due to asynchronous turn-count variability. Total **17,823 Java21 and 17,839 Java25** focused assertions pass. A deliberately broken integrated consumer which sorts the immutable completion result raises `UnsupportedOperationException` and is correctly rejected (`MUTANT_EXIT=1`). Logs are included in the evidence archive.

A Java25-only warm-directory CPU diagnostic (8205 `.plod` entries, 8192 previous queued keys, 18 runs: 3 warmups/15 measured) reports `measuredCallbacks=1978`, client callback **p50=12,489 ns; p95=41,733 ns; p99=139,360 ns; maximum=1,479,601 ns**. This is not a native Minecraft frame-time measurement. Each test turn includes an explicit sleep, so the total per-index wall time is not a game responsiveness measurement. The initial candidate had >20 ms callback outliers and was excluded; the final bounded variant was separately retested. CPU benchmarks do not guarantee a hard bound when the host stalls or filesystems block.

## Not executed and known limitations

Under the owner's October 27 build override, **full Java25 Gradle 9.2.1/NeoForge build, Minecraft world reopen/save, Windows/Vulkan/OpenGL GPU pixels, shader reload, AMD8060S and NVIDIA RTX, VRAM, frame p50/p95/p99, source-package identities, and JAR production are NOT RUN**. A source commit is not a qualified installer. No GitHub Actions/release, CurseForge or JAR publication was requested/run here.

Current cursor still requires filesystem metadata enumeration over potentially large directories (though each turn examines at most 256 entries); one blocking `DirectoryIterator.hasNext()` cannot be preempted. Cancellation when changing worlds is cooperative, and repeated rapid world resets should be covered by native thread-lifecycle tests. The client queue does at most 256 mutations/tick in this path, but complete frame-time/p99 acceptance is unproven. Stale file deletions or edits arriving between scan and disk reads still rely on existing loaded-revision guards and absent-file recovery. The disk cache only represents explored/delivered terrain; it does not reveal unexplored terrain. Water blending/opacity, native handoff and memory-pressure visual coverage require live validation.

**Next renderer-owned priority:** owner native Java25 world-reopen/teleport/camera-retarget and edit/reload tests, then compare default vs experimental 32 KiB part uploads with actual Vulkan/OpenGL p50/p95/p99 frame and GPU-completion timing, VRAM, triangle/draw counts and visible transparent-water coverage. FSR1 already has a spatial shader; DLSS temporal projection/motion/depth/RTX dispatch remain separate unfinished work. Do not raise a grade on unexecuted integration evidence.


<!-- END PRESERVED SOURCE -->
