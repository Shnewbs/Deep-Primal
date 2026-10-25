# Independent engine audit — October25,2026 10:49PDT

Pinned committed main: `67bbadc14d74d515a250f537bd8f3c3cef273bd8`. Upstream version alpha.29-dev.19. Audit build version `0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc`. Windows Vulkan bundle with OpenGL fallback and complete merged Themes; private development snapshot, not stable. No GitHub/CurseForge release publication by the auditor.

## Implemented changes since previous audit

Previous audited source ec687c2158563f03c8f6737cade635f7e2de66b9 was dev.3. Current committed dev.19 includes 12 soil classes/36 forms with generated resources and saved material revisions; channel revision1 for new profiles; aquifer/head/reserve and finite water-account changes; interval snapshots, capacity-aware mixed rain/snow, exact weather sampling and linearly metered finite stream baseflow; dual-tint soil sides, waterfall and midpoint expiry controls; current-frame shared water-height publication with accepted-byte preservation, next-frame retry and third-failure escalation. These are actual source changes, not new auditor implementation.

Frozen legacy behavior is checked through current core/native controls. New material2/channel1 defaults are separately saved revisions: new profiles intentionally change soil horizons/outcrops and channel beds. Broad old-world compatibility is not inferred from the version numbers alone. No staged R57/R60/R61/R62 or finite-grid candidate overlay was promoted. The [finite-grid overflow repair](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/docs/validation/engine-continuation/density-grid-finite-2026-10-25/README.md) has baseline failure and candidate core evidence only; production DensityGrid remains unrepaired at this pin. It earns no gameplay credit.

## Grade

**50/100; average5.0/10; Tier2/5 — carried-forward accepted baseline, not a fresh complete regrade. All ten category scores unchanged.** Current checks substantiate scoped progress but cannot establish a complete visual/gameplay/performance regrade. The current owner target is strictly greater than8.5/10;85/100 is insufficient. Builder is resumed for engine/mechanics only and remains untouched; the auditor stays enabled.

|Category|/10|Pinned evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; explicit channel revision changes beds for new profiles. V8 not registered.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite metered per-tick baseflow, deficit-cell delivery, reserve and mixed precipitation changes; broad catchment/unloaded/long-soak acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and channel0/1 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/67bbadc14d74d515a250f537bd8f3c3cef273bd8/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed gates and limitations

Independent snapshot, output, cache, single-use daemon and auditor receipt. All present Git inputs verified; pinned restoration helper verified retained Java25/Java21/Gradle9.2.1 tools. No active timing reservation observed; no controlled benchmark launched. Builder checkout, writer lease, staged work, schedules and implementation were not modified.

Initial full build failed solely because this sparse audit restoration lacked the newly required sand-final validation manifest. Restored the exact pinned Git blob without changing validation expectations. Initial command/log/result retained. The first export copy also failed for disk capacity; its diagnostic is retained. Only auditor-owned temporary generated/Themes/native-cache outputs were moved to a private memory-backed directory, preserving paths with symlinks; final exported bytes are still checked against the completed producer. The initial inherited audit label used dev.3; final producer uses dev.19 and its complete retry is recorded separately. Final gate status is in receipts below.

Outer/nested CRCs, platform payloads, compiled/package/independent core class identity, private fixture/tool exclusion, complete Themes, notices, approved ground/palette/generated soil resource hashes and exported producer byte identity are checked. The essential native compatibility gate FAILED before complete fresh/save/reopen acceptance: TerrainAuditNativeChecks invokes DepositNativeChecks.generator for V6, copying material_revision2 from the V7 preset. EngineChunkGenerator rejects that inconsistent pair. This is a committed private-fixture incompatibility, not evidence that ordinary valid V7 creation fails. No successful current native acceptance is claimed; reopen was not run. Gradle printed BUILD SUCCESSFUL despite the server exception, so its status alone is insufficient. Before that failure, fresh ground-material checks390, native surface1572864 block comparisons, soil1572864 comparisons/4395 material changes/638 outcrops, and source/debit/reserve cases passed. Their reopen checks were not reached. These scoped successes do not repair the failed legacy compatibility gate. They do not establish Windows/AMD8060S acceptance. Any native environment gate is separately recorded.

Reviewed dev.19 documentation reports initial creek mirrors0/121794 OpenGL and0/110781 Vulkan disagreements and11 fenced GPU readbacks/API. Those raw build-directory logs are not present in this committed sparse snapshot, and this audit did not rerun client graphics. The final live recheck was incomplete; mirrors are not every-frame rendered pixels/sides/opacity. Component p99 exceeds2ms, maximum-residency/tails remain unaccepted. Do not repeat the old dev18 shared-height defect as if dev19 had no correction.

Not run: separate full environmental-weather native fixtures and 1200-tick stream-retention runs; Windows/AMD GPU; fresh client OpenGL/Vulkan scene, lighting/mipmap/reload or temporal pixel acceptance; fresh controlled performance/FPS/full-tick benchmarks; cross-machine determinism; broad multiplayer/travel/edit/weather/unloaded soak; registered V8 saved-profile/native topology/routed-water integration; latest-version port acceptance. Existing native V8 preview fixtures do not register V8 gameplay. Historical R62 component sampling remains order-sensitive32-47ms column/255-307ms X-inner;906 coarse occupancy disagreements persist.

