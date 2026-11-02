# Dev202 — Single-copy tick telemetry snapshots (2026-11-02)

**Starting audited main:** `121f443bb4b02bc13cd9d1672e00e140751c151a` (Dev201 independent frame-budget audit). Scoped writer claim: [CLAIM.json](CLAIM.json). This work intentionally does not touch native water physics, terrain, Vulkan, LOD, organics, world saves or build settings.

## Reproduced issue and fix

`TickWindow.snapshot()` first copied the initialized ring prefix. `SampleStats.of(long[])` then cloned that copy again before sorting. Each snapshot therefore allocated two primitive arrays even though the live tick ring was not mutated.

`TickWindow.snapshot()` now passes the ring and its initialized count to a package-private `SampleStats.of(long[], int)`. That helper makes **one** defensive prefix copy before validating/sorting. The existing public `SampleStats.of(long[])` remains available and delegates to the helper. Partial rings, wrapped full rings, invalid duration admission and percentile semantics are preserved.

## Local validation (Java 21, isolated)

- Strict compilation of original and candidate production classes: `javac --release 21 -Xlint:all -Werror` **passed**.
- Candidate bytecode verification and independent reference oracle: `java -Xverify:all` **passed**.
- **469,065 checks** across **140,000 deterministic ring operations**, including capacities 1/2/3/7/16/64/256, partial/full rings, wrap, clear, negative samples, exact p50/p95/p99/max and public caller-array immutability.
- Existing `TickWindowDurationChecks` remains registered. New `TickWindowSnapshotChecks` is registered in `CoreChecks`; the **full CoreChecks suite was not executed**.
- Isolated HotSpot ThreadMXBean allocation benchmark (50,000 snapshots after warmup, same machine, 256-element ring): **4176 → 2112 bytes/snapshot**, 49.4% less. Partial 17-element ring: **352 → 200 bytes/snapshot**, 43.2% less. This is allocation, **not** measured Minecraft FPS or Vulkan throughput.
- Original and candidate source Git blob IDs were compared with GitHub and matched. New test and CoreChecks registration were also fetched and verified.

## Exact verified blobs

| File | Git blob |
| --- | --- |
| `engine-core/src/main/java/com/shnewbs/deepprimal/core/TickWindow.java` | `db37a1d5ef6cb450a150caa617581c98033c2700` |
| `engine-core/src/main/java/com/shnewbs/deepprimal/core/SampleStats.java` | `37b6ac9099dd039faa583f7fd60f33921a95335c` |
| `src/coreChecks/java/com/shnewbs/deepprimal/core/TickWindowSnapshotChecks.java` | `0c4e0e5f2dc16b6eff9eb7f26912ab68a1534060` |
| `src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java` | `d0cf32432683b87ec599fba18c3aa4e3c3da7da4` |

Reproducible evidence archive: `DeepPrimal_dev202_tick_snapshot_evidence.zip`, SHA-256 `c86deb7efa792d375df84dd5fa365bd8466210cd018e07e04206c39e5921bacb`. Contains baseline/candidate source, the exact registered test, benchmark, strict compile/replay script, log and scope notes.

## Production acceptance and artifact status

Local build environment: OpenJDK 21 only, no Gradle installation or complete private checkout, no resolved NeoForge dependencies. `git ls-remote` returned `Could not resolve host: github.com`; GitHub connector provided source access and direct-main writes. No GitHub Actions were invoked.

**No playable NeoForge JAR or JAR SHA-256 was produced.** No previously qualified JAR was found in this mounted runtime, and none was deleted. Do not replace a prior successful JAR with this evidence ZIP. Full Java 25/Gradle, native game startup, water save/reopen conservation, terrain seam and GPU/LOD performance gates remain open. Historical whole-engine grade remains 5.0/10; no fresh regrade.

Next: restore Java 25 plus full private checkout/dependency cache, execute full `CoreChecks`, produce a verified local JAR, then run native water and Vulkan/LOD acceptance before retiring any previous build.
