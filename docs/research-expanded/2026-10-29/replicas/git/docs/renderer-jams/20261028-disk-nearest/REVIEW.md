# Expanded replica — docs/renderer-jams/20261028-disk-nearest/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-disk-nearest/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-disk-nearest/REVIEW.md |
| Preserved source SHA-256 | `df0b6f414b61870f22f271be0ab6f877ad094f1454683607f3aedfc8292e881e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Selects the closest 8,192 canonical persisted pages with exact signed-coordinate ranking instead of keeping the first directory entries. The Java25 addendum reruns the same pinned scanner source and records a warm-directory CPU diagnostic without changing product code. The review and Java25 supplement preserve source-only limits and the separate historical engine grade.

## Research expansion — gaps and details to deepen

- This historical all-in-one scan is superseded by the cursor and immutable-admission repair. A stronger compiler run does not repair an integration contract outside the scanner, and warm-directory latency cannot be compared directly with unrelated Java21 runs or game frames.

## Research expansion — proposed contracts

- Add explicit superseding links and separate helper correctness, renderer consumption, incremental work, and native visible restoration. Record filesystem/cache condition, directory size, retained cap, JDK, and exact source/test hashes for every scanner measurement.

## Research expansion — acceptance checks

- Retain 9,006 real-directory assertions under both compiler targets, then verify actual immutable result admission and bounded cursor integration. Exercise late nearby files and fixed-camera refresh in the native artifact before claiming useful far-terrain recovery.

## Research expansion — source anchors

- Renderer self-audit: 20261028-disk-nearest
- Prior findings and ownership

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer self-audit: 20261028-disk-nearest

Product source commit [89b8bb0](https://github.com/Shnewbs/Deep-Primal/commit/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb), parent renderer claim 70fd4a72. Main at initial preflight c532d6bd. Inherited shared engine version 0.0.1-alpha.29-dev.45-v8sections.1 (unchanged). This is the author's read-only six-category review of the committed candidate, not the independent whole-engine audit. No product code was changed during scoring.

## Prior findings and ownership

The previous [page upload jam](../20261028-page-upload-latency/REVIEW.md) scored 4.67/10 and requested measured native camera/travel/edit and upload A/B checks. The independent [04:32 UTC auditor report](../../engine-hourly-audits/2026-10-28T0432-09a57c1/REVIEW.md) retained LOD GPU and V8/native gates. The root AGENTS.md is absent. Current renderer, work-state, water-work-state, LOD architecture, upscaling guidance, Git claims and build ownership were inspected. The separate engine builder owned V8 density, while the water task owned HydrologyRuntime; this jam's bounded claim owns only the renderer source and dedicated evidence folder. Neither other job nor its schedules/outputs were changed.

The old [disk discovery code](https://github.com/Shnewbs/Deep-Primal/blob/c532d6bd101768fb91a14264ec3e23658523bd26/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java) stopped at the first 8,192 filesystem filenames regardless of proximity. A nearby saved page found later could be excluded for the whole world session.

## Actual source increment

- [LodDiskNearestScan.java lines 25–53](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L25-L53): new background worker utility retains the closest 8,192 canonical saved pages using a bounded max heap; directory order cannot override spatial priority. [Lines 56–101](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L56-L101) reject ambiguous filenames and rank signed-extreme coordinates using exact unsigned 128-bit squared distance, stable ties and a Chebyshev ring.
- [VoxelLodRenderer.java lines 279–293](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L279-L293): asynchronously start once the player exists, snapshot camera coordinates on the client thread, and retain the existing bounded disk restoration queue, edit guards, worker cancellation and renderer lifecycle. [World reset lines 835–846](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L835-L846) clears the scan-start flag.
- [LodDiskNearestScanChecks.java](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/docs/renderer-jams/20261028-disk-nearest/LodDiskNearestScanChecks.java) is the focused production-excluded filesystem regression.

Only two production renderer files and one test changed. No terrain generation, hydrology/chemistry, saved generator revision, shaders, water transparency, FSR/DLSS, textures, GPU mesh formats or unrelated settings were changed.

## Test commands and result

Linux x86_64, javac/java OpenJDK 21.0.12.1; The Java25 archive was absent locally at initial review; it was subsequently restored from the owner-provided Library file for focused Java25 tests (see follow-up below).

    find source tests -name '*.java' | sort > sources.txt
    javac --release 21 -Xlint:all -Werror -d classes @sources.txt
    java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodDiskNearestScanChecks
    LOD_DISK_NEAREST_PASS assertions=9006 latePrefix=8205 extremes=700 maxRetained=8192

The suite creates 8,204 far filenames then a later near filename, verifies correct nearest 1/7/512/8192 selections against an independent BigInteger reference, uses 700 extreme signed-coordinate files and checks malformed paths, cancellation, missing directories and bounds. A separately compiled first-N-files mutant is correctly rejected (exit 1, AssertionError: late nearest file missing). The actual old first-8192 source anchor is pinned above.

A separate Java21 diagnostic repeatedly scans 8,206 cached-directory entries after warmup (21 runs): selection wall-time p50 **8.592 ms**, p95 **14.075 ms**, p99 **16.194 ms**. These are worker metadata times, excluding file creation/cold I/O, not frame/FPS/GPU measurements. The source helper and registered test match the exact local executed Git blob hashes aa91a4f7fc51e01ca2df9d5d8f88ee116075cf7f and 7c7fde38d7060727647379fbdc83b7e79fd12470. The integrated renderer Git blob c8cb68c86537d90d406228a8ec58457cb3f50492 was read back, confirming player-coordinate worker submission, old prefix removal and reset behavior.

Not run: complete Java25 Gradle/NeoForge/modules, JAR build/outer-nested archive CRC/platform payload/source-to-bytecode checks, Minecraft native save/reopen, long camera travel/edit/world switch, real Vulkan/OpenGL/water pixels, actual GPU fences/VRAM and frame p50/p95/p99, AMD driver or RTX DLSS. The owner performs these builds under the October 27 override. No GitHub Actions, installer, prerelease or CurseForge publication.

## Six-category conservative renderer self-audit

| Renderer category | Previous /10 | Current /10 | Pinned evidence and open gate |
|---|---:|---:|---|
| Distance / LOD correctness and usable coverage | 4 | **4.5** | The real-filesystem late-near regression and [nearest selector](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L25-L53) close prefix omission; native camera retarget and distant unseen delivery absent |
| Mesh generation / reuse / scheduling | 6 | **6** | [Worker hookup](https://github.com/Shnewbs/Deep-Primal/blob/89b8bb0bb78e809bf4a5c3a5a0480699fa4a05cb/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L279-L293) keeps disk I/O off render thread; existing mesher/optional splitter unchanged |
| GPU culling / submission / memory | 4 | **4** | Actual draw/VRAM/fence acceptance not run; no GPU path modified |
| Actual FSR / DLSS | 5 | **5** | Carried [FSR1 spatial and inactive NGX](../../UPSCALING.md); no new temporal inputs/RTX dispatch |
| Visual / backend / settings compatibility | 5 | **5** | Source preserves render path; Vulkan/OpenGL transparency/reload pixels not run |
| Validation / measured performance | 4 | **4** | 9,006 real-file checks, deliberately rejected mutant and 21 warm metadata scans; no full Java25 Gradle/native/GPU frame data |
| **Average** | **4.67** | **4.75** | **+0.08** (28.5/60 vs 28/60); target >8.5 not met |

Whole-engine independent grade **5.0/10** remains carried only, not freshly regraded.

## Remaining limitations and next repair

The retained key set is bounded to 8,192, but this first correction still enumerates the whole filesystem in one asynchronous worker task (unbounded metadata I/O duration). It selects around the initial camera and does not re-index after long travel. A full older recovery queue can also exclude new near keys. Implement a properly cancellable **fixed-entry-budget resumable cursor**, camera/session retarget and full-queue nearest-key reconciliation while preserving saved revisions/edit guards. The unpromoted R57 design was not silently copied. Then run native travel/LOD coverage and owner default-vs-experimental-32KiB upload A/B with full frame/GPU/page latency, VRAM, draw count and transparent water.

Source-only ZIP: DeepPrimal_Renderer_DiskNearest_20261028_89b8bb0_SourceEvidence.zip, 7,355 bytes, SHA256 049d0515d872017ecccf3fa624a1db8430314a875bde45c68aee90d95169593d, nine CRC-valid entries. Verified Library destination /Deep Primal/Renderer Jams/; library ID libfile_c5e928a962c881919cbfa5ad2cfe1e37. **No installable JAR.**

## Exact-code Java25 focused validation addendum — 2026-10-28 05:05 UTC

**No source code changes or score revision.** The owner-provided Temurin 25.0.5+8 Linux archive was retrieved from Library, path-checked (486 archive entries), extracted into this isolated validation workspace and verified using java -version and javac -version. The same pinned Git-hash-matched scanner and test source were compiled with Java25 --release 21 and again --release 25, both with strict lint and warnings as errors. Each Java25 run passes **9,006 assertions**, including the late-prefix and extreme-coordinate gates. A 21-sample warm-directory diagnostic under Java25 measured p50 **8.237 ms**, p95 **13.074 ms**, p99 **13.456 ms** for 8,206 metadata files; this is a separate non-comparable CPU-host diagnostic and **not** a Java21-vs-Java25 speedup, native game frame or GPU result. Gradle, full Minecraft/NeoForge modules, JAR build and Vulkan/OpenGL/AMD GPU remain explicitly **unrun**.

The focused Java25 evidence archive contains the extracted compiler/test result logs and commands: **DeepPrimal_Renderer_DiskNearest_Java25_20261028_89b8bb0_Evidence.zip**, 2,348 bytes, SHA256 **40e0d5e67c003689593c5d5776c0a5523d5ec36de0cd47415a43ed4f4aa4615b**, seven CRC-valid entries, Library ID **libfile_307174058df08191b9e22fa49ae86f80**. This supplement does not change the committed renderer code SHA 89b8bb0, six category scores or independent engine grade.


<!-- END PRESERVED SOURCE -->
