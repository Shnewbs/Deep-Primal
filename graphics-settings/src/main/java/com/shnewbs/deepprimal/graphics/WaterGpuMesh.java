package com.shnewbs.deepprimal.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;

/** Independently uploaded liquid vertices; the host owns the shared sequential quad indices. */
public final class WaterGpuMesh implements AutoCloseable {
  private final GpuBuffer vertices;
  private int indices;
  public final long bytes;

  public WaterGpuMesh(ByteBuffer data, int vertexCount) {
    vertices =
        RenderSystem.getDevice()
            .createBuffer(() -> "Deep Primal animated water", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, data);
    indices = vertexCount / 4 * 6;
    bytes = data.remaining();
  }

  /** Update within the existing allocation; bytes continues to report its full capacity. */
  public void update(ByteBuffer data, int vertexCount) {
    if (vertexCount < 0
        || (vertexCount & 3) != 0
        || (long) vertexCount * 36 != data.remaining()
        || data.remaining() > bytes)
      throw new IllegalArgumentException("Water update exceeds capacity or has invalid quad data");
    if (data.hasRemaining())
      RenderSystem.getDevice()
          .createCommandEncoder()
          .writeToBuffer(vertices.slice(0, data.remaining()), data);
    indices = vertexCount / 4 * 6;
  }

  /** Patch admitted vertices in place; topology and allocation capacity are unchanged. */
  public void updateRange(int offset, ByteBuffer data) {
    if(offset<0 || offset%36!=0 || data.remaining()%36!=0 || (long)offset+data.remaining()>(long)(indices/6)*4*36)
      throw new IllegalArgumentException("Invalid live water vertex range");
    if(data.hasRemaining())RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertices.slice(offset,data.remaining()),data);
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
