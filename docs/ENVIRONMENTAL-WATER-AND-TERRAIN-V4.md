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
