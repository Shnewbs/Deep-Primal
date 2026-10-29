# Expanded replica — LIGHTING-RESEARCH-NOTES.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_2bbc0602744c8191b7ecbed7434faf74&account_id=personal) |
| Source identity | library: LIGHTING-RESEARCH-NOTES.md |
| Preserved source SHA-256 | `87f8560eb46ed677f584524aea596cc6b9b61c8776c5a6f569c38c13784863e2` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../README.md) · [Integrated design](../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../chapters/lighting-integration.md) · [Discrepancy register](../../DISCREPANCIES.md)

## Research expansion — source scope

The notebook is a substantial source-backed research record with 72 entries and 71 unique primary sources across voxel engines, production renderers, transport, sampling, denoisers, APIs, color, and validation. It already separates observed mechanisms from proposed Deep Primal decisions and explicitly limits papers or artifacts that were not fully inspected.

## Research expansion — gaps and details to deepen

- The next addition should improve traceability into implementation decisions rather than repeat the engine survey. A reader should be able to move from a source claim to the corresponding master clause, concrete host adapter, experiment, and acceptance artifact without treating research maturity as product readiness.
- Live vendor documentation and moving repository branches can change after the research date. The existing retrieval context should be extended into machine-readable version and inspection-depth metadata, especially for temporal upscaling APIs, feature availability, and source pages that differ from original papers.
- The new rendering research overlaps selected synchronization, reconstruction, and resource topics. Cross-reference it using shared claim identifiers and preserve per-source attribution limits; avoid expanding one vendor page into repeated paraphrase across notebook, chapter, and every source card.

## Research expansion — proposed contracts

- Add an evidence ledger with stable ID, exact URL, retrieved date, inspected revision/version when available, source type, inspected depth, supported claim, limitation, design inference, affected master sections, and experiment status. Mark unverified full-paper or hardware conclusions explicitly instead of filling them with plausible detail.
- Create experiment receipts linking an adopted idea to input fixtures, reference oracle, rejected alternatives, native artifact/device, resource peak, visual outcome, and reproducible failure. Capture negative results and stop conditions so an attractive technique can be deferred without losing the reasoning.
- Link RP-01 through RP-12 where they add terrain residency, bounded submission, Vulkan ownership, or current reconstruction constraints. State that existing project FSR is spatial FSR 1 and the NVIDIA bridge is direct NGX; newer FSR and Streamline documentation remain integration research until qualified.

## Research expansion — acceptance checks

- Validate all ledger IDs and links against the preserved notebook and master references, checking that multiple entries from one page are not counted as independent corroboration. Preserve exact dates and historical engine versions instead of silently making every claim current.
- Audit each implementation recommendation for a specific supporting mechanism and a separately labeled project inference. Ensure no source-only experiment, vendor sample, or uninspected PDF is presented as native Deep Primal performance, hardware compatibility, or a completed renderer feature.

## Research expansion — source anchors

- 0. Scope, method, and principal decisions
- 2. Production engine architectures
- 3. GI, sampling, and reconstruction

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal Lighting Engine — Research Notebook

