# Expanded replica — docs/art/ground-textures-v4/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/ground-textures-v4/README.md) |
| Source identity | git: docs/art/ground-textures-v4/README.md |
| Preserved source SHA-256 | `ccbfa6ad10f200b02c63319b96bfd9a8e3b7a9727a4db19b0befe85cf4955e0a` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Ground-v4 restores the approved source after the rejected v3 appearance. It uses defined source crops for tops and sides, preserves characteristic grain and roots, joins to approved soil, removes the v3 blur/recenter/regenerated-source treatment and retains grayscale RGB8 plus complementary mask data. The manifest selects v4 with unchanged final sand. Recorded interior-pixel, hash and Java ImageIO checks support source/export identity; the document expressly does not claim a fresh complete build, native visual review, performance result or newly qualified installer. These 64-pixel assets are protected exceptions to the newer 32-pixel default.

## Research expansion — gaps and details to deepen

- The document clearly explains the restoration, but the current asset registry should capture approval as immutable versioned provenance rather than a directory name. Any proposed 32-pixel counterpart needs its own comparison and owner decision; it cannot replace approved 64-pixel bytes through a generalized resampling rule.
- Matching interior crops and valid RGB loading do not establish all side joins under actual tint, mips and lighting. The unchanged dual-tint shader contract needs a direct reference so source masks, generated packed channels and runtime shader behavior cannot evolve independently while each local check still passes.

## Research expansion — proposed contracts

- Make v4 the explicit approved-source entry in the asset ledger, with hashes for every face and mask, retained source crop coordinates, export dimensions and a reason for the 64-pixel exception. Record v3 as rejected and v2 as historical integration evidence rather than deleting their useful provenance.
- Add a promotion receipt that separately names source approval, decoded-pixel verification, generated-resource identity and native scene acceptance. Link soil/cover masks to the single dual-tint interface and state the intended side orientation, below-ground mask behavior and unchanged sand dependency.

## Research expansion — acceptance checks

- Re-run pixel and complementary-mask controls using the exact approved crop inputs, compare delivered PNG and decoded-pixel hashes and detect any unintended inclusion of v3 overlays. Preserve current bytes while experimenting with alternate resolutions in a separate candidate path.
- Place repeated ground blocks in native scenes with different tints, exposed sides, seams, mips and both graphics APIs. Verify reload and generated-atlas identity against the same manifest. Treat visually approved source as necessary but separate from native shader and package acceptance.

## Research expansion — source anchors

- Restore approved ground art at the intended feature scale
- Changes
- Validation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Restore approved ground art at the intended feature scale

This supersedes the v3 side-art correction, which the owner rejected as pale sandy noise. The selected art direction remains the original grass/dirt/podzol board, retained in docs/art/ground-textures-v2/approved-sheet.png and its unmodified source-crops. No new image generation or replacement artwork is used in this revision.

## Changes

Each top uses a selected 128x128 region from the approved crop, reduced to 64x64 instead of shrinking an entire roughly 460x400 panel. This retains larger identifiable leaves, soil clumps and litter within the 64-pixel limit. Crop coordinates and source hashes are in validation.json. Sides use 128x103 regions resized to 64x52, preserving the original root/litter scale, with a gradual join into the same approved dirt substrate. Dirt top and side are byte-identical; grass/podzol bottom rows match that dirt exactly, including their edge against a dirt block below. Sand is unchanged.

The export performs only crop, grayscale conversion, resampling, narrow wrap-edge correction, soil joining and complementary mask authoring. It removes the v3 blur subtraction, luminance recentering and regenerated gravel source. It preserves the reference's natural tonal structure. Runtime PNGs use neutral RGB8 (R=G=B), avoiding grayscale PNG color-space interpretation differences between ImageIO and the native loader. Masks use the same byte format and retain weights summing to 255.

The current main snapshot already implements GroundSideSpriteSource and the two-color shader path. That code, material registrations, lighting mixins and model references are preserved. These new masks feed the existing path. There is no new renderer or lighting change. The screenshot's full upper/lower brightness difference is not conclusively attributed to art alone; this change removes the known asset mismatch and needs a matching native scene to assess the remaining lighting.

## Validation

- Ten 64x64 neutral RGB PNGs decode and reproduce byte-for-byte.
- Intended horizontal wrap edges match; tops and dirt also match vertically. Organic sides deliberately do not repeat vertically.
- All 8,192 mask pairs sum to 255. Soil is fully selected below row 31.
- 14,268 unmodified interior pixels match the independently recropped approved artwork exactly. Five source crop hashes match the retained originals.
- Dirt top/side bytes match, and the bottom edge of grass/podzol matches the top edge of dirt.
- Independent JDK25 ImageIO checks validate 40,960 neutral RGB pixels and 40,960 pack/compose cases using the actual methods extracted from GroundSideSpriteSource at 7535060000b19d4def0295ba83252a81c3b8e77f. Arithmetic agrees with a direct weighted-color oracle within rounding tolerance, and mask weights are not gamma-remapped.
- build.gradle now points groundMaterialResources at this revision's manifest. The prior task still pointed at the v2 manifest despite later replacements; historical manifests are retained. GroundResourceChecks now reports the actual manifest image count instead of a hard-coded unchanged-art claim.
- Main integration (2026-10-25): resource validation uses this complete manifest directly, plus the unchanged final-sand manifest. The superseded v3 side-hash overlay is no longer applied over v4. Soil family resource checks remain enabled; no runtime artwork is changed by this integration fix.

actual-texture-proof.png shows the real 64x64 exports enlarged with nearest sampling: grass/dirt/podzol columns; grayscale top, grayscale side, illustrative tinted top, illustrative tinted side rows. Tint swatches are illustrative CPU colors, not screenshots of a game, and are not a new colormap. Repeat proofs expose the actual single-tile repetition; matching borders does not make a repeating 64x64 pattern invisible. This is a closer export of the selected art, not full-resolution preservation of the board. No full Gradle build, native resource reload, Vulkan/OpenGL visual test, frame-performance test, installer or new engine grade is claimed.

## Reproduce

Requires Node.js, ImageMagick, Python/Pillow and JDK25 for the Java image/material checks.

    node tools/ground-textures/export-approved-v4.mjs docs/art/ground-textures-v2/source-crops build/ground-v4
    node tools/ground-textures/export-approved-v4.mjs docs/art/ground-textures-v2/source-crops build/ground-v4-repeat
    python3 tools/ground-textures/check-approved-v4.py build/ground-v4 docs/art/ground-textures-v2/source-crops build/ground-v4-repeat
    node tools/ground-textures/check-approved-v4-java.mjs . PATH_TO_JDK25

The standalone exporter writes only to the specified output directory. Copy reviewed runtime files from that directory when promoting a future revision. Use the repository's groundMaterialResources/full build/native client checks for integration validation; the lightweight checks above are scoped asset evidence.


<!-- END PRESERVED SOURCE -->
