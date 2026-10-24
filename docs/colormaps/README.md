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

## Validation and limits

The 43 committed runtime assets are byte-identical to the delivered Natural Colormaps v1.0 pack. All images decode at 256x256 RGB, contain no black/transparent holes, and have a maximum neighboring channel jump of 2, 2 and 1 respectively. Generation reproduces all 43 resource files exactly. Water values satisfy the existing codec ranges and cover both current 40-entry palettes exactly.

198,147 checks passed against the actual Minecraft 26.1.2 grass/foliage/dry-foliage lookup classes in a headless JVM. The preview was visually inspected. See `validation.json`, `lookup-check.txt` and `water-palettes.csv` here. These checks do not establish in-game resource reload, Vulkan/OpenGL scene appearance or a rebuilt mod JAR. No new full build, native visual pass, engine grade or installer is claimed by this asset commit.

The 118 proposed Deep Primal catalog profiles, species-specific tint systems and dynamic source/chemistry-driven color authoring remain separate work. Existing V1-V7 geography, saved blocks and water quantity are unchanged. Builder pause and auditor coordination are unchanged.
