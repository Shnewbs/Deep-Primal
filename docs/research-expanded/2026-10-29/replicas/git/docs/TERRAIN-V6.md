# Expanded replica — docs/TERRAIN-V6.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/TERRAIN-V6.md) |
| Source identity | git: docs/TERRAIN-V6.md |
| Preserved source SHA-256 | `1180848f41098fb294b31741ad3faee195039004fe556bc061ecb705768859ac` |
| Review method | full-text |
| Proposed-work priority | P0 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/terrain-water.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This alpha 27 release document reports saved continental_v6 profiles, geological caves and roofed river tunnels, soil horizons, dunes, glacial forms, finite seasonal ablation, natural debris, and bounded underground failures. It provides concrete deposit schema limits and semantics, cache caps, generation-profile persistence, protected-cover rules, and hazard queue/work limits. It distinguishes continuous geological fields from exact distance guarantees, finite initial or melted water from renewable sources, and static scenery from live ice or sediment simulation. Earlier profiles and edits remain unchanged; native evidence is referenced but not independently rerun by this review.

## Research expansion — gaps and details to deepen

- Clarification: a missing terrain object selects creation defaults, while saved generators promise immutable effective options; creation, legacy load, and malformed-save decoding must be explicitly distinguished.
- Elaboration: deposit bounds and ID-sorted precedence are well specified, but compilation diagnostics, atomic replacement, and serialized semantic fingerprints need a complete contract.
- Elaboration: cave-in and melt caps are concrete; exactly-once recovery through crash windows and queue saturation require deeper acceptance than ordinary reopen.

## Research expansion — proposed contracts

- Define separate constructors for new-world defaults and saved-profile decoding. A saved V6 record must reconstruct its historical effective terrain/deposit definition; a missing or invalid required field must use the documented legacy profile or fail safely, never sample today's config silently.
- Compile deposit inputs atomically to immutable data with a canonical content hash, sorted IDs, bounded candidate estimates, and precise diagnostic paths. Preserve distinctions between body-center ranges, radii, ground cover, and absolute/depth/geological vertical coordinates.
- Retain the stated caps of 32 definitions, 16 weighted contents, and estimated 1024 candidates per chunk as profile limits. Runtime admission must additionally measure worst-case accepted compile/query work and reject arithmetic overflow or degenerate transforms before publication.
- Give physical melt a transaction tying one removed ice/snow block to one accepted full-water allocation and constituent policy. If the destination cannot accept water, preserve the source or escrow an explicit amount; do not remove first and lose the conversion on failure.
- Persist hazard work with world/profile and geometry revisions. At queue capacity 256, coalesce safely or refuse new hazard admission without silently erasing required state. Validate the protected cover and 64-block drop limit against both actual edits and the preserved generated surface.

## Research expansion — acceptance checks

- Save a custom V6 profile, change defaults, omit optional legacy fields, and reopen; verify historical effective settings rather than current-config adoption.
- Compare compiled deposit queries to the wider independent oracle at maximum permitted radii/candidate counts and negative-coordinate seams.
- Interrupt a melt and a rock relocation between persistence steps; restart must produce one material move and one water conversion at most.
- Saturate the hazard queue while unloading neighbors; bounded memory, explicit arrests, protected cover, and eventual useful progress remain observable.

## Research expansion — source anchors

- Configuration and persistence
- Water and instability
- Acceptance scope
- Definition IDs sort overlap precedence, so file order cannot change overlap priority.

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Terrain V6 — alpha.27

New Standard and Deep worlds use `deepprimal:continental_v6`. Existing worlds keep their saved generator, height, profile and edits. V1–V5 algorithms are retained; there is no retrogen. V5 remains available for the staged deposit worlds created during development.

## Generation

The independent continent and watershed planner now includes rock-dependent mountain relief, latitude/elevation climate, wind-aligned dunes where sand is available, cold troughs and moraines, and solid glacial ice. Drainage is planned after dune/glacial relief. Rivers carved sufficiently far below the uncut surface retain a roof only where the full cross-section has cover. The bank cut is suppressed across that footprint so the roof joins the surrounding rock; the same finite water blocks continue through the passage. There are no generated sinkholes.

