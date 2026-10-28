# Terrain blending — source implementation track

Owner direction, October 28, 2026: the owner built and is using
`0.0.1-alpha.29-dev.53-v8callback.1`; add terrain blending alongside ongoing
engine/water work. Do not deliver or refer to dev.32 as the current build.
The owner continues to handle full builds, Minecraft/GPU testing and JARs.

## Current checkpoint: dev.54-ecotone.1

Implemented `EcotoneBlend` in the normal dependency-free engine-core source set.
It provides normalized weights over Landscape's existing 40-biome palette instead
of immediately collapsing temperature/rainfall/height/slope/patch thresholds to
one biome. Quintic transitions soften those decision boundaries; exact zero-width
controls retain the old strict comparisons. Ocean, moving-river and land domains
remain separate and are selected by authoritative hydrology, not by smoothing.
The component changes neither terrain heights nor water quantities/levels.

This is **explicit-call-only infrastructure, not activated native worldgen**.
`Landscape.biome`, generators, presets, V1–V7 geography and the dev.53 V8 callback
repair are untouched. Existing V8 snapshots must also remain exact. The version
bump identifies new source, not a qualified JAR or a visible terrain improvement.
No new full-engine grade is assigned.

Half-widths in `Widths.PREVIEW` are provisional authoring values in climate,
height and slope units, not a uniform physical transition radius. Every query
uses a fixed decision tree and 40 weights; no world scans, chunk loads, shared
cache or mutable random generator. It allocates a bounded array and objects;
full-pipeline allocation/latency has not been benchmarked. Sample once per useful
column/profile, not blindly for every voxel.

## Integration sequence

1. **Persist the policy before activation.** Add an explicit versioned V8 blend
   profile containing all widths and policy version. Missing fields in old saves
   must preserve the previous hard policy; never apply a new creation setting to
   a saved world. Preserve graph/profile replay and dev.53 callback recovery.
2. **Consume continuous weights.** Feed coherent surface-material parameters,
   snow/soil transitions and vegetation density from the same absolute-coordinate
   climate sample. `dominantBiome()` is diagnostic, not an ecotone implementation.
   Discrete block choice needs a stable spatially correlated field, not per-call
   RNG or speckled per-block white noise. Keep shore/river masks authoritative.
3. **Blend landform joins before hydrology.** Coordinate plains/hills/foothills,
   mountains, plateaus and coasts through versioned density/relief parameters.
   Preserve sharp intentional cliffs, geological layers, canyon walls, river
   incision and lake outlets. Do not blur completed chunks or average liquid
   levels as a cosmetic shortcut. Weighted biome classification alone does not
   implement this geometry work.
4. **Accept the native result.** Verify complete chunk/base-column/height parity,
   signed-coordinate seams, reversed/parallel chunk order, saved-profile reopen,
   player edits, water conservation and multiple climates/seeds. Owner-side
   visual transects and measured CPU/memory tails are required before calling the
   terrain seamless or performance-accepted. Native old/new-generation-border
   handling remains a distinct unimplemented gate, not an automatic save migration.

Use [focused evidence](validation/engine-continuation/terrain-blending-2026-10-28/README.md)
and [scoped state](TERRAIN-BLENDING-WORK-STATE.json). Continue this through the
existing engine builder; preserve the separate renderer and independent auditor.
Do not create a competing terrain builder or overwrite another active scope.
