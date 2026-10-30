# Shared contracts for agriculture, fauna and authored quests

**Status:** proposed integration architecture, October 29, 2026. The individual system specifications own their detailed rules. This contract prevents incompatible assumptions at their joins. It introduces no runtime feature or new engine grade.

## Authority, identity and revisions

The portable core owns typed definitions and deterministic rules; host adapters own permitted-thread mutation, persistence, entity/inventory APIs and networking. Define stable IDs for crop species/cultivars, plant instances/plots, animal individuals/species, populations/encounters, NPC templates/instances, quests/objectives/chains and accepted gameplay events. A path rename, skin replacement or display-name translation never silently changes saved identity.

Definitions have schema and content revisions. Runtime instances carry the effective revision selected by their migration policy. Player quest progress and accepted rewards are separate from published quest definitions. Author rollback changes future definition selection; it is not a license to erase completed objectives, revoke delivered rewards or recreate a claimed reward period. Save independent definition, instance and evidence identities.

The shared owner decision remains: persistent non-spendable capped character XP, separate profession progression, and no vanilla XP restoration. Character level, profession skill, technological age and quest-chain progress are distinct condition operands. A quest cannot quietly spend accumulated character XP, or use a profession level as character level without an explicit condition type.

## Time is a declared input

Physical crops, soil, animals, product cycles, spoilage and environmental exchanges use the shared calendar and world inputs. The established default pauses the calendar when the server is empty and does not simulate elapsed real-world shutdown time. An individual player logging out does not pause a populated server. Ordinary ticks, sleep/calendar advance, unloaded-region catch-up and real-time appointment/reset clocks are separate policies.

No unloaded farm receives harvests, consumed feed, births, rainwater, milk or disease ticks merely because a timestamp is old. Any bounded catch-up policy must name available persisted inputs, resource reservations, event ordering and maximum work; unknown weather/water/habitat is not fabricated. Neglected-care effects should follow the explicitly selected simulation policy, with explainable player feedback.

A repeatable quest chooses its reset clock and unique period identity. Calendar time commands, rollback, reconnect and daylight-setting changes must not reopen an already-claimed period. A proposed UTC daily reset is an optional appointment policy, not permission for offline physical simulation; its choice must be shown in the authoring UI. Migration of reset policy needs an explicit one-time period mapping.

## Quantities and inventory

Use declared units and exact owned amounts for irrigation withdrawals, soil-held water, feed, harvested food/carcass products and consumed turn-in items. Soil hydration is a derived state or a stored physical account according to its model; it must not duplicate water already held elsewhere. Irrigation reserves and commits an actual finite source exchange. Decorative wetness or crop growth modifiers cannot refill the source.

Animal mass, carcass mass, individual product budgets and recurring product cycles retain their distinct accounting. An item reward is a deliberate economy grant with origin/reward policy, not a claim to conserved natural matter. It still needs one durable grant outcome. A minted quest grant must not satisfy a self-funded natural harvesting objective through an infinite reward loop.

Inventory operations preserve mass, crop cultivar/quality, food age, temperature where relevant, animal provenance, contamination abstractions and container contents. Objectives declare accepted variants and whether quantity means item count, mass, distinct cultivar or verified activity. Possession inspection, item consumption, harvesting activity and delivery are different objective types.

## Accepted events and exactly-once logical outcomes

Proposed accepted event envelope:

```json
{
  "schema_version": 1,
  "event_id": "server-generated-unique-id",
  "world_id": "saved-world-id",
  "kind": "deepprimal:crop_harvest_committed",
  "actor_id": "player-uuid",
  "subject_id": "plant-instance-id",
  "definition_revision": 3,
  "calendar_sequence": 420,
  "transaction_id": "committed-harvest-id",
  "provenance": "normal_gameplay",
  "outputs": [{"item": "deepprimal:example_grain", "mass_g": 750}]
}
```

