# Configuration, flexible ore deposits and visual authoring

**Owner direction, 2026-10-21 PDT:** expose useful ore/world settings through configs, datapacks and optional KubeJS integration, with TFC/GregTech-style authoring flexibility. All Deep Primal user configuration belongs beneath a deepprimal directory, organized into domain subdirectories. An editor outside Minecraft is a **proposed companion tool**; its packaging/toolkit and release scope are not yet selected.

This is the design contract, not a completed config migration, shipped ore API or working external editor. Current source baseline: **ca5d03f2484c75d5d92e9525c97d0a27f96edc0e**, alpha 21 runtime.

## Current source and intended boundary

The root DeepPrimal initializer still registers flat filenames for worldgen, river rules and diagnostics. GraphicsSettingsModule registers flat client-performance, water-visual and LOD filenames; PerformanceModule registers the common performance file. Existing KubeJS registry-context compatibility does not implement the ore authoring API described below.

Preserve module ownership: platform-neutral core schemas/compilation and deterministic planning; root host integration and server authority; optional KubeJS bridge; graphics/LOD/Vulkan boundaries retained. A standalone editor consumes shared schemas and portable preview functions instead of adding a desktop toolkit to the required game runtime.

## Configuration directory policy

All Deep Primal-owned user config paths use this domain layout beneath the appropriate scope root:

| Scope | Root and responsibility |
| --- | --- |
| Instance/common defaults, client settings and server defaults | `<instance>/config/deepprimal/` — applies to the selected game/server instance; individual files retain their actual registered config type. |
| Optional world/server overrides | `<save>/serverconfig/deepprimal/` — same domain layout, for server-authoritative world overrides supported by the host. Client appearance settings do not become world gameplay authority. |
| Pack-distributed default templates, when supported | `<instance>/defaultconfigs/deepprimal/` — mirror domain paths and verify target-version template behavior rather than assuming it. |
| Editor preferences/profiles | config/deepprimal/ui/ and config/deepprimal/presets/ within the selected instance. Portable profile export is an explicit user-selected operation. |

