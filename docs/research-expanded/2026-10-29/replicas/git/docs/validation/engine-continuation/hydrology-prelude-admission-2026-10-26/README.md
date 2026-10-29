# Expanded replica — docs/validation/engine-continuation/hydrology-prelude-admission-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/hydrology-prelude-admission-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/hydrology-prelude-admission-2026-10-26/README.md |
| Preserved source SHA-256 | `f0f877aeca9a1e37d828d491f731577ec3e32b4f8c3affca037e0525acd9e878` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

A second wrap-safe source-prelude admission check after index cleanup and before allocation and sorting, preserving the regular recharge loop outside both guards. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the guard prevents newly admitted work after expiry but does not bound a full scan or sort once started. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add separate prelude admission and incremental-work requirements, with exact accounting of which phases can still overrun a callback boundary. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Advance a deterministic clock during cleanup, assert no later allocation or sorting begins after expiry, preserve regular recharge admission and compare with the later incremental-index design. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Source-prelude admission continuation — 2026-10-26

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Source-prelude admission continuation — 2026-10-26
Base main: 771f08937fe966724e0b95c7eb91e826c41c7451. Production dev31 remains unchanged; no new JAR or grade.
Consumed audit docs/engine-hourly-audits/2026-10-26T1627-b198804/REVIEW.md, pin b198804f19db960fba6efc52a76b18c603d50601. The combined recharge/overflow qualification it requested was completed in dev31. This continuation addresses the remaining source scan/sort budget finding.

## Actual staged repair
The prior admission guard checked time only before index cleanup. Cleanup can consume the remaining admission window, after which allocation/sorting still starts. The new staged HydrologyRuntime adds a second wrap-safe admission check after cleanup and before allocating/sorting. The regular recharge loop remains outside both guards. No water quantity/constituent formulas, profiles, art, geography or renderer files change. Do not promote this overlay until full runtime gates pass; it is not part of the delivered dev31 JAR.

## Checks
Actual baseline, single-guard and revised source preludes extracted into a controlled-clock Java harness: 180 cases per variant, covering 0/1/64/1024/8192 streams, stale/empty index entries, tied headwater heights, initially expired/open/cleanup-expired windows, signed clock wrap. Baseline admitted105 expired sorts; single guard45; revised guard0. Open-deadline membership and descending headwater order pass. Normal and Python -OO runs pass; explicit exceptions and subprocess check=True remain active.
The complete candidate HydrologyRuntime compiled against retained Minecraft/NeoForge API dependencies with Java25, binary-only source lookup and no implicit compilation: five class files, exit0. Candidate SHA256 c21da0819f250d557d17080ad8cc3355e1e2a811dd0c7edc383f80474ed54696. This is API compilation plus extracted-method testing, not a native server run.
The retained current core suite was separately rerun with -Xverify:all; see core receipt for final outcome. Core classes were not freshly recompiled this cycle and do not exercise the native HydrologyRuntime integration.

## Limits and coordination
At preflight829MB remained; during qualification available disk reached0. The existing private full project/cache occupy865MB/547MB. New isolated full Gradle/native/package qualification is deferred; no new artifact is claimed. Only builder1732 owns new outputs; previous project/dependency files are read-only. Auditor recorded a17:30 run in progress, and its independent processes were observed; no auditor workspace/process was modified. No timing benchmark or hardware latency comparison was attempted. Personal context confirmed renderer.5 released ownership but did not provide direct fresh ownership evidence from the requested other engine conversation. Root/water claims were checked; water checkpointed.
Current source index cleanup and any sort begun before expiry are still unbounded. The guard prevents starting extra work after expiry, not an absolute2ms guarantee. Next qualify this overlay through full Gradle/module/core/packaged source-quota/recharge/weather/save/reopen gates; then build an incrementally bounded index with exact load/unload/headwater invalidation. Broad world-ledger ownership/catch-up, V8 and real GPU/water soak acceptance remain open.
Last full external score50/100 (5.0/10) carried unchanged. Keep builder enabled; this is a resumable staged checkpoint.

Final retained-core rerun: exit0, HYDROLOGY_PASS randomizedTransfers=500000, final PASS200000 differential surface cases/bounded-cache/tick-ring. At finish464MB free; still below the existing private project/cache footprint before native outputs. Core timing text is not controlled comparative evidence.


<!-- END PRESERVED SOURCE -->
