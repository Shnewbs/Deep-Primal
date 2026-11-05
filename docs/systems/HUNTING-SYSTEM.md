# Deep Primal — organic hunting system

**Edition:** November 2, 2026. **Status:** detailed design and implementation proposal; no runtime implementation, balancing, native validation, or release is claimed here. **Scope:** original wildlife hunting within the Deep Primal ecosystem, from observation to material recovery. Numerical examples are tunable game values, not biological measurements or final balance.

## 1. Player promise and boundaries

Hunting is a relationship with a living region. The player reads habitat, weather, tracks, calls, travel corridors and animal condition; chooses a safe approach; makes a consequential attempt; follows a recoverable trail; and decides how to use the single physical carcass. The population remembers harvest and disturbance. A successful hunt can feed a household, support leather/textile/crafting chains, or complete an authored quest. Watching, photographing/sketching in the journal, deterring, rescuing and conservation work are equally valid ways to learn a species.

This system extends [animal ecology and predators](ANIMAL-ECOLOGY-AND-PREDATORS.md), [shared contracts](SHARED-SYSTEM-CONTRACTS.md), the [systems roadmap](SYSTEMS-ROADMAP.md), [design decisions](../design-decisions-2026-10-18.md), and [MMO threats](MMO-THREATS-AND-ENCOUNTERS.md). Those documents own population authority, time, mass accounting, accepted events and threat taxonomy. This one owns the hunt interaction, sign production/interpretation, shot resolution, recovery and hunting-facing UI. It does not turn authored villains into biological deer, assume real wildlife values apply to fictional species, or silently make every animal hostile.

**Confirmed owner direction:** primitive weapons through hunting bows, visible back quiver, material-dependent points, localized animal hits, carcass processing, weight-based yields for every animal, First Aid-style body regions and restrained TFC/PrimalCore presentation. Bow modifications and a compound bow remain proposals. A late hunting rifle is tentative and optional; this spec does not commit to it. Deep Primal ultimately replaces vanilla mobs; initial hunting progression must work with original species and cannot require a vanilla cow, sheep, deer or skeleton drop.

### Design rules

1. The server owns detection, hit pose, injury, death, carcass, inventory and quest evidence. The client may predict animation and display observations, never award a kill.
2. One animal is one persistent identity and at most one death/carcass. A carcass has one conserved, claimable physical budget even in a party.
3. Animal perception is bounded and uncertain; neither prey nor predators know a player's location through terrain without a current valid stimulus.
4. Habitat, population pressure and calendar affect opportunity. Chunk reloading or repeated logout cannot reset a herd or mint more animals.
5. No mandatory blood or explicit gore. Surface signs, posture and tracking cues carry the gameplay; visual intensity is configurable.
6. The player can identify a failed attempt and make a new decision. Hidden rolls cannot force an untraceable wound or an unavoidable chase.
7. A hunt should be enjoyable at the ordinary game scale. Plausibility guides rules; literal real-world travel times and anatomy are not required.

## 2. The playable hunt loop

| Stage | Player sees and does | Server-side outcome | Alternative or failure recovery |
| --- | --- | --- | --- |
| Survey | Observe habitat, forage, paths, water, prints, browse, feathers, calls and old beds | Samples a bounded sign index tied to actual animal/cohort activity | A sparse region still has ecology journal clues, neighboring habitat hints and non-kill progression |
| Identify | Compare species, group size, freshness and travel direction; inspect through a spyglass/field journal if available | Knowledge level reveals confidence and uncertainty, never exact distant coordinates | Wrong sign interpretation costs time, not a fake guaranteed spawn |
| Plan | Check wind, light, weather, cover, access, escape route, equipment, haul capacity and season | Environmental snapshot affects perception and shot; no forced new chunk generation | Choose observation, stalk, wait, detour or abandon |
| Approach | Walk, crouch, pause, use cover and listen to alarm calls | Prey changes awareness through stimuli, with reaction time and group propagation | Alerted animals move to plausible refuges; signs remain trackable for a bounded time |
| Attempt | Aim at an animated animal, release/strike, see contact and reaction | Server validates projectile origin/history, sampled pose, hit region, armor/bone and wound | A miss can disturb the group; a superficial hit is not silently promoted to a kill |
| Recover | Follow clear, fading signs; wait, search, or call for help where available | One animal persists with injuries and last known uncertainty, including unload/reload | Track expires honestly; journal records lost trail without manufacturing a corpse |
| Process | Tag/claim, move or field dress the carcass; carry, preserve, butcher | Authoritative ledger debits physical pools once and records provenance | Abandonment permits scavengers, spoilage and nutrient return; no automatic loot vacuum |
| Reflect | Journal encounter, learn species and evaluate population impact | Accepted observation/track/kill/recovery events can drive progression and quests | Repeated kills of the same cohort are not an infinite XP loop |

