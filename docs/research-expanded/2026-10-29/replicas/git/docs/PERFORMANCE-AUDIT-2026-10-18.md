# Expanded replica — docs/PERFORMANCE-AUDIT-2026-10-18.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/PERFORMANCE-AUDIT-2026-10-18.md) |
| Source identity | git: docs/PERFORMANCE-AUDIT-2026-10-18.md |
| Preserved source SHA-256 | `893929158baf52039499665f5d6494b89e419673fba710aa5cdf5a9f04638e6a` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

A dated alpha-8 performance audit compares project work with Mojang, CurseForge-associated renderer examples, and Hytale references. It separates packed vertex/index payload reductions from FPS, identifies ordinary indexed batching rather than hardware indirect draws, and records the Vulkan boundary that later source work addresses.

## Research expansion — gaps and details to deepen

- The audit is useful historical evidence but can be mistaken for the current capability inventory. Its alpha-8 Vulkan absence and incomplete runtime validation need explicit links to later adapter, FSR 1, and renderer-jam records without rewriting what was actually true at the audit pin.
- The numerical payload reduction describes one representation, not complete device residency. The expanded comparison should identify excluded staging, descriptors, command resources, duplicate replacement pages, and retired allocations before readers use that percentage in a memory or performance forecast.
- The external-engine comparisons should become mechanism-to-experiment records. A named engine or high-level feature description does not establish a compatible terrain representation, equivalent visual workload, target-device result, or dependency suitable for this Java/NeoForge project.

## Research expansion — proposed contracts

- Add a historical-status box with reviewed commit/date, implementation and artifact scope, known superseding documents, and an explicit unresolved-gate list. Preserve original measurements and distinguish their units, source method, and tested pipeline from later proposals.
- Introduce a fair comparison manifest covering source/artifact identity, camera path, terrain delivery state, material and water coverage, render/output resolution, backend, driver, warm/cold state, and enabled reconstruction. Compare only workloads that draw equivalent valid content.
- Map each borrowed idea to a bounded Deep Primal experiment: compact vertex layout to bandwidth and decode cost; shared indexed setup to CPU/API overhead; coarse representation to coverage age; resource reuse to allocation and retirement peaks. State a stop condition before implementation.

## Research expansion — acceptance checks

- Recalculate geometry-payload arithmetic directly from vertex and index formats and label excluded allocations. Check that every historical absence remains dated and that links to later Vulkan and upscaling documents do not imply retrospective validation of the old artifact.
- For a future performance update, collect repeated native Vulkan and OpenGL captures with identical scene manifests, correctness images, complete-frame distributions, and memory ownership totals. Report component CPU measurements separately and preserve slow or failed trials rather than promoting a headline percentage.

## Research expansion — source anchors

- Evidence and applicability
- Full Vulkan boundary
- Still required

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Performance audit — Mojang, CurseForge, Hytale

This records the alpha 8 audit and its implementation status at that time. Alpha 9 subsequently adds the optional LGPL VulkanMod-derived backend, native rebuild pacing and memory/save admission; see [alpha 9 validation](VALIDATION-0.0.1-alpha.9.md) for current scope and limitations.

Deep Primal 26.1.2, alpha 8: author-written implementation; no third-party renderer, mod source or native library was bundled in that pass. A feature earns its footprint through useful behavior, bounded resources and tests. A menu containing every imaginable toggle is not a performance architecture.

## Evidence and applicability

