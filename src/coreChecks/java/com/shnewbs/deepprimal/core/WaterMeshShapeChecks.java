package com.shnewbs.deepprimal.core;

/** Smooth-water offsets stay bounded and periodic at 16-block page boundaries. */
public final class WaterMeshShapeChecks {
  public static void main(String[] args) {
    run();
  }

  static void run() {
    int checked = 0;
    float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
    float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
    for (int yi = 0; yi <= 64; yi++)
      for (int zi = 0; zi <= 64; zi++)
        for (int xi = 0; xi <= 64; xi++) {
          double x = xi / 4.0, y = yi / 4.0, z = zi / 4.0;
          float dx = WaterMeshShape.offsetX(x, y, z), dz = WaterMeshShape.offsetZ(x, y, z);
          if (Math.abs(dx) > .058 || Math.abs(dz) > .058)
            throw new AssertionError("Water shape exceeded its conservative displacement bound");
          if (Math.abs(dx - WaterMeshShape.offsetX(x + 16, y, z)) > .00001
              || Math.abs(dx - WaterMeshShape.offsetX(x, y + 16, z)) > .00001
              || Math.abs(dz - WaterMeshShape.offsetZ(x, y, z + 16)) > .00001)
            throw new AssertionError("Water shape is discontinuous at a mesh page boundary");
          float flowV = WaterMeshShape.flowingV(x, y, z);
          if (flowV < 0 || flowV >= 1
              || Math.abs(flowV - WaterMeshShape.flowingV(x + 16, y, z)) > .00001
              || Math.abs(flowV - WaterMeshShape.flowingV(x, y + 1, z)) > .00001
              || Math.abs(flowV - WaterMeshShape.flowingV(x, y, z + 16)) > .00001)
            throw new AssertionError("Flowing texture phase is not continuous and periodic");
          minX = Math.min(minX, dx);
          maxX = Math.max(maxX, dx);
          minZ = Math.min(minZ, dz);
          maxZ = Math.max(maxZ, dz);
          checked++;
        }
    if (maxX - minX < .07 || maxZ - minZ < .07)
      throw new AssertionError("Water surface has no measurable organic variation");
    // Side texture coordinates must decrease continuously through every vertical quad.
    // Modulo on each vertex instead jumps from 0 to .75 in the first quarter block.
    int sideUvCases = 0;
    for (int units = 1; units <= 16; units++) {
      float height = units / 16f;
      for (int section = 0; section < 4; section++) {
        float low = height * section / 4f, high = height * (section + 1f) / 4f;
        float lowV = WaterMeshShape.sideV(low), highV = WaterMeshShape.sideV(high);
        if (lowV < 0 || highV < 0 || lowV > 1 || highV > 1
            || Math.abs((lowV - highV) - (high - low)) > 1e-6f)
          throw new AssertionError("Waterfall UV direction reverses inside a quad");
        sideUvCases++;
      }
    }
    System.out.println(
        "WATER_MESH_SHAPE_PASS samples="
            + checked
            + " periodicPages=true boundedDisplacement=true organicVariation=true sideUvCases=" + sideUvCases);
  }

  private WaterMeshShapeChecks() {}
}
