# Independent committed-snapshot audit — October 24, 2026, 22:55 PDT

Pinned main: `d8a38d9067bf1ed6557957ec391a718cccd48e5b`. This snapshot differs from previous audited source `88f2bba41866bcea30d5b3397eabe690b26c9baf` only by the previous audit report and receipts. **No production source changes.** No implementation or staged promotion performed by the auditor. R57/R60/R61 staged overlays were not copied; the committed core and its accepted tests are built as-is.

Attempted development version: `0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9`, Windows Vulkan bundle with OpenGL fallback and complete merged Themes. This is not a stable release.

**Current full build failed. No current JAR is accepted or uploaded.** The ground resource check still reads the historical asset manifest at [build.gradle:124](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/build.gradle#L124), then fails the first current side-texture hash at [GroundResourceChecks:27](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/src/visualValidation/java/com/shnewbs/deepprimal/validation/GroundResourceChecks.java#L27). Seven asset/mask hashes differ. Both expectations and actual bytes are retained in resource-manifest-preflight.json. The auditor does not change approved artwork, replace expectations or bypass an essential gate. Reconciliation belongs to implementation.

## Score and evidence

**50/100; average 5.0/10; Tier 2/5 — carried-forward accepted baseline, not a fresh complete regrade.** All ten categories equally weighted. The original accepted rubric is [REVIEW.md](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/engine-review-2026-10-24/REVIEW.md#L1). Fresh source inspection and scoped regressions do not demonstrate new gameplay/artistic/performance acceptance. Unregistered V8 and unused/staged APIs earn no gameplay feature credit.

|Category|/10|Pinned source and verified scope/limitation|
|---|---:|---|
|Terrain realism/variety|5|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java#L176): V7 heightfield; density V8 remains unregistered; fresh V6/V7 core checks.|
|Biome blending/coherence|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java#L76): Categorical thresholds; weighted ecotones remain absent.|
|Hydrology/caves|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedRegion.java#L88): Connected drainage, conservation and cave checks; regional/long-duration acceptance remains open.|
|Features/structures|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96): Trees/deposits implemented; general structure spacing/ownership pipeline absent.|
|Performance/threading|3|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L391): Historical accepted native warm fill14.45–14.99ms exceeds5ms. No fresh timing acceptance; synchronous fill and synchronized region construction remain.|
|Determinism/stability|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/engine-review-2026-10-24/REVIEW.md#L1): Fresh core replay/seam suites pass; native save/reopen is historical, not rerun after this essential build failure. Cross-machine and long soak remain open.|
|Architecture/code quality|6|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/build.gradle#L97): Core/module boundaries checked; materialization/revision stages remain combined.|
|Extensibility/data-driven|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java#L54): Saved profiles/deposits; density graph library has no active gameplay integration.|
|Tests/benchmarks/docs|7|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/engine-review-2026-10-24/REVIEW.md#L1): Fresh full build fails the asset gate; independent core and boundary checks do not establish packaged gameplay or Windows acceptance.|
|Polish/config/tooling|4|[Evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/ACTIVE-ENGINE-AUDIT.md#L1): Water/LOD/transparency/recovery issues, editor and latest-version adapter remain open.|

## Isolation and performed checks

Full available Java25/Gradle build requested with `--rerun-tasks`, exclusive audit cache moved from completed prior run, new checkout/output paths and single-use daemon. Source-identity.json verifies 834 immutable build inputs against pinned Git blobs, with no mismatches or missing required files. The current documented restoration helper verifies all five pinned tool archives. Prior auditor generated build outputs were reclaimed only in obsolete completed audit directories; reports and exported files retained. No builder workspace, lease, process, schedule, state file or implementation was changed. No active timing reservation observed; no performance benchmark was run. The independent core compilation uses its own output and all tests run with `-Xverify:all`. Full raw pass/fail markers are in executed-checks.json; build-result.json and module-boundary-result.json are authoritative, rather than mere task presence.

Build uses the Windows platform payload; this Linux host cannot provide Windows/AMD8060S acceptance. Archive diagnostic records, when present, validate CRCs, class identity, fixtures exclusion, Themes resources, module notices and platform bytes only. They do not override the failed build. Any diagnostic export is rehashed and compared with completed producer bytes; it is not uploaded for installation.

## Performance, defects and next priorities

No fresh performance measurement. Accepted historical native warm fill means 14.45–14.99 ms exclude lighting, IO and other host costs, already above the 5 ms goal. Stage2 component results are not whole-engine acceptance. See the [stage2 evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/engine-stage2-2026-10-24/README.md#L1) and [R62 constructor evidence](https://github.com/Shnewbs/Deep-Primal/blob/d8a38d9067bf1ed6557957ec391a718cccd48e5b/docs/validation/engine-continuation/density-metadata-2026-10-24/README.md#L1). Historical 906 coarse occupancy disagreements remain unresolved. V8 remains unregistered; surface snapshot APIs are not saved gameplay integration. New masked sprite/shader/mipmap behavior has no fresh graphics acceptance from this run.

Priorities: reconcile validation with approved current asset identities, then rerun full delivery gates; validate ground shaders/lighting/mipmaps/reload in terrain scenes on OpenGL/Vulkan; complete V8 saved-profile, native parity/topology and routed hydrology; reduce density latency and scheduler contention; weighted biome transitions and feature ownership; resolve water/LOD temporal/coverage defects and perform owner Windows/GPU validation. Frozen V1–V7, quantities, player edits and modular rendering are preserved.

Not run after essential build failure: packaged native Minecraft save/reopen/shutdown, Windows execution, actual GL/Vulkan shader and visual/resource-reload/terrain-world checks, fresh performance, cross-machine replay, multiplayer/travel/edit/weather/unloaded long soak, V8 saved-profile migration and 26.3 port acceptance.

Last verified user JAR: source `5c54904a1316b27d6512aa219f938867d31a49f5`, [download](https://chatgpt.com/library?file_id=libfile_c282e4a126688191926a0876c4959747&account_id=personal). This older artifact is not the current failed build. No GitHub Release or CurseForge publication. Auditor remains enabled; no fresh >=8.0 audit is demonstrated. Builder state untouched.

## Current receipts

### source-identity.json

```json
{
  "source_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b",
  "verified_inputs": 834,
  "mismatches": [],
  "missing_build_inputs": []
}
```

### lease.json

```json
{
  "role": "auditor",
  "run": "20261025T0555",
  "source_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b",
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
      "elapsed_seconds": 0.09
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
  "source_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b"
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
  "log_sha256": "d2df252f38bd981a150cfff8548e4b360d85783fc56599cafb4e099e2b57a439"
}
```

### resource-manifest-preflight.json

```json
{
  "pinned_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b",
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
  "source_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b",
  "artifact": "/workspace/scratch/1a7cb29b04a2/audit-d8a38d9-20261025T0555/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar",
  "sha256": "dcc0b429461c18ae9b1cc080f76f8db39c36b7fba0096063d90db678e175a815",
  "size": 125120006,
  "archives": [
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar",
      "entries": 506
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Core-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9.jar",
      "entries": 115
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-GraphicsSettings-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9.jar",
      "entries": 57
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-LOD-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9.jar",
      "entries": 31
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Performance-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9.jar",
      "entries": 24
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Themes-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9.jar",
      "entries": 2873
    },
    {
      "archive": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar!META-INF/jarjar/com.shnewbs.deepprimal.DeepPrimal-Vulkan-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-windows.jar",
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
  "sha256": "dcc0b429461c18ae9b1cc080f76f8db39c36b7fba0096063d90db678e175a815",
  "colormap_and_water_palette_hash_matches": 43,
  "approved_ground_texture_hash_matches": 11
}
```

### delivery.json

```json
{
  "accepted": false,
  "diagnostic_archive_only": true,
  "source_sha": "d8a38d9067bf1ed6557957ec391a718cccd48e5b",
  "artifact": "DeepPrimal-26.1.2-0.0.1-alpha.29-dev.3-audit-20261025T0555-d8a38d9-vulkan-windows.jar",
  "sha256": "dcc0b429461c18ae9b1cc080f76f8db39c36b7fba0096063d90db678e175a815",
  "size": 125120006,
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

