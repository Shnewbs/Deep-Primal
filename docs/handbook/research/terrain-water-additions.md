# Terrain and Water: Focused Contract Additions

**Reviewed:** 2026-10-29. **Status:** proposed design and acceptance criteria; no new runtime qualification.  
**Compared against:** `docs/research-expanded/2026-10-29/ENGINE-DESIGN-EXPANSION.md` and `chapters/terrain-water.md`, the earlier edition pinned to Dev77 / `33c43c943f0469195e7ab1a6852ab4360fad1788`. These chapters already cover conservation, canonical boundaries, frozen saves, escrow recovery, bounded scheduling, and source-only evidence. The additions below specify narrower mechanisms and witnesses; they should be merged into those subjects rather than becoming another competing engine roadmap.

No numerical solver or external engine is prescribed as a dependency. The cited projects supply lessons; the following contracts are Deep Primal design proposals inferred from them.

## TW-01 — Separate source-cell representation, drainage termination, and replenishment authority

Preserve the owner's established behavior: world-generated water is initialized as source cells; downstream drainage stops when its route reaches a source cell; chemical mixing changes constituents without adding water. A source marker must have a separate meaning from unlimited supply.

Proposed serialized fields:

| Field | Meaning | Must not imply |
| --- | --- | --- |
| `nativeSourceCell` | Host representation of a full generated/source cell | Unlimited creation or automatic refill |
| `drainageAnchor` | Saved policy permits this cell to terminate downhill drainage | A renewable account or a globally connected ocean |
| `waterOwnerId` | Account holding the actual finite quantity | Permission to debit unavailable or unloaded state |
| `emitterPolicyId` | Named, revisioned natural replenishment rule, if any | Transferable spring identity in a bucket |
| `backingAccountId` | Finite account charged for accepted replenishment, when required | An initial-condition reset on chunk reload |
| `sourceProvenance` | Generated, player placed, imported, or administrative | Unrecorded upgrade from ordinary water to a natural emitter |

A generated lake may therefore contain native source cells and drainage anchors while retaining a finite account. This reconciles source-shaped initialization with the existing finite-lake requirement. Do not remove actual stored floodwater when an emitter's surface cap pauses delivery. When a source cell is consumed or altered, update anchor eligibility through the accepted state transaction rather than an unpersisted visual flag.

The exact reachability predicate for “hits a source” needs one versioned implementation: direct connected downstream path, supported liquid types, allowed vertical connections, and behavior while a neighbor is pending. A search budget expiring yields unknown and defers that drain decision. It must not silently classify the stream as draining or connected to an ocean. This clarification preserves the owner rule without claiming the current code already implements it.

**Acceptance:** generated lake/source markers survive save/reopen without replenishing depleted quantity; a stream ending at a source terminates drainage; removing the source updates only affected paths; saline/fresh mixing preserves total water; filling a bucket never transfers natural-emitter permission; a pending route never becomes a free source or drain.

## TW-02 — Make boundary exchange a time-stamped, single-owned face operation

The earlier chapter defines canonical seam observations and transactions. Add an explicit distinction between *read-only halo samples* and *owned boundary flux*. A halo copies the neighbor's state for calculation; it owns no extra water.

Proposed boundary record:

```text
BoundaryExchange {
  worldKey, topologyRevision, canonicalFaceId,
  sourceOwnerEpoch, destinationOwnerEpoch,
  sourceSnapshotRevision, destinationSnapshotRevision,
  intervalStart, intervalEnd, orientation,
  acceptedWaterQuanta, acceptedConstituentQuanta,
  transactionSequence, state
}
```

Both regions refer to one canonical face and opposite signs of the same accepted transfer. They may not independently round and commit their own estimates. Contributions are measured over the same simulation interval; ghost values from a newer neighbor step cannot be combined with an older interior state without an explicitly chosen interpolation policy.

Proposed halo descriptor: field semantics, integer domain, stencil extent, corner dependencies, terrain/edit/topology revision, simulation time, validity, and boundary condition. Compose local stages by their dependency footprints: two sequential radius-one stencils can require radius two of original inputs. Do not apply that finite-stencil reasoning to a whole-basin connectivity search, whose dependencies can be much larger. Cache validity must cover the complete dependency footprint, not just the central chunk.

**Acceptance:** a packet delivered twice commits once; swapping region calculation order preserves net transfer; signed coordinates share the same canonical face; stale halo revisions reject publication; diagonal/corner witnesses exercise all dependencies; sealed unloaded boundaries admit zero transfer under the current no-catch-up policy.

