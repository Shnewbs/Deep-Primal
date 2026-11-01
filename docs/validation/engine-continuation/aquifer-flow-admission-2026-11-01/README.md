# Dev164 — Aquifer finite-flow admission

**UTC:** 2026-11-01 20:01. **Base main:** `6ea26073cc4573fbae36f6b037ccf4438d500154`. **Claim:** bounded and released. **Production commit:** [`2bb3355681`](https://github.com/Shnewbs/Deep-Primal/commit/2bb335568102cf6b88caf8c2ba1605b741582c63).

## Defects and production correction

- `Aquifer.baseflowUnits(int)` computed a nonnegative long quota and narrowed it to signed `int` without an upper bound. For `Integer.MAX_VALUE` configured rain quanta the old expression returns a negative value (the mathematical result exceeds `Integer.MAX_VALUE`). The new implementation saturates at `Integer.MAX_VALUE`, retains the 32-unit floor and exact ordinary arithmetic, and remains monotone for nonnegative input.
- `Aquifer.dischargeUnits(...)` previously treated a positive-infinity source datum or negative-infinity receiving surface as effectively unlimited pressure, admitting a positive finite groundwater debit. Nonfinite datum or surface now fails closed to zero, without changing finite inputs.
- No water is minted by either change: this helper computes a quota or a proposed discharge, while actual delivery still requires a separate finite-account debit. The correction is not a claim of complete native water conservation.

## Exact-source reproducible validation

- Original `Aquifer.java` Git blob `9be73c20ab0ee8e93cff88bb5767481dfbd6cfd9`; exact source reconstructed locally and compared to the remote blob before modification.
- Final `Aquifer.java` Git blob `b20fa40eff5d2200bd76e83b98423d9ae7ddf952`, SHA-256 `07be6c3f78a43beb8251a0da2eeacc4a5f128a581af7854b4b72dd3f01787bc7`. Final `AquiferFlowAdmissionChecks.java` blob `a7ec2eeaeba0072f9eb75bbf7588b27f55818926`. Final `CoreChecks.java` blob `c3783def3099898136a7084e8efd41331498bdec`. All three identities were verified by fetching the final files from GitHub.
- Java 21 strict compilation `javac --release 21 -Xlint:all -Werror`, followed by `java -ea -Xverify:all`: **1,248,640 new assertions passed**, including a 524,288-value baseflow grid with a monotonicity check, 200,025 finite-discharge parity cases and 18 invalid-height cases.
- Existing `AquiferTickChecks` was reproduced from its exact Git blob `8126ba7423aa339bd26632df0c46e8f3b7334725`: **50,144 tick scenarios passed**. `WaterAccount.java` dependency exact blob `d8512869d8e06740d57898381145ece344f4661b`.
- **2/2 mutation negative controls failed as expected**: restoring the old narrowing cast fails at assertion 5; removing the finite-height guard fails at assertion 1,048,602.
- Local reproducible evidence ZIP: `DeepPrimal_dev164_aquifer_flow_evidence.zip`, SHA-256 `4646563599a4097ee1cf0170aed17b49f6839a375ffd92aa4899c9346606cb90`; integrity checked, includes production/test sources, strict logs, negative-control failures and a replay script.

## Scope, build and open gates

Changed production: `engine-core/src/main/java/com/shnewbs/deepprimal/core/Aquifer.java`; added `AquiferFlowAdmissionChecks.java` and registered it in `CoreChecks.java`. Organics, renderer, LOD, native hydrology and independent audit files were not modified. The writer claim is released.

**No playable JAR produced.** This isolated environment has Java 21 only, no full private checkout, no local Gradle distribution/cache, and no resolved NeoForge dependencies; local Git cannot resolve `github.com`. Connected GitHub API successfully published and verified source; GitHub Actions were not used. No prior successful game JAR was deleted.

**Whole-engine grade remains the historical 5.0/10**; this bounded regression does not regrade the engine. Next gates: restore JDK25 + complete checkout + dependencies, run full NeoForge build and native save/reopen/conservation, then Vulkan/LOD and waterfall hardware acceptance. A playable JAR and its SHA-256 must be reported only after an actual verified build.
