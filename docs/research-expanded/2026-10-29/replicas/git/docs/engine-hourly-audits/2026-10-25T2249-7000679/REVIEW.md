# Expanded replica — docs/engine-hourly-audits/2026-10-25T2249-7000679/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-25T2249-7000679/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-25T2249-7000679/REVIEW.md |
| Preserved source SHA-256 | `4e91dbd055f617eef5ec4bbaeaed5ed79d2cf06595886932755ca0c0dd122deb` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — October 25, 2026, 15:49 PDT”. Its original opening states: “Pinned committed main: 70006796ac665d8113cdfe1520281a956fef346a. Upstream version alpha.29-dev.21 (work-state candidate_version20 is stale). Exact audit version 0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679. Private Windows Vulkan development bundle with OpenGL fallback, complete merged Themes and module notices. No stable/release/CurseForge publication.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Treat restoration, test harnesses, and output paths as independently validated infrastructure so they cannot manufacture product success or failure.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define terminal process status, complete output set, bounded protocol/deadline behavior, and unconditional failure checks.
- Record environment repairs separately from product changes; retain original failure logs and exact fixture/tool revisions.

## Research expansion — acceptance checks

- Run the harness against deliberately failing, truncated, stale, timed-out, and success-marker-only producers.
- Verify wrong request IDs, malformed frames, dangling output paths, missing required inputs, and cleanup after cancellation.

## Research expansion — source anchors

- Independent engine audit — October 25, 2026, 15:49 PDT
- Actual code changes
- Executed checks and limits

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — October 25, 2026, 15:49 PDT

Pinned committed main: 70006796ac665d8113cdfe1520281a956fef346a. Upstream version alpha.29-dev.21 (work-state candidate_version20 is stale). Exact audit version 0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679. Private Windows Vulkan development bundle with OpenGL fallback, complete merged Themes and module notices. No stable/release/CurseForge publication.

## Actual code changes

