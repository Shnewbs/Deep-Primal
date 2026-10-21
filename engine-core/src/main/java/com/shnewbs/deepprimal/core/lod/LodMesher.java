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
      return java.util.stream.Stream.of(fine, medium, coarse)
          .flatMap(List::stream)
          .mapToLong(HorizonMesh::gpuBytes)
          .sum();
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
        Arrays.fill(
            dense,
            col * c.height + runs[j] - c.minY,
            col * c.height + runs[j + 1] - c.minY,
            colors[runs[j + 2]]);
    return new Levels(mesh(c, dense, 1), mesh(c, dense, 2), mesh(c, dense, 4));
  }

  private static boolean water(int c) {
    return c != 0 && (c >>> 24) < 255;
  }

  private static boolean exposed(int a, int b) {
    return a != 0 && (b == 0 || !water(a) && water(b));
  }

  private static List<HorizonMesh> mesh(ColumnarChunk c, int[] source, int step) {
    int n = 16 / step, h = c.height;
    int[] data = step == 1 ? source : new int[n * n * h];
    if (step != 1)
      for (int z = 0; z < n; z++)
        for (int x = 0; x < n; x++)
          for (int y = 0; y < h; y++) {
            int selected = 0;
            for (int dz = 0; dz < step; dz++)
              for (int dx = 0; dx < step; dx++) {
                int v = source[((z * step + dz) * 16 + x * step + dx) * h + y];
                if (v != 0 && (selected == 0 || water(selected) && !water(v))) selected = v;
              }
            data[(z * n + x) * h + y] = selected;
          }
    Builder b = new Builder(c.minY);
    for (int z = 0; z < n; z++)
      for (int x = 0; x < n; x++) {
        if (Thread.currentThread().isInterrupted())
          throw new java.util.concurrent.CancellationException();
        int base = (z * n + x) * h;
        float x0 = x * step, x1 = x0 + step, z0 = z * step, z1 = z0 + step;
        for (int y = 0; y < h; y++) {
          int v = data[base + y];
          if (v == 0) continue;
          float yy = c.minY + y;
          float surface = yy + (water(v) ? Math.clamp(((v >>> 24) - 224) / 16f, 1 / 16f, 1) : 1);
          if (exposed(v, y + 1 < h ? data[base + y + 1] : 0))
            b.quad(v, 0, 1, 0, x0, surface, z0, x0, surface, z1, x1, surface, z1, x1, surface, z0);
          if (exposed(v, y > 0 ? data[base + y - 1] : 0))
            b.quad(v, 0, -1, 0, x0, yy, z0, x1, yy, z0, x1, yy, z1, x0, yy, z1);
        }
        for (int side = 0; side < 4; side++) {
          int nx = x + (side == 0 ? -1 : side == 1 ? 1 : 0),
              nz = z + (side == 2 ? -1 : side == 3 ? 1 : 0),
              neighbor = nx >= 0 && nz >= 0 && nx < n && nz < n ? (nz * n + nx) * h : -1;
          for (int y = 0; y < h; ) {
            int v = data[base + y];
            if (!exposed(v, neighbor < 0 ? 0 : data[neighbor + y])) {
              y++;
              continue;
            }
            int end = y + 1;
            while (end < h
                && data[base + end] == v
                && exposed(v, neighbor < 0 ? 0 : data[neighbor + end])) end++;
            float lo = c.minY + y,
                hi =
                    c.minY
                        + end
                        - (water(v) ? 1 - Math.clamp(((v >>> 24) - 224) / 16f, 1 / 16f, 1) : 0);
            if (side < 2) {
              float xx = side == 0 ? x0 : x1;
              b.quad(v, side == 0 ? -1 : 1, 0, 0, xx, lo, z0, xx, hi, z0, xx, hi, z1, xx, lo, z1);
            } else {
              float zz = side == 2 ? z0 : z1;
              b.quad(v, 0, 0, side == 2 ? -1 : 1, x0, lo, zz, x1, lo, zz, x1, hi, zz, x0, hi, zz);
            }
            y = end;
          }
        }
      }
    return b.finish();
  }

  private static final class Builder {
    final List<HorizonMesh> parts = new ArrayList<>();
    float[] p = new float[4096 * 3];
    int[] c = new int[4096], n = new int[4096], ix = new int[6144];
    int vertices, indices, min, max;

    Builder(int floor) {
      min = max = floor;
    }

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
    }

    List<HorizonMesh> finish() {
      flush();
      return List.copyOf(parts);
    }
  }

  private LodMesher() {}
}
