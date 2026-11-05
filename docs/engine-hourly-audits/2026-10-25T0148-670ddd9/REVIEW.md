# Independent audit — 2026-10-24 18:48 PDT

Pinned committed main: 670ddd934d353359e6b6170ffa1e15bbbb00c564. Version attempted: 0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9. Previous audit pinned5c54904a1316b27d6512aa219f938867d31a49f5.

**Current build FAILED; no current verified JAR delivered.** :groundMaterialResources rejects grass_side.png. The build task still reads the historical v2 asset manifest, while committed v3 images replace seven side/mask hashes. The failure is reproducible from pinned resources and is not a corrupt download: all824restored files match Git blob identities and all current module inputs are present. Do not revert owner-approved art merely to satisfy old hashes. Builder should reconcile the validation manifest/task with approved v3 assets and rerun the complete resource/profile, package and native gates. Auditor made no product patch or test bypass.

**Grade50/100, average5.0/10, Tier2/5 — carried forward, not a fresh complete regrade.** This failed build cannot establish new release readiness. Ten equally weighted baseline categories, with freshly inspected pinned source limitations:

|Category|/10|Pinned evidence and limit|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## New committed implementation

Registered ground sample blocks/items, self drops, shovel/substrate tags and top/soil tints; coherent GradientPalette; strict inline GroundSurfaceProfile and optional density surface coating/base-column overload; gradient/resource/profile/native/client fixtures; revised sides/masks and sand asset. [GroundMaterials](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/GroundMaterials.java#L1), [profile](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/GroundSurfaceProfile.java#L19), [coating](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L18), [colors](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/main/java/com/shnewbs/deepprimal/client/GroundMaterialColors.java#L12).

V8 remains unregistered. Surface-profile eroded channel is a spatial control, not physical moisture/ecology. Standalone validation snapshots are not level.dat integration. Current sides use provisional soil tint; masks are not sampled for two-color sides. Sand is an asset only. These previews earn no new gameplay/worldgen credit; frozenV1-V7 remains unchanged.

## Fresh passed checks and failure

Full available Java25/Gradle9.2.1 build attempted with forced tasks, Windows Vulkan selection and unique run/version.31tasks executed before failure. Independent javac compiles61production core sources plus44test sources; complete CoreChecks passes. Full-build core suite also passes: gradient47290, noise183806, density graph40987, exact reuse1581340, adversarial105472, density occupancy16384, legacyV6/V7, deposit/water/conservation/cave/LOD suites. Cache checks pass. No new performance claim follows from incidental check durations.

Build failure location: [GroundResourceChecks L21–28](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/src/visualValidation/java/com/shnewbs/deepprimal/validation/GroundResourceChecks.java#L21). [build.gradle L120](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/build.gradle#L120) supplies historical docs/art/ground-textures-v2/validation.json. [v3 contract](https://github.com/Shnewbs/Deep-Primal/blob/670ddd934d353359e6b6170ffa1e15bbbb00c564/docs/art/ground-textures-v3/README.md#L1) explicitly distinguishes new assets from historical v2 hashes. Resource task stops at the first hash, so downstream image/profile checks are not fresh passes.

The producer completed before diagnostic archive inspection. The resulting diagnostic JAR passes recursive CRCs, source/independent-core/compiled/packaged class comparisons, WindowsDLL-only Vulkan payload, OpenGL fallback presence, private-class exclusion, complete2508Themes resource hashes/notices and new resource byte checks. This is not an accepted JAR: resource/build gate failed. Generated bytes are retained privately with SHA256/size and withheld from delivery. No current packaged native fresh/reopen/save/shutdown run was attempted after this essential build failure. Module boundary tasks were not reached. Diagnostic ZIP success does not override build failure.

## Isolation, benchmark evidence and limits

Independent checkout/cache/single-use daemon/output and receipt. Documented pinned restoration helper verifies tools before build; tools excluded from payload. No builder task/lease/checkout is changed, stopped, interrupted or claimed. No active timing reservation or builder Java/benchmark job was observed at start. Setup initially rejected two stale docs copies; exact pinned versions were fetched before compilation. No stagedR57/R60/R61 files overlaid. APIs already committed to main are included exactly as committed.

No fresh benchmark, Windows/AMD8060S execution, Vulkan/OpenGL visual/resource-reload/terrain-world acceptance, long travel/multiplayer/storm/drought/edit/unloaded soak or cross-machine replay. Repository records of another host's Windows inventory runs predate v3 asset revision and are not fresh acceptance here. Historical stage2 component means32.03msY-inner,321.75msX-inner,34.68msoccupancy remain above5ms; historical native warm fill14.45–14.99ms excludes lighting/IO/host costs. Coarse906occupancy disagreements, biome blending, routed V8 hydrology/topology/save integration, water/LOD/transparency and target-version acceptance remain open.

Next: resolve current asset-manifest gate preserving approved textures; run complete resource/profile/package/native gates; finish side dual-tint and terrain visual coverage; then native V8 lifecycle/topology/hydrology and latency, biome ecotones, feature ownership, bounded host scheduling and ownerGPU validation. No automatic builder pause or stop gate is created by this observation. Auditor remains enabled;8.0target and latest documented8.5minimum are both unmet.

## Last verified artifact — older source

[Windows development JAR from5c54904](https://chatgpt.com/library?file_id=libfile_c282e4a126688191926a0876c4959747&account_id=personal),125072670bytes,SHA256c2856811221c37c9dc1ba57fbf3b19f47113ecfccf9c3b8a185f0557831b99ad. This is the previous verified build, not the failed670ddd9snapshot. No stable release or CurseForge/GitHubRelease publication.

## Records

### build-result.json

```json
{
  "exit": 1,
  "pass": false,
  "source_sha": "670ddd934d353359e6b6170ffa1e15bbbb00c564"
}
```

### resource-gate-failure.json

```json
{
  "pass": false,
  "gate": ":groundMaterialResources",
  "error": "Owner asset changed: assets/deepprimal/textures/block/ground/grass_side.png",
  "cause": "build.gradle passes historical v2 validation.json; committed v3 side images/masks replace the historical hashes",
  "mismatches": [
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/grass_side.png",
      "historical_expected": "3e9af97dbc815c2e4c01485c6d0a034a957273eb8b197e1fe83bd3d8b5c346a9",
      "pinned_current": "892016e56edac23421f8c16f7fc187a7914af455a00cf7b7c5ee3ce1fa3e17d3"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/dirt_side.png",
      "historical_expected": "0d7e25a80523a324c3232eefd483cf466405f03e9856fe777806bfba4d0b5e41",
      "pinned_current": "85037d7789230af2d6014a6f7c2bb72ba2606d11f0b4063b941e8655df425324"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/podzol_side.png",
      "historical_expected": "58aa70894c2b6f65c899ce1edd9aa9c598c4d9923bba07df2e10984aaf49b618",
      "pinned_current": "cfcdd5a1c8c68a7028e3ead62b1547564533e3183a4ff9fd0fe261947e84fb57"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_surface.png",
      "historical_expected": "d8789463d6ae5fac74b785d0cacc5f1524628a519694a115be3b9ecc8cf5762f",
      "pinned_current": "d286bd10ca51a34393ed35ab23336060f9306f44153b419e6518cdce122d8291"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_soil.png",
      "historical_expected": "3a06a5ad3c506aa683ef0c6fba7f06445d4803cb726523b308c1ca2c5062bca0",
      "pinned_current": "6f3814d8327e2703b972d4c65fa4a802ff3d9a195fa725444b168284bc24a714"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_surface.png",
      "historical_expected": "ec16948ca900de30b72c3a8a9ad2517d492d81dc4cf930f6e16ea5d909cb3a1f",
      "pinned_current": "c3c2f416852aa4fca008c6312ec52554e38fbd16cc85b9a3b0b85d4b3fae65c7"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_soil.png",
      "historical_expected": "7979138694deb1c9306ea00786cc907302d1ced38f649a56d5bac51bbb63a4b3",
      "pinned_current": "bbec0e9aaefa9214b1396c894065dc0a55783e07b46d46aa3e4c800c83bafb10"
    }
  ],
  "native_gate": "not run after essential full-build failure",
  "delivery": "withheld; no current verified JAR",
  "last_verified_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "last_verified_library_file_id": "libfile_c282e4a126688191926a0876c4959747"
}
```

### source-identity.json

```json
{
  "source_sha": "670ddd934d353359e6b6170ffa1e15bbbb00c564",
  "verified_inputs": 824,
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
      "elapsed_seconds": 0.31
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.4
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.16
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
      "elapsed_seconds": 0.01
    }
  ]
}

```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 61,
  "test_sources": 44,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "538c684daa696a7ad3ae27587818327be46d69c6a33f4bdaa6aaf5aefc8469a7"
}
```

### package.json

```json
{
  "source_sha": "670ddd934d353359e6b6170ffa1e15bbbb00c564",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-670ddd9-20261025T0148/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar",
  "sha256": "97364d0bbedf5b85b89cbbe5fe8a7426f9a8d7afcdd79314727fee53d73d29a2",
  "size": 125097247,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar",
      "entries": 495
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T0148-670ddd9-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 531,
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
  "private_class_exclusions": 180,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "97364d0bbedf5b85b89cbbe5fe8a7426f9a8d7afcdd79314727fee53d73d29a2",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11
}
```

### executed-checks.json

```json
{
  "build-output.txt": {
    "sha256": "3ef46110ec5b76b621b1872997bd726a5ca0c36db8d55c27547a6c94bec0ef9e",
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
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=660.78869",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=69.695947 rawNewMs=15.807075 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true",
      "BUILD FAILED in 2m 30s",
      "31 actionable tasks: 31 executed"
    ]
  },
  "independent-core.txt": {
    "sha256": "538c684daa696a7ad3ae27587818327be46d69c6a33f4bdaa6aaf5aefc8469a7",
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
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=613.654363",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=70.32018 rawNewMs=15.338068 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true"
    ]
  }
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T0148",
  "source_sha": "670ddd934d353359e6b6170ffa1e15bbbb00c564",
  "status": "complete_build_failed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "current_jar_delivered": false
}
```

