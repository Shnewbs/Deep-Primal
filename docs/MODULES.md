# Runtime modules — alpha 21

The standard installer remains one JAR. It contains separately built nested JARs with one owner for each config, mixin and runtime service.

| Module | Owns | Project dependencies |
| --- | --- | --- |
| `engine-core` | Platform-neutral terrain/noise/drainage, fluid transport, liquid patch meshing/interpolation, voxel LOD storage/reduction, caches, queues and distance algorithms | None |
| `performance-engine` | Chunk scheduling/sending/saving, memory-distance policy, idle-mob policy and runtime metrics | Core |
| `graphics-settings` | Custom graphics tabs, visual/LOD budgets, shared work pacing, renderer ownership/handoff, common GPU mesh/lighting/shader resources and graphics API preference | Core |
| `lod-engine` | Actual-chunk capture, atlas/biome material sampling, edit invalidation, GPU LOD lifetime, cache/streaming and optional world identity channel | Core, graphics API/settings; no root or Vulkan imports |
| `renderer-vulkan` | Vulkan bootstrap, native libraries, device, synchronization, descriptors, shaders, presentation, caches and recovery | Graphics settings (and its core dependency) |
| Root integration | Custom chunk/biome/fluid adapters, water presentation, legacy worldgen/horizon, networking and diagnostics | Core, performance, graphics settings; LOD and Vulkan optional |

The Vulkan implementation imports no worldgen, horizon or performance-engine code. Performance and graphics settings import no Vulkan implementation classes. The settings module detects adapter availability by mod ID and exposes a backend-neutral restart preference. Saving a preference never initializes native libraries. The common performance module has no graphics-module dependency. Root integration supplies the combined experience.

`./gradlew check` enforces these source import boundaries and the platform-neutral core boundary. Native caches and descriptor regression fixtures are development-only. Existing config filenames are preserved; their registration moved to the owning module, rather than being duplicated. Removing Vulkan does not remove visual budgets or common performance policies.

## Builds

- Default lean installer: `./gradlew build` → `DeepPrimal-26.1.2-0.0.1-alpha.21.jar`. Includes core, common performance, graphics and LOD; no Vulkan natives.
- Platform bundle: `./gradlew build -PvulkanPlatform=windows` (or `linux`, `macos`, `macos-arm64`) → a distinctly classified `-vulkan-<platform>.jar` containing the optional adapter and only that platform's shaderc/VMA natives; MoltenVK only on macOS.
- `-PwithoutVulkan` excludes the optional Vulkan project entirely. Without it, the default build can still compile/check the optional module without nesting it into the root installer.
- `-PwithoutLod` omits the LOD runtime from the installer and development mod list. Output gets a distinct `-no-lod` suffix (also composable with Vulkan platform bundles). Graphics settings and world generation remain available.
- `-PbundleVulkan` explicitly bundles all supported Vulkan natives; this larger universal package is not the default download.
- Individual outputs remain under each module's `build/libs`; installing an assembled root installer requires no extraction.

Java 25 / Minecraft 26.1.2 / NeoForge 26.1.2.122. JarJar output is isolated by version/profile and audited against embedded metadata; validation fixtures, temporary files and the rejected standalone experiment are excluded. Install one assembled root JAR, never multiple profiles together.

## Extraction contract

These are real source/runtime boundaries for future selectable distribution, not a finished in-game module manager. A standalone installation must include each module's dependencies, including the core library. Do not install extracted modules alongside the full installer: it already includes them. Worldgen and horizon remain together in root integration. Module removal/migration for existing worlds is not introduced in this update.

Graphics API selection requires restart. OpenGL remains the default; known external graphics replacements take priority, and failed Vulkan startup recovers to OpenGL. Visual-quality budgets, chunk work and simulation policies are separate from API selection.

## River ownership — alpha 13

Drainage math stays in `engine-core`. Native terrain/aquifer integration stays in root `worldgen`; persistent current blocks and server-synced force settings live in root `water`. One common fluid-vector mixin integrates native physics and fluid rendering. Water code imports no Vulkan, graphics-settings or performance implementation. Native boat/item physics handles drift without an additional boat-force hook. This is an extraction-friendly package boundary, not a separately distributable water module yet. Existing worlds containing registered river blocks need the root mod to remain installed.


## Alpha 16 ownership

`engine-core` owns finite-volume, stamina, wave and wake math. The root mod owns server hydrology, saved soil attachments, native machine ports and worldgen adaptation. Client palettes, snow atmosphere and water meshes stay in the root client package. Graphics/performance controls remain in their existing modules; Vulkan is still its own optional module. No gameplay or core code imports the Vulkan implementation.

## Alpha 17 ownership

The platform-neutral core owns `TerrainEngine`, `TerrainNoise`, `WaterTransport`, `ColumnarChunk`, `LodStore` and `LodMesher`. Root `engine` owns Minecraft codecs, chunk/biome generation, the independent fluid/block/bucket and bounded game-thread transport. Root client code owns snapshot capture, atlas/biome material sampling, GPU lifetime and edit invalidation. Networking sends a persistent server world identity; it does not stream generated unseen terrain. The settings module owns configuration and original UI; callbacks expose root diagnostics without importing root classes. `ClientWorkPacer` shares the existing optional-work controller between retained horizon and new LOD work. No core/gameplay code imports Vulkan. The old horizon is only an explicit legacy diagnostic option.

## Alpha 19 ownership

`lod-engine` is an independently built feature. The root mod imports no `com.shnewbs.deepprimal.lod` implementation classes; `LodAccess` in graphics provides availability, clip/fog distances, status and diagnostics, with an inactive default when LOD is absent. `LodConfig` remains the stable graphics-facing configuration contract, registered once by graphics. The pure snapshot/mesh algorithms stay in the shared core. There is no circular dependency.

Shared `TerrainGpuMesh`, native-lightmap binding and the common terrain vertex shader now belong to graphics. LOD owns its fragment shader and two edit/material access mixins. The optional `deepprimal:lod_world` payload retains version 3 and the existing persisted identity path. Servers send it only to peers with that channel, so clients can omit LOD. Existing cache encoding and paths are preserved.

Graphics intercepts native Video Settings opening, preserves the actual parent screen, and installs `PrimalGraphicsScreen` directly. UI accessors/preset preview hooks belong to graphics; the former root menu-button mixin is removed. Renderer replacements keep their own entry. Pure UI hooks do not take terrain-renderer ownership. Preset capture intercepts only a scoped preview and is otherwise inert.

The independent 16-unit water adapter now has a bounded communicating-pool pressure pass and paired volume writes before neighbor notification. Its simulation remains under root `engine`, independent of graphics/LOD/backend implementations. Water presentation still has root fluid/terrain adapters; this is not a claim that the entire near-block graphics engine has been replaced or extracted.

## Alpha 21 liquid presentation

Core owns snapshot-to-geometry math and continuous height retargeting. Graphics owns the backend-neutral GPU allocation helper and liquid shaders. Root client integration owns loaded-world capture, patch scheduling, configuration handoff and lifecycle. Worker inputs contain primitive arrays and never reference a live world. LOD remains a separate optional module; client level events continue to invalidate its snapshots. Server transport stays independent of presentation.
