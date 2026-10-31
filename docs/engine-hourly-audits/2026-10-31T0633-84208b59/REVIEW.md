# Dev122 independent audit — 2026-10-31T06:33Z

Pinned committed main: `84208b59eaa7f72370b3f82b8dc4a33c3932860c`; version `0.0.1-alpha.29-dev.122-nbt-prewrite-admission.1`. Builder Dev122 commits: `bc8411b6`, `7a94db54`, `84208b59`. Builder's Dev122 lease was checkpointed/released; its checkout/cache/outputs, benchmark reservation and schedule were untouched.

## Confirmed defect and guarded repair
`V8ChunkGenerationNativeCommit.begin` previously admitted an ungenerated target at durable epoch `Long.MAX_VALUE`, even though `commit` would inevitably fail `Math.addExact` **after native block writes**. Isolated Java25 integration reproduced the late rejection and mutation (pre-fix check 250002). A mandatory discard cannot guarantee reversal of external native callback side effects. Fix: reject exhausted epoch after valid admission and **before** materialization, preserving the commit-time overflow defense. The penultimate epoch still publishes `Long.MAX_VALUE` once and then fails closed.

Production correction commit: `1c270ac6f7c6c9fefb980e2ce505a660ab43d635`. Compiled regression + mutant runner commit: `6c7543d2d8c88b7c7fe9449ad49057e2fc1bc8a1`. NativeCommit Git blob `f97b8da626867429de8ad65bb35e91d6103b41dc` → `1748ea6fd0ebd51c2b8964aec5724a178a818aad`. These are separate from the Builder's Dev122 NBT candidate-prewrite change.

## Fresh focused checks
Linux x86_64; Temurin Java25.0.5+8 restored from owner Library, OpenJDK21.0.12.1 retained. Both JDKs: strict compile (`--release 25` or `--release 21`, `-Xlint:all -Werror`), `java -ea -Xverify:all`. Java25 classfile major 69. Reconstructed exact pinned production Git blobs for NativeFill/NBT adapter/NativeCommit and three engine-core classes, with Minecraft API doubles. Independent native integration: **250,006** prewrite/overflow checks and **336,006** publication/error-preservation checks per JDK. New committed epoch test: **72,009** assertions per run, twice per JDK, deterministic, with compiled epoch-guard mutant rejected. No actual Minecraft native owner or disk transaction.

## Whole-engine grade
Last full accepted baseline carried forward, **not freshly regraded**: terrain 5, biome blending 4, hydrology/caves 6, features 4, performance 3, determinism 7, architecture 6, extensibility 4, tests/docs 7, polish/tooling 4. **50/100, average 5.0/10, Tier 2.** Strictly greater than 8.5 remains unproven.

## Open gates
No complete isolated checkout, Gradle wrapper/dependencies or Git CLI DNS; October 27 owner override assigns full game builds to the owner. **No current development JAR**. Full Java25 Gradle9.2.1, archive CRC/module identity, NeoForge native save/reopen/shutdown, Windows AMD8060S Vulkan/OpenGL, finite-water ledger, LOD and terrain/water performance were **not run**. `gradle.properties` declares NeoForge `26.1.2.122`; 26.3/26.4 unverified. No old JAR deleted, no Actions or releases. Latest three Builder commits did not change renderer, water, terrain or LOD implementation. Preserve frozen V1-V7, saved profiles and edits.

Next: atomic durable chunk/stamp publication and crash recovery; native signed-border save/reopen; finite-water conservation; owner Java25/Windows GPU package qualification; fresh full-engine regrade.
