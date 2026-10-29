# Expanded replica — docs/VALIDATION-0.0.1-alpha.23.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.23.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.23.md |
| Preserved source SHA-256 | `e6b6638dd8d5e40f6578f800a430ae37cc1a8160b4d6af2badbe10c4c6f9251b` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal alpha 23 — water cadence, terrain cuts and graphics repairs”. Its original opening states: “This update addresses reproduced bugs and specific incomplete runtime paths. The complete terrain/water/snow/LOD requirements remain an active implementation backlog. Repository builds and boundary checks cover all modules; focused native tests below are not a claim that every runtime behavior or upstream reference has been exhaustively audited.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Keep smooth water presentation, server ownership, delivered-neighbor coverage, and GPU publication as separate contracts.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define one shared-edge identity and presentation time for neighboring patches, including direction-only edits, dry fades, retirement, and rejected uploads.
- Distinguish missing delivered data from known air or dry cells; presentation fallback must not request simulation work or mint authoritative water.

## Research expansion — acceptance checks

- Capture staggered neighbor updates and near/LOD handoff while changing flow, height, opacity, and camera angle.
- Inject upload refusal and memory pressure, then verify old accepted geometry survives until a coherent replacement is published and retired safely.

## Research expansion — source anchors

- Deep Primal alpha 23 — water cadence, terrain cuts and graphics repairs
- Inventory orientation
- Continuous local water progression and the 60 Hz request

