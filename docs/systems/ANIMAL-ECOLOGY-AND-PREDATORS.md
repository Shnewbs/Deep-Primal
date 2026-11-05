# Deep Primal animal ecology, husbandry and predators

**Edition:** October 29, 2026, America/Los_Angeles. **Status:** researched specification and proposed implementation roadmap. No animal implementation, runtime compatibility, balancing result or completed acceptance is claimed. Names and creature designs below are original working concepts, pending art and gameplay review.

## 1. Confirmed direction and boundaries

All vanilla mobs are removed from Deep Primal's intended ecosystem: passive animals, hostile creatures, villagers, aquatic creatures, utility mobs and bosses. Replacement species use Deep Primal identities and mechanics. Retaining Minecraft as infrastructure does not retain its mob roster. The shared vanilla-removal/migration policy owns existing-save decisions; this document identifies creation paths and animal consequences without authorizing destructive deletion.

World of Warcraft supplies a broad creative direction: recognizable habitat communities, varied threat silhouettes, readable combat roles, rare encounters and regional identity. Do not copy its creature names, models, textures, animations, sounds, lore or encounter scripts. Real-world species and independently authored fantasy fauna can coexist when each has a plausible niche and readable behavior.

The existing [design decisions](../design-decisions-2026-10-18.md) remain binding: individual weight governs every animal's material yield; anatomical hits follow the animated body and are validated by the server; hunting includes tracking and carcass processing. The calendar pauses on an empty running server by default, has a configurable override, and never gains elapsed wall-clock time while shut down. One absent player does not pause a populated server. All numerical ranges below are proposed tuning data unless identified as reference evidence.

### MMO threat scope clarification

The owner clarified that predators must also sit alongside WoW-style monsters, villains and other MMO enemy types suited to Deep Primal. This document owns wildlife/husbandry and twelve animal concepts. The broader [encounter framework](MMO-THREATS-AND-ENCOUNTERS.md), [hostile factions and named villains](HOSTILE-FACTIONS-AND-VILLAINS.md), and [primary-source study](MMO-THREAT-REFERENCE-STUDY.md) own intelligent opponents, fantastical lineages, camps, patrols, regional arcs, rares and bosses. A creature's species, hostility, combat role, rarity and story role are separate properties. Wildlife does not all become permanently hostile, and a villain is not merely an animal with inflated health.

## 2. Research comparison: adopt, adapt, reject

TerraFirmaCraft is studied specifically as **Minecraft 1.21.1, TFC v4.x**, confirmed by its official developer documentation. The stable **v4.2.12** release is explicitly labeled 1.21.1; selected source files were read at that tag. The online guide supplies gameplay context but can show an older guide edition or configurable defaults. Moving branch source and a downloaded release must never be treated as identical evidence. [A01–A04]

| Reference and verified scope | What the evidence establishes | Adopt or adapt for Deep Primal | Reject or leave unclaimed |
| --- | --- | --- | --- |
| TFC 1.21.1/v4.2.12 | Familiarity, sex, age stages, pregnancy/eggs, equine inheritance; predators have home/activity/hunted-state logic | Familiarity and life history as separate systems; climate-qualified habitats; bounded pursuit | TFC's exact timings, product-use aging, or its retained vanilla-monster defaults as automatic Deep Primal rules |
| Original Alex's Mobs 1.22.9, MC1.20.1 Forge/NeoForge | Publisher describes species interactions, Animal Dictionary and behavioral rewards; inspected tiger source contains stealth, approach and attack phases | Strong tells, memorable species interactions, nonlethal discoveries | Assuming its mod-version filename 1.21.1 means MC1.21.1; copying its roster or assets [A10–A11] |
| Naturalist 2.0.7, MC1.21.1 NeoForge | Current publisher release verifies that platform; inspected lion and panic goals show schedules, following and alarm propagation | Family/herd cohesion, bounded shared danger memory | Treating a defined but unregistered lion prey goal as active stalking; equating roster size with ecology [A12–A14] |
| Better Animals Plus 11.0.10, MC1.19.2 Forge/Fabric | Published version; inspected source persists hunger and gates prey acquisition | Hunger, satiation, suitability and risk jointly motivate hunts | Claiming original-project 1.21.1 support; universal night aggression or instant food-based domestication [A15–A17] |
| Animania Base 2.0.3.28 + Farm1.0.2.28, MC1.12.2 Forge | Publisher documents care infrastructure, pregnancy, growth and lactation; breed selection can choose a parent | Troughs, sheltered husbandry and useful automation | Calling parent-breed selection full genetics; importing repetitive short care loops or old constants [A18–A19] |

