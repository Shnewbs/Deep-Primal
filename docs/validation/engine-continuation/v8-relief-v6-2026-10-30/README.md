# V8 relief V6 source handoff

## Implemented

- Added `V8ReliefJoinV6`, an explicit bounded plateau/coast join before canyon incision.
- Wired `TerrainShapeV8.definitionReliefV6(...)` and persisted the join output inside the new graph.
- Added `V8GeneratorSnapshot.newReliefV6(...)` for explicit new-world authoring only.
- Existing V1-V5 graph recipes and saved profile fields remain unchanged.
- No hydrology, physical liquid quantities, renderer-owned files, or native generator registration were changed.

## Validation

Source/static guard replayed against current `main` after the guarded writes. Required anchors were present in the final committed blobs:

- V6 factory and revision bound.
- V6 join node wiring and bounded join output.
- Snapshot factory.
- Version bump to `0.0.1-alpha.29-dev.112-relief-v6.1`.

Observed commits:

- `dc284c45610acf6279b95affc17981c5dd6ace9a`
- `40781380aa13bde85b75a5f40fc3d4d504f56101`
- `f87b1820ce3df0f8057d3e34e432a860c9ad9576`
- `f8708f18748a9aefbc44295455d2d6e30594f8f4`
- `e284d8dbed80496d4f54a12ca163fbf3e10e453f`

## Limits

This is source/static evidence only. No Java25/Gradle build, full CoreChecks, native Minecraft save/reopen/edit replay, GPU validation, visual transects, performance measurement, or complete finite-water acceptance was run. The V6 recipe is not native-activated and is not a new playable or qualified JAR.
