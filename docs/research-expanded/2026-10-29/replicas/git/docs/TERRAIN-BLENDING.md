# Expanded replica — docs/TERRAIN-BLENDING.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/TERRAIN-BLENDING.md) |
| Source identity | git: docs/TERRAIN-BLENDING.md |
| Preserved source SHA-256 | `b364c4ed989d1c32753795c370d3d756bbee0e1e9fd9533352c546acd2096132` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This October 28 source checkpoint describes explicit-call-only EcotoneBlend infrastructure over the existing forty-biome palette. It uses normalized continuous weights and quintic transitions, preserves exact strict comparisons at zero width, and keeps ocean, moving-river, and land domains under hydrology authority. It expressly does not activate native world generation, change heights or water, qualify a JAR, or modify V1–V7 and existing V8 snapshots. Its integration sequence already requires persisted policy, continuous material/vegetation consumers, separate pre-hydrology landform blending, signed-coordinate and replay tests, owner visual evidence, and a distinct old/new border gate.

## Research expansion — gaps and details to deepen

- Elaboration: normalized weights and zero-width behavior are stated, but numeric validation and a complete malformed-policy rejection contract remain to be written.
- Elaboration: the source calls for correlated discrete selection; a stable sample key, patch correlation policy, and material normalization rule are not yet defined.
- Clarification: existing V8 snapshots must remain exact even though future opt-in V8 blend policy is proposed; the creation-versus-load decoder needs an unambiguous compatibility boundary.

## Research expansion — proposed contracts

- Specify BlendProfile with saved schema/version, all half-widths and units, deterministic selection seed domain, numerical tolerance, and capability flag. Reject NaN/Inf, negative widths, and unsupported schema atomically; missing fields in an existing save select its prior hard policy, not new creation defaults.
- For every sample require finite nonnegative weights and a sum of one within a declared numerical tolerance. Keep the hydrology-selected domain as an input gate; renormalization may not spill marine or river influence into ineligible land.
- Use an absolute-coordinate, versioned correlated selector for discrete block/plant decisions. Define patch scale and transition behavior in world units separately from climate threshold widths. Repeated sampling, worker order, and cache eviction must yield the same selected state.
- Publish one immutable column/context record consumed by surface material, soil/snow parameters, vegetation density, and biome diagnostics. Avoid independent stochastic decisions for each consumer and per-voxel allocations when a column result is reusable.
- Separate material/ecotone activation from density/relief activation. A later landform blend occurs before drainage planning and retains hard geological or hydraulic constraints; it cannot average already committed liquid levels or blur saved edits.

## Research expansion — acceptance checks

- Exercise zero width, tiny positive widths, all threshold intersections, and invalid numbers; compare zero-width output exactly with the preserved hard policy.
- Traverse positive/negative chunk and region boundaries with reversed worker order; compare weight and discrete-selection hashes.
- Load old V8 snapshots after changing creation defaults; their saved geometry and classifications remain exact.
- Measure p95/p99 time and allocation over useful column sampling, then compare owner-side transects across land, river, and coast without upgrading source-only status.

## Research expansion — source anchors

- Current checkpoint: dev.54-ecotone.1
- Integration sequence
- explicit-call-only infrastructure, not activated native worldgen
- Existing V8 snapshots must also remain exact.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `TERRAIN-BLENDING-WORK-STATE.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/TERRAIN-BLENDING-WORK-STATE.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