These are design references, not selected runtime dependencies or a popularity ranking. Version support is project-specific; community ports require separate provenance. Source inspection does not establish that a listed release executed those exact source files.

TFC's livestock source persists familiarity, sex, birth/old dates and genetic size. Its aging combines calendar growth with use-related elderly transitions. Mammal source retains pregnancy and parental genes, and horse code carries bounded inherited attributes. This supports a useful distinction: social trust, nutrition, growth, reproductive state and inherited potential should not be one “tamed” boolean. Deep Primal's deeper body-condition, grazing and carcass-mass model remains our proposed extension. [A04–A06]

## 3. Species definitions and individual state

Each species definition declares stable ID/revision, adult size distribution, growth curve, sex characteristics where applicable, food guild, thermal/water needs, habitat suitability, home range, daily schedule, social system, reproductive mode, migration, prey/predator relationships and material profile. Shared tags describe traits; they do not make every tagged creature behaviorally identical.

An individual persists UUID, species/revision, birth date, sex/reproductive role, inherited traits, current body mass, body condition, health/injuries, familiarity/handlers, home/group, learned hazards, reproductive record, ownership and last accounted calendar time. Do not serialize a live pathfinding object or mutable entity reference. Stable UUID relationships resolve across unloads and dimension changes. Captured fish, live transport containers and released animals retain identity, mass and age; capture is an ownership transfer, not a new specimen. Catch journals preserve the original weight record after processing.

The first nonpredator community must support the predators and survival economy: a medium browsing herd, a small burrowing grazer, an omnivorous rooter, a seed-eating nesting bird, a cold-adapted wool/draft lineage, a warm-climate dairy lineage, shoaling fish, aquatic detritivores and pollinating insects. Assign original species IDs, art and interactions before implementation. Pollination, dung/nutrient return, pest control and seed dispersal are explicit services with bounded probabilities; insects need not all become individually persistent entities. No vanilla animal silently supplies a missing dependency.

Separate inherited frame potential from acquired condition. A large, poorly fed animal can weigh less than a smaller healthy peer. Growth approaches an age/trait target; nourishment supports growth, maintenance, recovery and production through bounded conversion profiles. Hydration and injury influence behavior and recovery. Do not advertise this simplified game model as veterinary physiology.

Use one understandable condition display: poor, lean, healthy, heavy, with causes and trend. Approximate weight is discoverable by observation; scales and husbandry skill improve precision. A single missed interaction does not cause sudden starvation or erase years of trust. Every negative state needs an intelligible remedy and an actionable advance warning.

## 4. Familiarity, domestication and humane progression

**Familiarity** is learned tolerance of people/handling. **Bonding** records trusted handlers. **Training** unlocks specific behaviors. **Domestication** is a species/lineage suitability and multigeneration process, not feeding a wild apex predator three items. Wild habituation may make an animal less fearful without making it safe, owned or breedable.

Trust rises through predictable feeding, quiet proximity, grooming where appropriate, safe handling and successful training. Repeated spam clicks share a cooldown and cannot accelerate progress. Frightening or hurting animals causes bounded, recoverable setbacks; the UI identifies the cause. Stable established familiarity decays slowly or stops under adequate routine care. Young animals learn more readily, but an adult can still become manageable within its species limits.

