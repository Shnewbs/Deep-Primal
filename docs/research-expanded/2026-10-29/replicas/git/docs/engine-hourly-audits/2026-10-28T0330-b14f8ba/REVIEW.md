# Expanded replica — docs/engine-hourly-audits/2026-10-28T0330-b14f8ba/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0330-b14f8ba/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T0330-b14f8ba/REVIEW.md |
| Preserved source SHA-256 | `e0116006218b4283f91269b16875467014d3e484939a66aede7ce9ed78f8a61e` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal — independent audit, 2026-10-28 03:30 UTC”. Its original opening states: “Pinned committed main: b14f8ba6eaf279212a0b96098ae493d6a34c1440; version 0.0.1-alpha.29-dev.44-v8chunk.1. Latest production-code commit: e67cd42d42b0e7017d74aac37fb156f3156e5c3e. The auditor made zero production source changes, left Builder/renderer workspaces and leases untouched, and conducted focused read-only source tests in an isolated Linux workspace. No JAR was built: the October 27 owner override assigns complete Gradle, Minecraft, GPU and owner JAR builds to the owner, superseding older audit-build instructions.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `DensityChunk.sample`, `DensityChunkPlanChecks`, `AuditorAdversarialChecks`, `LodMeshPartSplitterChecks`.

## Research expansion — gaps and details to deepen

- Deepen exact-versus-approximate density evidence and keep a pure sampler result separate from native terrain materialization.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Version arithmetic order, lattice spacing, graph limits, interval certificates, and exact fallback behavior; preserve a scalar reference oracle.
- Document whether a reported speedup excludes materialization, host biome/light/status work, allocation, or cold regional construction.
- Carry the documented approximation disagreements and stage-two integration block as historical evidence. A future optimization must compare sign/occupancy decisions independently and meet native cost and compatibility gates before promotion.

## Research expansion — acceptance checks

- Exercise zero crossings, negative/far coordinates, partial vertical sections, overflow endpoints, uncertain intervals, and all sampling orders.
- Compare actual generated blocks, base-column queries, heightmaps, lighting invalidation, and save/reopen at the real adapter boundary.

## Research expansion — source anchors

- Deep Primal — independent audit, 2026-10-28 03:30 UTC
- Pinned findings and source/test evidence

