# Expanded replica — docs/GLACIAL-LANDSCAPES.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/GLACIAL-LANDSCAPES.md) |
| Source identity | git: docs/GLACIAL-LANDSCAPES.md |
| Preserved source SHA-256 | `514c8bc66bdedd7995a5b832d000609fbe376ed120ee50855c079ea312ffc8a7` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This approved coverage plan adds eight glacial layers and three community variants while retaining 118 base profiles, 54 total variants, 63 total layers, and the separate natural-type roster. It distinguishes active ice from inherited landforms, fjords from freshwater lakes, direct ice deposits from sorted outwash, and regional community mosaics from a universal succession sequence. It already requires a connected meltwater/lake/outlet network and one shared budget for braided channels. Runtime glacier motion, erosion, calving, retreat, failure, and terrain-changing succession remain separate future systems rather than delivered mechanics.

## Research expansion — gaps and details to deepen

- Elaboration: the active/relict distinction is present, but the persisted history descriptor and relationship to present climate eligibility are unspecified.
- Elaboration: braided branches already share a budget; a numerical split/rejoin record and zero-flow behavior would make that requirement testable.
- Clarification: moraine subtype is explicitly a field, yet the interfaces connecting substrate identity, lake control structure, and a proposed failure model are not defined.

## Research expansion — proposed contracts

- Add a GlacialLandscapeDescriptor with canonical feature ID, generation profile, active/relict classification, landform layer IDs, source-rock assemblage, controlling sill ID, and authored provenance. Present ice and generated history must remain independently queryable.
- Represent a braided reach as directed branch edges sharing one incoming account. For every update require sum(outgoing branch water)+retained storage=accepted inflow plus declared local inputs; constituent debits follow the same branch allocation, including deterministic integer remainders.
- Give every lake a unique basin/control record with bed support, freshwater or marine connectivity, sill elevation, capacity representation, and current water owner. A fjord label may not create an ocean boundary in an unconnected inland depression.
- Treat foreland community choices as eligible variants over independently sampled substrate, moisture, and regional biological pool. Do not encode a mandatory elapsed-time ladder that turns every exposed patch into forest.
- Provide a static-first implementation path: generated glacial geometry and authored materials can ship before moving ice. Any later melt or calving system must transfer measured water/ice and invalidate geometry, collision, water connectivity, and lighting together.

## Research expansion — acceptance checks

- Split one finite meltwater inflow into three channels and rejoin it; total water and composition remain unchanged across chunk order and reload.
- Construct otherwise similar freshwater and marine-connected valleys; verify distinct lake/fjord identities and source privileges.
- Inspect an inherited U-shaped valley under warm present conditions; landform survives while active ice eligibility is independently evaluated.
- Check all LAY-056–063 and VAR-052–054 references resolve to existing parents without increasing the base-profile count.

## Research expansion — source anchors

- Representation and named coverage
- Physical and water connections
- Communities and natural functions
- Glacier motion, retreat, calving, dam failure, erosion and terrain-changing succession remain separate future systems.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — glacial landscapes

Planning addition: 2026-10-21. **Owner-approved coverage direction; environmental implementation remains deferred.** Extend the biome plan with glacier-carved valleys/fjords, moraines and debris, braided meltwater/outwash, connected glacial lakes, and ice-margin communities. Names below are planning records.

[Biome plan](BIOME-PLAN.md) · [Named profiles](BIOME-CATALOG.md) · [Variants and layers](BIOME-VARIANTS-AND-COVERAGE.md)

## Representation and named coverage

Retain **118 proposed base profiles**. Add **3 community variants** (VAR-052–054) and **8 local layers** (LAY-056–063), bringing those lists to **54 variants and 63 layers**. Existing IDs and all 62 habitat crosswalk entries remain intact. These counts do not change the 1,013-row natural-type catalog.

The canonical rows, parent eligibility and source rules are in [the variants/layers catalog](BIOME-VARIANTS-AND-COVERAGE.md). The new records cover:

| Record | Named coverage | Planning role |
| --- | --- | --- |
| LAY-056 | Glacial trough / U-shaped valley | Erosional landform |
| LAY-057 | Fjord | Marine glacial landform |
| LAY-058 | Moraine ridge | Glacial deposit/relief |
| LAY-059 | Till and erratic field | Glacial substrate/deposit |
| LAY-060 | Outwash plain | Meltwater depositional landform |
| LAY-061 | Braided meltwater reach | River-reach form |
| LAY-062 | Proglacial lake margin | Ice–lake–land boundary |
| LAY-063 | Glacial foreland mosaic | Habitat/substrate mosaic |
| VAR-052 | Glacial foreland pioneer community | Biological community expression within an eligible parent profile |
| VAR-053 | Postglacial shrub–meadow community | Biological community expression within an eligible parent profile |
| VAR-054 | Postglacial woodland community | Biological community expression within an eligible parent profile |

