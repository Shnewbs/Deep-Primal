// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan;

import oshi.hardware.CentralProcessor;

public class SystemInfo {
   public static final String cpuInfo;

   static {
      CentralProcessor centralProcessor = new oshi.SystemInfo().getHardware().getProcessor();
      cpuInfo = String.format("%s", centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
   }
}
