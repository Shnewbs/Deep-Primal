# Expanded replica — docs/engine-hourly-audits/2026-10-24T2352-eba73bd/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-24T2352-eba73bd/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-24T2352-eba73bd/REVIEW.md |
| Preserved source SHA-256 | `4e34d8261e701f7941bc8dee0c657770e03bd21cb2bed271a65ab92b69639ec5` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent build audit — 2026-10-24 16:52 PDT”. Its original opening states: “Pinned source eba73bdb7f30610469f822dec3dacfe8e0fad0d4; version 0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd. Compared with prior audit source 8524b32749b9df2e922f3e2069ca62ea689d1c62.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Deepen exact-versus-approximate density evidence and keep a pure sampler result separate from native terrain materialization.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version arithmetic order, lattice spacing, graph limits, interval certificates, and exact fallback behavior; preserve a scalar reference oracle.
- Document whether a reported speedup excludes materialization, host biome/light/status work, allocation, or cold regional construction.
- Carry the documented approximation disagreements and stage-two integration block as historical evidence. A future optimization must compare sign/occupancy decisions independently and meet native cost and compatibility gates before promotion.

## Research expansion — acceptance checks

- Exercise zero crossings, negative/far coordinates, partial vertical sections, overflow endpoints, uncertain intervals, and all sampling orders.
- Compare actual generated blocks, base-column queries, heightmaps, lighting invalidation, and save/reopen at the real adapter boundary.

## Research expansion — source anchors

