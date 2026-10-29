# Architecture, authoring and the path to an independent runtime

## 1. Status and purpose

This chapter proposes enduring contracts for Deep Primal's architecture and authoring pipeline. It expands the existing documents; it does not claim that the proposed editor, configuration migration, gameplay manifest system or standalone game is implemented. The underlying review covers the assigned architecture documents individually, preserving their different dates and evidence levels.

The final publication source pin `33c43c943f0469195e7ab1a6852ab4360fad1788` identifies Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25 and `0.0.1-alpha.29-dev.77-seam-contract.1` source. This edition covers 487 originals: 468 Git Markdown files plus 19 project-collection files. The Dev69 pin `9a1d92a44d7d3864d35e0c3ac288158a37c33015` was an earlier research refresh. The root README still describes dev32, while the root work-state remains checkpointed at dev53. A source version, an owner-confirmed playable build, a successful package and a delivery receipt must have separate identities. See the [discrepancy register](../DISCREPANCIES.md), [root README replica](../replicas/git/README.md) and [engine summary](../replicas/git/ENGINE_CORE_SUMMARY.md). This documentation batch does not rewrite production configuration or work state.

The architecture should make four questions answerable for every capability: who owns the authoritative state, which immutable inputs determine its output, when may the output become visible, and what evidence supports the stated status? Those questions apply equally to terrain, finite water, graphics settings, an ore editor and a future host replacement.

## 2. Preserve the portable core and name the host obligations

The existing [module contract](../replicas/git/docs/MODULES.md) already separates portable core, host integration, graphics, LOD and optional Vulkan. Preserve that direction. A reusable core should operate on explicit values, immutable definitions and owned work contexts. Minecraft types, native device handles, filesystem conventions, registry lookups and network callbacks belong behind adapters.

Portability is a set of interfaces and proven semantics, not a directory name. The current query facade supplies bounded density queries and preview meshes; its summary expressly excludes block-model materials and player edits. Authoritative placed blocks, saved water and rendered scene extraction remain separate responsibilities. A caller must not substitute an unedited density preview for actual edited terrain merely because both expose a mesh.

Proposed boundary contracts are:

| Boundary | Required ownership |
| --- | --- |
| World identity | Save, dimension, generation-profile revision and lifecycle generation |
| Definition compiler | Validated immutable inputs; deterministic output and diagnostics |
| Simulation command | Authoritative thread/owner, expected state and explicit result |
| World query | Availability, revision and distinction between planned and materialized data |
| Presentation extraction | Immutable snapshot and permission to publish only while current |
| Host services | Storage, registries, networking, clock sources, native resources and lifecycle |

A future independent runtime must implement these host services. Removing imports alone does not replace storage, lighting, networking, input, audio, content loading, authentication or recovery. Keep a minimal headless core harness available so deterministic logic can be exercised without a game client, while reserving host behavior claims for actual integration tests.

## 3. Distinguish three kinds of modularity

Deep Primal has build modules today, proposes configurable gameplay modules, and may use external authoring tools. They need different manifests. A JAR dependency controls class loading and packaging. A gameplay dependency controls whether a behavior can run with the current definitions and world. An authoring dependency controls how source assets or definitions are produced; it need not ship to players.

For each build module, record its public interfaces, hard dependencies, optional capabilities, configuration scopes, lifecycle owner, native payload and supported package profiles. Common graphics preferences must remain readable without loading an unavailable Vulkan native library. A dedicated server must not initialize client resources. Optional service discovery should return an explicit unavailable capability, with a reason, before code attempts unsupported initialization.

For gameplay modules, retain stable registry identities when behavior is disabled. Existing saved blocks and items cannot disappear because a preference changed. Define dependency resolution, conflicts, inert behavior, world-lock restrictions and recovery before exposing a module toggle. Separate a missing required definition from an intentionally disabled optional feature.

Unreal's Asset Manager is a useful reference for explicit primary asset identities and managed secondary dependencies, but adopting that idea does not require Unreal or its implementation. Deep Primal can use a small typed content graph whose lifecycle and packaging match its own host. [Primary reference: Unreal Asset Management](https://dev.epicgames.com/documentation/unreal-engine/asset-management-in-unreal-engine).

The manifest should also state resource ownership. Whoever creates a cache, executor, listener or native handle must either close it or transfer ownership explicitly. A module must never close a caller-owned executor merely because its own world is stopping.

