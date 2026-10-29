# Expanded replica — docs/validation/engine-continuation/v8-relief-v2-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-relief-v2-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-relief-v2-2026-10-28/README.md |
| Preserved source SHA-256 | `07eb87ebb42d1eb1dfb80bb05af68314fcf38a81225a6c9163db6b6cb01c179a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev60 explicit relief V2 graphs with continuous coastal, foothill, mountain and plateau shoulders calculated before hydrology and retaining canyon and overhang inputs. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that focused graph checks do not establish native coast compatibility, saved-world transition or a new playable JAR. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a versioned relief authoring workflow that saves complete graph definitions and requires water-domain review before activation. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare historical graph fingerprints and relief joins across seeds, keep later coastal counterexamples linked to this version and require native border and routing gates for any adopted profile. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deep Primal Dev60 — opt-in versioned V8 landform relief

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal Dev60 — opt-in versioned V8 landform relief

Date: 2026-10-28. Source version: `0.0.1-alpha.29-dev.60-relief-v2.1`.
Base: current main (including independent Dev59 audit and concurrent renderer fixes).
Latest consumed independent review: `docs/engine-hourly-audits/2026-10-28T1726-182cdce/REVIEW.md`, pinned `182cdce92eaed2eb371af4066b43b5dc45f61908`. The external score remains carried **50/100 (5.0/10)**, not rescored by this Builder.

## Implemented

- Added explicit `TerrainShapeV8.definitionReliefV2(boolean)`, a revision-tagged density graph with continuous coastal, foothill, mountain and plateau shoulders. All relief joins are calculated before any later hydrology. Existing canyon-cut input and final-cut placement, overhang and optional floating features remain intact.
- Existing `TerrainShapeV8.definition(boolean)` retains its complete prior node ordering and values. V1–V7 generators are untouched. Existing V8 snapshots keep their exact saved graph and are never silently rewritten.
- Added `V8GeneratorSnapshot.newReliefV2(...)` as an explicit NEW V8 revision-3 authoring path that serializes the entire new graph alongside existing saved blend/surface policies. This is opt-in; it is **not** an active Minecraft generator registration or a saved-world migration.
- No physical water or chemistry, approved art, native biome IDs, renderer code or player-edited chunks were changed.

## Focused verification

- Linux OpenJDK/Javac **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`. Production sources compiled against isolated Minecraft/Mojang API doubles from the prior dev59 evidence package.
- **12/12** focused Java21 component suites passed after invoking `EcotoneSurfaceFieldChecks` under its actual `com.shnewbs.deepprimal.core` package. New `V8ReliefV2Checks` passed **262,265** assertions.
- New tests include three seeds × floating/nonfloating graphs, legacy graph preservation, nonzero plateau witnesses, bounded adjacent absolute-coordinate relief, unchanged canyon incision, exact revision-3 codec profile reopen, 4-seed signed chunk voxel/heightmap-geometry parity after reopen, and rejection of invalid new profile heights.
- A separate deliberately altered source (`plateau_scale=33333`) compiled but the regression correctly rejected it at the bounded-relief assertion. Initial attempted mutation with the former too-narrow plateau thresholds was *not* rejected; thresholds were corrected after measuring near-zero active plateau coverage in three seed surveys. Test source SHA and production source hashes are preserved in the handoff.
- Tooling limitation: the official `tools/test_ecotone_blend.py` and `tools/validate_engine_work_state.py` were **not** run from a complete authoritative checkout; GitHub connector supplied the precise sources and documentation, while local isolated source archive supplied API doubles. Root work-state was left unchanged.

## Unpassed gates / owner handoff

No Java25, Gradle9.2.1/NeoForge, native actual Mojang NBT/JSON reopening, rendered multi-seed transects, old/new chunk-border transition, benchmark latency/memory, or Vulkan/OpenGL water continuity was run. The owner continues all full builds and JAR publication. No new JAR/release is claimed.

Next work: bind V8 climate and domain to a fully persisted authoritative world producer; register explicit new V8 generator lifecycle only after saved-world/legacy parity; integrate relief before routed hydrology, then native water transfer/unload/restart constituent conservation and visual gates when water ownership allows. 


<!-- END PRESERVED SOURCE -->
