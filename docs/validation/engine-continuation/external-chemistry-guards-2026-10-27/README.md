# Dev36 — external ocean chemistry invalidation and short-circuit loaded guards

**Date:** October 27, 2026. **Production-code commit:** \`dccc7bc2cd40d4fd298723a183079cbf29d49f41\`.
**Version:** \`0.0.1-alpha.29-dev.36-chemistryadmission.1\` (source-only, not a qualified JAR).

## Implemented

\`HydrologyRuntime.externalChange\` now removes a stale water composition record if an **external** write changes \`EngineWaterBlock.OCEAN\` even while keeping the same number of units. Previously, a river parcel could persist under a new ocean marker (or the reverse), bypassing the default salt identity selected by \`contents\`. A changed flow direction alone does not clear chemistry. Internal engine writes keep their own transactional ledger. No water-unit mutation, worldgen profile or terrain revision is introduced.

The independent auditor's October 27 19:33 UTC negative control detected a blind spot in \`tools/validate_hydrology_height_admission.py\`: merely finding both guard names allowed replacing \`||\` with \`&&\` and exposing unloaded/out-of-world reads. The updated validator enforces the short-circuit operator **before** each first block read in \`place\`, \`exposedColumnCell\`, and \`waterColumnBed\`, with four mutation controls. Source: \`docs/engine-hourly-audits/2026-10-27T1933-a765774/REVIEW.md\`.

## Focused local checks

- Isolated Linux Eclipse Temurin JDK **25.0.5+8**: actual \`boundary\`/\`externalChange\` Java method text transcribed from the reviewed production method and compiled into a JVM stand-in with fake state, position and ledger types; **9/9 cases PASS**, including two direction changes, count changes, flow-only, nested engine writes and empty save entries. A marker-guard removal was correctly rejected.
- Locally reproduced native-boundary method fragments and Python source guard checker: **16/16 checks**, **4/4 site-specific mutants rejected**; an additional bad \`&&\` source mutant was rejected. The committed scripts use the full checked-out production file; owner should rerun them against the **exact committed repo** to elevate from local stand-in checks.
- No full Java25 Gradle/NeoForge build, native Minecraft/packaged smoke, JAR, CRC, save/reopen, cross-chunk rollback injection, actual Vulkan/OpenGL or AMD8060S performance was executed. The owner handles full builds under the October 27 override.

**Next:** run the new source-bound scripts, the owner's full Java25 build and actual native external-ocean marker save/reopen ledger; then unloaded-boundary and write-rollback tests, sustained conservation and real GPU waterfall continuity. Preserve R57/R60/R61/R62 staged and V1-V7 geography. Independent complete grade carried **50/100, 5.0/10, Tier 2**; no regrade or release was claimed.
