# Expanded replica — docs/validation/engine-continuation/work-state-structure-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/work-state-structure-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/work-state-structure-2026-10-26/README.md |
| Preserved source SHA-256 | `044f405dc61497ae10028e4536d9c9bcbc4cba836ec9d47b4c9b1a40e18e64ed` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repair of a structurally valid work-state JSON whose wrapper hid coordination fields and duplicate key discarded a different channel-revision record. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that ordinary JSON parsing did not catch structural or duplicate-key loss, and newly preserved engine source still failed an independent core compile gate. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a work-state schema with duplicate-key rejection, explicit root ownership fields and evidence reconciliation separate from product readiness. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Load duplicate and wrongly nested fixtures through the strict validator, retain both recovered revision records and prohibit a qualified-artifact label for source that has not passed its own build. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Work-state structure repair and current-source build reproduction

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Work-state structure repair and current-source build reproduction

Base: `26aa06bdcc2108ad3ffdb1907cdd59bef68850ab`. The latest completed committed external audit remains `docs/engine-hourly-audits/2026-10-25T2249-7000679/REVIEW.md` (pin7000679); its old staged-overflow findings were already closed by dev.22. No new full regrade:50/100 carried forward.

The work-state JSON parsed successfully with ordinary JSON readers, but contained only10root fields. Its revision-five wrapper accidentally enclosed87coordination/history fields. A duplicate `latest_channel_revision_four` key silently discarded the first record, which actually described revision5. Recovered that first record without discarding the later revision4record, restored root ownership/acceptance/evidence fields and retained newer owner engine source. The root gate was reconciled with completed dev.22 evidence, rather than claiming its repairs remained staged.

Added `tools/validate_engine_work_state.py`: duplicate-key rejection at every depth, required root coordination fields, timestamp/active-lease order and revision-record identity. It is read-only and uses explicit exceptions, including under optimized Python. Twelve regression tests pass in each normal/-O/-OO mode. The actual base file fails with duplicate-key diagnostics; restored/final states pass. Unknown fields are preserved; this is not a lock service, full JSON schema or proof that a lease is currently unexpired. Callers still compare real time, inspect overlapping claims and guard Git updates.

Additional current-source reproduction: all74core Java files match exact Git blob hashes, then standalone JDK25 compilation exits1 with84errors. Examples: `StabilityRuntime` imports NeoForge ModConfigSpec; `ChunkMaterializer` imports the mod-layer DensityChunkMaterializer; other new core classes import dashboard/settings classes. See `current-core-compile.log` and `core-manifest.json`. These newer owner changes were preserved, not silently removed or treated as qualified. Full Gradle/native/package checks were not run: the current core already fails its independent compilation gate, and storage is constrained (overlay0free/shared memory65MB at start). No new runtime change, version bump or JAR/release. Previously delivered dev.22 is qualified only for source7aa3ae3.

Next: resolve module dependency direction and compile/API errors in the newly added engine facade/configuration, then full build/native/package validation before delivering a new uniquely versioned JAR. Preserve frozen geography, water quantity, owner art and staged overlays. No grade increase.


<!-- END PRESERVED SOURCE -->
