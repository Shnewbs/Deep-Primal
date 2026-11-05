# Restored core validation and pressure-cutoff reproduction

2026-10-22 continuation, starting from working source
`4979010d684e63a78104f7be2a479702ffc37f25`. No production mod source changed in
this run; the prior page-admission fix remains unreleased and native-unvalidated.

## Recovered environment and fresh checks

Direct Git cloning required unavailable CLI authentication. The connected GitHub
API restored 370 default-build input files into an isolated directory. Every file
was checked against its Git blob SHA-1 after removing the patch transport's extra
newline. Optional Vulkan source, standalone engine, documentation and the wrapper
JAR were not materialized; the downloaded Gradle launcher was used directly.

Temurin JDK 25.0.4.1+1 and Gradle 9.2.1 archives were downloaded and extracted.
The exact archive hashes and scope are in `2026-10-22-toolchain-restoration.json`.
The API's `latest` JDK URL may change; use the recorded version/hash when comparing.

- Compiled all 54 engine-core/coreChecks Java files with JDK 25; compiler exit 0.
- Executed `com.shnewbs.deepprimal.core.CoreChecks`; exit 0 and explicit terminal
  pass records. Nine seeds retain 5,846,893 centerline samples, 964 headwaters,
  650 lakes, 4,292 connected routes, 538,915 bank checks and 18,000 frozen V2
  comparisons. Surface, cave, drainage, conservation and cache checks also pass.
  Full output: `2026-10-22-restored-core.txt`.
- Repeated the actual-source page-admission fixture under JDK 25; it passes the
  513 overflow/2,000 randomized cases and memory-pressure checks. This still uses
  scheduler doubles, not a native renderer. See `2026-10-22-restored-admission.json`.
- The full default Gradle `build` attempt fails in settings plugin resolution,
  before mod compilation. Diagnostics show `java.net.SocketException: Network is
  unreachable` when Java contacts the plugin repositories. Curl reaches the
  requested Foojay marker with HTTP 200, so the missing-marker message does not
  establish that the plugin is absent. System truststore and IPv4 retries also
  failed. Do not change the project's plugin versions based on that message.

The core compile/run used `javac -d <classes>` over the engine-core and coreChecks
Java source sets, followed by `java -Xmx2G -cp <classes>
com.shnewbs.deepprimal.core.CoreChecks`. Gradle used `--no-daemon -PwithoutVulkan
-Pmod_version=0.0.1-alpha.25-dev build`; that development name was never packaged.

## Concrete pressure defect reproduced

`tools/water/reproduce_pressure_cutoff.py` compiles the current `equalize` and
`surface` method bodies with the real `WaterTransport` and `TerrainNoise` classes.
ServerLevel, block positions, storage and writes are small in-memory doubles.
This isolates traversal from native Minecraft scheduling and chunk behavior.

The fixture builds a sealed, face-connected U-bend of 262 full water cells, with
the left surface one block higher than the right. An independent connectivity
walk verifies the complete route. Only the two free surfaces have air above them.

Observed current behavior:

- A short 38-cell control transfers four units and conserves volume.
- The 262-cell case visits exactly 128 cells, moves no water and leaves zero
  queued pressure origins, despite the still-connected unequal surfaces.
- One hundred externally requested restarts repeat the same prefix without
  reaching the other arm. All existing water quantity is retained.
- The positive acceptance mode `--expect-fixed` fails with `Pressure cutoff
  remains`. The default diagnostic mode exits zero only after demonstrating the
  expected defect; its label is `PRESSURE_CUTOFF_REPRODUCED`, not a solver pass.

Run from the repository root:

```sh
python3 tools/water/reproduce_pressure_cutoff.py --java-home /path/to/jdk25 --json /tmp/pressure.json
python3 tools/water/reproduce_pressure_cutoff.py --java-home /path/to/jdk25 --expect-fixed
```

Evidence: `2026-10-22-pressure-cutoff.json` and
`2026-10-22-pressure-acceptance.json`. A broader game update may activate cells
again; this fixture specifically proves that this search discards continuation
at the capacity boundary. It does not claim a native whole-world deadlock.

## Open gate and next implementation

No pressure fix is shipped here. Increasing the search constant or blindly
requeueing the same prefix would not provide continuation. Retain a bounded
frontier and visited state fairly across ticks, preserve pending work when a
budget ends, and handle edits/unloads before committing endpoints discovered in
different ticks. Validate long-route progress, branch competition, cancellation,
quantity, unloaded boundaries and genuinely idle settled vessels natively.

Resolve Java artifact-download access and complete the full module/default build
before releasing the previous renderer fix. Then restore native display/driver
dependencies and run enabled-view pressure/teleport/edit/reload scenes. Full
Minecraft, graphics, Vulkan and performance checks were not run this time.
Earlier alpha.24 native results remain historical for these later branch changes.
Published V1/V2/V3 geography, water runtime and supplied installers are untouched.
