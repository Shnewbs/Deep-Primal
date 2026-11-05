# Deep Primal dev148 — far-horizon admission validation

Date: 2026-11-01 UTC. Scoped owner: hourly Deep Primal engineering.
Starting source head: `2edb3bb2a270c216e60cdb2fb4e36ad24b914a1a`.
Independent preceding audit: `docs/engine-hourly-audits/2026-11-01T0630-dev146/REVIEW.md`.

## Reproduced defect and correction

`FarTerrain.inRange()` previously squared `chunks` before comparing a tile's nearest distance. Negative radius inputs (including `Integer.MIN_VALUE`) therefore admitted tiles as though a positive render distance had been configured. This violates fail-closed horizon tile selection. Baseline isolated Java 21 run failed at `negative range accepted: -1`.

The corrected method returns false for negative chunk radii or nonfinite camera X/Z before calculating distances. All finite camera positions and nonnegative radii retain their existing tile-rectangle distance formula, including zero-radius boundary contact. This is an engine-core horizon admission guard; it does not change Vulkan-native code, LOD mesh generation, water physics, or persistence.

## Reproducible local validation

Run `bash docs/validation/engine-continuation/far-terrain-admission-2026-11-01/run.sh` from any working directory with JDK 21+.
The isolated runner compiles the exact production `FarTerrain.java` and regression source with `javac --release 21 -Xlint:all -Werror`, then runs `java -ea -Xverify:all`. Output: `FAR_TERRAIN_ADMISSION_PASS checks=300067`.

The regression uses an independently implemented nearest-point rectangle oracle for 300,000 seeded valid-input comparisons; 6 negative-radius rejection checks; 6 NaN/infinity rejection checks; five zero/threshold boundary cases; and 50 signed tile key/interior checks. The unchanged baseline failed on the first negative-radius control. The corrected source passed all checks. `CoreChecks` now registers this regression; the complete CoreChecks suite was not executed.

Production source SHA-256: `a85ba6a3ee5dabf8b8a3b4e1b8a9e006d8750c4f799ef513076dae4bcad7431a`.
Production Git blob: `fd1a247028c9a4fda99422515438027b809ea2eb`.
Regression Git blob: `a0844714fbecdeaffbc6fb98b9c84e1f84128cc9`.

## Build and acceptance blockers

This runner has Java 21, but no JDK 25, complete local repository checkout, or cached Gradle/NeoForge dependencies; local Git CLI cannot resolve github.com. No full Gradle build, NeoForge playable JAR, game JAR SHA-256, native GPU test, or Minecraft runtime validation is claimed. No GitHub Actions were used. Preserve the previous qualified JAR until a replacement is built and verified. Whole-engine historical audit grade remains 5.0/10, not updated by this scoped regression.
