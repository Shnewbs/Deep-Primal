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
