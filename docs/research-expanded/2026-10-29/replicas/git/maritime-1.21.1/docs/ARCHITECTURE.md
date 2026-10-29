# Expanded replica — maritime-1.21.1/docs/ARCHITECTURE.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/maritime-1.21.1/docs/ARCHITECTURE.md) |
| Source identity | git: maritime-1.21.1/docs/ARCHITECTURE.md |
| Preserved source SHA-256 | `0115086a6efeb7b85c591626782711bd48f97c0a3c361bc227550a5bbe786180` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Maritime architecture note maps steam pressure, rotation, food, equipment, ships and interfaces to their current TFC or optional physics host responsibilities. It proposes extracting deterministic pressure/load math while retaining platform adapters, stable IDs and independent tests before balancing. Its table already recognizes server container ownership and the need for a real physics listener. This is a useful boundary sketch for the independent 1.21.1 port, not a statement that the root 26.1.2 engine has adopted Maritime machinery or that portable math alone constitutes a standalone game runtime.

## Research expansion — gaps and details to deepen

- Each domain needs explicit units and clock semantics. Heat, pressure, torque, angular velocity, fuel quantity and food decay are not interchangeable resource accounts, and copying host values without a declared conversion can produce plausible but wrong behavior. Ship-local and world coordinates require equally explicit transforms and invalidation when a structure moves or unloads.
- The current mapping should become interfaces with ownership and failure rules: who samples host state, who applies a command, what thread executes a physics callback, and what happens if a dependency disappears or a world closes. Server-owned menus also need stale-container and item-transfer revision checks beyond visual UI layout.

## Research expansion — proposed contracts

- Define immutable simulation inputs and outputs with named units and timestamps, then keep native TFC calls, block/entity access and physics force application inside adapters. Document which host behavior is intentionally retained, which portable solver replaces it and how double application of heat or force is prevented.
- Add lifecycle and persistence contracts for machinery networks and ship links: stable owner ID, coordinate frame, membership revision, load/unload attachment, queued callback rejection and recovery after missing counterparts. Keep adapters independently optional and preserve original registry identities before designing an explicit save migration.

## Research expansion — acceptance checks

- Run deterministic pressure/load fixtures across tick rates and input order using declared units, then compare real TFC adapter behavior at selected operating points. Check fuel, inventory and energy accounting without assuming every physical quantity obeys the same conservation equation.
- Unload or move linked machinery during callbacks, stop the world with work pending and test stale server containers. Require operational ship-force evidence with the actual physics backend; successful API loading and isolated core math must remain separate from that integration claim.

## Research expansion — source anchors

- Recovered systems and integration seams

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Recovered systems and integration seams

Source root: `src/main/java/tfcmt/tfcitembarrelsandmore/`. The registry namespace remains `tfc_item_barrels_and_more` to preserve item/block identities.

| System | Main implementation areas | Coupling to keep visible |
| --- | --- | --- |
| Steam and pressure | `common/SteamEquilibriumModel`, `PistonPressure`, boiler/tank/engine block entities | TFC heat, fuel and fluid capabilities; configuration; world ticking |
| Rotation and processing | Axles, grinder, expeller, power hammers; `common/recipes` | TFC rotation networks, recipe caches, forging and inventory rules |
| Food and storage | Item barrels, smoke shacks, food traits and preservation handlers | TFC food components and decay, sealing, salt/honey, loot and item data |
| Exploration equipment | Diving armor, compressor, air hose, lamp, sounding line, knot meter and wind gauge | TFC climate/fluids, equipment state and player events |
| Ship fittings | Mast anchors/fillers, yards, booms, cleats and client mast renderers | Saved structure/link data, world positions, VS/Sails integration |
| Ship propulsion | `compat/valkyrienskies` | External ship world transforms, physics listeners, force/load feedback |
| UI and multiplayer | `client/screen`, menus and network payloads | Current container ownership, server-authoritative controls and synchronized data |

The port changes the platform boundary while retaining the recovered machine logic. It does not claim to redesign the original pressure model or finish upstream WIP features.

For Deep Primal, preserve the current IDs until a deliberate migration is designed. Extract deterministic pressure/load calculations first, then expose small adapters for heat/fuel, recipes, inventories, food, climate and ship forces. Keep external mod classes behind adapters that are loaded only when their mod is present. Test the independent mod before changing balance or replacing TFC dependencies; otherwise regressions become difficult to attribute.


<!-- END PRESERVED SOURCE -->
