# Deep Primal dev34 — conservative water-write admission (2026-10-27)

**Builder-only checkpoint; not a release.** Source parent before builder lease: `a5b5f0c7bf3a6b2774d47721c9a8b6ccb71253f0`. Source implementation through `fd2afc5a6f256441b9723a9375e0a8467ada30a9`; candidate version `0.0.1-alpha.29-dev.34-conservativewrites.1`. A separate renderer schedule pushed disjoint LOD code/docs during this window; no renderer/water renderer/theme files were overwritten. Writer lease is released at the end of this cycle.

## Independent audit consumed

[2026-10-27T1734-83c099f/REVIEW.md](../../engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md) pinned `83c099fd455c0154f511e2d2f67f8582822a83fa`. Carried original strict grade **50/100 (5.0/10)**, not a new regrade. Native unloaded-border conservation/save-reopen remains unaccepted. This repair hardens *actual* loaded-only read/write entry points and transfer accounting ahead of native validation, rather than promoting staged R57/R60/R61/R62 or contact/index candidates.

## Implemented directly in production

- `HydrologyRuntime.write` rejects out-of-world and unloaded positions **before** its first block-state read or setBlock. Public `pickup`, `infiltrate`, `oceanSink`, `restoreOcean`, `meltIce` and two-cell source-fill descents receive corresponding admission guards.
- `WaterRuntime.transfer` rejects unloaded/unbounded donors and receivers before reads; the new `WaterTransport.matches` verifies a proposed exchange against the actual preimage volumes, without creating water or touching chemistry. Failed destination writes must restore the donor; an unsuccessful rollback throws a conservation-integrity exception instead of silently proceeding with divergent persistent accounting.
- Added exhaustive native-unit/pressure admission matrix to full `CoreChecks` and bumped candidate version beyond dev33. No geographic revision, generated block/art, FSR/DLSS/LOD code, saved profile format or climate/hydrology balance was intentionally changed.

## Checks and limitations

- Temurin **Java 25.0.5+8** `java` and `javac` verified. Isolated `javac` compiled the **Git-blob-identical** production `WaterTransport.java` (blob `0ede1c7312e40ecf063b6cc86e5a690cce190443`) and the **Git-blob-identical** `WaterTransferAdmissionChecks.java` (blob `b8ddfee31d3adb39d52489d6c123d5f2d9861b75`). A local stand-in supplied only the `TerrainNoise.lerp` signature so the source compiles without unavailable platform dependencies.
- `WATER_TRANSFER_ADMISSION_PASS assertions=5836 accepted=1520 zeroUnitsCreated=true` on Java 25. Static checks confirm early world/chunk admission in eight production read/write paths plus matching exchange admission and rollback hard failure: **10/10**. Static checks are **not** native behavior tests.
- Full repository checkout/Gradle wrapper and dependency cache are unavailable in this isolated Linux runtime; `git ls-remote` failed DNS resolution of github.com (exit 128). No full NeoForge/Gradle compile, core aggregate, packaged native / persistent unload-save-reopen, Windows AMD8060S, real OpenGL/Vulkan cliff-pixel test or multiplayer long-soak test was completed. No JAR was built or qualified and no development release or CurseForge file is claimed.
- The scoped Java25 test is not proof that the changed `HydrologyRuntime` or `WaterRuntime` compile against NeoForge, and not proof of atomic host `setBlock` event behavior. The hard rollback exception protects diagnosis but can surface an engine fault under extreme external interference. Native failure injection and persistence conservation tests are required before release.

## Next builder action

Restore a **complete current GitHub checkout** and Gradle cache with the documented pinned tooling helper, run Java25 full multi-module build and server water native/water ledger/reopen. Verify loaded/unloaded boundary and rollback-failure injection in actual host callbacks, then perform packaged save/reopen/soak and GPU cliff continuity. Integrate already-staged chemistry contact/index only with its separate full gates. Maintain all frozen V1–V7 geography and exact constituent totals. Independent external score stays 50/100; water acceptance and strict >8.5 objective remain open.
