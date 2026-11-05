# Expanded replica — docs/PHYSEX-REFERENCE-2026-10-21.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/PHYSEX-REFERENCE-2026-10-21.md) |
| Source identity | git: docs/PHYSEX-REFERENCE-2026-10-21.md |
| Preserved source SHA-256 | `b8b4d571c52bdd042a7b5721c0920283c6b7a4cf2e835594eda7324fbc11e3fa` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 19 comparison records a specific PhysEx Reborn source revision and distinguishes its reference behavior from Deep Primal's independent sixteen-unit liquid system. It reports finite gravity/lateral transfers, bounded connected-pool pressure, paired writes, a 144-unit U-bend fixture, and numerical pair checks. It already lists missing displacement, hollow-block storage, transactional machine ports, larger pressure networks, environmental supply, and erosion. Explicit per-level/tick and per-search limits are supplied. The document requires matched workloads and refuses to equate a feature name or finer volume quantization with demonstrated parity.

## Research expansion — gaps and details to deepen

- Elaboration: bounded search counts are stated, but truncated-search continuation, fairness between connected pools, and progress evidence are left to the next requirement.
- Elaboration: failed destination writes restore the source, but concurrent terrain edits, interception, and rollback with composition need a broader transaction contract.
- Clarification: this historical sixteen-unit comparison must remain separate from later environmental-account implementation and from features merely listed by the reference author.

## Research expansion — proposed contracts

- Define pressure-job identity from world epoch, connected-component candidate, geometry/fluid revisions, visited frontier, and work cursor. Hitting 128 wet cells suspends or limits a qualified local solve; it must not claim a complete basin equilibrium.
- Persist or safely reconstruct bounded continuation metadata and age requests fairly under the 4096 pressure-cell visit allowance. Record useful component progress and maximum pending age instead of raising the scan limit to conceal starvation.
- Before paired transfers, validate donor amount, destination free capacity, source/destination revisions, and admitted composition. Commit both amounts before notifications; interception or failure either restores the whole transaction or leaves a recoverable named pending state.
- Specify pressure eligibility separately for enclosed communicating vessels and exposed routed rivers. A sloping open channel must not be globally leveled merely because all its wet cells are connected.
- Create a comparison manifest with world shape, active wet cells, supported obstacles, resolution, load radius, scheduler limits, build revision, and exact metric definitions. Reference feature coverage and Deep Primal runtime evidence occupy separate columns.

## Research expansion — acceptance checks

- Repeat the 144-unit U-bend at a chunk edge, then block the passage during an in-flight solve; no stale transfer or volume loss is allowed.
- Run a network longer than the search cap alongside small pools; all admitted components must complete useful work without monopolizing the tick.
- Inject destination rejection and external block replacement between validation and publication; donor, receiver, and species ledgers recover coherently.
- Compare open descending channels and sealed pipes using the same starting volumes to verify the intended pressure classification.

## Research expansion — source anchors

- Implementation and remaining work
- Comparison gates
- The native U-bend fixture conserves **144 units**
- at most 128 wet cells per search

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# PhysEx inspiration and the independent Deep Primal liquid system

Owner direction, 2026-10-21: reference mods are inspiration and comparison targets. Deep Primal owns its terrain, liquid and LOD systems and aims to improve on the references through integrated behavior and measured performance. A matching feature name does not prove parity or superiority.

## Reviewed reference

- [Author's PhysEx description](https://www.curseforge.com/minecraft/mc-mods/physex).
- [PhysEx Reborn source](https://github.com/Sasai-Kudasai-BM/PhysEx-Reborn), reviewed tree `1bf0a6e10fb5f8ba8730b97afdb7917e1b4510dc`.
- `ClassicFlowTask.java`, blob `84fb355f876c6665fc417690e3474fe100fc01f5`: gravity, lateral finite transfers and scheduled equalization.
- `EqualizationFluidTask.java`, blob `d35e2e978b2d50f3d28123080be06e94fd7f7346`: bounded connected-fluid traversal and volume redistribution through height layers.
- `gradle.properties`, blob `98b293128ba6e4c1c71dbafe747b517eef59f84a`: Fabric / Minecraft 1.21.11, mod 2.0.0. It is not a NeoForge 26.1.2 drop-in component.
- Its GPL-3.0 source is reference material here. No PhysEx classes, assets or runtime dependency are included in Deep Primal. Deep Primal's pressure implementation extends our pre-existing independently authored finite-water head/transfer approach to the 16-unit engine path.

The author's description includes finite flow, block/piston displacement, consuming fluid with bottles/farmland, fluids in hollow blocks, dripstone behavior and lava evaporation. Block physics is described as future work; it must not be counted as an already delivered feature of the reference.

## Implementation and remaining work

| Capability | Deep Primal alpha 19 | Next requirement |
| --- | --- | --- |
| Finite volume and gravity | Independent `Fluid`, 16 saved units per cell, conservative downward/lateral transport; native water imports into engine worlds | Broader dynamic-world conservation and scheduling stress cases |
| Connected-pool pressure | Connected wet-cell search finds free surfaces and reduces hydraulic-head differences, including raising water through a submerged U-bend | Larger connected networks, fair continuation and basin-wide planning rather than increasing per-tick scan limits |
| Work and loading bounds | 2,048 fluid ticks and 4,096 pressure-cell visits per level/tick; at most 128 wet cells per search; loaded-only sampling and notifications | Quantified frame/tick cost, saturation and long-network update latency |
| Paired writes | Source and destination volumes are written before neighbor notification; failed destination writes restore the source | Wider compatibility with external block-change interception |
| Smooth presentation | Shared-corner curved surfaces, palettes, stored currents and existing offshore swell | Stable transparency/LOD joins, shoreline breaking surf and flow-driven presentation |
| Block/piston displacement | Unfinished for the independent 16-unit fluid | Reserve destination capacity before replacing/moving a wet cell; conserve retained volume on failure |
| Hollow blocks and containers | Waterlogged solids remain outside the independent storage path; full-volume buckets are implemented; legacy machine ports are not an audited 16-unit integration | Collision-shaped storage, measured containers and transactional machine ports |
| Lakes, groundwater and supply | Generated finite cells plus local transport/pressure | Shared lake level/spillway contracts, catchment inflow, recharge/depletion and deliberate water budgets |
| Erosion and other liquids | Unfinished | Stable sediment/material exchange, conserved lava and temperature interactions |

The native U-bend fixture conserves **144 units**, raises the lower free surface and stops exchange when the passage is blocked. The core checks cover all legal volume pairs across 17 relative elevations, with no head overshoot. These results validate the new local behavior; they do not establish full PhysEx coverage or solve generated basin geometry.

## Comparison gates

Compare equal active volumes, obstacles, resolutions, loaded area and tick budgets. Record conservation error, settling time, update latency under saturation, CPU tick percentiles, allocations and memory, then repeat after save/reopen and chunk-border unload/reload. Larger feature counts or sixteen units instead of eight are not substitutes for those results. Keep water simulation independent of graphics/LOD and the optional GPU backend.


<!-- END PRESERVED SOURCE -->
