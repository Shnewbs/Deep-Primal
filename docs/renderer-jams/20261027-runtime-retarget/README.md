# Renderer jam 20261027-runtime-retarget — production integration

Pinned initial main `286af866077ff15cf14f621cf4bc59f2d0675429`; claim commit `282312e1ebba90c96b83a47e0ae41dc32b0ccecf`; exact code commit [`98d4b261c1c1e21a7d9f292166b8eb1916a608da`](https://github.com/Shnewbs/Deep-Primal/commit/98d4b261c1c1e21a7d9f292166b8eb1916a608da). All intended code is on main, with no feature branch and no changes to engine/water/upscaling ownership. Existing mod version remains `0.0.1-alpha.29-dev.32-upscaling.1` until a distinct qualified build; the **older dev32 JAR is not built from this source**.

## Runtime change

The [native chunk discovery helper](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/DeliveredChunkScan.java#L30-L45) now checks effective radius and Chebyshev center displacement with overflow-safe long arithmetic. The [real client tick](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L247-L280) retargets a completed scan after even one chunk of travel, any radius change, or an in-progress scan after a bounded 1–8-chunk displacement. Ring enumeration, the 128-coordinate/tick visit limit, 512-entry dirty-queue pressure guard, existing chunk-load listener, water mesh geometry and simulation remain intact. The exact earlier staged helper blob `afccbfb6d92e3b97e97d6223235cde4bd16df2f9` is now a production source.

Added [1056-case regression](https://github.com/Shnewbs/Deep-Primal/blob/98d4b261c1c1e21a7d9f292166b8eb1916a608da/src/coreChecks/java/com/shnewbs/deepprimal/core/lod/DeliveredRetargetChecks.java) to the standard dependency-free CoreChecks entry point, alongside original 108999-coordinate scan coverage.

## Fresh limited validation and release boundary

Host OpenJDK21 Linux; compiled exact helper and exact new test Git blobs with a functionally faithful local DistancePolicy mirror using `javac --release 21`, then executed `java -ea -Xverify:all`: `DELIVERED_SCAN_PASS coordinates=108999 outerBeyond32=92064`; `DELIVERED_RETARGET_PASS checks=1056 completeScans=9`. A negative control deleting completed-scan retarget compiled and failed *as intended* at `completed move +1:16`. Fetched product source/test blobs back from the new Git commit and confirmed a live client-tick call and core-suite registration. Prior staging had passed those same component checks; this is the fresh post-integration rerun.

**NOT RUN:** full Java25/Gradle/NeoForge module build, full CoreChecks, packaged native/resource checks, JAR/compiled class/fixture identity, client Vulkan/OpenGL water ordering, save/reopen/resize/world switch/shutdown, Windows/AMD8060S/RTX, measured p50/p95/p99 or VRAM. Host only has JDK21, cannot resolve private Git clone via DNS and has no full private Gradle checkout. No new JAR or release, no FPS/useful-distance claim. Previous dev32 bundle belongs to `bd28005d...`, not this commit.

## Next

Restore a complete private checkout and Java25/toolchain; re-run full module/build/package/native gates and publish a new distinct source-matched development JAR **only after** qualification. Reconcile the separately staged `20261027-admission-gate` byte-accounting candidate with this new tick. Test camera/travel/starvation, native handoff/edit/reload, negative-coordinate seams, transparent-water ordering and matched frame/upload/draw/VRAM tails. The bounded renderer claim is released. See [self-audit](REVIEW.md) and [ledger](jam-ledger.json).
