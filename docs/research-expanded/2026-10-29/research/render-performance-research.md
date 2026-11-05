# Deep Primal — Rendering, LOD, Performance, and Upscaling Research

**Research date:** 2026-10-29  
**Status:** Internal research and proposed documentation additions; no renderer implementation or owner-machine benchmark was performed.  
**Scope:** Twelve focused primary sources, plus original design proposals for Deep Primal's Java/NeoForge present, independent-engine direction, AMD target, Vulkan priority, and OpenGL fallback.

This notebook is a research input. It does not change the preserved source documents, authorize dependencies, or certify advertised capabilities. References use `RP-01` through `RP-12`; the adjacent JSON file records exact URLs and web citation references. Existing lighting research remains the source for GI, PBR, exposure ownership, and reflection design. Those subjects are not surveyed again here.

## Project baseline and research boundary

The reviewed project documentation describes FSR 1 spatial reconstruction, using EASU and optional RCAS through the common OpenGL/Vulkan interface. Newer temporal and machine-learning FSR documentation below is future integration research. It does not rename the implemented FSR 1 path. The project also documents an optional direct NGX JNI Vulkan Super Resolution bridge; a native binding exists, but complete temporal renderer integration, production runtime packaging, and real RTX qualification remain open in those sources. Streamline is a comparison architecture, not the project’s chosen integration. Streamline DLSS-G and Reflex requirements do not retroactively become requirements for direct NGX Super Resolution. See the preserved [upscaling document](../replicas/git/docs/UPSCALING.md) and [NGX provider README](../replicas/git/renderer-vulkan/src/main/native/dlss/README.md) for the source-scoped implementation state.

## 1. Decisions this research should change

The strongest cross-engine lesson is to make every visual promise subordinate to a complete representation and an explicit resource budget. Far visibility, detail refinement, editable terrain, reconstruction, and presentation have different lifetimes. They need contracts that meet at a coherent frame snapshot.

Proposed additions to the expanded documents:

1. **Far visibility is its own product.** Separate authoritative world residency, collision, detailed meshes, distant proxies, lighting representations, and disk caches. A distance setting must identify which of these it controls.
2. **A coarse fallback must survive refinement.** Accept finer detail only after its data, coverage, memory, upload dependencies, and boundary handling are ready. Capacity exhaustion should reduce detail without deleting visible terrain.
3. **Cleanup belongs in the budget.** Count queued jobs, in-flight uploads, obsolete completions, retired GPU allocations, and presentation-provider inputs. A mesh that is no longer visible is not necessarily safe to free.
4. **LOD transitions are temporal events.** Selection, stitching, coverage, normals, depth, shadow casting, velocity, and reconstruction history must describe the same handoff.
5. **A selected graphics option is a request.** Persist the request independently of the currently effective backend or provider. Record why a setting is reduced or unavailable.
6. **Upscaling and frame generation are distinct integrations.** A provider name does not prove a particular effect version, hardware capability, API path, or local quality qualification.
7. **Performance evidence includes tails and convergence.** Report CPU and GPU frame-time distributions, edit-to-visible latency, queue age, cold/warm traversal, stationary streaming churn, peak residency, and presentation cadence.

These are Deep Primal proposals inferred from the sources below, not claims that another engine implements this exact combined design.

## 2. Current capability wording that needs care

The live AMD release page and the integration manual were opened directly. Search snippets still returned older versions. The source inventory records the live results, and `RP-08` contains the small version-specific evidence summary.

Use the following proposed wording in the main specifications:

> Deep Primal qualifies a specific native provider package, graphics API, adapter, driver, and runtime effect. Upscaling, frame generation, denoising, and radiance caching are independently queried capabilities. SDK packaging, shipped effect version, driver-selected implementation, and Unreal plugin version are recorded separately. A vendor announcement is not a local support result.

Do not write a permanent rule that ML upscaling is unavailable on every APU, or that every newer FSR effect has the same Vulkan support. Equally, do not turn the current source's listed family support into a claim that Deep Primal's 8060S installation is already qualified. Device identity, driver, native binding, inputs, memory cost, and visual acceptance still need local evidence. [RP-08]

