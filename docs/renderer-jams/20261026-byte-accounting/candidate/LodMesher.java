package com.shnewbs.deepprimal.core.lod;

import com.shnewbs.deepprimal.core.HorizonMesh;
import java.util.*;

/**
 * Block-shaped interval meshes, with air gaps and thin roofs retained at all three horizontal
 * resolutions.
 */
public final class LodMesher {
  public record Levels(List<HorizonMesh> fine, List<HorizonMesh> medium, List<HorizonMesh> coarse) {
    public List<HorizonMesh> level(int d) {
      return d == 0 ? fine : d == 1 ? medium : coarse;
    }

    public long bytes() {
      return bytes(fine) + bytes(medium) + bytes(coarse);
    }

    private static long bytes(List<HorizonMesh> meshes) {
      long bytes = 0;
      // Indexed traversal avoids stream/iterator allocation during repeated budget checks.
      if (meshes instanceof RandomAccess) {
        for (int i = 0, size = meshes.size(); i < size; i++) bytes += meshes.get(i).gpuBytes();
      } else {
        for (HorizonMesh mesh : meshes) bytes += mesh.gpuBytes();
      }
      return bytes;
    }
  }

  public static Levels build(ColumnarChunk c) {
    int[] colors =
        c.palette().stream()
            .mapToInt(
                s -> {
                  try {
                    return (int) Long.parseLong(s.substring(0, s.indexOf('|')), 16);
                  } catch (RuntimeException e) {
                    throw new IllegalArgumentException("LOD material", e);
                  }
                })
            .toArray();
    int[] offsets = c.offsets(), runs = c.runs(), dense = new int[c.height * 256];
    for (int col = 0; col < 256; col++)
      for (int j = offsets[col]; j < offsets[col + 1]; j += 3)
        if (colors[runs[j + 2]] != 0) Arrays.fill(
            dense,
            col * c.height + runs[j] - c.minY,
            col * c.height + runs[j + 1] - c.minY,
            colors[runs[j + 2]]);
    // Scratch belongs to this build only. Finished mesh arrays/lists are copied before reuse.
    Builder scratch = new Builder();
    long[] mask = new long[16 * Math.max(16, c.height)];
    return new Levels(mesh(c, dense, 1, scratch, mask), mesh(c, dense, 2, scratch, mask),
        mesh(c, dense, 4, scratch, mask));
  }

  private static boolean water(int c) {
    return c != 0 && (c >>> 24) < 255;
  }

  private static boolean exposed(int a, int b) {
    return a != 0 && (b == 0 || !water(a) && water(b));
  }

  private static List<HorizonMesh> mesh(
      ColumnarChunk c, int[] source, int step, Builder b, long[] mask) {
    int n = 16 / step, h = c.height;
    int[] data = step == 1 ? source : new int[n * n * h];
    if (step != 1)
      for (int z = 0; z < n; z++)
        for (int x = 0; x < n; x++)
          for (int y = 0; y < h; y++) {
            int selected = 0;
            samples:
            for (int dz = 0; dz < step; dz++)
              for (int dx = 0; dx < step; dx++) {
                int v = source[((z * step + dz) * 16 + x * step + dx) * h + y];
                if (v != 0 && (selected == 0 || water(selected) && !water(v))) {
                  selected = v;
                  // The first opaque sample wins; later samples cannot replace it.
                  if (!water(v)) break samples;
                }
              }
            data[(z * n + x) * h + y] = selected;
          }
    // Each mask plane is greedily merged only across equal material and fluid height.
    for (int y = 0; y < h; y++) {
      checkInterrupted();
      for (int side = 0; side < 2; side++) {
        for (int z = 0; z < n; z++)
          for (int x = 0; x < n; x++) {
            int base = (z * n + x) * h, v = data[base + y];
            int neighborY = y + (side == 0 ? 1 : -1);
            int other = neighborY >= 0 && neighborY < h ? data[base + neighborY] : 0;
            boolean visible = exposed(v, other);
            // A partial water layer has a free top even below another occupied cell.
            if (side == 0 && water(v) && units(v) < 16 && !water(other)) visible = true;
            mask[z * n + x] = visible ? Integer.toUnsignedLong(v) : 0;
          }
        final int yy = y, face = side;
        rectangles(
            mask,
            n,
            n,
            false,
            (x, z, w, d, key) -> {
              int v = (int) key;
              float x0 = x * step, x1 = (x + w) * step, z0 = z * step, z1 = (z + d) * step;
              float height = c.minY + yy + (face == 0 ? units(v) / 16f : 0);
              if (face == 0)
                b.quad(v, 0, 1, 0, x0, height, z0, x0, height, z1, x1, height, z1, x1, height, z0);
              else
                b.quad(v, 0, -1, 0, x0, height, z0, x1, height, z0, x1, height, z1, x0, height, z1);
            });
      }
    }
    for (int side = 0; side < 4; side++)
      for (int plane = 0; plane < n; plane++) {
        checkInterrupted();
        for (int y = 0; y < h; y++)
          for (int along = 0; along < n; along++) {
            int x = side < 2 ? plane : along, z = side < 2 ? along : plane;
            int nx = x + (side == 0 ? -1 : side == 1 ? 1 : 0);
            int nz = z + (side == 2 ? -1 : side == 3 ? 1 : 0);
            int v = data[(z * n + x) * h + y];
            int other = nx >= 0 && nz >= 0 && nx < n && nz < n ? data[(nz * n + nx) * h + y] : 0;
            int low = 0, high = height(data, (z * n + x) * h, y, h);
            int otherHeight =
                nx >= 0 && nz >= 0 && nx < n && nz < n ? height(data, (nz * n + nx) * h, y, h) : 0;
            boolean visible = exposed(v, other);
            if (water(v) && water(other) && high > otherHeight) {
              visible = true;
              low = otherHeight;
            }
            mask[y * n + along] =
                visible ? (Integer.toUnsignedLong(v) << 10) | (low << 5) | high : 0;
          }
        final int direction = side, axis = plane;
        rectangles(
            mask,
            n,
            h,
            true,
            (along, y, w, d, key) -> {
              int v = (int) (key >>> 10), low = (int) (key >>> 5) & 31, high = (int) key & 31;
              float a = along * step, end = (along + w) * step;
              float lo = c.minY + y + low / 16f, hi = c.minY + y + d - 1 + high / 16f;
              float position = (axis + (direction == 1 || direction == 3 ? 1 : 0)) * step;
              if (direction < 2)
                b.quad(
                    v,
                    direction == 0 ? -1 : 1,
                    0,
                    0,
                    position,
                    lo,
                    a,
                    position,
                    hi,
                    a,
                    position,
                    hi,
                    end,
                    position,
                    lo,
                    end);
              else
                b.quad(
                    v,
                    0,
                    0,
                    direction == 2 ? -1 : 1,
                    a,
                    lo,
                    position,
                    end,
                    lo,
                    position,
                    end,
                    hi,
                    position,
                    a,
                    hi,
                    position);
            });
      }
    return b.finish();
  }

