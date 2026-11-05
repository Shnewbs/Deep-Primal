# Expanded replica — gui-themes/docs/ALPHA4-VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA4-VALIDATION.md) |
| Source identity | git: gui-themes/docs/ALPHA4-VALIDATION.md |
| Preserved source SHA-256 | `de35a40bfef5c552730548a60a9c36bf6d68879e6eabbf982ae7748f9e2351a7` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Themes alpha4 adaptive protected-region cropping, pixel-aligned labels, responsive footers, selected-theme feedback and consistent native inventory-grid spacing. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that local development screenshots do not cover every modded screen or resolution, and extreme aspect ratios may intentionally expose backdrop beyond the artwork. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a responsive layout specification separating protected artwork regions, native slot identity, scalable text and intentional fallback backdrop. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Exercise extreme aspect ratios, narrow and wide recipe layouts and Vanilla restoration, verifying real hit targets and original slot coordinates after switching themes. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.4 UI corrections

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
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


<!-- END PRESERVED SOURCE -->
