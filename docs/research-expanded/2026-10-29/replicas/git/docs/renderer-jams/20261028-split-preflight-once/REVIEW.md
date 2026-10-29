# Expanded replica — docs/renderer-jams/20261028-split-preflight-once/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-split-preflight-once/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-split-preflight-once/REVIEW.md |
| Preserved source SHA-256 | `dd1b63e91581f8c34439896f18ff7fac21eed61fbb99a2925abe9eb7181f034f` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Avoids verifying canonical quad indices twice in the off-by-default splitter by retaining a bounded primitive preflight plan for emission. Unsupported shapes pass through; position, material/alpha, normal, index, order, cancellation, and part limits are preserved. The reported 14–18.9% optional-fixture median benefit has mixed p99 and no default-path GPU qualification.

## Research expansion — gaps and details to deepen

- The validated plan relies on unchanged source arrays between preflight and emission. That ownership assumption should be explicit, and a lower optional-split CPU median cannot justify enabling splitting when additional native draw/descriptor cost is unknown.

## Research expansion — proposed contracts

- Specify immutable input lifetime, bounded plan size, cancellation cleanup, and failure-atomic emission. Keep unsupported topology behavior and default-off policy, and measure complete page/publication cost rather than only removing one validation pass.

## Research expansion — acceptance checks

- Retain exact parity and mutant controls across the 14,154,029 assertions, then test cancellation and source-ownership misuse. Compare default versus split on actual Vulkan/OpenGL with GPU/CPU tails, memory, descriptor/draw counts, and transparent-water images.

## Research expansion — source anchors

- Renderer jam 16 — one-pass canonical-quad split preflight (read-only self-audit)
- Consumed audit, scope and implementation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 16 — one-pass canonical-quad split preflight (read-only self-audit)

