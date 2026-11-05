# Expanded replica — gui-themes/docs/ALPHA6-VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA6-VALIDATION.md) |
| Source identity | git: gui-themes/docs/ALPHA6-VALIDATION.md |
| Preserved source SHA-256 | `ffb29dd0b7c56f7e90a5acf1da8aa539214d6f343a9a5c84b91105dcacaded75` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Themes alpha6 HUD layout editing with drag, keyboard nudge, Save/Reset/Cancel and screen-relative positions while preserving authoritative companion progression and thirst. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that live input tests used synthetic provider values and do not prove actual gameplay integration or pixel-identical concept art. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a layout persistence contract covering preview state, saved anchors, cancellation, safe margins and provider-dependent row visibility. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Resize after saving, use keyboard-only moves and Escape cancellation, verify no uncommitted positions persist and confirm provider absence collapses unused rows without inventing thirst. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.6 HUD and layout editor

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.6 HUD and layout editor

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

Changes:
- Removed Health/Hunger/Thirst word labels and compacted the meter row.
- Added the mockup-style slim framed purple progression bar and centered level text.
- Added Themes → HUD layout with live day/weather preview, mouse dragging,
  keyboard nudging, Save, Reset and Cancel. Escape discards unsaved edits.
- Saved positions use fractions of available screen space with six-pixel margins.
- Kept companion-provided progression and thirst authoritative. Deep Primal presence
  without a provider still suppresses vanilla XP; the unused bar row collapses.

Validation: `DEEPPRIMAL_HUD_VALIDATION=1 gradle -PthemeValidation -PthemeCompatValidation runClient`
under Xvfb/llvmpipe. The harness created a disposable flat world and exercised actual
screen input handlers in a running client. It verified:
- Theme selector entry point; editor disabled for Vanilla.
- Mouse drag grab offset and Shift-arrow movement.
- Draft edits do not mutate config; Save writes both coordinates to the TOML file.
- Reopening uses the saved position; Cancel and Escape discard draft edits.
- Reset previews the default and Save persists it.
- Top-left and bottom-right clamping, and GUI scale 2 → 1 → 2 behavior.
- Riverstone & Moss and Field Journal HUDs, provider absence, and Vanilla restoration.

Result: PASS (`run/alpha6-hud-validation-success.txt`). Captures of the live HUD,
selector and editor were visually inspected. The compatibility run used a dev-only
`deepprimal` presence stub and synthetic provider values; it did not test the actual
Deep Primal gameplay module. Validation classes and the presence stub are excluded
from the release JAR.

The retained native heart/food sprites, pixel droplets and current hotbar material
are not claimed to match all illustrated mockup details. This release changes labels,
progression styling and day/weather layout controls.


<!-- END PRESERVED SOURCE -->
