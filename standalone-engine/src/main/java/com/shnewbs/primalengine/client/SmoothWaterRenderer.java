package com.shnewbs.primalengine.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shnewbs.primalengine.*;
import com.shnewbs.primalengine.core.WaterTransport;
import com.shnewbs.primalengine.core.WaterMeshShape;
import com.shnewbs.primalengine.mixin.client.FluidModelAccess;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;

/** Original shared-corner curved patch mesher; uses the game's chunk buffers/materials only. */
public final class SmoothWaterRenderer implements CustomFluidRenderer {
  public static volatile long renderedCells;
  public static volatile long curvedCells;
  private static final int SUB = 2, SIDE_ALONG = 2, SIDE_VERTICAL = 4;

  private static boolean water(BlockAndTintGetter l, BlockPos p) {
    return l.getFluidState(p).getType() == PrimalEngine.WATER.get();
  }

  private static double corner(BlockAndTintGetter l, BlockPos corner) {
    double sum = 0;
    int count = 0;
    for (int dz = -1; dz <= 0; dz++)
      for (int dx = -1; dx <= 0; dx++) {
        var p = corner.offset(dx, 0, dz);
        var s = l.getFluidState(p);
        if (s.getType() != PrimalEngine.WATER.get()) continue;
        if (water(l, p.above())) return 1;
        sum += s.getOwnHeight();
        count++;
      }
    return count == 0 ? 1. / 16 : sum / count;
  }

  @Override
  public boolean renderFluid(
      FluidRenderer renderer,
      FluidState state,
      BlockAndTintGetter l,
      BlockPos p,
      FluidRenderer.Output output,
      BlockState block) {
    boolean top = !water(l, p.above()) && !l.getBlockState(p.above()).isSolid();
    boolean exposed = top;
    for (var d : Direction.values())
      exposed |= !water(l, p.relative(d)) && !l.getBlockState(p.relative(d)).isSolid();
    if (!exposed) return true;
    var model = ((FluidModelAccess) renderer).primalengine$models().get(state);
    var out = output.getBuilder(model.layer());
    var still = model.stillMaterial().sprite();
    var flowing = model.flowingMaterial().sprite();
    // Bilinear biome color for smoother tint transitions
    int c1 = net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(l, p);
    int c2 = net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(l, p.east());
    int c3 = net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(l, p.south());
    int c4 = net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(l, p.east().south());
    int r = (((c1 >> 16) & 255) + ((c2 >> 16) & 255) + ((c3 >> 16) & 255) + ((c4 >> 16) & 255)) >> 2;
    int g = (((c1 >> 8) & 255) + ((c2 >> 8) & 255) + ((c3 >> 8) & 255) + ((c4 >> 8) & 255)) >> 2;
    int b = ((c1 & 255) + (c2 & 255) + (c3 & 255) + (c4 & 255)) >> 2;
    int color = 0xff000000 | (r << 16) | (g << 8) | b;
    int light = LevelRenderer.getLightCoords(l, p.above());
    float x = p.getX() & 15, y = p.getY() & 15, z = p.getZ() & 15;
    double nw = corner(l, p),
        ne = corner(l, p.east()),
        sw = corner(l, p.south()),
        se = corner(l, p.east().south());
    double center = water(l, p.above()) ? 1 : state.getOwnHeight();
    boolean curved = Math.abs(center - (nw + ne + sw + se) * .25) >= 1e-5;
    int divisions = SUB;
    if (top && curved) curvedCells++;
    if (top)
      for (int iz = 0; iz < divisions; iz++)
        for (int ix = 0; ix < divisions; ix++) {
          float u = ix / (float) divisions,
              v = iz / (float) divisions,
              uu = (ix + 1) / (float) divisions,
              vv = (iz + 1) / (float) divisions;
          face(
              out,
              still,
              x,
              y,
              z,
              u,
              (float) WaterTransport.surface(nw, ne, sw, se, center, u, v),
              v,
              u,
              (float) WaterTransport.surface(nw, ne, sw, se, center, u, vv),
              vv,
              uu,
              (float) WaterTransport.surface(nw, ne, sw, se, center, uu, vv),
              vv,
              uu,
              (float) WaterTransport.surface(nw, ne, sw, se, center, uu, v),
              v,
              color,
              light,
              0);
        }
    for (var d : Direction.Plane.HORIZONTAL) {
      var adjacent = p.relative(d);
      if (water(l, adjacent) || l.getBlockState(adjacent).isSolid()) continue;
      float ax = d == Direction.EAST ? 1 : 0, az = d == Direction.SOUTH ? 1 : 0;
      float bx = d == Direction.WEST ? 0 : 1, bz = d == Direction.NORTH ? 0 : 1;
      if (d == Direction.WEST || d == Direction.EAST) {
        bx = ax;
        bz = 1;
        az = 0;
      } else {
        ax = 0;
        bx = 1;
        bz = az;
      }
      for (int across = 0; across < SIDE_ALONG; across++) {
        float from = across / (float) SIDE_ALONG, to = (across + 1f) / SIDE_ALONG;
        float x0 = ax + (bx - ax) * from, z0 = az + (bz - az) * from;
        float x1 = ax + (bx - ax) * to, z1 = az + (bz - az) * to;
        float h0 =
            (float) WaterTransport.surface(nw, ne, sw, se, center, x0, z0),
            h1 =
                (float) WaterTransport.surface(nw, ne, sw, se, center, x1, z1);
        for (int vertical = 0; vertical < SIDE_VERTICAL; vertical++) {
          float low = vertical / (float) SIDE_VERTICAL, high = (vertical + 1f) / SIDE_VERTICAL;
          face(
              out,
              flowing,
              x,
              y,
              z,
              x0,
              h0 * low,
              z0,
              x1,
              h1 * low,
              z1,
              x1,
              h1 * high,
              z1,
              x0,
              h0 * high,
              z0,
              color,
              light,
              d.getAxis() == Direction.Axis.X ? 1 : 2);
        }
      }
    }
    if (!water(l, p.below()) && !l.getBlockState(p.below()).isSolid())
      face(out, still, x, y, z, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, color, light, 0);
    renderedCells++;
    return true;
  }

