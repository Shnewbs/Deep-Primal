# Expanded replica — docs/VALIDATION-0.0.1-alpha.22.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.22.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.22.md |
| Preserved source SHA-256 | `104414a06c58809b9696ff2d0836c2e5c939a065d7565eebb28279070df5eb18` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal 0.0.1-alpha.22 — ocean rendering lifecycle repair”. Its original opening states: “The owner's alpha 21 screenshots showed rectangular water layers and apparent underwater gaps. A generated-ocean reproduction produced false water walls while all 74,716 expected server water cells were present and full. This repair changes client presentation; terrain generation, saved water volumes and server transport are unchanged.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks. Named source subjects include `OceanWaterChecks`.

## Research expansion — gaps and details to deepen

- Keep smooth water presentation, server ownership, delivered-neighbor coverage, and GPU publication as separate contracts.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Define one shared-edge identity and presentation time for neighboring patches, including direction-only edits, dry fades, retirement, and rejected uploads.
- Distinguish missing delivered data from known air or dry cells; presentation fallback must not request simulation work or mint authoritative water.

## Research expansion — acceptance checks

- Capture staggered neighbor updates and near/LOD handoff while changing flow, height, opacity, and camera angle.
- Inject upload refusal and memory pressure, then verify old accepted geometry survives until a coherent replacement is published and retired safely.

## Research expansion — source anchors

- Deep Primal 0.0.1-alpha.22 — ocean rendering lifecycle repair
- Limits

Related design: [render performance](../../../chapters/render-performance.md) · [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.22/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.22/installer-audit.json) |
| `validation/0.0.1-alpha.22/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.22/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal 0.0.1-alpha.22 — ocean rendering lifecycle repair

The owner's alpha 21 screenshots showed rectangular water layers and apparent underwater gaps. A generated-ocean reproduction produced false water walls while all 74,716 expected server water cells were present and full. This repair changes client presentation; terrain generation, saved water volumes and server transport are unchanged.

## Cause and repair

`ClientLevel.hasChunk()` returns true unconditionally in this host version. The animated renderer incorrectly used it as evidence that chunk data had been delivered. Missing halo data could therefore become air in a snapshot, exposing false vertical liquid faces. The same predicate prevented departed pages from being released. Whole-chunk arrivals/replacements also needed explicit halo invalidation.

- Read delivered chunks through `ClientChunkCache.getChunk(x, z, false)` and reject missing/empty results. Missing halo columns are sealed for presentation; this lookup never requests a server chunk.
- Capture nine chunk references once per snapshot, replacing 324 column availability lookups and repeated cell-level chunk selection. Workers still receive only immutable primitive snapshots.
- Invalidate affected pages and neighboring halos on client chunk load/replacement/unload. Departed pages release GPU storage and queue entries; completed work for a removed page is rejected by identity.
- Observe direct native section dirtiness, including light updates that bypass `setSectionDirtyWithNeighbors`.
- Apply the same delivered-chunk rule to the existing ocean wave overlay. Eviction clears the released GPU reference so an extracted page cannot draw a closed buffer.

## Verification

`OceanWaterChecks` is an opt-in development fixture, excluded from installers. It compares animated and fallback presentation above and below a 32×32 water volume spanning chunk and vertical-section boundaries, then moves to a generated ocean. Nine captures cover both paths and presentation with LOD and waves enabled. The fixture checks expected server water, retained pages, false faces toward undelivered chunks, resource reload, backend identity and full process shutdown.

The initial alpha 21 generated-ocean captures reproduce the walls. Subsequent repaired captures remove those walls. The test requires resident GPU water, zero undelivered pages and zero faces toward undelivered horizontal neighbors, both before and after resource reload. Compact pass records are in [native evidence](validation/0.0.1-alpha.22/native-evidence.txt); [installer audit](validation/0.0.1-alpha.22/installer-audit.json) records package inventories and hashes. Core checks and module-boundary checks pass.

```sh
./gradlew runClient -PvisualValidation -PfillingValidation -PoceanWaterValidation -PpackagedValidation -PcaptureWorld=<disposable-fixture-world> -PcaptureDir=<output>
```

For Linux Vulkan add `-PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanValidation`. Native graphics checks use Linux Mesa software drivers; the Windows bundle is packaged/audited rather than executed on Windows hardware.

## Limits

This fixes a reproduced client rendering regression, not every possible cause of an empty basin in an existing save. The initial audit measures a test world, not the owner's save. Large-view cache pressure, general translucent sorting, LOD page-edge geometry and breaking surf remain open. No new ocean replenishment, sea-level policy or water-cycle implementation is introduced. The separately adopted [renewable watershed and seasonal water contract](WATER-BUDGET-REQUIREMENTS.md) remains an implementation requirement. Existing worlds can use this presentation repair without regenerating their terrain.


<!-- END PRESERVED SOURCE -->