For NVIDIA, the proposed UI and adapter contract should use actual per-feature queries. Installing Streamline or selecting Vulkan is not a successful DLSS qualification. The AMD acceptance route must run independently of an NVIDIA provider. [RP-10, RP-11]

## 3. Proposed visual residency architecture

### 3.1 Separate the world from its visual products

Use explicit state for these products:

| Product | Authoritative purpose | Independent limits |
|---|---|---|
| World data | Saved geography, edits, generator identity, gameplay queries | Disk, memory, I/O, generation work |
| Simulation/collision neighborhood | Interaction, entities, physics, finite water simulation | Tick work, loaded cells, collision build latency |
| Near visual meshes | Detailed terrain and edit feedback | Meshing work, upload work, vertex/index memory |
| Far visual hierarchy | Distant coverage and recognizable silhouettes | Proxy memory, candidate counts, refinement error |
| Lighting representations | Gameplay light and separately qualified visual transport | Their existing lighting budgets and revision rules |
| Temporal/presentation resources | Reconstruction and presentation of a coherent view | Histories, frames in flight, provider-retained inputs |

This separation applies even if several products share a source chunk or cache file. An identifier must describe the product and revision, not only the chunk coordinate. Keeping a mountain visible cannot implicitly keep all its blocks, entities, fluids, collisions, and high-resolution lighting simulated.

HLOD provides the useful precedent of visible proxies for unloaded cells; Nanite provides useful resident-detail and overflow lessons. Deep Primal still needs its own edit-safe hierarchy. [RP-01, RP-02]

### 3.2 Define provenance before queueing work

Proposed job identity:

```text
(worldId, worldEpoch, dimensionId,
 generatorVersion, sourceRegion, sourceRevision,
 neighborBoundaryRevisions, materialRevision,
 representationKind, lodLevel, buildFormatVersion)
```

A camera request supplies priority and desired error, not a new authoritative world identity. A user changing visual distance must not alter the generator version or saved geography.

Jobs consume immutable snapshots or documented stable read access. On completion, publication checks the full identity and necessary neighbor versions. A cancelled or stale result may be discarded or retained in a compatible cache; it must not overwrite a newer accepted result. Negative coordinates, world changes, teleportation, and unload/reload need explicit cases.

Proposed resource phases:

```text
requested → source ready → build queued → CPU ready
→ upload submitted → publishable → resident
→ retired with outstanding uses → reusable/released
```

“Publishable” requires the consumer's upload dependencies and scene-version checks to be satisfied. It does not merely mean a copy was enqueued. A frame may submit a properly synchronized wait for an upload; the implementation need not stall the CPU to publish every resource.

### 3.3 Keep a complete visible cut

For every visible region inside the requested visual domain, select one valid coverage level, or a documented complementary transition between two levels. Children replace a parent only when the required child set and seam treatment are ready. Avoid a partially missing child set that exposes the world background.

Distinguish three cases:

- **Refinement pending:** draw the current valid coarse representation.
- **Representation invalid after an edit:** use a revision-compatible conservative replacement under the existing visual-staleness rules.
- **No qualified source coverage:** expose the defined boundary treatment or loading state; do not fabricate authoritative terrain.

A fallback can preserve continuity only within its validity contract. An obsolete proxy must not re-close a newly excavated cave indefinitely or restore removed terrain during an edit transition. Coordinate its publication rule with the existing lighting and water contracts.

### 3.4 Account for the full live memory set

Budget actual allocations by ownership:

```text
peak = accepted resident products
     + CPU build/results waiting for publication
     + upload staging and pending destination allocations
     + old allocations awaiting their last consumer
     + temporal/presentation resources
     + allocator overhead and reserved safety capacity
```

Count each physical allocation once. Shared aliases require a shared owner record; separate copies require separate accounting. On a unified-memory target, “GPU bytes” must not be mistaken for an unrelated second pool of physical RAM.

