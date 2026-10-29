# Expanded replica — docs/engine-hourly-audits/2026-10-25T1155-e2cc2b1/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-25T1155-e2cc2b1/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-25T1155-e2cc2b1/REVIEW.md |
| Preserved source SHA-256 | `719ae9604667dd984443b7fdf784914280f854389e6c9acba13769961aa9f6ba` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent committed-snapshot audit — October 25, 2026, 04:55 PDT”. Its original opening states: “Pinned main: e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5. Development version 0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1, Windows Vulkan with OpenGL fallback and complete merged Themes. This is a private development snapshot, not a stable release. No CurseForge or GitHub Release publication.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent committed-snapshot audit — October 25, 2026, 04:55 PDT
- Isolation, acceptance and limits
- build-result.json

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent committed-snapshot audit — October 25, 2026, 04:55 PDT

Pinned main: `e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5`. Development version `0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1`, Windows Vulkan with OpenGL fallback and complete merged Themes. This is a private development snapshot, not a stable release. No CurseForge or GitHub Release publication.

## Actual changes

Since the previous audited source `4cb8b0daaf4b4038bbbc5ddd252861d07cd626bf`, only the previous audit evidence has been committed. **No production source changes.** The auditor did not implement changes or promote staged R57/R60/R61 overlays. Current approved v4 textures and their matching manifest are preserved. Frozen V1–V7 geography, quantities, profiles, edits and independent module identity are unchanged. The fresh uniquely versioned build is required even for unchanged source.

No native graphics or new performance acceptance is claimed. Dedicated-server setup uses an empty client-asset root and explicit unused asset index because Gradle requires those launch properties even for the headless server. This is test-launch configuration only; it does not bypass the full build, archive, native fixture or normal shutdown checks.

## Grade

**50/100, average 5.0/10, Tier 2/5 — carried-forward accepted baseline, not a fresh complete regrade.** All ten categories equally weighted. [Original rubric](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/docs/engine-review-2026-10-24/REVIEW.md#L1). Unregistered V8, unused libraries and staged experiments earn no gameplay credit. Fresh resource/build/core/package checks do not establish performance or broad visual/gameplay acceptance.

|Category|/10|Pinned evidence and scope|
|---|---:|---|
|Terrain realism/variety|5|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/docs/engine-review-2026-10-24/REVIEW.md#L1): Core replay/seam checks plus recorded packaged save/reopen digests; cross-machine and long soak remain open.|
|Architecture/code quality|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/docs/engine-review-2026-10-24/REVIEW.md#L1): Current build, independent core and package gates are recorded below; native dedicated-server smoke is separate from graphics/Windows acceptance.|
|Polish/config/tooling|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/docs/ACTIVE-ENGINE-AUDIT.md#L1): Approved v4 textures and matching build gate; terrain-scene visuals, water/LOD defects, recovery, editor and latest-version adapter remain open.|

## Isolation, acceptance and limits

Separate snapshot/output directory, exclusively moved completed auditor cache, new single-use Gradle daemon, no build-cache reads and full rerun tasks. JDK25 and Gradle9.2.1 plus required retained tool archives are verified by the current pinned restoration helper. Source identity verifies every restored blob, including the newly restored build example. No builder process, workspace, lease, automation, state or implementation was changed. No active timing reservation observed. No dedicated performance benchmark was run.

Archive records verify outer/nested CRCs, Windows-only Vulkan native payload, compiled/packaged class bytes, independent core class identity, private-fixture exclusion, complete Themes resource bytes, notices and fallback entry points. Final exported bytes are rechecked against completed producer bytes. Native receipts, when present, cover only this Linux dedicated-server host, normal save/stop/reopen and exact loaded-class/replay identity. They are not Windows client or AMD8060S acceptance.

Not run: Windows/AMD GPU execution; actual GL/Vulkan terrain-world, lighting, mipmap, reload and temporal water/LOD visuals; fresh controlled performance/FPS benchmarks; cross-machine replay; multiplayer/travel/edit/weather/unloaded long soak; V8 saved-profile migration/registered gameplay or 26.3 port acceptance. The standalone approved-art image checks establish pixel/math behavior, not game rendering.

Historical accepted warm fill means14.45–14.99ms exclude lighting/IO and exceed5ms. R62 component means32–47ms column order and255–307ms X-inner do not constitute native acceptance;906 coarse occupancy disagreements remain unresolved. [R62 evidence](https://github.com/Shnewbs/Deep-Primal/blob/e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md#L1). No score increase follows from unused density code.

Next: terrain-scene verification of the new art on OpenGL/Vulkan and owner hardware; V8 saved-profile/native parity/topology/routed hydrology integration; latency and scheduling; weighted biome transitions and feature ownership; water/LOD temporal coverage and broader lifecycle/soak acceptance. The >=8.0 average target is not met. Auditor remains enabled. Builder is untouched.

## Delivery

```json
{
  "accepted": true,
  "development_only": true,
  "source_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5",
  "version": "0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar",
  "sha256": "0039eb3f02be4fc1c25bc64056fbe46d02d012cebd5588b4460ef99409f263f6",
  "size": 125150816,
  "full_build": true,
  "archive_crc_classes_resources": true,
  "native_fresh_save_reopen_shutdown": true,
  "windows_execution": false,
  "visual_acceptance": false,
  "fresh_benchmark": false,
  "producer_completed": true,
  "final_export_matches_producer": true,
  "post_upload_sha256_unchanged": true,
  "library_file_id": "libfile_03b2f49335c88191be8c0debdcac948b",
  "library_url": "https://chatgpt.com/library?file_id=libfile_03b2f49335c88191be8c0debdcac948b&account_id=personal",
  "local_metadata_applied": true
}
```

## Receipts

### source-identity.json

```json
{
  "source_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5",
  "verified_inputs": 844,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T1155",
  "source_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5",
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
      "elapsed_seconds": 0.21
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.18
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.17
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
  "source_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5"
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
  "log_sha256": "26cb681c6d619894143a6071b54fab5a72cb0d092ac7bc53ab6d337aa48d71f9"
}
```

### resource-manifest-preflight.json

```json
{
  "pinned_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5",
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
  "source_sha": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-e2cc2b1-20261025T1155/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar",
  "sha256": "0039eb3f02be4fc1c25bc64056fbe46d02d012cebd5588b4460ef99409f263f6",
  "size": 125150816,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar",
      "entries": 506
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T1155-e2cc2b1-windows.jar",
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
  "sha256": "0039eb3f02be4fc1c25bc64056fbe46d02d012cebd5588b4460ef99409f263f6",
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


Current main at publication: {"main_at_publication": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5", "pinned_source": "e2cc2b1097f97fd8dde249b6671a9f54ccd9b5d5", "advanced_since_pin": false}. The artifact remains pinned to its original source SHA; newer work, if any, is not included.


<!-- END PRESERVED SOURCE -->
