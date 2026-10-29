# Expanded replica — docs/art/block-textures-32-batch02/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/README.md) |
| Source identity | git: docs/art/block-textures-32-batch02/README.md |
| Preserved source SHA-256 | `36f605aa940b7cbc8e2c2aa56f6a3beecb52d22cb4c036a6dacc46e2c53030a3` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Batch02 adds 70 opaque grayscale 32-pixel candidates, bringing both batches to 110 exports. Its rock coverage distinguishes 88 catalog rows from 81 unique candidate bases, including 11 reused Batch01 assets; the separate generic-coal comparison reference is not an additional coverage asset. The document records master-bundle identity, isolated export requirements, Box resampling and contact-sheet screening. It preserves provisional argillite, dolostone naming and grouped evaporite decisions. Native game, tint, lighting, near/far, mip and resource-reload checks are still pending despite the substantial source and export documentation.

## Research expansion — gaps and details to deepen

- The coverage counts are easy to misuse unless the manifest represents reused assets, aliases and comparison-only references explicitly. A copied reference texture should retain the same stable source identity rather than appear to be a newly authored candidate. Multiple rock names sharing a base do not imply their gameplay or physical properties are identical.
- The edge-to-interior contrast threshold is a screening heuristic. It cannot decide whether a tile's structure, grain direction or repetition reads naturally in a large wall, or whether distinct coal ranks remain recognizable after tint and lighting. Reduced preview images also do not reproduce native sampler and mip behavior.

## Research expansion — proposed contracts

- Extend assets.json with role, reuse source, comparison-only status, provisional taxonomy reason and review evidence. Generate the coverage table from this crosswalk while preserving authored explanations. Add explicit approval transitions so source verification and coverage mapping never automatically publish resources into the runtime pack.
- Create a native rock-family review suite containing large tiled planes, columns, stair faces, contrasting light directions and neutral versus final tint. Include deliberate comparisons for the coal ranks, mixed ironstone/banded iron and weathered versus fine-grained families rather than inspecting all candidates only at enlarged contact-sheet scale.

## Research expansion — acceptance checks

- Verify the 70 new plus 11 reused bases map to the documented 88 rows, with the generic-coal reference excluded from production coverage. Require all master inputs before export and compare both decoded pixels and candidate file hashes.
- Record actual native captures at selected screen footprints and graphics settings, review repetition and identity under changing light, and check resource reload. Keep a failed visual review as a rejected candidate even if the automated edge metric passes.

## Research expansion — source anchors

