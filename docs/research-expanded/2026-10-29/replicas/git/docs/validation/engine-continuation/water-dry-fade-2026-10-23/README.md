# Expanded replica — docs/validation/engine-continuation/water-dry-fade-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-dry-fade-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-dry-fade-2026-10-23/README.md |
| Preserved source SHA-256 | `cf07c9eae39b57c4a7fa0375fd2e2e5aabc4708d07768ba2d7a4d10294534e58` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Preserving a drying water page when a later coherent dry/dry worker mesh returns no vertices before the original fade deadline. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the controlled premature-disappearance repair does not solve every live-scene flicker or alter authoritative water transport. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a presentation-retirement contract retaining visible old geometry until its accepted transition finishes, independently of later empty simulation snapshots. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Upload wet, drying and empty snapshots at controlled offsets, assert resource lifetime through the original cleanup deadline and repeat refill and stale-result cases on both backends. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Water drying-fade continuity — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water drying-fade continuity — 2026-10-23

Owner request: make flowing water look seamless, without visual flickering. Continues `8f9d484f` on the unreleased working branch. This fixes one demonstrated premature-disappearance cause, not every source of scene flicker. Main/installers, authoritative water quantities/transport, engine core, LOD, Vulkan and frozen geography remain unchanged.

## Native reproduction and focused fix

The real WaterPatchMesher builds wet and drying meshes and production AnimatedWaterRenderer.upload creates actual GPU water buffers in an isolated renderer. A controlled presentation clock uploads a drying page at t=100 ms, with the existing cleanup deadline t=201 (100 ms fade plus 1 ms grace). Another coherent mesh job sees dry/dry snapshots at t=125 and returns no vertices. The baseline immediately closes the 46,080-byte GPU page, although its opacity transition is only 25 ms old. Minecraft asserts `Empty recapture removed water before its fade completed`, Gradle exits one, and sealed output/crash context are preserved.

The independent fix adds one guard to the zero-vertex upload path: an existing GPU mesh with a future cleanup deadline stays resident. It retains the same fade deadline, so repeated empty snapshots cannot restart/prolong it. The old newer-edit guard still wins; truly empty pages and completed fades keep normal cleanup behavior. No water is created/frozen, no solver step/rate changes, and this is not a depth-bias workaround.

Candidate native checks retain the same GPU object/payload at t=125,150,200, release it at t=201, and retarget a real rewet upload from half opacity at t=450 toward one. A newer pending edit still protects a page even beyond an old cleanup deadline. Isolated GPU/worker ownership is released afterward. The clock and snapshots are controlled; the page is uploaded but not drawn into the live lake image. This proves production transition/GPU lifetime, not frame-by-frame pixel continuity or perfect flow in a generated river. Alpha endpoint quantization and neighbor page/native/LOD handoffs remain separate issues to measure.

## Fresh checks

- 511 current source inputs Git-blob verified in an isolated directory; final comparison finds 509 unchanged, one production renderer plus ClientCapture changed, and one new opt-in validation fixture. Validation/private build helpers are excluded from the mod archive.
- Full sealed uncached Linux Vulkan/core/module/geography build passes all required records with 25 executed tasks. Existing water checks pass 10,000 continuity retargets, 40 shared-edge samples, surface-shape/vertical-page regressions and 500,000 conservative core transfers. These are core checks, not native scene measurements.
- Six outer/nested CRCs, 407 compiled-class comparisons and five nested/standalone module identities pass. The development JAR SHA is 018bf258631c3bdafdc8446fe0656f28f0715cc8b7f94a2686b37cf04c6dbea8, identical before/after final native sessions. No installer is released.
- Two final strict development/packaged software-Vulkan sessions pass the fade retention/deadline/rewet/edit tests plus current LOD index/eviction/retry/stale-edit/native-capture, frame/device/reload/normal-save/shutdown controls. Packaged origins point to actual JARs. Live console bytes equal producer-sealed hashes; accepted cases have zero new crash/save errors, final tracked GPU buffers/images, VUIDs and validation errors. The baseline crash is inherited evidence, not an accepted pass.

Lake screenshots were inspected as limited scene evidence. They still show repeated terraces/fine water patterns and cannot diagnose temporal flicker from a still image. No real-river video, per-frame coverage/readback sequence, owner AMD 8060S/FPS guarantee or OpenGL comparison was measured in this checkpoint. Software Vulkan remains the primary sample API. Prior marker/console writer, reload/atlas stalls and direct-stop save failures remain unresolved. Three solver substeps are not independent 60 Hz.

## Next acceptance

Capture a matched live-flow sequence with waves and native/animated/LOD passes isolated; measure geometry/opacity continuity across adjacent horizontal/vertical pages, dry/rewet updates, native section compilation, reload, travel and memory pressure. Establish a shared stable flow-animation timebase/UV motion only after tracing the actual displayed path, preserving quantity and player edits. Repair demonstrated page/native/LOD overlap or gaps rather than hiding them by disabling view distance or freezing physics. Keep spatial/paged LOD/coarser coverage, player-input/server physical unload/reopen, staged drain p95/waiter/residency, regional water budgets and geology open; catalog/species remains deferred.

Reproduce with isolated project/ and pinned toolchain/ beside these scripts. Run build_candidate.py and audit_vulkan_package.py, then run_vulkan.py fade-final and fade-final-packaged sequentially with DEEPPRIMAL_NATIVE_RUN_DIR pointing to disposable audit world data and DEEPPRIMAL_CAPTURE_ROOT to a private writable output root. Audit actual capture names with audit_water_fade.py (symlink external captures beside the script). Respect the lease. before-AnimatedWaterRenderer.java reproduces the required native baseline failure.


<!-- END PRESERVED SOURCE -->
