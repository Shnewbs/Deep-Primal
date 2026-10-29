# Expanded replica — docs/ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md) |
| Source identity | git: docs/ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md |
| Preserved source SHA-256 | `50221c0f3a34c1fc6a8d1b75ac6268714357fd752bb53625682f39240b72360a` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 26 release record reports the sixteen-unit native fluid, dimension-scoped groundwater/snow stores, fixed-stage ocean and atmosphere accounts, sparse salt/sediment transport, and continental_v4 generation. It supplies concrete initialization, activation, recharge, discharge, and admission numbers, including 64×64 regions and 4096 environmental quanta per water block. It explicitly limits weather work to loaded catchments, excludes unloaded catch-up and renewable player springs, and lists unfinished terrain and water mechanics. These are dated implementation claims made by the source; this review does not revalidate current code or extend them to later profiles.

## Research expansion — gaps and details to deepen

- Elaboration: thresholds and capacities are already specified, but 'environmental steps', 'per environmental second', and rotating column visits need one timing vocabulary to make activation and throughput reproducible.
- Elaboration: proportional composition and retained rounding are documented; the region-wide multi-outlet transaction, retry identity, and concurrent save boundary are not fully described here.
- Clarification: legacy ocean adoption is guarded, but a test matrix should distinguish eligible unchanged surface water, edited shorelines, ordinary lakes at the same height, and unmarked deeper legacy water.

## Research expansion — proposed contracts

- Add a timing record containing simulation tick, environmental step index, elapsed credited seconds, visited region, sampled loaded catchment, and consumed regional discharge allowance. The 30-step activation rule must count eligible state transitions once, rather than once for each visited outlet.
- Specify one revisioned regional transfer plan: reserve donor water and constituent amounts, clamp the aggregate to 32 units per environmental second and each visit to its configured limit, commit accepted destinations once, then return rejected reservations without creating a later delivery backlog.
- Record source provenance separately from water composition. A full bucket moved from a marked Y=95 ocean boundary carries water and constituents, never the marker or recharge privilege; importing a legacy cell must not promote arbitrary matching-looking water.
- Publish a V4 compatibility matrix with generator codec, saved height, hydrology adoption status, material palette revision, and current geometry revision. New releases retain the documented geography and use an explicit new profile for changed generation.
- Make the nominal 2 ms admission window observable with maximum single-column time, percentile tick cost, useful visits, and oldest pending region age. Keep the existing caveat that checking time only between columns is not a hard deadline.

## Research expansion — acceptance checks

- Run one loaded region with 1, 4, and many eligible outlets; verify equal aggregate caps, no double debit, and no activation-speed increase from outlet count.
- Exercise exactly below/at/above 75%, 25%, and 12.5% storage, including interrupted runs and save/reopen during the 30-step counter.
- Test an ordinary lake and an edited dry ocean shoreline at sea stage 96; neither may acquire boundary replenishment.
- Replay partial accepted transfers and destination failure with salt/sediment; balances and donor remainders must match the reference ledger.

## Research expansion — source anchors

- Water ownership
- Ocean, lakes and chemistry
- Terrain V4
- A visit either recharges from the surface or discharges to it.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha 26: environmental water and continental V4

This revision implements finite environmental recovery and the next version of the independent Overworld terrain generator. It does not mark the entire terrain, climate, materials, or progression roadmap complete.

## Owner direction

The October 24 clarification permits streams to dry and sources to appear in new locations when sustained incoming water, storage and terrain support them. Players move measured water; they cannot place a renewable spring. This supersedes an unconditional minimum stream-flow guarantee for this implementation. Ordinary lakes remain finite. The ocean is an explicitly accounted, fixed-stage gameplay boundary.

## Water ownership

- Native fluid cells contain 1–16 units. One block is 16 units; environmental stores use 256 integer quanta per unit (4096 per block).
- Each dimension persists one groundwater/snow account per visited 64×64 region, plus coarse ocean/atmosphere accounts and sparse water composition. A region initializes its finite geological groundwater once. Subsequent chunk loads reuse it.
- Each groundwater and snow store holds at most 1024 blocks. Initial groundwater is 30–70% full according to seeded rainfall. Initial natural groundwater can supply existing natural waterways. New dry-land outlets require at least 75% storage for 30 environmental steps; visiting chunks or waiting on an unchanged initial store cannot activate them. An exhausted store must exceed 25% capacity for 30 environmental steps before reactivating, and shuts off below 12.5%.
- An aquifer's hydraulic datum is shared by its outlets and derived from the lowest of sixteen deterministic regional terrain samples. Storage changes the head. Actual exposed terrain determines where delivery is possible; the outlet need not be an original stream source.
- Springs pay for accepted water only. Solid/full destinations and backwater stop delivery without a delivery backlog. All outlets in one active regional work record share a maximum of 32 native units per environmental second, with a default maximum of four per visit.
- Higher surface water can infiltrate into available groundwater capacity. Clay and constructed floors are sealed; sand, gravel, soil and rock have different infiltration eligibility/rates. A visit either recharges from the surface or discharges to it.
- Weather recharge uses a coarse regional approximation: the currently loaded catchment area, the sampled exposed permeable surface, and the biome precipitation phase. Rain debits atmosphere into groundwater; snow enters a separate finite store and warm conditions move measured snow into groundwater. Groundwater evapotranspiration and coarse ocean evaporation return pure water to the atmosphere, retaining constituents.
- Work examines at most 64 surface columns per tick by default, rotating through loaded chunks. It stops between columns after a nominal 2 ms admission window. A single uncached calculation may exceed that window; this is not a proven hard 2 ms bound. No remote chunks are requested. Unloaded time produces no water, melt, delayed burst or activation catch-up.

