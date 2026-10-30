# Renderer jam 46 next priorities

P0: The optional NGX inputs must have distinct VkImage/VkImageView handles **and** genuinely disjoint backing VkDeviceMemory, plus validated source/output layouts, queue barriers, submission and fence-safe lifetime. Java-only handle checks cannot guarantee underlying allocation safety.

P0: Supply/validate jittered projection, sampled scene depth, camera **and moving-object** motion vectors, camera-cut/teleport/resize/reload history resets, and real NVIDIA RTX NGX dispatch before enabling DLSS in-game. FSR1 is spatial; frame generation remains separate.

P0: Owner-operates full Java25/Gradle/native JAR and matched Vulkan/OpenGL GPU tests (CPU/GPU p50/p95/p99, VRAM, draws, transparent water, FSR1 versus native). No performance or frame-quality credit from the Java resource contract.

P1: Keep active prefix-resume LOD claim, generator/water builder, and independent auditor separate. Reassess their own revisions after leases release; do not edit their scoped files.
