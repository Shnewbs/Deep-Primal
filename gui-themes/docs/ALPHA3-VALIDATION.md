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
