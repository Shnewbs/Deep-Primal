# NPC and quest authoring: reference study for Deep Primal

**Inspected:** October 29, 2026 Pacific / October 30 UTC. **Status:** research and proposed native design; no dependency selected, code copied, compatibility certified or runtime feature implemented. Four Minecraft tools were examined through maintainer documentation and repositories: Citizens2, Easy NPC, FTB Quests and BetonQuest. AzerothCore supplies an additional MMORPG administration comparison. Mutable documentation and branch names below identify the inspected scope, not immutable release pins or a claim of latest-version support.

## Product target and existing decisions

The owner's requested experience is an easy native admin workflow: create an NPC, choose its skin, attach dialogue and quests, restrict availability by character level or questline, and let multiple authorized admins author content. A finished screen is insufficient unless the server owns permissions, eligibility, progress, publication and reward delivery.

Existing Deep Primal decisions remain binding. `docs/design-decisions-2026-10-18.md:71,260,288–292` specifies persistent, non-spendable MMO character XP, a configurable maximum level, a separate profession track, and complete replacement of vanilla XP without a legacy pool. No numeric character cap has been selected. The same document `:266` keeps parties/guilds optional and solo progression usable. Therefore a reference tool's vanilla experience-level condition or experience cost must not silently become Deep Primal's character-level semantics.

## Reference systems and appropriate lessons

| Reference and inspected scope | Established capabilities | Adopt or adapt | Boundary and caution |
| --- | --- | --- | --- |
| **Citizens2**, current official command/skin wiki and repository `master`; wiki permissions described as 2.0.x | NPC creation/copy/configuration GUI; player-name and custom skin input; command permissions and ownership concepts | Fast create/select/copy, immediate model preview, explicit stable NPC identity, granular editing capabilities | Bukkit/Paper/Spigot ecosystem, rather than a verified NeoForge dependency. NPC command traits are not a complete native quest-state engine [C1–C4]. |
| **Easy NPC**, official wiki updated July–September2026 and repository `1.20.1` | In-game configuration wand/UI, branching dialogue conditions/actions, presets, skin input, server-controlled feature/command authority | Guided property pages, previewed branches, reusable templates and visible unavailable-option reasons | Documentation spans versions; inspected branch is not evidence of 26.x compatibility. Code licensing excludes creative assets [E1–E4]. |
| **FTB Quests**, official 1.21.1 docs plus repository `main` changelog containing 26.1.2.9 | Graph-based quests/tasks/rewards/dependencies, team behavior, delegated editor permission and documented undo/redo | Searchable graph and outline views, dependency picker, cycle checks, per-author editing privilege and reversible drafts | Versioned feature availability must be checked per build. Team progress differs from expedition-party participation; undo is not backup or transactional publish [F1–F5]. |
| **BetonQuest**, versioned **2.2** docs and project repository | Named conversation options, conditions/events/objectives, NPC binding and cross-conversation pointers; conditional party selection | Typed condition/action blocks, reusable dialogue fragments and explicit objective-state inspection | Plugin scripting and broad administration privileges are not a safe delegation model for every quest author. Version2.2 examples are not a claim about current3.x syntax [B1–B5]. |

### Citizens: skin and identity workflow

Citizens separates creating/selecting an NPC from changing its presentation. Its skin documentation distinguishes a retained player skin, following later changes, URL-backed skins and signed texture data; URL handling may use MineSkin. The command reference exposes both configuration GUI and powerful command execution, including temporary privileges and experience costs. Its permission wiki explicitly warns that the page may lag current `plugin.yml`. [C1–C3]

**Deep Primal inference:** provide a skin browser/upload preview with model-arm choice, cached content identity, attribution and a deliberate update policy. Keep NPC identity independent of displayed name or skin owner. A failed fetch retains the previous accepted skin and reports the reason. Server fetching needs bounded image sizes, decoded dimensions, timeouts, allowed resource schemes and address/redirect checks; client-supplied URLs must not become arbitrary server network/file access. These are proposed protections, not audited defects in Citizens.

### Easy NPC: useful guided editing, with authority checked again

