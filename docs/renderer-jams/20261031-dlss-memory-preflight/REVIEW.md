# Renderer jam 70 — read-only self-audit

**Exact implementation candidate:** `ada8b3255c6d9d00229918e3293fdbc2b4e91d2f`; parent claim commit `4bd777cbbd8bd5819e69915570beba0d27c8fafa`. This is the renderer jam's own **read-only self-audit**, not the independent engine auditor. No code changed during the scored review.

## Evidence and correctness

- `renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssVulkanProvider.java:179-185` invokes `DlssMemoryBindingContract.validate(frame)` before packing JNI arguments and calling `DlssNative.evaluate`. Source blob `bdbde785721fa743d8fea8276649a4b848f66bc7`.
- `renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssMemoryBindingContract.java:12-31` rejects unavailable/negative/zero/overflowed ranges and all six same-allocation overlap pairs, preserving adjacent nonoverlapping bindings and opaque nonzero signed native handles. Source blob `46f7cd38176452b602df83b57d50876fe95ddd99`.
- `renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssMemoryBindingContractChecks.java:1-97` exercises 200,013 assertions, including valid frame and legacy unknown-provenance fail-closed. Source blob `d7cbcd91fa9f531f81a453513d73660362f0bbef`.
- Java21 test used **exact pinned production** `DlssFrame.java` blob `2249db680f53b54b3b871e06cffc0199363c2cc4` and `DlssTemporalContract.java` blob `fa2b095befe5ce5fc92cfcfdb473e5b0f72fa031`, verified with `git hash-object`. It did not compile the full provider.
- GitHub compare claim→implementation showed exactly four paths: provider, new contract, new test, and this jam's README. No engine/water/graphics UI/FSR/automation source changed.
- The C++ JNI guard already independently rejects overlapping ranges; this Java guard adds earlier rejection. Neither validates that caller-supplied memory binding metadata actually matches Vulkan's bound allocations. DLSS remains unavailable without genuine renderer-owned depth, motion, layouts, barriers, lifetime and RTX dispatch.

## Executed checks

```sh
cd /mnt/data/renderer-jam70-isolated/net/vulkanmod/deepprimal/upscaling/dlss
git hash-object DlssFrame.java DlssTemporalContract.java DlssMemoryBindingContract.java DlssMemoryBindingContractChecks.java
javac -Xlint:all -Werror -d /mnt/data/renderer-jam70-isolated/out DlssFrame.java DlssTemporalContract.java DlssMemoryBindingContract.java DlssMemoryBindingContractChecks.java
java -cp /mnt/data/renderer-jam70-isolated/out net.vulkanmod.deepprimal.upscaling.dlss.DlssMemoryBindingContractChecks
# DLSS_MEMORY_PREFLIGHT_PASS assertions=200013 gpu=false
```

Negative control: compiled overlap-blind helper mutant, ran the same checks, received expected `AssertionError: all six overlap pairs must fail` (exit 1). This proves the focused regression rejects an omitted overlap guard.

**Unrun:** Java25 (not present; owner archive absent), full provider/Gradle/NeoForge, JNI/NGX, Vulkan/OpenGL client, RTX pixels, GPU p50/p95/p99, RAM/VRAM, water transparency/reload/resize/reopen/shutdown, qualified JAR/CRC/native payload/release. No matched frame-time or FPS improvement claimed.

## Renderer-only rubric (six equal weights)

| Category | /10 | Evidence / limitation |
|---|---:|---|
| Distance/LOD correctness and usable coverage | 6.75 | Carried from jam 69; no distance or native travel change |
| Mesh generation/reuse/scheduling | 6.85 | Carried; no mesh or scheduling change |
| GPU culling/submission/memory | 4.90 | Earlier memory-range helper plus this preflight; no verified Vulkan binding provenance, GPU allocation, or indirect draws |
| Actual AMD FSR/NVIDIA DLSS | 5.00 | Existing FSR1 and optional NGX bridge only; no newly enabled or tested DLSS/temporal FSR dispatch |
| Visual/backend/settings compatibility | 5.30 | No water/backend/settings path changed; GPU pixels and lifecycle unrun |
| Validation and measured performance | 5.55 | Exact production dependency source Java21 compile, 200,013 assertions, negative mutant; full provider/Java25/native/measurements absent |
| **Average** | **5.73** | `34.35 / 6 = 5.725`, rounded 5.73; **+0.01 vs jam 69's 5.72** |

Previous audit consumed: `docs/renderer-jams/20261030-pending-apply-counter-compile-fix/REVIEW.md`, `docs/renderer-jams/20261030-dlss-memory-binding/REVIEW.md`, `docs/renderer-jams/20261030-dlss-temporal-contract/REVIEW.md`. Whole-engine grade unchanged.

**Next:** owner Java25 focused production compile and Gradle; wire authoritative `VkDeviceMemory` allocation provenance, actual depth/motion/NGX resource lifetime and RTX validation; capture matched Vulkan/OpenGL travel/edit/water frame baselines.
