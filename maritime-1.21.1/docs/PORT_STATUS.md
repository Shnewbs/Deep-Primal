# Port status

Status: **buildable 1.21.1 alpha; dedicated-server and client world smoke checks passed**.

## Recovered input

- Original artifact: `TFCMaritimeTravelers-1.20.1-0.0.4..jar`, 2,161,678 bytes.
- SHA-256: `7dc148b8ba2caacf48eaa4aba95f8fd61a580de7d9c47c1dcc1106370f9cf975`.
- Original target: Minecraft 1.20.1 / Forge 47+, TFC 3.x.
- 293 class files reconstructed into 200 top-level Java sources; 1,366 original asset files and 724 original data files inspected.
- Original version strings disagree between filename, manifest and mod metadata; the hash identifies the input.

## Migration implemented

- Independent Java 21 / NeoForge 21.1.264 module, TFC 4.2.12 and Patchouli 92 development runtime.
- Deferred registries, armor material holders, component-based food/custom data, registry-aware block entity persistence, item/fluid/heat capability registration, block interactions and client rendering migrated to the current APIs.
- Six NeoForge payloads with explicit codecs and direction. Machine control rejects invalid players, distant/unloaded targets and targets outside the player's valid open container.
- Grinder, expeller and smoke-shack recipes use map codecs and stream codecs. TFC 4 anvil/forging APIs and inventory access migrated.
- Exploration lamp uses the TFC fluid-container component and item fluid capability. Barrel/lamp contents use block entity components instead of legacy item NBT mutation.
- Rendering uses current vertex APIs, normalized UVs and standalone model IDs. Curios renderer registered independently of Barrels 2012.
- Required TFC mixins ported; optional cart and dynamic-light mixins guarded by mod presence. Cart access and VS Minecraft access use isolated adapters.
- Datapack directories, item stacks, tags, advanced crafting remainders, lamp fuels, food and heating migrated. Fresh-food ingredients now use TFC's `and` predicate; retaining the old wrapper would match unrelated items in TFC 4.
- Optional recipes, loot, custom TFC data and external tag entries are guarded. A scoped DataManager mixin evaluates conditions only for this mod's custom data.
- Multi-output axle, brace and duct recipes retain their yields using TFC's four-item heavy stack limit. Ground meat and sausages have heat definitions.
- Shared hammer running-state property corrected to prevent a pneumatic hammer tick crash.

## Completed validation

`build`, `prepareServerRun` and `prepareClientRun` complete successfully. The jar contains the reconstructed 1.21.1 classes, current metadata, original visual assets and the migrated datapack.

A dedicated server with only NeoForge, TFC, Patchouli and this port boots successfully. TFC data self-tests pass at startup and after `/reload`. The tested datapack has no Maritime recipe, custom-data or missing optional-tag load errors.

Twenty-two representative blocks were freshly placed in a loaded test area: boiler, grinder, expeller, compressor, both tanks, all three engine variants, both hammers, exploration lamp, oak item barrel, oak smoke shack, oak mast, axle, brace, both propeller sizes, oak paddle and both telegraphs. They ticked without a fatal error, `save-all flush` completed, the existing test world reopened, and the server shut down normally.

These are placement, tick and persistence smoke checks, not proof of every processing recipe, GUI control, pressure/load edge case, inventory transfer or animation. Multiplayer synchronization and a full optional-mod matrix remain unverified.

A client with the same minimal mod set joined a copy of the saved world. The machines and original textures rendered in-world without a port exception or missing-model error; TFC data self-tests also passed on the client. Optional integrations were absent, exercising the guarded client-loading path. The headless environment had no audio device.

## Remaining acceptance work

1. Exercise each machine GUI and equipment interaction.
2. Operate each machine with fuel, heat, fluids and inputs; verify outputs, automation, failure states and item pickup/drop preservation.
3. Test real multiplayer container controls, chunk unload/reload and long-running pressure/load behavior.
4. Test Create, JEI, Curios, cart and dynamic-light integrations with matching 1.21.1 mods installed.
5. Validate ship assembly, propulsion, load feedback, sails and rigging with an actual compatible 1.21.1 Valkyrien Skies backend. The core compile API is not a working backend; absence or incompatible physics-listener APIs disable that integration.
6. Plan saved-data migration and adapters before integrating into Deep Primal.

No public release or CurseForge upload has been made. Original WIP behavior and incomplete upstream features remain part of the recovered baseline.

## Deep Primal boundary

The branch keeps TFC as a required dependency and preserves saved registry names. Later integration should separate steam/pressure and mechanical-load logic from heat/fuels, recipes, food, climate and ship adapters. Deep Primal's main build is unchanged.
