# Dev142 independent audit — water surface finite-endpoint overflow

**Audit date:** 2026-10-31 19:30 America/Los_Angeles (2026-11-01 UTC)  
**Base:** `bda1b3c588a7e0f9efcc1dfbfa614fafb6423ace` (Dev141 independent audit)  
**Builder source reviewed:** `95ccce33f75c9579dbaaa20516bcce5a88d2730d`, `a9161060f82222f1ee0e956776ae97699341a76d`, `06830712be41554182add8dbe4cab5bc71464c2a`  
**Builder lease:** `docs/ENGINE-MAIN-WRITER-LEASE.json` status checkpointed, released; no active overlapping claim at the time of this audit.

## Confirmed defect and correction

The Dev142 guard rejects nonfinite input samples, but two *finite* float endpoints can still produce a nonfinite intermediate value. Reproduction with production blob `7746a03a2e6a116e22cd6a7e4750d40fd7587b4f`: construct `WaterSurfaceTransition(Float.MAX_VALUE, 0)`, call `target(-Float.MAX_VALUE, 10)`, then `at(10)`. The original implementation computes `target - from == -Infinity` and `-Infinity * 0 == NaN`. Subsequent retargets may inherit nonfinite state. This is a presentation-only edge case; it does not establish that ordinary [0,1] water heights can reach this state.

The fix preserves the exact existing float result for ordinary finite interpolation. Only when the float result is nonfinite does it recompute the interpolation using double precision, whose finite endpoint range is sufficient to avoid overflow. No solver, saved-world schema, native renderer, GPU or LOD code was changed.

- Production correction: `4a1e8e2512fde18c2d32c398acf09dd1be8a781c`
- New randomized regression: `cba86a6f73fa86bcd2efd6a9cf33ee92f99d90c9`
- Registered in `CoreChecks`: `2b8fc630e8fe17692b813f201e1c22ba04a481a8`

## Local independent verification

- Source identity: original Git blob `7746a03a2e6a116e22cd6a7e4750d40fd7587b4f`, patched production blob `1ebc0df3cc85dafa059bbb9cd02203188b6eb1eb`, new test blob `07578ef295bd7802447f7e9b72e07c7c6efb3382`; all match GitHub.
- Java: OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all,-cast -Werror`, `java -ea -Xverify:all`.
- Existing `WaterSurfaceFiniteChecks` reproduced 1,000,027 passing assertions, including 200,000 finite-input parity operations. The local baseline copy was derived from the reviewed source; this is isolated verification, not a full Gradle CoreChecks run.
- New `WaterSurfaceOverflowChecks` passed **2,200,006 assertions** across 100,000 deterministic randomized extreme finite endpoint pairs, 1,100,000 samples, versioned/unversioned retargets, and a double-precision oracle.
- Negative control: original production source fails the new regression immediately at check 1 (`finite initial interpolation at t=0`).
- Total isolated passing assertions: **3,200,033**.

## Scope and acceptance

| Gate | Result |
|---|---|
| Three new Builder commits reviewed | Pass |
| Nonconflicting defect reproduced and fixed | Pass |
| Java21 isolated strict compilation and negative control | Pass |
| Corrected source and registered regression committed to main | Pass |
| Complete Java25 Gradle/NeoForge JAR | Blocked: no complete checkout, Java25, or cached Gradle dependencies |
| Live finite-water save/reopen and V8 persistence | Not tested |
| Vulkan/OpenGL, LOD, terrain native performance | Not tested |
| NeoForge 26.3/26.4 compatibility | Unverified; repository still pins 26.1.2.122 |
| Replacement JAR/SHA | Not produced; superseded audit JARs not deleted |
| GitHub Actions | Not used |

**Grades:** Scoped water-presentation finite-input safety **8.5/10 provisional**. Whole-engine **5.0/10 historical carried-forward baseline**, not freshly regraded. No whole-engine acceptance claim.

**Next:** Restore a complete Java25/Gradle build environment, produce and hash a playable NeoForge JAR, then run native water conservation/save-reopen, terrain-border, Vulkan and LOD performance tests. Keep the previous verified JAR until a replacement is successfully built and SHA-verified.
