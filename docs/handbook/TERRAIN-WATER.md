# Terrain, Water, Geology and Distant Coverage

**Consolidated:** 2026-10-29. **Role:** canonical behavioral chapter and source map. This merges the distinct requirements in the sources below; it does not promote a new implementation, certify current main, or alter saved worlds. A status attached to alpha 26, V6, V7 or a development checkpoint describes that checkpoint only. Later current-source receipts must be added by explicit scope.

**Latest global version observation supplied during this consolidation:** pinned `8f28f833` declares `0.0.1-alpha.29-dev.88-vegetation-receipts.1`. This is global source identity, not native-world qualification. The active terrain-blending work state labels dev.88; its vegetation-receipts README was absent from the reviewed inventory. No missing receipt is invented or promoted. The dated implementation matrix below remains scoped to the documents actually read.

## 1. Authority and governing decisions

Deep Primal owns the original Overworld terrain, biome, drainage and finite-fluid design. Minecraft/NeoForge remain the current host for saves, registries, scheduling, lighting, networking, entities and GPU integration; the standalone direction is a separate migration boundary. Nether and End retain their native generators in the documented prototypes.

Plan one connected world: define the biome roster first, then use climate, geology, soils and water to gate placement. Do not stack unrelated generators or treat catalogs as a count of delivered blocks/species. Reference engines, design software and mods provide inspected techniques and comparison targets. They do not authorize dependencies, copied assets, parity claims or guaranteed performance.

The governing rules are:

- Preserve saved generator/profile/height, generated geography and player edits. Creation defaults affect new worlds; geographic changes require an explicit saved revision, with old revisions retained. No retrogen is implied.
- Every water quantum and conserved constituent has exactly one authoritative owner. Accept a transfer only if matching debit and credit can commit; blocked output stays in its backing account with no emission backlog.
- Full finite water cells do not independently regenerate. Players move measured water and never obtain natural spring/ocean authority by moving a bucket.
- The October 24 finite-environmental model permits drought and later recharge-supported outlets. The older unconditional perennial-river allocation is historical/optional, not the implemented default. Distinguish that policy from the later *natural delivery quota*, which still spends finite groundwater.
- Ordinary lakes are finite. A marked, connected ocean boundary uses accounted circulation at prescribed mean sea stage. A sea-level Y coordinate alone never makes an inland lake an ocean.
- Underground karst, returning springs and mountain rivers are accepted. Generated sinkholes and live surface-collapse sinkholes are excluded. Cave-ins must arrest before breaching protected surface cover.
- Live erosion/sediment transport, live sand movement and destructive dam failure are optional extensions with their own scope and budgets. Consumed-input panning/sluice recipes are the baseline.
- Presentation, water smoothing, light, LOD, foam, canopy droplets, puddle effects and graphics choices never alter water quantities, collision truth or saved geography.
- Current loaded-only hydrology does no unloaded-time catch-up and seals unloaded borders. Future coarse exchange requires a separately enabled ownership/persistence policy.
- Passing a bounded helper or software-driver fixture proves that fixture. Native world generation, save/reopen, long-run hydrology, visual coverage and owner-hardware performance remain separate evidence.

**Sources:** [terrain scope][T], [water ownership and operating model][B], [alpha 26 owner direction][E], [Water Engine §§0–1, 9.6][W], [engine charter][L].

## 2. Saved revisions and documented implementation boundaries

