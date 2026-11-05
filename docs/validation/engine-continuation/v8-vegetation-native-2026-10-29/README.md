# Dev86 — explicit saved V8-r6 ecotone vegetation materializer

## Scope and provenance
- Source candidate: `0.0.1-alpha.29-dev.86-v8-vegetation.1`.
- Started on current main with bounded `docs/TERRAIN-BLENDING-WORK-STATE.json` scope, after the independent `docs/engine-hourly-audits/2026-10-29T1932-be9cde82/REVIEW.md` pinned at `be9cde8274a430c39eaa481d010b9c09d3466a3b`.
- Last accepted full audit: 50/100 (5.0/10). The latest auditor's focused Java25 Dev85 checks do not regrade the entire engine.
- No renderer-owner, current finite-water runtime, vanilla V1-V7, approved textures, old V8 profile, or player-edit files were intentionally changed.

## Source implementation
- Added mandatory `V8VegetationPolicy` with strict versioned saved `vegetation_policy` fields: version, anchor spacing (4/8/16 blocks), and bounded density scale. New `V8GeneratorSnapshot.VEGETATION_VERSION=6` is created only by explicit `withVegetationPolicy` from an existing saved revision 5; all earlier six-to-ten-field encodings remain their original dispatch paths, with hard legacy defaults unchanged.
- `V8VegetationColumnPlan` consumes *the same* frozen 16x16 column preflight as V8 ecotone material placement. It uses continuous precomputed `vegetationPatch`, grass/podzol weights and deterministic seeded absolute-coordinate lattice cell anchors. No per-call RNG, chunk-local seeds, winner-biome toggles, duplicate placements across signed chunk edges, new water, or new art.
- `DensityChunkMaterializer.fillSavedEcotoneVegetated` is a **new explicit-call-only** r6 method. It requires saved V8 water-domain authority, resolves plants and registry states before native writes, and writes plants inside the existing complete prewrite snapshot/rollback and heightmap transaction alongside density/soil. No mature tree system is activated. The previous `fillSavedEcotone` and native V1–V7 paths stay on a no-vegetation branch.
- New `planSavedEcotoneVegetated` and `baseColumnVegetated` provide a matching no-write planned-feature projection. Existing V8 routed-water fingerprint verification and climate admission accept r6 as an explicitly authored extension of r5; they do not interpret or change physical water stages.

## Executed source-only validation
On Linux OpenJDK/javac **21.0.12.1**: `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. The isolated `tools/test_v8_vegetation_columns.py` compiles the **exact production vegetation planner** with clearly labeled test-only cover/column/policy doubles. Two positive processes per runner mode (normal Python and Python `-OO`) each returned:
`V8_VEGETATION_PLAN_PASS assertions=162732 replay=true signed=true parallel=true`.
Four compiled broken controls per mode were rejected by actual `AssertionError` (signed-negative division, missing snow guard, duplicated count, ignored density thinning). Across four positives: **650,928** assertions; **8/8** negative controls rejected. Signed chunk coordinates, canonical 4/8/16-block anchors, deterministic replay, reverse/parallel execution, snow/wet/bedrock/high-ceiling exclusions, and zero/half/full density were covered.

The other modified production classes and the Mojang/NeoForge materializer were **not fully compiled or executed in this source-only harness**, and no actual native saved V8 profile round-trip or block/chunk write occurred. Static source/strict wire guards are not a replacement for real DFU codec tests. A separate actual complete pinned Java25/Gradle and native Minecraft test must validate this revision before a playable JAR can be considered.

## Open acceptance gates
- Native old/new-generated border handling, native chunk/base-column/heightmap bit parity, native crash/restart/save and player-edit protection.
- V8 authoritative ocean/rivers/lakes, global discharge and constituent conservation; loaded/unloaded transfer ledger coupling; water surfaces over natural cliffs.
- Actual multi-seed climate ecotone and landform relief visual transects, plant density/spacing distribution, native timings/allocation and Vulkan/OpenGL GPU acceptance.
- The owner is the full-build/JAR publisher and confirms dev53 as last playable. No dev86 JAR or release is claimed. Original 5.0/10 rubric remains; completion is strictly above 8.5 with water gates passed.

## Reproduce
Run `python3 tools/test_v8_vegetation_columns.py` from checkout root; repeat `python3 -OO tools/test_v8_vegetation_columns.py`. Source-only tests do not run automatically through CoreChecks. Results are in this folder's `results.json`.
