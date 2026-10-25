# Deep Primal — 32px texture batch 01

This batch contains **40 individual 32×32 PNG candidates**, including 34 opaque surfaces and 6 binary-alpha cutouts. It advances the block-texture production plan with actual game-size exports. It does not complete the 1,013-entry source catalog or choose the final gameplay roster.

**Status: exported and inspected; native game review pending.** No new block registration, resource-pack override, runtime texture replacement or release is included. Existing approved grass, dirt, podzol, masks and sand are preserved. No 64×64 exception was needed for this batch.

## Contents

| Family | Individual exports |
| --- | --- |
| Stone | Granite, basalt, gabbro, limestone, sandstone, shale, slate, marble, quartzite, chert |
| Sediment and soil | Rounded gravel, angular gravel, silt, compact clay, crumbly loam, fibrous peat |
| Wood | Oak bark/end grain/planks, pine bark/end grain/planks, birch bark, stripped grain |
| Building materials | Dressed stone masonry, thatch, fired clay, hammered metal |
| Mineral, growth and frozen | Coal, mineral vein overlay, lichen, moss, glacier ice |
| Foliage, plants and aquatic | Broadleaf canopy, conifer needles, grass tuft, kelp, fern |
| Fungi and volcanic | Mushroom cap surface, scoria |

The mineral vein, broadleaf, needles, grass, kelp and fern exports use binary transparency. Glacier ice is an **opaque material-base candidate**, not a completed translucent ice shader. Lichen and moss are opaque base surfaces, not masks. Oak/pine end grain and the three plant silhouettes are single-face assets; the remaining surfaces have repeat studies.

## Files and provenance

- `tiles/`: exact RGB8/RGBA8 PNGs, 32×32.
- `assets.json`: source catalog references, individual generation prompts and the pine-plank revision.
- `manifest.json`: exported tile paths, original-master and tile SHA-256 hashes, alpha and repeat intent.
- `validation.json` and `review.json`: mechanical results and visual review limits.
- `contact-1.png` through `contact-4.png`, plus `contact-all.png`: actual exports enlarged with nearest-neighbor sampling.
- `repeat-1.png` through `repeat-4.png`: repeated-surface proofs or single-face views.
- `proofs/` in the downloadable bundle: individual repeats and mechanical 32/16/8/4px cutout scale studies. The combined sheets are also committed to Git.
- `masters/` and `revisions/` in the downloadable source bundle: original image-generated masters and superseded pine-plank v1. These larger files stay outside the Git repository.

Every material was generated individually with the built-in image-generation tool. The pine-plank seam revision used its prior master as an editing reference. The exporter performs only mechanical size, neutral-channel and alpha normalization; it does not synthesize replacement art or paint seams. Source snapshot: `19491a10187e679eebf5731aa6fc3bd8079ce786`.

The owner's [complete source bundle](https://chatgpt.com/library?file_id=libfile_b54eedcff08c8191bc1d682908349303&account_id=personal) is `Deep-Primal-32px-Texture-Batch-01.zip` (57,334,887 bytes). Archive SHA-256: `652eb56453809069cb52d3dd71154c3301428417c4d92c106dca1a4f09cdbd4f`. Its inventory and persistent reference are recorded in `source-bundle.json`. The source bundle contains its original README; this Git copy adds the bundle reference after saving.

## Reproduce and validate

Use the source bundle to restore `masters/` alongside these scripts. Node.js, ImageMagick's `convert`/`montage`, and Python 3 with Pillow are required. This export used ImageMagick 6.9.12-98 and Pillow 12.3.0. Font: DejaVu Sans for preview labels.

```bash
node export.mjs .
python3 inspect.py
node preview.mjs .
```

The recipe reduces the full master with Box area sampling, normalizes neutral RGB8/RGBA8, and thresholds cutout alpha at 50%. The verifier checks the complete expected asset set, dimensions, neutral channels, binary alpha/opaque coverage, duplicate bytes and SHA-256. It checks master hashes when masters are present. A missing source makes export fail before writing a partial manifest.

The tile-byte hashes are the authoritative delivered outputs. Different ImageMagick builds may encode PNGs differently; compare decoded pixels when reproducing across versions.

## Review and remaining work

All 40 exports passed mechanical validation. Four contact sheets and four repeat sheets were visually inspected. Pine planks were revised after their top/bottom boundary was flagged; the unchanged screening threshold now reports zero flags. Boundary statistics do not prove seamlessness.

The 32px grass, kelp and fern silhouettes remain readable. Their thin details and the ore veins lose coverage in simple 16/8/4px scale studies. These previews are **not Minecraft mipmaps**. Check actual cutout rendering, near/far appearance, lighting, block orientation, host-rock compositing, tint palettes and resource reload before runtime promotion. Small recurring motifs in organic and ore patterns also need native context.

The next production work remains in the parent family map: canonical rock expansion and directional faces; complete wood face sets; soil boundaries and wet/dry states; snow/firn/ice and volcanic variants; crop growth, aquatic and fungal sets; then model-specific architecture, workstations and machinery. Generic families are samples, not species-complete coverage. Catalog rows and their approval/implementation status remain unchanged.

