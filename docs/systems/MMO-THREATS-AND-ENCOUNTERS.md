# MMO threats and encounters for Deep Primal

**Owner clarification:** October 29, 2026, America/Los_Angeles. Predators must include the broader hostile world of World of Warcraft and other MMORPGs: animals, monsters, intelligent opponents and villains that fit Deep Primal. **Status:** proposed design and open implementation gates. The owner confirmed that broader direction and removal of all vanilla mobs; the tiers, encounters, authoring controls and tuning below remain proposals.

This document extends [animal ecology](ANIMAL-ECOLOGY-AND-PREDATORS.md), [original factions and villains](HOSTILE-FACTIONS-AND-VILLAINS.md), [NPCs and quests](NPC-AND-QUEST-SYSTEM.md) and the [shared contracts](SHARED-SYSTEM-CONTRACTS.md). The animal roster remains useful. It supplies one part of the hostile world rather than defining every enemy as hungry wildlife. Reference-game facts belong in the research ledger; the engineering rules here are independently proposed Deep Primal behavior, not descriptions of proprietary MMO internals.

The [primary MMO reference study](MMO-THREAT-REFERENCE-STUDY.md) separates published patterns from these original proposals. See the [system index](README.md) and [phased roadmap](SYSTEMS-ROADMAP.md) for integration and open acceptance gates.

## 1. A hostile world with several causes

Use **threat** as the umbrella term. An ecological predator hunts or defends a territory. A territorial monster may protect a supernatural resource or nesting site. A sapient opponent chooses tactics to obtain food, tolls, territory, influence or knowledge. A recurring villain has a personal agenda, relationships and a story consequence. Identity, motive and current hostility remain separate: every member of a people or faction is not automatically an attack target.

Region design combines ordinary life with identifiable danger. Tracks, contested crossings, patrol standards, abandoned camps, warnings from travelers and a visible lair establish threats before combat. An enemy needs a place and purpose: where it rests, what it consumes or guards, how it reaches the encounter and why it pursues or stops. Fantasy creatures may use a declared supernatural resource instead of ordinary food; that resource is part of authored world logic, not an excuse for unlimited spawning.

Give each actor its own species/template ID, faction affiliation where applicable, instance identity, behavior profile, encounter role, reward provenance and saved revision. Species, faction and rank are independent axes: an animal can be an ordinary territorial threat or a rare named individual; a faction scout can be neutral during parley and hostile after a clearly authored escalation. Neither a skin nor an allegiance flag turns a vanilla mob into a native replacement.

## 2. Encounter tiers and readable roles

Ranks communicate the kind of interaction to expect. They do not automatically copy a player's level, enlarge hit regions or multiply health until a fight becomes long. Habitat and equipment progression define regional difficulty; previews disclose dangerous areas without assigning a global character cap.

| Tier | Purpose and behavior | Qualification requirement |
| --- | --- | --- |
| **Ambient or reactive** | Wildlife, workers and travelers that may flee, warn, defend or summon nearby help; no assumption of a combat reward | Ordinary approach, retreat and peaceful interaction work without accidental unavoidable aggression |
| **Standard** | One clear role and a small readable move set; an ambusher, raider, sentry or skirmisher | A prepared solo player can discover a tell and a dependable avoidance/interrupt route |
| **Veteran** | A practiced variant with a declared extra tactic or equipment interaction | New behavior is visible in silhouette/equipment/signs; extra health alone is insufficient |
| **Rare named** | A persistent identity, uncommon appearance or optional journal encounter | Rarity means limited availability; it does not silently mean a group requirement or a mandatory progression drop |
| **Captain** | Coordinates a bounded local squad, patrol or camp objective | Removing a signaler, cutting supplies, negotiating or separating the squad meaningfully changes the encounter |
| **Boss or story villain** | A distinctive encounter with preparation, stages, recovery windows and a consequence | Every mandatory story path has a viable solo resolution or authored alternative; optional group challenge is labeled before commitment |

Role labels include stalker, charger, defender, ranged harrier, support caller, controller and engineer. A squad combines a small selected set. Cap simultaneous committed attacks and control effects; a support caller cannot recursively summon other callers into an unbounded wave. Controllers use short, escapable restrictions with diminishing or explicitly bounded repeat application. Avoid unavoidable chains of knockback, immobilization and damage.

Fantasy abilities follow a shared visual language. A planted stance and ground mark can announce a rush; raised equipment and an audible buildup can announce a ranged attack; a visible channeler can explain a temporary barrier. Provide shape/motion/text equivalents for sound and color, plus reduced-flash settings. Warnings stay legible against block textures, rain, snow and darkness without making the entire screen glow.

