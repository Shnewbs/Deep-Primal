# Deep Primal Dev136 independent audit — 2026-10-31 20:30 UTC

## Commit scope
- Previous independent audit: `f9d70f5011bc98ea8e865272669c4e185af66f35`.
- New Builder commit: `914fca38c27bbff822155682849b5535e736a731` (signed ViewGrid ring relocation).
- Auditor correction: `9963173b36786ea1f335fcc9f8f2003d8416ca6a`.
- Builder claim `engine-20261031-view-grid-signed-boundaries` was marked `completed-released`; production ViewGrid code was not modified by auditor.

## Confirmed regression-test defect
The original `ViewGridBoundaryChecks.compare` caught `IllegalArgumentException` thrown by either the oracle or production code. A production exception for a *valid* representable window could therefore be misclassified as an expected invalid-window rejection.

Reproduction using exact fetched source:
- Production SHA256: `a02a461a4df030dfefefb5882c6fcb2a3076c3b4cf40a04500e805017528a0d7`.
- Original test SHA256: `9f11d8228ded3e8dbda6e58784eae9ea676f57732aeb799faf63c3e37aeaf896`.
- Java21 `--release 21 -Xlint:all -Werror`: original production/test PASS `293252` checks.
- Mutant throws `IllegalArgumentException` at valid `camera=Integer.MIN_VALUE+1, radius=1`. **Original test incorrectly PASSED** mutant (`293230` checks).
- Auditor separated oracle validation from production execution. Corrected test PASS `293252` checks and **REJECTS** targeted mutant at first case.
- Corrected test SHA256: `47f33da21d8682df3f03fae37b4d6dde064e11ca39ba9deda65447b2f8245e1f`.

## Grading and remaining acceptance
- Scoped ViewGrid mathematical correctness: **8.5/10 provisional** (BigInteger differential checks, no native runtime).
- Scoped regression-test sensitivity: **8.5/10** (targeted negative control rejected; not comprehensive mutation coverage).
- Whole-engine: **5.0/10 historical baseline**, not a fresh complete regrade.

`gradle.properties` remains on Dev127 version metadata and NeoForge `26.1.2.122`. Full Java25 Gradle/NeoForge build is blocked: no complete checkout, JDK25 or dependency cache in the isolated workspace; Git CLI cannot resolve GitHub. No native V8 persistence, finite-water, terrain, Vulkan/OpenGL, LOD or throughput validation was performed. No qualified JAR or JAR SHA was produced; existing JARs must be retained. No GitHub Actions used.

Evidence ZIP: `DeepPrimal_Dev136_IndependentAudit_20261031T2030_Evidence.zip` SHA256 `dbb57f28bf5b585e731087b286093909622c5f839f60a5f3752936f6e7e43366`.