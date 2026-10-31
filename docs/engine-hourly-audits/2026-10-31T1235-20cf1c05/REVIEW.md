# Deep Primal independent audit — Dev128 (2026-10-31 12:35 UTC)

## Source and changes
- Baseline after Dev127: `a2bae894deae4f5b35018c1be535ddccd66684ae`.
- Dev128 Builder commit: `20cf1c0534766017934aafd4774d24bcef2756a3`.
- Auditor performance correction: `fdc704b0fba29aa39bb3ac91f63f9a14124664bd`.
- The new `NearestChunks` unsigned-65-bit distance fix was reviewed. Its Git blob identity is `fc264d32c4b0a8afa9db6c350c42c5700ff772fa`. The original differential test blob is `ac1570e2cff415af9ec6a099289a44f8d5f80076`.

## Confirmed performance defect and correction
`PlayerChunkSenderMixin.deepprimal$boundedSelection` selected the nearest K *pending* chunks before checking `ChunkMap.getChunkToSend`. If those near chunks were not ready, they occupied the bounded quota and prevented farther ready chunks from being sent, even when send quota was available. Minimal reproduction: center (0,0), quota 1, pending (0,0) unavailable and (1,0) ready; the old implementation returned no chunk. The corrected implementation checks readiness before offering a candidate to the bounded nearest-heap. No change to the core heap, water, terrain, LOD or renderer. A second readiness check before sending is retained.

## Focused independent validation
- OpenJDK 21.0.12.1 `javac --release 21 -Xlint:all -Werror`: PASS for the exact GitHub `NearestChunks` and original committed differential test; Git blob SHA matched both.
- Original committed `NearestChunksOverflowChecks`: PASS, 56,707 assertions.
- Independent BigInteger differential oracle: PASS, 12,000 randomized trials and 657,961 assertions. Also identified `sort()` is not idempotent if called twice; production mixin calls it once per reset. This is a *latent API caveat*, not a demonstrated production regression.
- Ready-chunk selection model: PASS, starvation reproduction and 10,000 randomized cases.
- Production-equivalent `PlayerChunkSenderMixin` compiled against isolated Minecraft/fastutil/Mixin API stubs: PASS, 5,000 randomized differential cases plus starvation case.
- Compiled negative control restoring the old unfiltered offer loop: FAILED at `ready chunk starved`, as expected.
- The production mixin is verified in GitHub at blob `7a19907468a7420c77eaa4724b148dfc994ee790`. Stub tests validate the relevant algorithm and Java syntax, **not** full NeoForge runtime integration.

## Remaining acceptance gates
- No complete local repository checkout or Gradle dependency cache; Git CLI DNS for github.com unavailable. Full Java25 Gradle/NeoForge packaging, native Minecraft launch, save/reopen, renderer Vulkan/OpenGL, finite-water and terrain performance benchmarks were not executed.
- `gradle.properties` at Dev128 still declares `neo_version=26.1.2.122`; 26.3/26.4 compatibility not proven.
- New prefilter checks `getChunkToSend` for every pending candidate; it restores readiness correctness but may increase per-tick lookup cost. Profile in-game against the original implementation before claiming a net performance improvement.
- No qualified replacement JAR, no JAR SHA, no older JAR deletion, no GitHub Actions.
- Whole-engine accepted baseline remains 50/100 (5.0/10). This is not a fresh full-engine regrade; target >8.5 remains unverified.

## Coordination
Auditor modified only the previously unchanged `performance-engine/.../PlayerChunkSenderMixin.java` and this audit evidence. The Builder's Dev128 `NearestChunks.java` and CoreChecks additions were left untouched. No Builder checkout or lease was modified.
