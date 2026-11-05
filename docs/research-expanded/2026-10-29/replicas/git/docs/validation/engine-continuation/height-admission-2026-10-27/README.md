# Expanded replica — docs/validation/engine-continuation/height-admission-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/height-admission-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/height-admission-2026-10-27/README.md |
| Preserved source SHA-256 | `be2496fa32d0ccbf77a142799f53a58dc0f937ac2ff4ce0896c9b4ff14445e24` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev35 bounds and loaded-chunk admission before hydrology placement reads, plus bounded exposed-column and submerged-bed walks that fail closed. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the triggering audit finding was a missing source guard rather than a verified native fault; this checkpoint is source-only. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add one resident-read contract specifying height range, FULL availability, walk limits and exactly one finite-block quantity without implicit chunk loading. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Probe negative and upper height boundaries, unloaded intermediate cells and source/ocean markers, then run native conservation and save/reopen cases against the owner-built artifact. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev35: bounded loaded-world water reads (2026-10-27)

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev35: bounded loaded-world water reads (2026-10-27)

Source-only engine implementation, **not a development build or release**.

- Base production main: `c17295b7f79163b2c1602da53f782a75fa6e9f83`. Consumed newly completed independent audit `docs/engine-hourly-audits/2026-10-27T1834-ed919c35/REVIEW.md`, pinned `ed919c35a933ae54b0b8f9bee5c910d71a8be953`. The auditor's observation was a missing height gate in `HydrologyRuntime.place`, not a verified Minecraft fault.
- `place` now requires in-bounds and loaded chunk before any native block-state read and accepts exactly one full finite block (4096 quanta); source/ocean markers remain excluded.
- Shared bounded eight-cell exposed-column and sixteen-cell submerged-bed walks fail closed on out-of-world/unloaded cells. Used by precipitation, chunk survey and aquifer visit, replacing multiple repeated reads while keeping original search caps. No changes to water transfer arithmetic, constituents, geographic revision, material art, saved format, renderer/LOD or player edits.
- JDK25 Linux **isolated algorithm stand-in** passed `WATER_HEIGHT_ADMISSION_STANDIN_PASS checks=478944 illegalReads=0 samples=100000` with randomized world minimum/maximum heights, synthetic unloaded cells and seven-to-sixteen-cell walks. This is stand-in code, **not the NeoForge production class compiled**. A bounded-source/static admission precheck passed 8/8. The repeatable repository contract checker is `python3 tools/validate_hydrology_height_admission.py`; its results must be rerun in the owner's complete checkout.
- Full Java25 Gradle / NeoForge compiler, Minecraft loaded/unloaded callbacks, rollback fault injection, native save/reopen, exact constituent ledger, multiplayer, Vulkan/OpenGL waterfall and AMD frame-time/perf **not run** on this host (full source/dependency checkout and GPU unavailable). Owner will perform builds under 2026-10-27 override; no JAR or release claimed.
- Last externally accepted full score remains **50/100 (5.0/10)**, carried, not a regrade. Water acceptance incomplete. Next: production recompilation and native loaded/unloaded boundary + conservation/save-reopen; advance staged contact integration only after its gates.


<!-- END PRESERVED SOURCE -->
