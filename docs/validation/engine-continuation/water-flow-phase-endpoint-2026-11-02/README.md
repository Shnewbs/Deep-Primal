# Dev212 — Half-open flow-texture atlas phases (2026-11-02 20:00 UTC)

## Coordination and audit

Consumed independent Dev211 audit at `docs/engine-hourly-audits/2026-11-02T1930-dev211/REVIEW.md`. Starting `main` was `b9b7f8a089e54f78bc729d3b16fce83287dd9138`; the prior Dev210 writer claim was released. Acquired the bounded Dev212 lease before source edits. Work is limited to `WaterPatchMesher.flowUOffset/flowVOffset`, a new dedicated core regression, its `CoreChecks` registration, this report and the writer lease. Preserved Dev211's `WaterMeshShape` correction and audit files. No GitHub Actions.

## Reproduced defect

The two flow-offset helpers calculate a double fractional phase in [0,1) and narrow it to float. Some valid integer world coordinates produce fractions so close to one that float rounding yields exactly `1.0f`, escaping the half-open texture-atlas interval. The cardinal direction-table float residuals make this reproducible at `flowUOffset(4, 0, 1)` and `flowVOffset(8, 0, -1)`. An independent 16-direction x 65x65 grid found 1,997 invalid U and 1,997 invalid V endpoints before correction.

The correction changes only those rounded endpoints to `Math.nextDown(1.0f)`; every other float output is bitwise identical. It does not modify geometry topology, water quantity, salt/sediment, native renderers or saved formats.

## Local deterministic validation

- OpenJDK 21 `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all`: **passed** for an isolated exact-method fixture.
- Committed `WaterFlowPhaseEndpointChecks`: **675,603 assertions passed**; 16 directions x 65x65 grid, 16 x 9x9 signed-int extremes, 100,000 seeded random positions within +/-30 million, cardinal negative-control witnesses and invalid direction handling.
- **9,821** rounded endpoint observations; every changed value was originally exactly 1.0f; all ordinary outputs preserved their raw float bits.
- Original method-body negative control: fails at assertion 3, `flow phase escaped half-open atlas interval`, as expected.
- Dev211 follow-up: reconstructed exact `WaterMeshShape.java` Git blob `33eb816b53ef6ff8dc37a770ff6b6906d53f0bc6`, passed 303,003 finite assertions, 274,625 shape samples, 64 side-UV cases and 30 near-unit cases.
- GitHub fetched-back production blob: `5bc8fb9a098bc940d38e874c808103d1d48c8e32`. The permanent regression's local `git hash-object` and GitHub blob both equal `70c8894c70d3e554eef559c2e04a2730a119e101`; `CoreChecks` registration verified.
- Reproducible evidence ZIP SHA-256: `d12b305ee956021c28b1190df8386774d6a38c85e10447d8b52de12f9bb48909`.

The fixture contains the **exact production method bodies** and direction-table initialization, not the entire WaterPatchMesher class. This is isolated Java verification, not a full production compile or GPU acceptance.

## Delivery blockers and next gates

Only Java 21 is available locally. No complete private checkout, Java 25 toolchain, resolved Gradle/NeoForge dependencies or working local GitHub DNS. **No full Gradle/coreChecks/NeoForge JAR, JAR SHA-256 or native Vulkan/OpenGL waterfall acceptance**. Existing qualified JARs were preserved; none are mounted in this environment. No whole-engine regrade: historical baseline **5.0/10**.

Next: restore Java25 production build, test packaged flow-offset UV seams on Vulkan/OpenGL, validate finite-water save/reopen and natural waterfall continuity, then LOD/terrain native performance gates.
