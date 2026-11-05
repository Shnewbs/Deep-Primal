# Expanded replica — docs/validation/engine-continuation/aquifer-persistence-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/aquifer-persistence-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/aquifer-persistence-2026-10-26/README.md |
| Preserved source SHA-256 | `7427ccf403022acd6eb97062a75958e1104a6ccc522d094ee395c6f9622a3de1` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Qualification of the Aquifer elapsed-tick repair after a valid Long.MIN_VALUE saved timestamp made subtraction overflow when stepped at zero. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the repaired method advances at most one fixed step and creates no catch-up water; this extreme counter case does not explain an ordinary startup timeout. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a persisted-clock contract with ordered comparisons, overflow-safe elapsed tests and explicit single-step accounting. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Round-trip extreme timestamps through the real codec and packaged save/reopen path, reject non-forward ticks and verify that long pauses never mint accumulated recharge. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Dev.27 aquifer clock persistence qualification

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev.27 aquifer clock persistence qualification

This promotes the staged `Aquifer.step` elapsed-time repair from
`7f1d6310accf40649f25bf2f6698cec1949940f3`. The signed long subtraction could
overflow when a valid saved tick was `Long.MIN_VALUE` and the next tick was zero,
incorrectly rejecting an eligible step. Ordered timestamps are now checked first;
a negative elapsed subtraction then represents a very large positive interval.
The method still advances at most one fixed step and never creates catch-up water.
This is an extreme persisted-counter/API regression, not an observed ordinary
world stall or an explanation of the previous auditor's startup timeout.

Production version advances from alpha.29-dev.26-water.1 to alpha.29-dev.27.
Frozen V1–V7 geography, quantities/constituents, player edits, approved art, Themes,
Vulkan primary/OpenGL fallback and unaccepted staged optimizers are preserved.
Concurrent commit e62c669b adds a private WaterfallUploadChecks extension and
AMD GPU receipts; it is preserved and its fixture is included in compilation.
Those GPU receipts test the earlier 3acffb22 artifact, not this dev.27 JAR.

## Evidence

- Prior staged evidence: 50,144 signed-timestamp cases against a BigInteger
  oracle (baseline failure retained), independent 130-input complete Java25 core
  suite, full 35-task build, 587 product and 142 independent core class identities,
  seven valid archive CRCs and 208 private class exclusions.
- Four candidate packaged Linux server sessions passed: default fresh/reopen and
  channel-5 opt-in fresh/reopen. Explicit native pass records, replay hashes,
  loaded class identities, acknowledged flush, ordered shutdown and final process
  exit were checked. The opt-in creation toggle was disabled before reopening;
  saved channel revision 5 remained. No timeout/retry in these four sessions.
- Added private AquiferPersistenceChecks and three further packaged Linux
  sessions: fresh, reopen and verify. Two actual process restarts retain depth 1
  and 64 accounts, Long.MIN_VALUE and then zero saved timestamps, exact groundwater
  and snow water/salt/sediment. The huge gap increments ages exactly once; duplicate
  and 19-tick visits cannot advance. The second restart proves the new clock/age
  were actually persisted. Remote fixture regions remain unloaded.
- Validation uses Java25/Gradle9.2.1, a private copied dependency cache, private
  1024MiB native test heaps and localhost-only disposable worlds. Product defaults
  are unchanged. Functional validation is not a performance benchmark.
- Native logs and the baseline failure remain locally retained; compact results,
  executable harnesses and explicit pass records are committed here. Final
  exact-source rebuild/package/delivery receipt follows promotion.

## Coordination and limits

Consumed independent audit: docs/engine-hourly-audits/2026-10-26T0653-0818658/REVIEW.md,
pinned source 081865869425a1ce7d7148d6782ee485218549d1. Its water persistence finding
motivates the added restart check. Last full external grade stays 50/100, 5.0/10;
this is not a full regrade or completion of water persistence acceptance.

Git leases were expired at acquisition; the auditor's recorded run was completed.
Personal context returned historical foreground water activity, not a live claim.
Host process inventory was unavailable (ps reported a library lookup failure).
No other checkout/cache/output was modified. The concurrent GPU receipt/fixture
commit is retained as the promotion parent rather than overwritten.

Not run here: Windows/AMD execution of dev.27, rendered OpenGL/Vulkan scenes,
cross-page live waterfall pixels, controlled frame/tick latency, multiplayer or
long soak, complete coarse/loaded world ledger, V8 topology/native saved graph
integration. GitHub release/asset upload is unavailable in the connected tools
and no gh executable is installed; do not claim a hosted prerelease exists.
Next: broader water loaded/unloaded/edit/save accounting, bounded transfer failure
handling and V8 saved-profile/topology gates. Builder remains enabled.


<!-- END PRESERVED SOURCE -->
