# Architecture, authoring, assets and interfaces

Consolidated October 29, 2026 from the pinned documentation inventory. This chapter combines current design decisions and acceptance work; it does not certify implementation. Preserve the dated originals and the [earlier architecture research](../research-expanded/2026-10-29/chapters/architecture-authoring.md) and [assets research](../research-expanded/2026-10-29/chapters/assets-materials-ui.md) as supporting detail.

## Decisions that apply across the project

| Subject | Governing direction | Evidence and limits |
| --- | --- | --- |
| Product and runtime | Deep Primal is the product. Continue its original terrain, finite-water and rendering engine; keep pure rules portable and host operations in adapters. The owner has also requested an independent game. | [Decision register](../design-decisions-2026-10-18.md), [module contract](../MODULES.md). The existing `standalone-engine` directory is a historical Minecraft/NeoForge test mod, not an independent executable game. No standalone language, host, networking stack or migration is selected by this document. |
| Work order | Engine, rendering and world generation precede the wider survival conversion. | [Roadmap](../../roadmap.md). A new design requirement does not expand the hourly engine builder into unrelated gameplay or standalone development; [cycle scope](../ENGINE-HOURLY-BUILDS.md) still applies. |
| Data and behavior | Native code implements behavior; typed data defines supported materials, content and tuning. Keep one property model, one calendar and one heat/power model for applicable consumers. | [Detailed design](../design-reference.md). A catalog row or property sheet is not an implemented block or simulation. |
| Authority | The server owns gameplay, quantities, permissions and saved state. Client preferences own appearance within server/backend constraints. | [Decision register](../design-decisions-2026-10-18.md), [Themes interface](../../gui-themes/README.md). Displaying a statistic does not implement it. |
| Exceptions to broad configurability | Preserve frozen saved geography and player edits; keep the required core; do not restore vanilla XP or add native remote retrieval/deposit through a generic toggle. | [Detailed design](../design-reference.md), [owner decisions](../design-decisions-2026-10-18.md). Specific owner restrictions take precedence over older “everything can be re-enabled” wording. |
| Art | Block/item tiles default to 32×32; justify 64×64 where necessary detail cannot survive at 32. Preserve accepted ground-v4/sand bytes and separate UI/background dimensions. | [Texture production plan](../art/BLOCK-TEXTURE-PLAN.md). Candidate preparation does not authorize replacing accepted assets or implementing deferred environmental content. |

The old design's Hytale discussion is historical research. Its external API assumptions must be rechecked before use; it does not choose the independent game's technology. Keep three identities explicit: the shipping Minecraft adapter, the portable core, and the requested future independent host. Name a deliverable by the runtime it actually requires.

## Module responsibilities

The [runtime module contract](../MODULES.md) remains the starting point. The current documented separation is:

| Component | Responsibility | Boundary to retain |
| --- | --- | --- |
| `engine-core` | Portable terrain/drainage/fluid/LOD math and bounded data structures | No Minecraft/NeoForge types or native graphics handles |
| `performance-engine` | Scheduling, distance/memory policy and metrics | Depends on core; no Vulkan or graphics implementation dependency |
| `graphics-settings` | Settings, backend preference, shared GPU abstractions and presentation budgets | Saving a preference must not load unavailable native libraries |
| `lod-engine` | Authoritative chunk snapshots, edit invalidation, cache/streaming and LOD presentation | No root or Vulkan implementation imports; explicit unavailable service when absent |
| `renderer-vulkan` | Optional backend bootstrap, synchronization, device/presentation and recovery | No worldgen, liquid simulation or common performance ownership |
| Root adapters | Minecraft generation, fluid/world integration, networking and diagnostics | Host mutations remain on their permitted thread; server state is independent of presentation |
| `gui-themes` | Menu/HUD resources and client presentation | Provider reads synced state; no XP awards, thirst invention or teleport authority |

Build modules, gameplay feature modules and authoring dependencies have different meanings. A nested JAR is a packaging unit; a gameplay manifest governs behavior/data; Blender or an editor is source tooling. Do not infer a finished in-game module manager from the existing Gradle projects.

Each supported package profile needs one exact dependency list, public interfaces, native payload, configuration owner and dedicated-server behavior. Retain the documented one-installer rule and distinct no-LOD/platform bundles. Historical versioned filenames in module notes must not be relabelled as the latest delivered artifact. [Current installation guidance](../../README.md) and exact build receipts determine distribution status.

