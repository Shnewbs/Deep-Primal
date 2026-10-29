# Expanded replica — docs/art/block-textures-32-batch02/ROCK-COVERAGE.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/ROCK-COVERAGE.md) |
| Source identity | git: docs/art/block-textures-32-batch02/ROCK-COVERAGE.md |
| Preserved source SHA-256 | `705000b127d9c4a8a5a2b9849bdd9ef28c8dbc573edd7eda06998d6f709ec9f4` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The rock coverage table maps 44 legacy and 44 additional rows to the candidate grayscale bases. It explicitly handles grouped entries, reused materials and provisional choices: dolomite rock maps to dolostone rather than a mineral tile; lignite, bituminous and anthracite are distinct, with subbituminous added separately; some ironstone, eclogite and granulite entries reuse established bases; argillite provisionally reuses mudstone; and the evaporite umbrella resolves to halite/gypsum rather than inventing a third file. This is an asset crosswalk, not a declaration that all 88 rocks are registered, balanced, generated or visually accepted in the game.

## Research expansion — gaps and details to deepen

- The table is rich in human explanations but needs stable IDs and relationship types to prevent accidental collapse of taxonomy, gameplay definition and texture identity. A broad catalog row can intentionally resolve to multiple variants, while multiple rows can reuse one visual base. Those are different relationships with different runtime consequences.
- Provisional mappings require a decision owner and promotion condition. Without that state, a temporary reuse can become permanent through copy-and-paste, or a future texture replacement can unexpectedly alter every dependent family. Native acceptance and source approval should be attached to asset versions rather than inferred from table coverage.

## Research expansion — proposed contracts

- Represent catalog row, material definition, visual family, texture asset and face/shape binding as separate stable identities. Give each crosswalk edge a role such as exact base, shared family, provisional substitute, grouped expansion or comparison only. Generate counts from unique identities and document why they differ.
- Add an explicit variant-selection contract for grouped rows: deterministic seed or authored rule, valid host or process context, and no silent alteration of saved generation. Keep physical properties and ore/mineral taxonomy in their domain schemas so a shared texture never assigns composition or behavior by filename.

## Research expansion — acceptance checks

- Resolve every row to existing candidate files and detect dangling references, duplicated IDs and accidental promotion of the generic-coal comparison. Verify the expected 88-row/81-base relationship while allowing legitimate many-to-many mappings.
- Review argillite and umbrella evaporite decisions explicitly, inspect distinct coal ranks and grouped families under final tint, and check that replacing one visual asset updates only declared dependents. Require separate runtime registration and generation evidence before calling the catalog implemented.

## Research expansion — source anchors