| Checkpoint | Behavior recorded in its source | Limit retained in the merge |
| --- | --- | --- |
| Alpha 26 / continental V4 | Eight-neighbor bank containment; folded/faulted geological provinces and intrusions; finite host-dependent deposits; soil cover and latitude temperature; regional groundwater/snow and ocean/atmosphere accounts; salt/sediment advection | Temporary native palettes; incomplete global drainage, climate, catalog and gameplay integration |
| Alpha 27 / continental V6 | Geological cave routes and supported river tunnels; topsoil/subsoil/weathered parent horizons; climate palette; dunes, cold troughs/moraines/solid glacier ice; finite seasonal ablation; authored deposits; debris and bounded underground failures | No surface-landslide dynamics, moving ice sheets, external editor or KubeJS-specific authoring API |
| Alpha 28 / continental V7 | Seed-derived sampling phase fixes the local origin alias; blended lowland/hill/plateau/ridge forms; seed-varied continents/equator/rainfall; wider planned channels; biome-dependent canopy and ground cover | Existing V1–V6 saves stay exact, including newly explored chunks. Wider plan radius is not guaranteed wet raster width or perpetual refill |
| V7 channel revision 2 / dev.20 record | Great rivers/estuary widening; incised canyon trunks and hanging tributary cascades; shared-node meanders; dry braid bars | Saved revisions 0/1 replay; no fully routed physical discharge or separately budgeted braid branches |
| Channel revisions 3–5 | Opt-in delta fans, static oxbows, then conserved planned distributary shares and share-based width | Default V7 remains channel revision 2; 3/4 replay; these are core-checked staged geometry, without native generation/save-reopen/visual acceptance |
| dev.54 ecotone | Normalized continuous weights over the existing 40-biome palette, quintic boundaries and exact hard comparisons at zero width | Explicit-call-only core infrastructure: no native activation, changed heights or changed water. Old V8 snapshots and dev.53 callback behavior remain exact |
| dev.6–17 hydrology records | Natural head reference; more responsive planned-source scheduling; separate natural quota; protected drought reserve; representative loaded-chunk precipitation; fair rain/snow allocation; exact permeability subset | Finite accounts, stage cap and no catch-up remain. Component timings and bounded scenarios do not qualify catchment-scale climate or storms |
| dev.18–19 presentation records | Bounded midpoint hold/release and continuous resident boundary-height publication | No water-quantity change; CPU mirrors and isolated GPU readbacks are distinct from all-frame rendered waterfall/transparency/performance acceptance |

The alpha 26 consolidation's statement that “all outlets share 32 units” is historical. The later dev.8 record separates planned natural delivery, calculated from modeled mean recharge (80 units/interval at the documented default, never below 32), from dry-land springs' 32-unit quota. Both still debit finite storage. Do not combine the old cap with the newer quota as if both described one unchanged policy.

The dev.18 failure paragraph remains a historical witness after the dev.19 candidate correction; it must not be erased or repeated as a newly reproduced current defect. The wider acceptance gates remain open.

**Sources:** [E: Terrain V4 and Water ownership][E], [V6: all sections][V6], [V7: Changes, Channel revisions, Water rendering, Compatibility][V7], [blend: Current checkpoint and Integration sequence][C], [L: dated dev.6–20 updates][L]. V8 regional-topology succession belongs to the separate source-pinned research edition and current implementation receipts; this chapter does not infer native activation from a newer helper.

## 3. One terrain pipeline with shared geology and drainage

Resolve immutable saved inputs, macro relief and geological structures before dependent deposits, drainage/carving, material/soil eligibility and features. Persistent edits/depletion override regenerated baseline. Region plans and shared boundaries must be canonical in absolute coordinates; worker count, chunk order and cache eviction cannot change the declared deterministic result.

**Structural geology and deposits.** Keep coherent three-dimensional bodies, bedding/folds/faults, intrusions, contacts and metamorphic transitions across region seams. Cliffs and excavations reveal the same underlying units. Host geology, formation setting, structure and cover govern finite ore/gem occurrence; current elevation alone is not eligibility. Include barren rock, grade variation and rare rich pockets. No universal high-elevation ore ban, automatic gem enrichment or free resources from duplicate representations.

V6's implemented deposit schema uses stable namespaced IDs; lens/vein/pipe/disc ellipsoids; separate occurrence/spacing and internal density; host constraints; weighted block-state contents; and absolute, depth-below-anchor-surface or geological coordinates. IDs order overlap precedence, not file order. Center ranges do not truncate radii; ground cover and world limits do. It accepts at most 32 definitions, 16 contents per definition and a compiled estimated neighborhood of 1,024 candidates/chunk; invalid ranges, duplicate IDs and air/fluid contents reject compilation. Precomputed transforms/intersections avoid voxel-time allocation, trigonometry, scripts and file reads. Shapes are bounded bodies, not branching mineral networks.

**Drainage and landforms.** Use connected tributaries, canonical regional outlets and a unified acyclic river/lake/spillway plan before carving. Headwaters have stable identity. Width, depth, speed and source scale fit rainfall-weighted supply and slope: a spring can begin narrow; a broad headwater needs an appropriate lake/catchment. Distinguish cumulative drainage area, planned discharge shares and accepted physical transfer. Confluences conserve supply, and branches divide it.

V7 revision 2 caps radius at 112 blocks; its canyon trunks, stepped banks, meanders and braid bars are specific authored forms. Opt-in delta fans are bounded to their footprint, and revision-5 branch shares sum to one; oxbows remain still, contained cut-offs. Neither static geomorphology nor planned branch shares implement meander migration, avulsion, anastomosing channels or a live sediment solver.

