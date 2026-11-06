# Deep Primal owned biomes and habitat tags

## Runtime ownership

Deep Primal's current four authored engine presets now use 40 biome registry entries in the **deepprimal** namespace. The entries cover 34 broad profiles from the [biome catalog](BIOME-CATALOG.md), including explicit climate/community variants. This is the first runtime integration of that catalog, not completion of all 118 profiles or their species, seasons, climate scenarios and visual acceptance.

- Biome definitions: `data/deepprimal/worldgen/biome/<profile>.json`.
- Habitat tags: `data/deepprimal/tags/worldgen/biome/<tag>.json`; all default members are Deep Primal IDs.
- Water presentation: `assets/deepprimal/deepprimal/water_palette/<profile>.json`, matching the existing resource reload listener's path.
- Names: `biome.deepprimal.<profile>` translations, with slash-separated variants represented by dots.
- Authored input: [owned_palette.json](../tools/biomes/owned_palette.json). Each entry records its stable slot, catalog parent, identity, climate presentation, colors, water palette and habitat membership.

These are independent, editable biome definitions, not aliases to Minecraft biome registry entries. They do not copy native carvers, placed-feature lists or mob spawn tables. The existing terrain engine still owns climate/geographic selection, caves, geology, soil and manual vegetation placement. Existing block assets used by that placement are unchanged; this work does not implement the planned complete species library.

Minecraft's registry codec, environment attribute names and normal-world preset hook remain platform APIs. Using those API fields is distinct from defining a biome under `minecraft:...` or populating Deep Primal habitat tags with Minecraft biomes. No Minecraft biome definitions or native biome tags are overwritten by this integration.

## Selection and compatibility

The four active profiles are `engine_standard`, `engine_deep`, `engine_deltas_standard` and `engine_deltas_deep`. Their existing 40-slot `deepprimal:regional_v1` palettes now point to owned definitions. Slot order, terrain seeds, density/material/channel revisions and geography thresholds are unchanged; broader catalog/climate placement remains a later integration.

Saved worlds retain their explicit biome-source palettes and existing chunk biome IDs. Both old 40-slot and 8-slot sources remain accepted by the unchanged codec. Old palettes are not silently rewritten on load, avoiding a forced identity change at existing chunk borders. Historical native-noise prototype profiles are also unchanged.

Runtime ocean/river behavior now checks `deepprimal:is_ocean` and `deepprimal:is_river`. `DeepPrimalBiomeTags` permits the native-tag bridge only for holders outside Deep Primal's namespace, retaining behavior in old native-biome saves and external worlds. A native ocean/river tag cannot override an owned biome's habitat classification. Existing native-biome water palette resources are retained solely for those older/external palettes.

## Habitat tags

Eight classification tags describe ocean, river, forest, wetland, shore, mountain, cave and cold habitats. The revision-9 riparian_mosaic tag adds eligibility for the coherent dry-bank vegetation pass. Five water-vegetation tags describe edge vegetation, emergent plants, submerged plants, floating plants and water-rooted trees. Members must resolve to real owned biome definitions.

Floating freshwater plants are limited to the implemented forested-swamp profile rather than being admitted to warm marine water. Other detailed substrate, salinity, flow and species-specific eligibility still requires the broader organic-system work; tag admission alone is not a completed ecological simulation.

## Stable palette mapping

The legacy ID column records the previous slot's source for save compatibility and review; it is not a runtime dependency of the new biome definition.