Proposed progression:

1. **Observer:** identify diet, tracks, activity and warning signals; build a field journal without killing specimens.
2. **Caretaker:** provide shelter, safe water and forage/feed reserves; guide animals with species-appropriate lures and gates.
3. **Handler:** establish trust, name individuals, manage compatible groups, check weight and injuries.
4. **Breeder:** plan mating windows, select healthy complementary traits and track parentage.
5. **Specialist:** train original companion lines, dairy/wool stock, riding and draft animals; automate routine supply.

Troughs, feeders and pasture systems serve groups for several configurable days and clearly show remaining service time. Refill a shared store instead of clicking each animal daily. Breeding can be enabled per pen or pair; separation and a reversible breeding toggle provide ordinary control. Old animals can remain companions or retired stock; meat is not their only legitimate purpose. Optional harsher survival settings must not become invisible defaults.

Draft work has mass, gradient, stamina, harness and rest limits. Handler skill improves control and reduces waste rather than granting infinite carrying strength. Hitching and cargo transfer share the freight system's authoritative ownership; an animal, cart and inventory cannot be duplicated through unload, death or disconnection.

## 5. Food, grazing, water and seasonal care

Define grazing, browsing, seed-eating, omnivory, insectivory, carnivory and aquatic feeding separately. Diet profiles specify acceptable foods, energy/protein abstraction, digestive suitability and production conversion. Treats aid handling but do not automatically replace a full ration. Clearly warn about unsuitable fictional foods without requiring a spreadsheet to keep two animals alive.

Pastures track bounded edible biomass and regrowth against climate, soil moisture and season. Animals reserve and consume patches once; several mouths cannot each claim the same grass. Grazing changes forage cover before destroying terrain. Browsers can use reachable shoots; small grazers use low vegetation. Rotation and hay stores give meaningful winter preparation without mandatory minute-by-minute chores. Overgrazing recovers through vegetation rules rather than permanent punishment.

Water access is a reachable, suitable drinking surface or supplied trough, not a block somewhere inside a radius through a wall. Trough fills/withdrawals participate in the water/container transaction model. Decorative water pixels and unloaded rivers do not supply unaccounted finite liquid. Aggregate drinking can use a documented game-scale demand; tune this with hydrology rather than simulating real daily liters literally.

Seasons influence breeding windows, growth, coat/regrowth, forage, migration and water availability. Latitude and local climate matter; a universal northern winter schedule is insufficient. Shelter gives a clear benefit against exposed heat/cold. Keep a forecast and feed-days estimate, and provide grace time for unexpected weather. Avoid random hidden epidemics destroying unattended farms; any later health outbreak system needs its own consent/configuration and readable progression.

## 6. Reproduction, aging and inheritance

Mating eligibility checks species compatibility, mature age, reproductive role, health/condition, adequate expected feed capacity, current pregnancy/cooldown and owner breeding permission. Wild reproduction also checks regional carrying capacity. Consent is a player management setting, not a claim about animal behavior. Pregnancy, nesting/incubation, birth and post-birth recovery are distinct states.

At conception create a durable reproduction ID containing parent UUIDs, conception/due dates, deterministic offspring seed and reserved litter size. At birth, atomically commit offspring UUIDs and mark the event consumed. Save/reopen, duplicate ticks and two observing players cannot repeat the litter. Fertilized eggs carry embryo identity through storage; an egg cannot hatch and remain cookable simultaneously.

Inherited traits initially cover frame size, coat/markings, temperament, hardiness and one species-specific function such as wool or draft potential. Nutrition and handling still influence expressed performance. Bound all traits to prevent runaway super-animals; breeding tradeoffs should create choices rather than one permanently optimal line. Record parentage and provide a simple relatedness warning; avoid opaque mandatory genetics calculators or punitive deformity systems.

