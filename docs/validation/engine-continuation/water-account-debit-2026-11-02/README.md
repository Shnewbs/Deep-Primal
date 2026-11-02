# Dev208 — finite-water admitted-parcel debit (2026-11-02 UTC)

## Scope and coordination

Consumed independent Dev207 audit at `docs/engine-hourly-audits/2026-11-02T1730-dev207/REVIEW.md`. Main was `e833d7655862366b38fea1f839c625d4c5ae510b` before the claim. The engine lease was released, and no conflicting active claim was found. This cycle owns only `WaterAccount.java`, a dedicated `WaterAccountDebitChecks.java`, its `CoreChecks` registration, this evidence and the engine lease. No native hydrology, worldgen, renderer, organics, saved-format, Gradle or Actions edits.

## Change

`WaterAccount.move()` and `distribute()` previously called `preview()` to calculate the admitted water/salt/sediment parcel, then called `take()`, which calculated the same chemistry fractions a second time. The new private `debit(Parcel)` subtracts the **exact already-admitted parcel** after receiver admission. Public `take()` retains its previous preview-and-debit behavior.

This removes redundant multiplication/division and potential BigInteger allocations from the finite-water transfer hot path, without changing volume, chemistry rounding, admission, failure atomicity or conservation rules. Account ownership remains the existing single-writer precondition.

## Reproducible isolated evidence

- Exact original `WaterAccount.java` Git blob: `d8512869d8e06740d57898381145ece344f4661b`.
- Exact optimized `WaterAccount.java` Git blob: `3e18f72b42f32569fd7fa2c7c3751af12508dee2`.
- Exact new registered test Git blob: `db42debbf97935e173fb1c3f5a26ae049c282642`.
- `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **150,000 registered assertions passed on each variant**, across 25,000 seeded extreme-chemistry scenarios. Explicit BigInteger oracle verifies source debit, accepted quantity, water/salt/sediment conservation, capacity and failed-admission atomicity.
- Additional independent randomized harness: **638,966 assertions passed per variant**, identical final digest `f6b6ecc4bc15de32`.
- Negative control: mutant debiting only the first split parcel failed at assertion 8 (`Exact source debit`).
- Controlled Java 21 allocation benchmark (five 12,000-transfer samples after warmup, source/destination setup included): original **25,440,000 bytes** versus candidate **16,992,000 bytes** (33.2% lower) in one isolated run. Timing was 9.461 ms versus 6.067 ms in that run; other runs vary. This is **not** an in-game tick, server, or Vulkan benchmark.

Evidence ZIP SHA-256: `00b20d85ebcf12cc585aba6eef273f293e0ba96f6d0ddfa9ffbe60281b0a0b03`. ZIP contains exact source variants, permanent/extended tests, negative mutant, runnable `run.sh`, logs, SHA256 manifest and benchmark.

## Build and acceptance blockers

The local environment has OpenJDK 21, but not Java 25, the complete private checkout, Gradle/NeoForge dependencies or working GitHub DNS for local Git clone. The GitHub connector permits direct source commits and blob verification but does not mount the full project into the local runtime. No full production compilation, registered whole-suite execution, client/server, native water save/reopen or Vulkan/LOD runtime test was performed. **No new playable JAR or JAR SHA-256 was produced.** Previous successful JARs were not deleted; no GitHub Actions were used.

Scoped pure-core behavior: tested. Whole-engine historical grade remains 5.0/10, not freshly regraded. Next: restore full Java25 production build and native conservation/save-reopen acceptance, followed by waterfall continuity and LOD/Vulkan performance.
