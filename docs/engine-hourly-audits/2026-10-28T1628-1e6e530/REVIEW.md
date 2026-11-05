# Independent engine audit — 2026-10-28 16:28 UTC
Pinned main source [1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0](https://github.com/Shnewbs/Deep-Primal/commit/1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0); Dev58 version 0.0.1-alpha.29-dev.58-ecotone-native.1. Auditor read-only for production and other roles, no new JAR per 2026-10-27 owner-build override.

## Fresh independent evidence
- Reconstructed exact production V8EcotoneColumnPlan.java (3,876 bytes) and verified Git blob 89b9e298b7d246fa1ba40990138c7a62c0fd1a7c.
- Compiled actual production class with isolated lightweight API doubles using Java 21, --release 21 -Xlint:all -Werror. All **469,256** independent assertions passed over 512 randomized tests: signed absolute coordinates, river/ocean exclusion, soil-depth/original-height bounds, exact cover oracle and repeatability. Did not execute Minecraft or real Mojang APIs.
- Negative coordinate mutant compiled, then failed at assertion 7 as intended.
- Source finding [DensityChunkMaterializer.java L209-L218](https://github.com/Shnewbs/Deep-Primal/blob/1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L209-L218): once snow material is cached, a second snowPatch column enters the `else switch(cover)` and unnecessarily resolves grass/podzol/dirt. Source-extracted Java21 demonstration got 1 snow + 1 redundant grass lookup. Low-severity dependency/efficiency bug, not a reproduced native failure. Builder fix: nest snow lookup under `if (snowPatch) { ... } else switch`.
- Dev58 Builder reported 14,150,419 focused Java21 assertions; renderer same-ring jam reported 144,361 assertions and ~44.9% lower matched p50 allocation, ~17–23% lower CPU p50. **These are producer evidence, not auditor reruns or GPU FPS proof.**
- Eight commits since prior full audit pin cec6143; changed only V8 opt-in path, LOD disk same-ring selection, version, and evidence. No known V1–V7, water or player-edit code changes from this commit range.
- The root ENGINE-WORK-STATE.json still reports dev53 from 12:04 UTC; separate TERRAIN-BLENDING-WORK-STATE.json records dev58 completed at 16:17 UTC. Do not interpret stale root state as active ownership.

## Complete ten-category grade carried, NOT a fresh whole-engine regrade
| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/docs | 7 |
| Polish/tooling | 4 |
| **Total / Tier 2** | **50/100 = 5.0/10** |

Strictly >8.5/10 target unmet. Unused, staged or unregistered V8 previews earn no shipped gameplay score increase.

## Unrun / limitations
JDK25 not available in this workspace or matched in Library title search; Linux Java21 used. Full Gradle/NeoForge, current JAR packaging/outer and nested CRCs/class identities, native JSON/NBT and save/reopen, real climate provider and independent pre-generation baseColumn, finite-water unloading/chemistry, Windows/AMD8060S Vulkan/OpenGL and GPU/water/LOD/FSR 3/4 p95/p99: **not run**. Direct GitHub clone DNS blocked; connector source read succeeded. No JAR or release qualified, saved, removed or published. October 27 override assigns full builds to owner.

Independent source/Java tests, logs, SHA and reproduction instructions saved in CRC-verified Library ZIP `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1628_1e6e530_SourceEvidence.zip`, 10,620 bytes, SHA256 fd c1 fd 15 (full receipt value below). Auditor did not touch builder/renderer checkouts, caches, leases, timing slots or workloads.

## Next
Native V8 saved-authoritative climate/codec and before-generation column parity, ecotone relief/terrain without breaking frozen V1–V7, water volume/salt/sediment unload/edit/reopen, plus user-hardware GPU/camera/travel/moving-water performance. Correct snow material preflight when Builder scope permits. Observational audit is not a builder stop gate.