# Dev94: V8-r6 vegetation planned heightmap parity

Source-only checkpoint `0.0.1-alpha.29-dev.94-vegetation-height-parity.1`, parent `b84e3048acf23b40aa48ba1ca129e759cb1d680a`. Consumed latest independent Dev93 audit `docs/engine-hourly-audits/2026-10-30T0230-c57d2924/REVIEW.md` pinned at `c57d2924562a7dd35eed007a164205953bb13061`. The independent 50/100 (5/10) is a **carried October 24 baseline, not a fresh regrade**.

## Production code

Existing `V8VegetationColumnPlan` now exposes `plannedWorldSurfaceExclusive`: a saved V8-r6 plant changes planned top-exclusive height by one iff it occupies the existing column's top air cell and fits the saved vertical envelope. This is deterministic, independent of chunk order and does not touch the world, water, profile, geometry or another generation revision.

Explicit-only native adapter `DensityChunkMaterializer.baseHeightVegetated` distinguishes `WORLD_SURFACE_WG` (planned short grass/fern included) from `OCEAN_FLOOR_WG` (excluded). Unsupported heightmap types fail closed. Its `baseColumnVegetated` projection now also checks the same exact raw top. Earlier V8 versions and V1–V7 paths are unchanged. This is **prewrite planning**, not proof native callbacks accepted plants: after native writes, use actual heightmaps and `V8VegetationNativeReceipt`. V8 is not enabled as the default generator.

## Validation

Isolated Java21 component tests: OpenJDK 21.0.12.1, strict `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` API doubles. Normal and Python `-OO` modes, two positive JVM processes per runner, **163,503 assertions per process / 654,012 total**, and **12/12 compiled broken controls rejected**, including new height errors. The tested production planner/test/runner Git blob identities are `62e6f3f2d38aa8db62b9d6a4baeb37f61112708a`, `13659582efdb9177d703ada0f3890f2671c2a77f`, `05567abb13ee3d50806ef6e58e1bdacf7b10bdda`. The updated runner retains the existing current-main strict failure classification. Reproduce in a complete checkout with `python tools/test_v8_vegetation_columns.py` and `python -OO tools/test_v8_vegetation_columns.py`.

Not executed: complete pinned root work-state validator, the modified Mojang/NeoForge materializer full compile, real VM V5 `CoreChecks`, Java25/Gradle9.2.1, Minecraft native save/reopen/edits/callbacks, signed old/new chunk borders, Vulkan/OpenGL acceptance, full water conservation or owner-hardware measurements. No qualified playable JAR is claimed or published; owner handles full builds.

## Next concrete gates

Run the mandatory production V5 graph VM check and native V8-r6 heightmap/vegetation-receipt comparison, signed chunk seams, save/reopen and callback edits under Java25. Continue durable outlet-ledger binding to real finite water and certified ocean witnesses in non-overlapping engine scope. The engine remains incomplete at historical 5.0/10 until a fresh independent grade strictly above 8.5/10 and all water gates pass.
