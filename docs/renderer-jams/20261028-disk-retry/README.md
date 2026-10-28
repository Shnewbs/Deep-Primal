# Renderer jam 20261028-disk-retry — same-camera persisted LOD recovery

**Completed source-only implementation:** [`a5b29c00`](https://github.com/Shnewbs/Deep-Primal/commit/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4). Inherited engine version `0.0.1-alpha.29-dev.47-v8columns.1`; no version bump, JAR or release. This jam does not touch terrain/world generation, water simulation, shader contents, texture assets, Vulkan backend or unrelated schedules.

**Source lineage:** original main `eeaf7e067a55ddca668f993270c284b0a1e47505`; bounded renderer claim `55a212f6f6bef97a8ce0ae9fdaa22d8314be2978`; exact product/test commit `a5b29c00c66b243490cee9b8c4ae1e17f732f6a4`. Consumed independent [06:33 UTC audit](../../engine-hourly-audits/2026-10-28T0633-1aa65a8/REVIEW.md) and prior [renderer self-audit](../20261028-disk-cursor/REVIEW.md) (4.92/10). The 06:33 audit independently reproduced two same-camera permanent discovery gaps in the exact former product code: a directory created after initial absent-folder scan, and a transient directory permission failure that later recovered.

## Actual renderer changes

- [`LodDiskNearestScan.java`](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L100-L155) distinguishes an initially absent save directory from a successful stable scan. Its worker still owns directory enumeration, cancellation/close, nearest-key selection and the 256-entry-per-grant/8192-key bounds. Missing directories still preserve already queued recovery records.
- [`LodDiskIndexService.java`](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L18-L96) automatically retries after missing-folder completion and transient I/O failures without camera movement. A per-client-tick exponential delay progresses 20, 40, 80, 160, 320 ticks then caps; an actual camera retarget bypasses the delay; a successful scan clears it. Deferred queue reconciliation completes before any retry. I/O error reporting is once per failed attempt, missing-folder retry is silent.
- [Service worker reset/retirement](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L126-L153) retains reference to a cancelling cursor until its thread has stopped so rapid world resets cannot immediately create overlapping directory workers. This does not join or block on the render thread.
- [`LodDiskRetryRegression.java`](https://github.com/Shnewbs/Deep-Primal/blob/a5b29c00c66b243490cee9b8c4ae1e17f732f6a4/docs/renderer-jams/20261028-disk-retry/LodDiskRetryRegression.java) is a committed, source-only filesystem regression. A second wider local test covers 1,700 consecutive absent-folder ticks, backoff saturation, reset, retarget and unprivileged permission restoration. No foreign feature files were changed.

## Exact focused tests and results

Test workspace uses exact SHA-verified product sources, prior jam's scanner/incremental/service fixtures and new source-level regression. Linux x86_64; installed OpenJDK 21.0.12.1 and reused (read-only) Eclipse Temurin JDK 25.0.5+8. Both compilers used `-Xlint:all -Werror`; both JVMs used `-ea -Xverify:all`. JDK25 `java -version` and `javac -version` were actually checked. The Java25 executable was found at `/mnt/data/deepprimal-renderer-jam-20261028-disk-nearest/tools/jdk-25.0.5+8/bin` in this run; this path is not a persistent scheduler contract.

```bash
find src tests -name '*.java' | sort > sources.txt
"$JAVA_HOME/bin/javac" --release 25 -Xlint:all -Werror -d classes25 @sources.txt
/usr/bin/javac --release 21 -Xlint:all -Werror -d classes21 @sources.txt
# For each compiled JDK: java -ea -Xverify:all -cp classesN
# com.shnewbs.deepprimal.lod.client.{LodDiskNearestScanChecks,LodDiskIncrementalChecks,
# LodDiskIndexServiceChecks,LodDiskRetryChecks}
# Run LodDiskRetryRegression as an unprivileged Linux user for chmod 000 I/O.
```

| Focused regression | Java25 | Java21 |
|---|---:|---:|
| Nearest scanner | 9,006 pass | 9,006 pass |
| Incremental cursor | 8,295 pass | 8,295 pass |
| Client integration queue | 534 pass | 564 pass |
| Backoff/late folder/reset extended checks (root test) | 4,218 pass | 4,218 pass |
| Committed permission/late-folder regression (unprivileged) | 8 pass | 8 pass |
| **Final test pass totals** | **22,061** | **22,091** |

Queue assertion count varies with async worker completion timing; each complete final run passes. The extended retry fixture also passed 4,220 assertions unprivileged on Java25. A pinned baseline compile with the old product source fails the late-folder acceptance with `AssertionError: same-camera retry never found`, exit code 1 as expected. No game source mutation was made after the final product commit during self-scoring.

No controlled before/after FPS or full-frame latency comparison was possible: these are source-level functional tests. The retry has bounded tick cadence, not a guarantee of filesystem worker completion within a frame.

## Open gates

Owner's October 27 override: the owner performs complete Gradle/NeoForge/Minecraft/Vulkan/OpenGL/GPU builds; no hosted Actions/build/release was run and **no new JAR** is claimed. Native save/reopen, same-world edit/travel and world-switch stress, disk read-only device loss, full-scene LOD coverage, cross-frame water transparency and GPU frame p50/p95/p99 remain unrun. A *successfully scanned but initially empty* stable directory does not automatically detect files created later without camera movement or reset; it is a separate future rediscovery issue. Deep backlog: distant unexplored terrain delivery, full-detail network/tracking budgets, true GPU submission/batching, actual temporal FSR and DLSS Super Resolution integration, controlled Vulkan/OpenGL visual and named-GPU evidence. This repair does not improve those unrelated stages.

**Next renderer jam:** owner native world-reopen/teleport/edit pass using the committed retry with exact saved-page coverage assertions; then measure upload tails and implement independently bounded stable-directory rediscovery only if native checks demonstrate the gap.