This is illustrative schema notation, not a shipped API. The server generates identities after the accepted operation. Clients cannot report their own completed harvest, kill, delivery or reward result. The objective consumer deduplicates by event/attempt identity under a bounded durable retention policy. A lost acknowledgement may cause redelivery; it must not cause another outcome. Do not claim network exactly-once delivery.

Each transaction declares its authoritative write set and durable recovery owner. For a turn-in, reserve compatible items at the permitted NPC interaction, recheck objective/conditions/revision, commit item consumption plus one reward entitlement, then deliver within inventory capacity or keep bounded durable escrow. Partial inventory delivery cannot replay the full grant. Failure before commit consumes nothing; failure after commit resumes from the receipt. Where the host cannot atomically save all owners, a journal/reconciliation protocol must be implemented and fault-tested rather than assuming file rename or a setter is sufficient.

Quest reward escrow is claimed at the quest NPC or an explicitly authorized on-site service. It is not a handheld remote retrieval/deposit system or a general player-storage portal. Capacity/expiry/repair policy must avoid silent loss while remaining bounded; an admin cannot publish unlimited repeatable grants without the required economy permission and limits.

## One taxonomy, multiple participation rules

Species tags supply ecology, husbandry, combat and objective matching. A custom grazing animal can have a phenotype and a hauling role without becoming two actors. An encounter template can spawn a species with bounded behavior modifiers while keeping the original individual/provenance. Wild, bred, quest-spawned, admin-test and cosmetic actors are distinct reward eligibility classes.

Kill, observation, tracking, rescue, care and conservation quests are all supported design patterns. Shared party participation is snapshotted by a declared rule and radius/instance policy; it never blindly grants every team member every resource output. Contributors receive their own reward entitlements where appropriate. The physical carcass and its finite contents remain one owned object even when several players gain quest credit. Protection for owned livestock, neutral NPCs, safe routes and player claims must be checked before encounter placement and aggression.

## Content authoring and implementation boundaries

Native datapack/config/editor data must compile through the same validators. Custom JSON examples in these documents are proposed schemas, not promises that current KubeJS or host commands accept them. Scripts prepare validated definitions at an explicit lifecycle; they do not execute arbitrary code per crop, animal thought or quest predicate.

Administrative changes are versioned, permission checked and auditable. Resolve actor rights on the server at each mutation/publication. Client button visibility is only presentation. Optimistic concurrency prevents one author's save from overwriting another's newer NPC/quest definition. Revalidate dependency graphs and reward budgets at publish and activation. Test/preview events use isolated progress and cannot award production items or XP.

Queue and memory limits cover plot updates, plant spread/crossing, perception/path requests, population transitions, NPC instances, objective fan-out, event replay/deduplication, reward entitlements and author previews. Partition work by changed/active region; no global entity/plot scan and no distant force-load for quest eligibility. Record oldest eligible age and retained reservations so a bounded queue also makes progress.

## Shared acceptance scenes

| Scene | Required observation |
| --- | --- |
| Irrigate a breeding plot from a finite pond during drought | Crop/soil water exchange paid once; no phantom source refill; cultivar identity/soil result persists |
| Grazer damages a field and a player harvests surviving crops | Habitat/crop changes agree; one plant outcome/event; cosmetic animation cannot duplicate items |
| Two players claim predator credit and butcher the carcass | Individual quest credit follows the selected participation policy; one kill receipt and one finite carcass/product budget |
| Two admins edit/publish one NPC chain | Stale revision conflicts clearly; unauthorized actor cannot publish through a forged packet; existing players retain valid progress |
| Turn in with one free inventory slot, then crash at every transition | Items consumed once, one entitlement, partial delivery resumes without duplicate XP/items or silent loss |
| Reset calendar, reload definitions, travel dimensions and reopen | Completed/reset-period receipts and stable identities persist; no duplicate NPC/animal or reopened claimed period |
| Remove a vanilla-mob route or migrate a named mount | No denied vanilla actor enters active gameplay; player/cargo/ownership records remain recoverable |

These are proposed fixtures. The feature roadmap links each to a real implementation and native evidence before marking its milestone complete. Existing core/renderer audits cannot close these new gameplay gates by association.
