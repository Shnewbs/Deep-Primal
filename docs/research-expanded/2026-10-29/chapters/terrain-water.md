# Terrain, Hydrology, and Geology — Implementation Contracts

**Review date:** 2026-10-29  
**Status:** Proposed engineering expansion of the preserved Deep Primal documents.  
**Scope:** Editable streamed terrain, drainage, water accounts, material identity, environmental placement, and their persistence and scheduling boundaries.

This chapter connects the existing requirements into contracts that can be implemented and tested. It does not change owner decisions, authorize migration of saved geography, or claim a new build passed. Historical implementation statements belong to their dated source checkpoints. Research observations and limits are in the [primary-source notebook](../research/terrain-water-research.md); document precedence and stale status belong to the [discrepancy register](../DISCREPANCIES.md).

The main design inputs are the [Water Engine](../replicas/project/WATER-ENGINE.md), [water budgets](../replicas/git/docs/WATER-BUDGET-REQUIREMENTS.md), [terrain mechanics](../replicas/git/docs/TERRAIN-MECHANICS.md), [V4 environmental water](../replicas/git/docs/ENVIRONMENTAL-WATER-AND-TERRAIN-V4.md), [V6](../replicas/git/docs/TERRAIN-V6.md), [V7](../replicas/git/docs/TERRAIN-V7.md), and [biome plan](../replicas/git/docs/BIOME-PLAN.md). Their established constraints include frozen saved geography, finite ordinary lakes, no transferable renewable spring identity, no sinkholes, and optional/default-off live erosion. Consumed recipes remain the panning/sluice baseline.

## 1. Authority, status, and revision boundaries

Keep four independent concepts visible:

| Concept | Authority | Required behavior |
| --- | --- | --- |
| Generated baseline | Saved seed, generator profile, authored inputs | Reproducible immutable geography for its saved revision |
| Persistent edits and depletion | Server world state | Override baseline without being erased by regeneration |
| Runtime environmental state | Named water/material/thermal owners | Advances only through admitted, accounted operations |
| Presentation | Client snapshots and render caches | Cannot alter geometry, quantity, resource stock, or physics |

A source document reporting a fixture proves that dated fixture. It does not certify current main, every graphics backend, a whole season, or a larger active area. Conversely, a planning banner cannot erase a later local checkpoint. The [geology roster](../replicas/git/docs/NATURAL-TYPES-GEOLOGY.md) reports twelve soil textures and three forms at a 2026-10-25 checkpoint, while full horizon mechanics, organic states, farming, and ecological calibration remain open. V6 already describes generated topsoil/subsoil/parent-material profiles; that narrower geometry is distinct from the pending soil mechanics.

**Proposed compatibility contract:** decode existing saves from their serialized profile. Missing legacy fields use documented historical defaults; creation defaults apply only to new worlds. Unknown future revisions return unsupported and preserve raw data. They never fall back to the newest generator. Preserve V1–V7 geography and each existing V8 snapshot; future opt-in work gets its own explicit identity.

Terrain shape, material selection, channel shaping, hydrology schema, and configuration are separately versioned. A material revision may preserve occupancy while changing soil representation, but must prove that narrower promise against golden columns. Changing a channel profile cannot silently reinterpret old branch weights or source limits.

## 2. Shared data contracts

The following types are proposals, independent of Java/NeoForge storage details. The host adapter must validate its supported coordinate range rather than advertise an unverified world height.

```text
WorldKey {
  worldUuid, dimensionKey, savedSeed,
  generatorKey, generatorRevision, terrainProfileHash,
  materialRevision, channelRevision, hydrologySchema, configRevision
}

RegionKey { worldKey, signedRegionX, signedRegionZ }
SnapshotStamp { worldEpoch, geometryRevision, accountRevision, simulationStep }

TerrainSample {
  occupancy, materialKey, featureId?, provenance,
  geologyProfile?, soilProfile?, drainageEdge?,
  sampleStatus, snapshotStamp
}

WaterOwner {
  ownerKey, ownerKind, capacityQuanta, waterQuanta,
  constituents, optionalThermalState, revision
}

QueryResult<T> {
  status: current | pending | unavailable | unsupported,
  value?, dependencyStamp, reason?
}
```

