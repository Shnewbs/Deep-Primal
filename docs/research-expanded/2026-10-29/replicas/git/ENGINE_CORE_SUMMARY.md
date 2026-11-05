# Expanded replica — ENGINE_CORE_SUMMARY.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/ENGINE_CORE_SUMMARY.md) |
| Source identity | git: ENGINE_CORE_SUMMARY.md |
| Preserved source SHA-256 | `7f6c5fe714404ec41fa908ef62e233937a970ae80d0fd552b47797aa0ee34a00` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../README.md) · [Integrated design](../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../chapters/architecture-authoring.md) · [Discrepancy register](../../DISCREPANCIES.md)

## Research expansion — source scope

The integrated-engine summary documents a dev32 snapshot of lifecycle-owned query services, bounded density and voxel preview work, cache budgets, closed-grid water simulation and scene-only Spatial/FSR1 settings. It explicitly excludes block models, textures, vegetation, deposits and player edits from query meshes, and distinguishes the closed query water grid from the saved finite-water runtime. These distinctions are useful existing contracts. Its dev32 heading is a document epoch: the separately inspected pinned build properties identify dev77 source, while the work-state checkpoint identifies dev53; none of those labels alone proves a newly qualified runtime.

## Research expansion — gaps and details to deepen

- The summary already names ownership and bounded admission, but readers need the observable lifecycle consequences: which requests may finish after clear, which results may publish after close, what a dimension identity contains, and which party owns cancellation and executor shutdown. Those distinctions are scattered across later cache and terminal-query records.
- Query occupancy, preview meshes, host rendering and actual-world simulation appear in one short list. Add a capability-status matrix so a future caller cannot infer that the query mesh is authoritative for edited terrain, collisions, water placement or lighting geometry. Preserve the explicit unavailable DLSS and frame-generation states.

## Research expansion — proposed contracts

- Document a host-adapter contract for world attachment, immutable query input identity, request coalescing, admission rejection, generation invalidation and terminal closure. Include a result envelope carrying world, profile and revision identity, plus the consumer's obligation to reject stale completion before publication.
- Link the three configuration ownership examples to the central scope/path manifest: engine runtime cache settings, graphics upscaling preferences and authoritative world-generation profiles. Give each capability a source checkpoint, artifact checkpoint and last native evidence reference, with this summary's historical epoch retained.

## Research expansion — acceptance checks

- Use blocked and inline caller executors to verify close cannot be held behind terrain computation, terminal caches stay sealed, same-key work reserves capacity once and distinct dimensions never alias. Verify caller-owned executors remain usable after engine closure.
- Trace one real saved generator into the query facade and confirm matching range, options and revision. Separately inspect a player-edited chunk and prove documentation never presents the unedited query preview as current materialized geometry. Verify unavailable providers remain visibly unavailable.

## Research expansion — source anchors

- Integrated engine services — alpha.29-dev.32-upscaling.1

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Integrated engine services — alpha.29-dev.32-upscaling.1

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


<!-- END PRESERVED SOURCE -->
