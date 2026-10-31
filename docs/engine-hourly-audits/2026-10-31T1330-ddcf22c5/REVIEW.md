# Deep Primal independent audit — Dev129 (2026-10-31 13:30 UTC)

## Source pin
- Reviewed main at `ddcf22c56a0d0b6f8f134674d7ff31ae1acc4d41`, one commit since Dev128 `1c5889293371d5ba8b55b3464fac3142e311cf0d`.
- New source: `engine-core/src/main/java/com/shnewbs/deepprimal/core/NearestChunks.java` (repeated-sort idempotency and late-offer heap restoration), plus `src/coreChecks/java/com/shnewbs/deepprimal/core/NearestChunksOverflowChecks.java`.
- Exact verified production Git blob: `a218f884d5f231c2eb6ac040887093becdd8ecbf`; committed test blob: `5fe6aac1663d518e44a032426d5a6c44fa74182a`. Unchanged production `PlayerChunkSenderMixin.java` blob: `7a19907468a7420c77eaa4724b148dfc994ee790`.

## Independent focused validation
- Local OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: exact committed heap and test passed **224,254 assertions**.
- Independent BigInteger state-machine oracle passed **5,000 randomized lifecycle trials and 2,123,468 assertions per execution**, twice. Coverage: signed-coordinate extremes, 1..64 limits, resets, repeated sorts, late offers, exact nearest-first ordering.
- Exact production `PlayerChunkSenderMixin` compiled with isolated Minecraft/Mixin/fastutil API doubles under strict Java21 compilation and passed **5,000 readiness-selection differential cases plus the starvation reproduction**.
- Two compiled mutation controls failed as expected: removing the late-offer heap rebuild failed at assertion 20; removing the sort idempotency guard failed at assertion 11.
- No additional production defect confirmed. **No production source correction required.** Dev129 addresses the latent repeated-sort issue flagged by the Dev128 auditor.

## Risks and acceptance gaps
- Ready-only prefilter calls `ChunkMap.getChunkToSend` across pending candidates, then selected candidates again. In-game CPU overhead and transient readiness are not benchmarked.
- `gradle.properties` still declares `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1` despite Dev129 source and `neo_version=26.1.2.122`. Reconcile release metadata only with a qualified package. NeoForge 26.3/26.4 compatibility unverified.
- Complete checkout, Java25 and Gradle dependency cache unavailable locally; Git CLI DNS for github.com failed. No full Gradle/NeoForge package, native save/reopen, water/terrain, Vulkan/OpenGL, LOD or performance acceptance, verified mod JAR, JAR SHA or JAR deletion. **No GitHub Actions used.**
- Whole-engine baseline carried forward at **50/100 (5.0/10, Tier 2)**; not a fresh full-engine grade. Strictly greater than 8.5 remains unproven.

## Next acceptance
1. Full Java25 checkout/build, `coreChecks`, NeoForge packaging, native save/reopen and crash recovery; SHA256-verify new JAR before retiring any old audit JAR.
2. In-game chunk-send throughput and `SEND_NANOS` under large pending queues.
3. Finite-water conservation, terrain seams/determinism, Vulkan/OpenGL/LOD hardware acceptance.

The independent audit did not modify Builder checkout, lease, cache, outputs or source files. Local evidence archive: `DeepPrimal_Dev129_IndependentAudit_20261031T1330_ddcf22c5_Evidence.zip`, SHA256 `e9e3290eb3c94f06f551523c22858cbdaf2e870bb49ba12d199e24e4fcd43bd4`.
