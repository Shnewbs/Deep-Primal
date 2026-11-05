# Deep Primal independent audit — Dev134+ — 2026-10-31 18:30 UTC

## Source and scope
- Previous audit: `cacd1654496952e78344608d4e36654c0e9ace85`.
- Reviewed three Builder commits through `b2851422b29ce53d7923d8f611968da3ec0e3e93`.
- Tested production `engine-core/src/main/java/com/shnewbs/deepprimal/core/VerticalRange.java`, Git blob `92c3e1ff61e2ccbccf3706587e8f776c6e02f03d`, SHA256 `86ebd996a67ff17efede99957237f69bcde503786d04a1de270b29d4ac7cfb53`.
- Test-only correction committed as `84ede1e4cf7d1b186d5258b5ff20d2afd4691b70`, verified blob `50f4d9bdb63f9722b31bd785b73f95aba92c5e03`.

## Confirmed regression-test defect and correction
The new named camera-underflow assertion originally called `low(Integer.MIN_VALUE, -64, 128, 96)`. Because `count(128,96) = 128`, the window covers the entire world; its low endpoint remains -64 even when `cameraY-radius` overflows. The named assertion was therefore vacuous. A compiled 32-bit camera-subtraction mutant passed that original sentinel. Changed only the test case to `low(Integer.MIN_VALUE, -64, 512, 96) == -64` (window 193, world 512); the same mutant now fails at check 0. The Builder's production fix was preserved. Its broader randomized test may already detect the mutant elsewhere.

## Independent validation
- Java21 strict compilation (`javac --release 21 -Xlint:all -Werror`) and `java -ea -Xverify:all`: PASS.
- Corrected committed regression suite: `VERTICAL_RANGE_OVERFLOW_PASS checks=1431760`.
- Independent seeded BigInteger oracle: `INDEPENDENT_VERTICAL_PASS checks=2004891`, including 250,000 randomized windows, signed boundaries, ring periodicity and permutation, invalid input rejection.
- Three compiled negative controls rejected by corrected committed suite: camera overflow at check 0, slot overflow at check 1, radius overflow at check 2. Independent suite also rejected all three.
- Combined successful assertions: 3,436,651, limited to isolated Java math tests.

## Other acceptance gates
Reviewed `ViewAreaMixin`, `FogRendererMixin`, `build.gradle`, `settings.gradle`, `gradle.properties`. No changes in this interval to native Vulkan, LOD workers, finite water, terrain or V8 persistence. Repo still pins NeoForge `26.1.2.122`, Java toolchain 25, and stale `mod_version` Dev127. NeoForge 26.3 artifacts exist upstream but compatibility is unverified.

The isolated runtime lacks a full checkout, JDK25, Gradle dependencies and native Minecraft environment; Git CLI fails github.com DNS. Thus no full Gradle/JAR, save/reopen, finite-water, Vulkan/OpenGL/LOD or performance benchmark was run. No verified JAR or JAR SHA, no previous JAR deleted, no GitHub Actions.

## Grade and follow-up
Scoped vertical-window correctness/regression robustness: provisional **8.5/10**. Whole-engine baseline **5.0/10**, carried forward, not freshly regraded. Target strictly above 8.5 remains unproven. Next: restore pinned checkout and Java25/Gradle; build a verified JAR with SHA256; validate native camera movement, V8 durable publication/reopen, finite-water conservation and Vulkan/LOD performance. Retain prior audit JAR until replacement verified.
