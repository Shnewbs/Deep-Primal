# Deep Primal 0.0.1-alpha.21 — independent animated water

Alpha 21 addresses visible patch updates in the owner's 2026-10-21 recording. Liquid presentation now has independent meshes and GPU height interpolation. Server authority remains finite, tick based and conservative; no glacier emitter, replenishing source or ocean accounting system is introduced.

## Implementation

- Client-only liquid patches use immutable 18³ primitive snapshots for a 16³ section plus halo. Two workers build geometry without reading a live world. Four in-flight jobs, FIFO queues, a two-capture/two-upload frame cap and a soft 2 ms admission deadline bound work. Changing patches receive priority with background admission retained. A single capture/upload remains an atomic operation and can exceed that soft deadline on slower hardware.
- Water-only air/fluid changes invalidate liquid patches without rebuilding surrounding terrain. Solid/vegetation edits and native lighting changes still update their appropriate owners. Chunk/section changes, optional LOD invalidation and authority remain separate.
- Shared-edge heights interpolate over 100 ms in the GPU vertex shader, with opacity transitions for wet/dry faces. New arrivals into dry sections begin from an empty presentation. Retargeting preserves the currently displayed height; capture revisions prevent an older neighboring job reversing a newer shared edge. Stable meshes animate through per-frame uniforms without per-frame vertex uploads. Simulation pause freezes the animation clock.
- The generic GPU allocation helper and shaders live in graphics; portable meshing/interpolation math lives in core; world capture and lifecycle stay in the root client adapter. External-renderer ownership policy is preserved. The custom Water tab exposes **Smooth water movement**, separately from spatially curved meshing.
- The liquid GPU payload cap is 32 MiB; page and interpolation history counts are bounded. Cache eviction can delay/restrict presentation at extreme views. This is not an unlimited-distance liquid renderer or a replacement for LOD. Pages are drawn back to front; exact sorting against unrelated translucent models is not solved by this patch renderer.
- Configuration changes, logout, resource reload and shutdown release owned geometry. A shutdown latch prevents the last client tick restarting workers after teardown.
- Server drainage rejects cells with no lateral capacity before consuming/retrying search admission. Flat shallow cells without submerged communication do not queue redundant pressure searches. A conservative local L-shaped transfer handles nearby lower capacity immediately rather than leaving a final unit behind unrelated pressure work.

## Evidence

Compact pass records are in [native-evidence.txt](validation/0.0.1-alpha.21/native-evidence.txt); package sizes, hashes and nested inventories are in [installer-audit.json](validation/0.0.1-alpha.21/installer-audit.json).

Core checks cover 10,000 retarget cases, stale capture rejection, 64 shared edge samples, dry-face retention and settled geometry removal. The native 120-tick filling fixture adds 1,024 competing stable film cells ahead of the moving cases. The basin reaches 32 units over multiple consecutive updates with a one-tick maximum progress gap, while the film, plant wash-through and dormant interior cases retain their expected volume. The 144-unit pressure/separation/unloaded-boundary fixture also passes.

Packaged OpenGL and Linux Vulkan clients exercise intermediate height samples, an unchanged terrain section during water-only edits, drying, disabling/re-enabling the renderer, resource reload and full shutdown. Vulkan reports zero validation errors and zero tracked buffers/images after close. Internal captures show an intermediate GPU surface below the final target at the same fixed camera; this checks actual presentation as well as CPU interpolation math. Development fixtures/captures are not shipped.

The diagnostic counter records **terrain invalidation requests bypassed**, not a count of actual mesh builds saved. Source updates can already coalesce in the host. No matched-hardware FPS superiority or whole-world speedup is claimed from these fixtures.

Native route:

```sh
./gradlew runClient -PvisualValidation -PfillingValidation -PanimatedWaterValidation -PpackagedValidation -PcaptureWorld=<fixture-world> -PcaptureDir=<output>
```

Add `-PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanValidation` for the Linux Vulkan test. Windows natives are packaged/audited; these GPU checks run on Linux software drivers, not the owner's AMD Windows driver.

## Scope

The 100 ms presentation delay trades immediacy for smoother visible changes. Physics still uses sixteen units per block, bounded local searches and loaded chunks. Broad fixed-seed visual acceptance, waterfall/front topology, mixed transparency and extreme-view behavior need further refinement. Terrain carving, structural fragments, repeating river basins, LOD page walls, breaking surf, displacement, erosion, groundwater and native machine/container integration remain open.

Unlimited net inflow into a finite closed ocean would accumulate. The required source/catchment/ocean accounting is recorded in [water-budget requirements](WATER-BUDGET-REQUIREMENTS.md), with the fixed-level reservoir described as a proposal rather than an implemented safeguard. The dated reference audits describe alpha 20 and remain historical snapshots. Environmental catalogs and glacial/climate implementation remain deferred.
