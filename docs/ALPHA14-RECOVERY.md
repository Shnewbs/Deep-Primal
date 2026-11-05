# Historical alpha 14 recovery checkpoint

The full update has since been reconstructed from the checkpoints on this branch. See [current validation](VALIDATION-0.0.1-alpha.14.md) and [feature scope](WATER-AND-WEATHER-2026-10-20.md). The following describes the earlier loss and is retained as a historical reconstruction record, not the current delivery status.

# Original checkpoint notes

The scratch working directory disappeared before the complete alpha 14 update was published. Main remains alpha 13 at 6c516c5c220ebe8efd3ca3bb03c4c2c0d296cb0d. No alpha 14 installer is available from this checkpoint. This branch is an unbuilt recovery patch, not the previously tested full working tree.

## Source fixes preserved here

- Treat Vulkan maxStorageBufferRange as uint32 using Integer.toUnsignedLong. The alpha 13 cloud crash occurred at the signed comparison in VkCommandEncoder.setupUniforms. Retain genuine device-range rejection.
- Full-detail request ceiling 125: the native FixedPlayerDistanceChunkTracker stores a missing-node sentinel of distance + 2 in a signed byte. 125 gives positive 127; 126 gives negative -128. Keep heap protection and server-delivery bounds.
- Separate procedural horizon ceiling 512 with 1200 tile slots, shared bounds in the settings screen/config, server byte/entry cache, and matching validation inputs. Simulation remains independently capped at 32. Existing defaults stay unchanged.
- These patches do not contain the water/weather additions below. Their full implementation must be reconstructed and validated before release.

## Remaining alpha 14 scope

Reconstruct variable river speed, environment-dependent blended water colors/clarity, deeper ocean profiles, bounded surface swells and gentle boat motion, budgeted wakes/spray/flurries, optional source-regeneration policy, rain refill, pressure, farmland-water walking, ridden-horse support, cold-biome capped snow and covered foliage. Restore the denser smooth-shaded horizon, native lighting and water overlap fixes. Vulkan stays a separate module; presentation settings stay in graphics-settings. Do not represent source-regeneration policy as a full finite-volume solver or claim parity with all linked mods.

### Implementation design recovered from the session

