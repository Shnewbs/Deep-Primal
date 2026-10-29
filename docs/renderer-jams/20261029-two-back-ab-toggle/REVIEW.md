# Deep Primal renderer jam 40 — independent two-back reuse A/B

**Read-only renderer author self-audit**, exact candidate [`40553d0b69a2928fb2961cb433c91c96b1215fa7`](https://github.com/Shnewbs/Deep-Primal/commit/40553d0b69a2928fb2961cb433c91c96b1215fa7); claim `9de0c0c3cef433658521e1daaa21812ed1034075`. No code modification during the scored inspection. Existing owner engine version `0.0.1-alpha.29-dev.85-outlet-world-binding.1` is unchanged. Jam ID `renderer-20261029T2000-two-back-ab-toggle`.

## Consumed audit and changes

[Jam39 audit](../20261029-adjacent-first-two-back/REVIEW.md) found all-unique page-publication p50 2.3–2.9% slower with two-back metadata, with native GPU break-even unknown. [Independent Dev85 audit](../../engine-hourly-audits/2026-10-29T1932-be9cde82/REVIEW.md) contains no new native renderer GPU acceptance. Read engine/water state, applicable LOD/graphics roadmaps, prior claim and current Git parent; `AGENTS.md` absent. The engine's unrelated source/lease was not modified.

Exactly **three renderer-scoped implementation files**:
- [`LodPublishedBounds.java` L11–17, L65–74](https://github.com/Shnewbs/Deep-Primal/blob/40553d0b69a2928fb2961cb433c91c96b1215fa7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodPublishedBounds.java#L11-L17), blob `aa1b86c37641658abca6792180b88457f4ddcd8f`: default-enabled startup `-Ddeepprimal.lod.twoBackFrustumReuse=false` disables only the i-2 matching work and alias bits; adjacent and coarse union paths remain unchanged.
- [`LodClient.java` L96–122](https://github.com/Shnewbs/Deep-Primal/blob/40553d0b69a2928fb2961cb433c91c96b1215fa7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/LodClient.java#L96-L122), blob `abd0129d0b93bad4d53440d7c0e1e15aab81e4fd`: accurate effective status and opt-in CPU diagnostic semantics.
- [Committed random parity smoke](https://github.com/Shnewbs/Deep-Primal/blob/40553d0b69a2928fb2961cb433c91c96b1215fa7/docs/renderer-jams/20261029-two-back-ab-toggle/tests/com/shnewbs/deepprimal/lod/client/LodTwoBackABCommittedSmoke.java), blob `4681780c4d2a86ecf6d91c42802efda89b443e34`: literal false startup, random finite/malformed boxes and broadphase on/off. The unchanged [`VoxelLodRenderer` L612–648](https://github.com/Shnewbs/Deep-Primal/blob/40553d0b69a2928fb2961cb433c91c96b1215fa7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L612-L648), blob `7a63182b273b66b71e1ebf705a5bb3ecb8f12232`, still consumes empty two-back masks as per-part frustum fallback in original material order; transparent-water state, meshes and submissions remain unchanged in source.

## Tests and measurements, actually executed

Linux x86_64 OpenJDK/javac **21.0.12.1**, isolated retained test-only Minecraft/AABB stubs. The production helper and committed test compiled with `javac --release 21 -Xlint:all -Werror -d out @sources.txt`; executions used `java -ea -Xverify:all -Ddeepprimal.lod.pageFrustumBroadphase=true|false -Ddeepprimal.lod.twoBackFrustumReuse=true|false -cp out <fixture>`. Seven focused parity runs passed **19,592,076** assertions, with additional detailed 8,000-page randomized oracle, prior pinned parity and smoke. Four more executions of the *new Git-blob-identical committed smoke* passed **2,231,636**, total **21,823,712**. Uppercase `FALSE` and legacy ON replays also passed outside the total. Deliberately forcing ON despite explicit false compiled but failed at assertion **1** (exit 1), as expected. Read-only pinned scope/integration checks **14/14 PASS**. Test-only stubs are not packaged into a mod.

Warm synthetic Java publication: 4 LOD levels × 64 parts, 250 samples × 128 publications, two fresh JVMs per flag; **OFF versus default ON p50 reduction**: shared A/B alternating **34.3% / 52.8%**, all-unique **29.6% / 34.1%**, independently allocated value-equal uniform **12.1% / 19.5%**. Full p95/p99 in source evidence. In detailed oracle, ON avoided **95,416** two-back fine predicates and OFF avoided **zero**; adjacent reuse still avoided **122,504** in both. The switch exposes real tradeoff, **not a demonstrated FPS, GPU, or usable-distance improvement**.

## Six equal-weight renderer-only grades

| Category | /10 | Evidence and gap |
| --- | ---: | --- |
| Distance/LOD correctness, coverage | **6.80 carried** | Unchanged [ordered fine selection](https://github.com/Shnewbs/Deep-Primal/blob/40553d0b69a2928fb2961cb433c91c96b1215fa7/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java#L612-L648); unseen terrain/native useful distance unaccepted |
| Mesh generation/reuse/scheduling | **6.95 carried** | No mesher/worker/upload change |
| GPU culling/submission/memory | **4.55 carried** | CPU alias publication only; no true GPU indirect, timestamps or VRAM |
| Actual FSR/DLSS | **5.00 carried** | [FSR1 spatial](../../UPSCALING.md); newer temporal FSR/NGX DLSS Super Resolution not active/accepted |
| Visual/backend/settings compatibility | **5.30 carried** | Startup toggle preserves order; no native Vulkan/GL/water pixel test |
| Validation and measured performance | **6.45 (+0.05)** | 21,823,712 strict Java assertions, negative mutant, 14/14 pinned source checks, matched CPU p50/p95/p99; hardware absent |
| **Average** | **5.84** | 35.05/6=5.8417; **+0.01** from jam39 **5.83** |

Independent whole-engine score remains **5.0/10 carried**, not changed or freshly regraded. Strict renderer target above 8.5 unmet.

**Unrun owner gates:** JDK25 full Gradle9.2.1/NeoForge, JAR/native/ZIP packaging, Windows/NVIDIA/AMD Vulkan/OpenGL GPU, frame CPU/GPU p50/p95/p99, RAM/VRAM/draw/descriptor counts, transparent-water GPU pixel ordering, save/reopen/resize/reload/shutdown, FSR temporal, RTX DLSS and extended usable distance. Git CLI DNS unavailable; connected GitHub source/ref guards succeeded. No Actions, release or CurseForge publication. No JAR relabeling.

**Next:** on owner GPU, same seed/camera/resolution/warm state and world+water, run two-back ON/OFF alongside fixed/adaptive/disabled page culling, FSR1/native and Vulkan/OpenGL; measure frame p50/p95/p99 plus upload/cull, RAM/VRAM/pixel changes. Remove two-back overhead if true hardware costs exceed saved fine-frustum work.

**Artifact:** `DeepPrimal_Renderer_Jam40_TwoBackAB_20261029_40553d0_SourceEvidence.zip`; 19,188 bytes, SHA256 `e24f849080c6fa01d918e8f669f510363e01aaafb09f536255b620c5ba199422`; 19 ZIP entries, outer CRC and 18/18 internal SHA256 hashes verified. Saved to Library `/Deep Primal/Renderer Jams`, ID `libfile_1883796296508191b9cbfefce85cbd63`. Source/evidence ZIP only, not executable JAR.
