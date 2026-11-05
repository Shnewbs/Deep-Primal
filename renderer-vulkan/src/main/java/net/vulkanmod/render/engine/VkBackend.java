// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.GLFWErrorCapture.Error;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuBackend;
import com.mojang.blaze3d.systems.GpuDevice;

public class VkBackend implements GpuBackend {
   @Override
   public String getName() {
      return "Vulkan";
   }

   @Override
   public void setWindowHints() {
      org.lwjgl.glfw.GLFW.glfwDefaultWindowHints();
      org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_CLIENT_API, org.lwjgl.glfw.GLFW.GLFW_NO_API);
   }

   @Override
   public void handleWindowCreationErrors(Error error) throws BackendCreationException {
      com.shnewbs.deepprimal.settings.RendererPreferences.reportStartupFallback(
          "Vulkan window creation failed; see latest.log.");
      throw new BackendCreationException("Cannot create Vulkan window: " + error);
   }

   @Override
   public GpuDevice createDevice(long window, ShaderSource defaultShaderSource, GpuDebugOptions debugOptions) {
      try {
         net.vulkanmod.deepprimal.Startup.arm();
         return new GpuDevice(new VkGpuDevice(window, defaultShaderSource, debugOptions));
      } catch (Exception | LinkageError error) {
         com.shnewbs.deepprimal.settings.RendererPreferences.reportStartupFallback(
             "Vulkan initialization failed (" + error.getClass().getSimpleName()
                 + "); see latest.log.");
         net.vulkanmod.Initializer.LOGGER.error("Vulkan initialization failed; trying OpenGL", error);
         try { net.vulkanmod.vulkan.Vulkan.abortInitialization(); }
         catch (Exception | LinkageError cleanup) { error.addSuppressed(cleanup); }
         net.vulkanmod.deepprimal.Startup.fallback();
         return VkBackend.<RuntimeException, GpuDevice>fail(new BackendCreationException("Vulkan initialization failed: " + error));
      }
   }
   @SuppressWarnings("unchecked")
   private static <T extends Throwable, R> R fail(Throwable error) throws T { throw (T)error; }
}
