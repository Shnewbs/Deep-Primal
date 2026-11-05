# Expanded replica — docs/renderer-jams/20261029-submit-stage-timing/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-submit-stage-timing/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261029-submit-stage-timing/REVIEW.md |
| Preserved source SHA-256 | `488995d6787c8d09039bf4745290f3416e711ee7a53d751caef41dc92e9d1a30` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds bounded 240-observation CPU transform, draw-API/close, and combined submit-stage histories, including failures after transform writing and successful returned work before a later failure. Telemetry is opt-in, saturating, and reset with world state. The source check and helper suite establish instrumentation semantics, not a measured native frame benefit.

## Research expansion — gaps and details to deepen

- Preparation failures were omitted at this historical pin and are covered by the later pretransform repair. Independent stage percentiles still lack frame correlation and exclude empty/culling work; API return cannot establish GPU completion or hardware draw count.

## Research expansion — proposed contracts

- Link the pretransform successor and define sample populations, phase boundaries, partial-return counts, and unavailable states. Add frame/world/device identifiers and separate asynchronous GPU timestamps; do not add unrelated percentile values to estimate a frame percentile.

## Research expansion — acceptance checks

- Retain 20,000 scenarios and 29,276 probe assertions, then capture exact-artifact batched/direct paths with failures and no-visible frames. Compare telemetry-on/off overhead, actual GPU/frame distributions, memory, and water images before choosing a submission optimization.

## Research expansion — source anchors

- Renderer jam 21 — read-only self-audit of submitted source
- Consumed audit and scoped change

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 21 — read-only self-audit of submitted source

