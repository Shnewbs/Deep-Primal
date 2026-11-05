// Deep Primal loader adaptation of the VulkanMod configuration boundary. LGPL-3.0-only.
package net.vulkanmod;
public final class Initializer {
 public static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("Deep Primal Vulkan");
 public static final Configuration CONFIG = new Configuration();
 public static final class Configuration { public int frameQueueSize=2, device=-1; }
}