Before a gameplay module can be disabled in an existing world, define its transition: validate dependencies/conflicts; stop new admissions; drain or persist owned work; retain registry IDs and saved blocks/items; install the new behavior state at an authoritative boundary; record completion. World-shaping modules remain locked unless an explicit saved revision/migration is supported. Failed transitions preserve the old active state or a declared recoverable state. This is proposed implementation detail, not evidence that dynamic module switching exists.

Acceptance requires missing-dependency, disabled-feature, dedicated-server, repeated reload, queued cancellation and save/reopen cases. Confirm that unavailable optional modules remain inert and that stopping one module does not close caller-owned executors or erase another module's state.

## One definition and configuration pipeline

Keep the [configuration and ore-authoring contract](../CONFIGURATION-AND-ORE-AUTHORING.md) as the detailed schema source. Supported datapack, config, optional KubeJS and editor inputs must converge on the same typed effective definition:

1. Parse with a declared schema/dialect and bounded input limits.
2. Resolve stable IDs, tags, dependencies and declared coordinate/measurement units.
3. Apply the documented pack/script/config/server precedence, retaining per-value origin.
4. Validate structural and semantic constraints, then compile immutable native data.
5. Assign canonical definition identity and freeze the world-relevant generation revision before workers use it.

The proposed precedence is not a claim of a shipped KubeJS ore API. Scripts prepare supported definitions during a bounded lifecycle; no per-voxel script/JSON/file-I/O path is implied. Keep occurrence, placement weight, geometric density, composition grade and reserve quantity distinct. Unknown shapes, impossible ranges, non-finite values and missing hosts are structural errors, not silent clamping opportunities.

Each tunable records owner, type, units, default/range, scope, dependencies, origin and application mode: immediate presentation, reload, restart, server-controlled or new-world-only. Requested and effective values must both remain inspectable. A remote server's lock is not changed by saving a local preference.

All Deep Primal user configuration belongs under domain-organized `deepprimal` roots in the appropriate instance/world scope. Keep host-required datapacks, scripts, save data and caches in their own locations. Complete flat-file migration remains open; some newer files already use domain paths. Do not advertise intended paths as working for every legacy file.

Migration must preserve backups and unknown source data where supported, reject incompatible schema transitions, detect concurrent edits while write ownership is held, and leave one canonical live file per key/scope. Qualify old-only, new-only, both-present, malformed, interrupted, read-only and missing-module cases on supported hosts. Reopening a world must select its saved generation/profile identity rather than current defaults; no automatic retrogen, ore refill or deletion follows a config reload.

The optional external editor remains proposed. Its first useful slice is target-instance selection, exact typed values, scope/restart explanations, staged diff/undo, validation, recoverable save and registry-aware previews. Offline unknown IDs stay unverified. The editor uses the same schema/compiler and ships separately from required game libraries. Preview receipts name input hashes, registry manifest, seed, profile revision, coordinate bounds and approximation mode; preview density is not a substitute for actual saved terrain and edits.

## Assets and material families

Preserve the [1,013-row catalog-to-family mapping](../art/BLOCK-TEXTURE-PLAN.md) without treating it as 1,013 approved blocks or textures. The plan records 110 exported candidates and 88 rock rows mapped to 81 candidate bases; native visual/tint/model acceptance and complete state families remain open. Keep geological identity, visual base, shape, face and state as separate axes.

For each applicable stone family preserve the owner's requested Raw, Cobble, Brick, Mossy, Gravel, Paver, Smooth, Chiseled, Polished and Cracked variants, plus material-specific forms. A family applicability table should explain shared textures, meaningful omissions and custom faces; do not manufacture a distinct image for every shape when UV orientation/material reuse is sufficient. New complete families go through their recorded review before promotion.

An asset record needs stable ID, author/provenance and license, source/master hashes, tool/export version, runtime dimensions/channels/color space/alpha, material/model/UV references, palette/mask dependencies, approval state and exact promoted output hashes. Keep masters and review boards outside runtime assets. A license or accepted appearance for one source does not cover unrelated imported material.

Define tint, mask, normal and alpha semantics once for exporters, mip generation and shaders. Alpha used for cutout coverage must not also silently become a material weight. Keep palette changes separate from generation material-ID changes: recoloring a stone must not alter its saved geological identity. Check actual-size and repeated surfaces, required face joins, orientation, 32/16/8/4px appearance, cutout coverage, native lighting, resource reload and near/far presentation. Dimension/hash checks alone do not establish acceptable art.

## Settings, Themes and the native field guide

Retain the [Themes contract](../../gui-themes/README.md): native slot identities, provider-backed statistics, normalized HUD anchors, Save/Cancel/Escape semantics, rebindable actions and world-visible Journal behavior. Keep approved artwork, the removed Continue button and restrained text treatment. Container behavior must be exercised through real shift-click, drag, number-key/offhand swaps and recipe layouts; a screenshot of an open inventory does not validate transfers.

