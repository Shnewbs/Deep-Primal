# Renderer jam 70 — fail-closed Java DLSS memory-binding preflight

Implementation source parent: 4bd777cbbd8bd5819e69915570beba0d27c8fafa. Scope: DLSS Vulkan Java provider and focused source-only tests.

Added allocation-free Java-side rejection of missing, invalid, overflowing, or pairwise overlapping VkDeviceMemory ranges **before** packing JNI arrays and invoking NGX. Native guard remains authoritative at its own boundary. Legacy image metadata constructors remain supported but cannot pass the new dispatch preflight without allocation provenance.

Java21 focused test on exact pinned production DlssFrame.java and DlssTemporalContract.java blobs: `javac -Xlint:all -Werror -d out DlssFrame.java DlssTemporalContract.java DlssMemoryBindingContract.java DlssMemoryBindingContractChecks.java && java -cp out net.vulkanmod.deepprimal.upscaling.dlss.DlssMemoryBindingContractChecks` => `DLSS_MEMORY_PREFLIGHT_PASS assertions=200013 gpu=false`. Negative overlap-blind mutant was rejected (AssertionError).

No Java25, full Gradle, native Vulkan/NGX, RTX pixels, FPS, packaged JAR, or GPU memory proof. Owner builds per October 27 override. This does not establish truthful allocation provenance from Vulkan binding calls or enable DLSS.
