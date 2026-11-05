# Deep Primal independent audit — October 28, 2026, 07:31 UTC

**Pinned main:** [5a23f285d4680f9537b8baf1639b75036e7392ef](https://github.com/Shnewbs/Deep-Primal/commit/5a23f285d4680f9537b8baf1639b75036e7392ef); version `0.0.1-alpha.29-dev.48-v8materials.1`. This is independent audit-only work. No Builder/renderer code, working directory, cache, active lease, or user JAR was changed. The engine work state records dev48's source-only lease as released, and no exclusive benchmark reservation was active.

## New LOD finding — a renewed recovery can be discarded by an older scan

[The service's contract](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L10-L15) says a recovery added after a disk scan began should win admission. But the scan captures only an unversioned key list ([L67–70](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L67-L70)). The delayed completion's `discardKeys` are then removed from the **current** queue without checking for a newer re-enqueue, and the `forgotten` callback is invoked ([L100–123](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L100-L123)).

An independent JDK25 real-directory test compiled exact pinned product blobs `LodDiskIndexService.java=5e9bc172462c91ff0fadbe35ea37b87b1bbef39c` and `LodDiskNearestScan.java=fc76905ab1f82ba49cc4495ab8b943127b2b8d21`. With 8,192 distant recovery keys, after the scan started its farthest key was removed and re-added as a newer recovery; a near saved page was discovered. The completion **removed the renewed key** and invoked `forgotten` while admitting the new near key. This is a source-level post-start ownership/recovery defect, **not** proof of native Minecraft edit loss. The new diagnostic passes 505 assertions while explicitly observing that failure. The retained integration service fixture passes 528.

**Renderer-builder priority:** attach an enqueue generation/ownership token to a disk-scan snapshot and discard operation; skip discards when the same key was renewed after the snapshot. Preserve the 256-entry per-turn work limit, 8,192-key cap, edit/session/revision guards, and already-tested bounded retries. Add a native world-reopen/edit test. Membership alone cannot distinguish old and new re-enqueues.

## Existing successful-empty directory gap independently confirmed

The renderer has **already repaired** missing-directory and transient-I/O retries ([source L83–96](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodDiskIndexService.java#L83-L96)); that repair is not being reported as broken. A *successful scan of an existing empty directory*, however, clears retry state. A file created afterward was not discovered after 200 stationary-camera client ticks, whereas a reset rediscovers it. This matches the [renderer jam's explicit open limitation](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/docs/renderer-jams/20261028-disk-retry/README.md).

## Focused Java25 checks on the pinned snapshot

The ten copied product sources (nine density-core inputs and `DensityChunkMaterializer.java`) were SHA-matched to current GitHub blobs. Temurin **25.0.5+8** compiled them and the retained validation harness against **test-only Minecraft API doubles** with `javac --release 25 -Xlint:all -Werror`; `java -ea -Xverify:all` ran the following fresh tests:

| Test | Assertions passed |
|---|---:|
| Dev48 V8 planned ground and soil | 1,235,414 |
| V8 native-shaped base-height bridge | 874,642 |
| V8 section/materialization bridge | 11,047,019 |
| Retained LOD integration fixture | 528 |
| New independent LOD recovery/empty-directory audit | 505 |
| **Total** | **13,158,108** |

The [new planned-surface method](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java#L94-L117) remains **explicit-call-only**. It has not been integrated into the native registered world generator. These tests are not game saves, actual Minecraft heightmaps, player-world continuity, Windows tests, FPS, or a benchmark. The independent reproduction uses actual filesystem paths but no native NeoForge runtime.

## Ten-category grade — carried, not a fresh full-engine regrade

| Category | /10 |
|---|---:|
| Terrain realism | 5 |
| Biome coherence | 4 |
| Hydrology and caves | 6 |
| Features/structures | 4 |
| Performance/threading | 3 |
| Determinism/stability | 7 |
| Architecture/code quality | 6 |
| Extensibility | 4 |
| Testing/documentation | 7 |
| Polish/tooling | 4 |
| **Overall** | **50/100, average 5.0/10, Tier 2** |

These are the evidence-supported [October 24 review](https://github.com/Shnewbs/Deep-Primal/blob/5a23f285d4680f9537b8baf1639b75036e7392ef/docs/engine-review-2026-10-24/REVIEW.md) scores, **carried forward**. Isolated V8 experiments and focused tests earn no native gameplay or hardware performance points. The owner's completion threshold remains a fresh demonstrated average **strictly greater than 8.5/10**.

## Build policy and open gates

The October 27 owner override assigns full Gradle/Minecraft/GPU builds and JAR production to the owner. **No full Gradle 9.2.1 build, current development JAR, GitHub Actions, GitHub Release, CurseForge release, or JAR cleanup was performed.** Therefore there are no new archive CRCs, package/source-to-class identities, packaged native save/reopen/shutdown results or owner Windows/AMD8060S Vulkan/OpenGL benchmarks. All remain explicitly **unrun**, as do complete finite-water conservation/chemistry/loaded-unloaded persistence tests. JDK25 was copied from an old auditor-owned toolchain to an isolated audit directory; none of the active development checkout, cache or processes was modified. Frozen V1–V7 geography, profiles, edits and water quantities are unchanged by this audit.

**Next:** renderer fixes the newly observed stale discard; owner validates native V8 material/heightmap/lighting/save; engine Builder continues V8 saved-profile ownership and finite-water conserved ledger. The auditor does not stop or gate Builder progress. New reproducible Java25 evidence is preserved in a separate source-evidence ZIP with SHA-256 `b362ef49eac45a591464bc789fe7e89b34b6c66011cd1d945f3af09d2cafe0ea`.
