# R43 — bounded recovery after rejected water-boundary writes

2026-10-23. Staged on `work/engine-after-alpha24`, following `0e88a2b3841e70c42a76590d566aaa2bb18eef6b`. Engine acceptance is incomplete. Main, public production sources and supplied installers are unchanged.

## Defect and focused change

The fresh R42 product control propagates all 40 injected boundary-write rejections to its caller and has no automatic continuation. The fixture catches these exceptions so its client can finish saving. This is a controlled synchronous pre-backend reproduction, not 40 game crashes.

The independently authored renderer change defers rejected boundary ranges, continues other normal publication, and services recovery before optional extraction work. Recovery retains at most 16 ranges and 1 MiB of vertex data. A rendered-frame turn examines at most 16 stale headers and attempts one write of at most 65,520 bytes. Partially completed ranges rotate to the queue tail; their full retained arrays remain charged until removal. Queue overflow requests a fresh mesh through existing bounded scheduling. Three consecutive recovery-write failures on a range remain fatal, preserving the cause, rather than being swallowed indefinitely.

Each retry starts from the latest accepted CPU bytes. A core helper reapplies only the failed material-corner mask, preserving current height, light, opacity and unselected material fields. Full elapsed time distinguishes queued timestamps across later wraps; completed transitions become steady. A consumed buffer position on an initial rejection does not truncate the retained data. Accepted CPU bytes and cleanup commit after the corresponding GPU call succeeds. New page/material publication cancels stale jobs; incomplete older corners request a remesh when needed. Release and reset clear retained work.

Successful ordinary publication stays synchronous within its extraction. This implements bounded failure recovery, not normal cross-frame cohort publication or whole-cohort atomicity. Water quantity, physical solver, player edits, geometry, shaders, vertex layout and published V1/V2/V3 geography are unchanged.

## Fresh checks

- Two full builds pass, each with 25 executed tasks: R42 product control and candidate. Candidate checks add 470,000 known-input recovery cases across all 16 masks, large elapsed/wrapped clocks, nonzero offsets and current vertex fields. They retain 688,129 index cases and both 210,000-case packet suites, alongside terrain, watershed and module checks. The standalone quick recovery check repeats the same cases, not additional unique coverage.
- Four native sessions pass: fresh control, development Vulkan, immutable packaged Vulkan and OpenGL fallback. The same new fixture class is used throughout. Candidate sessions total 120 cases and 360 paired CPU/GPU reads: 48 automatic transient recoveries, 48 supersession/stale-page cancellations and 24 expected bounded terminal-failure controls. They execute 240 distinct rendered-frame recovery turns. Independent Python packet/range oracles check every captured byte, including unchanged data, expired failed ranges and current nonmaterial mutations. The fixture also checks 16-range backpressure, a 1,048,320-byte retained admission followed by rejection at the 1 MiB limit, and release/reset cleanup. These are private nondrawn pages, not actual scene cap churn.
- Candidate regression suites pass 768 normal boundary cases/1,536 paired reads, 48 admission cases/432 paired reads without budget-denied resident loss, 15,336 seam pixel comparisons/30,672 draws, 48 publication-consistency cases/192 reads, nine extraction-deadline cases and 129 distinct-frame retirement turns per session. Normal-boundary comparison uses a historical R42-captured R41 product control; admission/publication baselines are also historical. The new failure-recovery baseline above is fresh. The older publication fixture now recognizes intentional deferral through the production upload path while retaining CPU/GPU consistency and explicit manual reconciliation checks.
- Explicit saved disconnect, crash delta zero, Vulkan validationErrors zero, zero tracked resources and shutdown continuation pass. Six closed build/native raw/sealed console identities pass. Package checks pass six archive CRCs, 413 compiled-class identities and five nested/standalone identities. Private validation code is excluded; the audited packaged archive remains unchanged across launch. Three lake screenshots were inspected; water, banks and terrain render without an error screen. Different dimensions/load states and static images do not establish temporal flicker, enabled-view completeness or API parity.

## Self-audit and limits