Admission checks happen before expensive allocations and refinement. Track resident, pending, retired, and reserved bytes independently, with high-water marks and eviction reasons. Prefer reducing desired refinement over repeatedly admitting data that the next frame immediately evicts. A fixed camera should reach a stable working set under a declared quality/budget combination. [RP-01, RP-06]

## 4. Proposed meshing, culling, and LOD contract

### 4.1 Optimize the measured cost

A smaller triangle count is one useful measurement. Also record:

- Source bytes read and build CPU time.
- Output vertex/index bytes, vertex sharing, material runs, and draw count.
- Upload and retirement cost.
- Pixel overdraw and shadow-view work.
- Edit locality, boundary rebuild scope, and time to publish a correct replacement.
- Error in silhouette, cave openings, coastlines, normal/material discontinuities, and water boundaries.

Larger mesh regions may help submissions while delaying edits; aggressive material merging may lose important distinctions. Prototype alternative block sizes against the same traversal and edit trace before fixing one in the specification. Keep conventional indexed geometry as a qualified baseline while evaluating more specialized clustering. [RP-03, RP-05]

### 4.2 Make visibility rejection conservative

Proposed order: spatial hierarchy, conservative frustum bounds, qualified occlusion, then finer cluster or mesh rejection if that reduces measured work. Include all geometry displacement and animation in bounds. Mark opaque, alpha-cutout, transparent, and water coverage separately.

An old occlusion result cannot prove that newly exposed terrain is hidden after a camera discontinuity or excavation. Invalidate or conservatively bypass affected history until a current result is available. Missing query data should increase work instead of creating holes. Never treat a transparent water surface or sparse foliage as a solid occluder without a qualified coverage rule.

Enforce finite candidate and indirect-command capacities. Overflow needs a reproducible counter, a bounded fallback, and an acceptance scene. It must not silently drop random geometry. [RP-01, RP-05]

### 4.3 Select using projected error

For a perspective view, a useful proposed approximation is:

```text
projectedErrorPixels ≈ worldError × outputHeight
                     / (2 × conservativeViewDepth × tan(verticalFov / 2))
```

Use a conservative depth and explicit near-plane handling; this approximation is not a universal bound for intersecting or strongly extended geometry. Preserve a world-space maximum distance as a separate policy. Use stable output resolution for the target error unless a consciously chosen policy says otherwise, so automatic render-resolution changes do not repeatedly rebuild terrain.

Add separate refine/coarsen thresholds, minimum residence time where needed, and a bounded transition count. The quality governor should not oscillate between two incompatible LOD frontiers each frame. Unity supplies a clear screen-coverage selection example; the actual error metric and hysteresis are Deep Primal proposals. [RP-04]

### 4.4 Make transitions consistent across passes

Select a transition technique per representation: stitched border, geometric morph with valid correspondence, complementary coverage fade, or an immediate switch that passes visual acceptance. Do not label arbitrary remeshing as a valid morph.

Every transition declares:

| Concern | Required proposed contract |
|---|---|
| Coverage | No uncovered strip, overlapping surface fighting, or duplicate transparent contribution |
| Geometry | Shared boundary quantization and neighbor LOD policy |
| Shading | Compatible material IDs, normals, and light sampling |
| Depth and shadows | Coverage agrees with what the viewer sees |
| Motion | Valid previous position where correspondence exists; otherwise a rejection/reactivity policy |
| Lifetime | Both representations remain available until all transition consumers finish |
| Cost | Simultaneous geometry, draw, shadow, and history work is charged to the budget |

Test transitions while moving slowly across the threshold, rotating rapidly, zooming, changing quality, editing a seam, and viewing the same terrain in a reflection or shadow. [RP-01, RP-04, RP-09]

## 5. Proposed scheduling and telemetry

### 5.1 Bound every stage

Track separate ceilings for queued tasks, running worker jobs, per-frame publication work, uploaded bytes, GPU in-flight work, retirement work, candidate lists, and memory. A maximum job count alone does not bound bytes or latency.

Priority should consider visible error, near-player urgency, request age, movement prediction, and cost. Enforce aging so far or unlucky requests cannot starve forever. A cancelled job still consumes resources until it observes cancellation or completes; include that time and memory in telemetry.

