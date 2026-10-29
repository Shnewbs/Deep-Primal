# Expanded replica — docs/validation/engine-continuation/water-boundary-batches-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-boundary-batches-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-boundary-batches-2026-10-23/README.md |
| Preserved source SHA-256 | `330e072b473a4d9e50981ecba181cc2266f5505a9ce70ba8a35976df9c07f824` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

R39 coalescing physically contiguous changed water-boundary quads into bounded GPU ranges while flushing at unchanged gaps and page boundaries. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that fewer submissions in controlled contiguous fixtures do not prove real-scene frame tails, cohort atomicity or device-loss recovery. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a publication batching contract preserving byte-range boundaries, per-turn limits and the independent identities of geometry and material updates. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Compare contiguous and sparse boundary patterns, assert identical corner bytes and bounded ranges on both APIs, and preserve failure-retry and resource accounting while measuring latency separately. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Bounded batches for water-corner publication — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Bounded batches for water-corner publication — 2026-10-23

R39 continues from `159f64617a8e9710f83ccb1182a040199a6b1b95` on `work/engine-after-alpha24`. This is a staged correction and audit, not engine completion or installer release. Vulkan remains the reference API with OpenGL fallback. Main, public production source, water quantity, modular graphics/LOD/Vulkan and frozen V1/V2/V3 geography remain unchanged.

## Reproduction and focused correction

The staged neighbor synchronizer submitted one GPU range for every changed boundary quad, even when adjacent changed quads occupied contiguous bytes. Fresh native controls reproduced 256 and 2,048 submissions for corresponding contiguous boundaries. The focused correction combines physically contiguous changed quads, flushes at unchanged gaps or page boundaries, and splits at the existing 262,080-byte slice limit. It commits the accepted CPU mirror and queued cleanup deadline only after a successful GPU range submission. A rejected range leaves that range's accepted bytes/deadline intact; preceding successful ranges remain committed. Whole-cohort or asynchronous-device-loss atomicity is not claimed.

Exactly three files changed since R38: one production renderer file, validation action registration, and a new native fixture. The complete candidate contains twenty staged files (five production/fifteen validation), with 582 build inputs and provenance for the 575 original public inputs. All 579 other prior inputs are byte-identical. `since-r38.patch`, `source-scope-audit.json` and `staged-manifest.json` define the review scope. No vertex size, uniform, shader, topology, solver, chemical state, water level or geographic change was introduced.

The expanded fixture uses the actual neighbor method and readable GPU buffers at both origin and near-world-limit coordinates, both horizontal axes, contiguous and eight-quad groups, active/queued direction transitions, first/second-range rejection, manual retry and idempotence. Independent Python encoding verifies every CPU/GPU byte, untouched gap, submitted offset/length and accepted cleanup deadline. It verifies that only changed boundary quads are submitted. Baseline and final candidate use an identical compiled expanded fixture, with its exact class and source retained. Earlier twenty-case diagnostics and their original source/class/oracle are preserved separately; they are not relabeled as the expanded fixture.

| Changed quads | Layout | Baseline writes | Candidate writes | Bytes written |
|---:|---|---:|---:|---:|
| 256 | Contiguous | 256 | 1 | 36,864 |
| 256 | Eight-quad groups with gaps | 256 | 32 | 36,864 |
| 2,048 | Contiguous | 2,048 | 2 | 294,912 |
| 2,048 | Eight-quad groups with gaps | 2,048 | 256 | 294,912 |

The large contiguous case splits into 262,080 and 32,832 bytes. The geometry and total changed payload do not increase; unchanged gap quads are never uploaded. Stable repetition submits zero writes. Transient native scratch increases from 144 bytes to at most 262,080 bytes per synchronizer call, with an additional 144-byte heap quad scratch; buffers are freed on success/rejection. Retained page/CPU-mirror limits are unchanged. Broad resident-memory acceptance remains open.

## Fresh validation and limits

