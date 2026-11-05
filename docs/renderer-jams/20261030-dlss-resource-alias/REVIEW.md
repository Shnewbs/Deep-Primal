# Renderer jam 46 — read-only DLSS frame resource-alias self-audit

**Exact reviewed commit:** [`0addab7688686c53863324f72e6e3c840f84e4a0`](https://github.com/Shnewbs/Deep-Primal/commit/0addab7688686c53863324f72e6e3c840f84e4a0), claim parent `ab05bbf93f41b179b6a9fd231144f21272554a4b`. Source blob `d6d0d6a3d3e154d82afc170c0743b250749eb222`; test blob `a1da4b5466ac3790ba55131eacd89601d5e1b476`. This is the author's **read-only renderer self-audit**, not the independent whole-engine review. Source and tests were committed before inspection; no production code was modified during the scored review. Declared mod version stays `0.0.1-alpha.29-dev.88-vegetation-receipts.1`.

## Change and findings consumed

Prior renderer jam44 and the independent upscaling audit leave GPU pixels and active NVIDIA NGX unaccepted. This bounded jam did not overlap the active `20261030-prefix-resume` claim on `LodPublishedBounds.java`. Instead it corrects a DLSS input-safety gap. Previously `DlssFrame` checked only that output's VkImage handle differed from the three inputs, allowing aliased output VkImageViews and collisions among the sampled inputs. [Pinned source lines 36–69](https://github.com/Shnewbs/Deep-Primal/blob/0addab7688686c53863324f72e6e3c840f84e4a0/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssFrame.java#L36-L69) reject all six input/output pairs sharing either an image or view handle, with six direct comparisons and no temporary array on the frame constructor path. Existing size, format and finite-number guards remain. No geometry, LOD, water, shader, simulation or backend submission paths changed.

The check cannot detect different VkImage handles backed by overlapping VkDeviceMemory. The renderer still must prove nonaliasing, shader layouts/barriers, full-scene motion vectors, frame history, command-buffer synchronization and fences. A Java record guard **does not activate DLSS**.

## Actually executed

Linux x86_64 OpenJDK/Javac 21.0.12.1. Temurin 25 and the owner-supplied archive were absent. Locally compiled the **exact Git-matching** new Java source and fixture:

```sh
javac --release 21 -Xlint:all -Werror -d classes src/net/vulkanmod/deepprimal/upscaling/dlss/DlssFrame.java tests/net/vulkanmod/deepprimal/upscaling/dlss/DlssImageAliasChecks.java
java -ea -Xverify:all -cp classes net.vulkanmod.deepprimal.upscaling.dlss.DlssImageAliasChecks
```

**PASS:** strict compilation, four process executions × 1,038 checks = **4,152 assertions**. [Test lines 13–67](https://github.com/Shnewbs/Deep-Primal/blob/0addab7688686c53863324f72e6e3c840f84e4a0/renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssImageAliasChecks.java#L13-L67) cover 1,024 valid constructions per run, all six resource pairs by image and view collisions and two further output alias cases. **Expected negative control:** the predecessor Git blob `e1cfcb84b7dc9458e04139ceab084c799acc1f34` compiled with the identical test, then failed at the first color/depth image-handle alias (exit 1). No matched CPU/GPU frame, allocations, draw/descriptor, RAM or VRAM measurements were made. No FPS gain is claimed.

## Six equally weighted categories

| Renderer category | Score /10 | Pinned evidence |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | 6.80 | Carried from [jam44 pinned review](../20261029-validity-fastpath/REVIEW.md); [this exact commit](https://github.com/Shnewbs/Deep-Primal/commit/0addab7688686c53863324f72e6e3c840f84e4a0) changes no LOD or delivery path; native unexplored terrain unresolved |
| Mesh generation/reuse/scheduling | 6.95 | Carried from jam44; same exact commit has no mesh modifications or measured mesh improvement |
| GPU culling/submission/memory | 4.55 | Carried from jam44; [source L46–69](https://github.com/Shnewbs/Deep-Primal/blob/0addab7688686c53863324f72e6e3c840f84e4a0/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssFrame.java#L46-L69) is only Java safety validation, not GPU scheduling or resource lifetime |
| Actual AMD FSR/NVIDIA DLSS | 5.00 | Carried from jam44; [prior source-pinned upscaling validation](../../validation/upscaling-2026-10-27/README.md) demonstrates FSR1 and optional NGX bridge, not active temporal DLSS; current guard does not implement dispatch |
| Visual/backend/settings compatibility | 5.30 | Carried from jam44; no water/pixel/reload/resize/backend run and no production code in those paths changed |
| Validation and measured performance | 6.65 | Carried from jam44; [new regression](https://github.com/Shnewbs/Deep-Primal/blob/0addab7688686c53863324f72e6e3c840f84e4a0/renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssImageAliasChecks.java#L13-L67) executes 4,152 Java21 assertions and one failing predecessor, but no Java25/Gradle/native or real frame benchmark |
| **Mean** | **5.88** | **0.00** change from last *completed* renderer jam44 (5.88); concurrent prefix-resume jam was unscored at review time |

Independent whole-engine grade remains its prior **5.0/10**, not revised here.

## Unrun gates, evidence and next repairs

**Not run:** Java25/Gradle/NeoForge full build, qualified JAR, outer/nested archive or native payload CRCs, RTX NGX evaluation, Vulkan/OpenGL pixels and water transparency, frame p50/p95/p99 CPU/GPU, VRAM, resource reload/resize, world switch/save/reopen/shutdown. Owner's October27 build policy controls these. Next DLSS step is a verifiable allocation+barrier+motion-vector/history integration with real NVIDIA RTX acceptance. Renderer priority remains paired native LOD/FSR1/water measurements. No other workstream or automation was changed.

Verified source/testing evidence ZIP, **not a JAR**: `DeepPrimal_Renderer_Jam46_DlssImageAlias_20261030_0addab7_SourceEvidence.zip`, 11,971 bytes, SHA256 `f81612e160075c36b4c4f1150223550ae8aa346c5a13eb72dd225a4f970fbb02`, archive CRC PASS, 13 member SHA256 PASS, uploaded to Library at `/Deep Primal/Renderer Jams/` (ID `libfile_c4546f55a61081918837b812c35c12ef`).
