# Expanded replica — Renderer Jams/20261028-nonempty-refresh/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_de4f5b1a1fec819189b13e6873adeee1&account_id=personal) |
| Source identity | library: Renderer Jams/20261028-nonempty-refresh/README.md |
| Preserved source SHA-256 | `c19a7885f4595414e66e8b21e42036566394ce2f8e13c0a0336345ae802515c9` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/render-performance.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Adds successful-nonempty directory refresh after 400 client ticks, retaining separate empty polling, missing/error backoff, 64-chunk retarget, and bounded scan/admission. It closes a reproduced stationary-camera case where a newly saved page remained undiscovered. This preserved Library copy is byte-identical to its Git jam counterpart.

## Research expansion — gaps and details to deepen

- Polling cadence is measured in client ticks and full scans still enumerate every filename. The next improvement needs trustworthy producer publication and measured huge-save I/O cost, while preserving periodic recovery for missed notifications and external changes.

## Research expansion — proposed contracts

- Define a saved-page publication event with world identity, revision, canonical key, and durable-write completion. Treat it as a prompt discovery hint, not authority to bypass loaded revision guards; retain bounded reconciliation and report arrival-to-visible age.

## Research expansion — acceptance checks

- Retain stationary-nonempty negative controls and saturation tests. Exercise stopped ticks, long scans, repeated file replacement, deletion, notification loss, and world reset in the native client; measure extra polling CPU/I/O and visible latency rather than promising a wall-clock deadline.

## Research expansion — source anchors

- Renderer jam 20261028-nonempty-refresh — stationary persisted-page rediscovery
- Remaining acceptance and next repair

This original shares identical bytes with: [git:docs/renderer-jams/20261028-nonempty-refresh/README.md](../../../git/docs/renderer-jams/20261028-nonempty-refresh/README.md). Both source identities are retained.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md) |
| `../20261028-recovery-epochs/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-recovery-epochs/REVIEW.md) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 20261028-nonempty-refresh — stationary persisted-page rediscovery

**Scope:** `Shnewbs/Deep-Primal` renderer/LOD disk discovery only. Exact source commit [3478cf5d8f44fcc0cdcced2576f12704732f44f0](https://github.com/Shnewbs/Deep-Primal/commit/3478cf5d8f44fcc0cdcced2576f12704732f44f0), following the bounded renderer-only claim at `426aef0`/`befd029`/`403b968`. Source implementation first landed at `fc0d567`, then the exact trailing-newline identity of the three Java source files was corrected in `3478cf5` before final evidence review. No Java statement or test expectation changed in the identity correction. The inherited version is `0.0.1-alpha.29-dev.49-v8snapshot.1`; no version bump, Gradle or JAR delivery is claimed.

Consumed the independent [2026-10-28 08:30 UTC audit](../../engine-hourly-audits/2026-10-28T0830-563b6ef/REVIEW.md), which demonstrated a **previously documented but still open** LOD failure: after a successful scan of a nonempty directory, a new file `0_0.plod` was not discovered during 600 stationary client ticks, though reset made it visible (`LOD_NONEMPTY_REFRESH_GAP_OBSERVED checks=1815`). Previous renderer self-audit [20261028-recovery-epochs](../20261028-recovery-epochs/REVIEW.md) scored 5.08/10. Source preflight read main, engine/water work states, graphics/upscaling/LOD architecture, previous renderer backlog/audit, recent commits and released renderer claims. Root `AGENTS.md` was absent. The general engine builder was checkpointed; no active writer owned this LOD service, no exclusive timing reservation was taken and no other schedule/work-state was edited.

## Production increment and affected files

- [`lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java`](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L19-L24): adds a separate **400-client-tick** successful-nonempty rescan interval. Existing 200-tick empty, 20→40→80→160→320 failure/missing-folder backoff, 64-chunk camera retarget, 8192-key cap and 256-entry-per-turn worker/owner budgets are retained.
- [Post-success selection](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L114-L144): schedules a nonblocking background rediscovery cursor after a *successful* scan whether the folder was empty or nonempty. The next scan starts only after the cooldown and any pending bounded queue application, avoiding per-frame directory reads and overlapping retired cursors.
- [`docs/renderer-jams/20261028-nonempty-refresh/LodDiskNonemptyRefreshChecks.java`](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/docs/renderer-jams/20261028-nonempty-refresh/LodDiskNonemptyRefreshChecks.java#L53-L110): original real-filesystem regression for fixed-camera new pages, quiet cadence, invalid filename, second refresh and admission under a saturated 8192-key queue.
- [`docs/renderer-jams/20261028-nonempty-refresh/LodDiskRetryChecks.java`](https://github.com/Shnewbs/Deep-Primal/blob/3478cf5d8f44fcc0cdcced2576f12704732f44f0/docs/renderer-jams/20261028-nonempty-refresh/LodDiskRetryChecks.java#L47-L84): separate production-excluded historical retry fixture now checks the **new** successful-rescan contract, rather than asserting the old never-rescan behavior. Historical prior-jam paths are left intact.

No `VoxelLodRenderer`, mesh geometry, shaders, Vulkan/OpenGL backend, resource identifiers, renderer settings, water surface simulation/transparency, terrain generation, save codec, approved texture or general engine source was modified.

## Focused Java checks (executed)

Linux x86_64 shared host, 5 visible CPUs and no `/dev/dri` device. Verified `Temurin 25.0.5+8` read-only from the finished `20261028-disk-nearest` workspace (`java -version`, `javac -version`); OpenJDK 21.0.12.1 remains the default for other ports. The user-provided original archive and handoff directory were not present in this run. The same exact local Java source files and GitHub blob IDs were verified after commit: service `d3c80764a533f86e4d1623ec3d86d1df1c3752a0`; new regression `e2354c97a36a11ab370e05ae0ce1fe2721b7ebda`; adapted retry `399b775186bbcc0067c3cd6389ba389aadb80c22`; unchanged nearest scanner `fc76905ab1f82ba49cc4495ab8b943127b2b8d21`.

```bash
find src tests -name '*.java' | sort > sources.txt
$JDK25/bin/javac --release 25 -Xlint:all -Werror -d classes25 @sources.txt
/usr/bin/javac --release 21 -Xlint:all -Werror -d classes21 @sources.txt
for version in 25 21; do
  for t in LodDiskNearestScanChecks LodDiskIncrementalChecks LodDiskIndexServiceChecks LodDiskRetryChecks LodDiskRecoveryEpochChecks LodDiskNonemptyRefreshChecks; do
    "$JAVA_FOR_VERSION" -ea -Xverify:all -cp "classes${version}" "com.shnewbs.deepprimal.lod.client.$t"
  done