Next builder priorities: repair the committed legacy native fixture so V1-V6 requests retain valid material/channel revisions, rerun the complete packaged native gate; qualify the staged finite-grid repair; integrated V8 saved-profile/materialization/topology/hydrology; measured bounded water shared-height publication and actual full-frame/full-tick contention; weighted biome transitions/feature ownership; temporal water/LOD coverage and saved-world lifecycle. No progress report is a builder stop gate.

## Delivery

```json
{
  "accepted": false,
  "development_only": true,
  "source_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8",
  "upstream_version": "0.0.1-alpha.29-dev.19",
  "version": "0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar",
  "sha256": "69c995792def31a8b78d145d856b151f200e940f91404c8676a24fd7903e6a5e",
  "size": 125211819,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_fresh_save_reopen_shutdown": false,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "library_uploaded": false,
  "blocker": "Essential packaged native compatibility gate failed; no current JAR delivered.",
  "last_verified_audit_source": "ec687c2158563f03c8f6737cade635f7e2de66b9",
  "last_verified_audit_url": "https://chatgpt.com/library?file_id=libfile_8ea0a610675c8191938bb7f18f3bb31f&account_id=personal"
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8",
  "verified_inputs": 867,
  "mismatches": [],
  "missing_build_inputs": [],
  "restored_required_manifest": "docs/art/sand-final/validation.json"
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T1749",
  "source_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8",
  "status": "completed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "result": "delivery rejected"
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
      "elapsed_seconds": 0.1
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
  "source_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8"
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 46,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "7b5faff165430774678c35c6eac9575decb1af0610af2c02c838d1852e340702"
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
  "pinned_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8",
  "manifest": "docs/art/ground-textures-v4/validation.json",
  "mismatches": [],
  "assets_checked": 10,
  "sand_final_sha256": "9c2ccaf4d828b946192d03d3ba58f047d513c84f7d2d720a7f3040c833898a57",
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
  "source_sha": "67bbadc14d74d515a250f537bd8f3c3cef273bd8",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-67bbadc-20261025T1749/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar",
  "sha256": "69c995792def31a8b78d145d856b151f200e940f91404c8676a24fd7903e6a5e",
  "size": 125211819,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar",
      "entries": 659
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc.jar",
      "entries": 116
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.19-audit-20261025T1749-67bbadc-windows.jar",
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
  "private_class_exclusions": 196,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "69c995792def31a8b78d145d856b151f200e940f91404c8676a24fd7903e6a5e",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11,
  "generated_soil_resource_hash_matches": 148
}
```

### native-result.json

```json
{
  "pass": false,
  "platform": "Linux dedicated server; Windows untested",
  "fresh_session_completed": false,
  "reopen_run": false,
  "failure": "Committed TerrainAuditNativeChecks requests a V6 generator via DepositNativeChecks.generator, which copies the current V7 preset including material_revision=2; EngineChunkGenerator correctly rejects this incompatible pair. Server crashes before complete native audit/save/reopen acceptance.",
  "original_log": "native-0.txt",
  "runtime_exit_status_unreliable": "Gradle printed BUILD SUCCESSFUL despite server exception; explicit native gate failed.",
  "fresh_scoped_passes": [
    "GROUND_MATERIAL_NATIVE_PASS checks=390 materials=39 reopen=false selfDrops=true plantSupport=true soilPermeabilities=36",
    "GROUND_SURFACE_PROFILE_PASS checks=65 rejected=54 immutable=true exactLongSalts=true bounded=true",
    "GROUND_SURFACE_NATIVE_PASS blocks=1572864 seeds=2 adjacentChunks=2 reverseOrder=true baseColumns=true heightmaps=true thinRoof=true caveGap=true bedrock=true ceiling=true snapshotReopen=false",
    "SOIL_WORLDGEN_PASS blocks=1572864 changed=4395 unchangedDecorations=540 seeds=2 sites=4 legacyDefaults=true occupancyPreserved=true materialBaseColumns=true heightmaps=true reverseOrder=true outcrops=638 savedMaterialRevision=2 actualWorldMaterialRevision=2 reopen=false",
    "DENSITY_CHUNK_NATIVE_PASS blocks=196352 columns=256 baseColumnParity=true occupancyExact=true bedrockBarrier=true heightmaps=true",
    "STREAM_DROUGHT_RESERVE_NATIVE_PASS cases=6 attemptsPerCase=16 deficitCell=true exactWaterSaltSediment=true noStepOverspend=true",
    "STREAM_SOURCE_NATIVE_PASS firstOutletUnits=8 secondOutletUnits=8 noOverfill=true noRepeatDebit=true unrelatedWaterPreserved=true aquiferDebit=2560 firstVisits=1 secondVisits=1 outletY=288 regionalQuota=true damEdits=true finiteDrought=true sustainedRecharge=true intervalCadence=true smoothRelease=true headwaterFirst=true deficitCellUnits=16",
    "NOISE_PROFILE_CODEC_PASS cases=21 longSaltExact=true invalidRejected=true",
    "DENSITY_GRAPH_CODEC_PASS cases=34 roundtripSamples=2048 invalidRejected=23"
  ],
  "fresh_soil_and_surface_checks_passed_before_failure": true
}
```

Publication parent: {"latest_sha":"67bbadc14d74d515a250f537bd8f3c3cef273bd8","pinned_sha":"67bbadc14d74d515a250f537bd8f3c3cef273bd8","advanced":false}
. The JAR remains pinned to its original source; subsequent commits are not included.
