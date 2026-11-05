# Deep Primal renderer jam 38 — two-back LOD frustum reuse (read-only author self-audit)

**Jam:** renderer-20261029T1757-two-back-frustum. **Implementation parent:** [daf72986](https://github.com/Shnewbs/Deep-Primal/commit/daf72986659871600f635ae6f5a17dfed0ce12a3). **Exact reviewed source:** [2d795974](https://github.com/Shnewbs/Deep-Primal/commit/2d7959741f8a43c1790d19f5e976e5eef95cdfb5). **Source version:** `0.0.1-alpha.29-dev.83-outlet-ledger.1` (unchanged). This is the renderer author's **read-only self-audit**, not the independent whole-engine audit. No code was edited during scored review.

## Scope and consumed findings

Jam37 [fused publication review](../20261029-fused-resident-publication/REVIEW.md) established immutable resident AABB snapshots, adjacent same-bound frustum reuse and measured CPU publication gains. The [Dev83 independent whole-engine audit](../../engine-hourly-audits/2026-10-29T1730-2e3c945d/REVIEW.md) concerned the engine outlet ledger, not a new renderer GPU acceptance. New work is limited to renderer frustum metadata/selection and diagnostics; no terrain, water simulation, shader, GUI redesign, or other schedule changed.

- [LodPublishedBounds.java L17–92](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L17-L92), blob `a2beb3975af01e7e4935404628c6a0e5b5d3d116`: while computing existing frozen-page union and adjacent masks, optionally mark *i-2* equal finite AABBs, excluding adjacent duplicates. No per-frame coordinate comparisons. Legacy three-argument helper still works.
- [VoxelLodRenderer.java L620–684](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L620-L684), blob `7a63182b273b66b71e1ebf705a5bb3ecb8f12232`: reuse previous/two-prior visibility decisions in original part order, including rejection. [Publication L866–898](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L866-L898) stores the new bounded masks; broadphase-off retains no alias metadata. [Reset L1045–1054](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L1045-L1054) clears new counters. The translucent render pipeline at L62 remains unchanged.
- [LodClient.java L106–113](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L106-L113), blob `a9a2978fa4952f4ada736f92a301efa36265ff25`: separately labels successful and rejected two-back frustum reuses, not GPU draw savings.
- [Committed randomized smoke L1–70](https://github.com/Shnewbs/Deep-Primal/blob/2d7959741f8a43c1790d19f5e976e5eef95cdfb5/docs/renderer-jams/20261029-two-back-frustum/tests/com/shnewbs/deepprimal/lod/client/LodTwoBackCommittedSmoke.java#L1-L70), blob `6f4030730ecbaea3a0cea664954e3d7751f42f74`: validates two-back equivalence in 130-part alternating sequences and 4,000 randomized pages, on/off. The broader isolated testing harness covers 8,800 randomized pages, null/NaN/reversed bounds, and 63/64/127/128 word boundaries.

## Exact focused checks (Linux x86_64; Java 21.0.12.1)

**PASS:** Git blob-identical helper compiled `javac --release 21 -Xlint:all -Werror -d out @sources.txt` against test-only Minecraft stubs; committed test blob SHA also matched. `java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=true|false -cp out com.shnewbs.deepprimal.lod.client.<Class>` ran eight fixture classes under both modes (16 positive processes, **10,702,115 total assertions**). The eight classes were `LodPartFrustumRunsChecks`, `LodUniformPageVisibilityChecks`, `LodFrozenBoundsSmoke`, `LodFrozenBoundsCommittedSmoke`, `LodPublishedBoundsParity`, `LodPublishedBoundsCommittedSmoke`, `LodTwoBackFrustumChecks`, `LodTwoBackCommittedSmoke`. The deliberately disabled two-back-mask mutant compiled but failed with assertion 262, as expected. 12/12 read-only pinned integration checks passed. Tests do **not** compile the whole NeoForge renderer.

In the isolated deterministic 8,800-page CPU fixture, full per-part predicate evaluations fell from **648,608 to 434,743** with the new enabled-mode skip logic (213,865 or **32.97% fewer simulated fine checks**), including 105,441 adjacent reuses and 108,424 two-back reuses. Broadphase-off performed exactly 648,608 predicates with no aliases. This fixture models a visibility predicate; it is not a native Minecraft frustum, GPU draw submission, or FPS measurement.

Matched publication-only Java CPU fixture (three levels ×64 parts, alternating old/new order, 220 warmed pairs, 128 page publications per timing sample; ns per 128 publications):

| Fixture | Old p50 run1 / run2 | Two-back p50 run1 / run2 | Outcome |
| --- | ---: | ---: | --- |
| Alternating A/B | 182,775 / 168,704 | 258,981 / 255,065 | **41.7–51.2% publication overhead** |
| All-unique AABBs | 181,003 / 176,276 | 188,815 / 179,982 | **2.1–4.3% publication overhead** |

Publication runs only when GPU pages become resident. The extra one-bit-per-part mask is allocated only where a valid two-back repetition exists, but each enabled publication does additional CPU checks. No native amortization, memory, GPU time, water pixels or FPS improvement is established. Evaluate retained-heap impact and realistic part distributions before wider optimization claims.

## Six equal-weight renderer-only scores

| Category | /10 | Pinned evidence / outstanding gate |
| --- | ---: | --- |
| Distance, LOD correctness and usable coverage | **6.80** | +0.05, finite frozen-bounds alias decision parity L17–92 and render selection L620–684; no new unseen coverage |
| Mesh generation, reuse and scheduling | **6.95** | Carried from jam37; no mesh-builder change |
| GPU culling, submission and memory | **4.55** | Carried; no actual GPU indirect/VRAM/frame comparison; aliases are CPU frustum decisions |
| Real AMD FSR / NVIDIA DLSS | **5.00** | Carried; existing FSR1, missing active temporal FSR and NGX DLSS acceptance |
| Visual/backend/settings compatibility | **5.30** | Carried; translucent pipeline preserved in source, no GPU water pixel proof |
| Validation and measured performance | **6.35** | +0.05 for 16 regression processes, negative control, bounded CPU comparison; native p50/p95/p99 unrun |
| **Average** | **5.83** | **+0.02 vs renderer jam37 (5.81)**; whole-engine grade remains unchanged at historical 5.0 |

Scores reflect evidence, not a fully tested renderer. The renderer is still below the strictly >8.5 target.

## Known gates / next repair

**Not run:** owner-managed full Java25 Gradle9.2.1/NeoForge/JAR; Vulkan/OpenGL driver, FSR1 native vs upscaled pixel comparisons, real RTX DLSS and FSR temporal paths, transparent-water pixels, resize/resource-reload/world-switch/shutdown, p50/p95/p99 GPU/CPU frames, draw counts, VRAM and true gameplay RAM. The restored Temurin25 archive/workspace path was not present in this isolated run; available Java is 21. No JAR or qualified release is claimed. Git CLI private fetch failed DNS lookup (`Could not resolve host: github.com`); GitHub connector read/write and guarded parent updates succeeded.

**Next:** owner native fixed/adaptive/disabled frustum tests on alternating and all-unique pages, camera travel/turn/edit, water and FSR1/native comparisons with complete frame/RAM/VRAM telemetry. If the new per-frame alias probe costs more than saved frustum work, disable/reshape that path; then prioritize actual mesh/GPU bottlenecks or temporal upscaler prerequisites.
