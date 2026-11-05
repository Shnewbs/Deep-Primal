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
