# Expanded replica — docs/validation/engine-continuation/water-publication-commit-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-publication-commit-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-publication-commit-2026-10-23/README.md |
| Preserved source SHA-256 | `49f81c70bc2f422cfebcac34e6ad6e8be9860b3cc1f551b4a325ee6669dbbda5` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Committing CPU water mirror bytes and retirement cursors only after a synchronous GPU range write accepts separately prepared transfer data. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that pre-backend rejection injection establishes this commit ordering but automatic retry and asynchronous device-loss atomicity remain separate gates. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a publication transaction with candidate bytes, submission outcome and accepted mirror state, avoiding false success that suppresses later repair. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Reject first and second submissions across neighbor and retirement paths, require CPU state to remain at the accepted version and verify an explicit retry repairs matching GPU bytes. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Water publication commit — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water publication commit — 2026-10-23

The staged boundary-packet renderer committed CPU mirror bytes before a synchronous GPU range write could reject them. A subsequent explicit retry could see the target already present in the mirror and skip repairing the GPU. Sixteen actual Vulkan rejection routes reproduced 832 differing CPU/GPU bytes, including neighbor quad writes and bounded retirement, near/far coordinates, X/Z boundaries and rejection at first/second submission.

The focused fix constructs transfer bytes separately, submits them, then commits the accepted CPU mirror and cleanup deadline/cursor. Retirement adds a read-only expiry predicate so steady slices avoid copying/submission. Native failure injection is validation-only and throws before the selected backend write. The production code still propagates exceptions; automatic caller retries, asynchronous device loss and atomic publication of a whole neighbor cohort are not established by this patch.

Compared with checkpoint `91c8b9488fd617c6cc9705cce490b503fc3c6058`, two production files, two existing validation files and two new fixtures changed. All 17 staged source files are provided for complete restoration onto the independently verified 575-file public source base. Public production/test sources, main and supplied installers were not changed. V1/V2/V3 geography, water quantity, player edits, Vulkan priority and modular fallback remain intact.

Fresh development Vulkan, packaged Vulkan and OpenGL fallback runs pass 48 rejection/retry cases with 192 CPU/GPU reads and zero differing bytes. Each repaired retry matches the reproduced baseline's intended target bytes. The three final runs also pass 15,336 controlled pixel comparisons/30,672 draws, with maximum one-channel error of one and no uncovered pixels. Source/class/shader identity, explicit passing records, sealed console hashes, action completion, normal save/shutdown, no new crashes and released Vulkan resources are checked independently. A fresh full 25-task build and 210,000 packet/core cases pass. The rebuilt package passes six archive CRC checks, 411 class identities and five nested-module identities.

Each final session exercises eight private actual GPU pages holding 33,554,304 bytes, retires them across 129 explicit diagnostic turns and 136 writes, and verifies all GPU readback hashes against CPU data. Each write is at most 262,080 bytes. This is synthetic residency, not normal-frame 2,048-page churn. CPU-enqueue p95 values are 0.750/1.182/1.368 ms, with maxima 46.477/35.978/3.778 ms for development Vulkan/packaged Vulkan/GL. These include scanning/copy/submission but exclude GPU completion and normal-frame work. Performance acceptance remains **false**; the outlier cause is unproven.

A truncated generated outer JAR was caught before the first candidate launch. Its prelaunch metadata and stable invalid size/hash are retained; no runtime pass is attributed to that rejected attempt. A serial package rebuild passed before packaged tests. The archive mutation/truncation root cause remains open; no speculative packaging workaround was shipped.

The build/native environment was restored with verified pinned tools and software Vulkan. Thirty previously missing R35 wrappers were recovered only where their original Git blob hashes matched exactly; 73 originally missing artifacts remain unavailable. Three retained R35 raw sessions were independently replayed as historical evidence, with their missing action histories explicitly excluded. One additional old 5.28 MiB PNG archive was not served by the contents API. New capture bundles are split into parts at most 512 KiB, with full/member/part hashes, to allow future API recovery.

Run `python scripts/replay_archived.py` to reassemble/hash-check the complete archived captures and independently replay pixel, packet and publication oracles. This replays native output; it does not run Minecraft again. `python scripts/restore_staged.py <isolated-public-source>` checks every public input and staged source hash before writing any source. The recorded restoration and offline replay pass. Build/native scripts retain exact isolated paths and require adapting those paths and restoring the pinned environment before a new run; they are evidence rather than portable installers.

Next: measure and reduce worst-case CPU submission outliers and boundary scatter, then test normal-frame 32 MiB/2,048-page churn, edits/height/opacity and explicit retry policy. Long streams/ocean, side spatial seams, chemistry/budgets, arbitrary packs, pressure continuation, geology, distant LOD coverage and owner GPU/FPS remain unfinished. Solver substeps do not establish an independent 60 Hz simulation. This checkpoint is not engine completion or a market-superiority claim.


<!-- END PRESERVED SOURCE -->
