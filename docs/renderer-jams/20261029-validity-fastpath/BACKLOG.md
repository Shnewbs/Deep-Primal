# Renderer jam 44 next priorities

P0 owner GPU baseline: actual Vulkan/OpenGL, same seed/camera/travel/edit, full-detail/vertical/LOD/simulation settings, water opacity and FSR1 versus native, broadphase fixed/adaptive/disabled, two-back ON/OFF, CPU/GPU p50/p95/p99, GPU RAM/VRAM, draw/descriptor, mesh and upload timings. No JAR/hardware claim from this Java fixture.

P1 measure frequency of AABB identity-uniform pages and late-divergent shared prefixes in real terrain. Jam44 cuts isolated shared-uniform CPU p50 ~75%, invalid uniform ~45%, but mixed/alternating p95/p99 are inconsistent. Revert or conditionalize if device tails or prevalence make it unhelpful.

P1 follow real mesh/streaming/GPU submission and allocation bottlenecks; retain explicit budgets/fences. A loop of indexed draws is not GPU multi-draw indirect.

P2 persist independent full-detail/vertical/LOD limits and authoritative unexplored coverage only after host tracker/network/protocol admission; leave worldgen and simulation owned by their schedules.

P2 temporal FSR/NGX DLSS only with accurate temporal inputs, capability, vendor license, resource lifetime and real RTX validation. FSR1 remains spatial and frame generation separate.