  private static void face(
      VertexConsumer out,
      TextureAtlasSprite sprite,
      float x,
      float y,
      float z,
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
      float z3,
      int color,
      int light,
      int mode) {
    float px0 = WaterMeshShape.x(x + x0, y + y0, z + z0);
    float py0 = y + y0;
    float pz0 = WaterMeshShape.z(x + x0, y + y0, z + z0);
    float px1 = WaterMeshShape.x(x + x1, y + y1, z + z1);
    float py1 = y + y1;
    float pz1 = WaterMeshShape.z(x + x1, y + y1, z + z1);
    float px2 = WaterMeshShape.x(x + x2, y + y2, z + z2);
    float py2 = y + y2;
    float pz2 = WaterMeshShape.z(x + x2, y + y2, z + z2);
    float ax = px1 - px0, ay = py1 - py0, az = pz1 - pz0,
        bx = px2 - px0, by = py2 - py0, bz = pz2 - pz0;
    float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
    float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
    if (length < 1e-6) return;
    nx /= length;
    ny /= length;
    nz /= length;
    vertex(out, sprite, x, y, z, x0, y0, z0, color, light, mode, nx, ny, nz);
    vertex(out, sprite, x, y, z, x1, y1, z1, color, light, mode, nx, ny, nz);
    vertex(out, sprite, x, y, z, x2, y2, z2, color, light, mode, nx, ny, nz);
    vertex(out, sprite, x, y, z, x3, y3, z3, color, light, mode, nx, ny, nz);
    vertex(out, sprite, x, y, z, x3, y3, z3, color, light, mode, -nx, -ny, -nz);
    vertex(out, sprite, x, y, z, x2, y2, z2, color, light, mode, -nx, -ny, -nz);
    vertex(out, sprite, x, y, z, x1, y1, z1, color, light, mode, -nx, -ny, -nz);
    vertex(out, sprite, x, y, z, x0, y0, z0, color, light, mode, -nx, -ny, -nz);
  }

  private static void vertex(
      VertexConsumer out,
      TextureAtlasSprite sprite,
      float x,
      float y,
      float z,
      float dx,
      float dy,
      float dz,
      int color,
      int light,
      int mode,
      float nx,
      float ny,
      float nz) {
    float u = mode == 1 ? dz : dx,
        v = mode == 0 ? dz : WaterMeshShape.flowingV(x + dx, y + dy, z + dz);
    out.addVertex(
        WaterMeshShape.x(x + dx, y + dy, z + dz),
        y + dy,
        WaterMeshShape.z(x + dx, y + dy, z + dz),
        color,
        sprite.getU(u),
        sprite.getV(v),
        OverlayTexture.NO_OVERLAY,
        light,
        nx,
        ny,
        nz);
  }
}
