# Deep Primal — Dev132 independent audit (2026-10-31 16:30 UTC)

## Source and change window

- Last independent audit main: `39fec2915f59e4aaf1760749ff0e823e32560f31`.
- Pinned Dev132 engine commit: `b8e7aff217b713a65567884da90f4b6a218f9f14` (one new commit).
- Compared paths: `engine-core/src/main/java/com/shnewbs/deepprimal/core/SyncRebuildBudget.java`, `src/coreChecks/java/com/shnewbs/deepprimal/core/SyncRebuildBudgetOverflowChecks.java`, `src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java`, and `docs/validation/engine-continuation/sync-rebuild-overflow-2026-10-31/README.md`.
- Exact fetched production source blob SHA: `c8b40c80e47c1dda30ae983d2887e600d44648bf`; SHA-256: `650fad58e37526bdeba162f78f6673902c31280c509b85bd272d8b8a6dbe4253`.

## Independent verdict

**Scoped PASS, no new production defect confirmed.** The new saturation guard correctly keeps ordinary synchronous rebuild admission closed after an elapsed-time or count overflow, while retaining player-edit bypass and frame-reset behavior. The callsite `graphics-settings/src/main/java/com/shnewbs/deepprimal/settings/mixin/client/ChunkRebuildMixin.java` uses the budget with bounded config values (`SYNC_REBUILDS` 1–16; `SYNC_REBUILD_MICROS` 250–8000). No overlapping engine production changes were made.

### Independently executed tests

- OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`.
- **2,000,008 independent assertions passed**: 1000 deterministic 500-step sequences, `BigInteger` differential oracle, adversarial reset/cap values, player-edit bypass, duration sign and overflow, reflection-assisted count boundary and elapsed saturation.
- Three negative controls compiled against the independent harness: original pre-fix code failed at assertion 2 (count saturation), count-only regression failed at assertion 2, elapsed-only regression failed at assertion 4. The positive source passed.
- Source bytes locally matched the committed production SHA-256. Harness source and logs are retained in the evidence ZIP.

### Coverage and blockers

- Reviewed latest commit and callsite; no new renderer, water, terrain, LOD, or Vulkan changes in this single-commit window. No fresh native game or GPU tests were run for those subsystems.
- `gradle.properties` still declares `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1` and `neo_version=26.1.2.122` despite Dev132 source; version metadata is stale. This is release-label hygiene, not a proven runtime defect; avoid changes during concurrent Builder ownership.
- NeoForge 26.3 artifacts exist upstream, but Deep Primal 26.3/26.4 compatibility remains untested.
- Full local Gradle/NeoForge packaging **blocked**: no complete checkout, Gradle dependency cache or Java25 toolchain; Git CLI cannot resolve github.com. **No qualified JAR or JAR SHA**. No previous JAR deleted. No GitHub Actions used.
- Native save/reopen, finite-water conservation, terrain-border determinism, LOD/Vulkan/OpenGL and chunk throughput acceptance remain open.

## Grade

- Scoped synchronous-rebuild admission correctness: **8.5/10 provisional** (pure-Java checks and negative controls; native integration missing).
- Whole engine: **50/100 (5.0/10) carried forward**, not freshly regraded. Target strictly above 8.5/10 unproven.

## Next audit focus

Full pinned Java25/NeoForge build and verified JAR with SHA256; client-thread rebuild throughput/tail latency; save/reopen and V8 persistence; finite-water mass balance and river/waterfall stability; Vulkan/LOD/terrain benchmarks. Preserve prior JAR until a replacement passes verification.

Evidence archive SHA256: `134bfb53dbf3f825b591173d090dd3be54ee35e436c2e997d70af0c1c538d069`.
