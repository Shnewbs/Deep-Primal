// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader.converter;

public class Attribute implements GLSLParser.Node {
   String ioType;
   String type;
   String id;
   int location;

   public Attribute(String ioType, String type, String id) {
      switch (ioType) {
         case "in":
         case "out":
            this.ioType = ioType;
            this.type = type;
            this.id = id;
            return;
         default:
            throw new IllegalArgumentException();
      }
   }

   public void setLocation(int location) {
      this.location = location;
   }

   @Override
   public String getStringValue() {
      return "layout(location = %d) %s %s %s;\n".formatted(this.location, this.ioType, this.type, this.id);
   }
}
