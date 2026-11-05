# Dev83 V8 conserved outlet ledger (source-only) — October 29, 2026

Source version: `0.0.1-alpha.29-dev.83-outlet-ledger.1`.
Independent auditor read: `docs/engine-hourly-audits/2026-10-29T1635-a7ade50e/REVIEW.md`, pinned at `a7ade50ea8aac1458db8f2d6b1461e16967bc00d`. Last complete independent grade remains **50/100 = 5.0/10**. GitHub source baseline at scope acquisition: `663ef9f26f8e5740fe9325f7c1eaab3013adacaf`. Root work-state is still pinned to Dev53 and was deliberately not edited without a complete checkout and the required pinned validator.

## Scope

New dependency-free `V8ConservedOutletLedger` models bounded, independently saved physical water/salt/sediment accounts at signed, 32-grid-aligned watershed ports. It moves all three integer quantities atomically from an existing funded account to its explicitly authorized downstream account. No volume is synthesized from catchment area, sea level or chemistry. The receiver graph is checked for cycles, including disconnected cycles. A successful batch yields a new immutable state; an invalid batch cannot publish any partial mutation. Sequence identity per source, including the last complete receipt, is included in the snapshot, so restart-style snapshot reconstruction rejects duplicate, stale, conflicting or gapped commits. Exactly matching last-sequence replay is a no-op.

This is a **core authority prototype**, not native physics adoption. The caller must atomically persist the *whole* returned snapshot in a durable world-level transaction before acknowledging delivery. No native serialized codec, journal, load/unload integration, reconciliation of topology edits or crash-recovery fsync has yet been implemented. Caller-supplied receiver edges are not a global ocean certificate. Geometry/stage and presentation water heights remain separate and unchanged; frozen V1–V7 and existing V8 saves retain their old behavior. No source/volume from thin air or salt/sediment constituent conversion is added. No renderer code, approved textures or player edits are changed.

## Focused checks and identity

- OpenJDK `21.0.12.1`, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- Normal runner: `V8_OUTLET_LEDGER_PASS assertions=40134 accounts=12 receipts=11`. Three independently compiled broken variants (duplicate replay, credited water without a source debit, lost receipt persistence) were rejected. The complete result repeated under Python `-OO` with the same assertions and three rejected controls.
- Covers negative signed regional coordinates, capacity/overdraft/overflow, authorized receiver forest, conflicting receipt, in-batch duplicate, out-of-order sequence, snapshot reopen, 10,000 deterministic randomized transfer attempts, replay and global quantities exactly conserved.
- Pinned old topology helper compiled alongside the new source: Git blob `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a` matched the current production repository blob used for this isolated run.
- Exact local Git source blob: `e41226dbf0daf40ea455ee44247e2c4be81f29e9`; test blob `c1d85518ad4e456ce56e521ccdfc0edecf82aec8`; runner blob `8b666d2a03bcb99fd7b28b029d29807b24f11fc2`.
- Run from a full checkout: `python tools/test_v8_outlet_ledger.py --output /absolute/path/to/new-ledger-results`. Runner uses a fresh temporary classes directory and does not modify global Java.

## Unaccepted gates and next action

These are isolated Java21 checks against dependency-free production source. They are not Java25/Gradle 9.2.1, full Minecraft/NeoForge, native saved-world restart, journal durability, real physical chunk transfer, GPU, two-API water continuity, volume conservation across the actual simulator, bounded server tick latency, or a qualified game JAR. The owner retains full builds and publication. No score change is claimed.

Next: persist one authority-owned snapshot with crash-safe, exact-once integration to the native finite-water storage and unload/reopen path; certify incoming/outgoing global ocean/routed outlets and account for environmental sources and sinks. In parallel, continue opt-in V8 ecotone vegetation/relief activation and owner-side native blending acceptance.
