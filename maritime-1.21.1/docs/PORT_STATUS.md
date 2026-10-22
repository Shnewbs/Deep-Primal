# Port status

Status: **source reconstruction and API migration in progress; not playable**.

## Verified baseline

- Uploaded artifact: `TFCMaritimeTravelers-1.20.1-0.0.4..jar` (2,161,678 bytes).
- SHA-256: `7dc148b8ba2caacf48eaa4aba95f8fd61a580de7d9c47c1dcc1106370f9cf975`.
- Original target: Minecraft 1.20.1 / Forge 47+, TFC 3.x.
- 293 bytecode classes, reconstructed into 200 top-level Java source files.
- 1,366 original asset files and 724 original data files inspected.
- Original metadata is inconsistent: mod metadata says `1.0.0`, the manifest says `0.0.0-indev`, and the supplied filename says `0.0.4`. Provenance identifies this input by hash rather than relying on those versions.

## Scope recovered from the jar

| System | Recovered implementation | 1.21.1 validation |
| --- | --- | --- |
| Steam and heat | Boiler, cast-iron and steel tanks, pressure, bleeders, fuel, explosions and copper connections | API migration incomplete |
| Engines and rotation | Small and large single-piston engines, triple-expansion engine, load tolls, axles and braces | API migration incomplete |
| Propulsion | Copper/brass propellers and wood variants of water paddles; ship force listeners | VS API/backend compatibility incomplete |
| Processing | Grinder, expeller press, mechanical and pneumatic power hammers | Recipe codecs migrated; machine and anvil validation incomplete |
| Preservation and storage | Item barrels, braces, racks, salting, honey preservation and smoke shacks | Food registry and component migration started; save/load checks pending |
| Ship fittings | Masts, yards, booms, cleats and rigging links | Source and rendering migration started; interaction/save tests pending |
| Exploration | Diving suit, air compressor, hoses, exploration lamp, sounding line, knot meter and wind gauge | Component and equipment API migration incomplete |
| Optional integrations | Create, JEI, Curios, AstikorCarts, Barrels 2012, VS, VS Sails, icebreakers and dynamic lighting | Separate validation required for each target dependency |

## Changes in this branch

- Independent Java 21 / NeoForge 1.21.1 build module, with TFC 4.2.12 pinned and checksum verified.
- Minecraft SRG members restored to readable names, Forge registration converted toward NeoForge deferred holders, and TFC component package migrations applied.
- Six packets rewritten as NeoForge payloads with explicit directions and stream codecs. Server controls reject dead/spectator players, distant targets, and unloaded chunks.
- Grinder, expeller and smoke shack serializers replaced with map codecs and network stream codecs.
- Block entity persistence changed toward registry-aware signatures; inventory/fluid/heat providers moved toward capability registration events.
- Item custom-data reads now return copies and mutations replace components; food traits use deferred registry entries.
- Renderer vertex builder/model-ID changes and sprite UV normalization migrated; menu registration moved to its dedicated event.
- Recipe, loot and tag directories updated; item-stack outputs, common tags and TFC data paths migrated.
- 40 duplicate data definitions consolidated. 684 JSON files parse successfully, including 64 recipes made conditional on their optional mod dependencies.

## Validation and remaining blockers

The NeoForge Minecraft development artifacts were prepared successfully. The latest completed full source compile produced **283 errors and 12 warnings**; the branch does not currently produce a verified jar. JSON parsing is a structural check, not a successful Minecraft datapack reload.

Remaining work includes:

1. Finish armor material holders, armor model registration, item fluid components, tooltip signatures, and NeoForge tick events.
2. Finish TFC 4 anvil/forging access, heat/fuel accessors, container slots, save/update methods, rotation interface calls, and block interaction overrides.
3. Resolve the exact 1.21.1 dependencies/API adapters for VS world access, AstikorCarts, VS Sails, Barrels 2012 and dynamic lights. The VS core compile API is not a working 1.21.1 ship backend.
4. Finish ported mixins and conditionally register them. The old 1.20.1 mixin configs are intentionally not activated as if they were compatible.
5. Compile the complete source, then boot a dedicated server and a client. Validate registry and recipe reloads before gameplay checks.
6. Check every device's placement, interaction, GUI, automation, chunk unload/reload, item pickup/drop preservation, rendering and network synchronization. Test optional integrations both installed and absent.

No client/server gameplay test has passed for the port. No CurseForge upload or release has been made.

## Deep Primal integration boundary

This branch keeps TFC as a required dependency for the port. Later integration should extract the steam/pressure and mechanical-load logic into a small module, with adapters for heat/fuels, recipes, food, climate and ship forces. That separation should follow a working baseline port, with saved-data identity and migration reviewed before namespace changes. Main-branch Deep Primal remains outside this module's build.
