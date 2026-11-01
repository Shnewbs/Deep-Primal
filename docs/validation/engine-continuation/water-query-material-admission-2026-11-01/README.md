# Deep Primal dev150 — query-water material admission

Date: 2026-11-01 UTC. Owner: hourly Deep Primal engineering.
Auditor consumed: `docs/engine-hourly-audits/2026-11-01T1030-dev149/REVIEW.md` (whole-engine 5.0/10 historical, full native gates pending).
Baseline main before claim: `8e6005a358379f99cfe91b23b201d5400e97e4c1`.
Scope: `WaterSimulation.setMaterial` and an isolated registered core regression. No runtime water engine, saved-world codec, Vulkan, LOD, or terrain files changed.

## Reproduced defect

A dry query cell (`units == 0`) could be assigned `material == 1` using `setMaterial`, even though 1 represents water. The same malformed state was accepted by `fromChunkData` because it calls `setWaterLevel` then `setMaterial`. Such a cell could be serialized as a water material with zero volume, violating the invariant already enforced by normal set, remove and tick paths. The unmodified production Git blob `6882c5d52d68b787f1dad954e4f1fdededb3b305` failed the new regression immediately: `dry source: accepted invalid dry material`.

## Repair

Reject `material == 1` when `units == 0` before mutating state. Preserve existing wet material and solid guards. Dry source metadata remains allowed, but a solid conversion clears the source flag. Malformed imported dry-water cells now throw instead of entering the query grid. The method changes no finite-water quantity, flow law, or packet layout.

Production commit: `013f5b92085ea62366d09152fd9e5de1c48fda38`.
Exact production Git blob: `a1bf3e859ecfed046504ef0970c5f761776b8c65`.
Production SHA-256: `5c8b86237fa1a5e838139e0afefa5e0e3745513509328e8c36faf6f06c4b7506`.
Regression Git blob: `cec848f5beb6b538c67aa6c45e0e7aec2f90139b`.
Regression SHA-256: `730a192ddea1d362dea2020c2906505fd3f116fe40e9755a9bf6f821d9b8d00c`.
Registered in `CoreChecks` and reproducible via `bash docs/validation/engine-continuation/water-query-material-admission-2026-11-01/run.sh`.

## Executed tests

Java 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. Compiled the exact committed `WaterSimulation`, exact committed `WaterTransport` (blob `0ede1c7312e40ecf063b6cc86e5a690cce190443`), and exact committed regression test. Minimal `EngineMetrics` and `TerrainNoise.lerp` API stubs only, no Minecraft/native stubs masquerading as full integration.

`WATER_SIMULATION_MATERIAL_ADMISSION_PASS assertions=1301352 randomizedImports=400 conservationSteps=1200`

Includes malformed dry source and imported water material, wet/solid rejection, no mutation on failure, valid 256-cell imports, packet parity and caller buffer-position preservation, 400 seeded imports and 1200 conserved-volume steps. Original production code failed. Three deliberately defective variants (late check after mutation, source exemption, and no guard) all failed the regression.

Local evidence archive SHA-256: `b87f0829138b604cc58eec92dfd2cfb0e2b7e3b0366cdf2ff974a8fe4cd201c6` (7057 bytes). Git source and test blobs independently matched the compiled local files.

## Unpassed gates

No complete private checkout, Java 25, Gradle executable or cached NeoForge dependencies in the isolated execution environment; direct `git ls-remote` fails DNS resolution for github.com. Full `CoreChecks`, Java25/NeoForge build, Minecraft client/server startup, real native water/save-reopen, Vulkan/OpenGL performance and 26.3/26.4 compatibility are **not tested**. No new playable JAR or JAR SHA-256 was produced; previous verified JARs were not deleted. GitHub Actions were not run. The query simulation is not the live saved-world water runtime, so this fix does not establish long-soak conservation or cliff-water visual acceptance.
