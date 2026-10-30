# Renderer jam 48 — native NVIDIA DLSS resource-alias guard

**Read-only author self-audit** of exact [source commit](https://github.com/Shnewbs/Deep-Primal/commit/5cf284fd62632757a8cb740e565c541f58c07c58), parent claim commit `03de4cf670e497dbd8b0ee80c14479e1619f9c99`, initial main `69b4a241fc1be55301453dead8d9700cfada2d2b`. Implementation and tests were completed before this scored review, and production code was not changed during scoring. The version in `gradle.properties` at review was **0.0.1-alpha.29-dev.93-relief-core-gate.1**. This is not the separate whole-engine auditor's grade.

## Scope, consumed findings, and exact changes

Consumed [jam 47 DLSS packet audit](../20261030-dlss-packet-reuse/REVIEW.md), [jam 46 Java frame alias review](../20261030-dlss-resource-alias/REVIEW.md), [upscaling contract](../../UPSCALING.md), current renderer backlog, relevant engine/water state, current main/claims/recent commits. The existing LOD prefix-resume source was not modified. All other engine, water, terrain, UI, and port files are excluded.

Prior native JNI checking only rejected three output-versus-input **VkImage** collisions, even though the Java `DlssFrame` constructor rejects all six image/view pairs. Direct native callers could bypass the Java precondition. The [native JNI entry at L300–323](https://github.com/Shnewbs/Deep-Primal/blob/5cf284fd62632757a8cb740e565c541f58c07c58/renderer-vulkan/src/main/native/dlss/deepprimal_dlss.cpp#L300-L323) now calls [`hasDistinctImagesAndViews` L9–23](https://github.com/Shnewbs/Deep-Primal/blob/5cf284fd62632757a8cb740e565c541f58c07c58/renderer-vulkan/src/main/native/dlss/DlssImageAliasGuard.h#L9-L23) after array copies/exception checking and before constructing NGX image resources. It rejects absent handles and all six pairs sharing a VkImage or VkImageView. It does not falsely reject numerically equal handles of *different Vulkan object types*. The [native-free C++ fixture L1–71](https://github.com/Shnewbs/Deep-Primal/blob/5cf284fd62632757a8cb740e565c541f58c07c58/renderer-vulkan/src/nativeChecks/cpp/DlssImageAliasGuardChecks.cpp#L1-L71) includes deterministic direct-pair tests, reversed-pair tests, null handles, cross-type overlap acceptance, and 200,000 randomized comparisons to an independent `std::set` oracle.

**Known limitation:** Distinct image/view handles can still reference overlapping VkDeviceMemory; this guard is not memory-binding provenance, Vulkan layout/barrier validation, RTX NGX availability, temporal-input delivery, or actual DLSS activation. Preserve the native fallback.

## Tests and evidence actually executed

Linux x86_64 Debian `g++ 14.2.0`. Exact tested header/test bytes have Git blob IDs `42b1baf5703a999541c5f4ed20a793941f91f195` and `8b8b85c790452f3478e44f0268381f34bd8e9686` matching the committed files; native file blob `6ff42ef55bd48a56b5f4b73d2401e75b235f8a7a`.

```sh
g++ -std=c++17 -O2 -Wall -Wextra -Werror -pedantic -Isrc tests/DlssImageAliasGuardChecks.cpp -o logs/checks
./logs/checks                  # PASS assertions=200049
./logs/checks                  # PASS assertions=200049
./logs/checks                  # PASS assertions=200049
g++ -std=c++17 -O1 -g -fno-omit-frame-pointer -fsanitize=address,undefined -Wall -Wextra -Werror -pedantic -Isrc tests/DlssImageAliasGuardChecks.cpp -o logs/checks-sanitize
ASAN_OPTIONS=detect_leaks=1 UBSAN_OPTIONS=halt_on_error=1 ./logs/checks-sanitize
# PASS assertions=200049, no reported sanitizer issues
```

Four full executions passed **800,196 assertions**, including one sanitized execution. A deliberately defective variant changing `second < 4` to `second < 3` compiled and failed at assertion **10** (expected). Thirteen pinned GitHub integration/source checks passed, including guard placement after JNI array copies and before NGX dispatch, exact old-guard removal, native-free header, and retained Java contract. No benchmark of production Minecraft FPS or GPU time was performed. No Java 25 was present; the owner JDK 25 archive was absent and the host git clone could not resolve github.com; the authorized GitHub connector was used for guarded source commits.

## Equal-weight six-category renderer score

| Category | /10 | Pinning, credit boundaries |
|---|---:|---|
| Distance/LOD correctness and useful coverage | **6.80** | **Carried** from [jam 47](../20261030-dlss-packet-reuse/REVIEW.md); no LOD changed; unseen terrain and host tracking unverified |
| Mesh generation/reuse/scheduling | **6.95** | **Carried**; this C++ identity guard adds no mesh performance or scheduling capability |
| GPU culling/submission/memory | **4.55** | **Carried**; native argument validation does not establish GPU memory disjointness or frame/VRAM improvement |
| Actual AMD FSR/NVIDIA DLSS | **5.00** | **Carried**; [upscaling gate](../../UPSCALING.md) documents implemented FSR1 but no active full-scene temporal DLSS; new validation is not active integration |
| Visual/backend/settings compatibility | **5.30** | **Carried**; no Vulkan/GL water pixels, resource reload, resize, or world switch executed |
| Validation and measured performance | **6.65** | **Carried**; [C++ test](https://github.com/Shnewbs/Deep-Primal/blob/5cf284fd62632757a8cb740e565c541f58c07c58/renderer-vulkan/src/nativeChecks/cpp/DlssImageAliasGuardChecks.cpp#L35-L70) passes 800,196 source-only assertions and mutation rejection, but no production GPU timing |
| **Mean** | **5.88** | (6.80 + 6.95 + 4.55 + 5.00 + 5.30 + 6.65) / 6 = **5.875**, rounds 5.88; previous jam47 **5.88**, delta **0.00** |

**Unrun:** full JDK25 Gradle/NeoForge, production native JNI compilation against the licensed NVIDIA NGX SDK and Vulkan headers, optional provider dispatch on RTX hardware, packaging/nested CRC/native payload validation, native Vulkan/OpenGL frames and water pixels, GPU/CPU frame p50/p95/p99, RAM/VRAM, reload/resize/world-switch/save/reopen/shutdown. October 27 owner-build override remains authoritative; no JAR or release is claimed.

**Next repair:** capture/validate actual disjoint VkDeviceMemory binding ranges for scene color, depth, motion vectors and output, then preserve full-scene depth + moving-object motion vectors, insert layout transitions and frame fences, and test RTX NGX on supported hardware. Independently retain the owner Vulkan/GL A/B LOD and FSR1/water measurement gate. Do not confuse spatial FSR1 with FSR2/3/4 or DLSS frame generation.

**Artifact:** `DeepPrimal_Renderer_Jam48_NativeAliasGuard_20261030_5cf284f_SourceEvidence.zip` is 4,084 bytes, SHA-256 `94ad0681cb062f414b185c9c1641da4ef722e4b4a203d6f63d65b7155f7cdd7a`, ZIP CRC PASS, 5/5 manifest member hashes PASS, Library `/Deep Primal/Renderer Jams/` file ID `libfile_5affef34dd308191bb4d56e2f5d84b53`. Source/testing evidence only, not an executable JAR.
