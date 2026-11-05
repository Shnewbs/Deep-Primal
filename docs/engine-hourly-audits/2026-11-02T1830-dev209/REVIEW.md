# Dev209 — Independent Deep Primal audit (2026-11-02 18:30 UTC)

## Scope and coordination

Previous Dev207 audit head: `e833d7655862366b38fea1f839c625d4c5ae510b`. GitHub main at review: `2eaa56c24961ebd2c232ae202b9bbbad533c86aa`, **7 commits ahead, 0 behind**. Changed files: `WaterAccount.java`, registered `WaterAccountDebitChecks.java`, `CoreChecks.java`, Dev208 evidence and the released writer lease. No renderer, terrain, LOD or native hydrology code changed in this comparison. Inspected `docs/ENGINE-MAIN-WRITER-LEASE.json`: `completed-released`, `lease_released=true`, ended at 18:06 UTC. No production or writer-lease edits in this audit.

## Independently verified exact-source water transfer

- Fetched exact production `WaterAccount.java` and registered `WaterAccountDebitChecks.java` from GitHub. Reconstructed files have **Git blob SHAs matching main**: `3e18f72b42f32569fd7fa2c7c3751af12508dee2` and `db42debbf97935e173fb1c3f5a26ae049c282642`.
- Java21 strict compilation: `javac --release 21 -Xlint:all -Werror`, then `java -ea -Xverify:all` **passed**.
- Replayed registered Dev208 regression: **150,000 assertions passed**, 25,000 seeded cases.
- New independent BigInteger-referenced property test: **4,400,000 assertions passed**, **240,000 operations** over 80 randomized four-account networks. Checked move, split, evaporation, saturated constituent admission, exact debit/credit, failed-admission atomicity, negative/overflowing demand values, capacity and conservation of water/salt/sediment.
- Mutation sensitivity: replacing `from.debit(parcel)` with `from.debit(firstParcel)` in the split implementation compiles but the independent test fails at assertion **20** (`split debit`), as intended.
- No new production defect was confirmed. No speculative source correction was made. Dev208's claimed 33.2% allocation reduction was not independently benchmarked here; no native FPS inference.

## Compatibility and acceptance

- Root `gradle.properties` pins NeoForge `26.1.2.122`. Root Gradle requires Java25 and ModDevGradle `2.0.148`. Java21 isolated tests do not qualify a Java25 Gradle build or NeoForge 26.3/26.4 compatibility.
- Mojang's 26.4 Snapshot 1 (2026-09-22) makes Vulkan the default graphics preference and removes automatic API switching after startup crashes: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1 . Native Vulkan/OpenGL fallback, water waterfall continuity, terrain seams, saved-world recovery, LOD FPS/VRAM and actual gameplay performance remain untested this cycle.
- Runtime lacks a complete private repository checkout, Java25, Gradle/NeoForge dependency cache and working GitHub DNS for local clone. The GitHub connector permits direct scoped repository access but does not mount a full checkout. **No playable JAR or JAR SHA256 was produced.** Previous verified JARs preserved; no GitHub Actions used.

## Grades and next action

Scoped water-account correctness: **8.5/10 provisional** (exact-source, independent oracle, mutation negative control; not native acceptance). Whole engine: **5.0/10 historical baseline, not freshly regraded**. Next: restore full local Java25 checkout and dependencies; run `coreChecks`, `check`, `jar` and JarJar integrity checks, verify JAR SHA256, test client/server save/reopen, then GPU Vulkan/OpenGL water and LOD performance. Only delete a superseded JAR after a verified replacement exists.

Local replay evidence ZIP: `DeepPrimal_Dev209_IndependentAudit_20261102T1830_Evidence.zip`, SHA256 `b7ea7e6ff789c263b556957e73cebea36d81030c5d39cb619b4ec03ef48f3dd4`. Includes exact source, registered and independent tests, mutant, replay script, logs and SHA256 manifest; ZIP integrity and file hashes checked.

GitHub compare: https://github.com/Shnewbs/Deep-Primal/compare/e833d7655862366b38fea1f839c625d4c5ae510b...2eaa56c24961ebd2c232ae202b9bbbad533c86aa
