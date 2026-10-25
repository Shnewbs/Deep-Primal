package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.client.GroundSideSpriteSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;

final class GroundSideNativeChecks {
  static void quad(BakedQuad quad) {
    if (!(quad.materialInfo().sprite().contents() instanceof GroundSideSpriteSource.MaterialContents sprite))
      throw new AssertionError("Ground side did not load its generated material");
    for (var format : new com.mojang.blaze3d.vertex.VertexFormat[] {
        DefaultVertexFormat.BLOCK, DefaultVertexFormat.ENTITY}) {
      try (var storage = ByteBufferBuilder.exactlySized(format.getVertexSize() * 4)) {
        var builder = new BufferBuilder(storage, VertexFormat.Mode.QUADS, format);
        var instance = new QuadInstance();
        if (format == DefaultVertexFormat.BLOCK) builder.putBlockBakedQuad(0, 0, 0, quad, instance);
        else builder.putBakedQuad(new com.mojang.blaze3d.vertex.PoseStack().last(), quad, instance);
        try (var mesh = builder.buildOrThrow()) {
          var vertices = mesh.vertexBuffer().order(java.nio.ByteOrder.nativeOrder());
          int lightOffset = format.getOffset(com.mojang.blaze3d.vertex.VertexFormatElement.UV2);
          for (int v = 0; v < 4; v++)
            if ((vertices.getShort(v * format.getVertexSize() + lightOffset)
                & GroundSideSpriteSource.LIGHT_MARKER) == 0)
              throw new AssertionError("Native vertex upload lost scoped ground material marker");
        }
      }
    }
    sprite.increaseMipLevel(4);
    for (int mip = 1; mip < sprite.byMipLevel.length; mip++) {
      var previous = sprite.byMipLevel[mip - 1];
      var next = sprite.byMipLevel[mip];
      for (int y = 0; y < next.getHeight(); y++)
        for (int x = 0; x < next.getWidth(); x++)
          for (int shift = 0; shift < 32; shift += 8) {
            int sum = 0;
            for (int dy = 0; dy < 2; dy++)
              for (int dx = 0; dx < 2; dx++)
                sum += (previous.getPixel(x * 2 + dx, y * 2 + dy) >>> shift) & 255;
            if (((next.getPixel(x, y) >>> shift) & 255) != (sum + 2) / 4)
              throw new AssertionError("Material mip coefficients were gamma/alpha remapped");
          }
    }
  }

  private GroundSideNativeChecks() {}
}
