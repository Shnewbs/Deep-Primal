# Dev63 — V8 saved V7 climate/hydrology reference bridge

**Source-only candidate:** 0.0.1-alpha.29-dev.63-climate-bridge.1. This is not a native V8 registration, playable JAR or release.

## Consumed independent audit

Latest completed independent report: docs/engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md; pinned source 055d78f8535d5380ac99e2dd0fc43da49ae5ba89. Relief V4 preserved sampled numerical sea-plane classification, but V8 had no bound actual ocean/routed-water producer. The historical ten-category grade remains 50/100 (5.0/10), not a new grade. Water source ownership was expired; renderer files are separately owned and were not touched.

## Actual source implementation

New V8SavedClimateSource implements the already existing V8EcotoneColumnPlan.ClimateSource interface accepted by DensityChunkMaterializer.planSavedEcotone and fillSavedEcotone. It makes a read-only V7 reference source from an *explicitly passed saved* TerrainProfile/options, saved world seed and channel revision. It refuses mismatched seeds, missing profiles, other terrain revisions, missing/nonfrozen V8 revision-3 surface policy and channel revisions outside 0..5. The caller must obtain both profiles from the same saved world; this class cannot authenticate externally supplied data.

The private, pinned V7 TerrainEngine supplies seeded absolute-coordinate temperature, rainfall, slope and palette patch noise (scale 1100, salt 3130). The climate altitude input is the proposed V8 top Y. The protection domain comes from the V7 continental ocean mask first, then *any* wet reference cell (rivers, standing lakes), then land. The unchanged V8 prewrite planner excludes protected columns from soil/snow material placement. Observations synchronize on the adapter to avoid concurrent access to mutable V7 cache structures; source throughput/memory tails have not been measured and new independent workers should use separate instances.

This is an opt-in reference bridge for experimental new V8 authoring, NOT an authoritative V8 ocean geometry solution. It does not register the native generator, mutate existing snapshots, apply automatic profile upgrades, create liquids or change finite-water chemistry. V1–V7 geography, old V8 graphs/profiles, approved art, player edits and renderer files were preserved.

## Reproducible local checks

Run python3 tools/test_v8_saved_climate_source.py (also checked under python3 -OO). In this isolated Linux host: OpenJDK/javac 21.0.12.1; strict javac --release 21 -Xlint:all -Werror and java -ea -Xverify:all. Both Python modes passed exactly **130,377 explicit Java checks**, with 18,432 signed-coordinate V7-mocked climate queries across six channel revisions; 15,864 land, 1,482 river/lake, 1,086 ocean observations and 564 dedicated still-lake exclusions; and 12,288 parallel/reversed queries. Saved-version/seed/options and invalid-origin guards rejected deliberately malformed inputs.

The runner compiled the unchanged production V8EcotoneColumnPlan.java (Git blob 89b9e298b7d246fa1ba40990138c7a62c0fd1a7c) alongside new code and isolated **core/Minecraft API doubles**. Its prewrite mask excluded 46 modeled water columns and admitted 210 land columns. An intentionally broken water-domain implementation compiled successfully but was rejected by the regression. The first test run surfaced an ArithmeticException in the negative overflow fixture, which was correctly added to the test's expected failure types before the two accepted runs.

Verified SHA256 of actual tested files:
- V8SavedClimateSource.java: 3f1d536bdc72e5abcbb9c23750064fac6304d46c41421adc5ec63f9c1d5eb018
- V8SavedClimateSourceChecks.java: 9a57b3f94e40e8c021cfb292c6ea5dea85264049236360b8f01a598949cae65f
- test_v8_saved_climate_source.py: 2aff03bd2412c07b8029d34fe073624059072fc58a58e8e43145c45bd4a25363

The full project and the pinned root validator could not be materialized on this host; docs/ENGINE-WORK-STATE.json was left unchanged. Java25, full Gradle/NeoForge, real TerrainEngine/native save-reopen, actual V8 ocean masks, old/new chunk borders, owner GPU, finite-water long soak, constituent ledgers, multiplayer, waterfalls and measured performance were **not run**. No build or JAR publication attempted under the October 27 owner-build directive. The last owner-confirmed playable build is dev53.

## Next engineering priority

Persist an explicitly new V8 climate/hydrology producer revision in saved world metadata, validate real V7-versus-V8 ocean/river masks before native codec activation, verify save/reopen/edit borders and complete finite-water single-owner unload/restart conservation. The builder stop gate remains a fresh independent average strictly greater than 8.5/10 AND all recorded water-engine acceptance gates.
