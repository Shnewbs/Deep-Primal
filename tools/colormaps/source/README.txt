DEEP PRIMAL — NATURAL COLORMAPS v1.0
Target: Minecraft 26.1.2 / resource format 84 / Deep Primal alpha.29-dev.2

INSTALL
Put this ZIP directly in the instance's resourcepacks folder. Enable it above
other packs that replace grass or foliage colormaps. Do not put it in mods.
This is a client resource pack, not a mod JAR or a datapack. Removing it
restores lower-priority colors; no world generation or saved blocks change.

CONTENTS
assets/minecraft/textures/colormap/grass.png
assets/minecraft/textures/colormap/foliage.png
assets/minecraft/textures/colormap/dry_foliage.png
All three are exact 256x256 RGB PNG lookup textures, opaque and unlabelled.
The palette uses muted meadow greens, cool boreal greens, ochre drylands,
deeper humid foliage and warm brown dry foliage. No photographic noise.

40 JSON water/fog palettes cover every biome in the current engine's
40-entry Standard palette. They use the existing WaterPalettes loader:
assets/minecraft/deepprimal/water_palette/<biome>.json
The minecraft namespace is intentional: these are current minecraft biome
IDs, not the future proposed deepprimal catalog. Deep Primal is required
for the JSON water palettes; vanilla can use the three vegetation PNGs.

LOOKUP CONTRACT
x = int((1 - temperature) * 255)
y = int((1 - temperature * downfall) * 255)
Inputs are the host's normalized/clamped biome values. Top-left is hot/wet;
bottom-left hot/dry; bottom-right cold. The y>=x triangle is reachable.
The unused triangle is safely extended with the corresponding wet color.
No annotations, borders, alpha masks, black unused area or axis labels are
baked into the lookup textures. Preview labels appear only in preview.png.

LIMITS AND VISUAL BEHAVIOR
These PNGs tint materials whose color providers consult the climate maps.
Explicit biome overrides, swamp/dark-forest modifiers, species-specific
fixed leaf tints (such as spruce/birch/mangrove), untinted blocks and already
colored textures may bypass or modify them. Pink cherry leaves are not
recolored by the generic foliage map. This pack does not add new providers.
Vegetation maps apply globally wherever Minecraft consults these resources,
including compatible vanilla worlds. This is not restricted to V7 worlds.

Water JSON values are RGB integers, with no alpha. Clarity is the existing
fog-distance multiplier, not a physical water-quality or sediment model.
Fog overrides require Deep Primal's water fog option. Water tint must also
be enabled where its settings gate it. Existing altitude tint, water
chemistry, depth attenuation, blending, lighting and renderer composition
still apply; swatches show authored inputs, not final scene pixels.
No OptiFine/Continuity water.png support or custom shader is assumed.
No changes to solver quantities, water textures, terrain geography,
geology materials, biome registries, species or engine implementation.
Vulkan and OpenGL use existing resource paths; neither has been visually
validated with this pack. No full mod build or in-game visual pass claimed.
Do not overwrite biome JSON to force colors: that would also replace biome
data and conflict with the frozen-worldgen requirements.

AUTHORING
authoring/palette.json is the editable color specification.
authoring/water-palettes.csv lists all emitted biome values.
authoring/preview.png is a labelled contact sheet, not a game screenshot.
authoring/engine-inputs.json records the inspected source snapshot and
loader contract. The source snapshot is not bundled as an entire project.
Use Python 3 + Pillow: python authoring/build_colormaps.py
This creates a sibling pack/ directory; it does not rewrite the ZIP.
The generator's source inputs and LookupCheck.java must stay together.
LookupCheck exercises the actual Minecraft 26.1.2 lookup classes with JDK25
and an existing legal copy of the client JAR; that JAR is not distributed.
Validation evidence is in authoring/validation.json and lookup-check.txt.
Colors and all included artwork were generated for this project, with no
vanilla image copying. You may use, modify and distribute these assets
with Deep Primal and your resource packs.