Current [NeoForge 26.1 config documentation](https://docs.neoforged.net/docs/misc/config/) distinguishes CLIENT/COMMON/STARTUP/SERVER ownership and world serverconfig overrides. Verify nested registration paths, file discovery and sync against the actual pinned target before migration. Directory organization must not turn an unsynced common/client file into authority over multiplayer rules.

### Domain layout

Paths below are relative to config/deepprimal/ or the equivalent world override root. Names are the intended schema/layout, not a statement that every file already exists.

| Directory | Example files / settings |
| --- | --- |
| core/ | settings.toml, modules.toml, vanilla-content.toml; schema/preset selection and feature state. |
| worldgen/ | terrain.toml, geology.toml, ores.toml, biomes.toml, water.toml, climate.toml; generation rules/scales and creation-time choices. Climate numerical design still follows the biome roster. |
| materials/ | rocks.toml, soils.toml, wood.toml, building.toml; supported per-ID properties/toggles and appearance tuning. |
| survival/ | inventory.toml, needs.toml, calendar.toml, temperature-clothing.toml, death.toml. |
| ecology/ | food-farming.toml, fauna-threats.toml and supported vegetation/ecology settings. |
| technology/ | metallurgy-heat.toml, ore-processing.toml, power.toml, signals.toml; panning/sluice/processing scales remain separate from ore generation. |
| hazards/ | stability.toml, fire.toml, gas.toml, explosives.toml; accepted cave-ins remain distinct from excluded sinkholes. |
| graphics/ | quality.toml, water.toml, lod.toml, backend.toml; preserve optional graphics/LOD/Vulkan ownership. |
| performance/ | client.toml, server.toml, diagnostics.toml; scheduling/memory/work and diagnostic controls. |
| integration/ | adapters.toml, kubejs.toml and supported optional compatibility choices. |
| multiplayer/ | rules.toml, permissions.toml and supported cooperation policies. |
| ui/ | client.toml, guide-inspection.toml, editor.toml; HUD/accessibility/audio/editor preferences. |
| presets/ | primal.toml, hardcore.toml, high-tism.toml and named user profiles. |

Define one canonical owner for each key. A worldgen water file and a graphics water file have different purposes; neither should duplicate the other's authoritative values. Descriptions must identify scope, units, defaults, ranges, dependencies, restart/reload policy and requested versus effective values.

Content datapacks, KubeJS source scripts, world save state and runtime caches retain their host-required locations and ownership. They are not duplicate configuration roots. The editor may manage a generated pack or script export through its explicit target, while all Deep Primal settings/preferences stay under deepprimal.

### Migration of current flat files

- [ ] Inventory each registered file and any standalone module preference sidecars. Map it to a canonical domain path and preserve the original CLIENT/COMMON/STARTUP/SERVER scope.
- [ ] Migrate validated values with a backup and version marker, including per-world overrides/templates. Preserve user overrides, unknown keys where safe, and module-absence behavior.
- [ ] If old and new files coexist, do not overwrite a populated new file with an old one. Show/report the conflict, use documented canonical precedence, and retain recoverable copies.
- [ ] Leave one authoritative live file per scope/key after migration. Historical alpha instructions may still name old files; update current play/install documentation when migration ships.
- [ ] Validate Windows/Linux path handling, dedicated server, single-player worlds, read-only instances, missing optional modules, restart/reload and multiplayer sync before marking migration complete.

## Flexible ore/deposit definitions

TFC's [vein documentation](https://terrafirmacraft.github.io/Documentation/1.21.x/worldgen/features/veins/) demonstrates multi-chunk shapes, host replacements, occurrence/density, depth/projection and indicators. [GTCEu Modern authoring](https://gregtechceu.github.io/GregTech-Modern/1.20.1/Modpacks/Ore-Generation/01-Customizing-Veins/) demonstrates adding/modifying/removing definitions and filtering/tuning generation. These are behavior/API references; no dependency, copied API syntax or current-target port compatibility is implied.

Deep Primal needs its own versioned deposit schema interpreted by its own geology planner:

| Property group | Required controls |
| --- | --- |
| Identity | Namespaced ID, schema version, enabled state, source/provenance, seed salt and module dependency. |
| Geological eligibility | Host rock IDs/tags, geological unit/contact/fault context, replacement maps and dimension eligibility; optional biome/tag filters without a high-elevation ore ban. |
| Distribution | Candidate occurrence/spacing, relative selection weight, clustering and allowed overlap. Keep occurrence probability separate from relative weight and internal block density. |
| Geometry | Native generators for clusters, lenses/sheets, discs, pipes and geological veins/contact-bound forms as implemented; size/radius, thickness, orientation/dip, branching/taper and variability where supported. |
| Vertical rules | Explicit absolute-height versus depth-below-surface or geological-unit-relative ranges/distributions. Do not interpret every range as the same coordinate system. |
| Contents and grade | Multiple associated ores/gems/gangue, weighted constituents, poor/normal/rich zoning, inclusions/pockets and appropriate recovery properties. |
| Discovery | Surface/underground indicators, exposure behavior, outcrops and applicable prospecting metadata. |
| Reserves | Finite ordinary deposits and separately defined high-reserve bedrock nodes, with capacity/yield/depletion and any explicit recharge policy. Existing default finite-ore decisions remain. |
| Compatibility/tuning | Per-ID add/replace/patch/remove, tags, preset/global multipliers and optional host adapters; no need to edit Java for supported data fields. |

The natural default uses coherent host geology, varied grade and scarce rich zones. Expert pack overrides may deliberately change that behavior, but the effective definition and override provenance must be inspectable. Disabling a deposit definition stops eligible future placement; it does not unregister existing blocks or refill/deplete previously mined nodes automatically.

### One schema, several authoring routes

- **Datapacks:** primary complete content definitions, tags and overrides. Proposed custom-data directory: data/<namespace>/deepprimal/ore_deposits/. The loader/codec contract must be implemented before this path is advertised as working.
- **Configs:** broad scales/toggles and supported per-ID tuning patches under worldgen/ores.toml, not a second unbounded content language duplicating every datapack field.
- **Optional KubeJS:** deterministic add/modify/remove and recipe/prospecting hooks against the same typed definitions, with documented lifecycle and exact supported versions. Do not claim TFC/GTCEu script syntax works unchanged in Deep Primal.
- **Visual editor:** edits those same config/definition formats and exports a generated override pack. It must not invent a separate GUI-only ore model.

Proposed resolution order:

1. Resolve bundled definitions and enabled datapacks by normal pack priority.
2. Apply supported KubeJS definition transforms at the documented preparation phase.
3. Apply config tuning/per-ID patches, module enable policy and authoritative server constraints.
4. Validate all effective definitions, report each value's provenance, and compile immutable native planning data.
5. Freeze the relevant generation version/hash for a world or an explicit new-generation epoch before chunks consume it.

Config knobs therefore have a documented relationship to scripting/pack overrides. Reject invalid IDs, empty replacement rules, impossible ranges, negative/non-finite weights and unsupported generators before worker generation. Basic scalar values may use an explicitly documented clamp policy; structural errors must not silently rewrite an author's deposit.

Run scripts/JSON processing during bounded preparation, not once per voxel. Workers consume compiled data with independent per-deposit seeds and stable candidate ownership. Query radius and cache costs must remain bounded even with huge custom deposits. GTCEu's [generation/cache discussion](https://gregtechceu.github.io/GregTech-Modern/1.21/Development/General-Topics/Ore-Generation/) is a useful reference for separating planning from chunk-local placement; our implementation must prove its own determinism and budgets.

Existing worlds keep their generation contract. Ore-shape/distribution changes are new-world changes by default; any supported new-chunk epoch requires an explicit seam/candidate policy. Never silently retrogen ores, alter saved geology or refill depleted deposits on reload.

## Proposed companion editor outside Minecraft

Feasible direction: a separate desktop app or locally hosted browser UI that works when Minecraft is closed. Toolkit, platform packaging, distribution and maintenance scope remain a proposal. It must not require cloud accounts or a running Minecraft client just to edit ordinary settings.

The shared schema/tunables registry is the contract for the mod, in-game screens, external editor, generated reference and validation tools:

- Searchable domain navigation matching the directories; Basic/Advanced/Expert views and presets.
- Sliders paired with exact numeric entry, units, typed ranges, dropdowns, toggles and ID/tag selectors. Use suitable ranges/scales for rarity/size; do not make sliders the only way to enter a value.
- Show defaults, effective value, origin, dependencies, server locks and restart/new-world restrictions before Apply. Staging, Revert, per-key/domain reset, undo, a change diff and profile import/export are required.
- Ore editor with weighted composition/grade controls, host-rock selectors and seeded cross-section/3D previews. Share portable native sampling functions where possible; label approximate previews and verify them against actual native generation.
- Configuration for all supported domains, including terrain, water, survival, graphics, performance and integrations, not only ores. Complex scripts and unsupported plugin definitions can remain inspected/source-edited rather than silently flattened.
- Offline validation of schema/ranges with an optional registry manifest exported from the actual modpack. If the tool cannot know a runtime-defined ID, mark it unverified instead of pretending the whole pack has been validated.
- Atomic writes, backups, target-instance/world selection and concurrent-edit detection. Preserve comments/unknown data where supported and never rewrite arbitrary user JavaScript to simulate round-trip support.
- Dedicated-server profiles and read-only client-synced views; the tool cannot grant client authority over a remote server.
- Ship the tool separately/optionally so GUI frameworks and preview caches do not inflate the required game runtime. Keep preview jobs cancellable and cap CPU/memory; export a portable preset/pack rather than bundling a complete world.

The existing in-game graphics screen and localhost diagnostic dashboard are not this offline companion editor. A first version can cover schema-driven tuning and ore definitions before adding more sophisticated terrain preview.

## Implementation and acceptance work

- [ ] Define central tunable metadata, domain/scoped paths and a complete legacy migration manifest; verify real host path/sync semantics.
- [ ] Implement one deposit codec/schema, native compiler and deterministic planner with cross-chunk candidate ownership, bounded costs and saved generation identity.
- [ ] Implement datapack/config routes and exact override diagnostics; then add the optional versioned KubeJS bridge.
- [ ] Verify the same intended effective deposit from equivalent pack/config/script authoring, and explain differences when override precedence intentionally changes a value.
- [ ] Verify multi-chunk deposits across order/worker count/cache eviction/restart; test mixed grade/host boundaries, invalid custom data, removed definitions and finite depletion.
- [ ] Generate the config/reference schema and build a small external-editor proof of concept only when its implementation is selected. Verify file/GUI round trips, preview equivalence, staging, permissions, backups and comment preservation.
- [ ] Benchmark many custom definitions, large candidate bounds, reload compilation and preview workers; no per-block script/JSON/file-I/O overhead.
- [ ] Validate new-world locks and existing-save behavior; keep all implementation work open until source/runtime evidence exists.

Return to the [roadmap](../roadmap.md), [terrain mechanics](TERRAIN-MECHANICS.md), [engine architecture and references](LOD-AND-DRAINAGE-ARCHITECTURE.md), [design reference](design-reference.md) and [module boundaries](MODULES.md).
