# Agriculture, fauna and questing implementation roadmap

**Planning checkpoint:** October 29, 2026. **Status:** all work below is proposed or unimplemented unless a future linked native receipt proves the named scope. Research documents being complete does not mark these milestones complete. No release date, numeric character cap or accepted engine grade is assigned.

## Product decisions and scope

The owner requests native agriculture inspired by TFC1.21.1 and the exact AgriCraft-ReReloaded fork; deeper original animal husbandry/ecology; a full replacement of vanilla mobs; original predator concepts with MMO-readable roles; and an easy admin NPC/skin/quest creator with level and questline restrictions and delegated authors.

This is a separately authorized design expansion. Existing engine-only builder scopes and their graded readiness gates are not rewritten by a gameplay roadmap. Portable definitions, event/identity contracts and engine query interfaces can be prepared without silently assigning farming, NPCs or all named catalogs to the current engine milestone. Actual implementation needs a bounded work claim and the relevant world/calendar/water/persistence interfaces. The earlier M0–M6 roadmap remains a historical implementation sequence; these feature milestones make the new scope explicit rather than marking M3/M5 complete.

## Phases and dependencies

| Phase | Work and stable IDs | Dependencies | Required exit evidence |
| --- | --- | --- | --- |
| **SYS-0 — shared foundation** | Versioned species/crop/NPC/quest schemas; accepted-event envelope; clock/units; definition versus instance state; MOB-01–02 inventory and progression mapping | Existing portable core and authority/storage/query contracts | Invalid data and missing capabilities rejected; exact-target spawn/dependency manifest; reviewed named/tamed migration dry run; no required vanilla-mob resource dead end |
| **SYS-1 — original population baseline** | MOB-03–04, AN-01–02, initial PRED-01: custom herbivore, managed livestock, solitary predator and pack prototype; native custom NPC shell | SYS-0, habitat/world queries, permitted-thread entity adapter and population budget | No denied vanilla mob through exercised spawn routes; players/technical entities unaffected; actor identity/mass/feed/water round-trips; readable predator behavior and bounded perception/path work |
| **SYS-2 — first farm** | AG-01–05 and AG-11–13 pilot: calendar crop lifecycle, soil nutrients, hand/furrow irrigation, harvest, seed saving and food storage | SYS-0, finite-water transaction authority, climate/soil queries and inventory lots | One ordinary farm works without vanilla mobs/bonemeal/villagers; closed irrigation/soil balances; one concurrent harvest outcome; no calendar/offline shortcut; bootstrap food/seed progression playtest |
| **SYS-3 — husbandry and hunting** | AN-03–04: trust, handling, pregnancy/eggs, growth, bounded traits, milk/wool/eggs, one carcass pipeline | SYS-1–2, shared nutrition/feed/material/ownership services | Birth and product receipts survive reload; exact disjoint carcass mass partition and remainders; optional catch-up disabled until separately qualified; manageable group care and useful winter preparation |
| **SYS-4 — usable admin questing** | NQ-01–05: native NPC identity/skin, wizard, dialogue, level/questline conditions, personal objectives and one-time rewards | SYS-0, permission/asset services, durable progression and item transaction interfaces; SYS-1–3 supply real example objectives | Ordinary authorized admin creates, skins, places, gates and publishes without code; second admin collaborates without silent overwrite; unauthorized requests fail; full-inventory/crash/retry gives one grant |
| **SYS-5 — selection and persistent ecology** | AG-06–10, PRED-02: breeding/analyzer/rotation/weed pressure, orchards/shelter, regional cohorts and habitat capacity | Accepted basic farm/husbandry, shelter/light/heat queries and population transitions | Deterministic parentage and no seed reroll; paid cuttings and no duplicate orchard harvest; bounded enclosure invalidation; no active/dormant population duplication or free physical coarse catch-up |
| **SYS-6 — campaigns and predator variety** | NQ-06–08, PRED-03: graph/outline campaigns, optional party/public contribution, repeats/definitions/migration; twelve original threat candidates refined | Accepted NPC reward and progress core, ecology and content/art review | Chain/branch/expiry/rollback and party join/leave/reconnect fixtures; preview isolated from economy; every chosen predator has native tell/counterplay/habitat and approved original presentation |
| **SYS-7 — world and release qualification** | MOB-05–06, AG-14, AN/PRED-04, NQ-09 and integrated scenarios | All selected release-scope features accepted independently | Existing-world migration crash/retry; seasonal farm/hunt/quest playthrough; multiplayer permissions/economy faults; exact packaged source identity; measured frame/tick/memory/network tails and visual acceptance |

NPC task aliases NQ-01–09 are defined below for integration tracking; the NPC specification retains its own staged list and acceptance details. References AG/AN/PRED/MOB point to the respective specifications and must not be renumbered by copying them into every audit. SYS phases can be split into smaller claims; they are dependencies, not deadlines or an instruction to stop other authorized work.

