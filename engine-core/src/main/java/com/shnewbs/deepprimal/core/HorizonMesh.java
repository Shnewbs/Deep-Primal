package com.shnewbs.deepprimal.core;

/** Primitive worker-owned mesh data with shared roof vertices and unsigned-short indices. */
public record HorizonMesh(
    float[] positions,
    int[] colors,
    int[] indices,
    int[] normals,
    short[] heights,
    int minimum,
    int maximum) {
  public int vertices() {
    return colors.length;
  }

  public int triangles() {
    return indices.length / 3;
  }

  public long gpuBytes() {
    return vertices() * 20L + indices.length * 2L;
  }

  public static int vertexCount(int detail) {
    int cells = HorizonGeometry.cells(detail);
    return (cells + 1) * (cells + 1) + cells * 16 + 4;
  }

  public static int indexCount(int detail) {
    int cells = HorizonGeometry.cells(detail);
    return cells * cells * 6 + cells * 48 + 6;
  }

  public static long gpuBytes(int detail) {
    return vertexCount(detail) * 20L + indexCount(detail) * 2L;
  }

  public static HorizonMesh build(short[] source, int[] sourceColors, int detail, int floor) {
    int cells = HorizonGeometry.cells(detail), width = cells + 1;
    if (source.length != width * width || sourceColors.length != source.length)
      throw new IllegalArgumentException("Invalid horizon samples");
    short[] heights = source.clone();
    int low = floor, high = floor;
    for (int z = 0; z <= cells; z++) {
      for (int x = 0; x <= cells; x++) {
        heights[z * width + x] = (short) Math.round(HorizonGeometry.stitched(source, cells, x, z));
        int i = z * width + x;
        high = Math.max(high, heights[i]);
        // The water roof is lowered by 1/8 block; bounds must include that offset.
        low = Math.min(low, heights[i] - ((sourceColors[i] >>> 24) == 249 ? 1 : 0));
      }
    }
    Builder mesh = new Builder(vertexCount(detail), indexCount(detail));
    float step = 512f / cells;
    for (int z = 0; z <= cells; z++) {
      if (Thread.currentThread().isInterrupted())
        throw new java.util.concurrent.CancellationException();
      for (int x = 0; x <= cells; x++) {
        int i = z * width + x;
        int v =
            mesh.vertex(
                x * step,
                heights[i] - ((sourceColors[i] >>> 24) == 249 ? .125f : 0),
                z * step,
                sourceColors[i]);
        double dx =
            heights[z * width + Math.min(cells, x + 1)] - heights[z * width + Math.max(0, x - 1)];
        double dz =
            heights[Math.min(cells, z + 1) * width + x] - heights[Math.max(0, z - 1) * width + x];
        mesh.normals[v] =
            normal(
                -dx / (step * (x == 0 || x == cells ? 1 : 2)),
                1,
                -dz / (step * (z == 0 || z == cells ? 1 : 2)));
      }
    }
    for (int z = 0; z < cells; z++) {
      for (int x = 0; x < cells; x++) {
        int a = z * width + x, b = a + width, c = a + 1, d = b + 1;
        mesh.triangle(a, b, c);
        mesh.triangle(c, b, d);
      }
    }
    for (int i = 0; i < cells; i++) {
      mesh.wall(i * step, 0, (i + 1) * step, 0, heights[i], heights[i + 1], floor, sourceColors[i]);
      int a = cells * width + i;
      mesh.wall(
          i * step, 512, (i + 1) * step, 512, heights[a], heights[a + 1], floor, sourceColors[a]);
      a = i * width;
      mesh.wall(
          0, i * step, 0, (i + 1) * step, heights[a], heights[a + width], floor, sourceColors[a]);
      a += cells;
      mesh.wall(
          512,
          i * step,
          512,
          (i + 1) * step,
          heights[a],
          heights[a + width],
          floor,
          sourceColors[a]);
    }
    int a = mesh.vertex(0, floor, 0, 0xff565d55);
    int b = mesh.vertex(512, floor, 0, 0xff565d55);
    int c = mesh.vertex(0, floor, 512, 0xff565d55);
    int d = mesh.vertex(512, floor, 512, 0xff565d55);
    mesh.triangle(a, b, c);
    mesh.triangle(c, b, d);
    if (mesh.nextVertex != mesh.colors.length || mesh.nextIndex != mesh.indices.length)
      throw new IllegalStateException("Mesh topology size mismatch");
    return new HorizonMesh(
        mesh.positions, mesh.colors, mesh.indices, mesh.normals, heights, low, high);
  }

  private static int normal(double x, double y, double z) {
    double scale = 127 / Math.sqrt(x * x + y * y + z * z);
    return ((int) Math.round(x * scale) & 255)
        | (((int) Math.round(y * scale) & 255) << 8)
        | (((int) Math.round(z * scale) & 255) << 16);
  }

  private static final class Builder {
    final float[] positions;
    final int[] colors, indices, normals;
    int nextVertex, nextIndex;

    Builder(int vertices, int indexCount) {
      positions = new float[vertices * 3];
      colors = new int[vertices];
      normals = new int[vertices];
      indices = new int[indexCount];
    }

    int vertex(float x, float y, float z, int color) {
      int index = nextVertex++, p = index * 3;
      positions[p] = x;
      positions[p + 1] = y;
      positions[p + 2] = z;
      colors[index] = color;
      normals[index] = normal(0, 1, 0);
      return index;
    }

    void triangle(int a, int b, int c) {
      indices[nextIndex++] = a;
      indices[nextIndex++] = b;
      indices[nextIndex++] = c;
    }

    void wall(float x, float z, float nx, float nz, float y, float ny, float floor, int color) {
      int a = vertex(x, y, z, color), b = vertex(nx, ny, nz, color);
      int c = vertex(x, floor, z, color), d = vertex(nx, floor, nz, color);
      int n = normal(nz - z, 0, x - nx);
      normals[a] = normals[b] = normals[c] = normals[d] = n;
      triangle(a, b, c);
      triangle(c, b, d);
      triangle(c, b, a);
      triangle(d, b, c);
    }
  }
}