The maintainer's dialogue guide documents conditional branches, disabled-versus-hidden unavailable buttons, explanatory locks, server revalidation on interaction and server-tracked execution limits. It also distinguishes dialogue-selection conditions from individual button conditions. Its permissions guide clamps effective action authority to NPC/server limits and exposes separate feature rules; the actions guide describes explicit allowlisting for commands executed as a player. [E1–E3]

**Deep Primal inference:** copy the clarity, while exposing native typed actions such as start quest, show dialogue, consume approved objective items or claim reward. A quest author should not need console command strings to perform common actions. Always offer a recoverable close/back route; an empty set of eligible options must not trap a player. The reference's button count/day limit is a useful interaction control, but the inspected docs do not prove a durable exactly-once reward transaction across process failure.

### FTB Quests: make dependency semantics visible

The settings reference distinguishes all/one started or completed dependencies, minimum dependency counts, optional/repeatable quests, sequential tasks and visibility rules. The player guide distinguishes shared task completion from reward claiming and describes team rewards. These are useful separate axes rather than one ambiguous shared toggle. [F1,F2]

The changelog documents `ftbquests.editor` delegation, dependency-loop checking, stable serialized ordering, and100-level undo/redo in the relevant history. It explicitly cautions that undo is not a backup. It also records permission and persistence fixes across versions. [F3]

**Deep Primal inference:** show plain language summaries—“Complete either A or B; reach character level10”—beside the graph. Separate hiding, starting, progressing, turning in and claiming. Editing permission is not permission to award rewards, run commands or publish someone else's questline. Keep a textual outline and keyboard actions alongside the graph so large campaigns remain navigable.

### BetonQuest: reusable story logic and party semantics

BetonQuest2.2 conversations use named NPC/player options with pointers, conditions and events, can bind to Citizens NPC IDs, and can cross conversation/package boundaries. Its conditions include vanilla experience, tags, points and other predicates; those are reference primitives rather than Deep Primal's progression model. [B1,B2]

Its party feature selects players by radius and conditions at event time rather than requiring a persistent pre-created party. Administrative commands can modify objectives, tags, points and events; reload does not mean all player/database state is rebuilt. [B3,B4]

**Deep Primal inference:** support reusable fragments and test a dialogue as a selected sample character, but distinguish real party membership from nearby eligible participants. Preview must reveal why a branch is unavailable without leaking hidden story data to ordinary players. Treat reload, content publication, player-state migration and administrative repair as different operations.

## MMORPG contrast: AzerothCore

AzerothCore's official world-schema documentation separates quest definitions from player state. `quest_template` includes a unique quest ID and minimum level; `quest_template_addon` expresses previous/next quests, exclusive groups, maximum level and skill requirements. Its GM command reference separates adding a quest, completing objectives, rewarding it, removing it and inspecting status. [A1–A3]

This separation is valuable, while signed-ID conventions and database flags are unsuitable as the main author UI. Deep Primal should render typed prerequisites and exclusive-choice groups with validation and a human explanation. Completing an objective must not implicitly issue every reward, and removing a quest for support must not erase issued-reward history. No proprietary commercial MMO editor, production transaction implementation or MMO asset set was inspected.

## Native admin workflow proposed for Deep Primal

One **Create NPC** wizard should produce a reviewable draft through these steps:

1. **Identity and placement:** name, stable ID, template, world/location and allowed behavior. Preview collision and interaction range; placement authority is scoped to the author's permitted world/region.
2. **Appearance:** body/model, skin and equipment. Preview lighting and variants; validate resources and keep attribution. Appearance changes never reset quest progress.
3. **Dialogue:** start from greeting, quest offer, in-progress reminder, ready-to-turn-in and unavailable templates. Add typed choices and conditions; show a sample-player preview.
4. **Quest:** choose objective types and targets from registries; specify quantities, order, location and progress scope. Distinguish possession/detection from item consumption and distinguish kill count from an authored encounter outcome.
5. **Eligibility and rewards:** character/profession level, prerequisite states, mutually exclusive branch, repeat/cooldown policy, per-player/party credit and typed reward. Show nominal/effective XP at the cap without creating spendable levels.
6. **Validate and publish:** display a compact change summary, errors, warnings, affected NPCs/questlines and active-run policy. Authors can save drafts and test privately; authorized publishers activate the reviewed revision.