**Caves and mountain rivers.** Place dissolution chambers only in suitable rock and fractures/other routes elsewhere. Use supported entrances, roofs, cross-sections, bends, pools, side inflows and re-emergence under actual head. Entry/exit share supply; no duplicated exit source or magic uphill open-water flow. Blockage can back up or redirect accounted water. V6 protects at least ten blocks of initial cave cover, excludes glacier ice from cover calculations and retains a tunnel roof only when the full carved cross-section has cover. Perched cave water is finite initial water.

**Soils, wind, glaciers and hazards.** Distinguish litter/organic input, topsoil, subsoil, weathered parent and bedrock where appropriate; correlate depth with parent geology, slope, moisture and depositional setting. Share permeability, saturation, fertility/chemistry, roots, frost and excavation properties without registering every combination separately. Dunes/sand sheets and cold/coastal wind forms need available material, shared exposure/history and seam continuity; no mandatory globally ticking sand.

V6 glacier relief and solid ice are generated; its 96-game-day temperature cycle admits finite exposed ice/snow ablation into one full water block paid by removing one physical block. This is not moving ice-sheet dynamics. Bounded cave-ins use material span, moisture weakening and real constructed supports; preserve twelve blocks of actual and generated surface cover, at most 256 persisted positions and eight checks/tick. Missing support/chunks, water below or drops beyond 64 blocks arrest the event; rock moves to a verified floor without duplicating ore/items or replacing water. Surface slope failures remain a broader design requirement.

Fallen wood, root plates, debris and rock use persistent ordinary features where possible. Real obstruction changes channel capacity, ponding/spill/bypass and removal releases retained water under the same ledger. Re-generation cannot restore removed obstructions or harvested resources.

**Ecotones.** Save widths and policy version before native activation. Use one absolute-coordinate climate sample for materials, snow/soil and vegetation density. Discrete blocks require coherent stable spatial selection, not per-call randomness. Blend landform parameters *before hydrology*; preserve intentional cliffs, layers, canyon walls, incision and outlets. Hydrology selects ocean/river/land domains; biome smoothing never averages liquid levels or blurs finished chunks. Native old/new borders are a distinct unresolved gate.

**Sources:** [T: TERRAIN-03 and TERRAIN-05–10][T], [V6: Generation, Configuration, Water and instability][V6], [V7: Changes and Channel revisions][V7], [L: Terrain work order and Earth-scaled rivers][L], [C: Integration sequence][C]. Numerical candidate thresholds and form-specific bounds remain in their source sections rather than duplicated tunables.

## 4. Finite water, climate and chemistry

One native unit is 1/16 block; one environmental quantum is 1/256 unit, so a full block is 4,096 quanta. The alpha 26 records use a groundwater and snow pair per visited 64×64 region, each capped at 1,024 blocks and initialized once. Groundwater initially holds a rainfall-seeded 30–70%; new dry-land outlets need at least 75% for 30 environmental steps, exhausted outlets above 25% for 30 steps, and active supply stops below 12.5%. Define the step/second relation and initialization rounding from the applicable implementation; a document's undefined unit is not a new design choice.

Open rivers use local gravity/routing; bounded communicating-vessel pressure applies to appropriate connected pools and covered wells/pipes. Three substeps per 20 Hz host tick are nominal admission, not a frame-rate guarantee. Infiltration depends on actual substrate and free capacity; clay/constructed floors seal it. A visit does not both infiltrate and discharge the same water. Planned natural outlets obey head, drought reserve and fractional channel-stage capacity. Backwater/full/solid receivers stop delivery; source safety preserves actual floods, dam storage and overtopping.

The later natural-datum/scheduling records improve high-creek supply without adding new reservoirs. Rain/snow estimate at most sixteen already-loaded chunk centers per regional interval, weighted by each live permeability and precipitation phase; roofs/ocean boundaries are excluded. This is representative sampling, not exact integration of 4,096 columns. Scarce atmospheric supply is shared proportionately to capacity-capped demand, with deterministic integer remainder, preflight and no backlog. Keep the documented component optimization separate from real storm tick latency.

