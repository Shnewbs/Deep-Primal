# Expanded replica — docs/validation/engine-continuation/water-shared-budget-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-shared-budget-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-shared-budget-2026-10-23/README.md |
| Preserved source SHA-256 | `0fb8686e987077e79d0f7384cfe5691011029c771b80d5374720deee872c2380` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Sharing the extraction-entry deadline with mandatory water retirement so optional future polling cannot start a fresh 2 ms window after retirement delays. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that mandatory synchronous work can still overrun the interval; admission accounting does not certify a hard frame-time bound. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add one presentation-work budget with separate mandatory progress and optional admission, plus once-per-rendered-frame retirement ownership. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Inject 8, 12 and 20 ms retirement delays, require optional work to defer and verify frame-based retirement progress, then measure real scatter and churn tails independently. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Shared extraction budget and rendered-frame water retirement — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Shared extraction budget and rendered-frame water retirement — 2026-10-23

Continuation of R37 `f5e840c3f4475751003ae41b490ee0a3be0d0e4e`, on `work/engine-after-alpha24`. Staged candidate only; no main/public-source promotion or installer release. Vulkan is the reference API, OpenGL remains a tested fallback. Acceptance is incomplete.

## Actual implementation

The staged `AnimatedWaterRenderer.extract` retired expired presentation packets, then started a fresh optional 2 ms deadline. Retirement time was therefore excluded from the shared admission budget. A native reproduction called the real extraction entry point with controlled 8/12/20 ms synchronous retirement delays and four pending futures: the unchanged renderer polled all four futures after each delay. The fix derives the deadline from the existing extraction-entry timestamp. Mandatory retirement and subsequent visibility/unload maintenance remain in place; optional upload/discovery/capture admission receives only the remaining budget.

This is one production-file change plus three validation-file changes since R37. The complete candidate contains 19 staged files: five production and fourteen validation files. `since-r37.patch` and `source-scope-audit.json` identify the exact change. No solver, geography, chemistry, resource shader or modular API changes were introduced in this checkpoint. Published V1/V2/V3 and player edits remain unchanged. The patch is not a hard 2 ms total extraction bound: an individual call and mandatory service can exceed that duration.

The native deadline fixture is identical before/after (compiled class SHA-256 `8e73f68f23d9106f3d7910662626f2e582460037a1368302d288eeaf06e151d7`). All nine final delayed cases (development Vulkan, packaged Vulkan, GL) polled zero optional futures after the over-budget retirement, preserved one real retirement write each and matched all GPU readbacks to an independently encoded packet. The controlled delays are a functional admission regression, not ordinary latency measurements.

## Fresh checks

* Two successful full builds, 25 executed tasks each, including 210,000 retained packet/core cases. The final build includes the fix. Initial build failed before mod compilation because a generated NeoForm decompile JAR was not a ZIP. Only that generated cache node was quarantined; retry passed. Its exact 3,407,868 bytes and hash, cache record, console and independent ZIP rejection are preserved. Cause remains unproven.
* Final three native regression sessions: 15,336 pixel comparisons / 30,672 draws; zero channels beyond tolerance one, maximum channel error one, no uncovered pixels. Actual-upload CPU/class/shader identities and packaged class origins pass. Rejection/manual retry: 48 cases, 192 paired CPU/GPU reads, zero differing bytes; intended retry targets also match the explicitly historical R36 baseline.
* Real-frame fixture: private non-drawn 2,048 GPU pages / 33,554,304 bytes; one retirement call per distinct rendered frame after a setup fence and five-frame settle. Every active run completes 129 frames, 2,175 writes / 33,554,304 bytes, at most 18 writes and 262,080 bytes per frame. All 2,048 CPU/GPU hashes match an independent packet codec; no predicted Vulkan staging overflow under this schedule. Steady controls have zero writes. The old burst overflow remains historical diagnostic evidence; this experiment does not disprove it.
* Eight fresh native sessions including world creation, initial ordinary-frame controls, deadline reproduction, three final combined regressions and final steady control. Seven have explicit saved-disconnect passes; creation instead has explicit WORLD_CREATION_JOIN_PASS/WORLD_CREATION_PASS and saved-player/world/dimension records. All have normal shutdown, stable sealed consoles and zero new crash reports. Vulkan sessions report zero validation errors and no remaining tracked buffers/images; deliberately injected shutdown-completion rejection is followed by an explicit continuation pass. GL fallback/reload/page-order controls pass. A successful Gradle exit alone was never the acceptance gate.
* Final package audit: six archive CRC checks, 411 compiled class identities, five identical nested module artifacts, no private compatibility agent or validation fixture shipped. No installer was released.

