# Independent Dev65 engine audit — 2026-10-28 23:31Z

**Pinned immutable source:** [`d99837bb76ddfa6fd56cb5f133eb5021dde8c131`](https://github.com/Shnewbs/Deep-Primal/commit/d99837bb76ddfa6fd56cb5f133eb5021dde8c131); Builder version `0.0.1-alpha.29-dev.65-authoring-guard.1`. Focused independent Java25 source audit; no production edits or new JAR.

## Source deltas since preceding independent Dev64 review

Seven commits ahead of `a0d68a1d916e3cc6f71de827de2cf3e7b34d0da0`. Dev65 [V8GeneratorSnapshot.java lines 67–79](https://github.com/Shnewbs/Deep-Primal/blob/d99837bb76ddfa6fd56cb5f133eb5021dde8c131/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L67-L79) rejects authoring a revision-2 blend from r3/r4 snapshots so saved surface/climate provenance is not silently lost. Renderer's [BoundedIndexedDrawBatches.java lines 23–40 and 53–103](https://github.com/Shnewbs/Deep-Primal/blob/d99837bb76ddfa6fd56cb5f133eb5021dde8c131/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/BoundedIndexedDrawBatches.java#L53-L103) enforces hard count limits on synchronous API scratch, not GPU multidraw.

The scoped terrain lease is checkpointed/released at 23:11:30Z; root ENGINE-WORK-STATE.json still describes Dev53 and must not be mistaken for live Dev65 state. No builder/renderer/water checkout, lease, cache, production file or benchmark touched.

## This audit's independent evidence

Host Linux, **Temurin Java and javac 25.0.5+8 executed successfully**, archive tar.gz integrity passed (SHA256 `68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb`). Strict `javac --release 25 -Xlint:all -Werror`, `java -ea -Xverify:all` PASS.

- Exact pinned production `BoundedIndexedDrawBatches.java` bytes independently verified against Git blob `0ba6b843b105bde21d9d6df8efe069450b42f8a8`; **5,172,908 assertions PASS**, 5,000 randomized batch scenarios, ordered records, overflow, listIterator/subList, cleanup/reuse/reset.
- Dev65 `withBlendPolicy` method transcribed from lines 72–79 into a Java25 minimal test-double contract: **72,000 assertions PASS**, 8,000 randomized seeds (including signed extrema), r1/r2 legal and r3/r4 protected. This does **not** independently compile the complete production V8 snapshot or test real codecs.
- Mutated LOD helper without `add()` growth guard compiled but **failed expected negative check #1193**. Total independent passing assertions **5,244,908**.
- Reproducible 17-entry CRC/manifest SHA256-validated evidence archive saved in Library at `/Deep Primal/Independent Audits/DeepPrimal_Dev65_IndependentAudit_20261028T2331_d99837b_Java25Evidence.zip` (11,028 bytes; SHA256 `4ed2ea1dc52962868b22ea85775b4a17a637d45b2cd145e05f0c146c444178e6`).

## Ten equal-weight category grades (historical full baseline carried, NOT fresh whole-engine regrade)

| Terrain | Biomes | Hydrology/caves | Features | Performance | Determinism | Architecture | Extensibility | Tests/docs | Polish |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 5 | 4 | 6 | 4 | 3 | 7 | 6 | 4 | 7 | 4 |

**50/100, average 5.0/10, Tier 2 (carried baseline)**. Greater-than-8.5 threshold unmet; fixture assertions do not earn native gameplay credit.

## Unrun gates / next priorities

**No JAR produced** in accordance with latest Oct27 owner override assigning full Gradle/Minecraft/GPU builds to owner; complete Git checkout unavailable locally (`github.com` DNS resolution failed), despite pinned connector reads. Full Gradle9.2.1 Java25 build, module checks, nested/archive CRCs, source/compiled/package class identity, private-fixture exclusion, native Minecraft save/reopen/shutdown, Windows/AMD8060S Vulkan/OpenGL, VRAM/frame p50/p95/p99, water conservation soak **not run**; Linux Java25 source checks are not Windows acceptance. No JAR retention cleanup triggered, no release published.

Highest priority: authoritative V8 ocean/routing mask, Mojang native codec and V1–V7/V8 save/border testing, finite-water quantity/chemistry loaded-unloaded/restart proof, and owner GPU waterfall/LOD fault recovery. Audit is observational and not a Builder stop gate.
