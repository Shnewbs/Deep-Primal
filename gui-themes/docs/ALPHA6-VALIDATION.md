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