An early scene: in a cool foothill forest, a player finds fresh hoof prints on moist loam near a willow browse patch. The journal estimates a small group and a recent direction, with uncertainty. Downwind travel and partial tree cover allow a cautious approach. One animal raises its head and the others become watchful. The player may observe for a journal entry, take a suitable shot, or leave. The herd moves through a corridor toward cover; a valid vital hit produces one bounded recovery event and one carcass. Next season the reduced herd and altered route can be noticed. This scene must be possible without a quest, rare loot, special optics or external mod.

## 3. World inputs and ecological authority

Use existing saved world context: biome/habitat identity, topography, slope, ground cover, plant forage biomass, water accessibility, weather, wind, temperature, daylight, season, and persistent population/cohort state. A species profile maps these to **habitat suitability**, carrying capacity and behavior; hunting does not independently generate a second population table. Species can use thermal refuge, migration stopovers, breeding grounds and winter range. Seasonal movement follows qualified corridors and available resources, with persistent route memory where appropriate. The USGS migration research informs the idea that seasonal forage and stopovers affect route choice; our actual parameters remain invented game tuning. [R1]

An area's animal pressure has at least: resident biomass, recruits, natural losses, predator consumption, player harvest, emigration/immigration, forage/water limits and disturbance. Record transfers with reason and calendar sequence. A kill lowers resident individual count/biomass once; a predator/scavenger consuming it reduces available edible carcass pools, not a second living population. Do not repay every player harvest with an immediate distance-based spawn. Protect a low-density refuge or migration connection where ecology design calls for it, while avoiding invisible guaranteed replacement.

Foraging sign derives from foraging; bedding sign derives from rest; crossings derive from movement; predator sign from actual territory/foraging. The system may create *representative* signs when a coarse cohort traverses an area, but must debit or trace the event to that cohort, deterministic seed and interval. It may not place a fresh sign that promises an individual that cannot be materialized. A sign is evidence with confidence, not a magic waypoint to a fixed loot encounter.

### Soil and water join

Foundations Soil is a separate, existing soil project. The current [physical compatibility contract](../organic-systems/FOUNDATIONS-SOIL-COMPATIBILITY.md) exposes derived moisture/texture response through a read-only portable bridge; it does not give hunting ownership of soil nutrients or water stock. Hunting reads authoritative substrate and admitted moisture to choose footprint shape, clarity and persistence. A damp clay surface can retain a clearer impression than dry exposed rock; rain may degrade or wash signs. The exact mapping is balance data. Footsteps may compress vegetation or make a cosmetic disturbed patch, but **do not** decrement N/P/K, draw finite water, write a second 4×4 soil cell, or force-load a chunk. If a later gameplay action genuinely compacts soil, the soil owner supplies a transaction API and recovery tests first.

Waterholes and river crossings are likely encounter sites because animals actually drink and traverse there, not because wet blocks summon prey. Drinking consumes only the amount accepted by the finite-water/trough owner where that integration applies. Scent and track cues sample wind/weather from the shared environment; the hunter's UI shows a rough direction such as grass/smoke drift, not an exact hidden vector.

## 4. Original species roster and hunting roles

These are **working ecological roles** and placeholder original names; approve silhouettes, animations, biome fit and texture families before new art or blocks are submitted. They are not a claim that all are implemented. No copied WoW/TFC roster, sounds or assets.

