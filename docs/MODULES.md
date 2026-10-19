# Runtime modules — alpha 10

The standard installer remains one JAR. It contains separately built nested JARs with one owner for each config, mixin and runtime service.

| Module | Owns | Project dependencies |
| --- | --- | --- |
| `engine-core` | Platform-neutral bounded caches, queues, distance and horizon algorithms | None |
| `performance-engine` | Chunk scheduling/sending/saving, memory-distance policy, idle-mob policy and runtime metrics | Core |
| `graphics-settings` | Client visual budgets, profiles, renderer ownership/handoff, graphics API preference and settings screens | Core |
| `renderer-vulkan` | Vulkan bootstrap, native libraries, device, synchronization, descriptors, shaders, presentation, caches and recovery | Graphics settings (and its core dependency) |
| Root integration | Worldgen, horizon terrain, networking, diagnostics and game-facing entry points | Core, performance, graphics settings; Vulkan optional |

The Vulkan implementation imports no worldgen, horizon or performance-engine code. Performance and graphics settings import no Vulkan implementation classes. The settings module detects adapter availability by mod ID and exposes a backend-neutral restart preference. Saving a preference never initializes native libraries. The common performance module has no graphics-module dependency. Root integration supplies the combined experience.

`./gradlew check` enforces these source import boundaries and the platform-neutral core boundary. Native caches and descriptor regression fixtures are development-only. Existing config filenames are preserved; their registration moved to the owning module, rather than being duplicated. Removing Vulkan does not remove visual budgets or common performance policies.

## Builds

- Standard: `./gradlew build`.
- Without native Vulkan: `./gradlew build -PwithoutVulkan`.
- Individual outputs: `engine-core/build/libs`, `performance-engine/build/libs`, `graphics-settings/build/libs`, `renderer-vulkan/build/libs`.
- Independence fixture: `./gradlew runClient -PmoduleOnlyValidation -PgraphicsBackend=OpenGL`. With Vulkan included, use `-PgraphicsBackend=Vulkan -PvulkanRetry -PvulkanValidation` to test the other backend. The fixture loads the child modules and stops at the title screen while asserting the root worldgen/horizon mod is absent.

Builds use Java 25, Minecraft 26.1.2 and NeoForge 26.1.2.122. The standard and without-Vulkan build profiles currently share an output filename; keep their deliverables in separate directories.

## Extraction contract

These are real source/runtime boundaries for future selectable distribution, not a finished in-game module manager. A standalone installation must include each module's dependencies, including the core library. Do not install extracted modules alongside the full installer: it already includes them. Worldgen and horizon remain together in root integration. Module removal/migration for existing worlds is not introduced in this update.

Graphics API selection requires restart. OpenGL remains the default; known external graphics replacements take priority, and failed Vulkan startup recovers to OpenGL. Visual-quality budgets, chunk work and simulation policies are separate from API selection.

## River ownership — alpha 13

Drainage math stays in `engine-core`. Native terrain/aquifer integration stays in root `worldgen`; persistent current blocks and server-synced force settings live in root `water`. One common fluid-vector mixin integrates native physics and fluid rendering. Water code imports no Vulkan, graphics-settings or performance implementation. Native boat/item physics handles drift without an additional boat-force hook. This is an extraction-friendly package boundary, not a separately distributable water module yet. Existing worlds containing registered river blocks need the root mod to remain installed.
