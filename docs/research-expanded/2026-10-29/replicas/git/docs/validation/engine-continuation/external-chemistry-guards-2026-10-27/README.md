# Expanded replica — docs/validation/engine-continuation/external-chemistry-guards-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/external-chemistry-guards-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/external-chemistry-guards-2026-10-27/README.md |
| Preserved source SHA-256 | `57a8fa1342224bea4ef9b004021f8a516f4863037c8e3946b92ba85225887065` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev36 external ocean-marker chemistry invalidation when a block changes ocean identity without changing units, while flow-only and internal transactional writes remain distinct. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that this source-only checkpoint lacks native save/reopen and rollback injection, and the change does not alter volume or worldgen profiles. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an external-change classification table connecting material markers, chemistry defaults, pressure invalidation and internal transaction ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Toggle ocean and finite markers at equal volume, preserve chemistry for direction-only edits, then check salt and sediment persistence plus rejected-write rollback in a native world. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev36 — external ocean chemistry invalidation and short-circuit loaded guards

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev36 — external ocean chemistry invalidation and short-circuit loaded guards

**Date:** October 27, 2026. **Production-code commit:** \`dccc7bc2cd40d4fd298723a183079cbf29d49f41\`.
**Version:** \`0.0.1-alpha.29-dev.36-chemistryadmission.1\` (source-only, not a qualified JAR).

## Implemented

\`HydrologyRuntime.externalChange\` now removes a stale water composition record if an **external** write changes \`EngineWaterBlock.OCEAN\` even while keeping the same number of units. Previously, a river parcel could persist under a new ocean marker (or the reverse), bypassing the default salt identity selected by \`contents\`. A changed flow direction alone does not clear chemistry. Internal engine writes keep their own transactional ledger. No water-unit mutation, worldgen profile or terrain revision is introduced.

The independent auditor's October 27 19:33 UTC negative control detected a blind spot in \`tools/validate_hydrology_height_admission.py\`: merely finding both guard names allowed replacing \`||\` with \`&&\` and exposing unloaded/out-of-world reads. The updated validator enforces the short-circuit operator **before** each first block read in \`place\`, \`exposedColumnCell\`, and \`waterColumnBed\`, with four mutation controls. Source: \`docs/engine-hourly-audits/2026-10-27T1933-a765774/REVIEW.md\`.

## Focused local checks

- Isolated Linux Eclipse Temurin JDK **25.0.5+8**: actual \`boundary\`/\`externalChange\` Java method text transcribed from the reviewed production method and compiled into a JVM stand-in with fake state, position and ledger types; **9/9 cases PASS**, including two direction changes, count changes, flow-only, nested engine writes and empty save entries. A marker-guard removal was correctly rejected.
- Locally reproduced native-boundary method fragments and Python source guard checker: **16/16 checks**, **4/4 site-specific mutants rejected**; an additional bad \`&&\` source mutant was rejected. The committed scripts use the full checked-out production file; owner should rerun them against the **exact committed repo** to elevate from local stand-in checks.
- No full Java25 Gradle/NeoForge build, native Minecraft/packaged smoke, JAR, CRC, save/reopen, cross-chunk rollback injection, actual Vulkan/OpenGL or AMD8060S performance was executed. The owner handles full builds under the October 27 override.

**Next:** run the new source-bound scripts, the owner's full Java25 build and actual native external-ocean marker save/reopen ledger; then unloaded-boundary and write-rollback tests, sustained conservation and real GPU waterfall continuity. Preserve R57/R60/R61/R62 staged and V1-V7 geography. Independent complete grade carried **50/100, 5.0/10, Tier 2**; no regrade or release was claimed.


<!-- END PRESERVED SOURCE -->
