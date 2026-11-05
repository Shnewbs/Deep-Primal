# Expanded replica — docs/validation/engine-continuation/loaded-write-admission-2026-10-27/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/loaded-write-admission-2026-10-27/README.md) |
| Source identity | git: docs/validation/engine-continuation/loaded-write-admission-2026-10-27/README.md |
| Preserved source SHA-256 | `57c83dd0f7fcd5826f01f07f117880c45cdcf9821a57fcfb44cecfbd341f7c24` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev34 conservative water-write admission and isolated compilation of Git-verified WaterTransport with transfer admission regressions. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the local TerrainNoise signature stand-in and dependency-free checks do not establish real loaded-boundary callbacks or rollback behavior. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a native-write preflight and commit contract that names source and receiver ownership, required resident state and refusal semantics. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject unloaded recipients and rejected native writes, assert exact water, salt and sediment totals, then verify save/reopen against the owner-built multi-module artifact. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Deep Primal dev34 — conservative water-write admission (2026-10-27)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-27T1734-83c099f/REVIEW.md) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
