# Renderer jam 36 — frozen resident-page AABBs (read-only author self-audit)

**Jam ID:** `renderer-20261029T1558-frozen-resident-bounds`; **preclaim main:** `307452b5dfff09778fd8e3bb1a1a55259826ba53`; **scoped claim checkpoint:** [`d6af7a2b70ac4ef02e04022c4459a3a5f5cc237e`](https://github.com/Shnewbs/Deep-Primal/commit/d6af7a2b70ac4ef02e04022c4459a3a5f5cc237e). After another engine writer advanced main, implementation **immediate parent** `f8321f8db822162e4a953d5e251fc2dff0cf2f6f`; **exact reviewed renderer implementation** [`c876db37e6e5e772497551d1da98e79232b297dc`](https://github.com/Shnewbs/Deep-Primal/commit/c876db37e6e5e772497551d1da98e79232b297dc). Current engine-owned version `0.0.1-alpha.29-dev.82-routed-forest.1` from `gradle.properties`; renderer did not bump it.

This was a **read-only author self-audit of the exact committed candidate**, not an independent engine audit, not a GPU test and not a fresh full quality certificate. Source/test were complete before scoring; no code was modified during review. The previous renderer jam35 5.79/10 is the comparison, independent whole-engine historical 5.0/10 remains separate and unchanged.

## Consumed findings and actual scoped repair

Jam35 [review](../20261029-adjacent-bounds-runs/REVIEW.md) identified an unclosed contract: mutable `TerrainGpuMesh.bounds` references could diverge from cached page union and adjacent-mask metadata. The latest [independent Dev81 review](../../engine-hourly-audits/2026-10-29T1532-b6448f52/REVIEW.md) audited unrelated watershed routing, not renderer GPU acceptance.

- [`LodPageFrustumBounds.java` L20–77](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L20-L77), source Git blob `e8b289266fb33ed2b10e22acae0ab6bd8d3f893b`: capture one AABB reference per GPU part at resident page publication. Minecraft AABB fields are immutable; the GPU mesh's public `bounds` reference is writable. Build conservative page bounds from the exact same frozen array, retaining invalid/null fallback and the proven-uniform first-reference marker. Explicit broadphase-off still avoids unused coarse-union creation.
- [`LodPartFrustumRuns.java` L18–54](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPartFrustumRuns.java#L18-L54), blob `eea4bb00d9652796426fcc7a966bd6bb002dc6aa`: build compact adjacent-equal finite/ordered run bits from **the same** frozen array; no per-frame coordinate comparisons.
- [`VoxelLodRenderer.java` L65–72, L583–645, L847–863](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L583-L645), blob `39f8435b9389ce233321696bcf53315a38bf6511`: resident `Entry` retains frozen arrays; final page upload creates snapshot once; conservative page test, positive-uniform reuse, fine part tests and adjacency all refer to the same published AABB instances. Original visible part ordering and native-chunk handoff persist; no GPU vertex, index, shader, draw ordering, water transparency, simulation, FSR/DLSS or settings changes. Revisions still replace the entire resident entry.
- Committed production-excluded [mutable-bound smoke](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/docs/renderer-jams/20261029-frozen-resident-bounds/tests/com/shnewbs/deepprimal/lod/client/LodFrozenBoundsCommittedSmoke.java) plus two **test-only** stubs and a scoped README. No `engine-core`, water, terrain, graphics-settings, Vulkan, Gradle, shared build output or other automation modified. Diff has precisely seven intended files. The separate latest scoped claim is bounded and explicitly released after the docs checkpoint.

## Tests actually executed

Linux x86_64 OpenJDK **21.0.12.1**; JDK25 and the owner-provided JDK25 archive were absent in this workspace, and a Library search for the archive did not find it. Using exact Git-object-matched source helpers and *test-only* AABB/GPU-mesh doubles:

```sh
find src baseline stubs tests -name '*.java' | sort > sources.txt
javac --release 21 -Xlint:all -Werror -d out @sources.txt
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsSmoke
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsSmoke
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsCommittedSmoke
java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=false -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsCommittedSmoke
# Four previous jam34/35 smoke/extended classes also ran in both modes.
java -ea -Xverify:all -cp out com.shnewbs.deepprimal.lod.client.LodFrozenBoundsPublicationBench
```

**Passed:** strict compiler; **12 positive runs / 6,606,201 assertions**: new expanded randomized frozen/mutated 2,553,952 + 2,553,951; committed smoke 403,094 ×2; prior adjacent tests 40,015 and 90,021 each ×2; prior page-uniform tests 180,018 + 180,014 and 36,003 ×2. Invalid/nonfinite/reversed and negative coordinates, 64-bit runs, zero/unique/uniform/mixed, four detail levels and mutable-after-publication field reassignment were exercised. **Expected negative failure:** intentionally old live-bound selection exited 1 with `AssertionError: old live field hid resident geometry`; it demonstrates the defect. The candidate pinned GitHub read-only source integration review passed **25/25** checks and all local source/test Git identities matched. No whole game compile was implied.

**Matched synthetic page-publication overhead (Linux Java21, 4 detail levels ×64 parts, 400 alternating warmed samples ×128 publication operations; not GPU):**

| Repetition | Old p50 / p95 / p99 | Frozen candidate p50 / p95 / p99 | Allocations across 51,200 operations |
| --- | --- | --- | --- |
| 1 | 936,177 / 1,818,988 / 3,268,158 ns | 1,026,970 / 2,098,006 / 3,627,135 ns | Old 21,299,200 B → new 78,643,200 B |
| 2 | 1,004,309 / 2,178,997 / 3,323,755 ns | 1,101,640 / 2,467,798 / 3,859,137 ns | Old 21,299,200 B → new 78,643,200 B |

Snapshot safety costs about **9.7% extra median CPU time** and **1,120 extra Java bytes per synthetic page-publication operation** in this fixture; p95/p99 also regress. This is amortized at page acceptance, not a per-frame selector measurement. **No FPS/performance credit or actual GPU throughput improvement is claimed**; retain the publication overhead as a profiling candidate.

## Six equally weighted renderer categories /10

| Category | Grade | Pinned evidence and missing gate |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.75** | **+0.05**: [frozen exact-GPU AABBs](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L583-L633), [shared union/mask](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPageFrustumBounds.java#L20-L77) and explicit mutation negative. No new usable render radius or native coverage. |
| Mesh generation/reuse/scheduling | **6.95** | **Carried unchanged** from jam35. No mesher/worker, rebuild or allocation improvement proven. |
| GPU culling/submission/memory | **4.55** | **Carried unchanged**. CPU frustum correctness, not GPU occlusion/indirect draws or VRAM measurements. |
| Actual AMD FSR / NVIDIA DLSS | **5.00** | **Carried unchanged**: existing real FSR1 spatial; temporal FSR and RTX NGX Evaluate/RTX GPU validation absent. |
| Visual/backend/settings compatibility | **5.30** | **Carried unchanged**. [Translucent pipeline/source order](https://github.com/Shnewbs/Deep-Primal/blob/c876db37e6e5e772497551d1da98e79232b297dc/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L52-L63) unchanged; no actual water pixels, Vulkan/OpenGL lifecycle, resize/reload accepted. |
| Validation and measured performance | **6.25** | **Carried unchanged** despite rigorous mutation/legacy tests: measurable page-acceptance CPU +allocation regression, no GPU/real-frame metrics. |
| **Equal-weight arithmetic mean** | **5.80 / 10** | `(6.75+6.95+4.55+5.00+5.30+6.25)/6=5.80`, **+0.01** vs 5.79. Target demonstrated >8.5 unmet. |

## Delivery and next repair

Verified source/test evidence ZIP **`DeepPrimal_Renderer_FrozenResidentBounds_20261029_c876db3_SourceEvidence.zip`**, 27,233 bytes, SHA256 `9bf862bf3fde687fdcb9958b3a95724421c824d07b7b96e7d1d3a29b74395128`, 23 ZIP members, outer CRC/per-member SHA256 verified; uploaded successfully to Library `/Deep Primal/Renderer Jams/`. This is **source/regression evidence, not a Java25/NeoForge/GPU acceptance or development JAR**. Full Java25 build/JAR, nested/native platform payload, OpenGL/Vulkan, water transparency pixels, GPU frames p50/p95/p99, VRAM, resource reload/resize/world switch/save/reopen/shutdown, temporal FSR and RTX DLSS remain **not run** under the October 27 owner full-build arrangement. No release or CurseForge publication.

**Next:** owner same-seed native GPU A/B at normal/FSR1 scene sizes, fixed/adaptive/off page broadphase, unchanged geometry/material/transparent-water order and editable-world reload/residency; gather actual frame p50/p95/p99, resident GPU/RAM, upload and draw counts. If page publication overhead is material, test a compact frozen-parts snapshot alternative without sacrificing correctness, then prioritize measured mesh/GPU bottlenecks. Do not increase caps or claim unexplored-terrain coverage without tracking/network/data contract.
