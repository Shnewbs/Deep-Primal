# Expanded replica — docs/VALIDATION-0.0.1-alpha.18.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.18.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.18.md |
| Preserved source SHA-256 | `da7b0cf9c790180eae2fdcff5c22d9b071ac8c25e50462e2cc9e71e3ae4cc6d9` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal 0.0.1-alpha.18 — audit and world-creation recovery”. Its original opening states: “Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This update preserves the integrated Deep Primal name, module boundaries, registered generator IDs, saved geography and height. It adds no terrain/LOD runtime library. Environmental catalogs remain planning only.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

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

- Deep Primal 0.0.1-alpha.18 — audit and world-creation recovery
- World-creation failure

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.18/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.18/installer-audit.json) |
| `validation/0.0.1-alpha.18/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.18/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal 0.0.1-alpha.18 — audit and world-creation recovery

Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. This update preserves the integrated Deep Primal name, module boundaries, registered generator IDs, saved geography and height. It adds no terrain/LOD runtime library. Environmental catalogs remain planning only.

## World-creation failure

The supplied logs show an unsupported `primalengine:continental` save failing registry decoding, followed by KubeJS data generation failing to find the item registry. Restarting clears the stale context, explaining why the user could subsequently enter a world. Terrain generation itself was not the exception in these logs.

With pinned KubeJS 26.1.2-8.0.7, Rhino 2101.2.8-build.91 and Better Advanced Tooltips 2601.1.0-build.11, an alpha 17 packaged client reproduced the missing-item-registry exception after a dynamic-only KubeJS context. Alpha 18 composes missing built-in registries with the valid dynamic access **before** KubeJS constructs its NBT/JSON/Java serialization contexts. Dynamic registry entries take precedence. The mixin is applied only with KubeJS installed; no KubeJS dependency is bundled.

Native menu checks cover a fresh launch, a deliberately incomplete context, and rejecting a disposable copy of an unsupported experimental save followed by Create New World. They require the actual form, item lookup and item codec, Deep Primal generator selection, joining the new world and an orderly save/disconnect. Unsupported saved generators remain rejected rather than being silently remapped.

Both invisible water blocks now have a model definition. The alpha 17 startup emitted **3,200** missing-variant warnings; the corrected native startup emits **zero** for these blocks. Fluid geometry still comes from the fluid renderer.

## Cleanup and performance

- Terrain location queries reuse coordinate warp/coast noise across all nine candidate provinces, cache province trigonometry and reuse the winning mask. Differential checks compare every terrain sample field and raw height against the published algorithm across 1,800 locations, three seeds, negative coordinates and region-cache eviction. Results must be bit exact. No version-one terrain carving rules changed.
- LOD meshing greedily merges matching coplanar faces. Independent unit-face coverage checks verify exposed geometry, material boundaries, air gaps, valid indices and absence of duplicate faces. Fluid checks cover unequal fractional levels and stacked fluid without internal gaps. A solid test chunk falls from **72,128 to 1,656 GPU bytes** across all three levels, a 97.7% reduction in that fixture. This is not a whole-world memory or FPS measurement.
- Cache eviction sorts candidates once under memory pressure instead of rescanning the entire cache per removed entry. The highest occupied page coordinate is computed once from all mesh parts. Replacement can retire its own older page when both copies cannot fit the selected budget.
- Cache write/prune I/O errors disable persistence for the session while allowing live meshing to continue. Failed, rejected and discarded jobs release unused revision bookkeeping. Disk selection inspects at most 64 entries per attempt.
- After three live captures, an eligible disk reload gets a worker slot even under continuous updates. A coherent live snapshot can finish while a newer edit remains queued; revisions never replace a newer displayed snapshot with an older one, and disk results still lose to edits already observed in the current session. A native regression continuously invalidates a live chunk and requires visible progress, subsequent edit capture, native unload and disk restoration.
- Finite transport reuses cardinal directions and exact cardinal flow codes, and saturates its per-tick counter instead of incrementing beyond the budget.

The warmed 20,000-query raw-height benchmark records the best of five runs for each algorithm, with a common warm-up and output checksum. The final observed times were 78.68 ms before and 16.68 ms after. Exact results are in [the compact evidence](validation/0.0.1-alpha.18/native-evidence.txt). They are component timings on the validation host, not matched hardware game benchmarks or a claim to outperform Sodium/Distant Horizons.

## First continued water work

Newly loaded engine chunks enqueue shared borders for a coalesced wake scan. Dormant finite water is scheduled when it can exchange volume with the newly available neighbor. Work is limited to 256 queued borders per level, 512 scan cells per tick and a cooperative 1 ms scan target. Palette checks skip sections without engine water and unchanged full-water interiors. Reads use already obtained chunk objects; chunk lookups explicitly disallow generation/loading. Missing/unloaded neighbors are skipped. Main-thread chunk callbacks may reenter the queue, so no iterator survives those calls. Level state uses weak keys and is cleared at server stop.

A native sealed basin starts with an unscheduled 16-unit cell written directly into a chunk section. Loading/waking the shared edge schedules it, transport crosses the border, total volume remains 16, and probing an absent far neighbor does not load a chunk. This extends the existing independent fluid; it does not implement recharge, pressure or shoreline surf.

## Native rendering and packaging

The native fixture changes a player-built roof, requires a newer snapshot, unloads its full-detail chunk and checks that the LOD is actually drawn. It checks all six graphics tabs, Apply/Revert/reopen, settings after process restart, and restoration from the persistent LOD cache. The optional Vulkan run requires the real Vulkan backend and enabled validation; OpenGL and Vulkan use the common mesh/GPU path. The final Vulkan run completed with zero validation errors and zero remaining tracked buffers/images. Three nonfatal shader-interface performance warnings were reported. The lean installer is 522,953 bytes (0.50 MiB); the Windows Vulkan alternative is 11,452,595 bytes (10.92 MiB). Recursive checks find no duplicate classes, validation fixtures, rejected standalone classes or bundled Minecraft/NeoForge classes; embedded module metadata matches the contents. Exact hashes and records are linked below.

- [Native/core records](validation/0.0.1-alpha.18/native-evidence.txt)
- [Recursive installer audit](validation/0.0.1-alpha.18/installer-audit.json)

Use `./gradlew build -PvulkanPlatform=windows` for the optional Windows installer and checks, followed by `./gradlew assemble -PwithoutVulkan` for the lean installer. Java 25 is required. Source-boundary, descriptor/cache and core tests are development only. A container-only NeoForm process-command fallback was needed because this validation environment hides its Java process command; no such fallback or toolchain code is included in the mod.

For the native menu regression, use a disposable client directory with the pinned test mods in `mods`:

```sh
./gradlew runClient -PwithoutVulkan -PvisualValidation -PpackagedValidation \
  -PworldCreationValidation -PcreationMode=fresh \
  -PclientRunDir=/absolute/disposable-client -PcaptureDir=/absolute/menu-check
