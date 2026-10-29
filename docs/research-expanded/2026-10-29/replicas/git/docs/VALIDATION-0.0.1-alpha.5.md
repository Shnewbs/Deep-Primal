# Expanded replica — docs/VALIDATION-0.0.1-alpha.5.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.5.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.5.md |
| Preserved source SHA-256 | `b96a8f6fa20d2c4cf0ec1ffb2d7ea2c3cad233c362c03e546fe636aaed10e25e` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Validation — 0.0.1-alpha.5”. Its original opening states: “Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Fix for the alpha 4 extended-render-distance scheduling crash. The full survival conversion and Vulkan backend remain unimplemented.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `ChunkTaskPriorityQueue`, `ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT`, `ChunkLevel.MAX_LEVEL`.

## Research expansion — gaps and details to deepen

- Treat restoration, test harnesses, and output paths as independently validated infrastructure so they cannot manufacture product success or failure.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define terminal process status, complete output set, bounded protocol/deadline behavior, and unconditional failure checks.
- Record environment repairs separately from product changes; retain original failure logs and exact fixture/tool revisions.

## Research expansion — acceptance checks

- Run the harness against deliberately failing, truncated, stale, timed-out, and success-marker-only producers.
- Verify wrong request IDs, malformed frames, dangling output paths, missing required inputs, and cleanup after cancellation.

## Research expansion — source anchors

- Validation — 0.0.1-alpha.5
- Compatibility and limits
- Reproduction

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.5/packaged-live-checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.5/packaged-live-checks.txt) |
| `validation/0.0.1-alpha.5/queue-checks.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.5/queue-checks.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Validation — 0.0.1-alpha.5

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. Fix for the alpha 4 extended-render-distance scheduling crash. The full survival conversion and Vulkan backend remain unimplemented.

## Cause and correction

At render distance 96, the player-ticket distance tracker can submit priority 94 to `ChunkTaskPriorityQueue`. Native queues have 46 buckets, producing `ArrayIndexOutOfBoundsException: Index 94 out of bounds for length 46`. This was reproduced in the native transformed queue without other mods. The supplied crash report shows this bounds failure; it is not the earlier alpha 2 heap-exhaustion failure.

Each queue now captures `max(nativeCount, sessionTrackingCapacity + 3)` once during construction. Allocation, empty sentinel, reprioritization bounds, and work detection all use that same count. The extra buckets include the tracker's distance-plus-two removal level. The count stays fixed even after server shutdown resets the shared runtime capacity, so queued cleanup continues using its original allocation. Submit/pop/release and native ordering remain unchanged.

`ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT` remains 46 and `ChunkLevel.MAX_LEVEL` remains unchanged. Generation, light, and chunk-status levels are not extended. Ordinary sessions at capacity 32 retain their original 46 buckets.

## Native regression checks

Actual transformed Minecraft queues, independent reference model, 5,000 randomized operations per session:

| Session tracking capacity | Allocated queue buckets | Result |
| --- | --- | --- |
| 32 | 46 | Pass |
| 44 | 47 | Pass |
| 45 | 48 | Pass |
| 46 | 49 | Pass |
| 96 | 99 | Pass |

Each session checks every bucket, exact priority/FIFO/task ordering, task preservation, empty state, highest-to-lowest reprioritization, cancellation, release with/without unscheduling, and draining. A retained isolated queue is also exercised after the server-stop event has reset the global runtime capacity to 32. All 25,000 reference-model operations and shutdown checks pass. Baseline reproduction and test markers are retained in [queue-checks.txt](validation/0.0.1-alpha.5/queue-checks.txt).

`./gradlew build` passes the existing core checks, including surface-solver differential cases, nearest-queue comparisons, render-grid coordinates, vertical boundaries and cache/tick checks, plus queue-capacity/removal-sentinel bounds for every supported distance. The validation-only classes are excluded from the distributed JAR.

## Packaged client/server checks

The built alpha 5 JAR is the authoritative mod source, verified in the loader's classpath report. A dedicated server using the default normal preset created a disposable world at the reported crash seed `8000542833694305166`, Y=-128..639, sea 96, then saved and stopped successfully. That save was opened in an actual graphical single-player client.

The client used a 4-GiB maximum heap, render 96, simulation 2, and vertical radius 1 (111,747 render sections) on Mesa llvmpipe in a virtual display. The native player-ticket queue was inspected in the live integrated server and contained 99 buckets. Over 200 server ticks, the check moved the player 64 blocks along both horizontal axes, reduced render distance from 96 to 8, verified the server adopted 8 while its session allocation remained 96, and exited cleanly with all dimensions saved. No queue bounds exception or out-of-memory error occurred. [Live check markers](validation/0.0.1-alpha.5/packaged-live-checks.txt) are retained.

This is a short startup/scheduling/lifecycle check with a small vertical window. It does not wait for a complete 96-chunk scene to generate and stream, and is not a sustained exploration, full-height memory, real-GPU FPS, or third-party-mod compatibility test.

## Compatibility and limits

The 18 packaged worldgen/data/assets resources are byte-identical to alpha 4. No save-format or terrain changes occur in this fix; existing alpha 4 worlds can be reopened. This addresses the scheduling bounds failure, not the separate memory and hardware costs of loading a complete 96-chunk scene. Chunk loading and simulation still operate on full-height columns.

## Reproduction

The optional `visualValidation` source set adds native queue checks to the dedicated-server development run. With `view-distance` set before startup, use `./gradlew runServer -PvisualValidation -PqueueValidation=after -PrunDir=<test-directory>` for each distance. Stop the server to exercise the reset check. The `before` mode is for the unfixed alpha 4 baseline and deliberately expects priority 94 to be rejected.

The opt-in client check uses `runClient -PvisualValidation -PqueueStartupValidation -PpackagedValidation -PvalidationHeap=4G -PcaptureWorld=<disposable-save> -PclientRunDir=<test-directory>`. Preconfigure that test directory to render 96, simulation 2, and vertical radius 1. Its controller acknowledges the experimental-world dialog once for the disposable save, then checks startup, movement, reduction, and shutdown.

`-PpackagedValidation` makes the built JAR the authoritative mod source in client and server runs. Run `build` first. The optional client validation classes are added separately only when requested; they are not distributed in the JAR.

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.5.jar` (80,723 bytes). SHA-256: `3da4fea7ae5532d9e2e8d85402bf0045f3c80e39d2db9bca01e84a61c84c3c53`.


<!-- END PRESERVED SOURCE -->
