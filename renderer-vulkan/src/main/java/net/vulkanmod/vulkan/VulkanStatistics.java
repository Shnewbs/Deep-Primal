// Deep Primal Vulkan diagnostics. LGPL-3.0-only.
package net.vulkanmod.vulkan;

public final class VulkanStatistics {
  public static final java.util.concurrent.atomic.AtomicInteger validationErrors =
      new java.util.concurrent.atomic.AtomicInteger();
  public static long descriptorUpdates,
      descriptorReuse,
      descriptorBinds,
      descriptorBindSkips,
      pipelineCreates,
      pipelineCacheHits,
      pipelineCreationNanos,
      pipelineFeedbackValid;

  public static java.util.Map<String, Long> snapshot() {
    return java.util.Map.of(
        "descriptorUpdates",
        descriptorUpdates,
        "descriptorReuse",
        descriptorReuse,
        "descriptorBinds",
        descriptorBinds,
        "descriptorBindSkips",
        descriptorBindSkips,
        "pipelineCreates",
        pipelineCreates,
        "pipelineCacheHits",
        pipelineCacheHits,
        "pipelineCreationNanos",
        pipelineCreationNanos,
        "pipelineFeedbackValid",
        pipelineFeedbackValid,
        "validationErrors",
        (long) validationErrors.get());
  }

  private VulkanStatistics() {}
}
