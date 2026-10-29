# Expanded replica — docs/TERRAIN-V7.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/TERRAIN-V7.md) |
| Source identity | git: docs/TERRAIN-V7.md |
| Preserved source SHA-256 | `919f4a55e45e3b47053cf1500c7e7189c13e20d05213de7e543471be0e5cfcf1` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 28 record explains the seed-phase correction, blended landform controls, larger planned channels, and richer temporary biome palettes while freezing V7 geography and retaining all older saved profiles. It separately documents opt-in development channel revisions 3–5 for deltas, oxbows, and planned discharge shares, with explicit geometric bounds and uncompleted native gates. A rendering fix constrains supported thin water surfaces without changing volume. The source distinguishes continuous planned width from rasterized wet width, static planned branch allocation from runtime flow, and limited seed/software-renderer evidence from owner-GPU acceptance.

## Research expansion — gaps and details to deepen

- Elaboration: the seed-aliasing regression is well described but should have a persistent test corpus covering more than the origin and recording both raw fields and saved seed binding.
- Clarification: revision-5 shares are geometry metadata, not live allocation; downstream APIs must prevent them from being interpreted as additional source volume.
- Elaboration: frozen revision digests and the thin-film constraint need a shared replay/visual manifest spanning released and staged profiles without conflating their acceptance.

## Research expansion — proposed contracts

- Persist generator revision, channel revision, effective terrain profile, and seed-mapping algorithm independently. Continue selecting released channel revision 2 by default unless a later explicit owner decision changes it; absent fields preserve the old revision rather than picking the newest codec value.
- Define PlannedReachMetrics with catchment proxy, continuous radius/depth, planned share, branch ID, and generation revision. Define ActualReachState separately with owned cell water, accepted transfer totals, and sampled current. Rendering or a future solver must not multiply planned shares into new supply.
- For deltas and oxbows, compile feature footprints, boundary constraints, and sample certification into immutable records. Native materialization verifies supported beds/banks and contained stages against the actual block grid, not only plan samples.
- Extend frozen-profile evidence with field hashes, native chunk/base-column hashes, saved seed values, option manifests, and affected-footprint masks. Changes outside the declared fan/oxbow footprint fail staged revision acceptance.
- Maintain thin-water rendering as a derived shared-corner reconstruction with the documented 1/256-block support separation. It cannot change collision volume, native water amounts, bed geometry, or source identity, and temporal/LOD consumers must use compatible reconstruction.

## Research expansion — acceptance checks

- Replay revisions 0–5 at fixed seeds, positive/negative seams, and save/reopen; retain frozen 0–4 outputs where promised and default revision selection.
- Inspect a dry or blocked delta after runtime edits; planned shares cannot keep branches wet or create replacement water.
- Measure actual wet cross-sections separately from twice the planned radius and report both in width comparisons.
- Repeat the 325-unit and 3900-unit stair fixtures through animation, fallback, LOD transitions, and edits; volume hashes remain unchanged while surfaces respect support.

## Research expansion — source anchors

- Changes
- Channel revisions 3–5 (staged, opt-in)
- Water rendering, including existing saves
- the revision-5 budget is planned hydraulic geometry rather than a routed discharge state

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Terrain V7 — alpha.28

V7 addresses the owner's report of narrow streams, similar-looking biomes and similar terrain across seeds. Create a **new normal world** with the built-in engine enabled to use `deepprimal:continental_v7`. Standard remains Y=-128..639; Deep remains Y=-384..639. Existing V1–V6 worlds retain their saved generator, terrain profile, water amounts and player edits, including newly explored chunks in those worlds. They do not silently switch to V7. V7 geography is frozen after this release; further geographic changes require a new saved revision.

## Changes

The previous gradient fields sampled an integer lattice at the world origin. For eight tested seeds, V6's raw height, rain and temperature at (0,0) were identical: 165.42108, 0.63967 and 0.80483. This was a real local seed-aliasing defect; the whole previous world was not seed-independent. V7 adds a deterministic, seed-derived sampling phase. All eight origins now differ in raw height and rainfall, and actual saved-world/UI-entered seed binding is checked in the packaged game.

V7 uses continuous regional blends between lowlands, rolling hills, plateau rims and folded mountain ridges. Seed-varied continent lobes sit inside drainage domains with an ocean margin. Climate has a seed-varied equator, broader rainfall variation and elevation cooling. These are authored controls on the existing terrain/drainage architecture, not a claim of complete geological simulation.

Channel radius grows faster with contributing area. Across the fixed eight-seed central-domain ensemble, median planned channel width increases from approximately 2.8–2.9 blocks to 5.1–5.8 blocks; larger reaches grow further. Width here is twice the continuous plan radius, not a guarantee that every rasterized or partly drained water cross-section has that many wet blocks. Headwater tips and finite streams can still be narrow, shrink or dry out. The new width/depth is established during generation, with no perpetual runtime refill.

Canopy density follows the selected biome. Forest/jungle trees can root on modest slopes without requiring the entire canopy footprint to be flat. Plains retain sparse trees. Badlands use red sand and shallow terracotta layers; wetlands gain mud horizons, dark forests podzol, and biome ground cover includes ferns, meadow flowers and dead bushes. Existing Minecraft biome/block assets remain temporary palettes. This is not a completed species/content catalog.

