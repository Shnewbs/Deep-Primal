# Renderer DLSS resource alias contract jam — 2026-10-30

Scope is confined to the optional Vulkan NGX Java frame-input contract and a Java-only test. This does **not** enable DLSS in the game.

Before this change the Java contract rejected an output with the same VkImage handle as a sampled input, but accepted output *view* aliasing and collisions between simultaneous color/depth/motion inputs. The GPU cannot safely treat those as distinct read/write resources, even with matching Java metadata.

The contract now rejects any shared VkImage or VkImageView handle across the four images, with six allocation-free pair comparisons in each constructed frame. It preserves existing size/format/temporal validation. Separate VkImage handles can still point at overlapping VkDeviceMemory, which Java cannot detect; the renderer must guarantee nonaliasing, correct layouts/barriers, full-scene motion vectors and fence-safe lifetimes.

Standalone check (owner-build override: no complete Gradle/GPU test claimed):

```sh
javac --release 21 -Xlint:all -Werror -d /tmp/dlss-contract \
  renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssFrame.java \
  renderer-vulkan/src/nativeChecks/java/net/vulkanmod/deepprimal/upscaling/dlss/DlssImageAliasChecks.java
java -ea -Xverify:all -cp /tmp/dlss-contract net.vulkanmod.deepprimal.upscaling.dlss.DlssImageAliasChecks
```

Deliberately compile the same regression fixture against the previous source to verify the pre-fix acceptance bug. No Java25, native NVIDIA, RTX device, Vulkan pixels, mesh FPS, FSR temporal, JAR, or general-engine grade is claimed.
