# Expanded replica — docs/renderer-jams/20261027-quad-bounded-upload/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-quad-bounded-upload/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261027-quad-bounded-upload/REVIEW.md |
| Preserved source SHA-256 | `9347f8bfc6f165538cb66df7d9d55a357564a234920649615f63064559a0f8ec` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Defines an off-by-default 32 KiB canonical-quad splitter that preserves vertex/material/water/index order and payload bytes while rebasing indices. Preflight caps emitted parts per detail level; unsupported shapes pass through. Standard 94,208-byte parts already fit the normal upload allowance. The 282,240 shape assertions do not establish a driver or FPS improvement.

## Research expansion — gaps and details to deepen

- Smaller parts can increase draw and descriptor overhead without reducing total payload. The experiment needs a complete page-publication and GPU lifetime comparison, not only exact CPU geometry or an assumption that the default path needs splitting.

## Research expansion — proposed contracts

- State the supported topology and immutable-input preconditions, failure-atomic output publication, part-count ceiling, and fallback on unsupported or cancelled work. Measure pending replacement overlap and preserve conservative bounds and transparent ordering.

## Research expansion — acceptance checks

- Retain exact canonical-quad and alpha/index parity checks, then compare default and split modes on identical native Vulkan/OpenGL scenes. Record page latency, actual GPU/CPU tails, draw/descriptor counts, memory peak, and water pixels before considering a default change.

## Research expansion — source anchors

- Deep Primal renderer coding jam — optional bounded-quad LOD upload
- Consumed evidence and actual source change

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal renderer coding jam — optional bounded-quad LOD upload