- Deep Primal — 32px block textures, batch 02
- Full source bundle
- Catalog coverage

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `assets.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/assets.json) |
| `catalog-rows.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/catalog-rows.json) |
| `contact-1.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-1.png) |
| `contact-2.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-2.png) |
| `contact-3.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-3.png) |
| `contact-4.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-4.png) |
| `contact-5.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-5.png) |
| `contact-6.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-6.png) |
| `contact-7.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/contact-7.png) |
| `manifest.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/manifest.json) |
| `reference-assets.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/reference-assets.json) |
| `repeat-1.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-1.png) |
| `repeat-2.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-2.png) |
| `repeat-3.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-3.png) |
| `repeat-4.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-4.png) |
| `repeat-5.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-5.png) |
| `repeat-6.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-6.png) |
| `repeat-7.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/repeat-7.png) |
| `review.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/review.json) |
| `rock-coverage.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/rock-coverage.json) |
| `scale-study.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/scale-study.png) |
| `source-bundle.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/source-bundle.json) |
| `validation.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/validation.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — 32px block textures, batch 02

Seventy new, individually generated **32×32 opaque grayscale PNG candidates** extend the rock base library. Batch 01 and this batch together contain 110 individual tile exports. No 64×64 exception is used in this batch.

Status: **exported candidates; native game review, palette and model assignments pending**. These files are production art candidates under `docs/art`, not registered runtime resources or accepted complete block families.

## Full source bundle

[Download Deep-Primal-32px-Texture-Batch-02.zip](https://chatgpt.com/library?file_id=libfile_1c2e81cae8608191bbedcd874430254a&account_id=personal) — 86783544 bytes, containing all 70 original masters and individual repeat proofs. Archive SHA-256: `0cb6bd9f8767c5cdcd50e8369c7e7ba1860f106b1047afe3decd55fa542f0a7d`. The [bundle record](source-bundle.json) retains the download identity and validation counts.

## Catalog coverage

The [row-by-row crosswalk](ROCK-COVERAGE.md) maps all 88 retained and expanded rock catalog rows to 81 unique base candidates: 70 new here and 11 reused unchanged from batch 01. The machine-readable assignments, source names and checksums are in [rock-coverage.json](rock-coverage.json). Source rows are retained in [catalog-rows.json](catalog-rows.json), pinned to main snapshot `7f1d6310accf40649f25bf2f6698cec1949940f3`.

- Grouped entries now have separate mudstone/claystone, eclogite/granulite, ironstone/banded-iron and coal-rank candidates. Lignite, subbituminous coal, bituminous coal and anthracite have individual exports.
- Dolomite as a rock label shares the dolostone base. This does not merge the separate mineral catalog subject.
- Argillite (`ROCK-ADD-043`) provisionally shares mudstone art while terminology and classification remain unresolved.
- Evaporite is an umbrella assignment selecting halite or gypsum. Shared names do not create duplicate image files.

Art assignments do not change catalog approval, geology taxonomy, gameplay registration or world generation. Directional end/cross-section faces, states, worked variants and material palettes still require their own review.

## Review material

| Sheet | Material range | Actual 32px exports, enlarged | Repeated 3×3 surfaces |
| --- | --- | --- | --- |
| 1 | Diorite through syenite | [Contact 1](contact-1.png) | [Repeat 1](repeat-1.png) |
| 2 | Chalk through halite | [Contact 2](contact-2.png) | [Repeat 2](repeat-2.png) |
| 3 | Travertine through serpentinite | [Contact 3](contact-3.png) | [Repeat 3](repeat-3.png) |
| 4 | Soapstone through lherzolite | [Contact 4](contact-4.png) | [Repeat 4](repeat-4.png) |
| 5 | Pyroxenite through volcanic agglomerate | [Contact 5](contact-5.png) | [Repeat 5](repeat-5.png) |
| 6 | Arkose through subbituminous coal | [Contact 6](contact-6.png) | [Repeat 6](repeat-6.png) |
| 7 | Bituminous coal through metaconglomerate | [Contact 7](contact-7.png) | [Repeat 7](repeat-7.png) |

The [scale study](scale-study.png) compares eight selected candidates at 32, 16, 8 and 4 pixels using area averaging. It is a mechanical visibility study, not a simulation of the native renderer. [Validation](validation.json) records dimensions, channel equality, alpha, uniqueness, file hashes and wrap statistics. [Review notes](review.json) distinguish these checks from pending acceptance.

## Files and reproducibility

- `tiles/stone/`: 70 exact 32×32 RGB PNGs. Every pixel is opaque and neutral; material tint remains separate.
- [assets.json](assets.json): exact individual prompts, catalog references and target settings.
- [manifest.json](manifest.json): tile and source-master SHA-256 hashes, paths and review state.
- `masters/stone/`: all 70 unmodified original generated images, included in the full source bundle. Larger master dimensions are not runtime size exceptions.
- `proofs/`: individual nearest-neighbor repeat proofs, included in the full source bundle.
- [reference-assets.json](reference-assets.json): twelve unchanged batch-01 references, including eleven reused candidates and generic coal retained only for comparison.

The repository retains the small tiles, combined review sheets, provenance, scripts and coverage records. High-resolution originals and individual repeat proofs are in the separate source bundle. The bundle also includes prior reference tiles so validation and crosswalk links work without downloading batch 01.

Use Node.js, Python with Pillow, and ImageMagick's `convert`/`montage` commands with the DejaVu Sans font. From a folder containing this batch:

```sh
node export.mjs .
python3 inspect.py
node preview.mjs .
node scale-study.mjs .
python3 build_coverage.py
```

`export.mjs` requires every original master before writing any outputs. It converts to grayscale/sRGB, resizes with Box area sampling, emits 8-bit RGB, and strips metadata. There is no procedural substitute art, manual pixel painting or painted seam correction. `inspect.py` can validate the repository tile set without masters; when masters are present it verifies their hashes too. `build_coverage.py` verifies assigned tile hashes and resolves prior assets from the bundle's `reference-tiles/` or the adjacent repository batch.

Wrap statistics flag a candidate when its average edge step exceeds 2.5 times its interior step. Passing this screen is not proof of seamlessness. Read the repeated proofs and native rendering together before accepting a surface.

## Remaining acceptance

Native game lighting, material tint, resource reload, face orientation, near/far appearance and mip behavior are pending. Banded and foliated candidates require consistent model orientation; fine grains and the coal-rank differences need low-light and distance checks. A base assignment does not finish every required block face or state.

The protected ground-v4 faces/masks and final sand art retain their existing bytes. No engine source, runtime resource or release artifact is changed by this batch.


<!-- END PRESERVED SOURCE -->