## 4. One definition pipeline, several authoring routes

The existing [configuration and ore-authoring design](../replicas/git/docs/CONFIGURATION-AND-ORE-AUTHORING.md) already calls for configs, datapacks and an optional KubeJS bridge to converge on one schema and deterministic compiler. Make that the central content architecture. The proposed external editor should consume the same metadata and compiler diagnostics; it must not introduce a second, GUI-only model of deposits or world settings.

The pipeline should have explicit stages: parse source, validate structure, resolve IDs and tags, validate semantic constraints, compile immutable definitions, calculate canonical identity, then install at a permitted lifecycle boundary. Each stage produces diagnostics tied to source location and definition ID. Compilation must reject unsupported shapes, invalid units, missing dependencies, unbounded search extents and conflicting ownership before runtime sampling begins.

Unreal's documented data-driven workflow demonstrates typed records imported from external tables and reimported from source. The relevant lesson is that authored data and its runtime representation have a declared relationship. Deep Primal's proposed compiled packs should similarly retain source provenance and typed meaning rather than interpreting arbitrary strings inside a per-voxel loop. [Primary reference: Data Driven Gameplay Elements](https://dev.epicgames.com/documentation/unreal-engine/data-driven-gameplay-elements-in-unreal-engine).

Ore occurrence, placement weight, geometric density, composition grade and finite reserve are separate quantities. Absolute height and depth below surface require different units and reference frames. A script can prepare a definition during compilation, but an arbitrary script callback must not execute for every generated voxel. Compilation needs explicit work and memory bounds, including the total candidate neighborhood implied by large deposits.

Precedence must produce a readable explanation: which bundled definition was replaced, which pack or script changed it, which server override applied and which immutable result was frozen. Equivalent supported authoring routes should produce equal canonical output when their effective definitions are equal. Removal of a definition must not erase existing placed blocks or refill depleted resources.

## 5. Schema evolution and saved identity

Structural validation is one layer. JSON Schema distinguishes schema vocabularies and validation behavior; it does not decide whether a geological host tag exists or whether a generator remains compatible with a save. Use a pinned dialect and explicit validation policy, then run domain compilation. [Primary reference: JSON Schema 2020-12](https://json-schema.org/draft/2020-12).

Every persistent definition family should declare schema version, semantic revision, canonical encoding and compatibility policy. Keep stable namespaced IDs separate from file paths and content hashes. Moving an authoring file should not create a new material identity. Conversely, retaining a filename must not conceal changed generation semantics.

The V8 sequence illustrates why upgrades need explicit transitions. Earlier envelopes preserve their original fields and hard-policy behavior; later revisions add blend widths, frozen surface provenance, climate reference and hydrology binding. The dev65 record rejects an authoring helper that would downgrade newer profiles and discard policies. These are source-level contracts, with native codec and saved-world gates still separate. See the [authoring guard](../replicas/git/docs/validation/engine-continuation/v8-blend-authoring-guard-2026-10-28/README.md) and [saved-profile record](../replicas/git/docs/validation/engine-continuation/v8-saved-profile-2026-10-28/README.md).

Provide an allowed-transition matrix for every helper and migration. Reopening a saved profile must not silently select current defaults. A migration should create a reviewable new profile, preserve the old identity and explain effects on future chunks. Existing terrain and player edits require an explicit policy; a new content pack is not permission to regenerate them.

FlatBuffers' evolution guidance is a useful example of format-specific compatibility rules, including field identity, defaults and deprecation. Deep Primal need not adopt FlatBuffers, but must specify equally concrete rules for each format it actually uses. [Primary reference: FlatBuffers Schema Evolution](https://flatbuffers.dev/evolution/).

Content fingerprints identify inputs; they do not prove correct behavior. A declared routing digest is not an authoritative watershed. At the dev68 checkpoint, the planner binds routing to saved V8 geometry, but its regional boundary sea seed does not establish global connectivity across 8,192-block regions. Preserve that historical limit and require a newly persisted routing policy when inter-region behavior changes. [Project evidence](../replicas/git/docs/validation/engine-continuation/v8-routed-source-2026-10-29/README.md). The earlier source-only dev69 successor added routing policy v2 within saved-profile revision 5 using bounded sampled marine-component connectivity, while its priority-flood planner remained locally edge-seeded. [Dev69 evidence](../replicas/git/docs/validation/engine-continuation/v8-ocean-seams-2026-10-29/README.md).

By dev77, opt-in routing policies through v7, a bounded outlet-topology helper and a perimeter observation bridge extend that work. Routing-policy versions remain distinct from the r5 saved envelope. The current bridge rejects inconsistent shared flood, stage, catchment, water-class and receiver observations; it does not reconcile global routing or derive physical runoff from cumulative catchment. Its API-double checks do not qualify the complete production watershed or native V8 generation. [Final seam-contract evidence](../replicas/git/docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md).

## 6. Configuration that is inspectable and recoverable

The owner direction is to place Deep Primal user configuration beneath `config/deepprimal`, organized by domain. Preserve CLIENT, COMMON, STARTUP and SERVER scope; path cleanup must not alter authority. Some files already use the desired layout, including runtime engine and upscaling settings, while documented flat legacy files remain. The existing design explicitly says the complete migration is unfinished. Do not publish invented paths as current operating instructions.

Propose one metadata registry with key, owner, type, units, default, valid range, dependencies, sensitivity to world identity, scope and application mode. Application modes should distinguish immediate client presentation, resource reload, restart, server-controlled and new-world-only. The UI should show requested value, effective value and override origin. Saving a client request does not mean the server accepted it or the backend applied it.

Migration should proceed through discover, parse, validate, plan, backup, write, activate and verify. Before writing, show the exact old-to-new mapping and conflict decisions. A populated canonical destination wins according to the documented policy; retain the old file and a conflict report. Never overwrite it silently with an older flat file. Unknown keys require a deliberate policy: preserve source text for editing where safe, but do not execute unsupported runtime definitions.

Use a temporary sibling file, validate its contents, verify a recoverable backup and replace the destination using supported filesystem semantics. Record enough journal state to resume after interruption. Serialize cooperating writers through the selected write-ownership protocol, or use a genuine conditional replacement supplied by the storage system. Recheck the opened file hash while that ownership is held; a hash check followed by an unguarded rename leaves a check/write race. If the editor opened hash A and the file is now hash B, stop that save with a concrete diff instead of overwriting someone else's change. Permissions and read-only instances must produce visible failure, with the user's staged edits retained.

Godot's ConfigFile documentation illustrates that in-memory edits and explicit load/save operations are separate and that the format has defined preservation limits. It does not guarantee atomic migration. Deep Primal should make save success, comment retention and recovery properties explicit in its own implementation. [Primary reference: Godot ConfigFile](https://docs.godotengine.org/en/stable/classes/class_configfile.html).

Qualification should cover old-only, new-only, both-present, malformed, interrupted, read-only and missing-module cases on the actual supported hosts. Dedicated-server overrides, per-world files and templates need actual host tests; a desktop editor's successful TOML round trip is insufficient.

## 7. Lifecycle, bounded work and publication

Historical cache, LOD and hydrology records repeatedly expose three distinct failures: work starts after its admission window, callbacks invalidate the object currently being used, or a late result publishes after its owner has changed. Consolidate these into reusable contracts instead of rediscovering them independently.

A budget needs units and scope. Count visits, probes, retained entries, bytes and indivisible callbacks separately. Checking time before a large scan does not bound the scan. A per-lookup collision limit does not bound repeated lookups in one tick. Reserved admission opportunity is not a hard wall-clock guarantee when one callback can overrun it. Report what the scheduler actually bounds.

After a callback, revalidate the identity and revision of any object the caller intends to mutate. A cursor removed and replaced under the same key is a different owner. A matching coordinate alone does not justify reinsertion. Cancellation must cover tasks that never start: cleanup placed only in a callable's `finally` cannot run when its queued future is cancelled before invocation.

Distinguish reusable `clear` from terminal `close`. Clear invalidates admission from an older generation; it need not cancel arbitrary caller code. Close seals new work and prevents late cache insertion. Completion, publication eligibility and resource release are separate states. See the [cache lifecycle](../replicas/git/docs/validation/engine-continuation/cache-loader-lifecycle-2026-10-26/README.md), [terminal shutdown](../replicas/git/docs/validation/engine-continuation/terminal-query-shutdown-2026-10-26/README.md) and [future cancellation](../replicas/git/docs/validation/engine-continuation/lod-future-cancellation-2026-10-24/README.md) records.

Presentation follows the same ownership rule. Build candidate GPU bytes separately, submit them, then update the accepted CPU mirror. Retrying from a mirror that already claims success can leave GPU state stale. Bound recovery memory, preserve current unrelated fields and define escalation when repeated writes fail. This remains separate from asynchronous device-loss atomicity.

## 8. Simulation authority and clock domains

Server simulation owns finite quantities, inventories, depletion and permissions. Client interpolation changes appearance. Three substeps inside a 20 Hz host tick are not an independently scheduled 60 Hz simulation, and a 100 ms visual transition must not create water, change flow ownership or advance recharge.

Name at least four clock domains: authoritative simulation tick, environmental/calendar time, monotonic work-budget time and presentation time. State pause, restart, overflow and catch-up behavior for each. The decision ledger's empty-server pause and no offline wall-clock catch-up rule must be applied where intended, without silently making every timeout a calendar duration. Ordered saved ticks and short monotonic deadlines need different arithmetic contracts.

Finite-water fixes show why admission and conservation are separate. A receiver-capacity check prevents one failure, but a native callback can still replace a cell between writes. Throwing an exception or refusing chemistry success does not prove that physical blocks, reservoirs and constituents remain conserved. Define persistent single-owner transactions, idempotent outcomes and explicit unresolved recovery states before claiming restart-safe accounting. [Project evidence: post-receiver barrier](../replicas/git/docs/validation/engine-continuation/water-receiver-postcommit-2026-10-27/README.md).

The same architecture should govern ore depletion, nested cargo and processing rewards. Stable operation IDs prevent duplicate commits; expected revisions reject stale commands; recovery records identify who must finish or compensate. Do not force heat, pressure, mass and currency into one generic conserved scalar. Their units and permitted sources/sinks differ.

Availability is also authoritative data. An unloaded neighbor returning non-admission must not become evidence that the terrain is dry or empty. Exploration permissions should control disclosed server data, not just whether a client overlay draws it. These requirements follow the existing [owner decision ledger](../replicas/git/docs/design-decisions-2026-10-18.md).

## 9. Optional authoring tools with honest previews

The companion editor remains a proposed product. Its first useful slice is target-instance selection, schema-backed navigation, exact values and units, staged diffs, undo, validation and recoverable save. It should work offline with an exported registry manifest. Unknown runtime IDs must appear as unverified, not silently accepted or replaced with defaults.

Godot's Inspector provides a primary example of editing typed properties through metadata and grouped interfaces. Deep Primal can use that pattern while preserving its own scope, server-lock and new-world restrictions. [Primary reference: Godot EditorInspector](https://docs.godotengine.org/en/stable/classes/class_editorinspector.html).

A preview receipt should name seed, world profile, compiler revision, registry manifest, coordinate bounds and sampling mode. Distinguish exact portable-core output from approximate visualization and native materialization. Density occupancy certificates are not metric signed-distance bounds for sphere tracing. An editor must not accelerate its preview by treating arbitrary terrain density as a safe step length. [Project evidence: conservative density certificates](../replicas/git/docs/validation/engine-continuation/v8-conservative-density-bounds-2026-10-28/README.md).

Reproducibility needs declared inputs, environment and instructions. The Reproducible Builds definition concerns independently reproducing identical artifacts from those conditions; a recorded hash or fixed seed alone is not that proof. Apply the distinction to compiled packs, generated shaders and exported assets. [Primary reference: Reproducible Builds definition](https://reproducible-builds.org/docs/definition/).

## 10. Adoption and standalone direction

Start by documenting existing owners and identities, then centralize schema metadata and diagnostics, then implement a recoverable migration and one small editor workflow. Keep each step independently useful to the current game. Native art and UI qualification belongs in the [assets chapter](assets-materials-ui.md); evidence and release rules belong in the [validation chapter](validation-evidence.md).

The historical [standalone prototype](../replicas/git/standalone-engine/README.md) is a separate Minecraft-hosted experiment. The [Maritime project](../replicas/git/maritime-1.21.1/README.md) is an independent 1.21.1 port with TFC dependencies. Preserve both as explicit boundaries. Neither is automatically the current standalone runtime or a compatible module for the root installer.

A proposed independent-runtime sequence is headless deterministic core, minimal host adapter, persistent editable world, authoritative multiplayer and complete player workflow. Each stage needs an observable outcome and compatibility envelope. Reuse proven core semantics and original artwork; replace host responsibilities deliberately. Keep external engines and design software as research references until a specific integration is selected and qualified.