Use floor division and floor modulus for signed region coordinates. At a 64-column region width, X=-1 belongs to region -1 with local X=63; X=-64 is region -1/local 0; X=-65 is region -2/local 63. Pack coordinates with checked arithmetic. Do not convert large global integer positions directly to shader floats; render snapshots carry an integer anchor and small local offsets.

A successful query states what revision it describes. Missing geometry is not air, missing composition is not necessarily freshwater, and an unavailable aquifer is not an empty aquifer. An interaction requiring current capacity or source eligibility must defer or fail explicitly when those dependencies are pending. A visual fallback may show an older snapshot with bounded age; it cannot authorize a bucket transfer or edit.

Provenance distinguishes generated, player-placed, harvested, destroyed, and administrative changes. Stable feature identity survives cache eviction, unload, and redecoration. An edit journal is authoritative even when a new generated baseline would place vegetation or ore in the same location.

## 3. Authoring inputs and deterministic generation

Heightfields are useful macro-terrain inputs but cannot alone express arbitrary volumetric topology; [Houdini’s heightfield documentation](https://www.sidefx.com/docs/houdini/heightfields/) makes the representation boundary explicit. Deep Primal should import authored surfaces and masks into its own three-dimensional geological and occupancy model.

**Proposed import manifest:** tool/version, graph hash, seed, source license/provenance, world-space origin, axis convention, horizontal/vertical units, sample spacing, tile extent, overlap, nodata meaning, channel definitions, encoding, and checksums. Height, water stage, masks, sediment evidence, and flow direction are different channels. A brightness mask is not a sediment mass or groundwater store.

[Gaea’s tiled-build workflow](https://docs.gaea.app/using/using-gaea/build-and-export/tiled-builds.html) distinguishes upstream world-scale work from tiled detail. Adopt that distinction: resolve global dependencies before local detail and declare the finite halo of every local operation. Matching tile-edge samples does not prove that drainage or erosion had enough surrounding context.

Recommended runtime dependency order:

1. Resolve the saved profile and immutable authored or procedural inputs.
2. Evaluate macro relief, geology, and the finite planning domain.
3. Resolve canonical drainage, closed basins, lake outlets, and cross-region contracts.
4. Build volumetric occupancy, caves, deposits, and supported channel geometry.
5. Apply material and habitat eligibility, then selected communities and features.
6. Overlay persistent edits and depletion, initialize approved accounts once, and publish a revisioned snapshot.

The content-planning order remains biome identities first and climate scenarios afterward. Runtime placement nevertheless requires its chosen climate and water prerequisites before placing a habitat. These two orders serve different purposes.

Determinism means equal saved inputs produce equal generated output across worker count, query order, and cache eviction. Every randomized stage uses named, versioned seed derivation and stable integer feature IDs. A cache must accelerate a pure result, not become an undeclared source of randomness. Generation never reads a renderer’s LOD choice.

## 4. Drainage graphs, lakes, and branch ownership

The original [Priority-Flood paper](https://arxiv.org/html/1511.04463v1) is a useful drainage/depression reference, but a finite-domain boundary must be deliberate. An unloaded chunk is unknown runtime data, not an ocean outlet or algorithmic nodata sink.

**Proposed graph contract:** every region exports canonical boundary edges keyed by saved profile, integer boundary identity, orientation, and edge index. Each edge declares bed/sill elevation, planned water stage, receiving basin, width class, and the meaning of its discharge field. Four-region corners have one canonical owner. Neighbor order cannot alter the answer.

Separate three quantities:

- Catchment accumulation is a generation input or hydraulic estimate.
- Branch allocation is a fraction of one upstream available discharge.
- Actual flux is water accepted by a destination during a simulation interval.

A split cannot give every branch the full upstream supply. Allocate bounded shares whose sum is at most the available amount, using deterministic remainder handling. V7 staged geometry and branch weights must remain labeled geometry/planning until the live transfer path is accepted.

A lake record distinguishes area/elevation geometry, capacity, current volume, sill, outlet capacity, and downstream head. A large initial lake is not a perpetual source. Preserve intentional endorheic basins and waterfalls. Outlets require supported beds and banks against the actual discretized water surface.

Edits invalidate the smallest declared dependency region plus halo. Dams, excavation, and cave openings may split or merge hydraulic connectivity; they must not initialize new water or discard the old basin’s storage. Initial implementation may temporarily suspend affected transfers while rebuilding a bounded graph. It must expose pending status and preserve the last owned quantities.

### Earlier Dev69 source checkpoint — 2026-10-29

The [Dev69 stitched-ocean receipt](../replicas/git/docs/validation/engine-continuation/v8-ocean-seams-2026-10-29/README.md) reports an opt-in marine-connectivity policy responding to the historical Dev68 API-double closed-basin witness. A 32-block sampled lattice seeks connection to a designated deep marine core; a region edge alone is no longer sufficient under the new policy. Routing policy v2 has its own fingerprint inside the existing saved-profile revision-5 envelope. Existing v1 profiles retain their prior behavior; this is neither automatic migration nor native activation.

The receipt reports 158 new checks within 19 passing Java21/API-double suites. That evidence does not exercise the real WatershedRegion dependency, genuine Mojang codecs, Java25/NeoForge save/reopen, finite-water ledger, or GPU path. At this checkpoint, region-local priority-flood routing, narrow passages, and misleading inland continentalness remained open. Preserve Dev68 as its historical model witness. The later Dev70 receipt records a separate counterexample in Dev69 v2; the source-pinned succession below keeps those observations distinct.

Before activation, require actual cross-region basin/outlet and discharge evidence, sampled-feature/error limits, explicit unresolved-search semantics, old/new profile reopen, zero-drift water/constituent tests, and multiseed native Vulkan/OpenGL acceptance. A marine-mask correction alone does not establish complete global hydraulic connectivity.

### Successors through Dev77 — fixed source 33c43c943f04

This edition is frozen at [33c43c943f0469195e7ab1a6852ab4360fad1788](https://github.com/Shnewbs/Deep-Primal/commit/33c43c943f0469195e7ab1a6852ab4360fad1788), whose declared source version is `0.0.1-alpha.29-dev.77-seam-contract.1`. The following receipts describe source-level development, with no new playable-build, native-hydrology, GPU, or independent-grade acceptance claimed here.

| Checkpoint | Scoped change and evidence boundary |
| --- | --- |
| [Dev70](../replicas/git/docs/validation/engine-continuation/v8-basin-closure-2026-10-29/README.md) | Opt-in routing v3 excludes fully explored deep closed basins; budget-limited continuation still admits a heuristic. Java21/API-double evidence. |
| [Dev71](../replicas/git/docs/validation/engine-continuation/v8-ocean-outlet-seeds-2026-10-29/README.md) | Opt-in v4 supplies exact lattice marine seeds to an optional production watershed constructor. The changed real watershed dependency was not compiled in its tests. |
| [Dev72](../replicas/git/docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/README.md) | Opt-in v5 binds a signed authored `ocean_domain` to saved identity. Reproducible provenance is not global connectivity proof. |
| [Dev73](../replicas/git/docs/validation/engine-continuation/v8-ocean-interior-2026-10-29/README.md) | Opt-in v6 gates every traversed wet interior and separates thread-owned graph samplers. Actual production watershed/native integration remains unqualified. |
| [Dev74](../replicas/git/docs/validation/engine-continuation/v8-shoreline-coherence-2026-10-29/README.md) | Opt-in v7 aligns mixed-corner water-kind metadata with the sea-stage OR predicate, preserving stage and physical quantities. Earlier policies remain frozen. |
| [Dev75](../replicas/git/docs/validation/engine-continuation/v8-outlet-topology-2026-10-29/README.md) | A real standalone minimax port-topology helper has direct Java21 tests without Minecraft doubles. Supplied ports/anchors and planned runoff do not establish native fluid transport. |
| [Dev76](../replicas/git/docs/validation/engine-continuation/v8-region-port-bridge-2026-10-29/README.md) | Opt-in perimeter observations feed a conservative bridge with zero runoff. Adapter tests use synthetic regions, and the receipt limits exact post-publication source identity. |
| [Dev77](../replicas/git/docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md) | Merge rejects incompatible shared flood/stage, area, water-kind, and receiver observations, including the historical 90-versus-900 witness. Exact-blob adapter/mutation checks are reported; no native V8 caller exists. |

Routing policy v2–v7 identities are distinct from the existing saved-profile revision-5 envelope. Neither a newer source checkpoint nor a passing helper changes old saves. Future authoritative integration needs its own explicit saved identity and compatibility decision.

**Remaining contract:** rejecting contradictory seam observations prevents false agreement; it does not reconcile interior receivers, lake spills, or regional stage. Preserve missing-neighbor and incompatible states separately. Do not average conflicting fields or let arrival order choose a winner. Require independently justified marine anchors and an actual production watershed caller before treating the port graph as authoritative.

Keep cumulative catchment area, conserved planned runoff, and accepted physical water transfers as separate typed quantities. Dev76/77 observational ports retain zero runoff; summing cumulative area would duplicate upstream contributions. Direct tests of Dev75 qualify that helper only, while Dev76/77 doubles qualify their adapter contract. Real production dependency compilation, genuine codecs/reopen, old/new native borders, finite-water/salt/sediment lifecycle, and named-hardware Vulkan/OpenGL acceptance remain release gates.

## 5. Geology, soils, habitats, and resource identity

The [named catalog index](../replicas/git/docs/NATURAL-TYPE-CATALOG.md), [coverage inventory](../replicas/git/docs/ENVIRONMENTAL-COVERAGE-INVENTORY.md), [biome catalog](../replicas/git/docs/BIOME-CATALOG.md), and [variant/layer catalog](../replicas/git/docs/BIOME-VARIANTS-AND-COVERAGE.md) describe different axes. Their row counts are not additive promises of registered blocks, species, or biomes.

**Proposed material schema:** canonical material key; aliases; rock/mineral/mixture kind; composition; state; host-geology constraints; processing yields; optical properties; mechanical properties; and independently calibrated hydrologic properties. A display tint or texture-class name is insufficient authority for permeability, erosion resistance, fertility, or drinkability.

Represent soil texture, order, horizon, structure, organic fraction, salinity, and moisture separately. A selected property carries units, applicable scale, evidence status, and game approximation. Unresolved science remains unresolved. Surface masks do not define the geology exposed by a deep cut.

A habitat predicate returns eligible, ineligible, pending, or unsupported. Prerequisites can include province, relative relief, substrate, hydroperiod, actual water connection, salinity, and vertical daylight. A forest can exist above a cave and a water-column habitat above a seabed habitat. Seasonal wetness, snow, fire history, and regrowth are state/layer changes, not ordinary biome-ID replacements.

Each selected resource feature has one finite harvest account or an explicitly bounded regrowth rule. Canonical aliases, umbrella catalog rows, and material forms cannot produce duplicate loot. Regeneration must respect harvested and placed provenance. Cave-ins retain the accepted underground scope and cover protections; missing neighboring geometry arrests uncertain collapse, and no surface-collapse sinkhole is introduced.

## 6. Water quantities and the complete ledger

The V4/Water Engine checkpoint reports sixteen native units per full cell and 256 environmental quanta per native unit: **4096 quanta per block**. It reports 64×64 regional groundwater/snow stores with 1024-block capacities each. These are historical defaults, not new performance measurements or scientifically calibrated water-table physics.

**Proposed invariant**, applied independently to water and every conserved constituent:

```text
sum(all authoritative owners)
  = initial conditions + declared external inputs - declared external removals
```

“All owners” includes loaded cells, persisted unloaded cells, regional stores, ocean/atmosphere accounts, containers, pipes/tanks, enabled soil or film stores, ice/snow, dry residues, and transfers in escrow. An in-flight quantity is counted exactly once. A diagnostic restricted to loaded chunks must identify its limited scope.

Use checked signed 64-bit quantity storage with wider intermediates or checked multiplication/division for ratios. Overflow refuses the operation; it does not clamp or wrap. Derived concentration, stage, temperature, and fill fraction are not alternative quantity owners.

Initialization is a transaction with a persistent once-only marker. Region load, inspection, a new well, cache eviction, or a changed work-record object cannot reinitialize supply. The reported ocean/atmosphere reserves are finite gameplay accounts; marked ocean boundaries are distinct from ordinary inland water at the same elevation.

Source delivery debits only accepted water. Saturated destinations and backwater leave supply in its backing account, with no delivery backlog. The planned channel-surface guard pauses new natural emission; it cannot erase existing floodwater, rainfall, dam storage, or legitimate overtopping.

Pure-water evaporation retains nonvolatile constituents. If water reaches zero, a residue owner keeps those quantities until rewetting, removal, or an explicitly accounted reaction. Empty water storage is not permission to delete salt or sediment metadata.

## 7. Transfers, topology edits, and durable recovery

**Live transaction proposal:** compute against one immutable snapshot; validate owner identities, revisions, capacity, and quantity; reserve all affected owners; apply matching debit and credit on the authoritative thread; increment revisions; then publish events. Simulation/preview calls do not mutate or reserve indefinitely. Observers cannot alter quantities inside a post-commit event.

For host adapters, 1000 mB corresponds to 4096 quanta. An integer-mB interface cannot exactly represent every native quantity. Start with **125 mB = 512 quanta = two native units**, or introduce an explicitly owned rational remainder account. Repeated downward rounding without retained ownership is loss, even when it prevents duplication.

Durability needs more than an atomic save of one file. Use a journal or coordinated checkpoint covering source, destination, escrow, and replay evidence. A transfer key includes world UUID, dimension, boundary, owner epochs, and monotonic sequence. Record:

```text
txId, sourceOwner, destinationOwner,
sourceRevision, destinationRevision,
waterQuanta, constituentAmounts,
state, durableSequence, cancellationFence
```

Crash recovery determines whether a debit, escrow ownership, and destination credit exist before resolving the transaction. **Elapsed age alone cannot authorize rollback.** Fence the destination against late commit before returning escrow to the source. Deduplication data cannot be garbage-collected while an old message or recoverable checkpoint can still replay its sequence.

[Voxel Tools’ stream documentation](https://voxel-tools.readthedocs.io/en/latest/streams/) identifies an asynchronous-save/world-path hazard. Adopt immutable world identity for every job and storage handle. Closing world A and opening B cannot redirect a late save or terrain result into B.

A capacity-reducing gameplay edit is one geometry-and-water transaction. Route displaced water through bounded accepted destinations, use a separately bounded escrow owner if that design is implemented, or defer/reject the edit. Never silently delete water to make placement succeed. Operator destruction is a separately declared removal with a ledger event and permission scope.

When capacity is reduced below existing contents, retain an over-capacity state that refuses further input and permits accounted output. Quarantine preserves raw records and quantities while disabling exchange. It does not mean resetting the region to an empty aquifer or treating unknown solid terrain as air.

## 8. Composition, heat, and measured flow

The existing salt/sediment advection model is a starting point. **Proposed bulk-transfer contract:** accepted volume carries an integer share of each constituent, calculated with checked intermediates; subtract and add the same quantity; retain exact rounding residue with a named owner.

For multiple receivers, allocate from one donor snapshot with an aggregate donor budget. Sequential rounding must not create quantity or let total proposed transfers exceed the donor. Define canonical edge ordering or deterministic remainder allocation.

Composition-only diffusion changes constituent owners while leaving both water levels unchanged. Use per-edge proposals bounded by nonnegativity and no concentration overshoot, then enforce each donor’s total budget across all neighbors. Pairwise stability alone does not prove stability for a cell with several simultaneous edges. Test zero volume, unequal volumes, repeated tiny exchanges, and maximum representable quantities.

A per-cell constituent limit or sparse-entry cap needs a lossless overflow policy: refuse the relevant operation, use a bounded alternate representation, or compact only equivalent states. Dropping a fifth constituent or “rare” nondefault entry is forbidden. Unknown constituent IDs survive save/load as opaque accounted quantities when supported, or make the affected operation unavailable.

If thermal behavior is enabled, store energy and phase/capacity information; derive temperature. Mixing equal volumes at different heat capacities cannot use an unweighted temperature mean. Freeze/thaw moves water-equivalent quantity and explicitly routes constituents; a visual ice layer cannot create water or silently purify it.

Measured flux is derived from accepted transfers over a declared simulation-time window. Signed net flow and total throughput are different diagnostics. A flat full pipe can carry flow without a visible level gradient, so client gradients and generated river speed remain hints. Gameplay wheels, currents, and gauges require a current authoritative flow contract and invalidation when geometry changes.

## 9. Loaded-world scheduling, liveness, and LOD

Preserve the current **no unloaded catch-up** policy until an explicit coarse model is adopted. Unloaded boundaries remain sealed in that mode. Sampling a hypothetical climate value for an unloaded region does not authorize advancing its water accounts.

The reported environmental scanner admits up to 64 columns per tick and checks a nominal 2 ms window between columns. That is not a hard bound on one uncached operation. The reported 32-unit regional discharge allowance and four-unit outlet visit cap must be tied to one persisted logical interval, independent of how many cached outlet rechecks or work-record recreations occur.

**Proposed scheduling contract:**

- Separate deterministic simulation-step budgets from wall-clock admission.
- Coalesce repeated dirtiness by owner/revision while preserving the oldest outstanding obligation.
- Reserve fair service for old eligible work; apply bounded priority to urgent local interaction.
- Reject stale results, cancel departed-world work, and wake on geometry, material, border, or owner changes.
- Expose oldest-work age, queue occupancy, retries, refusals, interval usage, and time-window overruns.
- Defer work under overload; do not accumulate water to emit as a future burst.

For a fixed eligible set of N tasks and B reserved admissions per service round, require every task to receive an admission within ceil(N/B) rounds. Completion additionally requires its dependencies to remain valid long enough; do not promise convergence under endless edits to the same cell. Test a quiet region while other regions remain busy.

Distinguish generation determinism from replay determinism for the same activation/input history. Loaded-only evolution with different player movement histories may legitimately differ. Worker order must not alter an otherwise identical replay.

Render LOD never owns physical water. Keep fine collision, authoritative quantities, and generation/profile identity separate from distant geometry. The [GeoClaw paper](https://arxiv.org/html/1008.0455v2) describes a coastal refinement correction that can introduce water; that scientific approximation cannot be imported into a zero-drift gameplay ledger. Any future physical coarse/fine model needs conservative ownership transfer, its own validation, and an explicit enabled policy.

## 10. API errors and operational visibility

Return actionable errors: destination_full, insufficient_supply, pending_geometry, unavailable_owner, stale_revision, unsupported_schema, conversion_granularity, budget_deferred, or arithmetic_overflow. A rejected transfer changes no quantity; a deferred topology edit retains its original geometry and ownership.

Inspection must be read-only and must not initialize stores. Report saved profile, source provenance, owner, volume, composition status, head/stage units, last accepted transfer, refusal reason, and snapshot age. Show why a spring stopped without requiring players to interpret implementation internals.

Publish metrics for active/sleeping cells, border wakes, regional records, memory per owner kind, journal bytes, composition entries, save latency, network bytes, and maximum/percentile queue age. Choose release budgets on named hardware before judging performance. A bounded queue that permanently starves work has failed correctness even if its frame-time graph is attractive.

## 11. Acceptance fixtures

All numerical tests below are **proposed gates**, not passing results. Record seed, saved profile, commit, active-set/input replay, configuration, machine, backend, and expected answer.

| Fixture | Required result |
| --- | --- |
| Signed coordinates | Exact region/local mapping at -65, -64, -1, 0, 63, 64 and supported height limits; no alias or overflow |
| Generation replay | Identical hashes with 1, 2, and 4 workers, reversed region order, cache eviction, and shared corner queries |
| Frozen saves | Golden V1–V7 and existing V8 samples unchanged; material-only revisions preserve their declared geometry invariant |
| Canonical watershed | Neighbor edges/corners agree; intentional closed basins remain closed; missing chunks never become drains |
| Branch split | Sum of accepted branch flux never exceeds accepted upstream supply; no branch duplicates remainder |
| Still pool | No spontaneous drift, oscillating source delivery, or unequal equilibrium from axis bias |
| Dam edit | Placement/removal and basin split/merge retain exact water/constituents; source safety preserves actual stored floods |
| Crash matrix | Kill after every durable transaction transition; replay/delay messages past cancellation; exactly one transfer or none |
| Adapter round trips | One million supported reversible exchanges produce zero water/constituent drift; unsupported precision is refused |
| Dry residue | Evaporate saline water, save/reload, and rewet; recover the same nonvolatile quantities |
| Metadata saturation | Fifth constituent and full sparse storage never delete information or accept an unrepresentable transfer |
| Unload and world switch | No catch-up burst, reinitialization, cross-world write, late stale commit, or double-owned escrow |
| Scheduler saturation | Quiet-region work receives its declared fair admissions amid continued remote edits; oldest age is observable |
| Sub-unit and ice | Film promotion/demotion and enabled freeze/thaw retain exact water-equivalent quantity |
| Presentation independence | Identical physics hashes with water effects, LOD, and backend presentation choices changed |
| Catalog/provenance | Valid references and dispositions; harvest/redecoration cannot respawn finite stock or replace player construction |

For long-run tests, advance at least 1000 environmental intervals with randomized accepted edits and exchanges, auditing after every commit. Exact integer conservation tolerates **zero drift**. Visual tolerances, hydraulic equilibrium tolerance, and response deadlines must be declared per fixture before execution; do not substitute a screenshot for the ledger.

## 12. Build order and exit evidence

**Stage T0 — compatibility and quantities:** finalize saved-profile decoding, account enumeration, unit conversions, current/pending queries, and the replay harness. Exit with golden-save preservation and zero-drift reversible exchanges.

**Stage T1 — edits and recovery:** implement atomic displacement policy, journal recovery, world-epoch fencing, unloaded ownership, and fair wake-up. Exit with the crash matrix, topology edits, and saturated quiet-region progress.

**Stage T2 — connected terrain and habitats:** implement canonical drainage edges, finite lakes, explicit branch shares, material identities, and selected habitat prerequisites. Exit with seam/corner determinism and truthful planning-versus-runtime status cards.

**Stage T3 — environmental detail:** add only selected composition, thermal, snow, canopy, soil, or coarse-hydrology modules, each with quantity ownership and disabled behavior. Optional live erosion and more realistic circulation remain separate scopes.

**Stage T4 — scale and presentation:** tune measured budgets, render handoff, networking, and worst-case active areas on named hardware. Preserve the same invariants while changing quality settings.

The resulting chapter expands executable boundaries around existing goals. Completion remains tied to implementation plus recorded evidence, with originals and their historical claims preserved.

