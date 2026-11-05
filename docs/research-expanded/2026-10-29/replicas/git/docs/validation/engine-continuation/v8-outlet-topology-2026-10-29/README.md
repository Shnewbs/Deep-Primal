# Expanded replica — docs/validation/engine-continuation/v8-outlet-topology-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-outlet-topology-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-outlet-topology-2026-10-29/README.md |
| Preserved source SHA-256 | `35ce806c98a0d2eb3b37112827d306ef7129bce2a9fa50fda671c70cdd17116d` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Dev75 adds a production core helper that solves a bounded minimax priority-flood graph over explicitly supplied regional ports. It selects deterministic acyclic receivers toward independently verified ocean anchors, preserves unresolved frontiers, distinguishes inland spill from direct marine connection, and accumulates planned integer runoff with checked arithmetic. The helper has direct standalone Java21 tests without Minecraft API doubles, plus separately scoped older adapter regressions. It is not called by native V8 or the existing region planner. Port extraction, anchor provenance, live water, splits, capacities, native persistence, and performance remain open.

## Research expansion — gaps and details to deepen

- Actual integration gap: the helper assumes valid ports, links, frontier flags, and certified ocean anchors supplied by another owner. It does not itself prove those inputs correspond to actual terrain or globally connected marine water.
- Elaboration: planned integer runoff conservation is a graph invariant, not physical water conservation. The similarly named quanta require distinct types and must not be combined with live stores or cumulative catchment.
- Actual limits acknowledged: single-receiver routing does not establish distributary splits, storage, dams, lake stage feedback, or native hydraulic transport.
- Evidence clarification: direct real-helper tests are stronger than an API-double check at that boundary, but do not qualify the unconnected native caller or the Java25/NeoForge build.

## Research expansion — proposed contracts

- Define a versioned port schema with stable world/profile identity, exact lattice key, crest elevation, verified neighbor links, uncertainty, and separately evidenced ocean-anchor authority.
- Use distinct PlannedRunoffQuanta, CatchmentArea, and WaterQuanta types. Planned input equals terminal planned output exactly; only live accepted-transfer transactions may move physical quantities.
- Specify deterministic receiver tie-breaking, acyclicity, spill-crest semantics, and unknown-frontier propagation under the declared 16,384-port/65,536-link admission limits.
- Add explicit basin storage and distributary allocation contracts before extending the helper beyond single-receiver planning. New geometry/topology policies require saved identity and old-profile replay.
- Qualify an actual region export/caller separately, preserving the standalone oracle and mutation tests as evidence of the helper rather than whole-engine completion.

## Research expansion — acceptance checks

- Supply unverified anchors, missing links, duplicate ports, unknown frontiers, maximum-capacity graphs, and overflow-prone runoff; require explicit rejection or unresolved status.
- Check minimum spill crest and acyclic receivers against a small independent exhaustive graph oracle under scrambled signed-coordinate ordering.
- Verify planned input equals terminal output while every physical water and constituent account remains unchanged.
- Exercise lake-versus-marine classification, equal-crest ties, and closed components before connecting the helper to real regional topology.
- Run native export/reopen and measured CPU/memory tests only after the actual caller exists; keep those results separate from the 18,111 helper assertions.

## Research expansion — source anchors

- **Implementation:**
- **Runoff accounting:**
- **Current integration boundary:**
- **Source-only test:**
- **Not performed:**

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev75 — Bounded cross-region watershed outlet topology (source only)

**Implementation:** `WatershedOutletTopology` is a new production `engine-core` minimax priority-flood graph for explicitly supplied regional river/lake/coast *ports*. It treats cross-region seam connections as ordinary signed-world-coordinate links, chooses a deterministic acyclic receiver toward an independently verified ocean anchor, and computes the minimum spill crest to that anchor. Inland lake spill remains distinct from direct marine connection. No region edge is automatically an ocean anchor. When the supplied component has unknown frontier connections, its unresolved classification remains explicit rather than inventing global sea connectivity. Port coordinates use exact 32-block alignment; independent work is bounded at 16,384 ports/65,536 links.

**Runoff accounting:** deterministic single-receiver routing accumulates *planned integer runoff quanta*, uses overflow-checked addition, and verifies input quanta equal terminal quanta. This is **not** the finite live-water/constituent solver: it makes no world changes or quantity transfers, and does not create or destroy physical water. Split/distributary accounting and storage/dam capacities still require separate work.

**Current integration boundary:** The helper is in normal production `engine-core` source but is **not yet called by native V8 chunk generation or the existing `WatershedRegion` planner**. Valid ports, cross-region links, uncertainty flags and globally verified ocean anchors still need to be exported by the authoritative watershed. The earlier V8 routing v1–v7, persisted graphs, callback recovery, V1–V7 geography, approved art, and finite-water accounts are untouched. This checkpoint is not complete hydrology or terrain blending.

**Independent audit consumed:** `docs/engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md`, pinned `15d01f6c80c1b54555b40c996d93bbe653ecf867`. Auditor preserved 50/100 (5.0/10) and identified global outlet topology, native cross-region lake spill and conservation as open. Its renderer extreme-yaw finding belongs to the separate renderer worker, not this change.

**Source-only test:** `python3 tools/test_watershed_outlet_topology.py --evidence /tmp/port-test.json`. OpenJDK 21.0.12.1 `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`, 18,111 positive assertions passed. 300 scrambled signed-seam replays, 150 multiport-runoff cases, 512 deterministic tie orderings, 16 parallel solves, duplicate/missing/unbounded/overflow rejects, marine/closed/unresolved/inland-spill branches. Three separately compiled broken controls rejected (unverified ocean anchors, doubled routed runoff, suppressed frontier uncertainty). Reproduction uses actual standalone production helper and its test, **no Minecraft API doubles**; it is not Java25/Gradle/native/graphics/whole-engine evidence.

**Not performed:** complete root work-state validator or checkout (Git CLI DNS), Java25/Gradle9.2.1/NeoForge compilation, real native watershed/planner integration, Mojang save/reopen, visual coastal/river transects, finite-water unload/constituent/long soak, Vulkan/OpenGL, named-hardware CPU/memory bounds, mixed-generation borders, new JAR. Owner remains responsible for full builds. The existing independent full grade remains 5.0/10, not regraded.

**Next:** Export authoritative cross-region lake outlets and discharge from the real `WatershedRegion`, tie ports/ocean anchors to versioned V8 saved profiles and complete regional topology before any gameplay activation; then include explicit legacy/new border rules, exact finite water ledger conservation and native checks. Continue climate-weighted surfaces and versioned pre-hydrology landform blending in the engine scope.

**Additional independent-of-new-helper regression replay:** Unmodified Dev74 source/API-double portable suite, 19/19 processes passing under Java21. This is deliberately separate from the 18,111 real-helper checks and does not upgrade them to native/Java25 evidence. Dev75 source/evidence archive: 13,397 bytes, SHA256 `b3599ab36af9874e0a356da82a4430d3c8080c1234b5760ca7469de7363cf82c` (member hashes and CRC verified). The original ecotone-specific standalone script was unavailable in the portable checkout and not rerun.


<!-- END PRESERVED SOURCE -->
