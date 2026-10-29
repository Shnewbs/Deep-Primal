# Expanded replica — docs/validation/engine-continuation/guard-lexical-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/guard-lexical-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/guard-lexical-2026-10-27/README.md |
| Preserved source SHA-256 | `8f782de9c3d275f89de016671044e29f3d1a98abb3ded4efb1a952f862a76183` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev37 lexical masking for Java comments, strings, characters and text blocks, plus ocean-marker changes invalidating watched pressure graphs at unchanged volume. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the previous mutation controls missed a commented-out guard, and the 9,248-state pressure truth table is dependency-free rather than native acceptance. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a checker threat model with lexical decoys and an event-classification contract separating topology-marker changes from cosmetic flow changes. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Insert guards into every masked lexical form, preserve offsets for diagnostics, and confirm marker changes rebuild pressure state while direction-only updates retain it. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deep Primal dev37 — resident-read guard lexical regression closure (2026-10-27)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev37 — resident-read guard lexical regression closure (2026-10-27)

- **Parent before bounded builder claim:** `45b2137f9cf1f9ad1bb44dd2f24fd2a5440977e4`; writer claim `26f9589c4be9bbcd4aa6292422fe48bda632c4fb`.
- **Consumed independent audit:** `docs/engine-hourly-audits/2026-10-27T2025-73867fa/REVIEW.md` at audited source `73867fa544437554aa12ea44493aef745f553fd7`.
- **Observed defect:** the source validator accepted an entire resident guard inside a Java `/*...*/` comment. Its four previous AND/missing-height mutants still passed, so those tests alone did not detect this false positive.
- **Source change:** `tools/validate_hydrology_height_admission.py` now masks Java line/block comments, quoted strings/chars and text blocks with offset/newline preservation before finding methods, braces, state reads and guard contracts. It keeps the four existing controls and adds five negative controls for commented-out place/surface/bed guards and misleading guard string literals. The production water runtime, frozen V1–V7 geography, chemistry, finite quantities and graphics remain untouched.
- **Version:** `0.0.1-alpha.29-dev.37-heightlex.1`; source-only testing version, **not a qualified JAR or release**.
- **Focused independent-local checks:** Temurin 25.0.5+8 `java -version` and `javac -version` verified; edited checker `python3 -m py_compile` passed. On a locally constructed method-signature fixture reproducing the existing admission structure, `python3 tools/validate_hydrology_height_admission.py` returned `WATER_HEIGHT_ADMISSION_STATIC_PASS checks=21 mutationsRejected=9`; lexical edge tests included braces, comments, string decoys and pre-read ordering. `javac --release 25 CommentedPlaceCompiles.java` verified the private commented-place mutant is syntactically valid Java. Local check-script SHA256 `56670df3262681c9c2db3927c8603fad1942e470bad606f01aabb7c5d11f4a3e` (compare final GitHub blob before claiming byte identity).
- **Important limitation:** shell cannot clone github.com because DNS resolution fails. Focused Python fixture is **not** the full current runtime source nor a NeoForge/native execution. Current main runtime methods were inspected via the GitHub connector; full source script against current main, Java25/Gradle, packaged save/reopen, rollback, volume/constituent ledgers, real OpenGL/Vulkan pixels and Windows AMD8060S latency remain owner-run acceptance gates.
- **Priority after this change:** actual packaged Java25 production-source validator run, native loaded-XZ/out-of-Y and in-Y/unloaded admission, rollback injection with exact volume+chemistry conservation and save/reopen, then long-soak and waterfall GPU continuity on both backends. No new independent regrade; external baseline is carried at **50/100 (5.0/10)**.

No R57/R60/R61/R62 staged overlay, new art, renderer files or other schedule state was promoted or changed.

## Second implementation: ocean-marker pressure graph

`WaterRuntime.changed` now invalidates watched pressure jobs when an ocean/finite water marker changes at unchanged water units. Previously the flow-only fast path suppressed that invalidation. Volume changes on the same marker still refresh heads; optical flow-only changes remain ignored. A local Java25 dependency-free truth-table model passed 9,248 states (578 marker graph rebuilds; 68 cosmetic controls). This is not Minecraft native acceptance. Production commit: `351f6e6a1e4d968f1d11da81105dbd86b5a37609`.


<!-- END PRESERVED SOURCE -->
