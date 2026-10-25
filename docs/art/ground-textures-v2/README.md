# Approved ground textures v2

These files are extracted from the owner's selected sheet, `approved-sheet.png`. They do not use the subsequently generated standalone textures. Original crop pixels are retained in `source-crops/`; the ten runtime images are 64×64 neutral grayscale PNGs. These supersede the first grayscale-ground export as the selected art direction.

## Repository layout

The ZIP is rooted at the repository: copy its `src/`, `docs/`, and `tools/` directories into Deep-Primal. It contains no existing Java, model or world-generation files to overwrite. Runtime files are in `src/main/resources/assets/deepprimal/textures/block/ground/`:

| Material | Top | Side | Side surface / soil masks |
| --- | --- | --- | --- |
| Grass | `grass_top.png` | `grass_side.png` | `masks/grass_side_surface.png`, `masks/grass_side_soil.png` |
| Dirt | `dirt_top.png` | `dirt_side.png` | Not needed: one soil tint |
| Podzol | `podzol_top.png` | `podzol_side.png` | `masks/podzol_side_surface.png`, `masks/podzol_side_soil.png` |

## Extraction and repeat behavior

The original top panels were resampled to 64×64 with no redraw. Their dimensions in the supplied sheet are slightly rectangular, so the export maps the full panel to a square. The narrow side strips retain their proportions in the upper 14 rows. The remainder uses soil from the approved dirt panel; a three-row junction is blended. RGB hues are removed without imposing a new contrast curve. A four-texel edge band matches opposite repeat boundaries.

Grass, dirt and podzol tops repeat on both axes. Plain dirt sides also repeat both ways. Grass/podzol sides repeat horizontally only; the organic surface belongs at the top. Use plain dirt below the surface. Self-tiling does not provide cross-material connected transitions. The export deliberately keeps original grain and contrast; reducing the sheet to 64×64 loses fine leaf and needle detail. Full-resolution source crops are preserved for future authoring, outside runtime resources.

## Tint contract and remaining integration

All six images store grayscale luminance only. Tint grass tops with the grass color, dirt with the soil color, and podzol tops with a litter color. For sides, the surface mask is white at the organic surface and fades to black in the soil; the soil mask is its exact complement. These are authored region weights, not automatic semantic segmentation of every root/leaf. Both masks sum to 255 per texel, and horizontal wrap edges match.

For a shader/model path that supports two material colors, use the texture luminance with the weighted color `surfaceMask * surfaceTint + soilMask * soilTint`, using normalized weights and the renderer's chosen color-space convention. A plain Minecraft grayscale PNG does not itself create that behavior. These masks are opaque data textures, not ready-to-render transparent overlays. They require a material sampler or conversion to appropriate model layers/tint indices. No model JSON, color provider, renderer patch or block registration is included. Merely copying these assets into the repo does not activate them in gameplay or override vanilla textures.

Existing colormaps remain untouched. The exact illustrated colored strip is a concept; runtime colors depend on the selected tints and lighting. Multi-variant blending and cross-material transition masks remain separate work.

## Validation

`validation.json` records dimensions, source hash, output hashes and repeat axes. All ten PNGs decode at 64×64, are neutral grayscale and round-trip exactly. Opposite border pixels match on every intended repeat axis. All 8,192 side-mask pixel pairs sum to 255. `preview.png` and `repeat-proof.png` show the actual exported tiles and were visually checked. No in-game resource reload, mipmapping, renderer visual test or full mod build is claimed.

## Reproduce

Requires Node.js, ImageMagick commands `convert`/`montage`, and DejaVu Sans for proof labels. From the repository root:

```sh
node tools/ground-textures/export-approved-v2.mjs docs/art/ground-textures-v2/approved-sheet.png build/ground-textures-v2
```

Review the generated `src/` tree before updating runtime assets. The script does not write directly into the existing repository resource directory. The approved sheet was generated for Deep Primal and selected by the owner; external reference images are not redistributed in this package. The built-in image tool produced the earlier concept sheet; this export uses direct extraction only. No new generative prompt/redraw is used for these final tiles.
