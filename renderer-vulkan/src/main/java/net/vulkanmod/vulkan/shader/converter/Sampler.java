// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader.converter;

public class Sampler implements GLSLParser.Node {
   final Sampler.Type type;
   final String id;
   int binding;

   public Sampler(Sampler.Type type, String id) {
      this.type = type;
      this.id = id;
   }

   public void setBinding(int binding) {
      this.binding = binding;
   }

   @Override
   public String getStringValue() {
      return "layout(binding = %d) uniform %s %s;\n".formatted(this.binding, this.type.name, this.id);
   }

   public static enum Type {
      SAMPLER_2D("sampler2D"),
      SAMPLER_CUBE("samplerCube"),
      I_SAMPLER_BUFFER("isamplerBuffer");

      public final String name;

      private Type(String name) {
         this.name = name;
      }
   }
}
