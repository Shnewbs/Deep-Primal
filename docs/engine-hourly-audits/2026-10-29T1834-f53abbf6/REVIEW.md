# Dev84 independent audit — 2026-10-29 18:34 UTC

- **Pinned engine source:** [`f53abbf65d6dbce584b3e3c8203c0f589146e6c9`](https://github.com/Shnewbs/Deep-Primal/commit/f53abbf65d6dbce584b3e3c8203c0f589146e6c9), version `0.0.1-alpha.29-dev.84-outlet-journal.1`.
- **Latest main immediately before guarded audit publication:** `f53abbf65d6dbce584b3e3c8203c0f589146e6c9`. This audit did not modify the Builder, renderer or gameplay code.
- **Java 25:** Eclipse Temurin 25.0.5+8 restored from owner-approved Library toolchain, strict `javac --release 25 -Xlint:all -Werror`, real committed fixture **110 assertions PASS**, independent 128-commit CAS/conservation/replay/reopen oracle **657 PASS**. Java 25 classfile 69.
- **Java 21:** strict `javac --release 21`; committed fixture **110 PASS**, independent oracle **657 PASS**. Classfile 65. Four positive processes yield **1,534 assertions passed**.
- **Source identity:** store `839d66fab78d33bd56b633e092565b53e43fd36f`; ledger `e41226dbf0daf40ea455ee44247e2c4be81f29e9`; topology `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`; committed fixture `9ac9c00a491f44cf68874389ec70643754853e97`. All four Git object SHA-1 hashes checked locally.
- **Three defective implementations** (disabled generation CAS, digest checking, replay idempotence) compiled under Java 25 and were all rejected by actual fixture `AssertionError`, not compilation/launcher faults.
- **Independent conservation:** 128 successful generations, 128 no-generation exact replays, aggregate water/salt/sediment `[10015,6005,4003]` unchanged across restart on local Linux.
- **New integration finding:** the frame contains no world UUID/seed/saved-profile binding. A valid `world-A` checkpoint transplanted to `world-B` was accepted by current `open` API. This is an unsupported cross-world-copy scenario, **not** demonstrated normal gameplay contamination. Require world/profile identity verification by native owner before adoption (store L86–99, L215–258).
- **Low-severity canonical-format finding:** writer sorts account keys but reader accepts a validly rehashed, reordered account table (L225–231, L278–290); same semantic balances, no demonstrated physical water fault. Optionally enforce strict sorted-key decoding.
- **High-priority incomplete feature:** ledger store is opt-in and **not connected** to Minecraft finite-cell edits, world save/reopen, player edits, unloaded regions, certified ocean endpoints or environmental sources/sinks. Linux forced-file operations do not prove Windows/network-FS power-cut durability.
- **Not run:** full repository checkout (Git CLI DNS blocked), Gradle 9.2.1/NeoForge, full native/world JAR, packaged archives/class identity, Windows AMD8060S Vulkan/OpenGL, real save/reopen/shutdown, process-kill, real performance/soak.
- **Build policy:** owner October 27 override reserves full JAR/Gradle/GPU builds for owner; this auditor created no development JAR and published no release. Three-JAR retention unaffected.
- **Next:** world-owned atomic finite-cell/ledger transaction plus world identity; physically conserved loaded/unloaded restart/edits, certified marine outlets, owner native and GPU/timing gates.

### Equal-weight whole-engine grade (carried baseline; NOT a fresh regrade)
Terrain **5**, biomes **4**, hydrology/caves **6**, features **4**, performance **3**, determinism **7**, architecture **6**, extensibility **4**, testing/docs **7**, polish/tooling **4** = **50/100 (5.0/10), Tier 2**. Strictly greater than **8.5/10** remains unproven; no gameplay credit for the unintegrated store.

Evidence ZIP: `DeepPrimal_Dev84_IndependentAudit_20261029T1834_f53abbf6_Java25Evidence.zip`, 24,216 bytes, SHA256 `f1c74f327f1b81847366f7876784db19c1bdd1bf6f14be433008987869ec3749`, CRC and 20 member SHA256 checks passed, saved to **Deep Primal / Independent Audits** in Library. Root work state is historically Dev53, scoped engine work state records Dev84. No timing benchmark run, no builder lease taken.
