# Agriculture, original populations and MMO questing

**Owner request and study:** October 29, 2026, Pacific. **Status:** researched specifications and proposed implementation roadmap. This package extends Deep Primal's design; it does not claim that farming, original populations or the quest editor have been implemented.

| System | Specification | Main contents |
| --- | --- | --- |
| Agriculture | [AGRICULTURE-SYSTEM.md](AGRICULTURE-SYSTEM.md) | Seasonal physiology, soil/NPK, rotations, finite irrigation, seed genetics/analysis, weeds, perennials, greenhouses, food lots and preservation |
| Animal ecology and original predators | [ANIMAL-ECOLOGY-AND-PREDATORS.md](ANIMAL-ECOLOGY-AND-PREDATORS.md) | Familiarity, husbandry, breeding, body condition, weight-based products, persistent populations, perception/territories/packs and twelve original predator candidates |
| Organic hunting | [HUNTING-SYSTEM.md](HUNTING-SYSTEM.md) | Living hunt loop, tracks and weather, animal senses, animated hit validation, injury/recovery, single-carcass processing, ecology and phased acceptance |
| MMO threats and encounters | [MMO-THREATS-AND-ENCOUNTERS.md](MMO-THREATS-AND-ENCOUNTERS.md) | Wildlife, monsters, intelligent enemies, rare/elite/captain/boss roles, editable terrain, regional events and encounter authoring |
| Hostile factions and villains | [HOSTILE-FACTIONS-AND-VILLAINS.md](HOSTILE-FACTIONS-AND-VILLAINS.md) | Five original faction candidates, unit roles, named antagonists and linked story arcs |
| NPCs and questing | [NPC-AND-QUEST-SYSTEM.md](NPC-AND-QUEST-SYSTEM.md) | Easy admin wizard, custom skins, questline editor, level/prerequisite gates, delegated authors, publish/rollback, party credit and durable rewards |
| Vanilla mob replacement | [VANILLA-MOB-REPLACEMENT.md](VANILLA-MOB-REPLACEMENT.md) | Firm removal direction, every creation route, saved pets/mounts/cargo, custom NPC identity, resource/progression replacements and migration gates |
| Integration | [SHARED-SYSTEM-CONTRACTS.md](SHARED-SYSTEM-CONTRACTS.md) | One calendar/authority, stable revisions, accepted events, finite quantities and cross-system transactions |
| Implementation order | [SYSTEMS-ROADMAP.md](SYSTEMS-ROADMAP.md) | Phased work, dependencies, starter vertical slice, stable task IDs and required evidence |

The exact supplied [Vivoxti AgriCraft fork](https://github.com/Vivoxti/AgriCraft-ReReloaded) was inspected and pinned. TFC research targets Minecraft 1.21.1: the agriculture study pins a 1.21.x source commit, while animal research pins release v4.2.12. Those are distinct inspected snapshots, not one combined latest-release claim. Other references include Alex's Mobs, Naturalist, Better Animals Plus and Animania; their different version/license boundaries are retained rather than implied to be install-compatible.

MMORPG comparisons use official World of Warcraft, Final Fantasy XIV and Guild Wars 2 material. Citizens2, EasyNPC, FTB Quests, BetonQuest and AzerothCore supply additional authoring/state-model comparisons. They are references for independently implemented Deep Primal systems, not required dependencies or a license to copy their art/code.

Detailed source facts, exact versions, inspected dates and uncertainties: [agriculture research ledger](AGRICULTURE-RESEARCH.md), [animal bibliography](ANIMAL-ECOLOGY-AND-PREDATORS.md#13-sources-and-inspection-limits), [MMORPG comparison](NPC-AND-QUEST-SYSTEM.md#1-lessons-from-primary-mmorpg-sources), [NPC/tool study](NPC-QUEST-REFERENCE-STUDY.md), and the [source index](SOURCES.md).

Confirmed owner choices are distinguished from proposed models, candidate species, example numbers and unrun gates. All vanilla mobs are to be removed; older general coexistence/re-enable language is superseded for mobs. Native NPCs use Deep Primal entity IDs. Existing character XP, calendar, physical water, mass, solo/cooperation and local-storage restrictions remain in force. The broader [engineering handbook](../handbook/README.md) retains engine foundations and historical evidence.

## Expanded owner direction: MMO enemies and villains

Predators are one part of a broader original threat roster. The owner explicitly requests WoW-like predators, villains and other MMO patterns that fit Deep Primal and Minecraft. Regions therefore need wildlife, fantastical monsters, intelligent hostile groups, camps/patrols, recognizable elite roles and named story antagonists. The twelve animal candidates remain the wildlife layer; they are not the entire hostile replacement roster. Five proposed factions and their villains are candidate content, not approved final names or required release scope.

Read the [primary MMO threat study](MMO-THREAT-REFERENCE-STUDY.md), [encounter framework](MMO-THREATS-AND-ENCOUNTERS.md) and [faction/villain concepts](HOSTILE-FACTIONS-AND-VILLAINS.md) together. The design adapts encounter readability, motives and regional storytelling to editable blocks, finite resources, protected builds, optional cooperation and persistent quest outcomes. No Warcraft creature, faction, art or story is copied. [THR-01–08](MMO-THREATS-AND-ENCOUNTERS.md) extends the feature roadmap; implementation and native acceptance remain open.
