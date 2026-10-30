# Vanilla mob replacement and original population policy

**Owner decision:** October 29, 2026. **Status:** confirmed product direction, proposed implementation contract; this documentation task changes no running world or entity. This decision supersedes the older `vanilla.mobs` coexist/re-enable proposal in the detailed design. All vanilla mobs are replaced by Deep Primal fauna, threats and authored NPCs.

## 1. What removal means

The finished Deep Primal ruleset has no active vanilla passive, neutral, hostile, ambient, aquatic, utility, villager/trader/illager or boss mobs. This applies to every retained dimension and every acquisition/spawn route, rather than only Overworld natural spawning. Sheep, fish and villagers are included alongside zombies, creepers and bosses. Farming must not quietly depend on vanilla bees, animal farms or villager trades.

Players, dropped item entities, projectiles, vehicles, display entities and other non-mob infrastructure are a different classification. A blanket deletion of every `minecraft:*` entity would break the host. Resolve the exact target registry into an explicit classification manifest; identify multipart bosses and technical living entities separately. Registries can remain loaded for host serialization even when their mob types are forbidden in active gameplay. Never unregister saved IDs as a substitute for a migration.

Original mobs use stable Deep Primal type/species identities, authored behavior, permitted art and replacement drops. Reusing an appropriate host movement/AI base class does not authorize spawning its vanilla species under a renamed label. A human-looking quest giver is a Deep Primal NPC presentation, not a disguised vanilla villager dependency. The independent-game host uses the same species/policy definitions through its own adapter.

The user decision concerns vanilla mobs. Third-party entity types require explicit compatibility classification; they are not automatically erased merely because they come from another namespace. An adapter must not let a third-party spawner reintroduce a denied vanilla type. Missing or unknown third-party semantics are reported and left outside claimed support until reviewed. Production configuration cannot restore vanilla mobs through a general content toggle or an admin NPC skin/model selector.

## 2. Why one spawn-list edit cannot satisfy the decision

The NeoForge **1.21–1.21.1** biome-modifier documentation supplies a mechanism to remove biome spawn entries. Its API scope does not establish coverage of every entity creation route. The inspected `MobSpawnEvent` source explicitly excludes custom-rule spawners from one placement check, and `EntityJoinLevelEvent` distinguishes disk loading and warns against premature chunk interaction. These references justify layered policy enforcement; they are not a compiled adapter for Deep Primal's later target. [VM-S01–03]

| Admission route | Required policy behavior | Replacement/diagnostic behavior |
| --- | --- | --- |
| Natural biome and chunk-generation spawns | Remove denied entries and reject denied types at the real creation boundary | Native habitat/population planner supplies approved custom species |
| Structures, scripted encounters, raids and patrols | Validate every spawned member and any late reinforcement | Replace the encounter definition; never run an empty vanilla encounter as if complete |
| Ordinary/trial/custom spawners and spawn eggs | Denied mob cannot materialize, including altered spawner data | Disable/remove unreachable acquisition routes or show an explicit unsupported-content reason |
| Commands, command blocks, datapack functions and admin tools | A command does not bypass production entity policy | NPC/encounter authoring lists only permitted templates; typed error reports denied type |
| Breeding, hatching, splitting, conversion, infection and transformation | Validate every resulting type and product | New species lifecycle uses its own approved offspring/transformation rules |
| Buckets, bottles, containers and modded capture/release | Validate stored entity identity before release | Preserve or migrate the captured record; do not consume the container and silently lose it |
| Summoning rituals, lightning, traps, reinforcements and event rewards | Audit indirect creation paths and supporting entity groups | Substitute original event/recipe content or disable the unsupported event |
| Disk load, dimension transfer, passenger restore and reconnect | Apply reviewed migration/admission rules to persisted entities | No duplicate rider, orphan leash, lost inventory or repeated migration |
| External mod API creation | Final server admission remains authoritative | Identify the caller and rejected species without unbounded log spam |

The list is a required audit matrix, not a claim that each named path exists unchanged in every Minecraft release. At implementation, pin the exact NeoForge/Minecraft target and trace actual call sites. Static searches supplement executable witnesses; they do not prove complete enforcement. No synchronous chunk generation, network request or unbounded world scan belongs inside an entity admission callback.

