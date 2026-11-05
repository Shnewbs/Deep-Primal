# Dev102 — realized V8 base-column projection

## Source checkpoint
- Implementation commit: `ddf4671e62cadbb1fc6a20e9c71919fd8dae3870`
- Guard commit: `0578a05ac9891db5ea4cb04cc2c282cf73b09367`
- Final source blob: `efb2b594c06577ff668e86b65da5494773c42807`
- Guard blob: `e5d63e9fd2a1fe86d3e56f25ae9de2b24f3e57b8`

## Implemented
- Added `DensityChunkMaterializer.baseColumnVegetatedRealized(V8VegetatedReceiptResult, localX, localZ)`.
- Projection starts from the same ecotone base column as the saved plan.
- An unobserved or callback-vetoed plant remains air.
- A receipt bit without a planned placement fails closed.
- The returned `NoiseColumn` is a defensive projection and does not mutate the chunk, saved profile, terrain graph, or water state.

## Validation
The final source and guard were re-fetched from `main`. Static source assertions passed for:
- realized receipt gating,
- fail-closed unplanned-site rejection,
- species-specific projection,
- bounded local-coordinate checks.

This is source-level/API-double evidence only. No Java25/Gradle, native Minecraft, save/reopen, GPU, or full water acceptance was run.