```

`creationMode=context` supplies the incomplete registry case. `creationMode=failed` first requires a **disposable** integrated engine save at `saves/missing-engine`; the fixture deliberately changes its generator IDs. Never point this fixture at a valuable save. `-PvalidationJar=/absolute/alpha17.jar` allows testing the old release with the same fixture. The general native fixture uses the alpha 17 [reproduction commands](VALIDATION-0.0.1-alpha.17.md#reproduction), with `-PlodHotEdits` to require progress under continuous invalidation. Linux Vulkan uses `-PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanRetry -PvulkanValidation`.

## Remaining visual/engine work

The supplied screenshots remain evidence of unfinished terrain quality: repeated river bowls/canals and bank shapes, abrupt LOD page-edge walls, and placeholder trees. Those defects are not fixed by faster sampling or smaller meshes. The next terrain revision needs consistent channel cross-sections, order-independent carving, shared watershed/outlet contracts, unified lakes/spillways and broader fixed-seed native comparisons. Existing generator geography must remain versioned.

LOD still uses explored/delivered chunks, approximate material colors and block-shaped geometry. Neighbor-aware page boundaries, unseen-terrain planning/streaming, resource-pack invalidation, richer materials and water transparency remain open. Breaking coastal surf, physical erosion/deposition, groundwater/recharge, pressure/displacement and full geological placement remain unfinished. Reference methods remain the agreed direction; no proprietary Gaea/World Machine/Nanite engine or third-party terrain generator is embedded. Windows packaging is checked here; the user's Windows/AMD runtime and matched hardware performance are not certified by Linux software-rendering tests.


<!-- END PRESERVED SOURCE -->
