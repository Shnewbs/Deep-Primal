# Expanded replica — docs/validation/engine-continuation/stream-clock-wrap-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/stream-clock-wrap-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/stream-clock-wrap-2026-10-26/README.md |
| Preserved source SHA-256 | `0141cc35e9c54fa3a51797281dd44b31a317ce2acf2e2833d0a7ad1eb1cc70bb` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Wrap-safe natural-stream scheduling using signed elapsed subtraction at five short-deadline admission checks including the inner column loop. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that an admitted callback can still overrun, so short-interval wrap safety is not a hard real-time guarantee or general arbitrary-duration clock policy. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a monotonic deadline helper with documented valid duration range and separate contracts for scheduling time and saved simulation ticks. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Cross signed nanoTime boundaries at every admission site, preserve quota and headwater ordering, and measure actual callback tails only in later controlled native runs. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Wrap-safe natural-stream scheduling candidate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Wrap-safe natural-stream scheduling candidate

Pinned current main: 53b4d82f351352f52384f55d6f384cf8694f378a.
Production HydrologyRuntime matches SHA256 d09c46e587ec298bcd269b7eaf35e632961e3abcd9a8c21a219d9f373a758b12.
Candidate SHA256 ec9c07dcc1b3b7621fe6b099e3e51d812bda9984dfee270ffb63fa67a820c311.

This extends the previous staged between-column deadline repair. Signed absolute nanoTime comparisons can suppress work when start+budget wraps. Use nanoTime()-deadline < 0 at all five survey/tick admission checks, including the new inner stream-column check. For these short deadlines this signed difference preserves elapsed-time behavior across signed wrap. One call may still overrun; this is not a hard real-time limit. No quota, quantity, geography, profile, material or approved-art changes.

Fresh exact tick-method extraction with deterministic Minecraft/time doubles: baseline 216 characterization checks; candidate 1266 checks, covering six clock origins and 210 workload combinations each, plus six controls. Baseline admits 255 late callbacks in the page overrun control; candidate zero. Wrapped-clock control: baseline zero callbacks despite remaining budget, candidate20. Headwater-first ordering, stale pages and unloaded-page rejection retained. No forced load or fairness guarantee added.

Separate Java25 -Xverify:all arithmetic test passes1200000 checks against elapsed-duration expectations for both1ms survey and2ms tick budgets, with6 legacy disagreements. The full actual survey method was not executed; its two changed comparisons are covered only by the arithmetic test. Tick stub survey cost is controlled, not native behavior.

Commands:
```sh
python3 reproduce.py --source /checkout/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java --java-home /path/to/jdk25 --output /isolated/output
javac -d /isolated/output ClockBoundaryChecks.java
java -Xverify:all -cp /isolated/output ClockBoundaryChecks
```

Initial ordinary-filesystem compile FAILED with "error while writing ChunkPos: No space left on device". Retry in own /dev/shm/deepprimal-stream1210 passed. Host filesystem remains100% full even after removing our previously delivered duplicate dev.28 export. No other checkout/cache/process was changed. Full current Gradle, native source/budget/save/reopen/shutdown and package gates NOT run. This candidate is not promoted, version-bumped, packaged or released. Production and latest delivery remain unchanged; no old JAR is relabeled.

Consumed independent audit: docs/engine-hourly-audits/2026-10-26T1135-93ac6cb/REVIEW.md, pin93ac6cbc8b0377e6a08e3bcc847a41edcd54da0a. Auditor restores scoped score50/100 after precipitation regression repair; eight carried categories mean no complete regrade. Last complete baseline5.0. Chosen repair addresses its bounded natural-stream scheduling priority. Renderer jam2 main changes preserved without taking over that task.

Root/water claims were checkpointed before this bounded writer claim. Auditor recorded completed11:50Z; personal-context search did not establish any newer live cross-chat claim. Git claims are the coordination evidence, not a claim of live chat synchronization. No active exclusive benchmark was reserved.

Next: restore safe build headroom, reconcile latest production, qualify this combined staged candidate with full Java25/module/core plus packaged natural-source and finite-quota/restart tests; then promote with a new version. Actual2ms tails, sustained-overload fairness, loaded/unloaded world ledger, natural OpenGL/Vulkan water surfaces, Windows/AMD8060S and multiplayer/soak remain open. Retain prior staged overlays. Builder remains enabled.


<!-- END PRESERVED SOURCE -->
