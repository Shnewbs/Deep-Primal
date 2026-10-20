# Deep Primal Themes

Standalone client GUI themes for **Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25**.
This project builds independently of Deep Primal. It has no Deep Primal runtime dependency.

## Install and select a theme

Put `DeepPrimal-Themes-26.1.2-0.1.0-alpha.1.jar` in the client's `mods` folder.
Open **Themes** at the top left of the title screen or Options, or use **Mods → Deep Primal Themes → Config**.
Select a theme, then **Apply theme**. Applying reloads client resources; no world restart is required.
**Vanilla** is the default and removes the selected theme pack.

The file `config/deepprimal_themes-client.toml` also supports:

```toml
theme = "VANILLA"
titleBranding = true
integrationHud = true
```

| Config value | Appearance |
| --- | --- |
| `VANILLA` | Original Minecraft textures and title |
| `BASALT_COPPER` | Dark basalt and copper |
| `FIELD_JOURNAL` | Parchment and olive ink |
| `RAWHIDE_FLINT` | Stitched hide and flint |
| `RIVERSTONE_MOSS` | Pale weathered stone and moss |
| `TIMBER_IRON` | Timber grain and iron fittings |
| `CLAY_EMBER` | Warm clay and ember accents |
| `OBSIDIAN_ASH` | Obsidian, ash and orange embers |
| `FROST_SLATE` | Blue slate and frost |

## What this alpha changes

Eight native-resolution theme packs cover vanilla container panels, inventory,
crafting and workstation sprites, creative inventory, recipe book controls,
buttons and other widgets, tooltips, advancement frames, toasts, hotbar, and menu separators.
Custom themes add optional Deep Primal title branding and readable container labels.
The selector is available with every theme, including Vanilla.

This is an implementation of the eight material/palette directions, not a pixel-for-pixel
conversion of concept images. Inventory layout, slot counts, item rendering, click targets,
recipes, and server behavior remain controlled by Minecraft. Third-party screens with their
own textures require adapters. No new panorama or custom world background is bundled.

The build derives textures from the exact target version's native atlases. It checks every
texture's dimensions and alpha mask, and all 36 player inventory wells. Vanilla sprite
metadata is copied intact. Only the currently selected theme is registered as a required
client resource pack; selecting Vanilla unregisters it. A failed resource reload restores
Vanilla during Minecraft's recovery path.

## Later Deep Primal integration

`com.shnewbs.deepprimal.themes.api.ThemeIntegration` is the client-only bridge.
Register one `Provider` during companion client setup. It supplies current thirst, progression,
and optional Journal/Hearth actions. Query live client state in the provider. `SurvivalState`
uses thirst/progress fractions in `[0,1]`, and a nonnegative level. Unregister when appropriate.

Without a provider, no invented survival statistics or nonfunctional gameplay buttons appear.
The module never enables, disables, or awards vanilla XP. The companion owns gameplay,
network packets, cooldowns, and server permission checks. Never register a client provider
on a dedicated server.

```java
ThemeIntegration.register(new ThemeIntegration.Provider() {
    public Optional<ThemeIntegration.SurvivalState> survival() {
        return Optional.empty(); // Replace with your synced client survival state.
    }
    // Override journal() / hearth() when actual actions are available.
});
```

The optional survival strip sits above the vanilla HUD. Mods that add extra HUD rows should
adapt its placement or disable `integrationHud`. Optional inventory action buttons are
shown only when there is space above the inventory. This is a bridge for later integration,
not an implementation of Deep Primal gameplay systems.

## Build

From this directory, with Java 25 selected:

```sh
./gradlew build
```

Output is in `build/libs/`. The root Deep Primal build does not include this project yet.
Generated textures are reproducible build outputs, not checked-in copies of Minecraft assets.

An opt-in client harness creates a disposable flat world, switches all eight themes back to
Vanilla, checks each inventory's resource origin, and captures actual inventories/selectors:

```sh
./gradlew -PthemeValidation runClient
```

Use a graphical desktop or an X server. Results appear under `run/`: screenshots and a
`theme-validation-success.txt` or `theme-validation-failure.txt` marker. Harness classes are
excluded from the release JAR. Remove old markers before repeating the run.
