# Deep Primal dev162 — finite hydrology speed and ocean terrain admission

**Date:** 2026-11-01 UTC. **Baseline main:** `f253aa5d076be71275fbfa3c6fb1a02efaa091c4`.
**Consumed audit:** `docs/engine-hourly-audits/2026-11-01T1830-dev161/REVIEW.md`.
**Scoped claim:** `CLAIM.json`. The prior main writer claim was completed/released. No organics, renderer, live hydrology or auditor-owned files changed.

## Defects and fix

- `WaterDynamics.speed` previously returned `NaN` when slope, catchment or width was `NaN`; invalid hydrology data could enter animation speed selection. Nonfinite inputs now produce the conservative minimum speed `0.35`. All finite input arithmetic remains unchanged.
- `WaterDynamics.oceanHeight` previously returned `NaN` for malformed base, continent or noise inputs. An invalid base now uses the minimum ocean floor (`floor + 40`); invalid shaping inputs preserve the finite base, clamped to that minimum.
- `floor + 40` and `96 - floor - 80` previously performed signed 32-bit arithmetic before conversion, allowing extreme configured floor values to wrap. The new implementation promotes floor arithmetic to double before evaluation and keeps the ordinary finite terrain calculation bit-identical.

No water mass, save format, source recharge, native GPU shader or LOD algorithm was changed. This is a defensive numerical admission fix, not evidence of resolved natural waterfall flicker.

## Reproducible local validation

- OpenJDK 21, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all` passed for the complete dependency-free production `WaterDynamics.java` and tests.
- **600,024** new assertions: 300,000 deterministic finite speed parity cases, 300,000 deterministic finite ocean-height parity cases, 21 malformed input cases and three extreme floor cases.
- **700,028** existing water presentation assertions passed, plus **826,871** existing waterline admission assertions.
- **2,126,923** total isolated assertions passed. Three deliberately defective variants (no speed input guard, no ocean input guard, no floor overflow guard) failed the new regression as expected.
- Verified exact source Git blob `35af84115a215bd2d6993ce1102fc42fbd39ca5a`, SHA-256 `2379407647d8143d425de6db86e9a13429bca96c87ceea6c2538d61220d4a7d9`.
- Verified new regression Git blob `dfb4814b35fa7ba6224ac99400bec2360cb5f2cb`, SHA-256 `7af9e16749f049fd4e8fa99fbafe4ccca67792e89c6fd281da09dde9e03cf5c4`.
- Registered `WaterDynamicsHydrologyFiniteChecks.main` in `CoreChecks`.
- Local evidence ZIP: `DeepPrimal_dev162_hydrology_finite_evidence.zip`, 10,311 bytes, SHA-256 `e7375f47c3131a32f7eefe8aacc5201f75e85bec627b0cb914dc793c3ec9d7db`. Archive integrity verified. Contains exact source, new regression, unchanged existing regressions, `run.sh`, logs, negative controls and SHA manifest.

## Unpassed acceptance gates

A full Gradle attempt could not start: the execution environment has no full checkout (`./gradlew: No such file or directory`), only Java 21, and no resolved Gradle/NeoForge dependencies. A complete Java 25 NeoForge build, full `CoreChecks`, packaged native client/server, save/reopen, water conservation, Vulkan/OpenGL readbacks, LOD performance, and hardware benchmarks were **not** run. No new playable JAR or JAR SHA-256 exists. Previous successful JARs were not deleted; no GitHub Actions were used.

Historical independent whole-engine grade remains **5.0/10**, not freshly regraded. Next gate: restore complete checkout, Java 25 and dependency resolution; qualify a local JAR before replacing any prior successful artifact.