**Edition:** Companion to LIGHTING-ENGINE.md v0.3  
**Document date:** 2026-10-28 (project time zone)  
**Research window:** October 28–29, 2026  
**Status:** Primary-source study and proposed engineering synthesis. No engine build, shader run, native benchmark, or implementation completion is claimed.  
**Master specification:** [LIGHTING-ENGINE.md](https://chatgpt.com/library?file_id=libfile_ab421e9192508191b351dea170d99928&account_id=personal)

## 0. Scope, method, and principal decisions

The notebook records a broad comparative survey of seven voxel/editable-world systems, seven production-renderer families, ten transport/reconstruction/reference families, and supporting direct-light, reflection, atmosphere, material, and API work. It contains **72 reference entries pointing to 71 distinct primary-source URLs**. Multiple records can describe different uses of one source. This is a deliberately selected technical survey, not a claim to have inspected every proprietary, unpublished, or historical lighting system.

Sources are official documentation, original author publications or repositories, engine source/API contracts, and primary publication records. Each card distinguishes observed evidence from a proposed Deep Primal decision. Original benchmarks remain attached to their original workloads; none becomes a performance prediction for Deep Primal.

### Principal engineering synthesis

1. **Preserve an independent world-light core.** Correct source removal, missing boundaries, signed coordinates, chunk lifetime, and cache recovery matter before advanced image effects.
2. **Give every contribution one owner.** A voxel approximation, direct-light estimate, probe field, screen-space bounce, and reflection cache may overlap. Define replacement, partition, or normalized blending before combining them.
3. **Separate geometry, radiance, and cache coverage.** A world-space texture can still depend on camera-visible information. Require offscreen edits, stationary-camera mining, and cold-streaming tests.
4. **Count complete cost.** Representation construction, invalidation, uploads, tracing, denoising, history, and retired resources belong in the budget.
5. **Use measured competition for optional GI.** Visibility-aware probes and sparse-field GI are the first competitors; full 3D Radiance Cascades gets a separate feasibility branch.
6. **Make the modest-cost profile complete.** OpenGL and the owner AMD target must have correct current-world lighting even when optional SDKs or hardware features are unavailable.
7. **Build references and diagnostics early.** A scalar oracle validates logical light; a matched path-traced scene validates claimed transport. Moving captures reveal errors a still image cannot.

These are the proposed architecture's decisions. Their implementation and numerical acceptance remain future gates.

### Reading guide

- [1. Voxel and editable-world systems](#1-voxel-and-editable-world-systems)
- [2. Production engine architectures](#2-production-engine-architectures)
- [3. GI, sampling, and reconstruction](#3-gi-sampling-and-reconstruction)
- [4. Direct-light, reflection, and atmosphere foundations](#4-direct-light-reflection-and-atmosphere-foundations)
- [5. Supporting evidence and license declarations](#5-supporting-evidence-and-license-declarations)
- [6. Backend and imaging references](#6-backend-and-imaging-references)
- [7. Evidence limits and unresolved research](#7-evidence-limits-and-unresolved-research)
- [8. Research-to-requirement traceability](#8-research-to-requirement-traceability)
- [9. Working glossary](#9-working-glossary)
- [10. Source register](#10-source-register)

## 1. Voxel and editable-world systems

### VX-01 — Starlight propagation, skylight initialization, and save compatibility

**Source:** [Starlight Technical Details][VX-01] · official technical document

**Observed:** Starlight queues positions together with propagated levels, checks whether a neighbor can improve before reading its block state, and treats decrease propagation separately. Surviving sources queued for re-increase require a value check so later removals cannot resurrect stale light. Skylight sources are initialized explicitly, with opacity information used to avoid unnecessary work. The document distinguishes absent and uninitialized sections and describes forcing relighting when switching between its save representation and vanilla. Its opening notice says the old comparisons only apply to Minecraft 1.16–1.19.

**Proposed use:** Prototype a compact carried-value queue and guarded removal/re-increase against the independent oracle. Keep a simpler full rebuild as the recovery path. Optimize sky source discovery without assuming a heightmap proves every opening.

**Evidence limit:** Historical Java/host integration; not a portable current-version patch. Do not repeat its old speedup ratios as current evidence.

### VX-02 — Starlight chunk-owned state and scheduling

**Source:** [Starlight README][VX-02] · official repository README

**Observed:** The README ties light sections directly to chunk objects and describes a lighting implementation whose persistent state does not live in a second independent lifecycle. Its stated parallelism depends on correct external scheduling, rather than permission for concurrent overlapping writes. The repository is archived and says further updates are not expected. Its Minecraft 1.20 benchmark discussion says the absolute propagation timings are unlikely to change client FPS materially, despite relative improvements. This is a useful warning against equating solver speedups with whole-frame gains.

**Proposed use:** Keep ownership and residency explicit, and schedule overlapping influence regions safely. Benchmark end-to-end chunk readiness and frame tails alongside the solver.

**Evidence limit:** The README's use of stateless does not mean there is no light data or that all work is automatically thread-safe.

### VX-03 — Luanti explicit unlighting, relighting, and incomplete faces

**Source:** [voxelalgorithms.cpp][VX-03] · official source

**Observed:** The source processes day and night light banks separately using level-bucketed unlight and relight queues. Queue entries retain relative positions, map-block references, and a source direction. Missing neighbors mark a face incomplete; later border repair checks both touching faces. Bulk updates remove invalid light before rebuilding sources. Direct sunlight is handled through vertical scans. One initialization path loads the upper block and otherwise uses an underground-status heuristic to estimate incoming sunlight. The file declares SPDX LGPL-2.1-or-later.

**Proposed use:** Adapt explicit incomplete-face bookkeeping and staged removal. Reject the heuristic for authoritative Deep Primal sky access: retain unknown validity until a certified boundary exists.

**Evidence limit:** Source inspection is not a proof of thread safety, native-host compatibility, or performance. The inspected master file is mutable; pin a commit before implementation study.

### VX-04 — Veloren horizon data for distant sun shadows

**Source:** [This Week In Veloren 81][VX-04] · official developer account

**Observed:** The described renderer computes horizon information from a heightmap and the plane of the sun's path. Two angular values per chunk describe when the sun becomes visible; an occluder-height term supports an approximate volumetric interpretation. The article reports four bytes per chunk for that design. Server map data separates color, altitude, and horizon information so the client can reconstruct its display. The same account introduces a dedicated LOD rendering path, directional and point-light shadow maps, and material/water lighting work.

**Proposed use:** Evaluate a compact, versioned terrain-horizon representation for distant sunlight. Couple its invalidation to terrain edits and dimension sky rules. Keep local cave/overhang visibility in a representation that can actually express those features.

**Evidence limit:** This is a documented 2020 implementation, not proof of the current renderer. A heightmap and one solar plane are insufficient to represent arbitrary overhangs, multi-layer caves, or every celestial path; that limitation is our geometric inference.

### VX-05 — Voxel Tools bounded tasks and spatial locking

**Source:** [Performance][VX-05] · official documentation

**Observed:** Voxel Tools exposes worker-count controls and a main-thread task budget. The documentation explains why eagerly copying voxel neighborhoods can accumulate unbounded memory when producers outrun workers. It describes moving copying into jobs, then replacing many per-buffer read/write locks with spatial read/write regions protected by a short mutex. It also explains that individual voxel operations repeat lookup, locking, notification, and scheduling costs, whereas bulk operations amortize that work. These are storage and scheduling techniques, not evidence of a Minecraft-compatible lighting solver.

**Proposed use:** Add memory admission before snapshot work and account for the complete commit/upload path. Compare bounded snapshots and spatial-region ownership as alternatives; do not mix lock models casually.

**Evidence limit:** The page includes historical Godot 3 renderer observations that must not be represented as current Godot 4 or Deep Primal GPU results.

### VX-06 — Voxel Tools block meshing and baked ambient occlusion

**Source:** [Blocky terrains][VX-06] · official documentation

**Observed:** VoxelMesherBlocky builds chunk meshes from model IDs and removes mutually occluded faces. The page explicitly says this mesher does not use greedy meshing. Model libraries supply cubes and more varied geometry; default 16-bit voxel IDs allow 65,536 models. The documented vertex-color material option enables ambient occlusion baked around cube edges. Material reuse is encouraged to limit draw calls. These features concern mesh generation and local shading, not a demonstrated dynamic skylight or colored flood-propagation implementation.

**Proposed use:** Track whether a visual term lives in the mesh or a separate light page. An AO or shape edit needs the appropriate neighboring mesh invalidation; a light-texture update should not automatically remesh unchanged geometry.

**Evidence limit:** Do not label Voxel Tools a complete lighting engine or assume its terrain module supplies a production flood-light field.

### VX-07 — Vintage Story edit lifecycle and light-query interfaces

**Source:** [IBlockAccessor][VX-07] · official API documentation

**Observed:** The API distinguishes scalar light queries from RGB-plus-sunlight results. A bulk accessor's commit batches block updates and relights affected chunks. Removal of a block entity that changes light range requires an explicit light-removal call while its previous state is still retrievable. Non-placement edits such as rotation or chiseling can request neighboring redraw for ambient occlusion. The documented unloaded-chunk light fallback returns default sunlight, and some coordinate overloads explicitly lack dimension awareness. These are API contracts, not disclosed internal solver algorithms.

**Proposed use:** Preserve previous emitter data in committed edit events and support shape-only invalidation. Reject implicit sunlight for unknown Deep Primal data; use typed pending validity and dimension-aware coordinates.

**Evidence limit:** API output packing does not prove the engine's internal light storage format or propagation algorithm.

### VX-08 — Vintage Story material definition validation

**Source:** [BlockType.cs material loading][VX-08] · official source

**Observed:** The material loader resolves separate face-related flags and derives a default AO-emission setting from light absorption when one is absent. It creates a three-element light-HSV value when unspecified and clamps hue, saturation, and brightness to the color utility's respective finite ranges. Its comment identifies out-of-range light values as a crash risk. This is concrete evidence that material authoring values require validation at ingestion and that geometry/AO metadata is more detailed than a single transparent flag.

**Proposed use:** Validate Deep Primal descriptors before atomic registry publication, with explicit units, finite ranges, and diagnostics. Keep logical transmission, visual appearance, and mesh/AO flags independently versioned where their consequences differ.

**Evidence limit:** HSV is not a recommendation for the authoritative field. This loader does not reveal the proprietary engine's propagation, blending, or persistence algorithm.

### VX-09 — Distant Horizons separate light terms in LOD records

**Source:** [DhApiTerrainDataPoint][VX-09] · official API documentation

**Observed:** A Distant Horizons terrain data point contains separate block-light and sky-light values alongside material, biome, vertical bounds, and a detail level. The documented horizontal detail scale progresses from individual blocks to larger cells. Its lower vertical coordinate is relative to the level's minimum height, rather than directly assuming an absolute nonnegative world Y. This public contract exposes lighting terms independently from terrain identity and spatial scale. It does not document the internal codec bit layout or prove how those values are reconstructed.

**Proposed use:** Keep far-terrain sky and emission terms distinct, with explicit coordinate origins and provenance. Add conservative coverage and validity metadata needed by Deep Primal's caves and edits.

**Evidence limit:** Do not claim a particular 64-bit internal packing or flood algorithm from this API alone. Native source files were not retrievable in this run.

### VX-10 — Distant Horizons column-oriented chunk interchange

**Source:** [DhApiChunk][VX-10] · official API documentation

**Observed:** DhApiChunk exposes chunk X/Z, vertical bounds, and lists of terrain data points indexed by relative X/Z. Each column accessor accepts coordinates from zero through fifteen and specifies bounds failures. The data points carry their own vertical spans. This makes the chunk interchange format an array of vertical descriptions rather than requiring callers to supply one object per voxel. The contract is useful for understanding how geometry and associated light can travel together into a distant-terrain pipeline.

**Proposed use:** Consider coalesced column spans for far-world interchange and cache I/O while retaining a voxel representation where edits and cave illumination need it. Coalescing must preserve important openings and emitters.

**Evidence limit:** Public column lists do not establish the private storage codec, compression ratio, light averaging policy, or thread ownership.

### VX-11 — Distant Horizons version-independent optical metadata

**Source:** [IDhApiBlockStateWrapper][VX-11] · official API documentation

**Observed:** The public wrapper provides a Minecraft-version-independent block interface with material identity, serialized state, solidity, air/liquid classification, and opacity. Its documented opacity range runs from zero for transparent to sixteen for fully opaque. It also explicitly allows the underlying wrapped host object to be null for air. The API demonstrates a thin translation boundary between host-specific objects and terrain processing; its method set does not establish detailed face coverage or a full optical-material model.

**Proposed use:** Keep native types behind a host adapter and document opacity semantics separately from light-level range. Deep Primal should add face/shape and validity information required by its own solver instead of assuming the wrapper is sufficient.

**Evidence limit:** No proof of direction-dependent transparency, colored filtering, or current internal scheduler behavior.

### VX-12 — Phosphor's failure history as a regression-test source

**Source:** [Official release notes][VX-12] · official release notes

**Observed:** The release notes document failures involving light-map removal, empty sections mistaken for unloaded sections, missing notifications during skylight initialization, and concurrency causing cached lighting to disappear after chunk reload. They also state that fixing the cause did not repair already-corrupted caches. An explicit incompatibility declaration prevents simultaneous operation with Starlight. Later notes address world-height changes and initial-lighting work. The repository is archived as of May 12, 2025, and its listed newest release targets Minecraft 1.19.x.

**Proposed use:** Turn these failure classes into deterministic restart, unload, initialization-order, empty-section, and competing-provider fixtures. Repair existing derived caches explicitly when schema or historical correctness requires it.

**Evidence limit:** Historical defect fixes do not imply that current host versions still have the defects. Do not propose installing this archived mod on the current target.

## 2. Production engine architectures

Version-specific limitations belong to the inspected implementation. The survey does not imply that an algorithm is impossible on another platform or that current proprietary internals are fully disclosed.

### PROD-01 — Unreal Engine: Lumen Technical Details

**Source:** [Lumen Technical Details][PROD-01] · Official engine documentation (page currently labeled Unreal Engine 5.8)

**Observed:** Lumen combines screen traces with a world representation traced through distance fields or hardware ray tracing. A surface cache stores material/light information and amortizes updates. Its usefulness depends on representation coverage; missing cache areas do not contribute correctly. Large-world updates are camera-centered and can lag fast travel. Software and hardware paths support different geometry and impose different scene-update costs. The documented system is integrated with Unreal’s renderer; its forward-shading and platform constraints are specific to that implementation.

**Proposed use:** ADAPT: give each optional GI cache an explicit coverage, age, dependency revision, and fallback. Require a stationary-camera excavation test: a newly opened cave must become valid because of the edit, not because the player eventually moves. DEFER: a complete surface-card capture pipeline until procedural chunk-mesh updates can maintain it within budget.

**Evidence limit:** No claim that Deep Primal can import Lumen or inherits its reported frame times. Exact GPU/platform support is version-specific. Current docs also discuss skeletal surface cards; do not repeat the older blanket claim that all skeletal content is unsupported.

### PROD-02 — Unreal Engine: Virtual Shadow Maps

**Source:** [Virtual Shadow Maps][PROD-02] · Official engine documentation (page currently labeled Unreal Engine 5.8)

**Observed:** Virtual shadow maps allocate visible shadow tiles from a physical pool and reuse them across frames. Light motion invalidates that light’s cache; moving or deforming casters invalidate overlapping pages. Separate static/dynamic caching can avoid re-rendering expensive static geometry when small dynamic objects move. Tight, correct caster bounds matter. The documentation explicitly warns that physical-page exhaustion can corrupt shadows and that the implementation is designed around Nanite.

**Proposed use:** ADOPT: allocation-generation tags, page-pressure counters, caster-bound diagnostics, and revision-valid shadow reuse. ADAPT: static/dynamic shadow separation only after measuring its extra memory. DEFER: full virtual shadows; first qualify bounded cascades and a local-light atlas. Allocation failure must choose a valid simpler response before sampling invalid storage.

**Evidence limit:** This is virtual shadow mapping, not variance shadow mapping. Do not copy virtual dimensions, page size, or page-pool defaults into the project without workload evidence.

### PROD-03 — Unity HDRP: Understanding Adaptive Probe Volumes

**Source:** [Understanding Adaptive Probe Volumes][PROD-03] · Official versioned package documentation, HDRP 17.0.4

**Observed:** APV automatically places baked light probes at densities related to geometry, samples neighboring probes per pixel, and streams data for large scenes. Spatial layout is shared separately from lighting-scenario data. Multiple baked scenarios can be switched or blended, but geometry changes between bakes can alter probe positions and prevent sharing/blending. The page also describes runtime sky updates using sky-occlusion information. These capabilities do not turn arbitrary construction or excavation into an automatically updated baked solution.

**Proposed use:** ADAPT: separate reusable spatial topology from frequently changed radiance and track their revisions independently. Use visibility-aware interpolation around cave walls. DEFER: baked scenario blending as a general solution for destructible terrain; it is suitable only for explicitly immutable content or bounded authored scenes.

**Evidence limit:** APV probe locations are not freely hand-placed. This page and the SSGI pages below disagree in some fallback descriptions; do not assert a universal APV/SSGI combination without inspecting the selected package.

### PROD-04 — Unity HDRP: Use Screen Space Global Illumination

**Source:** [Use Screen Space Global Illumination][PROD-04] · Official versioned package documentation, HDRP 17.0.4

**Observed:** HDRP’s SSGI uses the screen’s depth and color buffers for diffuse bounce estimates; the same override exposes ray-traced variants. The overview says this override replaces ordinary lightmap/probe contributions. It also lists reflection-probe incompatibility, while the separate reference page exposes probe fallback controls. This is an actual documentation discrepancy in the inspected package pages. Screen-derived information remains tied to visible geometry, so this overview is insufficient evidence for off-screen lighting coverage.

**Proposed use:** ADOPT: a contribution-ownership table that says which term each visual feature replaces, augments, or supplies on a miss. ADAPT: optional screen-space detail with a separately qualified world-space baseline. Never accept an on-screen-only result as authoritative proof that a sealed or off-screen region is lit correctly.

**Evidence limit:** Treat the contradiction as unresolved documentation, not proof that Unity is broken. Avoid promising a particular fallback combination. No RT hardware requirements are inferred from the SSGI name.

### PROD-05 — Unity HDRP: Screen Space Global Illumination reference

**Source:** [Screen Space Global Illumination reference][PROD-05] · Official versioned package documentation, HDRP 17.0.4

**Observed:** The reference exposes screen-space ray-step limits, half/full resolution, denoising controls, and depth tolerance. It offers ray-miss policies involving sky, reflection probes, both, or no contribution. Its ray-traced mode has separate ray-length and sampling controls. The depth tolerance represents a general approximation rather than knowing each object’s true thickness. The documented miss options conflict with the overview’s broad reflection-probe limitation, so the mechanism is evidence for explicit miss policies, not verified combination support.

**Proposed use:** ADOPT: every tracing pass declares its miss semantics and maximum work. Differentiate a genuine open-sky hit from failure to find geometry. In caves, missing coverage must not silently become a sky sample. Profile denoising and reconstruction with the rays, rather than reporting trace cost alone.

**Evidence limit:** Do not treat a quality slider as a predictable total cost. The documentation's ray/bounce controls describe this implementation, not Deep Primal budgets or physics accuracy.

### PROD-06 — Godot: Signed distance field global illumination (SDFGI)

**Source:** [Signed distance field global illumination (SDFGI)][PROD-06] · Official stable engine documentation

**Observed:** SDFGI follows the camera with cascaded fields and supports changing lights, but its documented static-geometry capture does not continuously respond to nearby moving occluders or emissive meshes. A changed static mesh needs cascade regeneration or a system refresh. More cascades, smaller cells, and faster updates increase cost; convergence takes time and transitions can become visible. It requires Godot’s Forward+ renderer rather than its Mobile or Compatibility paths.

**Proposed use:** ADAPT: spatially bounded camera-centered visual data and explicit convergence diagnostics. REJECT for our editable-world baseline: waiting for camera movement to repair edited occlusion. If a distance-field tier is built, edits must enqueue conservative local rebuilds with immediate fallback and a measured completion bound.

**Evidence limit:** Procedural-world support is not equivalent to support for arbitrary nearby dynamic occluders. Do not cite this as an OpenGL fallback implementation.

### PROD-07 — Godot: Using Voxel global illumination

**Source:** [Using Voxel global illumination][PROD-07] · Official stable engine documentation

**Observed:** VoxelGI precomputes a bounded scene volume, then supports real-time lights and explicitly dynamic meshes. Changing captured static geometry requires another bake. Higher subdivision increases detail, bake/storage cost, and runtime expense; excessive volume extents reduce useful detail. Dynamic mesh contribution is slower than static capture. Bias trades self-occlusion against leaking, and half-resolution rendering can lose thin features. The implementation targets small/medium scenes and requires Godot’s Forward+ renderer.

**Proposed use:** ADAPT: the distinction between static capture, dynamic contributors, and receivers. Use chunk-aligned update regions and an occupancy representation obtained from our world data if researching voxel GI. Require thin-wall and moving-door tests at every cell size. DEFER: a monolithic world-sized voxel radiance volume.

**Evidence limit:** A real-time lighting label does not mean free real-time topology updates. The source’s custom-shader/material bake limitations are engine-specific and should not be generalized to all voxel methods.

### PROD-08 — Godot: LightmapGI

**Source:** [LightmapGI][PROD-08] · Official stable API/reference documentation

**Observed:** LightmapGI stores static baked lighting with low runtime cost and can light dynamic receivers through probes. Its lightmaps are not rebaked in an exported project, so the documented solution is unsuitable for continuously generated or user-built levels. Baking relies on appropriate geometry/UV data. The page distinguishes bake-time requirements from runtime use, including suitability for less powerful devices.

**Proposed use:** ADAPT: lightmaps for fixed test scenes, reference assets, or explicitly immutable structures only. For mutable terrain, the performance fallback must be recomputable world lighting rather than an obsolete bake. A moving object receiving a bake does not establish that it changes the baked visibility.

**Evidence limit:** Do not compare its static-scene runtime cost with dynamic GI as though both solve the same workload. Baked reference images remain useful even when baking is unsuitable for gameplay.

### PROD-09 — Filament: Physically Based Rendering in Filament

**Source:** [Physically Based Rendering in Filament][PROD-09] · Official developer design documentation

**Observed:** Filament documents a coherent material/light-unit pipeline, pre-exposure for limited-precision buffers, indirect diffuse/specular image-based lighting, and clustered forward light assignment. Its implementation discussion includes GPU and CPU assignment paths, with per-cluster light lists. It validates results using scene-referred diagnostics and reference rendering. Distant environment maps are an approximation of surrounding illumination; their acquisition and preprocessing are separate tasks rather than a general dynamic-world GI solution.

**Proposed use:** ADOPT: a small calibrated BRDF, explicit units/encoding, controlled pre-exposure, and debug views before advanced GI. ADAPT: bounded clustered or tiled light lists with CPU construction as a compatibility option. Require list-overflow behavior and compare a selected-light result against an uncapped fixture.

**Evidence limit:** This design document includes historical assumptions and TODOs. Do not repeat its historical comparisons as current claims about other engines. The proposed CPU path still needs profiling in Java; no performance gain is presumed.

### PROD-10 — O3DE Atom: Diffuse Probe Grid Component

**Source:** [Diffuse Probe Grid Component][PROD-10] · Official component documentation

**Observed:** Atom’s diffuse probe grid uses ray-traced samples around probes to build irradiance textures. Real-time mode responds to surrounding geometry and lights and requires supported ray-tracing hardware/API capability. Probe spacing controls fidelity, memory, and work. Baked mode consumes explicitly captured textures, while Auto Select chooses between real-time and baked modes. A usable baked fallback must actually have been produced; selecting that mode alone does not manufacture valid scene lighting.

**Proposed use:** ADAPT: an optional probe tier with separate tracing, visibility, convergence, and receiver-sampling contracts. Keep platform selection truthful. For our editable world, fall back to current voxel lighting when probes are absent or stale; never auto-select a bake that predates terrain edits.

**Evidence limit:** The component documents DXR or Vulkan-RT for real-time use. This is not evidence that the same probe tracing runs through our OpenGL path, nor a benchmark for the owner’s GPU.

### PROD-11 — O3DE Atom: Open 3D Engine 22.05.0 release notes — DiffuseProbeGrid changes

**Source:** [Open 3D Engine 22.05.0 release notes — DiffuseProbeGrid changes][PROD-11] · Official historical release notes

**Observed:** These historical release notes document camera-following scrolling probe grids, adjustable rays per probe, a position/direction irradiance query, improved overlapping-grid/IBL blending, and probe-value visualization. The source establishes that these design mechanisms were implemented in that release. It does not describe their complete present-day algorithm, convergence policy, or memory bounds.

**Proposed use:** ADAPT: scroll a bounded optional cache instead of allocating it across the entire terrain-LOD distance. Newly exposed or reassigned cells require new spatial identity and invalid history. Add query/visualization interfaces so apparent GI quality can be inspected independently of tone mapping.

**Evidence limit:** Historical mechanism reference only. Do not label these release-note statements as a verified current API or use them to infer support matrices.

### PROD-12 — CryEngine: Voxel-Based Global Illumination (SVOGI)

**Source:** [Voxel-Based Global Illumination (SVOGI)][PROD-12] · Official CRYENGINE V documentation; historical implementation details

**Observed:** SVOGI builds voxel scene data incrementally on the CPU and traces it on the GPU. Its documented opacity-only mode takes bounced lighting from reflective shadow maps, while richer modes store additional material/radiance data at greater memory cost. The modes have different bounce, specular, and dynamic-object capabilities. The page lists delayed updates, light leaks, teleport convergence, and missing GI on some forward-rendered components such as water/particles. Its timing/memory examples describe historical configurations.

**Proposed use:** ADAPT: avoid storing material/radiance channels until the selected technique needs them. Explicitly qualify water, foliage, particles, and moving objects rather than assuming opaque-terrain GI covers them. DEFER: adopting a radiance-injection mode before update cost and double-counting rules are measured.

**Evidence limit:** Do not reuse its historical millisecond or megabyte figures as current budgets. 'Voxel' does not imply exact block visibility or a complete dynamic-geometry solution.

### PROD-13 — Frostbite: Precomputed Global Illumination in Frostbite

**Source:** [Precomputed Global Illumination in Frostbite][PROD-13] · Original developer presentation, Yuriy O’Donnell, GDC 2018; official EA-hosted PDF

**Observed:** The presentation describes Flux path tracing for static lightmaps/irradiance volumes, directional encoding with spherical harmonics, and practical atlas/filtering work. It explicitly accounts for interpolation footprints when assigning chart texels and packing data. Proxy geometry and static authoring are part of that production pipeline. The talk separates a CPU baking solution from GPU work intended to improve interactive author workflows.

**Proposed use:** ADOPT: a reproducible offline reference path and interpolation-aware cache borders. Treat padding, filtering footprints, and invalid texels as correctness concerns. DEFER: full-world lightmaps for editable terrain. A fast authoring preview is not evidence that a shipped mutable world can retain those baked results after edits.

**Evidence limit:** Historical static-GI architecture, not a statement about every current Frostbite game. The document is 119 pages; relevant sections inspected include method, representation, and bilinear/atlas handling.

### PROD-14 — Frostbite: Moving Frostbite to Physically Based Rendering V3 — author publication page

**Source:** [Moving Frostbite to Physically Based Rendering V3 — author publication page][PROD-14] · Primary author publication page with course outline and corrections, Sébastien Lagarde, 2015

**Observed:** The authors organize their production-renderer account around validating models, validating in-engine approximations, materials, light units, direct/indirect lighting, occlusion, exposure, and large dynamic ranges. The publication page records revisions and corrections to the course notes. Its structure is evidence that a lighting design needs a validation methodology and a consistent imaging pipeline in addition to a GI algorithm.

**Proposed use:** ADOPT: separate tests of the intended shading model from tests of its optimized implementation and final artistic appearance. Maintain a correction log and documented approximations. The project's named scenes should make every accepted shortcut visible and reviewable.

**Evidence limit:** The linked full course PDF exceeded the web fetch size cap; only the author page, outline, and correction notes were inspected here. Do not cite equations or detailed paper claims from this limited read.

## 3. GI, sampling, and reconstruction

These references solve different stages: transport, sample selection, cache representation, or reconstruction. The master specification keeps their contributions and integration prerequisites distinct.

### G01 — Dynamic Diffuse Global Illumination with Ray-Traced Irradiance Fields

**Source:** [Dynamic Diffuse Global Illumination with Ray-Traced Irradiance Fields][G01] · Original research author publication page

**Observed:** Majercik and colleagues describe a dynamically updated irradiance field with compact probes, geometric ray sampling, and visibility-aware interpolation based on distance moments. The central product is diffuse indirect illumination. Its geometric and interpolation approximations matter independently of the number of rays used. The author page links the paper and reference GLSL implementation; it should be used to understand the method separately from later SDK packaging.

**Proposed use:** Prototype visibility-aware diffuse probes with explicit position, directional storage, visibility, age, and source/geometry revisions. Compare against thin-wall and source-removal fixtures.

**Evidence limit:** Diffuse GI purpose; not complete physical transport Deep Primal performance not measured Inspection scope: Author abstract and linked-material description inspected.

### G02 — RTXGI-DDGI official SDK

**Source:** [RTXGI-DDGI official SDK][G02] · Official source repository

**Observed:** The RTXGI-DDGI repository supplies an implementation aimed at GPU ray-tracing APIs, including DXR and Vulkan ray tracing. Its documentation also discusses baking probe information on a capable machine for use elsewhere. That static-data workflow does not establish dynamic GI for arbitrary edits on hardware lacking the tracing path. The associated changelog documents scrolling volumes, probe relocation/classification, and fixes for newly scrolled probe data.

**Proposed use:** Qualify the chosen SDK path independently of the algorithm. A static baked fallback cannot describe arbitrary new excavation; newly scrolled cells need new identity and invalid history.

**Evidence limit:** Do not confuse with newer NRC/SHaRC RTXGI repository Static data on non-RT platforms is not dynamic editable-world GI support Inspection scope: Official README inspected.

### G03 — Spatiotemporal reservoir resampling for real-time ray tracing with dynamic direct lighting

**Source:** [Spatiotemporal reservoir resampling for real-time ray tracing with dynamic direct lighting][G03] · Original research author publication page

**Observed:** ReSTIR DI applies reservoir resampling to candidates for direct illumination, sharing useful samples across space and time. The paper develops both unbiased and biased estimators; the latter trades some energy accuracy for lower noise. This distinction prevents calling every implementation unbiased. The work addresses sampling many dynamic sources, while visibility and shading still have to be evaluated by the renderer. Its measured examples are evidence about those examples, not predicted performance for this project.

**Proposed use:** Consider direct-light reservoirs only after actual emitter counts justify them. Preserve source generation, revalidate visibility, and compare estimator bias, variance, and total reconstruction cost.

**Evidence limit:** Source-reported quality and speed numbers do not predict this project Selected-light visibility still required Inspection scope: Author abstract and paper-indexed excerpts inspected.

### G04 — ReSTIR GI: Path Resampling for Real-Time Path Tracing

**Source:** [ReSTIR GI: Path Resampling for Real-Time Path Tracing][G04] · Original research author publication page

**Observed:** ReSTIR GI reuses multi-bounce indirect paths across pixels and frames. Its proposal begins with paths generated by a path tracer, then shares useful transport samples through resampling. The paper evaluates the resulting reconstruction with a denoiser. This is a separate transport task from selecting direct emitters. The original publication’s quality improvements are tied to its scenes, settings, estimators, and measurements.

**Proposed use:** Defer path reuse until the underlying transport, material, and geometry pipeline is correct. A direct-light sampling integration does not automatically supply global illumination.

**Evidence limit:** Distinct from direct-light selection Do not equate original GI algorithm with all later PT variants Inspection scope: Author abstract inspected.

### G05 — RTXDI official SDK and samples

**Source:** [RTXDI official SDK and samples][G05] · Official source repository

**Observed:** The inspected RTXDI repository exposes DI, GI, and PT shader integration folders and sample applications. Its README distinguishes the DI sample path from broader integrations. The sample stack uses native graphics infrastructure and shader toolchains; Vulkan availability does not make it a Java or OpenGL drop-in. Pin the chosen runtime, sample dependencies, and shader compiler versions rather than treating the repository name as a stable integration contract.

**Proposed use:** Pin the exact shader/runtime integration and native dependencies. Treat DI, GI, and PT as separate configured techniques behind the project's contribution and lifetime contracts.

**Evidence limit:** No Java or OpenGL drop-in claim Moving branch must be pinned before adoption Inspection scope: Official README inspected.

### G06 — FidelityFX Brixelizer GI 1.0.1

**Source:** [FidelityFX Brixelizer GI 1.0.1][G06] · Official technical manual

**Observed:** The inspected Brixelizer GI 1.0.1 manual describes a compute implementation using sparse distance fields and G-buffer inputs. Because the distance field lacks material information, prior lighting populates a radiance cache. Screen probes trace the distance field and feed a world-space irradiance cache using spherical harmonics; diffuse and specular outputs have separate resolution paths. Feeding direct lighting produces a one-bounce input; feeding the preceding composed result propagates additional bounces over repeated updates.

**Proposed use:** Run as a compute GI competitor. Measure distance-field construction and radiance refresh separately; require a current response to unseen edits and an explicit specular-output ownership rule.

**Evidence limit:** Previous-frame radiance and visible probe placement constrain coverage No measured project performance Native shader requirements require backend qualification Inspection scope: Detailed algorithm overview and integration sections inspected.

### G07 — Radiance Cascades original manuscript source

**Source:** [Radiance Cascades original manuscript source][G07] · Original author's work-in-progress manuscript

**Observed:** Radiance Cascades organizes radiance intervals so near-field levels have finer spatial detail and distant levels allocate more angular detail. The author’s text discusses 2D, screen-space, and world-space constructions. Its Path of Exile 2 example uses a screen-space hierarchy suitable for that camera setting. The stated limitations include missing offscreen contributors/occluders in that representation and substantial practical constants for 3D computation. Representation and parameter choices remain material to cost despite attractive asymptotic scaling.

**Proposed use:** Require full 3D, free-camera visibility, streamed boundaries, and measured constants before promotion. Screen-space or 2D demonstrations do not settle editable-world feasibility.

**Evidence limit:** Not journal-published; see S03 2D/screen-space success does not establish full 3D free-camera feasibility Storage and tracing representations are separate Inspection scope: Manuscript overview and limitations inspected.

### G08 — Interactive Indirect Illumination Using Voxel Cone Tracing

**Source:** [Interactive Indirect Illumination Using Voxel Cone Tracing][G08] · Original research author publication page

**Observed:** The original method uses a dynamically updated voxel hierarchy and filtered values to approximate light transport with cone-shaped queries. Query footprint determines the hierarchy level. The paper describes compact averaged visibility, and identifies the absence of view-dependent visibility in that representation as a problem for some large thin geometry. It is an approximation to transport, not an exact path-tracing estimator obtained simply by increasing cone count.

**Proposed use:** Define occupancy/transmittance/radiance meanings and mip aggregation before tracing cones. Preserve thin barriers and small emitter footprints; measure all affected hierarchy updates.

**Evidence limit:** Detailed visibility caveat supported by original PDF indexed excerpt S04 No claim of contemporary performance Inspection scope: Author page and original-paper indexed excerpts inspected.

### G09 — Spatiotemporal Variance-Guided Filtering

**Source:** [Spatiotemporal Variance-Guided Filtering][G09] · Original research author publication page

**Observed:** Spatiotemporal Variance-Guided Filtering accumulates noisy path-traced lighting over time and uses estimated luminance variance to guide hierarchical image-space filtering. Its purpose is to reconstruct a useful image from low sample counts while preserving detail. The research page links implementation material. A reconstruction algorithm should be evaluated as part of the full transport pipeline, because its temporal input and filtering decisions affect appearance as well as noise.

**Proposed use:** Use as a filtering-design reference. Capture raw and filtered lighting, then measure noise, lag, blur, and disocclusion separately; filtering cannot discover an absent occluder.

**Evidence limit:** A reconstruction method, not a visibility or transport generator Historical performance is not a current budget Inspection scope: Author abstract inspected.

### G10 — NVIDIA Real-Time Denoisers integration README

**Source:** [NVIDIA Real-Time Denoisers integration README][G10] · Official integration documentation

**Observed:** The inspected NRD README describes an API-agnostic spatiotemporal denoising library and documents normal, roughness, view-depth, motion, radiance, and hit-distance inputs. Diffuse and specular signals have distinct semantics; hit distance is not simply camera depth. Integration paths include native APIs and NVIDIA’s abstraction layer. Correct packing, motion conventions, radiometric treatment, and restart behavior are integration requirements rather than incidental sample details.

**Proposed use:** Adopt a signal descriptor before choosing a denoiser. Give water and transparency meaningful depth/motion/hit-distance guides or a qualified fallback.

**Evidence limit:** Opaque guide assumptions do not automatically cover water/transparency License is separate SDK terms, see S09 Target hardware untested Inspection scope: Detailed README input and integration requirements inspected.

### G11 — Real-Time Neural Radiance Caching for Path Tracing

**Source:** [Real-Time Neural Radiance Caching for Path Tracing][G11] · Original research author publication page

**Observed:** Neural radiance caching trains a compact radiance representation online during path tracing, using current rendering data rather than requiring a separately trained fixed scene model. The research addresses changing lights, materials, and geometry. Cached predictions allow some paths to terminate early; this introduces approximation and makes cache quality part of the transport result. The publication is a research method, not a requirement that every implementation use one vendor’s current SDK.

**Proposed use:** Keep learned caches as an optional research direction. Test cold start, unseen changes, removed-source persistence, training coverage, and approximation bias.

**Evidence limit:** Research method distinct from current SDK hardware requirements No Deep Primal cold-start or accuracy results Inspection scope: Author page inspected.

### G12 — RTXGI Neural Radiance Cache integration guide

**Source:** [RTXGI Neural Radiance Cache integration guide][G12] · Official SDK guide

**Observed:** The NRC guide separates training-path generation from full-resolution query paths and prediction/resolve work. The integration assumes an existing path tracer and acceleration structures. It documents an early-termination threshold that affects the bias/noise tradeoff, and distributes native NRC binaries with CUDA runtime dependencies. The associated RTXGI repository separately lists NRC and SHaRC requirements; they are not interchangeable names for DDGI.

**Proposed use:** The inspected SDK's platform prerequisites exclude it from the AMD baseline. Any alternative implementation needs independent hardware, integration, and quality evidence.

**Evidence limit:** Not a standalone GI plugin for arbitrary renderer Source-vendor prerequisites in S05 rule out owner's AMD baseline Inspection scope: Detailed guide and requirements inspected.

### G13 — pbrt-v4 reference implementation

**Source:** [pbrt-v4 reference implementation][G13] · Author-maintained source repository

**Observed:** pbrt-v4 provides a physically based ray tracer with CPU rendering and an optional GPU path based on CUDA/OptiX. Its spectral/material choices are relevant when comparing to a game renderer. The CPU path avoids making an NVIDIA GPU a prerequisite for offline reference generation. The source’s Apache-2.0 identifier is verified independently; that does not assign the same license to the book or every scene asset.

**Proposed use:** Use a small matched CPU-reference workflow early. Do not assume its spectral/material choices equal the game's RGB shading without an explicit conversion and parity test.

**Evidence limit:** No AMD GPU path inferred from GPU feature Scene assets and textbook licensing differ from source SPDX Inspection scope: Official README inspected.

### G14 — Physically Based Rendering, 4th edition — Monte Carlo Basics

**Source:** [Physically Based Rendering, 4th edition — Monte Carlo Basics][G14] · Original author textbook

**Observed:** The book’s Monte Carlo discussion distinguishes estimator bias from variance and explains the limits of finite-sample estimates. A high-sample render is therefore a reference estimate, not proof of an exact answer. Increasing sample count and comparing independent sample sets helps expose inadequate convergence; rare paths can remain difficult even when a simple local estimate of variance appears small.

**Proposed use:** Treat a reference image as a converging estimate. Compare independent seeds and increased sample counts; distinguish model mismatch, bias, and residual variance.

**Evidence limit:** High-sample image is a reference estimate, not an exact oracle Proposed sample-doubling workflow is project inference Inspection scope: Author text inspected.

## 4. Direct-light, reflection, and atmosphere foundations

### RND-01 — Clustered shading

**Source:** [Clustered Deferred and Forward Shading][RND-01] · Original authors' university publication record, 2012

**Observed:** The paper groups shading samples in three-dimensional clusters and associates relevant lights with those groups. Position and normal information can tighten assignment relative to purely two-dimensional tiles. Its reported results belong to the paper's scenes and implementation. The inspected university record and abstract establish the method; the publisher page and the separate virtual-shadow paper were not available through the retrieval route.

**Proposed use:** ADAPT: use bounded light lists with explicit overflow, large-light handling, and transparent-surface coverage. Benchmark CPU and GPU list construction in the actual host. Cluster assignment reduces candidate evaluation; visibility and shadow generation still require their own budgets.

**Evidence limit:** No original timing number is used as a Deep Primal estimate. The full clustered-shadow paper was not read; no implementation detail is attributed to it.

### RND-02 — Unreal MegaLights

**Source:** [MegaLights in Unreal Engine][RND-02] · Official documentation, page labeled Unreal Engine 5.8 when inspected

**Observed:** MegaLights samples direct lighting stochastically with a fixed number of rays per pixel and reconstructs the result with a denoiser. The documentation warns that increasingly complex overlapping lighting can cause blur, noise, or ghosting. It recommends limiting influence bounds. Its renderer, geometry, and platform support have explicit restrictions, including unsupported water in the inspected limitations. This is a particular production integration rather than a general promise of unlimited lights.

**Proposed use:** PROTOTYPE LATER: stochastic many-light direct sampling only after conventional bounded light lists become a measured bottleneck. Require temporal captures, abrupt-light-change tests, and a separate water path. A fixed ray budget must expose its quality loss as complexity rises.

**Evidence limit:** The separate 2025 conference slide PDF exceeded retrieval size limits. No claim is based on reading that entire slide deck. Do not transfer the integration's capabilities or timing to Deep Primal.

### RND-03 — XeGTAO

**Source:** [XeGTAO README][RND-03] · Original Intel implementation documentation; archived repository

**Observed:** XeGTAO is a screen-space ambient-occlusion implementation with depth preparation, occlusion evaluation, and spatial filtering. Its author describes tuning heuristics against ray-traced references. Temporal anti-aliasing can improve the result but is not an absolute dependency. The repository presents an HLSL/DirectX implementation under MIT and was archived in April 2024. The provided timings refer to its own configurations.

**Proposed use:** ADAPT: optional near-field ambient occlusion with an explicit receiver and radius. Use fixed-exposure reference comparisons and temporal motion tests. Keep contact darkening separate from logical illumination, directional shadows, and indirect-light transport; avoid multiplying every existing occlusion term together.

**Evidence limit:** No dependency selection or source import is made. The implementation would need backend translation and current performance qualification.

### RND-04 — AMD FidelityFX SSSR

**Source:** [FidelityFX Stochastic Screen-Space Reflections 1.5][RND-04] · Official AMD technical manual

**Observed:** SSSR uses screen depth, normals, roughness, and a lit color input to produce glossy reflections. Its manual specifies a direct-lit HDR input and an environment/probe fallback for rough surfaces or missed screen-space rays. It exposes hierarchical depth traversal, ray/tile classification, variance history, and roughness-convention controls. Integration therefore requires agreement about buffer meaning, missing information, and who supplies the reflected-light contribution.

**Proposed use:** ADAPT: confidence-based reflection selection with a valid world-space fallback. For caves, a screen-space miss cannot by itself certify open sky. Keep the exact provider's input contract explicit so already-integrated reflections or indirect terms are not recursively counted.

**Evidence limit:** Screen-space data alone cannot recover every off-screen occluder. SDK-wide Vulkan availability is not proof that any particular feature/backend combination is qualified in this project.

### RND-05 — Linearly Transformed Cosines

**Source:** [Real-Time Polygonal-Light Shading with Linearly Transformed Cosines][RND-05] · Original authors' SIGGRAPH 2016 project page

**Observed:** The authors approximate a material's angular reflectance with linearly transformed cosine distributions and integrate them over polygonal light domains. A change of variables reduces the integral to a form with an analytic expression. The page demonstrates approximation of GGX lobes and describes the remaining approximation error. It links fitting code, validation, and a demonstration; the inspected page concerns angular light integration.

**Proposed use:** PROTOTYPE LATER: efficient rectangular or polygonal emitters for large lamps and furnaces. Treat emitter shape, intensity, and visibility as separate inputs. Integrating an unoccluded area light does not supply the scene occlusion needed for a matching soft shadow.

**Evidence limit:** The source is not evidence for free shadowed area lights. No fitting tables or demo code were imported.

### RND-06 — Hillaire sky and atmosphere

**Source:** [Unreal Engine Sky Atmosphere Rendering Technique — reference implementation][RND-06] · Original author's reference project for the 2020 atmosphere paper

**Observed:** The reference project accompanies the 2020 atmosphere technique and includes lookup-table generation, a ray-marched atmosphere, and a volumetric path-tracing comparison. It can compare the new method with Bruneton's implementation. The README identifies the relevant shaders and describes Windows-only testing of this sample. The full paper exceeded the retrieval limit, so the note relies on the inspected project documentation.

**Proposed use:** ADAPT: a bounded sky/atmosphere service with reference comparisons and explicitly shared environmental inputs. Qualify the sky, aerial perspective, local fog, underwater transport, and distant terrain together. Keep sky-model changes separate from logical skylight propagation.

**Evidence limit:** This sample does not establish general local-cloud, cave-fog, or underwater correctness. Its sample backend and test environment do not establish Deep Primal support.

### RND-07 — PBRT radiometric foundations

**Source:** [Physically Based Rendering, Fourth Edition — Radiometry][RND-07] · Original authors' open textbook chapter

**Observed:** The chapter distinguishes radiant flux, intensity, irradiance, and radiance and states the simplifying assumptions of geometric-optics light transport. It treats linearity and conservation of energy as basic principles. These quantities describe different aspects of light and cannot be interchanged merely because each is represented by a numeric value. Its physical model also makes clear that a complete spectrum contains information beyond a display RGB triplet.

**Proposed use:** ADOPT: typed units and calibrated conversions at physical interfaces. Keep discrete gameplay light, artistic RGB, display exposure, physical irradiance, and heat transfer distinct. Test the chosen RGB shading approximation against its declared model instead of describing quantized flood light as physically accurate transport.

**Evidence limit:** No thermal model, spectral renderer, or physical gameplay irradiance calibration is implemented by this document.

## 5. Supporting evidence and license declarations

These entries support a specific capability, limitation, publication status, or observed license declaration. They do not constitute a dependency selection or a legal compatibility assessment. No external implementation or asset was imported.

| ID | Primary source | Observed fact | Boundary |
| --- | --- | --- | --- |
| S01 | [AMD FidelityFX Brixelizer/GI overview][S01] | DX12 and Vulkan SDK backends; Compute without hardware-accelerated RT; Officially labeled open source MIT; Sparse distance fields generated from triangle geometry | Does not establish OpenGL SDK support; Do not promote marketing multiple-bounce wording to zero whole-pipeline cost |
| S02 | [GDC 2024 GI with AMD FidelityFX Brixelizer][S02] | Slide 56 identifies screen-space radiance injection limits; Unseen changes require view coverage or additional injection; Small-scale detail loss identified | Versioned 2024 implementation evidence; not a universal algorithm limit; PDF limitations slide retrieved through indexed text |
| S03 | [Radiance Cascades manuscript repository][S03] | Work in progress; Author says never sent to a journal despite journal template | Do not present as peer-reviewed journal publication; No blanket reuse-license statement for all paper/demo materials |
| S04 | [Interactive Indirect Illumination Using Voxel Cone Tracing — authors PDF][S04] | Hierarchical voxel filtering and cone-footprint selection; Compact average visibility lacks view dependence and can mishandle thin geometry | Direct full-PDF extraction failed due size; relied on indexed original text; Fine implementation details require subsequent full read |
| S05 | [Current RTXGI NRC/SHaRC repository][S05] | NRC and SHaRC are separate integrations; Inspected requirements specify NRC NVIDIA Turing or newer; SHaRC has different requirements | Do not apply NRC restriction to every possible neural-cache implementation; Do not confuse current RTXGI with RTXGI-DDGI; SHaRC guide not fully studied |
| S06 | [RTXGI-DDGI changelog][S06] | Vulkan support and scrolling volumes documented; Probe relocation/classification and scrolling-data fixes | Historical additions/fixes are not proof all current integration cases work; Pin actual implementation revision |
| S07 | [RTXGI license file][S07] | Contains NVIDIA RTX SDKs license and supplement | Not MIT metadata; No legal interpretation or completed dependency audit claimed |
| S08 | [RTXDI license file][S08] | Contains NVIDIA SDK terms and RTX supplement | Not MIT metadata; No legal interpretation or completed dependency audit claimed |
| S09 | [NRD license file][S09] | Contains NVIDIA RTX SDKs license and supplement | Not MIT metadata; No legal interpretation or completed dependency audit claimed |
| S10 | [pbrt source SPDX identifier][S10] | Apache-2.0 source SPDX identifier | Does not assign source license to book or scene assets |
| S11 | [Falcor research framework][S11] | Render graph and path-tracing research infrastructure; D3D12/Vulkan support listed | Build/platform/dependency requirements need qualification; Not selected for Deep Primal; exact license not classified here |
| S12 | [Hart — Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces][S12] | Safe sphere tracing uses geometric distance and derivative bounds; Surface nonpenetration guarantee depends on the bound | No proof that Deep Primal V8 density is a metric SDF; Project interpolation/transforms/quantization/LOD/edit validity require separate proof |
| LX-01 | [LICENSE][LX-01] | LGPL version 3 text | No SPDX suffix inferred from generic license text. |
| LX-02 | [Source SPDX header][LX-02] | LGPL-2.1-or-later in inspected file | Other components need their own verification. |
| LX-03 | [Cargo.toml license declaration][LX-03] | Workspace package GPL-3.0-or-later | Official mirror; component and dependency licenses still separate. |
| LX-04 | [LICENSE.md][LX-04] | MIT license text | Component/dependency review before imports. |
| LX-05 | [license.txt][LX-05] | Proprietary source-readable terms oriented to Vintage Story mods | Do not treat as unrestricted reuse permission or as another repository's license. |
| LX-06 | [license_header.txt][LX-06] | GNU LGPL version 3 in project source header | Official search extraction contains header; live HTML returned Loading. |
| LX-07 | [LICENSE.txt][LX-07] | LGPL version 3 text | Use license text rather than inconsistent introductory README GPL wording. |

A public repository does not imply MIT or unrestricted code reuse. Future implementation work pins a revision and checks the actual shipped files, dependencies, assets, and notices. In particular, the Vintage Story API's source-readable terms and the NVIDIA SDK license/supplement records must not be relabeled from a repository's mere visibility.

## 6. Backend and imaging references

These foundations were already used in the prior specification and remain relevant to the proposed renderer.

| ID | Source | Relevance |
| --- | --- | --- |
| FND-01 | [Khronos Vulkan Guide — Synchronization Examples][FND-01] | Producer/consumer visibility and ownership must match actual access and queue use. A dedicated transfer queue is optional, not a universal requirement. |
| FND-02 | [Khronos Vulkan Specification — Images][FND-02] | sRGB texture formats have defined decoding behavior. Light levels, normals, depth, and other numeric data need explicit non-color interpretation. |
| FND-03 | [Khronos Vulkan Guide — Depth][FND-03] | Clip-space/depth conventions require explicit agreement between backend transforms, rasterization, comparisons, and reconstruction. |
| FND-04 | [Khronos glTF 2.0 material specification][FND-04] | An explicit material contract includes color interpretation, normal conventions, roughness/metallic semantics, and alpha modes. glTF is a reference, not a required project asset format. |
| FND-05 | [Microsoft — Cascaded Shadow Maps][FND-05] | Directional-shadow coverage, split placement, stable projections, and transition behavior involve precision and shimmering tradeoffs. The reference API is Direct3D; the principles require backend adaptation. |
| FND-06 | [AMD GPUOpen — FSR Super Resolution integration][FND-06] | Temporal integration depends on motion, depth, exposure, reactive content, and reset conventions. These requirements must match the selected provider version. |

## 7. Evidence limits and unresolved research

### Source boundaries retained

- **Starlight and Phosphor:** archived implementation history. Starlight's technical comparisons explicitly cover older Minecraft versions; they do not establish current host speedups.
- **Distant Horizons:** the inspected generated API supports the stated interchange and metadata claims. Internal light solver and codec sources were not retrievable here, so private packing and scheduling claims were excluded.
- **Vintage Story:** public API/material-loading observations do not expose its complete private lighting engine.
- **Voxel Tools:** researched for meshing, data access, and scheduling. The inspected pages do not establish a complete dynamic flood-light solver.
- **Unity HDRP 17.0.4:** APV/SSGI pages contain conflicting fallback statements. The notebook preserves that uncertainty; a selected package needs direct integration verification.
- **Veloren, CryEngine, and Frostbite:** historical mechanisms are identified as historical. Their published performance examples are not current Deep Primal targets.
- **Frostbite PBR and the atmosphere paper:** full large PDFs were unavailable to text extraction in this pass. The cited author outline/corrections and original atmosphere repository support only their inspected content.
- **Voxel cone tracing:** original author-page and indexed-paper evidence were available; full-paper extraction was limited. Detailed implementation adoption needs a complete follow-up read.
- **MegaLights:** the official documentation was inspected. The large 2025 conference slide deck was not read in full.
- **Clustered shading:** the original authors' university abstract/record was inspected. The separate clustered virtual-shadow paper was inaccessible; no detailed shadow algorithm is inferred from it.
- **Radiance Cascades:** manuscript and repository evidence preserve the author's publication status and representation-specific limitations.
- **SHaRC, newer ReSTIR PT variants, accurate caustics, and spectral media:** mentioned only where needed to distinguish scope; not researched deeply enough here to select an implementation.
- **Other engines, including undisclosed proprietary systems:** absence from this selected survey is not a quality judgment. No claim of universal coverage or a cross-engine benchmark ranking is made.

### What still needs real project evidence

No source establishes performance on Deep Primal's owner GPU/driver, Java/native boundary, world sizes, edits, water, terrain generator, or host render pipeline. Repository branch URLs and “stable” documentation can change; record exact commits/package versions when implementing.

The priority unknowns are the actual GPU memory envelope, sustainable edit rate, world-space radiance coverage, minimum supported backend features, acceptable convergence latency, and source/material calibration. The master specification turns each into a fixture or promotion gate rather than filling the gap with a guessed number.

A signed-distance-field experiment additionally needs proof of safe stepping. Multiplying a true distance function by two preserves the represented zero surface but can make a raw step too large. This simple counterexample shows why density-field shape and conservative traversal distance are different contracts. Hart's original abstract supports the distance/bound requirement; its application to V8 terrain is our reasoning. [S12][S12]

## 8. Research-to-requirement traceability

| Research lesson | Master contract | Evidence/gate |
| --- | --- | --- |
| Carried propagation work and removal races | Sections 5.4 and 6.6 | Tests 33–35; independent oracle |
| Unknown neighbors and incomplete faces | Sections 7.1–7.4 | Tests 7–9 and 22 |
| Snapshot and publication pressure | Sections 7.3 and 12 | Tests 17, 30, 57, and 60 |
| Native/material translation | Sections 4–5 and 14 | Tests 5, 23, and 32 |
| LOD structural light and coordinates | Sections 9.1.1 and 10 | Tests 15 and 24 |
| One owner per light contribution | Section 3.3 | Test 39 |
| Coherent BRDF, units, exposure | Sections 9.1.4–9.1.6 | Tests 29, 38, and 47 |
| Bounded local-light assignment | Section 9.3.1 | Tests 36–37 and 59 |
| Shadow invalidation economics | Sections 9.2–9.3.2 | Tests 28 and 53 |
| Geometry versus radiance coverage | Section 9.6 | Tests 40–45 |
| Reflection miss semantics | Section 9.7 | Test 46 |
| Water/media path ownership | Sections 8.2 and 9.7–9.8 | Tests 14, 26, 48, and 54 |
| Temporal signal validity | Sections 9.4 and 9.9 | Tests 49–51 and 58 |
| Complete hardware fallback | Section 9.10 | Tests 18 and 51 |
| Real resource footprint | Sections 12.4–12.6 | Tests 55, 57, and 60 |
| Independent reference quality | Section 15.3 | Test 56 |
| Comparable prototype choice | Sections 18–19 | Experiments E01–E07 and promotion evidence |

These are traceability links to proposed requirements. They do not indicate passing implementations.

## 9. Working glossary

| Term | Meaning in this design |
| --- | --- |
| Authoritative light | The world/server answer consumed by gameplay under the selected profile |
| Voxel flood field | A discrete local propagation approximation; not calibrated physical radiance |
| Direct lighting | Light reaching a receiver from a source without an intervening diffuse reflection |
| Indirect diffuse | Diffusely reflected transport arriving after other scene interactions |
| Indirect specular | Reflected environment/scene contribution resolved for a specular response |
| Radiance coverage | Which sources/surfaces have actually supplied usable light data |
| Visibility coverage | Which geometry and spatial domain a query can reliably test |
| Irradiance | Incident radiant power per unit area; distinct from directional radiance |
| SDF | Signed distance representation; safe tracing additionally requires a conservative distance/bound contract |
| Probe | A spatial sample of directional/averaged lighting and associated visibility/validity data |
| Cascade / clipmap | Bounded regions at different spatial scales, often centered or scrolled around the view |
| Reservoir | A compact sampled-candidate state used by a specified resampling estimator |
| Denoiser | Reconstruction of an input signal; not an independent source of light or geometry |
| Pre-exposure | A controlled scaling used within the lighting pipeline; history must use compatible scale |
| History validity | Proof that old samples remain meaningful under current geometry/source/signal identity |
| Certified escape | A trace has enough valid coverage to justify reaching the declared environment boundary |
| Unknown | Required scene information is absent; it cannot be silently substituted with air or sky |
| White-furnace test | Controlled environment test for the declared material's energy behavior |
| First-valid latency | Time from a change to a current conservative result |
| Refined latency | Time from a change to the intended higher-quality valid result |

## 10. Source register

Every source ID in this notebook and the research additions to the master resolves below. Source URLs are portable; transient retrieval identifiers are intentionally omitted. Primary-source observations are paraphrased, and project recommendations are explicitly separated.

- [VX-01 — Starlight Technical Details][VX-01]
- [VX-02 — Starlight README][VX-02]
- [VX-03 — voxelalgorithms.cpp][VX-03]
- [VX-04 — This Week In Veloren 81][VX-04]
- [VX-05 — Performance][VX-05]
- [VX-06 — Blocky terrains][VX-06]
- [VX-07 — IBlockAccessor][VX-07]
- [VX-08 — BlockType.cs material loading][VX-08]
- [VX-09 — DhApiTerrainDataPoint][VX-09]
- [VX-10 — DhApiChunk][VX-10]
- [VX-11 — IDhApiBlockStateWrapper][VX-11]
- [VX-12 — Official release notes][VX-12]
- [LX-01 — LICENSE][LX-01]
- [LX-02 — Source SPDX header][LX-02]
- [LX-03 — Cargo.toml license declaration][LX-03]
- [LX-04 — LICENSE.md][LX-04]
- [LX-05 — license.txt][LX-05]
- [LX-06 — license_header.txt][LX-06]
- [LX-07 — LICENSE.txt][LX-07]
- [PROD-01 — Lumen Technical Details][PROD-01]
- [PROD-02 — Virtual Shadow Maps][PROD-02]
- [PROD-03 — Understanding Adaptive Probe Volumes][PROD-03]
- [PROD-04 — Use Screen Space Global Illumination][PROD-04]
- [PROD-05 — Screen Space Global Illumination reference][PROD-05]
- [PROD-06 — Signed distance field global illumination (SDFGI)][PROD-06]
- [PROD-07 — Using Voxel global illumination][PROD-07]
- [PROD-08 — LightmapGI][PROD-08]
- [PROD-09 — Physically Based Rendering in Filament][PROD-09]
- [PROD-10 — Diffuse Probe Grid Component][PROD-10]
- [PROD-11 — Open 3D Engine 22.05.0 release notes — DiffuseProbeGrid changes][PROD-11]
- [PROD-12 — Voxel-Based Global Illumination (SVOGI)][PROD-12]
- [PROD-13 — Precomputed Global Illumination in Frostbite][PROD-13]
- [PROD-14 — Moving Frostbite to Physically Based Rendering V3 — author publication page][PROD-14]
- [G01 — Dynamic Diffuse Global Illumination with Ray-Traced Irradiance Fields][G01]
- [G02 — RTXGI-DDGI official SDK][G02]
- [G03 — Spatiotemporal reservoir resampling for real-time ray tracing with dynamic direct lighting][G03]
- [G04 — ReSTIR GI: Path Resampling for Real-Time Path Tracing][G04]
- [G05 — RTXDI official SDK and samples][G05]
- [G06 — FidelityFX Brixelizer GI 1.0.1][G06]
- [G07 — Radiance Cascades original manuscript source][G07]
- [G08 — Interactive Indirect Illumination Using Voxel Cone Tracing][G08]
- [G09 — Spatiotemporal Variance-Guided Filtering][G09]
- [G10 — NVIDIA Real-Time Denoisers integration README][G10]
- [G11 — Real-Time Neural Radiance Caching for Path Tracing][G11]
- [G12 — RTXGI Neural Radiance Cache integration guide][G12]
- [G13 — pbrt-v4 reference implementation][G13]
- [G14 — Physically Based Rendering, 4th edition — Monte Carlo Basics][G14]
- [S01 — AMD FidelityFX Brixelizer/GI overview][S01]
- [S02 — GDC 2024 GI with AMD FidelityFX Brixelizer][S02]
- [S03 — Radiance Cascades manuscript repository][S03]
- [S04 — Interactive Indirect Illumination Using Voxel Cone Tracing — authors PDF][S04]
- [S05 — Current RTXGI NRC/SHaRC repository][S05]
- [S06 — RTXGI-DDGI changelog][S06]
- [S07 — RTXGI license file][S07]
- [S08 — RTXDI license file][S08]
- [S09 — NRD license file][S09]
- [S10 — pbrt source SPDX identifier][S10]
- [S11 — Falcor research framework][S11]
- [S12 — Hart — Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces][S12]
- [RND-01 — Clustered Deferred and Forward Shading][RND-01]
- [RND-02 — MegaLights in Unreal Engine][RND-02]
- [RND-03 — XeGTAO README][RND-03]
- [RND-04 — FidelityFX Stochastic Screen-Space Reflections 1.5][RND-04]
- [RND-05 — Real-Time Polygonal-Light Shading with Linearly Transformed Cosines][RND-05]
- [RND-06 — Unreal Engine Sky Atmosphere Rendering Technique — reference implementation][RND-06]
- [RND-07 — Physically Based Rendering, Fourth Edition — Radiometry][RND-07]
- [FND-01 — Khronos Vulkan Guide — Synchronization Examples][FND-01]
- [FND-02 — Khronos Vulkan Specification — Images][FND-02]
- [FND-03 — Khronos Vulkan Guide — Depth][FND-03]
- [FND-04 — Khronos glTF 2.0 material specification][FND-04]
- [FND-05 — Microsoft — Cascaded Shadow Maps][FND-05]
- [FND-06 — AMD GPUOpen — FSR Super Resolution integration][FND-06]

[VX-01]: https://github.com/PaperMC/Starlight/blob/fabric/TECHNICAL_DETAILS.md
[VX-02]: https://github.com/PaperMC/Starlight
[VX-03]: https://github.com/luanti-org/luanti/blob/master/src/voxelalgorithms.cpp
[VX-04]: https://veloren.net/blog/devblog-81/
[VX-05]: https://voxel-tools.readthedocs.io/en/latest/performance/
[VX-06]: https://voxel-tools.readthedocs.io/en/latest/blocky_terrain/
[VX-07]: https://apidocs.vintagestory.at/api/Vintagestory.API.Common.IBlockAccessor.html
[VX-08]: https://github.com/anegostudios/vsessentialsmod/blob/master/Loading/BlockType.cs
[VX-09]: https://distant-horizons-team.gitlab.io/distant-horizons/com/seibel/distanthorizons/api/objects/data/DhApiTerrainDataPoint.html
[VX-10]: https://distant-horizons-team.gitlab.io/distant-horizons/com/seibel/distanthorizons/api/objects/data/DhApiChunk.html
[VX-11]: https://distant-horizons-team.gitlab.io/distant-horizons/com/seibel/distanthorizons/api/interfaces/block/IDhApiBlockStateWrapper.html
[VX-12]: https://github.com/CaffeineMC/phosphor-fabric/releases
[LX-01]: https://github.com/PaperMC/Starlight/blob/fabric/LICENSE
[LX-02]: https://github.com/luanti-org/luanti/blob/master/src/voxelalgorithms.cpp
[LX-03]: https://github.com/veloren/veloren/blob/master/Cargo.toml
[LX-04]: https://github.com/Zylann/godot_voxel/blob/master/LICENSE.md
[LX-05]: https://github.com/anegostudios/vsapi/blob/master/license.txt
[LX-06]: https://gitlab.com/distant-horizons-team/distant-horizons/-/blob/main/license_header.txt?ref_type=heads
[LX-07]: https://github.com/CaffeineMC/phosphor-fabric/blob/1.19.x/dev/LICENSE.txt
[PROD-01]: https://dev.epicgames.com/documentation/en-us/unreal-engine/lumen-technical-details-in-unreal-engine
[PROD-02]: https://dev.epicgames.com/documentation/en-us/unreal-engine/virtual-shadow-maps-in-unreal-engine
[PROD-03]: https://docs.unity3d.com/Packages/com.unity.render-pipelines.high-definition@17.0/manual/probevolumes-concept.html
[PROD-04]: https://docs.unity3d.com/Packages/com.unity.render-pipelines.high-definition@17.0/manual/Override-Screen-Space-GI.html
[PROD-05]: https://docs.unity3d.com/Packages/com.unity.render-pipelines.high-definition@17.0/manual/reference-screen-space-global-illumination.html
[PROD-06]: https://docs.godotengine.org/en/stable/tutorials/3d/global_illumination/using_sdfgi.html
[PROD-07]: https://docs.godotengine.org/en/stable/tutorials/3d/global_illumination/using_voxel_gi.html
[PROD-08]: https://docs.godotengine.org/en/stable/classes/class_lightmapgi.html
[PROD-09]: https://google.github.io/filament/main/filament.html
[PROD-10]: https://www.docs.o3de.org/docs/user-guide/components/reference/atom/diffuse-probe-grid/
[PROD-11]: https://www.docs.o3de.org/docs/release-notes/archive/22-05-0/
[PROD-12]: https://www.cryengine.com/docs/static/engines/cryengine-5/categories/23756816/pages/25535599
[PROD-13]: https://media.contentapi.ea.com/content/dam/eacom/frostbite/files/gdc2018-precomputedgiobalilluminationinfrostbite.pdf
[PROD-14]: https://seblagarde.wordpress.com/2015/07/14/siggraph-2014-moving-frostbite-to-physically-based-rendering/
[G01]: https://research.nvidia.com/publication/2019-05_dynamic-diffuse-global-illumination-ray-traced-irradiance-fields
[G02]: https://github.com/NVIDIAGameWorks/RTXGI-DDGI
[G03]: https://research.nvidia.com/index.php/publication/2020-07_spatiotemporal-reservoir-resampling-real-time-ray-tracing-dynamic-direct
[G04]: https://research.nvidia.com/publication/2021-06_restir-gi-path-resampling-real-time-path-tracing
[G05]: https://github.com/NVIDIA-RTX/RTXDI
[G06]: https://gpuopen.com/manuals/fidelityfx_sdk/techniques/brixelizer-gi/
[G07]: https://github.com/Raikiri/RadianceCascadesPaper/blob/main/RadianceCascades.tex
[G08]: https://research.nvidia.com/labs/rtr/publication/crassin2011givoxels/
[G09]: https://research.nvidia.com/index.php/publication/2017-07_spatiotemporal-variance-guided-filtering-real-time-reconstruction-path-traced
[G10]: https://github.com/NVIDIA-RTX/NRD/blob/master/README.md
[G11]: https://research.nvidia.com/labs/rtr/publication/muller2021nrc/
[G12]: https://github.com/NVIDIA-RTX/RTXGI/blob/main/Docs/NrcGuide.md
[G13]: https://github.com/mmp/pbrt-v4
[G14]: https://pbr-book.org/4ed/Monte_Carlo_Integration/Monte_Carlo_Basics
[S01]: https://gpuopen.com/fidelityfx-brixelizer/
[S02]: https://gpuopen.com/download/GDC2024_GI_with_AMD_FidelityFX_Brixelizer.pdf
[S03]: https://github.com/Raikiri/RadianceCascadesPaper
[S04]: https://research.nvidia.com/sites/default/files/pubs/2011-09_Interactive-Indirect-Illumination/GIVoxels-pg2011-authors.pdf
[S05]: https://github.com/NVIDIA-RTX/RTXGI
[S06]: https://github.com/NVIDIAGameWorks/RTXGI-DDGI/blob/main/ChangeLog.md
[S07]: https://github.com/NVIDIA-RTX/RTXGI/blob/main/License.md
[S08]: https://github.com/NVIDIA-RTX/RTXDI/blob/main/LICENSE.txt
[S09]: https://github.com/NVIDIA-RTX/NRD/blob/master/LICENSE.txt
[S10]: https://github.com/mmp/pbrt-v4/blob/master/src/pbrt/pbrt.cpp
[S11]: https://github.com/NVIDIAGameWorks/Falcor
[S12]: https://experts.illinois.edu/en/publications/sphere-tracing-a-geometric-method-for-the-antialiased-ray-tracing/
[RND-01]: https://research.chalmers.se/en/publication/161725
[RND-02]: https://dev.epicgames.com/documentation/unreal-engine/megalights-in-unreal-engine
[RND-03]: https://github.com/GameTechDev/XeGTAO/blob/master/README.md
[RND-04]: https://gpuopen.com/manuals/fidelityfx_sdk/techniques/stochastic-screen-space-reflections/
[RND-05]: https://eheitzresearch.wordpress.com/415-2/
[RND-06]: https://github.com/sebh/UnrealEngineSkyAtmosphere/blob/master/README.md
[RND-07]: https://pbr-book.org/4ed/Radiometry,_Spectra,_and_Color/Radiometry
[FND-01]: https://docs.vulkan.org/guide/latest/synchronization_examples.html
[FND-02]: https://docs.vulkan.org/spec/latest/chapters/images.html
[FND-03]: https://docs.vulkan.org/guide/latest/depth.html
[FND-04]: https://github.com/KhronosGroup/glTF/blob/main/specification/2.0/Specification.adoc
[FND-05]: https://learn.microsoft.com/en-us/windows/win32/dxtecharts/cascaded-shadow-maps
[FND-06]: https://gpuopen.com/manuals/fsr_sdk/techniques/super-resolution-upscaler/


<!-- END PRESERVED SOURCE -->
