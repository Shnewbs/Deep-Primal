# Merged requirements and acceptance register

This register consolidates shared work; it does not replace detailed catalogs or duplicate the original gap IDs. **Retained** means an existing project requirement. **Proposed** means added engineering detail from this review. All qualification gates below remain open here unless linked evidence explicitly closes their named scope. This documentation task executes none of them.

## Immediate cross-system work

| ID | Priority / origin | Requirement | Required evidence |
| --- | --- | --- | --- |
| ENG-01 | P0 / retained | Preserve source, saved recipe, world/dimension, edit, request and device identities through asynchronous admission/publication | Late completion after unload, world switch, reset and content revision cannot publish into the wrong owner |
| ENG-02 | P0 / retained | Make finite-cell state, transfer receipt and durable ledger one accepted ownership outcome | Real callbacks, duplicate delivery, crash at each transition, reopen and world/profile mismatch; exact quantity/salt/sediment totals |
| ENG-03 | P0 / retained | Derive canonical basin/outlet topology and distinguish unknown from marine connectivity | Actual multi-region closed basin, ocean endpoint, signed seam/corner and changed arrival-order fixtures; observed ports alone do not pass |
| ENG-04 | P0 / retained | Preserve frozen V1–V7 and all selected saved V8 policies, player edits and depleted resources | Old/new creation defaults, exact historical decode, newly explored border, codecs, save/reopen and native base-column/heightmap parity |
| ENG-05 | P0 / retained | Bind accepted edits to water displacement, geometry/light invalidation and LOD validity | Dam-in-rain integration fixture below with failed mutation, unload and reopen |
| ENG-06 | P1 / retained | Keep whole-engine qualification distinct from component correctness | [Evidence dimensions](POLICY-AND-STATUS.md), [exact renderer gates](RENDERING-LIGHTING.md), real native/platform evidence and fresh complete independent rubric |

## Terrain and physical water

| ID | Priority / origin | Consolidated requirement | Acceptance boundary |
| --- | --- | --- | --- |
| TW-01 | P0 / proposed hardening | Separate source marker/drain-stop anchor from paid replenishment | No unaccounted volume; define removed/disconnected marker behavior and all environmental exchanges |
| TW-02 | P0 / proposed hardening | One canonical, timestamped face flux with declared halo and same-step boundary reads | Equal/opposite owner transfers, no duplicate face ownership, stale halo rejection and seam equivalence |
| TW-03 | P1 / proposed hardening | Numerically qualify the selected physical model | Lake-at-rest, dry/wet fronts, nonnegative storage, bed steps and bounded timestep; no invented accuracy/CFL claim |
| TW-04 | P1 / proposed future mode | Conserve physical coarse/fine ownership separately from display LOD | Refinement/restriction and subcycled flux reconciliation, deterministic remainders, crash/reopen; no duplicated covered coarse account |
| TW-05 | P1 / proposed hardening | Address procedural randomness by stable saved identity and resolve overlap by total priority | Chunk/worker/cache order invariance and unrelated-stage stability under declared revision |
| TW-06 | P0 / proposed hardening | Include all edits and boundary dependencies in distant artifact validity | Edge/corner edits, dirty ancestors, transition meshes, unload/reopen and eventual update progress |
| TW-07 | P1 / retained | Coherent 3D geology, climate/biome mixtures, soils, deposits and vegetation | Shared cross-region materials/host geology; finite reserves; approved habitat/landform comparisons; catalogs alone do not pass |
| TW-08 | P1 / retained | Connected tributaries/lakes/spillways, underground routes, snow/ice and accounted chemistry | Unified routed water budget, actual native outlets, finite phase transfers and constituents; no generated sinkholes |
| TW-09 | P1 / retained | Smooth water presentation from accepted physical state | Continuous cliff/front/edge scenes on both APIs; no changed quantity from interpolation, foam, optics or LOD |

Detailed contracts and source registers: [terrain/water synthesis](TERRAIN-WATER.md), [TW-01–06 research](research/terrain-water-additions.md), [original Water Engine gaps/decisions/questions](sources/WATER-ENGINE.md). Preserve its 37 gap IDs, 16 decisions and ten open questions as original scoped records; their existence does not imply fresh failures.

## Renderer, lighting and reconstruction