  private static int units(int color) {
    return color == 0 ? 0 : water(color) ? Math.clamp((color >>> 24) - 224, 1, 16) : 16;
  }

  private static int height(int[] data, int base, int y, int h) {
    int v = data[base + y];
    return water(v) && y + 1 < h && water(data[base + y + 1]) ? 16 : units(v);
  }

  private static void checkInterrupted() {
    if (Thread.currentThread().isInterrupted())
      throw new java.util.concurrent.CancellationException();
  }

  @FunctionalInterface
  private interface Rectangle {
    void emit(int x, int y, int width, int height, long key);
  }

  private static void rectangles(
      long[] mask, int width, int height, boolean vertical, Rectangle emit) {
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; ) {
        long key = mask[y * width + x];
        if (key == 0) {
          x++;
          continue;
        }
        int w = 1, h = 1;
        while (x + w < width && mask[y * width + x + w] == key) w++;
        // Fractional water sides have a gap between layers and must remain separate.
        boolean fullHeight = !vertical || (key & 1023) == 16;
        if (fullHeight)
          outer:
          while (y + h < height) {
            for (int dx = 0; dx < w; dx++) if (mask[(y + h) * width + x + dx] != key) break outer;
            h++;
          }
        emit.emit(x, y, w, h, key);
        for (int dy = 0; dy < h; dy++)
          Arrays.fill(mask, (y + dy) * width + x, (y + dy) * width + x + w, 0);
        x += w;
      }
  }

  private static final class Builder {
    final List<HorizonMesh> parts = new ArrayList<>();
    float[] p = new float[4096 * 3];
    int[] c = new int[4096], n = new int[4096], ix = new int[6144];
    int vertices, indices;
    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

    void quad(
        int color,
        int nx,
        int ny,
        int nz,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float x3,
        float y3,
        float z3) {
      if (vertices + 4 > 4096) flush();
      if (parts.size() > 128) throw new IllegalArgumentException("LOD mesh complexity budget");
      int v = vertices,
          normal = (nx * 127 & 255) | ((ny * 127 & 255) << 8) | ((nz * 127 & 255) << 16);
      vertex(x0, y0, z0, color, normal);
      vertex(x1, y1, z1, color, normal);
      vertex(x2, y2, z2, color, normal);
      vertex(x3, y3, z3, color, normal);
      ix[indices++] = v;
      ix[indices++] = v + 1;
      ix[indices++] = v + 2;
      ix[indices++] = v;
      ix[indices++] = v + 2;
      ix[indices++] = v + 3;
    }

    void vertex(float x, float y, float z, int color, int normal) {
      int j = vertices * 3;
      p[j] = x;
      p[j + 1] = y;
      p[j + 2] = z;
      min = Math.min(min, (int) Math.floor(y));
      max = Math.max(max, (int) Math.ceil(y));
      c[vertices] = color;
      n[vertices++] = normal;
    }

    void flush() {
      if (vertices == 0) return;
      parts.add(
          new HorizonMesh(
              Arrays.copyOf(p, vertices * 3),
              Arrays.copyOf(c, vertices),
              Arrays.copyOf(ix, indices),
              Arrays.copyOf(n, vertices),
              new short[0],
              min,
              max));
      vertices = indices = 0;
      // Each GPU part is culled independently; prior parts and empty floor space
      // must not widen this part's conservative bounds.
      min = Integer.MAX_VALUE;
      max = Integer.MIN_VALUE;
    }

    List<HorizonMesh> finish() {
      flush();
      List<HorizonMesh> result = List.copyOf(parts);
      parts.clear();
      return result;
    }
  }

  private LodMesher() {}
}
