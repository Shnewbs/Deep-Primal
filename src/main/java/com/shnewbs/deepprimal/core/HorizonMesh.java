package com.shnewbs.deepprimal.core;

/** Primitive worker-owned mesh data with shared roof vertices and unsigned-short indices. */
public record HorizonMesh(float[] positions, int[] colors, int[] indices, short[] heights,
                          int minimum, int maximum) {
    public int vertices() { return colors.length; }
    public int triangles() { return indices.length / 3; }
    public long gpuBytes() { return vertices() * 16L + indices.length * 2L; }

    public static int vertexCount(int detail) {
        int cells = HorizonGeometry.cells(detail);
        return (cells + 1) * (cells + 1) + cells * 16 + 4;
    }

    public static int indexCount(int detail) {
        int cells = HorizonGeometry.cells(detail);
        return cells * cells * 6 + cells * 48 + 6;
    }

    public static long gpuBytes(int detail) {
        return vertexCount(detail) * 16L + indexCount(detail) * 2L;
    }

    public static HorizonMesh build(short[] source, int[] sourceColors, int detail, int floor) {
        int cells = HorizonGeometry.cells(detail), width = cells + 1;
        if (source.length != width * width || sourceColors.length != source.length)
            throw new IllegalArgumentException("Invalid horizon samples");
        short[] heights = source.clone();
        int high = floor;
        for (int z = 0; z <= cells; z++) {
            for (int x = 0; x <= cells; x++) {
                heights[z * width + x] = (short)Math.round(HorizonGeometry.stitched(source, cells, x, z));
                high = Math.max(high, heights[z * width + x]);
            }
        }
        Builder mesh = new Builder(vertexCount(detail), indexCount(detail));
        float step = 512f / cells;
        for (int z = 0; z <= cells; z++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException();
            for (int x = 0; x <= cells; x++)
                mesh.vertex(x * step, heights[z * width + x], z * step, sourceColors[z * width + x]);
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
            mesh.wall(i * step, 512, (i + 1) * step, 512, heights[a], heights[a + 1], floor, sourceColors[a]);
            a = i * width;
            mesh.wall(0, i * step, 0, (i + 1) * step, heights[a], heights[a + width], floor, sourceColors[a]);
            a += cells;
            mesh.wall(512, i * step, 512, (i + 1) * step, heights[a], heights[a + width], floor, sourceColors[a]);
        }
        int a = mesh.vertex(0, floor, 0, 0xff565d55);
        int b = mesh.vertex(512, floor, 0, 0xff565d55);
        int c = mesh.vertex(0, floor, 512, 0xff565d55);
        int d = mesh.vertex(512, floor, 512, 0xff565d55);
        mesh.triangle(a, b, c);
        mesh.triangle(c, b, d);
        if (mesh.nextVertex != mesh.colors.length || mesh.nextIndex != mesh.indices.length)
            throw new IllegalStateException("Mesh topology size mismatch");
        return new HorizonMesh(mesh.positions, mesh.colors, mesh.indices, heights, floor, high);
    }

    private static final class Builder {
        final float[] positions;
        final int[] colors, indices;
        int nextVertex, nextIndex;
        Builder(int vertices, int indexCount) {
            positions = new float[vertices * 3];
            colors = new int[vertices];
            indices = new int[indexCount];
        }
        int vertex(float x, float y, float z, int color) {
            int index = nextVertex++, p = index * 3;
            positions[p] = x; positions[p + 1] = y; positions[p + 2] = z;
            colors[index] = color;
            return index;
        }
        void triangle(int a, int b, int c) {
            indices[nextIndex++] = a; indices[nextIndex++] = b; indices[nextIndex++] = c;
        }
        void wall(float x, float z, float nx, float nz, float y, float ny, float floor, int color) {
            int a = vertex(x, y, z, color), b = vertex(nx, ny, nz, color);
            int c = vertex(x, floor, z, color), d = vertex(nx, floor, nz, color);
            triangle(a, b, c); triangle(c, b, d);
            triangle(c, b, a); triangle(d, b, c);
        }
    }
}
