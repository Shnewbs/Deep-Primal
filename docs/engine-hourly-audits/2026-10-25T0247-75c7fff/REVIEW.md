# Independent committed-snapshot audit — 2026-10-24 19:47 PDT

Pinned main source: `75c7ffff6878ef5ee6498400ed9ff2050f3ee848`. Previous audit source: `670ddd934d353359e6b6170ffa1e15bbbb00c564`.
Attempted unique version: `0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff`; Windows Vulkan bundle, OpenGL fallback and merged Themes.

**50/100; average 5.0/10; Tier 2/5. Carried-forward accepted baseline, not a fresh full regrade.** Current source and scoped executed regressions are freshly inspected; new gameplay/artistic/performance acceptance is not demonstrated. All ten categories equally weighted. Unused APIs and unregistered previews earn no gameplay feature credit.

|Category|/10|Pinned evidence and continuing limitation|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/docs/engine-review-2026-10-24/REVIEW.md): Current build fails; independently compiled core checks and retained diagnostics do not substitute for full delivery acceptance. Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Changes since prior pinned audit

Actual committed production changes: stacked waterfall side heights are clamped to the next wet cell; shader precompile diagnostics are expanded; ground sides gain generated opaque material sprites using complementary masks, coefficient-preserving mipmaps, tint/lighting marker mixins and shader patches; smooth/animated water rendering is revised; a bounded strict UTF-8/JSON TerrainPreviewSnapshot API is added. Fixtures add waterfall side coverage, ground math and native vertex/mipmap controls, and stronger material screenshot assertions. No implementation authored or promoted by the auditor. No staged R57/R60/R61 overlay.

