# Expanded replica — docs/VALIDATION-0.0.1-alpha.20.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/VALIDATION-0.0.1-alpha.20.md) |
| Source identity | git: docs/VALIDATION-0.0.1-alpha.20.md |
| Preserved source SHA-256 | `9f99f12c449afa7b8c61c2f1eb5970b6969a070478c798d224d9bb9fb4b19e98` |
| Review method | structured-evidence |
| Proposed-work priority | P2 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/validation-evidence.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

This preserved record is “Deep Primal 0.0.1-alpha.20 — liquid filling and mesh work”. Its original opening states: “Alpha 20 fixes independent-water activation, transport and rebuild scheduling. It preserves the serialized terrain generators, geography, liquid properties and module boundaries from alpha 19. Loaded water can now settle differently. This is a focused correction to flow stopping; frame-interpolated liquid presentation remains unfinished.”. Those statements retain the source document's date and environment; this expansion has not rerun the reported checks.

## Research expansion — gaps and details to deepen

- Turn the recorded finite-water scheduling behavior into a progress contract under contention, rather than equating an admission clock with a hard execution deadline.
- Editorial addition: connect this record's evidence to a stable finding/claim ID and a dated supersession record; do not reinterpret a historical limitation as a newly reproduced current defect.

## Research expansion — proposed contracts

- Specify resumable cursor ownership, fair service among source/recharge/transport queues, and the durable state retained at budget exhaustion.
- Track admitted work, actual elapsed cost, longest indivisible callback, queue age, and deferred debt separately.

## Research expansion — acceptance checks

- Saturate source work while regular recharge is enabled and verify bounded starvation without creating or losing water.
- Interrupt every cursor boundary, unload neighboring chunks, and resume after reopen; compare visited cells and conservation with an uninterrupted oracle.

## Research expansion — source anchors

- Deep Primal 0.0.1-alpha.20 — liquid filling and mesh work
- Changes
- Native checks

Related design: [terrain water](../../../chapters/terrain-water.md)

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `validation/0.0.1-alpha.20/installer-audit.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.20/installer-audit.json) |
| `validation/0.0.1-alpha.20/native-evidence.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/validation/0.0.1-alpha.20/native-evidence.txt) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal 0.0.1-alpha.20 — liquid filling and mesh work

Alpha 20 fixes independent-water activation, transport and rebuild scheduling. It preserves the serialized terrain generators, geography, liquid properties and module boundaries from alpha 19. Loaded water can now settle differently. This is a focused correction to flow stopping; frame-interpolated liquid presentation remains unfinished.

## Changes

- Native fluid callbacks feed a deduplicated FIFO instead of running transport every second tick. The server drains active work every level tick; unfinished jobs survive the 2,048-cell budget. Each active position gets at most one turn per pass. Overflow retains a one-tick native wake rather than discarding work. Unloaded cells are never requested by the runtime.
- Stable pressure jobs use a separate FIFO and a 4,096-visit budget, so they cannot consume gravity/spreading turns. Lateral exchanges move at most two units per neighbor; pressure exchanges at most four. Integer volume remains authoritative and paired writes retain rollback behavior.
- A bounded horizontal downhill search routes one-unit films toward nearby lower outlets. It visits at most 128 cells per search and 2,048 per tick, retaining unfinished budget-limited work. It does not find arbitrarily distant outlets or supply missing water.
- Empty, replaceable, collision-free vegetation can be displaced and drop its resources. Grass no longer acts as a watertight solid. Collision-shaped blocks and waterlogged solids are outside this change.
- Partial water cells have an internal free surface even beneath a solid ceiling. This prevents the last unit remaining in an upper reservoir while a covered lower cell still has capacity.
- Loaded chunk interiors are scanned top down using palette skips, 256-cell round-robin quanta, a 4,096-cell cap and a 1 ms deadline. Unsupported or uneven generated/saved water is activated without scanning an entire ocean every tick. Missing/unloaded chunks are removed from this work, and reload schedules a fresh scan. Existing border activation remains.
- Graphics finishes an ongoing non-player native chunk snapshot before compiling newer dirtiness. Skipped submissions retain the dirty flag. Player edits still replace pending work immediately; explicit cancellation, section reuse and synchronous completion clear tracked state. Renderer ownership policy controls these mixins. The diagnostic dashboard exposes the coalescing counter.

## Native checks

The disposable server fixture runs 120 actual ticks after placing 2,304 inert jobs ahead of live water. It checks conservation on every tick, monotonically filling lower cells, a one-unit film corridor/outlet, plant wash-through, a dormant directly written section cell and an absent far chunk. The enclosed two-cell basin fills all 32 units over multiple updates; measured progress gaps are one tick. The fixture retains its test chunks by observing their cells, rather than treating unloaded chunks as simulated.

The existing 144-unit U-bend fixture checks conservative pressure transfer, sealed separation and absent-chunk behavior with the new bounded transfer rate.

Client validation deliberately changes a water patch on every rendered frame, independently of the server transport fixture. It checks pending-task coalescing and retained dirtiness with a controlled pending task, confirms player edits bypass that task, counts actual native mesh replacements during continuous changes, and waits for the latest mesh after changes stop. This is evidence of mesh progress and state correctness; it is not a measurement of perceived smoothness or comparative FPS. OpenGL and Vulkan results are recorded in [native-evidence.txt](validation/0.0.1-alpha.20/native-evidence.txt).

The controlled coalescing case passes. In the small patch stress scene, workers normally finish before the next submission, so its additional coalescing count is zero; no performance gain is inferred from that scene. Vulkan reports zero validation errors and releases all tracked buffers/images. Three existing unused shader-output warnings remain.

An earlier run against the reused alpha-19 fixture reported one misplaced-chunk read. A subsequent on-disk audit found all 1,747 stored positions consistent. A fresh region store, two Vulkan reopen passes and OpenGL checks do not reproduce the read error; all 1,118 stored chunk positions match. Its cause is not established, and this update does not claim to fix it.

Development fixtures are excluded from release JARs. Routes:

```sh
DEEPPRIMAL_FILL_CHECKS=true DEEPPRIMAL_FILL_RECORD=/absolute/result.txt ./gradlew runServer -PvisualValidation -PrunDir=<disposable-flat-world>
DEEPPRIMAL_PRESSURE_CHECKS=true DEEPPRIMAL_PRESSURE_RECORD=/absolute/result.txt ./gradlew runServer -PvisualValidation -PrunDir=<disposable-engine-world>
./gradlew runClient -PvisualValidation -PfillingValidation -PcaptureWorld=<fixture-world> -PcaptureDir=<output>
```

Use `-PgraphicsBackend=Vulkan -PvulkanPlatform=linux -PvulkanValidation` for the Vulkan client route on Linux. Package inventories, sizes and hashes are in [installer-audit.json](validation/0.0.1-alpha.20/installer-audit.json).

## Remaining work

The simulation still advances at server tick frequency with sixteen volume units per block. Near water still uses native asynchronous section meshes. Per-frame height interpolation, dynamic wet/dry edge transitions, region-wide basin accounting/inflow/spillways, displacement, hollow-block storage, erosion, groundwater, lava and breaking coast surf remain unfinished. Bounded local routing/pressure cannot promise every connected space in a large watershed will fill. Terrain carving, floating terrain fragments, placeholder trees and LOD page walls were not changed. No PhysEx or other reference-mod runtime/code was added. Environmental catalogs remain planning-only.


<!-- END PRESERVED SOURCE -->
