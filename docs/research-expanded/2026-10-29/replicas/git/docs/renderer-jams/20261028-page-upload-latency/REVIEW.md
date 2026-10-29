# Expanded replica — docs/renderer-jams/20261028-page-upload-latency/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-page-upload-latency/REVIEW.md) |
| Source identity | git: docs/renderer-jams/20261028-page-upload-latency/REVIEW.md |
| Preserved source SHA-256 | `36985e22bdced80ca3f11c2dec65047384a90d65cd4d16e320e01b7a9deb0632` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds 240-observation page admission-to-cache-commit elapsed-time, extraction-pass count, and accepted-payload windows, with successful completions separated from stale and memory drops. The final source records after successful cache insertion and old-resource cleanup. The receipt’s source-shaped and helper checks are explicitly not a qualified JAR or native transfer measurement.

## Research expansion — gaps and details to deepen

- Wall elapsed page latency includes queueing and multiple frames; it is not GPU transfer time. Completion may need a separate visible/publication stage, and failed cleanup must not silently create a successful observation or lose allocation ownership.

## Research expansion — proposed contracts

- Add page identity, source generation, queue wait, accepted upload, cache publication, first visible frame, and GPU completion events. Preserve distinct drop reasons, saturation, and zero-sample semantics, then correlate the probe with part and frame histories.

## Research expansion — acceptance checks

- Retain the 36,117-assertion randomized and drop-classification tests plus source ordering checks. In the exact artifact, compare default and optional split uploads with page-to-visible latency, GPU timestamps, memory peak, complete-frame tails, and water images.

## Research expansion — source anchors

