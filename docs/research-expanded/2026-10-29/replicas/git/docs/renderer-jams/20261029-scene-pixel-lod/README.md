# Expanded replica — docs/renderer-jams/20261029-scene-pixel-lod/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-scene-pixel-lod/README.md) |
| Source identity | git: docs/renderer-jams/20261029-scene-pixel-lod/README.md |
| Preserved source SHA-256 | `3e4fc4b5a1bd21053872d4a62e0f4f52b3150c93a71da2ee2bf9178ed2cf3ffc` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Changes projected LOD error to use the valid active world-target height during extraction, retaining bit-exact native focal arithmetic otherwise. Selector/hysteresis and rendering order remain unchanged, while opt-in diagnostics expose effective height and target presence. Scaled synthetic cases choose coarser geometry without finer selections.

## Research expansion — gaps and details to deepen

- Scene-pixel error is an explicit quality policy, not a universal perceptual guarantee at reconstructed output resolution. The target’s lifetime and frame identity must also match extraction across resize, failure fallback, and restoration; source checks alone do not establish this native ordering.

## Research expansion — proposed contracts

- Declare scene versus output pixel units in the LOD contract and capture the effective render extent with the extraction snapshot. Evaluate reconstructed silhouette budgets separately, and require later temporal providers to select a qualified LOD/error policy instead of inheriting spatial assumptions silently.

## Research expansion — acceptance checks

- Retain native bit parity and scaled monotonicity, then test native, FSR 1, spatial, invalid-target, resize, and fallback paths on the exact client. Verify current-frame height telemetry, no stale target use, stable transitions, and post-upscale water/silhouette quality before interpreting coarser selection as a benefit.

## Research expansion — source anchors

- Renderer jam 31 — LOD error in actual scene pixels

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `RECEIPT.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-scene-pixel-lod/RECEIPT.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 31 — LOD error in actual scene pixels

**ID:** renderer-20261029T1100-scene-pixel-lod. **Exact source/tests:** [cf578b2e36fcdbde04320cc2564c5b1cd89004d6](https://github.com/Shnewbs/Deep-Primal/commit/cf578b2e36fcdbde04320cc2564c5b1cd89004d6). **Mod version in unchanged gradle.properties:** 0.0.1-alpha.29-dev.76-native-outlet-ports.1.

Previously LOD mesh selection always derived focal pixels from the native window height. When spatial or actual AMD FSR1 upscaling redirects the world into a smaller scene target, that overestimates screen error and can pick unnecessary fine geometry. The renderer now reads the current valid UpscalingRuntime.frameTarget().height during world extraction, otherwise uses native height with **bit-exact legacy focal arithmetic**. Per-page selection, hysteresis, fine visibility, GPU draw order and transparent-water shader/masks remain untouched. Opt-in LOD telemetry now reports effective pixel height and whether a world target was active, reset with the world.

Source-only strict Java21 test: 2 × **501,280 PASS** (1,002,560 total) checks of native bit parity, scaled selection monotonicity and 200,000 fixed-seed selector cases; scaled cases included 28,529 coarser selections. Negative old/native-only implementation rejected at assertion 3. **19/19** Git-pinned static/source integration checks passed. These are behavior checks, not GPU FPS, pixel or frame-time measurements.

The October 27 owner override reserves full Java25/Gradle/Minecraft/GPU/JAR builds to the owner. No JDK25 archive or install was available in this run; no such validation, qualified JAR or release is claimed. No other scheduler or module touched.

[Full read-only renderer self-audit](REVIEW.md) · [Next renderer work](NEXT.md) · [Source/test receipt](RECEIPT.json). The 12,880-byte CRC/SHA256-verified source evidence archive was saved to Library /Deep Primal/Renderer Jams/ (SHA256 9578e6c648b4f84611761c3fa27852a6a953c4138ccd421d94cd10df9af02258).


<!-- END PRESERVED SOURCE -->