| Primary source | Verified technique / constraint | Deep Primal treatment |
| --- | --- | --- |
| [Mojang 26.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2), Vulkan section | Experimental Vulkan backend with capability requirements, native presentation/shader resources, OpenGL fallback and startup recovery | Full Vulkan remains unimplemented. Preserve a working OpenGL backend and do not expose an empty backend selector. The API boundary differs from 26.1.2. |
| [Mojang 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3), technical changes | GPU MultiDrawIndirect on supported devices and a different window/input backend | Alpha 8 uses indexed geometry and shared setup via 26.1.2's drawMultipleIndexed API. Its OpenGL backend still issues individual GPU draws. This is not a claim that native MDI was backported. |
| [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) | Renderer-oriented optimization | Use compact meshes and reused frame data. Retain tested automatic renderer handoff; do not bundle a second renderer mod. |
| [ImmediatelyFast](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast) | Optimize immediate rendering work | Audit repeated submission setup and allocations; introduce a fallback-capable shared indexed submission path for our horizon only. This does not replace its GUI/entity pipeline. |
| [Entity Culling](https://www.curseforge.com/minecraft/mc-mods/entityculling) | Avoid rendering hidden entities | Expose conservative visual range/admission limits as an additional quality tradeoff. Existing frustum/face culling remains. Do not call point raycasts a proven conservative occlusion system. |
| [FerriteCore](https://www.curseforge.com/minecraft/mc-mods/ferritecore) | Reduce memory overhead | Remove repeated vertices and retain byte budgets. No wholesale Minecraft state-table rewrite without heap profiles. |
| [Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) | Improve simulation internals while preserving behavior | Keep simulation mechanics intact during this video/render pass. No untested hopper, collision, AI or tick-skipping patch collection. |
| [Dynamic FPS](https://www.curseforge.com/minecraft/mc-mods/dynamic-fps) | Reduce unnecessary client work outside active play | Add lower unfocused/minimized rendering ceilings and pause optional horizon requests/uploads. The FPS hook yields to Dynamic FPS if installed. Server simulation is independent. |
| [C2ME](https://www.curseforge.com/minecraft/mc-mods/c2me) | Parallel chunk generation, I/O and loading | Preserve bounded worker ownership and nearby-first requests. A second chunk scheduler/native-generation thread-safety rewrite needs travel, cancellation and multicore profiling; it is not installed as a video toggle. |
| [ModernFix](https://www.curseforge.com/minecraft/mc-mods/modernfix) | Startup, memory and bug-fix optimizations | Keep this pass focused on measured horizon payload and allocation costs. Startup/resource/model caches require their own loading profiles; no blanket patch collection is merged. |
| [MoreCulling](https://www.curseforge.com/minecraft/mc-mods/moreculling) | Additional client visibility culling | Keep conservative bounds, existing frustum/vertical/back-face tests and explicit visual limits. Model-specific occlusion, translucency and modded-renderer cases remain separate validation work. No project source is merged. |
| [Krypton](https://github.com/astei/krypton) | Networking stack and entity-tracker optimization | Keep horizon traffic bounded/coalesced and retain the optional native channel. Full packet-codec/compression/tracker replacement requires separate multiplayer throughput and compatibility measurements. |
| [Hytale Update 6, August 27](https://hytale.com/news/2026/8/update-6-patch-notes), performance | Nearby-first loading, reduced unnecessary chunk churn, generation worker/headroom tuning and frame pacing | Sort our requests by actual player position, reuse cached grids, bound upload work and preserve simulation headroom. Do not infer a safe universal worker count. |
| [Hytale Chapter I, October 12](https://hytale.com/news/2026/10/2026-10-12-chapter-i-out-of-the-breach/), performance | Less duplicate collision lookup work, fewer synchronous chunk-load stalls, memory-conscious saves and distance/vertical simulation throttling | Apply duplicate-work elimination to our sample grids. Native collision/save rewrites and vertical simulation pausing remain separate gameplay-sensitive work, not silently enabled video options. |
| [Hytale worldgen performance guide](https://docs.hytale.com/creating-content/world-generation/performance-optimization/) | Limit expensive field evaluation by region; reuse samples and contexts; measure varied terrain with sufficient samples | Keep the existing 2D height cache/direct column solver, derive lower-resolution snapshots from finer cached grids and avoid changing saved terrain during this pass. Larger worldgen benchmarks remain required. |
| [Hytale's 2024 ECS article](https://hytale.com/news/2024/6/summer-2024-technical-explainer-hytale-s-entity-component-system-opwpcamdi) | Explicitly marked outdated by its publisher | Do not present its abandoned C++/Flecs architecture as the current game's implementation. Data-local processing is a general technique, not evidence that Deep Primal should acquire an ECS dependency. |

## Implementation scope

Alpha 8 adds indexed roof/wall geometry, scalar view-policy caching, reusable transform records, optional shared indexed submission, staged per-frame upload ceilings, exact finer-to-coarser cache reuse and four pages of staged video controls. Defaults preserve native entity/block-entity/particle admission. Explicit scene caps and the Speed profile reduce visual range/detail; they do not stop entity AI, machines, crops or world simulation. Players, glowing/leashed entities and global block renderers have exceptions.

The memory reduction is exact payload arithmetic, not an FPS claim. Fine roof/wall tile including all three cached detail levels falls from 172,320 bytes to 59,412 bytes (65.52%). The GPU API uses unsigned-short indices; closed geometry, triangle order and colors are differentially checked against the alpha 7 reference. Pending CPU snapshots, one staged GPU tile and the near closure remain separately bounded; the configured retained-mesh ceiling is not total VRAM.

## Full Vulkan boundary

A full backend must implement 26.1.2's device, command encoding, buffers, texture/view/sampler ownership, pipeline/shader compilation, render passes, depth/transparency, window presentation, resize/fullscreen/vsync, resource reloads and synchronization. Startup fallback must work before loading a world, and an explicit OpenGL preference must avoid Vulkan initialization. Native bindings/compiler/MoltenVK packaging are necessary dependencies if that backend is shipped; they should not be loaded by dedicated servers or OpenGL-only launches.

The nearer official 26.2 API separates surface presentation and changes formats/query/device information. Copying its backend classes directly into 26.1.2 is not binary-compatible. The inspected VulkanMod default branch targets Fabric 1.21.11 and imports Fabric renderer/lifecycle APIs plus several native binding modules; it is not a ready-made NeoForge 26.1.2 adapter. No such code is copied here.

`python tools/vulkan_probe.py` only checks instance availability; it does not render. In this environment it returns `VK_ERROR_INCOMPATIBLE_DRIVER` (-9) requesting Vulkan 1.2. The retained result is in the alpha 8 validation directory. This blocks local Vulkan rendering validation, and the full game backend has not been authored. Neither this probe nor an OpenGL wrapper is presented as full Vulkan support.

## Still required

- Full Vulkan backend and startup recovery, representative driver/platform tests.
- Complete near-block mesher/submission replacement, GPU-driven occlusion/indirect draws where justified.
- Correct saved/edited-world LODs, structures/trees/material seams and update invalidation.
- Real multicore/multiplayer travel, resource reload, world/connection transitions and long-session allocation profiles.
- CPU/GPU/frame/tick/memory/terrain-arrival comparison on AMD/Intel/NVIDIA and integrated graphics. Capped software-renderer correctness runs do not establish a best-performing product.

No universal "best" claim or stable release gate is passed by this alpha. Keep raw measurements and promote changes based on reproducible results, not the length of the feature list.


<!-- END PRESERVED SOURCE -->
