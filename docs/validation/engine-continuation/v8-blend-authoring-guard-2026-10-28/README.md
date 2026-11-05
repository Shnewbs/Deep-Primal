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
