# Independent Deep Primal engine audit — 2026-10-27T18:34Z

**Observed main source SHA:** `ed919c35a933ae54b0b8f9bee5c910d71a8be953`; version `0.0.1-alpha.29-dev.34-conservativewrites.1`. Audited a committed snapshot, without modifying production, the separate engine builder's lease/output/checkout, or the renderer schedule. No accepted staged geography/art overlay was promoted.

## Strict ten-category engine rubric

The **50/100 (5.0/10), Tier 2** total below is **CARRIED** from [original full review](../../engine-review-2026-10-24/REVIEW.md) and the latest completed [17:34 audit](../2026-10-27T1734-83c099f/REVIEW.md). This is **not** a fresh complete GPU/native/whole-engine regrade and cannot satisfy the strictly-over-8.5 stopping threshold.

| Category | Score /10 |
|---|---:|
| Terrain realism/variety | 5 |
| Biome blending | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/benchmarks/docs | 7 |
| Polish/config/tooling | 4 |
| **Total** | **50/100** |

See the original rubric for pinned terrain, biome, features, performance, determinism, architecture and config evidence. Current dev34 new water boundaries are [WaterRuntime L137–171 and L207–239](https://github.com/Shnewbs/Deep-Primal/blob/ed919c35a933ae54b0b8f9bee5c910d71a8be953/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L137-L239), [HydrologyRuntime L72–80](https://github.com/Shnewbs/Deep-Primal/blob/ed919c35a933ae54b0b8f9bee5c910d71a8be953/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L72-L80), and the [builder's scoped receipt](../../validation/engine-continuation/loaded-write-admission-2026-10-27/README.md).

## New independent evidence

- **JDK25 PASS**: Temurin 25.0.5+8; `javac --release 25` and `java -Xverify:all` on the *exact* `WaterTransport.java` Git blob `0ede1c7312e40ecf063b6cc86e5a690cce190443` with an **audit-only** `TerrainNoise.lerp` dependency stub. `AUDITOR_JAVA25_WATER_CORE_PASS checks=1198395 admitted=1632 rejected=1112775 edgeMaxDiff=0`. Confirms core integer transfer, proposal admission, extreme pressure Y arithmetic and adjacent interpolated edge continuity; **not production Minecraft/NeoForge/native, chemistry, or graphics acceptance**.
- **Historical JAR byte integrity PASS, not current source**: previously qualified `0.0.1-alpha.29-dev.32-upscaling.1` Windows Vulkan/OpenGL fallback JAR, 125,383,020 bytes, SHA256 `04a7a8bf6f88c9b69e3b6cba2c24e6030e1d5e8c38192ee14b8c0e8e16a6d083`, outer+six nested archive CRCs pass. Dev34 functionality is not in this old JAR.
- **Current JAR BLOCKED**: independent directory has Java25 but not a complete checkout/wrapper/Gradle/dependencies. `git ls-remote https://github.com/Shnewbs/Deep-Primal.git refs/heads/main` fails DNS `Could not resolve host: github.com`, exit128. No Gradle/native/current package/source-bytecode identity, Windows/AMD8060S, OpenGL/Vulkan real pixels, saves/reopen/soak or performance-tail tests ran. **No current JAR produced or uploaded.**
- **Additional source-only risk**: [HydrologyRuntime.place L160–163](https://github.com/Shnewbs/Deep-Primal/blob/ed919c35a933ae54b0b8f9bee5c910d71a8be953/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L160-L163) calls `getBlockState` after `hasChunkAt` without an explicit Y/world-bounds guard, unlike common `write`. No native reproduction; verify height semantics and actual behavior before classifying as defect. The new rollback-hard-failure path must undergo native fault injection.

Full qualified **read-only** analysis and focused evidence are saved to the user's Library:
`/Deep Primal/DeepPrimal_Audit_20261027T1834_ed919c35_REVIEW.md`, 
`/Deep Primal/DeepPrimal_Independent_Audit_20261027T1834_ed919c35_Evidence.zip`.

**Next:** restore an isolated complete pinned build, qualify dev34 Java25 Gradle+native water loaded/unloaded/save/reopen/rollback fault-injection; only then qualify a *new* source-matched JAR. No source changes, no releases, no builder stop, no user JAR retention deletion because no new JAR was qualified.
