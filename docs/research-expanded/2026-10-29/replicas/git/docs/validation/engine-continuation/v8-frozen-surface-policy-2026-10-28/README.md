# Expanded replica — docs/validation/engine-continuation/v8-frozen-surface-policy-2026-10-28/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-frozen-surface-policy-2026-10-28/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-frozen-surface-policy-2026-10-28/README.md |
| Preserved source SHA-256 | `b89cc38ac7bf174f022bd3df9947df51b098f3ac05483715eca30f052a510391` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev57 saved surface policy revision3 pinning palette, spatial-field and climate-input revisions with a fingerprint over 200 ordered floating-point parameters. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the fingerprint identifies content rather than proving collision resistance or native climate wiring; saved palette provenance alone does not complete terrain blending. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a frozen-policy manifest with canonical parameter order, explicit upgrade eligibility and independently named source revisions. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Round-trip legacy and revision3 envelopes, reject conflicting palette provenance, and confirm only permitted revision2 profiles gain the new policy without silent mutation of existing output. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- V8 saved surface palette/provenance contract — dev57

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# V8 saved surface palette/provenance contract — dev57

Source version: `0.0.1-alpha.29-dev.57-surface-policy.1`.
Scoped engine builder, October 28, 2026. This is a source-only checkpoint, not a playable JAR or completed terrain blending.

## Implementation

- New `src/main/java/com/shnewbs/deepprimal/engine/V8SurfacePolicy.java`: explicit fixed-format surface policy carrying policy, 40-biome palette, spatial-field, and climate-input revisions. The saved 16-character FNV-1a fingerprint `684e3ee170e01a32` is computed over all 200 IEEE-754 land-cover parameters in stable biome-ID order; conflicting palette or climate provenance rejects reopening rather than silently altering saved output.
- `V8GeneratorSnapshot` supports opt-in saved revision **3**, exactly eight required fields with strict nested `surface` envelope and full signed 64-bit seed preservation. The revision-1 six-field and revision-2 seven-field codecs, constructors, and behaviors remain available without implicit policy upgrades.
- Only explicit revision-2 snapshots may call `withSurfacePolicy(V8SurfacePolicy.FROZEN_R1)`. Existing revision-2 preview keeps its behavior; revision-3 preview reads the pinned palette. V1-V7 generation and live Minecraft generator registration are untouched.
- No surface blocks, native geology, vegetation generation, finite-water fields, stored geography or world edits are mutated by this work. Climate-source revision identifies the expected **caller-supplied absolute-world-coordinate input**; it is not evidence that native climate inputs have been wired.

## Independent audit and correction

Consumed `docs/engine-hourly-audits/2026-10-28T1434-6e98343/REVIEW.md`, pinned dev56 checkpoint `6e98343f3f70266ca8ff6474c0dae08f509685bf`. Auditor identified absent saved palette provenance and unverified native integration. This patch addresses only saved palette provenance; native integration remains an open gate. Last full accepted score stays **50/100 (5.0/10)**, not regraded.

## Focused test evidence

Using Debian OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`, production V8 snapshot/section sources and isolated Mojang/Minecraft API stand-ins, all eight focused suites completed successfully: **14,122,671 assertions** including **5,235 new**, **43 explicit rejected invalid policies/profiles**, **1,024 revision-3 saved-profile roundtrips**, and **1,024 exact old/new preview sample comparisons**, plus reversed/parallel replay. The regression includes wrong/missing/extra nested fields, unsupported palette/climate revisions, changed palette fingerprint, seed extremes, and old-version non-upgrade.

Local independent fixture source SHAs (SHA256): snapshot `261c6918f4bca552f0be8465a74c3aed41217ffd8f22e9d09d8156d66ef8f971`; policy `17df4d40c9ff36459d89774ddaf8d4ac963593102509d3f73611dd87154bca57`; focused test `1cef4498ff7f09f2db1aad28d03de9d700a2ddef77a2bfe14be4a89b5bcf853a`. Production Git blob SHAs: snapshot `fecb8c4850730d0c515169c4a467207f3aaf1230`, policy `9aceaaa11f486d8861eb8fb20bbc5a944030bf60`.

**Limits:** The ecotone climate producer was a clearly marked API stand-in for this snapshot test. The original `tools/test_ecotone_blend.py` was not rerun here. No Java25, full Gradle/NeoForge, actual Mojang JSON/NBT decoding, native save/reopen, chunk/heightmap/light/callback, water conservation, GPU OpenGL/Vulkan, Windows AMD8060S, or hardware budget pass is claimed. The owner is performing full builds and JAR publication.

## Next engine work

Consume saved rev3 climate/cover policies in native V8 material generation **only after** old/new world border and material/relief parity gates; add explicit versioned relief blending before hydrology; continue water single-owner unload/restart constituent conservation when foreground ownership is available. Keep the independent auditor and renderer scoped separately.


<!-- END PRESERVED SOURCE -->
