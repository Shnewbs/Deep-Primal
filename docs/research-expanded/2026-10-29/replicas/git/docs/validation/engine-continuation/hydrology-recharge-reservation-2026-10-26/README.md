# Expanded replica — docs/validation/engine-continuation/hydrology-recharge-reservation-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/hydrology-recharge-reservation-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/hydrology-recharge-reservation-2026-10-26/README.md |
| Preserved source SHA-256 | `0b59f0e5b38ffb03841f60bc8e549d835e25226c720bac1ec214160f66fe1104` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The staged hydrology reservation that stops source admission 500 microseconds before the existing deadline when regular recharge columns are enabled. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that a costly callback can consume the reservation, so the scheduling improvement is an admission policy rather than a hard wall-clock guarantee. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a scheduler contract that distinguishes reserved admission opportunity, callback cost, source priority and bounded regular work. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat the 256-candidate starvation reproduction, verify enabled and disabled regular modes, then qualify native source quotas and recharge cadence without treating simulated clock timings as measured performance. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Staged regular hydrology admission reservation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Staged regular hydrology admission reservation

Not promoted; not included in the dev.30 JAR. Baseline is production `293516cdb0c62d66ae12a5146e4ac2e6590c45a3`.

After closing the source-column deadline gate, the next exact-method reproduction found that a busy natural-source pass can consume every tick's remaining deadline before the regular visit loop. That loop performs regional recharge/accounting work. With 256 candidates and a deterministic 100 microsecond callback, 100 simulated ticks admit zero regular visits. This is a CPU scheduling reproduction with platform doubles, not an observed Minecraft water-loss or latency measurement.

The candidate gives the source pass a deadline 500 microseconds before the total deadline when regular columns are enabled. Its headwater ordering and finite quota code stay unchanged. `COLUMNS=0` retains the full source-pass admission window. The candidate yields 500 regular visits in the reproduction. Baseline and candidate each pass 3,360 boundary cases across five clock origins, varying survey cost, source count/cost and configured regular breadth.

The full candidate also compiles against current Minecraft/NeoForge binary APIs under Java25: five classes. The first standalone compile failed because source JARs accidentally caused dependency-source compilation. The failed log is retained; the corrected invocation excludes source JARs, uses an empty sourcepath and disables implicit compilation. This is not a full Gradle or native qualification.

Run `probe.py` with `--source` pointing at baseline HydrologyRuntime.java, `--source-pin` identifying that exact commit, `--java-home`, `--fixture` pointing at `../stream-deadline-release-2026-10-26/reproduce-admission.py`, and a fresh `--output` directory. It extracts the actual production tick method and emits the candidate independently of the native runtime.

Before promotion: verify natural quota/headwater ordering and precipitation/recharge cadence in actual packaged native runs, full Java25/core/module/build and package identities, save/reopen/shutdown, and any required visual gates. Keep source/column fairness, loaded/unloaded ownership, long-soak/multiplayer and hardware latency gates open. A single expensive callback can consume the reservation; this is admission budgeting, not a hard wall-clock guarantee. No grade increase or engine completion is claimed.


<!-- END PRESERVED SOURCE -->