## NPC authoring task register

| ID | Deliverable | Acceptance |
| --- | --- | --- |
| NQ-01 | Stable native NPC template/instance schema, skin presets and instance lifecycle | Custom Deep Primal type; no vanilla villager; stable identity through skin/name changes; no duplicate after unload/respawn |
| NQ-02 | Create NPC and Create Quest wizards with keyboard-accessible preview | Name → skin → place → dialogue → quest → gates/rewards → test → publish without JSON or programming |
| NQ-03 | Server eligibility and objective engine | Character level and AND/OR questline prerequisites enforced at real boundaries; clear disclosed locks; bounded typed objective events |
| NQ-04 | Durable attempt/turn-in/reward ownership | One accepted completion/entitlement; partial inventory delivery and restart resume without duplicate items/XP or lost inputs |
| NQ-05 | Delegated editing, revisions and publication bundle | Scoped roles, revision conflict diff, live permission revocation and atomic reference-valid activation |
| NQ-06 | Campaign graph, dialogue branches and active-attempt migration | Cycle/missing-reference checks, branch exclusivity, retirement route and definition rollback without progress/reward rollback |
| NQ-07 | Optional party/public-event participation and phases | Personal eligibility and snapshotted contribution; no remote idle-member credit, copied physical loot or solo requirement |
| NQ-08 | Repeat periods, test/replay and admin support | Chosen reset clock visible; old period never reopens; test/replay awards disabled; typed repair with audit receipt |
| NQ-09 | Native usability, performance and abuse qualification | Multi-admin/full-inventory/crash/skin-failure/localization/large-campaign tests with real server clients and measured queues |

## Proposed first playable vertical slice

Build a bounded example rather than attempting every species/catalog/quest type together:

1. An original farmer NPC with an approved custom skin and a configurable minimum **character** level. An authorized second admin can edit an assigned draft.
2. A short talk introduction, then one self-cultivated harvest plus a consumptive delivery objective using native crop instances and accepted events.
3. A nearby original predator threatens the field/route. Track, deter/relocate where implemented, or defeat it; the quest accepts only a real supported resolution event. A safe training alternative is explicitly authored, not a fabricated kill.
4. Award configured character XP and an item entitlement once. Full inventory leaves an on-site claimable entitlement and still permits the story's selected completion boundary.
5. Add one livestock lineage with a finite feed/water store and a single useful product to test agriculture/animal integration. A joint product/carcass budget prevents collecting the same material twice.
6. Save, stop, reopen, switch definitions and retry duplicate actions. Demonstrate stable actors, crop lots, quest progress and reward receipts with no vanilla-mob dependency.

Species, NPC names, crop quantities and levels in the example exports are working data, not approved final content. Early skill and equipment paths must keep survival/food reachable; a minimum-level quest is not permission to gate the only starter seed or food source behind unreachable combat.

## Integrate with existing roadmaps

| Existing area | Added contract / work |
| --- | --- |
| M0/configuration and authoring | New versioned registries, validators, scoped permissions and migration responsibilities; compile through one definition pipeline |
| M2/terrain, biomes, water | Habitat, soil, climate/calendar, plant placement and water-account query/transfer interfaces; no farm/quest force-loading or changed frozen geography |
| M3/shared simulation and survival | Crop/soil life cycles, animals/food mass, accepted events, profession/character progression APIs and guide feedback |
| M5/integrated progression | Obtainability with no vanilla mobs, food/seed bootstrap, original resources, custom NPC chain and optional cooperation |
| M6/validation | Native lifecycle and packaged artifact, old-world safety, content/skin provenance, measured budgets and player/admin usability |
| Engineering handbook | Keep engine acceptance separate; link the new specs and shared contracts without converting research/proposals into completed code |

## Decisions still open

The direction is confirmed; final tuning remains open: named crop/livestock roster, seed loci and tradeoffs, precise physiology and growth calendars, whether optional catch-up is offered later, fantasy-to-naturalistic threat mix, numeric regional/quest levels, global cap/curve, quest reward budgets, neutral-NPC combat policy, encounter scaling, default quest reset clock and independent-game host/toolkit. Record decisions in their domain before using them as fixed shipping behavior.

Sources and detailed gates: [system index](README.md), [shared contracts](SHARED-SYSTEM-CONTRACTS.md), [agriculture](AGRICULTURE-SYSTEM.md), [animals/predators](ANIMAL-ECOLOGY-AND-PREDATORS.md), [NPCs/quests](NPC-AND-QUEST-SYSTEM.md), [mob replacement](VANILLA-MOB-REPLACEMENT.md), and the [main roadmap](../../roadmap.md).
