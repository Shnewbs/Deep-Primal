# Deep Primal block texture ownership and production plan

Owner direction recorded October 25, 2026: this texture workstream owns Deep Primal's block texture families across the roadmap and design documents. **32×32 is the default. Use 64×64 only when required detail cannot survive at 32×32.** This refines the former 64×64 ceiling; it does not change the separate approved UI/background dimensions.

## Sources and scope

Reviewed main snapshot: `df270704bcc261fac12324031c9aa978226f06ed`. Source documents: [roadmap](../../roadmap.md), [design reference](../design-reference.md), [natural type index](../NATURAL-TYPE-CATALOG.md), its geology/biology/environment catalogs, [environmental inventory](../ENVIRONMENTAL-COVERAGE-INVENTORY.md), [biome roster](../BIOME-CATALOG.md), [biome plan](../BIOME-PLAN.md), [variant crosswalk](../BIOME-VARIANTS-AND-COVERAGE.md), [glacial landscapes](../GLACIAL-LANDSCAPES.md), and [terrain mechanics](../TERRAIN-MECHANICS.md).

The [family map](block-texture-families.json) accounts for all **1,013 named rows in 28 sections**: 311 geology, 502 biology and 200 environmental entries. These include aliases, subtypes, states, forms, habitats and processes; they are not 1,013 distinct textures or approved blocks. Preserve each catalog's existing scope and approval status. Art preparation does not implement new gameplay or change world-generation placement.

This workstream covers material surfaces, block-face art, matching masks, block-use overlays, cutouts, animation frames and model-facing texture assignments. Coordinate model UVs and material channels with the implementing owner. Engine mechanics, renderer implementation, UI backgrounds and entity skins remain their existing workstreams.

## Texture rules

- Author for 32×32 at final block scale. Larger authoring masters and review boards are not runtime tiles.
- A 64×64 exception requires a side-by-side comparison showing which necessary feature is lost at 32: for example narrow roots, thin cutouts or a readable functional marking. Record the asset, reason, evidence and review status. Detail added only for decoration is not sufficient.
- Preserve grounded, natural, slightly weathered materials and the approved ground artwork's recognizable grass, soil clumps, mineral grit, litter and root transitions. Avoid featureless noise, heavy baked shadows and blurry resampling.
- Reuse neutral/grayscale families with material palettes where the existing pipeline supports them. Keep surface and soil masks independent. Do not bake an illustrative tint into a neutral production base.
- Distinguish materials by grain, fracture, bedding, bark, wood grain and litter structure as well as palette. Resolve aliases before assigning resources.
- Reuse material faces across stairs, slabs, walls, arches and other compatible shapes; add unique faces only when grain direction, end cuts or construction require them.
- Use exact PNG dimensions, consistent RGB/RGBA encoding and intentional alpha. Animation frames use the same 32 or justified 64 tile size; a strip's total height may be larger.
- Keep authoring masters and review art outside runtime resources. Maintain a provenance record, reproducible exports, filenames and checksums for accepted assets.

## Preserve existing approved ground art

Current resources include six 64×64 ground faces, four complementary 64×64 masks, and the separate 64×64 sand tile. Ground v4 restores the owner's chosen source artwork and supersedes the rejected v3 sides. See [ground v4](ground-textures-v4/README.md) and [final sand](sand-final/README.md).

Retain their current bytes while preparing 32×32 comparisons. The ground roots/leaves are candidates for a justified 64×64 exception; simple sand is a candidate for 32×32. Neither judgment is a completed size review. Do not regenerate the approved source artwork or silently replace its crops. Preserve the existing dual-tint material contract. Registered soil variants share this art; a material registration is not evidence of a unique painted texture.

## Family queue

This order prioritizes existing world surfaces, then the roadmap's material × shape foundation. Later families remain art backlog until their block representation is settled.

