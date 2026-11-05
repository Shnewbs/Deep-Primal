# Independent Deep Primal audit — 2026-10-28 02:33 UTC

**Pinned committed `main`:** `28051408b079d3f378083554a6fb789ee43e5124`; **version:** `0.0.1-alpha.29-dev.43-v8partition.1`. This is an independent source/fixture review under the owner's October 27 build override. No engine or renderer production files, builder lease, cache, toolchain, checkout or schedule were modified. No new development JAR or release was produced.

## Findings and evidence

1. **Dev43 V8 partition is bounded but not live worldgen.** [DensityVolumePlan.java L25–116](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityVolumePlan.java#L25-L116) emits disjoint SOLID/EMPTY/UNCERTAIN voxel tiles, fails open after at most 256 classifications and bounds input dimensions at 64 cells/axis. [DensityVolumeBounds.java L36–169](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityVolumeBounds.java#L36-L169) computes coordinate dependencies and interval certificates. Pinned code search finds `DensityVolumePlan.plan(` only in [registered tests](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/src/coreChecks/java/com/shnewbs/deepprimal/core/density/DensityVolumePlanChecks.java), not the native chunk generator. It earns **no worldgen quality or performance score** pending actual Java25 graph, topology and native integration.
2. **Independent Java21 property checks passed, with exact scope.** In a fresh isolated directory, Git-object hashes matched the committed Plan `8b7f764f52735ea9c7aa9496037b7a2e61de741b`, Bounds `a8f811a2676ae74d93b52506ed747e88f0d354a4` and registered checks `ec3cc5b2a042bbcbfa614cf9ca03b68e0c8ee4f5`. `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed **158,901 registered assertions**. The separate auditor-authored property matrix passed **1,663,078 assertions** over 14 graph shapes, 6,720 plans, 429,557 exact integer-voxel comparisons and additional continuous fractional samples within certified tiles. 288,996 sampled voxels were certified, 140,561 fell back as uncertain, and the maximum classification budget observed was 256. No sampled sign error or overlapping/missing tile was observed. **Graph/noise inputs were explicit API-compatible test doubles**, not real compiled `DensityGraph` or `SeededNoise3D`. These finite tests are not native correctness or speed proof.
3. **Renderer change is measurement infrastructure, not FPS acceptance.** The pinned [LodGpuUploadProbe.java L1–55](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodGpuUploadProbe.java#L1-L55) collects rolling accepted part CPU costs/byte sizes and failed/over-budget counters when telemetry is enabled, wired at [VoxelLodRenderer.java L664–697](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L664-L697). Verified source blobs: probe `3320dbc1833aa64796caf8e8bf3e89a89bc0b969`, original RollingTiming `0be7512957537596295ba204f8f1dfc176aa686f`, test `26b8aeb2413b1cffe0164a8f35e4d4b41adbe791`. Independent Java21 recompilation/rerun passed **40,551 assertions** over 77,100 modeled attempts (72,498 accepted / 4,602 failed). **No real GPU submissions or CPU/GPU frame benchmarks ran.**
4. **Water postwrite guards remain native-unqualified.** The previous [01:34 audit](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/docs/engine-hourly-audits/2026-10-28T0134-4fc83e6/REVIEW.md) found all seven callers structurally guarded. Real reentrant native callback exceptions, same-state ABA replacement, loaded/unloaded ownership, water/salt/sediment conservation and save/reopen remain open; this run made no new native claim.

## Ten-category grade (equal weights; historical carried score, not a fresh full regrade)

| Category | Score /10 | Current limiting evidence |
|---|---:|---|
| Terrain realism/variety | 5 | Experimental V8 planner not integrated into native generator |
| Biome blending/coherence | 4 | Weighted ecotone/material/vegetation acceptance absent |
| Hydrology/caves | 6 | Source-level postwrite guards; native callbacks and world ledger unaccepted |
| Structures/features | 4 | General structure spacing/exclusion and chunk ownership incomplete |
| Performance/threading | 3 | Historical native warm 14.45–14.99 ms fill, vs goal <5 ms; no fresh native distribution |
| Determinism/stability | 7 | Historical frozen V1–V7 replay; no new cross-platform or long soak |
| Architecture/code | 6 | Modular components, but V8/native lifecycle unintegrated |
| Extensibility | 4 | V8 profile and general biome/feature API not live |
| Testing/benchmarks/docs | 7 | Fresh focused Java21 model checks, not full Java25/native/GPU |
| Polish/config/tooling | 4 | Cliff water flicker, LOD coverage and Windows GPU proof open |
| **Overall** | **50/100 = 5.0/10; Tier 2** | **Carried from [complete 2026-10-24 rubric](https://github.com/Shnewbs/Deep-Primal/blob/28051408b079d3f378083554a6fb789ee43e5124/docs/engine-review-2026-10-24/REVIEW.md). Strict target >8.5 is NOT met.** |

## Audit gates and continuation

**Executed:** authenticated GitHub main pin; read work-state, handoffs, builder/auditor contract and latest completed audits; committed-source and test-blob verification; independent isolated Java21 compilation and V8/model test execution; separate renderer probe test execution; archive CRC/hash verified. Host: Linux x86_64 AMD EPYC 9V74, OpenJDK/javac 21.0.12.1.

**Not run:** Java25 (Temurin 25.0.5+8 installation/owner archive absent from this run); Java25 Gradle 9.2.1, whole package and native JAR/CRC/class-origin tests; real `DensityGraph`/`SeededNoise3D` compile; Minecraft/NeoForge V8 block/heightmap/save-reopen, finite-water callback and ledger acceptance; Windows AMD8060S Vulkan/OpenGL, GPU timestamps, FPS/VRAM and native p50/p95/p99. Under the **latest owner override, full builds are owner-run**. No JAR, hosted GitHub Actions, GitHub release, CurseForge publication or three-JAR retention mutation occurred. Prior dev31 is historical, not a current build.

**Next:** actual Java25 graph + native V8 chunk topology/replay; seven postwrite operations with real callback/ABA/unload/save/constituent conservation; then matched real-water/LOD travel/edit/resize, and CPU/GPU p50/p95/p99 on owner AMD Vulkan and fallback OpenGL. The independent auditor remains nonblocking. No score change until a new complete ten-category audit exceeds **8.5/10** with measured evidence.

**Local compact source-only evidence:** `DeepPrimal_Audit_20261028T0233_2805140_SourceEvidence.zip`; 21,850 bytes; SHA-256 `164bbcd1e1d3282b9573c003f48cfaeec765e63481c1a93b94247674fe2c13f9`; 19 CRC-valid entries. Not a game build.
