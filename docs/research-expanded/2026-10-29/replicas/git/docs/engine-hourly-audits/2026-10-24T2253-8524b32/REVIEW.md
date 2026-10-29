# Expanded replica — docs/engine-hourly-audits/2026-10-24T2253-8524b32/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-24T2253-8524b32/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-24T2253-8524b32/REVIEW.md |
| Preserved source SHA-256 | `5266ac3046c9545c67109ef9d632cf9e092ca293fe6add49838a99941c980849` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent build audit — 2026-10-24 15:53 PDT”. Its original opening states: “Pinned committed source: 8524b32749b9df2e922f3e2069ca62ea689d1c62. Version 0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent build audit — 2026-10-24 15:53 PDT
- build-result
- independent-core-result

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent build audit — 2026-10-24 15:53 PDT

Pinned committed source: `8524b32749b9df2e922f3e2069ca62ea689d1c62`. Version `0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32`.

Only prior audit records changed since the previous pinned build (`d38ea7e`); there are no production code changes. Fresh current JAR built from the new pinned commit, with staged R57/R60/R61/R62 excluded. No implementation authored or promoted. Builder stays owner-paused; auditor remains enabled. No builder checkout, lease, automation or state was edited.

**50/100, average 5.0/10, Tier 2/5 — carried forward baseline, not a fresh complete regrade.** The fresh build/core/package/native checks below support scoped compatibility; they do not supply missing gameplay, broad visuals or owner hardware measurements. All ten equally weighted categories:

| Category | /10 | Pinned implementation and evidence limit |
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Current checks and environment

Independent checkout, outputs, daemon/cache and receipt. The copied cache was seeded from a completed auditor run; no mutable cache/output directory was shared. Retained verified JDK/Gradle binaries were used read-only. Current helper verified all five pinned archives. 682 active engine/Themes inputs match Git blobs; GitHub compare confirms the pinned advance contains only separate audit evidence. The separate Maritime project is outside this build. Private native fixtures are test-only and excluded from all delivered archives.

Full forced Java25/Gradle build and independent `javac -g` core checks were executed anew, followed by recursive archive CRC, source/compiled/packaged class identity, Windows payload/fallback, Themes resources/notices and private-fixture exclusion. Packaged Linux dedicated-server fresh/reopen runs compare loaded product classes and world replay hashes, save and shut down normally. These are not Windows client acceptance. Concurrent auditor correctness runs invalidate incidental timings as performance evidence; no benchmark grade is inferred from log durations.

Frozen V1–V7 generation, saved profiles, player edits, water quantity and modular graphics/LOD/Vulkan/merged Themes are unchanged. No stable release, installer acceptance, CurseForge publication or GitHub Release is claimed.

## Latest completed builder evidence inspected