Related design: [terrain water](../../../../../chapters/terrain-water.md) · [architecture authoring](../../../../../chapters/architecture-authoring.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal — independent audit, 2026-10-28 03:30 UTC

**Pinned committed main:** `b14f8ba6eaf279212a0b96098ae493d6a34c1440`; version `0.0.1-alpha.29-dev.44-v8chunk.1`. Latest production-code commit: `e67cd42d42b0e7017d74aac37fb156f3156e5c3e`. The auditor made **zero production source changes**, left Builder/renderer workspaces and leases untouched, and conducted focused read-only source tests in an isolated Linux workspace. **No JAR was built**: the October 27 owner override assigns complete Gradle, Minecraft, GPU and owner JAR builds to the owner, superseding older audit-build instructions.

## Pinned findings and source/test evidence

1. **V8 exact chunk occupancy (opt-in, not native).** [DensityChunk.samplePlanned L72–128](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityChunk.java#L72-L128) combines [DensityVolumePlan](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityVolumePlan.java) certificates with exact fallback per 64-high slab. The original `DensityChunk.sample` remains unchanged; native [EngineChunkGenerator L96–129](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java#L96-L129) still selects V1–V7 and a pinned default-branch code search found no native `samplePlanned` invocation. Consequently source parity does **not** establish integrated gameplay, real chunk speedup or saved V8 profile support.
2. **Fresh product-graph Java21 parity test:** verified Git object identities for **nine actual product graph/partition/test sources**, then compiled and executed the registered `DensityChunkPlanChecks` under `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`. **1,218,333 assertions** passed across **1,208,064 voxels**, with **902,118 certified cells** avoiding exact sampling in that selected corpus. This reproduces the [builder's Dev44 source handoff](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/docs/validation/engine-continuation/v8-chunk-plan-parity-2026-10-28/README.md) but is an auditor-executed run.
3. **New independent adversarial graph/geometry test:** `AuditorAdversarialChecks` passed **3,172,138 assertions**, **180 chunk comparisons**, **1,996,800 exact voxel comparisons**, **408,704 certified cells**, and **42 fractional certificate probes**. Ten graph shapes (actual authored V8, optional floating, 2D/3D noise, signed limits, warp, clamp, min/max, smoothstep, extreme tiny signed thresholds), seeds 73/20261020, budgets 0/8/256, and heights 1/64/65 matched legacy occupancy, count, iteration order where applicable, payload and top-exclusive surfaces. Observed maximum **512** classifications across two slabs, each bounded at 256. This finite matrix is not a universal proof or native renderer/worldgen test.
4. **Renderer's new opt-in LOD splitter checked independently.** [LodMeshPartSplitter](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodMeshPartSplitter.java) Git object `770dfde8609da73d3e96bdbfad8d9606cc09efda` compiled with explicit API-compatible mesh doubles; registered `LodMeshPartSplitterChecks` passed **282,240 assertions**, including 1,800 randomized single meshes and 300 randomized pages. [Renderer self-audit](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/docs/renderer-jams/20261027-quad-bounded-upload/REVIEW.md) identifies default GPU admission of at least 256 KiB versus max built mesh 94,208 bytes; splitting remains off by default and lacks any observed driver/frame improvement.
5. **Coordination preflight:** pinned `docs/ENGINE-WORK-STATE.json` passed structural validation with 863 distinct object keys and no duplicates, schema 1, consistent timestamps, Builder `checkpointed`, benchmark reservation `released`. No lease taken. The earlier [water write guard audit](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/docs/engine-hourly-audits/2026-10-28T0134-4fc83e6/REVIEW.md) still requires real NeoForge callbacks, same-state ABA/compensation, unload/reopen, player edits and water/salt/sediment ledger acceptance.

## Equal-weight ten-category scoring

These are the **historical carried** values from the [full 2026-10-24 review](https://github.com/Shnewbs/Deep-Primal/blob/b14f8ba6eaf279212a0b96098ae493d6a34c1440/docs/engine-review-2026-10-24/REVIEW.md), **not a fresh complete regrade**. Source-only prototypes and unit/double tests earn no integrated feature or performance points. **Total 50/100, mean 5.0/10, Tier 2; strict target >8.5 NOT met.**

| Category | /10 | Current blocker |
| --- | ---: | --- |
| Terrain realism/variety | 5 | V8 remains experimental; no native 3D density chunk/profile or visual acceptance |
| Biome blending/coherence | 4 | Discrete biome palette thresholds; weighted ecotones not live |
| Hydrology/caves | 6 | Finite systems exist; postwrite callbacks, ledger, persistence and long-running water proof open |
| Structures/features | 4 | Native generator's structure stream empty; no general placement/exclusion pipeline |
| Performance/threading | 3 | Historical warm fills 14.45–14.99ms versus <5ms target; no fresh native p95/p99/alloc |
| Determinism/stability | 7 | V1–V7 freeze supported historically; new V8/native cross-host/save/unload untested |
| Architecture/code quality | 6 | Core/graphics/LOD/Vulkan separated; V8 integration and runtime lifecycle unqualified |
| Extensibility | 4 | V8 authoring not bound into saved generator; general feature/biome APIs incomplete |
| Testing/benchmarks/docs | 7 | Focused registered/independent Java21 tests pass; Java25/full native/GPU not run |
| Polish/config/tooling | 4 | Natural cliff water, distant LOD, recovery and AMD Vulkan/OpenGL proof open |

**Executed:** pinned Git refs/file and recent audit/handoff review; nine product source Git-object checks, current work-state structural scan, isolated JDK21 compilation with warnings fatal, registration/adversarial V8 parity, and LOD stub tests; archived source/logs/receipt with ZIP CRC/hash. Host Linux x86_64 AMD EPYC 9V74, Java/javac 21.0.12.1. No Builder, renderer, texture, game, saved-world or shipping artifacts changed.

**Not run:** owner Temurin JDK25.0.5+8 (installed path and uploaded archive absent here), complete Gradle 9.2.1, Minecraft native V8/save/reopen/shutdown, real water transaction callbacks/chemistry, Windows AMD8060S, Vulkan/OpenGL frame/VRAM/readback, package/jarJar CRC/class-origin/Windows payload and FSR/DLSS native acceptance. No new qualified JAR, hosted Actions, CurseForge or GitHub release. No JAR retention changes.

**Priority:** owner Java25 + native V8 chunk/heightmap/lighting/legacy replay and saved profile tests, matched end-to-end chunk performance/alloc; finite-water postwrite chemistry ledger under callbacks and unload/reopen; then actual Vulkan-first waterfall/LOD coverage and frame tails. Keep independent auditor enabled and nonblocking.

**Compact audit ZIP:** `DeepPrimal_Audit_20261028T0330_b14f8ba_SourceEvidence.zip`, 35,287 bytes; SHA-256 `41d105249badf0de34ccf2318d7e6105008bc5792878a82a470943855e873c66` (24 CRC-valid entries). This is not a JAR.


<!-- END PRESERVED SOURCE -->
