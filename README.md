# Deep Primal

**0.0.1-alpha.19 — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is a standalone survival total conversion under development. Alpha 19 replaces the Video Settings entry with our graphics screen, separates the explored-terrain LOD runtime into its own module, and adds bounded connected-pool pressure to the independent liquid engine. Deep Primal remains the product name. Reference mods guide the capabilities we intend to improve; superiority requires measured behavior and performance comparisons. The separate `standalone-engine/` experiment is historical source and is not shipped.

## Current foundation

- **Original Overworld generation:** `deepprimal:continental_v1` and `deepprimal:regional_v1` determine continents, relief, climate/palettes, drainage, cave tunnels and tree placement. The selected generator extends `ChunkGenerator`, not `NoiseBasedChunkGenerator`, and does not delegate those decisions to Minecraft's Overworld noise router or multi-noise biome source. Existing Minecraft blocks and biome assets are temporary palettes; Minecraft/NeoForge still provide storage, lighting, scheduling and host APIs. Nether/End remain native.
- **Original liquids:** a separate fluid type with 16 saved units per block, bounded conservative gravity/pooling and connected-pool equalization, stored currents, full-volume buckets and shared-corner curved water meshes. This path does not use `WaterFluid`/`FlowingFluid` for transport. Native water introduced into engine worlds is imported before native spreading. Lava and waterlogged solids are not replaced.
- **Actual-chunk LOD:** immutable vertical block runs preserve roofs, air gaps, trees and player edits. Three mesh resolutions, projected-size selection, native-ready handoff, bounded worker/uploads/GPU storage and world-scoped persistent caches run through the common GPU API on OpenGL and optional Vulkan.
- **Custom graphics settings:** **Video Settings** directly opens General, Quality, Performance, LOD, Water and Graphics API tabs with sliders, typed values, help, Apply/Revert and saved requested/effective status. All native video options remain accessible, including display modes, GUI scale, presets and texture filtering. Typed native values respect their actual increments. Preset previews are staged; texture and display changes receive their required native updates on Apply. This is original Deep Primal UI; Sodium is a usability reference, not a dependency.
- **Separate feature ownership:** graphics owns the settings, renderer policy, shared GPU resources and backend preference; `lod-engine` owns capture, streaming/cache, edit invalidation and LOD rendering. Root adapters use a small optional LOD interface.
- **Existing modules retained:** common performance policies, diagnostics, graphics ownership/handoff, prior terrain profiles and the optional Vulkan adapter remain in the repository and module boundaries.

The LOD currently extends **explored/delivered chunks only**. A 208- or 512-chunk request does not create unseen terrain. It uses approximate material-color voxel meshes, not full block models/textures or a finished DH-equivalent viewer. The old procedural horizon is disabled by default and is never silently selected when LOD is turned off.

## Install and inspect

Install **one** matching Deep Primal JAR in `mods`, replacing the earlier Deep Primal JAR. The default installer is the small OpenGL build. The `vulkan-windows` installer is an alternative with the optional Windows backend bundled; OpenGL remains its initial API until selected and restarted. Do not install both installers or duplicate their nested modules.

Create a **new normal world** for the new engine. Saved worlds retain their serialized generator, explored terrain and height; this update does not migrate or delete saves. `config/deepprimal-worldgen.toml` defaults to `builtInTerrainEngine = true`. Set `extraUndergroundDepth = true` and restart before creation for Y=-384..639 rather than -128..639. Old river-profile switches apply only when explicitly creating legacy terrain with the built-in engine disabled.

Use Creative for inspection. Open the custom graphics screen for LOD radius/detail, GPU payload/disk budgets, capture/upload work, native render/simulation/vertical limits, quality, water visuals and API preference. Restart is required for API changes. Requested distances stay saved while server/heap/session limits determine effective native delivery.

## Build

Use Java 25 and the Gradle wrapper:

```sh
./gradlew build
./gradlew build -PvulkanPlatform=windows
./gradlew build -PvulkanPlatform=linux
```

The default output is `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.19.jar`, without Vulkan native libraries. Platform bundles have distinct `-vulkan-<platform>.jar` filenames. `-PwithoutVulkan` also excludes the optional Vulkan project from the build. `-PbundleVulkan` explicitly requests the larger universal bundle. Default installers include core, common performance, graphics and LOD. `-PwithoutLod` omits the LOD runtime and produces a separate `-no-lod.jar` profile. No new runtime dependency was added for terrain/LOD/UI.

## Scope and evidence

This is an engine alpha, not visual acceptance of the full world design. Connected regional drainage and material fields are implemented foundations; complete river/lake spillways, cross-watershed contracts, erosion/sediment simulation, structural connectivity checks, displacement, groundwater/recharge and breaking shoreline surf remain unfinished. The new pressure pass searches at most 128 connected wet cells per attempt and 4,096 cells per level/tick; it is not whole-lake filling, recharge or a replacement for correctly generated spillways. Current visual defects include repeated river basins, abrupt exposed LOD page walls and placeholder tree shapes. Those remain open; this audit preserves the existing terrain version rather than changing saved-world geography. New native vegetation/coastal scenes still require broader fixed-seed refinement. Near-block meshing remains the host renderer; we have not built a wholesale replacement graphics API or demonstrated higher FPS than Sodium or Distant Horizons.

PhysEx Reborn, Finite Water, BetterWaterMesh and Water Erosion inform independently authored liquid behavior and visuals. No PhysEx runtime/code is bundled; displacement, collision-shaped fluid storage, containers, lava and erosion remain separate unfinished work. TFC informs geology/climate/drainage, Terralith terrain variety, and Tectonic continental relief. Their source/assets are not bundled. Gaea/World Machine methods guide independent algorithms; no proprietary engine is embedded. Distant Horizons, Nanite and Cesium are references. The earlier LWJGL meshoptimizer probe remains development-only, with no shipping meshoptimizer/clusterlod integration. Environmental catalogs remain planning-only.

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