| Priority | Family | Source coverage | Required review set |
| --- | --- | --- | --- |
| 0 | Existing grass, dirt, podzol, sand | Ground v4 and final sand | Current approved art beside 32 candidates; top, side, masks, tint and repeated surface |
| 1 | Raw stone | ROCK-LEGACY, ROCK-ADD | Distinct grain/fracture/bedding, directional faces where needed; grayscale and palette samples |
| 2 | Sediments and clay | SEDIMENT, CLAY-MIN, CLAY-USE | Fine sand, silt, gravel, compact clay and mixed deposits; wet/dry treatment |
| 3 | Soil profiles and organic cover | TEXTURE, SOIL-ORDER, HORIZON, SOIL-QUAL, SOIL-STRUCT, ORGANIC-TYPE | Shared texture families, profile edges and litter/peat/roots; retain independent classification axes |
| 4 | Wood and foliage | TREE-LEGACY, WOODY-ADD, WOOD-FORM | Bark side, end grain, stripped side/end, planks, leaves; special palm/bamboo forms separately |
| 5 | Mineral, ore and gem surfaces | MINERAL plus relevant rock rows | Host-rock reuse, ore overlay, sparse/rich expression where supported, exposed mineral faces |
| 6 | Plants, crops and fungi | GRAMINOID, UNDERSTORY, CROP, FUNGI, CRYPTOGAM | Whole block family with cutout silhouette, top/side needs, growth/season/dead states |
| 7 | Aquatic and coastal materials | AQUATIC and sediment/organic rows | Aquatic cutouts, algae/kelp, biogenic surfaces and strandline materials |
| 8 | Snow, ice and volcanic surfaces | SNOW-ICE, VOLCANIC-FORM | Snow/firn/ice, frost/dirty overlays, ash/crust; reuse canonical rock and sediment families |
| 9 | Water and environmental appearance | WATER-TYPE, AIR-FORM | Shared optical palettes, foam and deposits; coordinate renderer-owned inputs and effects |
| 10 | Worked stone and architectural forms | Design §4.12, §4.17 | Rough/dressed/cut/polished masonry where applicable; joints, roof tiles and trims |
| 11 | Built wood, fiber and shelter | Design §4.12, §4.17; wood/organic catalogs | Boards, beams, framing, wattle, thatch, rope and material joins |
| 12 | Primitive workstations and ceramic blocks | Design §4.3 | Fire/pit/kiln materials, drying racks, hand mill, drill/lathe frames; unfired/fired surfaces |
| 13 | Metallurgy and processing machinery | Design §4.4, §4.8 | Copper/bronze/iron/steel/alloy material families, refractory masonry, machine faces |
| 14 | Mechanical power, irrigation and storage | Design §4.5 and roadmap transport/storage | Axles, gears, belts/chains, wheels, pumps and supported containers; share wood/metal surfaces |
| 15 | Signals, explosives and specialty blocks | Design §4.15–4.16; ANIMAL-MAT where block-used | Cable jackets/connectors, powder-keg surfaces and approved specialty material faces |
| Context | Habitats and processes | HABITAT-TYPE, PROCESS-TYPE, biome and glacial documents | Coverage scenarios selecting the families above; no standalone block tile per context |

## First raw-stone study

The first review board targets 32×32 and covers Granite, Basalt, Gabbro, Limestone, Sandstone, Shale, Slate, Marble, Quartzite and Chert. Each has a retained catalog row; this representative art batch does not settle the roadmap's final ten-rock gameplay roster.

Status: **design preview only**. It was generated with the built-in image-generation tool using the approved ground-direction board as a style reference. The exact [prompt](stone-family-32-v1/prompt.txt) is retained. No individual 32×32 PNGs, repeat/seam acceptance, mipmap checks, in-game lighting checks, runtime integration or release is claimed. The generated board is retained in the originating conversation.

## First 40 individual exports

