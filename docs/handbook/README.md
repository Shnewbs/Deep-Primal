# Deep Primal engineering handbook

**Consolidated October 29, 2026.** Source snapshot: [`8f28f833caa259a0e59adac494a3aee8d750bc63`](https://github.com/Shnewbs/Deep-Primal/commit/8f28f833caa259a0e59adac494a3aee8d750bc63). This is the working entry point for merged requirements and their detailed sources. It is a documentation checkpoint, not a new game release or engine grade.

## Read by purpose

| Need | Start here |
| --- | --- |
| Engine boundaries, data/configuration, scripts, tools, complete texture families, UI and independent-game direction | [Architecture and authoring](ARCHITECTURE-AUTHORING.md) |
| Terrain, saved revisions, geology, climate/biomes, water ownership, routing, chemistry, coarsening and edited LOD | [Terrain and water](TERRAIN-WATER.md) |
| Vulkan/OpenGL, useful distance, LOD, lighting, FSR/DLSS, temporal guides, lifetime and measurement | [Rendering and lighting](RENDERING-LIGHTING.md) |
| Implementation priorities and exact acceptance boundaries | [Merged requirements](REQUIREMENTS.md) |
| Conflicting versions, historical grades, policy provenance and supersessions | [Policy and status](POLICY-AND-STATUS.md) |
| Every inventoried source, duplicate identity and retained research document | [Complete catalog](CATALOG.md) |
| What was checked, what was repaired, remaining limitations | [Documentation audit](AUDIT.md) |
| New primary-source study and follow-up questions | [Research register](research/README.md) |

## How the merge works

The inventory contains **520 original Git Markdown files and 24 project Library identities: 544 source identities, 533 distinct byte contents**. Eleven exact duplicate groups share one canonical target while retaining both source identities. The prior expansion's **507 Markdown files**—487 replicas and 20 shared documents—remain separately inventoried and frozen. The current original Git set adds 52 documents and changes one relative to that expansion's Dev77 input; none of its original paths were removed.

Shared behavioral requirements now live in the three chapters and [one cross-system gate register](REQUIREMENTS.md). Detailed catalogs remain the home for named materials, species, biomes and asset rows. Historical reports remain evidence for their exact source and execution boundary; they do not compete as active specifications. Raw project snapshots are retained under [sources](sources/README.md), with context links resolved separately rather than rewriting imported evidence.

Every original received checksum, duplicate, heading, open-checkbox and relative-target auditing. Domain reviews read the main specifications and latest evidence more deeply. Review depths are explicit in [AUDIT.md](AUDIT.md); this pass does not claim line-by-line re-review of all historical transcripts or all frozen replicas.

A [publication refresh](PUBLICATION-SUPPLEMENT.md) additionally audits five newly added Markdown documents from six concurrent commits. The combined input is **525 original Git documents plus 24 project identities**; the base catalog remains explicitly pinned.

## Maintenance rules

1. Change a requirement in its chapter and keep the stable gate ID in REQUIREMENTS.md. Link detailed records; do not paste the same open task into every new audit.
2. Record source, recipe/schema, implementation and evidence identities separately. Preserve saved profiles, edits, quantities and approved art.
3. Close a finding only with a linked successor result that covers its original failure boundary. Keep remaining native/persistence/visual/performance gates visible.
4. Add a dated research finding with a primary source, inspected version/date, applicability, proposed change and qualifying fixture. A source link alone is not a requirement or an integration.
5. Keep this handbook out of future original-input inventories, along with research-expanded, to prevent recursive replicas. Reconcile new or changed source documents against catalog.json before refreshing the checkpoint.
6. Publish authorized documentation with a fresh expected-parent check. Preserve other workers' code, leases, state and evidence. No automation schedule or build authority is changed by editing these documents.

## Gameplay-system research expansion — October 29, 2026

[New system specifications](../systems/README.md) add native agriculture, original animal/predator ecology, all-vanilla-mob replacement and delegated admin NPC/quest creation. [The feature roadmap](../systems/SYSTEMS-ROADMAP.md) defines dependencies and gates. The earlier catalog/verification above retains its explicitly pinned checkpoint; this extension has a separate [documentation verification](../systems/VERIFICATION.json). New design does not promote engine/native acceptance.
