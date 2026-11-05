# Expanded replica — docs/colormaps/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/colormaps/README.md) |
| Source identity | git: docs/colormaps/README.md |
| Preserved source SHA-256 | `e32a61d8fb79c4d6b0df92cb18e6f0145540daac47e38ec1b89e08cef54ddae8` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

The colormap README documents first-pass palettes for three opaque 256×256 RGB lookups and 40 existing water biome colors, with isolated export and exact resource checks. It explains the intentional Minecraft namespace required by existing loaders and the possible effect on other compatible worlds. It also distinguishes visual tint from GradientPalette's ordered material-ID fields and labels those V8 preview paths unregistered. The 40 mapped runtime profiles are not the full 118 proposed profiles. Some side-tint status text belongs to an earlier integration epoch and needs a pointer to later dual-tint evidence.

## Research expansion — gaps and details to deepen

- The namespace choice makes resource priority and cross-world reach an operational requirement. A valid pack can affect more than Deep Primal terrain, so acceptance should identify the exact loader, biome lookup and active pack stack. Headless resource equality does not prove runtime lookup coordinates, reload behavior or final lighting appearance.
- Color lookup values and material-gradient IDs need separate schemas and revision identities. The former changes visual output, while the latter can change generated block identity and save compatibility. A shared word such as palette should not obscure those different ownership and migration rules.

## Research expansion — proposed contracts

- Add a palette manifest naming lookup dimensions, color encoding, coordinate convention, expected loader, namespace, source definition and approval state. Track the 40 existing biome mappings explicitly and leave proposed profiles unverified until registered and reviewed. Link current dual-tint integration rather than rewriting historical observations as fresh results.
- Give visual tint and material-ID gradients separate authoring panels and compiler outputs. Material gradients require stable IDs, deterministic spatial variation and saved generator identity; visual palettes require runtime resource precedence and color-space checks. An offline editor should show which kind of output the user is changing before Apply.

## Research expansion — acceptance checks

- Load the pack through the actual target game, sample known climate coordinates and biome water entries, switch pack priority and reload. Verify the documented namespace behavior in both Deep Primal and another compatible world rather than assuming isolation.
- Compare exact exported resources with the manifest, inspect tint under real light and mips, and verify changing a visual palette never changes material IDs. Keep V8 material preview results separate from frozen V1–V7 generation and from owner approval of final color direction.

## Research expansion — source anchors

- Deep Primal natural colormaps v1.0
- Runtime assets
- Edit and regenerate

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal natural colormaps v1.0

Original first-pass art direction for Deep Primal, targeting Minecraft 26.1.2 (resource format 84). These are authored colors, not copies of Minecraft default images. They are not a final owner-approved visual palette.

## Runtime assets

Three opaque 256x256 RGB lookup textures are installed under `src/main/resources/assets/minecraft/textures/colormap/`: grass, foliage and dry foliage. Forty water/fog/clarity JSON files are installed under `src/main/resources/assets/minecraft/deepprimal/water_palette/`, matching all 40 biome IDs in both current Standard and Deep engine palettes. The `minecraft` namespace is required by the existing loaders and biome IDs.

The normal main resource source set includes these in future mod builds. A higher-priority resource pack can override them. No new loader, shader, registry, terrain revision or water solver change is introduced. Vegetation replacements affect every compatible biome that consults the standard maps, including worlds outside Deep Primal. Fixed species tints, explicit biome color overrides and biome modifiers still apply. Water altitude/chemistry/depth/lighting behavior and feature toggles remain in effect.

## Edit and regenerate

Edit `tools/colormaps/source/palette.json`, then from the repository root run:

```sh
python3 tools/colormaps/source/build_colormaps.py
```

Requires Python 3 and Pillow; DejaVu Sans is preferred for the preview, with a fallback font. This creates an isolated optional resource-pack tree at `tools/colormaps/pack/` (ignored by Git). It does not silently overwrite committed runtime assets. Review the generated PNGs/JSON and copy its `assets/` tree into `src/main/resources/assets/` when accepting palette edits. The generated pack also contains a preview and editable source. Read `tools/colormaps/source/README.txt` for the lookup convention and standalone installation details.

## Color lookup versus material gradients

The climate colormaps tint blocks whose Minecraft color provider reads the map; they do not choose or place block variants. Keep that concern separate from a material gradient:

- Use grayscale/albedo variants for future grass, dirt and podzol families, with the climate colormap supplying hue.
- Use an ordered material palette for transitions between those variants. The pure-core `GradientPalette` maps a normalized progression to ordered material IDs and can gently warp transition boundaries with low-frequency, seed-stable spatial noise. Zero variation gives a clean ordered ramp; coherent variation can form broad clusters without per-block random speckling.
- Feed progression from authored terrain context (for example moisture, exposure, elevation or weathering), not an unbounded random roll. Keep any palette accents deliberate and sparse so they do not destroy the underlying transition.

`GradientPalette` now drives ordered surface material selection through the explicit, unregistered V8 preview overload and its [versioned surface snapshot](../engine-stage2-2026-10-24/README.md#ground-surface-preview). It is not wired into V1-V7 material placement, a registered V8 generator or datapack loading, and it does not modify or generate textures. Existing world-generation output remains unchanged. The owner's approved grayscale textures are available, with three opt-in registered material samples; see the [ground material integration and limitations](../art/ground-textures-v2/README.md#material-sample-integration). Preserve the authored textures and masks when syncing upstream. Final material variants and dual-tinted sides remain open. The implementation direction follows the distinction between ordered transitions and splatter described in the [Conquest Reforged gradient guide](https://www.conquestreforged.com/guides/gradients), without copying its assets or code.

## Validation and limits

The 43 committed runtime assets are byte-identical to the delivered Natural Colormaps v1.0 pack. All images decode at 256x256 RGB, contain no black/transparent holes, and have a maximum neighboring channel jump of 2, 2 and 1 respectively. Generation reproduces all 43 resource files exactly. Water values satisfy the existing codec ranges and cover both current 40-entry palettes exactly.

198,147 checks passed against the actual Minecraft 26.1.2 grass/foliage/dry-foliage lookup classes in a headless JVM. The preview was visually inspected. See `validation.json`, `lookup-check.txt` and `water-palettes.csv` here. These checks do not establish in-game resource reload, Vulkan/OpenGL scene appearance or a rebuilt mod JAR. No new full build, native visual pass, engine grade or installer is claimed by this asset commit.

The 118 proposed Deep Primal catalog profiles, species-specific tint systems and dynamic source/chemistry-driven color authoring remain separate work. Existing V1-V7 geography, saved blocks and water quantity are unchanged. Builder pause and auditor coordination are unchanged.


<!-- END PRESERVED SOURCE -->
