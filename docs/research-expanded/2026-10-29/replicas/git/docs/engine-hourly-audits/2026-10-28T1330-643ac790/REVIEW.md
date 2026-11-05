# Expanded replica — docs/engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T1330-643ac790/REVIEW.md |
| Preserved source SHA-256 | `90529983f6421a42fa03387856d34dc5bb9e5396d48f831fae4cbed2d7b5124b` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent audit — 2026-10-28 13:30 UTC”. Its original opening states: “Pinned source SHA: 643ac7903f19b87626ee25f810bfb6f42cf7e0c9; version 0.0.1-alpha.29-dev.55-blendpolicy.1.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `FSR_31`, `FSR_41`.

## Research expansion — gaps and details to deepen

- Keep user preferences, provider availability, temporal-input completeness, and successful native activation distinct.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Record requested/effective provider, runtime effect version, actual device/API features, internal resolution, and explicit unavailable reasons.
- Require coherent depth, motion, jitter, exposure, reset, and resource-lifetime contracts before enabling a temporal path; spatial FSR evidence does not qualify it.

## Research expansion — acceptance checks

- Save an unavailable preference, restart, and verify preserved intent plus a truthful effective fallback.
- Test camera cuts, resolution changes, disocclusion, water/transparency, and depth preservation before hand/HUD rendering on each accepted backend.

## Research expansion — source anchors

- Deep Primal independent audit — 2026-10-28 13:30 UTC

Related design: [render performance](../../../../../chapters/render-performance.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent audit — 2026-10-28 13:30 UTC

**Pinned source SHA:** [`643ac7903f19b87626ee25f810bfb6f42cf7e0c9`](https://github.com/Shnewbs/Deep-Primal/commit/643ac7903f19b87626ee25f810bfb6f42cf7e0c9); version `0.0.1-alpha.29-dev.55-blendpolicy.1`.

**New confirmed issue:** The newly appended `FSR_31` and `FSR_41` user-preference enum values intentionally return unsupported with `Capabilities(true,true,false)` in [UpscalingPolicy.java L24–32](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/engine-core/src/main/java/com/shnewbs/deepprimal/core/UpscalingPolicy.java#L24-L32). However [UpscalingPolicyChecks.java L11–17](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/src/coreChecks/java/com/shnewbs/deepprimal/core/UpscalingPolicyChecks.java#L11-L17) erroneously requires every non-DLSS mode to become active. [CoreChecks.java L9](https://github.com/Shnewbs/Deep-Primal/blob/643ac7903f19b87626ee25f810bfb6f42cf7e0c9/src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java#L9) calls this test. Both policy and check sources reconstructed with exact Git blobs `ae7beffd40cb988c7e449d6330bfe3d8f4655bbf` / `e5b89272270a0041111366bb9fc73f24fb2cf7af`. Java 21 compilation `--release 21 -Xlint:all -Werror` succeeded and executing with `-ea -Xverify:all` exited **1**, `AssertionError: Spatial/FSR capability never implies DLSS support`. **Correct the test oracle, NOT the safe unavailable-provider fallback.**

**Independent passed source-level checks:** exact unchanged pinned Git blob identities of 10 previous V8 density/materializer files plus LOD capture priority. Java21 isolated Minecraft API-double tests: base height 874,642; section bridge 11,047,019; planned ground 1,235,414; rollback/callback recovery 16,397 — **13,173,472 assertions passed**, strict compilation and verified bytecode. These are not native Minecraft tests.

**New producer work observed, not re-accepted:** the opt-in V8 climate ecotone component reports 7,333,288 Java21 assertions in producer evidence; renderer eviction heap reports 251,940 checks and isolated CPU microbench improvements. Neither was rerun as an exact compiled new source in this audit. No gameplay credit, live FPS or GPU acceptance. The opt-in V8 blended policy is persisted but not connected to native biome/material transitions. FSR 3.1/4.1.1 remain preferences with unavailable temporal backends. Native AA selection with FSR 1 should be hidden/disabled because FSR1 is not temporal AA.

**Ten-category grade:** terrain 5, biomes 4, water/caves 6, structures 4, performance 3, determinism 7, architecture 6, extensibility 4, testing/docs 7, polish/tooling 4. **Total 50/100; average 5.0/10; Tier 2. CARRIED previous full grade, not fresh complete regrade.** Goal strictly >8.5 remains unproven.

**Unrun:** Java25 (toolchain/archive unavailable on this host), full Gradle/NeoForge, Java25 package/JAR CRC and compiled identities, native save/reopen/shutdown/water conservation, Windows/AMD8060S Vulkan/OpenGL, frame-time/VRAM/visual acceptance, real Mojang V8 profile codec and fresh development JAR. Owner's October 27 override assigns full build/JAR/GPU validation to owner; no old/new JAR repackaging or retention mutations. No production source touched by this auditor.

**Priorities:** 1) repair FSR CoreChecks expected capability oracle and Native AA UI semantics; 2) native V8 saved-blend/rollback/legacy save identity; 3) long-soak constituent water accounting and Windows Vulkan/GL rendering.
**Library evidence:** `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1330_643ac790_SourceEvidence.zip` (52,232 bytes, SHA256 `05073b9791e4b5a89ee1bea9e3f8bf31a9f4876accd2fb96581231b8054f079f`); sibling `_REVIEW.md` and `_RECEIPT.json`.


<!-- END PRESERVED SOURCE -->
