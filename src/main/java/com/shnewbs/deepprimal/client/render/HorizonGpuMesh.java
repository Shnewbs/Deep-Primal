package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.core.HorizonMesh;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.world.phys.AABB;
import org.lwjgl.system.MemoryUtil;

/** Owns every GPU allocation for one detail level. A root also owns its coarser levels. */
final class HorizonGpuMesh implements AutoCloseable {
  final int x, z, minY, maxY, vertexCount, indexCount, detail;
  final GpuBuffer vertices, indices;
  final RenderPass.Draw<Void> indexedDraw;
  com.mojang.blaze3d.buffers.GpuBufferSlice transformSlice;
  final short[] heights;
  AABB bounds;
  long byteSize;
  HorizonGpuMesh coarse;

  HorizonGpuMesh(
      int x,
      int z,
      int minY,
      int maxY,
      GpuBuffer vertices,
      GpuBuffer indices,
      int vertexCount,
      int indexCount,
      int detail,
      short[] heights) {
    this.x = x;
    this.z = z;
    this.minY = minY;
    this.maxY = maxY;
    this.vertices = vertices;
    this.indices = indices;
    this.vertexCount = vertexCount;
    this.indexCount = indexCount;
    this.detail = detail;
    this.heights = heights;
    byteSize = vertices.size() + (indices == null ? 0 : indices.size());
    indexedDraw =
        indices == null
            ? null
            : new RenderPass.Draw<>(
                0,
                vertices,
                indices,
                VertexFormat.IndexType.SHORT,
                0,
                indexCount,
                0,
                (unused, uploader) -> uploader.upload("DynamicTransforms", transformSlice));
    bounds = new AABB(x * 512.0, minY - 1, z * 512.0, (x + 1.0) * 512, maxY + 1, (z + 1.0) * 512);
  }

  static HorizonGpuMesh upload(int x, int z, int detail, HorizonMesh mesh) {
    return upload(x, z, detail, mesh, true);
  }

  static HorizonGpuMesh upload(int x, int z, int detail, HorizonMesh mesh, boolean normals) {
    ByteBuffer vertexData =
        MemoryUtil.memAlloc(mesh.vertices() * (normals ? 20 : 16)).order(ByteOrder.nativeOrder());
    ByteBuffer indexData = null;
    GpuBuffer vertices = null, indices = null;
    try {
      indexData = MemoryUtil.memAlloc(mesh.indices().length * 2).order(ByteOrder.nativeOrder());
      for (int i = 0; i < mesh.vertices(); i++) {
        int p = i * 3, color = mesh.colors()[i];
        vertexData
            .putFloat(mesh.positions()[p])
            .putFloat(mesh.positions()[p + 1])
            .putFloat(mesh.positions()[p + 2]);
        vertexData
            .put((byte) (color >> 16))
            .put((byte) (color >> 8))
            .put((byte) color)
            .put((byte) (color >> 24));
        if (normals) vertexData.putInt(mesh.normals()[i]);
      }
      for (int index : mesh.indices()) indexData.putShort((short) index);
      vertexData.flip();
      indexData.flip();
      var device = RenderSystem.getDevice();
      vertices =
          device.createBuffer(
              () -> "Deep Primal terrain vertices", GpuBuffer.USAGE_VERTEX, vertexData);
      indices =
          device.createBuffer(
              () -> "Deep Primal terrain indices", GpuBuffer.USAGE_INDEX, indexData);
      return new HorizonGpuMesh(
          x,
          z,
          mesh.minimum(),
          mesh.maximum(),
          vertices,
          indices,
          mesh.vertices(),
          mesh.indices().length,
          detail,
          mesh.heights());
    } catch (RuntimeException | Error error) {
      if (indices != null) indices.close();
      if (vertices != null) vertices.close();
      throw error;
    } finally {
      MemoryUtil.memFree(vertexData);
      if (indexData != null) MemoryUtil.memFree(indexData);
    }
  }

  void draw(RenderPass pass) {
    pass.setVertexBuffer(0, vertices);
    if (indices == null) pass.draw(0, vertexCount);
    else {
      pass.setIndexBuffer(indices, VertexFormat.IndexType.SHORT);
      pass.drawIndexed(0, 0, indexCount, 1);
    }
  }

  int triangles() {
    return (indices == null ? vertexCount : indexCount) / 3;
  }

  HorizonGpuMesh forDetail(int wanted) {
    return coarse != null && wanted > detail ? coarse.forDetail(wanted) : this;
  }

  @Override
  public void close() {
    transformSlice = null;
    vertices.close();
    if (indices != null) indices.close();
    if (coarse != null) coarse.close();
  }
}
