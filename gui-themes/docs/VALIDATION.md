# 0.1.0-alpha.1 validation

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

- Standalone Gradle build and development harness compilation passed.
- Client launched with the theme mod and NeoForge, without Deep Primal installed.
- An integrated flat test world loaded successfully.
- Basalt & Copper, Field Journal, Rawhide & Flint, Riverstone & Moss, Timber & Iron,
  Clay & Ember, Obsidian & Ash, and Frost & Slate loaded in sequence.
- The resource manager reported the expected inventory source pack for every theme.
- Returning to Vanilla restored the inventory resource source to `vanilla`.
- Actual title, inventory and theme-selector screenshots were captured and reviewed.
- Review identified and corrected low-contrast empty-slot icons and the crafting arrow.
  The title now uses a material panel for readable branding over the panorama.
- Each theme contains 286 native GUI texture replacements. The generator verifies original
  dimensions and per-pixel alpha masks, all 36 player inventory wells, and a distinct crafting
  arrow. Copied native sprite metadata preserves the target version's scaling behavior.
- Final packaging checks confirm all eight embedded packs, expanded mod metadata, mixins,
  and exclusion of development harness classes.

The client run used software OpenGL 4.5 (Mesa llvmpipe). The final smoke run used a null
sound device because the build environment has no physical audio output.

Scope: this validates standalone startup, rendering and configuration-driven theme transitions.
It does not establish compatibility with every third-party GUI or renderer mod, exercise all
inventory mouse/keyboard gestures, or test a Deep Primal provider. The optional gameplay
bridge awaits the later Deep Primal integration. This alpha implements native GUI themes;
it is not a pixel-for-pixel recreation of the concept images.

Reproduce with `./gradlew build` and `./gradlew -PthemeValidation runClient` from `gui-themes/`.
