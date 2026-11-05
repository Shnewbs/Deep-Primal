# Dev129 — idempotent nearest-chunk selection and safe late offers

Pinned main before work: `1c5889293371d5ba8b55b3464fac3142e311cf0d`. Independent audit consumed: `docs/engine-hourly-audits/2026-10-31T1235-20cf1c05/REVIEW.md`. The auditor reported that `NearestChunks.sort()` is not idempotent, although the production sender currently calls it only once per reset. Its separate readiness-starvation repair (`fdc704b0`) is preserved.

## Reproduced defect and correction

The original `sort()` heap-sorts the primitive max-heap in place. Calling it again treats the already sorted array as a heap, changing the result; offering another position after sorting likewise compares against the *nearest* root rather than the farthest selected position. A new regression against the original source failed at `repeated sort order at check 4`.

A `sorted` state bit now makes repeated `sort()` calls no-ops. If `offer()` is called after sorting, the heap is rebuilt bottom-up in O(K) before admission; normal `reset() -> offer* -> sort()` remains the same O(N log K) path without rebuilding. `reset()` clears the bit. No allocation, distance representation, tie-break, saved-world format, renderer, water or terrain changes.

## Executed focused validation (2026-10-31)

- Local OpenJDK 21.0.12.1: `javac --release 21 -Xlint:all -Werror` passed for the exact proposed production class and committed differential test; `java -ea -Xverify:all` passed **224,254 assertions** (2,000 deterministic randomized trials, signed coordinate extremes, repeated sort, near and far late offers).
- Original class with new test: **failed at assertion 4**, reproducing the idempotency defect.
- Compiled negative control removing only the post-sort heap rebuild: **failed at assertion 8** (`late offer order`), as required.
- Proposed production blob: `a218f884d5f231c2eb6ac040887093becdd8ecbf`, local SHA-256 `69d6302913fe11fc02c8ee892702ad3f57ec0b8ecce4e746d75032fe50e6168b`.
- Proposed test blob: `5fe6aac1663d518e44a032426d5a6c44fa74182a`, local SHA-256 `e147c942b1f1e48608a075ed897fe18168a636521ec2ae22d1ab2fd7ef5eb4f6`.
- GitHub blob creation returned these same Git object identities; the proposed exact files were compiled locally.

## Coordination and remaining acceptance

Scoped to `engine-core/.../NearestChunks.java`, `src/coreChecks/.../NearestChunksOverflowChecks.java` and this evidence only. Current audit changed `performance-engine/.../PlayerChunkSenderMixin.java`; that file is not touched. This is an API hardening follow-up, not evidence of a new in-game performance regression or a net speed improvement.

No full repository checkout, Java25 or Gradle dependency cache is present locally; Git CLI cannot resolve `github.com`. Thus no full `CoreChecks` suite, NeoForge package, native Minecraft save/reopen, Vulkan/OpenGL, LOD or water acceptance, verified JAR or JAR SHA was produced. The last successful owner JAR must be retained until a verified replacement. No GitHub Actions were run. The independent overall baseline remains 50/100 (5.0/10), not freshly regraded.

Next owner validation: full Java25 Gradle and Minecraft chunk-send readiness benchmark under heavy pending queues; check in-game lookup overhead introduced by the independent audit's ready-only prefilter.
