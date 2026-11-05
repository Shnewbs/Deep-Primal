# Natural type catalog — named planning lists

Planning update: 2026-10-20. Base commit: `7297ca1850ec23a624cd43e400f359b520d881a7`.

**Owner direction: fill out the actual named environmental lists while keeping implementation deferred.** The earlier [coverage inventory](ENVIRONMENTAL-COVERAGE-INVENTORY.md) identifies domains and questions; these linked catalogs now supply named entries.

## Named lists

| Catalog | Named entries | Contents |
| --- | ---: | --- |
| [Geology, sediment, soil and clay](NATURAL-TYPES-GEOLOGY.md) | 311 | Rocks, minerals/ores, loose deposits, texture classes, soil orders, horizons, modifiers/structure, clay minerals and grades. |
| [Plants, fungi, wood and organic matter](NATURAL-TYPES-BIOLOGY.md) | 502 | Retained and expanded woody groups, grasses, herbs, crops, fungi, cryptogams, aquatic cover, organic matter, wood forms and animal materials. |
| [Water, ice, atmosphere and environments](NATURAL-TYPES-ENVIRONMENTS.md) | 200 | Water classes, frozen forms, volcanic/geothermal forms, air/particles, habitats/landforms and processes. |
| **Total named entries** | **1013** | **28 sections across the three catalogs.** |

An entry may be a material type, umbrella group, subtype, mixture, anatomical part, state, form, habitat or process. The **Kind** column states which. Parent/child rows and cross-context references overlap; 1013 does not mean 1013 distinct species, substances, blocks or approved features.

## Preservation and expansion

- All **44 original rock/material rows** and **43 original tree/plant rows** are retained with their original names and function text.
- Grouped original entries are preserved while named splits are added: mudstone/claystone; coal ranks; ironstone/banded iron; eclogite/granulite; poplar/aspen; apple/pear/cherry/plum; redwood/sequoia; mango/cacao.
- Soil now has separate named lists for **all twelve USDA texture classes**, **all twelve USDA soil orders**, horizons, modifiers and structure. These are different classification axes.
- Clay now separates mineral groups/members, mixed-layer material, **the six USGS commodity classes**, and overlapping ceramic working grades/preparations.
- Plant and fungus entries use named groups/representatives where a global species list would be open-ended. Names do not automatically imply edibility, medicinal behavior, growth properties or accepted regional distribution.
- Existing detailed requirements stay in the [design reference](design-reference.md) and [owner decisions](design-decisions-2026-10-18.md). New names are planning candidates until their representation and scope are settled.

## Related biome plan

The [biome plan](BIOME-PLAN.md), [named biome roster](BIOME-CATALOG.md), and [variants/coverage crosswalk](BIOME-VARIANTS-AND-COVERAGE.md) connect these material/biological names to **118 proposed biome profiles**, **54 community variants** and **63 local layers**. These are a separate planning axis and do not increase the 1,013 named natural-type row count. All 62 existing habitat rows are explicitly reconciled; environmental implementation remains deferred. The [glacial landscape expansion](GLACIAL-LANDSCAPES.md) accounts for the latest 3 community variants and 8 local layers.

## Common-name, alias and overlap rules

| Label or overlap | Planned treatment |
| --- | --- |
| Dolomite | Retain original label; distinguish the dolomite mineral from dolostone rock. |
| Gypsum / rock salt | Separate mineral identity from massive rock/deposit assemblage. |
| Dolerite / diabase | Resolve as regional terminology/alias where appropriate; avoid duplicate material output. |
| Limestone / chalk / travertine / coquina | Retain broad family and named varieties with explicit relationships. |
| Chert / flint / chalcedony / quartz varieties | Resolve mineral, rock and variety relationships before assigning resources. |
| Lignite / bituminous / anthracite | Coal-rank children of the original grouped entry; subbituminous added as a candidate. |
| Cedar / redcedar / cypress / juniper | Disambiguate true cedars, Thuja, Chamaecyparis, junipers and cypresses. |
| Tulip tree / yellow-poplar | Common-name aliases; distinct from true poplars. |
| Acacia / wattle / Vachellia / Senegalia | Preserve original label; resolve regional accepted taxa and historical naming. |
| Mesquite / Prosopis relatives | Preserve original label; accepted-name and regional species mapping remains open. |
| Linden / lime tree | Distinct from citrus lime and lime-processing material. |
| Plantain herb / plantain banana | Distinct plants despite shared common name. |
| Hornwort aquatic plant / hornwort bryophyte | Distinct groups; names must include context. |
| Irish moss / reindeer moss | Red algae / lichen common-name usages, not bryophyte moss. |
| Bamboo / palms / tree ferns / banana | Separate botanical and anatomical treatment from ordinary wood-bearing trees. |
| Clay / clay mineral / clay body | Texture-size class, mineral composition and working mixture are separate. |
| Organic soil / peat / compost / humus-rich matter | Separate profiles, deposits, managed mixtures and soil-organic concepts. |
| Freshwater / clear water / safe water | Salinity, optical appearance and drinking quality are separate dimensions. |
| Wet/frozen/rotten/charred forms | Usually states of a source material, rather than new material identities. |
| Species/group versus guild/form | A decomposer guild, bracket form or grass growth form is not a species. |

## What is complete and what remains open

The finite classification lists explicitly labeled above are complete at that stated level. The named catalogs substantially expand environmental coverage, but are **not a claim to enumerate every rock name, mineral species, tree species, plant, fungus, cultivar, regional name or ecological community on Earth**.

The current planning task supplies names and broad role/relationship notes. These remain separate from:

- choosing individual versus grouped game representations and resolving aliases;
- accepted scientific names, regional representatives and distribution maps;
- fully researched physical/chemical/ecological property sheets;
- numerical values, process rates and gameplay balance;
- implementation scheduling, coding, asset creation or feature-completion evidence.

The [163-entry coverage review](ENVIRONMENTAL-COVERAGE-INVENTORY.md) remains the cross-check for missing families, habitats, states and natural processes. A name can be listed while its representation and behavior are still undecided. Do not mark a review complete merely because a catalog row exists.

## Maintenance rules

1. Add a missing name in the appropriate catalog, label its kind and provenance, and connect it to an existing coverage-review ID.
2. Preserve original requirements. Additional candidates can be grouped, deferred or excluded with a written rationale.
3. Link parent/child/alias relationships; do not inflate the roster by counting the same identity in several states or contexts.
4. Keep counts as named-row counts. Report species counts or unique-material counts only after canonical mapping supports them.
5. Keep environmental implementation deferred until requested. Update planning documents independently of implementation milestones.
