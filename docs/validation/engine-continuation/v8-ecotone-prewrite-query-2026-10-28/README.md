# V8 independent prewrite ecotone terrain query — 2026-10-28

**Source implementation:** `338bf489329cb8abb0ae800bb4cd7f8fccfa268f` (`0.0.1-alpha.29-dev.59-ecotone-query.1`). Target is current **main**; base main commit immediately prior to implementation: `829348829ab4a5d47327e227579871f7378672ab`.

**Consumed independent audit:** [2026-10-28 16:28Z report](../../../../engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md), pinned source `1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0`. This report carried the previous **50/100 (5.0/10)** without an independent regrade and identified an unnecessary material lookup in cached-snow columns. No independent grade is awarded by this builder.

## Actual implementation

- Added `DensityChunkMaterializer.planSavedEcotone(prepared, originWorldX, originWorldZ, minY, height, queriesPerSlab, climate)`. It creates an immutable V8 revision-3 chunk/density/cover plan **without a native chunk** and permits `baseColumn(plan,x,z)` before the chunk exists. It refuses unaligned chunk origins, legacy V8 snapshots, saved-height mismatches, and missing climate.
- `fillSavedEcotone` now uses that exact planning method, providing an identical plan for pre-generation base-column projections and subsequent materialization. No second climate/density algorithm was introduced; the V8 call is still opt-in and not registered as the native saved-world generator.
- Fixed the audited snow-cache bug: subsequent `snowPatch` columns no longer enter the non-snow grass/podzol/bare material-registry branch just because the snow state was cached. Native writes still resolve all needed materials before mutation and use dev53's bounded prewrite snapshot/rollback.

## Reproducible focused checks

The downloadable `DeepPrimal_Dev59_V8_Ecotone_Prewrite_SourceEvidence.zip` has the exact tested production source, Java/Mojang/Minecraft API doubles, 11 Java21 test sources, positive logs and a separate intentionally rejected dev58 control. Its outer SHA256 is `fd9940831f1e1919b5763b3b820b04e4379cc4c0fc729af827086b223362b1c4` (83,419 bytes), and ZIP CRC validation passed.

- Java/Javac: **21.0.12.1**, `javac --release 21`, `java -ea`. **11/11 focused suites passed**, **14,177,063** assertion/check operations. All suites used source compiled against **isolated API doubles**, NOT real Minecraft. Instrumented test doubles are explicitly marked and included in the ZIP.
- New independent query suite: `V8_ECOTONE_PREWRITE_QUERY_PASS assertions=26128 voxels=24576` with exact pre-generation planned base-column/native-stub block equality, signed/negative coordinates, invalid origin/height rejection before callback, river-domain preservation and parallel/reversed replay.
- Audited snow regression: `V8_SNOW_PREFLIGHT_PASS checks=516 snowyGrassColumns=65 grassLookups=0`. The same injected test against unchanged dev58 source **failed** with `IllegalStateException: unneeded grass lookup during all-snow preflight`, at dev58 materializer line 215. The new implementation passes with the optional grass registry unavailable, and no unintended material lookups.
- Existing V8 graph, section bridge, base-height, planned ground, saved profile, strict codec, blend/surface policy, ecotone native source-adapter and surface field suites all passed. A first harness invocation of `EcotoneSurfaceFieldChecks` used the wrong package and was corrected; its actual correctly invoked suite passed 297,752 checks.

**Source identity:** the tested `DensityChunkMaterializer.java` is Git blob `aabcb455761571f8615601ddff5ae4042d2e3f5a`, verified identical to the newly published GitHub blob. The unchanged dev58 blob was `2c63eb58dcf7bc3b7667cd8b565e4896cd3d10a7`.

## Remaining acceptance gates / owner-build policy

**Not run here:** Java25, full Gradle9.2.1/NeoForge build, real Mojang codecs, native Minecraft generation, real saved-profile reopen and editing, production absolute-coordinate climate/hydrology provider, registered generator integration, native pre-generated base-column/heightmap parity, named-hardware latency, Vulkan/OpenGL visual transects, owner Windows/AMD8060S GPU, legacy V1–V7 native replay, loaded/unloaded finite-water quantities/chemistry, restart, multiplayer and long soak. No JAR, GitHub prerelease, stable promotion or GPU acceptance is claimed. The owner's last confirmed playable JAR is dev53.

**Safety:** V1–V7 generation and existing saved V8 snapshot versions remain frozen. No profile codec/biome palette/terrain geometry, player edits, approved art, finite-water balances, renderer or water-owned files were modified. Cosmetic snow/soil never changes physical water quantities.

**Next priorities:** Bind the V8 climate/domain source to a versioned, saved authoritative world producer; native pre-generation parity and V8 revisioned plains-to-mountain relief blending **before hydrology** with cliff/canyon/river safeguards. Coordinate with the separate water owner for single-owner boundary transfers, persistence and constituent conservation, and with the renderer for Vulkan/OpenGL continuity. The external grade and all water gates must pass separately before accepting completion.
