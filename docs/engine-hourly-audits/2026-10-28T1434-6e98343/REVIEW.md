# Deep Primal independent audit — 2026-10-28 14:34 UTC

Pinned source [6e98343f3f70266ca8ff6474c0dae08f509685bf](https://github.com/Shnewbs/Deep-Primal/commit/6e98343f3f70266ca8ff6474c0dae08f509685bf); version `0.0.1-alpha.29-dev.56-ecotone-surface.1`. Production source is read-only for this role. October 27 owner override assigns full Gradle/Minecraft/GPU builds to owner; **no JAR was built or published**.

## Independently executed
- Reconstructed exactly pinned `UpscalingPolicy.java` Git blob `8cb56a92010413a2cb9e7e13d6764c99c063f96b`, strict Java21 compilation and **3,035** independent assertions PASS. Previous FSR_31/FSR_41 capability oracle is corrected by renderer: unsupported temporal providers remain unavailable. Native AA menu excludes FSR1.
- Reconstructed exactly pinned `EcotoneSurfaceField.java` blob `a171ac13e36db1f076ba4ac820c3e1d2ce11ec0e`, strict Java21 compile and **594,041** independent assertions PASS: weighted covers, negative/signed extremes, seams, parallel replay, input guards. Uses isolated *test-only EcotoneBlend weights stand-in*, not actual native V8 snapshots/game worldgen.
- Unchanged prior V8 density/materializer source retested using prior standalone API doubles and a prior V8GeneratorSnapshot fixture: **13,173,472** PASS (base height 874,642; ground 1,235,414; callback 16,397; section bridge 11,047,019). Modified current snapshot was not compiled in this test.
- Total focused **13,770,548** passed checks; no claimed native Minecraft/gameplay acceptance.

## Ten-category review — carried baseline, NOT fresh full regrade
| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology/caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Overall Tier 2** | **50/100 = 5.0/10** |

Last accepted full review [rubric](../../engine-review-2026-10-24/REVIEW.md). New V8 patch cover is explicit preview, not native terrain placement; experimental palette is not yet saved/frozen. FSR3/4 are preferences, not active temporal providers. Owner's 300+FPS Vulkan while flying at 125 chunks is useful field feedback, not matched GPU p95/VRAM benchmark. Strict target >8.5 remains unmet.

**Not run:** Java25 unavailable (only Java21 in this isolated host; original archive not mounted), full Gradle/NeoForge, current snapshot native Mojang JSON/NBT codec, new JAR/nested CRC/class identity, packaged native save/reopen/shutdown, Windows/AMD8060S Vulkan/OpenGL, FSR3/4, long water conservation soak, GPU/frame-time performance. No JAR retention changes, release, production change or lease interference.

**Priorities:** saved revisioned V8 palette/climate provenance and actual native surface/heightmap; finite liquid/salt/sediment unload/save/reopen and rollback; Windows Vulkan/OpenGL water/LOD/FSR menu and frame-time measurements. Complete reproducible review and receipt are retained in `/Deep Primal/Independent Audits/DeepPrimal_Audit_20261028T1434_6e98343_*`; evidence ZIP 59,572 bytes SHA256 `f75e7e2e51c873df0008108219cc65930e4d28162565ed4b9a70dbc359c2a48c` CRC PASS (50 entries).
