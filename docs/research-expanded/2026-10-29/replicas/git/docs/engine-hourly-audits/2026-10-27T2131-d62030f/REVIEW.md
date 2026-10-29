# Expanded replica — docs/engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T2131-d62030f/REVIEW.md |
| Preserved source SHA-256 | `8969332c3fd8bfa1efa1ee7139876f5409af15ee609d75f57c4bac2ca526d807` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine source audit — October 27, 2026, 21:31 UTC”. Its original opening states: “Pinned main source: d62030f4b6a2a13da0d09a83a278ece2aa49b8ea, version 0.0.1-alpha.29-dev.37-heightlex.1. Read-only auditor work: no edits to production engine, renderer, writer state, leased files or builder outputs. No fresh JAR: October 27 owner override reserves full Gradle/Minecraft/GPU builds for owner. No regrade claimed.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- A source-text guard match and an executable safety property require different evidence.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Keep lexical scanning as a diagnostic and define the required control-flow property: every native read is dominated by the correct bounds/residency admission.
- Separate checker weaknesses from live production defects and link each deliberate counterexample to the checker version that accepts or rejects it.

## Research expansion — acceptance checks

- Reject commented, quoted, unreachable, contradictory, and wrong-cell guards while retaining legitimate short-circuit checks.
- Use a native-read sentinel that fails if any out-of-range or unloaded path reaches the actual adapter read.

## Research expansion — source anchors

- Independent engine source audit — October 27, 2026, 21:31 UTC
- Fresh finding: lexical guard acceptance does not prove safety dominance

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine source audit — October 27, 2026, 21:31 UTC

**Pinned main source:** `d62030f4b6a2a13da0d09a83a278ece2aa49b8ea`, version `0.0.1-alpha.29-dev.37-heightlex.1`. Read-only auditor work: no edits to production engine, renderer, writer state, leased files or builder outputs. **No fresh JAR:** October 27 owner override reserves full Gradle/Minecraft/GPU builds for owner. No regrade claimed.

## Fresh finding: lexical guard acceptance does not prove safety dominance

Production [HydrologyRuntime.java L249–265](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L249-L265) still contains correct, active world-height/loaded-chunk guards before native block reads. This audit does **not** establish a production unguarded read.

Dev37 addressed the prior *commented-out* guard false positive, but [tools/validate_hydrology_height_admission.py L90–100](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/tools/validate_hydrology_height_admission.py#L90-L100) merely searches for matching guard source text before `l.getBlockState(p)`. It cannot ensure every path executes the guard. The following legal Java has the same guard text but does not protect reads:

```java
if (false) { if (!l.isInWorldBounds(p) || !l.hasChunkAt(p)) return null; }
var state = l.getBlockState(p);
```

An isolated Python reproduction of the pinned `code_only` and `guarded_before_read` predicate contract gave **five checked fixtures**: genuine guard accepted, commented guard rejected, but **three unreachable guards incorrectly accepted** (`if(false)`, unreachable `else`, contradictory prerequisite). Independent **Java21** (`javac -Xlint:all`, `java -Xverify:all`) source stub, two invalid-position cases + one valid, gave **0 forbidden reads** with the active guard and **2** with the false-branch guard. This is a regression-validator blind spot, **not a native game reproduction**. Java 25 archive/JDK path was absent from this run; shell GitHub DNS also failed. The GitHub connector supplied the exact pinned code.

Dev37 production [WaterRuntime.changed L85–119](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java#L85-L119) now invalidates resident pressure-job graphs on equal-units ocean marker flips, complementing [HydrologyRuntime.externalChange L80–98](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java#L80-L98). These changes are inspected source, **not** fresh packaged-water acceptance. Builder's 21 static/9,248 Java25 stand-in assertions were not independently replayed here.

**Next concrete repair:** require control-flow dominance (AST/CFG or actual instrumented extracted-method execution) rather than matching guard text. Add `if(false)` and unreachable-`else` negative mutations while keeping all nine existing controls. Follow with native in-bounds/unloaded and out-of-Y/loaded reads, equal-units boundary flip, exact volume/salt/sediment rollback and save/reopen.

## Equal-weight engine rubric — carried historical, not fresh full audit

| Category | /10 | Pinned evidence / missing acceptance |
|---|---:|---|
| Terrain realism | 5 | [TerrainEngine](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainEngine.java); V8 general 3D geometry not proven |
| Biome blending | 4 | [Landscape](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java); weighted ecotones absent/unaccepted |
| Hydrology and caves | 6 | [WaterRuntime](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java); native loaded/unloaded, long-soak and waterfall coverage missing |
| Features/structures | 4 | [EngineChunkGenerator](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/EngineChunkGenerator.java); general structure spacing not proven |
| Performance/threading | 3 | [Baseline review](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/docs/engine-review-2026-10-24/REVIEW.md) warm fill ≈14.45–14.99ms vs 5ms target; current p95/p99 unmeasured |
| Determinism/stability | 7 | Frozen revision/profile replay historical; no cross-architecture/long-travel acceptance |
| Architecture/code quality | 6 | Separate core, graphics, LOD and Vulkan; fresh complete build not run |
| Extensibility | 4 | [TerrainProfile](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/src/main/java/com/shnewbs/deepprimal/engine/TerrainProfile.java); full data-driven stages API not proven |
| Testing/docs | 7 | New checker false positive; substantial earlier core/native evidence, incomplete current native/device matrix |
| Polish/tooling | 4 | Water/LOD visual issues and owner Windows AMD Vulkan/OpenGL acceptance pending |
| **Historical total** | **50/100 = 5.0/10** | **Tier 2; no newly certified overall grade** |

Last complete accepted rubric: [2026-10-24 REVIEW.md](https://github.com/Shnewbs/Deep-Primal/blob/d62030f4b6a2a13da0d09a83a278ece2aa49b8ea/docs/engine-review-2026-10-24/REVIEW.md). Strict independent stopping threshold is **>8.5/10**, plus completed water-engine gates, not met.

**Execution/receipt:** Python3 fixture contract **5/5 executed**, 3 unsafe dead-guard examples improperly accepted by regex. Java21 standalone compilation/runtime **pass**, two forbidden accesses reproduced with deliberate dead-guard copy. Local zip evidence SHA256 `1a6c2403c1114f792de543841d6056295fcbfd9772385b05fb98a80ee81b18d0`, 7,531 bytes. No full repo checkout, Java25 Gradle/NeoForge, PCK/JAR/CRC, native save-reopen, Windows GPU pixels, Vulkan, OpenGL, AMD measurements or builds were run. Builder, renderer and original writer lease remain untouched. No stable/published artifact.

**Next auditor run:** verify source-matched production method behavioral guard tests and any owner native build receipts; review newly committed integration without taking the builder’s scope. Carry baseline rather than inflating scores.


<!-- END PRESERVED SOURCE -->
