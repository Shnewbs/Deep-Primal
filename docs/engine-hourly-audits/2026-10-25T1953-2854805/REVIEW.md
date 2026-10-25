# Independent engine audit — October25,2026 12:53PDT

Pinned committed main: `28548052886603214914c5d6fd49ffbfe85d2a02`. Upstream version alpha.29-dev.20. Audit build version `0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805`. Windows Vulkan bundle with OpenGL fallback and complete merged Themes; private development snapshot, not stable. No GitHub/CurseForge release publication by the auditor.

## Actual changes since the preceding audited source

Previous audit pinned31dd0b2 (qualified dev.20). Current pin adds committed core delta planning and private validation/tool changes; production version remains dev.20. WatershedRegion plans at most6 low-coastal great-trunk fans with3 distributaries each under core channel revision3. TerrainEngine accepts0..3 and enables deltas only at3. Existing0/1/2 retain the no-delta branch; DeltaChecks retains revision2 fingerprints, finite/containment/endpoint-depth/outside-footprint assertions. The builder expanded dry-bank survey coverage without lowering its original threshold after the original five near-trunk probes all sampled wet columns.

**At the pinned source, deltas are not integrated gameplay.** The [game codec](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L42) still accepts channel revisions0..2 and packaged presets remain2. Core3 is therefore inaccessible through normal saved-profile creation. No delta gameplay, saved migration or score credit follows from this core work. [Core delta entry](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L447). Existing Earth-scaled channel2 behavior remains in the JAR; the auditor does not promote staged R57/R60/R61/R62 or the finite-grid candidate.

The [shutdown helper](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/tools/server_smoke.py#L18) now rejects native errors even when Gradle exits0, verifies ordered startup/stop/final-save/loader-close and exact required PASS tokens, requires save acknowledgement, and emits success only after the complete fresh/reopen sequence. Ten Python regression methods pass both normal and optimized execution. This audit invokes the new verifier against its newly executed packaged native sessions as well as checking class/resource identity. The auditor changed no committed source or builder state.

Builder evidence at [delta/shutdown checkpoint](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/docs/validation/engine-continuation/delta-shutdown-gates-2026-10-25/README.md) distinguishes historical-log replay and direct-core checks from full native/build acceptance. Its staged finite-grid11728-case pass is not production promotion. Current-source and this audit's actual build/native results are recorded below.

## Grade

**50/100; average5.0/10; Tier2/5 — carried-forward accepted baseline, not a fresh complete regrade. All ten category scores unchanged.** Current checks substantiate scoped progress but cannot establish a complete visual/gameplay/performance regrade. The current owner target is strictly greater than8.5/10;85/100 is insufficient. Builder is resumed for engine/mechanics only and remains untouched; the auditor stays enabled.

|Category|/10|Pinned evidence and limits|
|---|---:|---|
|Terrain|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L65): Heightfield/V7 remains active; explicit channel revision changes beds for new profiles. V8 not registered.|
|Biomes|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; no complete weighted ecotones.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L368): Finite metered per-tick baseflow, deficit-cell delivery, reserve and mixed precipitation changes; broad catchment/unloaded/long-soak acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L120): General spacing/ownership structure pipeline remains absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L423): Synchronous fill and synchronized region creation persist. Historical warm fills14.45-14.99ms exceed5ms; dev19 shared-height p99 component exceeds2ms. No new controlled benchmark.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L41): Saved material0/1/2 and game channel0/1/2 controls plus fresh native replay receipts; cross-machine/long-soak not established.|
|Architecture|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/build.gradle#L1): Separate core/graphics/LOD/performance/Vulkan modules; generator and runtime responsibilities remain dense.|
|Extensibility|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Versioned soil/channel controls and strict profiles; no active saved V8 graph/feature lifecycle.|
|Tests/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/docs/ACTIVE-ENGINE-AUDIT.md#L1): Fresh build/core/package/native receipts below; broad platform/visual/performance coverage incomplete.|
|Polish/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/28548052886603214914c5d6fd49ffbfe85d2a02/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java#L430): Current-frame shared-height publication and rejected-write recovery implemented. Full pixels/sides/opacity/LOD and owner hardware acceptance remain open.|

## Executed gates and limits

Independent pinned source/output, exclusive completed-auditor cache, own single-use Gradle daemon and private memory-backed root/Themes/native-cache outputs. Source blobs and pinned Java25/Java21/Gradle9.2.1 restoration helper verified. No active timing reservation or Java job observed at start. No builder checkout, writer lease, schedule, staged source, approved art or implementation altered. No release publication on the builder's behalf.

