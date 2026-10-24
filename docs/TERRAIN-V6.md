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