Rain moves atmosphere to groundwater; snow enters finite snow storage; melt moves counted snow/ice to groundwater or physical cells as the selected path specifies. Evaporation/evapotranspiration return pure water and retain nonvolatile constituents. Saline dry residues need an owner until rewetting or accounted removal. The documented ocean and atmosphere initial reserves (134,217,728 and 67,108,864 blocks) are finite gameplay abstractions, not planetary physics.

Only marked sea surface cells at Y=95, stage 96, use ocean exchange. Outgoing replenishment spends the ocean account; incoming discharge leaves its sender and is credited once. Capacity/exhaustion fails closed. Historical V1–V3 adoption requires an already full compatible sea surface; it neither refills edits nor reclassifies the whole legacy ocean.

Fresh/brackish/salt/mineral/tannin/contamination are compatible composition traits, not twenty-three separate mutually exclusive fluids. Bulk flow advects integer salt/sediment with the accepted volume and donor remainders. Composition-only diffusion changes constituents, not levels. It remains distinct from registered fluid IDs, appearance and chemical reactions. Miscible liquids mix conservatively; immiscible phases retain interfaces/displacement; reactive pairs require accounted products and declared volume/phase change. Temperature is a separate energy/thermal field, not a constituent.

Lakes/dams track actual storage, stage, sill, spill capacity and downstream head. Continued net inflow behind a blockage raises stage or overtops; safety can pause new delivery but cannot erase stored water. Desert seasonal basins must fill and recede through actual infiltration, evaporation, drainage/extraction; ephemeral, supplied perennial and groundwater-refuge cases differ by budget. Frozen and dry are different states. Sub-unit moisture/soil films need storage if physically modeled; cosmetic wetness cannot hide a full cell. Biome identity stays stable through ordinary wet/dry cycles.

Wells share depletable aquifers; extraction priority and local drawdown remain design choices. Canopy interception/puddles, snowdrift/compaction/refreeze, hemisphere-aware climate, circulation/estuary/rip flow, shelf-to-trench bathymetry, physical tides/surge and deep-water gameplay remain separate work packages. Literal 11 km oceans require demonstrated host-height/storage/lighting/network feasibility. Visual waves alone implement no physical inundation.

**Sources:** [E: Water ownership; Ocean, lakes and chemistry][E], [B: all behavioral sections][B], [W: §§3, 5–10, 12–14][W], [L: dev.6–17][L]. Headwater source representation and the owner's drainage-stop rule are clarified as proposed contract **TW-01** in [the focused additions](research/terrain-water-additions.md#tw-01--separate-source-cell-representation-drainage-termination-and-replenishment-authority), without turning every full cell into unlimited supply.

## 5. Persistence, interfaces and failure behavior

Authoritative level mutation is serialized on the level owner; workers compute against immutable revisioned snapshots. Client smoothing never predicts authoritative physics. Every generated initialization is once-only. Transfers include world/dimension, owner epochs, revisions, quantity/constituents, sequence and durable outcome; stale completions, repeat messages and previous-world jobs cannot publish.

The old Water Engine §11.2 suggestion to roll back a transaction merely because it is old is unsafe under delayed credit. The merged recovery requirement is: establish exactly one terminal outcome and fence late destination commits before returning escrow. Persist source/destination/escrow/replay obligations coherently; a single-file atomic save does not prove a multi-owner transaction durable. Corrupt/unknown-schema records preserve original bytes and become sealed/unavailable; “fail closed” never means resetting their stock to dry or guessing new water.

Checked arithmetic rejects overflow. Native/host conversion is exact or retains named remainder ownership: 16 units = 4,096 quanta versus a 1,000 mB host bucket does not divide evenly. Clamping, dropping fifth constituents, metadata eviction and rounding round trips cannot delete or mint quantities. Lowering configured capacity retains an over-capacity account that refuses new input and drains normally.

Containers, waterlogging, displacement/pistons/falling blocks/explosions, sponge/dripstone/cauldrons and optional pump/pipe/tank/valve ports all need defined capacities and a transactional path. Pipes/tanks own their contents. Creative commands and destructive replacements are declared externals. Other mods/scripts may read data or submit transactions, not overwrite stocks. Bucket transport retains chemistry but not natural-source authority; dimension transfer respects explicit dimension policy.

Current commands report saved generator and existing water records without initializing supply. Extend read-only inspection to ownership, units/composition, actual flow, stage/head, outlet state, refusal reason, revision and queue/account health. Gauges, field-guide explanations and post-commit events read the same state; observers cannot mutate inside a committed event.

