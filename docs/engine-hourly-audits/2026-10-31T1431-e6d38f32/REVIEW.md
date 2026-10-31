# Deep Primal independent audit — Dev130 (2026-10-31 14:31 UTC)

## Source pin
Previous audit head: `b4442bc7c574e4bfaa2dd2810dbcf50805eb197e`. Reviewed main: `e6d38f3299f3bde2012bd34d1571d776343d6182` (three new commits). Builder change `dc95b4cd0366c3aa199c2f91d699163b232f1f1f` adds `NearestChunks.wouldAccept` before expensive `ChunkMap.getChunkToSend` probes; only candidates that can enter the nearest-ready heap require readiness lookup. No terrain, finite-water, renderer, LOD or native persistence code changed in the comparison.

Exact independently compiled Git blobs: `NearestChunks.java` = `148f6f02679d3e42ee1c3a00b1c57d2604dd3068`; `PlayerChunkSenderMixin.java` = `fa9b04316134a283a17cd406f9b7614263deb9b2`.

## Independent verification
OpenJDK 21.0.12.1, strict `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`:
- Independent BigInteger readiness-admission oracle: **5,000 randomized trials, 910,933 assertions, 78,222 synthetic skipped readiness probes**. Covers signed-coordinate extremes, exact 65-bit distance, ready-only admission, signed packed-position ties, sorted preflight and late offers.
- Prior independent heap lifecycle oracle rerun on exact Dev130 source: **5,000 trials, 2,123,468 assertions**.
- Existing exact 65-bit differential checks rerun: **224,254 assertions**.
- Actual Dev130 chunk-sender mixin compiled against isolated Minecraft/Mixin/fastutil doubles: **5,000 randomized selection cases plus starvation reproduction passed**.
- Three independently compiled broken controls rejected: always-accept, incorrect sorted-root preflight, and ignoring 65-bit carry.

**No additional confirmed production defect** in the reviewed scope; no source correction required. Synthetic lookup counts are not in-game CPU/frame-time measurements. Native readiness churn can make the second lookup differ from the first; live throughput and underfilled-batch behavior remain unverified.

## Build, compatibility, grade
`gradle.properties` still declares `mod_version=0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1` and `neo_version=26.1.2.122`. Root build specifies Java25, ModDev 2.0.148, Gradle 9.2.1. This isolated environment lacks a complete checkout, Gradle dependencies, verified Java25 and native Minecraft/GPU acceptance. Full Gradle/NeoForge build, 26.3/26.4 compatibility, water/terrain/LOD/Vulkan testing and a verified replacement mod JAR were **not performed**. No old JAR was deleted. No GitHub Actions were used.

Scoped nearest-ready admission correctness: **8/10 provisional** (strong Java oracle/mutation evidence, native integration untested). Whole-engine accepted baseline remains **50/100 (5.0/10, Tier 2)**, carried forward rather than freshly regraded; target >8.5 remains unproven.

Next: complete Java25 Gradle/NeoForge package and SHA256-verify a new JAR before replacing any older one; native chunk-send readiness churn and throughput, save/reopen/crash recovery, finite-water conservation, terrain seams, Vulkan/OpenGL and LOD acceptance. Reconcile release version only when a build is qualified.

Independent evidence archive: `DeepPrimal_Dev130_IndependentAudit_20261031T1431_e6d38f32_Evidence.zip` (local). Builder checkout, lease, caches and outputs were untouched.
