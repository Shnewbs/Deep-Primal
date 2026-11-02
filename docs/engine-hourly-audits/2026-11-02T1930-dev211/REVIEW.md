# Dev211 — Independent Deep Primal audit (2026-11-02 19:30 UTC)

## Scope and coordination

Previous independent Dev209 audit: `2f9dc0aa02899490afe22c76c6d2ccf9f736af3a`. GitHub main at audit start: `0e5a0e5e34560e0c202b15b46fefb8703b01bd36`, **7 commits ahead, 0 behind**. All seven commits were inspected: Dev210's finite water-mesh phase protection, its registered regression, validation notes and writer lease lifecycle. The compare changed only `WaterMeshShape.java`, its test, `CoreChecks.java`, validation documentation and the writer lease. No terrain, LOD, native finite-water accounts, Vulkan backend or performance source was modified by those seven commits.

The shared writer lease was `completed-released` with `lease_released=true`, ending 19:10 UTC. This audit changed only the water mesh flow-UV method, its already registered dedicated regression test and this audit document. No GitHub Actions were used.

## Confirmed defect: float-rounded flow UV escapes [0, 1)

Dev210 correctly guards nonfinite **double** phases, but `flowingV()` still cast the valid fractional result directly to `float`. IEEE-754 rounding can convert a double fraction slightly below 1 to **exactly 1.0f**, outside the promised half-open atlas interval. This is an additional boundary defect, not a contradiction of Dev210's NaN protection.

Reproducer from the original production expression:

```java
double y = Math.scalb(1.0, -25); // 2^-25
double phase = 1.0 - y;
float oldV = (float) (phase - Math.floor(phase)); // 1.0f, invalid
```

Independent JavaScript float32 numerical replay (`Math.fround`, the same IEEE-754 binary32 rounding mode for these values) found **29 failing near-unit cases** for `y=2^-k`, `k=25..53`. The corrected calculation yields `0.9999999403953552f` (`Math.nextDown(1.0f)`) in these cases. A separate horizontal-phase construction `x=16-2^-21, y=1, z=0` reproduces the same issue.

## Fix and permanent regression

- Production commit `556d9b1feb8617c6f90ce79b93ec040ad0c654cd`: compute the original float value, then clamp **only if it rounded to 1.0f**. Ordinary values retain their historical bit pattern; nonfinite phase admission remains unchanged.
- Test commit `3b06890f23f0d54e6f871ee317fa86badc222fd6`: add 29 vertical near-unit and one horizontal near-unit assertions to the already registered `WaterMeshShapeFiniteChecks` suite.
- Both files were fetched back from GitHub and their new blobs verified present on main: production `33eb816b53ef6ff8dc37a770ff6b6906d53f0bc6`, test `e8e980867f5a59b97ff655097afa12fb8260139f`.
- Independent float32 model: 100,000 deterministic ordinary coordinate triples; **0 changed outputs** between original and corrected model and **0 invalid corrected outputs**. The original model fails the near-unit boundary check; the corrected model passes all 29 vertical checks.
- The Java regression has been **committed but not executed** this cycle. The local container and Python execution backends returned connection/service errors. Do not present this model-level replay as a Java compile, a complete `coreChecks` run, or an in-game GPU result.

## Compatibility and acceptance

`gradle.properties` still pins NeoForge `26.1.2.122`; no NeoForge 26.3/26.4 port qualification. Mojang's 26.4 Snapshot 1 (2026-09-22) changed the default graphics API preference to Vulkan and removed automatic API switching after a startup crash: https://www.minecraft.net/en-us/article/minecraft-26-4-snapshot-1 . Native Vulkan/OpenGL waterfall rendering, water conservation and save/reopen, terrain seam continuity, LOD frame times and VRAM are unverified. This isolated UV fix does not establish an in-game waterfall flicker repair.

Local runtime execution services were unavailable; a full Java25 Gradle/NeoForge build could not be attempted. **No playable JAR or JAR SHA256 produced**. No previous JAR was deleted. No GitHub Actions used.

## Grades and follow-up

Scoped flow-UV numerical correctness: **8.0/10 provisional** (deterministic boundary reproduction, corrected float32 replay and permanent regression committed; Java/GPU acceptance pending). Whole engine: **5.0/10 historical baseline, not freshly regraded**.

Next: recover local runtime and Java25 checkout/dependencies, execute `coreChecks`, `check`, `jar`, verify JarJar contents and SHA256, then test Vulkan/OpenGL waterfall UV seams, water persistence, terrain continuity and LOD performance. Replace/delete a previous audit JAR **only after** a new JAR is fully built and verified.

GitHub compare: https://github.com/Shnewbs/Deep-Primal/compare/2f9dc0aa02899490afe22c76c6d2ccf9f736af3a...0e5a0e5e34560e0c202b15b46fefb8703b01bd36
