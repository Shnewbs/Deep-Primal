# Expanded replica — renderer-vulkan/src/main/native/dlss/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/renderer-vulkan/src/main/native/dlss/README.md) |
| Source identity | git: renderer-vulkan/src/main/native/dlss/README.md |
| Preserved source SHA-256 | `994596ebe3623562806ad09efe6ffe70e0766f739a5bea96e00765e641460f63` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../../README.md) · [Integrated design](../../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Documents an existing optional direct NVIDIA NGX JNI Vulkan Super Resolution provider, pinned external SDK/header provenance, explicit build inputs, recommended render dimensions, required depth/motion/exposure/jitter, caller-owned images and transitions, drain-before-close behavior, failure latching, and source/native verification limits. It does not claim active game integration or qualified RTX output.

## Research expansion — gaps and details to deepen

- The native contract is already strong; the missing expansion is its binding to actual host passes. World depth must survive the hand clear, motion must include object and water presentation changes, and the renderer must supply the exact extents and conventions rather than treating this as a color-only postprocess.
- The bridge’s tested native compilation and failure fixtures need a readiness receipt distinct from packaged runtime availability, device support, initialization success, and first real evaluation. This avoids both falsely claiming no bridge exists and falsely treating a loaded library as working DLSS.
- The drain callback already covers recorded but unsubmitted work. The renderer integration should prove that callback against its own command and retirement owners, including partial initialization, evaluate failure, resize, reload, and world/device replacement. Device-idle alone is not the full host contract.

## Research expansion — proposed contracts

- Add a call-sequence diagram and typed resource adapter mapping world color/depth, camera and object motion, jitter, pre-exposure, frame duration, reset flags, and output state to the bridge. Validate aliasing, format/usage, sample count, base layer/mip, and lifetime before native evaluation.
- Define a capability receipt with exact bridge/runtime/SDK versions, target architecture, selected physical device, queried requirements enabled before device creation, recommended input extent, support reason, initialization result, and first-dispatch result. Preserve native/FSR 1 fallback and saved user preference independently.
- Keep Streamline as comparison research unless an explicit architecture decision changes the integration. Do not import Streamline frame-generation or Reflex requirements into direct NGX Super Resolution. Record external runtime provenance and distribution qualification from the exact chosen package without asserting unsupported platform or licensing conclusions.

## Research expansion — acceptance checks

- Execute actual RTX tests for camera cuts, animated entities, water, disocclusion, thin geometry, exposure changes, pre-hand depth, and world-origin rebases. Verify image quality and command-state restoration with the exact packaged artifact; AMD-target acceptance must remain possible without this provider.
- Inject failures before native creation, after resource preparation, during evaluation, and during close. Prove every recorded or submitted reference is drained or revoked safely, no output is reused under a stale generation, and activation/fallback diagnostics describe the frame actually presented.

## Research expansion — source anchors

- External dependencies and provenance
- Renderer integration contract
- Verification

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Optional NVIDIA NGX Vulkan provider

This directory implements the actual NVIDIA NGX Super Resolution API through a small JNI
adapter. It is **not active in the current game renderer**. The current renderer does not yet
produce the complete temporal inputs required by DLSS, and the ordinary installers do not
bundle the NVIDIA runtime or this optional native adapter. Selecting DLSS in the graphics
menu must retain native rendering and an explicit unavailable reason until those conditions
are satisfied.

The implementation calls NGX's instance/device extension discovery, per-adapter feature
requirements, initialization, capability query, optimal render-size query, feature creation,
evaluation, release, and per-device shutdown. NVIDIA driver/adapter/runtime results determine
support. GPU vendor strings never enable the mode. A failed dispatch latches the session off
until its resources are retired. This provider does not implement frame generation or ray
reconstruction, and does not use the SDK's deprecated sharpening control.

## External dependencies and provenance

- NVIDIA DLSS SDK: <https://github.com/NVIDIA/DLSS>, compiled here against commit
  `9981c6350dc58c0ce21f024fd65e31e851c31689`.
- Khronos Vulkan-Headers: <https://github.com/KhronosGroup/Vulkan-Headers>, compiled here
  against commit `73814d006638aa00bd553215b054f0f02e457e22`.
- A matching JDK for JNI headers and a C++17 compiler.

No NVIDIA SDK source, static library, runtime DLL/SO, or development runtime is vendored in
this repository by this change. A release that distributes the built adapter or NVIDIA runtime
must retain the applicable NVIDIA SDK license and distribution notices from
<https://github.com/NVIDIA/DLSS/blob/main/LICENSE.txt>. Only the platform's production runtime
belongs in a distributable; SDK development binaries are not automatically packaged.

## Build

Set `JAVA_HOME` to the JDK that supplies the JNI headers. Point CMake at separate SDK
checkouts; neither dependency is fetched by the build:

```sh
cmake -S renderer-vulkan/src/main/native/dlss -B build/dlss-native \
  -DNGX_SDK_ROOT=/path/to/DLSS \
  -DVULKAN_HEADERS_ROOT=/path/to/Vulkan-Headers \
  -DCMAKE_BUILD_TYPE=Release
cmake --build build/dlss-native --config Release
```

The CMake configuration supports Linux x86-64/arm64 and MSVC Windows x64. Linux x86-64 was
compiled and linked during this change; Windows/arm64 compilation and RTX execution remain
unverified. The JNI bridge is explicitly opt-in using
`-Ddeepprimal.dlss.library=/absolute/path/to/libdeepprimal_dlss.so` (Windows:
`deepprimal_dlss.dll`). Loading that library alone does not enable DLSS.

## Renderer integration contract

1. Load the optional adapter before creating a Vulkan instance. Query
   `requiredInstanceExtensions`, then `requiredDeviceExtensions` after selecting the physical
   device. Enable supported requirements during normal instance/logical-device creation.
2. Supply `InputSupport` based on implemented renderer passes: scene color before UI,
   jittered projection, sampled scene depth, and unjittered current-to-previous motion vectors
   for **camera and moving objects**. Depth-based camera-only vectors are insufficient.
   Preserve world depth before Minecraft's hand-rendering path clears it. The current
   color-only FSR endpoint runs after that clear and hand rendering, so its depth attachment
   cannot be passed directly to DLSS as depth for the complete scene color. Integrate DLSS
   before the hand pass or provide separately preserved depth and consistent hand motion/
   composition; do not pair a world-color image with cleared or hand-only depth.
3. Query NGX requirements for the actual physical device. Construct one provider per logical
   device with its actual Vulkan handles and dispatch pointers. Give it writable cache/log
   storage and the directory containing the matching production NVIDIA runtime.
4. Query `optimalSize` for the chosen quality and display size. Allocate those render-sized
   inputs and a separate native-sized output with storage-image usage. `create` records NGX
   feature creation outside a render pass. Unsupported quality/resolution results are rejected.
5. Before `evaluate`, complete the scene passes and insert correct Vulkan access barriers.
   Provide sampled scene color/depth/motion images and a GENERAL-layout output, all from the
   same device, base mip 0, one layer. Motion formats are RG16F/RG32F; sampled depth views
   are D16/D24/D32F. This adapter does not allocate, transition, or own these images.
6. Supply render-pixel jitter, motion-vector scaling into render pixels, actual frame duration,
   and pre-exposure. Apply `resetHistory` on camera cuts, world transitions, teleports, or
   projection discontinuities. Restore renderer bindings after NGX, barrier the output for
   sampling, and compose HUD/menu text at native resolution.
7. On output-size/quality changes, finish or discard all command buffers that reference the
   old feature and wait for GPU completion, then close/recreate the session. The mandatory
   `drainGpu` callback performs that renderer-owned completion boundary. Native close also
   waits for submitted device work. A bare device-idle wait cannot protect unsubmitted
   recording buffers, which must be handled by the callback.
8. On NGX failure, finish/discard the affected command buffer safely, restore native-resolution
   rendering, show the error reason once, and retire the failed provider. Do not keep retrying
   a failed feature every frame. Do not claim DLSS is active before a real evaluation succeeds.

## Verification

`renderer-vulkan/src/nativeChecks/java/.../DlssProviderChecks.java` is a standalone executable
check, excluded from production source sets. Compile it with the three `upscaling/dlss` Java
sources. Run once without a native-library argument for missing-provider behavior, and once
with the built library path to verify the JNI ABI and safe rejection of invalid contexts.
These checks cover invalid/missing temporal resources, aliasing, dimension mismatches,
nonfinite frame parameters, missing-runtime behavior, and JNI loading. They do not substitute
for NVIDIA RTX image-quality, frame-time, resize, world-transition, or shutdown tests.

`DlssFailureChecks` uses the separately built `src/nativeChecks/cpp/DlssFaultFixture.cpp`
test-only library to inject JNI creation/evaluation errors and a failed GPU-drain callback.
It verifies history reset, failure latching, safe close retry, and idempotent close across the
actual Java/native boundary. The fixture contains no NVIDIA algorithm and performs no GPU
work. Its source and library must remain outside production source sets and installers.

Official integration references:

- <https://github.com/NVIDIA/DLSS/blob/main/include/nvsdk_ngx_vk.h>
- <https://github.com/NVIDIA/DLSS/blob/main/include/nvsdk_ngx_helpers_vk.h>
- <https://github.com/NVIDIA/DLSS/blob/main/include/nvsdk_ngx_helpers.h>
- <https://github.com/NVIDIA-RTX/Streamline/blob/main/docs/ProgrammingGuideDLSS.md>


<!-- END PRESERVED SOURCE -->
