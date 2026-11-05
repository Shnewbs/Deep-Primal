# Deep Primal rendering, lighting and reconstruction

**Edition:** October 29, 2026. **Source inventory:** `8f28f833caa259a0e59adac494a3aee8d750bc63`. **Purpose:** canonical working synthesis of the renderer plans, implementation receipts, lighting specification and research. This is a documentation audit and merge; no fresh build, GPU run, image acceptance or engine grade was produced.

Use this page for current scope, shared contracts and next gates. Original implementation reports remain evidence for their exact source/artifact/platform. The frozen research expansion remains intact; its Dev77 baseline does not describe every successor on current main.

## Direction and precedence

Vulkan is the owner's primary sample/reference API and validation target. OpenGL remains a supported modular fallback and saved preference. Nearby block meshing still belongs to Minecraft; the optional adapter does not import VulkanMod's entire terrain renderer. The lean default package omits the Vulkan module, while explicit platform/universal profiles include it. Verify both package contents and the actual selected device before describing a test as Vulkan. [Adapter](../../renderer-vulkan/README.md), [Vulkan-primary receipt](../validation/engine-continuation/vulkan-primary-2026-10-22/README.md).

The owner requests selectable FSR2, FSR3 and FSR4 in addition to existing choices. Keep that product requirement while implementing each real provider and its capability gate. A stored or selectable preference is not an active rendering algorithm. NVIDIA DLSS Super Resolution and frame generation have separate readiness and platform requirements.

The owner reported over 300 FPS while flying in creative with Vulkan and a 125-chunk distance setting. This is user-observed behavior, not an independently reproduced benchmark, and does not establish scene completeness, GPU time, lowest-frame performance or another machine's results.

Block/item texture tiles default to 32×32; 64×64 requires necessary-detail justification and review. Preserve already approved 64×64 ground/sand bytes until a reviewed replacement is accepted. Material, cutout and mip behavior must be qualified at actual runtime scale. [Texture ownership](../art/BLOCK-TEXTURE-PLAN.md).

## Source map and resolved overlaps

| Source | Retained responsibility | How to interpret it |
| --- | --- | --- |
| [UPSCALING.md](../UPSCALING.md) | Implemented scene reconstruction, menus, temporal blockers | Main implementation contract for Native, Spatial, FSR1 and optional NGX |
| [VULKAN-CAPABILITIES.md](../VULKAN-CAPABILITIES.md) | Adapter bounds, caches, uploads, ownership and optional features | Alpha-scoped audit; its old “upscaling unimplemented” row does not supersede the later graphics module |
| [RENDERER-AND-DIAGNOSTICS.md](../RENDERER-AND-DIAGNOSTICS.md) | Legacy horizon, settings and local diagnostic semantics | Historical alpha14 context; contains a 2–96 range conflicting with its 2–125 introduction and older OpenGL/package wording |
| [LOD-AND-DRAINAGE-ARCHITECTURE.md](../LOD-AND-DRAINAGE-ARCHITECTURE.md) | Authoritative chunk-derived viewer and water/terrain transitions | Supersedes procedural-heightfield descriptions as the active viewer direction; legacy macro horizon remains diagnostic and off by default |
| [Renderer backlog](../renderer-jams/RENDERER-BACKLOG.md) | Open native gates and individual source increments | Append-only evidence; the opening summary is now explicitly a dated jam42 checkpoint, rather than a live status pointer |
| [Rendering design chapter](../research-expanded/2026-10-29/chapters/render-performance.md) | Shared epochs, resource budgets, guide descriptors and fair measurement | Detailed proposed contracts at a frozen Dev77 baseline |
| [Lighting integration chapter](../research-expanded/2026-10-29/chapters/lighting-integration.md) | Host-light authority, contributions, temporal guides and LOD coherence | Implementation bridge, not proof of completed lighting integration |
| [Lighting master replica](sources/LIGHTING-ENGINE.md) | Detailed light transport, materials, GI study and acceptance scenes | Preserve original content; use its detailed scenarios rather than duplicating another full master |

The intended full-detail request ceiling is 125 in the newer summary; 512 belongs to coarse/LOD range, never 512 full-resolution simulated chunks. Runtime effective distance remains subject to heap, dimension height, server delivery and native tracking. The conflicting legacy 96 value remains a documentation discrepancy until a selected artifact's settings and effective values are checked. An “unlimited” option and greater usable distance remain future requirements subject to explicit resource and network constraints.

## Capability and evidence ledger

