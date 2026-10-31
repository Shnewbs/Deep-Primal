# Dev127 independent audit — 2026-10-31 11:34 UTC

Pinned Builder source: `09f5eeb94241eb3c71389bb6bbec1dacc1032517`. Version: `0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1`. Previous Dev126 audit source: `4ba0e816761ab2777c26de681d5b29d64bb6c28b`.

## Confirmed test defect and fix
The Dev127 coordinate-bypass mutation was incorrectly accepted as an expected `IllegalArgumentException` by `rejectCoordinate`, because a *different* V8 border-coordinate guard also throws that exception. The runner then failed at `check 72011`, not with the required `coordinate overflow admitted` marker. The original committed runner therefore failed its own negative control. Reproduced with the exact old fixture blob `c416266918b11bc73cdb47968f0242a47768c126`.

Updated `tools/tests/V8NativeEpochPreflightChecks.java` to accept only the exact production coordinate-overflow message; all other `IllegalArgumentException` instances are now reported as mutation failures. Commit `98b9e05db46ca98ca5337220b309417170d7c726`; fixed fixture blob `fe8d097ce23d9038d70bcc355087c166cf02c7e1`. No production engine code changed.

## Verification
- Exact production native coordinator blob `be9f88ba2eacb3b59530bf24bcd18542a103f06f`, NBT adapter `33782f5ef55b904c4cb3c3ac261d794ef5306c0e`, and three core dependencies compiled with Java21 strict flags and isolated Minecraft NBT doubles.
- Exact committed Python runner `f5d5e1031d362a249ec3d769a67151970fcf1cb5` passed **2 × 72,037 assertions**, rejecting both epoch and coordinate bypass mutations.
- Independent signed-coordinate 64-bit oracle passed **200,032 assertions**, including 50,000 deterministic randomized pairs and no owner snapshot on invalid coordinates.
- **344,106 total positive assertions**; the two mutation runs were separate negative controls. Pre-fix runner exited 1; corrected runner exited 0.

## Acceptance limitations and grade
No full repository checkout, Git CLI DNS, Gradle dependencies, JDK25, native Minecraft save/reopen, finite-water benchmarks, Vulkan/OpenGL GPU validation, 26.3/26.4 compatibility, or qualified JAR. `gradle.properties` still declares NeoForge `26.1.2.122`. No old JAR deleted; no GitHub Actions. The last accepted whole-engine grade **50/100 (5.0/10, Tier 2)** is carried forward, **not freshly regraded**; >8.5 target remains unverified.

Evidence ZIP: `DeepPrimal_Dev127_IndependentAudit_20261031T1134_09f5eeb9_98b9e05d_Evidence.zip` (21,799 bytes), SHA256 `185e733daf47282879251828f049c6f491b83371029d186ba4e183c34d1961ff`, ZIP CRC verified. The ZIP was delivered as a conversation artifact, not a game build.

Next: authoritative atomic V8 chunk-data/stamp publication, saved-world crash/reopen replay, Java25 Gradle packaging, finite-water conservation and Windows Vulkan/OpenGL acceptance.
