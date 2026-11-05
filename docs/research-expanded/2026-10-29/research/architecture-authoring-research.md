# Deep Primal — architecture, authoring, assets, settings, and validation research

**Research checked:** 2026-10-29  
**Status:** Research and proposed additions to the replicated design documents. No implementation, dependency adoption, benchmark, or migration is claimed.  
**Scope:** Twelve primary sources spanning Unreal, Godot, Unity, Blender, Substance Designer, schema evolution, and reproducible builds.

## 1. Project requirements retained

The following come from the assigned Deep Primal task, not from external engines: block and item textures default to **32 × 32**; **64 × 64** is available when justified; stone families and tinting need a deliberate reusable design; graphics preferences persist under **config/deepprimal**; the architecture must preserve a standalone direction and authoritative simulation.

Do not turn references into mandatory dependencies. Blender and Substance are possible authoring tools. Unreal, Unity, and Godot supply useful workflows and contracts. Their editor objects, runtime APIs, project layouts, and licensing models do not become Deep Primal requirements merely because they illustrate a good idea.

The central proposal is to separate five kinds of state: editable authoring sources, validated content definitions, generated runtime assets, client preferences, and authoritative world state. A source edit must identify which of these it affects before any reload, migration, or publication occurs.

## 2. Primary-source cards

Each evidence paragraph is 50–90 words. Design inferences are proposals for Deep Primal. The JSON companion retains citation reference IDs and the same claim boundaries.

### AA-01 — Unreal Engine — Data Driven Gameplay Elements

