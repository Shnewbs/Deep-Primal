# Independent Deep Primal engine audit — 2026-10-28T1030Z

**Pinned main at start:** [`10cb0c6bfcc32492e573aa8c282da75d13864e91`](https://github.com/Shnewbs/Deep-Primal/commit/10cb0c6bfcc32492e573aa8c282da75d13864e91), version `0.0.1-alpha.29-dev.51-v8preflight.1`. Latest production implementation [Dev51 `61369484aff15c0a0d7130ea69c397c5819a2341`](https://github.com/Shnewbs/Deep-Primal/commit/61369484aff15c0a0d7130ea69c397c5819a2341). No production files modified by the auditor.

## Scope/coordination

Read `docs/ENGINE-WORK-STATE.json`, `docs/ENGINE-BUILD-AUDIT-HANDOFF.md`, `docs/ACTIVE-ENGINE-AUDIT.md`, `docs/ENGINE-BUILDER-AUDITOR.md`, `docs/ENGINE-HOURLY-BUILDS.md`, the accepted ten-category review and preceding [09:33 independent audit](../2026-10-28T0933-4b61329/REVIEW.md). The builder's source-only cycle and benchmark reservation were released at the pinned source. This run used independent Linux directories, not the builder's or renderer's checkout/cache/output. Nothing staged was promoted.

## Changed code and fresh checks

- [V8 planned ground materialization](https://github.com/Shnewbs/Deep-Primal/blob/10cb0c6bfcc32492e573aa8c282da75d13864e91/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L95-L202), Git blob `58c4b7d0f04435620858c1eef919d50ec171f15f`: checks planned occupancy and surface/subsurface material before the first block write, defers final heightmap priming. Still explicit-call-only, unregistered native V8.
- [LOD signed filename scanner](https://github.com/Shnewbs/Deep-Primal/blob/10cb0c6bfcc32492e573aa8c282da75d13864e91/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L242-L311), Git blob `4452f36a0b0162b8194729cc3b97a47f8172228d`: parses canonical `x_z.plod` without substrings/coordinate arrays. Renderer owner measured 22.3–24.3% lower *scanner allocation*, but slower CPU median in both matched observations. No FPS credit.
- Verified 11 additional V8/snapshot and LOD service Git blobs against the pinned repository. Compiled source against separate Minecraft/Mojang API doubles with **OpenJDK 21.0.12.1** (`--release 21 -Xlint:all -Werror`) and executed `-ea -Xverify:all`: **13,766,368 V8**, **26,286 existing LOD**, **12,440 new independent filename-order parity**, **11 new Dev51 failure/heightmap** assertions all passed = **13,805,105 fresh assertions**.
- The independent filename test used **1,856 real files**, 1,219 canonical names, five cameras and five capacities against an independent BigInteger ranking oracle. The preflight test rejected a late null material and an invalid planner budget with **zero block/heightmap mutations**. In API doubles the painted path avoided **7,168 redundant heightmap update calls** versus raw planning; **not a runtime benchmark or FPS improvement**.
- **Remaining conditional failure-atomicity risk:** a simulated exception at the 1,000th native `setBlockState` call left **999 block writes** and bypassed final heightmap priming. This is an intentionally injected **Java double** fault, **not a confirmed native Minecraft defect**. Prewrite validation is improved; postwrite rollback or caller-abandonment semantics still need native tests.

## Ten equally weighted categories — carried, NOT freshly regraded

| Terrain | Biomes | Hydrology/caves | Features | Performance | Determinism | Architecture | Extensibility | Testing/docs | Polish |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 | 4 | 6 | 4 | 3 | 7 | 6 | 4 | 7 | 4 |

**Overall 50/100 = 5.0/10 (Tier 2), carried from [last accepted complete review](../../engine-review-2026-10-24/REVIEW.md).** No complete engine regrade was possible. Target is strictly **>8.5/10**, not met. Staged prototypes/unused libraries earn no gameplay credit.

## Unrun gates, delivery, priorities

The **latest October 27 owner override assigns full Gradle/Minecraft/GPU builds to the owner**. The owner-provided Java25 toolchain/archive were absent in this isolated workspace. **No Java25/Gradle, native Minecraft/Mojang codec, saved-world reopen, V1–V7 native replay, Windows/AMD8060S Vulkan/OpenGL, package CRC/class identity, water conservation long soak, GPU frame-time, qualified new JAR or GitHub/CurseForge release ran.** No old JAR was relabeled or retained/deleted here.

Next: owner-native V8 material/heightmap/lighting/preflight exception and saved snapshot tests, preserving frozen V1–V7; water mass/salt/sediment across callback/reopen/unload; and actual owner-hardware Vulkan/LOD/waterfall visual/performance gates. The auditor does not pause or take a lease from the Builder. Its 5.0 carried grade does not stop the existing task.

Independent evidence ZIP (conversation artifact): `DeepPrimal_Audit_20261028T1030_10cb0c6_SourceEvidence.zip`, **85,286 bytes**, **SHA256 `7b19d4f82550237f833df8a0f4839b203afbcfbd1becf4bcb6565157354809eb`**, all archive CRC entries passed.
