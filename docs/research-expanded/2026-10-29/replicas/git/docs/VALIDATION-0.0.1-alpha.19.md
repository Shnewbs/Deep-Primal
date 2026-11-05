# Expanded replica — docs/VALIDATION-0.0.1-alpha.19.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.19.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.19.md |
| Preserved source SHA-256 | `71ca5bfe7849946130289901d77b172f009fbb2b7be22637280a79bbfaafcf14` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal 0.0.1-alpha.19 — modular graphics/LOD and connected water”. Its original opening states: “Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Based on 6f5a4677ead15e36927f6c7dfb1cd4a6a56b486d, preserving the environmental planning updates. Source, build and native checks ran on 2026-10-21.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `LodAccess`.

## Research expansion — gaps and details to deepen

- Carry the renderer result from bounded CPU/model behavior to actual GPU lifetime, submission, presentation, and target-device evidence.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Use separate ledgers for queued, staged, resident, retired, and reusable bytes, and identify the completion signal permitting reuse.
- Name pre-draw preparation, command encoding, API submission, GPU execution, and presentation intervals separately.

## Research expansion — acceptance checks

- Inject failure before allocation, after reservation, during upload, and before publish; verify accounting and in-flight resource validity.
- Run matched native scenes with travel, stationary recovery, minimize/resize, world switch, and device recreation before claiming frame-time or VRAM gains.

## Research expansion — source anchors

- Deep Primal 0.0.1-alpha.19 — modular graphics/LOD and connected water
- Resulting behavior
- Artifacts and limits

Related design: [render performance](../../../chapters/render-performance.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.19/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.19/installer-audit.json) |
| `validation/0.0.1-alpha.19/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.19/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal 0.0.1-alpha.19 — modular graphics/LOD and connected water

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Based on `6f5a4677ead15e36927f6c7dfb1cd4a6a56b486d`, preserving the environmental planning updates. Source, build and native checks ran on 2026-10-21.

## Resulting behavior

Video Settings directly opens Deep Primal Graphics and returns to the actual Options parent on Done/Escape. Graphics owns the screen, option/config contracts, renderer policy, shared GPU resources and backend preference. The separate `lod-engine` project owns capture, material sampling, streaming/cache, edit invalidation, rendering and optional world identity networking. Root consumers use `LodAccess` and run with the feature omitted. See [module contracts](MODULES.md).

The screen exposes all 30 controls represented by the native video menu, plus Deep Primal's controls and additional visual/accessibility preferences. Display modes, fullscreen, GUI scale, quality presets, filtering and mipmaps remain accessible. Scoped preset previews populate the staged model without changing live options; explicit overrides survive Apply. FPS, entity-distance and fade values obey their native increments rather than silently normalizing typed values. Texture/display changes receive native reload/update actions. Known renderer replacements retain ownership; the native-entry handoff was also checked with Deep Primal renderer ownership disabled.

Independent 16-unit fluids now equalize connected free surfaces through submerged passages. Gravity/lateral movement run first. Pressure searches at most 128 connected wet cells per attempt and 4,096 visits per level/tick, alongside the existing 2,048-fluid-tick bound. Both volumes are written before loaded-neighbor notification; failed destination writes restore the source. No PhysEx dependency or source is included. See [reference review and gaps](PHYSEX-REFERENCE-2026-10-21.md).

## Executed checks

| Check | Evidence |
| --- | --- |
| Build and boundaries | `build compileVisualValidationJava` passes, including core, platform/module boundaries and optional Vulkan cache/descriptor checks. Root source is forbidden from importing the optional LOD implementation. |
| Core liquids | 4,913 legal volume/elevation cases conserve units/capacity and reduce head without overshoot; extreme-height arithmetic stays bounded. Existing terrain, queue, mesh, hydrology and cache regressions pass. |
| Native pressure | A dedicated-server U-bend starts with 144 units, raises the lower free surface, and retains all 144 units. A blocked passage prevents exchange. Ticking an absent distant chunk does not request it. |
| Menu and persistence | Direct Options entry, programmatic entry, exact parent return, Apply/Revert, small-window resize, reopen and full-process restart pass. Preset staging, explicit overrides, native texture reload and invalid native increments are exercised. |
| Omitted LOD | Packaged `-PwithoutLod` client passes the same menu/preset/step checks with `LodAccess.available() == false`; it contains no LOD runtime classes. Shared pure algorithms remain in core. |
| Renderer handoff | Packaged client with renderer ownership disabled retains native Video Settings. This is a handoff check, not a full Sodium/Embeddium compatibility matrix. |
| Packaged creation | Actual Create World form, creation, joining our generator and clean save/stop pass. Form preparation was about 919 ms on this host. |
| Packaged OpenGL LOD | Continuous invalidation progresses; a player edit replaces the captured revision. The player build remains rendered after its full native chunk unloads: 74 visible LOD chunks, about 14.2 MB resident payload at the checked point. |
| Packaged Vulkan LOD | Restarted settings, edit propagation, unloaded-source rendering and disk snapshot restoration pass. Validation is active: zero validation errors, zero outstanding shader results, zero tracked buffers/images at shutdown. Three unused shader-output performance warnings remain. |
| Installer audit | Default, no-LOD and Windows profiles contain exactly their declared nested modules, no duplicate classes, no standalone/validation classes and no root references to the optional LOD implementation. All root classes and the shared graphics JAR match across these profiles. Windows bundles only Windows native libraries. |

Native rendering used Mesa llvmpipe/lavapipe in a Linux container, not the owner's physical GPU. Windows native libraries were packaged/audited; Windows runtime execution was not performed. These tests do not establish higher FPS than Sodium, Distant Horizons or PhysEx. The last input-increment correction was tested in the packaged OpenGL/no-LOD menu and repackaged into both installers; LOD/GPU implementation code was unchanged after its two-backend checks.

## Artifacts and limits

Exact sizes, SHA-256 values and nested-module inventories are in [installer-audit.json](validation/0.0.1-alpha.19/installer-audit.json). Default OpenGL is approximately 0.54 MB; the alternative Windows Vulkan bundle is approximately 11.47 MB. Install one assembled profile. The no-LOD build is an extraction/absence proof and is not required for normal use.

Terrain algorithms, generator/data resources, cache encoding and saved fluid properties are unchanged. Loaded water can settle differently under the new pressure behavior. This update does not repair repeated basin carving, LOD page-edge walls or placeholder vegetation. Region-wide lake levels/inflow/spillways, displacement, hollow-block storage, measured machine/container integration, lava, erosion, groundwater and breaking coastal surf remain unfinished. Local pressure must not be presented as complete basin filling or full PhysEx parity. Environmental catalogs remain planning-only.

Compact pass records: [native-evidence.txt](validation/0.0.1-alpha.19/native-evidence.txt). Development fixtures live in `src/visualValidation` and `src/coreChecks` and are excluded from releases. Example native routes are `runClient -PvisualValidation -PpackagedValidation -PgraphicsValidation`, `-PworldCreationValidation`, and `-PengineValidation -PcaptureWorld=<fixture-world>`; `-PwithoutLod`, `-PwithoutVulkan` and `-PvulkanPlatform=<platform>` select the package. Dedicated pressure validation uses `DEEPPRIMAL_PRESSURE_CHECKS=true` and `DEEPPRIMAL_PRESSURE_RECORD=<absolute-output>` with a disposable server.


<!-- END PRESERVED SOURCE -->
