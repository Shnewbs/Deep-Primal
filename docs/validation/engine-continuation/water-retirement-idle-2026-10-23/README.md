# R44 — stop polling fully steady water pages

2026-10-23. Staged after `136892bc3b060fb12d75f063d3e92d40a7f45bac` on `work/engine-after-alpha24`. Normal cross-frame cohort publication and engine acceptance remain incomplete. Main/public production source is unchanged.

## Reproduction and focused fix

During the normal-publication audit, mandatory retirement was found to retain every admitted page indefinitely, even after all its material transitions became steady. A fresh R43 product control with 2,048 private readable GPU pages/33,554,304 vertex bytes still retains all 2,048 queue entries after the initial sweep and 16 additional distinct rendered frames. The fixture expects and records that defect; this is not a client crash.

The core packet helper now classifies a slice as live, expired, both, or fully steady in one read-only pass. The renderer removes a page after a complete sweep finds no live pair and no accepted write requiring another sweep. Accepted ordinary boundary writes and deferred retries rearm retirement after GPU/CPU success. A write behind an active cursor sets a conservative next-sweep flag without resetting cursor progress; repeatedly resetting it could starve a large page's tail. A full upload resets and enrolls its new data. Rejected retirement writes commit neither CPU data nor the failing slice's cursor/pending state. Release/reset clear scheduled work.

The existing limits remain: at most 32 page headers/262,080 scanned bytes per turn, with a nominal-cap sweep within 193 frames and at most 250 ms of presentation-clock advancement per frame. One boolean per page is added; vertex layout, shaders and allocated vertex/GPU payload sizes do not grow. A conservative rescan can add one sweep. Initial/live sweeps still cost time. Water quantity, physical solver, player edits, geometry and frozen V1/V2/V3 geography are unchanged.

This is a prerequisite cleanup, not bounded normal cohort publication. Successful normal neighbor publication remains synchronous. Simply splitting those writes across frames does not preserve shared-corner visibility; an explicit visibility/supersession/edit-unload policy is still required.

## Fresh checks

- Two full 25-task builds pass: R43 product control and candidate. The candidate adds 60,000 known-input retirement-state cases covering mixed ages, wrap, offset slices and read-only behavior, while retaining 470,000 recovery cases, 688,129 index cases, both 210,000 packet suites and terrain/watershed/module/geography checks. The quick check repeats the same 60,000 cases, not additional unique coverage.
- Five native sessions pass: fresh steady R43 control, matched steady candidate Vulkan, full development Vulkan, immutable packaged Vulkan and OpenGL fallback. The same enhanced outer/Job fixture class bytes are used. Four candidates each pass 145 distinct-frame calls, including 16 idle turns after the 129-turn nominal-residency sweep. All candidates drain their steady queue to zero. The baseline keeps 2,048 entries. The inferred baseline scan work over those 16 idle turns is 4,193,280 bytes versus zero after candidate queue drain; this is a source/queue model, not an instrumented CPU byte counter.
- Each session checks 2,048 CPU/GPU hashes against independently constructed steady packet bytes. Separate 393,120-byte lifecycle controls test writes behind a partially scanned cursor, normal and retry reactivation, retention of unexpired pairs, expiration, controlled pre-backend publication/retirement rejection and reset cleanup. Final paired raw CPU/GPU bytes match an independent Python packet oracle. Lifecycle steps are synchronous controls; they are not additional distinct-frame/60 Hz simulation evidence.
- The three full candidates retain 120 retry/safety cases/360 paired reads over 240 distinct recovery frames, 768 normal boundary cases/1,536 paired reads, 48 admission cases/432 reads without budget-denied resident loss, 15,336 seam pixel comparisons/30,672 draws, 48 publication-consistency cases/192 reads and nine deadline cases. Normal-boundary, admission and publication regression baselines remain historical; the R44 steady/idle control is fresh.
- Save/disconnect, crash delta zero, Vulkan validationErrors zero, tracked resources zero and shutdown continuation pass explicitly. Seven closed raw/sealed build/native console identities pass. Six archive CRCs, 413 compiled product-class identities and five nested/standalone module identities pass; private validation code is excluded. Package bytes remain unchanged across the packaged launch.

