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
