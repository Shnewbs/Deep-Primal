# Alpha.5 notification contrast and HUD spacing

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

## Fixes

Recipe notifications retained Minecraft's dark purple/black labels after the previous resource pack recolored their background. Recipe, tutorial, system and advancement toasts now draw a matching theme backplate and theme text. Preformatted tutorial lines receive the same treatment. Text fade alpha is preserved, and custom notification text does not add shadows. Vanilla mode keeps native colors and rendering.

Toast icons are no longer recolored by the fallback asset generator. Only the four supported toast backplates are replaced at render time; native icons retain their original artwork.

When Deep Primal is detected without a progression provider, health/hunger panels move down by 13 GUI pixels to close the unused progression row. Armor and air overlays follow the same offset. Registered progression and mounted jump bars retain their reserved space. This does not restore vanilla XP while Deep Primal is loaded.

## Checks

A clean Java 25 build includes the new toast mixin and regenerated resources. The opt-in client harness captures actual recipe, tutorial, system and advancement toast implementations for all eight themes and Vanilla, together with existing inventory/workstation/recipe-layout checks.

The `themeCompatValidation` Gradle property adds a development-only mod presence stub named `deepprimal`, allowing the no-progression-provider layout to be exercised. It does not implement or test Deep Primal gameplay. The normal test provider separately exercises the progression-visible layout. Both the stub and validation classes are excluded from the release JAR.

Run the compatibility harness with:

```sh
gradle --no-build-cache -PthemeValidation -PthemeCompatValidation clean build runClient
```

Manual visual review covers light/dark notification text, native recipe icons, tutorial text wrapping, system messages, advancement text, and the compact no-provider survival HUD. Normal theme switching restores Vanilla resource origins and inventory slot coordinates.