Aging is calendar-based life history with juvenile, mature and elderly stages. Production tapers rather than suddenly expiring after a milk/shear click count. Lifespan warnings and retirement options prevent surprise disappearance. Recurring milk, eggs, wool and similar products depend on species, body mass, maturity, condition, nutrition, physiological state and elapsed production interval. Lactation follows a species-defined reproductive cycle. Stores have capacity; collection frequency cannot manufacture extra production.

## 7. Food webs and population authority

Partition regions into habitat cells containing suitable forage, shelter, water, prey biomass, breeding space and disturbance. Seed initial populations deterministically under a saved ecology revision. Recruitment uses capacity and food, not endless distance-based replacements. Predators consume prey/carrion; food scarcity lowers recruitment and encourages bounded dispersal before mass extinction. Maintain recovery corridors/refuges so one busy settlement does not erase an entire species globally.

Use individual simulation near meaningful interactions and bounded aggregate cohorts elsewhere. A cohort owns counts, age/condition bands, biomass and migration/reproduction obligations. Materializing an individual reserves and subtracts a cohort token; dematerializing returns exactly that individual once. Named, owned, injured, pregnant, combat-engaged or cargo-carrying animals retain individual identity. Population culling for budgets is virtualization, never a silent death/loot event.

TFC's predator source supplies a concrete home/hunt/fight/pacified foundation and pack-level target sharing; its prey and climate guide supports readable habitat relationships. Deep Primal extends this into persistent territory, capacity and risk accounting. Do not call a temporary “hunted recently” memory a complete nutritional ecosystem. [A03, A07–A09]

A successful hunt creates one death/carcass event. Feeding consumes that carcass's remaining edible pool, reducing what players or scavengers can recover. Scavengers compete, warn and retreat; they do not summon fresh loot by revisiting a skeleton. Decomposition and scavenging expose nutrient returns through simplified ecological pools. These are bounded gameplay transfers, not a promise to model every atom.

## 8. Predator and prey decision model

Predators choose among rest, patrol, forage/scavenge, investigate, stalk, threaten, pursue, attack, feed and retreat. Utility considers hunger, prey size/condition, distance, terrain, group strength, injury risk, offspring/territory defense and prior disturbance. No species remains permanently enraged solely because a player entered render distance.

Perception is server-owned: visible silhouettes, bounded hearing events, fresh tracks and optional wind-affected scent. A remembered last-known position is not current exact knowledge through walls. Each stimulus has origin, confidence and expiry. Weather modifies confidence with readable effects; sneaking and cover matter. Spawned creatures cannot begin with omniscient knowledge of the player.

Packs have persistent group identity, home, membership and a bounded shared threat record. Leadership means coordination, not a magical damage aura. Roles can flank, pressure, guard young or contest food; only a bounded number commit attacks simultaneously. Breaking line of sight, reaching unsuitable terrain, inflicting deterrence or leaving territory can end pursuit. Leash return must not instantly heal or erase valid injuries while attackers exploit the boundary.

Prey scan, watch, alarm, group, flee, hide or defend young. Escape paths avoid cliffs, deep water and predator intercepts where navigable. Cornered prey can threaten or fight; not every herbivore is free food. Herd alarms have radius, cooldown and confidence decay, preventing a continent-wide panic cascade. Injury affects escape and tracking but never guarantees every hit becomes a kill.

## 9. Original wildlife predator roster

**Threat bands are proposed encounter levels, not automatic player scaling:** I(1–5), II(6–12), III(13–20), IV(21–30). Population, age, condition and habitat bound variation. A level never resizes anatomy or grants arbitrary immunity. Every creature has discoverable signs, a safe avoidance route and rewards beyond repeated killing. Working names require final originality/art review.