| Species concept | Habitat/social niche | Readable signs and behavior | Sustainable harvest/product role |
| --- | --- | --- | --- |
| **Mossback Browsers** | Temperate forest edge; family groups browsing shoots | Split hoof prints, clipped twigs, repeated dawn route; group sentinel | Medium meat/hide/sinew, antler only where biologically and seasonally appropriate; cautious starter hunt |
| **Cairn Hare** | Rocky scrub; small solitary grazer | Small paired bounds, pellet clusters, shallow shelter | Small game, quick processing; modest supplies, faster reproduction |
| **Reedstrider** | Wetland margins; wading flock | Long narrow prints in mud, calls, disturbed reeds | Feathers and small meat; nesting season increases avoidance/conservation value |
| **Ridge Grazer** | Alpine grass/steppe; moving herd | Wide travel lanes, cropped turf, wind-exposed lookout | Large carcass with serious haul/preservation constraints; weather and altitude matter |
| **Rooter Boar** | Warm forest and river edges; family sounder | Turned soil and snuffling calls; defensive adults | Fat, meat, hide; dangerous at close range without universal aggression |
| **Glacier Wooler** | Cold tundra/highlands; small herd | Snow depressions, shed fiber and shelter beds | Warm clothing supply and potential managed lineage; winter takes a population toll |
| **Brindle Prowler** | Wet understory; solitary ambush predator | Claw/scuff signs and concealed route | Defensive/rare specialist hunt; its removal changes prey risk and scavenger access |
| **Shale Runner** | Foothill scrub; coordinated pack predator | Repeated canine-like tracks and contact calls | Territorial threat that can be deterred, avoided or dealt with; bounded pack replenishment |

The two predator names above link to the existing wildlife concept roster; implement them only against that roster's approved final IDs. A prey species is never merely a food dispenser: it grazes, beds, breeds, reacts to predators, migrates or disperses, and may defend young. Aquatic hunting and fishing use a separate catch/gear loop but share identity, weight, population and material accounting. Authorable fantasy animals must declare biological anatomy and food-web role explicitly. Sapient factions and constructs use the MMO threat contract instead.

## 5. Senses, awareness and response

For each animal, maintain `calm → attentive → alarmed → fleeing/defending → settling`, plus independent hunger/rest/reproduction and injury states. Awareness is **stimulus-based**, not a permanent hostility toggle. Stimuli carry type, origin, strength, timestamp, confidence, expiry, and whether line of sight/hearing/scent was actually available. A remembered position remains uncertain, widening as time passes. Tuning may represent fear as a score, but transitions need thresholds, hysteresis and minimum reaction intervals so animals do not twitch between states each tick.

- **Vision:** pose, distance, relative movement, occlusion, light and species field-of-view. Check a bounded number of rays only for candidate targets; cover is useful but does not make the player invisible at arm's length.
- **Hearing:** footfalls, running, tools, projectile release, speech and impacts produce radius-limited acoustic events modified by terrain/weather. No every-tick global sound scan.
- **Scent:** optional coarse wind-aligned stimulus field with limited lifetime and cell budget. It is a gameplay abstraction, never a kilometer-scale fluid solver or exact through-wall player locator.
- **Social information:** local sentinels and group alarm calls pass bounded confidence to nearby members once per incident. A herd can scatter or regroup; there is no continent-wide panic broadcast.
- **Learning:** repeated dangerous encounters may shift one local group's route/use window within bounds. Fear is time/place/activity-dependent, not a permanent biome debuff; USGS Yellowstone work supports treating predation risk as dynamic. [R2]

Predators use the parallel ecology state machine: investigate, stalk, commit, pursue, disengage, feed and rest. Injuries, hunger, risk, terrain and territory matter. They do not all attack a player on sight, and pack members do not all land simultaneous untelegraphed hits. Threat perception and counterplay belong to [animal ecology](ANIMAL-ECOLOGY-AND-PREDATORS.md) and [encounters](MMO-THREATS-AND-ENCOUNTERS.md); the hunting subsystem consumes their accepted detection and injury events.

