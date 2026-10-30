# Renderer jam 51 self-audit

Read-only audit of the exact candidate snapshot after implementation.

| Category | Score /10 | Evidence |
|---|---:|---|
| Distance/LOD correctness and useful coverage | 6.80 | No distance, LOD, hierarchy, or streaming behavior changed. |
| Mesh generation/reuse/scheduling | 6.95 | No mesh or worker scheduling change. |
| GPU culling/submission/memory | 4.60 | Adds a tested memory-range contract, but it is not yet connected to Vulkan allocation ownership or dispatch. |
| Actual AMD FSR/NVIDIA DLSS | 5.00 | DLSS remains unavailable; no NGX dispatch or vendor runtime proof. |
| Visual/backend/settings compatibility | 5.30 | No render ordering, water, backend settings, reload, resize, or lifecycle change. |
| Validation/measured performance | 6.70 | Standalone C++17 contract test and randomized oracle; no native module, GPU, or frame-time evidence. |
| **Average** | **5.89** | **Change from jam 50: +0.01** |

## Validation

Executed as an isolated native-free C++17 check:

    c++ -std=c++17 -Wall -Wextra -Werror -pedantic -I. \
      DlssMemoryBindingGuardChecks.cpp -o check
    ./check

Expected result: PASS with deterministic and randomized overlap/overflow coverage.

Not run: Java 25/Gradle/NeoForge, full JNI/NGX build, Vulkan/OpenGL client execution,
GPU frame time/VRAM, water pixels, FSR/DLSS dispatch, packaging, or playable JAR.

## Remaining defects and next repair

- The live JNI contract still lacks binding provenance inputs.
- The Vulkan allocation owner must surface VkDeviceMemory handle, offset, and size
  for color/depth/motion/output, then validate the helper before NGX resource creation.
- DLSS must remain unavailable until full-scene depth, moving-object motion vectors,
  layouts/barriers, fence-safe lifetime, and RTX output are validated.
