# Deep Primal dev151 — finite water presentation arithmetic

Date: 2026-11-01 UTC. Owner: hourly Deep Primal engineering.
Consumed independent audit: `docs/engine-hourly-audits/2026-11-01T1130-dev150/REVIEW.md`.
Baseline main before scoped claim: `ff2f92fa00cae01e402186f1f18482a244352b48`.
Claim: `CLAIM.json`. No overlap with the auditor's dev150 `WaterSimulation` work; prior WaterDynamics waterline claim was released.

## Reproduced defect

The original production `WaterDynamics.mix(0x2878af, 0xffffff, NaN)` produced black `0x000000` because `Math.round(NaN)` narrows each RGB channel to zero. An invalid climate temperature propagated this black result through `palette`. `WaterDynamics.wave(0,0,NaN,.5)` produced `NaN`, which could reach shader-facing displacement data. Very large finite coordinates could overflow intermediate phase sums.

## Repair

- `mix`: return the existing RGB color for nonfinite blend factors rather than rounding NaN channels to black.
- `palette`: return the native RGB color for invalid temperature or rainfall, including infinities that would otherwise clamp to a misleading finite tint.
- `wave`: return zero displacement for nonfinite inputs or nonfinite computed displacement.
- Preserve original finite blend, climate palette, and ordinary wave arithmetic bit-for-bit. No water quantity, save format, flow law, terrain geography or shader implementation changed.

Production source Git blob: `364a5e25495ffa8b9e0bbf672106cb118ff9ef0f`.
Production SHA-256: `928b97fd0343bc9ca2f9e7c528be1bfddd858d7686cd4866e097c160b859b373`.
New regression Git blob: `171013456d4620b1ec5dff0136563d9b27d8a072`.
Regression SHA-256: `159b74bfce76b15597d13cfa745a767d567cae601df6bdb172503fea411367aa`.
Reproducible runner blob: `a79f8ff5b3f91d10a86a02c7bb6afb29b77fbdc2`.
Registered in `src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java`.

## Executed isolated checks

OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`; `java -ea -Xverify:all`. Exact production source, new regression and the existing unchanged waterline admission regression compiled without stubs or external libraries.

- `WATER_PRESENTATION_FINITE_PASS checks=700028 blendParity=300000 paletteParity=200000 waveParity=100000 invalidCases=24`
- `WATERLINE_ADMISSION_PASS checks=826871 invalidWaterlines=7 finiteParity=275619`
- Original production and four deliberately broken variants (no mix guard, no palette guard, no wave-input guard, no overflow-result guard) all fail the new regression, exit code 1.
- Re-run from the full checkout: `bash docs/validation/engine-continuation/water-presentation-finite-2026-11-01/run.sh`.

Local evidence ZIP: `DeepPrimal_dev151_water_presentation_evidence.zip`, 7,824 bytes, SHA-256 `bbcff0342815eb20754b8e3a382bb141dd71cb226da155f105cebff0814afe2a`. ZIP integrity verified; contains source, test, runner, logs, and manifest.

## Unpassed acceptance gates

No complete private checkout, Java25, Gradle wrapper/cache or NeoForge dependencies in this execution environment. `git ls-remote` failed DNS resolution for github.com; a local Gradle invocation exited 127 because the full checkout is absent. Full `CoreChecks`, NeoForge build, native client/server, actual GPU readbacks, cliff-water flicker, save/reopen, Vulkan/LOD benchmarks, and 26.3/26.4 compatibility are **not tested**. No playable replacement JAR or JAR SHA-256 was produced. Previous qualified JARs remain untouched. No GitHub Actions were used.

This is a bounded presentation-input safety fix, not proof that the reported water flicker or the overall engine is complete. Historical whole-engine grade remains 5.0/10 pending a fresh comprehensive audit.
