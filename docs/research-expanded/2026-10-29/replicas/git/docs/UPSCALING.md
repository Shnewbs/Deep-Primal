# Expanded replica — docs/UPSCALING.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/UPSCALING.md) |
| Source identity | git: docs/UPSCALING.md |
| Preserved source SHA-256 | `24bf231592be9b4a8cac6d77040c22563e0da7a1c2e8a50104c99e31d31c421b` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

Defines staged persistent upscaling settings, native/spatial fallback, implemented FSR 1 EASU and optional RCAS, reduced world-target ownership, native UI composition, compile-before-redirection safety, and a real but inactive optional NGX bridge. It explicitly records the pre-hand world-depth problem and missing temporal renderer inputs.

## Research expansion — gaps and details to deepen

- The current spatial endpoint should gain a formal resource descriptor before any temporal provider is added. Color space, pre-exposure, valid extent, projection/depth encoding, texture origin, motion units, jitter, and history generation must be explicit rather than inferred from a shared texture handle.
- The documented world-depth clear before the hand is a real integration dependency, already identified here. The expansion should resolve it through a preserved world guide and a chosen hand-composition policy, including animated geometry and water motion, rather than simply repeating that motion vectors are required.
- The mode list needs a versioned readiness model so saved FSR_31/FSR_41 or DLSS preferences cannot become effective from FSR 1 or linear-provider booleans. A vendor SDK capability, a compiled bridge, successful initialization, and a successfully evaluated frame are different states.

## Research expansion — proposed contracts

- Specify a provider-neutral reconstruction input contract and per-provider adapter. Require declared depth/color conventions, camera and object correspondence, reset causes, reactive/composition data where applicable, output ownership, state restoration, and the position of tone mapping, sharpening, and native UI.
- Add a capability receipt recording requested mode, active implementation, runtime/effect version, adapter/driver, enabled API requirements, initialization and first-dispatch results, effective fallback, and retry reason. Preserve Apply/Revert semantics and serialized compatibility; FSR 1 remains spatial.
- Define a shared history epoch for world attachment, resource reload, resize, origin rebase, projection change, exposure discontinuity, and source invalidation. Frame generation receives a separate presentation contract and cannot change simulation ticks, finite water, or the meaning of rendered-frame metrics.

## Research expansion — acceptance checks

- Run native FSR 1 reference-image and failure-path checks independently of new temporal work. Exercise compile failure before redirection, current-frame fallback, next-frame native mode, resize, provider recreation, legacy Native AA staging, and saved unavailable requests on both backends.
- For each temporal provider, validate pre-hand world depth, moving entities, water, disocclusion, thin foliage, camera cuts, and large-coordinate origin changes. Require actual native dispatch and state/lifetime verification before showing activation; compare native and reconstructed frames with identical output resolution and scene coverage.

## Research expansion — source anchors

- Implemented modes
- World and interface separation
- DLSS integration status

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Graphics upscaling

Open **Video Settings → Upscaling**. Changes are staged until **Apply**;
**Revert** restores saved values and closing discards unapplied edits. Preferences
live in `config/deepprimal/graphics/upscaling.toml`. The menu distinguishes the
saved/staged selection from the provider that actually completed the last world
frame. No API restart is required for upscaling settings. Changing OpenGL/Vulkan
still requires the existing restart workflow.

## Implemented modes

| Selection | Rendering behavior |
| --- | --- |
| Native | Full display-resolution world rendering. This is the default and unavailable-provider fallback. |
| Spatial | Lower-resolution world rendering followed by linear sampling. Uses the custom 50–100% scale. |
| FSR 1 (Spatial) | AMD's official FP32 EASU reconstruction and optional RCAS sharpening, through the common GPU interface on OpenGL or the optional Vulkan adapter. |
| NVIDIA DLSS | Saved preference with an explicit unavailable reason in this version. The optional native NGX provider exists, but the game renderer does not yet supply the complete temporal inputs needed to activate it. |

FSR is explicitly **FSR 1**, without temporal reconstruction or generated frames.
Its source is pinned to AMD's MIT-licensed implementation, with source and notices
retained under the graphics module's upscaling shader resources. The generated
GLSL retains AMD's EASU and RCAS kernels; the reproducible generator and reference
comparison are in `graphics-settings/tools`.