| Concept; habitat; band | Role, social structure and ecological constraint | Tells, counterplay and rewards |
| --- | --- | --- |
| **Brindle Prowler**; wet temperate understory; II | Solitary low ambush hunter of medium grazers; one adult territory, seasonal dependent young; avoids repeated injury | Paired claw marks, disturbed fern tunnel and crouch before leap. Keep open sightlines, face it and sidestep committed pounce. Weight-based spotted hide, sinew; journal stalking observation |
| **Shale Runner**; cool foothill scrub; II | Endurance pack hunter, 3–5 adults; prey availability caps packs, wounded members withdraw | Rhythmic contact calls and visible flank approach. Move toward defensible obstacles, interrupt the closest committed runner; avoid sprinting blindly. Tough leather, tendon; nonlethal pack-route mapping |
| **Reed Gnasher**; warm river margins; III | Solitary amphibious ambusher; needs connected deep refuge and fish/prey supply; no dry inland pursuit | Reed wake, exposed nostril ridge and jaw lift. Observe crossings, use broad shallow routes, retreat from bank rather than contest water. Scute material and weight-based meat; nest sighting credit |
| **Lantern Kite**; dusk woodland edges; I | Small aerial predator of vermin; pairs; throat glow attracts insects, not supernatural targeting | Sweeping wing shadow and rising whistle before dive. Covered paths and a timed lateral move foil its pass. Naturally shed flight feathers and observation rewards; juveniles never spawn as attack waves |
| **Cairn Breaker**; rocky conifer valleys; IV | Large omnivore/territorial bruiser, usually solitary; berries/carrion important, very low density | Overturned stones, guttural huff and planted forelimbs before shove. Give den space, withdraw behind solid cover; wide swing has recovery. Thick hide, fat and claw materials within carcass accounting |
| **Fen Whistler**; humid marsh; II | Long-legged pursuit bird in bonded pairs; hunts small prey and defends nesting islands | Alternating whistles and lifted crest precede straight rush. Break approach with terrain, sidestep rush, leave nesting zone. Molted plume collection and egg-observation journal; nests cannot be harvested into unlimited offspring |
| **Glasscrest Stalker**; hot broken desert; III | Solitary reptilian skirmisher using shade retreats; insect/small-animal diet, water/heat limits activity | Crest flashes, sand scrape and short windup to fictional irritant spit. Use cover, change angle during windup, avoid sustained noon pursuit. Shed crest fragments; bounded fictional gland reagent, no real chemical recipe |
| **Hollow Listener**; large cave entrances; III | Social gliding mammal, 2–3 hunters; requires connected roost and cave prey; cannot navigate sealed rock | Clicking cadence accelerates before glide; dust disturbed below roost. Quiet route planning and pillars break approach; all sound cues have visual equivalents. Shed membrane/fur and acoustic journal discovery |
| **Rootgrip Matron**; forest soil banks; II | Burrowing arthropod defending brood; solitary adult; consumes invertebrates/carrion, never an infinite tunnel generator | Surface ridges and tapping before limited emergence. Avoid marked brood pockets and move sideways from the warned strike. Shed plates and fiber; player construction is not excavated by default |
| **Rime Mantle**; alpine scree/snowline; III | Patient stalker/scavenger, solitary or pair; constrained by nesting ledges and seasonal prey migrations | Dragged-tail track, falling grit and low posture before downhill bound. Stay away from ledge funnel, deny elevation advantage, retreat to broad ground. Insulating pelt; winter-route discovery |
| **Saltwake Hunter**; temperate coastal channels; IV | Aquatic cooperative hunter, 2–4; follows fish schools, avoids cramped shallows; cannot pursue onto land | Repeated fin arc and bubble trail precede lateral ram. Read water, reach shallows or shelter a boat behind rock; boat damage is bounded and configurable. Blubber/hide by weight; school-following observation |
| **Ash Veil**; warm volcanic woodland; II | Carrion specialist with brief mobbing display, groups2–6; rarely initiates predation, requires carcass/fruit supply | Raspy gathering calls and dust-wing display. Cover or remove exposed carrion, back away from feeding cluster; no unavoidable food theft from sealed stores. Shed quills, scavenging discoveries; contested carcass remains one shared resource |