**Jam ID:** `20261027-quad-bounded-upload` (renderer only). **Pinned code commit:** [`f569a31cd3e59593c0343a8617fa0934289bb3e9`](https://github.com/Shnewbs/Deep-Primal/commit/f569a31cd3e59593c0343a8617fa0934289bb3e9). **Code parent/claim:** `43694cd262ce64f64cf0c84b5f745101cc1e9396`; underlying inspected code parent `3d4980de6202b65838e3e3b86f6ccb65a7353476`. **Shared version:** `0.0.1-alpha.29-dev.43-v8partition.1` (not modified by renderer). This is the **author's read-only renderer self-audit**, not the independent engine audit. No code changed during scoring.

## Consumed evidence and actual source change

Prior [part-upload-telemetry review](../20261027-part-upload-telemetry/REVIEW.md) reported **4.67/10**, called for named-hardware camera/travel/edit part-size and latency measurements, and identified oversized indivisible uploads as an *unaccepted* potential problem. The latest independent [02:33 UTC engine audit](../../engine-hourly-audits/2026-10-28T0233-2805140/REVIEW.md) confirmed telemetry code but no actual GPU/frame measurements. The root `AGENTS.md` was absent. Current engine/water work-state claims were separately scoped to V8 density and water callbacks, with no conflicting renderer file lease. Renderer ownership was established in this jam's `CLAIM.json`.

A close review of [`ClientPerformanceConfig.UPLOAD_KIB`](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/ClientPerformanceConfig.java#L54-L68) finds the *normal* upload byte limit is at least **256 KiB**, while [`LodMesher.Builder`](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodMesher.java#L236-L307) emits at most **1,024 quads / 4,096 vertices / 94,208 payload bytes** per part. Therefore **the existing default byte limit does not require splitting these generated parts**. No fabricated GPU stall or default performance fix is claimed.

Instead, [`LodMeshPartSplitter.java`](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java) implements an opt-in, worker-thread 92-byte-per-quad splitter for controlled future GPU experiments. It accepts only verified independent four-vertex/six-index triangles and no height array, never changes positions/colors/normals/index triangle order, rebases local unsigned-short-compatible indices, conservatively retains the original bounds, and preserves exact `gpuBytes()` totals. Legacy/shared-vertex meshes and insufficiently representable pages fall back unchanged; it preflights a **1,024-parts-per-detail** ceiling before allocations and observes interruption.

[`VoxelLodRenderer.java`](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L28-L35) adds `-Ddeepprimal.lod.experimentalPartUploadKiB=32` (default **0, off**, clamped 0–256); only its **two worker-owned mesh build paths** [L377](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L374-L379) and [L438](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L435-L440) opt into the splitter for fresh/cached pages. It changes no default GPU part/draw count, water simulation, generation, saved profiles, terrain geology, shader, FSR/DLSS, graphics setting values, main engine builder files or approved art. Opt-in may **increase** draw calls, CPU allocation and total frame time; it must not be advertised as a speedup.

## Fresh bounded checks

Isolated Linux x86_64 Java/javac **21.0.12.1**. The owner-provided Temurin Java25 directory and archive were not mounted, so **Java25 was not verified** and whole-root Gradle/Minecraft/SDK/GPU tests were left to the owner per the October27 override.

```sh
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodMeshPartSplitterChecks
# LOD_MESH_PART_SPLITTER_PASS assertions=282240 randomizedSingle=1800 randomizedPages=300 optIn32KiB=true
```

The exact local production helper Git object is **`770dfde8609da73d3e96bdbfad8d9606cc09efda`**, independently re-read at the code commit. Test source in this jam's folder is the exact executed Java source; the two API-compatible `HorizonMesh`/`Levels` shapes were local test doubles, not real Minecraft or a full module compile. Tests compare every ordered source-vertex triangle's raw float bits, alpha/color and normals, total GPU-byte estimates, pass-through identity, shared/legacy fallback, negative budgets, 1,025-part admission guard, cancellation and source immutability. A private mutant omitted local index rebasing, compiled, and was **rejected** with exit code1 (bounds fault). No mutant was committed.

A diagnostic **CPU-only** JDK21 1,024-quad single-mesh probe, after 800 warm-ups and with 400 recorded 32KiB partition samples, produced **3 parts**, **94,208 conserved GPU-payload bytes**, p50 **34,883 ns**, p95 **76,335 ns**, p99 **183,867 ns**, max **1,461,268 ns**. This records worker-side split cost for one fixture, not actual GPU submission/completion, frame time, FPS, VRAM or a matched driver improvement.

**Unrun:** Java25 and Gradle root/module builds; real compiled `LodMesher` plus native player chunks; outer/nested JAR CRC/source-to-bytecode/fixture exclusion, Windows payload and resources; Vulkan/OpenGL pixels and source/target water visibility; scene travel/edit/reload/resize/world-switch/save/shutdown; measured GPU memory and upload time; AMD/NVIDIA proprietary hardware, FSR/DLSS RTX dispatch, actual full CPU/GPU frame p50/p95/p99. **No owner JAR or development release was built, uploaded or relabeled.**

## Six equally weighted renderer categories

| Renderer category | Prior /10 | Current /10 | Verified evidence and missing acceptance |
|---|---:|---:|---|
| Distance/LOD usable coverage | 4 | **4** | [Live LOD selection](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L519-L560) unchanged; no native distant unseen delivery/travel/negative seam coverage |
| Mesh generation/reuse/scheduling | 6 | **6** | [New bounded CPU split helper](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java#L39-L96) exact payload controls; opt-in only, no driver/frame acceleration measured |
| GPU culling/submission/memory | 4 | **4** | [Unchanged resident-byte/admission logic](https://github.com/Shnewbs/Deep-Primal/blob/f569a31cd3e59593c0343a8617fa0934289bb3e9/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L623-L741); no actual driver resource-count/VRAM validation or native hardware |
| Actual FSR/DLSS | 5 | **5** | [Existing FSR1 spatial and incomplete DLSS NGX](../../UPSCALING.md) carried; no new temporal inputs/dispatch or RTX GPU evidence |
| Visual/backend/settings | 5 | **5** | Default off; source preserves draw/vertex ordering. Actual water transparency, GL/Vulkan pixel and reload/resize controls unrun |
| Validation/measured performance | 4 | **4** | 282,240 source-shape Java21 assertions + rejected mutant + CPU-only diagnostic; Java25/current product and actual GPU/frame data absent |
| **Mean / change** | **4.67** | **4.67** | **Δ 0.00**, target **>8.5** remains unmet |

The independent whole-engine baseline remains **5.0/10 carried** (not regraded).

## Next renderer jam

Use owner Vulkan/OpenGL with `-Ddeepprimal.lod.telemetry=true` for identical seed/cameras/resolutions, cold/warm/load/teleport/edit/water/resize/reload/world switch: collect accepted CPU part size/cost distributions, draw/descriptor counts, resident and pending bytes, actual GPU completion/VRAM and whole-frame p50/p95/p99. Run A/B against the **default-off** source and opt-in 32KiB experimental partition on the same scene, checking water translucency/culling/handoff and frame outliers. Do not enable partition by default until representative gains exceed extra draw/allocation costs. Separately continue native temporal inputs required for real DLSS/FSR upgrades, leaving water and terrain generation to their owners.


<!-- END PRESERVED SOURCE -->
