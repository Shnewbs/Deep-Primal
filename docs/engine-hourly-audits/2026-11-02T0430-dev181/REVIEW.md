# Deep Primal Dev181 — independent audit and repair
**UTC window:** 2026-11-02 04:26–04:30. **Base:** `2da5a9270f8ad4c0ec99741319fb8b4fca721af2`. **Engineering head reviewed:** `6f6de80a13845b230a45bb28f5c1010439bb65f7`. **Scope:** 11 intervening commits; terrain, water animation, LOD, horizon safety, compiled-artifact hygiene and build configuration.

## Confirmed defect and fix
Commit `140d43b6cd3ee6ae199fbb2d1ed29a75e2d37ebb` changed `SoilProfile.at(Sample, Column)`, which is explicitly documented as **frozen material revisions 0 and 1**. The new stochastic rain transition made dry samples at rain 0.34 return LOAM rather than the historical SAND, violating legacy world replay. The same revision-2 code leaves the upper half of its intended 0.33–0.37 blend unreachable: at rain 0.36 it returns 0/10,000 SAND across a deterministic ground sweep.

The repair restores the exact legacy material predicate **only for the legacy entry point**, preserving the current revision-2 dither byte-for-byte rather than silently changing saved revision-2 terrain. The revision-2 blend flaw requires a separately versioned material/world-generation upgrade, not an unversioned hotfix.

**Committed corrections:**
- `d681ac550406cb3829d948f7f8f749b3c0d9f587` — `engine-core/.../SoilProfile.java`, Git blob `81e400c94b51ca41479e69a466b8a8ee66847f7b`.
- `6c229337c789c7bbad32d688bff719683e4d107c` — `SoilProfileLegacyReplayChecks.java`, Git blob `21bea98edcc57656c357d37856f85712f35ea410`.
- `97ed5bdf4c0643f2845763f9998437373c228b60` — register regression in `CoreChecks`.

## Independent reproduction and validation
Exact pre-fix production source reconstructed with Git blob SHA `73e198e0cca7f6a31fcd8152ac516d172eea9928`. Java21 `javac -Xlint:all -Werror` compiled the production class and deterministic dependency doubles. The pre-fix source failed the new legacy replay regression at sample **728**. The corrected production class passed **150,001** assertions: 100,000 randomized frozen-legacy soil samples plus 50,000 revision-2 classification samples and one frozen distribution guard. Revision-2 sand counts stayed exactly `[10000,6256,4998,0,0]` at rain `[.329999,.34,.349999999,.36,.370001]`. This is isolated Java testing, **not** a full Minecraft/NeoForge build.

## Additional audit findings
1. The merge commit `6acce55a879e39f7a2a2a2529970e15948d4cb9c` records eight conflicted source/test/lease paths. Its first 300 compare entries include many committed `bin/**/*.class` files. Clean tracked build outputs separately after owner approval; do not conflate binary cleanup with a validated gameplay fix.
2. Dev180 `HorizonView` now fails closed for invalid workload, FOV, viewport and camera origins, and has a new core regression. Source inspected; no independent full runtime LOD acceptance.
3. Native water, renderer, terrain seams, persistence/reopen, Vulkan/OpenGL FPS and cross-version NeoForge compatibility remain **unverified** in this environment.
4. `gradle.properties` remains at NeoForge `26.1.2.122`; root Gradle targets Java25 and wrapper 9.2.1. NeoForge 26.3 supports Java25; 26.4 is still in snapshots as of 2026-11-01.

## Packaging gate
Local runtime has OpenJDK 21.0.12.1, no complete repository checkout, no JDK25, no local Gradle dependency cache and GitHub DNS resolution fails. GitHub connector supports source reads/writes but not a local private clone. **No new playable JAR, JAR SHA256, client/server smoke test or benchmark was produced.** No previous audit JAR was deleted. No GitHub Actions were used.

## Evidence-based grades (provisional)
- Frozen legacy soil replay: **8.5/10**, isolated deterministic regression + original negative control; not runtime-tested.
- Whole-engine acceptance: **5.0/10 historical baseline**, not newly regraded; insufficient packaged/native evidence.
- JAR deliverability this run: **0/10**, blocked by environment, not code compilation proof.

**Next:** obtain Java25 and a full private checkout with resolved Gradle dependencies; run `./gradlew clean check jar`, then launch native client/server, validate saves, terrain seams and water mass/chemistry conservation, and measure Vulkan/LOD frame-time percentiles. Only after a verified replacement JAR exists should the previous audit JAR be removed.
