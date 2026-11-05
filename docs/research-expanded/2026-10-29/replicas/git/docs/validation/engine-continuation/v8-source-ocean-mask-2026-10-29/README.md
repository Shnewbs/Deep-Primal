# Expanded replica — docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/README.md |
| Preserved source SHA-256 | `59d89644dc437fd4828cc9c139ca52ef43861005650568da3ba35293e20879e4` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev72 replaces heuristic seed admission for a new opt-in routing policy v5 with a saved, graph-authored signed ocean_domain channel plus wet-height gating. Negative values mark ocean; nonnegative and nonfinite values reject it. The source is two-dimensional and bound to the saved seed, density, climate, and revision-5 fingerprint. The receipt reports source/blob verification and 19 isolated Java21 suites, including a large lake denied by the authored mask. It explicitly says authored provenance does not prove global ocean topology and leaves real watershed compilation, native codecs, conservation, and rendering acceptance open.

## Research expansion — gaps and details to deepen

- Actual correctness boundary already stated: a saved authored mask is reproducible provenance, not a connectivity proof. A wrongly negative inland domain can still authorize an incorrect sea seed.
- Elaboration: mask sign, finite-value handling, wet-column gating, dimensionality, and source fingerprint are documented separately but need one validated schema so import or graph edits cannot alter semantics silently.
- Actual evidence remains synthetic at the Mojang/watershed boundaries. Archive CRC and exact source blobs establish identity, not native saved-world or hydraulic acceptance.

## Research expansion — proposed contracts

- Define ocean_domain as a typed signed classification field with units/meaning, Y-independence, finite-value requirement, zero-boundary rule, and immutable content fingerprint. Do not infer salinity or reservoir permission from an arbitrary artistic mask.
- Attach a provenance class to marine admission: authored declaration versus independently verified topology. Consumers that initialize ocean accounts or hydraulic boundaries require the latter or an explicitly approved approximation.
- Validate mask/height alignment, sampling origin, scale, and signed coordinates together. A mask edit creates a new saved policy identity and never retroactively relabels old water or changes frozen profiles.
- Derive future authoritative domains from canonical cross-region outlets and spill topology, with explicit unknown regions and bounded refinement; retain v5 as its exact historical authored policy.

## Research expansion — acceptance checks

- Test negative, zero, positive, NaN/infinite, and dry-height samples at exact seeds and mixed four-corner shores; verify documented nonadmission.
- Replay the eleven-plus-region lake with both deliberately correct and deliberately incorrect masks to expose the boundary between reproducibility and global correctness.
- Change the mask while preserving unrelated graph nodes; require fingerprint mismatch rejection and exact reopening of old v1–v4/v5 profiles.
- Qualify the actual watershed, native codec, mixed-generation borders, and finite-water ledger before using authored marine labels as physical boundary authority.

## Research expansion — source anchors

- Auditor finding consumed
- Implemented
- Checks
- Still open

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev72: saved ocean-domain priority-flood source

Source: `0.0.1-alpha.29-dev.72-saved-ocean-domain.1`, implementation commit `adbefcc16b3c5ac16526a93c2024a2cff3b05d75`; test commits `a8a88a8178768ded6df1c6c36f228cc89c9db7fd` and `316b940de29b2d9a8777ac364f362bf20323aa50`.

## Auditor finding consumed
Latest completed independent review: [Dev71 at 05:26 UTC](../../../engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md), pinned `9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe`. Its newly identified false-sea-seed risk came from accepting `marineCoreSeen && unresolvedWetGateway` once a connected lake exceeds eight 8,192-block regions. This can contaminate actual V8 `WatershedRegion` priority-flood seeds, not just surface labeling.

## Implemented
New explicit saved **routing policy v5** requires a graph-authored signed `ocean_domain` channel and a wet V8 height before each exact 32-block priority-flood boundary seed is accepted. Four-corner classification must agree with that saved ocean domain. The source is 2D/Y-independent, world-seed/density/climate-bound and domain-separated through the persisted revision-5 hydrology fingerprint. Negative marks ocean; zero or positive is not ocean. Nonfinite samples reject. The old V8 route policies v1-v4, previous fingerprints, frozen V1-V7 geography, player edits and liquid quantities are unmodified. This is opt-in and **not activated native V8 world generation**.

## Checks
OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **19/19** focused API-double suites, **21,013,270** fixture assertions/checks. Java source/test blobs were matched against the published Git objects. New tests deny an 11+ region-wide marine-looking lake based on its saved domain, exercise positive ocean and dry-column gating, signed coordinates, query ordering, 32-block seed alignment, graph provenance, modeled saved-profile reopen and edited-mask fingerprint rejection. Both separately compiled wrong implementations (ocean-mask sign reversal; bypassing dry-column gate) were rejected.

Portable source evidence ZIP `DeepPrimal_Dev72_SavedOceanDomain_SourceEvidence.zip`: **157,780 bytes**, SHA-256 `ab5181b92a377b49df1657b9f66477ec7d962f541f361dab6ecf71038b252224`, 73 members, ZIP CRC passes. Includes exact source/test files, isolated dependency doubles, runner, positive log and mutation receipts. It is **not a JAR**.

## Still open
The new mask is **authored**, not itself a proven ocean source. No universal cross-region outlet/river discharge/lake spill accounting yet, no native old/new world border, Mojang codec, real production `WatershedRegion` compilation, complete JDK25/Gradle/NeoForge, GPU, water restart/conservation, multi-seed visual or CPU/memory acceptance. The standalone `tools/test_ecotone_blend.py` runner was unavailable in the local partial source archive, so it was not independently rerun. Root `ENGINE-WORK-STATE.json` remains at its dev53 pinned checkpoint pending complete validator access. No new playable JAR or independent grade; last accepted complete score remains **50/100 = 5.0/10**.

Next: author a globally consistent ocean-domain source based on actual cross-region spill topology and prove that it matches saved terrain geometry; independently validate production watershed and native generation. Maintain water conservation and renderer separation.


<!-- END PRESERVED SOURCE -->
