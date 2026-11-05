package com.shnewbs.deepprimal.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;

/** Independently uploaded liquid vertices; the host owns the shared sequential quad indices. */
public final class WaterGpuMesh implements AutoCloseable {
  private final GpuBuffer vertices;
  private final int indices;
  public final long bytes;

  public WaterGpuMesh(ByteBuffer data, int vertexCount) {
    vertices =
        RenderSystem.getDevice()
            .createBuffer(() -> "Deep Primal animated water", GpuBuffer.USAGE_VERTEX, data);
    indices = vertexCount / 4 * 6;
    bytes = data.remaining();
  }

  public void draw(RenderPass pass) {
    var sequential = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    pass.setVertexBuffer(0, vertices);
    pass.setIndexBuffer(sequential.getBuffer(indices), sequential.type());
    pass.drawIndexed(0, 0, indices, 1);
  }

  public void close() {
    vertices.close();
  }
}
