# Expanded replica — docs/VULKAN-BACKPORT.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VULKAN-BACKPORT.md) |
| Source identity | git: docs/VULKAN-BACKPORT.md |
| Preserved source SHA-256 | `9598ec2cb6c0fba4cdcda1fc0d694fff77550245e4620e7233c0d4259d5a7d9f` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

Documents the real optional Vulkan adapter, its pinned VulkanMod-derived origin, thin integration boundary, early device/window handoff, shader and resource adaptations, image-view retirement, pipeline-format handling, restart selection, and saved fallback. It already distinguishes an actual backend from an instance-creation probe or renamed OpenGL path.

## Research expansion — gaps and details to deepen

- The next integration layer is a machine-readable capability and packaging receipt. The existing required-feature and build notes should expose which features were advertised, actually enabled, and exercised, with exact driver/device/runtime identities rather than one generic Vulkan-supported boolean.
- The resource lifetime notes need to meet optional lighting and reconstruction providers at a common ownership boundary. Recorded but unsubmitted commands, descriptor/image-view references, resize overlap, and failed cleanup must participate in the same retirement and memory accounting as resident LOD buffers.
- Historical upstream origin, project adaptation, native platform payload, and current artifact qualification should be navigable separately. A matched source origin or successful Linux build does not establish Windows AMD pixels, optional NVIDIA runtime distribution, or every supported platform’s packaging.

## Research expansion — proposed contracts

- Add a backend initialization manifest containing source/artifact identity, selected physical device, API version, required and optional features, format/usage limits, enabled extensions, native library provenance, startup marker, active backend, and explicit fallback reason. Keep the saved request intact when startup chooses OpenGL.
- Define adapter operations for resource creation, recording references, submission completion, safe retirement, and bounded failure quarantine. Preserve image-view-before-image destruction and pipeline attachment compatibility; external providers must restore documented command state and satisfy host drain requirements.
- Provide a packaging and troubleshooting map linking the module README, capabilities audit, cache formats, upscaling contract, and native provider README. Dedicated-server and lean builds should have explicit noninitialization and dependency expectations, without loading graphics natives merely to inspect settings.

## Research expansion — acceptance checks

- Exercise requested Vulkan with supported device, missing required feature, unavailable native, failed early window setup, and restart fallback. Verify active identity and diagnostics reflect the selected backend while persisted intent and nonrenderer settings remain unchanged.
- Run exact-artifact resize/reload/world-switch/shutdown and partial provider initialization tests with validation enabled where available. Check descriptor/view/image lifetime, stale generation rejection, bounded retained memory, screenshots/orientation, hand/UI composition, and OpenGL fallback on the named target platforms.

## Research expansion — source anchors

- Adaptations
- Build and limits
- Alpha 10 organization and native work

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../renderer-vulkan/UPSTREAM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/renderer-vulkan/UPSTREAM.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Vulkan adapter

Vulkan is the primary development sample/reference API and graphics validation target. The separately replaceable NeoForge backend is adapted from **VulkanMod 0.6.8+26.1.2**. The lean default JAR omits the optional adapter; include it using a Vulkan build profile below. Installed clients retain their saved API preference, with OpenGL as the recovery fallback. Open **Video Settings → Deep Primal distances → Performance → Graphics API**, select Vulkan, Apply, and restart Minecraft. The screen reports the active API separately from the next-launch preference.

This is a Vulkan device/backend implementation, not an OpenGL translation flag. It uses Vulkan command buffers, buffers/images, shaderc compilation to SPIR-V, descriptors, graphics pipelines, frame fences, timestamps and a GLFW surface/swapchain. Minecraft retains its native chunk mesher, section workers, render passes and gameplay simulation. Deep Primal's horizon uses the same device abstraction. Fabric Loader/API and VulkanMod's custom terrain mesher are not bundled.

## Origin and license