## 3. Content forms that fit a living, editable world

| Form | How it enters the world | Outcome and limits |
| --- | --- | --- |
| **Habitat encounter** | Existing animal or monster territory; visible spoor and suitable refuge | Observe, avoid, deter, relocate where supported, or defeat; biological outputs retain one finite carcass budget |
| **Roaming patrol** | A finite squad follows a valid local route between declared sites | Scout, evade, ambush or parley; no teleport behind a player and no fresh squad on every chunk boundary |
| **Faction camp** | A validated footprint with supplies, roles, warnings and a bounded reinforcement source | Disrupt supplies, rescue, negotiate or challenge the captain; camp state persists and does not reset because the observer leaves |
| **Public event** | A clearly signaled, bounded regional situation with a start/stop policy | Personal contribution and optional cooperation; no forced party, idle proximity credit or copied physical loot |
| **Lair or ruin expedition** | A discoverable site in existing allowed geography, with a published entrance and escape/recovery rules | Optional encounters, shortcuts and clues; no forced rewrite of frozen world generation or player construction |
| **Campaign confrontation** | A quest state exposes a villain encounter or alternate resolution | Durable story flags and attributable completion; personal presentation cannot silently change a shared world's political state |

Begin with authored local sites and explicit encounter volumes. A future instanced arena is a separately scoped capability, not a hidden dependency for ordinary quests. Block changes in a shared ruin remain shared. Personal dialogue, journal interpretation and eligibility may differ without showing mutually incompatible solid geometry to colliding players. Global faction outcomes require an administrator-selected world-state policy and migration/recovery plan.

Campaigns can include scouting, food or material preparation, evidence gathering, negotiation, sabotage of encounter-owned devices, defense and combat. Avoid requiring the same kill quota at every step. A villain should affect routes and choices through credible actions, and leave discoverable traces between appearances. Recurrence needs a story reason and a distinct encounter identity; a defeated named villain is not silently the same person respawning every few minutes for rewards.

For a shared-world named actor, choose the published policy before activation: persistent death/retirement, escape followed by a later distinct appearance, or an explicitly repeatable challenge represented as such. A character's personal quest phase cannot resurrect the globally dead actor, revoke another character's completion or recreate its inventory. Late arrivals use the durable aftermath, a witness/alternate receiver or a separately identified reward-free replay. An optional replay likeness never reuses the live actor's resource/kill identity. A repeatable successor or lieutenant is a different actor and cannot repay a one-time villain grant merely by sharing its display name.

## 4. Server authority and fair commitment

The server owns hostility, perception, path decisions, attack selection, hit validation, injuries, deaths, accepted resolution events and rewards. Clients display predicted motion and cues; they cannot report a completed kill or skipped phase. Stable encounter/actor/event IDs survive reload and prevent duplicate materialization. Active attacks save or safely suspend through the declared lifecycle rather than replaying damage after reconnect.

An attack has an announced preparation, commitment and recovery. Validate line of sight and the swept attack volume at the relevant server time. An interrupted preparation cannot still apply its scheduled hit. Attacks declared as line attacks do not pass through newly placed solid cover; area effects disclose their actual shape and occlusion rule. Network reconciliation is bounded, and the supported latency envelope must be measured. A telegraph that exists only on the server is not useful counterplay.

Aggression uses current perception or a confidence-limited last-known position. A faction alarm has source, range, expiry and a finite recipient budget; it does not reveal a concealed player's exact coordinates to the region. Losing visibility permits searching, guarding or retreating. A player who withdraws through a safe exit can end an ordinary encounter; story arena commitment is disclosed in advance and has a recovery route.

Leashes prevent hostile actors from being dragged indefinitely into settlements. Return preserves identity and valid wounds under its declared recovery rule. No instant full heal exactly at an invisible boundary while attacks still reach outward. At the same time, repeatedly crossing the boundary must not reset rewards, duplicate supplies or freeze an attacker into a free loot dispenser. Display pursuit limits through terrain, warning behavior and journal guidance; test both retreat and repeated reengagement.

Death, surrender, dispersal, capture where supported and negotiated withdrawal are different accepted outcomes. Quests subscribe only to their allowed outcomes. A surrender animation does not produce both a live prisoner and a complete corpse. Character death and disconnect suspend or fail only the explicitly authored objectives; ordinary disconnect is not permission for an enemy to raid an absent player's property. Preserve the existing player death contract: keep-inventory is the default with an explicit configurable-off option, and existing cargo recovery/fatigue rules remain authoritative. This encounter design introduces no extra XP loss, equipment destruction or death punishment.

