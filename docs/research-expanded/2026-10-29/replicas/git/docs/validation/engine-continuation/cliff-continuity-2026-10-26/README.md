# Expanded replica — docs/validation/engine-continuation/cliff-continuity-2026-10-26/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/cliff-continuity-2026-10-26/README.md) |
| Source identity | git: docs/validation/engine-continuation/cliff-continuity-2026-10-26/README.md |
| Preserved source SHA-256 | `1833d4df4d2168d14ec7b3b3d1a25e7a21605cbec75e24efc2be5f510688e956` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Two cliff-water presentation repairs: stacked walls meet the next cell base and connected stepped corners publish a common physical-layer trajectory. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the three corrected captures and numerical checks cover reproduced discontinuities, while natural cliffs, multi-layer junctions and temporal smoothness remain open. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a geometry-and-history identity contract connecting animated and fallback meshes, previous positions and per-frame shared corner publication. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Capture moving sequences through tributary junctions, wet/dry fronts and page borders, asserting watertight geometry and unchanged material/opacity while measuring frame-time tails separately. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Cliff water continuity — dev.28-water.2

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `cliff-moving.png` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/cliff-continuity-2026-10-26/cliff-moving.png) |
| `vulkan-live-cliff.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/cliff-continuity-2026-10-26/vulkan-live-cliff.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Cliff water continuity — dev.28-water.2

The owner reported choppy, flickering water down mountain cliffs. Two independently reproduced presentation defects are repaired.

A lower neighboring tributary could pull the upper edge of a stacked waterfall wall below the next cell, leaving an opening. Stacked walls now meet the next cell at its base in both the animated mesh and the chunk-buffer fallback, for current and previous positions.

Stepped surface vertices also used different vertical-layer histories and upload clocks. The renderer now keys connected corners to the same physical height layer and publishes their shared trajectory each visible frame. An index limits this work to border and stepped quads; material and opacity bytes retain their existing behavior. Authoritative water quantities are unchanged.

## Evidence

| Check | Before | Final result |
|---|---|---|
| Mixed-height waterfall walls | Wall top 0.921875 versus next wall bottom 1 | 12,288 moving vertices; 256 volume pairs; both smoothing modes; signed vertical sections; 13 interpolation frames |
| Actual native GPU stepped corners | 0.12402344-block gap with 50ms upload skew | OpenGL and Vulkan each: 12 scenarios, 312 readbacks, 468 comparisons, maximum gap 0 |
| Live integrated Vulkan cliff fixture | No original natural-scene capture available | 323 frames, 158,593 shared-corner comparisons, 0 above-tolerance mismatches; maximum floating-point gap 0.000061035156 blocks |
| Packaging | — | Seven valid archives, six modules, two Windows DLLs, 588 product classes match compiler output; private fixtures excluded |

The live fixture contains 252 client-only water cells across eight pages, a tall waterfall and a stepped cascade, with repeated height changes. Its per-frame measurement reads the CPU upload mirrors using production shader interpolation; it is not a live GPU readback or a pixel-difference test. Independent controlled-clock checks above do read actual GPU buffers. The live fixture reports zero Vulkan validation errors and 63,072 resident water bytes. Height publication p95 was 0.235ms in this fixture; this is not an FPS or whole-frame performance benchmark. See [moving scene](cliff-moving.png) and [live receipt](vulkan-live-cliff.json).

The first live run passed numerical checks but missed two screenshots because the output directory did not exist. Capture setup now creates the directory and requires all three captures. The corrected run passed; the initial failure is retained in scene-capture-retry.txt.

Full build/core/resource/module checks and Vulkan validation completed successfully: 35 tasks (29 executed, 6 up-to-date), 82 PASS records including native fixtures. The final two-line chunk-buffer parity change and private probe corrections were subsequently recompiled through normal-version assembly/module checks and native runs. Final OpenGL validation and the corrected live Vulkan scene both exited 0. No core implementation changed after the full suite. Existing fade, rewet, failure-recovery, expiry and waterfall GPU checks also pass.

## Reproduction and identity

Use Java25, Gradle9.2.1, Minecraft26.1.2 and NeoForge26.1.2.122. Hardware used was Windows with AMD Radeon8060S. Build with the repository's normal version in gradle.properties. The version override in preview-version.gradle records the earlier build setup; the final assembly uses the normal version.

- Full validation/build entry points: :build, :visualValidationClasses, :verifyModuleBoundaries, :vulkanWindowsJar.
- Native controlled upload checks are invoked by :runClient with -PvisualValidation and -PgroundMaterialClient=OpenGL (or Vulkan), plus isolated -PclientRunDir and -PcaptureDir directories.
- The live cliff uses -PanimatedWaterValidation, a disposable quick-play world, and the retained live-scene.gradle init script. Do not run the fixture in a valued save: it intentionally creates a temporary client platform and water patch.
- This host used a verified local Minecraft asset/artifact cache, offline Gradle, and command-line javac to avoid the host's unavailable compiler-worker IPC. Asset setup tasks were skipped only after cached inputs were verified; build assertions were not excluded.
- Run python docs/validation/engine-continuation/cliff-continuity-2026-10-26/verify_package.py after assembly.

Source inputs were rechecked against dev.28 plus the publication parent: 1,635 tracked build inputs, eight intentional edits, two new private fixtures, no missing or unexpected differences. Later main changes were documentation, staged accounting evidence and art candidates only. The staged precipitation-admission fix is not promoted by this build. See source-identity.json and package.json.

Artifact: DeepPrimal-26.1.2-0.0.1-alpha.29-dev.28-water.2-vulkan-windows.jar, 125,292,084 bytes.
SHA256: 012ede8a138058b7a1fda66dd9c0fcd3bba414159322b98c6757feada5052ed3.
The exported Windows JAR matches the completed producer after all native checks. Themes remains 0.1.0-alpha.8. Hosted transfer failed before upload because service discovery returned DNS; the verified local export is available. No GitHub Release or CurseForge publication is claimed.

## Remaining acceptance

This closes the two reproduced geometric discontinuities, not all cliff-water flicker. Natural mountain scenes, wet/dry fronts, overhangs, complex multi-layer junctions, side texture/advection continuity, long camera travel and frame-time tails remain acceptance priorities. Still images cannot demonstrate temporal smoothness. Full world conservation, loaded/unloaded persistence and multiplayer soak also remain open.

The existing builder and independent auditor remain the continuation path. The latest independent audit was consumed without changing its inherited 5.0/10 assessment; this is not a new grade or water-engine completion claim.


<!-- END PRESERVED SOURCE -->
