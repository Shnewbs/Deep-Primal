# Independent current-snapshot audit — 2026-10-24 13:52 PDT

Pinned source: `dd2471527174e03c32108545ae38fccab46bf9c3`. Version `0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715`. No production source changes since previous audited `dea1246`. Independent source/cache/daemon/output and lease; no builder checkout/lease/state changes. Builder remains owner-paused; auditor continues.

**Carried forward baseline, not a fresh complete regrade:50/100, average5.0/10, Tier2/5.** Fresh scoped checks cannot establish missing gameplay, artistic finish or owner hardware performance. Allten equally weighted categories remain:

|Category|/10|Pinned source/evidence and remaining limit|
|---|---:|---|
|Terrain realism/variety|5|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/docs/engine-review-2026-10-24/REVIEW.md): Core replay/seam checks and scoped native save/reopen; cross-platform/soak acceptance open.|
|Architecture/code quality|6|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/docs/engine-review-2026-10-24/REVIEW.md): Fresh full build/core/package/native checks, retained historical benchmark evidence; Windows/graphics/soak matrix missing.|
|Polish/config/tooling|4|[Source](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/docs/ACTIVE-ENGINE-AUDIT.md): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Changes and reviewed evidence

No runtime implementation authored/promoted in this audit. R57/R60/R61/R62 remain staged and excluded. V1–V7 frozen geography, saved profiles, finite water quantity, player edits, module separation, Themes and Deep Primal identity are preserved.

The newest completed builder evidence is R62 full isolated validation (33tasks,7archive CRCs,519product classes,102independent core classes,169private exclusions; codec/scalar/V7 replay/save/stop), but that was a staged Linux candidate, not the source built here. R62 constructor allocation reduction (~37–44KB to~18–21KB) earns no gameplay credit. Sampling remains order-dependent (~32–47ms Y-inner,~255–307ms X-inner);906coarse occupancy disagreements persist. Source: [R62 measurements](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md), [subsequent completed candidate gates](https://github.com/Shnewbs/Deep-Primal/blob/dd2471527174e03c32108545ae38fccab46bf9c3/docs/validation/engine-continuation/r62-full-validation-pause-2026-10-24/README.md). These measurements were inspected, not rerun.

## Current audit gates

### source-identity

```json
{
  "source_sha": "dd2471527174e03c32108545ae38fccab46bf9c3",
  "verified_inputs": 682,
  "mismatches": [],
  "extra_java": [],
  "production_changes_since_previous_audit": false
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
      "elapsed_seconds": 0.09
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.14
    },
    {
      "asset": "gradle.zip",
      "bytes": 135535598,
      "sha256": "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f",
      "verified": true,
      "elapsed_seconds": 0.18
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
  "workload": "full build+core+packaged Linux dedicated server V7 fresh/reopen; Windows Vulkan payload packaging",
  "windows_execution": false,
  "gpu_execution": false,
  "timing_acceptance": false
}
```

### build-result

```json
{
  "exit": 0,
  "pass": true
}
```

### independent-core-result

```json
{
  "exit": 0,
  "production_sources": 57,
  "log_sha256": "518e6b0d6e21ed405577380a8c94b3b2096080cd732c31d507b3d860135efbf0",
  "independent_product_classes_match": 101
}
```

### package

```json
{
  "source_sha": "dd2471527174e03c32108545ae38fccab46bf9c3",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-dd24715-20261024T2052/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar",
  "sha256": "331790e6699e771f894ce3eceb13e19cd1828e3fc369d1bf3ee392e68d0fbb7d",
  "size": 124987234,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar",
      "entries": 406
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715.jar",
      "entries": 109
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715-windows.jar",
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
  "sha256": "331790e6699e771f894ce3eceb13e19cd1828e3fc369d1bf3ee392e68d0fbb7d"
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
  "producer_complete": true,
  "post_native_unchanged": true,
  "final_recursive_crc": true,
  "source_sha": "dd2471527174e03c32108545ae38fccab46bf9c3",
  "sha256": "331790e6699e771f894ce3eceb13e19cd1828e3fc369d1bf3ee392e68d0fbb7d",
  "size": 124987234,
  "version": "0.0.1-alpha.29-dev.2-audit-20261024T2052-dd24715",
  "windows_execution": false
}
```

## Not run and priorities

No Windows client/AMD8060S execution, fresh GPU/visual acceptance, host performance benchmark, V8 materialization/base-column/heightmap/saved-graph/hydrology/topology acceptance, long multiplayer/travel/storm/drought soak, cross-machine determinism or26.3adapter validation. Linux dedicated-server checks do not constitute Windows acceptance. This is a private development JAR, not a stable release; no CurseForge/GitHub Release publication.

Builder priorities upon owner-authorized resumption: resolve exact density latency and conservative topology before V8 registration; complete native parity/saved profiles/hydrology; weighted ecotones and feature ownership; bounded host scheduling; water/LOD coverage and Windows owner-GPU acceptance. Target8.0has not been met; auditor stays enabled.

## Delivered development JAR

[Download Windows Vulkan development JAR](https://chatgpt.com/library?file_id=libfile_4385d69343848191a76afdbe004ee004&account_id=personal). Source `dd2471527174e03c32108545ae38fccab46bf9c3`; SHA256 `331790e6699e771f894ce3eceb13e19cd1828e3fc369d1bf3ee392e68d0fbb7d`; 124,987,234 bytes. Saving succeeded with local identity metadata applied. Main remained at the pinned SHA immediately before publication; this audit adds evidence only.

Full build:32 executed tasks passed. Independent57-source core compilation matches101product classes;518compiled/package class identities,7archive CRCs,164private fixture exclusions,2508Themes resource hashes. Each native run checks3,062,331blocks,44chunks and704biome bindings; replay hashes match across fresh/reopen; saved player edit persists; both sessions save/stop with exit0. No fresh whole-engine performance benchmark was run.

First native attempt failed at Gradle preparation before Minecraft started because generated assetProperties was missing after clean checkout restoration. Retained diagnostic below; private generated asset path was restored without changing product source, then both native sessions passed. Restricted-network public-key/LAN warnings were observed during offline localhost tests; no online authentication or LAN acceptance is claimed. Gradle reports deprecations incompatible with Gradle10.
