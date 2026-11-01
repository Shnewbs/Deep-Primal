# Dev151 — independent hourly Deep Primal audit

**Audit date:** 2026-11-01 12:30 UTC. **Baseline:** `ff2f92fa00cae01e402186f1f18482a244352b48`. **Inspected head:** `ca5d8674fa7d011c860cb2f1eea4c84d505d59dc`. **Commits reviewed:** 7, linear/ahead; no divergent commits in compare.

## Changed surface and review
- Production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterDynamics.java` (water-color mix, climate palette, wave displacement).
- Regression: `WaterDynamicsPresentationFiniteChecks.java`, registered in `CoreChecks.java`.
- Reproducible runner and engineering evidence: `docs/validation/engine-continuation/water-presentation-finite-2026-11-01/`.
- The Builder's scoped claim has `status: completed-released` and `lease_released: true`; no conflicting production edit was made by this audit.

## Independent verification
Copied the exact production source from GitHub; local SHA-256 **`928b97fd0343bc9ca2f9e7c528be1bfddd858d7686cd4866e097c160b859b373`**, matching the Builder's published source hash (Git blob `364a5e25495ffa8b9e0bbf672106cb118ff9ef0f`).

Compiled with **OpenJDK 21.0.12.1**, `javac --release 21 -Xlint:all -Werror`; ran `java -ea -Xverify:all`. **1,170,764 independent assertions passed**:
- 400,000 finite color-blend parity cases against the original arithmetic.
- 250,000 finite climate-palette parity cases.
- 250,000 bit-exact ordinary wave parity cases and 250,000 finite wave-amplitude checks.
- 20,736 extreme finite input combinations; all water displacements remained finite and bounded.
- 24 nonfinite input cases, neutralized as specified, plus four direct controls.

The independently constructed pre-fix mutation (removed mix/palette/wave guards) **failed as expected with exit code 1** on `invalid blend NaN`. No additional reproducible defect was confirmed. No production correction was necessary.

Local evidence archive: `DeepPrimal_Dev151_IndependentAudit_20261101T1230_Evidence.zip`, 7,721 bytes; SHA-256 **`bfb5d08f7e38744c1d451bc6c26ba9f9183b8e6c0228ddc44758e7fa66c4804d`**; ZIP integrity verified. Includes exact production snapshot, independent test, pre-fix mutation, compilation/test output, environment diagnostics and hashes.

## Acceptance and compatibility gates
- **Scoped presentation arithmetic:** PASS (isolated Java21 checks). **Grade: provisional 8.5/10**.
- **Native water flow/conservation and save/reopen:** NOT TESTED.
- **Terrain worldgen seams and asynchronous chunk completion:** NOT TESTED in native client/server.
- **Renderer, Vulkan/OpenGL, LOD, GPU readbacks and frame performance:** NOT TESTED.
- **NeoForge:** Repository `gradle.properties` still pins `26.1.2.122`; `build.gradle` requests Java25; wrapper task specifies Gradle 9.2.1. Compatibility with 26.3/26.4 **not demonstrated**. Official NeoForge 26.x guidance requires Java25 and Gradle >=9.1.
- **Full project build / playable JAR:** BLOCKED. Execution environment has only Java21, no complete local private checkout, no Gradle installation/cache, and Git CLI cannot resolve github.com. Source was read via authenticated GitHub connector. No verified JAR or JAR SHA-256 exists from this run; no prior JAR was deleted. **No GitHub Actions**.

Historical whole-engine grade **5.0/10** remains an *unretested baseline*, not a new comprehensive score. Prioritize a complete JDK25 + Gradle checkout and packaged smoke test, then native water, terrain, persistence and Vulkan/LOD acceptance.
