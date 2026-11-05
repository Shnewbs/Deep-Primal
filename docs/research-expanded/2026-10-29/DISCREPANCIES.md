# Source discrepancies and scope clarifications

This register preserves differences between documents, dates, versions, and evidence levels. It is a research finding about the documentation corpus, not a fresh runtime defect list.

**Final input snapshot:** [33c43c943f0469195e7ab1a6852ab4360fad1788](https://github.com/Shnewbs/Deep-Primal/commit/33c43c943f0469195e7ab1a6852ab4360fad1788), declaring Dev77 source. Research began at `31126b3996064e65351fd08870cd6a0949e38970`; `9a1d92a44d7d3864d35e0c3ac288158a37c33015` / Dev69 was an earlier refresh checkpoint. The final inventory preserves 487 originals: 468 Git Markdown files plus 19 project-collection files. The original files remain unchanged by this documentation edition.

## D-01 — Source version, overview version, and work-state checkpoint

| Observation at the pinned commit | Evidence | Interpretation |
| --- | --- | --- |
| Gradle declares `0.0.1-alpha.29-dev.77-seam-contract.1` | [Pinned gradle.properties](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gradle.properties) | Declared source version, not a qualified runtime |
| README and integrated core summary describe Dev32 upscaling | [README replica](replicas/git/README.md), [core summary replica](replicas/git/ENGINE_CORE_SUMMARY.md) | Older overview snapshots |
| Shared work-state is checkpointed at Dev53, updated 2026-10-28 12:04 UTC | [Pinned work-state](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/ENGINE-WORK-STATE.json) | Coordination checkpoint with its own source identity; later scoped terrain receipts do not replace it |
| Dev68 independent audit targets parent source `2c672857...` | [Audit replica](replicas/git/docs/engine-hourly-audits/2026-10-28T1928-2c672857/REVIEW.md) | Scoped independent source/API-double evidence |

**Expansion:** derive a current-status view from explicit source and evidence identities. Keep overview text, implementation version, last qualified package, and latest independently tested source as separate fields. This edition does not edit active work state.

## D-02 — Historical policy and acceptance thresholds

The corpus retains older 8.0, inclusive 8.5, strictly greater than 8.5, pause/resume, and build-delivery descriptions. [Builder/auditor policy](replicas/git/docs/ENGINE-BUILDER-AUDITOR.md) and [hourly build policy](replicas/git/docs/ENGINE-HOURLY-BUILDS.md) explicitly explain some supersessions. Later audit records also describe an October 27 owner-build override.

**Expansion:** attach policy identity and date to each run. Preserve original instructions as historical content; a replica does not start tasks, change cadence, or authorize a new runtime release. No grade or completion status is changed here.

## D-03 — Standalone direction and actual adapter boundaries

The project describes a standalone survival direction while its current main adapter and build metadata remain Java/NeoForge/Minecraft based. The [standalone-engine replica](replicas/git/standalone-engine/README.md) describes a specific prototype scope. [Maritime 1.21.1](replicas/git/maritime-1.21.1/README.md) is an independent project inside the repository.

**Expansion:** specify portable core APIs, host adapters, standalone prototype capabilities, and Maritime packaging separately. A future target architecture does not establish that the current game no longer uses host storage, scheduling, lighting, or rendering services.

## D-04 — Renderer generations and distance ranges

Historical renderer material includes the procedural horizon and older ranges/ownership, while the newer root overview describes actual delivered-chunk LOD. The [renderer/diagnostics replica](replicas/git/docs/RENDERER-AND-DIAGNOSTICS.md) and [LOD architecture replica](replicas/git/docs/LOD-AND-DRAINAGE-ARCHITECTURE.md) need their temporal scopes read explicitly.

**Expansion:** report requested distance, effective native delivery, available LOD coverage, implementation generation, and relevant backend separately. A large requested radius does not establish unseen-terrain generation or a finished textured distant renderer.

## D-05 — Upscaling preferences versus implemented providers

The [upscaling document](replicas/git/docs/UPSCALING.md) records spatial FSR 1 implementation. The [NGX bridge document](replicas/git/renderer-vulkan/src/main/native/dlss/README.md) describes existing native bridge work and missing temporal activation requirements. Later records include FSR preferences that remain unavailable until an actual provider is integrated.

**Expansion:** preserve these distinctions. The [current vendor study](research/render-performance-research.md) is a dated external capability reference; it does not certify Deep Primal integration or the owner's device. Runtime effect version, driver, API, temporal inputs, and initialization result determine effective availability.

## D-06 — Finite environmental accounts versus older perennial language

The [water budget requirements](replicas/git/docs/WATER-BUDGET-REQUIREMENTS.md) preserve earlier balanced/perennial aspirations alongside later finite-depletion implementation notes. The [Water Engine replica](replicas/project/WATER-ENGINE.md) provides the broader proposed conservation system.

**Expansion:** distinguish source identity, replenishment policy, stored quantity, external forcing, and visual appearance. A source label cannot create water. Record every permitted external exchange and its constituent payload.

## D-07 — Planned drainage shares versus live routed discharge

Channel revisions and delta planning records describe geometric or planned flow shares. Those values do not establish a time-varying conservative network solver. Terrain revisions, material revisions, channel revisions, and runtime water state are separate versioned concepts.

**Expansion:** give planned capacity, current flux, cumulative transfer, and stored volume different names and units. Add cross-region basin/outlet identity and real native witnesses before promoting a routing plan into authoritative hydrology.

## D-08 — Frozen profiles versus current creation defaults

The terrain and configuration documents preserve existing saved recipes and add later opt-in revisions. Reading current creation defaults while decoding an old world would violate that boundary even if the visible seed is unchanged.

**Expansion:** save the complete relevant recipe identity, define missing-field defaults per historical schema, reject unsupported combinations, and test reopen after creation settings change. See [terrain V6](replicas/git/docs/TERRAIN-V6.md), [terrain V7](replicas/git/docs/TERRAIN-V7.md), and [configuration](replicas/git/docs/CONFIGURATION-AND-ORE-AUTHORING.md).

## D-09 — The 32-pixel default and protected 64-pixel art

The [texture plan](replicas/git/docs/art/BLOCK-TEXTURE-PLAN.md) establishes the newer size policy while preserving approved ground-v4 and final-sand work pending actual alternatives and comparisons. Atlas dimensions and tile dimensions serve different purposes.

**Expansion:** encode default size, approved exception, source identity, recipe, and review status per asset. Replicating the documents does not authorize downsampling or replacing approved bytes.

## D-10 — Catalog-level planning and local implemented checkpoints

Large biology/geology/environment rosters contain deferred concepts, but a blanket “all catalog content is unimplemented” would lose local implementation evidence. The [geology catalog](replicas/git/docs/NATURAL-TYPES-GEOLOGY.md), for example, retains a soil-texture checkpoint describing twelve classes and three forms with a saved material revision and scoped validation.

**Expansion:** track every catalog item through defined, authored, exported, registered, native-verified, visually accepted, and gameplay-complete states. Preserve local evidence and its date; a rendered material does not imply complete soil-order, horizon, or physical-state mechanics.

## D-11 — Source-model findings and native failures

The Dev68 enclosed-basin witness uses an API-double regional dependency. Other records use source extraction, Java shims, modeled callbacks, or software renderers. Many originals correctly explain these limits.

**Expansion:** store the tested boundary as a structured field and require a separate native or target-device gate when the claim crosses it. Keep reproduced model risks visible without calling them newly observed in-game failures.

## D-12 — Assertion counts, carried grades, and package qualification

The repeated 50/100 historical grade, millions of assertions, successful component tests, and valid archive CRCs answer different questions. The [original review](replicas/git/docs/engine-review-2026-10-24/REVIEW.md) and subsequent audit replicas retain their actual scopes.

**Expansion:** count unique runs, identify fresh versus carried categories, compare packaged classes to the intended source, and verify the actual selected runtime artifact. The documentation expansion is not a fresh engine regrade.

## D-13 — Exact duplicates and distinct source identities

Nine project-collection documents are byte-identical to Git counterparts: eight renderer jam README/REVIEW files and the Dev68 V8 routing builder README. Other similarly named reports can differ.

**Expansion:** preserve all source identities and all 487 replicas. The manifest records exact duplicate groups so readers can recognize shared evidence without silently dropping an original.

## D-14 — The consolidated Water Engine retains an alpha-26 baseline

The [Water Engine](replicas/project/WATER-ENGINE.md) calls itself a single entry point while retaining alpha-26/V4 default statements. Later [V6](replicas/git/docs/TERRAIN-V6.md) and [V7](replicas/git/docs/TERRAIN-V7.md) documents describe later new-world defaults and compatibility.

**Expansion:** keep the consolidated design as a requirements reference and date its implementation baseline. Determine a specific world's behavior from its saved profile and the selected implementation revision. An old default in the consolidated design must not migrate an existing save.

## D-15 — Partial ice and soil behavior versus the complete physical model

The older Water Engine's ice section contains gap-fill proposals. The later V6 document reports finite exposed physical ice/snow ablation with a 96-game-day seasonal cycle and no unloaded catch-up. It also reports generated topsoil, subsoil, and parent-material profiles.

**Expansion:** retain those narrower dated implementations while keeping generalized freeze/thaw, energy conservation, salinity effects, dynamic glaciers, full soil horizons, moisture/chemistry, and ecological calibration as separately evidenced work. Generated layers and seasonal ablation do not close every soil or thermal requirement.

## D-16 — Proposed recovery semantics need additional conservation detail

The Water Engine's proposed boundary schema permits age-based pending-transfer rollback and relies on host atomic saving without specifying coordinated multi-owner recovery. Its listed closed-box sum omits persisted unloaded cells, and blanket load-order determinism is broader than loaded-only evolution supports.

**Expansion:** harden the proposal with durable escrow/debit/credit recovery, late-commit fencing, complete owner enumeration, exact conversion-remainder ownership, and replay determinism conditioned on equal accepted inputs and activation history. These are corrections to proposed contracts; this study has not reproduced a new runtime loss or duplication defect.

## D-17 — Dev69 and the earlier refresh checkpoint

The earlier refresh from the initial Dev68 research pin to Dev69 compared 17 intervening commits and found three added Markdown files plus an updated renderer backlog. All four document changes remain incorporated. At that checkpoint the inventory contained 452 originals; the subsequent Dev77 refresh is recorded below. The project collection retains the same nineteen Markdown files.

The [Dev69 receipt](replicas/git/docs/validation/engine-continuation/v8-ocean-seams-2026-10-29/README.md) reports opt-in stitched marine admission with routing policy v2 inside saved-profile r5. It preserves legacy v1 profiles and describes bounded sampled connectivity, a heuristic marine core, and source/API-double tests. It is not a new r6, automatic migration, or native acceptance. The real priority-flood planner's global outlet/discharge behavior and actual codec, save/reopen, conservation, and GPU gates remain distinct.

The [renderer jam added at that checkpoint](replicas/git/docs/renderer-jams/20261029-visible-snapshot-reuse/README.md) adds reusable visible-view extraction with scoped allocation evidence. The [backlog at the earlier Dev69 pin](https://github.com/Shnewbs/Deep-Primal/blob/9a1d92a44d7d3864d35e0c3ac288158a37c33015/docs/renderer-jams/RENDERER-BACKLOG.md) had an opening last-implementation pointer to jam22 while its appended history described jam23. That is a historical mismatch; the final [backlog replica](replicas/git/docs/renderer-jams/RENDERER-BACKLOG.md) now opens with jam28 and retains the intervening history.

**Expansion:** treat Dev68 findings and earlier allocation reports as dated inputs to the newer work. Require the named remaining integration gates; do not restate a historical witness as a newly demonstrated current in-game failure or convert an isolated allocation result into a frame-time claim.

## D-18 — Dev77 final freeze and additional source-only evidence

The final refresh from Dev69 to `33c43c943f0469195e7ab1a6852ab4360fad1788` compared 102 commits and adds 35 Markdown originals: eight terrain continuation receipts, nineteen renderer documents and eight hourly audits. The renderer backlog is also updated. This produces 487 originals and replicas, comprising 468 Git and 19 project-collection files. These counts describe documentation coverage, not implemented features or newly executed tests.

The terrain sequence progresses from bounded basin exclusion through saved ocean-domain policies and shoreline coherence to outlet topology and a perimeter bridge. Routing policies v3–v7 remain distinct from the saved-profile r5 envelope; they do not imply a saved r6/r7 format or automatic migration. A correctly serialized ocean mask still requires globally justified content. [Saved-domain record](replicas/git/docs/validation/engine-continuation/v8-source-ocean-mask-2026-10-29/README.md), [shoreline record](replicas/git/docs/validation/engine-continuation/v8-shoreline-coherence-2026-10-29/README.md).

The [Dev75 topology helper](replicas/git/docs/validation/engine-continuation/v8-outlet-topology-2026-10-29/README.md) was tested as actual standalone production core without Minecraft API doubles. The [Dev76 bridge](replicas/git/docs/validation/engine-continuation/v8-region-port-bridge-2026-10-29/README.md) and [Dev77 seam contract](replicas/git/docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md) have narrower source/API-double evidence; the complete production watershed dependency graph and native V8 lifecycle remain unqualified. Dev77 rejects conflicting shared observations rather than solving the missing regional reconciliation. Port observations carry no physical runoff derived from cumulative catchment.

**Expansion:** preserve the exact test boundary of each successor. Do not carry a superseded implementation description forward as current, but retain unresolved native, saved-world, global outlet/discharge and finite-water conservation gates. Source checkpoints, renderer self-assessments, independent component audits and the carried whole-engine grade remain separate evidence.

## How to resolve a future discrepancy

Start with the claim being made, then identify its source revision, implementation path, saved-schema revision, execution environment, and evidence boundary. Follow an explicit supersession link if one exists. If no fresh evidence resolves the difference, keep the uncertainty visible and define the next qualifying check. Do not choose a result simply because its filename, timestamp, or prose sounds newer.
