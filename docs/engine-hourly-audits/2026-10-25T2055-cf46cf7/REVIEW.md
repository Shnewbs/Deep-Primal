# Independent engine audit — October25,2026 13:55PDT

Pinned committed main: `cf46cf7e58760e0af1aa5f68edc021c91ab9b150`. Upstream version alpha.29-dev.21. Audit build version `0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7`. Windows Vulkan bundle with OpenGL fallback and complete merged Themes; private development snapshot, not stable. No GitHub/CurseForge release publication by the auditor.

## Actual implemented changes since the previous audit

Previous audit pinned2854805 (dev.20, core-only delta access). Current pinned gradle.properties is dev.21; work-state candidate_version20 is stale. The [creation switch](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/WorldgenConfig.java#L78) now selects the optional engine_deltas_standard/deep packs (channel4) while remaining false by default. Standard/deep defaults stay channel2. [Codec](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L42) accepts0..4; channel3 remains serialized-accessible but has no selecting preset. No automatic saved-profile migration is intended.

Channel3 delta fans/distributaries are now adapter-accessible opt-in; channel4 adds at most24 lowland oxbow candidates per8192-block region. Rim/channel/lake/sea/region-edge/overlap rejection limits placement; accepted crescents have still water/zero flow, bowl beds and a short sediment bank band. Existing channel0..3 behavior has separate replay controls. [Core tests](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/coreChecks/java/com/shnewbs/deepprimal/core/OxbowChecks.java#L1) record20 lakes/5455 wet samples, zero sampled flood edges, lowest rim gap0.5521525534,max stage gap2.25 and12261 replay-grid samples. Delta checks record21 fans/252 endpoints/350 deposited dry-bank points and zero sampled outside changes. These are bounded numerical observations, not broad visuals/physical discharge/long-soak acceptance.

The [ByteLru repair](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/docs/validation/engine-continuation/byte-lru-overflow-2026-10-25/README.md) remains an unpromoted document overlay: production can overflow byte accounting for Long.MAX_VALUE+1 admitted weights. The candidate's3379936 BigInteger-oracle assertions and complete direct-core pass do not qualify a shipped repair. No observed in-game overflow is inferred from that extreme API case. Production finite DensityGrid also retains its confirmed extreme-endpoint interpolation overflow; its11728-check repair remains staged. No staged R57/R60/R61/R62 or either overflow candidate was copied into this JAR. The auditor changes no engine implementation, art, builder checkout/state or lease.

## Grade

**50/100; average5.0/10; Tier2/5 — carried-forward accepted baseline, not a fresh complete regrade. All ten category scores unchanged.** Current checks substantiate scoped progress but cannot establish a complete visual/gameplay/performance regrade. The current owner target is strictly greater than8.5/10;85/100 is insufficient. Builder is resumed for engine/mechanics only and remains untouched; the auditor stays enabled.

|Category|/10|Pinned evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; saved channel2 default and channel4 opt-in delta/oxbow profiles. Registered V8 remains absent.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite baseflow/accounting plus opt-in delta/oxbow generation; coupled braid/distributary discharge, cave families and broad catchment/unloaded/soak gates remain open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and channel0..4 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/cf46cf7e58760e0af1aa5f68edc021c91ab9b150/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed checks and limitations

Independent immutable source snapshot, exclusive audit cache/daemon and own private memory-backed root/Themes/native-cache outputs. Pinned restoration helper verifies Java25/Java21/Gradle9.2.1; source blobs are checked against the pinned Git tree. Completed timing reservation and no Java job observed at start. Only completed auditor generated/restorable source copies were reclaimed for capacity. No builder resources, writer lease, schedule, staged implementation or approved art changed. No release publication by the auditor.

Full uncached rerun Gradle build, complete independent core suite, module boundaries and shutdown-helper regressions; outer/nested CRCs, Windows Vulkan platform payload, source/compiled/independent/packaged class identity, private fixtures/tools exclusion, complete Themes/notices/OpenGL fallback and ground/palette/generated soil resource hashes are recorded below. Exported SHA256/size are compared with the completed producer again after final native execution and saving.

Default channel2 native tests cover V1-V5 deposits, V6/V7 replay, soil2, source capacity/debit/reserve and explicit failure-free ordered startup/save/stop/reopen. Default terrain/deposit hashes must match the previous verified2854805 snapshot. A separate private world enables riverDeltas=true and requires actual saved channel4; before reopen its creation switch is changed to false, while saved generator4 must survive. Opt-in fresh/reopen hashes and loaded-class identities are checked. Receipts below, not a launcher success status, govern actual acceptance. These are Linux dedicated-server tests, not Windows/AMD8060S acceptance. Planned oxbow/delta footprint-specific native voxel/pixel witnesses are not exhaustively asserted by these generic terrain sites.

Not run: Windows/AMD; client GL/Vulkan terrain/lighting/mipmap/reload/temporal water/LOD pixel checks; full physical distributary/oxbow hydrology and boundary transport; separate environmental-weather/1200-tick reach fixtures; controlled performance/FPS/full-tick contention; cross-machine replay; long multiplayer/travel/edit/unloaded soak; integrated registered V8 topology/profile/routed-water acceptance; latest-version port. Incidental core timings are not benchmarks. Historical warm-fill14.45-14.99ms and R62 order-sensitive component32-47ms/255-307ms are not current profile timings. Shared-height p99 previously exceeded2ms. Coarse906 occupancy disagreements and both unpromoted overflow repairs remain open.

Next builder priorities: qualify ByteLru and finite-grid fixes with full gates and a new production version; V8 saved-profile/native topology/routed-water integration; broader opt-in4 footprint/flow/visual/lifecycle evidence; bounded whole-frame/tick generation/publication profiling; ecotones/feature ownership; temporal water/LOD and long saved-world soak. All ten baseline scores remain carried forward, not freshly regraded. Strictly greater than8.5 is required;85/100 does not pass. Auditor enabled; builder untouched.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "upstream_version": "0.0.1-alpha.29-dev.21",
  "version": "0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar",
  "sha256": "fddf6d1d0c532b2612a819c0b4f90ecf9e13fe9a5c8dcb7b2d0628cc6b99d3a9",
  "size": 125231164,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_default2_fresh_save_reopen_shutdown": true,
  "native_optin4_fresh_save_reopen_shutdown": true,
  "optin4_preserved_after_creation_toggle_disabled": true,
  "legacy_V1_V5_and_V6_V7": true,
  "soil_revision2_source_debit": true,
  "default_hashes_match_previous_verified": true,
  "native_shutdown_verifier_pass": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "library_file_id": "libfile_249bfdc3daac8191b0328488ffc9b69f",
  "library_url": "https://chatgpt.com/library?file_id=libfile_249bfdc3daac8191b0328488ffc9b69f&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "verified_inputs": 920,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T2055",
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "status": "completed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "result": "accepted development artifact"
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
      "elapsed_seconds": 0.4
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.49
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.19
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
  "exit": 0,
  "pass": true,
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150"
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 49,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "668cfbeb1fc4d4ce395f936ae796650d75de8f228524368297c1a32d41ffdb0c"
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

### resource-manifest-preflight.json

```json
{
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "mismatches": [],
  "assets_checked": 10,
  "sand_final_pass": true
}
```

### approved-v4-image-result.json

```json
{
  "pass": true,
  "pixels": 40960,
  "actualPackComposeCases": 40960,
  "masks": 8192,
  "nativeGame": false,
  "log_sha256": "395aa0fde8212f10f33536bfb762e108dbb39db102e0a689b09d764d971a7e2c"
}
```

### package.json

```json
{
  "source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-cf46cf7-20261025T2055/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar",
  "sha256": "fddf6d1d0c532b2612a819c0b4f90ecf9e13fe9a5c8dcb7b2d0628cc6b99d3a9",
  "size": 125231164,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar",
      "entries": 673
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7.jar",
      "entries": 121
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.21-audit-20261025T2055-cf46cf7-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 550,
  "independent_core_matches": 113,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 199,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "fddf6d1d0c532b2612a819c0b4f90ecf9e13fe9a5c8dcb7b2d0628cc6b99d3a9",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11,
  "generated_soil_resource_hash_matches": 148,
  "optin_revision4_pack_resource_matches": 4
}
```

### native-result.json

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
  "default_channel2_hashes_match_previous_verified": true,
  "native_failure_precedence_and_ordered_shutdown": true,
  "save_flush_acknowledged": true,
  "legacy_V1_V5_deposit_fresh_reopen": true,
  "soil_revision2_fresh_reopen": true
}
```

Publication parent: {"latest_sha":"cf46cf7e58760e0af1aa5f68edc021c91ab9b150","pinned_sha":"cf46cf7e58760e0af1aa5f68edc021c91ab9b150","advanced":false}
. The JAR remains pinned to its original source; subsequent commits are not included.

### optin-native-result.json

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
  "actual_saved_channel_revision": 4,
  "creation_toggle_disabled_before_reopen": true,
  "creation_switch": true,
  "native_failure_precedence_and_ordered_shutdown": true,
  "save_flush_acknowledged": true,
  "legacy_V1_V5_deposit_fresh_reopen": true,
  "soil_revision2_fresh_reopen": true
}
```
