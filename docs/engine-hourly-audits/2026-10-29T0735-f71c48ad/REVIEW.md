# Deep Primal Dev80 independent auditor — pinned Java 25 review

**Pinned committed main:** `f71c48ad869e6c2ecd6ad02194d07e3a108c1a20` (Dev80 version `0.0.1-alpha.29-dev.80-interior-ports.1`). **Builder implementation:** `c8d65a5ac6d7a7d4a6940cc8873cd9d8e5dbb9d3`. Audit identity: `2026-10-29T0735-f71c48ad`. Read-only auditor, independent Linux directory; builder checkout/lease/measurement not touched.

## Independent evidence

- Java **25.0.5+8**, exact Git blob identity: `WatershedInteriorPortRoutes.java` = `93f82561d5d856af70f56ac9de1b6a20d21cc144`, and `WatershedOutletTopology.java` = `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`; strict `javac --release 25 -Xlint:all -Werror` succeeded. This compiles the two exact pinned classes, **not** the complete watershed or NeoForge project.
- Independent DAG/route oracle ran twice: **1,600 synthetic grids, 6,036 routes and 88,017 assertions per run** (176,034 total, both passed). Verified signed coordinates, first receiver, exit, exact hops, crest and invalid-input rejection.
- Two independently compiled defective helper versions **both rejected**, for erased route crest and disabled adjacency guard.
- **Scope caveat:** standalone helper accepts a disconnected interior receiver cycle and a perimeter→interior→same-perimeter cycle as empty route lists. The intended production `WatershedRegion.routingSnapshot()` uses `boundarySnapshot()` verification first; do not treat this standalone-helper precondition gap as demonstrated native gameplay failure. Consider explicit whole-array acyclicity before untrusted use, or enforce/document the verified-producer-only precondition.
- New `WatershedRegion.routingSnapshot()` and `WatershedRegionPortBridge.assembleRouted()` were source-inspected, not compiled against full production graph. This is **opt-in observational routing**, not active native generation, ocean outlet certification, water quantity/constituent accounting or gameplay proof.
- Builder-reported Java21 Dev80 fixture evidence was inspected, not rerun here. Root work-state remains older Dev53 and the full repository validator was not run.

## Full rubric (carried; no fresh regrade)

Terrain realism **5**, biome coherence **4**, hydrology/caves **6**, features/structures **4**, performance/threading **3**, determinism **7**, architecture **6**, extensibility **4**, tests/docs **7**, polish/tooling **4**. **Total 50/100, average 5.0/10, Tier 2** from `docs/engine-review-2026-10-24/REVIEW.md`. This is a previous accepted complete grade carried forward. No credit awarded for inactive planners; the fresh `>8.5/10` threshold is not met.

## Unrun and priorities

**No new JAR.** October 27 owner override assigns full Gradle/Minecraft/GPU builds to owner; `git ls-remote` also failed DNS. Full Gradle 9.2.1 Java25 build, native save/reopen/shutdown, packaged CRC/compiled class/native/notice/Theme checks, physical water/salt/sediment conservation, Windows AMD8060S Vulkan/OpenGL and performance/visual soaks **not run**. A Java25 isolated Linux fixture is not Windows acceptance. No releases, builder state edits or JAR retention mutations.

Next: certify global marine/outlet topology and interior-to-port integration, once-only physical discharge/constituents, opt-in native saved V8 lifecycle, frozen V1–V7 and old/new boundary parity, Windows renderer water continuity, and fresh complete ten-category audit. Audit progress does not gate Builder.

**Library evidence:** `Deep Primal/Independent Audits/DeepPrimal_Dev80_IndependentAudit_20261029T0735_f71c48ad_Java25Evidence.zip`; **15,248 bytes**, SHA256 `b7218d37c007a66ffa5fbcee4a99e08256b35a746560edbf3b96f71913c9c6a6`, ZIP CRC and member hashes checked.
