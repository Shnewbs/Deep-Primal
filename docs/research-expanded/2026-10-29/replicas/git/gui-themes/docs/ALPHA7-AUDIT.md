# Expanded replica — gui-themes/docs/ALPHA7-AUDIT.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA7-AUDIT.md) |
| Source identity | git: gui-themes/docs/ALPHA7-AUDIT.md |
| Preserved source SHA-256 | `650c393de916c914872ab2ab747c97cc48b1f36341fbedf5a10c05c140cddb16` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

The alpha7 standalone Themes code and visual audit correcting runtime rims, compact hotbar bounds, slot sampling and other integration details across 34 Java files. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that forced reload error recovery was code-reviewed only, and arbitrary third-party screens and live Deep Primal providers were not exhaustively exercised. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a UI lifecycle matrix separating normal theme switching, overlapping reloads, failed resource loads and provider attach/detach. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject reload failure and completion reordering, assert no stale theme publication or slot-coordinate drift and inspect real accessibility and third-party screens independently of the standalone audit. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.7 code and visual audit

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.7 code and visual audit

Scope: the standalone Deep Primal Themes module for Minecraft 26.1.2,
NeoForge 26.1.2.122 and Java 25. Reviewed all 34 runtime Java files,
the asset generator, mixin declarations, config, resource-pack registration,
build configuration, integration API and development validation harnesses.
The main Deep Primal gameplay/rendering repository is outside this module.

## Findings and corrections

| Area | Finding | Correction |
| --- | --- | --- |
| Panel and toolbar perimeters | Large mockup-panel corners and repeated edge strips did not join at small sizes; hotbar art extended outside its native bounds. | Continuous runtime rims, retained interior materials, compact hotbar frame inside its 182 × 22 bounds. |
| Slots and selection | Sampling whole illustrated wells duplicated borders; the selection outline reached beyond the bar. | Sample only well interiors, draw one inset rim, contain selection and offhand frames. |
| HUD actions | Click rectangles differed from displayed hints; offhand items and long key labels could overlap hints. | Shared render/hit-test geometry with offhand/attack-indicator clearance and available-width checks; disabled companion actions are not advertised. |
| Native buttons | Theme drawing mutated persistent foreground settings and cleared other mods' colours; disabled/hovered states could lose contrast. | Adjust submitted label text only; preserve explicitly configured foregrounds and Vanilla behaviour. |
| Menu text | Narrow Clay & Ember Singleplayer button used half the size of its neighbours. | Consistent two-pixel side margins; oversized labels truncate instead of spilling beyond a button. |
| Native workstations | Generic backplate replacement erased baked guides; gray arrows became almost invisible on dark themes. | Limit wholesale backplate replacement to known plain creative/chest panels; retain specialized native layouts and recolour guide symbols separately from slot wells. |
| Survival display | A positive but very low health value could round to no hearts; frozen/hardcore/hunger variants were omitted. | Round positive display units upward and use native heart variants, hunger sprites and NeoForge heart-type customization. Move absorption text away from the armor row. |
| Editor | A badge dragged over Save/Reset/Cancel intercepted those controls. | Route input to the visible toolbar first; preserve draft/save/cancel behaviour. |
| Recipe navigation | Narrow recipe-book Back was a drawn click zone without normal keyboard focus. | Real focusable button with keyboard activation and matching mouse geometry. |
| Live reload | An already-open custom container could retain custom geometry after switching to Vanilla. | Reopen the appropriate native/custom screen and restore menu slot positions. Invalidate stale reload callbacks after recovery. |
| Overlay ordering | The HUD was drawn above chat; the default day badge overlapped status effects. | Register the HUD immediately after the contextual bar, below chat; default corner anchor reserves space for native effect icons. |
| Drawing overhead | Each frame rebuilt atlas descriptors/identifiers and tiled many small border fragments. | Cache atlas metadata and texture identifiers; draw material interiors once with continuous rims. No FPS improvement is claimed without a benchmark. |

## Verification

Development clients ran under Xvfb with software OpenGL. Screenshots were inspected
at native size and in contact sheets, including each theme's inventory, workstation,
creative tabs and HUD. No validation classes or compatibility stub ship in the JAR.

- Full client pass: eight themes plus Vanilla; title, selector, inventory, furnace,
  crafting, chest, creative, HUD and four toast classes. Verified 46 inventory slot
  IDs, bounds, unique coordinates and restoration of native positions.
- Additional rendered screens: anvil, smithing, enchanting, brewing, hopper,
  stonecutter, cartography, grindstone, smoker and blast furnace.
- Menu pass: all eight themes and Vanilla at GUI scales 1 and 2; geometry checked
  at 320 × 240, 427 × 240, 640 × 360, 960 × 540 and 1280 × 540 GUI dimensions;
  Singleplayer/Options navigation and Vanilla title restoration.
- HUD/editor pass: real drag and key handlers, Save to TOML, reopen, Reset,
  Cancel/Escape, edge clamping, GUI scaling and overlapping Save controls.
- Regression checks: native/default and explicit widget foreground state;
  keyboard Recipe Back; live Vanilla/custom inventory and furnace switches;
  furnace slot restoration; low health, armor, air and offhand display;
  final-slot selection; day/status-effect separation; corrected workstation guides.
- Asset generator: 274 textures per custom theme retain original dimensions and
  alpha masks; all 36 native player-inventory wells remain valid. Measured arrow
  contrast across 32 combinations of four workstation guides and eight themes:
  minimum 5.61:1 against adjacent panel pixels.

Commands (with Java 25 and Gradle 9.2.1):

```sh
gradle clean build --no-build-cache
gradle -PthemeValidation -PthemeCompatValidation runClient
DEEPPRIMAL_MENU_VALIDATION=1 gradle -PthemeValidation runClient
DEEPPRIMAL_HUD_VALIDATION=1 gradle -PthemeValidation -PthemeCompatValidation runClient
```

Pass markers: `alpha7-theme-validation-success.txt`,
`alpha7-menu-validation-success.txt`, `alpha7-hud-validation-success.txt`
in the development run directory.

## Limits

The companion compatibility test uses a dev-only presence stub and synthetic
progression/thirst data, not the actual Deep Primal gameplay module. Arbitrary
third-party screens, hardware-specific rendering, and forced resource-pack failures
were not exhaustively exercised. The reload race fix was reviewed in code; normal
reload and theme switching were exercised; forced error recovery was code-reviewed only.
The HUD still uses native heart/food artwork and simple thirst droplets; this release
does not claim pixel-identical reproduction of every illustrated mockup asset.


<!-- END PRESERVED SOURCE -->
