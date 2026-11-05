# Renderer jam 47 — read-only scoped self-audit

**Exact source reviewed:** [`18c8eb774f58a96067eb17f04d0fb3b607ccbc39`](https://github.com/Shnewbs/Deep-Primal/commit/18c8eb774f58a96067eb17f04d0fb3b607ccbc39); claim parent `1d98333bd05c0fee93e00fb7b22ba08ce9212669`, production version **0.0.1-alpha.29-dev.92-relief-joins.1** (checked from `gradle.properties` during review). **This is the renderer author's read-only self-audit, not an independent engine audit.** Implementation and regression test files were committed before the review. No production code changed during scoring; later documentation only.

## Evidence consumed, exact changes

Jam46's [input-resource alias review](../20261030-dlss-resource-alias/REVIEW.md) and [October27 upscaling gate](../../UPSCALING.md) show that optional DLSS has no active in-game temporal pipeline; it must not be represented as working RTX DLSS. Jam44's unrelated LOD publication work and the still-listed `20261030-prefix-resume` claim were not touched. Other engine/water writes were excluded.

The original [provider before/after commit](https://github.com/Shnewbs/Deep-Primal/commit/18c8eb774f58a96067eb17f04d0fb3b607ccbc39) created `long[8]` and `int[4]` on every evaluation. [Provider L105–110 and L175–186](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssVulkanProvider.java#L105-L186) now holds one session-owned `DlssPackedFrame` and passes its reused arrays to the unchanged native call. [Helper L9–25](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrame.java#L9-L25) writes all four image/view pairs and four formats in the old order, without hot-path arrays. The [JNI source](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/main/native/dlss/deepprimal_dlss.cpp#L300-L309) synchronously copies both Java arrays before NGX; it does not retain their pointers. Existing per-frame `DlssFrame` validation, command-buffer ownership, device-memory alias limitations, shader barriers and required temporal inputs remain as before.

## Actual validation

Environment: Linux x86_64, OpenJDK/Javac **21.0.12.1**. Temurin25 and owner JDK25 tarball missing here. An isolated local checkout through git could not run because `github.com` DNS was unavailable; pinned GitHub content was used for integration, and exact Git object hashes verified the local tested helper and test sources.

```sh
javac --release 21 -Xlint:all -Werror -d classes \
  src/net/vulkanmod/deepprimal/upscaling/dlss/DlssFrame.java \
  src/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrame.java \
  test/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrameChecks.java
java -ea -Xverify:all -cp classes net.vulkanmod.deepprimal.upscaling.dlss.DlssPackedFrameChecks
# executed three times; each PASS assertions=300002
```

[Exact committed test L9–38](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrameChecks.java#L9-L38): 100,000 frames per process, varied handles and RG16F/RG32F motion formats, ordered ABI payload and fixed array identity, plus cross-session isolation. Total **900,006 assertions passed**. A defective helper that reallocated both arrays on `fill` compiled but failed at `DlssPackedFrameChecks.java:22` with `AssertionError` (expected negative control).

Separately a production-excluded **synthetic** Java21 allocation harness (included in the source-evidence ZIP) ran three fresh processes with nine alternating old/new samples each, 100,000 iterations per sample. `com.sun.management.ThreadMXBean` recorded **112.00 bytes/frame** for escaped fresh arrays and **0.00 bytes/frame** for the reused packet in all three runs; p50 cost was **8.0–8.7 ns/frame old** versus **3.8–4.1 ns/frame reused**. The harness deliberately publishes its arrays to a volatile sink. It does not invoke NVIDIA NGX or quantify Minecraft FPS. Full production provider compilation, Java25 module/Gradle build, and native JNI/GPU acceptance were **not run** here.

## Equal-weight six-category renderer scoring

| Category | Score /10 | Source-pinned evidence / missing credit |
| --- | ---: | --- |
| Distance/LOD correctness and usable coverage | **6.80** | Carried from [jam46](../20261030-dlss-resource-alias/REVIEW.md); [exact commit](https://github.com/Shnewbs/Deep-Primal/commit/18c8eb774f58a96067eb17f04d0fb3b607ccbc39) changes no LOD; unseen terrain/host coverage unproved |
| Mesh generation/reuse/scheduling | **6.95** | Carried from jam46; no mesh, rebuild or upload pipeline changes, no mesh improvement credit |
| GPU culling/submission/memory | **4.55** | Carried from jam46; [packet source](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrame.java#L9-L25) is Java heap scratch, not measured GPU submission/VRAM |
| Actual AMD FSR/NVIDIA DLSS | **5.00** | Carried from jam46 and [upscaling status](../../UPSCALING.md); FSR1 spatial exists but optional DLSS lacks live temporal inputs/RTX validation; this array optimization does **not** activate it |
| Visual/backend/settings compatibility | **5.30** | Carried from jam46; no pixels, water, swapchain, world switch, resize or OpenGL/Vulkan runs |
| Validation and measured performance | **6.65** | Carried from jam46 rather than increased: [test L9–38](https://github.com/Shnewbs/Deep-Primal/blob/18c8eb774f58a96067eb17f04d0fb3b607ccbc39/renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssPackedFrameChecks.java#L9-L38), 900,006 assertions, one rejected mutant and isolated 112→0 B Java allocation; no real frame benchmark/full module compile |
| **Mean** | **5.88** | (6.80+6.95+4.55+5.00+5.30+6.65)/6 = **5.875**, rounded **5.88**; previous completed jam46 **5.88**, delta **0.00** |

Independent whole-engine grade is not changed.

## Not-run gates, defects and next repair

**Not run:** Java25/Gradle/NeoForge full build; qualified development JAR and bundled native archive checks; actual RTX NGX dispatch, Vulkan/OpenGL captures, water transparency pixels; GPU memory/timing p50/p95/p99; native reload/resize/world-switch/save/reopen/shutdown and streaming/budget acceptance. No new playable binary is claimed. The owner runs full builds under the October27 override.

Outstanding: JNI native code still cannot prove backing VkDeviceMemory disjointness; renderer lacks full-scene motion vectors, valid preserved depth, history reset integration, barriers and device/queue lifetime proof. The source-only allocation probe does not establish host FPS or active DLSS. Next renderer jam should prioritize verified temporal resource preconditions or owner-hardware native A/B LOD/FSR1/water capture, without editing other scopes.