Pinned source: [water wall](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java#L207), [waterfall fixture](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/coreChecks/java/com/shnewbs/deepprimal/core/WaterfallSideChecks.java#L8), [masked sprite](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/client/GroundSideSpriteSource.java#L23), [shader patch](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/client/GroundMaterialShaders.java#L8), [snapshot API](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/main/java/com/shnewbs/deepprimal/engine/TerrainPreviewSnapshot.java#L26). Inspect committed-changes.diff for the complete bounded change list. New fixtures being present does not establish execution; executed-checks.json is authoritative.

V8 remains unregistered. TerrainPreviewSnapshot is a preview identity/API, not gameplay level.dat integration. New side rendering/mipmap/client changes have no fresh native/client/owner-GPU acceptance in this run. Prior inventory material acceptance predates these changes and is not transferred to them.

## Isolation and source verification

Independent checkout, copied independent cache, single-use Gradle daemon, outputs and audit lease. 834 present inputs match exact immutable Git blob identities; missing build inputs and mismatches are empty. Pinned restoration-helper tool verification passes. No active timing reservation or Java/benchmark process observed at start. No builder checkout, lease, task, implementation or state mutated. Coordination documentation contains older pause records alongside an explicit local owner-resumed entry; the auditor does not change either task or infer a new pause.

The host began at zero free disk space. Removed only obsolete auditor Gradle caches and generated module/root build directories in the 23:52 and 00:55 audit directories. Prior reports, receipts and exported JARs retained; builder files untouched. No code/tooling added to mod payload.

## Failure and delivery policy

The current build is not accepted. Full raw output and exit record retained below. The pinned build still supplies `docs/art/ground-textures-v2/validation.json` at build.gradle:124 while seven current side/mask bytes differ from those historical expected hashes. [Ground resource check](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/src/visualValidation/java/com/shnewbs/deepprimal/validation/GroundResourceChecks.java#L21) intentionally rejects the first mismatch. resource-manifest-preflight.json records all seven exact hash differences. Auditor does not rewrite expectations or bypass checks to make an installer pass. Corrections belong to the builder.

Any produced archive is diagnostic only. CRC/class/resource checks do not override the failed essential build gate. No current JAR uploaded or presented as successful. Last verified owner JAR remains source `5c54904a1316b27d6512aa219f938867d31a49f5`, Library `libfile_c282e4a126688191926a0876c4959747`.

## Performance evidence and next priorities

No fresh performance benchmark. Historical full accepted native warm fill14.45–14.99ms excludes lighting/IO/host costs. Stage2 exact sampler~32.03ms Y-inner/~321.75ms X-inner and occupancy materialization~34.68ms are component measurements, not whole-engine or owner hardware performance. The5ms target and906coarse occupancy disagreements remain open. [Stage2 evidence](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/docs/engine-stage2-2026-10-24/README.md#L1); [original rubric](https://github.com/Shnewbs/Deep-Primal/blob/75c7ffff6878ef5ee6498400ed9ff2050f3ee848/docs/engine-review-2026-10-24/REVIEW.md#L1).

Next: reconcile the approved current asset manifest and rerun full delivery gates; validate the new ground shader/lighting/mipmap/reload path on both APIs in terrain scenes; V8 saved-profile/native parity/topology/routed hydrology; density latency and scheduler contention; biome ecotones/feature ownership; water/LOD temporal coverage and Windows/8060S acceptance.

Not run after essential build failure: packaged Minecraft native save/reopen/shutdown, Windows client/AMD GPU, fresh OpenGL/Vulkan shader execution/visual/resource-reload/terrain-world checks, new performance benchmarks, cross-machine replay, long multiplayer/travel/edit/storm/drought/unloaded soak, gameplay V8 profile migration,26.3port. Linux mathematical/core checks are not Windows acceptance. No stable release, CurseForge or GitHub Release publication. Auditor stays enabled; neither the8.0automation threshold nor latest documented8.5minimum is demonstrated.

## Fresh machine-readable receipts

### source-identity.json

```json
{
  "source_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848",
  "verified_inputs": 834,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### host.json

```json
{
  "platform": "Linux-6.18.55-x86_64-with-glibc2.39",
  "cpu_count": 9,
  "machine": "x86_64",
  "performance_benchmark": false
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T0247",
  "source_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848",
  "status": "completed-build-gate-failed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true
}
```

### toolchain-verification.json

```json
{
  "scope": "private-validation-tools",
  "verify_only": true,
  "all_verified": true,
  "assets": [
    {
      "asset": "jdk25.tar.gz",
      "bytes": 141329719,
      "sha256": "dbb698396d478e7fa2b1e50f4103324b2a99b90569ee27c33f2261f9215cf41e",
      "verified": true,
      "elapsed_seconds": 0.11
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.15
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.09
    },
    {
      "asset": "checked-agent/neoform-runtime-2.0.31-all.jar",
      "bytes": 4483539,
      "sha256": "6f5b0aae7e423efeea21cb848c5096e76fc4eec40256cbefdcfaa1583b197c07",
      "verified": true,
      "elapsed_seconds": 0.01
    },
    {
      "asset": "checked-agent/asm-9.9.1.jar",
      "bytes": 126252,
      "sha256": "6f3828a215c920059a5efa2fb55c233d6c54ec5cadca99ce1b1bdd10077c7ddd",
      "verified": true,
      "elapsed_seconds": 0.0
    }
  ]
}

```

### build-result.json

```json
{
  "exit": 1,
  "pass": false,
  "source_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848"
}
```

### module-boundary-result.json

```json
{
  "exit": 0,
  "pass": true,
  "tasks": [
    "verifyCoreBoundary",
    "verifyModuleBoundaries"
  ]
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 61,
  "test_sources": 45,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "2bee4e6ed650a4963d68f51b980edec006e6266181e4b8b27a80db2f85225fe0"
}
```

### resource-manifest-preflight.json

```json
{
  "pinned_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848",
  "mismatches": [
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/grass_side.png",
      "expected": "3e9af97dbc815c2e4c01485c6d0a034a957273eb8b197e1fe83bd3d8b5c346a9",
      "current": "892016e56edac23421f8c16f7fc187a7914af455a00cf7b7c5ee3ce1fa3e17d3"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/dirt_side.png",
      "expected": "0d7e25a80523a324c3232eefd483cf466405f03e9856fe777806bfba4d0b5e41",
      "current": "85037d7789230af2d6014a6f7c2bb72ba2606d11f0b4063b941e8655df425324"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/podzol_side.png",
      "expected": "58aa70894c2b6f65c899ce1edd9aa9c598c4d9923bba07df2e10984aaf49b618",
      "current": "cfcdd5a1c8c68a7028e3ead62b1547564533e3183a4ff9fd0fe261947e84fb57"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_surface.png",
      "expected": "d8789463d6ae5fac74b785d0cacc5f1524628a519694a115be3b9ecc8cf5762f",
      "current": "d286bd10ca51a34393ed35ab23336060f9306f44153b419e6518cdce122d8291"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_soil.png",
      "expected": "3a06a5ad3c506aa683ef0c6fba7f06445d4803cb726523b308c1ca2c5062bca0",
      "current": "6f3814d8327e2703b972d4c65fa4a802ff3d9a195fa725444b168284bc24a714"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_surface.png",
      "expected": "ec16948ca900de30b72c3a8a9ad2517d492d81dc4cf930f6e16ea5d909cb3a1f",
      "current": "c3c2f416852aa4fca008c6312ec52554e38fbd16cc85b9a3b0b85d4b3fae65c7"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_soil.png",
      "expected": "7979138694deb1c9306ea00786cc907302d1ced38f649a56d5bac51bbb63a4b3",
      "current": "bbec0e9aaefa9214b1396c894065dc0a55783e07b46d46aa3e4c800c83bafb10"
    }
  ]
}
```

### package.json

```json
{
  "source_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-75c7fff-20261025T0247/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar",
  "sha256": "eaa1893b5c970fd9421de805ffb3a0695045cdf9ac828866154c366eb0883ac2",
  "size": 125120002,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar",
      "entries": 506
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 540,
  "independent_core_matches": 107,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 183,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "eaa1893b5c970fd9421de805ffb3a0695045cdf9ac828866154c366eb0883ac2",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11
}
```

### executed-checks.json

```json
{
  "build-output.txt": {
    "sha256": "1b51da65d6455e132344a8567628882843f3e7cb2d873f5ee82dc15874bd0e1e",
    "markers": [
      "CACHE_CHECKS_PASS corruption truncation bounds LRU250 exactDescriptorEquality dynamicOffsets commandReset",
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
      "TERRAIN_GRADIENT_PASS checks=47290 linearBands=4 clusteredBands=5 isolated=0 transitions=90 deterministic=true",
      "NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64 fingerprint=6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1",
      "DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784 overhangColumns=5 minHeight=20.93897487422737 maxHeight=325.50950620538816 fingerprint=c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26",
      "DENSITY_REUSE_PASS checks=1581340 recipeVolumes=8 exactSamples=1572864 sharedWarpCoordinates=true signedZero=true independentJobs=16",
      "DENSITY_ADVERSARIAL_PASS checks=105472 acceptedGraphs=180 rejectedByBudget=0 permutations=true orders=3 full128Nodes=true highIndexFactors=true",
      "DENSITY_CHUNK_PASS exactV8Samples=16384 maxHeight=2048 boundedOccupancy=true topExclusive=true zeroIsAir=true",
      "WATER_BED_PASS cases=6144 supportedVertices=24576 sharedPageVertices=512 volumeUnchanged=true",
      "DEPOSIT_PLAN_CORE_PASS cases=1559040 hits=13631 shapes=4 verticalReferences=3 signedChunkSeams=true widerOracle=true",
      "WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true",
      "STREAM_SOURCE_CAPACITY_PASS cases=4168 fractionalLevels=true cappedAtOneBlock=true failsClosed=true noQuantityMutation=true",
      "BOAT_BUOYANCY_POLICY_PASS cases=8 vanillaUnchanged=true engineWaterSubmersionRecovers=true",
      "TERRAIN_V4_PASS banks=237 oreSamples=1343 rockSuites=8 deterministic=true",
      "DEPOSIT_FIELD_CORE_PASS cases=90960 clippedV4=1072 signedSeams=25000 candidates=9 oracleCandidates=49 replay=true first={seed=0 x=-4064 z=-143 y=17 ore=COPPER}",
      "DEPOSIT_ALLOCATION_CORE_PASS cases=4194304 oreHits=1542 emptyColumns=54463 activeColumns=11073 integerExtremes=true retainedColumns=true nullHostRejected=true",
      "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40 dryFade=true immutableSnapshots=true",
      "WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges=400000 maxCircularError=5.960464477539063E-8 negativeDistantSectionEdges=true phaseBounded=true",
      "WATER_FLOW_TEXTURE_CORE_PASS directions=16 vertices=320 legacyStill=true quantityAndGeometryUnchanged=true snapshotImmutable=true dryMaterialRetained=true atlasBounds=true",
      "WATER_FLOW_BLEND_CORE_PASS pairs=272 earlyFirstRetarget=true repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchBlended=true",
      "WATER_FLOW_FIELD_CORE_PASS codecCases=8435621 heightClockCases=98307 haloEdges=34000 uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36",
      "WATER_CORNER_PACKET_CORE_PASS cases=210000 payloadBits=97 directionStates=17 epochBits=16 floatInputsNormalizedFinite=true heightOpacityGeometryLightPreserved=true retirementFramesBound=193 maxClockMillisPerFrame=250",
      "WATER_CORNER_BULK_CORE_PASS cases=210000 masks=16 scalarBytesIdentical=true readOnlyPredicate=true arbitraryVertexOpacityLightPreserved=true nonzeroOffsets=true",
      "WATER_BOUNDARY_INDEX_CORE_PASS cases=688129 oldVisits=7680 indexedVisits=1088 exactCoordinateOracle=true stableOrder=true immutable=true maxEntriesPerInput=3",
      "WATER_CORNER_RETRY_CORE_PASS cases=470000 masks=16 currentBytesPreserved=true expiredPairsSteady=true longElapsedWraps=true",
      "WATER_RETIREMENT_STATE_CORE_PASS cases=60000 mixedAges=true readOnly=true",
      "WATER_SURFACE_SHAPE_PASS planarSamples=441 extremaCases=20000 planarPatchVertices=4 verticalSeamVertices=64 verticalPageSeamVertices=1024",
      "WATERFALL_SIDE_PASS vertices=131072 amounts=16 previousAmounts=16 smoothModes=2 verticalPages=true",
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=618.604077",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=70.516955 rawNewMs=15.581715 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true",
      "BUILD FAILED in 2m 29s",
      "31 actionable tasks: 31 executed"
    ]
  },
  "independent-core.txt": {
    "sha256": "2bee4e6ed650a4963d68f51b980edec006e6266181e4b8b27a80db2f85225fe0",
    "markers": [
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
      "TERRAIN_GRADIENT_PASS checks=47290 linearBands=4 clusteredBands=5 isolated=0 transitions=90 deterministic=true",
      "NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64 fingerprint=6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1",
      "DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784 overhangColumns=5 minHeight=20.93897487422737 maxHeight=325.50950620538816 fingerprint=c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26",
      "DENSITY_REUSE_PASS checks=1581340 recipeVolumes=8 exactSamples=1572864 sharedWarpCoordinates=true signedZero=true independentJobs=16",
      "DENSITY_ADVERSARIAL_PASS checks=105472 acceptedGraphs=180 rejectedByBudget=0 permutations=true orders=3 full128Nodes=true highIndexFactors=true",
      "DENSITY_CHUNK_PASS exactV8Samples=16384 maxHeight=2048 boundedOccupancy=true topExclusive=true zeroIsAir=true",
      "WATER_BED_PASS cases=6144 supportedVertices=24576 sharedPageVertices=512 volumeUnchanged=true",
      "DEPOSIT_PLAN_CORE_PASS cases=1559040 hits=13631 shapes=4 verticalReferences=3 signedChunkSeams=true widerOracle=true",
      "WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true",
      "STREAM_SOURCE_CAPACITY_PASS cases=4168 fractionalLevels=true cappedAtOneBlock=true failsClosed=true noQuantityMutation=true",
      "BOAT_BUOYANCY_POLICY_PASS cases=8 vanillaUnchanged=true engineWaterSubmersionRecovers=true",
      "TERRAIN_V4_PASS banks=237 oreSamples=1343 rockSuites=8 deterministic=true",
      "DEPOSIT_FIELD_CORE_PASS cases=90960 clippedV4=1072 signedSeams=25000 candidates=9 oracleCandidates=49 replay=true first={seed=0 x=-4064 z=-143 y=17 ore=COPPER}",
      "DEPOSIT_ALLOCATION_CORE_PASS cases=4194304 oreHits=1542 emptyColumns=54463 activeColumns=11073 integerExtremes=true retainedColumns=true nullHostRejected=true",
      "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40 dryFade=true immutableSnapshots=true",
      "WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges=400000 maxCircularError=5.960464477539063E-8 negativeDistantSectionEdges=true phaseBounded=true",
      "WATER_FLOW_TEXTURE_CORE_PASS directions=16 vertices=320 legacyStill=true quantityAndGeometryUnchanged=true snapshotImmutable=true dryMaterialRetained=true atlasBounds=true",
      "WATER_FLOW_BLEND_CORE_PASS pairs=272 earlyFirstRetarget=true repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchBlended=true",
      "WATER_FLOW_FIELD_CORE_PASS codecCases=8435621 heightClockCases=98307 haloEdges=34000 uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36",
      "WATER_CORNER_PACKET_CORE_PASS cases=210000 payloadBits=97 directionStates=17 epochBits=16 floatInputsNormalizedFinite=true heightOpacityGeometryLightPreserved=true retirementFramesBound=193 maxClockMillisPerFrame=250",
      "WATER_CORNER_BULK_CORE_PASS cases=210000 masks=16 scalarBytesIdentical=true readOnlyPredicate=true arbitraryVertexOpacityLightPreserved=true nonzeroOffsets=true",
      "WATER_BOUNDARY_INDEX_CORE_PASS cases=688129 oldVisits=7680 indexedVisits=1088 exactCoordinateOracle=true stableOrder=true immutable=true maxEntriesPerInput=3",
      "WATER_CORNER_RETRY_CORE_PASS cases=470000 masks=16 currentBytesPreserved=true expiredPairsSteady=true longElapsedWraps=true",
      "WATER_RETIREMENT_STATE_CORE_PASS cases=60000 mixedAges=true readOnly=true",
      "WATER_SURFACE_SHAPE_PASS planarSamples=441 extremaCases=20000 planarPatchVertices=4 verticalSeamVertices=64 verticalPageSeamVertices=1024",
      "WATERFALL_SIDE_PASS vertices=131072 amounts=16 previousAmounts=16 smoothModes=2 verticalPages=true",
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=659.74191",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=70.44692 rawNewMs=15.352509 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true"
    ]
  },
  "module-boundary-output.txt": {
    "sha256": "a034a5235726ba5b822d777638d2d5d72aa28f2a4c83f52879d44bf687ecbf11",
    "markers": [
      "BUILD SUCCESSFUL in 6s",
      "2 actionable tasks: 2 executed"
    ]
  }
}
```

### delivery.json

```json
{
  "accepted": false,
  "diagnostic_archive_only": true,
  "source_sha": "75c7ffff6878ef5ee6498400ed9ff2050f3ee848",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0247-75c7fff-vulkan-windows.jar",
  "sha256": "eaa1893b5c970fd9421de805ffb3a0695045cdf9ac828866154c366eb0883ac2",
  "size": 125120002,
  "final_export_matches_producer": true,
  "reason": "Full build fails groundMaterialResources historical texture hash gate",
  "native_smoke": "not run",
  "windows_execution": false,
  "library_uploaded": false,
  "last_verified_source": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "last_verified_library_file_id": "libfile_c282e4a126688191926a0876c4959747"
}
```

