# Deep Primal Themes

Standalone client GUI overhaul for **Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25**.
This project builds independently of Deep Primal and has no Deep Primal runtime dependency.

## Install and select a theme

Put `DeepPrimal-Themes-26.1.2-0.1.0-alpha.2.jar` in the client's `mods` folder, replacing alpha.1.
Open **Themes** on the title screen or Options, or **Mods → Deep Primal Themes → Config**.
Select a theme and **Apply theme**. Resources reload without a world restart.
**Vanilla** is the default; it restores native screens, slot positions, textures and HUD.

`config/deepprimal_themes-client.toml` supports:

```toml
theme = "VANILLA"
titleBranding = true
integrationHud = true
```

| Config value | Theme |
| --- | --- |
| `VANILLA` | Original Minecraft |
| `BASALT_COPPER` | Basalt & Copper |
| `FIELD_JOURNAL` | Field Journal |
| `RAWHIDE_FLINT` | Rawhide & Flint |
| `RIVERSTONE_MOSS` | Riverstone & Moss |
| `TIMBER_IRON` | Timber & Iron |
| `CLAY_EMBER` | Clay & Ember |
| `OBSIDIAN_ASH` | Obsidian & Ash |
| `FROST_SLATE` | Frost & Slate |

## Original mockup artwork and functional screens

Alpha.2 uses the eight approved boards as unmodified source atlases. Material samples,
frames, corners, slot wells and buttons are drawn from each original design. Field Journal
uses revision 3; the other seven use revision 2. The original illustrated title scene is
bundled with functional Continue, Singleplayer, Multiplayer, Settings and Quit controls.
Continue remembers the last local save. The scene is static artwork, not a world preset.

The survival inventory has the mockups' Inventory/Crafting/Journal tabs, player preview,
armor and offhand areas, 2×2 crafting, 27 storage slots and nine hotbar slots. It retains the
native menu, item rendering and slot IDs. Only client-side positions change; closing the
screen restores them. Crafting opens the native recipe book, with a separate view at narrow
GUI sizes and side-by-side placement when space permits.

Crafting tables, furnaces, smokers and blast furnaces have larger themed panels with real
input, fuel, output and progress. This module does not introduce a firepit gameplay block.
Large chests and creative inventory use matching artwork with their native slot geometry.
Secondary vanilla widgets and screens also receive generated theme textures and readable
labels. Third-party screens with custom rendering may need explicit adapters.

The HUD shows actual health, hunger, progression, day and weather. Timber & Iron and Clay &
Ember use the reference bar treatments; other themes use icon rows. Journal opens with **J**
(default) or its inventory tab. The standalone journal shows live player data. Hearth uses
**H** and stays disabled until a companion supplies an action. Keys are rebindable.
`integrationHud = false` restores native health/food/progression rendering while retaining
selected theme textures; `titleBranding = false` keeps the native title layout.

## Later Deep Primal integration

`com.shnewbs.deepprimal.themes.api.ThemeIntegration` is the client-only bridge.
Register one `Provider` during companion client setup. It supplies thirst, progression and
optional Journal/Hearth actions. `SurvivalState` takes fractions in `[0,1]` and a nonnegative
level. Query synced client state in the provider; unregister when appropriate.

```java
ThemeIntegration.register(new ThemeIntegration.Provider() {
    public Optional<ThemeIntegration.SurvivalState> survival() {
        return Optional.empty(); // Supply your synced survival state here.
    }
    // Override journal() / hearth() with real actions.
});
```

Without a provider, no thirst statistic is invented. Standalone progression displays vanilla
XP; with Deep Primal loaded, that fallback is suppressed. The module never awards XP or
implements teleportation. Deep Primal owns gameplay, networking, cooldowns and permission
checks. Do not register client integration on a dedicated server.

## Build and validation

With Java 25 selected, run from this directory:

```sh
./gradlew build
./gradlew -PthemeValidation runClient
```

The root Deep Primal build does not include this project yet. Release JARs appear in
`build/libs/`. Generated fallback textures derive from the target game's native assets.
The opt-in harness creates a disposable flat world, switches all eight themes and Vanilla,
checks resource sources and inventory geometry, and captures actual game screens under
`run/screenshots/`. A success/failure marker appears under `run/`. Remove old markers before
rerunning. Harness classes and test survival data are excluded from the release JAR.
See `docs/VALIDATION.md` for verification scope and limitations.
