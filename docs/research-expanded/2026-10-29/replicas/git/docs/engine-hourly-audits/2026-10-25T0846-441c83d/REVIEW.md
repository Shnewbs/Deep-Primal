# Expanded replica — docs/engine-hourly-audits/2026-10-25T0846-441c83d/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-25T0846-441c83d/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-25T0846-441c83d/REVIEW.md |
| Preserved source SHA-256 | `7252c3ea923f2fc489901451f65bbb0ded2a8f0359c283d90a317a81b8b4bbc0` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent committed-snapshot audit — October 25, 2026, 01:46 PDT”. Its original opening states: “Pinned main: 441c83dceb3942f7efc5a71294183ee5a7f9de77. This snapshot differs from previous audited source 3cfb730c898fa720521d740d99160f6b1cc14590 only by the previous audit report and receipts. No production source changes. No implementation or staged promotion performed by the auditor. R57/R60/R61 staged overlays were not copied; the committed core and its accepted tests are built as-is.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Preserve the distinction between source-art hashes, exported-runtime hashes, and an obsolete validation manifest; a failed hash check is not permission to replace approved artwork.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version the asset recipe and exported manifest together, and record which manifest generation each build task consumes.
- Keep the failed resource gate, corrected input identity, and subsequent package evidence linked by one finding ID; do not erase earlier failed attempts.

## Research expansion — acceptance checks

- Rebuild exports from the recorded recipe and compare runtime-asset hashes with the selected manifest, then reject a deliberately stale manifest.
- Verify packaged approved textures and required examples, and confirm that a test-environment omission is distinguished from a product-resource defect.

## Research expansion — source anchors

- Independent committed-snapshot audit — October 25, 2026, 01:46 PDT
- Performance, defects and next priorities
- build-result.json

