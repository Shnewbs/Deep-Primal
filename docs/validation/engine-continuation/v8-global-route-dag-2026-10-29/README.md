# Dev81 — global receiver-DAG validation before V8 port extraction

Base: `4f809f64b64a8dfc0408bcd03558f24a12578e6b` (bounded scoped writer lease on independent-auditor main). Source version: `0.0.1-alpha.29-dev.81-route-dag.1`.

## Audit consumed

`docs/engine-hourly-audits/2026-10-29T0735-f71c48ad/REVIEW.md`, pinned at `f71c48ad869e6c2ecd6ad02194d07e3a108c1a20`. Its independently compiled Java25 positive oracle passed 88,017 assertions per run but found the standalone `WatershedInteriorPortRoutes.trace` could return an empty list on disconnected interior cycles and perimeter → interior → same-perimeter cycles. Audit correctly distinguished that helper precondition gap from demonstrated native gameplay failure.

## Code change

`WatershedInteriorPortRoutes.trace` now scans the **entire** receiver array with an iterative three-state functional-graph traversal before any port extraction. Every parent is already locally checked for adjacency, finite ground/stage and downstream stage. The new complete-graph pass rejects cycles **even if no traced boundary reaches them**. The existing bounded scratch arrays are reused; no recursion, shared mutable state, chunk access, ocean claims, native chunk writes or physical water accounting. Existing valid routes and memoization remain unchanged.

The Java21 fixture adds disconnected interior cycles at positive/negative origins, perimeter-interior self-return, perimeter two-cell cycles, and a valid disconnected acyclic chain. A compiled negative control turns off the global rejection test. Previous Dev80 source is shown to fail the newly added disconnected-cycle case.

## Executed validation

- Linux OpenJDK 21.0.12.1; `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`.
- `python3 tools/test_watershed_interior_ports.py --output ...`: two positive processes, 1,040 assertions each, **pass**, and three independently compiled broken controls correctly rejected.
- `python3 -OO tools/test_watershed_interior_ports.py --output ...`: two more positive processes, 1,040 assertions each, **pass**, and all three broken controls rejected again.
- Previous Dev80 source plus new Java21 test fails at the new disconnected interior cycle (negative regression).
- Existing `WatershedRegionPortBridgeChecks` strict Java21 API-double positive run: **3,931** assertions, pass.
- Existing `tools/test_watershed_outlet_topology.py`: Java21 production helper positive **pass** and three compiled negative controls **rejected**. Its independent reported corpus is not mixed with the route counts.

**Exact locally tested Git blobs**: production `6b527106571054e88352cc5bdf07b289fdca73a0`, Java test `cd0b57873c27b62bac26090c87219357bca8fa6c`, Python runner `ff042d3c2eb1061a4435b5b0ec7add161e83a673`.

## Unrun gates / policy

This is Java21 isolated engine-core and API-double verification, **not** real production `WatershedRegion` dependency compilation, Java25 full Gradle 9.2.1, NeoForge, native Minecraft save/reopen, live water, Vulkan/OpenGL, owner AMD hardware latency, finite water/salt/sediment accounting, or visual terrain-blending acceptance. Existing V8 policies/profiles and V1–V7 geography are untouched. No native generator activation, new playable JAR, publication or independent regrade. Last accepted full rubric remains 50/100 (5.0/10).

Complete local repository and pinned root work-state were not available to the builder; `tools/validate_engine_work_state.py` was not run, and root work-state was **not modified**. Scoped terrain state and Git lease are handled separately. Owner performs full builds and JAR publication.

Next: authoritative cross-region port discharge/constituent ledger and versioned V8 native integration, plus activation/gates for ecotone vegetation and pre-hydrology landform joins; retain frozen saved policies and all water acceptance gates.
