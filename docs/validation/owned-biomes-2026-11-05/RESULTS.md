# Owned-biome integration validation

Source baseline: `0bb193a4b29c0a83b81580836ac5970f790df0a3`.

## Passed

- `python tools/generate_owned_biomes.py --check`: 40 independent biome definitions, 40 owned water palettes, 13 owned habitat tags, four active engine palettes and language output match their authored manifest (98 generated files).
- All 40 runtime IDs and all tag members use the Deep Primal namespace; each broad profile maps to the existing planning catalog.
- Six production/validation Java sources compile against the cached Minecraft 26.1.2 / NeoForge 26.1.2.122 APIs using Java 25.0.4.1; no missing classpath entries. Existing compiled project/module dependencies supply unchanged types.
- The focused native runner uses the actual unmodified Minecraft 26.1.2 client JAR, production `RegionBiomeSource`, production `DeepPrimalBiomeTags` and the existing pure engine-core dependency. It does not use loader/client stand-ins.
- `OWNED_BIOME_NATIVE_PASS biomes=40 tags=13 profiles=4 checks=4055`, exit 0 with JVM verification enabled.
- Native biome codecs decode and round-trip all 40 definitions; the biome registry freezes with all owned references resolved; all 13 tags bind successfully.
- Native checks verify authored water colors and temperature, empty inherited feature/mob tables, 3,468 source selections across three seeds/four profiles, source serialization and both old 40-slot and 8-slot palette decoding.
- Runtime ocean/river identity works when no native tags contain the owned biomes. Deliberately adding an owned prairie to the native ocean/river tags does not override its owned habitat identity. Native-biome ocean/river holders retain the explicit compatibility bridge.
- A palette containing an unknown owned biome is rejected by the actual codec/constructor path.

The standalone harness matches registry startup order by binding tags before freeze. The color assertion compares RGB components because the game getter returns an alpha-bearing color.

## Limits

These checks validate registry assets and the bounded biome-source/identity integration. They are not a full Gradle build, NeoForge client/server startup, native world creation or save reopening, rendered biome/water acceptance, complete species/ecology integration or performance benchmark. No new JAR or release is asserted.

Existing saved palette IDs, legacy native-noise profiles, Nether/End definitions and terrain/material/channel revision fields are retained. The runtime ownership change applies to the four authored engine profiles described in [BIOME-IMPLEMENTATION.md](../../BIOME-IMPLEMENTATION.md).
