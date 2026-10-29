# Dev87 — V8-r6 native foliage support admission (source-only)

## Provenance and scope

The Dev86 independent audit at `docs/engine-hourly-audits/2026-10-29T2030-3df845f0/REVIEW.md` (pinned `3df845f0b26511ce676d006ced01969e3aa00699`) passed the planner but did not run the native foliage materializer. A later Git review at `13e7cf743bd0471b2579d4b05cc661cff78e62aa` tightened the earlier mutation-runner classification without finding a new gameplay defect.

This is a defense-in-depth implementation for explicitly selected **V8-r6**. New `V8VegetationNativeAdmission` checks actual ground against saved preflight material, a sturdy dry upper support face, and an air/dry destination immediately before each planned short-grass/fern write. A failing candidate is skipped, never relocated. `DensityChunkMaterializer` consumes the admission inside its existing Dev53 complete prewrite rollback and final heightmap priming, after raw and topsoil writes. The V8-r1..r5, V1–V7, saved policy, seed, hydrology and approved textures are unchanged. No water amount or constituent is created.

## Evidence actually executed

Linux OpenJDK 21.0.12.1, strict `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`. The *exact new production guard source* compiled against isolated Minecraft API doubles. Two positive JVMs per runner mode (normal Python, Python `-OO`) passed, each `V8_NATIVE_PLANT_SUPPORT_PASS assertions=25409`: **101,636 positive assertions across four processes**. Five independently compiled broken controls per runner were rejected by an AssertionError from the intended test fixture (10/10). Cases covered signed world coordinates, equivalent ground state, callback-modified ground, non-sturdy/wet footing, occupied/wet destination and no writes from the admission helper.

One initial fixture assertion incorrectly required a native read for an already-invalid air support. The fixture assertion was corrected before final successful runs; the failed attempt is excluded. An archived Dev86 original vegetation planner fixture replayed once separately: 162,732 assertions and four compiled broken controls rejected. The later Git-review mutation-runner changes for Dev86 were not replayed in that archived copy.

## Limits and next gates

The modified full native `DensityChunkMaterializer` and Mojang/NeoForge APIs were **not compiled or executed** in the isolated source harness. This is not demonstrated Minecraft plant survival, block/query/heightmap parity, saved-profile reopen, old/new border recovery or callback/rollback acceptance. Long-term native survival after further external block updates, GPU visual transects, Java25/Gradle9.2.1 full build and finite-water conservation remain open. The owner retains complete builds and JAR publishing. No new playable JAR or grade is claimed; accepted overall baseline stays **5.0/10**, requiring fresh independent **>8.5** and water gates.

From a complete checkout, run `python3 tools/test_v8_vegetation_native_admission.py` and `python3 -OO tools/test_v8_vegetation_native_admission.py`, plus the separate existing `tools/test_v8_vegetation_columns.py`.
