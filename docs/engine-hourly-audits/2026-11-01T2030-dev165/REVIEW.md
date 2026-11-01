# Dev165 — Independent Deep Primal audit (2026-11-01 20:30 UTC)

**Baseline:** `6ea26073cc4573fbae36f6b037ccf4438d500154` (Dev163 audit).  
**Builder head reviewed:** `dc48e7372f27f64fb789defd3a554c690decb4f2` (Dev164).  
**Scope:** independent three-commit review, aquifer numerical regression, finite water-account transfer invariants, toolchain/build gate and NeoForge version watch. **No GitHub Actions.**

## Commit and file review

[GitHub comparison](https://github.com/Shnewbs/Deep-Primal/compare/6ea26073cc4573fbae36f6b037ccf4438d500154...dc48e7372f27f64fb789defd3a554c690decb4f2) reports **3 commits, 5 changed paths**:
- `95aa9b112e56445d4c0602495f938b344861153b` — bounded writer claim;
- `2bb335568102cf6b88caf8c2ba1605b741582c63` — `Aquifer.baseflowUnits` signed-int saturation and nonfinite `dischargeUnits` admission;
- `dc48e7372f27f64fb789defd3a554c690decb4f2` — builder verification and released writer claim.

The changed production file is `engine-core/src/main/java/com/shnewbs/deepprimal/core/Aquifer.java`; one new test `AquiferFlowAdmissionChecks.java` was registered in `CoreChecks.java`. The two remaining changes are claim/validation documents. **No renderer, LOD, terrain, native worldgen, or Vulkan files changed in this delta.** Their runtime and performance status is **not retested** in this audit.

## Independent exact-source tests

Fetched GitHub files and reconstructed the complete source locally; `git hash-object` matched both remote blob IDs:
- `Aquifer.java`: `b20fa40eff5d2200bd76e83b98423d9ae7ddf952`;
- `WaterAccount.java`: `d8512869d8e06740d57898381145ece344f4661b`.

Auditor-authored `IndependentAquiferAudit.java` passed **2,374,581 assertions** under Java21 with `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`. Checks include:
- 262,144 monotonic BigInteger-referenced quota grid samples and 250,000 randomized quotas;
- 600,000 finite aquifer pressure cases across depths 1/2/16/64 plus 120 nonfinite datum/surface admissions;
- 100,000 randomized conserved `WaterAccount.move/distribute` transfers, checking water/salt/sediment totals and nonnegative stores;
- extreme signed-long aquifer tick ordering, bounded output, and pure read-only discharge behavior.

**Mutation controls 2/2 rejected:** removing quota saturation failed at assertion 415,063 (input 1,700,093,952); removing the nonfinite pressure guard failed at assertion 774,313. These independently reproduce the two Dev164 failure modes and validate the repaired behavior. No additional confirmed defect emerged; **no production change was warranted**. These are exact-source *isolated* tests, not full NeoForge execution.

## Compatibility and acceptance gates

`gradle.properties` pins `neo_version=26.1.2.122`; root `build.gradle` specifies Java25 and moddev `2.0.148`, and the wrapper specifies Gradle `9.2.1`. The official NeoForge 26.3 branch specifies `java_version=25` ([source](https://github.com/neoforged/NeoForge/blob/26.3.x/gradle.properties)). Public version trackers list stable NeoForge 26.3 builds by 2026-11-01, while Minecraft 26.4 is still in snapshots ([version tracker](https://gamehosting.pl/najnowsza-wersja-minecraft/)). **Deep Primal has not been built or validated on 26.3 or 26.4**.

Local runtime has OpenJDK `21.0.12.1` only, no complete private checkout, no Gradle distribution or resolved NeoForge dependencies, and no `github.com` DNS resolution for local Git. The linked GitHub connector allowed source review and this documentation commit. **No full Java25 NeoForge build, runnable JAR, JAR SHA256, Vulkan/OpenGL benchmark, terrain-seam test, native water-conservation run, or save/reopen test was possible.** No existing playable JAR was deleted; no replacement was produced.

## Grading and next gates

- **8.5/10 provisional scoped aquifer arithmetic/admission correctness**, based on exact-source independent property checks and both rejected negative controls. Not a native runtime grade.
- **5.0/10 historical whole-engine baseline** retained; not independently regraded this run.
- Renderer, LOD, native water, terrain, persistence, and NeoForge 26.3/26.4: **unverified** (no numerical grade assigned).

Next: obtain full checkout, JDK25 and Gradle dependencies, run the complete NeoForge build and `coreChecks`, validate JarJar manifest, generate a playable JAR and SHA256, then conduct finite-water conservation, save/reopen, terrain seam, Vulkan/LOD and FPS acceptance. Preserve previous JAR until the replacement is verified.

## Reproducible local evidence

`DeepPrimal_Dev165_IndependentAudit_20261101T2030_Evidence.zip` — SHA256 `4f836e56f94eb198c6dbf1ffff2398adec37fc07d486ecb1a3db7dac044cae69`. ZIP integrity verified. Contains exact production sources, independent test harness, two mutated sources, compiler/test/failure logs, environment diagnostics, hashes and `replay.sh`. This evidence is attached to the audit conversation, **not** committed as a binary to GitHub.
