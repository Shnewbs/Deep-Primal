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
