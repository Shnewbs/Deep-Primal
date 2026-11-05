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
