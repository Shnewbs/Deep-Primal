# Expanded replica — docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md) |
| Source identity | git: docs/validation/engine-continuation/v8-region-seam-contract-2026-10-29/README.md |
| Preserved source SHA-256 | `0d7fc9887af1e6c37c3942dda1fa39450525a7e5f2f31cd1b261c13e44b46f56` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/terrain-water.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

This Dev77 source checkpoint hardens bridge merge admission after an independent Dev76 adapter witness accepted equal-ground observations with flooded/stage values of 90 versus 900. It now rejects incompatible hydraulic elevations, catchment, ocean/lake flags, and receiver identities while preserving exact lattice keys, explicit frontiers, certified-anchor requirements, and zero observational runoff. The receipt reports exact source/test/runner blob reconstruction, repeated Java21/API-double checks, eight rejected mutations, and a separate unchanged helper replay. No native V8 path calls the bridge, and real watershed reconciliation, saved-profile activation, conservation, and full-platform qualification remain open.

## Research expansion — gaps and details to deepen

- Important distinction: rejecting incompatible observations prevents false agreement but does not reconcile region-local plans. Real adjacent regions may legitimately disagree until an authoritative interior/outlet algorithm produces one shared hydraulic result.
- Actual open integration: the bridge has no native V8 caller and its real production WatershedRegion dependency graph was not compiled. The reported seam witness is an adapter contract defect, not a live native liquid failure.
- Elaboration: exact field equality needs a schema defining units, finite-value handling, signed-zero policy, receiver canonicalization, and compatible source revisions before observations can be admitted safely.
- Actual remaining design is explicit: future saved routing identity, independent runoff accounting, lake-spill/discharge feedback, and finite-water lifecycle tests are still required.

## Research expansion — proposed contracts

- Return a structured seam result: compatible, missing-neighbor, or incompatible, with the conflicting fields and source stamps. Incompatibility remains pending/rejected; do not average fields, choose arrival order, or silently force one side.
- Specify canonical receiver keys and numerical normalization at observation creation. Reject nonfinite hydraulic data and unsupported revisions before bitwise comparison; preserve frozen behavior through explicit policy versioning.
- Separate observational bridge admission from authoritative topology reconciliation. A new saved policy must produce consistent interior receivers, spill stages, and independent runoff partitions before native activation.
- Carry zero observational runoff through all accepted/rejected paths and enforce distinct types for cumulative catchment, planned graph runoff, and live water quantities.
- Keep the exact-blob/mutation receipt and unchanged real-helper replay separate; require actual production dependency, native saved-world, and conservation gates before claiming a completed seam solution.

## Research expansion — acceptance checks

- Reproduce the 90-versus-900 mismatch in both orders, then mutate each hydraulic field independently; require rejection without changed quantities or topology publication.
- Test identical observations, canonical receiver equality, nonfinite data, signed-zero policy, mixed revisions, and missing edge/corner copies under the chosen schema.
- Run parallel/reversed/cache-evicted bridge queries and verify stable compatible outputs and explicit unresolved frontier counts.
- Compile the full real watershed and inspect natural multi-region plans; incompatible observations must remain visible until genuine reconciliation exists.
- Before native adoption, run genuine profile reopen, old/new borders, discharge/spill accounting, and zero-drift water/salt/sediment restart and edit fixtures.

## Research expansion — source anchors

- Production code change
- Tests actually executed
- Unaccepted gates
- **Next implementation:**

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dev77 — fail-closed watershed seam observations (2026-10-29)

Consumed the completed independent Dev76 audit, `docs/engine-hourly-audits/2026-10-29T1032-00921e12/REVIEW.md`, pinned to `00921e12323bb759a22b557b0d07b8758ee1d753` (50/100 carried; not a regrade). Its reproduced failure was that two adjacent 8,192-block watershed boundary snapshots could report the same ground coordinate but `floodedElevation` / `plannedStage` **90 versus 900**; the Dev76 bridge accepted these incompatible water states and produced the same topological ports and links. The snapshots could also disagree on receiver/catchment/water classification without detection. This is an adapter contract defect, not evidence of a live native liquid bug.

## Production code change

`WatershedRegionPortBridge.Sample.merge` now rejects bit-unequal shared groundwater/flood elevations, planned stages and cumulative catchment values, and rejects differing ocean/lake classifications or downstream receiver identities. It continues to reject unequal terrain heights, preserves exact world lattice keys, unknown frontiers and explicit marine certification, and continues assigning zero *physical* runoff to observational ports. Identical paired observations retain deterministic previous topology; no native V8 generation path calls the bridge yet. This intentionally prevents region-local flood snapshots from silently masquerading as a globally reconciled watershed; authoritative interior/outlet paths still require a new opt-in saved revision.

No V1–V7 geography, old V8 saved profiles, graph fingerprints, art, player edits, finite water, chemistry, renderer, active material/vegetation or height-stage values were changed. The `mod_version` change is a **source checkpoint only**, not a new playable JAR.

## Tests actually executed

Host: OpenJDK `21.0.12.1`, javac `--release 21 -Xlint:all -Werror`, runtime `-ea -Xverify:all`. Isolated Java API-double boundary snapshots, not native Minecraft/production watershed dependency compilation.

- `python3 tools/test_watershed_region_port_bridge.py --output <fresh-dir>`: two positive processes, **3,909 assertions per process**, deterministic **1,791 ports / 1,792 links / 257 shared signed coordinates / 1,536 unresolved frontiers**; **8/8 separately compiled broken controls rejected**. Original three controls plus new flood-only, stage-only, catchment, ocean/lake classification and receiver controls. The independent audit's 90-vs-900 mismatch is now rejected in both region orders.
- `python3 -OO tools/test_watershed_region_port_bridge.py --output <fresh-dir>`: same two positive processes and all eight rejected controls.
- Separate unchanged `WatershedOutletTopology` source-only Java21 fixture: **18,111 assertions**, all **3/3 compiled negative controls rejected**. Its source Git blob remains `068d75588c41eb3b4af5a813b24b8dc9db4c9e4a`.
- The three changed source/test/runner files were reconstructed from exact pinned Git blobs and have byte-exact local/test Git blob identities equal to the GitHub objects used for publication. See `results.json`.

The source-only runner does not invoke `tools/test_ecotone_blend.py`; the ecotone source itself was not modified. This cycle did not execute `tools/validate_engine_work_state.py` because a complete isolated root checkout and its pinned 130 KB root JSON were unavailable. The root work-state was neither claimed nor modified. Git CLI could not resolve GitHub; the GitHub connector supplied the current refs, leases and the direct-main source objects.

## Unaccepted gates

Actual full Java 25/Gradle 9.2.1/NeoForge compile, complete production `WatershedRegion` class link, old/new native chunk borders, saved V8 profile reopening and edits, river discharge and lake-spill accounting, finite water/salt/sediment loaded/unloaded restart accounting, controlled OpenGL/Vulkan water surfaces, native multi-seed terrain/ecotone transects, and bounded owner-hardware CPU/memory/frame/tick latency are **not run**. No qualified JAR or external score change.

**Next implementation:** explicit saved V8 revision consuming a reconciled interior/outlet topology with independent runoff accounting; avoid sourcing physical volume from cumulative catchment. Terrain/ecotone weighted surface/vegetation and relief blending still require native activation/acceptance, and the ongoing finite-water lifecycle work remains open.


<!-- END PRESERVED SOURCE -->