## 3. Existing worlds: remove active vanilla populations without destroying records

For a new world, enable the complete policy only alongside a validated starter ecology and resource/progression replacement. For an existing world, first produce a dry-run inventory: type, UUID, dimension, tame/name status, owner, inventory/equipment, passengers, leash/hitch relations, trading/quest references and affected progression. Estimate work by region and retain a recoverable source snapshot.

The migration manifest records source schema, policy revision, target type or preserved-record disposition, and idempotent migration ID. Named/tamed animals, mounts, traders and inventory-bearing entities need explicit mappings. An approved conversion may retain identity and related ownership only where the target schema supports it. Unsupported records enter durable non-active archival quarantine, with inspect/export and later reviewed conversion. They do not tick, trade, breed, drop items or generate kill/quest credit. They must not be silently deleted, automatically slaughtered, converted into loot or repeatedly reprocessed on load.

Commit target creation and source retirement as one recoverable logical outcome. Fence late old-world callbacks and cross-dimension passenger restoration. Rollback of a partially attempted migration cannot create both a source animal and its replacement, or duplicate saddle/cargo/body mass. If preservation cannot complete, fail the bounded migration action with its source bytes intact and an actionable operator report. Migration feasibility is an implementation gate; this document does not execute or approve a destructive world conversion.

## 4. Replace the functions, not only the silhouettes

| Removed dependency | Deep Primal requirement | Initial design direction |
| --- | --- | --- |
| Passive farm animals and wild game | Food, fat, hide, bone, fiber, milk/eggs and ecological grazing | Weight-based original livestock/wildlife; [animal system](ANIMAL-ECOLOGY-AND-PREDATORS.md) |
| Vanilla bees and pollination assumptions | Crop fertility/pollination where designed | Native pollinator species or bounded habitat service; no invisible required bee entity |
| Vanilla merchants/villagers and services | Settlements, introductions, professions, trade and quests | Authored NPC roles and separate native trade/economy contract; [NPC system](NPC-AND-QUEST-SYSTEM.md) |
| Night monster/resource farms | Threats, combat practice, specialty resources and progression | Habitat/encounter-based predator and original hostile families; no fixed vanilla drop farms |
| Vanilla mounts and pack animals | Travel, hauling, hitching and animal-powered work | Approved domesticated species with training, ownership, mass and stamina interfaces |
| Boss drops and dimension/progression triggers | Obtainable required recipes, access and endgame loops | Independently authored progression graph; exact bosses/dimension access remain design decisions |
| Fishing/aquatic mobs and loot shortcuts | Living aquatic populations, tackle/traps and species journal | Native species identities and individual catch mass; no simultaneous creature plus loot-table duplicate |
| Vanilla XP orbs and repair/enchant consumers | Persistent capped character progression | Existing no-vanilla-XP rule remains mandatory; quests/combat use the character/profession interfaces |

A resource-obtainability audit covers recipes, repairs, food, medicine abstractions, clothing, building, transport, quests, advancements and portals. A recipe must not require an item whose only permitted source was removed. Mark any intentional temporary unavailability clearly; do not claim a playable replacement milestone with a progression dead end. Quest objectives and NPC dialogue must never send players to a forbidden mob.

## 5. Original threats with MMO readability

The [predator roster](ANIMAL-ECOLOGY-AND-PREDATORS.md) supplies candidate species and combat roles. The owner has chosen the direction, not approved every proposed creature name or balance value. Use recognizable behavior roles: territorial bruiser, pursuit pack, ambush stalker, river hazard, burrow defender, flying scavenger and rare apex. Combine habitat, footprints/calls, posture and attack wind-up so danger can be understood before damage.

Naturalistic predators hunt for modeled needs and defend territory; they do not all behave as permanent player-seeking enemies. Fantasy threats may use encounter rules, with explicit origin, limits and reward model. Nearby ecology and encounters share admission budgets without pretending a scripted elite was naturally born. Spawned event actors, test actors and breeder offspring need provenance so they cannot be laundered into unrestricted kill/butchery rewards.

Regional level bands, elite variants and quest unlocks are proposed content tools. Do not secretly scale every animal to the highest player or turn a settled starter farm into an unwinnable fight because a veteran passed nearby. Define per-encounter scaling, participant snapshot and reward attribution before activation. Stable settlements, safe entry routes, leash/return behavior, obstacle handling and protection from dragged elites need real multiplayer fixtures.

