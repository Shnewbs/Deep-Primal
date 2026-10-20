# Alpha 14 — water, weather, horizon and Vulkan crash repair

Minecraft **26.1.2**, NeoForge **26.1.2.122**, Java **25**, Gradle **9.2.1**. Based on alpha 13 at `6c516c5c220ebe8efd3ca3bb03c4c2c0d296cb0d`. This update was reconstructed after the unpublished working directory was lost. Only observations from the reconstructed build are claimed below; the historical recovery notes are not certification of this installer.

## Resulting behavior

The cloud storage-buffer guard now treats Vulkan's device range as uint32 instead of a signed Java int. Real device-range rejection stays enabled. Pure regression checks exercise high-bit limits, exact boundaries, oversized ranges and negative sizes.

Full-detail distance requests permit 125 chunks, the last value whose native signed-byte `distance + 2` tracker sentinel stays positive. The separate procedural horizon permits 512 chunks. Heap guards, server-delivery bounds, independently capped simulation and vertical rendering remain active. The horizon uses denser stitched grids, smooth packed normals, native lightmap sampling and smooth water overlap. Visual inspection caught an opaque near-transition wall over high valleys: full-height rendering now omits that wall and preserves the full native near-grid radius, with water blending across its edge. Minimum upload payload is 256 KiB, enough for a complete fine tile; actual vertex/index bytes drive admission and eviction.

Water gains persistent speed bands, blended tint/clarity, new offshore trench profiles, one bounded indexed ocean-wave overlay, gentle boat motion, budgeted particles, optional gameplay policies and bounded cold-weather snow. See [feature scope and reference comparison](WATER-AND-WEATHER-2026-10-20.md) for settings and explicit boundaries. The linked mods' full feature sets are not bundled. All 44 older generation data/preset/pack resources remain byte-identical. Existing saves keep their generator; no explored chunks are regenerated.

Vulkan remains a separate module. Graphics-settings owns the visual controls; engine-core remains platform-neutral. Surface effects reuse the native GPU interface, lightmap, samplers and shared indexed uploader. Logout/shutdown explicitly release custom meshes, pending work and GPU timers. No wave entities, block entities, custom current packets or global weather scans were added.

## Recorded checks

| Check | Reconstructed-build result |
| --- | --- |
| Build/check/module boundaries | Java compilation, core differential checks, cache checks, fixture compilation and the four-module build pass. |
| Native tracker and wire format | Request 125 → sentinel 127; ClientInformation packet round-trip 125. This does not allocate a 125-chunk world. |
| Native standard terrain, seed 20261018 | Mountain/gorge/course and coastal fixtures pass; 28 full-height 16×16 support scans, including repeated final-status scans, find no unsupported current blocks. |
| Native deep terrain, seed 452343 | Equivalent mountain/gorge/course/coast fixtures and 28 support scans pass. |
| Native current speed | Item displacement over the fixture's 40 ticks: slow 5.155694 / fast 11.375 blocks (fast reaches the fixture wall). Boats: slow .528095 / fast 2.741916. Default item 11.375 / boat 1.443114; disabled currents 0 for both. |
| State and buckets | Sixteen directions serialize, remain exactly native water, support ordinary water buckets and retain direction 11/current after standard and deep save/reopen. |
| Native trenches | Standard bed Y=-88, water depth 183; deep bed Y=-344, depth 439. Actual native columns retain a solid bed. |
| Native precipitation | 24-layer and partial 10-layer caps pass. Collision growth lifts a standing entity .625 blocks over six increments. Rain refills exposed existing flow and leaves warm dry air empty. |
| Native water rules | Actual contiguous pressure column 40; air pocket reduces it to 20. Source regeneration policy, optional farmland collision and real ServerPlayer riding a swimming horse pass. |
| OpenGL integration | Native software OpenGL boot, near chunks, horizon upload/draw/memory bounds, staged settings, 35 opaque blended tint samples, negative-coordinate joins and spruce/birch/oak decorator registration pass. |

The isolated snow fixture explicitly makes its entity section visible: no players otherwise keep that remote test chunk tracked. It calls native Block.pushEntitiesUp, not a test-only substitute for collision behavior. Fixture classes and commands are excluded from the distributed installer.

## Limits

The procedural horizon represents generator macro terrain. It does not reproduce every distant tree, structure or player edit. Filling a 512-chunk horizon is progressive under bounded server/client work; raising a slider does not promise immediate terrain or hardware FPS. Tests do not constitute 125-full-detail/512-horizon stress certification.

The runtime here uses Mesa llvmpipe software rendering. The user's AMD 8060S/Windows driver and actual modpack have not been benchmarked. Full finite-volume fluids, water-table depletion, pumps/pipes, industrial fluid adapters, mesh wakes, tides and external Physics Mod/Distant Horizons/Iris integration remain separate work.

## Local build environment

The isolated runner did not expose a Java executable through ProcessHandle.Info.command. Two local NeoForm build-tool defaults were adapted to the already-known `java.home/bin/java`; explicit Java selection remained unchanged. This was a private build-tool compatibility adjustment, not a mod payload change or a relaxation of device validation. Normal environments use the repository's standard Gradle wrapper. Java 21 was also installed locally for ModDev's asset-download task; game/mod compilation used Java 25.
