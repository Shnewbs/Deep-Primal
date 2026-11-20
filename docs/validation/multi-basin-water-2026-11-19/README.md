# River sources under load in dev.132

Tested on Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25. The comparison point is dev.131 at `e174488103c126ddec3d54935164e5036efc5532`. The saved channel revision is 12.

## Spring delivery and glacier melt

On dev.131 a blocked headwater could send the first four units of groundwater to a low outlet in its chunk, even with a higher open spring just across the chunk edge. Dev.132 compares outlet elevations across all 16 chunks of each aquifer. The highest available spring received all four units. Removing the blockage returned the following interval's first four units to the original headwater. The test checked the aquifer debit against the receiving cell's water, salt and sediment.

| Native server check | Dev.131 | Dev.132 |
| --- | --- | --- |
| First delivery to a loaded lowland during 96 ticks with 8,192 unavailable stream chunks ahead of it | No delivery | Tick 2 on the final run, tick 3 on two earlier runs |
| Median server-thread allocation per hydrology tick in that fixture | 97,784 bytes | 5,696 bytes on the final run |
| A warm, exposed ice block visited after dry ground in the same interval | Missed the melt | Melted one full block. A cold ice block stayed frozen, and the second warm block waited until the next interval. |

The pressure fixture registered one synthetic pending chunk per distant aquifer and one resident outlet in a real server level. Server ticks used the production time budget. The loaded chunk count and the amount of initialized groundwater stayed constant. These allocation figures cover the hydrology tick on this host. Timing depends on when the server thread yields.

Two physical eviction cycles unloaded 64 auxiliary chunks and reloaded the spring chunks four times. An independently resident aquifer retained its existing interval throughout. Two accelerated 96-day years also completed with 384 ocean exchanges and a process restart at the winter boundary. The combined water, salt and sediment balance of the aquifer, snow, atmosphere, ocean and two measured cells matched at every exchange.

Java 25 compiled all 109 engine-core source files from scratch, including `WatershedRegion`. The full Gradle build and visual validation compilation passed. The existing terrain checks found reaches in all six width bands from under four blocks to over 128, with a widest sampled channel of 214.6 blocks. Across 1,029 river transects the check found no flooded outer edges.

## Two players in different river regions

Two native clients joined one integrated server over loopback in a disposable standard world with seed `20261020`. The rivers were at `(822, 172, 1089)` and `(50, 187, 1427)`, about 843 blocks apart. Each client checked for generated water in its local river and watched a sealed measurement cell nine blocks above it. The server filled both cells, waited at least 1,200 game ticks, and recovered both parcels. Each client had to observe the full 16-unit block and its return to air. The players then swapped rivers and repeated the test.

| Run | Visit | Server ticks through both empty-cell receipts | Aquifer advances at each river |
| --- | --- | ---: | --- |
| First pair | Initial positions | 1,203 | 60 and 60 |
| First pair | After swapping rivers | 1,202 | 60 and 60 |
| Fresh pair launched with Gradle | Initial positions | 1,204 | 61 and 60 |
| Fresh pair launched with Gradle | After swapping rivers | 1,203 | 60 and 60 |

The runs lasted 2,406 and 2,408 server ticks from initial placement through the last acknowledgement, including the swap. All eight returned parcels matched their initial water, salt and sediment exactly. Both clients acknowledged completion in each run. The tests used Linux software OpenGL and a three-chunk view distance. Multi-hour sessions across more seeds, natural waterfall flicker on Windows, and frame times at long view distances remain open.

## Reproducing the checks

For the two-player check, use two working copies of this revision on one machine with distinct game directories. Put a disposable seed-`20261020` standard channel-revision-12 save named `river-world` in the host's `run-water-host/saves`. Disable pause on lost focus in the clients' options. In both terminals, set `DEEPPRIMAL_MULTIPLAYER_RECORD` to the same newly created empty directory (absolute path) and `DEEPPRIMAL_MULTIPLAYER_PORT` to the same free loopback port. The following shell example uses port 25575.

In the host checkout, run

```sh
DEEPPRIMAL_MULTIPLAYER_WATER=host ./gradlew :runClient -PvisualValidation \
  -I tools/water/multiplayer.init.gradle -PclientRunDir=run-water-host -PcaptureWorld=river-world
```

Once the `listening` file appears, run this in the guest checkout

```sh
DEEPPRIMAL_MULTIPLAYER_WATER=guest ./gradlew :runClient -PvisualValidation \
  -I tools/water/multiplayer.init.gradle -PclientRunDir=run-water-guest -PcaptureServer=127.0.0.1:25575
```

For the scheduling, pressure and glacier checks, start a disposable dedicated server with the same seed and preset using `:runServer -PvisualValidation`. Set `DEEPPRIMAL_ENVIRONMENT_MODE=watershed-scheduling` and `DEEPPRIMAL_ENVIRONMENT_RECORD` to the desired result file. The server writes `WATERSHED_SCHEDULING_PASS` when all three checks pass.
