# Expanded replica — docs/engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-28T1628-1e6e530/REVIEW.md |
| Preserved source SHA-256 | `ee45fa04bebcf303186961fc4ab7baa375afe5410aee91bdfc2c2461646b208a` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Independent engine audit — 2026-10-28 16:28 UTC”. Its original opening states: “Strictly >8.5/10 target unmet. Unused, staged or unregistered V8 previews earn no shipped gameplay score increase.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Extend the recorded generation change with saved-revision, chunk-order, boundary, and authoritative-water compatibility checks.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Freeze existing generator/channel/material recipes; new terrain behavior requires an explicit saved revision and complete dependency identity.
- Distinguish surface appearance, regional drainage plans, ocean connectivity, and live finite-water ownership in the acceptance record.

## Research expansion — acceptance checks

- Generate the same signed-coordinate corpus in reversed and randomized chunk orders, then compare overlap, seams, and saved-profile replay.
- Reopen with creation defaults changed and verify old geography, player edits, water quantity, and material identities remain governed by the saved recipe.

## Research expansion — source anchors

- Independent engine audit — 2026-10-28 16:28 UTC
- Unrun / limitations
- Next

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Independent engine audit — 2026-10-28 16:28 UTC
Pinned main source [1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0](https://github.com/Shnewbs/Deep-Primal/commit/1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0); Dev58 version 0.0.1-alpha.29-dev.58-ecotone-native.1. Auditor read-only for production and other roles, no new JAR per 2026-10-27 owner-build override.

## Fresh independent evidence
- Reconstructed exact production V8EcotoneColumnPlan.java (3,876 bytes) and verified Git blob 89b9e298b7d246fa1ba40990138c7a62c0fd1a7c.
- Compiled actual production class with isolated lightweight API doubles using Java 21, --release 21 -Xlint:all -Werror. All **469,256** independent assertions passed over 512 randomized tests: signed absolute coordinates, river/ocean exclusion, soil-depth/original-height bounds, exact cover oracle and repeatability. Did not execute Minecraft or real Mojang APIs.
- Negative coordinate mutant compiled, then failed at assertion 7 as intended.
- Source finding [DensityChunkMaterializer.java L209-L218](https://github.com/Shnewbs/Deep-Primal/blob/1e6e530fc76d5997eb2ec1abc0ea49f6570ef7f0/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L209-L218): once snow material is cached, a second snowPatch column enters the `else switch(cover)` and unnecessarily resolves grass/podzol/dirt. Source-extracted Java21 demonstration got 1 snow + 1 redundant grass lookup. Low-severity dependency/efficiency bug, not a reproduced native failure. Builder fix: nest snow lookup under `if (snowPatch) { ... } else switch`.
- Dev58 Builder reported 14,150,419 focused Java21 assertions; renderer same-ring jam reported 144,361 assertions and ~44.9% lower matched p50 allocation, ~17–23% lower CPU p50. **These are producer evidence, not auditor reruns or GPU FPS proof.**
- Eight commits since prior full audit pin cec6143; changed only V8 opt-in path, LOD disk same-ring selection, version, and evidence. No known V1–V7, water or player-edit code changes from this commit range.
- The root ENGINE-WORK-STATE.json still reports dev53 from 12:04 UTC; separate TERRAIN-BLENDING-WORK-STATE.json records dev58 completed at 16:17 UTC. Do not interpret stale root state as active ownership.

## Complete ten-category grade carried, NOT a fresh whole-engine regrade
| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/docs | 7 |
| Polish/tooling | 4 |
| **Total / Tier 2** | **50/100 = 5.0/10** |

Strictly >8.5/10 target unmet. Unused, staged or unregistered V8 previews earn no shipped gameplay score increase.

## Unrun / limitations
JDK25 not available in this workspace or matched in Library title search; Linux Java21 used. Full Gradle/NeoForge, current JAR packaging/outer and nested CRCs/class identities, native JSON/NBT and save/reopen, real climate provider and independent pre-generation baseColumn, finite-water unloading/chemistry, Windows/AMD8060S Vulkan/OpenGL and GPU/water/LOD/FSR 3/4 p95/p99: **not run**. Direct GitHub clone DNS blocked; connector source read succeeded. No JAR or release qualified, saved, removed or published. October 27 override assigns full builds to owner.

Independent source/Java tests, logs, SHA and reproduction instructions saved in CRC-verified Library ZIP `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1628_1e6e530_SourceEvidence.zip`, 10,620 bytes, SHA256 fd c1 fd 15 (full receipt value below). Auditor did not touch builder/renderer checkouts, caches, leases, timing slots or workloads.

## Next
Native V8 saved-authoritative climate/codec and before-generation column parity, ecotone relief/terrain without breaking frozen V1–V7, water volume/salt/sediment unload/edit/reopen, plus user-hardware GPU/camera/travel/moving-water performance. Correct snow material preflight when Builder scope permits. Observational audit is not a builder stop gate.

<!-- END PRESERVED SOURCE -->
