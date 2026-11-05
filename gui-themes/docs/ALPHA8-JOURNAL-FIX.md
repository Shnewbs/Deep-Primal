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