Full uncached rerun build, complete independent Java25 core suite, module boundaries and Python shutdown regressions; archive CRCs for outer/nested bundles, Windows Vulkan payload, source/compiled/independent/packaged class identity, private fixture/tool exclusion, complete merged Themes/notices/OpenGL fallback, palette/ground/generated soil resource hashes and final exported producer bytes are recorded below. Packaged Linux native checks cover V1-V5 deposits, V6/V7 terrain replay, soil revision2 and base-column/heightmap controls, finite source/debit/reserve and normal save/reopen/shutdown. The new failure-precedence/ordered-lifecycle verifier and explicit successful save acknowledgement are required. Receipts govern actual acceptance; no Gradle-success-only native claim.

Not run: native channel3 delta gameplay/profile creation (not registered/codec-supported); separate full environmental-weather/1200-tick reach fixtures; Windows/AMD8060S execution; fresh GL/Vulkan terrain, lighting/mipmap/reload or temporal water/LOD pixel acceptance; controlled performance/FPS/full-tick measurements; cross-machine replay; long multiplayer/travel/edit/unloaded soak; registered V8 saved-profile/topology/routed-hydrology integration; latest-version port acceptance. Independent core timings are incidental, not controlled benchmarks. Historical native warm fills14.45-14.99ms and R62 component32-47ms column/255-307ms X-inner are historical only, not current delta-enabled or current-frame timings. Previous shared-height p99 exceeded2ms; broad current latency/tails remain open.906 coarse occupancy disagreements and production finite-grid extreme-endpoint overflow remain unresolved.

Next: qualify finite-grid runtime/regression without silently promoting an overlay; integrate V8 saved-profile/materialization/topology/routed water; require native/profile/visual/physical-discharge gates before enabling delta3; measured bounded generation/publication under realistic contention; weighted ecotones/feature ownership; temporal water/LOD and saved-world soak. Preserve saved channels0/1/2 and frozen geography. The target is strictly greater than8.5/10;85/100 is insufficient. No full regrade is claimed and all ten accepted baseline scores carry forward. Auditor remains enabled; builder untouched.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02",
  "upstream_version": "0.0.1-alpha.29-dev.20",
  "version": "0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar",
  "sha256": "0436015bdbbf1b260c4f07c8ed94c895431b2fda69c10187432251d553298bfb",
  "size": 125221601,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_fresh_save_reopen_shutdown": true,
  "legacy_V1_V5_and_V6_V7": true,
  "soil_revision2_source_debit": true,
  "native_hashes_match_previous_verified_31dd0b2": true,
  "native_shutdown_verifier_pass": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "library_file_id": "libfile_c1d837cf82e88191a2d904e7270da02f",
  "library_url": "https://chatgpt.com/library?file_id=libfile_c1d837cf82e88191a2d904e7270da02f&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02",
  "verified_inputs": 901,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T1953",
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02",
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
      "elapsed_seconds": 0.12
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
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02"
}
```

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 62,
  "test_sources": 48,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "86b5caddd804bcfe57e4586d92b49356e2b6f91a3b31a16a202b2b022dc3d8b3"
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
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02",
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
  "source_sha": "28548052886603214914c5d6fd49ffbfe85d2a02",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-2854805-20261025T1953/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar",
  "sha256": "0436015bdbbf1b260c4f07c8ed94c895431b2fda69c10187432251d553298bfb",
  "size": 125221601,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar",
      "entries": 659
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805.jar",
      "entries": 119
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.20-audit-20261025T1953-2854805-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 548,
  "independent_core_matches": 111,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 198,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "0436015bdbbf1b260c4f07c8ed94c895431b2fda69c10187432251d553298bfb",
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
  "native_hashes_match_previous_verified_31dd0b2": true,
  "native_failure_precedence_and_ordered_shutdown": true,
  "save_flush_acknowledged": true,
  "legacy_V1_V5_deposit_fresh_reopen": true,
  "soil_revision2_fresh_reopen": true
}
```

Publication parent: {"latest_sha":"5effdc13eb1cdb14fd8ccafb4b3e6d5bda504b57","pinned_sha":"28548052886603214914c5d6fd49ffbfe85d2a02","advanced":true}
. The JAR remains pinned to its original source; subsequent commits are not included.

## Newer builder snapshot, excluded

After this audit pinned its source, main advanced to `5effdc13eb1cdb14fd8ccafb4b3e6d5bda504b57` (`0.0.1-alpha.29-dev.21`). Newer dev.21 adds opt-in delta creation, codec0..3 and preset packs. That work is excluded from this JAR and its executed checks; this audit does not grade or qualify it. The statement that delta3 is inaccessible applies to the pinned dev.20 source, not that newer snapshot. See newer-main.json for the changed paths.
