# Dev125+ independent audit — 2026-10-31T09:28Z

Pinned main `97c743048dd37cf4b4b763bdef69f28990bd0192`, version `0.0.1-alpha.29-dev.125-v8-materializer-coordinate.1`.

## Confirmed defect and fix
`DensityChunkMaterializer.fill(DensityGraph, ChunkAccess)` (pinned lines 45–66) and its profile preview caller (lines 22–25) lacked the Dev125 signed ChunkPos guard. A chunk at X=134217728 aliases the origin to -2147483648; the direct preview sampled and wrote the wrong world coordinates. Added the existing `requireRepresentableChunk(chunk)` before the preview's sampling and setters. Expanded `tools/test_v8_materializer_coordinates.py` to check this fourth direct V8 path. V1–V7, saved profiles, finite-water, graphics, LOD, Vulkan and art unchanged.

## Evidence
Owner Library Temurin Java25 25.0.5+8 restored in isolated Linux audit directory. Java25 strict compilation (`--release 25 -Xlint:all -Werror`), verified runtime and classfile major 69 pass. Java21 release/runtime check also passes. Independent API-double reproduction of the direct fill body: 4 invalid signed chunk pairs sampled/wrote before fix; all 4 reject before sampling/setters afterward. Four valid boundary pairs still sample once/write 256 cells/prime once. Additional 100,000 seeded coordinate cases match a 64-bit oracle. Candidate production blob `0a2fb2fd93396760a2ba52731abe72aa2d174c05`; runner blob `1d6a93de29bf96c8780a822dcb0ca2a630fcfd4f`; all four guard callsite checks pass. Independent harness SHA256 `08135efc7f17855d0fec8f41ec56de9af2262e5df77e2b1b0af118bd6e68e2a5`.

## Omitted
Git CLI cannot resolve github.com; complete isolated checkout and Gradle dependencies unavailable. No full Gradle/NeoForge build, JAR/package CRC/class identity, native save/reopen/shutdown, finite-water/terrain/LOD benchmark, Windows AMD8060S Vulkan/OpenGL, or NeoForge26.3/26.4 verification. October27 owner-build override respected. **No JAR created or deleted; no GitHub Actions.**

## Grade and next
Last accepted whole-engine **50/100, 5.0/10, Tier 2** carried forward, not a fresh regrade. Terrain5, biomes4, hydrology/caves6, features4, performance3, determinism7, architecture6, extensibility4, tests/docs7, polish/tooling4. Strictly >8.5 remains unmet. Next native atomic chunk/stamp save/recovery, V8 border reopen, finite-water ledger, full Java25 package and Windows GPU/performance acceptance. Builder lease, checkout, outputs and timed measurements untouched.