Measured accepted flux, not cosmetic current, drives future water wheels/entity forces. Wheels consume available head/energy rather than water, cannot generate from still water and share head when placed in series. Quality/treatment, irrigation/salinity, fire/lava/quench, thermal coupling, pressure gear/stamina/mount swimming and flood warnings retain their approved/proposed scope; recipes route removed constituents/products to named destinations.

**Sources:** [W: §§11, 13, 16–20][W], [B: Water accounting and Loaded/unloaded ownership][B]. **TW-02–05** in [focused additions](research/terrain-water-additions.md) supply detailed face/halo, numerical, future physical-coarsening and deterministic-random witnesses. Physical coarse simulation remains separate from render LOD.

## 6. Actual-chunk LOD and continuous water presentation

The documented alpha 17+ viewer derives from explored/delivered authoritative client chunks. The legacy procedural horizon is historical, diagnostic and off by default; disabling actual-chunk LOD never enables it. No unseen 512-chunk full-resolution generation, Distant Horizons parity or superiority is established.

Capture immutable small section summaries on the owning thread; workers retain no mutable live chunk objects. Include solid intervals, materials/classes, water, coverage and light. World/dimension/schema/generation/edit/light/water revisions key memory and checksummed bounded-decompression disk artifacts; coalesce dirty work, cancel obsolete jobs and reject stale results. Atomic replacement and current-world identity protect caches.

Keep solids, cutouts and translucent water separate. Preserve roofs, cliffs, trunks, shorelines, holes, tint/fog, light, alpha/fade and snow/composition continuity. Start with greedy coplanar faces; optional simplification locks actual shared seams/material/hard features and compacts all attributes. A generic open-border flag is insufficient for closed skirt meshes. The standalone meshoptimizer probe remains a CPU development result, not shipping integration or a cluster hierarchy.

Select by projected geometric error, bounds/frustum and hysteresis. Keep covering parents until required children are ready; define explicit fallback when coverage is missing. Budget capture, workers/scratch, pending results, disk/compression, live and retired GPU memory, depth resources and uploads separately. Edits, relight, teleport, disconnect, reload, corrupt cache and memory saturation must progress without silent holes or starvation. Preserve external renderer handoff and equivalent OpenGL/Vulkan behavior.

Water pages need a common compatible corner trajectory at the presentation time. Thin supported layers retain the shared-corner floor constraint; dev.18–19 hold/release and resident publication do not change amounts or prevent real draining. Successful write/accepted-byte state is separate from intended CPU state, and retries retain the last valid covering result. Mixed transparency, waterfall/front topology, live page/LOD boundaries and all-frame pixel continuity need their own acceptance.

