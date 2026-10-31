# Dev139 — query-water solid-cell mutation guard (2026-10-31)

## Scope and reproduced defect

Parent before claim: `b2e2ab64670af69b37c50a8389b775a9c497b30e`; claim commit: `e153f0f3435af224d1974bd593fd5d619f9f9270`.
`WaterSimulation.setWaterLevel` silently overwrote `material=2` (solid) with `material=1` or `material=0` and could create water inside solid cells. The existing `addWater` path already rejects solids. Converting a zero-volume source to solid also retained the source flag. The old source failed the new regression immediately: `positive setter on solid: accepted invalid mutation`.

Fix: quantize/validate the new level before mutation; reject positive-volume or source-flag writes to solid cells; treat a zero-volume, non-source setter on solid as a no-op; and clear the source flag when converting a dry cell to solid. Existing non-solid setter, pressure-exchange and explicit addition/removal behavior are preserved.

**Boundary:** this is the isolated 16×16 engine-core **query simulation**, not `HydrologyRuntime`, native Minecraft water, saved constituent accounting, or the renderer. No saved-world format, preset, water texture or default worldgen revision changed.

## Isolated validation

- Exact original production Git blob `6219c6e9f7f59a999a0bc5dff30ba78ee321e061` reproduced the failure.
- Compiled patched `WaterSimulation`, exact production `WaterTransport` Git blob `0ede1c7312e40ecf063b6cc86e5a690cce190443`, repository regression and minimal `EngineMetrics` / `TerrainNoise` API stubs with `javac --release 21 -Xlint:all -Werror`.
- `java -ea -Xverify:all`: `WATER_SIMULATION_MATERIAL_PASS assertions=693494 trials=300`. Includes 300 seeded 16×16 closed-grid worlds × 12 steps, conservation across each tick, solid impermeability, explicit mutation failures, packet serialization, and import of solid/wet cell flags.
- Three negative controls independently rejected: remove solid guard, omit dry-solid source clearing, erase solid on zero-volume write. The original unmodified source also failed.
- Patched production file SHA-256 `29f99434e0d70cf6624d23f26b5a0286838f6bc03a2a239b396f4436d20b22c8`. Test source SHA-256 `3bab8ec9cbc1c810a27fa4d925b606cd5e64c5c697ff726e2d2bcca7989cc49a`.
- `CoreChecks` now invokes `WaterSimulationMaterialChecks`; the entire repository-wide suite was **not** run.

## Unmet acceptance

No complete local private checkout, Java 25, cached Gradle/NeoForge dependencies or authenticated Git CLI checkout in this runner. Therefore no full `gradlew build`, native Minecraft client/server tests, JAR, JAR SHA-256, Vulkan GPU test, or new independent whole-engine grade. Historical 5.0/10 whole-engine grade is not upgraded. No GitHub Actions used. Retain previously verified JAR until a newly built replacement passes validation.

Next: native V8 crash-safe chunk persistence, live finite-water conservation across save/reopen, terrain border acceptance, and Vulkan/LOD hardware profiling.
