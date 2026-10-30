# Deep Primal NPC and quest system

**Status:** researched design and implementation requirements, not shipped functionality. **Study date:** October 29, 2026 Pacific / October 30 UTC. The owner requests an approachable administration interface: create an NPC, choose its skin, assign quests, restrict them by level and questline, and let other authorized administrators build content. World of Warcraft informs the interaction pattern; all names, dialogue, artwork and implementation remain original.

This system must use Deep Primal's existing progression direction: persistent, accumulated, non-spendable character XP with a configurable curve and maximum level; profession skill and technology age are separate. No vanilla XP pool, spendable levels or compatibility escape restores vanilla XP. Quest rewards cannot subtract character progression. Preserve solo play, optional parties, physical cargo, local-only connected storage, frozen world generation and server authority. See the [owner decision register](https://github.com/Shnewbs/Deep-Primal/blob/main/docs/design-decisions-2026-10-18.md), especially Character XP and professions, Cooperation, and Time. This proposal does not silently assign the complete MMORPG feature set to the current engine milestone.

## 1. Lessons from primary MMORPG sources

| Reference and version | Verified behavior | Deep Primal application |
| --- | --- | --- |
| Blizzard, [Legion World Quests and You](https://news.blizzard.com/en-gb/article/20252892/legion-world-quests-and-you), September 23, 2016 | That Legion system combined a level gate, introductory quest and faction reputation; maps previewed objectives, rewards and remaining availability. | Separate eligibility predicates from objectives; preview reward and expiry before acceptance. Its historical level 110 is not a current cap or Deep Primal default. |
| Blizzard, [8.2.5 update notes](https://news.blizzard.com/en-us/article/23127427/8-2-5-content-update-notes), 2019; [Party Sync explanation](https://news.blizzard.com/en-us/article/23451087/explore-azeroth-with-friends-using-party-sync), June 9, 2020 | Party Sync coordinated quest state/phase and exposed party progress; replay had reward limits and excluded some repeatable or story-sensitive quests. | Party credit, invitation, replay and phase compatibility are separate policies. Joining a party must not silently satisfy prerequisites or reopen one-time rewards. |
| Blizzard, [The War Within UI and quest update](https://news.blizzard.com/en-us/article/24117139/user-interface-and-quest-updates-in-the-war-withintm), expansion UI reference | Campaign, important, repeatable, in-progress and turn-in content have distinguishable presentation and tracking. | Provide original symbols plus text labels and a legend; never make color the only state cue. |
| Square Enix, [quest icon guide](https://na.finalfantasyxiv.com/uiguide/know/faq-display/interface_npc_icon.html), live guide inspected on study date | Important/main-story quests differ; unavailable requirements direct players toward journal hints. | Explain level and prerequisite restrictions where disclosure is allowed. |
| Square Enix, [New Game+ manual](https://na.finalfantasyxiv.com/game_manual/newgame-plus/), displayed update October 13, 2026 | Story replay can change displayed NPC/story state without reverting overall progress; that replay awards no completion rewards. | Offer a distinct, reward-free preview/replay mode rather than erasing permanent completion. |
| ArenaNet, [Dynamic Events](https://www.guildwars2.com/en-gb/the-game/dynamic-events/), live description inspected on study date | Open-world cooperation can work without a formal party, with individual reward attribution and event outcomes affecting follow-up activity. | A later public-event mode may share participation while keeping personal reward receipts. Do not equate presence with contribution. |

These are selected documented patterns, not a claim to reproduce each game's current rules. Blizzard's proprietary transaction/storage internals are not disclosed by these sources; the durability design below is Deep Primal's proposed engineering contract.

### Minecraft authoring references and integration boundaries

[Easy NPC's dialogue guide](https://github.com/MarkusBordihn/BOs-Easy-NPC/wiki/Dialogs), edited September 27, 2026, distinguishes choosing a dialogue from enabling its response buttons; it describes locked/hidden options, reasons and server revalidation. Use that clarity in the native wizard. [FTB Quests' versioned 1.21.1 settings](https://docs.feed-the-beast.com/mod-docs/1.21.1/mods/suite/Quests/Developer/Quests/Settings/) separates visibility, all/one started/completed prerequisites, minimum dependency counts, sequential tasks and repeatability. Preserve those distinct meanings rather than one ambiguous “locked” checkbox.

[Citizens' commands](https://wiki.citizensnpcs.co/Commands) and [skin documentation](https://wiki.citizensnpcs.co/Skins) provide create/configure/skin workflow references; its Bukkit/Paper/Spigot context is not a verified NeoForge dependency, and direct wiki retrieval was restricted in this study. [BetonQuest 2.2 conversations](https://betonquest.org/2.2/Documentation/Features/Conversations/) demonstrate reusable dialogue branches; its [party selection](https://betonquest.org/2.2/Documentation/Scripting/Parties/) uses radius and conditions, which differs from persistent expedition membership. Every adapter must declare which meaning it implements.

These sources do not establish Deep Primal compatibility, complete publication transactions or crash-safe rewards. Reference documentation may span versions. No code or art is selected for reuse: Citizens, Easy NPC, FTB Quests and BetonQuest have different licensing/asset boundaries requiring exact-artifact review before any adoption. Native design remains independent and usable without installing these references.

See the companion [NPC and quest authoring reference study](NPC-QUEST-REFERENCE-STUDY.md) for inspected versions, primary sources, access limits and license distinctions.

## 2. Administration: create useful content without code

Entry: **Administration → NPCs & Quests**, with searchable NPCs, questlines, quests, drafts, published content and validation issues. A visible **Create NPC** button opens a guided flow. Every screen supports Back, Save Draft, Cancel, keyboard navigation and contextual examples; advanced data is optional. Autosave records a draft revision without changing the live world.

| Screen | Controls and resulting behavior |
| --- | --- |
| 1. Identity | Name, title, description, stable namespaced ID suggested automatically, owner/team, tags and locale. Choose an original Deep Primal humanoid/custom NPC rig backed by native `deepprimal` entity-type IDs; do not depend on a vanilla villager entity. Display names may change without changing saved identity. |
| 2. Skin and appearance | Search approved presets; rotate/zoom preview; select skin, body options, clothing, equipment display, nameplate and idle pose. Upload/import is shown only with asset-import permission. Equipment is cosmetic unless a separately configured service owns real items. Missing skin uses a named fallback. |
| 3. Placement | **Place where I am**, point-and-place ghost, or exact dimension/coordinates/yaw. Show collisions, floor safety, claim ownership and region restrictions. Choose stationary, bounded patrol or scheduled placement; a preview ghost neither blocks players nor starts objectives. |
| 4. Behavior and roles | Quest giver, quest receiver, dialogue, guide or separately supported service roles. Set interaction range, facing, protected/attackable policy, home/respawn and allowed schedule. Quest giver protection is the suggested default; combat/service roles require their own capabilities. |
| 5. Dialogue | Greeting plus eligible, unavailable, active, ready, completed and farewell branches. Edit text and typed response buttons; preview long/localized text. Conditions attach to branches. No programming language or server-command text box is required. |
| 6. Quests offered | **Add Existing Quest**, **Create Quest** or **Create Questline**. Show offer/turn-in roles separately, display order and labels. One NPC can offer many quests; one quest can have several approved givers/receivers. |
| 7. Availability | Minimum/optional maximum character level; completed quests; active/absent quests; story flags; supported profession, region or optional faction criteria. Visual **ALL conditions** and **ANY condition** groups provide AND/OR nesting, with plain-language summaries and an explanation preview. |
| 8. Preview and test | Select level, saved progression, flags, faction capability and party situation; show exactly which quests appear and why. Spawn a private test copy, simulate objectives or enter a test character. A large test banner identifies isolated state and disabled production rewards. |
| 9. Publish | Validation results, changes, dependencies, assets, affected active attempts, reward budget and approver. Full administrators can **Save & Publish** when server policy permits; delegated authors can **Submit for Review**. Publication installs one validated version atomically. |

The quest wizard similarly asks: title/category/story text; giver and receiver; eligibility; objectives; dialogue; rewards and choices; repeat/reset policy; sharing; preview; publish. Templates include Talk, Visit, Harvest, Craft, Deliver, Defend, Hunt, Escort and a simple chain. Unsupported templates are visibly unavailable. Authors never need to learn JSON to create the normal cases.

For questlines, a graph view and accessible ordered list show steps, branches, convergence, optional quests and endings. Connecting A to B suggests “A completion committed” as B's prerequisite; authors can change it deliberately. Merely placing two cards beside each other creates no hidden dependency. **Duplicate as New** creates new IDs and clears publication/award history; **Create Revision** preserves identity and history.

## 3. Delegated administrators and publication

| Permission | Allowed scope |
| --- | --- |
| Viewer | Inspect permitted definitions and validation; no edits. |
| NPC/Quest Author | Create/edit owned drafts in assigned namespaces/regions, use approved assets and run isolated tests. |
| Collaborator | Edit specifically shared drafts; ownership does not follow temporary party/guild membership. |
| Reviewer/Publisher | Review/publish allowed content and reward bands; optional separation from authors is server policy. |
| Operator | Retire/rollback content, repair individual attempts and inspect relevant audit records. |
| Owner | Delegate bounded roles, assign reward limits, approve asset sources and configure server-wide policy. |

Other administrators receive these capabilities directly through the UI or an existing permission adapter; the design must not require giving everyone unrestricted operator powers. Grants specify namespace, region, actions, asset import and reward ceilings. A grantor cannot delegate powers they do not hold. Every write and publish rechecks permission server-side, including after an edit session opens.

Each save uses an expected draft revision. Conflicting editors receive a diff and merge choices; last writer never silently overwrites another administrator. The audit records actor, timestamp, operation, before/after version hashes, review, test receipt, publish result and reason for privileged repairs. Permission changes and ownership transfers are recorded too. Removing an author does not orphan published content.

Rollback changes the active definition pointer; it does not undo already earned XP, reissue rewards or erase completion. Emergency disable prevents new acceptance and provides an explicit active-attempt/turn-in policy. Repairs are typed operations such as Resume, Migrate Objective, Refund Escrow or Mark Rewarded With Receipt, not raw data deletion.

## 4. Player interaction and all quest states

Targeting an NPC shows name, role and the most relevant original marker; interaction opens dialogue and a short sorted quest list. Priority is claimable reward/turn-in, active objectives, available story, available side/repeatable, then disclosed unavailable content. A quest detail panel presents requirements, objectives, location hints, rewards, choices, repeat interval and expiry before **Accept**.

| State | Player presentation and permitted action |
| --- | --- |
| Hidden | No marker or quest details. Used for spoilers or undiscovered content; unrelated NPC dialogue remains available. |
| Locked/disclosed | Disabled offer with permitted reasons: “Requires level 5” or “Finish First Harvest.” Secret predicates use a safe generic hint. |
| Available | Accept/Decline; server revalidates eligibility and log capacity. |
| Active/incomplete | Progress and reminders; no premature Complete button. Track, untrack and allowed abandonment remain available. |
| Ready | All objective requirements satisfied; return/turn-in location and needed consumables shown. Revalidate at turn-in. |
| Completing | Brief pending state while durable work resolves; repeated requests return the same operation result. |
| Reward owed | Completion committed with escrow entitlement; choose/claim through an authorized interaction. Full inventory does not drop or delete rewards. |
| Rewarded/claimed | All owed grants delivered; permanent receipt remains inspectable. One-time completion is already durable when reward entitlement commits. |
| Cooldown | Next reset/time remaining and permitted repeat rules. Not offered as a new attempt yet. |
| Failed/expired/abandoned | Explain cause and recovery/retry policy; preserve permanent completion and owed rewards. |
| Suspended/retired | Content or integration unavailable; preserve progress and give an operator-supported resolution path. |

Unavailable is a computed offer result, not necessarily a saved failure state. NPC visibility, dialogue eligibility, quest acceptance and turn-in are independent predicates. First release should phase dialogue/appearance, not conflicting solid terrain. Shared-world story changes require explicit world-state authority; a personal flag cannot delete another player's settlement or force everyone into a private story phase.

Condition names distinguish `completion_committed` from `all_rewards_claimed`. The sample chain uses the former, so an item reward held in escrow does not block the next story step. For delivery objectives, readiness means eligible inputs are available; consumed progress commits only with the turn-in transaction, never with the visual Ready marker.

The server evaluates conditions against one consistent player/world snapshot. Normal level/questline gates apply at acceptance; maximum-level gates do not invalidate an already accepted quest merely because the player levels up. Explicit ongoing gates, item possession, receiver validity and critical permissions recheck later. Changes of party/faction/phase suspend incompatible work visibly rather than discard it. No packet may request an arbitrary hidden quest by ID and bypass these checks.

## 5. Typed definitions and a farming/protection chain

Definitions use stable IDs, schema version, immutable author revision, content hash and ownership. Runtime attempts reference the accepted revision. Predicates are pure bounded queries: `all`, `any`, restricted `not`, `character_level`, `quest_state`, typed scoped `flag`, `profession_level`, `region`, and optional `faction_standing`. Unknown capabilities fail validation, never evaluate as automatically true. Predicate limits include nesting, node count and query cost.

Objectives declare type, target selector, count, scope, provenance, progress semantics and credit policy. Supported types include conversation, enter-region, server-observed harvest/craft, possess, deliver/consume, resolve-threat, defeat, escort/checkpoint and bounded timed defense. **Possess** reads current inventory; **harvest/craft** counts proven events after activation; **deliver** consumes eligible goods exactly once. They are not interchangeable.

Actions are an allowlisted typed registry: offer/accept quest, show dialogue, set an authorized scoped flag, grant defined progression, transfer escrow items, unlock a guide entry or activate a supported encounter. No arbitrary JavaScript, shell, console command, URL execution or administrator impersonation is accepted as quest content. Optional code integrations are separately installed trusted server adapters exposing typed validation and idempotent commit contracts.

Illustrative authoring export below uses proposed IDs and event adapters. It is valid JSON, not a claim these crops/ecology integrations exist. Publication must resolve every ID and capability against the selected runtime.

```json
{
  "schema_version": 1,
  "id": "deepprimal:homestead_help",
  "author_revision": 1,
  "scope": "character",
  "npc": {
    "id": "deepprimal:mara_farmer",
    "display_name": "Mara",
    "entity_type": "deepprimal:npc",
    "rig": "deepprimal:humanoid",
    "skin": "deepprimal:approved/farmer_01",
    "roles": ["quest_giver", "quest_receiver"],
    "protected": true
  },
  "quests": [
    {
      "id": "deepprimal:meet_mara",
      "title": "A Place to Grow",
      "repeat": {"kind": "once"},
      "requirements": {"character_level": {"min": 1}},
      "objectives": [
        {"id": "talk", "type": "conversation", "npc": "deepprimal:mara_farmer", "node": "farm_introduction"}
      ],
      "turn_in": {"npc": "deepprimal:mara_farmer"},
      "rewards": {"character_xp": 20}
    },
    {
      "id": "deepprimal:first_harvest",
      "title": "First Harvest",
      "repeat": {"kind": "once"},
      "requirements": {"all": [
        {"character_level": {"min": 2}},
        {"quest_state": {"id": "deepprimal:meet_mara", "is": "completion_committed"}}
      ]},
      "objectives": [
        {"id": "grow", "type": "harvest", "target_tag": "deepprimal:starter_food_crops", "count": 4,
         "provenance": "self_cultivated", "maturity": "harvestable", "since": "objective_activation", "credit": "personal"},
        {"id": "supply", "type": "deliver", "after": ["grow"], "npc": "deepprimal:mara_farmer",
         "item_tag": "deepprimal:starter_food_crops", "count": 4, "consume": true, "minimum_quality": "edible"}
      ],
      "rewards": {"character_xp": 60, "guide_unlocks": ["deepprimal:crop_rotation"]}
    },
    {
      "id": "deepprimal:protect_the_fields",
      "title": "Protect the Fields",
      "repeat": {"kind": "once"},
      "requirements": {"all": [
        {"character_level": {"min": 3}},
        {"quest_state": {"id": "deepprimal:first_harvest", "is": "completion_committed"}},
        {"any": [
          {"flag": {"scope": "region", "id": "deepprimal:field_threat_detected", "equals": true}},
          {"flag": {"scope": "character", "id": "deepprimal:guard_training_approved", "equals": true}}
        ]}
      ]},
      "objectives": [
        {"id": "protect", "type": "resolve_threat", "region": "deepprimal:mara_fields", "count": 1,
         "allowed_outcomes": ["repelled", "relocated", "defeated"], "credit": "eligible_party_contributors"}
      ],
      "turn_in": {"npc": "deepprimal:mara_farmer"},
      "rewards": {"character_xp": 80, "set_character_flags": {"deepprimal:homestead_trusted": true}}
    }
  ]
}
```

The dialogue introduces farming before a real cultivated harvest and consumptive delivery; protection follows only after that chain and a genuine regional threat or approved training path. Crop species, quality thresholds, counts and XP are illustrative tunables, not approved balance. Resolve aliases/tags and show accepted crop examples in the wizard.

Harvest events need crop-instance/planting/harvest identity and actor/plot permission; bought goods or repeatedly dropping crops do not count as growing them. Delivery preserves freshness, quality, mass and contents and transfers those goods to an explicit sink/stock account. Protection events need a real threat identity, eligible animal/encounter, resolution and contribution record. Killing an unrelated tame animal, spawning creatures through an unauthorized command or replaying a death packet provides no credit. Nonlethal outcomes require actual ecology support; the quest cannot invent them from a dialogue click.

## 6. Cooperation, objectives and repeat policies

Every objective chooses personal, eligible party contributors or explicitly supported public-event credit. Each recipient must have the correct active quest/objective/revision, valid phase and applicable level/acceptance status. Party invitations share an offer, not progress or completion automatically; recipients accept separately after eligibility checks. Joining late does not retroactively earn completed harvests or kills.

Contribution policy defines distance/dimension, participation window, damage, healing, defense/support and pet ownership. Last-hit-only credit is optional rather than accidental. Offline, absent or idle members receive no new credit by default. Membership snapshots bind to the event; inviting somebody after a kill cannot duplicate its recipients. A kill and its quest credit do not create an extra animal drop or bypass individual weight-based yields. Party loot ownership and quest credit remain separate.

Collections/turn-ins consume each player's own required goods unless a quest explicitly uses a shared contribution account. A shared account debits actual contributions; it never clones one stack into every member's inventory. Personal rewards have individual operation IDs. Leaving a party preserves earned personal progress; escort/public encounter outcomes follow the published attempt membership rules.

Repeat kinds are once, bounded repeatable, cooldown, daily, weekly or seasonal window. Store the repeat scope and nondecreasing server reset epoch independently of author revision. Default proposal uses the shared game calendar to preserve existing time policy; an administrator may explicitly choose real UTC/server-time reset epochs for an MMO-style schedule. This does not run farm/NPC production while the world is closed. Show the selected clock and next reset; local client time cannot reset quests.

A reset cannot erase owed rewards or duplicate an active attempt. Define expiry/grace and whether unfinished attempts carry forward; allow only one active attempt per quest/scope and one reward receipt per eligible cycle. Rewinding game time, daylight-saving changes, downtime and publishing a new revision do not produce extra cycles. Story replay is separate: suggested default is reward-free, with permanent quest flags/XP unchanged. Any paid mentoring replay needs its own bounded reward identity and approval.

Failure conditions are authored explicitly: death does not fail every quest automatically, and ordinary disconnect is suspension rather than a covert penalty. Timed encounters declare pause/offline behavior. Abandonment may remove only proven temporary quest-owned items under its transaction policy, never matching unrelated player goods. Acceptance kits and loaned tools have durable attempt-bound grants and return rules, preventing abandon/reaccept from supplying infinite free equipment.

## 7. Durable completion and rewards

The server accepts objective events only from authoritative adapters. Each event has world/session provenance, source-object/operation ID, actor, server sequence and affected attempt. Event deduplication persists with progress; a dedup window is not discarded while an old event remains replayable. Ordering handles delayed events and objective activation boundaries explicitly. Client animation, tracking or dialogue acknowledgements do not award progress.

A completion key includes world identity, character UUID, stable quest ID, attempt ID and repeat epoch. Definition revision is recorded but cannot manufacture a fresh one-time claim. Reward choice, input consumption and result are bound to that key. Repeated clicks, network retries, reconnects and two receivers racing return the original receipt.

Completion requires a durable transaction across quest state, required inventory debits, XP, flags and reward entitlement:

1. Recheck attempt/receiver/objectives and reserve exact eligible input stacks under authoritative inventory ownership. Validate reward choice and funded reward limits.
2. Journal the operation, before/after revisions, consumption, capped XP result, flag changes and deterministic escrow grant IDs durably.
3. Commit all participants through the declared inventory/progression persistence protocol; publish a committed receipt only after its durability barrier.
4. Recover incomplete operations before admitting conflicting player actions. Replay committed participants idempotently; abort uncommitted reservations without losing inputs.

This requires an implemented host transaction/recovery adapter. Saving a quest JSON file and later mutating vanilla inventory is not atomic. If the selected inventory or external reward adapter cannot provide durable idempotent participation, that reward/objective type remains unavailable until integrated; tests must prove the actual native save/recovery path.

Item rewards first become durable per-character escrow entitlements. **Claim** reserves inventory space and transfers a grant atomically; a full inventory leaves it owed, showing required space. No automatic world drops, item deletion or duplicate mailing fallback. Claim at the configured NPC/service; the quest journal reports entitlements rather than becoming general remote deposit/withdraw storage. Escrow capacity/backpressure is explicit and validated before consuming inputs; it must not strand a reward in an unbounded hidden queue.

XP uses the central character progression service, caps new awards safely and shows actual XP gained. Lowering a cap or changing its curve must not delete previously earned progression; that requires a separately defined migration/display policy. At-cap behavior is explicit; excess does not secretly convert into currency or vanilla XP. Profession rewards require their own supported service and attribution. Item grants declare approved source stock or a bounded authored reward budget; repeatables cannot print finite ore or bypass the intended resource economy through accidental cycles. Choice rewards are frozen once committed. Abandon/reaccept, rollback, character death and content retirement never refund consumed inputs while retaining a paid reward.

## 8. NPC lifecycle, skins and saved revisions

Separate NPC template ID, placement/spawn ID and runtime entity UUID. Persist placement, home, role bindings, author revision and lifecycle state. Chunk unload is suspension, not death or a new spawn entitlement; restart reconciles saved spawn IDs before creating entities. Protected quest NPCs yield no farmable combat XP/items. Attackable NPCs require explicit death, respawn, loot, active-quest recovery and interaction policies.

Patrols/pathfinding and schedules have bounded regions/work. Do not force unlimited chunk loading to find an NPC. If a receiver is absent, retain the attempt, show its last known permitted location and provide a published alternate receiver/recovery policy. Moving or retiring a placement updates quest navigation without silently deleting completions.

Skins are referenced by approved immutable asset ID/hash. The normal picker uses bundled/world-pack presets; an authorized import validates supported model dimensions, file type, decoded size and source rights. Server imports use approved sources, bounded downloads/decoding and explicit asset ownership; clients do not fetch arbitrary author URLs. Cache by content hash with memory/disk limits, timeout and a recognizable fallback. Offline play must retain a working appearance; a username change or remote skin update must not silently alter a published NPC. Asset promotion is independent of quest publication permissions.

Persist immutable published definitions plus player attempts, objective progress/dedup state, accepted version, eligibility decisions requiring pinning, permanent completion, repeat epochs, escrow and receipts. A cosmetic text fix may be compatible; changed objective meaning/rewards requires a migration plan. Keep active attempts pinned by default. Migrations map objective IDs and progress explicitly, preserve rewards already owed and report unsupported changes as suspended. Tombstones retain deleted IDs/history. Backup/restore includes quest, inventory, progression and reward journals as one coherent boundary; restoring only completion data must not enable claiming a second copy.

## 9. Validation, administration tests and implementation order

Publication validates IDs/capabilities/assets, condition types, impossible level ranges, missing prerequisites/receivers, incompatible phases, objective dependency cycles, questline cycles and unreachable endings. Repeat edges are explicit reset transitions, not ordinary prerequisite cycles. Check mutually exclusive branches, bounded counters, consumptive item matches, world-flag privileges and repeatable reward loops. Warnings show location/author action; missing required capabilities block publication.

**Test as Character** evaluates real compiled conditions against isolated state. Cases include below/exact/above level, A incomplete/rewarded, AND/OR branches, secret hints, full inventory, no skin network, missing NPC, party joins/leaves, at-cap XP and every repeat boundary. Simulated grants remain in a separate test world/account namespace; test items/flags cannot transfer into production. A saved test receipt names definition hashes and scenarios.

Qualification additionally injects crashes before/after every completion/claim barrier, duplicates/reorders events, races two turn-ins, restarts with escrow, rolls back definitions and tests backup restoration. Assert conservation of consumed/delivered items, at-most-once reward effects, no progression decrease and recoverable attempts. Exercise native inventory, modded stack metadata, fresh/decayed crops, source animal attribution and dedicated-server operation; pure schema tests do not establish these outcomes.

Performance work indexes active objectives by event type/target/region, rather than scanning all quests per player/tick. Incremental eligibility updates respond to level/flag/quest changes. Bound predicate cost, subscriptions, NPC updates, dialogue pages, client packets, authored assets and storage retention; surface diagnostics for stuck attempts, rejected events and reward reconciliation without logging private dialogue unnecessarily.

Implementation slices: (1) typed content/permissions and draft editor; (2) protected NPCs, skin picker and Talk/Visit quests; (3) durable attempts, XP and transactional deliver/reward escrow; (4) full wizard, chains/AND-OR, preview and collaboration; (5) farming/ecology objectives and party credit; (6) explicit repeats, migration/rollback and optional public events/factions. Every slice needs actual acceptance evidence. Farming, predator ecology, factions, economy and independent-host capabilities remain separately implemented dependencies; an attractive quest editor cannot substitute for them.

## 10. Encounter, faction and villain authoring extension

The owner's MMO direction includes hostile factions and named villains alongside animal predators. Extend the same authoring workspace with **Create Encounter** and **Create Villain Arc**, using the [encounter specification](MMO-THREATS-AND-ENCOUNTERS.md) and [candidate faction catalog](HOSTILE-FACTIONS-AND-VILLAINS.md). Normal authors choose an approved custom rig/skin, creature or faction, role, home/camp bounds, behavior preset, level guidance, activation conditions, questline links and rewards. A hostile template is not a disguised vanilla entity. A skin alone never installs combat abilities or changes authority.

Permissions distinguish dialogue/NPC editing, hostile placement, encounter publication, world-state changes, economy grants and any exceptional terrain interaction. Region ownership and build protection are rechecked at activation, not only at editor placement. Ordinary quest authors cannot enable sieges, rewrite another region, summon unbounded reinforcements or publish arbitrary scripts. The preview shows safe exit routes, attack footprints, authorized terrain effects, spawn/path quotas, solo/story alternatives and repeat/permanence policy before publication.

Level and questline conditions can gate an offer, an explicitly authored encounter activation or an approved content region. They do not grant player-specific immunity, secretly resize hit regions, turn private quest progress into a global terrain phase or replace shared-world state. Named villain template identity, actual world actor/encounter identity and a character's quest record remain distinct. Defeat, surrender, negotiation or disruption advance only the supported typed resolution paths; a private repeat cannot resurrect a globally resolved villain or reissue physical loot. Repeatable echoes/training versions are explicitly labeled and carry separate economy/provenance rules. Existing completion and reward recovery semantics continue to apply.
