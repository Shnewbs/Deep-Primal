# V8 relief replay sampling handoff

## Implemented

- Added `V8GeneratorSnapshot.Prepared.sampleHeightAt(int worldX, int worldZ)` for explicit saved-profile relief sampling at absolute coordinates.
- Added `requireExactHeightReplay(...)` using exact IEEE-754 bit comparison so signed-coordinate/reversed-parallel replay can fail closed before native writes.
- Sampling is available only for explicit saved profile revisions (version >= 2); legacy V8 remains unchanged.
- The helper reads the persisted compiled graph and does not mutate chunks, choose a dominant biome, or create physical water.

## Direct-main commits

- `187dd74b155de51bbbfe5e84e6501ad4b6227e8d`
- `9799ae47ac4e80c78df3486e8994ae3bb718eb4e`
- `0c62af5b6f91d64e2c90390d05930d5910714e82`

## Validation

The focused source guard is intended to be run as:

    python3 tools/test_v8_relief_replay_guard.py

This cycle did not run Java25/Gradle, native Minecraft save/reopen/edit replay, GPU validation, visual transects, performance measurement, or full water acceptance.