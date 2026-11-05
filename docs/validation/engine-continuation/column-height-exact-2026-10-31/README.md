# Dev135 — exact signed column-height materialization

## Source scope and audit
- Reviewed the independent Dev134 audit at `docs/engine-hourly-audits/2026-10-31T1830-b2851422/REVIEW.md` (commit `91a8c3605bb144276f93b046c88e6e1f03b64b4a`), the engine roadmap and prior released claim. No active claim for `ColumnHeight` was found before the bounded claim in this folder.
- Changes are limited to `engine-core/src/main/java/com/shnewbs/deepprimal/core/ColumnHeight.java`, a new `ColumnHeightBoundaryChecks.java`, and one registration line in `CoreChecks.java`.
- `ColumnSurface.compute` invokes the corrected helper. Its codec restricts cell height to 1..64; tests also cover hostile direct helper inputs.
- No native V8 lifecycle, finite-water quantity, renderer/LOD worker, saved profile or asset change.

## Confirmed defects
1. For `surfaceY(Double.MIN_VALUE, -64, 64, 8)`, the original `height / step` underflows to zero. It selects cell `-8` instead of the zero cell even though the height is strictly positive. This is a real signed-world, codec-legal cell size.
2. `Math.floorDiv(upper, step) * step` can overflow a 32-bit signed integer when `upper` is near `Integer.MIN_VALUE`. With `surfaceY(3e9, Integer.MIN_VALUE, Integer.MIN_VALUE, 3)`, the original returned `Integer.MAX_VALUE`, outside the configured world; the correct fallback is `Integer.MIN_VALUE`.

The fix performs grid alignment in `long`, uses the greatest integer strictly below `height` before dividing by `step`, and clamps to `lower` before narrowing to `int`. The hot path remains constant time and allocation-free.

## Local verification
- Original committed production blob `b48533c85e846ee39184e8f10efbf24c0f035dc7` plus new test: **failed at check 1** on the positive-subnormal sentinel.
- Corrected production Git blob `852dc341919c9cff8d225d267491dfaac7c44617`, SHA-256 `e7563db49ab03a72140b7300bb612b2e188cd2d235bd15c3fc92046d1f39e707`.
- Test Git blob `a5084d53b5ca9f90874c267a0ae0f0e52dc5ff7c`, SHA-256 `8298a368bb0190ac1aec75f6a7bb4cf1b2807724e5a16e511fa0a49144b9ce56`.
- `CoreChecks.java` now invokes `ColumnHeightBoundaryChecks.main` (Git blob `4dfcf1a196cd784b139a7e7809cd9d7d9ed321fb`).
- Java 21.0.12.1, `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **504,412 assertions per run**, three deterministic positive runs, **1,513,236 total successful assertions**. A separate long-integer binary-search oracle checked 250,000 seeded randomized scenarios per run, plus signed extremes, exact cell boundaries, subnormal doubles and invalid input.
- Two independently compiled negative mutants were rejected: old floating division at check 1, old signed multiplication at check 3.
- To reproduce in a complete checkout, compile `ColumnHeight.java` and `ColumnHeightBoundaryChecks.java` with the flags above and execute `com.shnewbs.deepprimal.core.ColumnHeightBoundaryChecks`. The suite is registered in the repository's `CoreChecks` entry point, but that full entry point was not executed here.

## Build and acceptance limitations
The isolated runtime contains Java 21 but no complete repository checkout, JDK25, Gradle dependency cache or native Minecraft runtime; Git CLI cannot resolve github.com. GitHub source changes were committed through the connected integration, without GitHub Actions. **No full NeoForge JAR, qualified game test or JAR SHA-256 was produced; no prior JAR was deleted.** Real Minecraft worldgen/save-reopen and Vulkan/LOD performance acceptance remain open. Independent whole-engine baseline stays **5.0/10**, not freshly regraded.

Next: restore a complete Java25/Gradle checkout and produce a verified game JAR; then prioritize durable V8 chunk publication, finite-water save/reopen conservation and native GPU/LOD acceptance.
