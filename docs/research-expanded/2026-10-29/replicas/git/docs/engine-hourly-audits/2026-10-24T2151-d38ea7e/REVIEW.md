# Expanded replica — docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-24T2151-d38ea7e/REVIEW.md |
| Preserved source SHA-256 | `bc8b71a3d04e827ce6a854bcccb0a2b2e742feb4c4ca32aae29c7693b504155f` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent current-snapshot audit — 2026-10-24 14:51 PDT”. Its original opening states: “Pinned source: d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a. Version 0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e. Only the previous audit report/receipts changed since dd24715; no production source changes. This audit pins d38ea7e, not the older JAR source. Independent source/cache/daemon/output and lease; no builder checkout/lease/state changes. Builder remains owner-paused; auditor continues.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Independent current-snapshot audit — 2026-10-24 14:51 PDT
- build-result
- independent-core-result

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent current-snapshot audit — 2026-10-24 14:51 PDT

Pinned source: `d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a`. Version `0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e`. Only the previous audit report/receipts changed since `dd24715`; no production source changes. This audit pins `d38ea7e`, not the older JAR source. Independent source/cache/daemon/output and lease; no builder checkout/lease/state changes. Builder remains owner-paused; auditor continues.

**Carried forward baseline, not a fresh complete regrade:50/100, average 5.0/10, Tier 2/5.** Fresh scoped checks cannot establish missing gameplay, artistic finish or owner hardware performance. All ten equally weighted categories remain:

|Category|/10|Pinned source/evidence and remaining limit|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Changes and reviewed evidence

No runtime implementation authored/promoted in this audit. R57/R60/R61/R62 remain staged and excluded. V1–V7 frozen geography, saved profiles, finite water quantity, player edits, module separation, Themes and Deep Primal identity are preserved.

