# Dev83 independent audit — 2026-10-29 17:30 UTC

Pinned source: `2e3c945dd43e13cf9968dfdfe138212a3afe753f` • Version: `0.0.1-alpha.29-dev.83-outlet-ledger.1` • **No source changes by the auditor.**

## Change and evidence
The Builder added the source-only `V8ConservedOutletLedger`: immutable, bounded three-quantity accounts; signed 32-aligned port IDs; acyclic authorized receiver graph; debit+credit; per-source receipt sequences and replay guards. See [ledger source lines 30–47, 66–106, 125–154](https://github.com/Shnewbs/Deep-Primal/blob/2e3c945dd43e13cf9968dfdfe138212a3afe753f/engine-core/src/main/java/com/shnewbs/deepprimal/core/V8ConservedOutletLedger.java#L30-L154). The earlier source-only Builder regression reports 40,134 Java21 assertions with three broken controls rejected; those counts were **not** independently rerun.

Independent results from owner-supplied Temurin **25.0.5+8**, isolated Linux resources: exact pinned Git blobs ledger `e41226dbf0daf40ea455ee44247e2c4be81f29e9` and topology `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`; strict Java25 and Java21 compilation, classfile versions 69 and 65, **606,056 assertions PASS on each JDK**, 6,000 deterministic randomized transfers each. A separate BigInteger-based oracle checks water/salt/sediment totals, replay/reopen, sequence control, signed-coordinate capacity and cycle rules. **Three** independent negative variants (mint water, drop receipt, no replay) all compiled and were correctly rejected. The audit evidence ZIP (CRC plus member hashes verified): 14,717 bytes, SHA256 `b287f970d78d3f6e85708c9ca732963e942535fbd4a1de9846d7f3839094b962`, Library `/Deep Primal/Independent Audits/DeepPrimal_Dev83_IndependentAudit_20261029T1730_2e3c945d_Java25Evidence.zip`.

**Integration risk demonstrated:** Two valid sequence-1 transfers from the *same* initial snapshot produce distinct candidate snapshots, each reporting success. This is correct for the documented immutable candidate API, but native integration must perform a single-writer CAS/transaction and durably persist **balances + receivers + receipts** before acknowledgement; otherwise concurrent callers can lose updates. Native storage implementation is not present. The helper is not certified global marine routing, a native finite-water solver, or actual saved-world conservation. Source-only positive tests do **not** earn gameplay feature credit.

## Ten-category full-grade baseline carried forward, not a fresh full regrade

| Rubric category | /10 |
|---|---:|
| Terrain realism/variety | 5 |
| Biomes/blending | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture/quality | 6 |
| Extensibility | 4 |
| Tests/benchmarks/docs | 7 |
| Polish/config/tooling | 4 |
| **Tier 2 (carried)** | **50/100 = 5.0/10** |

The threshold remains **strictly above 8.5/10**; no acceptance or task-stop is justified.

## Unrun gates and priority
No complete repo checkout (Git CLI DNS unavailable), full Gradle 9.2.1/NeoForge, packaged source/class/CRC, private-fixture exclusion, actual native save/reopen/shutdown, persistent water/salt/sediment conservation, Windows AMD8060S Vulkan/OpenGL, or real GPU/tick benchmarks. **No qualified development JAR built**: the October 27 owner override reserves full Gradle/Minecraft/GPU builds to the owner. No release or retention mutations, and no builder/renderer checkout, lease, timed workload or source was touched. Root `docs/ENGINE-WORK-STATE.json` is Dev53; scoped `docs/TERRAIN-BLENDING-WORK-STATE.json` records Dev83 checkpoint.

Priority: implement native whole-snapshot transactional journal/CAS, coherent receiver changes and global outlet certification, then actual unloaded/reopened/editable-world accounting and Windows Vulkan/OpenGL acceptance.