- Rock base candidate coverage

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../block-textures-32-batch01/tiles/stone/basalt.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/basalt.png) |
| `../block-textures-32-batch01/tiles/stone/chert.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/chert.png) |
| `../block-textures-32-batch01/tiles/stone/gabbro.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/gabbro.png) |
| `../block-textures-32-batch01/tiles/stone/granite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/granite.png) |
| `../block-textures-32-batch01/tiles/stone/limestone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/limestone.png) |
| `../block-textures-32-batch01/tiles/stone/marble.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/marble.png) |
| `../block-textures-32-batch01/tiles/stone/quartzite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/quartzite.png) |
| `../block-textures-32-batch01/tiles/stone/sandstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/sandstone.png) |
| `../block-textures-32-batch01/tiles/stone/shale.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/shale.png) |
| `../block-textures-32-batch01/tiles/stone/slate.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/stone/slate.png) |
| `../block-textures-32-batch01/tiles/volcanic/scoria.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch01/tiles/volcanic/scoria.png) |
| `../block-textures-32-batch02/tiles/stone/amphibolite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/amphibolite.png) |
| `../block-textures-32-batch02/tiles/stone/andesite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/andesite.png) |
| `../block-textures-32-batch02/tiles/stone/anorthosite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/anorthosite.png) |
| `../block-textures-32-batch02/tiles/stone/anthracite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/anthracite.png) |
| `../block-textures-32-batch02/tiles/stone/arkose.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/arkose.png) |
| `../block-textures-32-batch02/tiles/stone/banded_iron_formation.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/banded_iron_formation.png) |
| `../block-textures-32-batch02/tiles/stone/bituminous_coal.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/bituminous_coal.png) |
| `../block-textures-32-batch02/tiles/stone/blueschist.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/blueschist.png) |
| `../block-textures-32-batch02/tiles/stone/breccia.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/breccia.png) |
| `../block-textures-32-batch02/tiles/stone/cataclasite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/cataclasite.png) |
| `../block-textures-32-batch02/tiles/stone/chalk.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/chalk.png) |
| `../block-textures-32-batch02/tiles/stone/claystone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/claystone.png) |
| `../block-textures-32-batch02/tiles/stone/conglomerate.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/conglomerate.png) |
| `../block-textures-32-batch02/tiles/stone/coquina.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/coquina.png) |
| `../block-textures-32-batch02/tiles/stone/dacite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/dacite.png) |
| `../block-textures-32-batch02/tiles/stone/diatomite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/diatomite.png) |
| `../block-textures-32-batch02/tiles/stone/diorite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/diorite.png) |
| `../block-textures-32-batch02/tiles/stone/dolerite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/dolerite.png) |
| `../block-textures-32-batch02/tiles/stone/dolostone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/dolostone.png) |
| `../block-textures-32-batch02/tiles/stone/dunite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/dunite.png) |
| `../block-textures-32-batch02/tiles/stone/eclogite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/eclogite.png) |
| `../block-textures-32-batch02/tiles/stone/flint.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/flint.png) |
| `../block-textures-32-batch02/tiles/stone/gneiss.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/gneiss.png) |
| `../block-textures-32-batch02/tiles/stone/granodiorite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/granodiorite.png) |
| `../block-textures-32-batch02/tiles/stone/granulite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/granulite.png) |
| `../block-textures-32-batch02/tiles/stone/greenschist.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/greenschist.png) |
| `../block-textures-32-batch02/tiles/stone/greywacke.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/greywacke.png) |
| `../block-textures-32-batch02/tiles/stone/gypsum.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/gypsum.png) |
| `../block-textures-32-batch02/tiles/stone/halite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/halite.png) |
| `../block-textures-32-batch02/tiles/stone/harzburgite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/harzburgite.png) |
| `../block-textures-32-batch02/tiles/stone/hornfels.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/hornfels.png) |
| `../block-textures-32-batch02/tiles/stone/ignimbrite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/ignimbrite.png) |
| `../block-textures-32-batch02/tiles/stone/ironstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/ironstone.png) |
| `../block-textures-32-batch02/tiles/stone/kimberlite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/kimberlite.png) |
| `../block-textures-32-batch02/tiles/stone/komatiite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/komatiite.png) |
| `../block-textures-32-batch02/tiles/stone/lamproite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/lamproite.png) |
| `../block-textures-32-batch02/tiles/stone/laterite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/laterite.png) |
| `../block-textures-32-batch02/tiles/stone/lherzolite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/lherzolite.png) |
| `../block-textures-32-batch02/tiles/stone/lignite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/lignite.png) |
| `../block-textures-32-batch02/tiles/stone/marlstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/marlstone.png) |
| `../block-textures-32-batch02/tiles/stone/metaconglomerate.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/metaconglomerate.png) |
| `../block-textures-32-batch02/tiles/stone/migmatite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/migmatite.png) |
| `../block-textures-32-batch02/tiles/stone/monzonite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/monzonite.png) |
| `../block-textures-32-batch02/tiles/stone/mudstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/mudstone.png) |
| `../block-textures-32-batch02/tiles/stone/mylonite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/mylonite.png) |
| `../block-textures-32-batch02/tiles/stone/obsidian.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/obsidian.png) |
| `../block-textures-32-batch02/tiles/stone/oil_shale.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/oil_shale.png) |
| `../block-textures-32-batch02/tiles/stone/pegmatite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/pegmatite.png) |
| `../block-textures-32-batch02/tiles/stone/peridotite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/peridotite.png) |
| `../block-textures-32-batch02/tiles/stone/phonolite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/phonolite.png) |
| `../block-textures-32-batch02/tiles/stone/phosphorite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/phosphorite.png) |
| `../block-textures-32-batch02/tiles/stone/phyllite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/phyllite.png) |
| `../block-textures-32-batch02/tiles/stone/pumice.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/pumice.png) |
| `../block-textures-32-batch02/tiles/stone/pyroxenite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/pyroxenite.png) |
| `../block-textures-32-batch02/tiles/stone/quartz_arenite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/quartz_arenite.png) |
| `../block-textures-32-batch02/tiles/stone/radiolarite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/radiolarite.png) |
| `../block-textures-32-batch02/tiles/stone/rhyolite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/rhyolite.png) |
| `../block-textures-32-batch02/tiles/stone/schist.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/schist.png) |
| `../block-textures-32-batch02/tiles/stone/serpentinite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/serpentinite.png) |
| `../block-textures-32-batch02/tiles/stone/siltstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/siltstone.png) |
| `../block-textures-32-batch02/tiles/stone/skarn.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/skarn.png) |
| `../block-textures-32-batch02/tiles/stone/soapstone.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/soapstone.png) |
| `../block-textures-32-batch02/tiles/stone/subbituminous_coal.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/subbituminous_coal.png) |
| `../block-textures-32-batch02/tiles/stone/syenite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/syenite.png) |
| `../block-textures-32-batch02/tiles/stone/tonalite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/tonalite.png) |
| `../block-textures-32-batch02/tiles/stone/trachyandesite.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/trachyandesite.png) |
| `../block-textures-32-batch02/tiles/stone/trachyte.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/trachyte.png) |
| `../block-textures-32-batch02/tiles/stone/travertine.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/travertine.png) |
| `../block-textures-32-batch02/tiles/stone/tuff.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/tuff.png) |
| `../block-textures-32-batch02/tiles/stone/volcanic_agglomerate.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/art/block-textures-32-batch02/tiles/stone/volcanic_agglomerate.png) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Rock base candidate coverage

