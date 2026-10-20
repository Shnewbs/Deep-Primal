# Alpha 15 — horizon composition and river surfaces

Minecraft 26.1.2; NeoForge 26.1.2.122; Java 25. Installer `DeepPrimal-26.1.2-0.0.1-alpha.15.jar`, 22,637,617 bytes, SHA-256 `9109e7d79b675dd0fc0b208fd31e8fb3c6808bb7fea3944e19e5d991d80889bd`.

## Changes

The procedural horizon now renders after sky and before native opaque chunks, using an isolated depth attachment. Approximate macro heights cannot write Minecraft's terrain depth. The reusable depth texture resizes with the viewport and closes on disable/logout/shutdown. Diagnostics expose its byte size separately from the tile mesh budget. The product does not copy depth every frame.

New serialized `waterways_v5` terrain uses shallower channel beds and quantized partial native water at the surface. Partial water retains the persistent downstream direction and speed. Cached exact per-column cuts prevent the four-block density interpolation grid from bridging a narrow/shallow stream; no new per-voxel noise or routing is added. A thin solid bed remains protected from native carvers. This is static generation plus normal native fluid behavior, not a conserved-volume solver.

Spring reaches taper before joining downstream channels. Catchments of at least sixteen routing cells can create bank-contained 14–24-block-radius headwater lakes; unsafe sites remain tapered springs. `naturalRiverSteps` defaults true and remains independent of `deepOceanTrenches`. Four new profile combinations cover trench/plain and standard/extra depth. All 52 alpha 14 worldgen data/pack resources remain byte-identical. Existing worlds retain their old generator; no retrogen.

The [reference audit](WATER-REFERENCE-AUDIT-2026-10-20.md) describes every linked project's implemented, partial and missing features. Alpha 15 does not contain all of their feature suites.

## Verification

- Core differential/cache/module-boundary checks and fixture compilation pass. Natural-stream checks include 137 spring sources, seven contained lakes, 36,473 partial-surface samples and integer source-height preservation.
- Standard native world, seed 20261018: `minecraft:normal` resolves to `deepprimal:waterways_v5`, floor -128. Nine headwater lakes in the routing survey; the first at X=6277/Z=-250 has radius 14 and survives full generation. Mountain/gorge, twelve downstream reaches, support, coast, current speed, native partial-fluid current, weather, pressure and 125-byte-wire checks pass.
- Extra-depth native world, seed 452343: normal resolves to `deepprimal:waterways_v5_deep`, floor -384. Twelve surveyed headwater lakes; the first X=-3515/Z=-1478 has radius 14 and survives full generation. Equivalent native checks pass.
- Standard trench bed -88, depth 183; extra-depth trench bed -344, depth 439. Solid-bed checks pass.
- A separate alpha 14 serialized v4 world still passed its legacy terrain/current/weather checks with the alpha 15 product, confirming that old profile decoding remains available.
- Installer audit: four nested modules; 301 own project classes; no duplicate project classes, development fixtures or Minecraft classes.

The original source-water fixture checked Y=floor(waterline)-1. New partial surfaces can occupy the next block, so that obsolete expectation reported gravel as a dry river while water existed immediately above it. The corrected test checks the native quantized surface and retains all support/full-generation assertions. A failed early candidate was not released.

## Limits

OpenGL/Vulkan validation runs use Mesa software rendering. They are not AMD 8060S/Windows FPS benchmarks. Full-detail distance requests remain capped at 125 with memory/server guards; the separate 512-chunk horizon remains procedural macro terrain. This update does not certify those maxima under full load, reproduce distant player edits/trees, or implement an external shader/LOD integration.