One private fixture compilation attempt failed before Minecraft started because `row()` omitted a checked exception declaration; the original source and console are retained. An audit-script syntax error was corrected. Two oracle assumptions were also corrected, without changing product/native code: a newly constructed steady transition has age zero, and only failed ranges are rebased—already accepted ranges retain their original packet bytes. Failed oracle versions and the rationale remain reviewable. The final independent checks pass all captured bytes. No new unhandled native game failure or crash report occurred; persistent injected failures are caught by the fixture as expected controls.

| Scoped observation | Development Vulkan | Packaged Vulkan | OpenGL fallback |
| --- | ---: | ---: | ---: |
| Maximum private recovery call, ms | 0.875326 | 1.636311 | 1.284562 |
| Maximum normal boundary call, ms | 2.069334 | 1.324601 | 1.107355 |
| Maximum private retirement call, ms | 10.224964 | 9.923411 | 1.695910 |

These single-run observations do not establish a latency improvement, API ranking, owner-GPU/FPS or hard 2 ms acceptance. Mandatory recovery adds work alongside retirement, and normal publication remains synchronous. Extra recovery storage includes up to 1 MiB of vertex data, at most 7,281 mask bytes, up to 65,520 transient patch bytes, bounded queue objects and one generation long per page. Object headers and total process/GPU peak memory are not accepted by these raw bounds. Existing index and CPU-mirror limits remain.

The injected failures occur before backend writes, including one consumed argument-position case. Partial post-write failure and asynchronous device loss have no atomicity guarantee here. Actual capture/full-upload, multipage saturation and active-retirement interleavings with pending recovery, visible-scene travel/edit/cap churn, overflow-to-remesh completion and failure-cohort pixels need broader validation. Static lake screenshots and private pages do not establish sustained stream/ocean continuity or distant LOD coverage.

Next implement bounded normal cohort publication with a cross-frame visibility policy, supersession and edit/unload rules, preserving these recovery gates. Continue real scene coverage, streams/oceans, LOD/coarser hierarchy, regional water budgets/compatible constituents, pressure continuation and geology. Catalog/species remains deferred. Solver substeps are not independent 60 Hz. Prior artifact, marker, reload/atlas and save causes remain open; 73 older missing originals remain missing. No installer is released or market superiority claimed.

## Restore and replay

```
python scripts/restore_snapshot.py EVIDENCE_DIR NEW_ISOLATED_PROJECT
python scripts/replay_checkpoint.py EVIDENCE_DIR NEW_REPLAY_DIR
```

Restoration verifies all 588 candidate source/archive inputs before writing to a nonexistent destination. The staged overlay has 26 files: six production and 20 validation. Since R42, two production and five private validation files change; the other 581 prior inputs are identical. The 587-input control manifest and overlay reconstruct the fresh control, including its corrected private fixture. Replay checks sealed archives and raw packet/pixel data without a new build, game launch or timing measurement. All Gradle/native jobs are sequential using the retained JDK25/JDK21/Gradle9.2.1, Mesa25.2.8 software driver and Xvfb1.9 pins.

Offline replay passes after lossless snapshot-level compaction: 1,092 physical capture/build/historical-admission archive members, 15,740 logical capture paths, 588 candidate inputs and 587 reconstructed control inputs are verified. Current and required historical boundary readbacks share content-addressed CPU/GPU snapshots; every original complete-file SHA-256 is reconstructed. The final archive shrinks from 82,111,663 to 29,827,493 bytes. `capture-compaction.json` records the earlier whole-file deduplication; `readback-compaction.json` and `capture-bundle.json` describe the final layout. Pre-compaction manifests are retained compressed. Replaying performs no new build, native launch or timing measurement.

A transient execution-transport disconnect recovered the same isolated workspace. Source and raw/sealed evidence hashes were rechecked, then both the original and compacted archive replays passed. No validation is inferred from activity during the disconnect. Finalization checks find no live Java or Xvfb jobs. The work-state graphics/restoration aliases are refreshed to this 588-input/26-file checkpoint; older evidence and blockers are retained.