[Batch 01](block-textures-32-batch01/README.md) now supplies **40 actual 32×32 PNG candidates**: ten raw stones, six sediment/soil/organic bases, eight wood faces, four building materials, five mineral/growth/ice surfaces, five foliage/plant/aquatic cutouts, a mushroom cap and scoria. Thirty-four are opaque; six use binary alpha. No 64×64 exception was needed.

Each master was generated individually, then mechanically exported and checked. The batch includes exact prompts, source and tile hashes, export/preview scripts, validation results, actual-export contact sheets, repeat proofs and cutout scale studies. Pine planks were revised after seam screening flagged the first export. All 40 pass dimensions, neutral-channel, alpha, uniqueness and hash checks; no repeat-screening flags remain. Repeated surfaces and actual-size art were inspected, but those checks do not replace native rendering review.

Status: **exported; native game review and model/tint assignment pending**. Thin foliage and ore features lose coverage in the mechanical 16/8/4px scale studies and need in-game cutout/mipmap checks. This is representative family coverage, not completion of catalog species, faces, growth states or machinery. Existing approved ground art remains untouched. See the batch README for the downloadable original masters and full source bundle.

## Rock base coverage: 70 more individual exports

[Batch 02](block-textures-32-batch02/README.md) adds **70 opaque 32×32 candidates**, bringing the two production batches to **110 distinct individual exports**. All 88 retained and expanded rock catalog rows now have base-art assignments through 81 unique candidates: 70 new here and 11 reused unchanged from batch 01. The [complete crosswalk](block-textures-32-batch02/ROCK-COVERAGE.md) retains the source names and explains shared assignments. This pass used catalog snapshot `7f1d6310accf40649f25bf2f6698cec1949940f3`.

Grouped mudstone/claystone, eclogite/granulite and ironstone/banded-iron entries now have distinct surfaces. Lignite, subbituminous coal, bituminous coal and anthracite have separate exports. Dolomite as a rock label shares dolostone art; the mineral remains a separate subject. Evaporite selects halite or gypsum. **Argillite has a provisional shared mudstone assignment pending a terminology decision.** No catalog classification or gameplay registration is changed.

All 70 pass dimensions, neutral channels, opacity, uniqueness and hashes. No automated wrap-screening flags remain. All seven contact and repeat sheets were visually reviewed, along with selected 32/16/8/4px scale comparisons. Coarse motifs and banding need large-surface orientation review, while fine grains and coal-rank differences fade at small scales. These checks do not establish native acceptance.

Status: **base candidates mapped; native game review, tint, model assignment and complete directional/state families pending**. No new 64×64 exception was needed. Original masters, individual proofs and reproduction scripts are retained in the downloadable source bundle. The family map continues to distinguish art coverage from runtime implementation and from completion of the wider 1,013-row inventory. Sediment, clay and soil families remain next in the production queue, alongside native review of this rock set.

## Per-family delivery and acceptance

1. Review the complete proposed family together, using actual native-size exports and enlarged nearest-neighbor views when exports exist. Identify concept boards explicitly.
2. Inspect repeated surfaces and the needed block faces. Check horizontal/vertical wrapping only where the material should repeat; organic sides require horizontal continuity and correct joins to the block below.
3. Verify dimensions, neutral channels, alpha, mask complements, mip levels and palette application. For 64 exceptions retain the 32 comparison.
4. Inspect native game lighting, orientation, seams, cutout/mipmap behavior, resource reload and near/far appearance with the engine owner before claiming integration.
5. Promote accepted artwork with its source, export recipe and hashes. Keep review candidates separate from runtime assets.

## Coordination

Check current main and relevant art/engine work state before each change. Use guarded main updates; reconcile concurrent commits. Texture ownership is recorded here for the other workstreams, with production statuses in the family map. Preserve existing accepted art and the engine's audit/build schedules. Production candidates remain under docs/art with their review state. No engine source or runtime texture is replaced by the current batch.