Start with useful defaults and progressive disclosure. A wizard may generate one canonical typed definition, editable later in the graph/outline editor. It must not maintain a second divergent configuration. Batch duplicate/rename should preserve referenced IDs or require an explicit remap. Localization keys and display text remain separate from identity.

## Multiple admins: publication is a content transaction

The following is **new proposed design**, not a feature proven in the four tools' inspected documentation. Store immutable definition revisions with author, base revision, change summary, validator version, content hashes and affected references. Save a draft with an expected-revision check; conflicting edits show a resolvable diff rather than last-writer-wins. Independent drafts on separate NPCs/questlines should not block one another.

Grant capabilities by scope: view, create draft, edit owned/assigned content, test, review, publish, retire, and repair player state. A regional builder may place an approved NPC without changing its reward budget. Reward classes, maximum amounts and any privileged extension action require separate authority. Recheck permission on every server request and publication, including after a role is revoked while an editor remains open.

Publish a coherent bundle of NPC/dialogue/quest references atomically. Missing resources or invalid dependencies leave the previous active revision intact. An active quest instance pins a definition revision or follows an explicitly reviewed migration; a publisher cannot silently change collected items or move the finish line. Rollback selects a prior **definition** revision and records a new deployment event. It does not rewind completed quests, issued rewards or the character economy. Retirement must leave an explicit completion, migration or cancellation route for active instances.

## Party credit and exactly-once rewards

Every objective declares its credit model: personal, shared party counter, individually eligible participants, or explicitly global event. Select membership/eligibility at a documented boundary, including distance, participation, quest acceptance and world. Define joining/leaving, death, disconnect, rejoin, multiple attackers and late events. A persistent team must not automatically receive a distant expedition reward. Optional parties cannot become a hidden solo progression requirement.

For each quest instance and repeat occurrence, use a durable claim key scoped to character or declared party reward owner, quest revision/instance, objective occurrence and reward ID. The authoritative server rechecks eligibility, marks the accepted transition and records its grant outcome. Duplicate clicks, reordered network requests, reconnect and restart must return the same result. Inventory consumption, reward insertion and XP/account effects need one recoverable transaction or explicit durable pending state; a command list executed sequentially is not that contract.

When inventory is full, retain a pending claim or reject before consuming costs. Never silently drop items or repeat partial awards. External effects use a transactional outbox and an idempotent recipient contract; unsupported non-idempotent external commands cannot be advertised as exactly-once. At the character cap, record nominal and effective XP once under the selected cap policy. No action spends character XP or restores vanilla XP. Administrative grants/repairs name actor, reason and target, retain prior receipts and require their own permissions.

## Validation and acceptance before release

Compilation should reject duplicate IDs, missing NPC/quest/resource references, prerequisite cycles, unreachable mandatory objectives, empty eligible dialogue exits, contradictory gates, invalid ranges, unsupported registry targets, unbounded scripts and non-finite rewards. Some story cycles are intentional; distinguish bounded repeatable instances from illegal prerequisite cycles. Summaries and validator errors should identify the exact field and reference path.

Required adversarial fixtures include two admins publishing from one base; revoked editor privileges; malicious skin URL/oversized image; nonexistent objective target; cyclic prerequisites; full-inventory turn-in; repeated reward requests across a crash; party join/leave/disconnect; definition rollback with active instances; duplicate kill/event delivery; XP-cap behavior; and a player removed from the required questline between offer and acceptance. A native end-to-end test must create, skin, gate, complete, claim, reopen and inspect an NPC quest using actual server authority. UI screenshots or source-only tests do not establish this lifecycle.

## License, version and evidence boundaries

Citizens' repository identifies **OSL3.0** [C4]; BetonQuest identifies **GPL3.0** [B5]. EasyNPC's inspected license applies **MIT to code while excluding images/models/other assets** [E4]. FTBQuests' inspected project metadata identifies **All Rights Reserved** [F4]. These are recorded labels, not a determination that any code/assets may be embedded in Deep Primal. Prefer independent native design; any future reuse requires checking the exact selected artifact, dependencies, notices and terms. AzerothCore is an MMORPG emulator reference, not a Minecraft compatibility layer [A4].

