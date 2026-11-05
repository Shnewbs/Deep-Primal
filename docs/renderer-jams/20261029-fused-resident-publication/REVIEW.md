# Deep Primal renderer jam 37: fused resident publication — read-only author self-audit

**Jam ID:** renderer-20261029T1656-fused-resident-publication. **Immediate implementation parent:** [88491c2](https://github.com/Shnewbs/Deep-Primal/commit/88491c225954c3524775a3f5da27d095b1996f82); **exact scored renderer source:** [d20f9c9](https://github.com/Shnewbs/Deep-Primal/commit/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1). Original bounded claim [663ef9f](https://github.com/Shnewbs/Deep-Primal/commit/663ef9f26f8e5740fe9325f7c1eaab3013adacaf); owner-managed version 0.0.1-alpha.29-dev.82-routed-forest.1 was not bumped. A separate engine writer advanced main between claim and integration; the source change was guarded against the fresh main parent and the renderer blob remained unchanged.

This was a **read-only author self-audit** of the committed candidate after code was complete, **not an independent external audit**. The whole-engine grade is not changed.

## Consumed findings and exact change

Jam36's [self-audit](../20261029-frozen-resident-bounds/REVIEW.md) measured ~9.7% slower snapshot metadata publication and +1,120 B/page (a necessary frozen AABB reference array). The [independent Dev82 review](../../engine-hourly-audits/2026-10-29T1635-a7ade50e/REVIEW.md) independently checked frozen-bound helper correctness but performed no native GPU test.

- [LodPublishedBounds.java L14–71](https://github.com/Shnewbs/Deep-Primal/blob/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L14-L71), blob 9352ccaa7403d4ca36240fbb64cd6f47dfe13ec7, now computes conservative page unions and adjacent identical-bounds run masks in one scan of the *same frozen GPU page AABBs*. Invalid bounds still disable coarse rejection without suppressing later valid neighboring runs. Uniform-page first-reference identity and 64-bit mask edges are retained. Caller-supplied arrays avoid a per-publication metadata record.
- [VoxelLodRenderer.java L849–864](https://github.com/Shnewbs/Deep-Primal/blob/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L849-L864), blob b95df54f7687aff07b1e4c145bc72ea6bc988b94, now calls that fused publication helper once before resident entry replacement. Fine-part order, edit invalidation, cached snapshot, world switch/reset and transparent BlendFunction.TRANSLUCENT pipeline L53–63 are not altered.
- [Committed randomized smoke L1–77](https://github.com/Shnewbs/Deep-Primal/blob/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1/docs/renderer-jams/20261029-fused-resident-publication/tests/com/shnewbs/deepprimal/lod/client/LodPublishedBoundsCommittedSmoke.java#L1-L77), blob 6ce8cee1be1a489461cffa896956c3f9cf61f0f2, compares bit-exact old/new page unions, uniform identity and adjacency in both feature modes.

Only these three source/test paths changed at the scored source commit. No engine, terrain, finite-water, GPU shader, FSR/DLSS, UI redesign, Gradle, shared build cache or unrelated automation changed.

## Executed and measured

**Passed:** Linux x86_64 Java 21.0.12.1 exact helper Git blob compilation with javac --release 21 -Xlint:all -Werror; 16 Java processes with -ea -Xverify:all using eight focused classes × broadphase enabled/disabled, **6,786,388 terminal assertions**. Included 8,000 randomized parity pages, old frozen-bound smoke and the committed new smoke. Both defective versions rejected with expected assertion failures: historical live-field bounds and early-exit-on-invalid fused runs. Pinned code/metadata integration inspection **12/12** checks. Exact new/helper/test blobs matched those compiled in the isolated CPU fixture. No entire VoxelLodRenderer Java25/NeoForge compile or actual graphics driver was tested.

Matched synthetic *page publication CPU* fixture (four levels ×64 parts, 400 alternating warmed pairs, 128 operations per sample, 51,200 operations each side per run; not game frames):

| Run | Previous p50 / p95 / p99 (ns) | Fused p50 / p95 / p99 (ns) | Allocated bytes |
| --- | ---: | ---: | ---: |
| 1 | 423356 / 955085 / 2340887 | 271888 / 609516 / 2027646 | 21299200 → 21299472 |
| 2 | 429506 / 706691 / 1252582 | 275003 / 516436 / 1131119 | 21299200 → 21299472 |

**35.8–36.0% lower isolated p50**, nearly identical allocated bytes and variable p99. This metadata executes when a resident GPU page is published, not every frame; **no FPS/GPU or native memory improvement is claimed**. Frozen reference arrays remain allocated for safety.

## Six equal-weight grades (/10)

| Category | Grade | Evidence and open gate |
| --- | ---: | --- |
| Distance / LOD correctness and usable coverage | 6.75 | Carried from jam36; bit-exact [fused culling metadata](https://github.com/Shnewbs/Deep-Primal/blob/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L14-L71), no new usable distant-terrain coverage |
| Mesh generation/reuse/scheduling | 6.95 | Carried; no mesh builder or worker scheduling changes |
| GPU culling/submission/memory | 4.55 | Carried; CPU publication only, no GPU indirect draws or VRAM evidence |
| Actual AMD FSR/NVIDIA DLSS | 5.00 | Carried; existing official FSR1 spatial, but temporal FSR/active RTX DLSS unaccepted |
| Visual/backend/settings compatibility | 5.30 | Carried; [transparent pipeline L53–63](https://github.com/Shnewbs/Deep-Primal/blob/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L53-L63) unchanged; actual water GPU pixels unrun |
| Validation and measured performance | 6.30 | +0.05 for strict randomized/negative regressions and matched CPU publication timing, not native FPS |
| **Equal-weight mean** | **5.81** | (6.75+6.95+4.55+5.00+5.30+6.30)/6=5.8083, **+0.01** over jam36 5.80 |

Independent whole-engine historical **5.0/10** unchanged. Renderer target >8.5/10 is not met.

**Not run:** Java25/full Gradle9.2.1+NeoForge or qualified JAR (owner performs complete builds), Vulkan/OpenGL native scene/pixel, water translucency on GPU, frame CPU/GPU p50/p95/p99, draw/descriptor/VRAM, GPU page cap/reload/resize/world switching, temporal FSR and RTX DLSS. Compilation of test-only Minecraft doubles cannot prove these gates. No GitHub Actions, CurseForge or release publication.

**Source/evidence:** [implementation commit](https://github.com/Shnewbs/Deep-Primal/commit/d20f9c92c682309ea5d8c944f15a90a58fa9d6d1); verified 39,414-byte source and test evidence ZIP SHA256 bc6bdfeeda190ba05129f832f3d01fe88e8077ef60c25f8db69dd7308f06af72, **31 ZIP members**, 30 CRC and per-member SHA256 validations; Library /Deep Primal/Renderer Jams/DeepPrimal_Renderer_FusedResidentPublication_20261029_d20f9c9_SourceEvidence.zip, id libfile_7f6635d96dc4819197b62ef8c5092574. This is source evidence, **not a qualified JAR**.

**Next:** owner native Vulkan/OpenGL A/B fixed vs adaptive vs disabled broadphase, water pixels, FSR1 vs native, camera edit/turn, resource reload and frame/VRAM metrics. Retain bounds correctness; next renderer jam should target the hardware-measured bottleneck. Scoped lease released at documentation checkpoint.