Budget checks after a long indivisible API call cannot retroactively protect the frame. Measure maximum call duration and change the unit of work when necessary. Keep safety headroom for uploads and destruction, and avoid draining an entire retirement backlog in one frame. [RP-03, RP-06]

### 5.2 Use a measurement record that can be compared

Each captured run should record build/commit identity, world/generator identity, seed, edit trace, camera path, API, GPU/driver, window/output size, internal render size, effective graphics settings, provider versions, and warm/cold state.

Collect the following proposed measurements:

| Group | Measurements |
|---|---|
| CPU | Main/render thread distribution, worker utilization, queue waits, allocation/GC pauses |
| GPU | Named pass times, critical-path time, barriers/idle intervals, dispatch/draw counts |
| Streaming | Bytes read/generated/uploaded, pending and cancelled work, request age, convergence |
| Memory | CPU, device/shared allocations, peak live, retired, allocator pressure |
| Visual latency | Edit-to-first-correct-frame, near-field coverage recovery, teleport recovery |
| Presentation | Real rendered frames, generated frames, actually presented frames, pacing and measured latency |

Report distributions and bad intervals, not only a mean. Define sampling windows and percentile calculation. Report overlapping GPU queues without naively adding their durations as though they execute serially. Clock rates, power behavior, and background activity belong in the run context when they affect comparisons.

RGP is a useful Vulkan diagnostic on a supported AMD configuration; it cannot stand in for OpenGL telemetry or full application profiling. [RP-07, RP-11]

### 5.3 Keep automatic quality control stable

Proposed policy: a governor observes sustained measurements, changes one budget dimension deliberately, and records its decision. Apply hysteresis, bounded rate of change, and a recovery period. It can change visual refinement, shadow/probe work, or internal render resolution only within user-approved limits.

It must not change simulation tick semantics, world generation, persisted edits, finite-water quantities, or authoritative gameplay light. A user-requested setting remains stored while the active quality controller applies a temporary effective value. Diagnose CPU-bound and GPU-bound cases separately; reducing pixel work is not a general fix for terrain meshing stalls.

## 6. Proposed provider and settings contracts

### 6.1 Capabilities are feature-specific

Proposed provider status:

```text
requested provider/mode
available package and native-binding version
adapter/API/driver capability result
qualified effect and active runtime version
effective provider/mode/render size
fallback reason and recovery policy
```

Keep `Upscaler`, `FrameGenerator`, and optional denoising/transport adapters separate. Each owns a declared part of the frame graph and a declared set of input/output lifetimes. The base renderer and native image path must remain operable if any optional provider fails.

Temporal-input metadata should include current/previous camera transforms, jitter conventions, depth range/direction, render and output extents, exposure convention, motion convention, frame and scene epochs, and reset reasons. Provider adapters convert to exact SDK expectations rather than pretending different vendors use identical conventions. [RP-08 through RP-11]

### 6.2 Persist intention and expose effective behavior

Proposed configuration requirements:

- Version the settings schema; validate ranges and enum values during load.
- Preserve requested backend/provider/quality choices separately from detected effective values.
- Persist successfully applied user changes reliably and test restart behavior.
- Restore a safe effective profile after a failed backend/provider initialization without erasing the user's request.
- Record migration decisions and make reset behavior explicit.
- Distinguish local display preferences from world-authoritative settings.
- Avoid writing an automatic quality adjustment to disk as though the user had chosen it.

A missing effect, unsupported driver, or failed initialization should have a visible reason. It should not silently turn a saved request into a different permanent preference.

### 6.3 Frame generation belongs to presentation

Define real rendered frames, generated presentation frames, and simulation ticks as separate counters and clocks. Generated images must not advance gameplay, fluid simulation, terrain revision, lighting propagation, save timing, or network authority.

Charge provider-retained depth/motion/color inputs to the lifetime tracker. Resizing, changing displays, switching providers, pausing, loading, and recreating the device must follow the pinned provider's lifecycle. Verify UI, cursor, HUD, and water artifacts as well as scene motion. Accept presentation-rate improvements only with pacing and latency evidence; do not use generated FPS as proof of a faster terrain engine. [RP-11]

