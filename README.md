# Deep Primal

**0.0.1-alpha.28 — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is a standalone survival total conversion under development. Alpha.28 adds V7 terrain with seed-varied origin/climate, regional lowlands/plateaus/mountains, wider drainage channels and biome-based vegetation/surfaces. It fixes supported thin water rendering below its bed without adding water. Saved V1–V6 worlds retain their generation and edits. See [terrain scope and configuration](docs/TERRAIN-V7.md) and [release validation](docs/VALIDATION-0.0.1-alpha.28.md).

The consolidated engine, optional Vulkan/LOD modules and Themes alpha.8 remain together on main. The separate maritime-1.21.1/TFC project remains independent and is not bundled into this Minecraft 26.1.2 installer. Broader engine acceptance and Windows/owner-GPU performance remain open; this is an alpha release.

## Current foundation

For engine-only game validation, use the proxy helper's `--root-game-task runClient`
before its `--` separator and omit game selectors after it. With plain Gradle,
use `:runClient`. Unqualified `runClient` selects both the engine and standalone
Themes clients in this combined build, mixing their capture/output records.
Explicit `:gui-themes:runClient` remains available for standalone Themes tests.
Dry-run task isolation verifies dispatch only; native water/save/shutdown gates
must still run separately on the selected client.

- **Original Overworld generation:** `deepprimal:continental_v7` and `deepprimal:regional_v1` determine continents, relief, climate/palettes, drainage, cave tunnels and tree placement. The selected generator extends `ChunkGenerator`, not `NoiseBasedChunkGenerator`, and does not delegate those decisions to Minecraft's Overworld noise router or multi-noise biome source. Existing Minecraft blocks and biome assets are temporary palettes; Minecraft/NeoForge still provide storage, lighting, scheduling and host APIs. Nether/End remain native.
- **Original liquids:** a separate fluid type with 16 saved units per block, bounded conservative gravity/pooling and connected-pool equalization, stored currents, full-volume buckets and shared-corner curved water meshes. This path does not use `WaterFluid`/`FlowingFluid` for transport. Native water introduced into engine worlds is imported before native spreading. Lava and waterlogged solids are not replaced.
- **Actual-chunk LOD:** immutable vertical block runs preserve roofs, air gaps, trees and player edits. Three mesh resolutions, projected-size selection, native-ready handoff, bounded worker/uploads/GPU storage and world-scoped persistent caches run through the common GPU API on OpenGL and optional Vulkan.
- **Custom graphics settings:** **Video Settings** directly opens General, Quality, Performance, LOD, Water and Graphics API tabs with authored flat controls, sliders, typed values, tab search, changed-value markers, help, Apply/Revert and staged/effective distance status. All native video options remain accessible, including display modes, GUI scale, presets and texture filtering. Typed native values respect their actual increments. Preset previews are staged; texture and display changes receive their required native updates on Apply. This is original Deep Primal UI; Sodium is a usability reference, not a dependency.
- **Separate feature ownership:** graphics owns the settings, renderer policy, shared GPU resources and backend preference; `lod-engine` owns capture, streaming/cache, edit invalidation and LOD rendering. Root adapters use a small optional LOD interface.
- **Existing modules retained:** common performance policies, diagnostics, graphics ownership/handoff, prior terrain profiles and the optional Vulkan adapter remain in the repository and module boundaries.

The LOD currently extends **explored/delivered chunks only**. A 208- or 512-chunk request does not create unseen terrain. It uses approximate material-color voxel meshes, not full block models/textures or a finished DH-equivalent viewer. The old procedural horizon is disabled by default and is never silently selected when LOD is turned off.

## Install and inspect

Install **one** matching Deep Primal JAR in `mods`, replacing the earlier Deep Primal JAR. The default installer is the small OpenGL build. The `vulkan-windows` installer is an alternative with the optional Windows backend bundled; OpenGL remains its initial API until selected and restarted. Do not install both installers or duplicate their nested modules. Remove any separate Deep Primal Themes JAR when using this combined installer; the themes module is included. Choose a theme from the Themes button or Mods configuration. All eight 4K menu images are retained, so this installer is larger than the earlier engine-only JAR.

Create a **new normal world** to inspect V7 geography. Existing `continental_v1` and `continental_v2` worlds retain their saved generator, explored terrain and height. The update does not migrate or delete saves. Saved V1–V7 geography is frozen; new worlds use V7. Existing V1–V6 saves retain their revision even in newly explored chunks. Future geographic changes require another saved revision. `config/deepprimal-worldgen.toml` defaults to `builtInTerrainEngine = true`. Set `extraUndergroundDepth = true` and restart before creation for Y=-384..639 instead of -128..639. Legacy river-profile switches apply only when creating legacy terrain with the built-in engine disabled.

Use Creative for inspection. Open the custom graphics screen for LOD radius/detail, GPU payload/disk budgets, capture/upload work, native render/simulation/vertical limits, quality, water visuals and API preference. Restart is required for API changes. Requested distances stay saved while server/heap/session limits determine effective native delivery.

## Build

Use Java 25 and the Gradle wrapper:

