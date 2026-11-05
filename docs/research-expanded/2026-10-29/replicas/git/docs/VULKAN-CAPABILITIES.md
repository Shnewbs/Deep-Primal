# Expanded replica — docs/VULKAN-CAPABILITIES.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VULKAN-CAPABILITIES.md) |
| Source identity | git: docs/VULKAN-CAPABILITIES.md |
| Preserved source SHA-256 | `c86ee86e55b9fe9b47cbe1ddc4b4fd6838bfc70c96f20fcc0d6f04e798a7d4ce` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

A detailed alpha-10 capability audit describes bounded SPIR-V and driver-pipeline caches, exact descriptor reuse, coherent staging on a single graphics queue, fences, image/view retirement, actual usage/format checks, optional feature queries, and deliberately unimplemented features. The alpha-14 addendum corrects unsigned storage-buffer range handling.

## Research expansion — gaps and details to deepen

- The deliberately-unimplemented list is dated and must not be read as a current project-wide absence: later documents implement FSR 1 and additional renderer work. Add a supersession map while preserving the audit’s original feature and validation scope.
- The cache limits and identity checks are concrete, but the runtime acceptance record should distinguish cache hit, valid driver acceptance, measured compilation latency, and complete-frame benefit. A high hit rate does not establish lower stutter or safe reuse across a changed device generation.
- Existing synchronization fixes need a reusable hazard descriptor for new lighting/upscaling passes. Queue topology, host coherence, pipeline access/layout, immutable allocation identity, and retirement completion are separate decisions; optional extensions cannot replace those contracts by their mere presence.

## Research expansion — proposed contracts

- Add a source-scoped capability table with supported, queried-only, optional, and unavailable states. Preserve fence-based single-queue operation as a valid baseline; timeline semaphores, synchronization2, async transfers, bindless resources, and GPU-driven commands remain proposals until individually qualified.
- Extend cache provenance with backend/device generation, shader and pipeline ABI, attachment formats, enabled features, corruption/rejection outcome, and fallback compilation. Keep the existing size/entry ceilings and atomic publication while recording evictions and rejected bytes without exposing driver blobs as portable artifacts.
- Specify per-resource producer/consumer stage, access, layout, queue ownership when applicable, format/usage/sample count, and last recorded/submitted consumer. Integrate staging, replacement, histories, and retired resources into a shared peak plan rather than treating optional memory-budget telemetry as a guaranteed allocation allowance.

## Research expansion — acceptance checks

- Test truncated or corrupt caches, shader/resource reload, changed driver/device identity, attachment format changes, missing optional extensions, and unsigned limit boundaries. Verify fallback compilation remains correct and that a failed cache load cannot be reported as an accepted pipeline.
- Validate upload-to-draw, render-to-sample, depth producer changes, resize, and destruction on actual Vulkan hardware, then equivalent semantic behavior on OpenGL. Capture CPU compilation cost and asynchronous GPU/frame timings separately, including cache-cold and cache-warm runs with identical rendered content.

## Research expansion — source anchors

- Implemented paths
- Deliberately unimplemented capabilities
- Alpha 14 cloud storage-range correction

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../renderer-vulkan/UPSTREAM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/renderer-vulkan/UPSTREAM.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
