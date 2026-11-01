package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Immutable 16-cube plus one-cell halo. Worker code never reads a live world. */
public final class WaterPatchMesher {
  public static final int SIZE = 18, CELLS = SIZE * SIZE * SIZE, STRIDE = 14;

  private static final float[] FLOW_X = new float[16], FLOW_Z = new float[16];

  static {
    for (int d = 0; d < 16; d++) {
      FLOW_X[d] = (float) Math.cos(d * Math.PI / 8);
      FLOW_Z[d] = (float) Math.sin(d * Math.PI / 8);
    }
  }

  /** Flow is captured with quantity; null retains the legacy still-water material. */
  public record Snapshot(byte[] units, byte[] solid, int[] lights, int[] colors, byte[] flow) {
    public Snapshot(byte[] units, byte[] solid, int[] lights, int[] colors) {
      this(units, solid, lights, colors, null);
    }

    public int direction(int x, int y, int z) {
      int raw = flow == null ? 16 : flow[index(x, y, z)] & 255;
      // A malformed captured byte is still water, never an undefined material ID.
      return raw <= 16 ? raw : 16;
    }

    public int water(int x, int y, int z) {
      // Only 0..16 are valid volume units. Malformed signed captures must not
      // create phantom water faces or feed invalid heights to the mesher.
      int raw = units[index(x, y, z)] & 255;
      return raw <= WaterTransport.CAPACITY ? raw : 0;
    }

    public boolean solid(int x, int y, int z) {
      return solid[index(x, y, z)] != 0;
    }
  }

  public record Mesh(float[] vertices) {
    public int count() {
      return vertices.length / STRIDE;
    }
  }

  public static int index(int x, int y, int z) {
    return ((y + 1) * SIZE + z + 1) * SIZE + x + 1;
  }

  // Slot 10 retains the face in its low three bits and a flowing-direction code above it.
  // Code zero means still; codes 1..16 mean directions 0..15. All values are exact floats.
  public static int face(float material) {
    return (int) material & 7;
  }

  public static int direction(float material) {
    int code = (int) material >>> 3;
    return code == 0 ? 16 : code - 1;
  }

  /** Periodic phase at a world cell origin; double intermediates retain distant/negative seams. */
  public static float flowUOffset(int direction, int worldX, int worldZ) {
    if (direction < 0 || direction >= 16) return 0;
    double phase = .5 * ((double) FLOW_Z[direction] * worldX - (double) FLOW_X[direction] * worldZ);
    return (float) (phase - Math.floor(phase));
  }

  public static float flowVOffset(int direction, int worldX, int worldZ) {
    if (direction < 0 || direction >= 16) return 0;
    double phase = .5 * ((double) FLOW_X[direction] * worldX + (double) FLOW_Z[direction] * worldZ);
    return (float) (phase - Math.floor(phase));
  }

  public static float flowX(int direction) { return FLOW_X[direction]; }
  public static float flowZ(int direction) { return FLOW_Z[direction]; }

  private int flowMaterial;
  private float flowX, flowZ;
  private float[] vertices = new float[8192];
  private int size;
  private final Snapshot current, previous;
  private final float[] currentCorners = new float[17 * 16 * 17];
  private final float[] previousCorners;

  private WaterPatchMesher(Snapshot current, Snapshot previous) {
    this.current = current;
    this.previous = previous == null ? current : previous;
    Arrays.fill(currentCorners, Float.NaN);
    previousCorners = this.previous == current ? currentCorners : new float[currentCorners.length];
    if (previousCorners != currentCorners) Arrays.fill(previousCorners, Float.NaN);
  }

