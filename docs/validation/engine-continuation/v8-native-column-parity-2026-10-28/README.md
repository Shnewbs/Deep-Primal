# Dev47 — exact opt-in V8 base-height and planned column parity

Version: `0.0.1-alpha.29-dev.47-v8columns.1`. Source-only engine implementation, not a qualified Minecraft JAR or release.

## Independent audit and ownership

Consumed `docs/engine-hourly-audits/2026-10-28T0530-4cf0f1b/REVIEW.md`, pinned at `4cf0f1b930b182903af3086071859d727379a74d`. The auditor carried the last full external grade at **50/100 (5.0/10)**; no fresh complete regrade was awarded. The newly demonstrated immutable persisted-LOD scan consumer mismatch is specifically renderer-owned and is not modified by this engine-only pass. Current main and the expired builder/water lease were read, a new bounded engine-main writer lease acquired with an expected-parent update, and the separate renderer/auditor automations checked. Other Deep Primal chat context was stale for current direct writer claims and is not asserted as fresh coordination.

## Production change

`src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java` adds:

- `baseHeight(DensityGraph,worldX,worldZ,LevelHeightAccessor)`: exact top-down base-height lookup retaining cave roofs/floating blocks and the implicit two-cell bedrock floor. It is read-only, does not touch Minecraft chunks, and is appropriate for the current prototype's WORLD_SURFACE_WG/OCEAN_FLOOR_WG solid-state equivalence.
- `baseHeight(DensityChunk.Planned,localX,localZ)`: constant-time lookup from the *already computed exact planned chunk*; correct even if the graph is entirely air because the native materializer adds bedrock at the bottom.
- `baseColumn(DensityChunk.Planned,localX,localZ)`: deterministic read-only `NoiseColumn` from existing exact occupancy, with no second graph scan.
- Existing graph `baseColumn` now preflights height/domain bounds before allocation. These changes do **not** register V8, change any V1–V7 path, touch approved art, water quantities, saved profiles or player edits, or modify renderer code.

The exact committed production source blob was compared with the actually tested local Java source Git blob `6a6fafe5330be47c992d200b0cb8dd5a09ca0dca`; the dev46 input adapter was `20443809ed72fa10f1c5f6ea885dd551f433d4ab`.

## Actual tests (scoped)

On the available host **Java 21.0.12.1**, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` pass using production adapter and Git-matched density graph/section sources, compiled with **test-only Minecraft API doubles**.

- New `DENSITY_BASEHEIGHT_PASS`: **874,642 assertions**, **34,304 columns**, **771,584 voxel comparisons**, **134 scenarios**. Tests direct/planned base columns and heights against exact material states, including signed/partial heights, air/solid/roof/floating/noise/V8 graph, 0/64/256 query budgets and 12 invalid-input cases.
- Rerun preexisting `DENSITY_NATIVE_BRIDGE_PASS` on the patched source: **11,047,019 assertions**, **1,376,512 native-layout positions**, **34 scenarios**.
- Three independently compiled faulty height variants (missing implicit bedrock, wrong planned top, inverted top-down scan) are **each rejected** by the new unchanged fixture.
- Owner-accessible exact source, runnable tests, API doubles, diff, outputs and mutation evidence ZIP: `DeepPrimal_Dev47_V8_BaseHeight_SourceEvidence.zip`, **34,814 bytes**, SHA-256 `270b87ce09e43e9e5798b9c18d05c4af98b65a75880c1e3c945df44619e04d00`, 28 valid ZIP entries.

**Not run this cycle:** Java 25 compiler/runtime (the prior owner JDK25 path and tarball were not mounted on this run), full Gradle/NeoForge build, native chunk heightmap/lighting, Java25 Minecraft save/reopen, selected-JAR producer, nested archive CRC/source identities, Vulkan/OpenGL/AMD8060S, water ledger/multiplayer/soak and performance p95/p99. Historical dev46 Java25 passes do not cover this changed source. Owner handles full builds under the October 27 override. No JAR or publication claimed.

## Next source and native acceptance

For V8: owner-run Java25/NeoForge source compilation and actual native `getBaseHeight`/base-column/section block/heightmap/lighting/save/reopen and V1–V7 replay with matched voxels. Register a saved V8 codec/preset only after those native and topology gates pass. Continue the finite water loaded/unloaded owner ledger, contact chemistry, seasonal/lake/ocean exchange, connected cliff temporal GPU and long-soak gates in their nonconflicting scopes. Keep engine-builder enabled until a fresh external ten-category grade is **strictly greater than 8.5/10** **and** the water acceptance criteria pass.
