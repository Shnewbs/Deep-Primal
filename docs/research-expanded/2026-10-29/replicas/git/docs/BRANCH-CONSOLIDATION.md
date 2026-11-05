# Expanded replica — docs/BRANCH-CONSOLIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/BRANCH-CONSOLIDATION.md) |
| Source identity | git: docs/BRANCH-CONSOLIDATION.md |
| Preserved source SHA-256 | `835ba045ef96d8d9e0660a8f761eee8abcb11f318e05038f7fe3cdefc25d3d04` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

The alpha 25 merged installer bringing eight branch heads into ancestry, bundling GUI Themes alpha 8 and retaining Maritime as a separate incompatible 1.21.1 project. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that Linux Mesa native Journal checks do not establish Windows owner-GPU performance or make the Maritime directory loadable in the root installer. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a consolidation matrix separating ancestry inclusion, conflict decisions, nested-module presence and independently tested runtime environments. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Audit the assembled profile for duplicate Themes modules, repeat Journal navigation and GUI scaling, and verify Maritime remains outside the 26.1.2 runtime classpath. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Branch consolidation — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Branch consolidation — 2026-10-23

Version: **0.0.1-alpha.25-merged.1**, Minecraft **26.1.2**, NeoForge **26.1.2.122**, Java **25**.

All eight existing branch heads are included in the consolidation ancestry. The engine continuation is the source baseline; older Vulkan/waterways changes are retained through their newer implementations. GUI Themes alpha.8 is bundled into the root installer. The separate `maritime-1.21.1` directory is preserved verbatim from its branch, with its existing TFC dependency and Minecraft 1.21.1 build. Maritime is not loaded by the incompatible 26.1.2 installer.

## Inputs and conflict decisions

| Branch | Input head | Resolution |
| --- | --- | --- |
| `main` | `d021557dd9fc6a7e3d29d81a80b55a608fdbb3d6` | First parent; retain all existing work. |
| `audit/alpha24-recovery` | `d021557dd9fc6a7e3d29d81a80b55a608fdbb3d6` | Identical to main. |
| `codex/alpha14-recovery-20261020` | `a5af7b2684063c95c8a82bc2a306aefe824b26d4` | Already an ancestor of main. |
| `work/engine-after-alpha24` | `364385c5cd2db0943cf6580002ff4db1a13d0f3a` | Keep its full tree and evidence; apply completed R44 candidate source described below. |
| `feature/gui-themes` | `761d7b02a2ab65be9f6d0b32938c9f171331d348` | Preserve project/wrappers and update to the newer saved alpha.8 source. |
| `maritime-1.21.1` | `dae2e89aa21f6af28bbdd7dadda5e4d7e4279a22` | Preserve the complete independent project subtree. |
| `work/vulkan-alpha10-modules` | `eaa2d5c5b6ca9448ad06a8e13d6476f113447741` | Preserve newer engine versions of overlapping files. Module extraction, caches, descriptor reuse and lifetime fixes are present; do not restore superseded lifetime/configuration code. |
| `work/waterways-alpha11` | `9540f8f4edc640a7e77e0fdd7bc4bf8d4647ad0c` | Preserve newer versioned waterways implementations, frozen saved profiles, added surface-water flag and later routing/containment fixes. |

The older branches have 103 Vulkan and eight waterways changed paths already matching the engine tree, including expected deletions. Other overlaps were reviewed against their merge bases and newer implementations. Five Vulkan core moves did not change their ancestor contents. The otherwise clean WorldgenProbe merge would duplicate imports already present after formatting; retain the current file. Keep current module boundaries, validation fixes, configuration ranges and version metadata. No old branch is force-reset or deleted.

The GUI input is `DeepPrimal-Themes-26.1.2-0.1.0-alpha.8-source.zip` (saved version 4). It includes the Journal world-background correction, HUD position editor, corrected slot geometry and eight 3840×2160 menu images. Root Gradle includes and embeds the module. The standalone build remains usable. The original eight reference atlases and all newer menu images are retained unchanged.

The engine source includes the completed local R44 checkpoint, `water-cohort-2328`, with six staged production files and its accompanying checks. This carries the R43 retry recovery, direction index, corner packets and R44 removal of fully steady water pages from retirement polling. The candidate production files were copied intact; this merge does not change water quantity, player edits or saved V1/V2/V3 geography. R44 is included as an owner-requested experimental preview, not as engine/performance completion.

## Verification

- Full Windows-profile build passed: 31 tasks executed, including terrain, watershed, water, cache and module-boundary checks. The added retirement-state suite covers 60,000 cases; recovery covers 470,000; boundary index covers 688,129; both packet suites cover 210,000 each.
- Combined client loaded Deep Primal, graphics, performance, LOD, Themes and the Vulkan backend. All eight themes passed actual inventory Journal mouse-click, Back/Escape navigation, J shortcut and GUI scales 1/2. Forty actual game captures were produced. All eight Journal-tab captures were visually inspected: world and HUD remain visible behind native dimming.
- The live client used Linux Mesa software Vulkan with validation enabled. It saved and stopped normally, recorded zero Vulkan validation errors and released tracked buffers/images to zero. No client crash or Journal failure marker occurred. The headless machine has no audio device, so sound was disabled.
- The unqualified Gradle validation invocation also dispatched the standalone Themes client; both clients completed successfully. Their overlap is explicitly not a performance benchmark. Use `:runClient` to repeat only the integrated client.
- Windows archive and all six nested archives passed CRC checks. All 459 compiled project classes match current output. JarJar inventory matches metadata, private validation code is absent, all eight menu images are 4K, and the Vulkan module contains Windows native libraries without Linux `.so` files. See `validation/branch-consolidation/archive-audit.json`.

The supplied Windows JAR is 124,855,842 bytes. Its size includes all eight original 4K menu scenes and reference artwork. Windows-native execution and the owner's AMD GPU remain unverified. Water page bands, active-work latency, normal cross-frame publication, sustained streams/oceans and distant LOD acceptance remain open. Historical continuation documents record their then-current staged status; they do not establish completion of this experimental consolidated build.

## Install

Replace the previous Deep Primal JAR with `DeepPrimal-26.1.2-0.0.1-alpha.25-merged.1-vulkan-windows.jar`. Remove any separate Deep Primal Themes JAR or duplicate extracted modules. This is the Windows Vulkan-capable bundle; select Vulkan in Graphics API and restart if it was not already selected. Existing theme and backend preferences remain in their normal config files. Maritime requires its independent Minecraft 1.21.1 environment.

## Repeat checks

```sh
./gradlew build -PvulkanPlatform=windows
DEEPPRIMAL_JOURNAL_VALIDATION=1 ./gradlew :runClient -PvulkanPlatform=linux -PgraphicsBackend=vulkan -PvulkanValidation -PthemeValidation
```

The second command creates a disposable flat world and runs the integration harness. Do not use it in a normal play instance.


<!-- END PRESERVED SOURCE -->
