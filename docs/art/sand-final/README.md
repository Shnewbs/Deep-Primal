# Deep Primal final sand design

Final art revision of the initial sand tile: isotropic fine mineral grains, subdued contrast, no deliberate ripples or directional bands. The opaque 64x64 grayscale sand.png is shared by top, sides and bottom; apply the chosen sand/mineral color through the material tint provider. No separate masks are necessary for this single-material surface.

Generated with the built-in image-generation tool; the exact prompt is in prompt.txt. The high-resolution master is an authoring source, not the runtime texture. The exported image is grayscale, Lanczos downsampled, and corrected through four-texel bands to match opposing boundaries. The 64x64 export, 3x3 enlarged repeat and 8x8 native-resolution repeat were inspected. PNG round-trip and repeat-border equality pass; a second export reproduces sand.png byte-for-byte.

A single tile still repeats every 64 texels. This design reduces recognizable directional motifs; it does not implement stochastic material variants or cross-material blending. Actual game lighting, mipmap behavior and colormap application are not validated. This asset commit includes the final sand artwork; model/color-provider integration remains separate.

Reproduce with Node.js and ImageMagick from the repository root:

    node tools/ground-textures/export-sand.mjs docs/art/sand-final/master.png build/sand-final

The output directory contains sand.png, previews and validation.json. Review before copying sand.png into runtime resources. Existing ground assets are not overwritten by this exporter.
