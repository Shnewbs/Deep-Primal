# Terrain, Water, and Geology — Primary-Source Research Notes

**Reviewed:** 2026-10-29  
**Purpose:** Research input for expanded Deep Primal Markdown replicas.  
**Status:** Intermediate notes. The recommendations below are proposed engineering contracts, not verified features of Deep Primal and not an instruction to change frozen saved worlds.

This lane inspected twelve primary sources across Houdini, Gaea, World Machine, Unreal Engine, Godot Voxel Tools, GeoClaw, and the original Priority-Flood paper. Evidence summaries describe the specific pages reviewed. Design inferences are separate. No vendor timing, visual demonstration, advertised scale, or scientific benchmark is treated as a Deep Primal performance result.

The documents served during review included Houdini 22.0 and Unreal Engine 5.8 labels. This records the pages observed, not an instruction to change the project's toolchain. World Machine's inspected addendum is explicitly historical. The two papers are historical methods references; their current software implementations were not audited.

## 1. Source notes

### TW-01 — Houdini Heightfields and terrains

**Primary source:** [Houdini Heightfields and terrains](https://www.sidefx.com/docs/houdini/heightfields/)  
**Type and inspected scope:** Official product documentation. Overview, limitations, scale/resolution, named layers and masks; page served as Houdini 22.0 documentation.  

**Observed evidence:** Houdini represents terrain with two-dimensional height grids and separately named layers and masks. Masks select where operations apply; height layers can distinguish a base from added soil. The documentation explicitly describes limits on vertical surfaces and offers conversion to geometry or other detailing workflows. Grid spacing and overall terrain size are separate controls, so changing sample resolution is not the same operation as changing world scale.

**Design inference:** ADAPT: Use heightfields and masks as versioned authoring inputs for macro shape, soil, and surface appearance. Import them into an explicit three-dimensional terrain/material model before they can define caves, overhangs, collision, excavated surfaces, or fluid capacity.

**Limits:** A two-dimensional surface document does not establish volumetric topology, conserved material quantities, or a runtime voxel editing system.

### TW-02 — Houdini HeightField Erode 3.0

**Primary source:** [Houdini HeightField Erode 3.0](https://www.sidefx.com/docs/houdini/nodes/sop/heightfield_erode)  
**Type and inspected scope:** Official node reference. Solver, masks, feature scale, layer bindings, freeze/cache behavior; node is documented as introduced in Houdini 21.0.  

**Observed evidence:** Erode 3.0 exposes hydraulic and thermal controls, spatial masks, a random seed, and frozen simulation output. Its feature size uses meters and is clamped to at least three input sample spacings. Separate outputs include terrain height, deposited sediment, thermal debris, transported material flow, and direction. Sediment removal is an explicit control; similar appearance after resampling is qualified when feature-size clamping changes.

**Design inference:** ADAPT: Bake macro erosion with a manifest of tool/node version, graph, seed, units, resolution, iterations, masks, and output semantics. Treat erosion layers as authored evidence or initialization proposals. A runtime sediment transaction must use the game's material ledger, including any authorized sink.

**Limits:** Visual similarity is not bitwise determinism or mass conservation. The inspected reference is Erode 3.0, not its older namesake.

### TW-03 — Gaea Tiled Builds

**Primary source:** [Gaea Tiled Builds](https://docs.gaea.app/using/using-gaea/build-and-export/tiled-builds.html)  
**Type and inspected scope:** Official product documentation. Tiled/full differences, world/detail separation, hybrid buckets, blending, and data-map import.  

**Observed evidence:** Gaea distinguishes globally authored structure from local detail and explains that operations lacking neighboring context can disagree across tiles. It recommends baking unsuitable upstream nodes, avoiding independent tile normalization, and using larger build buckets with overlap where needed. Export tile dimensions need not equal evaluation bucket dimensions. Masks and packed control maps should retain linear data values, and adjacent tiles should be checked before a large export.

**Design inference:** ADOPT the separation between canonical macro products and bounded detail jobs. Each import declares coordinates, sample registration, world scale, overlap/crop ownership, encoding, and channel meaning. Validate boundaries and interior landmarks against a fixed macro reference; do not use blending to disguise contradictory basin ownership.

**Limits:** An authoring overlap recommendation is not proof of deterministic independent chunk generation or a conserved cross-tile water simulation.

### TW-04 — Gaea Stratify

**Primary source:** [Gaea Stratify](https://docs.gaea.app/reference/nodes/surface/stratify.html)  
**Type and inspected scope:** Official node reference. Description and controls for spacing, octaves, strength, shape, seed, tilt, and orientation.  

**Observed evidence:** Stratify creates irregular visible rock bands and locally broken layers rather than simple uniform terraces. The reference exposes spacing, variation, intensity, profile shape, seed, tilt, and direction controls. These parameters describe the appearance and repeatability of a procedural surface formation. The page presents the node as a way to give terrain uneven sedimentary or layered character, not as a subsurface rock-property database.

**Design inference:** ADAPT only as an appearance or authoring reference. Define geological unit IDs, ordered contacts, thickness bounds, faults, mineral associations, and material properties independently in three dimensions. Surface banding may visualize those units, but a cosmetic node must not silently decide mining yield or aquifer connectivity.

**Limits:** No evidence here establishes physical stratigraphic age, groundwater behavior, ore distributions, or consistent lithology through an excavated volume.

### TW-05 — World Machine Professional Edition Addendum — tiling

**Primary source:** [World Machine Professional Edition Addendum — tiling](https://help.world-machine.com/topic/world-machine-professional-edition-addendum/)  
**Type and inspected scope:** Official legacy workflow documentation. Tiled output, shared edge vertices, subsets, coordinate naming, orientation, and context-sensitive simulation caveats.  

**Observed evidence:** World Machine separates tile dimensions, output extent, naming, axis orientation, and shared edge values. It permits a selected subset of tiles to be rebuilt. Its documentation warns that erosion and snow can differ between whole-domain and tiled evaluation because surrounding information is missing. The described workaround computes extra border area and blends during assembly, while shared edge vertices separately enforce geometric agreement at adjacent mesh boundaries.

**Design inference:** ADAPT explicit export manifests and subset dependency tracking. Every changed macro input determines which outputs and overlap regions need rebuilding. Check shared sample ownership and watertight geometry separately from drainage and material consistency; an equal border height alone does not prove the simulation matches.

**Limits:** This is an old addendum with obsolete platform references. No current product limits, speed, thread counts, or recommended tile sizes are inferred.

### TW-06 — Unreal Engine — Using PCG Generation Modes

**Primary source:** [Unreal Engine — Using PCG Generation Modes](https://dev.epicgames.com/documentation/en-us/unreal-engine/using-pcg-generation-modes-in-unreal-engine)  
**Type and inspected scope:** Official engine documentation. Partitioned/hierarchical generation, coarse-to-fine data, duplicate point handling, runtime sources, cleanup radius, and scheduler controls.  

**Observed evidence:** Unreal PCG partitions generation and permits different graph stages to execute at different spatial scales. Coarse results can feed finer grids; passing them downward can duplicate points unless ownership is restricted. Runtime generation uses nearby sources, a distinct cleanup radius, scheduling priorities, and concurrency or frame-work controls. The documented framework therefore separates generation granularity, retained content, and the policy choosing which work executes next.

**Design inference:** ADAPT hierarchical authored products and stable feature ownership. Give each tree, deposit, structure, and river segment a canonical ID and owner; rasterize intersections into chunks idempotently. Camera priority may schedule visual decoration, but must not select authoritative terrain, resource availability, or water updates.

**Limits:** PCG mesh generation does not establish a voxel authority model, save transaction semantics, or cross-platform deterministic world generation.

### TW-07 — Unreal Engine World Partition

**Primary source:** [Unreal Engine World Partition](https://dev.epicgames.com/documentation/unreal-engine/world-partition-in-unreal-engine)  
**Type and inspected scope:** Official engine documentation. Spatial cell lifecycle, actor dependencies, streaming sources, loaded/activated states, teleport preparation, and debug/parallel-load controls.  

**Observed evidence:** World Partition stores a world as streamable cells controlled by streaming sources and grid policy. A source can request loaded or activated cells, and a documented completion query supports preparing a teleport destination before movement. Referencing actors can create jointly loaded groups. Editor builders can process large-world derived data without loading everything simultaneously. These mechanisms distinguish residency, activation, and preparation work rather than treating visibility as complete readiness.

**Design inference:** ADAPT bounded tickets and explicit readiness by subsystem: geometry, collision, water authority, navigation, and lighting. A teleport waits for its required revision set. Unloaded cells retain persistent ownership and summaries; they do not become empty terrain or an open water outlet.

**Limits:** The inspected page concerns Unreal actor/cell management, not transactional mutable voxel terrain. Its default spatial policy is not adopted automatically.

### TW-08 — Godot Voxel Tools — Generators

**Primary source:** [Godot Voxel Tools — Generators](https://voxel-tools.readthedocs.io/en/latest/generators/)  
**Type and inspected scope:** Official project documentation. Generator API, density fields, multipass dependencies/determinism/limits, modifiers, and output caching.  

**Observed evidence:** Voxel Tools can generate block data from spatial inputs and supports parallel calls. Its multipass documentation warns that overlapping structure writes can depend on chunk arrival order, and that completing a column requires the neighboring passes capable of affecting it. Large features can instead use precomputed or deterministic blueprints. Generation may repeat; destructive edits override procedural modifiers. The graph's density values need not be exact signed distances.

**Design inference:** ADOPT pure versioned generation inputs, canonical feature IDs, deterministic conflict resolution, bounded declared halos, and persistent edit precedence. Keep density sampling distinct from certified distance queries. Replay reversed chunk arrivals and worker schedules against identical committed world inputs.

**Limits:** Some page sections are historical; implementation capability needs a pinned revision. The documented multipass column method does not directly provide arbitrary-LOD generation.

### TW-09 — Godot Voxel Tools — Streams

**Primary source:** [Godot Voxel Tools — Streams](https://voxel-tools.readthedocs.io/en/latest/streams/)  
**Type and inspected scope:** Official project documentation. Chunk saves, modified/generated output policy, asynchronous completion, world switching, resource lifetime, and forced shutdown.  

**Observed evidence:** Voxel streams load and save chunks, normally persisting modified blocks while allowing generated output to be saved when regeneration is expensive. Saving is asynchronous and can continue after the terrain node is destroyed. The documentation warns that changing a stream's destination while old tasks remain can send previous-world writes to a different save, and recommends a separate stream instance for the new world.

**Design inference:** ADOPT immutable world/save identity per job, monotonic world epochs, explicit save completion, crash recovery markers, and retirement of old sessions. Acknowledging a player edit or cross-region water transfer must follow the selected durability policy; a queued write is not a completed durable commit.

**Limits:** Asynchronous chunk persistence does not itself guarantee atomic multi-chunk terrain edits, exactly-once transfers, or crash-safe composition accounting.

### TW-10 — GeoClaw description and contents

**Primary source:** [GeoClaw description and contents](https://www.clawpack.org/geoclaw.html)  
**Type and inspected scope:** Official scientific software documentation. Model scope, topography, dry cells, wetting/drying, moving terrain and parameter/reference navigation.  

**Observed evidence:** GeoClaw focuses on two-dimensional, depth-averaged flows over varying terrain elevation. The documentation treats the advancing or receding boundary between wet and dry cells as a central numerical issue: depth can reach zero, and neighboring cells can transition between dry and wet states. Its scope includes dedicated topography, moving-topography, and initialization inputs rather than obtaining water state from a rendered surface.

**Design inference:** ADAPT wet/dry and terrain-change fixtures to the finite-water engine. Keep water volume, surface reconstruction, terrain capacity, and source policy separate. A shallow-water provider can be an optional qualified model for open surface regions; cave networks and waterfalls require declared exchange or a different model.

**Limits:** Depth averaging cannot establish general three-dimensional cave flow, pressurized plumbing, air entrainment, or composition transport.

### TW-11 — Berger, George, LeVeque and Mandli — GeoClaw depth-averaged flows with adaptive refinement

**Primary source:** [Berger, George, LeVeque and Mandli — GeoClaw depth-averaged flows with adaptive refinement](https://arxiv.org/html/1008.0455v2)  
**Type and inspected scope:** Original research paper. Sections 2–5: conservation variables, equilibrium, wet/dry interfaces, refinement/time stepping, restriction and topography integration.  

**Observed evidence:** The paper describes finite-volume updates, balanced resting-water states, dry-state handling, and adaptive grids with finer time steps and explicit boundary values. Refinement must use consistent terrain averages and coarsening must account for covered volumes. Crucially, the authors describe a shoreline exception: refining a dry coarse cell may introduce water to preserve the prescribed sea level. The paper therefore does not promise unconditional closed-system mass conservation during every regrid.

**Design inference:** ADOPT the equilibrium and refinement test discipline, not the coastal water-creation convention. Deep Primal must preserve owned water and species across activation/refinement, or record an authorized reservoir exchange explicitly. Measure storage plus boundary/source/sink accounting at each accepted update and fail unaccounted residuals.

**Limits:** This is a 2011 model/method paper, not a claim about today's code. Its scientific approximation and reservoir assumptions differ from finite game accounts.

### TW-12 — Barnes, Lehman and Mulla — Priority-Flood

**Primary source:** [Barnes, Lehman and Mulla — Priority-Flood](https://arxiv.org/html/1511.04463v1)  
**Type and inspected scope:** Original research paper. Algorithms 1–2 and sections 3, 4 and 7: depression treatment, queue ordering, NoData, flow directions and watershed labels.  

**Observed evidence:** Priority-Flood processes an elevation domain inward from its outlets with ordered queues. Variants fill depressions, derive drainage directions, or assign watershed labels; those operations have different effects on the terrain data. The paper gives a defined treatment for cells outside the elevation domain and discusses ordering for equal priorities. Its watershed variant can label drainage structure without performing the same elevation modification as the filling variant.

**Design inference:** ADAPT a canonical macro drainage graph with explicit outlets, lakes, closed basins, and total-order tie breaking. Preserve the original height data when only labels are required. Never map an unloaded runtime chunk to the paper's outside-domain outlet or run depression filling over saved player terrain as an implicit repair.

**Limits:** This is a complete-domain elevation algorithm, not a three-dimensional finite-water solver or a guarantee that arbitrary chunk-local drainage is globally consistent.

## 2. Proposed contracts ready to adapt into project replicas

These are our engineering proposals. References identify motivating evidence; they do not imply that a vendor implements this exact Deep Primal design.

### 2.1 Separate immutable authoring products, generation, edits, and rendering

A world profile identifies the exact generator/schema version, seed, coordinate domain, numerical rules, material definitions, and authored-product fingerprints. Preserve old profile implementations or their committed output when a save depends on them. A new authoring bake must not alter V1–V7 geography, previously committed V8 chunks, or player edits through an ordinary graphics or resource reload.

The evaluation chain should have explicit ownership:

1. Versioned macro products establish large landforms, basin identities, geological domains, and major feature blueprints.
2. Deterministic local evaluation samples those products with declared dependency extents.
3. Persistent committed edits override the appropriate generated cells or feature instances.
4. Collision, lighting, water connectivity, meshes, and LOD are revisioned derived consumers.

Generation can be called repeatedly. Its result must not depend on a mutable global random-number cursor, worker count, arrival direction, camera visibility, or whether a neighbor happened to be generated first. Hash or counter-based random inputs should include the world/profile, feature class, canonical feature ID, and local sample where required. A seed alone is insufficient without a specified algorithm and numerical contract. [TW-03][TW-03] [TW-08][TW-08]

### 2.2 Authoring import schema

Every imported height, density, vector, mask, or material product declares:

| Field | Required meaning |
| --- | --- |
| Identity | Tool/node/graph version, graph and source hashes, seed, authored revision |
| Coordinates | Axis orientation, handedness, world origin, units, extents, negative-coordinate rules |
| Sampling | Cell versus vertex registration, spacing, dimensions, interpolation, border ownership |
| Encoding | Numeric format, range, scaling/offset, signedness, non-color versus color interpretation |
| Coverage | Valid extent, missing-data representation, required overlap and crop behavior |
| Semantics | Height, deposition amount, artistic flow mask, velocity, material likelihood, or another explicit type |
| Dependencies | Upstream macro products, masks, neighboring outputs, and rebuild footprint |
| Validation | Finite values, allowed ranges, seam comparisons, interior reference landmarks, and content hash |

An artistic erosion-flow map is not a measured water discharge. A sediment-colored mask is not automatically kilograms of suspended sediment. A terrain height is not a water surface or a density distance bound. Any conversion into gameplay state names its units, normalization, calibration, uncertainty or approximation, and version. [TW-01][TW-01] [TW-02][TW-02] [TW-05][TW-05]

### 2.3 Geology should survive excavation

Use a three-dimensional geological description or a versioned approximation that can answer material queries below the original surface. Proposed inputs include domain IDs, ordered contacts, layer-thickness bounds, orientation/fault transforms, and explicit associations for mechanical, weathering, and resource properties. Density/occupancy answers whether material exists; geological classification answers what it is. Neither alone establishes the other.

Surface weathering, soil cover, exposed rock appearance, moisture, and lighting are derived or independently authored states. Digging through a visually banded cliff must not reveal arbitrary surface-texture colors interpreted as mineral inventory. An erosion pass may propose a changed exposed unit or deposited cover, but runtime rock removal and deposition must produce explicit terrain/material transactions. This contract does not require plate tectonics or a full geological simulator. [TW-04][TW-04]

### 2.4 Give large features a canonical identity

Trees, ruins, caves, ore bodies, faults, river reaches, and lake basins can span chunk boundaries. Define their origin/owner, stable ID, influence extent, generation profile, and deterministic conflict rule before rasterizing them into local storage. A chunk stores or reconstructs the intersection, rather than creating a competing whole feature every time it loads.

A pass can become final only after all authoritative influences capable of changing its result are known. Bound the halo and cache footprint explicitly. If a feature is larger than that halo, obtain a canonical blueprint or summary; do not recursively generate an unbounded neighborhood. For overlapping structures, test a total-order resolution or a commutative merge rule. [TW-06][TW-06] [TW-08][TW-08]

### 2.5 Drainage analysis has an explicit domain

A drainage product identifies its terrain revision, outlets, closed basins, lake spill levels, tie-breaking rule, and boundary conditions. Surface drainage metadata and three-dimensional cave/aquifer connectivity are separate queries.

Do not classify an ungenerated or unloaded region as an outlet. A finite authoring domain may have declared open boundaries; a missing runtime neighbor remains unknown. Global basin changes cannot be repaired by independently normalizing each tile. A player dam, tunnel, or excavation invalidates the affected connectivity and spill relations through a bounded resumable process; an incomplete analysis returns pending or a documented conservative behavior.

Depression filling is a terrain-changing authoring operation. Watershed labeling or drainage analysis must not implicitly fill lakes or undo excavations in committed saves. [TW-12][TW-12]

### 2.6 Finite water and composition use conserved accounts

Retain the existing authoritative water rules where they are already defined. A proposed generic account is water volume or fixed-point water units plus separate conserved quantities for each supported constituent. Store salt or sediment amount independently of concentration; concentration is a derived ratio with a defined zero-water rule.

For every accepted transfer, the donor debit, receiver credit, source/sink entry, and constituent transfer use one declared transaction identity. Sum simultaneous outgoing proposals before admission so several neighboring receivers cannot each spend the donor's full pre-step amount. Apply capacity, availability, and composition limits to the actual admitted transfer. Specify deterministic remainder handling when dividing integer quantities.

For a closed test system:

```text
residual(q) =
    stored_after(q)
  - stored_before(q)
  - authorized_inputs(q)
  + authorized_outputs(q)
```

Here `q` is water or a conserved constituent, and storage includes loaded cells, unloaded persistent owners, reservoirs, in-flight escrow, and any explicitly modeled deposited phase exactly once. Integer accounts require zero unexplained residual. A floating representation needs a predeclared absolute/relative error bound and cumulative-drift gate. Sediment settling changes phase/owner; it does not erase matter. Evaporation follows the selected constituent policy and must not automatically remove nonvolatile salt. These are proposed game-account rules, not claims supplied by the authoring tools.

### 2.7 Terrain edits reconcile fluid capacity

Mining changes void space and connectivity; construction can reduce capacity and displace existing water. The transaction must specify how terrain, collision, water capacity, retained composition, and invalidated derived state become coherent.

If displaced water cannot immediately be placed, retain it in a bounded explicit owner or reject/defer the edit according to the established gameplay contract. Never discard it by truncating a cell to its new capacity. Rollback restores accounts and topology together and rejects candidates computed from the abandoned revision. An erosion/deposition feature later promoted to runtime uses this same transaction path.

### 2.8 Simulation resolution must not borrow rendering's authority

Render LOD changes presentation. It must not mint or delete finite water, reset a chemical mixture, or trigger independent competing solvers for the same physical volume.

If physical coarse/fine simulation is later adopted, specify one owner per covered volume, conservative restriction/prolongation, fine/coarse boundary exchange, temporal synchronization, and a rule for partially wet coarse cells. Summing a coarse cell and its active fine children double-counts storage. Reconstructed free-surface appearance must not introduce volume silently. A large reservoir may exchange water only through an explicit account and boundary condition. The equilibrium and mass-account gates remain separate: passing one does not excuse failure of the other. [TW-10][TW-10] [TW-11][TW-11]

### 2.9 Streaming readiness and persistence are named states

Track generated, loaded, committed, collision-ready, water-ready, lighting-ready, visual-ready, saving, and retired states as appropriate; a mesh being visible proves none of the other states. Tickets carry purpose, priority, bounds, expiry/retirement, and budgets. A teleport or simulation activation consumes the readiness set it actually needs.

Jobs capture immutable world/save identity, world epoch, region residency, transaction/content revisions, and dependent profile fingerprints. A world switch creates a new session identity; old writes remain bound to the old destination. Save completion is observable. A durable checkpoint records enough transaction state to recover without losing or duplicating a terrain edit, cross-region transfer, or species amount. [TW-07][TW-07] [TW-09][TW-09]

### 2.10 Budgets need liveness, not only caps

Bound snapshot bytes, outstanding generation and save work, retained dependency halos, dirty metadata, physical active regions, and pending cross-border exchanges. Coalesce requests without dropping required invalidation. Reject or defer admissions before allocating beyond a cap.

Measure oldest pending age, completed useful revisions, retry count, discarded obsolete work, outstanding durable transfers, and drain time after overload. Optional decoration can be reduced first. Under the declared admitted workload, water and terrain work must continue completing; perpetual cancellation is not successful backpressure. Do not promise bounded latency for unlimited edits or assume that a published vendor frame budget fits this project.

## 3. Suggested acceptance fixtures

| Fixture | Required evidence |
| --- | --- |
| Frozen-profile reopen | Existing authoritative terrain/material snapshots and player edits retain their hashes; proposed new profiles are explicit |
| Generator schedule permutation | Equal final inputs produce identical authoritative cells/features across worker counts, chunk arrival orders, and repeated generation |
| Shared-edge and macro-domain import | Adjacent sample ownership agrees; no mirrored tile, per-tile normalization shift, or mismatched basin/stratum identity |
| Large feature across eight chunks | One canonical feature; unload/reload and reversed arrivals produce no duplicate tree, deposit, cave, or river segment |
| Unknown drainage frontier | Missing neighbor stays unknown; no false outlet, lake drain, or unbounded load chain |
| Excavated layered cliff | Newly exposed material follows the geological model and committed edits, independently of texture/LOD settings |
| Resting water over uneven terrain | No spurious net current under the declared equilibrium model; conservation residual stays within its predeclared bound |
| Wet/dry front and dam breach | Nonnegative water, bounded transfers, correct wetting/drying, measured queue progress, explicit open/closed boundary policy |
| Unequal-mixture transfer | Water and every constituent balance; dilution, partial transfer, multiple receivers, and zero-water states are correct |
| Fill an occupied water cell | Construction redistributes, escrows, or rejects according to contract; no hidden water/species deletion |
| Refinement or activation at shoreline | Storage is conserved across representation changes; any reservoir exchange is named and accounted |
| Cross-border transfer plus crash | Recovery applies a transfer once, never zero or twice, including source/receiver saved at different moments |
| World switch during save | Delayed old tasks cannot write into the new world or revive old region resources |
| Continuous edits plus travel | Bounded memory and sustained useful progress; explicit overload and recovery behavior |

These fixtures should augment the source documents' existing gates, not replace them or imply that all proposed optional simulation models must ship together.

## 4. Adoption boundaries and unresolved evidence

- **Recommended now:** explicit authoring schemas; immutable profile/seed semantics; deterministic feature ownership; preserved edit precedence; typed unknown/readiness; exact finite-water and constituent accounting; crash-aware persistence; seam, schedule, and wet/dry fixtures.
- **Prototype only after current invariants pass:** macro drainage extraction, optional runtime erosion/deposition, regional shallow-water transport, and physical adaptive resolution.
- **Do not transplant:** vendor performance claims, an editor's camera-driven cleanup into world ownership, a heightfield into universal 3D terrain truth, artistic masks into physical quantities, or shoreline water creation into a finite closed account.
- **Evidence still required:** the actual current Git-main contracts, existing water units and save schema, numerical determinism target, native host edit lifecycle, measured active-region budgets, concrete authored asset formats, and licensing review only if implementation code or redistribution is later selected.

## 5. Portable source links

[TW-01]: https://www.sidefx.com/docs/houdini/heightfields/
[TW-02]: https://www.sidefx.com/docs/houdini/nodes/sop/heightfield_erode
[TW-03]: https://docs.gaea.app/using/using-gaea/build-and-export/tiled-builds.html
[TW-04]: https://docs.gaea.app/reference/nodes/surface/stratify.html
[TW-05]: https://help.world-machine.com/topic/world-machine-professional-edition-addendum/
[TW-06]: https://dev.epicgames.com/documentation/en-us/unreal-engine/using-pcg-generation-modes-in-unreal-engine
[TW-07]: https://dev.epicgames.com/documentation/unreal-engine/world-partition-in-unreal-engine
[TW-08]: https://voxel-tools.readthedocs.io/en/latest/generators/
[TW-09]: https://voxel-tools.readthedocs.io/en/latest/streams/
[TW-10]: https://www.clawpack.org/geoclaw.html
[TW-11]: https://arxiv.org/html/1008.0455v2
[TW-12]: https://arxiv.org/html/1511.04463v1

