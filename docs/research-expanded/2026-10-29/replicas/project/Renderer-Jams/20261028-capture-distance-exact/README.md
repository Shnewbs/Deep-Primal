# Expanded replica — Renderer Jams/20261028-capture-distance-exact/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://chatgpt.com/library?file_id=libfile_2565a85f549481918ea0cb58d3dc3ed3&account_id=personal) |
| Source identity | library: Renderer Jams/20261028-capture-distance-exact/README.md |
| Preserved source SHA-256 | `c20160e17d14e91c44a748aa443e90a0ab02eca737b18fc89dbc07d83eb2dc26` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/render-performance.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Repairs capture-priority double-distance ties by comparing exact unsigned 128-bit sums of widened delta squares within the same Chebyshev ring. Exact distance ties remain insertion ordered, and the existing oldest-first fairness escape is preserved. This preserved Library copy is byte-identical to its Git jam counterpart.

## Research expansion — gaps and details to deepen

- Exact ordering is a correctness property, while the recorded chooser medians became slightly slower. The extension should connect this helper to actual dirty-queue age and visible publication without making extreme-coordinate correctness a performance claim.

## Research expansion — proposed contracts

- Document the full ordering tuple, signed coordinate domain, insertion-tie policy, and fairness quota. Preserve exact tested-source identity, including the corrected trailing newline, and link independent GPU-relative coordinate and projection contracts.

## Research expansion — acceptance checks

- Retain the BigInteger oracle and former-baseline failure at signed extrema, plus saturation/fairness checks. On the native client, measure queue selection cost and capture-to-visible age under realistic and adversarial dirty sets; verify no geometry or water behavior changes.

## Research expansion — source anchors

- Renderer jam 2026-10-28T11:54Z — exact capture-distance tie handling
- Source change

This original shares identical bytes with: [git:docs/renderer-jams/20261028-capture-distance-exact/README.md](../../../git/docs/renderer-jams/20261028-capture-distance-exact/README.md). Both source identities are retained.

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `../../engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md) |
| `../20261028-near-ring-fast-reject/REVIEW.md` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/docs/renderer-jams/20261028-near-ring-fast-reject/REVIEW.md) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Renderer jam 2026-10-28T11:54Z — exact capture-distance tie handling

Scope: `lod-engine` capture scheduling only. Claim: `renderer-chat-20261028T1154Z-capture-distance-exact`. Starting `main` `aa91e9c3cd7464cf3133e6b79c1d4f159c1e1c64`; scoped-claim commit `1f681c22ae9e533d62d83f46090cad624e36c148`; initial implementation `826aee58e11024fe1d0ed2480eba5c9ca8443396`; exact tested-byte correction `6ec69da13429709f53b6a664af4ed14e120a35d8`. Inherited version is `0.0.1-alpha.29-dev.52-v8recover.1`; no version bump/JAR publication. No active renderer, engine or water file lease overlapped; prior renderer claim was released.

## Source change

`lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java` still chooses Chebyshev ring first, and retains its every-eighth oldest-first scheduling in its caller. For candidates with the same ring whose double squared distances round equal, the chooser now compares exact unsigned 128-bit sums of two signed 64-bit delta squares. This preserves true nearest-first ordering across the signed-int coordinate domain and leaves exact distance ties in insertion order. It does not change or allocate GPU buffers, alter meshes/water/worldgen, touch rendering backend configuration or increase distance caps. No BigInteger is used in production.

Reproduced defect on source baseline: camera `(-2147483648, 0)`, dirty keys `(2147483647,2)` followed by `(2147483647,1)`. Both squared distances round to the same `double`, so old code retains the first even though the second is strictly closer. A BigInteger-based test oracle rejects the baseline and accepts the repaired source.

The latest independent auditor's [2026-10-28 11:28Z review](../../engine-hourly-audits/2026-10-28T1128-581f343/REVIEW.md) still reports native GPU/Lod acceptance absent and tests only stand-ins. This jam does not conflate source correctness with native pixels or a fresh engine regrade. Previous renderer [near-ring source review](../20261028-near-ring-fast-reject/REVIEW.md) scored 5.21/10.

## Executed tests

Isolated Debian Linux OpenJDK 21.0.12.1, no complete Gradle checkout. Exact candidate `git hash-object` **c0f94f73ffa989738a71b0ce019a0160a137c817** matches committed blob at final implementation SHA. Strict command:

```bash
javac --release 21 -Xlint:all -Werror -d classes src/com/shnewbs/deepprimal/lod/client/LodCapturePriority.java test/com/shnewbs/deepprimal/lod/client/*.java
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodCapturePriorityChecks
java -ea -Xverify:all -cp classes com.shnewbs.deepprimal.lod.client.LodCaptureExactDistanceChecks
```

Results: inherited source-only `LodCapturePriorityChecks` **5,264 assertions**; new BigInteger-oracle + fairness/saturation regression **4,910 assertions**; **10,174 total**. Negative control compiled unmodified repository production baseline (`git blob 6bde987ac19417df6b5ff2a41998eb5b96c47652`) with the new tests and exited 1 at the expected extreme-distance mismatch. A Git contents-path transfer initially omitted one trailing newline; the corrected final committed blobs now match locally executed Java source bytes; the first content commit is superseded and not the self-audited source.

Additional diagnostic: compile baseline/candidate separately with identical Java21 settings and run `LodCapturePriorityBenchmark` twice in alternating order (`6000` warmups; `3000` measured samples per size). Baseline/candidate p50 in nanoseconds for 256-key dirty queues: `521/641`, `561/631`; for 4096-key dirty queues: `6920/7150`, `6940/7070`. The tied-distance correctness fix introduces modest median CPU overhead on these samples, and p99 measurements are noisy. These are isolated JVM microbenchmarks, NOT real Minecraft frame time, no FPS gain, and no GPU memory acceptance.

## Unrun / next priority

JDK25 archive not present in the current runtime/Library filename search had no toolchain archive match. Full owner-operated Java25 Gradle/NeoForge/Minecraft/JAR, real Vulkan/OpenGL/Windows pixels, AMD/NVIDIA driver tests, LOD save/reopen/teleport/edit, upscaling temporal resource lifetimes, GPU frame p50/p95/p99, memory/VRAM, resource reload/shutdown and water-transparency visual checks were NOT RUN. No development JAR, prerelease or CurseForge upload is claimed. Next: owner-native camera/reopen/edit and matched GPU baseline; reduce live capture CPU cost if on-device profiling warrants it, retaining exact ordering.


<!-- END PRESERVED SOURCE -->
