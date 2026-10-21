# Primal Engine

An original standalone engine prototype for Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25. Version 0.0.1-alpha.1 is an initial vertical slice, not the completed terrain/liquid/LOD replacement.

Build this directory independently from the repository root: `./gradlew -p standalone-engine build` (`gradlew.bat -p standalone-engine build` on Windows). Output: `standalone-engine/build/libs/PrimalEngine-26.1.2-0.0.1-alpha.1.jar`. The main legacy project is not a dependency. Install this JAR in place of the old Deep Primal worldgen prototype and create a disposable new world normally. The normal Overworld preset selects `primalengine:continental`. Old saves are not migrated. This prototype deliberately declares the old `deepprimal` mod incompatible until an integration adapter exists. Nether/End generation remains native.

## Implemented ownership

- Original seeded scalar fields create several-kilometre landmasses and surrounding ocean gaps. A bounded twelve-continent cache holds whole-continent priority-flood drainage plans, downhill parent links and rainfall-weighted catchment area. Per-chunk terrain, carved channels and climate sampling use that plan.
- `ContinentalGenerator` extends the platform's abstract `ChunkGenerator`, not its noise generator. It fills solid columns directly and reconstructs its own sparse trees within each chunk; it does not call native carvers, feature decoration or structure generation. Existing block assets are placeholders. Geography is still rudimentary and needs visual tuning and stronger hydrology constraints.
- `EngineFluid` derives directly from the platform `Fluid` interface. Its authoritative state contains sixteen volume units per block and a current direction. Native saved fluid ticks call our bounded gravity/pooling solver; they do not run the vanilla water-spreading algorithm. The default dimension budget is 2,048 visited fluid cells per tick. No fluid transfer requests unloaded chunks. Settled water is dormant until an edit/neighbor update wakes it.
- Full engine-water cells can be collected with an ordinary empty bucket; the returned Primal Water Bucket places sixteen units. Partial cells cannot yield a free full bucket. Water introduced by native buckets in an engine Overworld is converted to engine units before native spreading. Solid waterlogged cells stay sealed.
- Our `CustomFluidRenderer` replaces the native water mesher for the engine fluid. Global shared corner heights and curved two-by-two surface patches preserve common edges; flat surfaces retain one quad, and face emission avoids temporary vertex arrays. Minecraft supplies the chunk vertex buffers, animated water sprites and material pipeline. This is a smooth surface experiment, not coastal surf or a completed hydraulic renderer.
- `lod-core` is a separate library module. Immutable column runs preserve vertical materials and air gaps, including trees/buildings and fluid-state properties. Snapshots use signed integer coordinates, a versioned gzip/CRC format, bounded decoding, temporary-file replacement and revision checks. `ChunkCapture` reads caller-owned loaded chunks incrementally under a supplied read budget. These building blocks are implemented; an automatically recording/rendering LOD system is not yet included.

## Reference audit

| Reference | Inspected evidence | What we learn from it |
| --- | --- | --- |
| [PhysEx Reborn](https://github.com/Sasai-Kudasai-BM/PhysEx-Reborn) | `ClassicFlowTask`, fluid task/storage layout and the author's CurseForge description | Conserved transfers, pressure/equalization, displacement and fluid occupancy deserve separate contracts. Not all of those features are implemented here. |
| [JDKDigital Finite Water](https://github.com/JDKDigital/finitewater/tree/dev-1.20.1) | The source denies source creation outside a biome tag; its license file says ARR | Disabling source regeneration alone is not volume conservation. We implement transport independently. |
| [BetterWaterMesh](https://github.com/KatatsumuriPan/BetterWaterMesh/tree/1.12.2/forge) | `CustomWaterMeshUtil` and `CustomLiquidRenderer`; README identifies LGPL 2.1 | Shape the surface within cells while maintaining shared edges. Its code/formulas are not copied into this engine. |
| [Water Erosion](https://github.com/13rac1/Minecraft-Water-Erosion) | README and `erosion-common/Tasks.java` | Material resistance, bank erosion and streambed evolution. Live erosion is not implemented here. |
| [Distant Horizons](https://gitlab.com/distant-horizons-team/distant-horizons) | Official project description and [terrain data API](https://distant-horizons-team.gitlab.io/distant-horizons/com/seibel/distanthorizons/api/interfaces/data/IDhApiTerrainDataRepo.html) | Actual chunk/column data, multiple detail levels, asynchronous replacement and persistence. Raw GitLab source retrieval was unavailable during this pass; a full source audit and implementation comparison remain pending. |

No code from these reference projects is copied, linked or bundled. The new source is independently authored. Existing platform APIs and block assets are used through Minecraft/NeoForge.

## LOD acceptance target

Match Distant Horizons' useful behavior before making performance claims: distant real terrain, vegetation, structures and player edits; persistent caches; multiplayer data; configurable detail/radius; robust native-chunk handoff. Our architecture must then demonstrate an improvement under matched conditions.

Measure cold/warm cache, flying, teleporting, tall worlds, coasts, forests, player builds and server contention. Track median/p95/p99 CPU and GPU frame times, RAM and VRAM at steady state and peak, time to first far coverage/refinement, generated chunks per second, disk/network throughput and edit-to-visible delay. Compare at matched visual error and distance; a lower-detail scene is not a fair performance win. Keep OpenGL/Vulkan submission separate from world storage/simulation. Do not reintroduce the old procedural horizon as a substitute for this requirement.

## Incomplete systems

This is not TFC geography parity, PhysEx parity, a full Navier–Stokes solution or a Distant Horizons replacement. Dynamic bank erosion/sediment, robust lake containment, springs/recharge, water tables, pressure/U-bends, displaced water around placed blocks/pistons, hollow/waterlogged shapes, bottle/farmland consumption, lava, breaking surf/run-up/foam, complete native fluid integrations, advanced biome/geology content, settings UI, LOD reduction/rendering/networking and hardware profiling remain unfinished. Chunk-order and multithreaded generation stress coverage must expand. Literal unlimited world height is not implemented; this profile uses -128 through 639.

Do not distribute it as the full next Deep Primal update. Validation details are recorded in `VALIDATION.md` after the executable checks complete.