### 6.4 Qualify OpenGL as a complete profile

Keep renderer choice, graphics API, and features separate in the documentation. The proposed OpenGL profile needs its own bounds on geometry, lighting, water, transparency, and temporal processing. Features can differ while the world, gameplay, saved preferences, and defined visual intent remain coherent.

The compatibility profile must pass coverage and edit correctness before optional parity work. Where temporal reconstruction is unavailable, qualify a native or spatial path explicitly. Do not claim that a modern vendor SDK becomes an OpenGL integration because its effects are conceptually similar to shaders already used by the game. [RP-12]

## 7. Proposed acceptance matrix

These are test requirements to add, not passed results or invented numeric targets. Root should align numeric gates with the project's existing measured owner-machine targets.

| Scenario | Evidence required |
|---|---|
| Fixed camera at maximum requested distance | Stable working set, no endless reload/eviction cycle, bounded candidates |
| Long traversal and rapid return | Bounded queues and retirement, coverage continuity, warm-cache behavior |
| Teleport and world switch | Old world results rejected, histories reset appropriately, recovery measured |
| Dig/build at LOD and chunk boundaries | Correct near geometry, valid far replacement, no persistent stale occluder |
| Cave mouth and thin silhouette | Simplification preserves accepted openings and shape |
| Coastline and finite water | Stable geometry and motion inputs; simulation invariant under quality changes |
| Dense foliage and transparency | Coverage, overdraw, and reconstruction artifacts measured |
| Tiny and deliberately constrained memory budgets | Predictable coarse fallback, no random missing geometry |
| Native, upscaled, and generated presentation | Separate rendered/presented measurements, image quality and latency |
| Vulkan/OpenGL switch and restart | Saved request preserved, effective fallback explained, world unchanged |
| Resize, minimize, full screen, provider switch | Resource lifetime validation, no stale inputs or unbounded accumulation |
| Owner AMD machine | Actual adapter/driver/API/provider qualification recorded with captures |

## 8. Source evidence and individual design inferences

Evidence paragraphs below are deliberately short. Proposals are original Deep Primal design inferences. Exact machine-readable fields and web references are in `render-performance-sources.json`.

### RP-01 — Nanite Technical Details