FSR quality presets use linear ratios of 1/1.3 (Ultra Quality), 1/1.5 (Quality),
1/1.7 (Balanced), and 1/2 (Performance). Custom uses the saved render-scale slider.
Dimensions round upward and never become zero. Quality is the default FSR preset.
For a 1920×1080 display, Quality renders the scene at 1280×720 and reconstructs it
to 1920×1080. Performance uses 960×540. Sharpness is independent of render size;
0 skips RCAS and 100% uses maximum RCAS strength. It has no effect on DLSS.

## World and interface separation

The common graphics module owns a reduced scene color/depth target. A scoped
`getMainRenderTarget` override covers world and hand rendering, entity outlines,
and world post-processing. Reconstruction runs before GUI composition. The
window's native MainTarget remains full size; menus, HUD, text, and normal screen
captures use the full-resolution output. The previous Vulkan MainTarget scaling
mixins are no longer registered, preventing double scaling and blurred GUI.

Shader compilation must succeed before the scene is redirected. FSR failure
latches the provider off; the current completed scene is linearly presented and
subsequent frames render natively. Reapplying settings or reloading resources
allows a new initialization attempt. A failure of the GPU/device or the fallback
itself may still propagate through the host renderer's normal error handling.

Resizing recreates bounded scene/intermediate targets. The render guard restores
the target and window/global-uniform dimensions even when world rendering throws.
Resource reload invalidates provider readiness. Leaving a world or closing the
renderer releases owned textures, views, and uniform buffers. Renderer handoff
to recognized replacement mods also disables these overlapping mixins.

## DLSS integration status

`renderer-vulkan/.../upscaling/dlss` and `src/main/native/dlss` contain an optional
JNI bridge to NVIDIA's real Vulkan NGX API. The bridge handles extension discovery,
adapter requirements, initialization/capability queries, SDK optimal dimensions,
feature creation/evaluation/release, history resets, and failure latching. It
validates input extents, formats, aliasing, thread ownership, and context lifetime.
Its CMake build and pinned SDK requirements are documented beside the native code.

**This does not make DLSS active in this release.** Remaining integration work is:

1. Jittered scene projection and valid motion vectors for both camera and moving
   objects, with the required depth/exposure/history data. The current FSR endpoint
   runs after hand rendering: Minecraft clears the world depth target before drawing
   the hand. That endpoint's depth attachment is therefore not a valid depth image
   for its complete scene color. DLSS needs preserved world depth captured before
   that clear, and an explicit choice about reconstructing or separately composing
   the hand with matching temporal inputs.
2. Required extension negotiation before creating the Vulkan instance/device,
   resource layout transitions, dispatch, and post-dispatch state restoration.
3. Packaging the platform adapter and production NVIDIA runtime under their
   applicable licenses, followed by actual RTX render/lifecycle verification.

No proprietary NVIDIA runtime is bundled here. A GPU brand name is never used
as proof that DLSS is supported. DLSS render dimensions must come from NGX,
not FSR's fixed ratios. Frame generation remains unavailable independently of
super resolution; an older `frameGeneration=true` preference cannot silently
disable supported FSR/spatial rendering.

## Validation

See `docs/validation/upscaling-2026-10-27` for the build and functional receipts.
The production-excluded `UpscalingMenuChecks` and `UpscalingFrameChecks` exercise
the actual client. The kernel comparison checks actual OpenGL output against
AMD's unmodified reference shader, including odd sizes and edge cases. The native
DLSS checks cover compilation, ABI, input contracts, and failure/lifetime behavior;
they do not prove RTX dispatch or DLSS image quality.

Software OpenGL/Vulkan tests establish functional behavior only. They do not
measure a frame-rate improvement on the owner's hardware, certify Windows GPU
drivers, or establish performance superiority.

Primary vendor sources:

- [AMD FidelityFX FSR 1](https://github.com/GPUOpen-Effects/FidelityFX-FSR)
- [NVIDIA DLSS SDK](https://github.com/NVIDIA/DLSS)


<!-- END PRESERVED SOURCE -->
