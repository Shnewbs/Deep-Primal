# Expanded replica — docs/renderer-jams/20261029-pose-frustum/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-pose-frustum/README.md) |
| Source identity | git: docs/renderer-jams/20261029-pose-frustum/README.md |
| Preserved source SHA-256 | `67437492f99b2ecad9ae9ef7dbc8a11c162dc52a991e79ab94275e6f0aa0edc5` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Extends default-off adaptive sampling to force a fresh coarse sample after more than four blocks of travel or eight degrees of yaw/pitch change from the last successful sample. Fine culling stays authoritative, and the coarse-off startup override remains dominant.

## Research expansion — gaps and details to deepen

- The thresholds trigger CPU sampling opportunities rather than visual visibility decisions. The expanded guide should identify how failed samples, projection/scene changes, origin rebases, and later extreme-angle fixes affect policy history without implying that bypassed coarse work hides geometry.

## Research expansion — proposed contracts

- Define a policy-history identity for world attachment, successful sample pose, projection/viewport generation, and cancellation. Preserve completion-only pose commit and reset; link finite-overflow and normal-angle successors as numeric refinements rather than new GPU culling capabilities.

## Research expansion — acceptance checks

- Exercise just-below/equal/above travel and angular thresholds, wraparound, failed extraction, and world reset with actual camera getters. Run three-way native camera-turn/travel captures and water images; use measured workload benefit before retuning thresholds or changing default-off adaptation.

## Research expansion — source anchors

- Pose-aware adaptive frustum gate — renderer jam 28

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumPoseChecks.java` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-pose-frustum/tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumPoseChecks.java) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Pose-aware adaptive frustum gate — renderer jam 28

This is a renderer-only, source-tested experiment, not a new development JAR. Exact reviewed source/test SHA: [`5713d702`](https://github.com/Shnewbs/Deep-Primal/commit/5713d70252e658625fd0fff1b6c911efba073359); [read-only self-audit](REVIEW.md).

The optional `-Ddeepprimal.lod.pageFrustumAutoSkip=true` now forces a fresh coarse page test after more than four blocks of camera motion or more than eight degrees of yaw/pitch movement relative to the previous successful sample. The original fine-part fallback remains authoritative and the opt-in remains **disabled by default**. The separate `-Ddeepprimal.lod.pageFrustumBroadphase=false` override wins over adaptation.

Reproduce source-only tests with Java 21 and the two production-excluded test doubles from the previous page-frustum A/B jam. Compile the production gate, production page bounds and committed [pose regression](tests/com/shnewbs/deepprimal/lod/client/LodPageFrustumPoseChecks.java) with `javac --release 21 -Xlint:all -Werror`; run with `java -ea -Xverify:all` in both adaptive modes. The archive in the user's Library includes exact test logs, source identities, mutant rejection and a SHA-256 manifest.

**Owner native gates:** a full Java25 Gradle/NeoForge build plus matched Vulkan/OpenGL fixed/adaptive/off camera-turn and all-visible trials, frame p50/p95/p99, VRAM, water pixels, reload/reopen/shutdown and valid DLSS/temporal FSR status. No FPS or active DLSS claims here.


<!-- END PRESERVED SOURCE -->
