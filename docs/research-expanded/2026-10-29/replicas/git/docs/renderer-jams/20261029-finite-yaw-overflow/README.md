# Expanded replica — docs/renderer-jams/20261029-finite-yaw-overflow/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261029-finite-yaw-overflow/README.md) |
| Source identity | git: docs/renderer-jams/20261029-finite-yaw-overflow/README.md |
| Preserved source SHA-256 | `bae6156d650c4695a7604b914bf1c9b00cb65ef11321e5698adafd84eebf6fbc` |
| Review method | full-text |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs finite float yaw subtraction overflow in the optional adaptive sampling gate by reducing large headings individually in double precision before circular comparison. Ordinary bounded angles retain a fast path and nonfinite inputs force sampling. Fine-part visibility and default-off adaptation are unchanged.

## Research expansion — gaps and details to deepen

- The defect missed a coarse CPU sampling opportunity; it was not demonstrated missing terrain or GPU culling corruption. The document should preserve that severity and identify near-threshold numeric semantics rather than presenting all finite-angle coverage as one undifferentiated test result.

## Research expansion — proposed contracts

- Define circular comparison units, finite-input normalization, exact threshold inclusivity, invalid-pose behavior, and last-successful-sample ownership. Link the later normal-angle successor and keep numeric correctness separate from the performance decision to enable adaptation.

## Research expansion — acceptance checks

- Retain the original-source failure witness, signed extreme headings, wraparound, and invalid inputs. Exercise adjacent representable values around the eight-degree decision and actual camera getters in the native artifact; require fixed/adaptive/off images and frame evidence before claiming player-visible improvement.

## Research expansion — source anchors

- Renderer jam 29 — finite camera-angle overflow guard

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 29 — finite camera-angle overflow guard

**Source and test checkpoint:** [ce3ff1d8](https://github.com/Shnewbs/Deep-Primal/commit/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad). This scoped renderer-only repair consumes the [independent Dev74 audit](../../engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md).

In the optional adaptive page-frustum admission gate (`-Ddeepprimal.lod.pageFrustumAutoSkip=true`), subtracting extreme *finite* float yaw values could produce infinity before modulo, incorrectly postponing a coarse page sample. The correction normalizes large headings individually as doubles before comparing their shortest circular difference, while preserving the original float fast path for ordinary camera angles within ±360°. Invalid inputs still force conservative sampling.

**Intentionally unchanged:** fine-part visibility and order, page-bound AABBs, water transparency, terrain/water simulation, mesh geometry, Vulkan/GL draw paths, GPU buffers, game settings, FSR1 and DLSS provider behavior. The optional adaptive gate remains disabled by default. The original source fails the dedicated overflow regression; the new source passes. This is not a demonstrated Minecraft FPS or native GPU improvement.

Read the [six-category renderer self-audit](REVIEW.md) and exact [source/test commit](https://github.com/Shnewbs/Deep-Primal/commit/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad). Source-only evidence ZIP: `DeepPrimal_Renderer_FiniteYaw_20261029_ce3ff1d_SourceEvidence.zip`, SHA256 `a75703c45913108f35f936b7db9fc6167f0abd81b61762e2580f837637e0c3c9`, 25,098 bytes; Library `/Deep Primal/Renderer Jams/`. **No new development JAR.** Owner still performs Java25 full build, named-hardware Vulkan/OpenGL and packaged acceptance.


<!-- END PRESERVED SOURCE -->