  public static Mesh build(Snapshot current, Snapshot previous, boolean smooth) {
    var m = new WaterPatchMesher(current, previous);
    for (int y = 0; y < 16; y++)
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          if (Thread.currentThread().isInterrupted())
            throw new java.util.concurrent.CancellationException();
          if (current.water(x, y, z) == 0 && m.previous.water(x, y, z) == 0) continue;
          int direction = (current.water(x, y, z) > 0 ? current : m.previous).direction(x, y, z);
          m.flowMaterial = direction < 16 ? (direction + 1) << 3 : 0;
          if (m.flowMaterial != 0) {
            m.flowX = FLOW_X[direction];
            m.flowZ = FLOW_Z[direction];
          }
          boolean top = m.face(current, x, y, z, 0), oldTop = m.face(m.previous, x, y, z, 0);
          if (top || oldTop) {
            int sub =
                smooth && (!m.planar(current, x, y, z) || !m.planar(m.previous, x, y, z)) ? 2 : 1;
            for (int iz = 0; iz < sub; iz++)
              for (int ix = 0; ix < sub; ix++) {
                float a = ix / (float) sub,
                    b = iz / (float) sub,
                    c = (ix + 1) / (float) sub,
                    d = (iz + 1) / (float) sub;
                m.vertex(x, y, z, a, b, 0, true, oldTop, top);
                m.vertex(x, y, z, a, d, 0, true, oldTop, top);
                m.vertex(x, y, z, c, d, 0, true, oldTop, top);
                m.vertex(x, y, z, c, b, 0, true, oldTop, top);
              }
          }
          for (int side = 1; side <= 4; side++) {
            boolean next = m.face(current, x, y, z, side), old = m.face(m.previous, x, y, z, side);
            if (!next && !old) continue;
            float ax = side == 2 ? 1 : 0,
                az = side == 3 ? 1 : 0,
                bx = side == 4 ? 0 : 1,
                bz = side == 1 ? 0 : 1;
            if (side == 2 || side == 4) {
              bx = ax;
              az = 0;
              bz = 1;
            } else {
              ax = 0;
              bx = 1;
              bz = az;
            }
            m.vertex(x, y, z, ax, az, side, false, old, next);
            m.vertex(x, y, z, bx, bz, side, false, old, next);
            m.vertex(x, y, z, bx, bz, side, true, old, next);
            m.vertex(x, y, z, ax, az, side, true, old, next);
          }
          boolean bottom = m.face(current, x, y, z, 5), oldBottom = m.face(m.previous, x, y, z, 5);
          if (bottom || oldBottom) {
            m.vertex(x, y, z, 0, 0, 5, false, oldBottom, bottom);
            m.vertex(x, y, z, 1, 0, 5, false, oldBottom, bottom);
            m.vertex(x, y, z, 1, 1, 5, false, oldBottom, bottom);
            m.vertex(x, y, z, 0, 1, 5, false, oldBottom, bottom);
          }
        }
    return new Mesh(Arrays.copyOf(m.vertices, m.size));
  }

  private boolean face(Snapshot s, int x, int y, int z, int face) {
    int amount = s.water(x, y, z);
    if (amount == 0) return false;
    int dx = face == 2 ? 1 : face == 4 ? -1 : 0,
        dy = face == 0 ? 1 : face == 5 ? -1 : 0,
        dz = face == 1 ? -1 : face == 3 ? 1 : 0;
    return s.water(x + dx, y + dy, z + dz) == 0
        && (!s.solid(x + dx, y + dy, z + dz) || (face == 0 && amount < 16));
  }


  /**
   * Presentation identity of a vertex on a one-cell step. Bit zero raises its
   * history anchor to the upper layer; bit one marks a connection across layers.
   * Half-cell vertices use the corners with nonzero bilinear weight.
   */
  public static int surfaceConnection(Snapshot s, int x2, int y, int z2) {
    if (s == null) return 0;
    int result = 0;
    for (int gz = z2 / 2; gz <= (z2 + 1) / 2; gz++)
      for (int gx = x2 / 2; gx <= (x2 + 1) / 2; gx++)
        for (int dz = -1; dz <= 0; dz++)
          for (int dx = -1; dx <= 0; dx++) {
            int x = gx + dx, z = gz + dz, here = s.water(x, y, z);
            if ((here > 0 || s.solid(x, y, z)) && s.water(x, y + 1, z) > 0)
              result |= 3;
            else if (here == 0 && s.water(x, y - 1, z) > 0)
              result |= 2;
          }
    return result;
  }

  private float corner(Snapshot s, int x, int y, int z) {
    int key = (y * 17 + z) * 17 + x;
    float[] corners = s == current ? currentCorners : previousCorners;
    float cached = corners[key];
    if (!Float.isNaN(cached)) return cached;
    float sum = 0;
    int count = 0;
    boolean supportedFilm = false, supportedAbove = false;
    for (int dz = -1; dz <= 0; dz++)
      for (int dx = -1; dx <= 0; dx++) {
        int n = s.water(x + dx, y, z + dz);
        boolean solidHere = s.solid(x + dx, y, z + dz);
        int above = n > 0 || solidHere ? s.water(x + dx, y + 1, z + dz) : 0;
        supportedFilm |= n > 0 && s.solid(x + dx, y - 1, z + dz);
        supportedAbove |= solidHere && above > 0;
        double height =
            WaterTransport.surfaceColumn(
                n == 0 ? s.water(x + dx, y - 1, z + dz) : 0,
                n,
                above, solidHere);
        if (!Double.isFinite(height)) continue;
        sum += (float) height;
        count++;
      }
    // A downhill neighbour may pull the average beneath a wet cell's solid bed.
    // Apply the shared corner's support constraint, never a per-cell clamp.
    return corners[key] = (float)WaterTransport.supportedSurface(count==0?0:sum/count,supportedFilm,supportedAbove);
  }

  private float height(Snapshot s, int x, int y, int z, float u, float v) {
    float a = corner(s, x, y, z),
        b = corner(s, x + 1, y, z),
        c = corner(s, x, y, z + 1),
        d = corner(s, x + 1, y, z + 1);
    return (float) WaterTransport.surface(a, b, c, d, 0, u, v);
  }

  private boolean planar(Snapshot s, int x, int y, int z) {
    return Math.abs(
            corner(s, x, y, z)
                - corner(s, x + 1, y, z)
                - corner(s, x, y, z + 1)
                + corner(s, x + 1, y, z + 1))
        < .00001;
  }

  private void vertex(
      int x, int y, int z, float u, float v, int face, boolean moving, boolean was, boolean next) {
    if (size + STRIDE > vertices.length) vertices = Arrays.copyOf(vertices, vertices.length * 2);
    float target = moving ? height(current, x, y, z, u, v) : 0,
        old = moving ? height(previous, x, y, z, u, v) : 0;
    if (moving && face >= 1 && face <= 4) {
      // A connected falling column fills the wall up to the next cell exactly.
      // Averaging a lower tributary into this endpoint opens a gap beneath that cell.
      if (current.water(x, y + 1, z) > 0) target = 1;
      if (previous.water(x, y + 1, z) > 0) old = 1;
    }
    Snapshot material = current.water(x, y, z) > 0 ? current : previous;
    int color = material.colors[z * 16 + x], light = material.lights[index(x, y, z)];
    float shade = face == 0 ? 1 : face == 5 ? .5f : face == 2 || face == 4 ? .8f : .9f;
    color =
        0xff000000
            | ((int) (((color >> 16) & 255) * shade) << 16)
            | ((int) (((color >> 8) & 255) * shade) << 8)
            | (int) ((color & 255) * shade);
    float textureU = (face == 2 || face == 4) ? v : u,
        textureV = face == 0 ? v : Math.clamp(1 - target, 0, 1);
    if (flowMaterial != 0) {
      if (face == 0) {
        // The flowing sprite covers twice the still sprite's texel width. Rotate its
        // central half tile so increasing V follows the captured horizontal flow.
        float a = textureU - .5f, b = textureV - .5f;
        textureU = .5f + .5f * (flowZ * a - flowX * b);
        textureV = .5f + .5f * (flowX * a + flowZ * b);
      } else {
        textureU *= .5f;
        textureV *= .5f;
      }
    }
    vertices[size++] = x + u;
    vertices[size++] = y;
    vertices[size++] = z + v;
    vertices[size++] = target;
    vertices[size++] = old;
    vertices[size++] = textureU;
    vertices[size++] = textureV;
    vertices[size++] = Float.intBitsToFloat(color);
    vertices[size++] = Float.intBitsToFloat(light);
    vertices[size++] = x | (z << 4) | (y << 8);
    vertices[size++] = face | flowMaterial;
    vertices[size++] = moving ? 1 : 0;
    vertices[size++] = was ? 1 : 0;
    vertices[size++] = next ? 1 : 0;
  }
}