| Slot | Runtime ID | Catalog profile | Previous palette ID |
| --- | --- | --- | --- |
| 0 | `deepprimal:mixed_grass_prairie` | `deepprimal:mixed_grass_prairie` | `minecraft:plains` |
| 1 | `deepprimal:temperate_deciduous_forest` | `deepprimal:temperate_deciduous_forest` | `minecraft:forest` |
| 2 | `deepprimal:hot_desert` | `deepprimal:hot_desert` | `minecraft:desert` |
| 3 | `deepprimal:open_ocean` | `deepprimal:open_ocean` | `minecraft:ocean` |
| 4 | `deepprimal:closed_boreal_forest` | `deepprimal:closed_boreal_forest` | `minecraft:taiga` |
| 5 | `deepprimal:dry_tundra` | `deepprimal:dry_tundra` | `minecraft:snowy_plains` |
| 6 | `deepprimal:forested_swamp` | `deepprimal:forested_swamp` | `minecraft:swamp` |
| 7 | `deepprimal:alpine_barrens` | `deepprimal:alpine_barrens` | `minecraft:stony_peaks` |
| 8 | `deepprimal:open_savanna` | `deepprimal:open_savanna` | `minecraft:savanna` |
| 9 | `deepprimal:semidesert` | `deepprimal:semidesert` | `minecraft:badlands` |
| 10 | `deepprimal:tropical_rainforest` | `deepprimal:tropical_rainforest` | `minecraft:jungle` |
| 11 | `deepprimal:tropical_rainforest/bamboo` | `deepprimal:tropical_rainforest` | `minecraft:bamboo_jungle` |
| 12 | `deepprimal:seasonal_tropical_forest` | `deepprimal:seasonal_tropical_forest` | `minecraft:sparse_jungle` |
| 13 | `deepprimal:temperate_rainforest` | `deepprimal:temperate_rainforest` | `minecraft:dark_forest` |
| 14 | `deepprimal:cool_mixed_forest` | `deepprimal:cool_mixed_forest` | `minecraft:birch_forest` |
| 15 | `deepprimal:dry_boreal_woodland` | `deepprimal:dry_boreal_woodland` | `minecraft:old_growth_pine_taiga` |
| 16 | `deepprimal:open_boreal_woodland` | `deepprimal:open_boreal_woodland` | `minecraft:snowy_taiga` |
| 17 | `deepprimal:subalpine_woodland` | `deepprimal:subalpine_woodland` | `minecraft:grove` |
| 18 | `deepprimal:alpine_heath` | `deepprimal:alpine_heath` | `minecraft:snowy_slopes` |
| 19 | `deepprimal:icefield` | `deepprimal:icefield` | `minecraft:frozen_peaks` |
| 20 | `deepprimal:alpine_barrens/rocky_peaks` | `deepprimal:alpine_barrens` | `minecraft:jagged_peaks` |
| 21 | `deepprimal:montane_meadow` | `deepprimal:montane_meadow` | `minecraft:meadow` |
| 22 | `deepprimal:montane_forest` | `deepprimal:montane_forest` | `minecraft:cherry_grove` |
| 23 | `deepprimal:cold_steppe` | `deepprimal:cold_steppe` | `minecraft:windswept_hills` |
| 24 | `deepprimal:temperate_conifer_forest` | `deepprimal:temperate_conifer_forest` | `minecraft:windswept_forest` |
| 25 | `deepprimal:sandy_shore` | `deepprimal:sandy_shore` | `minecraft:beach` |
| 26 | `deepprimal:sandy_shore/polar` | `deepprimal:sandy_shore` | `minecraft:snowy_beach` |
| 27 | `deepprimal:rocky_shore` | `deepprimal:rocky_shore` | `minecraft:stony_shore` |
| 28 | `deepprimal:mangrove_forest` | `deepprimal:mangrove_forest` | `minecraft:mangrove_swamp` |
| 29 | `deepprimal:warm_ocean` | `deepprimal:warm_ocean` | `minecraft:warm_ocean` |
| 30 | `deepprimal:marine_shelf` | `deepprimal:marine_shelf` | `minecraft:lukewarm_ocean` |
| 31 | `deepprimal:cold_ocean` | `deepprimal:cold_ocean` | `minecraft:cold_ocean` |
| 32 | `deepprimal:cold_ocean/polar` | `deepprimal:cold_ocean` | `minecraft:frozen_ocean` |
| 33 | `deepprimal:deep_ocean_floor` | `deepprimal:deep_ocean_floor` | `minecraft:deep_ocean` |
| 34 | `deepprimal:deep_ocean_floor/cold` | `deepprimal:deep_ocean_floor` | `minecraft:deep_cold_ocean` |
| 35 | `deepprimal:deep_ocean_floor/polar` | `deepprimal:deep_ocean_floor` | `minecraft:deep_frozen_ocean` |
| 36 | `deepprimal:lowland_river` | `deepprimal:lowland_river` | `minecraft:river` |
| 37 | `deepprimal:mountain_river/cold` | `deepprimal:mountain_river` | `minecraft:frozen_river` |
| 38 | `deepprimal:wet_cave` | `deepprimal:wet_cave` | `minecraft:lush_caves` |
| 39 | `deepprimal:dry_cave` | `deepprimal:dry_cave` | `minecraft:dripstone_caves` |

Variant keys are climate or existing-community expressions. They are not additional broad profiles and do not expand the 118-profile planning roster.

## Generation and validation

Run `python tools/generate_owned_biomes.py` after editing the manifest, and `python tools/generate_owned_biomes.py --check` to verify checked-in assets. The generator rejects invalid namespaces, unknown catalog parents, duplicate/changed slots and unexpected active palette contents. It writes 40 biome definitions, 40 water palettes, 14 tags, the language additions and four active profiles.

[OwnedBiomeRegistryChecks.java](../tools/biomes/OwnedBiomeRegistryChecks.java) runs against actual Minecraft 26.1.2 codecs and registries, plus the production `RegionBiomeSource` and habitat helper. It decodes and freezes the biome registry, binds all owned tags, verifies colors and empty native feature/spawn tables, tests ocean/river behavior independent of native tags, samples the authored source under multiple seeds, checks serialization, preserves legacy palettes and rejects a missing biome reference.

Full NeoForge client/server world creation, saved-world reopening, GPU screenshots and biome/ecology acceptance must be reported separately from this focused registry check.
