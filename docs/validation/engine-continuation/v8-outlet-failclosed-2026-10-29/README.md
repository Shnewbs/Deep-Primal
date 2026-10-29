# Deep Primal dev90 — fail-closed durable V8 outlet checkpoint recovery

Source-only version `0.0.1-alpha.29-dev.90-outlet-failclosed.1`; opt-in V8 engine-core only. Direct main scoped change; this is not a playable build.

## Change

`V8OutletLedgerStore` now refuses to open or transact when an unacknowledged `.pending` image exists. It preserves the original bytes, leaves previously accepted generation unchanged, and poisons an in-flight instance after a rejected transaction. A world owner must separately reconcile any possibly delivered native cells and archive/remove the pending image before attempting reopen; **never infer physical commit from a rename or overwrite pending automatically**.

The store resolves parent-directory symlink aliases before choosing its one-writer lock name. It refuses symlinked checkpoint, lock and pending paths; on Unix providers with `unix:nlink`, it also refuses hardlinked checkpoint aliases. Transaction admission rechecks path identity. This prevents independent aliases using different lock paths from accepting conflicting water/salt/sediment debits in the exercised filesystem model. This is **not a guarantee against hostile TOCTOU races, unsupported Windows hardlinks/network filesystems, or a real Minecraft crash**.

No change to the canonical V1/V2 on-disk wire envelope, world/profile identity, existing record hashes, volume/constituent accounting, saved geography, graphs, ecology, physical liquid stage, or renderer. Old checkpoints without orphan pending frames reopen as before. A world with a pending frame now deliberately fails closed rather than silently reusing/deleting it; native reconciliation must be designed before gameplay activation.

## Source identity and focused tests

Exact tested source/test/runner Git blobs:
- `V8OutletLedgerStore.java`: `0a7e659432e8ef796e555ea6f73db82fd374fe92`
- `V8OutletLedgerStoreChecks.java`: `5351ef22c5d0abe694ced3e38c9ef72da07c4734`
- `test_v8_outlet_store.py`: `b7462050915ee7ceb7e0a650925c3679a1e13249`

Strict OpenJDK **21.0.12.1** `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`; `python3 tools/test_v8_outlet_store.py --output ...` and `python3 -OO tools/test_v8_outlet_store.py --output ...`. Each passing Java process executed **149 assertions**, maintained generation 42 and exact triple totals `[800,420,260]`; all **8 compiled intentionally broken implementations failed** under each mode (16/16 across both modes). Tested V1 legacy and V2 world-bound reopen, competing writers, CAS/replay, checksums, canonical ordering, symlink/hardlink directory aliases, pending-frame preservation. First local runner attempt expected historical 133 test assertions despite passing Java tests; its failure was corrected to 149 and retained separately, not counted as a passing process. An attempted broader authority regression timed out; it is not a passing result. Source/test/runner Git blobs match files tested on Linux.

## Coordination and outstanding gates

Base main and git scoped claims checked before lease. Latest completed independent audit `docs/engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md` pins Dev87 source `5f7c3f680e47bd29d03a90cd09c5c34e202f09e7`; its native realized-vegetation receipt finding was already addressed by the separate dev88 terrain commit `16284bd98c9fcfbad7bd33752a2cd558aa534e20`. Root work-state is stale Dev53 and unchanged; a **complete pinned local root validator was not performed**. Terrain/renderer/HydrologyRuntime/gradle files are untouched.

This is source-only Java21 filesystem evidence. The owner retains full Java25/Gradle9.2.1/Minecraft/GPU builds and JAR publication. No real NeoForge loaded/unloaded cell transaction, restart reconciliation, world/profile reopen, Windows filesystem proof, ocean/atmosphere circulation, native water conservation, graphics, FPS or water acceptance is claimed. V8 terrain ecotone/vegetation/relief remains opt-in and requires native materializer, save/reopen, signed coordinate, old/new border, edit, water and visual transect acceptance.

Next: bind native water cell state to a journaled once-only account transaction and crash reconciliation; keep terrain/V8 versioned relief and vegetation acceptance active. Independent full grade remains **50/100 (5.0/10)** until fresh full regrade and strictly >8.5 plus all water gates.
