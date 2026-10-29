# Expanded replica — docs/renderer-jams/20261027-part-upload-telemetry/NEXT.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-part-upload-telemetry/NEXT.md) |
| Source identity | git: docs/renderer-jams/20261027-part-upload-telemetry/NEXT.md |
| Preserved source SHA-256 | `e6984dd51b5b0c126bd5781ad241fcb90d29e2a3adbed5c961791fb52e0f766d` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds LodGpuUploadProbe observations for accepted-part CPU time and actual payload bytes, separate failure attempts, saturating counters, and reset. It also closes a newly created buffer when later bounds/admission work fails before ownership is appended. Use the new probe for same-scene default versus optional split measurements, including whole-frame tails and water pixels.

## Research expansion — gaps and details to deepen

- Accepted CPU upload calls are not completed GPU transfers. The ownership interval between allocation and pending append should join the shared lifecycle ledger, while the probe needs frame/part identity and a clear denominator for failed or rejected work.

## Research expansion — proposed contracts

- Define allocation, accepted ownership, returned API result, submission, and completion as distinct events. Preserve the primary exception when cleanup fails, record accepted part-size distribution, and correlate part observations with page commit and retired-memory state.

## Research expansion — acceptance checks

- Retain the focused 40,551-assertion accounting/failure coverage and inject faults after buffer creation but before ownership transfer in the actual backend. Verify no orphaned allocation, accurate attempt counts, and native frame/GPU evidence before performance claims.

## Research expansion — source anchors

- Dedicated renderer backlog continuation

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Dedicated renderer backlog continuation

1. Validate `lodGpuPartCpuTiming`, `lodGpuPartSizeTiming`, `lodGpuPartOverCpuBudget`, and `lodGpuPartFailed` at pinned Vulkan + OpenGL scenes, record named hardware and frame/GPU timestamps separately. Measure warm/cold camera, travel, edits, sea/river transparency, native handoff, reload, resize, world reset and budget stress.
2. If measured spikes correlate with indivisible part sizes, add bounded quad-preserving part subdivision and paired timing/VRAM evidence; otherwise prefer worker re-use, fewer allocations and real GPU draw/submission profiling.
3. Audit saturated LOD disk-key discovery and near-native handoff, including no stale edit reuse. Keep world generation, server tracking and unseen-region authority separate.
4. Distinguish requested/effective full detail, vertical, LOD and simulation budgets in persistent UI; do not increase host-tracker limits by slider alone.
5. Preserve shipped FSR1 EASU/RCAS; build temporal inputs, NGX device extension/dispatch/lifetime and RTX acceptance before activating DLSS; frame generation remains a separate feature.

Owner runs complete Gradle/Minecraft/GPU builds until explicitly changing the October 27 source-only handoff. Renderer jam self-audit is not the independent engine auditor.

<!-- END PRESERVED SOURCE -->
