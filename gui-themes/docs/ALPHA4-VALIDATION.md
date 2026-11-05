# Alpha.4 UI corrections

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

## Changes

- Title backgrounds use adaptive cropping with a protected logo/menu region; all four actions follow the same transform. Extreme aspect ratios retain a dark backdrop where necessary.
- Main labels scale with button height and align to physical pixels. Footer controls form a centered, responsive row.
- Themed native button labels disable the font shadow through their text style. Custom headings and HUD labels use one unshadowed text pass. Vanilla rendering is retained when Vanilla is selected.
- Theme selection visibly highlights the selected option and previews larger, evenly spaced wells.
- Inventory and workstation player grids use a consistent 24-pixel pitch in both directions, with 20-pixel wells. Panels are 260 by 210 GUI pixels. Armor wells fit their 18-pixel pitch.
- Slot rims use subdued inset edges instead of bright connected borders; the hotbar has gaps between wells.
- Survival meters have full-size nine-pixel icons and padded headings. Standalone Creative omits survival/progression panels. The Journal hint is one line; Hearth appears only when an integration supplies an enabled action.
- Day/weather share a compact panel. Armor/air layers are raised clear of the custom survival meters.
- Full Java compilation avoids stale classes observed during incremental builds in this workspace.

## Validation scope

The menu harness checks all eight 3840 by 2160 textures, action bounds across five GUI sizes, GUI scales one and two, Singleplayer/Options navigation, and Vanilla title restoration.
The in-world harness exercises eight themes and Vanilla, 46 unique inventory slot IDs and bounds, original Vanilla coordinate restoration, narrow/wide recipe layouts, inventory, selector, furnace, crafting table, chest, creative inventory and HUD. It also captures standalone survival and Creative HUDs without a companion provider.
The final JAR is checked to contain the updated HUD class, eight menu PNGs, and no development validation classes.

These are local development-client checks, not a test against every third-party mod or desktop resolution. The four main actions stay within their protected region; exceptionally wide/narrow windows may show a backdrop beyond the artwork. The existing upscaled 4K assets remain unchanged, so download size is still about 109 MiB.