```sh
./gradlew build
./gradlew build -PvulkanPlatform=windows
./gradlew build -PvulkanPlatform=linux
```

The default output is `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28.jar`, without Vulkan native libraries. Platform bundles have distinct `-vulkan-<platform>.jar` filenames. `-PwithoutVulkan` also excludes the optional Vulkan project from the build. `-PbundleVulkan` explicitly requests the larger universal bundle. Default installers include core, common performance, graphics, LOD and GUI themes. `-PwithoutLod` omits the LOD runtime and produces a separate `-no-lod.jar` profile. No new runtime dependency was added for terrain/LOD/UI.

## Scope and evidence

This is an engine alpha with specific verified improvements. V7 extends V6 with the seed, landform, channel and biome changes described above and retains finite environmental water accounts. Full world design, large-region hydraulic continuation, moving surface landslides, a dynamic ice-sheet solver, live erosion/deposition, displacement, breaking surf and the deferred content catalogs remain incomplete. Repetitive terrain contours, distant-water/LOD coverage, mixed transparency and placeholder vegetation still need work. The pressure search remains limited to 128 wet cells per attempt and 12,288 visits per level/tick across three substeps. Near solid-block meshing remains the host renderer; no superiority over Sodium or Distant Horizons is established.

Liquid authority commits on the 20 Hz server thread, with three transport passes per tick: nominal 60 substeps/second at 20 TPS. These passes occur within each host tick, not on an independent 60 Hz wall clock. Client animation runs every rendered frame; no hardware-independent 60 FPS guarantee is made. Persistent queues prevent dropped work, and bounded downhill routing can drain one-unit films toward nearby outlets. Neither local routing nor the 128-cell pressure search is an unlimited regional lake solver. Near water uses independent patches with 100 ms GPU height interpolation and wet/dry face fading. The Water tab offers **Smooth water movement**; disabling it restores the previous fluid rendering path. This adds visual latency and bounded GPU storage. Waterfall/front topology, mixed transparency and extreme-view behavior still need refinement.

PhysEx Reborn, Finite Water, BetterWaterMesh and Water Erosion inform independently authored liquid behavior and visuals. No PhysEx runtime/code is bundled; displacement, collision-shaped fluid storage, containers, lava and erosion remain separate unfinished work. TFC informs geology/climate/drainage, Terralith terrain variety, and Tectonic continental relief. Their source/assets are not bundled. Gaea/World Machine methods guide independent algorithms; no proprietary engine is embedded. Distant Horizons, Nanite and Cesium are references. The earlier LWJGL meshoptimizer probe remains development-only, with no shipping meshoptimizer/clusterlod integration. Environmental catalogs remain planning-only.

- [Alpha 24 watershed, water-surface and engine audit](docs/VALIDATION-0.0.1-alpha.24.md)
- [Active build/audit continuation](docs/ENGINE-WORK-STATE.json)
- [Alpha 23 water, terrain, settings and inventory repair](docs/VALIDATION-0.0.1-alpha.23.md)
- [Alpha 22 ocean rendering repair](docs/VALIDATION-0.0.1-alpha.22.md)
- [Alpha 21 independent animated water](docs/VALIDATION-0.0.1-alpha.21.md)
- [Water budgets and long-running world requirements](docs/WATER-BUDGET-REQUIREMENTS.md)
- [Alpha 20 liquid filling and mesh progress](docs/VALIDATION-0.0.1-alpha.20.md)
- [Alpha 19 module, menu and liquid validation](docs/VALIDATION-0.0.1-alpha.19.md)
- [PhysEx reference review and independent water work](docs/PHYSEX-REFERENCE-2026-10-21.md)
- [Alpha 18 audit, regression tests and limits](docs/VALIDATION-0.0.1-alpha.18.md)
- [Alpha 17 integrated foundation](docs/VALIDATION-0.0.1-alpha.17.md)
- [Engine/LOD architecture and reference decisions](docs/LOD-AND-DRAINAGE-ARCHITECTURE.md)
- [Install, settings and diagnostics](docs/WORLDGEN-PROTOTYPE.md)
- [Module ownership and package profiles](docs/MODULES.md)
- [Roadmap](roadmap.md), [owner decisions](docs/design-decisions-2026-10-18.md), [detailed design](docs/design-reference.md)
- [Named environmental catalogs](docs/NATURAL-TYPE-CATALOG.md), [coverage inventory](docs/ENVIRONMENTAL-COVERAGE-INVENTORY.md) — planning only
- Historical validation: [alpha 16](docs/VALIDATION-0.0.1-alpha.16.md), [alpha 15](docs/VALIDATION-0.0.1-alpha.15.md), [alpha 14](docs/VALIDATION-0.0.1-alpha.14.md)
- [Water reference audit](docs/WATER-REFERENCE-AUDIT-2026-10-20.md), [Vulkan provenance](docs/VULKAN-BACKPORT.md), [capabilities](docs/VULKAN-CAPABILITIES.md)

Original code remains All Rights Reserved pending the owner's licensing choice. The optional Vulkan adapter contains adapted VulkanMod source under LGPL-3.0-only with its notices/source retained. Minecraft and NeoForge are dependencies, not bundled game distributions.