- Deep Primal renderer jam — bounded LOD page-upload completion telemetry
- Consumed findings, ownership and actual changes

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `CLAIM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-page-upload-latency/CLAIM.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal renderer jam — bounded LOD page-upload completion telemetry

**Jam:** `20261028-page-upload-latency`. **Pinned final product source:** [`0f1ef9cd0f0e739a6bd757421105df42b3f85d44`](https://github.com/Shnewbs/Deep-Primal/commit/0f1ef9cd0f0e739a6bd757421105df42b3f85d44); prior renderer code parent `fa9e9d5576c299cf7e8cd630fd1c2a479f2f3de5`. Initial commit `72b08b3` was corrected before this review so page observations occur **after** successful cache insertion/old-resource cleanup. **Shared mod version:** `0.0.1-alpha.29-dev.45-v8sections.1` (engine owned, not changed here). This is the **author's read-only renderer self-audit of the exact final product commit**, not the independent whole-engine audit. No product code was edited during scoring.

## Consumed findings, ownership and actual changes

The prior [quad-bounded-upload self-audit](../20261027-quad-bounded-upload/REVIEW.md) scored **4.67/10** and requested comparable camera/travel/edit telemetry of default versus opt-in 32KiB parts. The latest [03:30 UTC independent engine audit](../../engine-hourly-audits/2026-10-28T0330-b14f8ba/REVIEW.md) confirmed that the optional splitter is a CPU-only experiment; no driver/frame improvement was measured. Current main, the absent root `AGENTS.md`, work-state/water claims, LOD architecture and upscaling guidance were checked. The active builder held only V8 section/density, root state and gradle.properties; no renderer/LOD writer owned the affected files. The scoped [CLAIM.json](CLAIM.json) was recorded and released. No unrelated schedules, physics, chemistry, saves, art, modules or texture resources changed.

Four committed files changed (two new):

- [LodPageUploadProbe.java lines 13–58](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageUploadProbe.java#L13-L58) adds fixed-capacity 240-sample windows for **admission-to-cache-commit wall elapsed nanoseconds**, number of extract passes required per committed page and accepted GPU payload bytes. Completed-page counters are separate from stale-revision and memory-denial drops, and saturate on overflow. Only fully committed pages enter percentiles; reset clears old-world observations.
- [VoxelLodRenderer.java lines 113–137, 640–660, 733–750, 833–840](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L640-L750) timestamps a newly admitted pending page, counts bounded per-frame upload turns, records the two actual discard paths and records successful completion **only after** cache insertion, previous-page cleanup and upload-commit bookkeeping. All observation paths are gated behind `-Ddeepprimal.lod.telemetry=true` (off by default). No upload cap, draw order, part layout, GPU lifetime, distance radius or water mesh was changed.
- [LodClient.java lines 70–79, 117–124](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L70-L79) exports labeled p50/p95/p99/max latency, frames, accepted bytes, completions and rejection counts via existing diagnostics. Crucially, **wall latency includes pacing/pauses, not GPU completion, full frame or worker generation time**.
- [LodPageUploadSmoke.java](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/docs/renderer-jams/20261028-page-upload-latency/LodPageUploadSmoke.java) is a production-excluded standalone smoke gate.

## Focused executed checks (Linux x86_64, Java/javac 21.0.12.1)

```sh
find source tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d classes @sources.txt
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadProbeChecks
# LOD_PAGE_UPLOAD_PROBE_PASS assertions=36117 randomizedObservations=60000 capacityVariants=4 seedVariants=3
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodPageUploadSmoke
# LOD_PAGE_UPLOAD_SMOKE_PASS
```

The extended fixed-seed oracle validates capacities 1/17/64/240, nearest-rank rolling p50/p95/p99/max for three independent measures, successful-only samples, drop classification, reset, invalid inputs and saturating counters. A deliberately malformed private variant routes MEMORY drops to the STALE counter; it compiles and is correctly rejected (exit 1, assertion `successful-only counts`). No mutant was committed. The helper/committed smoke and original `RollingTiming` match exact Git blobs `ca5535ad8d6ce443455c266b821ccd51e1dc8842`, `ba5a8d5686e6da74b83af3ad23c10df10e58f6b9` and `0be7512957537596295ba204f8f1dfc176aa686f`. Read-back of the final `VoxelLodRenderer` `ec9c1785ada7bee01f24603a1e89d18d979ff8fc` and `LodClient` `ac486c40240be52ff993620d8472047c6f9a459e` passes six source-integration anchors, including commit-after-cache ordering. The tested Java helper and test source plus logs are in the source-only evidence ZIP (9 CRC-valid entries, 6,765 bytes; SHA256 `fb7e2192189eb6a54df397f1caec58da5e0c5131eb7a502424bd4a4a408c90ec`).

**Unrun:** JDK25 (owner-provided path and archive absent here), complete Gradle/module/NeoForge compile, Minecraft selected binary and outer/nested CRC/class-origin/Windows payload verification, real Vulkan/OpenGL pixels, water transparency, resize/reload/world-switch/save/shutdown, same-scene draw/descriptor/memory and complete CPU/GPU frame p50/p95/p99, actual AMD GPU, RTX NGX DLSS, FSR/DLSS runtime acceptance. This feature records diagnostic observations but has **no measured FPS/latency improvement claim**. The owner performs full builds/GPU acceptance under the October 27 override. **No JAR, GitHub release, Actions or CurseForge upload was attempted.**

## Renderer self-audit: six equally weighted categories

| Category | Prior /10 | Current /10 | Pinned basis and missing acceptance |
|---|---:|---:|---|
| Distance/LOD correctness & usable coverage | 4 | **4** | Existing [camera/LOD selection](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L521-L567) unchanged; native travel/unseen coverage missing |
| Mesh generation/reuse/scheduling | 6 | **6** | [Optional splitter](../20261027-quad-bounded-upload/REVIEW.md) unchanged; no GPU/draw performance proof |
| GPU culling/submission/memory | 4 | **4** | New [real-path page lifecycle hooks](https://github.com/Shnewbs/Deep-Primal/blob/0f1ef9cd0f0e739a6bd757421105df42b3f85d44/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L640-L750); no actual hardware-memory/tail verification |
| Actual FSR/DLSS | 5 | **5** | Prior shipped FSR1 spatial; [native NGX](../../UPSCALING.md) remains unavailable for real DLSS dispatch |
| Visual/backend/settings compatibility | 5 | **5** | Telemetry off by default, no rendering behavior change; named API/resize/water pixels not run |
| Validation & measured performance | 4 | **4** | Java21 36,117 pure-component assertions and rejected mutant plus final Git-source checks; no Java25/native frame/GPU performance |
| **Overall** | **4.67** | **4.67** | **Δ 0.00**, >8.5 goal still open |

Independent whole-engine **5.0/10** is carried from the previous full review, **not regraded**.

## Next renderer-only jam

Use the new part/page and existing frame-window telemetry on the owner's **same seed, camera path, near/vertical/LOD settings, water and native GPU** with identical default/32KiB A/B trials. Record per-stage CPU timing, full CPU/GPU frame p50/p95/p99, actual GPU fences/VRAM, draw and descriptor counts, page admission-to-completion and dropped/stale rates. Keep experimental splitting disabled until verified gains outweigh added draws, then prioritize bounded upload/culling without reducing near-native/LOD and transparent-water coverage. Continue separate temporal depth/motion/jitter prerequisites for DLSS/FSR upgrades only after source and SDK readiness; no NVIDIA/AMD capability claims from these unit tests.


<!-- END PRESERVED SOURCE -->
