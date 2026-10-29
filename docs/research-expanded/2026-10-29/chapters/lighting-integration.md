# Integrating the lighting specification with the renderer

**Status:** Proposed implementation bridge, expanded on 2026-10-29. The existing [lighting master](../replicas/project/LIGHTING-ENGINE.md) remains the detailed authority for light transport, contribution ownership, streaming validity, water, materials, and acceptance scenes. This chapter connects that specification to the source-scoped renderer and upscaling work; it does not claim those lighting features are implemented.

The [lighting research notebook](../replicas/project/LIGHTING-RESEARCH-NOTES.md) supplies the underlying engine and algorithm study. The new [rendering research](../research/render-performance-research.md) and [rendering integration chapter](render-performance.md) cover resource budgets, renderer capabilities, temporal reconstruction, and evidence quality. Their role is to resolve integration seams, not to duplicate the master specification.

## 1. Bind conceptual responsibilities to concrete owners

The master already distinguishes authoritative light state from rendering effects and specifies host-authority rollout modes. Implement that decision as an explicit mode selected at world attachment. The Minecraft/NeoForge adapter translates optical edits, chunk identity, block/material metadata, queries, and lifecycle events. The renderer consumes immutable published data. Neither the optional Vulkan module nor an upscaling provider owns gameplay light queries.

Create an integration manifest that maps each master interface to an implementation, its source revision, and its evidence level. Initial entries should cover edit ingestion, chunk snapshot capture, light propagation, border exchange, conservative pending fallback, mesh light sampling, dynamic-light selection, shadow scheduling, temporal guides, and diagnostic publication. An unimplemented entry remains unavailable. A placeholder texture or vendor library presence cannot satisfy the manifest.

Use the renderer's common world/device/resource generations and append the lighting-specific geometry, optical-material, source, environment, and solver-policy revisions required by master sections 7 and 12. Publication checks the whole dependency set. Reusing a coordinate after a world switch must never reuse the prior world's light, shadow, probe, reflection, or reconstruction history.

## 2. Preserve contribution ownership at the host boundary

The energy ledger in master section 3.3 already prevents double counting and unsafe subtraction from a maxima-combined baseline. The integration task is to preserve those semantics across host lightmaps, LOD vertex data, dynamic emitters, enhanced GI, water shading, and post-processing.

Give every framegraph resource a semantic name and descriptor. Required examples are authoritative gameplay light, baseline visual estimate, direct surface radiance, qualified diffuse-transport result, pre-reflection radiance, reflection contribution, and final scene color. A provider that expects direct-only radiance receives that contract even when the renderer has a richer internal estimate. Add diffuse bounce to a provider input only when that provider is explicitly qualified for it, as required by master section 9.9.

Map the existing LOD material/light encoding to those products field by field. Preserve sky and block terms where the selected representation requires them. A low-resolution proxy cannot be shaded once from baked host light and again from the same enhanced contribution without a declared replacement or blend rule. The integration manifest should identify which stage owns the final multiply, interpolation, and exposure conversion.

This is also an image-debugging contract. Provide contribution views that use the same source revisions as the composed frame. An apparent light leak should be traceable to baseline propagation, stale geometry, shadow visibility, probe transport, reflection history, or output transform. Debug colors must not enter gameplay queries or provider histories.

## 3. Connect temporal lighting to actual upscaling inputs

The source-scoped upscaler currently implements FSR 1 spatial EASU/RCAS. Its endpoint includes world and hand rendering before reconstruction, with native-resolution UI afterward. The optional direct NGX bridge exists, but the project documents missing complete temporal renderer integration and unqualified RTX execution. These boundaries come from [UPSCALING.md](../replicas/git/docs/UPSCALING.md) and [the NGX native README](../replicas/git/renderer-vulkan/src/main/native/dlss/README.md).

The immediate dependency is a trustworthy guide-production layer. Preserve world depth before the host clears depth for the hand. Publish previous and current transforms, texture origin, viewport, projection/depth encoding, render/output extents, exposure, jitter, and history-reset reasons. Motion must include camera, entity animation, terrain replacement, and water presentation. If previous geometry is unavailable, mark the correspondence invalid rather than manufacturing a stationary surface.

