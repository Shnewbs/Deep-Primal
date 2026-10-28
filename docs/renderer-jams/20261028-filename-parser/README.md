# Renderer jam 20261028-filename-parser — bounded saved-LOD indexing allocations

**Jam ID:** `renderer-20261028T0953-filename-parser`. **Frozen production and test source:** [`4d19fa909bb39fe21a5db95f87fe9eeb5092c108`](https://github.com/Shnewbs/Deep-Primal/commit/4d19fa909bb39fe21a5db95f87fe9eeb5092c108) at inherited version `0.0.1-alpha.29-dev.50-v8strict.1` (no renderer release/version bump). Parent before ownership claim: `f6a46f1730b697a8e91a8e0cc51cf87f6aaf536c`; bounded claim commit `6263f4eaa7b0c7343a2623568161850f3f7dab8c`. Final documentation checkpoint is a later docs-only commit; do not treat it as newly compiled source.

**Scope and consumed evidence.** Read current main, absence of root `AGENTS.md`, engine/water/upscaling work state, LOD architecture, graphics docs, renderer backlog, previous renderer [5.17/10 self-audit](../20261028-nonempty-refresh/REVIEW.md), independent [09:33 UTC dev50 audit](../../engine-hourly-audits/2026-10-28T0933-4b61329/REVIEW.md), recent commits and prior released claims. Neither independent audit demonstrated a GPU frame improvement; both emphasized remaining native LOD coverage and I/O-tail gates. No shared benchmark reservation, GPU test, renderer provider change or other role file was taken.

## Concrete production change

- [`LodDiskNearestScan.java` (Git blob `4452f36a0b0162b8194729cc3b97a47f8172228d`)](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L240-L257): scan each canonical `x_z.plod` filename directly. Avoid per-entry coordinate array, two integer substrings, a reconstructed canonical string and base substring. The signed-32-bit parser [L283–310](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskNearestScan.java#L283-L310) uses negative accumulation, rejects overflow and noncanonical leading zeros, `-0`, plus signs and extra separators, while retaining `Integer.MIN_VALUE`/max and signed-extreme ordering.
- New production-excluded [`LodFilenameParserChecks.java` (blob `73596dd1770064e5765dfd84c351a5c91c829925`)](https://github.com/Shnewbs/Deep-Primal/blob/4d19fa909bb39fe21a5db95f87fe9eeb5092c108/docs/renderer-jams/20261028-filename-parser/LodFilenameParserChecks.java#L1-L104) compares the output of the scanner against a copy of its previous canonical parser on 3,122 actual filesystem names, five signed/extreme camera positions and five admission limits.
- No `VoxelLodRenderer`, GPU buffer, shader, DLSS/FSR, water simulation/surface, terrain, saved-profile codec, resource ID, UI, configuration, unrelated module or other job output changed. Existing 256 metadata entries/grant, 8,192-key cap, 400/200-tick rediscovery and cancellation remain unchanged.

## Executed focused checks

Environment: isolated Linux x86-64 container, OpenJDK 21.0.12.1 `java`/`javac`, no known GPU/`/dev/dri`; original Java25 archive and previous workspace path both absent. Explicitly **not** a Java25/Gradle/NeoForge or native GPU build.

```bash
find src tests -name '*.java' -print > candidate/sources.txt
printf '%s\n' LodFilenameScanAllocationBench.java >> candidate/sources.txt
javac --release 21 -Xlint:all -Werror -d candidate/classes @candidate/sources.txt
for t in LodDiskNearestScanChecks LodDiskIncrementalChecks LodDiskIndexServiceChecks LodDiskRetryChecks LodDiskRecoveryEpochChecks LodDiskNonemptyRefreshChecks LodFilenameParserChecks; do
  timeout 50s java -ea -Xverify:all -cp candidate/classes com.shnewbs.deepprimal.lod.client."$t"
done
java -cp candidate/classes com.shnewbs.deepprimal.lod.client.LodFilenameScanAllocationBench
```

Final source-identity matches committed Git blob: production `4452f36a0b0162b8194729cc3b97a47f8172228d`, test `73596dd1770064e5765dfd84c351a5c91c829925`. All **seven Java21 focused suites passed, 43,764 assertions** in the final run (concurrent test counts vary), including bounded-turn, saturated-queue, cancellation, nonempty rediscovery, retry and filename parity. Permission-denial fixture was not applicable under privileged filesystem execution. A separate full *baseline* replay was interrupted during a concurrent recovery epoch test and was not treated as passing; the complete **candidate** replay passed.

### Before/after isolated component diagnostic — not FPS

The reproducible owner-independent diagnostic (source in the user-facing evidence ZIP) enumerates 6,100 real filenames, retains 512 candidates, performs five warmups then 32 scans, measures Java-thread allocated bytes via `ThreadMXBean`, and reports thread elapsed nanoseconds. Same Java21 host/procedure/camera/limit:

| Run | Old allocation p50 | New allocation p50 | Reduction | Old scan CPU p50 | New scan CPU p50 |
| --- | ---: | ---: | ---: | ---: | ---: |
| Initial sequential | 5,885,640 B | 4,454,152 B | **24.3%** | 7.91 ms | 8.88 ms |
| Recheck sequential | 5,734,544 B | 4,454,096 B | **22.3%** | 16.29 ms | 17.95 ms |

The measured allocation reduction belongs to the **synchronous scanner's whole thread**, including filesystem APIs; it is not a heap-retention result, render frame, GPU memory/latency or demonstrated FPS improvement. Both CPU-time comparisons were slower for the candidate on a noisy shared host; **no speed gain is claimed**. No independent matched native GPU performance or water pixel evidence exists.

## Remaining defects/gates and next repair

The directory enumerator still touches every saved filename during each full background rescan, though work is limited to 256 entries/grant. Parsing still allocates retained ranked key objects and filesystem `Path` wrappers; results still require selection heap and sorting. No authoritative distant unseen-terrain delivery, native 64-chunk retarget/world-reopen/teleport/edit validation, transparency screenshots, actual Vulkan/OpenGL frame p50/p95/p99, RAM/VRAM, or DLSS temporal/RTX evidence is established.

**Owner override:** no full Java25 Gradle/NeoForge build, Windows/Minecraft/GPU execution, final JAR, GitHub release or CurseForge publication was attempted. Owner handles those build gates. Next renderer increment should collect owner-native LOD/mesh-upload/I/O tails and implement only measured, dependency-ready scoped repairs; continue FSR temporal/DLSS work separately after correct GPU inputs and vendor requirements. This jam neither changes the independent whole-engine grade nor alters other schedules.
