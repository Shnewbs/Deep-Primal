# Expanded replica — docs/validation/engine-continuation/water-shared-corners-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-shared-corners-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-shared-corners-2026-10-23/README.md |
| Preserved source SHA-256 | `077c54e933aa6b2981ca0dae70d8c486e930aea3ab0d36a47c72b48f0e201c8c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Canonical global water-corner directions, bounded shared transition histories and sampled-color blending that preserves uniform endpoint behavior. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that mixed transitions can evaluate eight endpoints and add history/varying storage, so the seam correction is not automatically a performance improvement. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a shared-corner identity and shader ABI contract with explicit uniform fast paths, history bounds and render-work accounting. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare different-direction seams and uniform controls through actual uploads, verify low light and opacity bytes remain intact and profile mixed versus uniform scenes with matched geometry. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Shared-corner water rendering — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Shared-corner water rendering — 2026-10-23

Parent source `71383974f8c52856f3c2fba8d5d95bff06354b89`; guarded lease parent `adb740db532a7c76412514239cd21dd3a65331ef`.

## Shared-corner water presentation — 2026-10-23

Implemented the previously reproduced different-direction top-surface seam in actual production uploads. Five production files add a canonical global-corner direction from wet-neighbor FLOW vectors, bounded shared-corner transition histories, and bilinear blending of sampled colors. Uniform source/target UVs retain the old interpolated endpoint coordinates; a shared Q25 coefficient include prevents shader drift. The 36-byte GPU / 14-float CPU vertex strides, low light bytes, opacity clocks, geometry and water quantities remain unchanged. A separate 32,768-entry corner history and 24 additional varying bytes are added; mixed transitions can evaluate eight endpoints, static uniform water one and uniform transitions two. This cost requires broader profiling.

The exact unchanged parent baseline verifies 411 production blobs and loaded class/shader identities: 2,204/2,332 shared-edge routes fail, while all 136 uniform controls are byte-exact. A mixed-source baseline attempt was excluded. Self-audit rejected 24 nearest-texel fidelity differences (maximum 5/255), then 25 source-endpoint fade differences; both endpoints now interpolate consistently without changing tolerance. An independent raw-byte audit also found an expired height-clock signed-byte wrap; clamping completed offsets to the 100 ms window fixes it, covered by 98,307 long-idle cases. Earlier passing variants are excluded from final totals and preserved with failures.

Fresh serial full build passes all 25 tasks; core checks include 8,435,621 codec cases, 98,307 height-clock cases and 34,000 shared-halo comparisons. All six archive CRCs, 410 compiled-class identities, shader source identities and five nested module identities pass. Two final software Vulkan sessions (development and packaged class origins), followed by OpenGL fallback, pass 29,616 actual-upload diagnostic draws / 14,808 pixel comparisons. All final top-field comparisons have zero channels beyond 1/255 and zero uncovered pixels; retained direction/material/side/height/fade/reuse checks pass. Explicit records, sealed console hashes, source/runtime identities, saves, crash deltas, Vulkan error/resource cleanup and screenshots are audited independently. These are controlled edge reprojections of real uploaded payloads, not full live-stream acceptance or owner GPU/FPS evidence.

Next: reproduce a new corner request arriving on only one page while its neighbor remains resident, then implement bounded neighbor-consistent adoption without losing accepted endpoints. Matched delayed repacks here are checked only after both pages have adopted the request. Active cap churn, real long streams/ocean, spatial side seams, tint/chemistry/other liquids, arbitrary alpha/linear packs, broader height/opacity/edit combinations and matched frame-time/resident-memory acceptance remain open. Lake screenshots still show distant rectangular LOD/horizon boundaries. Water budgets, pressure continuation, geology, enabled-view/LOD acceptance and other requirement-register work remain unfinished. Solver/runtime, player edits, backend/LOD source and frozen V1/V2/V3 geography are unchanged. No installer release or main merge; this is an unreleased continuation checkpoint.

Evidence: `docs/validation/engine-continuation/water-shared-corners-2026-10-23/README.md`.

## Evidence replay

Run `python scripts/verify_archived_evidence.py .` in this directory. It validates every compressed raw length/hash, ZIP/PNG identity, and independently replays the unchanged baseline plus all three final native suites. Large raw data are gzip-compressed with explicit manifests; repeated PNGs are content-deduplicated in per-session ZIPs. No installer binary is published here.

The tiny20/40vertex upload CPU observations are retained per session in `strict-corner-native-audit.json`; these include allocation/staging and are noisy, not a matched full-scene performance benchmark. The baseline and candidate differ in render work; no speedup or ownerGPU guarantee follows.


<!-- END PRESERVED SOURCE -->