V7 inherits V6's saved creation-time profile, geological caves, river tunnels, deposits, glaciers, finite melt, dunes and bounded cave-ins. See [V6 mechanics and configuration](TERRAIN-V6.md). No deposit default, transport budget or recharge law is changed in this release.

## Channel revisions 3–5 (staged, opt-in)

Channel revision 3 adds river-mouth delta fans; revision 4 adds oxbow lakes on top; revision 5 gives each fan's distributaries conserved discharge shares and sqrt(share) widths. All three are opt-in development options for new worlds, selected by the `riverDeltas` creation setting and the `engine_deltas_standard` / `engine_deltas_deep` presets, which now save `channel_revision: 5`. Revisions 3 and 4 remain available through the generator codec (0..5) but no preset selects them. They are not part of the released alpha.28 V7 world: the default engine presets still save `channel_revision: 2`, and revisions 0–2 produce identical output. Revisions 3–4 replay exactly to frozen digests.

Where a routed trunk with at least 1,800,000 blocks of catchment reaches the ocean with its water surface no more than six blocks above sea level, the generator plans a sediment fan (at most six per 8192-block region, largest first). The fan surface rises gently inland from sea level + 0.35 to the mouth stage + 0.45, and only raises ground below that surface; it never lowers or buries higher terrain. Three distributary reaches fork from the trunk at 45% of the fan and spread to about 62% of its half-width, ending just above sea level with roughly 1–3 blocks of water. Low ground, shallow estuary and shoals inside the fan become a flat delta plain; deeper water and the open ocean are left alone.

Revision 4 adds at most 24 oxbow lakes per 8192-block region beside flat, unincised lowland trunks (catchment ≥ 160,000, incision ≤ 0.5, braid ≤ 0.05, grade ≤ 0.0035, stage ≥ sea + 1.5). Each cut-off is a tapering crescent whose still surface sits at least 1.2 blocks below the lowest sampled rim; the checks require the sampled lakes to stay within 3 blocks of the parent river stage (measured 2.25). Candidates near channels, deltas, lakes, the sea or the region edge are rejected, as are loops with more than 8 blocks of rim relief or a rim more than 9 blocks above the reach. The bed is bowl-shaped, flow is zero, and the surrounding bank rises to the lake level + 0.35. Oxbows are indexed with the existing channel/delta tiles and only rework their own footprint plus bank band.

Revision 5 splits each fan's trunk discharge between its three distributary branches instead of drawing them as equal copies. A deterministic field gives the middle branch the largest share (0.49 in the checked corpus), the side branches less (down to 0.24), and the three shares sum to one; no branch gains water the trunk does not carry. Branch width follows sqrt(share) around the revision-4 capacity-fair width, so the dominant branch is the widest (measured widest/narrowest 1.27) and its bed is correspondingly deeper through the hydraulic-geometry depth rule. Branch mouths must stay wet, seaward-flowing and within the delta depth limit, and nothing outside the fan footprint changes; revisions 3 and 4 replay exactly against frozen digests.

Limitations: geography remains frozen, so the fan does not prograde into `mask < 0` ocean; there is no avulsion or anastomosing behaviour, oxbows are static cut-offs rather than simulated meander migration, and the revision-5 budget is planned hydraulic geometry rather than a routed discharge state or a time-varying allocation. Braided reaches still use a single implicit discharge. Core checks verify determinism, frozen revisions 3–4, fan bounds, wet and contained distributaries, oxbow containment, conserved and width-ranked distributary shares, and unchanged terrain outside oxbow and fan footprints; native generation, save/reopen and visual acceptance for revisions 3–5 are not done.

## Water rendering, including existing saves

A corner average could pull a thin upper water layer below its solid bed when its downhill neighbor occupied the layer below. The mesher now applies a shared-corner support constraint, keeping a supported film at least 1/256 block above its floor. Both animated water and the smooth fallback use it. The shared-corner rule preserves independently meshed page agreement; it does not add water, place sources, alter the riverbed or prevent real drainage.

Matched native stair fixtures contain the same 325 units (thin) or 3,900 units (fuller), identical block-state hashes and plains biome in alpha.27 and alpha.28. The previous package shows clipped dry-looking strips; the candidate exposes the supported thin surface. This diagnoses one cause of the reported appearance, not every possible water artifact. Small visual flecks, transparent overlaps, distant page/LOD coverage and temporal behavior across broad editable worlds still need separate acceptance. A static frozen fixture is not a live hydrology endurance test.

## Compatibility and limits

The release retains finite environmental water accounting. Streams may exhaust; rainfall/recharge can feed suitable locations elsewhere under the existing rules. There is no unlimited source conversion or flood-to-fill operation. Installing the JAR does not widen rivers in old worlds or regenerate player edits.

[Validation](VALIDATION-0.0.1-alpha.28.md) records the exact packages, native tests and rejected runs. These checks cover eight core seeds, four native seeds, Standard/Deep save/reload, an actual Create World flow and software OpenGL/Vulkan views. They do not establish every-seed quality, long-duration storm/drought balance, large-region hydraulic continuation, finished LOD/transparency, Windows GPU execution or FPS guarantees. The mountain view is limited by the short test render distance; the river view includes a broad mouth. Those screenshots are illustrative, not comprehensive landscape acceptance.


<!-- END PRESERVED SOURCE -->
