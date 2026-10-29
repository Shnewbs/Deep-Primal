# Expanded replica — standalone-engine/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/standalone-engine/README.md) |
| Source identity | git: standalone-engine/README.md |
| Preserved source SHA-256 | `6c2f46f20ca710372bb0c96946b2f7d44935cd85c66d7cbc6f4eadf32a49cc8a` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

The standalone-engine README describes a historical alpha1 Minecraft-hosted prototype, despite its directory name. It has an independently authored terrain/transport core and generator, a bounded integer-water solver, host-buffer water presentation and a versioned LOD storage library, while recording unfinished renderer, LOD, gameplay and settings integration. It excludes the legacy root mod rather than providing a native executable independent of Minecraft. Later integrated-core work belongs to another source epoch. The document is valuable experimental history, but its build/install instructions should not be presented as the current standalone direction or as an old-world migration path.

## Research expansion — gaps and details to deepen

- The word standalone needs a precise capability definition. Portable core, separate Minecraft mod, headless simulator, external editor and complete independent game are different products. The prototype proves some core boundaries while still relying on host storage, scheduling, rendering resources, lighting, networking and player interaction.
- Prototype terrain and water figures need their original scope preserved. Bounded active work and deterministic drainage do not establish parity with the integrated V7/V8 architecture, complete unloaded-world conservation or broad performance. LOD serialization without capture/render integration is a storage capability rather than a visible distant-terrain feature.
- The historical instructions and references need a status banner and links to current root architecture. Older rejected or superseded implementation paths should remain available for research without automatically reentering production because they live under a promising directory name.

## Research expansion — proposed contracts

- Add a historical-prototype capability map and a separate proposed host-replacement plan. List interfaces for world storage, authoritative commands, registry/material resolution, networking, render extraction, input/audio, resources and lifecycle, with portable core tests reused where semantics genuinely match.
- Define incremental standalone milestones: headless deterministic core, controlled host adapter, minimal playable scene, persistent world, authoritative multiplayer and full authoring integration. Each milestone should declare its supported content and compatibility envelope; no external platform should be selected merely because a draft mentioned it.

## Research expansion — acceptance checks

- Verify the archived prototype package and native evidence at their exact alpha1 identity, using disposable worlds and its own dependency target. Do not substitute it for a newer integrated artifact or infer compatibility with existing Deep Primal saves.
- For any extracted portable subsystem, run independent deterministic and lifecycle tests plus adapter parity fixtures. Require visible LOD, persistence, edit handling and authority evidence separately before describing a complete standalone engine, and preserve original approved artwork and frozen generation identities during migration.

## Research expansion — source anchors

- Primal Engine
- Project identity and accepted direction
- LOD settings

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Primal Engine

An original standalone engine prototype for Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25. Version 0.0.1-alpha.1 is an initial vertical slice, not the completed terrain/liquid/LOD replacement.

Build this directory independently from the repository root: `./gradlew -p standalone-engine build` (`gradlew.bat -p standalone-engine build` on Windows). Output: `standalone-engine/build/libs/PrimalEngine-26.1.2-0.0.1-alpha.1.jar`. The main legacy project is not a dependency. Install this JAR in place of the old Deep Primal worldgen prototype and create a disposable new world normally. The normal Overworld preset selects `primalengine:continental`. Old saves are not migrated. This prototype deliberately declares the old `deepprimal` mod incompatible until an integration adapter exists. Nether/End generation remains native.

## Project identity and accepted direction

Deep Primal remains the overall project; Primal Engine names this independent engine component and preview JAR. The [terrain and LOD architecture](../docs/LOD-AND-DRAINAGE-ARCHITECTURE.md) remains the accepted direction. Its current status and reference/tool decisions are recorded there; starting this replacement does not discard that plan.

## LOD settings

Version 0.0.1-alpha.1 has no LOD settings screen or LOD configuration file: it does not yet render distant chunk LODs. The snapshot/storage library is groundwork. The older Deep Primal alpha.16 build exposes **Far terrain** distance for its procedural horizon; that screen is not included here and does not configure this module.

The intended custom settings section will cover enable/disable, viewing radius, detail/screen-space error, CPU/background work, upload work, RAM/VRAM and disk-cache limits. Persist requested values, show effective limits separately, and verify reopen/restart behavior before shipping. These controls remain unimplemented.

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


<!-- END PRESERVED SOURCE -->
