# Expanded replica — docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md |
| Preserved source SHA-256 | `de37893eeb322e611f8808ff24c103968760060006b490d42bf0643122e1cbb7` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — October25,2026 18:54 PDT”. Its original opening states: “Pinned main 4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc; upstream alpha.29-dev.24. Exact build 0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0. Private Windows Vulkan development bundle, OpenGL fallback, complete merged Themes/module notices. No release or CurseForge publication.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Extend the recorded cache/lifecycle result across clear, close, cancellation, capacity recovery, and world replacement.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Bind each request and completion to immutable world identity plus generation; prohibit late publication after that generation closes.
- Separate caller-owned executors from subsystem-owned workers and specify exactly which component completes, cancels, or releases each task.

## Research expansion — acceptance checks

- Pause a loader, clear or close the cache, then resume it and verify that storage cannot be repopulated by stale work.
- Fill admission capacity, free it without moving the camera, and verify eventual progress with bounded queues and reconciled bytes.

## Research expansion — source anchors

- Independent engine audit — October25,2026 18:54 PDT
- Implemented and verified changes
- Execution, failures and limits

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — October25,2026 18:54 PDT

Pinned main 4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc; upstream alpha.29-dev.24. Exact build 0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0. Private Windows Vulkan development bundle, OpenGL fallback, complete merged Themes/module notices. No release or CurseForge publication.

## Implemented and verified changes

Compared with905e550, dev.23 repairs the failed integration: standalone core no longer imports platform/dashboard/settings implementations; materializer and density contracts are reconciled, configurations consolidated, production integration fixtures moved to private tests, core queries connected to world lifecycle/dashboard, and renderer placeholder vendor shaders removed. Working core service offers bounded/coalesced CPU terrain materialization, exposed voxel faces, raycasts and a closed integer water query grid. Existing host block generation/rendering/world-water remain responsible for real-world edits/materials. CPU query meshes do not include player edits, block-model textures, vegetation or deposits. This is not registered V8 worldgen.

