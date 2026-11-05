# Dev137 independent Deep Primal audit — 2026-10-31 21:30 UTC

## Scope and repository state
- Audited commit: `a4cc8267913a2c852983fac0730a2f434992efb6` on `main`, compared with prior auditor head `1b7046a0c5cb6f367aa76a1fd8259525b6b4b50d` (two commits).
- Reviewed `VisualBudget.frameLimit`, its `FrameLimitMixin` call path, `ClientPerformanceConfig` validated bounds, CoreChecks registration, and the Builder's Dev137 evidence/claim. No overlapping Builder source files were changed by this audit.
- NeoForge declared version remains `26.1.2.122` in `gradle.properties`; Java 25 is required by `build.gradle` (toolchain). Source metadata still names Dev127. No native NeoForge 26.3/26.4 compatibility was tested.

## Independent verification
- Reconstructed the exact committed `VisualBudget.java` and `VisualBudgetFrameLimitChecks.java` from the GitHub connector. Their SHA-256 digests match the Builder's documented digests exactly:
  - production `7c96fe8aec9160ba9a5ee1d0202eb5475bf44d2bdff525ba44de32ceb4362989`
  - test `ee88ce5df1e3757099ee11c9d11662999d4dfd0de944c8f0cf22f34028e671e3`
- Java 21 `--release 21 -Xlint:all -Werror` compilation succeeded. The committed test passed **100,565 checks**.
- Independent state-transition/property test passed **2,829,154 assertions** over **300,000 deterministic trials**, including native-unlimited, lower native caps, minimized precedence, disabled override, refocus, unrelated-config invariance, and positive-cap monotonicity.
- Four deliberately defective implementations were **all rejected**: original zero-native cap bug, native cap increase, ignored minimized state, and disabled-override regression.
- The first draft of the independent test incorrectly assumed monotonicity across cap=0 (disabled) to cap=1 (enabled). That *test oracle* was corrected before final validation; this was not a production defect.
- **No new confirmed production defect** in the changed code. No corrective source commit warranted.

## Broader acceptance and grade
- Whole-engine baseline: historical **5.0/10**, **not freshly regraded**; provisional scoped frame-limit policy correctness **8.5/10** based only on isolated tests.
- Not tested: native Minecraft frame pacing, renderer Vulkan/OpenGL, actual terrain/water, V8 persistence, LOD throughput, 26.3/26.4 compatibility.
- Full Gradle/NeoForge JAR build **blocked**: this execution environment has Java 21 only, no full repository checkout or dependency cache, and `git ls-remote` cannot resolve github.com. GitHub connector reads are available but do not provide a complete local checkout.
- **No JAR produced or qualified; no JAR SHA-256; no previous audit JAR deleted.** No GitHub Actions used.

## Next acceptance gates
1. Restore a complete local checkout and JDK25/Gradle dependencies; build and launch a verified mod JAR without GitHub Actions.
2. Test native FPS transitions: focused unlimited -> unfocused 20 -> minimized 5 -> refocused unlimited, both Vulkan and OpenGL, plus native FPS ceilings below configured limits.
3. Run V8 crash/reopen persistence, finite-water conservation, terrain boundary, LOD and real FPS/frametime benchmarks before raising whole-engine grade.
