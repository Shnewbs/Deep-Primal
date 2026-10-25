# Independent committed-snapshot audit — October 25, 2026, 02:49 PDT

Pinned main: `e061ee22ce0c8111f5818e6cc2b4219a21671e99`. Development version `0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2`, Windows Vulkan with OpenGL fallback and complete merged Themes. This is a private development snapshot, not a stable release. No CurseForge or GitHub Release publication.

## Actual changes

Since the previous audit pin `441c83dceb3942f7efc5a71294183ee5a7f9de77`, main adds approved v4 ground artwork exports, complementary masks, matching validation manifest, standalone art checks, and selects the current manifest in build.gradle. No engine Java or world-generation behavior changed. The auditor restored committed inputs only and did not implement changes or promote staged R57/R60/R61 overlays. Frozen V1–V7 geography, quantities, profiles, edits and module identity are preserved. The new artwork does not establish native terrain-scene visual acceptance.

The new asset hash gate now passes. Initial full build reached and passed ground resources, surface-profile and 2,359,296 channel-math checks, then failed because the sparse audit restoration lacked the documented surface example. That exact pinned Git blob was restored; initial failure is retained in build-initial-output.txt and audit-corrections.json. No validation expectation was bypassed. The final full retry and delivery records below are authoritative.

## Grade

**50/100, average 5.0/10, Tier 2/5 — carried-forward accepted baseline, not a fresh complete regrade.** All ten categories equally weighted. [Original rubric](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/docs/engine-review-2026-10-24/REVIEW.md#L1). Unregistered V8, unused libraries and staged experiments earn no gameplay credit. Fresh resource/build/core/package checks do not establish performance or broad visual/gameplay acceptance.

|Category|/10|Pinned evidence and scope|
|---|---:|---|
|Terrain realism/variety|5|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/docs/engine-review-2026-10-24/REVIEW.md#L1): Core replay/seam checks plus recorded packaged save/reopen digests; cross-machine and long soak remain open.|
|Architecture/code quality|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/docs/engine-review-2026-10-24/REVIEW.md#L1): Current build, independent core and package gates are recorded below; native dedicated-server smoke is separate from graphics/Windows acceptance.|
|Polish/config/tooling|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/docs/ACTIVE-ENGINE-AUDIT.md#L1): Approved v4 textures and matching build gate; terrain-scene visuals, water/LOD defects, recovery, editor and latest-version adapter remain open.|

## Isolation, acceptance and limits

Separate snapshot/output directory, exclusively moved completed auditor cache, new single-use Gradle daemon, no build-cache reads and full rerun tasks. JDK25 and Gradle9.2.1 plus required retained tool archives are verified by the current pinned restoration helper. Source identity verifies every restored blob, including the newly restored build example. No builder process, workspace, lease, automation, state or implementation was changed. No active timing reservation observed. No dedicated performance benchmark was run.

Archive records verify outer/nested CRCs, Windows-only Vulkan native payload, compiled/packaged class bytes, independent core class identity, private-fixture exclusion, complete Themes resource bytes, notices and fallback entry points. Final exported bytes are rechecked against completed producer bytes. Native receipts, when present, cover only this Linux dedicated-server host, normal save/stop/reopen and exact loaded-class/replay identity. They are not Windows client or AMD8060S acceptance.

Not run: Windows/AMD GPU execution; actual GL/Vulkan terrain-world, lighting, mipmap, reload and temporal water/LOD visuals; fresh controlled performance/FPS benchmarks; cross-machine replay; multiplayer/travel/edit/weather/unloaded long soak; V8 saved-profile migration/registered gameplay or 26.3 port acceptance. The standalone approved-art image checks establish pixel/math behavior, not game rendering.

Historical accepted warm fill means14.45–14.99ms exclude lighting/IO and exceed5ms. R62 component means32–47ms column order and255–307ms X-inner do not constitute native acceptance;906 coarse occupancy disagreements remain unresolved. [R62 evidence](https://github.com/Shnewbs/Deep-Primal/blob/e061ee22ce0c8111f5818e6cc2b4219a21671e99/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md#L1). No score increase follows from unused density code.

Next: terrain-scene verification of the new art on OpenGL/Vulkan and owner hardware; V8 saved-profile/native parity/topology/routed hydrology integration; latency and scheduling; weighted biome transitions and feature ownership; water/LOD temporal coverage and broader lifecycle/soak acceptance. The >=8.0 average target is not met. Auditor remains enabled. Builder is untouched.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99",
  "version": "0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar",
  "sha256": "1e95a80a8d4d7a0b17c814b944de2d581e505e8869d1b8acc01d38c3830df14c",
  "size": 125150810,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_fresh_save_reopen_shutdown": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "library_file_id": "libfile_10ea6fff0c9881919870e7cce9dbe18b",
  "library_url": "https://chatgpt.com/library?file_id=libfile_10ea6fff0c9881919870e7cce9dbe18b&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99",
  "verified_inputs": 844,
  "mismatches": [],
  "missing_build_inputs": [],
  "restored_documented_build_example": "docs/engine-stage2-2026-10-24/examples/ground-surface-profile.json"
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T0949",
  "source_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99",
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
      "elapsed_seconds": 0.14
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.19
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
  "source_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99"
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

### independent-core-result.json

```json
{
  "exit": 0,
  "production_sources": 61,
  "test_sources": 45,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "f1646226858999bd6603d1205825afaa4501d5e2695d93c5f8346a4369a01d6c"
}
```

### resource-manifest-preflight.json

```json
{
  "pinned_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99",
  "manifest": "docs/art/ground-textures-v4/validation.json",
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
  "source_sha": "e061ee22ce0c8111f5818e6cc2b4219a21671e99",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-e061ee2-20261025T0949/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar",
  "sha256": "1e95a80a8d4d7a0b17c814b944de2d581e505e8869d1b8acc01d38c3830df14c",
  "size": 125150810,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar",
      "entries": 506
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T0949-e061ee2-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 540,
  "independent_core_matches": 107,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package.json

```json
{
  "pass": true,
  "private_class_exclusions": 183,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "1e95a80a8d4d7a0b17c814b944de2d581e505e8869d1b8acc01d38c3830df14c",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11
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
  "loaded_classes_match": true
}
```


Current main at publication: {"main_at_publication": "e061ee22ce0c8111f5818e6cc2b4219a21671e99", "pinned_source": "e061ee22ce0c8111f5818e6cc2b4219a21671e99", "advanced_since_pin": false}. The artifact remains pinned to its original source SHA; newer work, if any, is not included.