**Source:** [Nanite Technical Details](https://dev.epicgames.com/documentation/unreal-engine/nanite-technical-details?lang=en-US) — Official Epic Games documentation. Inspected 2026-10-29.

**Evidence:** Nanite exposes a minimum resident geometry allocation and streams additional detail. Its documentation explains that an undersized streaming pool can repeatedly reload data even when the camera stops moving. Candidate and visible cluster buffers have finite capacities; documented overflow can cause missing or blinking geometry. Vertex quantization must align shared boundaries, and diagnostic views expose culling and overdraw. These are concrete examples of memory, geometry precision, and visibility constraints inside a production virtualized geometry system.

**Design inference:** Adopt a bounded visual hierarchy with a resident coarse ancestor for every accepted refinement. Reserve capacity before refinement; fall back to a complete coarser cut before visibility lists overflow. Measure a stationary-view convergence condition, streaming churn, candidate high-water marks, and peak live-plus-retiring bytes. Quantize neighboring terrain vertices from one shared grid definition.

**Limits:** Unreal-specific mechanisms and constants are examples; they do not establish Deep Primal support or performance.

### RP-02 — World Partition — Hierarchical Level of Detail

**Source:** [World Partition — Hierarchical Level of Detail](https://dev.epicgames.com/documentation/en-us/unreal-engine/world-partition---hierarchical-level-of-detail-in-unreal-engine) — Official Epic Games documentation. Inspected 2026-10-29.

**Evidence:** World Partition HLOD represents distant, noninteractive content with proxy geometry while the source cells are unloaded. HLOD layers can use instancing, merged meshes, or simplified meshes, with their own cell sizes and loading ranges. The documented proxy pipeline also merges materials. Its settings demonstrate that aggressive merging can change appearance and geometry, including closing small openings; source representation and distant visual representation therefore have different purposes.

**Design inference:** Give simulation, collision, detailed rendering, and far visualization independent residency policies. A far proxy must carry world and source revisions, a coverage definition, and a replacement policy after edits. Require cave-mouth, silhouette, coast, and water-level preservation tests. A loaded visual proxy must never be treated as proof that authoritative terrain or collision data is available.

**Limits:** The documented workflow primarily targets authored static actors; editable voxel invalidation is our proposed extension.

### RP-03 — Voxel Tools — Performance

**Source:** [Voxel Tools — Performance](https://voxel-tools.readthedocs.io/en/latest/performance/) — Original project documentation. Inspected 2026-10-29.

**Evidence:** Voxel Tools uses background threads for heavy work and exposes a main-thread time budget so remaining work can continue in later frames. Its terrain discussion identifies a tradeoff: larger mesh blocks reduce draw calls but can increase the cost of voxel modifications. The page also records profiling cases where upload behavior or destruction of many mesh buffers caused slow updates and movement-related spikes, showing why build throughput alone is an incomplete terrain performance metric.

**Design inference:** Budget generation, meshing, uploads, collider publication, and resource retirement separately. Track queue age and oldest unfinished near-field request as well as throughput. Keep worker capacity below a measured CPU saturation point. Choose mesh granularity using edit and traversal traces, and split or defer expensive publication work before a frame deadline is exhausted.

**Limits:** Its OpenGL issue is explicitly historical Godot 3.x behavior, not proof of a current universal driver defect.

### RP-04 — Unity 6.6 — LOD Group component reference

**Source:** [Unity 6.6 — LOD Group component reference](https://docs.unity.com/en-us/engine/6000.6/manual/analysis/graphics-performance-profiling/lod/lod-group/class-lodgroup) — Official Unity documentation. Inspected 2026-10-29.

**Evidence:** Unity's LOD Group selects levels using an object's screen-height percentage rather than one universal world-space distance. It offers an immediate switch, cross-fading, and a separate SpeedTree-specific vertex interpolation mode. Cross-fade transitions can use a shared transition duration. The component also exposes bounds recalculation after geometry changes. These controls distinguish selection criteria, transition appearance, and the bounding volume needed to classify the geometry.

**Design inference:** Use projected geometric error plus hysteresis for terrain selection and a separately budgeted transition policy. Preserve both representations until the handoff is valid, and include both in memory and draw estimates. Transition depth, shadows, normals, and motion history must agree with visible coverage. Do not assume arbitrary voxel meshes can morph using a tree-specific technique.

**Limits:** This is an engine component example, not a ready-made terrain stitching algorithm.

### RP-05 — meshoptimizer — official project README

**Source:** [meshoptimizer — official project README](https://github.com/zeux/meshoptimizer) — Original implementation documentation. Inspected 2026-10-29.

**Evidence:** meshoptimizer separates mesh simplification, vertex-cache and vertex-fetch optimization, and meshlet construction. Its meshlet builder balances vertex reuse with spatial and directional bounds for culling; bounds described for static geometry support sphere and cone rejection. Simplification can preserve attribute discontinuities or incorporate weighted attribute error, and strong seams can restrict achievable reduction. Its documentation also distinguishes raster-friendly clustering from clustering optimized for ray-tracing intersection work.

**Design inference:** Evaluate terrain mesh quality across bytes, vertex reuse, overdraw, seam preservation, and build latency. Treat material boundaries and voxel corner-light discontinuities as explicit attributes. Keep a conventional indexed-mesh path; prototype meshlets only after the CPU or GPU bottleneck is measured. Rebuild or conservatively expand bounds for deformed geometry.

**Limits:** No library adoption is proposed without a pinned version, integration review, and project-specific measurements.

### RP-06 — Vulkan Guide — Synchronization Examples

**Source:** [Vulkan Guide — Synchronization Examples](https://docs.vulkan.org/guide/latest/synchronization_examples.html) — Official Khronos documentation. Inspected 2026-10-29.

**Evidence:** Khronos provides distinct synchronization examples for transfer uploads, shader writes, attachment writes, and host readback. The upload examples distinguish separate host/device memory from unified memory and show noncoherent host-memory flushing where required. Separate queue families require the appropriate ownership and dependency treatment; same-queue execution has its own valid path. The readback example waits for GPU completion before host access and invalidates noncoherent memory when necessary.

**Design inference:** Give each allocation a last-use completion record covering every consumer, including presentation providers. Reuse or retire it only after those uses complete. Describe pass stage/access/layout requirements explicitly and validate the actual queue configuration. Benchmark direct mapped uploads versus staging on the owner APU without assuming either is universally faster.

**Limits:** Examples require adaptation to actual access patterns, image layouts, memory types, queue families, and enabled extensions.

### RP-07 — The AMD Radeon GPU Profiler — manual

**Source:** [The AMD Radeon GPU Profiler — manual](https://gpuopen.com/manuals/rgp_manual/) — Official AMD GPUOpen documentation. Inspected 2026-10-29.

**Evidence:** Radeon GPU Profiler exposes GPU event timing, wavefront occupancy, barriers, queue synchronization, and asynchronous work for supported explicit graphics APIs. The manual lists Vulkan and DirectX 12 graphics support and explicitly excludes OpenGL. It documents user markers and views for expensive events, targets, and pipelines. Its hardware and operating-system support lists are separate from the features of an application, so capture availability must be checked on the actual test machine.

**Design inference:** Use stable pass markers and reproducible camera/edit traces for the AMD acceptance machine. Pair GPU captures with engine CPU, worker, allocation, garbage-collection, I/O, and queue-age telemetry. Report native render times separately from presentation rate. Give OpenGL its own valid timing and capture route instead of requiring a tool that does not capture it.

**Limits:** No GPU capture, owner-machine compatibility test, or benchmark was performed in this research task.

### RP-08 — AMD FSR SDK — live release and requirements page

**Source:** [AMD FSR SDK — live release and requirements page](https://gpuopen.com/amd-fsr-sdk/) — Official AMD GPUOpen documentation. Inspected 2026-10-29.

**Evidence:** The live page documents SDK 2.4, upscaling 4.1.2, and Vulkan 1.2 support. Its supported upscaling hardware includes selected Ryzen AI Max/APU families; the new APU and Vulkan paths require driver 26.10.1 or later. ML frame generation has a narrower hardware requirement and an analytical fallback. The SDK requires signed loader/effect DLLs. Ray regeneration and radiance caching remain DirectX 12-only. Driver-selected effects and shipped effect versions can differ.

**Design inference:** Represent SDK version, shipped effect version, active runtime version, adapter identity, driver, API, and queried capabilities independently. Make AMD qualification an explicit local milestone. Keep upscaling, interpolation, denoising, and radiance caching as independent adapters and settings; selecting one must not imply the others. Pin active effects for reproducible comparison runs.

**Limits:** Checked 2026-10-29; SDK, effect, plugin, driver, and actual integration qualification are distinct.

### RP-09 — AMD FidelityFX Super Resolution 3.1.6 — integration manual

**Source:** [AMD FidelityFX Super Resolution 3.1.6 — integration manual](https://gpuopen.com/manuals/fsr_sdk/techniques/super-resolution-upscaler/) — Official AMD GPUOpen documentation. Inspected 2026-10-29.

**Evidence:** The FSR upscaler manual specifies input color, depth, motion, exposure, and optional reactive and transparency/composition masks, with declared resource resolutions and jitter handling. Motion describes current pixels relative to previous-frame positions. The algorithm uses temporal information and replaces a separate ordinary TAA pass in the documented integration. The manual also defines camera-cut reset handling and placement in the frame, so a quality-mode selector alone is not a complete integration.

**Design inference:** Create a provider-neutral temporal-input contract and provider-specific conversion adapters. Include camera motion, entities, deformation, water, particles, LOD changes, rebases, and dynamic resolution in validation. Reset only the histories invalidated by a discontinuity; retain a native non-provider route for comparison and failure recovery.

**Limits:** The inspected page is version 3.1.6; its API contract must not be generalized to every FSR effect.

### RP-10 — NVIDIA Streamline — DLSS Programming Guide

**Source:** [NVIDIA Streamline — DLSS Programming Guide](https://github.com/NVIDIA-RTX/Streamline/blob/main/docs/ProgrammingGuideDLSS.md) — Official NVIDIA source documentation. Inspected 2026-10-29.

**Evidence:** The DLSS guide requires a feature-support query for the selected adapter and shows explicit failure paths for unsupported hardware, operating systems, and drivers. Integration supplies depth, motion, input color, output color, camera constants, and resource lifetimes; a Vulkan device setup entry point is documented. The guide queries valid render sizes and directs the host to restore command-buffer state after evaluation. An unsupported adapter follows an alternative reconstruction path.

**Design inference:** Keep DLSS optional behind a native adapter boundary and a separate qualification suite. Preserve a user's requested mode while showing an effective supported mode and a clear reason when unavailable. AMD target acceptance must not depend on DLSS. Never infer feature support from the presence of a DLL, a Vulkan device, or a marketing family label.

**Limits:** Vendor documentation does not establish active game integration. Deep Primal already documents an optional direct NGX JNI bridge, with runtime packaging, complete temporal renderer inputs, and RTX qualification still required. Streamline is comparison research, not the current provider.

### RP-11 — NVIDIA Streamline — DLSS-G Programming Guide

**Source:** [NVIDIA Streamline — DLSS-G Programming Guide](https://github.com/NVIDIA-RTX/Streamline/blob/main/docs/ProgrammingGuideDLSS_G.md) — Official NVIDIA source documentation. Inspected 2026-10-29.

**Evidence:** DLSS-G has its own capability checks, UI-resource handling, Streamline Reflex requirement, and presentation integration. Its Vulkan instructions specify acquire/present synchronization and additional input-completion handling for relevant queue modes. The guide counts actually presented frames, which can include interpolated frames; interpolated frames can also be dropped. It describes resource lifetimes extending to presentation, rather than assuming that ordinary rendering completion makes all frame-generation inputs safe to replace.

**Design inference:** Treat frame generation as a presentation provider, separate from upscaling and simulation. Track rendered, generated, and presented frames independently, including pacing and latency. Validate resize, pause, menus, loading, device recreation, and provider switching. Require provider completion before input reuse, and keep generated frames out of terrain, physics, lighting, and persistence clocks.

**Limits:** Specific requirements belong to the pinned integration; Deep Primal's AMD path must remain fully functional independently.

### RP-12 — Godot — Overview of renderers

**Source:** [Godot — Overview of renderers](https://docs.godotengine.org/en/stable/tutorials/rendering/renderers.html) — Official Godot documentation. Inspected 2026-10-29.

**Evidence:** Godot separates rendering methods from graphics drivers. Its Forward+ and Mobile renderers use the RenderingDevice abstraction over modern APIs, while Compatibility uses OpenGL. The documentation warns that changing renderer can require lighting and environment adjustments because feature sets differ. Its fallback sequence may allow a project to run on unsupported hardware while producing a different appearance. A shared application therefore does not imply identical capabilities across rendering paths.

**Design inference:** Define a shared scene and material contract, with an explicit capability table and separately validated backend profiles. Make the OpenGL profile complete for gameplay, terrain coverage, water readability, and saved settings. Show effective feature reductions without silently rewriting user preferences. Build comparisons around correctness and visual intent, not bitwise image equality.

**Limits:** Godot's backend split is a design example; it does not constrain which OpenGL features Deep Primal can implement.

## 9. Handoff boundaries

This notebook proposes additions for the rendering, LOD, performance, settings, upscaling, architecture, and acceptance documents. It does not modify those documents or import third-party code. Preserve historical audits as evidence, with new analysis clearly dated instead of rewriting their historical conclusions.

The current lighting specification already defines contribution ownership, optional GI, visual/gameplay separation, Vulkan/OpenGL profiles, water history, and edit invalidation. Expanded rendering documents should reference those contracts and avoid introducing a second incompatible lighting or temporal pipeline. Where a source describes a static authored workflow, its editable-world equivalent remains our design work.


