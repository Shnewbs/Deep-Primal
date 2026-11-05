# Expanded replica — gui-themes/docs/ALPHA8-JOURNAL-FIX.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA8-JOURNAL-FIX.md) |
| Source identity | git: gui-themes/docs/ALPHA8-JOURNAL-FIX.md |
| Preserved source SHA-256 | `750c37009ccb682c86068778be36a5dd849844b43a0f3ec28a3dff97ad21ea82` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Alpha8 Journal in-game classification so native translucent dimming preserves world and HUD instead of the opaque menu-material background. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the fix changes only Journal classification and version metadata; companion-owned Journal implementations were not changed or tested. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a screen ownership contract distinguishing in-world panels, title-menu paths, return destinations and companion-provided screens. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Open from inventory and the J key across all themes and GUI scales, verify Back/Escape destinations, visible world and deferred subtitles, then test outside-world behavior separately. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.8 Journal world-background fix

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.8 Journal world-background fix

The standalone Journal inherited `Screen.isInGameUi() == false`. Minecraft then
rendered its blurred menu background; the Themes menu mixin replaced that with an
opaque material, covering the world when the inventory's Journal tab was selected.

`ThemeJournalScreen.isInGameUi()` now returns true while a client world exists.
Minecraft uses its native translucent in-game background and continues deferred
subtitle rendering. Outside a world it retains the normal menu-background path.
Journal content, return destinations, pause behavior and artwork are unchanged.

## Verification

- Java 25 / NeoForge 26.1.2.122 build passed.
- Live client harness passed across all eight custom themes.
- Mouse-clicked Journal from the themed inventory; Back and Escape both returned
  to that inventory. The J key mapping opened Journal directly; Back and Escape
  both returned to gameplay.
- Captured inventory, Journal from the tab, returned inventory, Journal from J,
  and Journal at GUI scale 1 for each theme (40 actual game screenshots).
- Visually inspected all eight Journal tab captures and the scale-1 view: sky,
  terrain and HUD remain visible behind the panel with native dimming.
- Release JAR entry comparison against alpha.7 found only the Journal class and
  mod version metadata changed. No validation code is included in the JAR.

Run `DEEPPRIMAL_JOURNAL_VALIDATION=1 gradle -PthemeValidation runClient` to repeat
the regression harness. The success marker is `run/alpha8-journal-validation-success.txt`.
These checks cover the standalone Journal; companion-provided Journal screens
are owned by their integration and were not changed or tested here.


<!-- END PRESERVED SOURCE -->
