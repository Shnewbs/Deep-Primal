# Expanded replica — docs/validation/engine-continuation/water-admission-plan-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-admission-plan-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-admission-plan-2026-10-23/README.md |
| Preserved source SHA-256 | `7558669ac9517f1514232acf563665d2eacc615a2046e25fdc2e2ecacae389eb` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R40 planning peak water-mesh reservation before evicting farther pages, including simultaneous old-target and incoming allocations and preserving zero-GPU placeholders. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the guarded native failure is unnecessary coverage eviction on denied admission, while stale stronger-fixture attempts cannot qualify final runtime classes. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a two-phase admission plan that proves sufficient eligible capacity before committing the minimum stable farthest-first eviction prefix. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Deny a large mesh without evicting valid neighbors, verify busy and zero-byte exclusions, and check loaded fixture class hashes before accepting the stronger coverage scenario. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- R40 — plan water admission before evicting coverage

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# R40 — plan water admission before evicting coverage

2026-10-23. Staged on `work/engine-after-alpha24`; parent R39 `d57dbeeba37c18c3c5892b840683fc3052f2ff64`. Engine acceptance is incomplete. Main, public production sources and installers are unchanged.

## Defect and focused change

Production mesh growth greedily released farther pages while seeking room, then rejected the incoming mesh if eligible memory was insufficient. In the guarded native baseline, eight budget denials removed sixteen valid mesher-generated neighboring meshes and eight empty placeholders while preserving the old target. All sixteen baseline cases unnecessarily evicted an empty placeholder.

The independently authored renderer fix first computes the required peak reservation, including the existing target plus incoming allocation. It collects strictly farther, nonbusy pages with GPU allocations. If their combined bytes cannot suffice, it returns without evicting any page. Otherwise a stable farthest-first ordering releases only the sufficient prefix. Zero-GPU placeholders remain available to the separate, unchanged discovery/page-count policy. No GPU cap, water quantity, geometry, shader, saved geography or edit policy changes.

`since-r39.patch` changes one production renderer and two private validation files. The complete candidate has 583 inputs and 21 staged files: 5 production and 16 validation. The other 580 prior source inputs match R39. Temporary reservation list/sort work is bounded by the existing 2,048-page map; no hard CPU/frame-time or measured heap bound follows.

## Fresh checks

- Three full builds passed, each with 25 executed tasks and 210,000 retained core packet cases. A separate forced private validation compile also passed. The two full candidate builds produced the identical complete package SHA256 recorded in `package-audit.json`.
- Guarded baseline: 16 real production upload cases with actual 33,554,304 resident bytes under the 33,554,432 cap, real mesher old/grown targets of 46,080/140,832 bytes, both axes and near/world-limit origins. Far/busy neighbors use real mesher geometry; protected near allocation is patterned native ballast. 112 paired CPU/GPU reads pass. Eight nominal denials lose sixteen valid neighbors before the fix.
- Development Vulkan, packaged Vulkan and OpenGL fallback: 48 candidate cases, 24 nominal denials, zero eviction on denial, zero unnecessary placeholder eviction and 432 matching paired CPU/GPU reads. An independent AABB-distance/budget/victim model checks decisions and exact survivor accounting. Shared before/after/survivor packets match the identical guarded baseline fixture.
- Existing regression gates pass in all three final sessions: 15,336 seam pixel comparisons/30,672 draws, 132 boundary cases/264 paired reads, 48 synchronous rejection/retry cases/192 paired reads, nine extraction-deadline cases and 129 distinct-frame retirement turns per session. Save/disconnect, crash-delta, Vulkan validation/resource/shutdown records are explicit. Static lake captures were inspected; they are not full-view or temporal-flicker acceptance.
- Package audit passes six archive CRC checks, 411 compiled class identities and five nested/standalone identities; private validation and compatibility-agent code are absent. The successful packaged launch uses the audited full-build artifact, excludes root jar/jarJar rebuilding, and verifies the complete package is unchanged before/after execution.
- Archived replay verifies all 583 source inputs before writing a nonexistent isolated destination, 15,828 logical capture paths from 967 distinct content objects, and 983 physical capture/build/rejected-package members. It reruns independent data oracles; it is not additional native execution.

## Rejected evidence and cleanup

Ten native attempts are retained: five accepted fresh native results (initial allocation diagnostic, guarded real-mesher baseline and three final candidate sessions), four stale stronger-fixture attempts and one failed packaged startup. The initial allocation-only diagnostic is not real-mesher neighbor coverage. Four later attempts loaded its older class despite a newer checked source. A forced scoped compile produced the new class, but a later native task restored the old output; even one global no-cache rerun did not establish a cure. Cause remains unproven. Native validation now disables private compile caching/incremental compilation, forces that task, and checks the immediate post-compiler, pre-native and loaded runtime class identities. Only guarded matching records count. Original private stale classes/compile metadata were quarantined and retained; unrelated outputs were not cleaned.

The rejected packaged startup reported `Missing license` because its newly built graphics descriptor was zero bytes, while the source was nonempty. The exact failed outer archive, nested descriptors and console are retained in `rejected-package-bundle.json`; archive replay independently confirms the empty nested descriptor. This is a failed startup with no world/save pass. A fresh complete build restored the identical previously audited package; the subsequent immutable package launch passes. The underlying artifact writer/cause remains unproven; there is no speculative shipping workaround.

All recorded Gradle/native launcher intervals are sequential (`sequential-job-audit.json`). Capture compaction preserves every original logical path/hash and reduces compressed capture bytes from 35,084,078 to 24,835,584. Earlier repository evidence remains untouched; 73 older missing originals remain missing.

## Limits and next implementation

This closes nominal budget-denied coverage destruction in the controlled production upload path. Private pages bypass actual discovery/travel and are not drawn as visible cap-pressure scene meshes. Per-renderer tracked 32MiB residency is not total GPU/process peak memory acceptance: validation ballast and deferred backend frees may temporarily accumulate. A nominally sufficient plan followed by allocation/device failure can still lose victims. Automatic retry policy, whole-cohort GPU atomicity and asynchronous device loss remain open.

Larger scattered boundary scans and retirement outliers still exceed the 2ms target; see exact current records in the strict audits. There is no full-scene, sustained stream/ocean, complete enabled-view/LOD, owner-GPU/FPS or market-comparison acceptance. Solver substeps remain distinct from independent60Hz simulation. Frozen V1/V2/V3 geography and source/quantity semantics are preserved; regional reservoirs, chemistry/compatibility, pressure continuation, geology and catalog/species requirements remain as recorded (catalog/species deferred).

Next: matched visible-scene discovery/admission/eviction under travel, edits and cap churn, followed by bounded scattered boundary publication. Preserve existing local-corner, retry, quantity and explicit save/crash/resource gates; continue the distant LOD/coarser-coverage and long-stream/ocean work.

## Restore and replay

From this evidence directory's parent workspace, use Python with an existing evidence directory and a nonexistent destination:

```
python scripts/restore_snapshot.py EVIDENCE_DIR NEW_ISOLATED_PROJECT
python scripts/replay_checkpoint.py EVIDENCE_DIR NEW_REPLAY_DIR
```

The source bundle is complete; `staged-manifest.json` alone is an overlay, not a complete checkout. Toolchain/software-driver pins remain those recorded by R38/R39; the shared local toolchain is `../water-frame-1732/toolchain`. `source-scope-audit.json`, strict audits, runtime fixture bytes, raw capture/build bundles, rejected attempts and self-audit make this checkpoint reviewable without trusting a successful Gradle exit alone.


<!-- END PRESERVED SOURCE -->