| Area | Present documented capability | Evidence limit and next work |
| --- | --- | --- |
| Native rendering | Full display-resolution world path and unavailable-provider fallback | Recheck matched native images and owner-device frame tails at the tested commit |
| Spatial scaling | Reduced scene target followed by linear reconstruction | No temporal detail reconstruction |
| FSR1 | Official FP32 EASU and optional RCAS through the common GPU interface; OpenGL/optional Vulkan adapter | Functional/reference receipts exist; owner-device speed/quality remains a separate gate |
| FSR2/3/4 | Owner-requested provider choices; FSR3/4 preferences and unsupported-provider policy are documented | Temporal provider integration and acceptance remain open; do not substitute FSR1 behind a newer active label |
| DLSS | Optional real Vulkan NGX JNI bridge, capability/extent/lifetime contracts | Complete temporal scene guides, device-extension integration, licensed runtime packaging and actual RTX dispatch remain open |
| Frame generation | Separate future feature | No rendered-FPS, simulation-throughput or latency credit from a preference flag |
| Chunk LOD | Authoritative chunk snapshots, revisioned caches, bounded recovery/capture/upload and conservative selection | Native useful coverage, transparency, travel/edit/reopen and pressure gates remain open |
| Submission | Ordered synchronous indexed API batches with a 4,096-record hard limit | This is not GPU MultiDrawIndirect or a replacement near-block renderer |
| Recent CPU changes | Source/component checks for selection, bounds reuse, accounting, cleanup and telemetry | Some isolated tails/regressions remain; no automatic whole-game FPS credit |
| Lighting | Extensive authority/material/transport/acceptance specification; existing native lightmap integration | Advanced GI, shadows, reflections and provider tiers require mapped implementation and native evidence |

An old software-Vulkan acceptance receipt proves its own artifact and exercised actions, not every later source change. Conversely, later Java21 helper tests do not invalidate earlier functional evidence; they add narrower evidence at newer pins. Keep four independent states: **source present → compiled into artifact → exercised → accepted on named platform**.

## Shared scene and ownership contract

Terrain, water, geometry, optical materials, logical light, visual radiance, reconstruction and UI have distinct owners. Gameplay light queries use the selected world-light authority. GI, shadows and upscalers consume accepted snapshots; they cannot become a second water or world-state authority.

Every derived job carries world/dimension identity, attachment generation, source chunk/edit revisions, relevant material/light/water revisions, request generation and cancellation state. GPU work also carries device/resource generation. Check identity at admission and publication. Cancellation revokes publication rights immediately but does not establish that a worker or recorded command has stopped using resources.

Maintain a contribution ledger: native/baseline light, analytic direct light, indirect diffuse, specular reflection, atmosphere and output transform each name their owned contribution. Give providers the exact signal they require; direct-only input differs from already integrated diffuse or reflected radiance. Debug views share the composed frame's revisions and do not feed gameplay queries or histories.

Water shading, motion, depth, normals and absorption thickness consume one coherent accepted presentation epoch. A failed GPU range update retains retryable work and must not advance the CPU mirror as though publication succeeded. LOD presentation never changes finite-water quantity, chemistry or ownership.

For terrain refinement, retain valid covering geometry while required children are pending. Withdraw known-invalid geometry/visibility after edits according to the conservative fallback rule. Do not turn a missing cache page into synthetic authoritative unexplored terrain or silently increase simulation distance to satisfy a visual request.

## Resource admission, lifetime and progress

Count reserved work, CPU/native preparation, upload staging, resident resources, old/new replacement overlap, provider scratch/history and retired references. Track heap, native memory, device estimates and actual reported budget separately. A resident-payload cap does not bound total game memory.

Each stage has one queue owner, admission bound, cancellation rule and progress counter. Near-camera priority also needs bounded service for old eligible work. Record oldest eligible age and denied/deferred reasons; finite queue sizes alone do not prove useful progress. The existing disk-recovery limits—256 entries per worker turn and 8,192 nearest retained keys—bound work items, not filesystem wall time.

The existing graphics-queue/fence path remains a valid baseline. Timeline semaphores, async queues, bindless descriptors, indirect terrain and mesh shaders require measured benefit and compatibility work; inactive toggles must not imply functionality. Retired images, views, buffers and descriptors remain charged until their last recording/submission references finish. Failure quarantine retains uncertain ownership rather than recycling it silently.

Add presentation-specific retirement separately from submit-fence completion, and explicitly record validation blind spots. The newly researched requirements and fixtures **R-L01/LR-A01** and **R-L02/LR-A02** are in [targeted lighting/renderer additions](research/lighting-rendering-additions.md). They are proposed integration checks, not newly discovered runtime failures.

## Temporal reconstruction and settings

The current endpoint reconstructs the reduced world, hand, outlines and world post-processing before native-resolution GUI composition. Minecraft clears world depth before hand rendering. A temporal provider must therefore preserve pre-hand world depth and either reconstruct the hand with matching guides or compose it separately through a qualified path. The current endpoint's final depth cannot be assumed to describe its complete scene color.

A backend-neutral guide descriptor declares render/output extents, viewport and valid region, color domain/primaries, pre-exposure, depth encoding and near/far convention, current/previous transforms, motion direction/units/scale, jitter convention, mask semantics and history reset reasons. Account for camera, entities, animated foliage/textures, water and terrain replacement. Missing correspondence supplies invalidity/reactivity, not fabricated stationary motion. An origin rebase transforms both frames consistently or invalidates the affected history.

One transaction stages requested values, validates provider/device/resource readiness, commits effective behavior and preserves saved intent on fallback. Apply/Revert, close without Apply, reload, resize, world switch and restart-required API changes remain distinct operations. FSR1 omits temporal Native AA and stages a compatible Quality choice for legacy incompatible preferences without prematurely rewriting saved settings. DLSS dimensions come from NGX recommendations rather than copied FSR ratios.

