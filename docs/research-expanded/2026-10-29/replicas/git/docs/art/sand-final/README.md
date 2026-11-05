# Expanded replica — docs/art/sand-final/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/sand-final/README.md) |
| Source identity | git: docs/art/sand-final/README.md |
| Preserved source SHA-256 | `772c0b84f394cc3f6f0f7f36d3d3e6d6308ede17b7b565fc39258a23042a9593` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The final-sand README defines a protected 64-pixel grayscale, low-contrast, nondirectional fine-grain source intended for all faces with tint and no mask. It records its generated master, wrapped resampling and repeated-image proof. It does not claim native rendering, stochastic variants or a cross-blend implementation. Although short, this document establishes a concrete approved material direction that the broader texture pipeline must preserve. The 32-pixel default in the later texture plan calls for comparisons where useful, not automatic replacement or downsampling of this approved sand.

## Research expansion — gaps and details to deepen

- The one-tile, all-face choice needs a stable material and source identity in the shared asset ledger so later family-generation tools do not replace it with directional noise or a procedural default. Resolution exception, approval status, tint role and absence of coverage masks should be explicit fields rather than inferred from prose.
- Wrapped resampling and repeat-proof bytes establish a particular export process, but they do not establish visual isotropy under lighting, mip stability, native color handling or the appropriate contrast over a broad landscape. Those acceptance categories should remain distinct from the recorded source/export checks.

## Research expansion — proposed contracts

- Add a sand role card with canonical source master, crop/resampling recipe, exact PNG and decoded-pixel hashes, 64-pixel exception rationale, neutral luminance expectations and all-face binding. Keep any 32-pixel study as a new unapproved candidate linked to this source, with explicit promotion criteria.
- Define how the runtime applies sand tint in linear shading and which material parameters belong to the shader rather than being painted into albedo. Preserve the intended low-contrast fine grain; additional wetness, beach variation or stochastic sampling should be separate proposed features with their own saved/material compatibility and visual review.

## Research expansion — acceptance checks

- Verify the same approved tile is bound to every intended sand face and no alpha-mask or organic-ground blend rule is accidentally applied. Re-export in isolation and compare the recorded repeat and pixel identities without modifying the approved runtime resource.
- Review large native sand planes and slopes at near and distant footprints, with multiple sun angles, tints and mip levels on GL and Vulkan. Record whether grain becomes directional or noisy. Require owner approval before replacing the source, resolution or broad material character.

## Research expansion — source anchors

- Deep Primal final sand design

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal final sand design

Final art revision of the initial sand tile: isotropic fine mineral grains, subdued contrast, no deliberate ripples or directional bands. The opaque 64x64 grayscale sand.png is shared by top, sides and bottom; apply the chosen sand/mineral color through the material tint provider. No separate masks are necessary for this single-material surface.

Generated with the built-in image-generation tool; the exact prompt is in prompt.txt. The high-resolution master is an authoring source, not the runtime texture. The exported image is grayscale, Lanczos downsampled, and corrected through four-texel bands to match opposing boundaries. The 64x64 export, 3x3 enlarged repeat and 8x8 native-resolution repeat were inspected. PNG round-trip and repeat-border equality pass; a second export reproduces sand.png byte-for-byte.

A single tile still repeats every 64 texels. This design reduces recognizable directional motifs; it does not implement stochastic material variants or cross-material blending. Actual game lighting, mipmap behavior and colormap application are not validated. This asset commit includes the final sand artwork; model/color-provider integration remains separate.

Reproduce with Node.js and ImageMagick from the repository root:

    node tools/ground-textures/export-sand.mjs docs/art/sand-final/master.png build/sand-final

The output directory contains sand.png, previews and validation.json. Review before copying sand.png into runtime resources. Existing ground assets are not overwritten by this exporter.


<!-- END PRESERVED SOURCE -->