The earlier research already requires keyboard focus, activation, text scaling, contrast and long-label checks. Extend that into a concrete navigation contract: every screen declares initial focus; rebuilding, filtering or hiding a focused control selects a deterministic visible successor; modal close returns focus to its opener; no settings keystroke leaks into gameplay. Exact numeric entry, Apply/Revert, scrolling and HUD positioning remain usable without a pointer. Test those paths after resize, theme switch, resource reload and an invalid-value error. These are proposed Deep Primal acceptance details informed by [Godot's focus guidance](https://docs.godotengine.org/en/stable/tutorials/ui/gui_navigation.html), not adoption of Godot.

Narration/accessibility output needs labels, roles, values, units, unavailable reasons and logical reading order in the actual host. Speech synthesis alone is not evidence of usable screen-reader navigation. [Godot's application guidance](https://docs.godotengine.org/en/stable/tutorials/ui/creating_applications.html#screen-reader-integration) provides a comparative reference; [W3C keyboard guidance](https://www.w3.org/WAI/WCAG22/Understanding/keyboard.html) informs equivalent non-pointer actions. This is a design reference, not a WCAG conformance claim for a game.

The native Markdown/datapack guide and independent inspection API remain required design work. Define a bounded rendering dialect and explicit extensions before claiming ordinary GitHub Markdown renders identically in-game. [CommonMark 0.31.2](https://spec.commonmark.org/0.31.2/) specifies parsing and includes HTML constructs; it does not select Deep Primal's supported subset. Proposed guide compilation must specify tables/code/link/image behavior, stable namespaced page IDs independent of translated headings, locale fallback, resource resolution and unsupported-syntax diagnostics. Raw HTML is not permission to execute embedded content or fetch remote resources automatically.

Generated material/config pages consume the same compiled definitions as gameplay/editor views. Search, bookmarks, cross-links and third-party inspection must respect discovery/permission policy at the data boundary where applicable. Validate duplicate IDs, missing translated pages, renamed headings, cyclic links, absent assets, excessive nesting and a denied discovery lookup. Keep authoring validation distinct from dynamic gameplay truth.

## Build and independent-host acceptance

Record package source commit, wrapper/JDK/native-tool versions, exact profile/dependencies, generated asset/shader inputs, module identities and verification result separately from playability. Gradle documents deterministic archive order/timestamps as defaults starting at version 9; that alone does not make compilers, generators or native payloads reproducible. A reproducibility claim requires independent clean builds under declared conditions and byte comparison, with differences explained. Dependency verification metadata requires reviewed provenance; automatically recording the bytes just downloaded is not independent verification. [Gradle archive documentation](https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives), [dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html).

An independent game needs an explicit host plan for window/input/audio, rendering and lighting, storage/save migration, content/asset loading, server authority/networking, simulation clocks, settings, packaging and crash recovery. For the first selected host, require a small vertical slice: launch without Minecraft; create a seeded world; move/edit; run a conservative fluid case; save/reopen retaining edits and quantities; close cleanly; compare portable-core vectors against the Minecraft adapter. Runtime/engine selection, targets and budgets remain choices to record before implementation. This chapter preserves the owner's independent-game direction without presenting the historical NeoForge prototype as that game.

## Source map

| Consolidated subject | Preserved detailed source |
| --- | --- |
| Scope, exceptions and player rules | [Owner decision register](../design-decisions-2026-10-18.md), [detailed design](../design-reference.md), [roadmap](../../roadmap.md) |
| Runtime/module/package boundaries | [Modules](../MODULES.md), [Themes](../../gui-themes/README.md), [historical standalone prototype](../../standalone-engine/README.md) |
| Data, configuration, scripts and editor | [Configuration/ore authoring](../CONFIGURATION-AND-ORE-AUTHORING.md), [prior architecture chapter](../research-expanded/2026-10-29/chapters/architecture-authoring.md) |
| Assets, tints and review | [Texture plan](../art/BLOCK-TEXTURE-PLAN.md), [prior assets chapter](../research-expanded/2026-10-29/chapters/assets-materials-ui.md) |
| Execution ownership | [Engine cycle policy](../ENGINE-HOURLY-BUILDS.md); current live work state and audit receipts remain authoritative for their individual runs |

Primary external references above were inspected October 29, 2026. Proposed contracts expand existing design; they do not grant a new engine grade, approve candidate art or mark implementation complete.