Cave routes follow continuous fields modified by the local host rock: soluble calcite can form larger chambers; other rocks produce narrower fractures. At least ten blocks of initial ground cover remain. Perched cave water is finite initial water, not a regenerating source. Glacier ice is excluded from the ground-cover calculation. Soil profiles distinguish topsoil, subsoil and weathered parent material, with depth and material controlled by slope, moisture, temperature and geology.

The serialized palette has 40 native biome assets, including two underground choices. These are temporary content palettes, not delivery of the larger natural-material/species catalog. Two-dimensional surface selection and native three-dimensional selection use the same slope-aware column. Immutable biome metadata is capped at 4,096 columns; foundation metadata is capped at 8,192 columns.

Worldgen can place fallen logs across narrow, bank-supported streams and sparse rock debris. These initial solid obstructions participate in the ordinary finite water solver after generation. This is not a separate live sediment simulation.

## Configuration and persistence

`config/deepprimal/worldgen/terrain.toml` supplies creation-time defaults: geological caves, river tunnels, dunes, glaciers, natural debris, mountain scale (0.5–2), and polar distance (4,000–1,000,000 blocks). Each V6 generator saves its effective options and deposit definitions inline under `terrain`. Editing creation defaults or a preset later does not change a saved V6 generator. Other historical config filenames have not been migrated by this revision.

A datapack can author the `terrain` object in the generator of a normal Minecraft world preset. `docs/examples/v6-terrain-profile.json` is a complete example of that object; replacing `deposits` replaces the whole list. An empty list disables generated ores. Removing `terrain` selects the creation defaults and eight built-in deposit definitions. An optional KubeJS-specific authoring API and external desktop editor are not included.

Deposit definitions support namespaced IDs; four oriented ellipsoid forms (`lens`, `vein`, `pipe`, `disc`); spacing and occurrence distinct from internal density; host-rock constraints; weighted block-state contents; and absolute, depth-below-surface or geological-coordinate vertical references. Depth is relative to the body anchor, not independently reprojected at every voxel. Bodies cross chunk boundaries deterministically. Definition IDs sort overlap precedence, so file order cannot change overlap priority. Ranges describe body centers; body radii may extend beyond them, subject to ground cover and world bounds. Shapes are bounded geometric bodies, not branching mineral networks.

Compilation rejects invalid ranges, duplicate IDs, air/fluid contents, more than 32 definitions, and definitions whose estimated neighborhood exceeds 1,024 candidates per chunk. A definition has at most 16 weighted contents. Candidate transforms and column intersections are precomputed; voxel queries perform no trigonometry, allocation or file reads.

## Water and instability

Existing finite ocean/atmosphere/groundwater/snow accounts remain authoritative. Warm seasonal conditions can convert one exposed physical ice/snow block to one full finite water block during an environmental step. Removing ice pays for that water exactly once; repeated visits cannot refill it. The 96-game-day seasonal temperature cycle only advances with game time, with no unloaded-time catch-up. Physical cover uses the actual heightmap rather than asynchronously updated light. Cold polar ice remains solid. This is finite seasonal ablation, not a dynamic ice-sheet flow simulation.

`<save>/serverconfig/deepprimal/hazards/stability.toml` controls V6 underground cave-ins. Mining queues local roof checks. Host material determines supported span; adjacent water weakens that span. Solid construction supports are respected. A failure moves the same rock to a verified solid floor, with no ore/item duplication and no replacement of water. Twelve blocks of actual and generated surface cover are protected. The persisted queue holds at most 256 positions and examines at most eight per host tick. Missing chunks are never force-loaded. Drops beyond 64 blocks, water below, and unverified support boundaries arrest the local event. This implements bounded underground failures; moving surface landslides and animated falling-rock entities are not included.

## Acceptance scope

Core checks cover climate belts, protected roofs, order/cache independence and an independent wider deposit oracle. Packaged native checks compare actual blocks/base columns, traverse a 3×3 tunnel neighborhood, witness every default ore, preserve saved profiles and edits, and exercise finite melt and constructed support/water backpressure. Exact final run counts and archive identities are recorded in `VALIDATION-0.0.1-alpha.27.md`.

The release is an alpha terrain revision. It does not certify owner-GPU performance, every seed or extreme datapack combination, the deferred species roster, global hydraulic continuation, or the broader renderer/LOD acceptance work tracked separately. Native software-driver captures and single-machine timings are scoped evidence, not a perfection or FPS guarantee.


<!-- END PRESERVED SOURCE -->
