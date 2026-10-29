# Expanded replica — docs/engine-hourly-audits/2026-10-26T0047-905e550/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-26T0047-905e550/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-26T0047-905e550/REVIEW.md |
| Preserved source SHA-256 | `a3ef230a652e0cfee0917095a5d5d01e8b22229b9b6af69273dd6ebdca3a2375` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — October25,2026 17:47 PDT”. Its original opening states: “Pinned committed main: 905e550174e32f9d25d4e01e4e398bb6712288b2. Source version alpha.29-dev.22. Fresh uniquely identified Windows Vulkan/OpenGL-fallback build was attempted from this exact source with Java25/Gradle9.2.1. Build rejected; no current user-facing JAR. No source implementation, builder checkout/process/lease, staged overlay, approved art, saved world or release was changed.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent engine audit — October25,2026 17:47 PDT
- Grade and limitations
- build-result.json

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — October25,2026 17:47 PDT

Pinned committed main: 905e550174e32f9d25d4e01e4e398bb6712288b2. Source version alpha.29-dev.22. Fresh uniquely identified Windows Vulkan/OpenGL-fallback build was attempted from this exact source with Java25/Gradle9.2.1. **Build rejected; no current user-facing JAR.** No source implementation, builder checkout/process/lease, staged overlay, approved art, saved world or release was changed.

## Actual changes and observed defects

Compared with previous verified pin7aa3ae3bd4460b6a5d8164d0f58f65fef2190db7, new engine facade/bootstrap/metrics/cache/water/density/pipeline classes, upscaling settings/renderer/shader plumbing, adapter/configuration wiring and a production-source integration fixture are committed. Channel codec/presets now include optional revision5 distributary share budgets. New names/imports and shaders do not establish functional V8, working upscaling, hydrology or performance acceptance.

Full Gradle build FAILS at engine-core:compileJava (13seconds,2tasks executed; compiler truncates at100errors). Independent current compilation reports105total diagnostics. This independently reproduces and extends the earlier builder's84-error core-only observation at its older source. Exact restored Git blobs match:993present inputs,zero mismatches,zero missing required build inputs. These are source/module/API failures, not a capacity-induced failed compile.

Both explicit source gates FAIL: verifyCoreBoundary names StabilityRuntime's NeoForge ModConfigSpec import; verifyModuleBoundaries names CoreValidation's mod-layer diagnostics import. EngineCore/EngineBootstrap also import LocalDashboard; production EngineIntegrationTest imports optional settings; core EngineCore/WorldGenPipeline refer to missing ChunkMaterializer types, which now live under main source while declaring package core and importing adapter DensityChunkMaterializer. WorldGenPipeline additionally tries constructing interface DensityField and uses incompatible graph APIs; its mesh is empty and GPU upload returns null. See retained compiler logs and pinned source excerpts. Do not fix this by excluding broken files or weakening guards in an audit snapshot.

The prior ByteLru/finite-grid fixes remain committed; do not reopen them merely because unrelated newer facade changes broke compilation. The previous audit independently verified both fixes in the nested core and default2/opt-in4 native replay at7aa3ae3. That successful artifact does not contain the current unqualified facade/revision5/upscaling changes.

Latest completed builder evidence repairs a structurally corrupted work-state JSON and adds strict duplicate/root/timestamp/revision validation. This audit's validator passes. All12state tests and16shutdown tests pass in each normal/-O/-OO mode. An initial auditor validator invocation used the wrong working directory, failed to find docs/ENGINE-WORK-STATE.json and did not run the independent compiler; corrected invocation in the isolated project passes before the actual compile failure. No product edit was needed.

## Grade and limitations

**Provisional42/100, average4.2/10. Tier2/5 baseline capability, current snapshot build-blocked.** This is a mixed-evidence partial reassessment: four categories are downgraded from actual current failures; six retain historical scores with no fresh runtime credit. It is **not a fresh complete audit/regrade**. Last full accepted baseline remains50/100(5.0). It must not be read as a passing current build, or as a new complete acceptance baseline. Strictly greater than8.5 remains unmet; auditor stays enabled.