## 5. Buildable terrain is part of the encounter

An editable block world lets players construct walls, bridges, doors, towers and water channels. Treat sensible preparation as a valid solution. Enemy rules need bounded responses rather than invisible punishment whenever a player finds safe ground.

| Player/world situation | Proposed response | Forbidden shortcut |
| --- | --- | --- |
| Player climbs a narrow pillar | Ground-only actor seeks cover, guards a useful route, disengages or uses its already declared ranged ability | Granting a melee animal a surprise projectile, teleport or unlimited upward reach |
| Door or new wall blocks pursuit | Replan within budget; wait/search/retreat according to role; use doors only if the rig and published ability support it | Phasing through blocks, secretly opening locked owned doors, or deleting construction |
| Water trench or deep river | Respect swim capability, current, depth, stamina and reachable exits; retreat if the route is unsafe | Making every faction unit swim infinitely or forcing ecological predators onto impossible surfaces |
| Terrain is dug away mid-attack | Revalidate collision/support and interrupt unsafe motion; preserve normal fall and injury rules | Hovering until the animation ends or striking through a vanished floor |
| No legal path remains | Stop expensive retries, expose a path-failure state, then search locally or withdraw | Mining arbitrary tunnels to the target or force-loading distant chunks |
| Claimed farm or settlement lies on a patrol route | Validate protection and use a legal route; warn or suspend invalid placement | Relocating the squad inside a pen or damaging crops while its owner is offline |

Only a specifically authored breacher can damage encounter-owned breakable obstacles inside an authorized footprint. Player claims, containers, critical infrastructure and unapproved terrain remain protected by default. Any opt-in siege mode needs separate administrator controls, structural and fire budgets, restitution/recovery rules and player-visible consent. It is not the default consequence of enabling villains.

Boss movement needs adequate actual space for its body, turns and attacks. Pick or validate a site rather than compressing a large model into two-block tunnels. Failed validation blocks activation and explains the required clearance. Moving camps or adding ruins must respect existing frozen-generation decisions and local owner permissions.

## 6. Population, loot and campaign persistence

Separate ecological populations, faction reinforcement budgets, authored encounter instances and cosmetic background actors. A defeated patrol consumes its recorded squad; a replacement requires the declared recruitment/source/reset policy. Leaving and reentering a chunk does not mint new guards. A corpse, dropped equipment, quest grant and camp container have separate provenance and cannot each pay the same supply twice.

Animals retain their animated hunting anatomy and weight-aware joint carcass partition. Sapient equipment and salvage use their own inventory/material ownership; a visually equipped weapon is not automatically an extra item on top of a full cloned kit. Monsters and constructs declare whether they have biological remains, recoverable components or only authored rewards. A construct's exposed joint or ward device is an authored component weakpoint, not a fictional lung silently fed into the animal vital-hit table. Organic monsters need their own validated anatomy/material profile before anatomical hunting applies. Avoid inventing edible meat for every supernatural creature. Scavenging, capture, transformation and encounter cleanup transfer or retire a single material/reward identity.

Persistent character XP stays capped and non-spendable; profession and technology progression remain separate. Quest grants use the existing durable entitlement/claim protocol. Individual participation credit does not duplicate a shared carcass or permit remote retrieval from a camp chest. Public events publish eligible contributions such as accepted damage, interrupt, repair, rescue or objective interaction; tag-only farming and idle presence do not suffice.

Default physical simulation does not run during shutdown or become a free catch-up entitlement on load. No dormant faction rolls invisible dice to destroy a farm, murder livestock or complete a raid. An authored future public-event clock must declare pause, expiry, participant departure and recovery behavior. Calendar rewinds, content revisions and server restarts cannot reopen a claimed reward epoch. Admin preview uses isolated actors/progress and produces no production drops or XP.

## 7. Administration: build an encounter beside the quest

Extend **Administration → NPCs & Quests** with **Threats & Encounters** using the same asset picker, scoped permissions, draft revisions, validation, preview and publication workflow. A normal author chooses a template rather than writes a script.

The wizard asks for name/identity; approved native rig and skin; faction/motive and default disposition; tier and bounded ability preset; placement/home/route; squad composition and reinforcement cap; activation level/questline/world conditions; combat/peaceful outcomes and linked quest objectives; reward/reset policy; protected footprint; then test and publish. Read-only summaries explain character level versus profession/technology requirements. Changing appearance never changes saved identity.

