# Vulkan adapter — alpha 9

The standard JAR contains an optional NeoForge graphics backend adapted from **VulkanMod 0.6.8+26.1.2**. OpenGL is the default. Open **Video Settings → Deep Primal distances → Performance → Graphics API**, select Vulkan, Apply, and restart Minecraft. The screen reports the active API separately from the next-launch preference.

This is a Vulkan device/backend implementation, not an OpenGL translation flag. It uses Vulkan command buffers, buffers/images, shaderc compilation to SPIR-V, descriptors, graphics pipelines, frame fences, timestamps and a GLFW surface/swapchain. Minecraft retains its native chunk mesher, section workers, render passes and gameplay simulation. Deep Primal's horizon uses the same device abstraction. Fabric Loader/API and VulkanMod's custom terrain mesher are not bundled.

## Origin and license

The matching published [VulkanMod version](https://modrinth.com/mod/vulkanmod/version/0.6.8%2B26.1.2) is Fabric-only. Its newer device interfaces match Minecraft 26.1.2; the public default development branch inspected during this work targeted 1.21.11. An older NeoForge fork targeted 1.21.1 and was not used as a compatible binary dependency.

The matching LGPL source was reconstructed from the published artifact using Vineflower 1.11.2 with the Minecraft 26.1.2 classpath, then adapted here. Artifact origin, version, hash and reconstruction details are pinned in [UPSTREAM.json](../renderer-vulkan/UPSTREAM.json). The adapter is separately licensed **LGPL-3.0-only**; its license, GPL text and origin notice are included in its JAR. LWJGL dependencies retain their notices. Minecraft game classes and Fabric modules are not repackaged.

Commercial games in the Vulkan ecosystem are architectural references, not sources of reusable engine code. This implementation derives from the licensed VulkanMod backend and authored integration changes.

## Adaptations

- Select the Vulkan backend before window/device construction. Close NeoForge's early OpenGL loading renderer before replacing its window; use opaque handles across FML's GLFW/Vulkan class-loader boundary. During a failed-device retry, release the failed window while retaining GLFW for the next OpenGL window.
- Retain the native MainTarget and present it through a Vulkan image blit. Correct sprite-atlas projection and screenshot orientation for the different image origin.
- Implement native buffer copies, texture copies/readback, per-draw base vertices and uniforms, bufferless drawing, texel-buffer descriptors, device timestamp queries and generation-aware frame fences.
- Support NeoForge depth/stencil formats and stencil state. Retire views, framebuffer wrappers and image resources without having framebuffer wrappers own their attached textures.
- Cache pipeline variants by compatible attachment formats rather than framebuffer identity. Cache their hashes without per-draw boxing. Rebuild shader/pipeline caches safely during resource reloads.
- Store metadata outside Minecraft pipeline objects. No global OpenGL-call emulation mixins or native vertex-format overwrite are installed.

The preference is `backend=Vulkan` or `backend=OpenGL` in `config/deepprimal-renderer.properties`. A startup marker keeps an incomplete Vulkan startup from repeating on the next launch. Applying a new graphics preference clears that marker. Backend creation failures attempt OpenGL immediately. Known competing renderer mods disable this adapter; Deep Primal's existing renderer handoff remains active.

## Build and limits

Build with `./gradlew build` (Java 25). The adapter is a separate nested JAR with its required LWJGL Vulkan, shaderc and VMA bindings/natives. `./gradlew build -PwithoutVulkan` omits it for a small core-only package. For development runs, `-PgraphicsBackend=Vulkan` selects Vulkan explicitly; ordinary installed clients use their saved preference.

The current backend requires Vulkan 1.2 and the swapchain/dynamic-rendering features it uses. Unsupported initialization falls back to OpenGL. Linux x64 software-driver validation is documented in [the alpha 9 report](VALIDATION-0.0.1-alpha.9.md). Bundled Windows/macOS natives do not establish hardware certification. AMD/Windows performance, shader packs, arbitrary direct-OpenGL mods and broad modpack compatibility remain unverified. Vulkan is experimental; it is not a claim of universal FPS gains or a complete Sodium/OptiFine/Lithium replacement.
