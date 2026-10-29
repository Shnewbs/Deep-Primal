# Expanded replica — docs/validation/engine-continuation/water-retirement-profile-2026-10-23/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/engine-continuation/water-retirement-profile-2026-10-23/README.md) |
| Source identity | git: docs/validation/engine-continuation/water-retirement-profile-2026-10-23/README.md |
| Preserved source SHA-256 | `8026b29b7068ed8f5488a2cde9f0948ffaaf71bcd26b71ab8db80777877d0dec` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../../README.md) · [Integrated design](../../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../../chapters/validation-evidence.md) · [Discrepancy register](../../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Validation-only retirement saturation profiling with 2,048 actual GPU pages, staging occupancy and separate CPU, encoder, backend and GC observations. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that diagnostic turns are not rendered frames or representative gameplay latency, and optional Vulkan occupancy probes have no GL equivalent in this fixture. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a timing receipt that identifies invocation cadence, setup completion, nondrawn residency and exactly which backend measurements exist. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Repeat one mandatory retirement invocation per actual frame under matched scenes, charge staging occupancy and total peak memory, and keep prior diagnostic sweep results explicitly scoped. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Water retirement saturation and staging audit — 2026-10-23

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Water retirement saturation and staging audit — 2026-10-23

Continued after publication-fix checkpoint `afa0c20803c72d288434ebc91bec7746398e62e7`. No additional production source changed. Two staged validation files now add per-turn CPU/backend/encoder/GC observations, actual 2,048-page GPU residency, staging occupancy and runtime fixture identity. The fixture cleanup replaces a redundant full-size constructor upload with one seed quad before installing the readable buffer; its private allocation metadata is checked and no production test hook is added. Optional Vulkan occupancy probes are skipped on OpenGL.

Eight sequential native sessions pass explicit result, sealed console, action, save/shutdown and no-new-crash checks. Five exercise 2,048 private actual GPU pages totaling 33,554,304 bytes. Each complete sweep takes129diagnostic turns and2,175range writes, with at most17writes/262,080bytes per turn and16,416bytes per range. All2,048CPU/GPU hashes in each session match an independent Python encoding of steady direction6/timestamp0/opaque packets. The8-page controls perform136writes; their readback hashes also match. Private reflected pages bypass production admission/eviction and are not drawn. No active cap churn, normal-frame scheduling, memory acceptance or full-scene/FPS claim follows.

The duplicate setup uploaded67,108,608bytes into a67,108,864-byte Vulkan staging buffer, leaving256bytes before the first16,416-byte retirement write. The probed native write observed an offset reset to16,416 and took56.727ms. Unchanged `StagingBuffer.copyBuffer` overflows through `submitUploads`, command flush and synchronous `waitFences` before reset. Occupancy/reset follows that source path; backend elapsed time includes other work and potentially concurrent GC, so exclusive fence time and every earlier outlier's cause are not established.

The cleaned fixture's setup uploads33,849,216bytes for2,048pages instead. Repeating all129turns inside one action still fills the64MiB staging buffer: the overflow moves to turn126/write2155. Single-setup maxima were57.156,41.561 and41.398ms across three runs. The final run's p95 was1.843ms, with a40.543ms backend write during that overflow. This is evidence of a burst/staging-pressure cost, not a validated production performance improvement. The final OpenGL8-page control has p952.849ms/max16.534ms; it is not a like-for-like API benchmark. All performance gates remain open.

The final Vulkan/GL runs expose the same runtime fixture class resource SHA256 as the retained compiled class. Earlier diagnostic source variants are retained separately; their runtime fixture class hashes were not captured. Production package CRC/class checks remain unchanged and pass; prior full25-task/core and controlled corner acceptance records remain at the publication checkpoint. This validation-only overlay does not require or claim another full production build, and does not promote public source or an installer. Vulkan remains the primary reference API with the OpenGL fallback preserved.

Restore the publication checkpoint's17staged files first, then run `python scripts/restore_profile.py <isolated-project> <publication-staged-restore-manifest.json>`. It verifies every prior staged hash before applying these two validation files. `python scripts/replay_profile.py` verifies/reassembles the hashed capture archives and independently checks accounting, packet hashes, per-turn bounds and runtime fixture identity. The replay and restoration records pass; replay is not another native execution. Launcher scripts retain exact isolated paths and need adapting before reuse.

Next: drive one retirement turn per distinct rendered frame, after controlled setup completion, recording staging occupancy plus matched total frame/memory costs. Then exercise admission/eviction churn, boundary-scatter submissions, edits/height/opacity, automatic retry policy and full streams/ocean. Keep synchronous overflow latency, other CPU/JIT/GC observations and ordinary frame behavior separate. Existing LOD/geology/water-budget/pressure-continuation requirements remain open. Geography, water quantity and player edits were unchanged.


<!-- END PRESERVED SOURCE -->