Dev.24 [ChunkCache](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/engine-core/src/main/java/com/shnewbs/deepprimal/core/ChunkCache.java#L25) runs caller loaders outside the cache monitor. The generation counter prevents pre-clear work from repopulating cleared storage, concurrent same-key completions reuse one admitted winner, and negative weights fail before callbacks. [Cache fixture](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/coreChecks/java/com/shnewbs/deepprimal/core/CacheLifecycleChecks.java#L8) covers13 assertions, and [close fixture](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreCloseLifecycleChecks.java#L7) verifies close while a caller-owned worker stays blocked, with caller executor ownership retained. Builder baseline dev.23 cache/close failures and20repeat interleavings are retained in its completed evidence; this audit reviews those records, independently reruns current complete core and both fixtures against the actual final nested core. A caller may still receive its computed result after clear; clear does not cancel arbitrary caller code.

[Integration evidence](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/docs/validation/engine-continuation/continue-integration-2026-10-26/README.md) explicitly limits Vulkan spatial scaling: whole frame including UI, default native resolution; no installed FSR/DLSS/frame-generation provider. Vendor requests fall back to native. This audit did not execute a rendered client, so working source/classes do not prove GPU output or owner FPS. [Latest builder evidence](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/docs/validation/engine-continuation/cache-loader-lifecycle-2026-10-26/README.md) reports current build/native success; all current auditor gates below are independently recorded.

Optional creation packs save channel5; defaults stay channel2, prior3/4 replay checks remain. Distributary core checks cover21fans/63branches/126wet samples and conserved shares with zero sampled flood/outside changes. The auditor native opt-in world must actually save5, retain5 after disabling the creation toggle and reopen with matching hashes. This does not prove every distributary native footprint, physical solver coupling, temporal visuals or full catchment accounting. Frozen profile/geography, approved art, finite quantity and player-edit compatibility claims remain scoped to actual fixtures. R57/R60/R61/R62 remain unpromoted. Prior ByteLru/finite-grid overflow fixes remain committed.

## Scores

**Scoped reassessment returns to50/100, average5.0/10; Tier2/5. Not a fresh complete engine regrade.** Four provisional penalties from the broken905e550 snapshot are removed only after current build/core/boundary/package/native gates pass. Six category scores remain carried forward; all ten now match the last full accepted50/100 baseline. No score increase beyond that baseline. Strictly greater than8.5 remains unmet; auditor enabled. The previous4.2 was a mixed-evidence partial downgrade, not a replacement full accepted baseline.

|Category|Current /10|Basis|
|---|---:|---|
|Terrain|5|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Biomes|4|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Hydrology/caves|6|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Features/structures|4|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Performance/threading|3|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Determinism/stability|7|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41); Provisional4 restored to7: complete current build, legacy/default replay, saved opt-in5 and ordered shutdown pass; cross-machine/long-soak still open.|
|Architecture|6|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/build.gradle#L1); Provisional4 restored to6: dependency direction and both boundaries pass; CPU query facade does not register V8 worldgen.|
|Extensibility|4|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54); Historical score carried; no fresh complete gameplay/visual/performance acceptance.|
|Tests/docs|7|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/docs/ACTIVE-ENGINE-AUDIT.md#L1); Provisional5 restored to7: full core/module/resource plus private integration/cache/close, packaged-native and Python gates pass; broad platform/visual/performance matrix absent.|
|Polish/tooling|4|[Pinned source](https://github.com/Shnewbs/Deep-Primal/blob/4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430); Provisional3 restored to4: runnable qualified artifact and lifecycle/configuration integration; owner GPU, visuals and broad authoring/tooling incomplete.|

## Execution, failures and limits

Isolated pinned checkout, exclusive audit Gradle cache/daemon/output and independent lease.1025present restored inputs match Git blobs, zero mismatches/missing required inputs. Pinned restoration helper verifies retained Java25/Java21/Gradle9.2.1. A completed timing reservation was observed; another ordinary builder build was running on this shared container. No performance comparison was attempted, no other job interrupted and no builder resource edited.

Initial optimized shutdown-tool run failed with ENOSPC, retained separately. Only completed auditor scratch duplicates with verified durable receipts and completed/restorable prior-audit files were reclaimed. The audit's completed Themes outputs were moved to private memory storage after their tasks finished, with all file SHA256 values compared. Final tests are rerun, not inferred. The first full Gradle run later lost its daemon; container OOM kill count1 was observed, without a pre-run counter proving attribution. Retained original log/result; bounded single-worker audit retry limits daemon to384MiB and other JVMs to512MiB. No source/assertion changed. A second run failed copying Themes into jarJar with ENOSPC, including failed execution-history writes; its diagnostics are retained. The completed audit root outputs were byte-compared into private memory storage and the failed execution-history cache was removed before another full rerun. The final full rerun result governs build acceptance. Independent complete core and source-boundary gates run separately.

The unique regular exported JAR is copied from the completed producer; final SHA256/size and byte identity are checked after all native producers and after saving. No prior artifact is overwritten. Recursive outer/nested CRCs, platform DLL selection, source/compiled/independent/packaged/loaded identity, fixture/tool exclusion, complete Themes/notices, palette/ground/generated-soil hashes are recorded below. Linux default2 fresh/reopen and opt-in5 fresh/reopen require actual fixture receipts, acknowledged save flush, failure precedence and ordered normal stop/save/loader close. Default terrain/deposit hashes match prior verified7aa3ae3.

Python state12/shutdown16 tests run normal/-O/-OO. Standalone approved-image composition is not in-game visual acceptance. Full core and packaged lifecycle results prove bounded fixture cases, not a universal cancellation/deadlock guarantee.

Not run: Windows/AMD8060S, actual OpenGL/Vulkan presentation/scaling pixels, lighting/mips/resource reload/temporal water/LOD acceptance, FSR/DLSS/frame generation, owner FPS, controlled full-frame/full-tick/tail performance, exhaustive native channel5 footprints/discharge/world ledger, physical unload/edit/long weather and multiplayer travel soak, cross-machine determinism, registered V8 saved-graph/topology/routed-water integration or latest-version port. Historical warm-fill14.45–14.99ms remains over5ms; R62 order sensitivity and906coarse topology disagreements remain unaccepted. Incidental core timings under shared load are not benchmarks.

Next: registered V8 native saved-profile/topology/routed-water gates; channel5 physical hydrology and visual acceptance; whole-tick/frame latency and bounded water publication; ecotones/feature ownership; edits/unload/long saved-world soak. Keep current CPU query and spatial-scaling limitations explicit. Audit progress is observational and never becomes a builder stop gate.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc",
  "upstream_version": "0.0.1-alpha.29-dev.24",
  "version": "0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar",
  "size": 125289246,
  "sha256": "4fdb36fc0b772e76e0111b2e0fda823844ac03f089ac6283af2d5a49454af6c8",
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "full_build_final_pass": true,
  "initial_daemon_failure_retained": true,
  "capacity_retry_failure_retained": true,
  "packaged_lifecycle_pass": true,
  "native_default2_fresh_save_reopen_shutdown": true,
  "native_optin5_fresh_save_reopen_shutdown": true,
  "creation_toggle_disabled_before_optin_reopen": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "library_file_id": "libfile_7fb4e7dee68881919c942626cd799fb7",
  "library_url": "https://chatgpt.com/library?file_id=libfile_7fb4e7dee68881919c942626cd799fb7&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc",
  "verified_inputs": 1025,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261026T0154",
  "source_sha": "4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc",
  "status": "completed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "result": "qualified dev24 development JAR"
}
```

### build-result.json

```json
{
  "exit": 0,
  "pass": true,
  "source_sha": "4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc",
  "retry_after_daemon_and_capacity_failure": true
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 73,
  "test_sources": 55,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "2e7648e3c023ad03ad504c4b90a8df5ec7e7941417fa70b55dcaf4dc3d12dd52"
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

### package.json

```json
{
  "source_sha": "4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc",
  "artifact": "/workspace/scratch/a2f7076b7762/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar",
  "sha256": "4fdb36fc0b772e76e0111b2e0fda823844ac03f089ac6283af2d5a49454af6c8",
  "size": 125289246,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar",
      "entries": 675
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0.jar",
      "entries": 150
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0.jar",
      "entries": 58
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.24-audit-20261026T0154-4fb29e0-windows.jar",
      "entries": 4302
    }
  ],
  "product_class_matches": 587,
  "independent_core_matches": 142,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 205,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "4fdb36fc0b772e76e0111b2e0fda823844ac03f089ac6283af2d5a49454af6c8",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11,
  "generated_soil_resource_hash_matches": 148,
  "optin_revision5_pack_resource_matches": 4
}
```

### packaged-lifecycle-result.json

```json
{
  "pass": true,
  "actual_code_source_verified": true,
  "nested_core_sha256": "7db99717bcef33213d606ec88be7cc7fca73c35afbbc8db0a3fef421ad38e332",
  "checks": [
    "cache loader outside monitor",
    "post-clear admission rejected",
    "one winner for duplicate completion",
    "negative weight before callback",
    "EngineCore close while caller worker blocked",
    "caller executor ownership retained"
  ]
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
  "actual_saved_channel_revision": 5,
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
  "sha256": "4fdb36fc0b772e76e0111b2e0fda823844ac03f089ac6283af2d5a49454af6c8",
  "size": 125289246,
  "producer_completed": true,
  "producer_export_equal": true,
  "default_and_optin_native_gates_pass": true,
  "regular_workspace_file": true
}
```

### publication-parent.json

```json
{"sha":"9434965b90bcbaf2c307044376ff01391832dc33","tree":"2add78c7ed72075b8274b663d5357aab49d80f51","artifact_source_sha":"4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc"}

```

## Newer main, excluded from this JAR and grade

{"pinned":"4fb29e06282bfa860c7ac2dbaae8fe7d69dbc6bc","newer_main":"9434965b90bcbaf2c307044376ff01391832dc33","changed_files":["docs/ENGINE-WORK-STATE.json","docs/WATER-ENGINE-WORK-STATE.json"],"included_in_jar":false,"graded":false}



<!-- END PRESERVED SOURCE -->