Related design: [render performance](../../../chapters/render-performance.md) · [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.23/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.23/installer-audit.json) |
| `validation/0.0.1-alpha.23/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.23/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal alpha 23 — water cadence, terrain cuts and graphics repairs

This update addresses reproduced bugs and specific incomplete runtime paths. The complete terrain/water/snow/LOD requirements remain an active implementation backlog. Repository builds and boundary checks cover all modules; focused native tests below are not a claim that every runtime behavior or upstream reference has been exhaustively audited.

## Inventory orientation

The Vulkan reproduction showed an inverted inventory entity and absent item icons. The GUI item atlas uses OpenGL bottom-origin texture coordinates; Vulkan offscreen images use the same top-origin convention as uploaded textures. Correct the complete atlas V coordinate, including its row, and correct picture-in-picture sampling for entity previews and oversized items. World presentation, uploaded sprites and screenshot readback retain their own existing origin handling.

Native captures on OpenGL and Vulkan cover all four inventory rows, a golden helmet above diamond boots, cached atlas reuse and a real resource reload. Pixel checks require the helmet above the boots and colored items in every inventory row. Vulkan validation reports zero errors and releases all tracked buffers/images on shutdown. These are Linux Mesa software-driver checks; the Windows bundle is packaged and audited rather than run on Windows hardware.

## Continuous local water progression and the 60 Hz request

Previously one update consumed only the active cells present at its start, leaving newly activated falling/spreading cells until the following game tick. Three substeps now allow that front to continue during the same host tick. Gravity/spreading retains a separate budget from communicating-pool pressure; unresolved work remains in a coalesced FIFO and settled work disappears.

The nominal rate is **three transport passes × 20 server TPS = 60 substeps/second**. They execute sequentially on the owning server thread within each 50 ms host tick. This is not an independent wall-clock 60 Hz server, a guaranteed 60 FPS minimum, or unlimited flood-to-completion processing. Network/world commits retain the host cadence; existing 100 ms GPU height/opacity transitions are evaluated every rendered frame. Server lag and work saturation can reduce effective progress.

Per-level limits are 6,144 active-cell visits, 6,144 downhill-search visits and 12,288 pressure-search visits per tick, split across the three passes. Each local search still sees at most 128 cells; loaded borders remain sealed. Larger communicating networks and regional hydrology need further work. Removing the work list would sacrifice bounded server execution without solving those topology limits.

The native filling fixture includes 2,304 competing wake entries, 1,024 settled cells, one-unit films, a plant obstruction, a dormant generated column and a finite reservoir. Water falls three blocks in its first host tick, the 32-unit basin fills by tick four with a maximum one-tick progress gap, all tested volume remains conserved, and pending work reaches zero. A separate U-bend test preserves 144 units, raises the lower communicating surface, respects a blocked passage and avoids force-loading missing chunks.

## Hidden water pages

Fully submerged/occluded sections used to retain active page slots even when their mesh contained no faces. Their residency competed with visible liquid and could lead to repeated capture/eviction work. Stable empty meshes now release their active page and enter an 8,192-key bounded negative cache. Fluid/neighbor edits, section/light changes, chunk arrivals/departures and resets invalidate that result. Nearby exposed water is admitted immediately to the normal bounded work path. A result made obsolete during meshing cannot enter the negative cache.

The generated-ocean regression checks both APIs, animated/fallback rendering, LOD/waves, actual server volume, resource reload and zero settled empty active pages. It opens a client-only hole in a cached submerged section, requires a GPU mesh to return, closes the hole and requires the active page to be released again. This reduces wasted residency; the positive-page/GPU budget and complete large-view coverage problem still require further work.

## Version-two terrain and sample reuse

New normal worlds serialize `deepprimal:continental_v2`. Existing `continental_v1` decoding, generation and saved height remain available. Version two lowers the tributary catchment threshold, starts small channels narrower, varies width along reaches using connected catchment area and forms intersecting valleys by union of cuts. Another segment cannot raise a bank rim across an already carved channel. Tree support is checked around the footprint before placement beside channels.

This is a refinement of the independent continental planner, not a complete replacement of its remaining drainage/lake model. The twelve-thousand-point, three-seed check requires deterministic replay, finite terrain, water above its bed, no river cuts above uncut ground and reproducibility after cache eviction. Existing version-one differential checks still require 1,800 samples to match the published terrain bit for bit. Native checks cover new-world selection, codec round-trip, reopening version two and retaining version one, with fixed-seed coast/river/ridge captures.

The chunk adapter retains its 256 generated surface samples for decoration and footprint checks rather than recomputing them. Differential native runs load the previous and current adapter from separate installers and require identical SHA-256 digests of all block states in nine generated chunks. Startup/request wall times are diagnostic and include warm-up and dependency loading; they do not establish an FPS improvement.

## Video Settings

The six tabs retain staged Apply/Revert behavior, numeric entry, native increments, tooltips and accessibility. Flat buttons and slider tracks replace textured controls; numeric values appear once, changed rows have markers, search filters the current tab, and a scrollbar shows additional controls. Target FPS appears under Performance. Distance text distinguishes the staged request from the current limit and identifies memory/server limiting.

Native tests exercise search, Performance value reopening, resize to 340×240, staged edits, revert, Escape, programmatic video-menu replacement, native preset overrides, display/texture updates and a complete restart. The persisted native menu value is 3 and custom pacing target is 89. OpenGL and Vulkan menu checks pass.

## Completion status

The following remain implementation requirements: coherent basin lakes/spillways and cross-region drainage; native renewable watershed/aquifer/snow/ocean accounting; displacement, sixteen-unit machinery ports and broader interactions; source-carried composition; breaking shoreline surf, tides and circulation; complete snow and geological content; LOD unseen-terrain contracts, stronger hierarchy/coverage and mixed-transparency handling; and large-view animated-water cache coverage. Existing asset palettes and placeholder tree models remain.

The references continue to inform independently authored systems. No new TFC/Terralith/Tectonic, PhysEx, Distant Horizons, Gaea/World Machine or meshoptimizer runtime is incorporated by this update. Performance superiority requires matched measurements.

Compact records and final package hashes are in [native evidence](validation/0.0.1-alpha.23/native-evidence.txt) and [installer audit](validation/0.0.1-alpha.23/installer-audit.json).


<!-- END PRESERVED SOURCE -->