The quest editor can link **Track**, **Observe**, **Disrupt**, **Rescue**, **Parley**, **Defend**, **Defeat** and **Resolve Threat** objectives only when the corresponding authoritative event adapter exists. Unsupported actions stay unavailable. A villain profile links dialogue, faction relationships, story appearances, encounter templates and resolution flags; it is not one entity forced to remain loaded across the whole map.

Permissions separately cover content drafts, skin import, combat presets, placement regions, economy grants, protected-terrain exceptions, world-state changes and publication. Delegated admins can author assigned campaigns without gaining arbitrary command execution. Two authors editing one encounter receive a revision conflict, with active-instance impact shown before publication. Retiring a villain preserves completed flags, owed rewards and a published path for active attempts.

## 8. A first region and bounded playable slice

**Working region: the Reedward Vale.** Farms occupy open terraces; a wet woodland holds the existing Brindle Prowler; a ruined crossing controls access to an upland quarry supply route. The proposed **Red Ledger Company** (`deepprimal:red_ledger`) runs a coercive toll operation, led by **Factor Veyra Sorn** (`deepprimal:veyra_sorn`). The companion roster owns these profiles. Names and exact site placement remain proposed content.

The first arc starts with genuine signs of prowler activity near a field. Track and deter/relocate where implemented, or defeat the animal; an explicitly authored safe training route can teach signs without fabricating a kill. The Red Ledger story then has three beats: investigate missing grain and a ledger discrepancy; secure the contested road through stealth, negotiation or combat; expose Sorn's fabricated debt and free the seized cargo, or confront her at the quarry tollhouse. Survey a patrol, speak with a dissenter and recover encounter-owned evidence along those routes. Missing cargo is authored encounter stock, never a surprise theft from an unloaded player farm. Essential seed, food and survival access remain available before combat or minimum-level quest gates.

Implement one predator, two faction roles—Red Ledger tallyguard and signal clerk—one small camp and one captain encounter before a regional campaign. Prove a player-built wall, a blocked doorway, a water crossing, an interrupted attack, a retreat and a reload. The same published chain must let an authorized second admin change a draft skin/dialogue/gate, reject unauthorized publication, preserve an existing player's attempt, and pay a full-inventory reward once. Sorn can first appear through dialogue and evidence; her larger confrontation and the recurring villain's wider campaign follow only after this local loop works.

## 9. Stable task register and acceptance

All tasks are open. The [systems roadmap](SYSTEMS-ROADMAP.md) assigns dependencies and future native evidence; this register does not alter engine-only worker scope or mark a gameplay milestone complete.

| ID | Deliverable | Required acceptance evidence |
| --- | --- | --- |
| **THR-01** | Native threat taxonomy, tiers, faction/disposition and versioned encounter schemas | Wildlife/monster/sapient role fixtures; invalid combinations rejected; no vanilla mob dependency; save/reload preserves identities and provenance |
| **THR-02** | Shared tell/commit/recovery and server combat adapter | Every chosen attack has an observable cue and avoidance/interrupt test; collision/occlusion and supported-latency native captures; bounded concurrent attacks and control effects |
| **THR-03** | Editable-terrain navigation and protection | Pillar, door, water, cliff, mid-attack block edit and no-path fixtures; bounded retries; zero unauthorized block/container/claim mutation |
| **THR-04** | One faction patrol/camp with finite recruitment and roles | Spawn/load/unload/restart races produce one squad; bounded alarms/reinforcements; accessible peaceful or avoidance route; no offline property attack |
| **THR-05** | Villain/campaign resolution and solo route | One coherent three-beat arc; accepted alternative outcome events; optional group label; no mandatory group-only survival progression; retirement preserves active attempts |
| **THR-06** | Admin threat/encounter wizard integrated with NPC questing | A delegated author creates, skins, gates, previews and submits without code; second-author conflict and permission revocation tests; previews cannot enter production economy |
| **THR-07** | Persistent rewards, participation and encounter recovery | Crash/retry/rejoin/revision/reset fixtures; one durable shared-world death/resolution plus one entitlement per eligible character claim key; finite material/equipment transfer; no replayed one-time villain reward |
| **THR-08** | Representative regional performance and playtest qualification | Dedicated-server/client traces for path/perception/attack/event queues; declared population/load/latency envelopes; tail timings and retained-work fairness; accessible tells and successful solo/co-op playthroughs |

Acceptance records name exact source/artifact hashes, world/configuration, selected actor and ability revisions, geometry edits, participant count, latency, fault points and measured budgets. Unit fixtures support rules; native captures establish real interaction; playtests establish legibility and usefulness. A polished roster or a passing document check does not establish any of those runtime outcomes.