The lighting master already specifies light-change and geometry-change invalidation. Feed those events into the reconstruction adapter as well as lighting caches. A newly opened cave, removed emitter, changed transmission material, LOD replacement, origin rebase, or abrupt environment cut can invalidate different regions or products. Define which histories can preserve valid samples and which must reset; a global reset is a safe initial implementation when localized correctness is not yet proven.

Keep one exposure owner and one output-transform owner. Provider adapters state whether their color is scene-linear or display-referred and whether pre-exposure has been applied. A temporal provider cannot infer that from the texture format. The FSR 1 path remains separately qualified while temporal providers are introduced. Do not stack an ordinary TAA pass over a provider that already owns temporal reconstruction without a deliberately tested contract.

## 4. Publish water and LOD changes coherently

The terrain document already records the bounded settling and continuous resident-height publication repairs. The lighting master already forbids visual effects from changing finite-water authority. Integration must connect these two rules: lighting and motion consume the same committed water presentation epoch that produced the rendered surface.

A successful CPU water calculation is not a successful GPU boundary update. A failed range upload must retain retryable state and must not advance the CPU mirror as if the GPU had accepted it. A frame can use an older valid presentation state where the master permits it; it cannot claim current geometry with stale depth, absorption thickness, normals, or motion. See [continuous resident publication](../replicas/git/docs/LOD-AND-DRAINAGE-ARCHITECTURE.md) and master sections 8–9.

For LOD transitions, publish a coherent representation with compatible material and light provenance. Keep covering geometry where valid while refinement is pending. When an edit invalidates that representation, use the master’s conservative fallback immediately. A parent selected for coverage and a child selected for shadows must not disagree about an opening without an explicit conservative visibility policy.

Add combined image fixtures: moving water across a near/LOD boundary under a moving light; cave opening during probe lag; emissive removal while reconstruction history is active; and repeated world attachment at identical coordinates. These tests extend the existing acceptance scenes with host integration, rather than defining a second lighting model.

## 5. Share a resource plan with the rest of the renderer

Lighting's resource planner must participate in the same admission decision as LOD pages, upload staging, reconstruction history, provider scratch, and retired allocations. Budgeting each subsystem independently can admit a combined peak that fits none of the intended device profiles.

Each allocation records its owner, generation, format, extent, sample count, mip/layer count, lifetime, and last consumer. Replacement and resize plans include overlap between old and new resources. Retired images stay charged until no recorded or submitted work can access them. The master already provides concrete memory arithmetic and degradation rules; integration supplies the actual backend measurements and avoids double counting shared depth or motion textures.

Start from the existing fence-based Vulkan architecture and equivalent OpenGL lifetime semantics. Optional GI, reflection, or reconstruction features negotiate formats and usages through the active device profile. Failure to admit an optional feature selects the master's required fallback and reports the effective profile. It never silently lowers gameplay light correctness or rewrites the user's saved request.

The direct NGX bridge requires a host drain contract that covers recorded but unsubmitted commands, not merely a device-idle wait. Apply the same ownership discipline to every external provider. Existing renderer cleanup helpers preserve primary failures and attempt later releases; they do not independently establish device-loss recovery.

## 6. Make the first integration increment reviewable

Build a minimal executable path in this order: host-authority mode and optical edits; immutable light snapshots with revision checks; required baseline shading; conservative pending behavior; shared depth/color/motion descriptors; then one optional enhancement behind a qualified fallback. The ordering proves the contracts before tuning a sophisticated GI implementation.

Correlate the existing 240-sample CPU submit/upload probes with frame identifiers, light publication age, fallback counts, GPU timestamps, and current-plus-retired memory. The preparation-failure probe improves CPU observability, but it does not measure GPU lighting or empty frames. Lighting acceptance must include the whole frame and the time from an authoritative edit to a valid visible result. The source timing boundary is documented in [submit telemetry](../replicas/git/docs/renderer-jams/20261029-submit-stage-timing/README.md) and [pre-transform failures](../replicas/git/docs/renderer-jams/20261029-pretransform-failures/README.md).

The acceptance package should contain the exact artifact/device manifest, deterministic edit replay, contribution debug images, baseline/enhanced comparisons, world-switch and resize traces, memory peaks, and explicit unavailable-provider results. Validate Vulkan first and the supported OpenGL fallback on the same scene definitions. A correct source oracle establishes light-state behavior; native images establish the adapter and composition behavior. Both are required before the integration is described as complete.