| Final session | Retirement CPU p95 / max (ms) | Render Pre→Post CPU p95 / max (ms) | Pre→next Pre interval p95 / max (ms) |
|---|---:|---:|---:|
| Development Vulkan, active | 1.221446 / 11.126275 | 5.740560 / 18.916495 | 57.267424 / 78.512694 |
| Packaged Vulkan, active | 1.200735 / 3.763378 | 4.841794 / 16.749797 | 56.447833 / 83.264327 |
| GL fallback, active | 1.086613 / 2.267559 | 35.436873 / 41.742980 | 50.768169 / 66.595541 |
| Development Vulkan, steady | 0.374763 / 0.894044 | 4.602890 / 14.456455 | 60.539412 / 71.178978 |

These are instrumented host/software-driver observations. Admitted scene-water key sets differ between runs; LOD cached/visible tiles are zero. The private pages bypass admission/eviction and are not drawn. Heap/direct-pool observations omit other native allocations; frame intervals include waits/limiting. The controlled 250 ms clock advance per frame is not independent 60 Hz simulation. No matched API speedup, complete scene continuity, residency/churn, owner-GPU/FPS or performance promotion follows. Screenshot inspection confirms the lake scene renders but leaves the distant cutoff unresolved; still images cannot establish temporal flicker acceptance.

## Restore and replay

The complete, compact source snapshot replaces repeated per-file reconstruction. It contains exactly 581 build inputs, with original public blob provenance and nineteen staged replacements/additions. All archive chunks, member paths, sizes and SHA-256/Git hashes are verified before any write; the destination must not exist. No build outputs, caches, credentials, world saves or installers are part of this source snapshot.

```
python scripts/restore_snapshot.py . /absolute/new/isolated/project
python scripts/replay_checkpoint.py . /absolute/new/isolated/replay
```

All binary archive parts are at most 512 KiB. `capture-bundles.json.gz` enumerates every retained capture file and hash. `offline-audit-replay.json` records an actual independent replay of all archived members, all four native-data oracles, final fixture byte identities, both successful build records, failed ZIP reproduction and restoration of all 581 source inputs. Replay is not new native execution. The 23,700-byte R36 rejection baseline archive is explicitly historical (original Git blob `607dd7b2a7fbf71546b1eb77d226c0d0982c9b37`); the new deadline baseline is fresh. The initial ordinary-frame fixture preceded addition of two window/FPS metadata fields: original class hashes remain in those captures, reconstructed source is retained, and original compiled bytes are not claimed to survive.

`scripts/` retains actual build, launcher, independent oracles and toolchain restore scripts. Original pinned Mesa 25.2.8 remains unchanged. The obsolete Ubuntu Xvfb 1.6 download failed (snapshot HTTP 403/canonical HTTP 404); the restored Xvfb/xserver-common 1.9 pair was verified against current official Ubuntu package metadata, with changed pins and hashes recorded. Nine other graphics packages retain their original pins. No comparison with the old Xvfb environment is claimed. JDKs and Gradle were restored from the recorded hashes, and the private compatibility agent passed its checks and is excluded from shipped artifacts. The seventy-three still-missing older original evidence files remain missing; current validation does not recreate them.

## Self-audit and next implementation

The focused fix preserves mandatory expiry service and accepted GPU packet bytes. The fixture restores backend and visible render state in finally blocks, resets private pages/workers and observes explicit GPU completion/readback before accepting output. Full total-frame/resident-memory acceptance, matched scene workload, admission/eviction cap churn, scattered boundary publication costs, automatic retry/cohort atomicity, async device loss, arbitrary alpha/resource packs, real long-stream/ocean continuity and the distant LOD cutoff remain open. Next: profile matched scene workloads with real cap admission/eviction and boundary scatter, reproduce a remaining tail or starvation defect, then make another focused correction. Do not bypass the frozen-geography, water budget/chemistry, pressure continuation and geology requirement register. Catalog/species remain separately deferred.


<!-- END PRESERVED SOURCE -->
