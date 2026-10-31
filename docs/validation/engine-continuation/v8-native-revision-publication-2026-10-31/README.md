# Dev126 — prevent terrain-only publication of V8 vegetation profiles

**Scope:** existing hourly engine builder, current main after independent dev125+ auditor repair. Source version `0.0.1-alpha.29-dev.126-v8-revision-publication.1`. No full build, native hook, qualified JAR or regrade.

## Defect and implementation

`V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit` previously accepted every saved V8 revision >=2, including revision 6. Revision 6 is the saved vegetation/receipt policy. A caller could materialize only raw terrain, atomically mark the r6 chunk GENERATED and then have no supported path to add the omitted vegetation and receipt on reopen. This is a semantic generation-completeness defect, distinct from the existing signed-coordinate overflow repaired by the independent auditor.

The terrain-only publication entry point now rejects revision 6 and later **before** the owner border ticket, native block setters or publication. Explicit terrain revisions 2–5 remain admitted. The separate revision-6 `fillSavedVegetationAndCommit` remains the sole lifecycle entry point for publishing r6 terrain, vegetation and receipt together. The saved V8 policy format, V1–V7 geography, water quantities, renderer, Vulkan and LOD are unchanged. No existing chunk is migrated.

The existing `V8ChunkGenerationNativeFillChecks` fixture was revised to use terrain revisions 2–5 for terrain-only successes and r6 for vegetation, and now rejects r6 terrain-only calls before ticket acquisition, materialization or discard. `tools/test_v8_native_fill.py` adds a compiled broken-control mutation for the revision guard. **These updated repository tests were not run from a complete checkout in this environment.**

## Executed validation and limits

- OpenJDK **21.0.12.1** local extracted-method/API-double model compiled with `javac --release 21 -Xlint:all -Werror` and executed with `java -ea -Xverify:all` twice: **900,000 assertions per process**, same output each time. Terrain revisions 2–5 admitted, r6 rejected before modeled ticket/write, wrong profile and chunk identity rejected.
- A compiled deliberately defective control replacing the revision check with `if(false)` failed as expected (`AssertionError: accepted invalid input 4`). Model harness SHA-256 `21f3d941f7afa188e388e3d5bb362291d27dd7624ac496081a170411b5a5fa91`.
- Staged exact production and test blobs were inspected for the revision guard, fixture use, Python mutation anchor and balanced braces. **This is static inspection, not a full Java compile of the production class.**
- The model is not a native NeoForge/Minecraft test and does not exercise real NBT, atomic chunk I/O, crash/reopen or signed old/new-generation border geometry.
- Git CLI DNS and complete local checkout unavailable. Java25/Gradle9.2.1 full build, CoreChecks, owner Windows GPU/LOD performance, water acceptance, package CRCs and JAR SHA are **not run**. No existing JAR was deleted.
- Latest completed independent audit: `docs/engine-hourly-audits/2026-10-31T0928-97c74304/REVIEW.md`, pinned `97c743048dd37cf4b4b763bdef69f28990bd0192`; auditor's preview-coordinate repair is preserved. Baseline grade **50/100 (5.0/10)** carried, not regraded.

## Next acceptance gates

Run `python3 tools/test_v8_native_fill.py` and `python3 -OO tools/test_v8_native_fill.py` on the full checkout, followed by full Java25/Gradle and real Minecraft V8-r6 fresh/reopen tests. The authoritative native chunk/stamp serializer and durable compare-and-publish owner remain unregistered. Verify old/new borders, V5/V7 relief parity and finite-water ledger under the independent audit plan. The October 27 owner-build arrangement remains in effect until explicitly changed.
