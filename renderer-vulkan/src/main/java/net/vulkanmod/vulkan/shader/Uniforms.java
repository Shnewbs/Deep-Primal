// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.util.MappedBuffer;

public class Uniforms {
   public static Object2ReferenceOpenHashMap<String, Supplier<Integer>> vec1i_uniformMap = new Object2ReferenceOpenHashMap<>();
   public static Object2ReferenceOpenHashMap<String, Supplier<Float>> vec1f_uniformMap = new Object2ReferenceOpenHashMap<>();
   public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec2f_uniformMap = new Object2ReferenceOpenHashMap<>();
   public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec3f_uniformMap = new Object2ReferenceOpenHashMap<>();
   public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec4f_uniformMap = new Object2ReferenceOpenHashMap<>();
   public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> mat4f_uniformMap = new Object2ReferenceOpenHashMap<>();

   public static void setupDefaultUniforms() {
      mat4f_uniformMap.put("ModelViewMat", VRenderSystem::getModelViewMatrix);
      mat4f_uniformMap.put("ProjMat", VRenderSystem::getProjectionMatrix);
      mat4f_uniformMap.put("MVP", VRenderSystem::getMVP);
      mat4f_uniformMap.put("TextureMat", VRenderSystem::getTextureMatrix);
      vec1i_uniformMap.put("EndPortalLayers", () -> 15);
      vec1i_uniformMap.put("UseRgss", () -> Minecraft.getInstance().options.textureFiltering().get() == TextureFilteringMethod.RGSS ? 1 : 0);
      vec1i_uniformMap.put("CurrentTime", VRenderSystem::getCurrentTime);
      vec1f_uniformMap.put("FogStart", () -> VRenderSystem.getFogData().renderDistanceStart);
      vec1f_uniformMap.put("FogEnd", () -> VRenderSystem.getFogData().renderDistanceEnd);
      vec1f_uniformMap.put("FogEnvironmentalStart", () -> VRenderSystem.getFogData().environmentalStart);
      vec1f_uniformMap.put("FogEnvironmentalEnd", () -> VRenderSystem.getFogData().environmentalEnd);
      vec1f_uniformMap.put("FogRenderDistanceStart", () -> VRenderSystem.getFogData().renderDistanceStart);
      vec1f_uniformMap.put("FogRenderDistanceEnd", () -> VRenderSystem.getFogData().renderDistanceEnd);
      vec1f_uniformMap.put("FogSkyEnd", () -> VRenderSystem.getFogData().skyEnd);
      vec1f_uniformMap.put("FogCloudsEnd", () -> VRenderSystem.getFogData().cloudEnd);
      vec1f_uniformMap.put("AlphaCutout", () -> VRenderSystem.alphaCutout);
      vec2f_uniformMap.put("ScreenSize", VRenderSystem::getScreenSize);
      vec2f_uniformMap.put("TextureSize", VRenderSystem::getTextureSize);
      vec2f_uniformMap.put("TexelSize", VRenderSystem::getTexelSize);
      vec3f_uniformMap.put("Light0_Direction", () -> VRenderSystem.lightDirection0);
      vec3f_uniformMap.put("Light1_Direction", () -> VRenderSystem.lightDirection1);
      vec3f_uniformMap.put("ModelOffset", () -> VRenderSystem.modelOffset);
      vec3f_uniformMap.put("ChunkOffset", () -> VRenderSystem.modelOffset);
      vec4f_uniformMap.put("ColorModulator", VRenderSystem::getShaderColor);
      vec4f_uniformMap.put("FogColor", VRenderSystem::getShaderFogColor);
   }

   public static Supplier<MappedBuffer> getUniformSupplier(String type, String name) {
      return switch (type) {
         case "mat4" -> (Supplier)mat4f_uniformMap.get(name);
         case "vec4" -> (Supplier)vec4f_uniformMap.get(name);
         case "vec3" -> (Supplier)vec3f_uniformMap.get(name);
         case "vec2", "ivec2" -> (Supplier)vec2f_uniformMap.get(name);
         default -> null;
      };
   }
}
