# Dev207 — Independent Deep Primal audit (2026-11-02 17:30 UTC)

## GitHub scope and coordination

- Prior audit head: `d85da72b456042cd59a578784277dc1f91db9de6` (Dev205).
- Inspected `main` at `c6cb9c997b7b24263a49188c1aa0d51e128fec5d`: **7 commits ahead**, zero behind. Changes comprise Foundations Soil read-only organic bridge and Dev206 LOD column-query binary search, tests, evidence and released writer lease. No concurrent active writer claim was found in the inspected lease (`completed-released`, `lease_released=true`).
- Reviewed exact `ColumnarChunk.java`, its registered test, `FoundationsSoilPhysicsBridge.java`, its test, integration contract, `gradle.properties`, root/engine-core Gradle configuration, and prior audit.
- This audit did **not** alter production source, organics, water, renderer, terrain, LOD, test registration or GitHub Actions. No new production defect was confirmed in the tested slice.

## Independent exact-source LOD validation

- Candidate `ColumnarChunk.java` Git blob: `1699be49b34b223ad53e509f4a32e0730fc41865`.
- Historical linear-lookup baseline Git blob: `402a89d7b8da4ad90bd3759355f87f7191a06b6f`.
- Both source copies were reconstructed from connector-fetched exact GitHub text, then independently checked using `git hash-object` against those blob IDs.
- `javac --release 21 -Xlint:all -Werror` and `java -ea -Xverify:all -Xmx512m` passed on both variants.
- **675,168 independent assertions passed in each variant**, across 54 deterministic scenarios: heights 1, 2, 3, 7, 31, 64, 257, 1024, 4096; six minimum-Y positions including `Integer.MIN_VALUE` and `Integer.MAX_VALUE-height`; uniform, alternating, layered and seeded-random column states. The oracle compares returned palette state against independently generated voxel IDs, with selected linear scans, invalid-coordinate checks, malformed-run/offset rejection, and defensive-copy checks.
- A deliberately corrupted binary-search boundary predicate (`<` to `<=`) compiled but failed the independent oracle at check 1,654, confirming boundary-test sensitivity.
- This run did not measure representative in-game LOD FPS, CPU allocation, Vulkan, or GPU performance. The earlier Dev206 performance figure is not independently reproduced here.

## Organics and other engine boundaries

- Source-reviewed the newly added read-only `FoundationsSoilPhysicsBridge` and its seeded deterministic test. No authoritative water or N/P/K mutation appears in that class. The 50,000-trial bridge test and upstream Foundations Soil runtime contract were **not independently executed in this audit**.
- No new commits changed the native water, terrain, Vulkan or OpenGL implementations in the compared diff. Those subsystems and save/reopen were not runtime-tested in this run. No compatibility or performance pass is inferred from their absence in the diff.
- Root `gradle.properties` still pins `neo_version=26.1.2.122`; root/engine-core Gradle require Java25 and ModDevGradle 2.0.148. NeoForged's project listing currently shows 26.3.0.101 and ModDevGradle 2.0.148. No NeoForge 26.3/26.4 compilation or client/server startup occurred. Minecraft 26.4 snapshot Vulkan compatibility remains a native acceptance requirement.

## Build, packaging and preservation

- This container has OpenJDK 21 and Git, but no complete private repository checkout, Java25 toolchain, Gradle/NeoForge dependencies, or accessible GitHub DNS for `git clone`/`git ls-remote`. The GitHub connector permits scoped reads and audit report commits, not a full authenticated clone into the local runtime.
- **No full Gradle build, fresh playable JAR, or playable JAR SHA256 was produced.** Existing audit JARs were not deleted. No GitHub Actions were invoked.
- No production fix was committed because the independently tested candidate passed and no confirmed nonconflicting defect warranted a change.
- Independently verified evidence ZIP SHA256: `154c3506c55f971fe80d25681408ea9ea75849133ffae9d867434598ffd17a41`.

## Grades and next action

- Scoped LOD CPU state-query correctness: **8.5/10 provisional** (exact-source verification, two implementation variants, boundary and malformed-input coverage, negative control). This is not a gameplay/renderer grade.
- Whole-engine: **5.0/10 historical baseline, not freshly regraded**; full runtime acceptance is blocked.
- Next: restore a complete local Java25 Gradle/NeoForge checkout; build `jar` and `coreChecks`, verify package/JarJar integrity and SHA256, launch client and server, validate finite-water conservation/save/reopen, terrain seams, native Vulkan/OpenGL waterfall rendering and LOD frame-time/VRAM behavior. Preserve the previous verified JAR until a replacement passes.

## Sources

- https://github.com/Shnewbs/Deep-Primal/compare/d85da72b456042cd59a578784277dc1f91db9de6...c6cb9c997b7b24263a49188c1aa0d51e128fec5d
- https://github.com/Shnewbs/Deep-Primal/blob/c6cb9c997b7b24263a49188c1aa0d51e128fec5d/docs/validation/engine-continuation/lod-column-lookup-2026-11-02/README.md
- https://projects.neoforged.net/
- https://www.minecraft.net/fr-ca/article/minecraft-26-4-snapshot-2
