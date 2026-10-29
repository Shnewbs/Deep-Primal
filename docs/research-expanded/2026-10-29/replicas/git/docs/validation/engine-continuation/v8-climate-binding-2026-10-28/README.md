# Expanded replica — docs/validation/engine-continuation/v8-climate-binding-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-climate-binding-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-climate-binding-2026-10-28/README.md |
| Preserved source SHA-256 | `7208a137d0f42ae8a3d3c7ec32435cf738bd3dde10c97ffe6c8b2683a249ed89` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev64 snapshot revision4 binding V7 terrain revision, channel revision and seven terrain options into a strict canonical saved climate reference. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that binding reference identity is not an authoritative V8 ocean or routed-water producer, and the owner-confirmed playable snapshot remained dev53 in this record. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a producer identity contract distinguishing referenced climate inputs, saved policy compatibility and the authoritative domain required for V8 water. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Round-trip exact option bits and seeds, reject noncanonical bindings and envelope downgrades, then validate real producer masks and saved-world reopen before native activation. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deep Primal dev64 — explicit saved V8 climate/hydrology reference binding

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev64 — explicit saved V8 climate/hydrology reference binding

**Version:** `0.0.1-alpha.29-dev.64-climate-binding.1`. **Parent baseline:** dev63 checkpoint `1520ef17a34d2ea15ed9a75feaee6336490e3023`. Scoped claim `4f94e12cb42dd248885ed37020f34ad76fcd39d1`. Existing owner-confirmed playable version is dev53; **this is source-only**.

## Actual implementation

- `V8ClimateBinding` saves a strict v1 reference identity: pinned terrain revision 7, channel revision 0..5 and all seven V7 terrain options. A canonical bit-exact key preserves boolean values and both floating-point values without runtime recreation/default drift. Unsupported, malformed and noncanonical inputs fail.
- `V8GeneratorSnapshot` adds an **explicit** revision-4 nine-field envelope, `climate_binding`. Revision 1/2/3 codecs and prior constructors remain intact; no implicit upgrade. New `withClimateBinding()` is legal only from revision 3, and revision 4 requires the saved binding.
- `V8SavedClimateSource.fromSavedV8()` reopens the saved V8-r4 reference with strict seed parity and a frozen surface policy. The previous revision-3 saved-V7 explicit bridge is unchanged. The V8 prewrite and materialization planning paths now accept explicitly selected revisions 3 or 4, preserving their density graph and all wet-domain exclusions.
- Saved geometry does not change. No V1-V7 generator, block palette, physical fluid, water constituent, player edit or renderer-owned file is modified. The reference V7 hydrology is **not** a proven authoritative V8 ocean mask; native worldgen is not enabled.

## Tested in this run

Isolated OpenJDK **21.0.12.1**, strict `javac --release 21 -Xlint:all -Werror`, runtime `java -ea -Xverify:all`, API doubles only:

- **15 focused Java suites passed**, accounting for **20,945,922** locally reported assertions/checks (counts are fixture assertions, not independent integration evidence).
- New `V8ClimateBindingChecks`: **16,138 checks**, **1,536** exact revision-4 persisted roundtrips, **4,096** r3/r4 matched prewrite columns with fixed source, **103** rejection cases, including seed mismatch and root-envelope downgrades.
- Standalone `python3 tools/test_v8_climate_binding.py` passed **12,297** checks, and its compiled channel-revision guard mutant was rejected. This is a small standalone codec/API-double check, not the complete V8 integration fixture.
- The broader regression and new negative-control fixtures are retained in the accompanying source-evidence ZIP. Initial test dispatch used an incorrect package for EcotoneSurfaceFieldChecks; corrected run passed. An initial non-discriminating negative mutation also passed; it was rejected as evidence and replaced by the seed-mismatch mutant, which compiled and failed the required assertion.
- Source blobs were matched byte-exact to their Git object SHA before publication. The full copied dev62 V8 source plus the current dev63 bridge were compiled together in an independent isolated workspace; they were not a complete current repository checkout.

## Explicitly unaccepted

No Java25, Gradle9.2.1, NeoForge, actual Mojang codec/JSON/NBT save+reopen, engine-core module boundary, native chunk generation, Windows AMD8060S, OpenGL/Vulkan, long soak, timed CPU/memory or full-world finite-water ledger gate was executed. `RecordCodecBuilder`'s nine-field path was exercised with a test API double, not the real DFU. No full build, native pass, playable JAR, release or stable promotion is claimed. Root `ENGINE-WORK-STATE.json` remains untouched because the full pinned root validator could not be executed from this isolated workspace.

Latest **completed repository** external audit consumed: `docs/engine-hourly-audits/2026-10-28T2034-055d78f8/REVIEW.md`, pinned `055d78f8535d5380ac99e2dd0fc43da49ae5ba89`, carried grade **50/100 (5.0/10)**. The auditor's later run record was not yet a complete published regrade.

**Next:** Real Mojang codec save/reopen and native V8 registration with an authoritative V8 ocean/routed river/lake mask, signed chunk borders and player edits; then water loaded/unloaded quantity/constituent ledger, multiplayer/long soak and actual GPU waterfall continuity. Keep independent auditor and renderer isolated. This dev64 change does not satisfy engine/water acceptance.


<!-- END PRESERVED SOURCE -->
