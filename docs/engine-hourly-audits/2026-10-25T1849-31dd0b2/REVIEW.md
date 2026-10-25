# Independent engine audit — October25,2026 11:49PDT

Pinned committed main: `31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545`. Upstream version alpha.29-dev.20. Audit build version `0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2`. Windows Vulkan bundle with OpenGL fallback and complete merged Themes; private development snapshot, not stable. No GitHub/CurseForge release publication by the auditor.

## Implemented changes since previous audit

Previous audit pinned67bbadc (dev.19). Current committed dev.20 adds channel revision2: hydraulic-geometry widths, great-river/estuary flare, graded trunk incision, stepped canyon walls, meanders and braided dry bars. New presets select channel2; saved channels0/1 remain separate and their core replay controls pass. No automatic migration of saved profiles is claimed. This changes new-world geography through an explicit saved channel subrevision while retaining V7 generator identity; broader frozen-world compatibility must remain a gate.

The private DepositNativeChecks helper now removes V7-only material/channel fields for non-V7 requests and removes unsupported terrain profiles for V1-V5. This addresses the preceding audit failure. The builder retained baseline/candidate126 extracted-template cases and compilation evidence; this audit additionally invokes the committed native fixture. The auditor made no engine/fixture-source changes and did not promote staged R57/R60/R61/R62 or the finite-grid candidate.

Fresh [EarthRiverChecks](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/coreChecks/java/com/shnewbs/deepprimal/core/EarthRiverChecks.java#L10) covers three seeds/nine regions each. Its pass line records widths[12833,401730,37742,3070,1939,318], widest214.6437405 versus legacy30.0621485;1029 transects,67596 wet columns,57 dry-bar columns,327 incised centres,deepest76.2728691,23436 canyon benches and zero reported flood edges/tunnelled canyons in this corpus. These are component/numerical checks, not visual sign-off or complete physical flow topology. Deltas, anastomosing channels, oxbows, separately budgeted braid discharge and outwash placement remain unimplemented/unaccepted. [Width implementation](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L418).

## Grade

**50/100; average5.0/10; Tier2/5 — carried-forward accepted baseline, not a fresh complete regrade. All ten category scores unchanged.** Current checks substantiate scoped progress but cannot establish a complete visual/gameplay/performance regrade. The current owner target is strictly greater than8.5/10;85/100 is insufficient. Builder is resumed for engine/mechanics only and remains untouched; the auditor stays enabled.

|Category|/10|Pinned evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; explicit channel revision2 adds wider/incised channels for new profiles. V8 not registered.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite metered per-tick baseflow, deficit-cell delivery, reserve and mixed precipitation changes; broad catchment/unloaded/long-soak acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and channel0/1/2 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed gates and limitations

Independent committed snapshot, exclusive auditor cache, own single-use daemon and private memory-backed build/Themes/native-cache outputs. All present source inputs match pinned Git blobs; the pinned restoration helper verifies Java25/Java21/Gradle9.2.1. Current timing reservation was completed; no active Java job observed at start. No builder checkout, lease, staged work, schedule, approved art or implementation changed. No releases published on the builder's behalf.

An initial auditor-only native readiness check waited for terrain/deposit markers but asserted soil/source markers before those fixtures completed. The harness condition was corrected and a clean complete fresh/reopen sequence rerun; initial diagnostics/world are retained and excluded. No committed source changed.

The first file transfer failed before saving while the export used a memory-backed symlink. After reclaiming only completed auditor temporary dependency downloads, a regular workspace copy was verified byte-for-byte and saved successfully; initial transfer failure is retained.

Full rerun build and independent core tests, module checks, outer/nested CRCs, Windows Vulkan payload, compiled/package/independent core identity, private fixture/tool exclusion, complete Themes/notices/fallback, approved ground/palette/generated soil resources, producer completion and exported bytes are recorded below. Packaged native fixtures request V1-V5 deposit compatibility, V6/V7 terrain replay, soil revision2/base-column/horizon/heightmaps, finite stream source/debit/reserve and normal fresh/save/stop/reopen lifecycle. Actual pass/fail receipts below govern acceptance; a Gradle status alone is not native acceptance. Linux dedicated server checks do not establish Windows/AMD8060S acceptance.

Dev.20 documentation reports13 native gates and fresh/reopen terrain acceptance, and dev.19 documentation reports scoped OpenGL/Vulkan water GPU controls. Raw build-directory/client logs are not present in this committed sparse snapshot; this audit does not claim to have rerun graphics. Native/client/performance claims are kept separate. The core suite's incidental timing lines are not controlled benchmark evidence.

Not run: separate environmental-weather and1200-tick reach fixtures; Windows/AMD GPU; fresh client OpenGL/Vulkan terrain/lighting/mipmap/reload, full-frame water/sides/opacity/LOD visuals; controlled performance/FPS/full-tick benchmarks; cross-machine replay; long multiplayer/travel/edit/unloaded soak; integrated registered V8 saved-profile/topology/routed-water acceptance; current-version port acceptance. Historical14.45-14.99ms native warm fills and R62 component32-47ms/255-307ms are historical only, not current dev.20 timings. The dev.19 shared-height component p99 exceeded2ms; current broad latency/tail acceptance remains open. The906 coarse occupancy disagreements and unpromoted finite-grid overflow repair remain unresolved production/library gates.

Next priorities: complete legacy/native qualification, qualify finite-grid repair against current source; integrated V8 saved-profile/materialization/topology/hydrology; measured bounded water/river generation and publication under full-frame/full-tick contention; weighted biome transitions/feature ownership; temporal water/LOD and saved-world soak. Target strictly greater than8.5;85/100 does not pass. Auditor enabled, builder untouched. A completed audit is not a builder stop gate.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545",
  "upstream_version": "0.0.1-alpha.29-dev.20",
  "version": "0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar",
  "sha256": "fb62d8fd1074da67c4a71cdbfa28e6934cd5c200fd2a1d79b8325af6b8f49e6f",
  "size": 125215116,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_fresh_save_reopen_shutdown": true,
  "legacy_V1_V5_and_V6_V7": true,
  "soil_revision2_source_debit": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "library_file_id": "libfile_8fd937c6871c8191b139f728ec3be8bc",
  "library_url": "https://chatgpt.com/library?file_id=libfile_8fd937c6871c8191b139f728ec3be8bc&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545",
  "verified_inputs": 883,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T1849",
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545",
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
      "elapsed_seconds": 0.1
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.16
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.11
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
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545"
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 47,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "5aaea4045a77a012c5a13e480e4404a2db49c35343389ecdce2d7ff892ea7d36"
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
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545",
  "assets_checked": 10,
  "sand_final_pass": true,
  "mismatches": []
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
  "source_sha": "31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-31dd0b2-20261025T1849/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar",
  "sha256": "fb62d8fd1074da67c4a71cdbfa28e6934cd5c200fd2a1d79b8325af6b8f49e6f",
  "size": 125215116,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar",
      "entries": 659
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2.jar",
      "entries": 116
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.20-audit-20261025T1849-31dd0b2-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 545,
  "independent_core_matches": 108,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 197,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "fb62d8fd1074da67c4a71cdbfa28e6934cd5c200fd2a1d79b8325af6b8f49e6f",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11,
  "generated_soil_resource_hash_matches": 148
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
  "legacy_V1_V5_deposit_fresh_reopen": true,
  "soil_revision2_fresh_reopen": true
}
```

Publication parent: {"latest_sha":"31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545","pinned_sha":"31dd0b2f528d8c21e9f1eb3c4c00679f7f4d9545","advanced":false}
. The JAR remains pinned to its original source; subsequent commits are not included.