All 88 retained/expanded rock catalog rows have base-art assignments. Argillite has a provisional shared assignment pending terminology review. These are candidate surfaces, not accepted gameplay registrations or complete directional/state families.

| Catalog ID | Source name | Candidate base(s) | Mapping |
| --- | --- | --- | --- |
| ROCK-LEGACY-001 | Granite | [granite](../block-textures-32-batch01/tiles/stone/granite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-002 | Diorite | [diorite](../block-textures-32-batch02/tiles/stone/diorite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-003 | Gabbro | [gabbro](../block-textures-32-batch01/tiles/stone/gabbro.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-004 | Basalt | [basalt](../block-textures-32-batch01/tiles/stone/basalt.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-005 | Andesite | [andesite](../block-textures-32-batch02/tiles/stone/andesite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-006 | Rhyolite | [rhyolite](../block-textures-32-batch02/tiles/stone/rhyolite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-007 | Obsidian | [obsidian](../block-textures-32-batch02/tiles/stone/obsidian.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-008 | Pumice | [pumice](../block-textures-32-batch02/tiles/stone/pumice.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-009 | Tuff | [tuff](../block-textures-32-batch02/tiles/stone/tuff.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-010 | Scoria | [scoria](../block-textures-32-batch01/tiles/volcanic/scoria.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-011 | Peridotite | [peridotite](../block-textures-32-batch02/tiles/stone/peridotite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-012 | Pegmatite | [pegmatite](../block-textures-32-batch02/tiles/stone/pegmatite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-013 | Dolerite | [dolerite](../block-textures-32-batch02/tiles/stone/dolerite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-014 | Syenite | [syenite](../block-textures-32-batch02/tiles/stone/syenite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-015 | Sandstone | [sandstone](../block-textures-32-batch01/tiles/stone/sandstone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-016 | Limestone | [limestone](../block-textures-32-batch01/tiles/stone/limestone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-017 | Chalk | [chalk](../block-textures-32-batch02/tiles/stone/chalk.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-018 | Dolomite | [dolostone](../block-textures-32-batch02/tiles/stone/dolostone.png) | Retained Dolomite rock label maps to the dolostone base; mineral dolomite is a separate catalog subject. |
| ROCK-LEGACY-019 | Shale | [shale](../block-textures-32-batch01/tiles/stone/shale.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-020 | Mudstone / claystone | [mudstone](../block-textures-32-batch02/tiles/stone/mudstone.png), [claystone](../block-textures-32-batch02/tiles/stone/claystone.png) | Retained grouped row now has distinct mudstone and claystone candidates. |
| ROCK-LEGACY-021 | Siltstone | [siltstone](../block-textures-32-batch02/tiles/stone/siltstone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-022 | Conglomerate | [conglomerate](../block-textures-32-batch02/tiles/stone/conglomerate.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-023 | Breccia | [breccia](../block-textures-32-batch02/tiles/stone/breccia.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-024 | Greywacke | [greywacke](../block-textures-32-batch02/tiles/stone/greywacke.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-025 | Chert | [chert](../block-textures-32-batch01/tiles/stone/chert.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-026 | Flint | [flint](../block-textures-32-batch02/tiles/stone/flint.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-027 | Coal (lignite, bituminous, anthracite) | [lignite](../block-textures-32-batch02/tiles/stone/lignite.png), [bituminous_coal](../block-textures-32-batch02/tiles/stone/bituminous_coal.png), [anthracite](../block-textures-32-batch02/tiles/stone/anthracite.png) | The three ranks named in this retained row now have separate candidates. Generic batch-01 coal remains a historical comparison, not a rank assignment. |
| ROCK-LEGACY-028 | Gypsum | [gypsum](../block-textures-32-batch02/tiles/stone/gypsum.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-029 | Rock salt (halite) | [halite](../block-textures-32-batch02/tiles/stone/halite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-030 | Travertine | [travertine](../block-textures-32-batch02/tiles/stone/travertine.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-031 | Ironstone / banded iron | [ironstone](../block-textures-32-batch02/tiles/stone/ironstone.png), [banded_iron_formation](../block-textures-32-batch02/tiles/stone/banded_iron_formation.png) | Ironstone and banded iron formation have separate candidate surfaces. |
| ROCK-LEGACY-032 | Coquina | [coquina](../block-textures-32-batch02/tiles/stone/coquina.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-033 | Laterite | [laterite](../block-textures-32-batch02/tiles/stone/laterite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-034 | Marble | [marble](../block-textures-32-batch01/tiles/stone/marble.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-035 | Slate | [slate](../block-textures-32-batch01/tiles/stone/slate.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-036 | Schist | [schist](../block-textures-32-batch02/tiles/stone/schist.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-037 | Gneiss | [gneiss](../block-textures-32-batch02/tiles/stone/gneiss.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-038 | Quartzite | [quartzite](../block-textures-32-batch01/tiles/stone/quartzite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-039 | Phyllite | [phyllite](../block-textures-32-batch02/tiles/stone/phyllite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-040 | Hornfels | [hornfels](../block-textures-32-batch02/tiles/stone/hornfels.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-041 | Amphibolite | [amphibolite](../block-textures-32-batch02/tiles/stone/amphibolite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-042 | Serpentinite | [serpentinite](../block-textures-32-batch02/tiles/stone/serpentinite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-043 | Soapstone (steatite) | [soapstone](../block-textures-32-batch02/tiles/stone/soapstone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-LEGACY-044 | Eclogite / granulite | [eclogite](../block-textures-32-batch02/tiles/stone/eclogite.png), [granulite](../block-textures-32-batch02/tiles/stone/granulite.png) | Eclogite and granulite have separate candidate surfaces. |
| ROCK-ADD-001 | Granodiorite | [granodiorite](../block-textures-32-batch02/tiles/stone/granodiorite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-002 | Tonalite | [tonalite](../block-textures-32-batch02/tiles/stone/tonalite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-003 | Monzonite | [monzonite](../block-textures-32-batch02/tiles/stone/monzonite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-004 | Anorthosite | [anorthosite](../block-textures-32-batch02/tiles/stone/anorthosite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-005 | Dunite | [dunite](../block-textures-32-batch02/tiles/stone/dunite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-006 | Harzburgite | [harzburgite](../block-textures-32-batch02/tiles/stone/harzburgite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-007 | Lherzolite | [lherzolite](../block-textures-32-batch02/tiles/stone/lherzolite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-008 | Pyroxenite | [pyroxenite](../block-textures-32-batch02/tiles/stone/pyroxenite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-009 | Dacite | [dacite](../block-textures-32-batch02/tiles/stone/dacite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-010 | Trachyte | [trachyte](../block-textures-32-batch02/tiles/stone/trachyte.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-011 | Phonolite | [phonolite](../block-textures-32-batch02/tiles/stone/phonolite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-012 | Trachyandesite | [trachyandesite](../block-textures-32-batch02/tiles/stone/trachyandesite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-013 | Komatiite | [komatiite](../block-textures-32-batch02/tiles/stone/komatiite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-014 | Kimberlite | [kimberlite](../block-textures-32-batch02/tiles/stone/kimberlite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-015 | Lamproite | [lamproite](../block-textures-32-batch02/tiles/stone/lamproite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-016 | Ignimbrite | [ignimbrite](../block-textures-32-batch02/tiles/stone/ignimbrite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-017 | Volcanic agglomerate | [volcanic_agglomerate](../block-textures-32-batch02/tiles/stone/volcanic_agglomerate.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-018 | Arkose | [arkose](../block-textures-32-batch02/tiles/stone/arkose.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-019 | Quartz arenite | [quartz_arenite](../block-textures-32-batch02/tiles/stone/quartz_arenite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-020 | Claystone | [claystone](../block-textures-32-batch02/tiles/stone/claystone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-021 | Mudstone | [mudstone](../block-textures-32-batch02/tiles/stone/mudstone.png) | Reuses the retained mudstone candidate. |
| ROCK-ADD-022 | Marlstone | [marlstone](../block-textures-32-batch02/tiles/stone/marlstone.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-023 | Dolostone | [dolostone](../block-textures-32-batch02/tiles/stone/dolostone.png) | Reuses the dolostone candidate mapped from the retained Dolomite rock label. |
| ROCK-ADD-024 | Diatomite | [diatomite](../block-textures-32-batch02/tiles/stone/diatomite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-025 | Radiolarite | [radiolarite](../block-textures-32-batch02/tiles/stone/radiolarite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-026 | Phosphorite | [phosphorite](../block-textures-32-batch02/tiles/stone/phosphorite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-027 | Oil shale | [oil_shale](../block-textures-32-batch02/tiles/stone/oil_shale.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-028 | Lignite | [lignite](../block-textures-32-batch02/tiles/stone/lignite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-029 | Subbituminous coal | [subbituminous_coal](../block-textures-32-batch02/tiles/stone/subbituminous_coal.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-030 | Bituminous coal | [bituminous_coal](../block-textures-32-batch02/tiles/stone/bituminous_coal.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-031 | Anthracite | [anthracite](../block-textures-32-batch02/tiles/stone/anthracite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-032 | Banded iron formation | [banded_iron_formation](../block-textures-32-batch02/tiles/stone/banded_iron_formation.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-033 | Ironstone | [ironstone](../block-textures-32-batch02/tiles/stone/ironstone.png) | Reuses the ironstone candidate from the retained grouped row. |
| ROCK-ADD-034 | Eclogite | [eclogite](../block-textures-32-batch02/tiles/stone/eclogite.png) | Reuses the eclogite candidate from the retained grouped row. |
| ROCK-ADD-035 | Granulite | [granulite](../block-textures-32-batch02/tiles/stone/granulite.png) | Reuses the granulite candidate from the retained grouped row. |
| ROCK-ADD-036 | Migmatite | [migmatite](../block-textures-32-batch02/tiles/stone/migmatite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-037 | Skarn | [skarn](../block-textures-32-batch02/tiles/stone/skarn.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-038 | Greenschist | [greenschist](../block-textures-32-batch02/tiles/stone/greenschist.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-039 | Blueschist | [blueschist](../block-textures-32-batch02/tiles/stone/blueschist.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-040 | Mylonite | [mylonite](../block-textures-32-batch02/tiles/stone/mylonite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-041 | Cataclasite | [cataclasite](../block-textures-32-batch02/tiles/stone/cataclasite.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-042 | Metaconglomerate | [metaconglomerate](../block-textures-32-batch02/tiles/stone/metaconglomerate.png) | Individual grayscale base candidate; palette, directional faces and native rendering review remain pending. |
| ROCK-ADD-043 | Argillite | [mudstone](../block-textures-32-batch02/tiles/stone/mudstone.png) | Provisional art reuse of compact mudstone. Argillite terminology and rock classification still require a design decision; this is not a taxonomy or gameplay change. |
| ROCK-ADD-044 | Evaporite rock | [halite](../block-textures-32-batch02/tiles/stone/halite.png), [gypsum](../block-textures-32-batch02/tiles/stone/gypsum.png) | Umbrella family selects salt- or gypsum-bearing base art; no third duplicate evaporite resource is introduced. |


<!-- END PRESERVED SOURCE -->