No copyrighted MMO creature names, lore, skins, models, animations, sounds or dialogue are adopted by analogy. Original art follows the existing tile policy where applicable; character skins and entity models need their own dimension/rig policy rather than forcing a 32px block-texture rule onto every asset.

## 6. Population authority and visibility

One server ecology service owns active individuals, dormant individuals, reproduction reservations and any regional abundance summaries. Decide which quantities are authoritative before mixing aggregates with entities. Promote/demote an individual through an ID-based ownership transition; never count it in both active and dormant populations. Named/tamed/quest-critical individuals remain persistent and are not generic disposable spawn budget.

Render distance and LOD cannot authorize births, kill actors or refill a herd. Far visibility is presentation; loaded simulation, dormant state and any future regional catch-up are separately bounded policies. Calendar pause and no real-world offline simulation remain the default project contract. Habitat shifts from drought, farming or player construction invalidate suitable territories through bounded dirty-region work rather than scanning the world every tick.

Admission failures are observable by reason: forbidden type, habitat invalid, local/global population budget, encounter conflict, incomplete world data or migration pending. Diagnostics need counts and bounded examples rather than unrestricted per-entity logs. Rejected birth/encounter requests retain or release their reservations exactly once.

## 7. Implementation and acceptance roadmap

| ID | Work package | Required exit evidence |
| --- | --- | --- |
| MOB-01 | Exact-target entity/type and creation-route manifest | Every vanilla mob classified; players/technical entities preserved; all supported dimensions and route families traced |
| MOB-02 | Resource/progression and existing-world dry run | Obtainability graph has no required forbidden-mob edge; named/tamed/cargo/passenger records have reviewed dispositions |
| MOB-03 | Portable population policy and native admission adapter | Each admission route rejects denied types; no command/spawner/container bypass; no join-callback chunk deadlock |
| MOB-04 | Minimal original ecology and NPC population | Starter herbivore, livestock, predator and custom NPC function with persistence and no vanilla species dependency |
| MOB-05 | Transactional migration and restart | Crash/retry/duplicate delivery and dimension transfer preserve one logical actor and its contents; quarantined records stay non-active |
| MOB-06 | Broad replacement certification | Entire registry/route/dimension matrix, long-run spawn audit, multiplayer grief/encounter cases, native performance and visual acceptance |

Pass conditions include zero accepted forbidden-mob admissions in the exercised matrix; no silent entity/item loss; exact actor/offspring/carcass ownership; no repeated event or kill rewards; and useful custom populations under their declared budgets. Numeric timing/population budgets are selected on reference hardware before acceptance, not invented by this research. All six milestones are unimplemented in this documentation task. Dependencies and the full feature order are in [SYSTEMS-ROADMAP.md](SYSTEMS-ROADMAP.md).

## Primary reference evidence

Inspected October 29, 2026. These establish narrow host mechanisms. The Deep Primal policy, migration design and route matrix above are our proposed engineering response.

- **VM-S01:** [NeoForge biome modifiers, version1.21–1.21.1](https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/) — data-driven removal from biome spawn lists, not universal entity removal.
- **VM-S02:** [NeoForge EntityJoinLevelEvent source, branch 1.21.1](https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/event/entity/EntityJoinLevelEvent.java), inspected blob `2428e13c14b495ad17b65660c672ff4ab399acb7` — admission and disk-load distinction; early chunk interaction warning. Revalidate the exact later host target before implementation.
- **VM-S03:** [NeoForge MobSpawnEvent source, branch 1.21.1](https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/event/entity/living/MobSpawnEvent.java), inspected blob `5362dcc35164def51a8a94be866eca2c6f25af46` — placement-check scope excludes custom-rule spawners. Source comments about other stages require actual target call-site inspection.
- **VM-S04:** [Minecraft official command guide](https://www.minecraft.net/en-us/article/minecraft-commands) — documents a summon route independently of biome configuration. It is a usage reference, not proof of all Java creation paths.

Project constraints: [owner decisions](../design-decisions-2026-10-18.md), [original detailed design](../design-reference.md), [handbook architecture](../handbook/ARCHITECTURE-AUTHORING.md).
