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