- Independent build audit — 2026-10-24 16:52 PDT
- build-result
- independent-core-result

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [architecture authoring](../../../../../chapters/architecture-authoring.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent build audit — 2026-10-24 16:52 PDT

Pinned source `eba73bdb7f30610469f822dec3dacfe8e0fad0d4`; version `0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd`. Compared with prior audit source `8524b32749b9df2e922f3e2069ca62ea689d1c62`.

**50/100, average 5.0/10, Tier 2/5: carried-forward baseline, not a fresh full regrade.** New scoped correctness evidence does not establish the missing graphics, performance, cross-platform and broad gameplay gates. Ten equally weighted categories:

|Category|/10|Pinned evidence and limits|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Actual committed changes

This snapshot has production changes: density sampler reuse and an exact bounded occupancy container/native materializer, fractional stream-source capacity and cached outlet rechecks, custom-water boat status classification, temporal shader easing, three vegetation maps and forty biome water/fog palettes. The V8 materializer is an isolated adapter, not a registered generator. No new V8 gameplay terrain credit. Source maps reproduce the committed Git blob bytes, and all43 palette assets survive packaging exactly.

The audit includes exactly committed main files; it authors no implementation and overlays no staged experiment. In particular, the now-committed sampler/fixtures are included as they appear in this snapshot, while unpromoted R57 and other docs/staged sources are not build inputs. ENGINE-WORK-STATE still describes the earlier unpromoted R62 state; the newer main source and stage2 documentation contain sampler/materializer changes. This audit records that discrepancy without rewriting owner/builder state.

Stream capacity checks cover rounding/bounds and no mutation in the helper; boat checks cover the boolean classification policy. Those checks alone do not prove a moving boat recovers in a live river or that long running loaded/unloaded hydrology maintains capacity/conservation. Native save/reopen checks below are scoped; no new broad hydrology/boat acceptance is inferred. Shader easing and the palettes lack live Vulkan/OpenGL scene acceptance on owner hardware.

## Execution and preserved boundaries

Independent source/output/cache/daemon and audit receipt; no builder checkout, lease, automation or work-state edits. Owner pause remains in force, auditor remains enabled; no active timing reservation was present. Read-only pinned tools were verified by the documented restoration helper. Reproducible completed-auditor caches/tool copies were pruned to recover disk; builder directories, retained reports and delivered JARs were preserved.741 source/resource inputs match pinned Git blobs. Separate Maritime/standalone projects are outside this build.

Full forced Java25/Gradle build and independent javac core checks run anew. Recursive archive CRCs, compiled/packaged and independent core class identity, fixture exclusion, Windows DLL payload, OpenGL fallback, complete merged Themes and notices checked. Packaged Linux server fresh/reopen checks execute codec, exact V8 temporary-chunk materialization/base-column/heightmap/bedrock parity and existing V7 replay; save/shutdown and loaded product class identities checked. Linux is not Windows client acceptance. Any incidental durations from concurrent correctness jobs are not accepted performance measurements.

Frozen V1-V7 terrain generation code/geography, saved profile format and player edits were not modified by this audit. Committed hydrology source-delivery behavior changes are distinguished from frozen terrain generation. This is a private development snapshot, with no stable-release, CurseForge or GitHub Release claim.

## Latest completed evidence and open work

- [Pinned docs/engine-stage2-2026-10-24/RESULTS.md](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/engine-stage2-2026-10-24/RESULTS.md)
- [Pinned docs/colormaps/README.md](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/colormaps/README.md)
- [Pinned docs/WATER-BUDGET-REQUIREMENTS.md](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/WATER-BUDGET-REQUIREMENTS.md)
- [Pinned docs/validation/engine-continuation/density-metadata-2026-10-24/README.md](https://github.com/Shnewbs/Deep-Primal/blob/eba73bdb7f30610469f822dec3dacfe8e0fad0d4/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md)

Historical exact sampling remains above5ms and traversal-order sensitive;906 coarse occupancy disagreements remain unresolved. No fresh controlled performance benchmark was run. Native V8 now has an opt-in one-chunk adapter check, but still lacks registered gameplay, saved graph profiles, river/aquifer/topology coupling and broad seed/seam/order coverage. Weighted ecotones, structure ownership, bounded scheduling, water/LOD coverage/recovery and Windows GPU acceptance remain next priorities.

## Fresh records

### source-identity

```json
{
  "source_sha": "eba73bdb7f30610469f822dec3dacfe8e0fad0d4",
  "verified_inputs": 741,
  "mismatches": [],
  "production_changes_since_previous_audit": true
}
```

### toolchain-verification

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
      "elapsed_seconds": 0.28
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.33
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.52
    },
    {
      "asset": "checked-agent/neoform-runtime-2.0.31-all.jar",
      "bytes": 4483539,
      "sha256": "6f5b0aae7e423efeea21cb848c5096e76fc4eec40256cbefdcfaa1583b197c07",
      "verified": true,
      "elapsed_seconds": 0.08
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

### host

```json
{
  "platform": "Linux-6.18.55-x86_64-with-glibc2.39",
  "cpu_count": 9,
  "machine": "x86_64",
  "performance_benchmark": false
}
```

### build-result

```json
{
  "exit": 0,
  "pass": true,
  "source_sha": "eba73bdb7f30610469f822dec3dacfe8e0fad0d4"
}
```

### independent-core-result

```json
{
  "exit": 0,
  "production_sources": 60,
  "test_sources": 43,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "4f92bb26bc7f2abec53900172df831d080455176ac9f4d5a1bff7e45cd655b2e"
}
```

### package

```json
{
  "source_sha": "eba73bdb7f30610469f822dec3dacfe8e0fad0d4",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-eba73bd-20261024T2352/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar",
  "sha256": "4df561680d03ed7d64a8510214406353da311445f67196e3bda0eb7e09bb5de0",
  "size": 125048668,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar",
      "entries": 455
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd.jar",
      "entries": 114
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 524,
  "independent_core_matches": 106,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package

```json
{
  "pass": true,
  "private_class_exclusions": 171,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "4df561680d03ed7d64a8510214406353da311445f67196e3bda0eb7e09bb5de0",
  "colormap_and_water_palette_hash_matches": 43
}
```

### native-result

```json
{
  "pass": true,
  "platform": "Linux dedicated server; Windows client untested",
  "sessions": [
    {
      "run": 0,
      "exit": 0,
      "pass": true
    },
    {
      "run": 1,
      "exit": 0,
      "pass": true
    }
  ],
  "replay_hashes_equal": true,
  "loaded_classes_match": true,
  "scoped_markers": [
    "DENSITY_CHUNK_NATIVE_PASS blocks=196352 columns=256 baseColumnParity=true occupancyExact=true bedrockBarrier=true heightmaps=true",
    "NOISE_PROFILE_CODEC_PASS cases=21 longSaltExact=true invalidRejected=true",
    "DENSITY_GRAPH_CODEC_PASS cases=34 roundtripSamples=2048 invalidRejected=23",
    "TERRAIN_AUDIT_NATIVE_PASS blocks=3062331 chunks=44 logs=139 water=51181 biomes=704 reopen=false",
    "DENSITY_CHUNK_NATIVE_PASS blocks=196352 columns=256 baseColumnParity=true occupancyExact=true bedrockBarrier=true heightmaps=true",
    "NOISE_PROFILE_CODEC_PASS cases=21 longSaltExact=true invalidRejected=true",
    "DENSITY_GRAPH_CODEC_PASS cases=34 roundtripSamples=2048 invalidRejected=23",
    "TERRAIN_AUDIT_NATIVE_PASS blocks=3062331 chunks=44 logs=139 water=51181 biomes=704 reopen=true"
  ]
}
```

### final-export

```json
{
  "pass": true,
  "source_sha": "eba73bdb7f30610469f822dec3dacfe8e0fad0d4",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar",
  "sha256": "4df561680d03ed7d64a8510214406353da311445f67196e3bda0eb7e09bb5de0",
  "size": 125048668,
  "producer_completed": true,
  "build_output_matches_export": true,
  "native_logs": [
    {
      "file": "native-0.txt",
      "sha256": "06754a64f5aa088ce71ed5e798ee6b66ea200fb0ebb9ab7aece281115729ddf7",
      "save_shutdown_pass": true,
      "errors": [
        "[08:57:14] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ]
    },
    {
      "file": "native-1.txt",
      "sha256": "21240c9b03b73c16849672f7c71083c0da515a42562c0601b8b2c8a677da45de",
      "save_shutdown_pass": true,
      "errors": [
        "[08:57:37] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ]
    }
  ],
  "windows_execution": false,
  "online_authentication_tested": false
}
```

### executed-checks

```json
{
  "build-output.txt": {
    "sha256": "1798fe64b786c1f17a579108bdf88e4785a7ca8af6ba45ee1d129ccc3758992a",
    "markers": [
      "CACHE_CHECKS_PASS corruption truncation bounds LRU250 exactDescriptorEquality dynamicOffsets commandReset",
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
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
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=587.094943",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=70.191042 rawNewMs=15.234407 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true",
      "BUILD SUCCESSFUL in 2m 23s",
      "32 actionable tasks: 32 executed"
    ]
  },
  "independent-core.txt": {
    "sha256": "4f92bb26bc7f2abec53900172df831d080455176ac9f4d5a1bff7e45cd655b2e",
    "markers": [
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
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
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=601.941159",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=70.460879 rawNewMs=15.696713 checksum=2787130.868268061",
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

## Not run

Windows client/AMD8060S GPU, graphics/resource-reload/visual acceptance, fresh controlled performance benchmarks, live moving-boat recovery, broad native stream capacity/recharge/conservation scenarios, online authentication, cross-machine determinism, long multiplayer/travel/storm/drought soak, registered V8 saved-world/hydrology/topology acceptance and26.3 adapter checks. Offline-mode external-service errors are retained separately. The agreement requires a demonstrated8.0 average; it is unmet and the auditor stays enabled.

## Current development JAR

[DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2352-eba73bd-vulkan-windows.jar](https://chatgpt.com/library?file_id=libfile_74e52da975948191a04de68419fde074&account_id=personal)

SHA256 `4df561680d03ed7d64a8510214406353da311445f67196e3bda0eb7e09bb5de0`; 125,048,668 bytes. Windows Vulkan bundle, OpenGL fallback and complete Themes. Final export bytes matched after producer completion. JAR pinned source remains `eba73bdb7f30610469f822dec3dacfe8e0fad0d4`; report publication parent is separately recorded.


<!-- END PRESERVED SOURCE -->
