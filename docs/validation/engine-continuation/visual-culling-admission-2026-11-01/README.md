# Deep Primal dev149 — visual culling admission

Date: 2026-11-01 UTC. Scoped writer: hourly Deep Primal engineering.
Starting source head: `b05984693a47422348665a2c4756a8da41df6efd`.
Consumed independent audit: `docs/engine-hourly-audits/2026-11-01T0830-dev148/REVIEW.md`.
The auditor's separate water-snapshot quantity repair was preserved; this change does not overlap its source files.

## Reproduced defect and correction

`VisualBudget.outside()` previously computed squared distances without validating its AABB or camera coordinates. NaN makes the final `>` comparison false, silently admitting optional visual work. An inverted AABB could also be treated as a valid geometry envelope. The original exact source fails the regression at `invalid coordinate must be culled: index=0`.

The corrected method fails closed for nonfinite camera/AABB values and inverted AABB axes when the positive radius culling policy is enabled. A zero or negative radius retains the existing unlimited/disabled culling contract. Valid finite, ordered AABBs retain the original squared-distance computation and inclusive radius boundary. This is an engine-core CPU visual admission guard; it does not modify native Vulkan/LOD draw submission, terrain geography, water conservation, or saved-world data.

## Reproducible validation

Run `bash docs/validation/engine-continuation/visual-culling-admission-2026-11-01/run.sh` from a complete checkout with JDK 21+. The script compiles the exact production `VisualBudget.java` and registered `VisualBudgetOutsideChecks.java` using `javac --release 21 -Xlint:all -Werror`, then runs `java -ea -Xverify:all`.

Result: `VISUAL_OUTSIDE_PASS checks=350057 invalidInputsRejected=true finiteParity=true independentOracle=true`. Includes 250,000 seeded finite-input parity comparisons with the original formula, approximately 100,000 independent box-distance oracle cases, explicit inclusive boundary cases, 27 nonfinite input cases, three inverted axes and unlimited-radius preservation.

Negative controls: 4/4 rejected (unchanged original, missing inverted-AABB guard, missing camera-finiteness guard, and altered unlimited-radius semantics).

Production source SHA-256: `4947be98d1bb9504a045a2bdd32c2954e10fded82d267466390f7db98ff94b7c`; Git blob `11a728c82b0b1e8dc37c8f35756c73025c4fa04e`.
Regression source SHA-256: `983493a6b873d5824727af4a38d171c67c7e2ac38b7982fe0be8383a002c0064`; Git blob `adde03239f5b2c228f2b6a509e7830ec5a9db14c`.
CoreChecks registration Git blob: `7b90bc9ac9458ade95485b015fad693ab0ad172c`.

## Qualification limits and next actions

Only an isolated Java21 engine-core check was executed. Full CoreChecks, Java25 Gradle 9.2.1/NeoForge, game launch, Windows AMD/Vulkan/OpenGL culling behavior, GPU performance and long-soak water/V8 persistence tests remain unrun. The environment has no complete private checkout, Java25, cached Gradle/NeoForge dependencies or local GitHub DNS resolution (`git ls-remote` exits 128). No GitHub Actions were used. No playable replacement JAR or JAR SHA-256 was produced; do not delete the last successful build.

The historical independent whole-engine grade remains 5.0/10 pending a complete audit. Next: restore the full build pipeline, qualify a playable JAR, test native finite-water persistence and V8 save/reopen, then measure Vulkan/LOD visual and frame performance.
