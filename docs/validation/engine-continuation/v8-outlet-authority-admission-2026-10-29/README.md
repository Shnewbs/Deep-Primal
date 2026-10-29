# Deep Primal dev89 — certified V8 outlet authority admission (source-only)

**Scope:** The production `V8OutletAuthorityAdmission` now bridges bounded, observed `WatershedOutletTopology` ports to the existing conserved `V8ConservedOutletLedger` and world/profile-bound `V8OutletLedgerStore`. It recomputes the topology rather than accepting an unvalidated caller-created `Solution`, requires a complete explicit marine-root set and independently attested *directed* receiver edges, rejects every unknown frontier, any missing/extra physical account, runoff-as-physical-volume, disconnected or unresolved basins, and global 64-bit water/salt/sediment overflow. It admits a new store only through the explicit V2 world/profile-bound API. The returned admission cannot be forged via a public constructor.

This is not a native Minecraft water transaction. The independent receiver and marine-domain witnesses are supplied by the native owner; a topology solver cannot prove them. The caller also supplies already-existing funded balances; watershed catchment area, stage and planned runoff **never mint water**. It does not register a V8 generator, change saved terrain, write finite fluid cells, or claim ocean connectivity from mere coastline height. No volume or presentation-height mixing occurs. V1–V7 and previous V8 snapshot formats remain intact; no saved-world adoption or migration.

## Coordination and source checkpoint

- Main base observed at `16284bd98c9fcfbad7bd33752a2cd558aa534e20` (dev88 native vegetation implementation). Latest complete available independent audit: `docs/engine-hourly-audits/2026-10-29T2131-5f7c3f68/REVIEW.md`, pinned `5f7c3f680e47bd29d03a90cd09c5c34e202f09e7`. Its foliage-receipt finding was already actively claimed by the separate dev88 terrain writer. Did not edit its `DensityChunkMaterializer`, tests, `gradle.properties`, or renderer paths.
- Stale foreground water scope last updated 2026-10-28; no fresh confirmation of its owner. Work is confined to a newly named engine-core source and isolated new tests.
- Scoped writer lease: adjacent `LEASE.json`; independent from renderer and terrain claims.
- Root `ENGINE-WORK-STATE.json` (dev53) is unchanged. A **complete pinned** root work-state validator could not run in this isolated subset; no claim of root ownership or refreshed root state.

## Focused reproducible checks

- Local OpenJDK 21.0.12.1, strict `javac --release 21 -Xlint:all -Werror`, and `java -ea -Xverify:all`. Isolated checks are Java21 source component checks using actual committed prior outlet topology/ledger/store classes, **not** native NeoForge, Java25 full Gradle, saved Minecraft profile or GPU tests.
- `python3 tools/test_v8_outlet_authority.py --output <scratch>` and `python3 -OO tools/test_v8_outlet_authority.py --output <other-scratch>` each pass **8,220 assertions** including 2,048 signed-coordinate/permuted 3-port cases, certified directed marine receiver forests, exact triple conservation and actual local V2 file/reopen/replay. Each runner rejects six individually compiled broken implementations by actual Java AssertionError (12 of 12 across modes), including unknown ocean frontier, phantom runoff, false marine roots, invented accounts, unverified receiver and overflowing totals.
- Unmodified previous `tools/test_v8_outlet_store.py` also passed normally and under Python `-OO`: **133 assertions per process**, all five compiled controls rejected per mode; existing V1 compatibility and V2 world/profile binding, CAS, and receipts remain passing.
- Negative testing deliberately retained separately; a Java fixture initially used an *upstream* unknown-frontier component, which the pre-existing topology status already rejected even when the new guard was mutated. The fixture was strengthened with an isolated unknown **marine root** to ensure the guard itself is exercised. The final positive and mutation runners both pass.

## Explicit unresolved production gates

The new bound store remains explicit-call-only: **do not treat it as activated physical water**. Native ownership must independently attest the saved marine roots and direction, obtain native cells' one-time funded balances, and implement atomic/journaled reconcile of loaded/unloaded native cells with the outlet store across failure and restart. Verify world/profile identity from native saved data and reject old/new generation boundaries unless a versioned policy permits them. Then test finite water/salt/sediment conservation, multiplayer/restart/edits, bounded scheduling, and real OpenGL/Vulkan seasonal river/waterfall rendering and frame-time evidence. Terrain landform joins and native V8-r6 vegetation reopening/visual gates remain parallel priorities.

**Independent baseline stays 50/100 (5.0/10); >8.5 and every water acceptance gate remain mandatory.** Owner handles full Java25/Minecraft/GPU builds and JAR publication; no new playable JAR or release from this source-only checkpoint.