* Four full builds pass 25 executed tasks each, including 210,000 retained packet/core cases each. These are repeated retained cases, not newly introduced core cases. No build failed in this checkpoint.
* Seven fresh native sessions: initial baseline, two initial candidate diagnostics, expanded baseline and three final candidates (development Vulkan, packaged Vulkan, GL). All pass explicit saved disconnect, saved players/worlds/dimensions, normal shutdown, stable sealed console and zero new crash gates. Vulkan reports zero validation errors, zero remaining tracked buffers/images and explicit continuation after the deliberately rejected shutdown completion. GL fallback, reload and page-order controls pass. No failed native session is omitted.
* Final expanded candidates: 132 cases / 264 paired CPU/GPU readbacks, zero differing bytes, matching the independent packet/range/deadline oracle and the baseline's intended final packets. The expanded baseline contributes 44 cases / 88 paired reads separately. Rejected writes are intentional successful recovery tests, not failed sessions.
* Final corner/texture regression: 15,336 comparisons / 30,672 draws, maximum channel error one, zero channels beyond one and no uncovered pixels. Existing rejection/manual-retry controls pass 48 cases / 192 paired reads against the explicitly historical R36 intended target. R38 shared-deadline regression passes all nine final cases using its unchanged compiled fixture; its original failing baseline was not rerun here.
* Three rendered-frame retirement controls each verify 2,048 private actual-GPU pages / 33,554,304 bytes across 129 distinct frames, 2,175 writes, at most eighteen writes / 262,080 bytes per frame, no predicted Vulkan staging overflow and independent readback hashes. Development Vulkan retirement p95/max is 1.608468/5.797546 ms, packaged 1.370791/15.202327 ms, GL 0.946683/1.356970 ms. These observations do not establish API or checkpoint performance comparisons.
* Final package checks pass six archive CRCs, 411 compiled-class identities and five identical nested-module artifacts; private compatibility agent and validation fixtures are excluded. No installer is published.

Single-call boundary timings remain scoped observations: expanded development Vulkan's 2,048-quad scattered cases took approximately 6.04–10.43 ms, while contiguous cases took 2.01–4.66 ms. This correction reduces submission counts, but the scan and whole-cohort extraction cost still exceed the 2 ms target in larger controls. There is no hard total extraction-time bound or FPS improvement claim.

All private pages bypass real discovery/admission/eviction and are not drawn. Actual admitted scene-water key sets differ; cached/visible LOD tiles remain zero. Timing instrumentation includes host/waits/limiting, and controlled 250 ms clock increments are not independent 60 Hz simulation. No full-scene, cap-churn, complete temporal stream/ocean, owner-GPU/FPS, general residency or market-superiority acceptance follows. Inspected lake captures render normally and still leave the distant horizon cutoff unresolved; static screenshots do not establish absence of flicker.

## Exact restoration and independent replay

```
python scripts/restore_snapshot.py . /absolute/new/isolated/project
python scripts/replay_checkpoint.py . /absolute/new/isolated/replay
```

The complete source snapshot verifies all archive parts, paths, member sizes and SHA-256/Git identities before writing, and requires a nonexistent destination. All archive parts are at most 512 KiB. Source, staged files, original/candidate variants, launch/build scripts, independent oracles, runtime fixture classes and raw readbacks are retained.

Lossless compaction represents 26,155 capture paths with 968 distinct content objects. `capture-layout.json.gz` maps every original path to its verified object; `capture-bundles.json.gz` enumerates exact archive/member/part hashes. Per-session compressed capture archives were reduced from 39,867,410 to 29,944,089 bytes without omitting any original path. Fresh independent replay verifies 980 physical capture/build archive members, reconstructs all 26,155 capture paths, reruns all native-data oracles, compares both fixture variants/classes and restores all 582 source inputs. Replay is not new native execution. `offline-audit-replay.json` and `capture-compaction.json` record the checks.

The original R36 rejection archive is explicitly historical, Git blob `607dd7b2a7fbf71546b1eb77d226c0d0982c9b37`; it is used only to compare intended retry packet targets. R38 toolchain/driver provenance remains applicable: the same restored JDK/Gradle/Mesa25.2.8/Xvfb1.9 assets were reused, with sequential build/native jobs. No environment or historical measurement was silently promoted. The prior corrupt generated-JAR cause and seventy-three missing older original evidence files remain unresolved; current source/evidence preservation does not recreate them.

## Remaining implementation

Next: real workload-matched admission/eviction cap churn, then reduce or incrementally bound scattered boundary scan/publication costs while preserving shared-corner continuity and retry safety. Retirement tails, automatic retry/cohort atomicity, async device loss, real streams/oceans, complete enabled-view/LOD hierarchy and cutoff, pressure continuation/fanout/unpinned filling, explicit renewable/finite water and compatible-liquid chemistry budgets, and coherent geology remain open. Preserve the requirement register and frozen geography. Catalog/species remain separately deferred. Engine acceptance is incomplete; the checkpoint is a continuation point.


<!-- END PRESERVED SOURCE -->
