# Expanded replica — docs/validation/engine-continuation/2026-10-22-build-access-and-native-pressure.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-build-access-and-native-pressure.md) |
| Source identity | git: docs/validation/engine-continuation/2026-10-22-build-access-and-native-pressure.md |
| Preserved source SHA-256 | `2aa92233db2ed917d33b788cf21006515c96bc95af33b0b0814a5ca96233e50a` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Restoration of the non-Vulkan Gradle build through current-proxy tooling and an opt-in pinned NeoForm process-command fallback, followed by native reproduction of the 128-cell pressure cutoff. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the compatibility agent is private development tooling, and the fresh build and pressure reproduction do not qualify graphics or repair the long bend. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a build-environment manifest separating host workarounds from shipped modules and retaining negative pin-mismatch tests. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject unknown NeoForm bytes, verify the agent is absent from installers, and rerun the native 4,192-unit long bend with eventual rise and conservation after continuation changes. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Fresh build restoration and native pressure reproduction

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `2026-10-22-build-checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-build-checks.txt) |
| `2026-10-22-build-tool-checks.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-build-tool-checks.json) |
| `2026-10-22-development-archive-check.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-development-archive-check.json) |
| `2026-10-22-long-pressure-native.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/2026-10-22-long-pressure-native.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Fresh build restoration and native pressure reproduction

Date: 2026-10-22, evidence reviewed at 09:48 UTC. Prior continuation: `bdaedd0822641697e169b28a682e7e8f074b6191`; isolated lease parent: `23659ad3accea309896e372ae526428b61de2eb6`. Main remains delivered alpha.24 at `d021557dd9fc6a7e3d29d81a80b55a608fdbb3d6`.

## Changes since the prior checkpoint

Added optional development tooling to restore the blocked default Gradle build. `tools/build_with_current_proxy.py` reads the current invocation's HTTP(S) proxy environment for Gradle and its child JVMs instead of using an unreachable inherited proxy. It does not save transient ports, change repository configuration or support authenticated proxy URLs.

NeoForm 2.0.31 then failed because this container hides `ProcessHandle.current().info().command()`. The separately built, opt-in `tools/compat/ProcessCommandFallback.java` agent supplies the running JDK's executable only when that Optional is empty. It targets exactly two SHA-256-pinned NeoForm classes and one call in each; unknown bytes and unexpected replacement counts are rejected. Existing commands and unrelated classes are retained. It is compiled for Java 21 and used only in the private build environment, never shipped in the mod. The normal wrapper remains the ordinary build path. No vendor jar is rewritten.

Added opt-in `LongPressureChecks` under the existing validation source set. It checks a native short control, then a sealed 262-cell U-bend, explicitly invokes a search, waits 120 server ticks, checks quantity and writes an explicit reproduction record. It requires `DEEPPRIMAL_LONG_PRESSURE_CHECKS=true` and `DEEPPRIMAL_LONG_PRESSURE_RECORD`. It is excluded from release archives. No production mod/runtime/geography source changed in this continuation; all 370 recovered source inputs still match their recorded Git blob hashes. The prior page-admission fix is included in the compiled source but has no new visual acceptance here.

## Fresh checks

- Full default **non-Vulkan** Gradle build: **passed**, exit 0, `BUILD SUCCESSFUL in 2m 8s`, 20 actionable tasks. Main, graphics-settings, performance-engine, LOD and core compile; `verifyCoreBoundary`, `verifyModuleBoundaries` and the complete Gradle CoreChecks pass. This is the first fresh full mod build since restoration, superseding the previous Java-network blocker.
- CoreChecks include frozen V1/V2 checks and the nine-seed watershed suite: 5,846,893 centerline samples, 964 headwaters, 650 lakes, 4,292 connected reaches, 538,915 contained-bank checks and 18,000 saved-V2 checks. Individual fixture timings are diagnostic, not FPS or engine-performance guarantees. See [compact build log](2026-10-22-build-checks.txt).
- Private compatibility checks: **passed** for both pinned targets, retained commands, executable fallback, unknown-byte rejection, untouched unrelated classes and real NeoForm construction. Proxy parsing/rejection checks and Python compilation pass. See [tool checks](2026-10-22-build-tool-checks.json).
- Native Minecraft dedicated server: **ran and reproduced the defect** at 09:46 UTC. Short control: 144 units, raised surface, conserved. Long bend: first search visits exactly 128 cells; after 120 ticks, all 4,192 units remain, the high surface still has 16 units and the lower outlet remains empty. Explicit record: `LONG_PRESSURE_CUTOFF_NATIVE_REPRODUCED ... conserved=true solverAcceptance=false`. This corroborates the prior world-double reproduction; the solver acceptance still fails. See [native record and inspected shutdown](2026-10-22-long-pressure-native.txt).
- Native launch exit 0 alone was not used as evidence. Both explicit records, normal server shutdown and final all-dimensions save were inspected; no assertion/crash report was present. The offline loopback server logged nonfatal public-key DNS and LAN-pinger failures. The fixture forces 18 test chunks and releases them; it adds no production chunk forcing.
- Unreleased alpha.25-dev default JAR: 606,627 bytes; root and four nested archive CRCs pass; build-compatibility/validation classes absent. [Hash and scope](2026-10-22-development-archive-check.json). This is a development build, not an audited/released installer. Supplied alpha.24 bytes remain unchanged.

## Failures encountered and resolved

The first current-proxy build reached NeoForm but failed its missing process-command lookup; a second lookup in external Java-tool launch also failed. The pinned optional agent addresses both. The first native fixture compile used an obsolete `ChunkPos(BlockPos)` constructor and was corrected to explicit shifted chunk coordinates. Native launch then failed before Minecraft while auto-provisioning Java 21 for asset download; a private Java 21 installation and explicit Gradle toolchain paths resolved this. Final compile, asset download and server launch passed. Logs retain the failures locally; compact final evidence is staged here.

## Open gates and next work

Implement bounded persistent pressure/drain search continuation with fair scheduling, cancellation/invalidation on edits, safe unloaded boundaries and conserved transfers; raising 128 is insufficient. Re-run this native long-bend case with an eventual-rise acceptance condition, then edit/unload/save/reload and idle-budget cases. Current production solver remains unfixed.

Enabled-view page/fallback coverage, mixed transparency, teleport/edit/reload behavior and visual LOD isolation remain open. No fresh OpenGL/Vulkan captures or renderer/performance checks ran here; the optional Vulkan source/toolchain was not restored for this build and a display environment is unavailable. Earlier alpha.24 native graphics evidence remains separate from this fresh build/server evidence. Owner-GPU/FPS, independent 60 Hz simulation, regional finite/renewable water budgets, geology/terrain quality and full LOD acceptance are not established. V3 geography stays frozen; catalog/species work remains deferred. Acceptance is incomplete and continuation remains enabled.


<!-- END PRESERVED SOURCE -->