The newest completed builder evidence is R62 full isolated validation (33tasks,7archive CRCs,519product classes,102independent core classes,169private exclusions; codec/scalar/V7 replay/save/stop), but that was a staged Linux candidate, not the source built here. R62 constructor allocation reduction (~37–44KB to~18–21KB) earns no gameplay credit. Sampling remains order-dependent (~32–47ms Y-inner,~255–307ms X-inner);906coarse occupancy disagreements persist. Measured on the historical R62 workload, not remeasured here. Source: [R62 measurements](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md), [subsequent completed candidate gates](https://github.com/Shnewbs/Deep-Primal/blob/d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a/docs/validation/engine-continuation/r62-full-validation-pause-2026-10-24/README.md). These measurements were inspected, not rerun.

## Current audit gates

### source-identity

```json
{
  "source_sha": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
  "verified_inputs": 682,
  "mismatches": [],
  "extra_java": [],
  "production_changes_since_previous_audit": false
}
```

### reviewed-evidence

```json
{
  "fresh_pinned_documents": [
    {
      "path": "docs/validation/engine-continuation/density-metadata-2026-10-24/README.md",
      "blob_sha": "ab1e004d239bb64039195695c74c98d5fc4bce77"
    },
    {
      "path": "docs/engine-review-2026-10-24/REVIEW.md",
      "blob_sha": "574ed311723992aac86785b4a662a94d7acd89f5"
    },
    {
      "path": "docs/validation/engine-continuation/r62-full-validation-pause-2026-10-24/README.md",
      "blob_sha": "049cc2a6f456d6cf8faecc8eac268cce4ccdeeca"
    }
  ],
  "copied_documents_not_used_for_identity": true
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
      "elapsed_seconds": 0.17
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.25
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.12
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

### host

```json
{
  "platform": "Linux-6.18.55-x86_64-with-glibc2.39",
  "machine": "x86_64",
  "logical_cpus": 9,
  "cpu_info": "AMD EPYC 9V74 80-Core Processor",
  "java": "25.0.4.1+1",
  "gradle": "9.2.1",
  "workload": "full forced build + independent core + packaged Linux dedicated server V7 fresh/reopen; Windows Vulkan payload packaging",
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
  "seconds": 139.08381915092468
}
```

### independent-core-result

```json
{
  "exit": 0,
  "production_sources": 57,
  "log_sha256": "dc1f334fa0450715d829639677344a536f6ac30a6f1a7f04794a88aa0bf32269",
  "independent_product_classes_match": 101,
  "tested_instruction_identity_matches": 101,
  "initial_identity_rejection": "Independent javac omitted -g local-variable debug metadata. No instruction differences; matched-debug clean compile and all 101 instruction comparisons pass. Original checked output retained."
}
```

### audit-corrections

```json
{
  "first_package_acceptance": false,
  "reason": "independent javac default debug metadata differed from Gradle -g; FarTerrain javap diff retained",
  "remediation": "clean independent -g compile, exact product-class hashes, all tested instruction streams compared",
  "product_source_changes": false,
  "second_package_driver_rejection": "private validator path update was incomplete; corrected before archive acceptance"
}
```

### package

```json
{
  "source_sha": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-d38ea7e-20261024T2151/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar",
  "sha256": "cb6e251a95abdec0f3d92bcadf3360750f9e055bceea282da1ccd1e69d70be86",
  "size": 124987218,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar",
      "entries": 406
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e.jar",
      "entries": 109
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-windows.jar",
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
  "sha256": "cb6e251a95abdec0f3d92bcadf3360750f9e055bceea282da1ccd1e69d70be86"
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
  "source_sha": "d38ea7e8a590f2f76a40a67d2cb83f70aa24ef4a",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2151-d38ea7e-vulkan-windows.jar",
  "sha256": "cb6e251a95abdec0f3d92bcadf3360750f9e055bceea282da1ccd1e69d70be86",
  "size": 124987218,
  "producer_completed": true,
  "build_output_matches_export": true,
  "native_logs": [
    {
      "file": "native-0.txt",
      "sha256": "9774e21ee604952f12e1ebe022a845d187e06a86e20ea0f2080771606cf193d6",
      "save_shutdown_pass": true,
      "external_service_error": [
        "[06:56:10] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ],
      "game_errors": []
    },
    {
      "file": "native-1.txt",
      "sha256": "64e3962f63062a58e3f3f77c3001955d68aeae6d632717c16219401a4f6c754f",
      "save_shutdown_pass": true,
      "external_service_error": [
        "[06:56:35] [Yggdrasil Key Fetcher/ERROR] [mojang/YggdrasilServicesKeyInfo]: Failed to request yggdrasil public key"
      ],
      "game_errors": []
    }
  ],
  "windows_execution": false,
  "online_authentication_tested": false
}
```

## Audit corrections

The first independent/package identity comparison was rejected because standalone javac omitted local-variable debug metadata. Matched-debug compilation is checked byte-for-byte against the Gradle output, and javap instruction identity is checked against all previously executed independent core classes. This is an audit harness correction; no runtime source changed. The original difference is retained in independent-compile-debug-mismatch.txt; a subsequent private validator path error was also corrected before acceptance.

## Not run and priorities

No online authentication acceptance (both offline-mode sessions logged a Yggdrasil key-fetch DNS failure for api.minecraftservices.com; gameplay assertions/save/shutdown passed). No Windows client/AMD8060S execution, fresh GPU/visual acceptance, host performance benchmark, V8 materialization/base-column/heightmap/saved-graph/hydrology/topology acceptance, long multiplayer/travel/storm/drought soak, cross-machine determinism or26.3adapter validation. Linux dedicated-server checks do not constitute Windows acceptance. This is a private development JAR, not a stable release; no CurseForge/GitHub Release publication.

Builder priorities upon owner-authorized resumption: resolve exact density latency and conservative topology before V8 registration; complete native parity/saved profiles/hydrology; weighted ecotones and feature ownership; bounded host scheduling; water/LOD coverage and Windows owner-GPU acceptance. Target8.0has not been met; auditor stays enabled.

## Development JAR

[Download the verified Windows Vulkan development bundle](https://chatgpt.com/library?file_id=libfile_35a74d9bf1b88191a2213b24557f1a77&account_id=personal). OpenGL fallback and all merged Themes are included. No later builder changes arrived before publication.


<!-- END PRESERVED SOURCE -->
