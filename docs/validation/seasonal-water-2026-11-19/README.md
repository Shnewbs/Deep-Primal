# Water simulation and loading in dev.131

Parent source is `ae7849eebd907b44d495ddf1d5a0d42138eecc87`. These tests used Java 25, Minecraft 26.1.2 and NeoForge 26.1.2.122. Saved geography stays at channel revision 12.

## Seasonal water and chunk delivery

In the parent, advancing the same aquifer from a cold column first left 8,192 snow quanta. Starting from a warmer column left 4,096. A second failure dropped the hydrology cursor of a chunk waiting for delivery. Both failures reproduced in a native singleplayer server. Dev.131 uses the mean temperature of the 16 terrain samples already read for each aquifer. A ready chunk can now advance past a pending neighbor, and the neighbor resumes with its original cursor when delivered.

The seasonal fixture drove every 20-tick environmental interval for two 96-day years. It used one 64×64 region, a controlled weather schedule, two exposed rain and snow collection sites, and an actual generated ocean cell. Each ocean exchange delivered measured groundwater and cycled one bucket through the ocean. All world mutations occurred in a disposable copy of a standard seed-20261020 save.

| Native check | Observed result |
| --- | --- |
| Snowmelt visit order | Both visitation orders retained 4,096 quanta at the warmer phase and 8,192 at the coldest phase. |
| Seasonal intervals | 115,200 per year. Both years included 1,536 intervals of simultaneous rain recharge and snow accumulation. Net snow loss occurred in 3,694 and 4,741 intervals respectively. |
| Ocean exchange | 192 exchanges per year. The marked sea surface returned to 16 units and the inlet emptied every time. |
| Account balances | The net change across the selected aquifer, snow, atmosphere, ocean and two native cells was zero for water, salt and sediment in both years. The check compared all three totals after every exchange. |
| Physical chunk unloads | Two cycles produced 64 auxiliary chunk evictions, 32 verified auxiliary reloads and four spring-chunk reloads. Both regions stayed at 32 discharged units within the original interval and reached 64 after advancing. All constituent totals held. |
| Disk restart | The two years ran in separate game processes. All six account snapshots and the aquifer clock, recharge counters, depth and active flag matched on reopening. Winter snow held 4,423,500 quanta at the restart. |

The dedicated-server validation entry accepts `DEEPPRIMAL_ENVIRONMENT_MODE=seasonal-write` and then `seasonal-reopen`. Set `DEEPPRIMAL_ENVIRONMENT_RECORD` to the same receipt path for both starts, use a disposable standard world with seed 20261020, and set `enabled=false` in its hydrology configuration before starting. The fixture temporarily enables hydrology during its own server-thread work. Use the visual-validation source set. The clock is advanced in fixed intervals and weather is controlled. Full-world real-time multiplayer behavior and Windows long-distance frame and loading times need separate measurements.

## Loading and stream allocation

Per-column stream data is created when the first waterway is identified. Source elevations also determine the native outlet height directly. A Java 25 thread-allocation probe created 1,024 dry chunk cursors per batch, warmed up for five batches, and took the median of nine further batches. Measured allocations fell from 8,616 to 2,112 bytes per cursor, including construction overhead in the probe. This measures allocation during hydrology registration. The native stream test also exercised reprioritizing a planned headwater, delivering into a partially empty bed, drought shutdown, recharge and exact debits.

A clean Java 25 Gradle compilation rebuilt all 109 engine-core sources, including WatershedRegion. The full Gradle command `build compileVisualValidationJava -PvulkanPlatform=linux` also passed with the repository core checks enabled. The repository tool runner passed 42 tests with 13 existing skips. The chemistry test covered twelve scoped and external edits. The accounted-write tests passed 2,762 scenarios and rejected all six injected faults, including a silent foreign edit during a refused write.
