# Expanded replica — gui-themes/docs/ALPHA3-VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA3-VALIDATION.md) |
| Source identity | git: gui-themes/docs/ALPHA3-VALIDATION.md |
| Preserved source SHA-256 | `f343a7fb70a66a2e75d330cd376daf516bbba936b58ac3426cb2d997f645385c` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Themes alpha3 menu and label verification across eight upscaled 3840x2160 backgrounds, screen bounds and navigation with released class identity checks. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that captures were visually reviewed at 854x480 rather than desktop 4K, and alpha2 inventory, HUD and workstation gameplay checks were not repeated. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a theme acceptance matrix recording source art resolution, display resolution, GUI scale, screen family and exact tested artifact. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat menu geometry at narrow and wide sizes, restore Vanilla resource origins and verify no Continue or LAST_WORLD references or private validation classes enter the release. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.3 verification

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.3 verification

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.

- Clean Gradle build with build cache disabled passed.
- JAR archive CRC, metadata and all eight 3840 x 2160 PNG dimensions passed.
- Compiled release classes contain no Continue or LAST_WORLD references.
- Development client loaded all eight 4K backgrounds and restored Vanilla.
- Singleplayer and Options navigation passed for each theme and Vanilla.
- Runtime buttons stayed in screen bounds; four-button geometry passed at five GUI dimensions.
- Screenshots for all eight themes were visually reviewed at 854 x 480; desktop 4K rendering was not separately tested.
- Release class bytes were compared against the classes exercised by the client harness.
- Validation classes are excluded from the release JAR.

Backgrounds are upscaled from 1672 x 941, not native 4K renders. The current update changes title menus and label fitting. Existing inventory, HUD and workstation implementations remain from alpha.2; their gameplay checks were not repeated in this focused run.

Run `DEEPPRIMAL_MENU_VALIDATION=1 gradle -PthemeValidation runClient` with a working OpenGL display. Inspect `run/menu-validation-success.txt` and the current client log.


<!-- END PRESERVED SOURCE -->