done
```

Final **Java 25**: `9006 + 8295 + 528 + 4399 + 790 + 3300 = 26,318` focused assertions. Final **Java 21**: `9006 + 8295 + 536 + 4399 + 823 + 3300 = 26,359` assertions. Counts for concurrent scanner tests can vary slightly with scheduling; all final exit codes were zero. The adapted retry test's permission-error subcase reported not applicable under privileged filesystem execution; missing-folder/backoff/reset cases passed. Two independent **old-source negative controls** failed as expected: the new stationary rediscovery test and the updated old retry expectation. This confirms the defect exists in the previous coordinator without fabricating native GPU failure evidence.

An isolated diagnostic compared 900 unchanged-camera `tick()` callbacks on 2,048 real filenames plus one added near file, using Java25 and `Thread.sleep(1)` between ticks. **Old source:** new file never found, callback p50/p95/p99 **2,293/12,378/43,886 ns**; **new source:** near file found at tick **420**, callback p50/p95/p99 **1,722/15,583/73,501 ns**. The candidate's higher p99 reflects additional periodic work, not a frame-time improvement. The shared-host runs were not exclusive or a paired native frame benchmark. Full Gradle was deliberately not run under the October 27 owner-build override.

## Remaining acceptance and next repair

The 400-tick interval is measured in **client ticks**, not wall-clock seconds. No guarantee is made while client ticks stop or if a pathological directory scan takes many worker turns. Repeat scans still enumerate the entire folder in bounded increments; monitor background I/O and cancellation on huge saves, and consider trustworthy producer-side notification for prompt local saved-page arrival. Owner-native save/reopen, stationary edit, teleports, negative-coordinate seams, transparency, resource reload and Vulkan/GL screenshots remain open. The separate unexplored-terrain delivery contract, real GPU frame/VRAM/batching/indirect acceptance, effective render-distance budgeting, temporal FSR and valid RTX DLSS dispatch remain unqualified.

**Unrun:** complete Java25 Gradle/NeoForge, Minecraft world/open/close/reload, Vulkan/OpenGL GPU pixels, AMD Windows or NVIDIA RTX native execution, GPU completion fences, real frame-time p50/p95/p99, full archive/nested JAR/package verification. No current JAR, GitHub Release, CurseForge upload or FPS claim. Owner handles these full builds per October 27 override. The independent whole-engine ten-category grade remains 5.0/10, **carried** and not modified. The renderer-only claim is released after checkpointing.


<!-- END PRESERVED SOURCE -->