Open natural flowing reaches use local gravity/routing instead of remote hydrostatic leveling of the entire connected river. This bounds the pressure shortcut to the intended communicating-vessel use instead of treating an entire sloping river as a static tank. Covered passages and enclosed wells/pipes retain communicating-vessel pressure; the open-channel classification uses saved geography and actual exposed/enclosed state.

## Ocean, lakes and chemistry

V4 records generated continental-ocean water origin, including its initial salinity. Only marked surface cells at Y=95 act as ocean boundaries (sea stage 96). Existing V1–V3 worlds can adopt the boundary only on a full, already existing sea surface with full neighboring water and matching ocean geography. This does not rebuild banks or refill edited dry terrain. Unmarked legacy underwater cells retain their previous fresh default until composition is transported into them; the migration does not rescan/reclassify the whole ocean.

Outgoing boundary water is replenished only by debiting the finite ocean account. Incoming water must reach sea stage, leave its sender, and be credited once. The boundary never adds a block above sea stage. Ordinary unmarked lakes at the same elevation do not receive this behavior. Exhausted or full accounts fail closed; their capacities are explicit, not unbounded counters.

The initial coarse ocean reserve is 134,217,728 water blocks and atmosphere reserve 67,108,864 blocks; both are finite declared initial circulation storage. These are gameplay regional-boundary abstractions, not a simulation of planetary ocean area or atmospheric physics. Generated surface water and finite geological initialization are initial conditions. Creative commands, creative buckets and destructive block replacement remain external inputs/removals.

Bulk transfer carries integer salt and sediment quantities in proportion to accepted volume, retaining rounding remainders in the donor. Groundwater, snow, ocean, atmosphere and sparse cell state are serialized together. Buckets preserve constituent quantities but never copy ocean/source identity. Constituents currently have no independent visual appearance, chemical reaction system or composition-only diffusion. There is no live erosion/deposition solver.

## Terrain V4

The default new-world packs now select `deepprimal:continental_v4`; V1/V2/V3 codecs remain available. The independent pipeline retains seeded continents/coasts, priority-flood watersheds, river/lake geometry, caves, climate-based biome selection and custom vegetation. V4 adds:

- Eight-neighbor bank containment against the actual discretized water surface, including bends and chunk boundaries.
- Seeded folded bedding, faults, rock provinces and intrusions using temporary native block palettes.
- Finite host-dependent ore/gem bodies. One deterministic regional deposit center is shared between columns; host geology determines eligibility, including high-elevation occurrences.
- Variable soil cover tied to rainfall, sediment and slope, revealing underlying geology in cuts.
- A broad latitude component in temperature, shared with biome selection.

This is a working terrain generation pipeline, not completion of every accepted environmental mechanic. Expanded biome/material catalogs, deposit datapack authoring/editor, geology-linked cave networks and through-mountain rivers, seasonal glacier geometry, dunes, material-dependent cave-ins, natural debris generation, surface-water evaporation/films and coarse transfers across unloaded borders remain open. Nether and End keep their native generators.

## Configuration and inspection

Server config: `deepprimal-hydrology.toml`.

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | true | Environmental recovery and ocean boundary exchanges |
| `existingEngineWorlds` | true | Enable hydrology in saved V1–V3 worlds without replacing terrain |
| `columnsPerTick` | 64 | Maximum loaded surface columns examined |
| `outletUnitsPerVisit` | 4 | Per-outlet delivery cap, inside the shared regional cap |
| `rainQuantaPerColumnSecond` | 64 | Coarse loaded-catchment precipitation rate before substrate weighting |

`/deepprimal info` reports the actual saved generator revision. `/deepprimal water` reports enabled state, visited local groundwater/snow storage, spring activation and coarse ocean/atmosphere storage without initializing a new aquifer. Finite drought and extraction can still dry streams; local ponding or overtopping remains possible when real supply exceeds an outlet.

See the alpha 26 validation report for measured checks and remaining acceptance limits. Do not infer owner-GPU performance, permanent perennial flow, entire-world hydrology simulation or full terrain-roadmap completion from this revision.


<!-- END PRESERVED SOURCE -->