- [Pinned density-metadata-2026-10-24](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md)
- [Pinned r62-full-validation-pause-2026-10-24](https://github.com/Shnewbs/Deep-Primal/blob/8524b32749b9df2e922f3e2069ca62ea689d1c62/docs/validation/engine-continuation/r62-full-validation-pause-2026-10-24/README.md)

R62 has a staged constructor allocation reduction (~37–44 KB to ~18–21 KB in its terrain recipe corpus) and completed isolated candidate build/native checks, but remains unpromoted. Its sampling measurements remain order-sensitive (~32–47 ms Y-inner and ~255–307 ms X-inner); historical 906 coarse occupancy disagreements remain unresolved. Those retained benchmarks were read, not rerun. No unused/staged code earns gameplay feature credit.

## Fresh gate records

### source-identity

```json
{
  "source_sha": "8524b32749b9df2e922f3e2069ca62ea689d1c62",
  "verified_inputs": 682,
  "mismatches": [],
  "production_changes_since_previous_audit": false,
  "pinned_compare": {
    "repository_full_name": "Shnewbs/Deep-Primal",
    "base": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
    "head": "8524b32749b9df2e922f3e2069ca62ea689d1c62",
    "status": "ahead",
    "ahead_by": 1,
    "behind_by": 0,
    "total_commits": 1,
    "too_large": null,
    "base_commit": {
      "sha": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
      "url": "https://api.github.com/repos/Shnewbs/Deep-Primal/commits/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
      "html_url": "https://github.com/Shnewbs/Deep-Primal/commit/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a"
    },
    "merge_base_commit": {
      "sha": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
      "url": "https://api.github.com/repos/Shnewbs/Deep-Primal/commits/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
      "html_url": "https://github.com/Shnewbs/Deep-Primal/commit/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a"
    },
    "files": [
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/REVIEW.md",
        "status": "added",
        "additions": 296,
        "deletions": 0,
        "changes": 296,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/audit-corrections.json",
        "status": "added",
        "additions": 7,
        "deletions": 0,
        "changes": 7,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/build-result.json",
        "status": "added",
        "additions": 5,
        "deletions": 0,
        "changes": 5,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/delivery.json",
        "status": "added",
        "additions": 5,
        "deletions": 0,
        "changes": 5,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/executed-checks.json",
        "status": "added",
        "additions": 105,
        "deletions": 0,
        "changes": 105,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/extra-package.json",
        "status": "added",
        "additions": 12,
        "deletions": 0,
        "changes": 12,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/final-export.json",
        "status": "added",
        "additions": 31,
        "deletions": 0,
        "changes": 31,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/host.json",
        "status": "added",
        "additions": 12,
        "deletions": 0,
        "changes": 12,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/independent-compile-debug-mismatch.txt",
        "status": "added",
        "additions": 57,
        "deletions": 0,
        "changes": 57,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/independent-core-result.json",
        "status": "added",
        "additions": 8,
        "deletions": 0,
        "changes": 8,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/lease.json",
        "status": "added",
        "additions": 9,
        "deletions": 0,
        "changes": 9,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/native-0.json",
        "status": "added",
        "additions": 418,
        "deletions": 0,
        "changes": 418,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/native-1.json",
        "status": "added",
        "additions": 418,
        "deletions": 0,
        "changes": 418,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/native-result.json",
        "status": "added",
        "additions": 18,
        "deletions": 0,
        "changes": 18,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/package.json",
        "status": "added",
        "additions": 41,
        "deletions": 0,
        "changes": 41,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/reviewed-evidence.json",
        "status": "added",
        "additions": 17,
        "deletions": 0,
        "changes": 17,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/source-identity.json",
        "status": "added",
        "additions": 7,
        "deletions": 0,
        "changes": 7,
        "previous_filename": null
      },
      {
        "filename": "docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/toolchain-verification.json",
        "status": "added",
        "additions": 42,
        "deletions": 0,
        "changes": 42,
        "previous_filename": null
      }
    ]
  },
  "scope": "682 active engine/Themes build inputs; separate maritime project excluded",
  "extra_production_java": [],
  "production_java_inputs": 337,
  "unpromoted_sampler_excluded": true
}
```

### toolchain-verification

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
      "elapsed_seconds": 0.2
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.13
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

### host

```json
{
  "platform": "Linux-6.18.55-x86_64-with-glibc2.39",
  "machine": "x86_64",
  "logical_cpus": 9,
  "cpu_info": "AMD EPYC 9V74 80-Core Processor",
  "java": "25.0.4.1+1",
  "gradle": "9.2.1",
  "workload": "forced full build and independent core; packaged Linux server fresh/reopen; Windows bundle packaging",
  "windows_execution": false,
  "gpu_execution": false,
  "timing_acceptance": false
}
```

### build-result

```json
{
  "exit": 0,
  "pass": true,
  "seconds": 147.28968414399424
}
```

### independent-core-result

```json
{
  "exit": 0,
  "production_sources": 57,
  "test_sources": 38,
  "debug_metadata": "-g matches Gradle",
  "log_sha256": "55bafc81fe808e1cba4f6a2cc063379fc0560ac7942107f523f839946f2b547b"
}
```

### package

```json
{
  "source_sha": "8524b32749b9df2e922f3e2069ca62ea689d1c62",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-8524b32-20261024T2253/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar",
  "sha256": "22834470390e78972cf67943309d28a5ff5c303efb7083f4bd695c476dbd1e99",
  "size": 124987252,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar",
      "entries": 406
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.jar",
      "entries": 109
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-windows.jar",
      "entries": 4293
    }
  ],
  "product_class_matches": 518,
  "independent_core_matches": 101,
  "private_leaks": [],
  "class_mismatches": [],
  "windows_execution": false
}
```

### extra-package

```json
{
  "pass": true,
  "private_class_exclusions": 164,
  "themes_resource_hash_matches": 2508,
  "notices": [
    "META-INF/licenses/vulkanmod/LICENSE",
    "META-INF/licenses/vulkanmod/COPYING",
    "META-INF/licenses/vulkanmod/UPSTREAM.json"
  ],
  "opengl_fallback_present": true,
  "sha256": "22834470390e78972cf67943309d28a5ff5c303efb7083f4bd695c476dbd1e99"
}
```

### native-result

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

### final-export

```json
{
  "pass": true,
  "source_sha": "8524b32749b9df2e922f3e2069ca62ea689d1c62",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar",
  "sha256": "22834470390e78972cf67943309d28a5ff5c303efb7083f4bd695c476dbd1e99",
  "size": 124987252,
  "producer_completed": true,
  "build_output_matches_export": true,
  "native_logs": [
    {
      "file": "native-0.txt",
      "sha256": "6a7317e42f4c5aafe99247cbfe807d5ced439b72e3e53a6fa8bb645bdf7e0e0a",
      "save_shutdown_pass": true,
      "errors": [
        "[07:58:05] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ]
    },
    {
      "file": "native-1.txt",
      "sha256": "1a82b991517f79779112571e475ba78054c642c99f8e32aec7aa8d9d1e6270c2",
      "save_shutdown_pass": true,
      "errors": [
        "[07:58:31] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ]
    }
  ],
  "windows_execution": false,
  "online_authentication_tested": false
}
```

### executed-checks

```json
{
  "build-output.txt": {
    "sha256": "646fba9051d91582b1be269ed106430213ac7a9f29263cfd3f66aa8970b677a5",
    "markers": [
      "CACHE_CHECKS_PASS corruption truncation bounds LRU250 exactDescriptorEquality dynamicOffsets commandReset",
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
      "NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64 fingerprint=6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1",
      "DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784 overhangColumns=5 minHeight=20.93897487422737 maxHeight=325.50950620538816 fingerprint=c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26",
      "WATER_BED_PASS cases=6144 supportedVertices=24576 sharedPageVertices=512 volumeUnchanged=true",
      "DEPOSIT_PLAN_CORE_PASS cases=1559040 hits=13631 shapes=4 verticalReferences=3 signedChunkSeams=true widerOracle=true",
      "WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true",
      "TERRAIN_V4_PASS banks=237 oreSamples=1343 rockSuites=8 deterministic=true",
      "DEPOSIT_FIELD_CORE_PASS cases=90960 clippedV4=1072 signedSeams=25000 candidates=9 oracleCandidates=49 replay=true first={seed=0 x=-4064 z=-143 y=17 ore=COPPER}",
      "DEPOSIT_ALLOCATION_CORE_PASS cases=4194304 oreHits=1542 emptyColumns=54463 activeColumns=11073 integerExtremes=true retainedColumns=true nullHostRejected=true",
      "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40 dryFade=true immutableSnapshots=true",
      "WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges=400000 maxCircularError=5.960464477539063E-8 negativeDistantSectionEdges=true phaseBounded=true",
      "WATER_FLOW_TEXTURE_CORE_PASS directions=16 vertices=320 legacyStill=true quantityAndGeometryUnchanged=true snapshotImmutable=true dryMaterialRetained=true atlasBounds=true",
      "WATER_FLOW_BLEND_CORE_PASS pairs=272 earlyFirstRetarget=true repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchBlended=true",
      "WATER_FLOW_FIELD_CORE_PASS codecCases=8435621 heightClockCases=98307 haloEdges=34000 uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36",
      "WATER_CORNER_PACKET_CORE_PASS cases=210000 payloadBits=97 directionStates=17 epochBits=16 floatInputsNormalizedFinite=true heightOpacityGeometryLightPreserved=true retirementFramesBound=193 maxClockMillisPerFrame=250",
      "WATER_CORNER_BULK_CORE_PASS cases=210000 masks=16 scalarBytesIdentical=true readOnlyPredicate=true arbitraryVertexOpacityLightPreserved=true nonzeroOffsets=true",
      "WATER_BOUNDARY_INDEX_CORE_PASS cases=688129 oldVisits=7680 indexedVisits=1088 exactCoordinateOracle=true stableOrder=true immutable=true maxEntriesPerInput=3",
      "WATER_CORNER_RETRY_CORE_PASS cases=470000 masks=16 currentBytesPreserved=true expiredPairsSteady=true longElapsedWraps=true",
      "WATER_RETIREMENT_STATE_CORE_PASS cases=60000 mixedAges=true readOnly=true",
      "WATER_SURFACE_SHAPE_PASS planarSamples=441 extremaCases=20000 planarPatchVertices=4 verticalSeamVertices=64 verticalPageSeamVertices=1024",
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=632.06744",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=71.357078 rawNewMs=16.095068 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true",
      "BUILD SUCCESSFUL in 2m 27s",
      "32 actionable tasks: 32 executed"
    ]
  },
  "independent-core.txt": {
    "sha256": "55bafc81fe808e1cba4f6a2cc063379fc0560ac7942107f523f839946f2b547b",
    "markers": [
      "LOD_BUDGET_FIXED_PASS checks=19 aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true",
      "TERRAIN_V6_CORE_PASS columns=8712 caves=110398 icyColumns=1281 tunnelColumns=1 biomes=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37]",
      "TERRAIN_V7_PASS seeds=8 columns=2312 biomeIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 31, 36] uniqueOriginHeights=8 uniqueOriginRain=8 reaches=127840",
      "NOISE_CORE_PASS checks=183806 oracleSamples=40000 sharedFaceSamples=77394 parallelJobs=64 fingerprint=6d53af6ab4d0f54f725723a6fcf0708bb6a7ff82b0b8a6082bb78b31d07267c1",
      "DENSITY_GRAPH_PASS checks=40987 sharedFaceSamples=29784 overhangColumns=5 minHeight=20.93897487422737 maxHeight=325.50950620538816 fingerprint=c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26",
      "WATER_BED_PASS cases=6144 supportedVertices=24576 sharedPageVertices=512 volumeUnchanged=true",
      "DEPOSIT_PLAN_CORE_PASS cases=1559040 hits=13631 shapes=4 verticalReferences=3 signedChunkSeams=true widerOracle=true",
      "WATER_BUDGET_PASS transfers=200000 exactVolumeSaltSediment=true hysteresis=true blocked=true noCatchup=true",
      "TERRAIN_V4_PASS banks=237 oreSamples=1343 rockSuites=8 deterministic=true",
      "DEPOSIT_FIELD_CORE_PASS cases=90960 clippedV4=1072 signedSeams=25000 candidates=9 oracleCandidates=49 replay=true first={seed=0 x=-4064 z=-143 y=17 ore=COPPER}",
      "DEPOSIT_ALLOCATION_CORE_PASS cases=4194304 oreHits=1542 emptyColumns=54463 activeColumns=11073 integerExtremes=true retainedColumns=true nullHostRejected=true",
      "WATER_ANIMATION_CORE_PASS retargets=10000 sharedEdges=40 dryFade=true immutableSnapshots=true",
      "WATER_FLOW_PHASE_CORE_PASS directions=16 origins=100000 edges=400000 maxCircularError=5.960464477539063E-8 negativeDistantSectionEdges=true phaseBounded=true",
      "WATER_FLOW_TEXTURE_CORE_PASS directions=16 vertices=320 legacyStill=true quantityAndGeometryUnchanged=true snapshotImmutable=true dryMaterialRetained=true atlasBounds=true",
      "WATER_FLOW_BLEND_CORE_PASS pairs=272 earlyFirstRetarget=true repeatedUpload=true queuedLatest=true staleGuard=true finiteTwoEndpoints=true stillSwitchBlended=true",
      "WATER_FLOW_FIELD_CORE_PASS codecCases=8435621 heightClockCases=98307 haloEdges=34000 uniformDirections=17 dryStillDistinct=true quantityUnchanged=true vertexStride=36",
      "WATER_CORNER_PACKET_CORE_PASS cases=210000 payloadBits=97 directionStates=17 epochBits=16 floatInputsNormalizedFinite=true heightOpacityGeometryLightPreserved=true retirementFramesBound=193 maxClockMillisPerFrame=250",
      "WATER_CORNER_BULK_CORE_PASS cases=210000 masks=16 scalarBytesIdentical=true readOnlyPredicate=true arbitraryVertexOpacityLightPreserved=true nonzeroOffsets=true",
      "WATER_BOUNDARY_INDEX_CORE_PASS cases=688129 oldVisits=7680 indexedVisits=1088 exactCoordinateOracle=true stableOrder=true immutable=true maxEntriesPerInput=3",
      "WATER_CORNER_RETRY_CORE_PASS cases=470000 masks=16 currentBytesPreserved=true expiredPairsSteady=true longElapsedWraps=true",
      "WATER_RETIREMENT_STATE_CORE_PASS cases=60000 mixedAges=true readOnly=true",
      "WATER_SURFACE_SHAPE_PASS planarSamples=441 extremaCases=20000 planarPatchVertices=4 verticalSeamVertices=64 verticalPageSeamVertices=1024",
      "CAVE_COLUMN_PASS samples=761165 cavities=17272",
      "ENGINE_PRESSURE_CORE_PASS cases=4913 conserved=true monotonicHead=true",
      "INTEGRATED_CORE_PASS samples=5776 river=6 land=1621 ocean=4149 height=-44.93363252194638..288.3561252973467 lodBytes=4692 ms=607.851029",
      "ENGINE_AUDIT_PASS terrainSamples=1800 cubeBytesBefore=72128 cubeBytesAfter=1656 rawOldMs=81.050737 rawNewMs=16.574389 checksum=2787130.868268061",
      "TERRAIN_V2_CORE_PASS samples=12000 changed=381 inlandWater=30 noRaisedRims=true deterministic=true",
      "WATERSHED_CORE_PASS centerlineSamples=5846893 headwaters=964 lakes=650 savedV2=18000 faceConnectedFlowReaches=4292 containedBanks=538915",
      "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true cliffs=true bounds=true continuity=true",
      "CONTAINED_CORE_PASS courseSamples=44088 terraces=38113 descents=4639 random=20000 directions=true",
      "NATURAL_RIVER_CORE_PASS sources=137 lakes=7 partial=36473 integerSourceHeights=true",
      "DRAINAGE_CORE_PASS channels=1312 elevated=1127 gorges=122 random=20000 acyclic=true downhill=true cacheEviction=true",
      "WATER_DYNAMICS_PASS slope RGB speedBands ocean20000 waveBounds minimumUpload",
      "HYDROLOGY_PASS randomizedTransfers=500000 conserved=16244 nativeLevels=8 pressure=true stamina=true"
    ]
  }
}
```

## Not run; remaining priorities

No Windows client/AMD8060S GPU, graphics/visual acceptance, fresh performance benchmark, online-authentication acceptance, cross-machine determinism, long multiplayer/travel/storm/drought soak, V8 materialization/base-column/heightmap/saved-graph/hydrology/topology acceptance or 26.3 adapter checks. Offline-mode native logs retain external-service errors separately from gameplay assertions. No blanket geography/conservation/visual acceptance is inferred from the scoped fixtures.

Next upon authorized builder resumption: exact density latency and conservative topology; V8 native parity and saved-profile/hydrology gates; weighted ecotones and feature ownership; bounded host scheduling; water/LOD lifecycle and visible coverage; Windows owner-GPU acceptance. The agreed 8.0 average is unmet, so the auditor remains enabled.

## Current development JAR

[DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2253-8524b32-vulkan-windows.jar](https://chatgpt.com/library?file_id=libfile_1f0a9e1b7010819185fc1956f7dfc1cc&account_id=personal)

SHA256 `22834470390e78972cf67943309d28a5ff5c303efb7083f4bd695c476dbd1e99`; 124,987,252 bytes. Windows Vulkan bundle with OpenGL fallback and complete merged Themes. Verified final exported bytes after producer completion. Pinned JAR commit remains `8524b32749b9df2e922f3e2069ca62ea689d1c62`; publication parent is recorded separately.

## Audit harness diagnostics

The first package selector rejected two filenames (older and current output) rather than accepting an ambiguous JAR. Exact run/commit selection then passed all archive/identity/native/final-byte gates. The rejected selector output is retained. A metadata-only source scan path was corrected to the actual DensityGraph subdirectory;337production Java inputs match committed entries, with no extra runtime source and no staged sampler. No product code changed. The origin of the older-named output was not established; it is not delivered.


<!-- END PRESERVED SOURCE -->
