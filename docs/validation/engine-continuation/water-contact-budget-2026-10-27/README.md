# Fixed-residency water contact budget — source candidate

`dev.32-contact-candidate.2` advances the staged native chemistry integration from
`4aa62dd72fe72090efc07630d8ee3a1a5c46accd`. It remains excluded from production and
from the qualified dev.31 JAR. No grade or water acceptance gate is promoted.

## Engine change

`WaterContactBudget` replaces the adapter's per-contact `Edge` objects and
`HashSet` growth with two fixed primitive key arrays and generation stamps.
Exact unordered packed-position pairs, including all signed long values, remain
the identity; a hash collision never substitutes for equality. Every attempted
neighbor still spends one of the existing 2,048 visits. Retained pairs are capped
independently at 2,048. The existing water FIFO owns deferred work.

At the native limit the table has 4,096 slots: 81,920 raw array bytes per active
level, plus headers and the budget/weak-map entry. This is an upfront allocation
even for sparse activity. No level reference is retained by the value. Tick
changes reset counters and advance a generation without clearing the key arrays;
reusing the empty generation clears the bounded stamp array. Backward/wrapping
game clocks admit one budget, never elapsed-time catch-up. Each lookup is bounded
by table capacity; adversarial collisions can still be expensive. This is not a
hard 2ms tick guarantee.

Water amounts, constituents, ocean-boundary overrides, solver activation,
physical vertical-contact rules, saved-data codecs and world block writes are
unchanged. The helper allocates nothing during normal contact operations; the
surrounding adapter still allocates positions/accounts/parcels and existing
scheduling structures. No whole-engine allocation or latency gain is claimed.

## Evidence and limitations

See `checks.json`, raw test/native logs and `diagnostic-package.json` for final
results. `WaterContactBudgetChecks` compares against an independent exact pair
set, tests colliding keys and generation reuse, and measures helper-only thread
allocation after warmup. Two broken controls omit the second key or stamp reset;
both must be rejected. The existing contact fixture tests individual water and
constituent conservation, reciprocal admission, dry/unloaded borders, player
edits, lifecycle and bounded retries.

The complete independent core snapshot is anchored to the committed audit's
Git/SHA256 manifest (139 baseline sources), with the staged WaterAccount and new
helper overlaid. Current non-doc Git root identities were checked separately;
the broad compare reached its 300-file limit and was not used as proof of an
unchanged production tree. Actual Minecraft/NeoForge API compilation and Linux
fresh/reopen diagnostics use private copies of immutable completed auditor
inputs. Four loaded candidate class identities are checked. Seven private split
archives pass CRC, duplicate, fixture-exclusion and compiled-byte checks.

These native diagnostics overlay fresh candidate classes into a qualified dev.31
baseline and load its modules separately. They are not a full Gradle producer or
a releasable distribution. Initial ENOSPC core/logging failures are retained and
excluded; the final receipts govern qualification. Offline public-key DNS
warnings are narrowly identified in native receipts, without multiplayer claims.

Outstanding: combined source-index/contact full Java25/Gradle producer and module
gates; complete bundled source/compiled/package identity and release; weather,
travel and loaded/unloaded ownership integration; real OpenGL/Vulkan scenes;
Windows/AMD8060S; sustained multiplayer/soak; named-hardware tick/frame latency.
No geographic, art or staged R57/R60/R61/R62 change is included.

## Coordination and audit

Consumed `docs/engine-hourly-audits/2026-10-27T0230-2e15d24/REVIEW.md`, pinned to
`2e15d246c76b047c57cd8f5ce0b70d9219d3b894`. It carries the original 50/100 (5.0/10)
baseline without a fresh regrade. Its combined integration and bounded-work gaps
remain open. The foreground water claim was checkpointed. The active renderer
claim covers separate telemetry sources, with no exclusive timing reservation.
Current auditor/builder run records and Personal Context were consulted; no fresh
direct ownership evidence from the original engine chat was available.

Next: qualify the combined staged source-index and contact changes with an
isolated full producer when capacity permits, then exercise weather/travel,
persistent single-owner transfers and actual visual/performance acceptance.

## Late-main reconciliation

After the water source checkpoint, main integrated qualified renderer telemetry
at `2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e`. Its four core/test source blobs were
fetched and verified exactly; the combined private core snapshot now has 143
sources (142 production-baseline/check sources plus the new water helper).
The initial 140-source core run passed before this main update. Final combined
results are recorded separately. One trailing EOF newline lost during copying
was restored; an independent clean recompile produced all 229 identical classes.
No renderer source was edited or replaced in main. Native contact diagnostics
remain explicitly pinned to the earlier dev.31 baseline with the water overlay;
they do not claim the combined renderer distribution was exercised.
