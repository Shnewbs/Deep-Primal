# Expanded replica — docs/engine-hourly-audits/2026-10-25T0055-5c54904/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-25T0055-5c54904/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-25T0055-5c54904/REVIEW.md |
| Preserved source SHA-256 | `0dbbb7b3fec8998cdc2abd1116923dbb87671633abbca88524cc63c1deff6b10` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent audit — 2026-10-24 17:55 PDT”. Its original opening states: “Pinned source 5c54904a1316b27d6512aa219f938867d31a49f5. Version 0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904. Previous audit pinned eba73bdb7f30610469f822dec3dacfe8e0fad0d4.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Preserve the distinction between source-art hashes, exported-runtime hashes, and an obsolete validation manifest; a failed hash check is not permission to replace approved artwork.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version the asset recipe and exported manifest together, and record which manifest generation each build task consumes.
- Keep the failed resource gate, corrected input identity, and subsequent package evidence linked by one finding ID; do not erase earlier failed attempts.

## Research expansion — acceptance checks

- Rebuild exports from the recorded recipe and compare runtime-asset hashes with the selected manifest, then reject a deliberately stale manifest.
- Verify packaged approved textures and required examples, and confirm that a test-environment omission is distinguished from a product-resource defect.

## Research expansion — source anchors

- Independent audit — 2026-10-24 17:55 PDT
- Committed changes and audit scope
- Historical benchmarks and next priorities

