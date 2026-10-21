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