Rare mature individuals can have named journal identities and a wider practiced move set, not raid-boss health inflation. Regional encounter variety comes from habitat and behavior combinations. Do not add unavoidable stacked disables, invisible attacks, spawning behind a player, or predator density tuned solely to combat frequency. Ecological threats should sometimes be watched, deterred, bypassed or relocated instead of fought.

## 10. Hunting, carcasses and material conservation

Animated anatomical regions include vital organs, major bones and non-vital soft tissue appropriate to the species. The server validates projectile history, impact position/angle and body pose with bounded latency reconciliation. Range does not enlarge hit regions. The confirmed deer chest/shoulder one-shot aspiration remains a capable outcome under suitable equipment and valid vital impact, not every shoulder hit.

Wounds can cause immediate incapacitation, reduced stamina, retreat or a trackable delayed outcome. Provide readable tracks, disturbed vegetation and journal signs without gratuitous gore. Limit wounded-animal suffering through workable tracking/recovery gameplay and configurable presentation; do not turn prolonged injury into a reward multiplier. Fictional coatings retain contamination flags; trimming cannot universally erase systemic contamination.

At death freeze slaughter mass, species profile, injuries/contamination and product pools into one carcass UUID. Replace ordinary fixed loot routes. For each pool, available mass equals species fraction of applicable carcass mass minus prior recovery, damage, scavenging and spoilage. Hide/horns preserve sensible anatomical counts; weight affects size/area/processing yield instead of creating extra limbs.

Proposed canonical mass accounting uses integer grams plus an explicitly owned fractional remainder where conversion requires it. All disjoint meat, fat, organ, hide/skin, bone and other recoverable pools, together with residue, removed fluid, gut contents and unusable loss, partition no more than the recorded available carcass mass. Wet mass, dry matter and water are separate declared measurements; none is added to the same total twice. Anatomical item counts are additional constraints, not extra mass. Scavenging, spoilage and disposal transfer material to named consumed/residue/environment accounts; rounding cannot silently create or erase recoverable portions.

Recurring product accumulation must debit declared feed, water and any permitted body reserves through a bounded species conversion model. Collection transfers an already accrued pool. On death, any uncollected production store enters the carcass partition or a separately identified transferred store exactly once; it cannot remain claimable alongside an unchanged full carcass budget. Maintain a clear weighable-mass definition including or excluding those stores, with an explicit conversion, so repeated milking/shearing and death cannot duplicate the same biological material.

Field dressing, hauling, hanging, skinning, butchery and preservation are alternative stages with one authoritative budget. Tool/skill improves bounded recovery and time, never total biological supply. Concurrent processing acquires a revisioned claim; atomic output and pool debit prevent two players taking the same cut. Portion remainders persist; stack merging retains mass, age and contamination. Recurring products have separate accrued-production pools, preventing milking/shearing from duplicating carcass mass.

## 11. Time, persistence, spawn control and budgets

TFC v4.2.12 calendar/config source confirms its configurable empty-server stop and default true. Adopt the existing Deep Primal decision, not every reference subsystem's catch-up behavior. [A20–A21]

The proposed initial default pauses unloaded animal physiology and recurring production; unloading does not create a catch-up entitlement. A later explicitly enabled policy may, while players remain online, use bounded calendar catch-up against recorded feed/water/production capacities. Advance to meaningful events, not one simulated tick per missed tick. Birth, food exhaustion and product-capacity events occur once with durable IDs. Inputs depleted during catch-up cannot support later production. Shutdown adds no wall-clock time. Sleep/time commands need an explicit shared skip policy; do not create free production or instant predator hunts.

Rendering culling is visual only. Distance-based AI scheduling reduces sensing/planning frequency without deleting authoritative state. Coarse population work cannot invent remote attacks on owned animals or players; preserve settled shelter/care state until actual interaction can be resolved. Busy-server catch-up must not produce surprise pen annihilation as a loading effect. Background mortality, if later chosen, requires a separately exposed policy.

