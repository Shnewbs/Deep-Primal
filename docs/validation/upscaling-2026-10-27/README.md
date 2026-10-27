# Upscaling implementation and validation — 2026-10-27

**Development version: `0.0.1-alpha.29-dev.32-upscaling.1`.**
Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25, Gradle 9.2.1.

FSR 1 and Spatial scene upscaling are implemented in the actual OpenGL and
Vulkan world-render paths. The new Video Settings → Upscaling tab stages and
persists mode, quality, custom scale and FSR sharpening. The native main target,
HUD and menus retain display resolution. DLSS has a real optional NGX JNI bridge
and a saved setting, but remains explicitly unavailable in-game: full temporal
renderer inputs, dispatch integration, production runtime packaging and RTX
execution are still outstanding. Frame generation is unavailable.

## Acceptance results

| Gate | Result | Evidence |
| --- | --- | --- |
| Full root/module build and regression gates | PASS; `:build :visualValidationClasses -PvulkanPlatform=windows`, exit 0 in 4m36s; core, cache, resource and module-boundary checks included | [Build summary](build-summary.txt) |
| Actual settings screen | PASS; all four method selections, staged changes, Apply, Revert, persistence, reopening, small-window layout assertions and disabled frame generation | [Menu receipt](menu-opengl.txt) |
| Actual OpenGL world rendering | PASS; FSR, Spatial, native fallback/off, odd resize, reload, HUD isolation and disconnect | [OpenGL frames](frames-opengl.txt) |
| Actual Vulkan world rendering | PASS; same stages, verified Vulkan backend, clean resource shutdown | [Vulkan frames](frames-vulkan.txt) |
| Exact distributed Windows JAR on Linux OpenGL fallback | PASS; production root and nested modules loaded from the final archive, validation fixture injected separately; all world stages passed | [Packaged frames](frames-packaged-opengl.txt) |
| AMD kernel/reference comparison | PASS; 25 cases, 100 production draws plus 100 full-reference draws, within one byte per channel | [FSR report](fsr-validation.md) |
| Production GPU API and Vulkan shader conversion | PASS; resize/failure/reload/ownership checks, actual converter layouts and four validated SPIR-V stages | [FSR report](fsr-validation.md) |
| Optional NGX bridge | PASS for Linux compile/link, 16 host contracts, 20 production JNI/ABI checks and 13 explicitly synthetic failure/lifecycle checks; no RTX evaluation | [DLSS receipt](dlss-provider.json) |
| Archive identity and exclusions | PASS; seven CRC-valid archives, exact nested-module metadata and 613 compiled project classes matched byte-for-byte; shader sources/notices match; fixtures and NVIDIA runtime absent | [Package receipt](package.json) |

Native client tests used a disposable Creative superflat world, seed 20261027,
with a visible HUD. Each scaled stage requires at least eight completed actual
upscaling frames. At 800×600, FSR Quality renders 534×400 and Spatial 63% renders
504×378. At 801×601, FSR Quality renders 534×401. Tests also require no upscaler
dispatches for DLSS's native fallback and Off, restored target/dimensions after
render, released resources at mode changes/disconnect, and successful FSR after
a complete resource/shader reload. Four framebuffer readbacks per world run
check native dimensions and nonblank scene output. OpenGL and Vulkan captures
were visually inspected for orientation, visible scene/hand and HUD composition.

## Environment and limits

Actual client rendering ran under Linux/Xvfb with Mesa 25.2.8 software rendering:
llvmpipe OpenGL 4.5 and lavapipe Vulkan 1.4.318. These are functional results,
not measured improvements on the owner's GPU. The final archive contains Windows
x64 shaderc/VMA libraries; exercising its OpenGL fallback on Linux does not test
Windows driver execution. No RTX GPU, actual DLSS output, temporal image quality,
frame generation, hardware performance comparison or broad engine regrade is
claimed. The flat fixture does not establish every terrain/water/transparency
or third-party-renderer combination.

## Defects found and resolved

The first Vulkan fixture failed before FSR, while resizing the title/world-loading
surface. The existing acquire path consumed an uninitialized image index after
`VK_ERROR_OUT_OF_DATE_KHR`. The repaired path acquires before recording or selecting
frame resources, consumes an index only for SUCCESS/SUBOPTIMAL, and recreates and
retries OUT_OF_DATE. SUBOPTIMAL keeps the acquired image through presentation;
resize results still retire staging and acquisition state. Recreation clears stale
bindings and destroys synchronization objects using their original list sizes.
The complete Vulkan fixture subsequently passed.

An earlier broad build stopped while newly added fixtures referenced an older
compiled runtime class. The final full build compiled the consistent source set
and passed. Uppercase vendor-notice resource filenames were changed to lowercase;
the final archive check verifies the corrected names and exact notice contents.
The final source-identity check also removed checkout-only terminal blank lines
from four unchanged files. Recompilation preserved all 613 project class bytes
and the exact previously tested archive SHA-256.

## Artifact and source identity

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.32-upscaling.1-vulkan-windows.jar`

- Size: **125,383,020 bytes**.
- SHA-256: `04a7a8bf6f88c9b69e3b6cba2c24e6030e1d5e8c38192ee14b8c0e8e16a6d083`.
- Original source base: `2d63c595a56928449b71cdd798c89b0c22498a0f`.
- [Source input manifest](source-inputs.json): 646 production source/resource,
  generator, build-configuration and distribution-notice files. External
  dependencies and generated outputs are excluded. The optional DLSS C++ is
  source-only and is not part of the ordinary Java installer.
- Manifest SHA-256: `7a08951e6ef2efe9ee79cdde5d5aab1f4429f0945ffa72b3e92be7866ccebf9e`.

Publication uses the fresh remote main tree and a guarded expected-parent update,
preserving concurrent staged engine/LOD work. The source manifest can be checked
against the containing commit without a self-referential commit-id field.

## Reproduction

Use Java 25 and the repository Gradle wrapper. The native client checks require
a working graphics/display environment and create isolated fixture worlds.

```sh
./gradlew :build :visualValidationClasses -PvulkanPlatform=windows
./gradlew :runClient -PvisualValidation -PupscalingValidation \
  -PgraphicsBackend=OpenGL -PclientRunDir=run-upscaling-menu
./gradlew :runClient -PvisualValidation -PupscalingFrameValidation \
  -PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanRetry \
  -PclientRunDir=run-upscaling-vulkan -PcaptureDir=build/upscaling-vulkan
./gradlew :runClient -PpackagedValidation -PvisualValidation \
  -PupscalingFrameValidation -PvulkanPlatform=windows -PgraphicsBackend=OpenGL \
  -PvalidationJar=build/libs/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.32-upscaling.1-vulkan-windows.jar \
  -PclientRunDir=run-upscaling-packaged -PcaptureDir=build/upscaling-packaged
```

Use the matching Vulkan platform for the machine executing the Vulkan fixture.
The packaging/fallback command intentionally opens the Windows bundle through
OpenGL, which needs no bundled Vulkan native library. FSR kernel/API/converter
reproduction is in [the FSR report](fsr-validation.md). DLSS integration contracts
and remaining work are in [the native provider README](../../../renderer-vulkan/src/main/native/dlss/README.md)
and [the user-facing upscaling description](../../UPSCALING.md).