Compared with previous audit pin19491a10187e679eebf5731aa6fc3bd8079ce786, no engine runtime, resource, native fixture or version changed. The new [combined overflow package runner](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/docs/validation/engine-continuation/combined-overflow-package-2026-10-25/run_package_checks.py#L1) compiles product core separately from private fixtures, packages only core classes, validates CRC/class identity/exclusion and runs both staged repairs' regressions and CoreChecks through that private core JAR. [Loaded-source guard](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/docs/validation/engine-continuation/combined-overflow-package-2026-10-25/PackagedOverflowChecks.java#L1) rejects deliberately shadowed baseline classes. Builder evidence records113 product comparisons,65 fixture exclusions,3379936 ByteLru assertions and11728 finite-grid checks. This is core-only staged qualification, not an installable mod or full native qualification; no candidate overlay is included in this auditor JAR. The auditor reviewed the committed completed evidence, but did not rerun this separate candidate suite or grant gameplay credit. Current production core is independently rerun below.

New32px texture-family candidates and authoring plans are committed under docs/art, not registered mod resources. Candidate documentation does not establish owner approval or in-game acceptance. Existing approved textures remain byte-identical. Smoke-tool regressions remain16 passing tests in each normal/-O/-OO mode; no new smoke-tool change this pin.

Existing optional saved channel4 deltas/oxbows remain opt-in; standard/deep defaults remain channel2. Delta core controls:21 fans,252 endpoints,350 deposited dry-bank points,zero sampled outside changes. Oxbow controls:20 lakes,5455 wet samples,zero sampled flood edges,rim gap0.5521525534,max stage gap2.25,12261 replay-grid samples. These are bounded numerical observations, not exhaustive native footprint/visual/discharge acceptance. This audit independently rebuilds unchanged runtime source with unique run identity.

Production [ByteLru](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/engine-core/src/main/java/com/shnewbs/deepprimal/core/ByteLru.java#L1) still overflows accounting for admitted Long.MAX_VALUE+1 weights; its staged repair has3379936 oracle assertions, not shipped full qualification. This extreme API case does not prove observed ordinary in-game overflow. Production finite DensityGrid still has its confirmed extreme-endpoint interpolation overflow;11728-check candidate remains staged. Neither overflow overlay nor staged R57/R60/R61/R62 is promoted. V8 remains unregistered.

## Grade

**50/100; average 5.0/10; Tier 2/5. Carried-forward accepted baseline, not a fresh complete regrade. All ten scores unchanged.** Scoped successful checks do not establish broad visual/gameplay/performance acceptance. The target is strictly greater than8.5/10;85/100 is insufficient.

|Category|/10|Pinned source evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; default saved channel2 and optional saved channel4 deltas/oxbows. V8 not registered.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite metered supply/reserve and mixed precipitation plus opt-in delta/oxbow generation. Coupled distributary discharge, cave families and broad catchment/unloaded/soak acceptance remain open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and channel0..4 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/70006796ac665d8113cdfe1520281a956fef346a/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed checks and limits

The restored isolated source is checked against the exact Git tree. Pinned restoration helper verifies Java25/Java21/Gradle9.2.1. Exclusive auditor cache/daemon and private memory-backed root/Themes outputs and separate auditor native cache; no builder workspace, lease, process, schedule or staged source changed. The timing reservation was completed and no Java job was observed at start. Only completed auditor outputs and verified durable scratch duplicates were reclaimed; receipts remain. Full rerun Gradle build, independent complete core suite and both module-boundary gates are recorded in receipts below. No controlled benchmark ran; simultaneous core/build timings are incidental.

Final outer/nested CRCs, Windows Vulkan DLL payload, absence of Linux/macOS Vulkan payloads, source/compiled/independent/packaged/loaded class identity, private fixture/tool exclusion, complete Themes/notices/OpenGL fallback, approved ground/palette and generated soil resource hashes pass. The exported regular file is compared with the completed producer after all native sessions and after saving; exact final SHA256/size below.

Four Linux dedicated-server sessions pass: default channel2 fresh/reopen and opt-in4 fresh/reopen. Tests require all expected native markers, actual receipt pass, acknowledged save-all flush, no server error, ordered RCON/save/stop/save-complete/loader-close and zero exit. Default terrain/deposit hashes equal the previous verified19491a1 snapshot. Opt-in4 is created with riverDeltas=true, reopened after setting that creation switch false, and must retain actual saved channel4 and matching hashes. V1-V5 deposits, V6/V7 terrain, soil2, source/debit/reserve and loaded-class identity controls are included. Generic terrain sites do not exhaustively witness all planned delta/oxbow native footprints.

Not run: Windows/AMD8060S execution; client OpenGL/Vulkan terrain/lighting/mips/resource reload/temporal pixel/LOD checks; exhaustive footprint-specific delta/oxbow flow and boundary transport; separate environmental-weather/1200-tick reach fixtures; fresh controlled performance/FPS/full-tick contention; cross-machine replay; broad multiplayer/travel/edit/unloaded long soak; registered V8 topology/saved-profile/routed-water acceptance; latest-version port. Linux dedicated-server success is not Windows acceptance.

Historical warm-fill14.45–14.99ms exceeds5ms. Historical R62 order-sensitive components32–47ms column/255–307ms X-inner and906 coarse occupancy mismatches remain unresolved; these are not current runtime benchmarks. Dev19 shared-height corrections have scoped past GPU/mirror evidence, not full temporal pixels/sides/opacity/LOD acceptance; its component p99 exceeded2ms. No score credit for unused libraries or staged prototypes.

Next priorities: qualify ByteLru/finite-grid fixes with a new production version and full package/native gates; V8 saved-profile/native topology/routed-water integration; broader opt-in4 footprint/discharge/visual/lifecycle checks; bounded whole-frame/tick generation/publication profiling; ecotones/feature ownership; water/LOD and long saved-world soak. Builder remains untouched and the auditor remains enabled. Reports are observational, not builder stop gates.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
  "upstream_version": "0.0.1-alpha.29-dev.21",
  "version": "0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar",
  "sha256": "258019cdd4b81d33e5ad98b70f928896d8502de7ec1f4d2c38ccdd6cbfc9f483",
  "size": 125231172,
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
  "library_file_id": "libfile_ecc62e514d9c81919d372004ed02136f",
  "library_url": "https://chatgpt.com/library?file_id=libfile_ecc62e514d9c81919d372004ed02136f&account_id=personal",
  "local_metadata_applied": true,
  "initial_build_failed": false,
  "final_full_retry_passed": false,
  "shutdown_tool_regressions": "16 tests normal/-O/-OO",
  "production_runtime_changed": false,
  "full_build_first_attempt_passed": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
  "verified_inputs": 949,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### source-changes.json

```json
{
  "previous_source_sha": "19491a10187e679eebf5731aa6fc3bd8079ce786",
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
  "runtime_source_changed": false,
  "changes": [
    "docs/art/BLOCK-TEXTURE-PLAN.md",
    "docs/art/block-texture-families.json",
    "docs/art/block-textures-32-batch01/README.md",
    "docs/art/block-textures-32-batch01/assets.json",
    "docs/art/block-textures-32-batch01/export.mjs",
    "docs/art/block-textures-32-batch01/inspect.py",
    "docs/art/block-textures-32-batch01/manifest.json",
    "docs/art/block-textures-32-batch01/preview.mjs",
    "docs/art/block-textures-32-batch01/review.json",
    "docs/art/block-textures-32-batch01/source-bundle.json",
    "docs/art/block-textures-32-batch01/validation.json",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/PackagedOverflowChecks.java",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/README.md",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/core.log",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/environment.txt",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/inputs.json",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/overlays.json",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/regression.log",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/result.json",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/run_package_checks.py",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/shadow-rejection.log",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/source-rejection.log",
    "docs/validation/engine-continuation/combined-overflow-package-2026-10-25/summary.json"
  ],
  "unmaterialized_optional_docs": "new PNG art candidates remain on Git; not product inputs"
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T2249",
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
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
      "elapsed_seconds": 0.16
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
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a"
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 49,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "721d179b8450adb58120fe240179558dda0407b0f45f9260c9f451b157fa1c86"
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
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
  "mismatches": [],
  "assets_checked": 10,
  "sand_final_pass": true
}
```

### approved-v4-image-result.json

```json
{
  "pass": true,
  "log_sha256": "395aa0fde8212f10f33536bfb762e108dbb39db102e0a689b09d764d971a7e2c",
  "native_game": false
}
```

### package.json

```json
{
  "source_sha": "70006796ac665d8113cdfe1520281a956fef346a",
  "artifact": "/workspace/scratch/a2f7076b7762/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar",
  "sha256": "258019cdd4b81d33e5ad98b70f928896d8502de7ec1f4d2c38ccdd6cbfc9f483",
  "size": 125231172,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar",
      "entries": 673
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679.jar",
      "entries": 121
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.21-audit-20261025T2249-7000679-windows.jar",
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
  "sha256": "258019cdd4b81d33e5ad98b70f928896d8502de7ec1f4d2c38ccdd6cbfc9f483",
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
  "sha256": "258019cdd4b81d33e5ad98b70f928896d8502de7ec1f4d2c38ccdd6cbfc9f483",
  "size": 125231172,
  "producer_completed": true,
  "producer_export_equal": true,
  "default_and_optin_native_gates_pass": true,
  "regular_workspace_file": true
}
```

### publication-parent.json

```json
{"sha":"70006796ac665d8113cdfe1520281a956fef346a","tree":"6cfa438422fa9fc23c656c6574416f4c1a344c68","artifact_source_sha":"70006796ac665d8113cdfe1520281a956fef346a","source_changed_during_audit":false}

```



<!-- END PRESERVED SOURCE -->