## 6. Signs and tracking

### Sign model

Each sign has stable `sign_id`, origin animal/group/cohort, species or ambiguity class, event kind, creation calendar sequence, position/heading, substrate, weather exposure, base strength, current visibility, persistence policy and eligible observers. Materialized signs are sparse world objects/decals only near active players; an indexed region record supplies coarse inquiry elsewhere. Avoid a block entity for every footprint. A sequence of prints may be rendered from one trail segment; it should not generate twenty saved objects. The [NPS track resources](#13-reference-notes) show why track *pattern and gait* matter in addition to one isolated print. [R3, R4]

Sign classes: prints (gait/direction/size class), clipped or grazed plants, beds, rubbing/scratch marks, hair/feathers, calls, nesting material, scat (abstract/non-graphic), disturbed soil, kill/scavenge sites, and wound trails. A print's apparent size is not an exact animal weight; substrate and speed introduce uncertainty. Species journal learning increases the quality of interpretation, not the frequency of synthetic signs.

Freshness is a range (e.g. recent / older / weathered) derived from the world calendar and actual exposure. Rain, snow, wind, sunlight, traffic, substrate and local water flow degrade evidence. Snow can reveal fresh paths and then cover them; hard rock preserves little. A sign cannot become younger after reload or sleep. Cross-chunk segments have stable ownership (region key plus sign ID), and their visual pieces never duplicate the underlying clue. Unloaded time obeys the shared calendar policy: empty-server pause, no shutdown wall-clock advance, and no invented missing weather history.

### Interpretation UI

A small journal entry says, for example: “medium browsing herd; probably passed this morning; moving toward the stream; confidence moderate.” It shows why (multiple overlapping prints, fresh browse, matching calls). At low skill it may say “large hoofed animal, older sign.” Do not reveal exact entity coordinates, vital hit boxes, a deterministic path overlay or a guaranteed kill location. An accessibility option uses a high-contrast outline and text/audio cue for visible sign without changing server evidence. Color alone never conveys age or direction.

The field journal records observation, species silhouettes, known habitats, seasonality, sign examples, successful and lost trails, and ethical/management notes. Discovery comes from observing living animals and signs as well as taking a carcass. No repeated AFK stare or sign re-render grants uncapped experience.

## 7. Equipment, approach and shot resolution

### Progression and identity

Early equipment: primitive spear and sling or simple thrown implement, crude bow, stone/bone points, fiber binding and basic carrying gear. Later craft: better shafts, fletching, metal points, durable hunting bows, repair and optional bow modifications. The **visible back quiver** renders arrows stored in an authoritative container; rendering never adds inventory capacity or duplicates a loose arrow stack. Point material affects penetration, edge retention, breakage and recoverability through declared data, not arbitrary species immunity. Carry weight, weather exposure and fatigue are practical constraints, not a forced single best weapon.

Weapons and upgrades need one readable stat sheet: handling, effective game range band, draw/recovery time, noise, penetration class, durability, ammo mass and condition. Exact projectile speeds and damage are balance inputs, not commitments to a real-world ballistic simulator. A weapon remains usable in creative/testing without granting survival loot or quest credit through admin/test provenance. Compound bow remains a later optional equipment branch; rifle remains tentative and excluded from initial implementation.

### Server validation

At release, server records shooter/action ID, world tick, weapon revision/condition, ammo ID and consumed inventory claim, origin, direction and bounded pose history. The moving projectile holds ownership and deterministic simulation state. At contact, evaluate the animal's **animated pose at the accepted impact time** using a bounded server pose ring: body-part collision volumes follow the actual neck/chest/abdomen/limb/head orientation, not a static full-body box. Validate line of travel, occlusion, velocity class, impact angle, material/armor, and requested lag window. If history is unavailable, use a documented conservative fallback; never enlarge vital regions or accept client-supplied “heart” claims. Reject replayed shot IDs and impossible origin/rotation/teleport histories.

Anatomy profile declares coarse external regions, major bone barriers, vital zones, functional limb effects and species-specific variants. It is **game anatomy**, reviewed with animation and art; actual organ labels should not become a universal point-and-click weakspot UI. A chest/shoulder hit can be rapidly fatal on suitable species with adequate equipment and valid geometry, matching the existing owner aspiration. A shoulder hit through bone from a poor angle may be non-vital. Do not guarantee one-hit death from the name of a body region alone.

Proposed damage pipeline (deterministic server logic, with seeded uncertainty only where exposed by config):

```text
shot claim → consume/reserve ammo once → projectile simulation
→ authoritative pose/contact region → cover + material + impact energy class
→ tissue/bone/vital effect → immediate response and persistent wound
→ accepted hit event → possible death event → one carcass creation
```

If another player hits simultaneously, both attempts can be recorded, but the first accepted death transition owns the carcass UUID. The later valid hit may receive contribution evidence according to the quest rule; it never yields a second carcass. No client-authoritative hit region and no projectile/client desync that creates loot twice.

## 8. Injury, pursuit and recovery

Use a compact wound record: wound ID, region, severity, functional effects, bleeding *abstraction*, source/action, treatment/recovery state, last accounted calendar sequence and public sign budget. Avoid gratuitous medical simulation or guaranteed delayed kills. Affected animals slow, rest, seek cover/water, avoid a recent danger or regroup according to species and severity. The player sees plausible response: stumble, altered gait, retreat, tracks and a finite wound-trail clue. Signs are capped; no infinite blood decals.

Wounded prey can recover if a hit was minor. A severe wound may lead to incapacitation or death over bounded simulated time, but the player must be able to follow a meaningful trail while active and within reasonable distance. No reward bonus for waiting on suffering. The option to end a hunt promptly, and an alternate non-gory presentation, support humane play. A lost animal persists in the population with the injury or resolves under the same ecological simulation when unloaded; it does not despawn and reappear healthy next chunk load. If death occurs unloaded, create one region-owned carcass record with a discoverability/expiry policy, not a full forced-load entity.

The hunter's search area should widen with sign age, wind, weather and erratic movement. Track confidence decays honestly. A recovery event requires locating the actual animal/carcass; a quest “track” event may require enough distinct connected clues rather than merely clicking one repeatedly. Predators and scavengers may consume or contest a carcass, creating legible urgency and danger. Player kill attribution is retained even if a scavenger reaches it first; edible portions are not magically restored.

## 9. Carcass, hauling and processing

Death freezes species/revision, animal UUID, live and available carcass mass definition, condition, injuries, contamination, organ/skin/bone constraints, calendar time, cause, provenance and an immutable `carcass_id`. The [animal contract](ANIMAL-ECOLOGY-AND-PREDATORS.md#10-hunting-carcasses-and-material-conservation) owns canonical mass partitions: meat, fat, organs, hide, bone, other usable pools, fluid/gut/residue and unrecoverable loss in declared units. Each recoverable pool can be debited by processing, scavenging and spoilage. **Sum of remaining recoverable pools plus named consumed/lost/transferred accounts never exceeds original available carcass mass.** An antler, wing or hide count cannot exceed the animal's anatomy. Weight changes output mass/area, not limb count.

Large carcasses require field dressing, a travois/cart/pack animal, helpers or staged trips. If the player cannot carry it, it remains a world object/record with a location and rights policy. Small game can be carried whole. Marking a carcass identifies provenance and avoids accidental party disputes but does not let one hunter reserve a public natural resource forever; server/admin claim duration and cooperative permissions are explicit.

Processing stages: inspect → field dress → transport/hang/chill or work in place → skin → cut/bone → preserve/cook/craft → dispose/compost. Each step consumes time, tool durability and one revisioned slice of the appropriate pool, then produces traceable lots with mass, age, temperature history and contamination abstraction. The player may choose alternatives; no compulsory seven-screen mini-game. Skill and clean tools improve **recovery fraction and speed**, never total biological mass. Hide quality depends on size, damage and treatment; feathers/wool/horns are species-appropriate. Normal food spoilage applies to lots, not a second independent carcass timer that consumes the same meat twice.

One transaction owns each claim: reserve a pool revision and output capacity; calculate deterministic outputs and remainders; commit pool debit plus inventory/world placement; emit accepted `carcass_portion_recovered` event; reconcile after crash. Concurrent players cannot both recover the same cut. A full inventory leaves output in an on-site claimable state or cancels before debit. Dropped stacks keep provenance and mass, including merges and splits. If an action produces a byproduct for soil or scavengers, that mass transfers to a named environment account through the appropriate owner. Fictional toxins/contamination cannot be universally erased by trimming one visible piece.

## 10. Progression, quests and population management

Hunting skill is a separate profession track from persistent, capped, non-spendable character XP. Earn it from first observations, connected sign interpretation, careful approach, responsible recovery, processing and species variety. Diminishing returns for identical repeated actions prevent sign farming and spawn cycling. Skill improves interpretation confidence, use efficiency and recovery percentage within bounded caps; it does not confer x-ray vision, infinite critical hits or multiplied carcass mass.

Example authored objectives: observe a living Mossback at a water crossing; identify three connected signs of one herd; deliver a specified **mass** of preserved meat with natural provenance; deter a predator from a farm; retrieve a carcass before scavengers; document an overhunted corridor and help restore habitat. Each objective declares event kind, subject ID/class, provenance, contributors and clock. A kill objective may credit a party under a declared participation rule, but all participants share the one physical carcass. Admin test spawns, cosmetic animals, quest-granted item loops and client claims are ineligible unless the author deliberately enables a narrowly documented training objective. A nonlethal route should exist for story-critical wildlife interactions where feasible.

An ecology journal can summarize local herd trend (abundant/stable/scarce), evidence and uncertainty after sufficient observation. The player should see seasonal forage/corridor pressure, not a precise omniscient population number. Server options may choose a stricter population mode, but all modes retain one authority and avoid instant respawn exploitation. A future protected reserve/culling permit system belongs to settlement/faction policy and must not be implied by this wildlife spec alone.

## 11. Data and ownership contract

Illustrative definitions, subject to schema review:

```json
{
  "schema_version": 1,
  "species_id": "deepprimal:mossback_browser",
  "definition_revision": 1,
  "habitat_tags": ["forest_edge", "browse_cover"],
  "social_role": "sentinel_herd",
  "activity_profile": "crepuscular_variable",
  "anatomy_profile": "deepprimal:medium_browsing_quadruped_v1",
  "sign_profile": "deepprimal:split_hoof_browse_v1",
  "carcass_profile": "deepprimal:medium_browser_v1",
  "recovery_policy": "bounded_wound_and_trail_v1"
}
```

These are references to independently validated, versioned definitions; no arbitrary JSON string may execute code. Validate IDs, finite numeric ranges, anatomy-to-animation region mapping, bounded sign lifetime/count, habitat eligibility, loot mass fractions and cross-reference existence before enabling a species. Registry reload may affect **future** individuals; existing saved individuals retain effective revision or undergo an explicit migration. New/renamed display names and skins never change saved species identity.

Persistent state is partitioned by owner:

| Owner | Stores or supplies | Hunting access |
| --- | --- | --- |
| Animal/ecology | Individual UUID, cohort reservation, condition, home, movement, injuries, group and population ledger | Read/candidate events; accepted injury/death transitions |
| World environment | Calendar, weather, wind, biome, vegetation and accessible water | Snapshot/read, bounded queries; no forced distant chunks |
| Foundations Soil or chosen soil owner | Physical profile and admitted moisture-derived conditions | Read-only derived sign substrate; no nutrient/water writes |
| Hunting | Shot/action IDs, sign/trail records, discovery, contribution and recovery evidence | Owns deduplication and clue lifecycle |
| Carcass/material | Carcass UUID, versioned pools, ownership/expiry, lots, scavenging and spoilage | Atomic portion claims and audit log |
| Quest/progression | Accepted event consumption, objective/party rules and reward entitlements | Server-signed event envelope; deduplicated consumption |

An illustrative server event after a committed operation:

```json
{
  "schema_version": 1,
  "event_id": "server-generated-id",
  "world_id": "saved-world-id",
  "kind": "deepprimal:wildlife_recovery_committed",
  "actor_id": "player-uuid",
  "subject_id": "animal-uuid",
  "calendar_sequence": 420,
  "transaction_id": "recovery-id",
  "provenance": "normal_gameplay",
  "carcass_id": "carcass-uuid",
  "contributors": ["eligible-player-uuid"]
}
```

The event is emitted **after** the authoritative state transition. Delivery may repeat; consumer deduplication and a durable transaction journal give one logical result. Save order, crash recovery and retention bounds must be proven. Clients cannot set `provenance`, `contributors`, `carcass_id` or outcome. Observations, tracking, hits, kills, recovery and processing have separate event kinds so a quest cannot confuse “looked at a print” with “harvested an animal.”

## 12. Simulation and performance plan

**Near player:** individually simulated animals, animation-following hit volumes, perception candidates, local navigation and sparse trail rendering. **Middle distance:** cohort movement between habitat cells and aggregate sign/travel summaries at scheduled intervals. **Far/unloaded:** persisted cohorts, obligations and calendar sequence with bounded on-demand catch-up only when its required historical inputs are known. A named, owned, wounded, pregnant or combat-engaged animal remains individually addressable; virtualization cannot erase it or issue loot.

Implementation must not scan every animal against every player each tick. Use spatial cell indexes, stimulus queues, time-sliced perception, bounded pose history, movement-event trail emission and regional budgets. Line-of-sight rays target shortlisted candidates. Sign records have cell caps, age eviction and deterministic order; culling visuals never changes an accepted trail or physical entity. Do not synchronously read or generate neighboring unloaded chunks for tracks, water, soil or path queries. Feature flags and metrics separate ecology, perception, signs, projectile pose, carcass persistence, and client rendering costs.

Suggested **initial prototype budgets to measure and revise**, not promises: no more than one coarse habitat decision per distant cohort per game-minute; no more than 64 active rendered sign segments around one player; cap a local herd's alarm fanout at 16 recipients per incident; cap pose history to the accepted latency window; bound per-region sign storage and reconcile evictions by age. Establish actual reference hardware/server load and target p50/p95/p99 tick, allocation, save bytes, packet bytes, exploration latency and client frame cost before locking values. A crowded 20-player crossing, dense forest, wet weather and simultaneous carcass processing must be profiled. Correctness under load is mandatory even if optional cosmetic signs are reduced.

### Cross-boundary and adversarial cases

Test animal crossing a region/chunk border during an arrow flight; sleep/calendar advance during a wound; server restart between death and carcass creation; two players dressing the same carcass; crash after pool debit before output acknowledgment; scavenger and player claiming one pool; player logout while wounded prey remains loaded by another; seasonal migration during unload; carcass item stack split/merge; movement through a portal where species allows it; creative/admin spawn; a modded damage source; resource-pack animation variation; and old-save definition revision. Every case has one source animal, one death, one carcass and an auditable material ledger.

## 13. Phased delivery and acceptance

| Phase | Deliverable | Minimum release gate |
| --- | --- | --- |
| H-0 contracts | Species/anatomy/sign/carcass schemas; source ownership; calendar/soil/environment adapters | Invalid definitions rejected, version migration specified, original no-vanilla-resource bootstrap reviewed |
| H-1 first hunt | One original browsing prey, one habitat, observation journal, two sign classes, primitive weapon, server pose validation, one carcass | Cold/reload and two-player tests; miss/hit/non-vital/vital outcomes; no duplicate kill or mass |
| H-2 living region | Herd/sentinel, forage/water routine, regional cohort authority, alarm/refuge, seasonal route | Population and sign provenance replay; no chunk-reload respawn; empty-server time policy |
| H-3 recovery and processing | Wounds, trail, spoilage/scavenging, hauling, field processing, item lots | Wound survives reload; mass and anatomy constraints; crash/concurrency reconciliation |
| H-4 ecological variety | Additional prey guilds, one original predator, biomes/seasons, quests and cooperative credit | Avoidance and nonlethal objectives; one carcass for party; multi-player performance and acceptance scenes |
| H-5 polish | Approved textures/animations, accessibility, full sound/visual settings, balance and native gameplay | Playtest across climates/season extremes; no impossible starter; no sign occlusion/UI failure; measured tick/frame/save budgets |

Acceptance fixtures use known world seed, habitat revision, calendar sequence, weather series, actor IDs and action IDs to replay exactly. Record baseline and changed outcomes for: a clean stalk; a noisy approach; rainfall erasing prints; a chest/shoulder hit from two distinct valid angles; a recoverable superficial wound; a cliff-edge escape; a player and predator contesting one carcass; a scarce population after overharvest; and an observation-only quest. The exact hit outcome should be checked against **animated** pose footage and server logs, not a static unit box alone. A full server build, dedicated-server gameplay, save/reopen, mixed-client latency and visual review are required before claiming completion; this MD alone passes none of them.

## 14. Open decisions for later balancing

| Decision | Proposal / test needed |
| --- | --- |
| First prey identity and art | Choose a starter habitat and original silhouette; approve its full 32×32 texture family, 64×64 only where detail requires it |
| Calendar-to-real-time scale | Reuse the shared world calendar; tune sign lifetimes and wound windows in game time after a real playtest |
| Population strictness | Select default rebound and refuge behavior so regions react without permanent local extinction from one early mistake |
| PvP/claims | Determine whether other players can take a tagged carcass, for how long, and how public lands differ from claims |
| Scavenging pressure | Tune contest/decomposition to create urgency without punishing solo players or offline absence |
| Skill UI | Decide whether hunting is one profession or fieldcraft/processing subskills; both remain distinct from character XP |
| Additional weapons | Bow modifications/compound bow proposed; rifle tentative and out of first scope |
| Presentation | Pick default low-gore cues, sound mix, color/accessibility and animal distress limits through player testing |

## 15. Reference notes and inspection limits

The following references supply ecological and interface inspiration, not imported code, art or guaranteed numerical parameters. Research descriptions and source code do not establish Deep Primal runtime behavior.

- **[R1]** [USGS, *Ungulate Migration in a Changing Climate*](https://pubs.usgs.gov/publication/cir1493/full): seasonal forage, migration routes and stopovers. [USGS seasonal habitat study](https://www.usgs.gov/publications/white-tailed-deer-habitat-use-and-implications-chronic-wasting-disease-transmission) documents variation by sex, age, season and region.
- **[R2]** [USGS, *Diel predator activity drives a dynamic landscape of fear*](https://www.usgs.gov/publications/diel-predator-activity-drives-a-dynamic-landscape-fear): risk varies with time and activity; [landscape heterogeneity study](https://www.usgs.gov/publications/landscape-heterogeneity-shapes-predation-a-newly-restored-predator-prey-system) supports refuges and terrain-dependent outcomes.
- **[R3]** [NPS wildlife tracks](https://www.nps.gov/places/wildlife-tracks.htm): tracks/scat at water sources and species clues.
- **[R4]** [NPS carnivore tracking guide](https://www.nps.gov/mora/planyourvisit/upload/Carnivore-Tracks-12-20-21_508.pdf): track pattern and gait as well as individual footprint shape.
- **[R5]** [NPS scavenger study](https://www.nps.gov/articles/000/scavenging.htm): carrion benefits other animals and creates vigilance/competition; our conserved carcass accounting is a game design extension.
- **[R6]** [TerraFirmaCraft 1.21.x source book](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/resources/generate_book.py) and [animal properties source](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/src/main/java/net/dries007/tfc/common/entities/livestock/TFCAnimalProperties.java): reference for familiarity, life history, predator territory and activity. Branch source may change; this is not a copied implementation or compatibility claim.

**Current integration state:** Deep Primal's existing documentation and portable Foundations Soil physics bridge were inspected on `main` for this design. Native hunting behavior and its acceptance suite are future work. Implementation should begin at H-0/H-1 and enter the existing SYS phase gates rather than claiming a complete hunting mod from a document.