**Sources:** [L: Required plan, Chunk-derived LOD work order, Executed meshoptimizer probe, Acceptance][L], [V7: Water rendering][V7], [W: §14][W]. Edited-artifact dependency closure is specified by **TW-06** in [focused additions](research/terrain-water-additions.md#tw-06--define-edited-lod-dependency-closure-and-incomplete-coverage). Rendering/resource details also belong to the renderer chapter rather than a second terrain renderer specification.

## 7. Remaining work and acceptance

| Work package | Distinct requirements retained | Promotion evidence |
| --- | --- | --- |
| Quantity and interface completeness | Defined units/head/permeability/rain tables; displacement/waterlogging; exact adapters/remainders; full owner ledger; creative/dimension policy | Closed-box and million-round-trip exchanges; overflow/metadata saturation; same-tick multiplayer; configuration-overcapacity |
| Shared hydrology and recovery | Canonical region/corner outlets; lakes/sills/spills; topology edits; unloaded coarse stores/backpressure; fenced transactions and migration | Actual multi-region closed basin, dam split/merge, crash at every transition, delayed replay, golden save/reopen |
| Climate and environmental detail | Chosen pause/catch-up; desert recession/films; canopy, snow/drift/ice; calibrated evaporation/recharge | Repeated wet/dry/melt cycles, freshwater/saline residues, seasonal supply and many-region storm tails |
| Material/habitat terrain | Structural geology, grade/scarcity, soil properties, diverse mountain routes, wind, obstructions, scoped hazards | Native cross-sections, ridge-to-valley transects, blocked/returning rivers, no sinkholes, deterministic order/cache replay |
| Blending and staged geography | Saved ecotone policy; coherent material/vegetation consumption; landform joins before water; staged delta/oxbow versions | Full chunk/base-column/height parity, old/new border treatment, signed seams and multi-climate native captures |
| Chemistry and gameplay | Diffusion/appearance, optional constituents/thermal/reactions; measured flow, wells/ports, irrigation/quality, currents/gear and wheels | No duplicated water/constituents/energy; useful player feedback; individually qualified machine adapters |
| Presentation and scale | Actual edited coverage, water near/far material/depth agreement, waterfalls/transparency; bounded CPU/GPU/disk/network and fair scheduling | Native enabled-view travel/edit scenes, owner hardware, p50/p95/p99 readiness/tick/frame/lag and peaks, matched baseline |

Every test records source commit, saved profile, seed/coordinates, active-set/input history, backend/config/hardware and expected answer. Exact integer conserved quantity drift is zero. Numerical/visual errors and response deadlines are separate declared tolerances. Different loading histories do not imply identical physical evolution unless that equivalence is explicitly chosen.

Retain all original Water Engine gap IDs **GAP-01–37** and decisions **D-1–16**, plus factual questions **Q-1–10**, as source provenance; do not duplicate them as competing tracking IDs. Their current dispositions need the appropriate newer implementation receipt. In particular the optional-mode, well priority, waterlog capacity, ocean pumping cap, catch-up, harsh presets, foreign-water ingress, dimension accounting, extra constituents, thermal/quality/reaction scope, tide/surge and API stability choices remain explicitly dispositioned work. The chapter makes no owner decision on those proposals.

The nominal 64 columns/tick and 2 ms between-column admission are documented historical defaults, not a hard deadline. The older scanner's 1,280 columns/second coverage arithmetic explains revisit delay; later targeted-source and representative-precipitation paths must be measured independently. Track oldest queue age, memory by owner/stage, refused transfer reasons, save/network costs and eventual quiet-region progress. Degrade by deferring bounded work, never by skipping conservation, losing quantities or force-loading distant chunks.

## 8. Source map and maintenance

| Source | Sections consolidated here | Role |
| --- | --- | --- |
| [TERRAIN-MECHANICS.md][T] | Scope; TERRAIN-03 and TERRAIN-05–10; Integration; alpha 26/27 updates | Accepted mechanics, exclusions, broad acceptance |
| [TERRAIN-V6.md][V6] | Generation; Configuration and persistence; Water and instability; Acceptance scope | Alpha 27 mechanics and concrete implementation limits |
| [TERRAIN-V7.md][V7] | Changes; Channel revisions 3–5; Water rendering; Compatibility | Alpha 28 and staged channel identities |
| [TERRAIN-BLENDING.md][C] | Current checkpoint; Integration sequence | Explicit-call-only ecotone and native activation gates |
| [LOD-AND-DRAINAGE-ARCHITECTURE.md][L] | Charter; dated dev.6–20 updates; Terrain/LOD work orders; probe; acceptance | Cross-system ownership, implementation succession, coverage/performance |
| [WATER-BUDGET-REQUIREMENTS.md][B] | Operating/accounting model; ocean; seasonal; modes; unloaded; chemistry; alpha 26/24 clarification | Owner directions and historical policy changes |
| [ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md][E] | All behavioral sections and configuration | Alpha 26 checkpoint |
| [WATER-ENGINE.md][W] | §§0–25; gaps/decisions/questions | Library consolidation at alpha 26, with tagged proposals |
| [Earlier expanded terrain-water chapter][R] | Full chapter, especially saved revisions and Dev69–77 distinction | Historical research edition; preserves its source pin |
| [Focused terrain/water additions](research/terrain-water-additions.md) | TW-01–06 | New proposed contracts and primary-source research |

Update this chapter for behavior, the central task tracker for work state, and the relevant implementation receipt for evidence. Keep originals and historical grades unchanged. New source-only progress must identify the actual caller and tested dependencies; it cannot silently become native acceptance.

[T]: ../../docs/TERRAIN-MECHANICS.md
[V6]: ../../docs/TERRAIN-V6.md
[V7]: ../../docs/TERRAIN-V7.md
[C]: ../../docs/TERRAIN-BLENDING.md
[L]: ../../docs/LOD-AND-DRAINAGE-ARCHITECTURE.md
[B]: ../../docs/WATER-BUDGET-REQUIREMENTS.md
[E]: ../../docs/ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md
[W]: sources/WATER-ENGINE.md
[R]: https://github.com/Shnewbs/Deep-Primal/blob/8f28f833caa259a0e59adac494a3aee8d750bc63/docs/research-expanded/2026-10-29/chapters/terrain-water.md

