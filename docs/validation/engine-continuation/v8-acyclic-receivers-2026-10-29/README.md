# Dev79 — reject cyclic V8 watershed receiver observations

Scope: independent Dev78 audit repair; opt-in source-only cross-region outlet planning. Consumed `docs/engine-hourly-audits/2026-10-29T1231-bee0509a/REVIEW.md` pinned to `bee0509a258c9b7b9a3c735a8f998f903b6423b4`, which reproduced equal-stage adjacent parents pointing at each other and collapsing to one undirected spill link. The complete independent grade remains **50/100 (5.0/10)**, unchanged.

## Production changes
- `WatershedRegionPortBridge.assemble` now validates the **directed** observed perimeter receiver forest before any graph is returned. Equal-stage two-node and longer directed cycles are rejected, including negative-coordinate cases; a legitimate one-way receiver is unchanged.
- Iterative O(observed nodes), bounded by the existing admitted 12-region / 16,384-port limits; no recursion, mutable global state or water creation. The check cannot prove unseen interior routing.
- `WatershedRegion.boundarySnapshot()` now invokes its **existing full-region `verify()`** before exporting perimeter observations. This checks producer-side interior route order even though public snapshot records can still be caller-fabricated.
- `tools/tests/WatershedRegionPortBridgeChecks.java` adds the independent two-cycle reproduction, three-cycle, signed two-cycle and valid one-way control. Runner includes a compiled guard-removal mutation.
- Source version `0.0.1-alpha.29-dev.79-receiver-acyclic.1`. No V1–V7 geography, saved V8 profile, water quantity, rendering files or old worlds changed.

## Executed evidence
- OpenJDK **21.0.12.1** `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: `V8_REGION_PORT_BRIDGE_PASS assertions=3923 seamPorts=1791 seamLinks=1792 shared=257 unresolved=1536`, run twice.
- Unchanged outlet-topology fixture: `WATERSHED_OUTLET_TOPOLOGY_PASS checks=18111`.
- Recompiled original Dev78 bridge against the new checks: **rejected** at `accepted invalid equal-stage reciprocal perimeter parents`.
- Recompiled intentionally broken Dev79 bridge omitting the cycle validator: **rejected** at the same regression.
- Python 3 `-OO -m py_compile` runner syntax check passed. The full multi-mutant Python runner initially timed out and is **not** counted as passed; only the independently executed negative controls above count.
- Exact local Java21 bridge/checks/runner test content matches Git blobs `4227213b3d0a6a45f32c284706186510cd817d50`, `76f4b5ca35e2da3b579555e90c454c3b81e4e81d`, `43dd402e28dc811087f4a0c4ab5107aa066c5eb0`.

## Open gates and coordination
The modified full `WatershedRegion` dependency graph was **not** compiled, and no Java25/Gradle9.2.1, real NeoForge/Minecraft, actual saved-world reopen, independent GPU, physical water/salt/sediment lifecycle, or bounded native CPU/memory test was performed. The full pinned root work-state validator was unavailable with only a partial checkout; root `docs/ENGINE-WORK-STATE.json` remains untouched. Terrain blending saved policy and column preflight were preserved, but native ecotone materials/vegetation and versioned density joins remain open. Exact interior-to-port routing, once-only discharge, global marine connectivity and water lifecycle remain open.

This code respects the owner-operated build/JAR directive; no playable build or release is claimed. The renderer's concurrent LOD changes were excluded.
