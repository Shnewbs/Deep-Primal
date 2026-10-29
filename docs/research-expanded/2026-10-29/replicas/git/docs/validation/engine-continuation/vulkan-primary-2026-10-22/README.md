# Expanded replica — docs/validation/engine-continuation/vulkan-primary-2026-10-22/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/vulkan-primary-2026-10-22/README.md) |
| Source identity | git: docs/validation/engine-continuation/vulkan-primary-2026-10-22/README.md |
| Preserved source SHA-256 | `7defe33f36dd3587f87fd129c0a03b619299f7123768d30ff6d6ce585e4d6ae7` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Vulkan-first reference validation and correction of documentation that a lean default build omits Vulkan while explicit platform or universal profiles include it. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that software-driver sessions establish scoped functionality rather than owner-GPU FPS or complete visual coverage. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a renderer capability matrix separating user preference, bundled native payload, actual backend, recovery state and hardware-qualified results. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Audit lean and Vulkan profiles independently, require explicit backend observations and final resource counts, and repeat water/LOD coverage stress on owner hardware before performance claims. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Vulkan primary reference validation — 2026-10-22

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Vulkan primary reference validation — 2026-10-22

The owner's primary sample/reference API is Vulkan. OpenGL remains a modular
fallback and saved preference. Corrected stale adapter documentation: the lean
default build does not bundle Vulkan; use an explicit platform/universal profile.
No production renderer/geography change is included. The accepted water cleanup
is commit 900ebeb; its separate report records 13 final native passes and scope.

New validation-only commands isolate animated/fallback/waves and LOD, set the
recorded lake camera, and persist actual water/LOD observations. They do not assert
complete visual coverage. The private bounded runner sequences requests, waits
for screenshot callbacks, records inherited crash hashes and stops after 600s.
Tests/host tools remain excluded from mod payload. No installer released.

Restored all 124 optional module inputs with exact Git blob matches, including
LGPL license/origin files. The uncached Vulkan Linux build passes core/frozen
geography/module checks. Six archive CRCs and 406 compiled class-byte comparisons
(181 Vulkan + 225 other) pass; Linux natives and license/origin bytes are checked,
validation/private agent payload is absent. Final JAR hash remains
e22173a1c52792cb255a01441210347e01e8a6d9c5a69c28e403462da77ab567.

Four strict software-Vulkan sessions pass: real Create World/join with V3,
explicit GPU transfer/shader/depth/stencil checks, clean saved-world reopen, and
the complete enabled-view/reload sequence. All require actual Vulkan, explicit
records, normal completion, zero VUIDs/validation errors, and zero final tracked
buffers/images. Every shutdown injects a rejected completion and proves the
following completion still executes. The initial reporter listed inherited crash
files in reused directories; the strict audit distinguishes the known failed
OpenGL attempt, and final/smoke prelaunch hashes confirm no new/changed crash.
Shader-output-not-consumed performance warnings remain (2/2/7/4 per session).
Headless OpenAL device warnings are environmental; sound was not validated.

The final sequence has all 19 actions acknowledged. It starts without a recovery
marker or forced retry and ends with no marker. LOD is actually active/drawing,
not just enabled in configuration: captured chunks 46 -> 69 -> 113 and visible
chunks 2 -> 2 -> 7 across animated/fallback/reloaded observations. Animated water
payload is 837,216 bytes, fallback releases to zero, and reload restores 749,664
bytes. Water pending work 22 -> 0 -> 13 is not a settled-world performance result.
Drawn-page/LOD-triangle counters are cumulative. No macro horizon was active.

All five final captures, the creation form and the early reopen screenshot were visually inspected. The early reopen image shows partial water and exposed terrain while delivery/residency are still warming; it is a concrete transient coverage observation, not proof of a persistent hole or its cause. A functional reopen pass does not accept that frame as complete coverage. All five warmed final captures were visually inspected. They show the lake and shores,
water surface in animated and native fallback modes, and terrain beyond the near
view when LOD is enabled. The no-LOD image is diagnostic only. Repeated terrain
terraces and tiled underwater/surface patterns remain visible quality questions;
their cause is not proved. Different transparency and changing LOD residency are
visible. The world continued simulating and LOD continued warming between images:
these are camera-matched isolation captures, not identical-state/output or FPS
comparisons. They do not close mixed transparency or memory-pressure/teleport/edit
coverage acceptance. Owner hardware and independent 60 Hz acceptance remain open.

Failures retained: first device probe returned VK_ERROR_INCOMPATIBLE_DRIVER before
Mesa restoration; Xvfb failed Unix-socket and then xkbcomp-path setup. Package
checksum checking caught a truncated Mesa download and redownloaded it correctly.
These host problems were corrected privately. A recovery marker after the first
session caused one OpenGL reopen, correctly rejected by its backend assertion.
Root cause is unproven; two later no-retry opens succeed. One scene capture ceased
advancing after waves and was stopped normally; its console lacks final build
completion and it is rejected even though exit=0 and shutdown record exists.
The untouched-output final repeat completes all actions and strict shutdown gates.
Do not count that incomplete capture as a pass or infer a fixed runtime defect.

Reproduction layout: runner/auditors beside project/ and toolchain/. Restore the
existing pinned Java/Gradle tools, optional module and its dependencies. Private
graphics packages are from Ubuntu snapshot 20260828T000000Z; exact package hashes
are in graphics-restoration.json. Extract .debs under toolchain/graphics-verified,
set the recorded loader/ICD/layer paths, and retain the original Xvfb. The private
Xvfb copy changes its sole /usr/bin compiler-directory string to /tmp/dpv, a
non-overwriting symlink to extracted usr/bin; private-xvfb-path.json pins both
hashes. Use a free alias or ordinary installed Xvfb elsewhere. TCP display is local
to the isolated launch namespace; use normal installed drivers on an owner host.
Run run_vulkan.py create, then view-final and smoke sequentially; inspect all
assertions and screenshots after producer exit. Exact staged paths are recorded
in command.json locally. Avoid reading or editing live output files during a producer; the completed untouched-output repeat is the accepted capture. The normal documented Gradle commands need no host shim.

Next: Vulkan-first matched-state water/LOD coverage stress, including constrained
budgets, teleports, player edits and physical reload, with settled asynchronous
residency and separate transparency passes. Add owner GPU checks before hardware
claims. Retained-drain p95/large-region continuation, regional water ledgers and
coherent geology remain unfinished; catalog/species stays deferred. Published
V1/V2/V3 geography is unchanged. Acceptance and ongoing work remain open.


<!-- END PRESERVED SOURCE -->
