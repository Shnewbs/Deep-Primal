# Integrated engine services — alpha.29-dev.32-upscaling.1

> Documentation checkpoint: the version/build statements below describe their dated implementation snapshot. Use the [merged handbook](docs/handbook/README.md) and [status/decision ledger](docs/handbook/POLICY-AND-STATUS.md) for the October 29, 2026 consolidation and evidence boundaries. This notice does not qualify a new game build.

The Continue additions now compile against the real core and Minecraft 26.1.2 APIs.
There is no prototype source directory or excluded prototype build profile.

- `EngineCoreModule` is owned by the existing `deepprimal` mod and attaches a query
  facade to each actual saved EngineChunkGenerator after server startup. It reuses
  that generator's terrain, options, channel revision and vertical range. It stops
  owned work on server shutdown and never shuts down caller-owned executors.
- `EngineCore`, `WorldGenPipeline`, `ChunkMaterializer` and `AdvancedDensityField`
  supply bounded CPU occupancy/voxel-face meshes, terrain queries and raycasts.
  Concurrent requests coalesce, dimension keys stay distinct, oversized cache
  entries are rejected, and work admission is bounded. These are query/preview
  services, not a replacement for Minecraft block-model rendering or the shipping
  material/deposit pipeline. Positive occupancy includes terrain caves; meshes do
  not contain block textures, vegetation, ores or player edits.
- `ChunkCache` has synchronized access-order LRU and overflow-safe byte budgets.
  Runtime cache limits live in `config/deepprimal/engine/runtime.toml`.
- `WaterSimulation` uses integer sixteenth-block volume and the production
  pressure-exchange law for closed query grids. Source labels do not mint water;
  explicit additions/removals belong to the caller. Actual worlds retain the
  existing saved finite-water/constituent runtime and environmental accounts.
- Core validation uses lifecycle-driven ticks rather than an orphan timer.
  Metrics are visible through the existing dashboard adapter. Integration checks
  live in coreChecks and are excluded from shipping JARs.
- Duplicate terrain, hydrology and hazard config classes were consolidated into
  the existing registered `.engine` implementations. The draft's unsupported
  earthquake/volcano/landslide switches are not advertised as implemented mechanics.
- **Video Settings → Upscaling** now offers scene-only Spatial scaling and actual
  AMD FSR 1 EASU/RCAS on OpenGL and Vulkan, with native-resolution HUD and menus.
  Quality presets, custom scale, sharpening and Apply/Revert are saved in
  `config/deepprimal/graphics/upscaling.toml` and apply without restarting the game.
  Native remains the default. DLSS has an optional real NGX bridge, but stays
  unavailable until the renderer supplies complete temporal inputs and the native
  runtime is integrated. Frame generation remains unavailable. See the
  [implementation and limits](docs/UPSCALING.md) and
  [functional validation](docs/validation/upscaling-2026-10-27/README.md).

Saved V1–V7 generation is unchanged. Channel revision 5, overflow fixes, approved
art, GUI themes, existing Vulkan/LOD modules and both branches' audit evidence are
retained. Broad engine acceptance and a fresh independent regrade remain separate.
