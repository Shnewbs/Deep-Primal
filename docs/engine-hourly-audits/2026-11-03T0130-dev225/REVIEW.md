# Dev225 — Independent Deep Primal audit (2026-11-03 01:30 UTC)

## Baseline and change review
- GitHub compare `0a39e28cd39e915f430e04c5343162d64f8c6760...main`: **identical, zero new commits and zero changed files** at review time.
- No new production regression can be attributed to intervening commits; do not claim new fixes or test passes.
- Root `gradle.properties` still specifies NeoForge `26.1.2.122`; `build.gradle` specifies Java toolchain 25 and ModDevGradle `2.0.148`.
- Previous Dev223 report provides isolated distance-policy tests, not end-to-end runtime acceptance.

## Local build preflight
- Local `java -version`: OpenJDK 21.0.12.1, below the requested Java 25 toolchain.
- `git clone --depth 1 https://github.com/Shnewbs/Deep-Primal.git /tmp/deep-primal-audit` failed: `Could not resolve host: github.com`.
- No complete local checkout, resolved dependencies or verified game runtime available. **No Gradle build or runnable JAR produced.**
- No JAR SHA256 exists for this run; do not delete superseded JARs.
- No GitHub Actions were invoked.

## Audit matrix and evidence-limited grading
| Area | Evidence | Result |
|---|---|---|
| New commits | GitHub compare: identical | No new changes |
| Engine/core | Dev223 historical isolated test only | Not re-executed |
| Finite water / persistence | No runnable client/server | Unverified |
| Terrain and save/reopen | No runnable world | Unverified |
| Renderer / Vulkan / OpenGL | No native client or frame capture | Unverified |
| LOD and FPS | No performance capture | Unverified |
| NeoForge 26.3/26.4 | Pinned to 26.1.2.122, no alternate-version compile | Unverified |
| Packaging | Java 21 and clone DNS failure | Blocked |

- Whole-engine grade: **not reassessed** (historical 5.0/10 is not new evidence).
- Release readiness: **2.0/10 provisional** due to missing build and runtime verification.
- No production code was modified because no nonconflicting defect was independently reproduced.

## Next acceptance gates
1. Restore GitHub checkout and Java 25 with dependency resolution.
2. Run `coreChecks`, `check`, and a full local `build` without GitHub Actions.
3. Validate resulting JAR metadata, archive integrity and SHA256.
4. Run client/server startup, finite-water conservation and save/reopen, terrain seams, Vulkan/OpenGL fallback, LOD stability and measured FPS.
5. Delete an older audit JAR only after the replacement is verified.