|Category|Previous|Current provisional|Basis and pinned evidence|
|---|---:|---:|---|
|Terrain|5|5|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Prior heightfield/V7 capability only; current runtime cannot execute. Newly named pipeline/advanced density classes earn no working-world credit.|
|Biomes|4|4|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical climate thresholds remain; no fresh ecotone/visual acceptance.|
|Hydrology/caves|6|6|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L1): Revision5 distributary code/presets are committed but not qualified in this build. No new discharge/native cave credit.|
|Features/structures|4|4|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): No verified general structure/spacing ownership pipeline.|
|Performance/threading|3|3|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L84): Async facade is unbuildable; empty mesh and null GPU upload at94-104. No new measured performance credit.|
|Determinism/stability|7|4|fresh downgrade; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/EngineCore.java#L20): Current committed build fails; unresolved moved materializer/types prevent launch and replay acceptance. Prior frozen-world receipts remain historical.|
|Architecture|6|4|fresh downgrade; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/StabilityRuntime.java#L3): Both source boundaries fail. Core imports NeoForge and mod diagnostics/settings; moved materializer remains package core while requiring adapter types.|
|Extensibility|4|4|carry; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L95): Facade/prototype integration is incomplete; null upload and empty mesh provide no supported generation lifecycle.|
|Tests/docs|7|5|fresh downgrade; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/EngineIntegrationTest.java#L1): New integration fixture resides in production core and cannot compile; full core/package/native gates blocked. Python state12/shutdown16 regressions pass normal/-O/-OO; guards detect failures.|
|Polish/tooling|4|3|fresh downgrade; [source](https://github.com/Shnewbs/Deep-Primal/blob/905e550174e32f9d25d4e01e4e398bb6712288b2/engine-core/src/main/java/com/shnewbs/deepprimal/core/CoreValidation.java#L3): Current installable build unavailable. Strict work-state validator is useful, but does not repair unbuildable facade/configuration plumbing.|

## Executed and not run

Completed pinned restoration-helper verification, independent source identity, full Gradle build attempt, independent complete-source Java25 compilation attempt, both explicit source-boundary tasks with --continue, state validator and normal/-O/-OO state/shutdown test suites. Separate auditor output/cache/daemon/private memory directory used. Timing reservation completed and no Java process observed at acquisition; no timed benchmark launched. Other-chat live ownership is not continuously observable. No builder stop/lease takeover.

Not run/reached: complete Java core execution, completed installable JAR, outer/nested final archive CRCs, compiled/packaged/loaded identity, final platform payload/resource/fixture checks, native save/reopen/shutdown, Windows/AMD8060S, client GL/Vulkan/shader/upscaling/visual/water/LOD pixels, controlled performance/FPS/tails, cross-machine or sustained world soak, registered V8 native topology/profile gates. No failed or older JAR is relabeled as this pin. No release/CurseForge publication.

First priorities: restore dependency direction (platform/config/dashboard integration in adapter modules; core uses neutral interfaces/data), reconcile materializer package/API and DensityField/graph contracts, move production test fixtures to private test sources, replace or leave clearly disabled empty/null stages, then full compile/core/module gates. Only after those pass qualify fresh archives/native saved revisions0..5/quantities/edits and renderer startup/shader behavior. Preserve concurrent owner work and frozen geography; do not claim unused prototypes as game features. Benchmark only runnable bounded workloads after coordinating timing reservations.

## Last verified download — older source

[DeepPrimal-26.1.2-0.0.1-alpha.29-dev.22-audit-20261025T2349-7aa3ae3-vulkan-windows.jar](https://chatgpt.com/library?file_id=libfile_32a8445a40e88191982ad2db7634f8ad&account_id=personal) — pinned 7aa3ae3bd4460b6a5d8164d0f58f65fef2190db7, SHA256 2856a1d61f5e7372525b9b4ead11861ead9c5cc931d90b01f0ffe8862b70f368, 125231202bytes. This is the previous qualified development build, not a successful current build.

## Receipts

### source-identity.json

```json
{
  "source_sha": "905e550174e32f9d25d4e01e4e398bb6712288b2",
  "verified_inputs": 993,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261026T0047",
  "source_sha": "905e550174e32f9d25d4e01e4e398bb6712288b2",
  "status": "completed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "result": "current JAR rejected before packaging"
}
```

### build-result.json

```json
{
  "exit": 1,
  "pass": false,
  "source_sha": "905e550174e32f9d25d4e01e4e398bb6712288b2"
}
```

### independent-core-result.json

```json
{
  "exit": 1,
  "pass": false,
  "production_sources": 73,
  "test_sources": 52,
  "diagnostics_total": 105,
  "log_sha256": "f25cef429278a35e22fa0f1066536b431a46b1cc9ed6c9f2caacb738c2fea528",
  "core_execution": "not run; compilation failed"
}
```

### module-boundary-result.json

```json
{
  "exit": 1,
  "pass": false,
  "tasks": [
    "verifyCoreBoundary",
    "verifyModuleBoundaries"
  ],
  "continue_after_failure": true
}
```

### grade.json

```json
{
  "historical_full_accepted_total": 50,
  "historical_full_accepted_average": 5.0,
  "provisional_current_total": 42,
  "provisional_current_average": 4.2,
  "fresh_complete_regrade": false,
  "mixed_evidence": true,
  "categories": [
    {
      "name": "Terrain",
      "previous": 5,
      "score": 5,
      "basis": "carry",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65",
      "reason": "Prior heightfield/V7 capability only; current runtime cannot execute. Newly named pipeline/advanced density classes earn no working-world credit."
    },
    {
      "name": "Biomes",
      "previous": 4,
      "score": 4,
      "basis": "carry",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76",
      "reason": "Categorical climate thresholds remain; no fresh ecotone/visual acceptance."
    },
    {
      "name": "Hydrology/caves",
      "previous": 6,
      "score": 6,
      "basis": "carry",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L1",
      "reason": "Revision5 distributary code/presets are committed but not qualified in this build. No new discharge/native cave credit."
    },
    {
      "name": "Features/structures",
      "previous": 4,
      "score": 4,
      "basis": "carry",
      "path": "src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120",
      "reason": "No verified general structure/spacing ownership pipeline."
    },
    {
      "name": "Performance/threading",
      "previous": 3,
      "score": 3,
      "basis": "carry",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L84",
      "reason": "Async facade is unbuildable; empty mesh and null GPU upload at94-104. No new measured performance credit."
    },
    {
      "name": "Determinism/stability",
      "previous": 7,
      "score": 4,
      "basis": "fresh downgrade",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/EngineCore.java#L20",
      "reason": "Current committed build fails; unresolved moved materializer/types prevent launch and replay acceptance. Prior frozen-world receipts remain historical."
    },
    {
      "name": "Architecture",
      "previous": 6,
      "score": 4,
      "basis": "fresh downgrade",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/StabilityRuntime.java#L3",
      "reason": "Both source boundaries fail. Core imports NeoForge and mod diagnostics/settings; moved materializer remains package core while requiring adapter types."
    },
    {
      "name": "Extensibility",
      "previous": 4,
      "score": 4,
      "basis": "carry",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/WorldGenPipeline.java#L95",
      "reason": "Facade/prototype integration is incomplete; null upload and empty mesh provide no supported generation lifecycle."
    },
    {
      "name": "Tests/docs",
      "previous": 7,
      "score": 5,
      "basis": "fresh downgrade",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/EngineIntegrationTest.java#L1",
      "reason": "New integration fixture resides in production core and cannot compile; full core/package/native gates blocked. Python state12/shutdown16 regressions pass normal/-O/-OO; guards detect failures."
    },
    {
      "name": "Polish/tooling",
      "previous": 4,
      "score": 3,
      "basis": "fresh downgrade",
      "path": "engine-core/src/main/java/com/shnewbs/deepprimal/core/CoreValidation.java#L3",
      "reason": "Current installable build unavailable. Strict work-state validator is useful, but does not repair unbuildable facade/configuration plumbing."
    }
  ]
}
```

### delivery.json

```json
{
  "accepted": false,
  "source_sha": "905e550174e32f9d25d4e01e4e398bb6712288b2",
  "reason": "Full Java25 Gradle build and independent core compilation fail; both module boundaries fail",
  "new_jar": false,
  "last_verified": {
    "accepted": true,
    "development_only": true,
    "source_sha": "7aa3ae3bd4460b6a5d8164d0f58f65fef2190db7",
    "upstream_version": "0.0.1-alpha.29-dev.22",
    "version": "0.0.1-alpha.29-dev.22-audit-20261025T2349-7aa3ae3",
    "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.22-audit-20261025T2349-7aa3ae3-vulkan-windows.jar",
    "sha256": "2856a1d61f5e7372525b9b4ead11861ead9c5cc931d90b01f0ffe8862b70f368",
    "size": 125231202,
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
    "library_file_id": "libfile_32a8445a40e88191982ad2db7634f8ad",
    "library_url": "https://chatgpt.com/library?file_id=libfile_32a8445a40e88191982ad2db7634f8ad&account_id=personal",
    "local_metadata_applied": true,
    "shutdown_tool_regressions": "16 tests normal/-O/-OO",
    "production_runtime_changed": true,
    "full_build_first_attempt_passed": true,
    "packaged_overflow_controls_pass": true,
    "baseline_overflow_expected_failures": true,
    "independent_core_retry_passed": true
  },
  "windows_execution": false,
  "archive_crc_classes_resources": "not reached",
  "native_smokes": "not run; build failed"
}
```

### publication-parent.json

```json
{"sha":"905e550174e32f9d25d4e01e4e398bb6712288b2","tree":"e51308e2ca1369787501eb016aaa6ee9613eddfc","artifact_source_sha":"905e550174e32f9d25d4e01e4e398bb6712288b2"}

```


<!-- END PRESERVED SOURCE -->