The matching published [VulkanMod version](https://modrinth.com/mod/vulkanmod/version/0.6.8%2B26.1.2) is Fabric-only. Its newer device interfaces match Minecraft 26.1.2; the public default development branch inspected during this work targeted 1.21.11. An older NeoForge fork targeted 1.21.1 and was not used as a compatible binary dependency.

The matching LGPL source was reconstructed from the published artifact using Vineflower 1.11.2 with the Minecraft 26.1.2 classpath, then adapted here. Artifact origin, version, hash and reconstruction details are pinned in [UPSTREAM.json](../renderer-vulkan/UPSTREAM.json). The adapter is separately licensed **LGPL-3.0-only**; its license, GPL text and origin notice are included in its JAR. LWJGL dependencies retain their notices. Minecraft game classes and Fabric modules are not repackaged.

Commercial games in the Vulkan ecosystem are architectural references, not sources of reusable engine code. This implementation derives from the licensed VulkanMod backend and authored integration changes.

## Adaptations

- Select the Vulkan backend before window/device construction. Close NeoForge's early OpenGL loading renderer before replacing its window; use opaque handles across FML's GLFW/Vulkan class-loader boundary. During a failed-device retry, release the failed window while retaining GLFW for the next OpenGL window.
- Retain the native MainTarget and present it through a Vulkan image blit. Correct sprite-atlas projection and screenshot orientation for the different image origin.
- Implement native buffer copies, texture copies/readback, per-draw base vertices and uniforms, bufferless drawing, packed readonly storage-buffer descriptors for signed-byte texel reads, device timestamp queries and generation-aware frame fences.
- Support NeoForge depth/stencil formats and stencil state. Retire views, framebuffer wrappers and image resources without having framebuffer wrappers own their attached textures.
- Cache pipeline variants by compatible attachment formats rather than framebuffer identity. Cache their hashes without per-draw boxing. Rebuild shader/pipeline caches safely during resource reloads.
- Store metadata outside Minecraft pipeline objects. No global OpenGL-call emulation mixins or native vertex-format overwrite are installed.

The preference is `backend=Vulkan` or `backend=OpenGL` in `config/deepprimal-renderer.properties`. A startup marker keeps an incomplete Vulkan startup from repeating on the next launch. Applying a new graphics preference clears that marker. Backend creation failures attempt OpenGL immediately. Known competing renderer mods disable this adapter; Deep Primal's existing renderer handoff remains active.

## Build and limits

Use Java 25. The primary Linux sample build is `./gradlew -PvulkanPlatform=linux build`; use `windows`, `macos` or `macos-arm64` for that platform, or `-PbundleVulkan` for universal natives. The adapter remains a separate nested JAR with its LWJGL Vulkan, shaderc and VMA bindings/natives. `./gradlew -PwithoutVulkan build` excludes the optional module for renderless/core or lean-package checks. Such server checks validate neither graphics API.

The primary native sample launch is `./gradlew -PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanValidation runClient`. Verify the active backend explicitly: a recovered OpenGL launch is not a Vulkan pass. Ordinary installed clients use their saved preference. Keep OpenGL fallback comparisons after the Vulkan gates, preserving modularity and saved preferences.

The current backend requires Vulkan 1.2 and the swapchain/dynamic-rendering features it uses. Unsupported initialization falls back to OpenGL. Historical Linux software-driver validation is documented in [the alpha 9 report](VALIDATION-0.0.1-alpha.9.md); current checks and remaining limits are recorded in [the engine handoff](ENGINE-BUILD-AUDIT-HANDOFF.md). Bundled Windows/macOS natives do not establish hardware certification. AMD/Windows performance, shader packs, arbitrary direct-OpenGL mods and broad modpack compatibility remain unverified. Vulkan is experimental; it is not a claim of universal FPS gains or a complete Sodium/OptiFine/Lithium replacement.

## Alpha 10 organization and native work

The backend now depends on the independent graphics-settings module rather than the worldgen/horizon integration. Performance policies have a separate common module. See [module boundaries](MODULES.md), [capability audit](VULKAN-CAPABILITIES.md) and [alpha 10 validation](VALIDATION-0.0.1-alpha.10.md). Persistent caches, exact descriptor reuse, native ownership cleanup and corrected synchronization are implemented; advanced Vulkan capabilities remain explicitly tracked rather than represented as inactive settings.


<!-- END PRESERVED SOURCE -->