- Core WaterDynamics: speed = clamp(.3 + 2.2*sqrt(max(0,slope)+.0025) + .12*sqrt(max(1,catchment))/max(4,width*3), .35, 1.9). River sample adds speed; persistent SPEED property bands .35, 1, 1.45, 1.9, default band 1 for old saves. Cache vectors by 4 speed bands x 16 directions. Keep native Fluids.WATER and buckets.
- Native EntityFluidInteraction$Tracker.applyCurrentTo normalizes non-player flow and loses speed. A narrowly scoped ModifyExpressionValue at its first Vec3.normalize preserves accumulatedCurrent/currentCount only in enabled RiverWaterBlock currents. Do not add another horizontal boat force.
- Water palette uses native colors, cyan cold/high streams and muted tropical brown. Blend four native tint-cache lattice samples every 16 blocks using floorDiv/floorMod for negative coordinates. Preserve opaque ARGB alpha. ClientLevel only pre-registers known ColorResolvers: replace BiomeColors.WATER_COLOR_RESOLVER during its static initializer, with config evaluation deferred until world use. Clear tint caches on Apply; Cancel must preserve values.
- Finite source policy uses CreateFluidSourceEvent, optionally retaining natural river/ocean renewal. It is not finite-volume levelling, pumps or industrial adapters.
- Rain/snow piggyback ServerLevel.tickPrecipitation at return, inspecting one loaded heightmap column. Refill existing non-source, non-falling exposed water only. The highest motion-blocking/fluid surface excludes roofs; a redundant skylight query can lag after newly exposed terrain.
- Snow accumulates only in cold conditions, default cap 24 layers (range 1–64). Promote 8 layers to a snow block. Clamp/remove the extra native layer above a full cap. Use Block.pushEntitiesUp before increasing collision height. Sparse canopy-gap checks inspect at most 12 blocks; no global scans.
- Farm walking is optional, source-water/player-only collision immediately beside farmland. Check EntityCollisionContext and Player BEFORE reading config: native block cache initialization happens before configs load.
- Horses keep riders underwater and gain small buoyancy support. NeoForge FakePlayer refuses all mounting; native tests need an actual ServerPlayer with the fixture's no-network connection.
- Optional pressure reads actual contiguous water above the eyes, capped at 512 blocks once per second. An air pocket stops the count; conduit power, creative and spectator are exempt.
- Core wave field: phase=ticks*.045; (sin(x*.095+z*.031-phase) + .45*sin(z*.17-x*.04-phase*1.41) + .2*sin(x*.29+z*.12-phase*1.9)) * (.10+clamp(storm,0,1)*.15). Tiny ocean boat delta uses the difference between successive ticks, scaled .4.
- A single loaded-ocean GPU overlay uses shared 2-block grid vertices, radius 32 (8–64), re-sampled at most every 10 ticks or on movement. Require 3 blocks of water depth and open air; omit shore/fall cells; retain native water underneath positive crests. Mask RGB before adding a partial alpha. One shared optional particle budget defaults 24/tick; bound wake entity inspections to 128 and yield duplicates to Particular/Wakes.
- Decorate registered leaf tint sources once for LeavesBlock registrations, preserving delegate particle colors and relevant properties. This covers constant spruce/birch palettes as well as biome-tinted oak. Put decorator logic in a concrete client helper, not an anonymous inner class inside a mixin.
- Create new v4 ocean generation resources and OceanHeight density codec rather than editing v1/v2/v3. Offshore mask smoothstep(clamp((-continent-.25)/.35)), trench mask smoothstep(clamp((.19-abs(noise))/.19)); height=max(floor+40, base-offshore*(35+cut*(96-floor-80))). Noise octave -9, amplitudes 1,.5. Standard floor -128, optional deep floor -384, ceiling 639. All 44 older worldgen resources must stay byte-identical. Saved generators and explored chunks are not retrogen.
- Horizon grids 64/32/16 cells per 512-block tile (8/16/32-block spacing). Shared boundary anchors use stride cells/16. Fine/medium thresholds 1024/2560, projected view quality floor .5. Protocol must bump when sample counts change.
- Smooth normals use native POSITION_COLOR_NORMAL, packed signed bytes, 20-byte stride; wave-only vertices stay POSITION_COLOR, 16 bytes. GPU byte estimates must match actual buffers. Minimum upload budget 256 KiB so a fine tile fits. Reuse vertices and existing native samplers.
- Sample horizon water elevations from the seeded WaterwayHeight, not just global sea level, and use native biome colors. Keep bounded server work and no chunk tickets. Preserve memory guards; far sampling is progressive.
- Both custom surfaces need native lightmap sampling. GameRenderer.lightmap() returns a GpuTextureView directly. Bind with RenderPass.bindTexture and RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST); sample texel (0,15) for outdoor block=0/sky=15. This includes day/night/weather/brightness/vision without another texture.
- Native source water ends at level minus 1/9. Macro water should sit slightly below it (the working version used -.125). Skip opaque near-transition closures across flat sea surfaces in full-height mode; otherwise a dark square shows through translucent water. Use a smooth alpha overlap with a standard translucent blend; screen-space dither produced a visibly speckled band without TAA. Keep land alpha 1.

## Earlier observations, not certification of this recovery branch

Before loss, the full working tree built on Java 25 / Gradle 9.2.1 / Minecraft 26.1.2 / NeoForge 26.1.2.122. Twelve standard/deep/legacy/v1/v2/v3 creation/reopen cases passed. Fifty-six complete 16x16 support scans (25 distinct chunks per seed) found no unsupported stored current blocks. Native trenches had beds -88/-344 and water depths 183/439. Sixteen directions and four speed bands, native buckets, restart persistence, air-pocket pressure, farm collision, mounted horses, rain refill, a 24-layer snow cap and .5-block entity lift passed targeted checks.

Software OpenGL/Vulkan sessions exercised tinting, staged controls, shader compilation/cache cleanup, signed-byte storage readback at offset 65792, buffer/texture/depth transfers, atlas mips and stencil. Native lighting was inspected day/night. AMD hardware/FPS was not tested. The final alpha-overlap refinement had built but its last daylight inspection/delivery was interrupted. Never claim the missing installer is released or attach the old alpha 13 binary under an alpha 14 name.

The last temporary archive was named DeepPrimal-26.1.2-0.0.1-alpha.14.jar (22601268 bytes), SHA-256 58aa38d322a145784cf206b6ce6edb4462db552b4ae7231cec0347d0229132b7. The file was not saved to Library or published; the hash is a recovery reference only.

Rebuild from saved source checkpoints, rerun checks, audit the four nested modules/no duplicate or fixture classes, and save the installer durably before announcing delivery. Checkpoint work on a branch throughout reconstruction.
