# Expanded replica — docs/renderer-jams/20261027-rediscovery-retarget/BACKLOG.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261027-rediscovery-retarget/BACKLOG.md) |
| Source identity | git: docs/renderer-jams/20261027-rediscovery-retarget/BACKLOG.md |
| Preserved source SHA-256 | `027bbee38917f23d59dfd36bfa61b7aca8c8c771f1b9c66e5b9de6bbc7a8a741` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Stages DeliveredChunkScan retarget logic using radius changes, completed-scan movement, and a bounded movement threshold for incomplete scans. Existing 128-visit and dirty-queue limits remain; integration used a source-shaped fixture rather than a complete client checkout. Resolve the later production integration and run its actual native coverage gate.

## Research expansion — gaps and details to deepen

- Repeated retargeting during continuous travel can postpone old regions even with bounded individual turns. The source needs a useful-progress contract that distinguishes overlap reuse, cancelled work, unavailable chunks, and truly starved eligible coverage.

## Research expansion — proposed contracts

- Add oldest eligible age and retarget/reset reasons, preserve progress for overlapping valid coverage where feasible, and link the later runtime-retarget promotion. Do not replace the current renderer wholesale with a stale staged file.

## Research expansion — acceptance checks

- Replay stationary, small-motion, threshold-crossing, radius-change, and continuous-travel paths under saturation. Verify nonloading authority and world-generation isolation, then inspect actual native visible coverage and latency rather than only 108,999 visited coordinates.

## Research expansion — source anchors

- Renderer continuation: rediscovery retarget

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer continuation: rediscovery retarget

Candidate files and isolated checks are staged, not production. Read `README.md` and `test-results.json`; current JAR remains the previously qualified renderer.7 snapshot unless a different role has genuinely released newer bytes.

1. Reconcile `20261027-admission-gate` changes to `VoxelLodRenderer` under a bounded new production claim; validate the retarget patch against fresh main, merge only the intended code, run **full Java25 Gradle/NeoForge modules, core checks and packaging**, and deliver a new qualified JAR tied to exact source.
2. Reproduce actual discovery readiness under camera drift/teleports, changed effective native radius, dirty-queue pressure, reload, world switch and native handoff. Ensure no repeated scanning starvation or screen-side seams; frame/camera/scene/seed and cold/warm measured CPU/GPU p50/p95/p99 remain open.
3. GPU Vulkan-first/OpenGL fallback mesh submission, memory/VRAM, transparency and water ordering/native scene lifecycle, Windows AMD8060S and NVIDIA-specific acceptance.
4. Further bounded upload reuse/worker scheduling and hierarchy/refinement/cache/invalidation. Authoritative unexplored terrain contract and admission budgets, **not** just slider maxima.
5. FSR/DLSS SDK/provider integration belongs to the currently active, separately claimed upscaling worker. Respect its files; no duplicate work or vendor claims.
6. Carry renderer grades [4,6,4,0,4,4], 3.67 mean, 0 delta; external engine grade 5.0 unchanged. Staged source provides **no shipped feature credit**.


<!-- END PRESERVED SOURCE -->
