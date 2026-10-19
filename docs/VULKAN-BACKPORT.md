# Vulkan backport investigation for 26.1.2

Status: compatibility investigation completed; a working Vulkan backend is not included in alpha 8. The current build renders with OpenGL. Deep Primal has an authored horizon renderer using the platform GPU API and automatic compatibility handoff. It does not implement a Vulkan device backend. The distance screen displays the backend actually in use.

The owner requested investigation of the newer Vulkan features while retaining Minecraft 26.1.2 and NeoForge. Official experimental Vulkan support arrived in **26.2**. Later 26.3/26.4 changes build on that renderer. This is a separate client-renderer project; server simulation range and world generation need CPU work of their own.

## Verified evidence

- [Official 26.2 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2) describe experimental Vulkan, OpenGL fallback, shader/hardware requirements, and startup recovery. Vulkan can be slower or less stable on some systems. Its stated initial requirement is Vulkan 1.2 plus dynamic rendering and push descriptors; macOS uses MoltenVK.
- [Official 26.3 notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3) introduce SDL3 window/input integration, MultiDrawIndirect terrain rendering on supported devices, and order-independent transparency. These are substantial rendering changes.
- [26.4 Snapshot 1 notes](https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1) describe a different OIT accumulation algorithm, with modest gains on some devices. That pipeline does not exist in this 26.1.2 build.
- Official 26.2/26.3 client JARs were downloaded through Mojang's version manifest and checked against its SHA-1 digests for local inspection. No game classes, shaders, native libraries, or game JARs are distributed in this repository or mod.
- Public API inspection finds **69 Vulkan class entries in 26.2** and **60 in 26.3**, plus incompatible device APIs. The retained [API audit](validation/0.0.1-alpha.3/renderer-api-audit.json) records the inspected JAR hashes and method differences.

In the inspected 26.1.2 API, presentation and vsync belong to `GpuDeviceBackend`, and textures use `TextureFormat`. In 26.2, presentation is separated into `GpuSurfaceBackend`, texture signatures use `GpuFormat`, and device information/query APIs differ. In 26.3 the API has moved to `com.mojang.renderpearl`, with asynchronous pipeline compilation. Later client dependencies include Vulkan bindings, ShaderC natives and SDL3. These observations mean that loading the newer backend classes into the old renderer directly is not binary-compatible.

`VulkanMod` demonstrates an independent renderer implementation, but its [official installation instructions](https://github.com/xCollateral/VulkanMod) target Fabric. It is not a drop-in NeoForge dependency. No VulkanMod code has been incorporated.

## Required implementation work

The following is an engineering assessment from the API differences, not a claim of an implemented backport:

1. Choose a maintainable backend boundary for 26.1.2/NeoForge. Assess adapting the nearer 26.2 API before the more extensively changed 26.3 API.
2. Implement device, buffer, texture, command-encoder, pipeline and presentation adapters, with correct ownership, synchronization and resource disposal.
3. Adapt shader compilation and resource-pack inputs; validate uniform layouts, texture binding, render targets, depth/stencil and transparency behavior.
4. Integrate window creation, resizing, swapchain recreation, fullscreen, vsync and minimized-window behavior with NeoForge's loading display and client hooks.
5. Add explicit experimental selection, device-capability detection, a working OpenGL fallback, and startup-crash recovery. A setting becomes available only when a functioning backend exists.
6. Test Windows/Linux/macOS and representative AMD/Intel/NVIDIA hardware; verify GUI, terrain, fluids, entities, block entities, screenshots, resource reloads and mod rendering hooks. Profile frame times, CPU submission, GPU work, VRAM and stability against OpenGL.

The Vulkan work is tracked separately from the alpha 3 distance/CPU optimizations. This investigation establishes the compatibility gap and next implementation boundary; it does not establish a working or faster Vulkan port. Unbounded rendering would additionally require a different terrain representation or streaming architecture. A graphics API cannot make infinite full-detail terrain or full simulation finite in cost.

## Reproduce the API inspection

With Java 25 and legitimately obtained local client JARs:

```sh
python3 tools/renderer_audit.py BASELINE_26_1_2.jar CLIENT_26_2.jar CLIENT_26_3.jar --javap PATH_TO_JAVAP
```

The tool prints interface signatures, Vulkan class counts and file hashes. It does not modify the JARs or copy their implementations into the mod.

## Alpha 8 availability probe

`python3 tools/vulkan_probe.py` checks a Vulkan 1.2 instance only. The local loader exists, but instance creation returned `VK_ERROR_INCOMPATIBLE_DRIVER` (-9); [the retained result](validation/0.0.1-alpha.8/vulkan-instance-probe.json) has `rendererValidated: false`. The full backend remains unimplemented. The probe is development tooling and adds no runtime/native dependencies to the production mod. See the [cross-project audit](PERFORMANCE-AUDIT-2026-10-18.md) for Mojang, CurseForge and Hytale comparisons.