Related design: [assets materials ui](../../../../../chapters/assets-materials-ui.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent committed-snapshot audit — October 25, 2026, 01:46 PDT

Pinned main: `441c83dceb3942f7efc5a71294183ee5a7f9de77`. This snapshot differs from previous audited source `3cfb730c898fa720521d740d99160f6b1cc14590` only by the previous audit report and receipts. **No production source changes.** No implementation or staged promotion performed by the auditor. R57/R60/R61 staged overlays were not copied; the committed core and its accepted tests are built as-is.

Attempted development version: `0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d`, Windows Vulkan bundle with OpenGL fallback and complete merged Themes. This is not a stable release.

**Current full build failed. No current JAR is accepted or uploaded.** The ground resource check still reads the historical asset manifest at [build.gradle:124](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/build.gradle#L124), then fails the first current side-texture hash at [GroundResourceChecks:27](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/src/visualValidation/java/com/shnewbs/deepprimal/validation/GroundResourceChecks.java#L27). Seven asset/mask hashes differ. Both expectations and actual bytes are retained in resource-manifest-preflight.json. The auditor does not change approved artwork, replace expectations or bypass an essential gate. Reconciliation belongs to implementation.

## Score and evidence

**50/100; average 5.0/10; Tier 2/5 — carried-forward accepted baseline, not a fresh complete regrade.** All ten categories equally weighted. The original accepted rubric is [REVIEW.md](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/engine-review-2026-10-24/REVIEW.md#L1). Fresh source inspection and scoped regressions do not demonstrate new gameplay/artistic/performance acceptance. Unregistered V8 and unused/staged APIs earn no gameplay feature credit.

|Category|/10|Pinned source and verified scope/limitation|
|---|---:|---|
|Terrain realism/variety|5|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/engine-review-2026-10-24/REVIEW.md#L1): Fresh core replay/seam suites pass; native save/reopen is historical, not rerun after this essential build failure. Cross-machine and long soak remain open.|
|Architecture/code quality|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/engine-review-2026-10-24/REVIEW.md#L1): Fresh full build fails the asset gate; independent core and boundary checks do not establish packaged gameplay or Windows acceptance.|
|Polish/config/tooling|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/ACTIVE-ENGINE-AUDIT.md#L1): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Isolation and performed checks

Full available Java25/Gradle build requested with `--rerun-tasks`, exclusive audit cache moved from completed prior run, new checkout/output paths and single-use daemon. Source-identity.json verifies 834 immutable build inputs against pinned Git blobs, with no mismatches or missing required files. The current documented restoration helper verifies all five pinned tool archives. Prior auditor generated build outputs were reclaimed only in obsolete completed audit directories; reports and exported files retained. No builder workspace, lease, process, schedule, state file or implementation was changed. No active timing reservation observed; no performance benchmark was run. The independent core compilation uses its own output and all tests run with `-Xverify:all`. Full raw pass/fail markers are in executed-checks.json; build-result.json and module-boundary-result.json are authoritative, rather than mere task presence.

Build uses the Windows platform payload; this Linux host cannot provide Windows/AMD8060S acceptance. Archive diagnostic records, when present, validate CRCs, class identity, fixtures exclusion, Themes resources, module notices and platform bytes only. They do not override the failed build. Any diagnostic export is rehashed and compared with completed producer bytes; it is not uploaded for installation.

## Performance, defects and next priorities

No fresh performance measurement. Accepted historical native warm fill means 14.45–14.99 ms exclude lighting, IO and other host costs, already above the 5 ms goal. Stage2 component results are not whole-engine acceptance. See the [stage2 evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/engine-stage2-2026-10-24/README.md#L1) and [R62 constructor evidence](https://github.com/Shnewbs/Deep-Primal/blob/441c83dceb3942f7efc5a71294183ee5a7f9de77/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md#L1). Historical 906 coarse occupancy disagreements remain unresolved. V8 remains unregistered; surface snapshot APIs are not saved gameplay integration. New masked sprite/shader/mipmap behavior has no fresh graphics acceptance from this run.

Priorities: reconcile validation with approved current asset identities, then rerun full delivery gates; validate ground shaders/lighting/mipmaps/reload in terrain scenes on OpenGL/Vulkan; complete V8 saved-profile, native parity/topology and routed hydrology; reduce density latency and scheduler contention; weighted biome transitions and feature ownership; resolve water/LOD temporal/coverage defects and perform owner Windows/GPU validation. Frozen V1–V7, quantities, player edits and modular rendering are preserved.

Not run after essential build failure: packaged native Minecraft save/reopen/shutdown, Windows execution, actual GL/Vulkan shader and visual/resource-reload/terrain-world checks, fresh performance, cross-machine replay, multiplayer/travel/edit/weather/unloaded long soak, V8 saved-profile migration and 26.3 port acceptance.

Last verified user JAR: source `5c54904a1316b27d6512aa219f938867d31a49f5`, [download](https://chatgpt.com/library?file_id=libfile_c282e4a126688191926a0876c4959747&account_id=personal). This older artifact is not the current failed build. No GitHub Release or CurseForge publication. Auditor remains enabled; no fresh >=8.0 audit is demonstrated. Builder state untouched.

## Current receipts

### source-identity.json

```json
{
  "source_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77",
  "verified_inputs": 834,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T0846",
  "source_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77",
  "status": "completed",
  "builder_mutated": false,
  "performance_reservation": "No active timing reservation observed; no Java/benchmark process at start",
  "isolated_cache_outputs": true,
  "result": "build failed; no accepted artifact"
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
      "elapsed_seconds": 0.11
    },
    {
      "asset": "jdk21.tar.gz",
      "bytes": 207473347,
      "sha256": "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94",
      "verified": true,
      "elapsed_seconds": 0.17
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

### build-result.json

```json
{
  "exit": 1,
  "pass": false,
  "source_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77"
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
  "log_sha256": "10db4450fc2c83ef2127b7c39ae041563eb569356bcb3bea48e3cf40e74ea0c6"
}
```

### resource-manifest-preflight.json

```json
{
  "pinned_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77",
  "mismatches": [
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/grass_side.png",
      "expected": "3e9af97dbc815c2e4c01485c6d0a034a957273eb8b197e1fe83bd3d8b5c346a9",
      "current": "892016e56edac23421f8c16f7fc187a7914af455a00cf7b7c5ee3ce1fa3e17d3"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/dirt_side.png",
      "expected": "0d7e25a80523a324c3232eefd483cf466405f03e9856fe777806bfba4d0b5e41",
      "current": "85037d7789230af2d6014a6f7c2bb72ba2606d11f0b4063b941e8655df425324"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/podzol_side.png",
      "expected": "58aa70894c2b6f65c899ce1edd9aa9c598c4d9923bba07df2e10984aaf49b618",
      "current": "cfcdd5a1c8c68a7028e3ead62b1547564533e3183a4ff9fd0fe261947e84fb57"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_surface.png",
      "expected": "d8789463d6ae5fac74b785d0cacc5f1524628a519694a115be3b9ecc8cf5762f",
      "current": "d286bd10ca51a34393ed35ab23336060f9306f44153b419e6518cdce122d8291"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/grass_side_soil.png",
      "expected": "3a06a5ad3c506aa683ef0c6fba7f06445d4803cb726523b308c1ca2c5062bca0",
      "current": "6f3814d8327e2703b972d4c65fa4a802ff3d9a195fa725444b168284bc24a714"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_surface.png",
      "expected": "ec16948ca900de30b72c3a8a9ad2517d492d81dc4cf930f6e16ea5d909cb3a1f",
      "current": "c3c2f416852aa4fca008c6312ec52554e38fbd16cc85b9a3b0b85d4b3fae65c7"
    },
    {
      "path": "src/main/resources/assets/deepprimal/textures/block/ground/masks/podzol_side_soil.png",
      "expected": "7979138694deb1c9306ea00786cc907302d1ced38f649a56d5bac51bbb63a4b3",
      "current": "bbec0e9aaefa9214b1396c894065dc0a55783e07b46d46aa3e4c800c83bafb10"
    }
  ]
}
```

### package.json

```json
{
  "source_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-441c83d-20261025T0846/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar",
  "sha256": "7a4cbfdbb53299c27ae04e3e3084e91bb3bbc3c2286a750ba7d614ad16528c6b",
  "size": 125120010,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar",
      "entries": 506
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-windows.jar",
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
  "sha256": "7a4cbfdbb53299c27ae04e3e3084e91bb3bbc3c2286a750ba7d614ad16528c6b",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11
}
```

### delivery.json

```json
{
  "accepted": false,
  "diagnostic_archive_only": true,
  "source_sha": "441c83dceb3942f7efc5a71294183ee5a7f9de77",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0846-441c83d-vulkan-windows.jar",
  "sha256": "7a4cbfdbb53299c27ae04e3e3084e91bb3bbc3c2286a750ba7d614ad16528c6b",
  "size": 125120010,
  "final_export_matches_producer": true,
  "producer_completed": true,
  "reason": "Full build fails groundMaterialResources historical texture hash gate",
  "native_smoke": "not run after essential build failure",
  "windows_execution": false,
  "library_uploaded": false,
  "last_verified_source": "5c54904a1316b27d6512aa219f938867d31a49f5",
  "last_verified_library_file_id": "libfile_c282e4a126688191926a0876c4959747"
}
```



<!-- END PRESERVED SOURCE -->
