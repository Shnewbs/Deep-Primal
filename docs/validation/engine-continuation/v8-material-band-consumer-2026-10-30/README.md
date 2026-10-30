# Deep Primal dev108 — V8 ecotone material-band consumer

## Source implementation
- `DensityChunkMaterializer` now consumes `V8EcotoneColumnPlan.Column.materialBand()` for the native V8-r6 surface preflight and write path.
- The continuous `snowFraction`, `soilFactor`, and vegetation density remain the authoritative ecotone values; the discrete band is only the bounded block-family projection.
- Snow support/plant admission now uses the same band chosen for the surface write, preventing a surface/support mismatch when continuous snow crosses the explicit snow threshold.
- No physical height, water amount, hydrology ownership, saved graph bytes, renderer-owned files, or legacy V1–V7 path was changed.

## Direct-main commits
- `b54742254f866de6e8ebfc88a0dc0f48fd18c0d8` — consume ecotone material band in native V8 surface pass
- `07ac9e2b67b252bc9d0778a099f34b596c809371` — bump source version to `0.0.1-alpha.29-dev.108-material-band-consumer.1`
- `6090c504e8934c532cfbc3b41e21d9ed24a7d528` — focused source guard

## Focused validation
The guard checks the final source for:
- snow-band selection before registry resolution;
- band-driven top-state selection;
- band-driven snow exclusion in vegetation support admission;
- the dev108 source version.

This is source/static evidence only. It is not Java25/Gradle, native Minecraft, saved-world reopen, GPU, visual-transect, performance, or water-engine acceptance.

## Remaining gates
- Run the owner-controlled Java25/Gradle build and native V8-r6 generation.
- Validate signed-coordinate seams, reversed/parallel generation, and saved-policy reopen/edit behavior.
- Continue toward explicit old/new-generation border handling and authoritative finite-water-cell integration.