**Jam ID:** `renderer-20261029T0054-submit-stage-timing`. **Exact implementation pin:** [`42d03be8bf4526c8b2ffd7465b5de95f234035c4`](https://github.com/Shnewbs/Deep-Primal/commit/42d03be8bf4526c8b2ffd7465b5de95f234035c4). **Claim parent:** `dc2f74516d8c14fa6e3aa5282788ebe02c734d28`. **Implementation parent:** `a83c3ef6ccc4636aca58d1998dcd84df225e0e0f` (scoped claim). Intermediate implementation `4d8105d547964721d9814e8a222cc3e3270ac38e` was refined for telemetry-off hot-path cost before the **final** read-only review. **Version in `gradle.properties`:** `0.0.1-alpha.29-dev.66-water-domain-admission.1` (unchanged; source-only renderer checkpoint).

This is a **renderer jam self-audit**, not a new independent whole-engine grade. Implementation was complete before the scored inspection; no production source changes occurred during the scored review.

## Consumed audit and scoped change

Consumed [renderer jam 20's audit](../20261028-triangle-counter/REVIEW.md), which explicitly left native p50/p95/p99 CPU/GPU submission metrics, API-call counts, water pixels, travel/edit/teleport and owner JAR unverified, plus [the latest independent Dev66 review](../../engine-hourly-audits/2026-10-28T1735-cf254e27/REVIEW.md), which retained the separate historical whole-engine grade at 5.0/10 without renderer performance acceptance. Current main and the scoped ownership claim were checked before each integration; no shared GPU measurement reservation or other writer's work was changed.

Production changed:
- [`LodSubmissionProbe.java` lines 12–65](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodSubmissionProbe.java#L12-L65), Git blob `ce6edd9927d1beed278c197f9e27621d7ebb412f`: 240-sample bounded rolling percentile windows for CPU transform setup, draw-pass API recording/close, and combined submit stages; completed/failed passes; successfully returned API records and calls; validated observations and saturating lifetime counters.
- [`VoxelLodRenderer.java` lines 781–870](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L781-L870), blob `a491d3e04795deca6878ec2f03f6720532df8b75`: brackets transform/command stages with `System.nanoTime` only under `TELEMETRY`; credits API records and calls only after successful returns, including partial passes; observes draw-pass errors in a `finally`; leaves all existing draw calls/ordering/material/water and 4,096-record hard cap unchanged. [Reset around line 889](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L879-L897) clears stage samples on world unload/reopen.
- [`LodClient.java` lines 61–75](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L61-L75), blob `3a28384f3b8bf94b13e4064e573eb2b8ecd1bab8`: exposes CPU p50/p95/p99 stage timings, completed/failed passes and returned records/API calls, with explicit non-GPU semantics.
- [Isolated deterministic regression](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/docs/renderer-jams/20261029-submit-stage-timing/LodSubmissionProbeChecks.java), blob `241f91c0207b278e59dcc8c074071d08078d365d`, plus [README](README.md). No terrain, finite water, texture, FSR provider, shader, graphics module, Vulkan adapter or unrelated automation changed.

**Semantics and limitations:** The 240-window samples cover **LOD-visible** passes only and begin at the start of transform setup; exceptions **before successful `writeTransforms`** are not sampled. CPU time includes Java and synchronous API/driver call latency, not GPU fences or actual GPU execution. A draw-call API returning is not GPU completion or multi-draw indirect. Failed render passes after transforms are included in latency windows; successful API calls before a later failure are still credited. Telemetry is opt-in (`-Ddeepprimal.lod.telemetry=true`); no native always-on instrumentation performance result exists.

## Checks actually executed

Isolated host: Linux OpenJDK/javac **21.0.12.1**. `RollingTiming.java` was copied from exact unchanged engine-core blob `0be7512957537596295ba204f8f1dfc176aa686f`. The isolated helper and regression local Git blobs **match pinned production** (`ce6edd...`, `241f91...`). **No full Minecraft source compilation**.

```sh
find src tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSubmissionProbeChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodSubmissionProbeChecks
```

**PASS:** two source-identity-matched Java21 runs, each **20,000 scenarios / 29,276 assertions**; randomized 240-window percentile oracle, success/failure counting, returned records/calls, validation-before-mutation and world reset. An intentionally broken helper that omitted failed-pass accounting **compiled** but **FAILED as expected** at assertion 33. **PASS:** 20/20 read-only pinned production-source integration checks (guards, stage order, API-return crediting, reset, diagnostics, unchanged renderer-only scope). These checks do not replace a full module compile or live GPU pass.

**Artifact:** `DeepPrimal_Renderer_SubmitStageTiming_20261029_42d03be_SourceEvidence.zip`; size **6,030 bytes**, SHA-256 `adce08dc56d9596e45ab42d97706bedbdd8fec12d79b7ef26278282f9111cd8e`; ZIP CRC and all 8 manifest member hashes verified. Successfully uploaded to Library `/Deep Primal/Renderer Jams/`. This is test/source evidence, **not a JAR**.

## Six-category, equal-weight renderer score

| Category | /10 | Pinned evidence and required gate |
|---|---:|---|
| Distance/LOD correctness and usable coverage | **6.50 carried** | [Existing page visibility and render](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L774-L790); no new usable distance/negative-coord live GPU gate |
| Mesh generation/reuse/scheduling | **6.95 carried** | [Earlier constrained optional splitter](../20261028-split-preflight-once/REVIEW.md); no new mesher scheduling work |
| GPU culling/submission/memory | **4.45 carried** | [Current synchronous API submission](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L808-L855) and [new counters](https://github.com/Shnewbs/Deep-Primal/blob/42d03be8bf4526c8b2ffd7465b5de95f234035c4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L61-L75); no measured GPU driver/VRAM gains or true MDI |
| Actual AMD FSR / NVIDIA DLSS | **5.00 carried** | [Official FSR1 active, incomplete temporal/DLSS renderer contract](../../UPSCALING.md); no FSR3/4 or RTX dispatch proof |
| Visual/backend/settings compatibility | **5.25 carried** | Diff only renderer diagnostics and opt-in CPU observation; transparent-water GPU pixels and native Vulkan/OpenGL remain unrun |
| Validation and measured performance | **5.55 (+0.05)** | 29,276 Java21 regression assertions, failed negative mutant, 20/20 pinned source checks; new CPU instrumentation but **no measured native frame performance** |
| **Equal-weight mean** | **5.62 (+0.01)** | `33.70 / 6 = 5.6167` compared with prior 5.61. This is **not** the external engine grade; Dev66 independently carried 5.0/10. |

## Unrun owner gates, remaining defects and next repair

JDK25 directory/archive were **not mounted** in this execution workspace; `java -version` and `javac -version` showed **21.0.12.1**. GitHub name resolution failed in isolated Linux, so pinned source was read and guarded commits were written through the connected GitHub API; no complete checkout or Gradle dependency graph was available. Under the October 27 owner override, full Java25/Gradle 9.2.1/NeoForge, native Windows client, JAR production, nested/outer CRC, source/packaged identity, device/native payload inspection and GitHub release were **intentionally not run**.

Native Vulkan/OpenGL GPU pixels, alpha ordering/water transparency, driver/device timestamps, CPU/GPU whole-frame p50/p95/p99, draw/descriptor counts, VRAM/RAM, 125/192/512 useful distance, native batched on/off and optional 32KiB split A/B, world switch/reload/resize/shutdown, AMD FSR temporal and NVIDIA RTX DLSS are **unaccepted**. Current stage counters are instrumentation awaiting owner-run capture, not an observed FPS win. Failing transform setup is not yet observed by this stage probe; end-to-end failure instrumentation should add a separate early-abort observation in a later scoped jam, without polluting successful-pass p50/p95/p99.

**Next:** on owner hardware, capture same seed/resolution/camera, LOD+transparent-water Vulkan/OpenGL trace for batched on/off (frames p50/p95/p99, returned API calls, GPU timestamps/VRAM and pixels). Use the staged CPU transform-vs-draw breakdown to choose a measured GPU-submission/mesh fix. Preserve actual FSR1 and pursue real FSR temporal/DLSS temporal inputs once native evidence identifies bandwidth and projection constraints.


<!-- END PRESERVED SOURCE -->
