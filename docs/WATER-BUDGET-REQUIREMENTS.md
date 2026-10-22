# Water budgets and a persistent world

Owner concern, 2026-10-22: many replenishing meltwater sources could progressively flood a long-running world, including raising ocean water. This is a required design problem, not a solved alpha-21 feature.

## Current implementation

An independent liquid cell holds at most sixteen finite units. Gravity, local routing and pressure transfer existing volume; a full cell is not a replenishing emitter. There is no runtime glacier-melt emitter or complete native ocean reservoir/water-cycle ledger. Generation initializes water in newly generated terrain. Loaded simulation seals unloaded borders and does not simulate the whole planet.

Alpha 21 smooths presentation and improves local work scheduling. Neither client interpolation nor a faster transfer step supplies new water. Existing conservation fixtures cover finite flows, including 144 units in a communicating vessel and a 32-unit basin under competing work.

## Required accounting

Every emitter needs an explicit backing store, a bounded discharge rate, and a debit for accepted output. A finite ice/snow store can lose only the amount it contains. Renewable recharge must name its source and timescale. Rainfall, snowfall, groundwater, extraction, runoff and ocean exchange must not each create an independent copy of the same water.

For each accounted region, storage change equals inputs minus outputs. A source that adds net water indefinitely to a finite closed region necessarily increases stored volume; calling it glacial does not prevent flooding. Evaporation must transfer water into an atmospheric account rather than act as an unexplained deletion or guaranteed balancing amount.

Connected catchments need shared tributary discharge, lakes with controlling sills/outlets, downstream capacity and ocean handoff. Inland flooding remains a valid result when inflow exceeds outflow. A processing limit must delay accounted work rather than silently delete volume.

Unloaded boundaries need persistent transfer records or region-level storage. They must neither force generation/ticking of arbitrarily distant chunks nor regenerate source volume on reload. Define pause/unloaded/offline rules consistently with the owner's host-game/TFC timing direction before implementing recharge.

## Proposed ocean policy — not implemented

Use an accounted ocean reservoir with a prescribed gameplay sea level. Rivers deposit into its account rather than continually stacking fluid blocks over loaded ocean surfaces. Later rain/snow/recharge draws from the wider circulation budget. This is an explicit hybrid boundary model, not a fully physical global ocean simulation.

Global sea-level change would be a separate opt-in design, with its own storage/area relationship, terrain and save implications, performance limits and tests. Do not introduce it accidentally through local source blocks. The owner has raised the flooding constraint; exact reservoir/recharge rules still need the later hydrology/climate design phase.

## Acceptance requirements

- Long-run closed-box conservation and bounded source depletion; no copied water at branch splits.
- Multiple glacier/catchment sources, lake overflow and inland flood cases with explicit budgets.
- Ocean inputs and atmospheric return accounted over repeated cycles, without progressive block flooding under the prescribed-level policy.
- Loaded/unloaded/reload/restart transitions without duplication, deletion or forced remote chunk creation.
- Separate cosmetic puddles, surface wetness and wave displacement from physical stored volume.

See the [glacial landscape plan](GLACIAL-LANDSCAPES.md), [reference completeness audit](TERRAIN-WATER-SNOW-LOD-AUDIT-2026-10-22.md), and [stream/canopy/puddle review](STREAMS-CANOPY-PUDDLES-AUDIT-2026-10-22.md). Environmental implementation remains deferred.