| ID | Priority / origin | Consolidated requirement | Acceptance boundary |
| --- | --- | --- | --- |
| R-L01 | P0 / proposed hardening | Separate presentation retirement from submit completion | Per-image/present ownership, resize/minimize/recreate/shutdown trace, supported extension path where applicable |
| R-L02 | P0 / proposed hardening | Declare checker coverage and prove unobserved hazards independently | SyncVal version/settings plus alias/host/descriptor lifetime map and intentional checker witness |
| R-L03 | P1 / proposed hardening | Record versioned vendor/runtime capability evidence, including conflicting guidance | Actual query, initialization and first evaluation; truthful unavailable/failure reason; no hardware-family guess |
| R-L04 | P0 / proposed hardening | Make temporal guide conventions executable | Known camera/object/foliage/water transforms, pre-hand depth, exposure, jitter, reset/rebase and admitted extent envelope |
| RL-09 | P1 / retained | Useful bounded LOD and greater distance | Delivered/edited coverage, native-ready handoff and transition correctness; requested distance never implies delivery |
| RL-10 | P0 / retained | Complete resource ledgers and useful queue progress | CPU/native/GPU pending/resident/retired peaks, failures, stationary capacity recovery and bounded oldest eligible age |
| RL-11 | P1 / retained | Add real selectable FSR2/3/4 and DLSS providers | Qualify each provider/backend/device separately with [RL-G01–08](RENDERING-LIGHTING.md); preference storage alone remains unsupported |
| RL-12 | P1 / retained | Logical-light authority, material optics and explicit contribution ownership | Host oracle parity, source addition/removal, caves, pending neighbors, LOD/edit/reload, debug composition and actual images |
| RL-13 | P1 / retained | End-to-end performance demonstrated with matching output | Named hardware, seeds/cameras/settings/cache state, CPU/GPU p50/p95/p99 and raw sample counts, memory/coverage, all-visible and travel/edit worst cases |

The [rendering chapter](RENDERING-LIGHTING.md) owns RL-G01–08. [New renderer/lighting research](research/lighting-rendering-additions.md) owns LR-A01–04. [The detailed lighting master](sources/LIGHTING-ENGINE.md) retains its complete numerical and scene requirements. This register links them rather than renumbering hundreds of detailed tests.

## Architecture, content and interfaces

| ID | Priority / origin | Consolidated requirement | Acceptance boundary |
| --- | --- | --- | --- |
| ARC-01 | P1 / retained | Keep portable core, host adapters, build modules, gameplay modules and tools distinct | Dependency/import/dedicated-server/profile checks; optional module absence inert; no claim of a finished module manager |
| ARC-02 | P1 / retained | One typed definition/config/script/editor pipeline with stable IDs and explicit precedence | Malformed/unknown/old/new/overlapping inputs, immutable worker definitions, per-value origin, safe migration and saved recipe |
| ARC-03 | P1 / retained | Stable assets and complete applicable stone families | Raw/Cobble/Brick/Mossy/Gravel/Paver/Smooth/Chiseled/Polished/Cracked plus material-specific forms; source/export hashes and reviewed native appearance |
| ARC-04 | P1 / proposed hardening | Deterministic focus, narration and non-pointer equivalents | Filter/rebuild/modal/resize/theme/reload/error paths, exact numeric entry and Apply/Revert; no gameplay input leak |
| ARC-05 | P2 / proposed detail | Bounded in-game Markdown guide dialect and stable localized page IDs | Supported syntax/resources, duplicate IDs, locale fallback, cyclic/missing links, limits and discovery-aware lookup |
| ARC-06 | P1 / proposed hardening | Declare build/dependency/generator/native inputs for reproducibility | Independent clean builds and nested/output byte comparison; reviewed dependency provenance; archive defaults alone insufficient |
| ARC-07 | P2 / retained direction | Plan the independent-game host without relabelling a hosted prototype | Selected window/input/audio/render/storage/network host and launch/edit/fluid/save/reopen slice without Minecraft |

Detailed requirements and constraints: [architecture/authoring](ARCHITECTURE-AUTHORING.md). Wider survival milestones and named content catalogs remain linked from the [original roadmap](../../roadmap.md) and [catalog](CATALOG.md); documenting them does not expand the current engine builder's scope.

## Shared integration witness

**Proposed, not executed:** a player edits a dam during rain, observes it, travels away, and reopens the world.

1. Resolve the world/profile/edit and physical account identities. Reserve legal displacement or defer the edit.
2. Commit the barrier and finite-volume/constituent ownership under one accepted result. Test callbacks that change, reject, unload or partially apply writes.
3. Reconcile downstream/outlet and cross-region obligations without duplicating replenishment or mistaking an unknown basin for ocean.
4. Invalidate near geometry, optical/light state, water presentation and every affected coarse edit dependency from the same accepted change.
5. Maintain declared coverage and lifetime under delayed workers, upload failure and device reset; retain or withdraw stale presentation according to its contract.
6. Persist cells, edits, account/transfer receipts and outstanding topology work. Reopen under the same saved recipe, fence late old-session work, and compare exact physical totals.
7. Record real native correctness, images, queue progress, frame/tick latency and peak ownership. Only the tested boundaries may close their requirements.

## Continued study queue

The next study should resolve a decision or fixture, not add another general engine survey. Priorities: actual V8 outlet/native-save coupling; source-anchor behavior after player edits; world-depth/hand and temporal guide production; backend presentation ownership; offline editing/LOD invalidation; practical tool/schema migration. Each study records the precise source version, inspected limitation, applicable Deep Primal boundary and a small falsifiable check in [the research register](research/README.md).