All creation vectors require coverage: natural spawns; chunk generation; structure/data-driven entities; ordinary/trial spawners; spawn eggs/dispensers; reproduction, hatching and metamorphosis; bucket/container placement and captured-animal/fishing/trap release; conversions/infection/curing; splitting/reinforcements; weather/trap events; raids/patrols/settlement rituals; portal/dimension transfers; command/function summons; saved-entity loading; imports/admin tools; and external-mod API creation. Check policy before authoritative admission and log reason/source. Do not confuse all `minecraft` entities with mobs: vehicles/projectiles remain separate infrastructure. Existing entities follow the parent migration policy, with no automatic destructive cleanup here.

Use spatial queries and cached habitat summaries, never all-entity scans per animal. Budget active brains, perception queries, path expansions, sound/scent events, herd messages, births/materializations and carcass work separately. Proposed initial targets are at most one routine sense update per animal per second and amortized regional updates; combat interrupts use a bounded high-priority lane. Final quotas and millisecond budgets come from representative server profiles, with player/owned-state fairness and oldest-work counters. Defer work safely rather than skipping feed debits or reproductive commits.

## 12. Phased acceptance roadmap

| Phase | Deliverable | Required acceptance before promotion |
| --- | --- | --- |
| **AN-01** | Versioned species/individual schema and vanilla-policy adapters | Round-trip every state; invalid data rejected; all creation vectors exercised; protected existing-save migration fixture; zero duplicate UUIDs |
| **AN-02** | Weight, condition, feeding, forage and troughs | Shared grass/water consumption conserves reserves; starvation grace/UI works; no repeated-click gains; empty versus populated-server and shutdown tests |
| **AN-03** | Trust, handling, breeding, growth and traits | Adult/young caps and recovery verified; reloaded conception/birth/egg events execute once; bounded inheritance; default unloaded-pause tests and, only if enabled, separately qualified feed-limited catch-up; humane routine-care playtest |
| **AN-04** | Carcass and recurring-product pipeline | Randomized concurrent processing/splitting/storage/scavenging preserves the joint disjoint partition, units, remainders and recurring-product debits; damage/contamination/age persist; distinct hides/horns stay anatomical |
| **PRED-01** | One prey herd, one solitary predator, one pack | Perception occlusion/expiry, alarm limits, pursuit exit, injury and attack telegraphs tested; no path-budget stalls or omniscient reacquisition |
| **PRED-02** | Habitat populations and aggregate cohorts | Same seed/revision initialization; generation/load-order witnesses; materialize/dematerialize/crash conservation; no duplicate offspring or loot from virtualization |
| **PRED-03** | Twelve distinct wildlife roster prototypes; broader MMO threats tracked separately as THR-01–08 | Each habitat/role/counterplay observed; accessibility cues, water/cliff navigation and pack attack limits pass; no copied assets; density and prey capacity enforced |
| **AN/PRED-04** | Integrated seasonal farm/hunt expedition | Prepare winter, travel, sleep, reopen, process a scavenged carcass and move draft cargo; actual server/client captures, latency traces, memory/AI tails and no-state-loss evidence |

Every phase records source/artifact hashes, configuration, seed/calendar history, loaded-area pattern, runtime and sample counts. Unit/model tests prove bounded rules; native integration proves spawn/lifecycle/animation/hit behavior; playtests establish whether care and combat are understandable and enjoyable. Neither a roster document nor source-only helper counts as accepted fauna.

## 13. Sources and inspection limits

Primary sources checked October 29, 2026. No reference code or assets are copied by this specification. Licensing, selected dependencies and actual compatibility remain separate decisions.

