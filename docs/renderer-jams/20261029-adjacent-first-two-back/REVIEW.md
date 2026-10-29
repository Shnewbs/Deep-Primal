# Deep Primal renderer jam 39 — adjacent-first resident AABB comparisons

**Read-only renderer author self-audit.** Exact scored candidate: [c6b4aa0d](https://github.com/Shnewbs/Deep-Primal/commit/c6b4aa0d074b00a737a3d488c118a73ff76994bc). Claim checkpoint [dac0e43a](https://github.com/Shnewbs/Deep-Primal/commit/dac0e43a4c4b6b97648bc8c9b386f96d3c92a4ae), implementation [ff65940b](https://github.com/Shnewbs/Deep-Primal/commit/ff65940b3f2c88b241bf08354c92cf8f48861faf), test-oracle checkpoint c6b4aa0d. No renderer code was modified during scored review. Owner engine version at candidate: 0.0.1-alpha.29-dev.84-outlet-journal.1 (not bumped by renderer). Jam ID renderer-20261029T1900-adjacent-first-two-back.

## Consumed findings and scoped implementation

[Jam 38](../20261029-two-back-frustum/REVIEW.md) recorded 41.7–51.2% higher *synthetic* alternating-page publication p50 after adding two-back frustum alias metadata, with native break-even unknown. [Independent Dev84 audit](../../engine-hourly-audits/2026-10-29T1834-f53abbf6/REVIEW.md) concerned V8 outlet-journal durability, not renderer GPU performance. At integration, the active engine lease covered V8 source, its tests, gradle.properties and its own validation files; it did not own the resident AABB helper or this jam documentation.

Exactly three changed files between claim and scored candidate:
- [LodPublishedBounds.java L50–70](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L50-L70), blob aecf53beaa37b69fe782cba3c8701af35b18fdd0: calculate adjacent equality once and reuse that Boolean before the i-2 comparison, preventing redundant coordinate comparisons in adjacent runs. [L98–109](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L98-L109) checks shared immutable AABB reference identity before exact value equality; validity remains a prerequisite.
- [Committed parity test L1–101](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/docs/renderer-jams/20261029-adjacent-first-two-back/tests/com/shnewbs/deepprimal/lod/client/LodAdjacentFirstParityChecks.java#L1-L101), blob f4b07be588f601bb32435c0b09816f0e2c9727e2: exact conservative union, uniform reference identity, adjacent and two-back mask parity, exclusive alias bits and randomized invalid/finite inputs. Sizes include 0, 1, 63/64/65, 127/128/129 and 256 parts.
- [Pinned jam38 legacy oracle](tests/com/shnewbs/deepprimal/lod/client/LegacyLodPublishedBounds.java), blob a6b554ae273e34919ea8e1330550cc047aeb5678: test-only frozen old source to prevent a self-referential oracle.

The unchanged [VoxelLodRenderer L620–689](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L620-L689) still has blob 7a63182b273b66b71e1ebf705a5bb3ecb8f12232. LodClient remains a9a2978fa4952f4ada736f92a301efa36265ff25. No changes to draw order, transparent-water material, shaders, terrain, water simulation, graphics settings, GPU backends, other jobs or schedules.

## Exact checks and measurements

Linux x86_64 with Java 21.0.12.1, isolated workspace and test-only AABB/LOD/Minecraft doubles. Strict compile PASS:
- javac --release 21 -Xlint:all -Werror -d /mnt/data/renderjam39/compile/out @/mnt/data/renderjam39/compile/sources.txt
- java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=true or false -cp /mnt/data/renderjam39/compile/out com.shnewbs.deepprimal.lod.client.LodAdjacentFirstParityChecks
- Same java command for LodTwoBackCommittedSmoke and LodTwoBackFrustumChecks in each of the two modes.

**6/6 positive JVM processes PASS, 10,134,936 assertions.** The new parity oracle passed 5,932,077 enabled and 135,096 disabled checks; earlier two-back smoke and randomized visibility predicates passed. With broadphase disabled there were zero aliases, 648,608 modeled fine tests. A deliberately corrupted two-back mask compiled with the strict flags but failed at parity check **332**, exit 1 as expected. Tested helper/test/oracle Git object SHA-1 identities matched the scored commit. Source-only evidence is not full NeoForge compilation.

Matched synthetic CPU publication benchmark, 4 levels x 64 parts, 8,000 warmed publications per version, 280 alternating-order pairs x 128 publications/sample, two runs per type. p50 nanoseconds per 128 page publications:

| Fixture | Before runs 1/2 | After runs 1/2 | Change |
| --- | ---: | ---: | --- |
| Uniform, shared AABB reference | 604821 / 605840 | 252011 / 300582 | 58.3% / 50.4% lower |
| Uniform, distinct value-equal AABBs | 588627 / 570288 | 406849 / 450753 | 30.9% / 21.0% lower |
| Alternating, shared A/B references | 417002 / 396913 | 325618 / 314813 | 21.9% / 20.7% lower |
| Alternating, distinct value-equal AABBs | 382712 / 403052 | 377264 / 394820 | 1.4% / 2.0% lower |
| All unique | 323825 / 298628 | 331166 / 307141 | **2.3% / 2.9% slower** |

Detailed p95/p99 logs are in the verified evidence ZIP. Several tails remain inconsistent. These numbers are not GPU timings, game FPS, VRAM or proof of improved usable render distance. Cached metadata still consumes resources and its real-world amortization is unproven.

## Six equally weighted renderer-only grades

| Category | /10 | Evidence and remaining limitation |
| --- | ---: | --- |
| Distance/LOD correctness and useful coverage | **6.80** | Carried from jam38; [L50–94](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L50-L94) bit-exact parity but no unseen-terrain delivery |
| Mesh generation/reuse/scheduling | **6.95** | Carried; no mesh builder or worker change |
| GPU culling/submission/memory | **4.55** | Carried; [draw consumer](https://github.com/Shnewbs/Deep-Primal/blob/c6b4aa0d074b00a737a3d488c118a73ff76994bc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L620-L689) unchanged, no GPU/VRAM timings |
| Real AMD FSR/NVIDIA DLSS | **5.00** | Carried; [documented FSR 1 spatial](../../UPSCALING.md) exists, but temporal FSR and active NGX DLSS remain unaccepted |
| Visual/backend/settings compatibility | **5.30** | Carried; transparent pipeline unchanged, native GPU pixels, resize and water ordering unrun |
| Validation and measured performance | **6.40** | +0.05: exact-source parity, negative mutant, matched publication p50/p95/p99; all-unique tail regression and missing hardware remain |
| **Equal-weight mean** | **5.83** | 35.00/6 = 5.8333, **+0.00 displayed vs jam38 5.83** |

Whole-engine historical score **5.0/10 remains separate**; this is NOT its independent regrade. Renderer remains below the strictly >8.5/10 target.

## Unrun gates and next repair

**Unrun:** Java25 and Gradle9.2.1/NeoForge full build and JAR (owner October 27 override), Vulkan/OpenGL native GPU, water pixel order/transparency, FSR1 native comparison, RTX DLSS/temporal FSR, frame p50/p95/p99, draw/descriptor counts, real VRAM/RAM, client reload/resize/world switch/save/reopen/shutdown, camera/travel/edit GPU validation. JDK25 not mounted here; Java21 tests were executed. No hosted Actions, prerelease or CurseForge publication. This source ZIP is not a playable JAR.

**Risk:** all-unique synthetic page publication is 2.3–2.9% slower, and extra two-back mask work may not pay off in live pages. Next priority is owner-native Vulkan/OpenGL A/B for fixed/adaptive/disabled frustum on uniform, alternating and unique parts, with native/FSR1, transparent water and GPU/CPU p50/p95/p99, RAM/VRAM. Use those results to choose actual mesh/upload/submission improvement instead of extending speculative masks.

**Artifact:** DeepPrimal_Renderer_Jam39_AdjacentFirst_20261029_c6b4aa0_SourceEvidence.zip; 18,372 bytes, SHA256 6b4680d9563a33d6617235add846fb2fe1ef8e03afa9377d3398bfdbdd9d0bcd; CRC plus 15/15 member SHA256 matches. Successfully saved to Library /Deep Primal/Renderer Jams, id libfile_1ad055d6b2cc81919f68ab2ab7482ac3.