## Self-audit, performance and visual limits

One candidate full-build attempt stopped during core checks when the execution context changed. Its process handle became unavailable; no local Java process or held workspace Gradle lock remained. The incomplete raw log is preserved and is not a full-build pass or a diagnosed compiler failure. A fresh full retry passes. No native game crash was observed. The environment has no audio device and the game disables sound; shader-output-not-consumed performance warnings persist. Zero Vulkan validation errors does not mean a warning-free console.

| Private call observation | R43 steady control | R44 steady candidate | R44 development active | R44 packaged active | R44 OpenGL active |
| --- | ---: | ---: | ---: | ---: | ---: |
| Retirement maximum, ms | 4.523674 | 16.692228 | 4.315359 | 4.810693 | 4.849591 |
| Idle-call median, ms | 0.103956 | 0.016785 | 0.018688 | 0.016795 | 0.016275 |

The same-fixture steady observations show a larger candidate initial-sweep maximum, despite removing subsequent idle polling. The execution context changed between the control and candidate captures; equivalent host conditions were not established, so their timings are not a controlled comparison. The hard 2 ms total-frame gate remains unmet. These single-run private fixture measurements do not establish a general latency improvement, API ranking, process/GPU peak-memory or owner-GPU/FPS guarantee. Added classification still costs CPU while work is active. Private pages bypass actual scene discovery/admission/eviction and are not drawn.

Three lake screenshots were inspected. Lake water, banks and hills render without an error screen, but page-like water bands remain visible and need matched attribution. Different dimensions/load states and still images do not establish API parity, temporal flicker, seamless sustained streams/oceans, mixed transparency or complete enabled-view/distant LOD acceptance. Normal publication, active-retirement outliers, actual cap churn, overflow-to-real-remesh completion, allocation/partial-write/async-loss atomicity and pending-recovery/retirement interleavings remain open.

Continue bounded normal cross-frame cohort publication with a visibility policy, then actual travel/edit/cap-churn/stream coverage and active-work profiling. Continue LOD/coarser coverage, regional budgets/compatible constituents, pressure continuation and geology under the requirement register; catalog/species stays deferred. Solver substeps are not independent 60 Hz. Prior artifact/marker/reload/atlas/save causes remain unresolved; 73 older missing originals remain missing. No market-superiority or engine-completion claim follows.

## Source restore and replay

```
python scripts/restore_snapshot.py EVIDENCE_DIR NEW_ISOLATED_PROJECT
python scripts/replay_checkpoint.py EVIDENCE_DIR NEW_REPLAY_DIR
```

All 589 candidate inputs are verified before writes to a nonexistent destination. There are 27 staged files: six production and 21 validation. Since R43, two production and three validation files change; 584 prior inputs are identical. The 588-input control is reconstructed from the overlay with the same enhanced private fixture. Native readbacks use lossless content-addressed snapshots; each complete logical file retains its own length/SHA-256. Replay performs no new build, game launch or timing measurement. Toolchain pins remain JDK25/JDK21, Gradle9.2.1, Mesa25.2.8 lavapipe and Xvfb1.9.

## Owner-requested preview

An experimental R43 Windows Vulkan JAR was supplied during this work at the owner's request. Windows packaging passes six archive CRCs and all 4,366 compiled classes (including bundled libraries) match the tested R43 Linux archive. This is separate from the 413 current compiled product-class checks above. Windows native GPU execution remains unverified; supplying a preview is not performance/engine promotion. The incomplete R44 build was not supplied.

Offline replay passes: 1093physical archive members, 15757logical capture paths, 589 candidate inputs and 588 reconstructed control inputs are verified. Lossless snapshot deduplication retains all complete-file hashes and compacts 1,133,554,686 logical capture bytes to 32,425,676 archive bytes. No new build/native or timing execution occurs during replay. All native/Gradle jobs are closed.
