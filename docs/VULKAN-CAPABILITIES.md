# Vulkan capability and overhead audit — alpha 10

This is an expansion of the embedded LGPL VulkanMod device adapter, retaining Minecraft's native near-block mesher and simulation. It is not the complete upstream VulkanMod terrain renderer. Vulkan is an API with optional capabilities, not a universal performance preset. We enable features used by actual code, query device support, and retain OpenGL recovery. Hardware-specific gains require measurement.

## Implemented paths

| Area | Implementation and bound |
| --- | --- |
| Shader compilation | shaderc performance optimization targeting Vulkan 1.2; preprocess includes/defines before SHA-256 identity. Explicit, idempotent compiler-result ownership covers success, cache-hit and error paths. |
| Persistent SPIR-V | Checksummed atomic disk cache: 2 MiB per entry, 48 MiB total, 1,024 entries. Missing/corrupt/oversized data misses the cache; I/O failure disables that cache, not rendering. |
| Driver pipeline cache | Vendor/device/driver/UUID identity plus native header validation; incompatible data discarded; rejected driver data retries empty. 16 MiB per entry, 32 MiB total, eight entries. Compatible attachment-format variants remain shared. |
| Descriptor updates | Exact buffer/image/sampler/layout snapshots reuse descriptor sets, including A→B→A. Hash collisions miss safely. Pools grow only when necessary and old pools retire after frame completion. |
| Descriptor binding | Command-local state includes pipeline layout, set, bind point and all dynamic offsets. Identical binds are skipped; flush/new-command state is reset. No descriptor-indexing extension is required. |
| Byte-indexed shader data | Signed RED8I texel-buffer reads lowered to packed readonly storage buffers, with logical slice offsets in push constants, physical four-byte padding, and device storage-range/push-constant bounds. Avoids optional R8_SINT uniform-texel format requirements and native view churn. |
| Uploads | A single graphics-queue path preserves API order for writes, draws and copies. Startup uploads batch; active-frame operations record in the current command stream. Staging uses coherent mapped memory and aligned transfer offsets. Unused transfer/compute queue wrappers and upload manager removed. |
| Synchronization | Correct acquisition-semaphore consumption across flushes, same-layout write dependencies, early+late depth tests, and accumulated producers for partial depth/stencil clears. Frame fences protect reuse and retirement. |
| Image support | Cached per-device format/usage/flags/dimension/mip/layer checks reject unsupported requests before allocation. Native D32 readback and combined depth/stencil rendering/copy are supported where the device supports them. |
| Lifetime | Immutable buffer-allocation records survive resize; image views retire before images; descriptor offset storage and compiler/include callbacks are freed. Shutdown drains deferred ownership and reports remaining tracked buffers/images. |
| Optional features | Enable `VK_EXT_memory_budget` for VMA and `VK_EXT_pipeline_creation_feedback` only when advertised. Report actual enabled extensions. Sampler anisotropy/line features use queried device limits. |
| Diagnostics | Real Vulkan debug object names and command labels, opt-in Khronos validation, descriptor/pipeline counts, cache hit/compile counts and native ownership counts. Normal launches do not require SDK layers. |
| Presentation | Native GLFW surface/swapchain, dynamic rendering, resize recovery and existing frame fences/timestamp queries. Pipeline variants use compatible formats rather than framebuffer identity. |

Pipeline driver-cache feedback may report no acceleration even when loading succeeds: the driver owns that optimization. Shader cache hits and skipped descriptor operations are measured separately. The limits above bound the persistent caches; this is not a claim that total game memory is bounded by them.

## Deliberately unimplemented capabilities

| Capability | Reason and next gate |
| --- | --- |
| Timeline semaphores / synchronization2 | Current single graphics upload path uses validated fences/barriers; conversion needs a measured scheduling benefit and equivalent compatibility coverage. |
| Async transfer / async compute | No independent compute workload; separate queues would require ownership transfers and could add overhead. Do not expose an inactive toggle. |
| Descriptor indexing / bindless textures / descriptor buffers | Requires renderer and shader-layout redesign plus device capability/fallback tests. Current exact reuse reduces overhead without those requirements. |
| GPU-driven culling / indirect terrain submission / mesh shaders | Requires replacement near-block mesh/submission architecture and conservative visibility proof. Native meshing remains in use. |
| Ray tracing / variable-rate shading / upscaling / frame generation | Separate rendering features, assets and quality constraints; no implementation or performance claim here. |
| Parallel Vulkan command recording / asynchronous pipeline creation | Shared mutable draw/uniform state must be partitioned before threading; stalls and CPU costs must be profiled on hardware. |
| Shader packs / arbitrary direct-OpenGL mod emulation | No shader-pack contract or general OpenGL emulation layer. Known renderer replacements trigger handoff. |
| Packed depth/stencil CPU readback | Explicitly rejected because generic pixel sizes do not express Vulkan's per-aspect packing. Rendering, clears and whole-image copies are separate supported operations. |

Enabling every extension would increase requirements without making unused functionality faster. Future work should be driven by GPU/CPU traces, cold/warm shader and pipeline timings, p95/p99 frame times, upload stalls, memory budgets and mod compatibility on AMD/NVIDIA/Intel hardware. Linux software-driver correctness tests cannot establish these numbers.

## Primary references checked

- [Khronos: pipeline caches](https://docs.vulkan.org/guide/latest/pipeline_cache.html)
- [Khronos: properties, features, extensions, limits and formats](https://docs.vulkan.org/guide/latest/querying_extensions_features.html)
- [Khronos: synchronization and validation](https://docs.vulkan.org/guide/latest/synchronization.html)
- [VulkanMod source and license](https://github.com/xCollateral/VulkanMod); the exact imported artifact remains pinned in [UPSTREAM.json](../renderer-vulkan/UPSTREAM.json).

Commercial Vulkan engines provide design context; their proprietary implementations are not imported. Existing public branch features do not establish features in this adapter.

## Alpha 14 cloud storage-range correction

`VkPhysicalDeviceLimits.maxStorageBufferRange` is a Vulkan uint32 represented by a Java int. The signed-byte cloud buffer path now widens it with `Integer.toUnsignedLong` before comparing positive buffer sizes. A reported limit with its high bit set no longer rejects every cloud buffer. Real device-range checks remain enabled. Pure checks cover 2 GiB/4 GiB unsigned boundaries, and the native GPU fixture reads seventeen signed bytes from a slice at offset 65792. Vulkan remains isolated in its existing module; water visuals use Minecraft's common GPU interface and native lightmap.
