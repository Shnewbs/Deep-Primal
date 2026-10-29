# Expanded replica — docs/VALIDATION-0.0.1-alpha.10.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.10.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.10.md |
| Preserved source SHA-256 | `e8512650f33b4d3ce88ae2b50611fbda3d36fa8cbe8e792ef08f981df258da04` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Alpha 10 — Vulkan overhead and module separation”. Its original opening states: “Validated on 2026-10-19 against Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. The base is alpha 9 commit c87cf27f6295b572b36035b20e08507733a3ac3f. This is an experimental build.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Carry the renderer result from bounded CPU/model behavior to actual GPU lifetime, submission, presentation, and target-device evidence.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Use separate ledgers for queued, staged, resident, retired, and reusable bytes, and identify the completion signal permitting reuse.
- Name pre-draw preparation, command encoding, API submission, GPU execution, and presentation intervals separately.

## Research expansion — acceptance checks

- Inject failure before allocation, after reservation, during upload, and before publish; verify accounting and in-flight resource validity.
- Run matched native scenes with travel, stationary recovery, minimize/resize, world switch, and device recreation before claiming frame-time or VRAM gains.

## Research expansion — source anchors

- Alpha 10 — Vulkan overhead and module separation
- Limits

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.10/checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.10/checks.txt) |
| `validation/0.0.1-alpha.10/runtime-summary.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.10/runtime-summary.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha 10 — Vulkan overhead and module separation

Validated on 2026-10-19 against Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. The base is alpha 9 commit `c87cf27f6295b572b36035b20e08507733a3ac3f`. This is an experimental build.

## Delivered behavior

The standard installer separates platform-neutral core, common performance, graphics settings and Vulkan into independently built nested modules. Vulkan no longer depends on worldgen/horizon integration. Configs, mixins and runtime services have one owning module. The build rejects cross-module dependencies outside the allowed boundaries, including fully qualified Java references. See [module contracts](MODULES.md).

The backend adds bounded checksummed persistent SPIR-V/driver caches, exact descriptor-set reuse, redundant-bind skipping, portable signed-byte storage-buffer reads, cached format/capability checks, actual enabled-extension reporting and native debug labels. Upload ordering, partial depth/stencil dependencies, acquisition semaphores, compiler results, resized buffer ownership and image-view retirement are corrected. Unused transfer/compute queues, upload manager, indirect-buffer wrapper and duplicate mapped-memory implementation were removed. See the [capability audit](VULKAN-CAPABILITIES.md) for the implemented paths and remaining advanced work.

## Checks run

- `./gradlew clean build compileVisualValidationJava compileModuleValidationJava`: passed, including architecture checks, 200,000 differential surface cases, 3,000 nearest-queue cases, 34,300 ring-grid coordinates, horizon seam/budget checks, CRC/truncation/oversize/LRU cache cases, exact descriptor collisions/A→B→A and dynamic-offset/command-reset cases.
- Dedicated server started, generated and saved the disposable world, saved all dimensions and exited successfully.
- Packaged Vulkan client entered that world, rendered near terrain and horizon, exercised staged performance settings, resized, reloaded resources, captured screenshots, saved and exited successfully. Full GPU checks were repeated after the final clean build.
- Shader source changes produced different bytecode; repeat source hit the cache; invalid shader syntax was rejected; double-close was safe; native result count returned to zero.
- Signed RED8I storage reads at logical byte offset 65,792 returned all 17 expected signed values, including a padded final word. This tests beyond the 64 KiB uniform-texel minimum.
- Buffer copy (16 bytes), timestamps, texture copy (four pixels), both sampled atlas mip levels, nonblack lightmap and D32 depth readback (16 pixels at exactly 0.375) passed.
- DEPTH32_STENCIL8 rendering/copy passed reference 0/1 checks after stencil clear followed by depth-only clear, verifying preservation of stencil data and its producer dependency. This driver does not support the requested DEPTH24_STENCIL8 image usage; the backend rejected it before allocation and the fixture verified rejection.
- Khronos validation was actually active with synchronization validation enabled. Final Vulkan world test, Vulkan module-only boot and UUID-negative boot had **zero validation errors** through shutdown. Existing unused-vertex-output performance warnings remain; this is not a zero-warning claim. The SDK is a development dependency, not bundled in the installer.
- Core/performance/graphics modules booted with OpenGL and Vulkan while root worldgen/horizon was absent. A valid-checksum driver cache with a deliberately changed native UUID was discarded (`loadedBytes=0`).
- Unfinished-startup marker, missing Vulkan driver and Sodium 0.9.2+mc26.1.2 handoff all reached OpenGL, saved and exited successfully. The marker case also passed live entity/glow/block-entity/particle/profile controls.
- `./gradlew build compileVisualValidationJava -PwithoutVulkan`: passed. This package loaded a world using OpenGL despite a saved Vulkan preference, passed performance controls, saved and exited. Restoring the standard build produced the exact original installer bytes.

## Observations from the final Vulkan session

| Counter | Value |
| --- | ---: |
| Descriptor updates | 98,459 |
| Descriptor-set reuses | 99,574 |
| Descriptor binds skipped | 4 |
| Pipelines created | 85 |
| Valid pipeline-creation feedback records | 85 |
| Pipeline feedback cache hits | 72 |
| Warm shader disk-cache hits | 409 |
| Shaders compiled in this warm session | 0 |
| Outstanding native shader results at shutdown | 0 |
| Tracked buffers / images after retirement | 0 / 0 |

These are session operation counts, including a resource reload, not FPS results or comparisons against another renderer. SwiftShader supplied only a 32-byte driver-cache header; pipeline feedback hits include reuse within the running device and do not prove cross-launch driver compilation savings. The separate SPIR-V cache avoided native compilation in the warm session.

## Package audit

Standard installer: `DeepPrimal-26.1.2-0.0.1-alpha.10.jar`, **22,504,023 bytes**.

SHA-256: `839cf84a1d4fb34778ff78cf55023cfc5d4c333bbc96f7ee572c111a2573e424`.

Every compiled project class was compared byte-for-byte with its shipped class; nested module JARs also matched their build outputs exactly. Counts: root 38, core 21, performance 14, graphics settings 16, Vulkan 178. There were no duplicate project classes, moved implementations left in root, development controllers, or repackaged Minecraft/Mojang classes. All 12 terrain data files and four preset/pack files match alpha 9 exactly. The standard package is 14,848 bytes larger than alpha 9.

The without-Vulkan package was **199,835 bytes**, contained the other three nested modules, and had no Vulkan binary. Its SHA-256 was `b8fbeecb8450dfbd180c05db3a6a837040aad410321f626fa7574f3b3b00da36`. Both profiles currently use the same output filename; copy them to separate directories when keeping both.

## Limits

Tests used Linux x64 X11 with SwiftShader Vulkan and llvmpipe OpenGL, a 3 GiB client heap, and locally extracted Khronos validation layers 1.3.275.0. They establish behavior on that environment. Real AMD/NVIDIA/Intel hardware, Windows/macOS, broad modpack combinations, frame-time/FPS improvement and device-specific queue scaling remain unverified. Packed depth/stencil CPU readback is explicitly unsupported. Shader packs, GPU-driven near-block rendering, mesh/ray features and general direct-OpenGL mod emulation are not implemented. The native near-block mesher and gameplay simulation remain in use; this is not complete Sodium/OptiFine/Lithium feature equivalence.

Evidence: [machine-readable summary](validation/0.0.1-alpha.10/runtime-summary.json) and [check markers](validation/0.0.1-alpha.10/checks.txt). Development fixtures are under `src/cacheChecks`, `src/coreChecks`, `src/moduleValidation` and `src/visualValidation`; none ship in the mod.


<!-- END PRESERVED SOURCE -->