## TW-03 — Qualify wet/dry behavior separately from conservation

Exact accounting alone does not prove plausible or stable flow. [Clawpack's solver guide][S1] distinguishes bathymetry-aware well-balanced schemes from algorithms able to handle dry states. That distinction is useful even if Deep Primal retains a discrete transfer solver.

Add a declared numerical qualification card containing: state variables and units; admissible depths; dry threshold and whether it affects authority or only velocity; update interval; maximum admitted outflow; bed/free-surface convention; boundary conditions; and response when the allowed work budget cannot finish an update. Simulation-time debt must remain observable. Do not increase a physical timestep solely to clear a performance backlog.

Proposed gates:

| Witness | Required outcome |
| --- | --- |
| Lake at rest over uneven bed | No unexplained water motion or source cycling; declared level/velocity tolerance |
| Wet-to-dry front | No negative quantity, invalid concentration, or NaN; bounded outward motion |
| One quantum in a broad shallow cell | No threshold deletion; any visually dry quantity retains an owner |
| Dam break and backwater | Accepted transfer bounded by source quantity and destination capacity |
| Repeated axis rotation | Equivalent outcomes within declared numerical tolerance; no unexplained axis preference |
| Salt or sediment tracer | Same flux ownership as water, with nonnegative constituent quantity and no artificial extrema from clipping |
| Uneven frame rate | Same accepted simulation history; rendering interpolation changes no authoritative result |

If a continuous shallow-water or similar model is later adopted, specify its stability restriction and positivity method, with a separate proof or test for source terms and wet/dry boundaries. Do not advertise a particular accuracy order or CFL bound before choosing the actual scheme.

## TW-04 — Keep physical coarsening separate from render LOD

The current chapter already states that render LOD owns no physical water and that coarse simulation is a future policy. Add a precise conservation gate before enabling physical multiresolution.

[AMReX's documentation][S2] explains that coarse and fine advances can disagree on area/time integrated interface flux, and uses a flux register to reconcile that difference. The transferable lesson is interface reconciliation, not adopting AMReX.

Proposed rules for a future coarse hydrology mode:

1. Exactly one level owns each physical volume at a time; covered coarse cells are derived summaries rather than duplicate accounts.
2. Refinement partitions a parent's integer water and constituent quanta into children with deterministic remainder ownership. Restriction sums quantities; it does not average quantities.
3. Coarse/fine interface exchanges share one integrated flux and interval. If levels subcycle, accumulate each fine contribution and reconcile before publishing a completed interval.
4. A reconciliation that would overdraw an owner or violate capacity cannot be fixed by clamping away mass. Refuse the interval, use a conservative redistribution with named owners, or retain it pending under the selected policy.
5. Regridding and all unresolved interface obligations survive crash/reopen together.
6. Visual water surfaces can be filtered at any LOD without running these physical transfers.

**Acceptance:** move a stationary refinement boundary across a still pool; transport a pulse across it in both directions; refine/coarsen saline water repeatedly; crash after each ownership switch; compare all owner totals including pending interface obligations. Exact integer quantity drift must be zero. Temporal and spatial error tolerances are separate measurements.

## TW-05 — Make deterministic random addressing and collision precedence explicit

The earlier chapter requires named/versioned seed derivation. A remaining implementation choice is whether variable draw counts in one feature can perturb another. [Random123][S3] demonstrates stateless generation from a key and counter; that is a useful option for addressable procedural randomness.

Proposed address:

```text
randomBits = PRF(
  key = savedSeed + generatorRevision + stageId,
  counter = canonicalFeatureId + samplePurpose + drawIndex
)
```

The notation describes a tuple, not string concatenation or a recommended ad hoc hash. Specify exact byte encoding, signed-coordinate mapping, endianness, hash/PRNG algorithm and revision, output-to-range conversion, and overflow behavior. Never key randomness by thread index, task completion order, pointer address, or mutable shared consumption count. Rejection sampling advances only the identified sample's local draw index.

Addressable RNG does not resolve overlapping features by itself. Define a total stable priority for overlapping claims, allowed replacement classes, and feature IDs. A tree crossing a chunk edge is one canonical feature clipped into chunks. [Voxel Tools' generator documentation][S4] explicitly warns that parallel multipass placement and unpredictable column order can break determinism, making this a relevant acceptance target.

**Acceptance:** reorder chunks, worker counts, cache evictions and rejected candidates; identical saved inputs give identical occupancy/material/feature hashes. Add one optional decorative draw in one stage and prove unrelated stages unchanged under the declared revision policy. A changed terrain algorithm still requires a saved revision; deterministic execution is not compatibility.

## TW-06 — Define edited-LOD dependency closure and incomplete coverage

[Voxel Tools' smooth-terrain documentation][S5] distinguishes full-resolution editable data from distant lower-resolution data and explains the memory cost of loading all edited chunks. Deep Primal needs its own explicit choice for distant edited geometry.

Proposed derivation identity:

```text
LodArtifactKey {
  worldKey, level, canonicalRegionExtent,
  generatorRevision, materialRevision,
  editDependencyDigest, boundaryDependencyDigest,
  mesherRevision, simplifierRevision, schemaRevision
}
```

The edit dependency digest represents all authoritative edits that can influence the artifact, including its sampling/meshing halo. A scalar “last edited chunk revision” is insufficient if the artifact crosses multiple chunks. Maintain a bounded spatial dirty index or revision hierarchy so distant parent artifacts can be invalidated without loading every edited block.

Coverage is typed: generated baseline only, edits complete, pending edits, unavailable, or incompatible. A coarse mesh built from the procedural baseline while the edit layer is unavailable cannot publish as edits-complete. Retain a correctly stamped older artifact or an explicit conservative visual fallback under a declared policy. None can authorize collision, fluid capacity, mining, or visibility-sensitive gameplay as current truth.

**Acceptance:** edit at a chunk edge and four-region corner; travel beyond fine range; unload/reopen; return from the opposite direction; prove every affected parent and transition artifact invalidates. An edited dam cannot reappear as an open channel in an artifact labeled current. Measure dirty-index memory and queue age during repeated remote edits; correctness includes eventual progress.

## Integration and evidence

Merge TW-01 into source/account semantics, TW-02 into seams and scheduling, TW-03 into solver acceptance, TW-04 into the future coarse-model section, TW-05 into generation, and TW-06 into LOD. Link each requirement to an implementation owner and actual test receipt when implemented. Historical receipts remain unchanged. This study did not execute native world generation, a save/reopen scenario, or GPU acceptance.

## Primary sources

All sources accessed 2026-10-29. Current web documentation can evolve; freeze a source version when implementing a specific algorithm.

- **S1 — Clawpack, “Shallow water Riemann solvers.”** <https://www.clawpack.org/riemann/Shallow_water_Riemann_solvers.html>. Observed distinction: bathymetry treatment, well-balanced behavior, dry-state support, and tracer forms. It does not establish suitability of any solver for the voxel host or prove Deep Primal conformance.
- **S2 — AMReX, “AmrCore Source Code: Details,” Flux Registers.** <https://amrex-codes.github.io/amrex/docs_html/AmrCore.html#flux-registers>. Observed technique: reconcile area/time weighted coarse/fine interface flux discrepancies. This concerns physical AMR, not a requirement to make render LOD physical.
- **S3 — D. E. Shaw Research, Random123 official repository.** <https://github.com/DEShawResearch/random123>. Observed mechanism: counter/key addressed integer pseudorandom outputs and stateless APIs. No recommendation to silently replace the existing saved generator.
- **S4 — Voxel Tools, “Generators,” Determinism.** <https://voxel-tools.readthedocs.io/en/latest/generators/#determinism>. Observed issue: parallel multipass order and overlapping placement can affect results. The contract here is Deep Primal's proposed resolution.
- **S5 — Voxel Tools, “Smooth terrains,” Full load mode.** <https://voxel-tools.readthedocs.io/en/latest/smooth_terrain/#full-load-mode-aka-turning-off-data-streaming>. Observed tradeoff: distant data can be coarser, while full-load editing retains more edited data. Deep Primal's storage, rendering and edit strategy must be qualified independently.

[S1]: https://www.clawpack.org/riemann/Shallow_water_Riemann_solvers.html
[S2]: https://amrex-codes.github.io/amrex/docs_html/AmrCore.html#flux-registers
[S3]: https://github.com/DEShawResearch/random123
[S4]: https://voxel-tools.readthedocs.io/en/latest/generators/#determinism
[S5]: https://voxel-tools.readthedocs.io/en/latest/smooth_terrain/#full-load-mode-aka-turning-off-data-streaming

