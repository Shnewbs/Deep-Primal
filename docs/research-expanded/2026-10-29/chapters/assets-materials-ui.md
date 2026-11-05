# Assets, materials, settings and interface authoring

## 1. Preserve the approved direction

Deep Primal's owner direction is **32×32 by default for block and item tiles, with 64×64 when the required detail justifies it**. UI panels and background artwork have separate requirements. The approved ground-v4 faces and masks and final sand are protected 64-pixel assets. The default is not authorization to downsample, repaint or regenerate those bytes. Any 32-pixel comparison remains a candidate until explicitly reviewed. [Project source: block texture plan](../replicas/git/docs/art/BLOCK-TEXTURE-PLAN.md).

The existing work is substantial but has distinct completion states. Batch 01 contains 40 candidates and Batch 02 adds 70, for 110 exports. The rock crosswalk maps 88 rows to 81 unique candidate bases, including 11 reused assets. Those counts do not mean 110 runtime registrations, 88 implemented rock behaviors or completed native visual acceptance. [Batch 01](../replicas/git/docs/art/block-textures-32-batch01/README.md), [Batch 02](../replicas/git/docs/art/block-textures-32-batch02/README.md) and [coverage table](../replicas/git/docs/art/block-textures-32-batch02/ROCK-COVERAGE.md).

This chapter proposes a production contract for extending that work. It preserves source approval, export verification, runtime integration and native acceptance as separate states. Existing generated-resource and shader implementation should be described at its actual granularity; new candidate art does not automatically inherit every test of an earlier asset version.

## 2. An asset is an identity and its dependencies

