# Dev152 — independent hourly Deep Primal audit

**Date:** 2026-11-01 13:30 UTC. **Prior audit:** `36cad80c51c3eb4e6c832269ac085e021afce3f5`. **Builder head reviewed:** `9fc9fe4c1058651f73c083f03716821c3fbf9dab`. **Change count:** six linear Builder commits, five changed files. **Scope:** finite waterway terrain admission, regression coverage, NeoForge build gates, adjacent engine acceptance status.

## Confirmed defect

The Builder's finite-height guard correctly prevents NaN/Infinity from escaping `WaterwayShape.height()`, but a **finite** upstream height beyond double's unit precision can still suffer catastrophic cancellation in full-strength coast, river, or tributary blending.

Reproduction on the exact original Git blob `5572aabb7a83779acb40eb121afe34039f144942`:
- `coastHeight(1e18, -0.20, 0.8, 96)` returns **128.0**, while the full-weight shelf target is **91.5**.
- `coastHeight(Double.MAX_VALUE, -0.20, 0.8, 96)` returns **0.0** rather than **91.5**.
- Full-strength river and tributary carving can similarly lose the exact bed target (89.0).

This is an **extreme finite-input robustness defect**, not evidence of a normal-worldgen failure.

## Correction and regression

The correction takes the exact shelf or bed target when the corresponding blend weight equals 1.0 and the finite incoming elevation magnitude is at least `2^53`; the existing arithmetic remains unchanged for normal finite terrain values. The change avoids altering the V1–V7 shaping constants or ordinary-worldgen bit parity.

- Production commit: `ce9adcde8063e6a18ad7b6fff2a8a2ced4578f91`; exact production Git blob `4f0526cad27e801e1b885199ced780f3b6043c61`.
- Regression commit: `d77a3bc0c89dca79e1eb1f5472586159ca7604e4`; exact regression Git blob `f6f222cba21a3b4eb928ff98c53e507ad68dd6cf`.
- CoreChecks registration: `7ca96926a23ef42f22306b8e14ed676cf6b6313c`.

## Independent verification

**PASS — isolated OpenJDK 21 strict compilation** (`javac --release 21 -Xlint:all -Werror`), with `java -ea -Xverify:all`:
- New precision regression: **600,008 assertions passed**, including 300,000 bit-exact ordinary finite-domain comparisons, 300,000 finite-output checks and eight targeted assertions.
- Existing Builder finite-admission regression: **600,020 assertions passed**.
- **Three independent mutation controls rejected:** removing the coast, river, or stream precision fallback separately causes the new regression to fail.
- The tested production and regression source files were verified against the GitHub Git blob SHAs.

Local evidence archive: `DeepPrimal_Dev152_IndependentAudit_20261101T1330_Evidence.zip`, **8,501 bytes**, SHA-256 `9cae42b3c84585a5e0e871549d44c7c8166bcf7a82f5d5051134263f2e8b6cf8`; ZIP integrity checked.

## Acceptance and compatibility gates

| Gate | Outcome |
|---|---|
| Six new Builder commits reviewed | Complete |
| Finite waterway admission and precision | Passed isolated Java21 checks; provisional **8.5/10** |
| Full native finite-water conservation, V8 save/reopen | Not tested |
| Terrain seams and async worldgen native integration | Not tested |
| Vulkan/OpenGL, LOD, GPU FPS/performance | Not benchmarked |
| NeoForge 26.1.2.122 configured | Verified in `gradle.properties`, not runtime-tested |
| NeoForge 26.3/26.4 compatibility | Unverified |
| Full Java25 NeoForge build / playable JAR | Blocked |

The local runtime provides OpenJDK21 only; no complete private checkout, Gradle executable/cache or GitHub DNS for Git CLI. The repository's wrapper specifies Gradle 9.2.1, and its build requests JDK25; official current NeoForge getting-started documentation also specifies JDK25. **No playable JAR or JAR SHA-256 was produced. No superseded JAR was deleted. No GitHub Actions were used.**

The historical whole-engine baseline **5.0/10** remains untested; scoped test scores are not a whole-engine regrade. Next: restore complete JDK25/Gradle packaging, then native finite-water conservation, save/reopen, terrain-border and Vulkan/LOD acceptance.