Related design: [assets materials ui](../../../../../chapters/assets-materials-ui.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent audit — 2026-10-24 17:55 PDT

Pinned source `5c54904a1316b27d6512aa219f938867d31a49f5`. Version `0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904`. Previous audit pinned `eba73bdb7f30610469f822dec3dacfe8e0fad0d4`.

**50/100, average 5.0/10, Tier 2/5 — carried-forward full baseline, not a fresh complete regrade.** Scoped new tests and assets do not demonstrate broad gameplay/graphics/performance acceptance. Ten equally weighted categories:

|Category|/10|Pinned source and current limit|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Committed changes and audit scope

No runtime Java implementation changed since the previous pinned snapshot. Ten new 64x64 grayscale ground textures/masks and their exporter/proofs were committed. The stream-source native fixture and its validation hook were also added; owner scope documentation now specifies minimum8.5/10 and ideal9.5/10. The older8.0goal in the handoff is stale. Neither threshold is reached; auditor remains enabled and builder remains owner-paused. No automation or builder state is changed by this audit.

New ground assets are packaged byte-for-byte, but model/tint providers are absent. They are unintegrated art assets, not new in-game terrain/biome functionality. Fresh Pillow checks verify dimensions, neutral grayscale, intended repeat edges, hashes and8192complementary side-mask pairs. No visual gameplay credit. See [asset contract](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/art/ground-textures-v2/README.md#L21) and [stream fixture](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/src/visualValidation/java/com/shnewbs/deepprimal/validation/StreamSourceNativeChecks.java#L37).

New packaged native stream checks invoke the actual replenishment path with a private reflection fixture and planned outlet state. They verify a6-unit cell and a dry outlet each reach8units, additional full-outlet calls debit nothing, unrelated water remains unchanged, and aquifer debit equals10accepted units. This is controlled runtime-path validation, not natural source discovery, long-duration conservation or broad saved-world stream acceptance. Fresh and reopened server sessions reset their local test outlets; they do not prove persistence of these exact test cells. V7 saved replay hashes are independently compared between sessions. V8 remains a temporary-chunk adapter check, not a registered generator.

Independent audit source/output/cache/daemon and receipt. No active timing reservation or competing builder job was present. Read-only pinned tools verified using documented restoration helper. Only completed older auditor caches/build outputs were pruned; prior reports/JARs and builder files preserved. Source restoration verifies793available files against pinned Git blob identities, including all current build-source inputs. Previously recovered complete Themes assets retained. Historical docs scripts and separate Maritime/standalone projects are outside this build. No staged R57/R60/R61 files overlaid; already committed sampler APIs included exactly as main contains them. Frozen V1–V7, profiles, finite-water quantities and player edits are untouched by the auditor.

## Checks and evidence

Full forced Java25/Gradle Windows Vulkan build, independent javac/core checks, recursive archive CRCs, source/compiled/packaged/loaded class identity, private fixture exclusion, Windows DLL-only payload, OpenGL fallback, complete merged Themes/notices, and post-producer export verification. Packaged Linux server fresh/reopen checks include codec validation, exact V8 temporary-chunk occupancy/base-column/heightmap/bedrock parity, V7 replay, new stream fixture, normal save and shutdown. Linux server execution is not Windows client acceptance. Incidental job durations are not controlled performance evidence.

An initial jarJar build failed reading a dependency; an independent ZIP scan rejected the Themes output. Retried build re-executed packaging successfully; all final archives were checked after producer completion. The cause of the transient read/invalid-output failure is not established; first-attempt output/result and archive diagnostics retained. No failed artifact delivered.

The initial source scope assertion mistakenly treated historical docs scripts and unrelated standalone projects as required build inputs. Corrected scope follows settings.gradle; no missing current module inputs or byte mismatches. No product source change. Dedicated-server asset preparation references the independent copied index30, as in the previous accepted audit.

## Historical benchmarks and next priorities

No fresh controlled performance benchmark. Historical R62 constructor allocation dropped37–44KB to18–21KB, but exact sampling remained32–47ms Y-inner and255–307ms X-inner on the recorded component workload; current native warm fill historical14.45–14.99ms excludes lighting/IO/host costs. No speed/FPS improvement claimed. The906coarse occupancy disagreements remain unresolved. Read [density metadata evidence](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md), [stage2results](https://github.com/Shnewbs/Deep-Primal/blob/5c54904a1316b27d6512aa219f938867d31a49f5/docs/engine-stage2-2026-10-24/RESULTS.md), and the original rubric.

Priorities upon owner-authorized builder resumption: exact density latency and conservative topology; native V8 saved-profile/hydrology/multi-seed/seam acceptance before registration; weighted ecotones and feature ownership; bounded host scheduling; water/LOD/transparency coverage; Windows8060Sclient and current-version acceptance.

## Not run

Windows client/AMD8060S GPU; current Vulkan/OpenGL scene/resource-reload/visual acceptance; fresh performance benchmarks; live boat recovery; natural stream discovery/recharge and broad storm/drought/edit/unloaded conservation; long multiplayer/travel soak; cross-machine replay; registered V8 saved graph/hydrology/topology integration;26.3adapter. Offline external-service errors are retained and do not certify online authentication. This is a private development snapshot, with no stable-release or CurseForge/GitHub Releases publication.

## Fresh records

### source-identity

```json
{
  "source_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "verified_inputs": 793,
  "mismatches": [],
  "missing_build_inputs": [],
  "production_changes_since_previous_audit": "ten ground PNG resources; no runtime Java changes"
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
      "elapsed_seconds": 0.79
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.93
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.22
    },
    {
      "asset": "checked-agent/neoform-runtime-2.0.31-all.jar",
      "bytes": 4483539,
      "sha256": "6f5b0aae7e423efeea21cb848c5096e76fc4eec40256cbefdcfaa1583b197c07",
      "verified": true,
      "elapsed_seconds": 0.02
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
  "source_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "first_attempt": "build-attempt1.json",
  "retry": "retained successful task outputs; rerun failed packaging and remaining tasks"
}
```

### independent-core-result

```json
{
  "exit": 0,
  "production_sources": 60,
  "test_sources": 43,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "424632af1231251921da59405e856f42cf768e1f33615bd73b14be1668e2259b"
}
```

### ground-assets

```json
{
  "pass": true,
  "runtime_pngs": [
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/grass_top.png",
      "sha256": "c1a5fc2c808088e183d6303a620989eef7ffbaaa3187c281c1c2a8c31644dfb9"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/dirt_top.png",
      "sha256": "aece8a3a2841d5f7975ca8d5691a436da603af389fe5c2ecf85b3c02185166cb"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/podzol_top.png",
      "sha256": "e6a3d2b3e8342e0598641f4e74b4066cf8ea71c8762df4716433e519ca3751cd"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/grass_side.png",
      "sha256": "3e9af97dbc815c2e4c01485c6d0a034a957273eb8b197e1fe83bd3d8b5c346a9"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/dirt_side.png",
      "sha256": "0d7e25a80523a324c3232eefd483cf466405f03e9856fe777806bfba4d0b5e41"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/podzol_side.png",
      "sha256": "58aa70894c2b6f65c899ce1edd9aa9c598c4d9923bba07df2e10984aaf49b618"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_surface.png",
      "sha256": "d8789463d6ae5fac74b785d0cacc5f1524628a519694a115be3b9ecc8cf5762f"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_soil.png",
      "sha256": "3a06a5ad3c506aa683ef0c6fba7f06445d4803cb726523b308c1ca2c5062bca0"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_surface.png",
      "sha256": "ec16948ca900de30b72c3a8a9ad2517d492d81dc4cf930f6e16ea5d909cb3a1f"
    },
    {
      "file": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_soil.png",
      "sha256": "7979138694deb1c9306ea00786cc907302d1ced38f649a56d5bac51bbb63a4b3"
    }
  ],
  "dimensions_grayscale_repeat_checks": true,
  "complementary_mask_pairs": 8192,
  "gameplay_integration": false
}
```

### package

```json
{
  "source_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-5c54904-20261025T0055/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar",
  "sha256": "c2856811221c37c9dc1ba57fbf3b19f47113ecfccf9c3b8a185f0557831b99ad",
  "size": 125072670,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar",
      "entries": 469
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904.jar",
      "entries": 114
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-windows.jar",
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
  "private_class_exclusions": 172,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "c2856811221c37c9dc1ba57fbf3b19f47113ecfccf9c3b8a185f0557831b99ad",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 10
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
  "loaded_classes_match": true
}
```

### final-export

```json
{
  "pass": true,
  "source_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar",
  "sha256": "c2856811221c37c9dc1ba57fbf3b19f47113ecfccf9c3b8a185f0557831b99ad",
  "size": 125072670,
  "producer_completed": true,
  "build_output_matches_export": true,
  "native_logs": [
    {
      "file": "native-0.txt",
      "sha256": "d66f842cdeb5507251139717a113fb517be62f3ebbd0b2ff4b9e147a02bfd5b5",
      "save_shutdown_pass": true,
      "errors": [
        "[10:02:34] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ]
    },
    {
      "file": "native-1.txt",
      "sha256": "00174264c5a9a3dcf8da4f909a7ed0ab531c467dc66c65834d40eedc70b9549f",
      "save_shutdown_pass": true,
      "errors": [
        "[10:03:01] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
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
    "sha256": "7088e2954266fbcbd1fe3286645d9c64948e750f5192cc4f7f99d28891d5b376",
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
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=677.095678",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=75.165835 rawNewMs=15.515224 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true",
      "BUILD SUCCESSFUL in 2m 13s",
      "32 actionable tasks: 8 executed, 24 up-to-date"
    ]
  },
  "independent-core.txt": {
    "sha256": "424632af1231251921da59405e856f42cf768e1f33615bd73b14be1668e2259b",
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
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=706.722288",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=71.088251 rawNewMs=15.268516 checksum=2787130.868268061",
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

### source-after-build

```json
{
  "source_sha": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "matching_files": 793,
  "mismatches": []
}
```

### audit-corrections

```json
{
  "first_build_failed": "jarJar: Could not read jar file for dependency",
  "independent_scan": "Themes output rejected as not a zip file",
  "retry": "full requested task graph passed; 8 executed, 24 up-to-date after 28 initial tasks executed",
  "failure_cause": "unproven; failed first output not delivered",
  "asset_preparation": "independent copied index30",
  "source_scope": "exclude historical docs scripts, separate standalone-engine and Maritime; all selected module inputs match pinned blobs"
}
```

## Development JAR

[DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261025T0055-5c54904-vulkan-windows.jar](https://chatgpt.com/library?file_id=libfile_c282e4a126688191926a0876c4959747&account_id=personal)

SHA256 `c2856811221c37c9dc1ba57fbf3b19f47113ecfccf9c3b8a185f0557831b99ad`; 125,072,670 bytes. Windows Vulkan bundle, OpenGL fallback and complete Themes. Exact pinned JAR source `5c54904a1316b27d6512aa219f938867d31a49f5`; publication parent is recorded separately.


<!-- END PRESERVED SOURCE -->
