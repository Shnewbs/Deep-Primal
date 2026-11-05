# Expanded replica — docs/renderer-jams/20261028-near-ring-fast-reject/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-near-ring-fast-reject/README.md) |
| Source identity | git: docs/renderer-jams/20261028-near-ring-fast-reject/README.md |
| Preserved source SHA-256 | `ffb0294b8b05339a525ee04f052c5c9f96f6cec2e63730072ee6822fbd86fa62` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../../README.md) · [Integrated design](../../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../../chapters/render-performance.md) · [Discrepancy register](../../../../../DISCREPANCIES.md)

## Research expansion — source scope

Rejects strictly farther Chebyshev-ring disk candidates before boxing, set lookup, and exact distance work once the nearest heap is full. Equal rings continue through full ordering; 256-entry turns and 8,192-key retention remain unchanged.

## Research expansion — gaps and details to deepen

- The recorded allocation benefit depends on dominated far misses and does not apply uniformly to every directory. A flaky asynchronous scanning-state observation is explicitly separate from product correctness and needs a deterministic fixture signal.

## Research expansion — proposed contracts

- Record candidate population by nearer/equal/farther ring, retained-set capacity, and exact tie policy. Replace transient-state polling in tests with observable lifecycle synchronization while preserving source behavior; link the later same-ring full-rank optimization separately.

## Research expansion — acceptance checks

- Keep BigInteger ordering and the rejected greater-or-equal mutant, then replay empty, unsaturated, equal-ring, and far-dominated directories. Compare native visible recovery and frame/I/O tails; mixed CPU results and 6.7–7.7% allocation changes are not FPS evidence.

## Research expansion — source anchors

- Renderer coding jam: nearest-ring early reject (2026-10-28 11:02 UTC)
- Change and consumed audit

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `CLAIM.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-near-ring-fast-reject/CLAIM.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer coding jam: nearest-ring early reject (2026-10-28 11:02 UTC)

Jam ID: `renderer-20261028T1102-near-ring-fast-reject`. Scoped claim: [CLAIM.json](CLAIM.json). Parent at implementation integration: `a6ee38079422d1e317928ffb50ae57bf2cfd59b1`; inherited version `0.0.1-alpha.29-dev.51-v8preflight.1`. No jar, GPU or renderer release generated under the October 27 owner-build override.

## Change and consumed audit

The independent [10:30 UTC audit](../../engine-hourly-audits/2026-10-28T1030-10cb0c6/REVIEW.md) carried unresolved frame/LOD coverage; the previous [5.21 renderer self-audit](../20261028-filename-parser/REVIEW.md) measured 22-24% fewer parser allocations but slower scan CPU. This jam changes only the nearest saved-page selection inside `lod-engine/.../LodDiskNearestScan.java`, plus a source-excluded focused regression. Retain 256 entries/turn, 8192-key queue, 400/200 tick rediscovery, signed-extreme and water/LOD order.

Once the bounded nearest-candidate heap is full, discard an incoming page immediately if its unsigned-safe Chebyshev ring from the camera is **strictly greater** than the currently farthest retained ring. This avoids HashSet membership/Long boxing, RankedKey allocation and 128-bit squared-distance computation on far misses. Equal rings **always proceed** to full tie-break comparisons, retaining exact result ordering and coordinate determinism. This does not increase rendered distance or claim unexplored-world delivery.

## Focused execution

Isolated Linux Java21 `java 21.0.12.1`, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all` on eight source-only test classes: NearestScan, Incremental, IndexService, Retry, RecoveryEpoch, NonemptyRefresh, FilenameParser, and new NearRingFastReject. Final pass: **79,526 assertions**. The pre-existing asynchronous RetryChecks test intermittently loses an ephemeral scanning-state observation (one earlier failure, later replays pass), and is not counted as a new renderer defect repair. An intentionally mutated `>=` reject variant failed the nearest-ranking test as expected.

Reproduction from the evidence bundle:
```bash
find src tests -name '*.java' -print > candidate-sources.txt
printf '%s\n' LodFilenameScanAllocationBench.java >> candidate-sources.txt
javac --release 21 -Xlint:all -Werror -d candidate-classes @candidate-sources.txt
java -ea -Xverify:all -cp candidate-classes com.shnewbs.deepprimal.lod.client.LodNearRingFastRejectChecks
java -cp candidate-classes LodNearRingPairedBench baseline-classes candidate-classes
```
Paired 6000-real-filename / 512-retained-key Java-thread allocation p50: baseline 3,937,464 B vs candidate 3,634,632 B (**7.69% lower**), recheck 3,954,032 B vs 3,687,808 B (**6.73% lower**). CPU p50: 5.772 vs 4.972 ms in trial 1 but 4.969 vs 5.064 ms in trial 2; p95/p99 mixed. This is I/O+CPU metadata selection, not FPS, resident memory or GPU benefit.

Java25 installation and owner archive were not mounted in this isolated run; a Library title search found no archive. No full Gradle/NeoForge, native world save/reopen, Vulkan/GL pixels, named AMD/NVIDIA hardware, VRAM, FSR temporal, DLSS RTX, JAR or release test ran. No engine, water, UI, shader, saved profiles, other scheduler or mutable build/cache outputs changed.

Next: host-native camera/edit/travel and snapshot restore measurements, alongside actual GPU CPU/p95/p99 frame, upload/VRAM/water observations. Investigate intermittent RetryChecks scanning-state test separately without treating a flaky observation as product failure.


<!-- END PRESERVED SOURCE -->