Use a stable asset ID that survives file moves. Keep source master identity, authoring recipe, imported intermediate, runtime resource and material binding separate. Unity's metadata documentation illustrates why stable identity and associated metadata matter when assets move or are recreated. Deep Primal can adopt the principle without adopting Unity's file format. [Primary reference: Unity Asset Metadata](https://docs.unity3d.com/6000.2/Documentation/Manual/AssetMetadata.html).

The proposed asset manifest should contain:

| Field group | Required meaning |
| --- | --- |
| Identity | Asset ID, version, family, role and dependent resources |
| Provenance | Master hash, source/crop reference, creation method and approval |
| Export | Tool versions, parameters, dimensions, encoding and generated outputs |
| Material contract | Color/data semantics, tint channels, alpha meaning and mip policy |
| Evidence | Exact-file hash, decoded-pixel hash, native scene and owner review |
| Lifecycle | Candidate, approved source, export verified, native verified, rejected or superseded |

Exact PNG hashes identify delivered bytes. Decoded-pixel hashes distinguish image content from encoder or metadata differences. Neither proves visual quality. A missing master must fail a reproducibility workflow before it writes a misleading partial success manifest. Preserve larger masters outside runtime packages while keeping their durable identity and retrieval instructions.

Godot's import documentation separates authored source, import metadata and generated cache, and warns that editor source loading and exported resource access are different contexts. The useful design is a declared import boundary: generated artifacts are rebuildable outputs, while source and import choices are retained. [Primary reference: Godot import process](https://docs.godotengine.org/en/4.4/tutorials/assets_pipeline/import_process.html).

## 3. Material families and resolution decisions

Do not turn the concept catalog into one unique texture for every row. Separate geological material, visual family, shape, face and state. A stair and a full block can share a material while using different geometry and orientation. Two rocks may share a neutral base but retain distinct physical definitions. A grouped catalog entry can require multiple visual variants.

The current coverage table already makes consequential distinctions: dolomite rock is mapped to dolostone rather than a mineral tile; coal ranks have separate candidates; argillite remains a provisional mudstone reuse; the evaporite umbrella resolves to halite/gypsum. Preserve those explanations in machine-readable relationship types such as exact base, shared family, provisional substitute and grouped expansion. Do not assign physical properties from filenames.

A 64-pixel exception should name the visual requirement: a small item silhouette, directional structure or detail that fails at 32 under actual display conditions. It should include a comparison and approval. More pixels are not automatically better; fine contrast can become noise at distance. Conversely, a broad 32-pixel rule must not destroy already approved material character.

Review neutral bases at intended scale before final tint. Check grain direction, feature size, repetition and family recognition under several light directions. The documented edge-contrast threshold and repeated contact sheets are screening tools. They cannot decide whether a large wall looks natural or whether a coal rank remains distinguishable after lighting and tint.

## 4. Use design software as optional source tooling

Blender Geometry Nodes can support reusable geometry authoring through shared node groups, exposed inputs and attribute domains. A Deep Primal workflow could use it for shape prototypes, repeated components or inspection geometry, provided exports have explicit units, axes, pivot, topology and material-slot contracts. It is an optional authoring tool, not a required game runtime. [Primary reference: Blender Geometry Nodes modifier](https://docs.blender.org/manual/en/5.2/modeling/modifiers/geometry_nodes.html).

Blender's baking documentation describes retaining evaluated intermediate data for node workflows. That does not make a bake cache a stable interchange format or guarantee cross-version reproducibility. Export the intended runtime representation and record the tool/version and inputs that produced it. Keep cache acceleration separate from authoritative source. [Primary reference: Blender Geometry Nodes baking](https://docs.blender.org/manual/en/5.2/modeling/geometry_nodes/baking.html).

Substance Designer distinguishes editable graph sources, compiled parameterized assets and exported maps. The relevant option is a controlled material-source workflow: expose approved parameters, export static runtime maps where appropriate and retain graph provenance. Running a compiled procedural material in game would require a separately selected integration; exporting maps does not preserve all procedural behavior. [Primary reference: Adobe Substance Designer overview](https://experienceleague.adobe.com/en/docs/substance-3d-designer/using/getting-started/overview/overview).

None of these tools authorizes replacing approved Deep Primal artwork with a new procedural look. Use them where they solve an identified production problem. Keep approved source and rejected experiments distinguishable, including the ground-v3 rejection and v4 restoration. A source tool's successful export is not an art-direction decision.

## 5. Declare texture-channel meaning

Every texture channel needs one declared interpretation. Color data, normal vectors, roughness, coverage, tint weights and packed timing metadata must not pass through interchangeable import rules. A grayscale appearance does not establish whether a texture is color-encoded albedo or linear data. Record conversion and sampling behavior at the material boundary.

Ground materials already provide a concrete example. The historical v2/dev4 integration describes a dual-tint representation with complementary soil/cover masks, a premultiplied soil contribution and blend information carried in a channel that is **data, not transparency**. Linear mip treatment and supported rounding belong to that interface. Generic alpha-cutout processing would corrupt its meaning. The current approved art is v4 with unchanged final sand. [Historical integration](../replicas/git/docs/art/ground-textures-v2/README.md), [approved v4](../replicas/git/docs/art/ground-textures-v4/README.md), [final sand](../replicas/git/docs/art/sand-final/README.md).

Write the material equation once and make exporter, mip generator and shader fixtures consume the same contract. Verify constant-color and constant-weight controls that expose double tinting, incorrect conversion and alpha misuse. Then inspect real placed blocks; correct arithmetic alone does not prove that the side transition looks good.

Batch 01's six binary cutouts need separate coverage and mip acceptance. A 50-percent export threshold is not proof that needles, grass or kelp remain readable at distance. Opaque glacier ice is a material base, not an implemented translucent ice shader. Lichen and moss bases are not masks merely because the runtime might later blend them.

Historical water work shows another boundary: packed UV, normal and timing carriers formed an ABI shared by CPU and shaders. A marker collided with repeating UV behavior and passed some controls before failing another. Any packed representation needs finite-value rules, disjoint markers, cross-backend decode checks and resource-reload compatibility. Reuse that lesson; do not copy the historical packet format into unrelated materials without need. [Project evidence](../replicas/git/docs/validation/engine-continuation/water-boundary-packet-2026-10-23/README.md).

## 6. Keep tint palettes separate from generated materials

The colormap work includes three 256×256 RGB lookups and 40 existing biome water mappings, while 118 profiles remain broader proposed content. Its Minecraft namespace is intentional for the current loader and can affect other compatible worlds. Acceptance must therefore record active pack priority and actual loader behavior, not assume a Deep Primal-only namespace boundary. [Project source](../replicas/git/docs/colormaps/README.md).

A visual tint palette changes appearance. A material-ID gradient can change generated block identity and saved-world compatibility. Give them different schema types, editor panels and revision rules. A color adjustment should not silently alter geology; a material-selection change should not masquerade as a harmless color preference.

Native review should sample known lookup coordinates and biome entries, reload the pack, vary lighting and inspect the result in both intended and other affected worlds. Mark first-pass palettes as candidates until approved. Historical resource equality checks support those exact bytes, not a later edited palette or every runtime lookup.

## 7. Settings should explain the effective result

The game already documents custom video tabs and staged Apply/Revert behavior. Expand that interface through shared metadata rather than inventing a second settings system in the external editor. Both should expose exact numeric entry, units, defaults, valid ranges and dependencies. Basic/Advanced/Expert grouping should change presentation, not hidden semantics.

For each control, show requested value, effective value, origin and application mode. Explain server locks, unsupported hardware, required restart, resource reload and new-world restrictions before applying. An unavailable provider should remain unavailable with a concrete reason; a saved preference is not proof of active capability. Failed persistence must leave an obvious unsaved state and retain the staged values.

The proposed configuration migration belongs under the architecture chapter's recoverable save contract. Current flat files, including Themes preferences, need an explicit migration map before documentation changes their operating paths. Existing files under `config/deepprimal/engine` and `config/deepprimal/graphics` do not establish that every domain already migrated. [Configuration design](../replicas/git/docs/CONFIGURATION-AND-ORE-AUTHORING.md).

Accessibility acceptance should cover keyboard focus and activation, visible focus, text scaling, contrast, long localized labels, constrained windows and precise entry without a mouse. A slider must not be the only way to enter a rare-event probability or large world value. Preview changes should be reversible, with clear per-key and per-domain reset behavior.

## 8. Themes and HUD are presentation clients

The Themes module preserves native slot identities and exposes provider-based progression and thirst. Keep that authority boundary. A HUD may display a server-approved value; it must not invent thirst, award progression or restore vanilla XP when Deep Primal's policy suppresses it. Disabling custom HUD presentation is not permission to bypass the progression decision. [Themes contract](../replicas/git/gui-themes/README.md).

Provider snapshots should carry session/world identity, availability and capability revision. Registration, replacement and disconnect need explicit invalidation so late callbacks cannot display data from a previous world. Separate a Journal screen from the future field-guide content service. Content IDs, localization, search and exploration permissions belong to the content/authority layer, while theme selection controls appearance.

Retain normalized HUD anchors, safe margins, keyboard nudging and Save/Cancel/Escape semantics. Test real server-container actions separately from screenshots of an opened menu: shift-click, drag, number-key swaps, offhand targets and recipe layouts must preserve native behavior. Theme restoration should restore original resource origins and slot coordinates.

Reload needs a generation identity. If theme A starts loading, theme B is selected and A finishes last, A must not replace B. Forced resource failure, overlapping reloads and provider changes need separate acceptance from ordinary successful switching. The background assets are upscaled from smaller masters; their 4K dimensions should not be described as native 4K artwork. Large-file and memory effects require measurement rather than assumptions.

## 9. Generated shaders and upscaling capabilities

The FSR1 tools retain pinned vendor sources, checked-in generated shaders and independent reference tests. Preserve generation provenance and license notices, and detect drift between source recipe and checked-in output. Normal player builds should not require Python or a C preprocessor merely to use an already generated shader. [Tool workflow](../replicas/git/graphics-settings/tools/README.md).

Keep verification layers distinct: reference kernel pixels, production API lifetime, shader conversion, live game composition and named-hardware performance. The dev32 report documents scene-only Spatial/FSR1 with native-resolution UI; its optional NGX bridge does not complete DLSS temporal inputs or runtime integration, and frame generation remains unavailable in that snapshot. A converter producing valid SPIR-V is not a Vulkan game-frame test. [Implementation report](../replicas/git/docs/validation/upscaling-2026-10-27/README.md), [FSR evidence](../replicas/git/docs/validation/upscaling-2026-10-27/fsr-validation.md).

## 10. Promotion requires several kinds of evidence

An asset can be approved by its owner yet fail export validation. It can export exactly yet fail a shader contract. It can render correctly in one fixture yet fail native mips, reload or distant readability. Preserve those distinctions in the promotion ledger.

The proposed minimum review scene includes repeated floors and walls, directional faces, multiple tints, bright and dim lighting, nearby and distant screen footprints, cutout silhouettes, resource reload and both supported graphics paths. Record actual asset hashes, game artifact, scene coordinates, settings and inspected images. Water continuity needs motion sequences when temporal behavior is the claim; still images cannot establish fluidity.

Promotion should select an explicit manifest, never whichever directory sorts last. Rejected ground-v3 remains useful negative evidence; approved v4 and sand remain protected. Candidate 32-pixel families advance through their own native and owner review. These contracts let the project grow its content without losing the decisions and implementation already earned.