| ID | Source | Evidence scope |
| --- | --- | --- |
| A01 | [TFC1.21.x developer index](https://github.com/TerraFirmaCraft/Documentation/blob/main/1.21.x/index.md) | Explicit TFC4.x/Minecraft 1.21.1 scope |
| A02 | [TFC v4.2.12 release](https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/tag/v4.2.12) | Published1.21.1 release identity |
| A03 | [TFC Wild Animals guide](https://terrafirmacraft.github.io/Field-Guide/en_us/the_world/wild_animals.html) | Habitat, predator/prey, home and activity descriptions |
| A04 | [TFC Animal Husbandry guide](https://terrafirmacraft.github.io/Field-Guide/en_us/food/animal_husbandry.html) | Familiarity, life stages and production; online defaults |
| A05 | [TFCAnimalProperties at v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/livestock/TFCAnimalProperties.java) | Persisted age/trust/size; use-related aging; blob9fb204453db61d3f91b5c93a0ccdc11b3a54b34b |
| A06 | [MammalProperties](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/livestock/MammalProperties.java), [HorseProperties](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/livestock/horse/HorseProperties.java) | Pregnancy/genes and bounded horse inheritance |
| A07 | [PredatorAi at v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/ai/predator/PredatorAi.java) | Actual home/activity/target memory |
| A08 | [PackPredatorAi](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/ai/predator/PackPredatorAi.java), [PredatorBehaviors](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/ai/predator/PredatorBehaviors.java) | Pack sharing, home limits and recent-hunt state |
| A09 | [Fauna at v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/common/entities/Fauna.java) | Typed climate/month/suitability data, not full populations |
| A10 | [Alex's Mobs1.22.9 publisher file](https://www.curseforge.com/minecraft/mc-mods/alexs-mobs/files/5698791) | MC1.20.1 release scope |
| A11 | [Alex EntityTiger,1.20 branch](https://github.com/AlexModGuy/AlexsMobs/blob/1.20/src/main/java/com/github/alexthe666/alexsmobs/entity/EntityTiger.java) | Inspected branch behavior; not binary qualification |
| A12 | [Naturalist2.0.7 NeoForge 1.21.1 publisher file](https://www.curseforge.com/minecraft/mc-mods/naturalist/files/9280587) | Exact inspected supported release |
| A13 | [Naturalist Lion,1.21.1-NeoForge](https://github.com/crispytwig/Naturalist/blob/1.21.1-NeoForge/src/main/java/com/starfish_studios/naturalist/server/entity/mob/Lion.java) | Registered goals versus merely defined class |
| A14 | [Naturalist AlertOthersPanicGoal](https://github.com/crispytwig/Naturalist/blob/1.21.1-NeoForge/src/main/java/com/starfish_studios/naturalist/server/entity/ai/goal/AlertOthersPanicGoal.java) | Alarm propagation/navigation |
| A15 | [Better Animals Plus11.0.10 Forge publisher file](https://www.curseforge.com/minecraft/mc-mods/betteranimalsplus/files/4318518) | MC1.19.2 scope |
| A16 | [BAP hunger target goal](https://github.com/itsmeow/betteranimalsplus/blob/1.19/common/src/main/java/dev/itsmeow/betteranimalsplus/common/entity/ai/HungerNearestAttackableTargetGoal.java) | Hunger-gated acquisition |
| A17 | [BAP IHaveHunger](https://github.com/itsmeow/betteranimalsplus/blob/1.19/common/src/main/java/dev/itsmeow/betteranimalsplus/common/entity/util/IHaveHunger.java) | Hunger persistence |
| A18 | [Animania Base publisher](https://www.curseforge.com/minecraft/mc-mods/animania) | Historical MC1.12.2 project/version and care systems |
| A19 | [Animania Farm publisher](https://www.curseforge.com/minecraft/mc-mods/animania-farm) | Farm addon, breeding/lactation and care infrastructure |
| A20 | [TFC ServerCalendar at v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/util/calendar/ServerCalendar.java) | Actual server/player calendar advancement |
| A21 | [TFC ServerConfig at v4.2.12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/v4.2.12/src/main/java/net/dries007/tfc/config/ServerConfig.java) | Empty-server pause default true; separate vanilla controls |