The inspected sources do not establish multi-admin draft approval, conflict-safe atomic publication, definition rollback with player-state migration, or durable exactly-once cross-system rewards as complete features. This is an evidence limit, not a claim that those products categorically lack them. No inspected branch/wiki certifies support for Deep Primal's current or future NeoForge target. Pin and test any future optional integration independently.

## Primary source register

- **C1:** [Citizens command reference](https://wiki.citizensnpcs.co/Commands), mutable2.x wiki; indexed official content inspected, direct page fetch returned403.
- **C2:** [Citizens skins](https://wiki.citizensnpcs.co/Skins), same access boundary; skin retention/following and URL/signature workflow.
- **C3:** [Citizens permissions](https://wiki.citizensnpcs.co/Permissions), official indexed content warns to check current `plugin.yml`.
- **C4:** [Citizens2 license, master](https://github.com/CitizensDev/Citizens2/blob/master/LICENSE), OSL3.0 text inspected; [project repository](https://github.com/CitizensDev/Citizens2).
- **E1:** [EasyNPC Dialogs](https://github.com/MarkusBordihn/BOs-Easy-NPC/wiki/Dialogs), displayed revision September27,2026.
- **E2:** [EasyNPC Actions](https://github.com/MarkusBordihn/BOs-Easy-NPC/wiki/Actions), displayed revision August4,2026.
- **E3:** [EasyNPC Permissions](https://github.com/MarkusBordihn/BOs-Easy-NPC/wiki/Permissions), displayed revision July25,2026.
- **E4:** [EasyNPC license,1.20.1 branch](https://github.com/MarkusBordihn/BOs-Easy-NPC/blob/1.20.1/LICENSE.md) and [maintainer repository](https://github.com/MarkusBordihn/BOs-Easy-NPC).
- **F1:** [FTBQuests1.21.1 settings](https://docs.feed-the-beast.com/mod-docs/1.21.1/mods/suite/Quests/Developer/Quests/Settings/).
- **F2:** [FTBQuests1.21.1 player questbook](https://docs.feed-the-beast.com/mod-docs/1.21.1/mods/suite/Quests/Player/Questbook/Navigating/).
- **F3:** [FTBQuests changelog,main](https://github.com/FTBTeam/FTB-Quests/blob/main/CHANGELOG.md); observed26.1.2.9 heading; undo26.1.2.3, delegation1.20.1 history, cycle-check1902.4.16 history.
- **F4:** [FTBQuests project license metadata](https://github.com/FTBTeam/FTB-Quests/blob/main/fabric/src/main/resources/fabric.mod.json).
- **F5:** [FTBQuests1.21.1 author commands](https://docs.feed-the-beast.com/mod-docs/1.21.1/mods/suite/Quests/Developer/Commands/) and [quest creation](https://docs.feed-the-beast.com/mod-docs/1.21.1/mods/suite/Quests/Developer/Quests/).
- **B1:** [BetonQuest2.2 conversations](https://betonquest.org/2.2/Documentation/Features/Conversations/).
- **B2:** [BetonQuest2.2 conditions](https://betonquest.org/2.2/Documentation/Scripting/Building-Blocks/Conditions-List/) and [objectives](https://betonquest.org/2.2/Documentation/Scripting/Building-Blocks/Objectives-List/).
- **B3:** [BetonQuest2.2 parties](https://betonquest.org/2.2/Documentation/Scripting/Parties/).
- **B4:** [BetonQuest2.2 permissions/commands](https://betonquest.org/2.2/Documentation/Configuration/Commands-and-permissions/).
- **B5:** [BetonQuest maintainer repository](https://github.com/BetonQuest/BetonQuest), GPL3.0 metadata;2.2 docs also display GPLv3.
- **A1:** [AzerothCore quest_template](https://www.azerothcore.org/wiki/quest_template).
- **A2:** [AzerothCore quest_template_addon](https://www.azerothcore.org/wiki/quest_template_addon).
- **A3:** [AzerothCore GM commands](https://www.azerothcore.org/wiki/gm-commands), quest add/complete/reward/remove/status distinction.
- **A4:** [AzerothCore maintainer repository](https://github.com/azerothcore/azerothcore-wotlk), project/runtime scope.
