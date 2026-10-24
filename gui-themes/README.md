# Deep Primal Themes

Client GUI overhaul for **Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25**.
The root Deep Primal build now bundles this module into its single installable JAR.
It can also build independently and has no Deep Primal runtime dependency.

## Install and select a theme

The combined Deep Primal installer already contains this module; remove older separate Themes JARs. For standalone use, install the Themes JAR built by this project.
Open **Themes** on the title screen or Options, or **Mods → Deep Primal Themes → Config**.
Select a theme and **Apply theme**. Resources reload without a world restart.
**Vanilla** is the default; it restores native screens, slot positions, textures and HUD.

`config/deepprimal_themes-client.toml` supports:

```toml
theme = "VANILLA"
titleBranding = true
integrationHud = true

[hudLayout]
dayX = 1.0
dayY = 0.0
```

**Themes → HUD layout** opens the day/weather editor after a custom theme is applied.
Drag the badge, or select it and use arrow keys (Shift moves ten GUI pixels).
**Save** stores the layout; **Cancel** or Escape discards edits. **Reset** previews the
default top-right position; click Save to keep it. Positions are relative to the available
screen area, so resizing or changing GUI scale keeps the badge on-screen. The editor also
offers a preview from the title menu. The layout is shared across the eight custom themes.

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

## Alpha.8 Journal background fix

Opening Journal from the inventory tab or J keeps the world visible behind the panel.
The Journal now identifies itself as in-game UI, using Minecraft's translucent dimming
instead of the opaque themed menu background. Back and Escape retain their existing
return destinations. The Journal content and the theme artwork are unchanged.

Run `DEEPPRIMAL_JOURNAL_VALIDATION=1 gradle -PthemeValidation runClient` for the
dedicated Journal navigation and screenshot checks across all eight themes and GUI scales 1/2.

## Alpha.7 audit fixes

Hotbars, buttons, creative tabs and panels now use continuous rims drawn at their actual
size. Decorative board corners and repeated edge strips no longer spill into adjacent
controls. Slot wells have a single inset border; selection outlines stay inside the hotbar.
The existing artwork materials and all eight 4K menu backgrounds are retained.

HUD shortcut rendering and hitboxes share geometry, reserve space for offhand items and
hide when a rebound key label would not fit. Low health retains a visible half-heart;
hardcore, freezing, hunger effects and NeoForge heart-type hooks are preserved. The default
day/weather anchor leaves room for status-effect icons, and chat renders over the HUD.

Native button foreground settings are preserved. Hover/disabled labels have appropriate
contrast, and oversized custom labels fit or truncate within their buttons. Specialized
vanilla workstations retain their native diagrams, with readable arrows and plus signs.
Live theme switches rebuild open custom containers and restore native slot positions.
Recipe Back supports keyboard activation; dragging the day badge over editor controls no
longer blocks them. Reload callbacks are invalidated after resource-pack recovery.

See `docs/ALPHA7-AUDIT.md` for scope, verification and limitations.

## Original mockup artwork and functional screens

The themes use a dedicated 3840 × 2160 background for each title menu. The images were
regenerated from the approved boards, then upscaled from 1672 × 941 with Lanczos resampling
and mild sharpening; they are not native 4K renders. The original approved boards remain
as source atlases for inventory and workstation materials.

Singleplayer, Multiplayer, Options and Quit are live game controls with localized labels.
Continue and its saved-world tracking have been removed. Backgrounds preserve their 16:9
aspect ratio with adaptive cropping that keeps the logo and controls visible. Extreme
aspect ratios use a dark backdrop when needed. Labels and the footer scale with the layout.
The scenes are static menu artwork, not world presets. Eight lossless 4K PNG backgrounds
account for approximately 83 MiB of the roughly 109 MiB release JAR.

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

Notifications use matching theme text and backplates, including recipe, tutorial, system and advancement toasts.

The HUD shows actual health, hunger, progression, day and weather. Alpha.6 removes the
Health/Hunger/Thirst word labels and their tall boxes. Progression uses a slim bevelled
frame, purple fill, and centered `Level N` plaque above the hotbar. Day/weather uses a
movable two-line badge. Native heart/food sprites and the existing theme hotbar textures
remain in use; this is not an exact reproduction of every illustrated mockup asset.
When Deep Primal is
loaded without a progression provider, the empty bar row collapses; mounted jump bars keep
their space. Timber & Iron and Clay &
Ember use the reference bar treatments; other themes use icon rows. Journal opens with **J**
(default) or its inventory tab. The standalone journal shows live player data. Hearth uses
**H**; its HUD hint stays hidden until a companion supplies an enabled action. Keys are rebindable.
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

With Java 25 and Gradle 9.2.1 selected, run from this directory:

```sh
gradle clean build --no-build-cache
gradle -PthemeValidation runClient
```

The root Deep Primal build includes this project and bundles its JAR. Standalone JARs appear in
`build/libs/`. Generated fallback textures derive from the target game's native assets.
The opt-in harness creates a disposable flat world, switches all eight themes and Vanilla,
checks resource sources and inventory geometry, and captures actual game screens under
`run/screenshots/`. A success/failure marker appears under `run/`. Remove old markers before
rerunning. Harness classes and test survival data are excluded from the release JAR.
See `docs/VALIDATION.md` for verification scope and limitations.

Set `DEEPPRIMAL_MENU_VALIDATION=1` when launching the opt-in harness to run the menu checks without creating a world. This verifies all eight 4K resources, button geometry and navigation, and Vanilla restoration.