**Jam ID:** `renderer-20261028T1952-split-preflight-once`. **Final reviewed production source:** [`0daeacbb7f5c3f26893a0c258341290abb96fa5c`](https://github.com/Shnewbs/Deep-Primal/commit/0daeacbb7f5c3f26893a0c258341290abb96fa5c), source parent `7035445014d6e7823d626b9aa7d951b4288f0243` (scoped claim). Shared engine version `0.0.1-alpha.29-dev.61-coastal-relief-guard.1`; renderer did not edit it. This is the **renderer author's READ-ONLY self-audit of the exact final code commit**, not the independent engine audit.

## Consumed audit, scope and implementation

The independent [19:30 UTC Dev61 audit](../../engine-hourly-audits/2026-10-28T1930-2a341f9/REVIEW.md) saw no renderer acceptance beyond the earlier GPU teardown test; it requested native LOD fault cleanup and actual Vulkan frame times. The [previous renderer jam](../20261028-reset-control-cleanup/REVIEW.md) independently requests native world-switch/device-loss verification. No matching hardware or complete Java25 checkout existed in this isolated run, so a bounded available renderer optimization was implemented instead of another plan-only report. Prior LOD [quad splitter audit](../20261027-quad-bounded-upload/REVIEW.md) had stressed that **32 KiB splitting is opt-in**, may increase draws, and must preserve water/transparency, quad order and memory caps.

The [production splitter at lines 42–84](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java#L42-L84) formerly verified every oversized canonical quad's six-index pattern in preflight, **then repeated the same full scan during emission**. It now records one primitive `int[]` of validated plan counts, bounded by `MAX_PARTS_PER_LEVEL=1024`, and uses its recorded pass-through/split decision during emission without rescanning indices. Invalid/shared/legacy meshes remain unchanged; quad output still copies the same position, color including transparent alpha, normal, triangle indices, bounding interval and original list order. Cancellation checks, 1024-per-detail cap, old fallback, and zero-budget default-off identity remain.

**Actual production changes:** `lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java` (the 8-line net change), plus the production-excluded [source-only regression smoke](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/docs/renderer-jams/20261028-split-preflight-once/LodSplitPreflightSmoke.java#L1-L70). No engine geography, water simulation/render order, generator, shaders, FSR/DLSS, Vulkan backend, graphics menu, saved profiles, tracking cap, CI or other scheduler changed. Scoped claim at `CLAIM.json` was established before source changes and released after this audit.

**Implementation limitation:** Normal `LodMesher` emits at most 1024 quads / 94,208 payload bytes per part while the normal host upload byte budget is at least 256 KiB. This helper only runs a split at an optional `-Ddeepprimal.lod.experimentalPartUploadKiB=32` (default 0); therefore **no default-path, FPS, GPU draw, VRAM or playable-distance gain** is claimed. The short-lived primitive plan incurs its own small allocation; only the second topology scan is eliminated, and worker-owned immutable meshes are assumed.

## Commands and measured evidence (isolated Linux Java 21.0.12.1)

Production baseline Git blob `770dfde8609da73d3e96bdbfad8d9606cc09efda`, candidate production blob `e9ab67d591b5628e61d7b2576d90adced763f1aa`, committed smoke blob `1700b590313caea47c467bd305eeae6a0742dd61`. All three independently matched `git hash-object` of the exact compiled local bytes. These are standalone Java21 source-shape tests with production-compatible primitive `HorizonMesh` and `LodMesher.Levels` stubs, **not a full `LodMesher` or Minecraft module compile**.

```sh
find isolated/source isolated/stubs isolated/tests -name '*.java' | sort > isolated/sources.txt
javac --release 21 -Xlint:all -Werror -d isolated/classes @isolated/sources.txt
java -ea -Xverify:all -cp isolated/classes com.shnewbs.deepprimal.lod.client.LodSplitPreflightSmoke
# LOD_SPLIT_PREFLIGHT_SMOKE_PASS iterations=256 parts=504
java -ea -Xverify:all -cp isolated/classes com.shnewbs.deepprimal.lod.client.LodSplitPreflightChecks
# LOD_SPLIT_PREFLIGHT_PASS assertions=14154029 cases=750 digest=e27a4c5073a0730e
```

The **unchanged baseline also passed the exact 14,154,029 assertions and had the identical output digest**, establishing behavioral parity for 750 randomized mixed mesh/pages plus boundary/invalid/topology, transparency colors, exact raw-float vertices, normals, index rebasing, interruption, level identity, cap and source preservation. A deliberately mutated `plannedParts[meshIndex] != 1` branch strictly compiled but failed the count oracle at assertion 3 (expected exit 1); the mutated source was never committed.

A same-process, order-alternated Java21 diagnostic compiled renamed baseline and candidate source classes and used 5 canonical 1024-quad meshes, 32KiB budget, 1500 warm-up pairs and 700 measured pairs (three complete runs, identical fixture). It measures **CPU split work only**:

| Run | Baseline p50 ns | Candidate p50 ns | Baseline p95 ns | Candidate p95 ns | Baseline p99 ns | Candidate p99 ns |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 62,302 | 53,599 | 167,127 | 145,005 | 330,388 | 268,257 |
| 2 | 58,536 | 47,501 | 158,144 | 138,996 | 428,023 | 375,366 |
| 3 | 56,664 | 46,809 | 129,282 | 113,157 | 189,090 | **216,470** |

Medians decreased **14.0%, 18.9%, 17.4%** in this isolated opt-in fixture; third p99 worsened. Median Java-thread allocated bytes per call decreased only **534,632 → 534,608 B** (24 B), so this is **not** a major allocation improvement. Timing may reflect JIT/GC/host noise and does not measure device upload, GPU fences, whole frame, water pixels or native default. Local benchmark logs and baseline/candidate tests are in the companion source-only evidence ZIP.

## Six equal-weight grades (0–10)

| Category | Score | Exact pinned basis / missing evidence |
| --- | ---: | --- |
| Distance / LOD correctness, usable coverage | **6.50 carried** | [Existing radius and handoff](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L516-L556) unchanged; no native unseen-delivery, travel or distance admission |
| Mesh generation/reuse/scheduling | **6.95 (+0.10)** | [One topology validation and bounded plan](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java#L42-L85); 3 CPU-only A/B fixtures with lower medians, opt-in only; no evidence for normal-frame gains |
| GPU culling/submission/memory | **4.30 carried** | [Unmodified indexed draw path](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L789-L819); no GPU counters/batching/VRAM |
| Actual AMD FSR / NVIDIA DLSS | **5.00 carried** | [Upscaling contract](../../UPSCALING.md): FSR1 implemented; temporal FSR3/4 and live NGX DLSS unavailable |
| Visual/backend/settings compatibility | **5.25 carried** | [Default-off split](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L28-L33); CPU exact vertex/alpha/normal/index ordering, but no Vulkan/GL pixels/water validation |
| Validation & measured performance | **5.30 (+0.05)** | [Committed smoke](https://github.com/Shnewbs/Deep-Primal/blob/0daeacbb7f5c3f26893a0c258341290abb96fa5c/docs/renderer-jams/20261028-split-preflight-once/LodSplitPreflightSmoke.java) plus Git-identity-matched 14,154,029 randomized assertions, negative mutant, paired CPU fixture; no Java25, native GPU or frame p50/p95/p99 |
| **Mean** | **5.55 / 10 (+0.02)** | **33.30/60**, prior renderer **5.53/10**. Target >8.5 unmet; independent whole-engine remains **5.0/10 carried**, NOT regraded. |

### Unrun acceptance and next repair

Temurin JDK25 archive/restored path absent from this isolated workspace; system JDK21 was used. October 27 owner override retains full Java25 Gradle/NeoForge, complete source checkout, packaged JAR CRC/class/source identity, proprietary runtime packaging, Windows Vulkan/OpenGL/named AMD/NVIDIA GPU, water depth/transparent ordering, resize/reload/world-switch/save/reopen/shutdown, actual GPU counters/VRAM/frame-time p50/p95/p99 and DLSS/temporal FSR dispatch with owner. **No new JAR, release, source-to-native pass or RTX validation claimed.** Next focus: owner same-scene Vulkan/OpenGL default-versus-32KiB part A/B including actual GPU draw/descriptor/VRAM/frame tails, and native LOD teardown device-loss checks before changing default upload behavior.


<!-- END PRESERVED SOURCE -->
