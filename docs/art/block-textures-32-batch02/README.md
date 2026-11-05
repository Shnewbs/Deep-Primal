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
