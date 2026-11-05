# Independent Deep Primal Dev92 source audit — 2026-10-30 01:25 UTC

Pinned main source: `32efd75ddb17817757dc13c33a2da1cc721fcf20`, version `0.0.1-alpha.29-dev.92-relief-joins.1`. Git main at publication planning: `32efd75ddb17817757dc13c33a2da1cc721fcf20`. Observational read-only auditor; no Builder lease, source, checkout, cache, output, timing reservation, schedule or accepted JAR was touched.

## Source and new tests

Dev92 production commit [a5aee4c6](https://github.com/Shnewbs/Deep-Primal/commit/a5aee4c694559d2a9ed7d866917105cce7072976) adds opt-in V8 relief-5 preview join. [V8ReliefJoinV5.java L13–56](https://github.com/Shnewbs/Deep-Primal/blob/32efd75ddb17817757dc13c33a2da1cc721fcf20/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/V8ReliefJoinV5.java#L13-L56) adds 14 frozen nodes; [TerrainShapeV8.java L123–140](https://github.com/Shnewbs/Deep-Primal/blob/32efd75ddb17817757dc13c33a2da1cc721fcf20/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/TerrainShapeV8.java#L123-L140) inserts them before historical sea-side/canyon guard; [V8GeneratorSnapshot.java L159–173](https://github.com/Shnewbs/Deep-Primal/blob/32efd75ddb17817757dc13c33a2da1cc721fcf20/src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java#L159-L173) creates an explicit saved graph for new opt-in V8-r3 worlds. No evidence of new registered native generator or frozen legacy migration. The new independent runner negative-control correction is commit [953c4d67](https://github.com/Shnewbs/Deep-Primal/commit/953c4d6738cac386afdf61556f85351d4c6cbdec). Separate renderer DLSS handle-alias source commit [0addab76](https://github.com/Shnewbs/Deep-Primal/commit/0addab7688686c53863324f72e6e3c840f84e4a0) has no active-GPU acceptance.

I independently fetched and recreated exact source Git blob `b193fa30f25b6ace3dfa8be077f3137f17aa3d2e` and synthetic DensityGraph API double `675cdcb1ee046da7dd5a71f02932eb7f211682fd`. Both matched `git hash-object`. Retrieved owner-supplied Temurin 25.0.5+8 from Library, left Java21 untouched. Strict `javac -Xlint:all -Werror` compile and `java -ea -Xverify:all` checks passed. Independent scalar oracle executed four processes: Java25 runtime / release21 twice, Java21 runtime / release21 once, Java25 runtime / release25 once, each 2,419,907 assertions; **9,679,628 fresh assertions passed**. Verified Java25 class major 69, bounded window and +/-11 synthetic relief, 68,000 random signed coordinates per run, 2,048 adjacent samples, deterministic replay and coastal/highland unchanged outputs. Six individually compiled wrong variants were rejected by intentional auditor fixture assertions. **Important:** production DensityGraph VM, actual seeded noise, graph complexity/cost and native save lifecycle were *not* compiled or run. These results are not equivalent to actual terrain or finished graphics.

Evidence ZIP (17 CRC-verified members; own independent test, source, logs, mutations, receipt) `DeepPrimal_Dev92_IndependentAudit_20261030T0125_32efd75d_Java25Evidence.zip`; size **13,948 bytes**, SHA256 `b2e61e04529f38fccffc15eb3b33dfe667604a32415a2535131b2b3fa3395864`, Library `/Deep Primal/Independent Audits/`.

## Open gates and priorities

1. P1: physical Minecraft finite-water cell/world-ledger atomics and marine-root witness across loaded/unloaded chunks, player edits and crash/reopen remain unaccepted after Dev91.
2. P1: owner full Java25 Gradle 9.2.1/NeoForge, packaged compiled/source/JAR CRC/payload, native V8-r3 save/reopen/shutdown, Windows AMD8060S Vulkan+OpenGL GPU and frame/tick tails **not run**; under the October27 latest owner override **no current JAR built or delivered**.
3. P2: the complete full-core `V8ReliefV5JoinChecks` graph budget, signed chunks, exact older recipes, native coastal seams and Minecraft saved V8-r3 snapshots remain unrun.
4. P2: root `docs/ENGINE-WORK-STATE.json` remains Dev53 while scoped terrain state and gradle.properties record Dev92. Builder owns reconciliation. Renderer alias check alone does not activate DLSS or establish performance.

## Ten equal-weight grades — historical carry-forward, **not fresh regrade**

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biomes | 4 |
| Hydrology and caves | 6 |
| Features and structures | 4 |
| Performance and threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/docs | 7 |
| Polish/tooling | 4 |
| **Overall / Tier 2** | **50/100, average 5.0** |

The target strictly **>8.5/10** requires a *fresh complete* ten-category review with actually implemented/native evidence. No unused helper gains gameplay grade credit. Audit implementation changes: none. No GitHub Actions, release, CurseForge, JAR rotation or builder stop. The October27 owner override reserves full builds/JARs to owner, superseding older mandatory-JAR wording.
