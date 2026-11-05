# Expanded replica — docs/engine-hourly-audits/2026-10-27T1933-a765774/REVIEW.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T1933-a765774/REVIEW.md) |
| Source identity | git: docs/engine-hourly-audits/2026-10-27T1933-a765774/REVIEW.md |
| Preserved source SHA-256 | `149fd8421a38f1f45b32a1f05c5f41db9607e9e322b0bc7a5cddf0678770f5e4` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal independent audit — 2026-10-27 19:33 UTC”. Its original opening states: “Pinned production snapshot: a7657749969c46e7296af3b8d822c9a14367e84f. Read-only analysis; engine candidate 0.0.1-alpha.29-dev.35-heightadmission.1 and renderer signed-distance commit 52635aa9c774c61c20271102b7719d49985ef1e5. This audit produced no code changes and no JAR. Full Gradle/Minecraft/GPU builds belong to the owner under the October 27 override.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `LodChunkDistance.java`, `LodChunkDistanceChecks.java`, `LodNormalRangeParityChecks.java`, `HydrologyRuntime.place`.

## Research expansion — gaps and details to deepen

- Carry the water finding through the real native callback boundary while preserving quantity, constituent identity, and exactly-once ownership.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Bind write scopes to world, level, immutable cell coordinates, expected state, and revision; unrelated callbacks must retain their own invalidation and accounting.
- Recheck final state after synchronous callbacks before committing water/salt/sediment ledgers; define bounded compensation and explicit failure when restoration is impossible.

## Research expansion — acceptance checks

- Exercise refused-but-applied and accepted-but-replaced writes, same-volume chemistry changes, unrelated-cell mutation, cross-level callbacks, and unload during commit.
- Compare joint loaded/unloaded/reservoir quantity and composition before and after edit, save, restart, and reopen using the actual host runtime.

## Research expansion — source anchors

- Deep Primal independent audit — 2026-10-27 19:33 UTC
- Focused independently verified results

Related design: [terrain water](../../../../../chapters/terrain-water.md)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal independent audit — 2026-10-27 19:33 UTC

**Pinned production snapshot:** `a7657749969c46e7296af3b8d822c9a14367e84f`. Read-only analysis; engine candidate `0.0.1-alpha.29-dev.35-heightadmission.1` and renderer signed-distance commit `52635aa9c774c61c20271102b7719d49985ef1e5`. This audit produced no code changes and no JAR. Full Gradle/Minecraft/GPU builds belong to the owner under the October 27 override.

## Focused independently verified results

- **PASS, JDK25.0.5+8**: `javac --release 25` and `java -Xverify:all` on three Git-blob-verified Java files from the pinned tree: `LodChunkDistance.java` blob `a40a0cd81cab1cddace4a17b6db24f5838351857`, `LodChunkDistanceChecks.java` blob `8d95e911b0bff6496ed63e99a8c58c4f34aa806c`, `LodNormalRangeParityChecks.java` blob `5a4c684a8be474b7cf640c71b513f883ecf91112`. Results: `LOD_SIGNED_DISTANCE_PASS exactOracle=100008`; `LOD_NORMAL_RANGE_PARITY_PASS comparisons=1000000`. Not a native rendering or whole-engine check.
- **Source-level repaired, native pending**: dev35 `HydrologyRuntime.place`, `exposedColumnCell` and `waterColumnBed` now test both bounds and loaded chunk before block-state access. This addresses the previous 18:34 audit's missing explicit height test. Native loaded/unloaded callbacks, rollback fault injection, constituent ledger/save/reopen remain unverified.
- **NEW testing weakness, audit-only negative control**: The committed `tools/validate_hydrology_height_admission.py` uses twelve text-order predicates and one `place` mutation control. In-memory replacement of `exposedColumnCell`'s `!isInWorldBounds(p) || !hasChunkAt(p)` with unsafe `&&` still passed all 12 predicates; the independent `place` mutation control continued working. No repository file was modified for this probe. Builder should add semantic negative cases for exposed-column and bed paths, plus Minecraft out-of-Y/loaded-XZ and in-Y/unloaded tests.

## Original strict ten-category rubric (historical, NOT regraded)

Terrain 5; biome blending 4; hydrology/caves 6; features/structures 4; performance/threading 3; determinism 7; architecture 6; extensibility 4; testing/benchmarks/docs 7; polish/tooling 4. **Total 50/100, 5.0/10, Tier 2 carried** from `docs/engine-review-2026-10-24/REVIEW.md`. A full independent >8.5/10 is NOT demonstrated.

**Not run:** Gradle/NeoForge, current JAR CRC/package/native payload/source-class parity, save/reopen/shutdown or conservation/multiplayer soak, actual Vulkan/OpenGL waterfall frames, AMD8060S GPU p50/p95/p99. The shell still fails github.com DNS; the user requested handling full builds. No old JAR was repackaged or published. No builder lease/output/cache was touched. Scoped evidence is retained separately in the auditor run's verified ZIP.

**Priorities:** semantic load/bounds regression tests, real native rollback and volume/constituent persistence, then GPU temporal waterfall continuity and measured performance. Neither builder nor renderer task was paused.


<!-- END PRESERVED SOURCE -->
