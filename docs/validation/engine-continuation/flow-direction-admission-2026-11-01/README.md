# Deep Primal dev147 — invalid water flow direction admission

Date: 2026-11-01 UTC. Scoped writer: `hourly-deep-primal-engine-dev147-flow-direction-admission`.
Audited previous run: `docs/engine-hourly-audits/2026-11-01T0630-dev146/REVIEW.md`.

## Defect and change

`WaterFlowTransition` accepted arbitrary signed integer direction IDs, including negative values and IDs above 16. Those values could become retained material endpoints or pending requests; in the near-water renderer, directions are packed into 5-bit GPU corner fields. Malformed data could therefore produce undefined visual directions or disrupt the versioned transition state.

Only IDs 0–15 (flow vectors) and 16 (still water) are valid. Invalid initial directions now use still-water ID 16. Invalid subsequent samples are ignored **before** updating the authoritative revision, so a corrupt high-version packet cannot block a later valid direction. The two-endpoint 100 ms transition and valid inputs are otherwise unchanged. This is presentation-only, not a fluid-volume or shader change.

## Reproduction and evidence

- Java 21.0.12.1 isolated source compile: `javac --release 21 -Xlint:all,-cast -Werror`; runtime `java -ea -Xverify:all`.
- The unmodified baseline fails on the first invalid initial direction: `AssertionError: invalid initial must fall back to still water: -2147483648 check=1`.
- Corrected source passes `WATER_FLOW_DIRECTION_ADMISSION_PASS checks=1000045 randomizedValidUpdates=200000 invalidCases=9`.
- Existing `WaterFlowBlendChecks` passes 272 pairs, including still/flow switching, stale revisions, queued endpoints, and initial blending.
- Regression is registered in `CoreChecks`. Run the isolated test using `bash docs/validation/engine-continuation/flow-direction-admission-2026-11-01/run.sh` from a complete checkout.
- Corrected production source SHA-256: `579f745955ed5fcf34de2a6c10a32264f1d302ac121da7d66405402462f8f661`.
- New regression source SHA-256: `caa9b30a03aefb817908aef01a8a7c048e22d94b0f7b43515991e9c902b38a81`.

## Limitations

These tests exercise exact core logic in isolation, not a full NeoForge client or GPU shader. This environment lacks a complete private repository checkout, Java 25, cached Gradle/NeoForge dependencies and a native GPU test target; Git CLI cannot resolve github.com. No playable JAR or JAR SHA-256 was produced, no GitHub Actions were run, and no older qualified JAR was deleted. Historical whole-engine grade remains 5.0/10 pending full validation.