**Source:** [Unreal Engine — Data Driven Gameplay Elements](https://dev.epicgames.com/documentation/unreal-engine/data-driven-gameplay-elements-in-unreal-engine)  
**Type/version:** Official engine documentation; Page presented as Unreal Engine 5.8 documentation; a workflow reference, not a project dependency.

**Evidence.** Unreal documents importing externally authored tabular and curve data into typed engine structures. A DataTable row maps to a programmer-defined structure, with a named key and fields that may include asset references. The import UI distinguishes missing and extra fields, and reimport uses the original source location. This is an example of a contract between authoring data and executable behavior: an editable table still needs a defined runtime interpretation.

**Design inference.** Use typed, versioned block, item, stone-family, biome, and material definitions. Keep authoring convenience separate from authoritative interpretation. Reimport may replace a validated definition snapshot; it must not silently regenerate geography or alter existing world profiles.

**Limits.** Does not establish a serialization format for Deep Primal. Spreadsheet macros and Unreal DataTables are not recommended dependencies. Engine data-driven authoring does not by itself prove safe live simulation changes.

### AA-02 — Unreal Engine — Asset Management

**Source:** [Unreal Engine — Asset Management](https://dev.epicgames.com/documentation/unreal-engine/asset-management-in-unreal-engine)  
**Type/version:** Official engine documentation; Page presented as Unreal Engine 5.8 documentation.

**Evidence.** Unreal distinguishes primary assets, addressed through typed primary identifiers, from secondary assets loaded through references. Its Asset Manager exists in both editing and packaged-game contexts and supports discovery, loading, content chunking, and disk/memory auditing. Streamable handles retain asynchronously loaded objects while they are needed. The documentation also distinguishes asset instances from Blueprint classes rather than treating every editable data object as executable behavior.

**Design inference.** Give standalone-capable Deep Primal services explicit asset identity, dependency, residency, and lifetime contracts. Let editor tools request content through adapters without making the simulation depend on an editor object model. Separate immutable definitions from per-world mutable instances.

**Limits.** Unreal-specific identifiers and handles are examples, not a Java API prescription. The source does not prove that one global manager is the best architecture for this project. Memory/loading behavior must be measured in Deep Primal.

### AA-03 — Godot 4.4 — Import process

**Source:** [Godot 4.4 — Import process](https://docs.godotengine.org/en/4.4/tutorials/assets_pipeline/import_process.html)  
**Type/version:** Official engine documentation; Pinned Godot 4.4 manual.

**Evidence.** Godot retains original source assets and records import configuration separately from generated internal resources. Source checksum changes trigger reimport using the asset's configured preset. Its manual recommends preserving source-side import metadata in version control while treating the generated import directory as reconstructible cache. It also distinguishes loading imported resources through the resource loader from directly opening original files, because editor and exported-project layouts differ.

**Design inference.** Maintain source assets, recipes, stable IDs, and dependency metadata; generate runtime artifacts through a reproducible import step. Resolve runtime assets through a manifest, not development-machine source paths. Add an export-without-authoring-tools test.

**Limits.** Godot's exact file layout should not be copied into an existing Deep Primal repository without inspection. Checksum-based change detection is not a proof of deterministic import. Source access was via the official indexed manual.

### AA-04 — Unity 6.2 — Asset metadata

**Source:** [Unity 6.2 — Asset metadata](https://docs.unity3d.com/6000.2/Documentation/Manual/AssetMetadata.html)  
**Type/version:** Official engine documentation; Unity 6.2 / 6000.2 manual; page build identifies 6000.2.16f1.

**Evidence.** Unity assigns imported assets unique identifiers and stores those identifiers alongside import settings in metadata files. References can survive moves and renames when the associated metadata moves with the asset. Losing metadata causes a replacement identity and can break materials, scripts, or other references. Changing recorded import settings triggers reimport and updates derived data. Identity therefore belongs to the content record, rather than being inferred solely from the current filename.

**Design inference.** Use stable namespaced content IDs plus explicit rename aliases/migrations. Keep paths as locations, hashes as content versions, and IDs as identity; do not conflate them. Validate duplicate IDs and missing dependency targets before publishing an asset pack.

**Limits.** This does not require Unity-style sidecar files; a project manifest may carry equivalent identity. Renaming a saved gameplay ID requires a migration even if a texture file move is harmless. The metadata page was read as HTML text.

### AA-05 — Blender — Geometry Nodes Modifier

**Source:** [Blender — Geometry Nodes Modifier](https://docs.blender.org/manual/en/5.2/modeling/modifiers/geometry_nodes.html)  
**Type/version:** Official authoring-tool manual; Retrieved page is labeled Blender 5.2 LTS; no installed tool version is assumed.

**Evidence.** The Geometry Nodes modifier receives and outputs geometry through a node group. A shared group can have different input values on separate modifier instances. Inputs may use a single value or an attribute field, and the connected node determines the attribute domain and type. Field outputs can expose attributes, while warnings can surface in the modifier interface. These features support reusable authoring recipes with explicit parameter and geometry-data boundaries.

**Design inference.** Use optional node-based authoring for reusable rock, vegetation, prop, and collision-approximation recipes. Record inputs, seeds, coordinate units, output attributes, and export rules. Export validated geometry; do not require Blender or its nodes in the game or headless server.

**Limits.** Official search-index text was inspected; direct manual fetch encountered a tool access error. This does not guarantee deterministic outputs across Blender versions. Procedural render meshes must not silently define authoritative voxel geography.

### AA-06 — Blender — Geometry Nodes Baking

**Source:** [Blender — Geometry Nodes Baking](https://docs.blender.org/manual/en/5.2/modeling/geometry_nodes/baking.html)  
**Type/version:** Official authoring-tool manual; Retrieved page is labeled Blender 5.2 LTS.

**Evidence.** Blender describes Geometry Nodes baking as saving intermediate geometry to improve evaluation performance. It explicitly says that the stored geometry format is not an import/export format, with OpenVDB volumes identified as an exception. It also warns that data written by one Blender version is not guaranteed readable by another. Simulation-zone baking and arbitrary node-tree baking are separate authoring operations, and disk data is only produced when baking is performed.

**Design inference.** Treat geometry-node and simulation bake caches as rebuildable authoring intermediates. Retain the original recipe and pinned tool version, then export a documented interchange/runtime artifact. A cached Blender simulation must not become Deep Primal's authoritative water, terrain, or replay format.

**Limits.** Official indexed text was inspected; direct manual fetch returned a tool access error. No general claim about other Blender export formats follows from the bake-cache caveat. No Blender runtime integration is selected.

### AA-07 — Adobe Substance 3D Designer — Overview

**Source:** [Adobe Substance 3D Designer — Overview](https://experienceleague.adobe.com/en/docs/substance-3d-designer/using/getting-started/overview/overview)  
**Type/version:** Official authoring-tool documentation; Page updated September 25, 2026.

**Evidence.** Adobe describes Designer as a procedural, parameterized authoring system with linked texture outputs. Its editable SBS packages retain graph structure and resources. Compiled SBSAR archives preserve parameter adjustment but do not expose graph editing, and consuming them requires a supporting integration. Static bitmap exports work through ordinary image formats but lose dynamic generation and have fixed resolution. These are distinct deliverables with different editing and runtime requirements.

**Design inference.** Author stone families as reusable patterns, masks, and bounded parameter sets, then export approved 32-by-32 textures or justified 64-by-64 exceptions. Preserve editable sources and recipes. Choose static runtime images by default; an SBSAR runtime is a separate dependency decision.

**Limits.** The source does not prescribe pixel-art style, texture sizes, or stone taxonomy; those are project choices. No Substance runtime licensing or distribution entitlement was evaluated. Archived helpx pages returned 404; the current official Experience League page was successfully inspected.

### AA-08 — Godot — EditorInspector

**Source:** [Godot — EditorInspector](https://docs.godotengine.org/en/stable/classes/class_editorinspector.html)  
**Type/version:** Official editor API documentation; Stable documentation retrieved 2026-10-29.

**Evidence.** Godot uses its property inspector in both settings dialogs and the object-inspection dock. Property metadata controls presentation order, path-like names create nested sections, and group/subgroup usage flags create organized control groups. The API supports property editors, filtering, focus behavior, and edit notifications. This illustrates how a shared typed property description can drive multiple editing surfaces without requiring every surface to invent its own interpretation.

**Design inference.** Define one settings/definition descriptor with type, units, range, default, applicability, help, and apply semantics. Generate consistent panels and validation from it. Keep player-facing groups concise, with advanced controls and explicit requested/effective settings, while authoring tools expose richer diagnostics.

**Limits.** A grouped inspector is not proof of usability; keyboard navigation and task completion need project tests. Do not expose every engine field as a player setting. Godot UI classes are references, not dependencies for NeoForge or the standalone frontend.

### AA-09 — Godot — ConfigFile

**Source:** [Godot — ConfigFile](https://docs.godotengine.org/en/stable/classes/class_configfile.html)  
**Type/version:** Official persistence API documentation; Stable documentation retrieved 2026-10-29.

**Evidence.** Godot's ConfigFile stores section/key values and can load, parse, or save them through explicit operations. Mutating an in-memory value does not persist it automatically. The methods report errors, and the documentation distinguishes in-memory configuration from filesystem state. It also notes format-specific behavior, including comments being lost on save. A settings UI and a durable configuration file therefore require a deliberately connected lifecycle rather than an assumption that editing a control writes safely.

**Design inference.** Persist requested graphics preferences under config/deepprimal using the existing project format if present. Make validation, preview/apply, save success, restart requirements, and recovery explicit. Keep effective capability downgrades distinct from saved intent, and keep client graphics separate from authoritative world configuration.

**Limits.** ConfigFile is a persistence example, not a recommended Java format or library. The page does not guarantee atomic writes or crash recovery; those are proposed project requirements. The required config/deepprimal location comes from project instructions, not Godot's user path.

### AA-10 — JSON Schema — Draft 2020-12

**Source:** [JSON Schema — Draft 2020-12](https://json-schema.org/draft/2020-12)  
**Type/version:** Official schema specification overview; Draft 2020-12; publication June 16, 2022.

**Evidence.** The official 2020-12 overview identifies separate core and validation specifications and describes dialect-specific changes to arrays, dynamic references, embedded schemas, and vocabularies. It distinguishes format annotation from format assertion. Schemas therefore need an explicitly selected dialect and compatible validator behavior; using a keyword without verifying the supported vocabulary can mean less validation than an author expects. The specification describes document validation, not the semantics of a game's simulation.

**Design inference.** Pin a schema dialect and validator for editable definitions and graphics settings. Validate syntax and shape first, then run semantic checks for IDs, references, units, ranges, texture policy, and authority rules. Use an explicit project schemaVersion rather than mistaking the JSON Schema dialect for a content migration version.

**Limits.** JSON Schema alone does not prove gameplay validity, cross-file referential integrity, or deterministic results. No validator library is selected. Source overview inspected; implementation must select the exact relevant validation specification and vocabulary.

### AA-11 — FlatBuffers — Schema Evolution

**Source:** [FlatBuffers — Schema Evolution](https://flatbuffers.dev/evolution/)  
**Type/version:** Official serialization documentation; Official documentation retrieved 2026-10-29.

**Evidence.** FlatBuffers documents specific rules for compatible schema evolution: ordinary table fields are appended, removed fields are deprecated rather than deleted, and explicit field IDs can permit controlled ordering. Changing defaults can reinterpret older data that omitted those values. The guide also identifies a source-control hazard when separately generated binary data and concurrently extended schemas are merged. Compatibility comes from obeying a defined format contract, not merely incrementing a version label.

**Design inference.** Apply format-specific migration rules to world data, content catalogs, and graphics config. Preserve old fixture files, defaults, IDs, and migration provenance. Validate upgrades before writing and retain recovery. Keep generated artifacts tied to the exact committed schema and generator version.

**Limits.** FlatBuffers is not selected as a project serialization dependency. Its binary evolution rules must not be copied verbatim to unrelated JSON or native host formats. Syntactic compatibility does not guarantee unchanged gameplay meaning.

### AA-12 — Reproducible Builds — Definitions

**Source:** [Reproducible Builds — Definitions](https://reproducible-builds.org/docs/definition/)  
**Type/version:** Primary reproducibility guidance; Official definition retrieved 2026-10-29.

**Evidence.** The Reproducible Builds project defines reproducibility in terms of declared source, build instructions, and environment producing identical specified artifacts. Relevant environment details can include dependency versions, flags, and locale. Authors identify which outputs are covered; unrelated build logs need not be included. Verification compares outputs byte for byte, commonly through cryptographic hashes. Recording a build recipe is therefore useful evidence, but equality must still be checked.

**Design inference.** Record tool/importer versions, content hashes, recipe parameters, seeds, color conventions, and target formats for generated assets. Rebuild in clean environments and compare declared artifacts. Distinguish exact runtime data from tolerance-tested authoring previews, and never label a merely repeatable-looking screenshot a reproducible build.

**Limits.** A fixed random seed alone does not establish cross-platform determinism. This source does not guarantee bit-identical outputs from Blender, Substance, GPU processing, or compression tools. Select exact reproducibility targets and test the chosen pipeline.

## 3. Proposed architecture and authority boundaries

| Domain | Owns | Changes through | Required boundary |
| --- | --- | --- | --- |
| Authoring tools | Editable graphs, meshes, images, recipes, previews | An authoring transaction | Cannot write live authoritative world state |
| Content definitions | Stable IDs, material/item/block rules, references, declared schema | Validated content publication | Gameplay-affecting changes require an authority/profile decision |
| Asset build pipeline | Derived textures, meshes, atlases, manifests | A recorded import/export recipe | Generated artifacts identify their sources and toolchain |
| Runtime content service | Immutable definitions, resource handles, residency | Versioned publication and lifetime management | Paths and object pointers are not persistent content identity |
| Renderer and graphics preferences | Appearance, selected capability tier, client resource policy | Validated client configuration | Cannot change water quantity, terrain geography, or gameplay light |
| Simulation and saved world | Authoritative rules, quantities, world profile and mutable state | Simulation transactions and explicit migrations | Does not require an editor, graphics context, or procedural authoring runtime |
| Host/platform adapters | NeoForge integration, filesystem roots, device APIs, clocks and I/O | Version-specific adapter contracts | Core content semantics remain usable by a later standalone host |

This is a proposal for dependency direction, not a demand for seven new frameworks or one class per row. A small codebase may implement several responsibilities together while preserving their contracts. Avoid choosing an ECS, scene graph, or plugin system solely because another engine uses one. Evaluate those implementation choices against the actual world queries and update costs.

Definitions should be immutable at runtime. A world instance stores state referring to definition IDs, rather than duplicating mutable authoring objects. A material's decorative variant must not implicitly redefine its collision, thermal behavior, scalar light emission, or fluid interaction. Those references should be typed and explicit.

For reload, create a candidate snapshot, validate its complete dependency closure, prepare necessary runtime resources within a declared memory cap, and publish at an appropriate boundary. Retire old resources only after their consumers finish. If publication fails, retain the last valid snapshot and report the rejected revision. A partial asset reload must not leave a new mesh referencing a retired material or a server consuming half of a rule update.

## 4. Proposed content identity and import contract

Stable identity, location, and revision are separate:

- **Identity:** a persistent namespaced content key; changing it needs an alias or migration policy.
- **Location:** a source file, package entry, or runtime asset location; moving it should not automatically create a new identity.
- **Revision:** the content and recipe version used to build a particular result.
- **Integrity hash:** evidence of exact bytes; it does not by itself describe compatibility or gameplay meaning.

Every derived runtime asset should record its content ID, definition/schema version, source dependencies and hashes, importer/tool version, recipe parameters, random seed policy, output roles/formats, target capability variant, validation result, and produced artifact hashes. Use explicit dependency edges so changing a shared stone mask rebuilds the affected outputs without rebuilding unrelated content.

Source control should retain editable sources or a durable reference to them, stable identity metadata, import recipes, schemas, and necessary manifests. Rebuildable caches need a declared exclusion policy. Whether final generated assets are committed, released separately, or generated during packaging depends on their size and tool availability; choose one reproducible workflow rather than blindly copying an engine's ignore list.

An exporter must fail visibly when an unsupported node, material feature, attribute, unit convention, or missing dependency would change meaning. A successful file write is not proof of a valid runtime asset. A packaged client and a headless server must load their required content without reaching into an author's workstation or launching Blender/Substance.

## 5. Proposed texture and stone-family specification

### 5.1 Resolution policy

Interpret **32 × 32** as the default authored/exported block tile or item image, not the total size of a texture atlas. The importer must know which asset class the rule applies to. Atlases, normal/roughness data, UI art, and multi-frame sheets require explicit layouts rather than being mistaken for a single tile.

A **64 × 64** exception records a concrete reason: necessary silhouette readability, legible item detail, a specific close-up use, or another reviewed visual need. It is not a blanket quality preset. Doubling both dimensions gives four times as many base-level texels; total runtime cost still depends on formats, mips, packing, and residency. Upscaling an existing 32 × 32 image alone does not add authored detail.

High-resolution authoring inputs may be useful, but approval is based on the intended final pixel grid and viewing distance. Define source color convention, alpha treatment, sampling, mip generation, and atlas padding explicitly. Data maps and color maps receive distinct encoding rules. Any optional extra maps retain useful defaults for existing color-only art.

### 5.2 Stone families and tinting

Proposed family definitions include a stable family ID, localization key, base pattern/structure, grain or vein scale, contrast range, approved color palette, roughness treatment, tint-mask policy, variation rules, and references to textures and gameplay material definitions.

A family and a color variant are different identities. Reuse is valuable, but a catalog should not imply that every geological family is merely one repeated image with a different hue. Where the intended families differ structurally, vary their grain, seams, layering, fracture cues, or pattern scale. Within one family, bounded tint and small variations can create coherent diversity.

Define whether tint affects the entire image, selected mask regions, or a named palette entry. Apply it under a declared linear-light material convention; do not accidentally tint roughness or normal data. Keep material-local tint, environmental wetness, biome appearance, damage, and lighting as separate parameters so the same state is not baked and applied again.

Validate a contact sheet at native pixel size, repeated tiling, normal gameplay distance, neutral light, warm light, low exposure, and adjacent-family transitions. Evaluate recognition and cohesion in the game view. A graph preview or large beauty render does not establish readability at 32 × 32.

### 5.3 Optional procedural authoring

A useful Blender recipe exposes only meaningful controls: dimensions, topology limits, seed, detail level, output attributes, pivots, orientation, and collision/export settings. A useful material recipe exposes palette, pattern density, grain/vein scale, masks, and bounded wear. Record the parameter values used for each exported asset.

Retain editable sources separately from compiled graph packages and runtime exports. Blender bake caches are authoring intermediates; Substance archives still require a supported consumer. Static exported geometry and images are the initial runtime-compatible proposal. Runtime procedural generation is a separate capability decision with its own deterministic behavior, latency, memory, and licensing checks.

## 6. Proposed settings UX and persistence contract

Use the existing project configuration format after inspecting it. The established location is **config/deepprimal**; this research does not invent a replacement filename or file extension. A future standalone host should map the same logical configuration contract through an explicit platform path adapter.

Each setting descriptor contains a stable key, type, units, range, default, category, help text, applicability, scope, and apply behavior. Apply behavior distinguishes immediate changes, previewable changes, resource-recreation changes, and restart-required changes. The player UI should expose meaningful choices rather than every underlying shader parameter.

Maintain these values separately:

| Value | Meaning |
| --- | --- |
| Requested | The user's saved preference |
| Effective | What current hardware, backend, and supported features can provide |
| Pending | A validated change awaiting apply/restart/resource recreation |
| Default | The versioned value used when no preference exists |
| Temporary override | A diagnostic/session setting that should not silently overwrite preference |

A downgrade to a supported OpenGL profile should report the effective result without destroying a saved request for a richer Vulkan profile. Restore defaults and reset a single group are different actions. Basic and advanced views should share the same validated descriptors, keyboard behavior, labels, and units.

The proposed save lifecycle is: parse into a candidate, validate, migrate if supported, resolve capabilities, apply or stage changes, then persist a coherent requested-state snapshot and report success or failure. Keep the user informed when a change is active but unsaved. Display-mode changes may need a timed revert where a lost image would prevent recovery; ordinary reversible appearance adjustments do not need repeated confirmation.

Use a tested recoverable write strategy, such as a temporary sibling file plus replace and a last-known-good backup where supported. Do not claim filesystem atomicity without qualifying the platform operation. Preserve an invalid or future-version file for diagnosis rather than overwriting it with defaults. Do not silently clamp a malformed value and present that as the user's intentional saved choice.

Graphics preferences must remain separate from world generator profiles, authority modes, simulation parameters, save schema, and gameplay material definitions. A graphics preset must not regenerate terrain, change finite-water quantities, or modify server decisions.

## 7. Proposed schema evolution and validation

The schema language's dialect and the project's content version are distinct. A JSON Schema dialect controls validation semantics; a project schemaVersion identifies the meaning and migration rules of a particular settings or content format.

Validate in stages:

1. Parse and bound input size/structure.
2. Validate the selected schema and supported vocabulary.
3. Resolve stable IDs and dependencies; reject duplicate or missing required targets.
4. Validate semantic units, ranges, geometry/material compatibility, and texture policy.
5. Classify effects as visual-only, content-definition, or authoritative-world changes.
6. Build a candidate artifact/snapshot and run applicable fixtures.
7. Publish coherently, recording the accepted revision and migration provenance.

Migration must preserve the meaning of old defaults and identifiers. Do not assume that omitting a field today means the same thing it meant when an old file was written. Retain historical fixtures and specify how renamed, removed, and unknown fields behave. A future schema version should have an explicit read-only/reject policy rather than being silently rewritten by an older program.

Separate configuration migration, content-catalog migration, and saved-world migration. Importing a new art pack must not become a generator migration. Replacing a texture ID with an alias may be a harmless visual repair; changing a saved block or material identity can affect authoritative state and requires its own controlled mapping.

Exact reproducibility and visual equivalence are different gates. Declare which artifacts must match byte for byte and which authoring previews may be assessed under a stated tolerance. Stable seeds, canonical ordering, pinned tools, and recorded recipes support reproducibility, but clean rebuild comparisons provide the evidence. Do not infer cross-platform simulation determinism from a deterministic asset build.

## 8. Proposed acceptance fixtures

| Fixture | Required evidence |
| --- | --- |
| Rename/move a source asset | Stable runtime references and content identity remain valid |
| Change a shared source mask | Correct dependent assets rebuild; unrelated assets remain untouched |
| Rebuild from clean sources | Declared artifacts match the recorded reproducibility policy |
| Export without authoring applications | Packaged client/server uses only declared runtime content |
| Broken dependency or invalid definition | Candidate rejected; last valid runtime snapshot remains coherent |
| Stone family contact sheet | 32 × 32 readability, approved 64 × 64 exceptions, tint and tiling behavior |
| Graphics save/restart | Requested values survive restart; effective values reflect actual capabilities |
| Unsupported graphics request | Accurate downgrade reason; saved intent preserved |
| Interrupted/corrupt settings write | Recoverable configuration and visible status; no silent destructive overwrite |
| Old/future schema fixtures | Defined migration or rejection; old defaults and IDs retain meaning |
| Visual preset and resource reload | Identical authoritative world state and simulation inputs |
| Asset reload under residency pressure | Bounded transient memory and no retired-resource references |

These are proposed implementation gates, not tests executed during this documentation research.

## 9. Where these findings should expand the replicated documents

The architecture material should add authority boundaries, data ownership, host-independent interfaces, reload/publication rules, and standalone adapter responsibilities. The asset/material material should add stable IDs, source-versus-runtime formats, 32/64 policy, stone-family recipes, tint semantics, and import validation. The settings material should add requested/effective state, persistent configuration, meaningful UI groups, capability downgrade behavior, and recovery. The validation material should add schema/migration fixtures, source provenance, reproducibility targets, and packaged-runtime checks.

Keep existing project facts and historical audits distinct from these proposals. Do not rewrite old evidence as though a new design decision had already been implemented. The research does not establish that any referenced engine or tool should replace the current project stack.

## 10. Research limits

The sources describe specific engines and authoring applications, not measured Deep Primal outcomes. No native build, schema implementation, asset import, GUI usability session, or cross-platform rebuild was performed. Blender manual text was available through the official search index, while direct fetches returned tool access errors; those limits are retained in the source cards. Current Adobe documentation was located on Experience League after older help links failed.

No code, shader, plugin, or authoring-runtime license was evaluated for adoption. Source descriptions support workflow comparisons; the proposed module boundaries, settings behavior, pixel-art policy, and acceptance gates are project design inferences. Future implementation must inspect the actual checkout and preserve its established data and configuration contracts.

