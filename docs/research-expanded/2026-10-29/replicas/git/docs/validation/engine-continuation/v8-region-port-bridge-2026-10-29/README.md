# Expanded replica — docs/validation/engine-continuation/v8-region-port-bridge-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-region-port-bridge-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-region-port-bridge-2026-10-29/README.md |
| Preserved source SHA-256 | `c5af54fffcc0674d4455b332f053f7efc98df166a465e14ffc7beb9eb9af9789` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

The Dev76 receipt adds an opt-in production boundarySnapshot export and a bridge from region perimeter observations to the Dev75 topology helper. It reports 1,024 perimeter samples, exact signed-lattice seam matching, two-copy edge/four-copy corner ownership, explicit unknown frontiers, and zero planned runoff. Its executed adapter fixtures use synthetic region/terrain API doubles, while the modified full production watershed dependency was not compiled. The receipt also limits identity evidence to a semantically equivalent pre-publication snapshot. Interior topology, trusted anchors, stage feedback, native activation, and full finite-water acceptance remain open.

## Research expansion — gaps and details to deepen

- Actual limitation: perimeter adjacency alone does not reconstruct interior receiver paths, lake outlets, upstream discharge partitions, or global marine certification. Zero runoff is intentional and cannot be replaced by summing cumulative area.
- Historical contract gap: this checkpoint checks shared ground equality but does not establish agreement of flood elevation, stage, area, classification, and receiver observations. The later Dev77 receipt must be linked as its scoped successor.
- Evidence limitation: the local positive run used a semantically equivalent pre-publication snapshot, not independently recompiled exact published source/test blobs. That distinction prevents automatic package/source qualification.
- Elaboration: missing neighboring copies and caller-provided anchor certificates need observable uncertainty and provenance rather than an assumption that a successful bridge has globally complete coverage.

## Research expansion — proposed contracts

- Define boundarySnapshot as immutable observations stamped by world/profile, geometry, hydrology-plan revision, and source owner. Observations cannot independently authorize physical transfers or create runoff.
- Require exact canonical shared-edge/corner ownership and a declared compatibility check for every hydraulic field, with structured rejection reasons and no averaging of contradictory plans.
- Retain unknownFrontier until all required neighboring observations are available and compatible. Anchor certificates name the independently verified topology/source rather than merely a caller boolean.
- Keep planned runoff zero until an independent nonduplicating source-partition algorithm exists; cumulative catchment remains an observation with area units.
- Require exact published-blob replay and real production dependency compilation as separate promotion gates before native integration.

## Research expansion — acceptance checks

- Test two-copy shared edges, four-copy corners, missing copies, duplicate origins, nonaligned keys, incomplete perimeters, and negative coordinates.
- Inject matching ground with unequal stage, flooded height, area, water kind, or receiver; link the expected later Dev77 rejection contract rather than count Dev76 as fully reconciled.
- Compare zero runoff and unchanged physical accounts before/after bridge construction, including adversarial cumulative-area values.
- Reproduce the 1,791-port/1,792-link fixture from exact published blobs and separately exercise actual boundarySnapshot output from the compiled real watershed.
- Measure bounded admission with twelve snapshots and topology limits while preserving explicit unresolved frontiers.

## Research expansion — source anchors

- Production changes and invariants
- Focused checks
- **Evidence boundary:**
- **Unfinished:**

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal dev76 — native watershed boundary snapshots and conservative V8 port draft

**Engineering scope:** new opt-in export of real production `WatershedRegion` perimeter nodes, and `WatershedRegionPortBridge` which joins exact signed-coordinate neighbor seams into dev75's `WatershedOutletTopology`. No native worldgen feature activation.

**Consumed independent audit:** `docs/engine-hourly-audits/2026-10-29T0935-8968d3d/REVIEW.md` pinned to `8968d3d007bd1c9e976710c43a2ed1553480793b`. That independent Java25 audit passed 93,015 oracle assertions for the existing dev75 helper and flagged the missing actual-region port extraction and trusted global marine anchors.

## Production changes and invariants

- `WatershedRegion.boundarySnapshot()` exposes 1,024 real native region perimeter cells, including exact integer coordinates, initial ground, priority-flood height, routed stage, cumulative catchment area, ocean/lake flags and local receiver key. Export is explicitly called only and has no effect on V1-V7 or any saved geography.
- The bridge bounds admission to 12 watershed snapshots and the existing topology's 16,384 ports/65,536 links. Shared 32-block lattice coordinates must match ground bit-for-bit. Perimeter links use only actual adjacent samples; no interior shortcuts, cross-region flow edges, or marine roots are fabricated.
- Missing neighboring regions stay `unknownFrontier`; complete signed-cell seam ownership requires 2 regional copies on a shared edge and 4 at a region corner. A certified ocean anchor must be explicitly supplied, be observed by every touching region, and have terrain no higher than sea stage. This is NOT a proof that the caller's certificate is globally authoritative.
- Planned runoff is **zero by design**. Existing cumulative `WatershedRegion.area` values are not independent additive runoff and must not be summed into physical water. The draft alters no water quantity, constituents or stage.

## Focused checks

Local OpenJDK **21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: isolated adapter + production dev75 solver source compiled against labeled synthetic `WatershedRegion`/TerrainEngine API doubles. Two positive passes yielded **3,901 checks each**, 1,791 unique seam ports, 1,792 perimeter links, 257 deduplicated shared coordinates and 1,536 explicitly unresolved frontiers. Reversed ordering, parallel execution, negative-coordinate seams, strict ocean-anchor certification, height discrepancies, duplicate origins, incomplete perimeters, and non-generation of runoff passed. **Three separately compiled broken variants rejected** (both seam-parity guards removed, unknown-frontier guard removed, fabricated runoff). Runner also passed with Python `-OO`.

Reproduce in full checkout:
```sh
python3 tools/test_watershed_region_port_bridge.py --output /tmp/deepprimal-v8-port-bridge
```

**Evidence boundary:** The **modified full production WatershedRegion** (55 KB source plus new opt-in export) was not compiled against its complete real dependency graph. Tests use isolated API doubles and do not establish real Minecraft routing, saved-profile reopening or native-physics conservation. The local positive receipt is from a semantically equivalent pre-publication snapshot; identical Git blob/test identities were not independently compiled after publication.

**Unfinished:** true interior receiver/port topology, upstream discharge partitioning, fully verified global marine/outlet mask, lake-spill stage feedback into regional priority-flood, new saved routing policy and native V8 chunk integration. Java25/Gradle, real Minecraft, native save/reopen, old/new borders, OpenGL/Vulkan temporal waterfall pixels, hardware p95/allocations, multiplayer soak and finite-water/chemistry lifecycle are all open. No qualified JAR, new owner-playable build or independent regrade. Last full external grade remains 50/100 (5.0/10). Owner retains full build/JAR publication.


<!-- END PRESERVED SOURCE -->
