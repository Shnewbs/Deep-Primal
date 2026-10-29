# Deep Primal dev80 — observed interior-to-perimeter watershed receivers

**Source version:** `0.0.1-alpha.29-dev.80-interior-ports.1`. This is an opt-in, dependency-free planning and observational-bridge repair; **not** active native V8 world generation, a physical water ledger, nor a playable build.

## Coordination and audit

- Base claim on main: `5a0718cb3f5c0531516ad0ea165d5d033b1425a6` (CAS-parent guarded).
- Latest completed independent audit: `docs/engine-hourly-audits/2026-10-29T1333-83801d65/REVIEW.md`, pinned to `83801d658dc717be63bb0e3a301084cc7443d126`. Audit confirmed Dev79 perimeter receiver DAG checks and identified missing interior-to-port paths, runoff accounting and native activation.
- Original rubric: `docs/engine-review-2026-10-24/REVIEW.md`; last complete accepted grade **50/100 = 5.0/10**, unchanged.
- Separate renderer-owned modules and water runtime were not edited. Water scoped writer state was already stale. Root `ENGINE-WORK-STATE.json` was not changed because the complete pinned checkout and root validator execution were unavailable.

## Source implementation

1. `engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedInteriorPortRoutes.java`: O(side²) capped, memoized first-downstream-boundary receiver tracing through interior watershed lattice cells. Validates finite ground/stage, adjacent receiver direction, monotonic stage, unsigned/signed coordinate bounds and directed cycles, without independent chunk reads, random state or guessed outlet paths. Outputs immutable origin, destination, first receiver, maximum observed bed crest and hop count.
2. `WatershedRegion.routingSnapshot()`: explicit producer call verifies the original whole-region parent graph using `boundarySnapshot()`, then exports real interior receiver traces from that same region. Previous constructors and existing boundary export remain unchanged.
3. `WatershedRegionPortBridge.assembleRouted()`: opt-in route consumer validates exact known perimeter endpoints, first receiver identity, downstream stage and crest; deduplicates stable links and rejects conflicting routes. Existing `assemble()` stays unchanged. **Observed catchment area is not physical discharge; solve input runoff remains zero** until quantity/constituent ownership and certified outlet topology are designed and qualified.

No V1–V7 terrain, player blocks, saved V8 profile, graph, physical liquid level, chemistry, water quantity, renderer source or approved art was changed. No native generator calls this new exporter by default.

## Source-exact tests and limits

Executed using **OpenJDK 21.0.12.1**, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` on explicit core-source/API-double fixture files; these are **not** full production WatershedRegion compilation or Minecraft native tests.

- `python3 tools/test_watershed_interior_ports.py`: **2 accepted positive JVMs**, each `V8_INTERIOR_ROUTES_PASS assertions=1035 routed=2 wideHops=256`. Tests include two tributaries sharing the same interior exit, 8,192-block region-wide routing, negative signed origins, bounded memoization, parallel deterministic runs, unchanged source arrays, sink rejection, malformed parent jump/stage and cycle rejection.
- **2/2 compiled broken controls rejected:** disabling receiver-neighbor checking admits invalid jumps; removing path-crest max changes the expected 90-block sill. Both compile before failing their assertions.
- Direct focused `WatershedRegionPortBridgeChecks` compilation and run: `V8_REGION_PORT_BRIDGE_PASS assertions=3931 seamPorts=1791 seamLinks=1792 shared=257 unresolved=1536`. It checks opt-in routed shortcut, unchanged perimeter-only default, reversed region order, source-parent identity, crest/stage, duplicate source, invalid non-perimeter target, and **zero invented physical runoff**. This is an API-double bridge compilation, not an actual native region run.
- A full bridge multi-mutation Python runner **timed out**; no completed result is claimed for that run. Older independent 18,111 outlet-topology assertions were not rerun here. Whole production WatershedRegion dependencies, Java25/Gradle9.2.1, NeoForge, real save/reopen, chunk/base height parity, finite-water quantity/salt/sediment accounting, Vulkan/OpenGL, CPU/memory and visual transects remain **unrun**.
- Tested helper/bridge/source/test blob identities: helper `93f82561d5d856af70f56ac9de1b6a20d21cc144`, bridge `cd14393174af306bf3a0ca3c4cf7bb26f3b4a937`, helper fixture `bfd3f19abcd307330a1b260172564f04247a7ec2`, bridge fixture `f874b9b54a46d8298e8e213f8679af1c0bb1d372`, helper runner `02e7892ecfaf027eb6905e1db31bfd3fb21800e3`, bridge runner `e157ade7fe6e3dbc5ec5adb197001013b36d9253`. Production WatershedRegion exporter blob `08c0a9e412c0a0a9842f2c5658bef2ff121a3d5d` is source-inspected but not fully compiled.

## Next acceptance gates

Native opt-in V8 saved-world generation and profile reopening; certified regional marine/outlet topology; once-only cross-region discharge and physical water/salt/sediment (including unload/restart/backpressure); old/new chunk border parity; explicit ecotone vegetation/material activation and landform blending **before** hydrology without changing frozen save results. Do not claim this planning-only step is global hydrology, terrain-blending completion, FPS or visuals. Owner continues full builds and JAR publication; last owner-confirmed playable build remains dev53. Completion requires a **fresh complete independent grade >8.5/10** and all water-engine acceptance gates.
