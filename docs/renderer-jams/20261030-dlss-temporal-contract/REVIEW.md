# Renderer jam 49 — DLSS temporal contract centralization

**Read-only author self-audit** of exact source changes made on current main after jam 48. This is not the independent engine auditor's grade.

## Scope and consumed findings

Consumed jam 48's native alias guard review and docs/UPSCALING.md. The prior native guard rejected duplicate VkImage/VkImageView handles but did not prove disjoint VkDeviceMemory. This jam does not claim that missing proof is solved; it centralizes the Java-side temporal frame contract so future native/memory-provenance work has one allocation-free validator.

## Implementation

- Added DlssTemporalContract, a package-private allocation-free validator for shared render extents, output admission, pairwise image/view identity, motion-vector/depth formats, and finite temporal constants.
- DlssFrame now delegates all cross-image and temporal checks to that helper; constructor behavior is preserved while the contract is single-sourced.
- No renderer geometry, water behavior, backend settings, FSR path, DLSS dispatch, or frame-generation code changed.

## Validation

Executed in isolated Debian Linux with OpenJDK 21.0.12.1:

    javac -Xlint:all -Werror -d <tmp>/out DlssTemporalContract.java DlssFrame.java Test.java
    java -cp <tmp>/out Test
    PASS

The focused fixture covered a valid frame and a rejected motion-format/identity collision. No full Gradle/NeoForge build, Java 25 build, JNI/NGX build, Vulkan/OpenGL client run, GPU pixels, RAM/VRAM, or playable JAR was produced. Owner build override remains authoritative.

## Six-category renderer score

| Category | Score /10 | Evidence |
|---|---:|---|
| Distance/LOD correctness and useful coverage | 6.80 carried | No LOD or streaming change |
| Mesh generation/reuse/scheduling | 6.95 carried | No mesh or worker change |
| GPU culling/submission/memory | 4.55 carried | No GPU submission or memory-binding proof |
| Actual AMD FSR/NVIDIA DLSS | 5.00 carried | Java contract only; NGX dispatch still unavailable |
| Visual/backend/settings compatibility | 5.30 carried | No backend, water, reload, resize or settings behavior change |
| Validation and measured performance | 6.65 carried | Focused Java compile/run only; no frame/GPU timing |
| **Average** | **5.88** | Same as jam 48; delta **0.00** |

## Remaining defects and next repair

- Separate VkDeviceMemory aliasing is still unproven.
- World-depth preservation, moving-object motion vectors, Vulkan layout/barrier/fence correctness, NGX SDK build, and RTX output remain unrun.
- Next scoped repair: add explicit memory-binding provenance to the native frame contract or, if the owner supplies a native build path, validate binding ranges before NGX dispatch. Do not enable DLSS until those gates pass.