The provider-instance envelope includes admitted dimensions, formats, guide conventions and actual effect version. Recreate or fall back when a dispatch no longer fits. Activation requires successful initialization and evaluation, followed by acceptance of the relevant image/lifecycle gates. Do not apply an ordinary additional TAA or a second tone mapper unless its combined contract is deliberately qualified.

AMD's live FSR4.1.2 manual changed after older documentation and contains inconsistent device guidance. Preserve the discrepancy and query the actual deployed runtime/device rather than assuming broad family support. The source-freshness finding **R-L03** and executable guide fixtures **R-L04/LR-A04** are in [the researched additions](research/lighting-rendering-additions.md). This does not qualify Radeon8060S or any other hardware for Deep Primal FSR4.

## Lighting implementation order

Implement the baseline bridge first: selected host-authority mode, optical edit ingestion, revisioned immutable light snapshots, required baseline shading, conservative pending response and contribution diagnostics. Map each interface to an implementation and evidence pin before adding optional transport.

Then qualify directional shadows and bounded local-light selection, with explicit overflow and stale-cache behavior. Optional GI/reflection providers declare geometry coverage, offscreen behavior, material restrictions, update latency, memory and fallback. A stable probe/cache cannot establish representation of a newly exposed cave or unseen source. Full 3D radiance cascades, ray tracing and other advanced providers remain research/experiment candidates until their actual free-camera, streamed-world requirements pass.

Near/far materials share sky/environment state, light decoding, exposure and optical conventions. Coarse summaries must preserve sealed-cave visibility and calibrated emission footprints. A tiny torch must not become a mountain-sized emitter. A parent used for coverage and a child used for shadows need a conservative agreement rule around openings.

## Exact acceptance gates

| Gate | Required checks and evidence | Promotion rule |
| --- | --- | --- |
| RL-G01 — Artifact and ownership | Exact commit/JAR/native hashes; Java/toolchain; OS/GPU/driver; actual API/provider; packaged module/licenses; renderer handoff | No runtime claim from source presence or an unselected backend |
| RL-G02 — Coherent coverage | Cliffs, caves, player builds/edits, forests, shoreline/water, signed-coordinate corners; stationary recovery, travel, teleport, reconnect and reopen | Accepted state/coverage revisions and reviewable native images; pending gaps separately identified |
| RL-G03 — Lifetime and failure | Upload/close/submission failure, cancel, resize, reload, world/device recreation and shutdown; live-plus-retired accounting; LR-A01/LR-A02 | No invalid reuse, stale publication or lost tracked ownership; recovery limits recorded |
| RL-G04 — Temporal guides | Known-transform guide fixtures LR-A04; preserved world depth; hand/UI decision; exposure, cut, origin/extent changes and water/foliage motion | Real guide conformance before temporal activation/image claims |
| RL-G05 — Lighting truth | Addition/removal/overlapping sources, sealed cave opening, missing neighbor, water optics, LOD boundary, world switch; contribution/raw/filtered debug views | Logical oracle parity plus actual composition/temporal images for the selected tier |
| RL-G06 — Provider settings | Native, Spatial, FSR1 and every available temporal provider; unavailable/failure paths; Apply/Revert, legacy save, reopen, resize/reload | Requested/effective state stays truthful; saved intent and valid fallback preserved |
| RL-G07 — Performance | Matched world/camera/weather/water/cache/resolution/distances; cold/warm/travel/edit/pressure; repeated CPU/GPU p50/p95/p99, sample counts, memory peaks and coverage age | Claim only measured end-to-end gains with matching useful output; retain slow and failed trials |
| RL-G08 — Delivery | Full Java25/Gradle/NeoForge build under the verified publication policy, archive/class checks, native Vulkan priority and supported OpenGL fallback, hardware images/footage | Source-only increments remain source-qualified until these artifact/platform gates pass |

Retain numeric/image tolerances and scene hashes in the selected fixture manifest before comparing candidates. No new arbitrary quality/performance threshold is invented by this merge. The detailed lighting master supplies its larger acceptance scene set.

## Measurement and next work

Correlate frame/epoch IDs across CPU preparation, queue age, uploads, GPU timestamps, publication and presentation. The bounded CPU submission histories do not measure whole-frame GPU completion. Frame intervals include waits and FPS caps; page admission latency includes waiting. Do not add stage percentiles to estimate a frame percentile. Count failures, deferrals, empty work and dropped/unavailable measurements explicitly.

The immediate work order is: establish owner-device matched baselines; qualify lifecycle/coverage under failure and pressure; implement conforming temporal guides; integrate one real temporal provider; then choose the next mesh/streaming/lighting optimization from measured bottlenecks. Compare fixed/adaptive/off frustum modes and Native/FSR1 before promoting experimental defaults. Maintain separate source/component, native-functional, visual and hardware-performance evidence. Historical self-audit scores remain historical and do not become a fresh grade through consolidation.
