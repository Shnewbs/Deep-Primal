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

## Tint contract

All six images store grayscale luminance only. Tint grass tops with the grass color, dirt with the soil color, and podzol tops with a litter color. For sides, the surface mask is white at the organic surface and fades to black in the soil; the soil mask is its exact complement. These are authored region weights, not automatic semantic segmentation of every root/leaf. Both masks sum to 255 per texel, and horizontal wrap edges match.

For a shader/model path that supports two material colors, use the texture luminance with the weighted color `surfaceMask * surfaceTint + soilMask * soilTint`, using normalized weights and the renderer's chosen color-space convention. A plain Minecraft grayscale PNG does not itself create that behavior. These masks are opaque data textures, not ready-to-render transparent overlays. They require a material sampler or conversion to appropriate model layers/tint indices. The original asset export included no models, color providers or block registrations.

Existing colormaps remain untouched. The exact illustrated colored strip is a concept; runtime colors depend on the selected tints and lighting. Multi-variant blending and cross-material transition masks remain separate work.

## Material sample integration

Three opt-in building blocks now use the six approved textures: `deepprimal:ground_grass`, `deepprimal:ground_dirt` and `deepprimal:ground_podzol`. They have matching items in the Natural Blocks creative tab, self-drop loot tables, shovel mining and dirt substrate tags. They are stable material samples, not spreading grass, farmland or a completed soil simulation. They do not replace vanilla blocks or appear in any frozen V1-V7 terrain generator.

The shared solid cube model assigns index 0 to the top and index 1 to the bottom/sides. Grass tops read the active grass colormap with normal biome blending; podzol tops read the dry-foliage colormap. Dirt tops, bottoms and all sides currently use provisional soil tint `#b6a080`. Inventory tints match the block providers' default colors; particle tint uses soil because the particle texture is a side. Resource packs can override the models, textures and inventory tint values.

**Side-surface dual tint remains open.** Both grass and podzol sides currently receive only the soil tint, including the organic strip. The authored masks remain byte-for-byte intact but are not sampled by this initial solid model. A coplanar translucent overlay, thresholded mask or green whole-side tint would not reproduce the specified blend reliably, so none is used. This is a deliberately limited material preview, not final visual acceptance. The [isolated surface preview](../../engine-stage2-2026-10-24/README.md#ground-surface-preview) uses the ordered gradient to select these blocks, but gameplay placement still awaits a registered, saved worldgen revision. A tested two-color material path, final material variants and native/Vulkan/OpenGL visual coverage are still required.

## Validation

`validation.json` records dimensions, source hash, output hashes and repeat axes. All ten PNGs decode at 64×64, are neutral grayscale and round-trip exactly. Opposite border pixels match on every intended repeat axis. All 8,192 side-mask pixel pairs sum to 255. `preview.png` and `repeat-proof.png` show the actual exported tiles and were visually checked. No in-game resource reload, mipmapping, renderer visual test or full mod build is claimed.

### Integration validation, alpha.29-dev.3

The preceding export-only limits remain historical. The material integration now has the following separately executed local evidence:

- Full Windows Vulkan `build` passes, including core checks, cache checks and the new headless material/resource task (33 tasks). Ground resources report 50,140 checks; surface profile checks report 65 cases, including 54 invalid inputs.
- Selected-JAR native placement/reopen verifies all three actual block/item registrations, self-drop loot, shovel/substrate tags and plant support. The isolated V8 surface preview compares 1,572,864 blocks per run across two seeds, two adjacent signed-coordinate chunks and reversed generation order. Saved graph/surface snapshots reopen with identical fingerprints. Thin roof/cave-gap, empty/full, bedrock and inclusive-ceiling controls pass.
- Actual Windows OpenGL and Vulkan clients bake all 18 cube quads, validate sprite links/tint indices, render inventory items and reload resources. The packaged captures are pixel-identical before/after reload and across APIs (1200x800, zero differing pixels), and match development captures. Vulkan shutdown reports zero validation errors, buffers and images. This verifies this small inventory scene only, not world terrain, biome transitions, FPS or mask blending.
- Archive checks match all 132 root production classes, all 184 root runtime resources and the compiled classes in all six nested module JARs; all archive CRCs pass. Validation classes are excluded from the distributed archive.

Reproduction uses `.\gradlew.bat build -PvulkanPlatform=windows`, `.\gradlew.bat groundMaterialResources`, and **root-only** `:runServer` / `:runClient` with `-PvisualValidation`. Native mode uses `DEEPPRIMAL_GROUND_MATERIAL_CHECK=fresh` then `reopen` in an isolated run directory. Client mode uses `-PgroundMaterialClient=OpenGL` or `Vulkan`, `-PclientRunDir=...` and `-PcaptureDir=...`. Selected-JAR runs additionally use `-PpackagedValidation`, `-PvalidationJar=...` and `DEEPPRIMAL_GROUND_VALIDATION_JAR` pointing to that same archive; the fixture rejects class-origin/byte mismatches.

Local ignored evidence is in `build/ground-material-windows-build.log`, `build/ground-material-packaged-{fresh,reopen}.log`, `build/ground-material-packaged-{opengl,vulkan}.log` and their capture directories. The JAR is `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-vulkan-windows.jar`, 125,093,683 bytes, SHA-256 `7f508ce99a0c8bc495ed74900e6c35743e5fd118e198e4fae2ffc0791d2153e8`.

Rejected validation attempts exposed an ImageIO color-managed mask-read mismatch, a non-repeatable fixture placement assertion, an inclusive world-ceiling error in the new coating overload (fixed), and an inventory check before the platform bound item components (fixture now loads the native world-creation data context first). An unqualified Gradle client selector also started the Themes client; accepted runs use `:runClient`. These failures are not counted as passes. No soil, water or V8 gameplay completion or new engine grade is claimed.

## Reproduce

Requires Node.js, ImageMagick commands `convert`/`montage`, and DejaVu Sans for proof labels. From the repository root:

```sh
node tools/ground-textures/export-approved-v2.mjs docs/art/ground-textures-v2/approved-sheet.png build/ground-textures-v2
```

Review the generated `src/` tree before updating runtime assets. The script does not write directly into the existing repository resource directory. The approved sheet was generated for Deep Primal and selected by the owner; external reference images are not redistributed in this package. The built-in image tool produced the earlier concept sheet; this export uses direct extraction only. No new generative prompt/redraw is used for these final tiles.
