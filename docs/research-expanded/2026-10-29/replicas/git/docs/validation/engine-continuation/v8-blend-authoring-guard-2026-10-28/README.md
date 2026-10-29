# Expanded replica — docs/validation/engine-continuation/v8-blend-authoring-guard-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-blend-authoring-guard-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-blend-authoring-guard-2026-10-28/README.md |
| Preserved source SHA-256 | `148a90f511c4f1813238ae9d359f8ff100f9e7410292b597f158897353a56eae` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev65 rejecting withBlendPolicy calls on saved V8 revisions 3 or 4 that previously returned revision 2 and silently discarded surface or climate policies. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the guard preserves explicit revision1-to2 authoring and revision2 edits, but is source-only and does not activate native V8. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add an allowed-transition matrix for every authoring helper, making policy preservation and prohibited downgrades explicit. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Call each helper on every supported saved revision, require rejection before constructing a downgraded profile and verify all frozen policy fields survive permitted edits. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev65 — prevent explicit V8 authoring API from downgrading saved policies

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev65 — prevent explicit V8 authoring API from downgrading saved policies

Source-only change, 2026-10-28. Independent audit `docs/engine-hourly-audits/2026-10-28T2230-a0d68a1d/REVIEW.md` (pinned `a0d68a1d916e3cc6f71de827de2cf3e7b34d0da0`) found that `V8GeneratorSnapshot.withBlendPolicy()` always created revision 2 even when invoked on already saved revision 3 or 4. This discarded the frozen surface policy and climate binding from a newly returned profile without warning. The change rejects these calls with `IllegalStateException`, while retaining explicit revision-1→2 authoring and revision-2 policy edits.

No automatic save migration, native generator activation, hydrology/ocean-mask change, water quantity change, or renderer code touched. The API remains opt-in and does not claim prevention of all saved-world corruption.

## Focused verification

Actual production `V8GeneratorSnapshot.java` compiled with isolated Mojang/Minecraft API doubles and checked using new `V8BlendAuthoringGuardChecks`:

- Java: OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`.
- New class: `V8_BLEND_AUTHORING_GUARD_PASS assertions=15873 rejected=3072 roundtrips=2048`; exercises all four saved formats, exact negative signed seeds, revision-3/4 rejection both before and after stand-in codec reopen, original snapshot fidelity, and accepted revision-1/2 edits.
- Source-extracted production method regression: `V8_BLEND_AUTHORING_EXTRACT_PASS cases=12288` under both Python normal and `-OO`; two independently compiled mutants (no guard / revision-4-only guard) both failed their test oracle.
- Existing focused suites: `V8_SAVED_PROFILE_PASS`, `V8_BLEND_POLICY_PASS`, `V8_SURFACE_POLICY_PASS`, `V8_CLIMATE_BINDING_PASS`, `V8_ECOTONE_PREWRITE_QUERY_PASS`, `V8_ECOTONE_NATIVE_PLAN_PASS`, `V8_STRICT_ENVELOPE_PASS`, `V8_RELIEF_V4_COAST_PASS`, plus base-height, section bridge, planned-ground, ecotone surface, relief V2/V3 and snow preflight all passed in the same isolated source fixture.

These are Java21/API-double tests, not true Mojang DFU JSON/NBT/save-reopen, Java25/Gradle/NeoForge, GPU or performance acceptance. Root work-state was not published. Authoritative V8 water masks, native old/new borders and finite-water acceptance stay open.

## Reproduction

From the complete Git repository, run `python tools/test_v8_blend_authoring_guard.py`. The test is a source-extracted Java21 contract, and does not by itself compile the entire production class. The supplementary Java test uses the isolated fixture described in the attached source evidence and must never be misreported as native Minecraft verification. The owner continues to handle full builds and JARs.


<!-- END PRESERVED SOURCE -->
