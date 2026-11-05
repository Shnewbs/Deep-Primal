# Independent engine audit — October 25, 2026, 14:55 PDT

Pinned committed main: 19491a10187e679eebf5731aa6fc3bd8079ce786. Upstream version alpha.29-dev.21 (work-state candidate_version20 is stale). Exact audit version 0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1. Private Windows Vulkan development bundle with OpenGL fallback, complete merged Themes and module notices. No stable/release/CurseForge publication.

## Actual code changes

Compared with prior audit pin cf46cf7e58760e0af1aa5f68edc021c91ab9b150, there are no engine runtime, resource or product-version changes. This pin replaces ten optimization-removable assertions in [server_smoke.py](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/tools/server_smoke.py#L110) with unconditional RuntimeError guards; six additional regression methods in [test_server_smoke.py](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/tools/test_server_smoke.py#L1) exercise the real run path with process/RCON doubles. Wrong seed, missing/changed sample, missing bedrock, busy benchmark, missing cache hit, failed radius/active-request rejection, cancellation and forced-chunk cleanup now reject malformed evidence even under Python -O/-OO. The builder's [completed evidence](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/README.md) retains baseline optimized failures. The independent audit reran all 16 current tests successfully under normal/-O/-OO. Tools are excluded from the product JAR.

Existing optional saved channel4 deltas/oxbows remain opt-in; standard/deep defaults remain channel2. Delta core controls:21 fans,252 endpoints,350 deposited dry-bank points,zero sampled outside changes. Oxbow controls:20 lakes,5455 wet samples,zero sampled flood edges,rim gap0.5521525534,max stage gap2.25,12261 replay-grid samples. These are bounded numerical observations, not exhaustive native footprint/visual/discharge acceptance. This audit independently rebuilds unchanged runtime source with unique run identity.

Production [ByteLru](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/engine-core/src/main/java/com/shnewbs/deepprimal/core/ByteLru.java#L1) still overflows accounting for admitted Long.MAX_VALUE+1 weights; its staged repair has3379936 oracle assertions, not shipped full qualification. This extreme API case does not prove observed ordinary in-game overflow. Production finite DensityGrid still has its confirmed extreme-endpoint interpolation overflow;11728-check candidate remains staged. Neither overflow overlay nor staged R57/R60/R61/R62 is promoted. V8 remains unregistered.

## Grade

**50/100; average 5.0/10; Tier 2/5. Carried-forward accepted baseline, not a fresh complete regrade. All ten scores unchanged.** Scoped successful checks do not establish broad visual/gameplay/performance acceptance. The target is strictly greater than8.5/10;85/100 is insufficient.

|Category|/10|Pinned source evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; default saved channel2 and optional saved channel4 deltas/oxbows. V8 not registered.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite metered supply/reserve and mixed precipitation plus opt-in delta/oxbow generation. Coupled distributary discharge, cave families and broad catchment/unloaded/soak acceptance remain open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and channel0..4 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/19491a10187e679eebf5731aa6fc3bd8079ce786/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed checks and limits

The restored isolated source is checked against the exact Git tree. Pinned restoration helper verifies Java25/Java21/Gradle9.2.1. Exclusive auditor cache/daemon and private memory-backed root/Themes/native outputs; no builder workspace, lease, process, schedule or staged source changed. The timing reservation was completed and no Java job was observed at start. Only completed auditor outputs and verified durable scratch duplicates were reclaimed; receipts remain. The first full rerun completed its core checks but failed jarJar with "Could not read jar file for dependency". Its original log/result are retained. A targeted unchanged-source jarJar diagnostic succeeded; a second complete uncached rerun is recorded as the final build gate. No root cause is proven by that successful retry. Full final rerun Gradle build, independent complete core suite and both module-boundary gates pass. No controlled benchmark ran; simultaneous core/build timings are incidental.

Final outer/nested CRCs, Windows Vulkan DLL payload, absence of Linux/macOS Vulkan payloads, source/compiled/independent/packaged/loaded class identity, private fixture/tool exclusion, complete Themes/notices/OpenGL fallback, approved ground/palette and generated soil resource hashes pass. The exported regular file is compared with the completed producer after all native sessions and after saving; exact final SHA256/size below.

Four Linux dedicated-server sessions pass: default channel2 fresh/reopen and opt-in4 fresh/reopen. Tests require all expected native markers, actual receipt pass, acknowledged save-all flush, no server error, ordered RCON/save/stop/save-complete/loader-close and zero exit. Default terrain/deposit hashes equal the previous verified cf46cf7 snapshot. Opt-in4 is created with riverDeltas=true, reopened after setting that creation switch false, and must retain actual saved channel4 and matching hashes. V1-V5 deposits, V6/V7 terrain, soil2, source/debit/reserve and loaded-class identity controls are included. Generic terrain sites do not exhaustively witness all planned delta/oxbow native footprints.

Not run: Windows/AMD8060S execution; client OpenGL/Vulkan terrain/lighting/mips/resource reload/temporal pixel/LOD checks; exhaustive footprint-specific delta/oxbow flow and boundary transport; separate environmental-weather/1200-tick reach fixtures; fresh controlled performance/FPS/full-tick contention; cross-machine replay; broad multiplayer/travel/edit/unloaded long soak; registered V8 topology/saved-profile/routed-water acceptance; latest-version port. Linux dedicated-server success is not Windows acceptance.

Historical warm-fill14.45–14.99ms exceeds5ms. Historical R62 order-sensitive components32–47ms column/255–307ms X-inner and906 coarse occupancy mismatches remain unresolved; these are not current runtime benchmarks. Dev19 shared-height corrections have scoped past GPU/mirror evidence, not full temporal pixels/sides/opacity/LOD acceptance; its component p99 exceeded2ms. No score credit for unused libraries or staged prototypes.

Next priorities: qualify ByteLru/finite-grid fixes with a new production version and full package/native gates; V8 saved-profile/native topology/routed-water integration; broader opt-in4 footprint/discharge/visual/lifecycle checks; bounded whole-frame/tick generation/publication profiling; ecotones/feature ownership; water/LOD and long saved-world soak. Builder remains untouched and the auditor remains enabled. Reports are observational, not builder stop gates.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "upstream_version": "0.0.1-alpha.29-dev.21",
  "version": "0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar",
  "sha256": "cdf6e2ef40212065aa79d6243ed1bf6013ecb86df645b86cd522762256485222",
  "size": 125231156,
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
  "library_file_id": "libfile_44c3ab6faa208191876e710fac04dcd1",
  "library_url": "https://chatgpt.com/library?file_id=libfile_44c3ab6faa208191876e710fac04dcd1&account_id=personal",
  "local_metadata_applied": true,
  "initial_build_failed": true,
  "final_full_retry_passed": true,
  "shutdown_tool_regressions": "16 tests normal/-O/-OO",
  "production_runtime_changed": false
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "verified_inputs": 928,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### source-changes.json

```json
{
  "previous_source_sha": "cf46cf7e58760e0af1aa5f68edc021c91ab9b150",
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "runtime_source_changed": false,
  "changes": [
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/README.md",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/baseline-normal.log",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/baseline-optimized.log",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/candidate-double-optimized.log",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/candidate-optimized.log",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/candidate-tests.log",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/environment.txt",
    "docs/validation/engine-continuation/smoke-optimization-gates-2026-10-25/summary.json",
    "tools/server_smoke.py",
    "tools/test_server_smoke.py"
  ]
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T2155",
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
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
      "elapsed_seconds": 0.13
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
      "elapsed_seconds": 0.0
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
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "retry_after_initial_jarjar_dependency_read_failure": true
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 49,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "6d48a4bbe69410eb683c9ed0d10cb7122dba6e39bbf6a94be4e6a2a3e21e3fcb"
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
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "mismatches": [],
  "assets_checked": 10,
  "sand_final_pass": true
}
```

### approved-v4-image-result.json

```json
{
  "pass": true,
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "log_sha256": "395aa0fde8212f10f33536bfb762e108dbb39db102e0a689b09d764d971a7e2c",
  "native_game": false
}
```

### package.json

```json
{
  "source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "artifact": "/workspace/scratch/a2f7076b7762/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar",
  "sha256": "cdf6e2ef40212065aa79d6243ed1bf6013ecb86df645b86cd522762256485222",
  "size": 125231156,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar",
      "entries": 673
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1.jar",
      "entries": 121
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.21-audit-20261025T2155-19491a1-windows.jar",
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
  "sha256": "cdf6e2ef40212065aa79d6243ed1bf6013ecb86df645b86cd522762256485222",
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

### pre-upload.json

```json
{
  "sha256": "cdf6e2ef40212065aa79d6243ed1bf6013ecb86df645b86cd522762256485222",
  "size": 125231156,
  "producer_completed": true,
  "producer_export_equal": true,
  "default_and_optin_native_gates_pass": true,
  "regular_workspace_file": true
}
```

### publication-parent.json

```json
{"sha":"19491a10187e679eebf5731aa6fc3bd8079ce786","tree":"a17ee157ba37166a07fa4e611c607bf5f63c555d","artifact_source_sha":"19491a10187e679eebf5731aa6fc3bd8079ce786","source_changed_during_audit":false}

```

