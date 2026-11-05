# Expanded replica — gui-themes/docs/VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/VALIDATION.md) |
| Source identity | git: gui-themes/docs/VALIDATION.md |
| Preserved source SHA-256 | `23b8c47f09ecabf57bfeacf1ae893d5e3ab54b55a951ec4598940238413752ce` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Themes alpha2 standalone validation preserving all 46 native survival slot identities, unique themed positions and restoration of Vanilla resource origins. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that visually opened workstation menus did not validate every input gesture or server transaction, and live providers or third-party renderers were not exercised. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a distinction between visual layout, input behavior and authoritative container transactions in every UI acceptance record. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat shift-click, drag, number-key and accessibility navigation with real server containers, verify native slot identities throughout and keep concept-art fidelity separate from functional acceptance. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- 0.1.0-alpha.2 validation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# 0.1.0-alpha.2 validation

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

## Completed checks

- Standalone Gradle compilation and release build; development harness excluded from JAR.
- Actual client startup and integrated flat world, without Deep Primal installed.
- All eight themes load in sequence, followed by Vanilla. Resource origins match the
  selected theme; Vanilla restores the original inventory resource source.
- All 46 survival inventory slots retain their native identities and count, have unique
  positions within the custom panel, and restore their original coordinates under Vanilla.
- Actual title, inventory, selector, furnace, crafting, HUD, large chest and creative
  inventory screenshots captured for every theme and Vanilla.
- Original mockup material samples reviewed to remove baked text, item borders and board
  captions from runtime backgrounds. Eight original PNG atlases are bundled unmodified.
- Large chest texture slices replaced with one continuous panel, including player inventory.
- Narrow recipe-book view uses a separate themed panel and a return control. Wide view uses
  a larger horizontal offset for the 260-pixel inventory. The native layout threshold is
  adjusted only for this mod's custom screens.
- Native fallback asset generator checks texture dimensions and alpha masks, inventory
  wells and crafting-arrow contrast. Sprite metadata preserves native scaling behavior.
- Packaging checks cover all eight original atlases and generated packs, expanded mod
  metadata, mixin declarations, and exclusion of validation classes.

The client used software OpenGL 4.5 (Mesa llvmpipe), Xvfb and a null audio device. A
validation-only provider supplies thirst/progression to exercise those HUD regions; it is
not included in release builds. The test world's item stacks are also harness-only data.

## Limits

The harness checks rendering, resource transitions and geometry. It does not exercise every
mouse/keyboard inventory gesture or validate server transactions through the visually opened
workstation test menus. No live Deep Primal provider or third-party renderer was tested.
Thirst and Hearth still require the later Deep Primal integration. Native game item sprites,
fonts and interactions are retained, so this is a playable adaptation of the approved boards,
not a claim that every rendered pixel is identical to concept art.

Reproduce with `./gradlew build` and `./gradlew -PthemeValidation runClient` from `gui-themes/`.
Remove old run markers before repeating; review both the current marker and client log.


<!-- END PRESERVED SOURCE -->