Moraine subtype is a field rather than a separate biome ID. Glacial Lake, Icefield, existing tundra/alpine/forest profiles and existing ground themes remain the ecological/material parents.

## Physical and water connections

- Distinguish active ice, recently exposed margins and inherited glacial terrain. A glacial landform can remain after its ice has gone; present ice still requires suitable long-term conditions.
- Model the intended connection as ice/source catchment → meltwater reach → optional lake → controlling sill/outlet → downstream river → optional estuary/fjord/ocean. Lakes and fjords are not interchangeable: the latter have a marine connection.
- A braided reach shares one incoming water budget. Channels may divide and rejoin around sediment bars; their branch discharges must reconcile downstream. It is a local reach form within the routed network.
- Lake beds, shore belts, outlets and downstream carving must agree on water levels and solid support. Record moraine/bedrock controls explicitly. Do not assume every glacial lake is dammed by the same material.
- Use source-rock-aware till, boulders, gravel, sand and fine sediment from existing catalogs. Keep direct ice deposition and meltwater sorting distinct; suspended sediment is a separate water property.
- Water clarity and cold temperature do not determine drinking safety. Existing water-quality, salinity and source rules apply.
- During the later climate phase, set accumulation/melt, seasonal discharge, temperature and groundwater inputs with shared water budgets. Avoid adding water independently for each channel or lake.
- Glacier motion, retreat, calving, dam failure, erosion and terrain-changing succession remain separate future systems. Generated history and initial scenery do not imply live simulation.

## Communities and natural functions

Treat the foreland as a patchwork: exposed rock/till, moist hollows, sparse pioneer cover, eligible shrub/meadow patches and possible woodland. Local conditions and regional species pools decide its expression. Persistent tundra/barrens and wet habitats remain valid outcomes.

Use existing cryptogam, herb/grass, shrub/tree, soil and organic catalogs. VAR-052–054 describe community expressions, not a universal sequence or fixed time-to-forest. Exact species, dispersal, tolerances and renewal rates require property-sheet review. Glacier Bay is a reference example, not a species mix to copy globally.

Plan exposed source materials, access to meltwater, crossing constraints, sparse fuel near ice, sheltered vegetation pockets and shoreline/fauna hooks. Evaluate these against existing survival/spawn requirements without inventing numerical balance defaults.

## Map mockup direction

Preserve the existing continents, equator, polar regions, islands and major biomes. Enrich eligible northern/southern coasts and alpine valleys with broad glacial troughs, narrow seawater fjords, moraine ridges, sediment plains, split/rejoining meltwater channels and lakes with visible outlets. Show sparse-to-denser foreland vegetation where eligible. Use restrained labels and preserve the world map's illustrative status and unspecified scale.

## Planning review

- [ ] Map each selected glacial setting to existing parent profiles, actual material references and the new layer/community IDs.
- [ ] Review active/relict history, marine/freshwater boundaries, solid support and downstream connectivity.
- [ ] Review community pools, natural-resource roles and sparse-cover exceptions.
- [ ] Defer numerical climate thresholds, size/frequency tuning and runtime simulation choices to their agreed later phases.
- [ ] When implementation is authorized, verify connected outlets, branch budgets, region boundaries, source materials, species eligibility and full-detail/LOD agreement.

## Primary research anchors

These sources support the geological/ecological distinctions. The record IDs, mappings and game contracts are our design proposals.

- [USGS glacier terminology](https://pubs.usgs.gov/of/2004/1216/text.html): glacial landform and fjord definitions.
- [NPS Kenai Fjords glacial features](https://www.nps.gov/kefj/learn/nature/glaciers.htm): eroded valleys and deposited debris.
- [NPS glacier terminology](https://www.nps.gov/kefj/learn/nature/glacier-terminology.htm): till, moraine subtypes and meltwater-sorted outwash.
- [USGS Cape Cod glacial history](https://pubs.usgs.gov/gip/7000013/report.pdf): meltwater sediment and braided channel context.
- [NPS Glacier Bay succession study](https://www.nps.gov/glba/learn/nature/succession.htm): changing communities on exposed glacial ground; local history is not a universal timetable.


<!-- END PRESERVED SOURCE -->
