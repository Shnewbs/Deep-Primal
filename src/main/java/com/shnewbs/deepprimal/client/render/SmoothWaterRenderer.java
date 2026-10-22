package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shnewbs.deepprimal.core.WaterTransport;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.mixin.client.FluidModelAccess;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;

/** Original shared-corner bilinear patch mesher; uses the game's chunk buffers/materials only. */
public final class SmoothWaterRenderer implements CustomFluidRenderer {
  public static volatile long renderedCells;
  public static volatile long curvedCells;
  private static final int SUB = 2;

  private static boolean water(BlockAndTintGetter l, BlockPos p) {
    return l.getFluidState(p).getType() == EngineRegistry.WATER.get();
  }

  private static double corner(BlockAndTintGetter l, BlockPos corner) {
    double sum = 0;
    int count = 0;
    for (int dz = -1; dz <= 0; dz++)
      for (int dx = -1; dx <= 0; dx++) {
        var p = corner.offset(dx, 0, dz);
        var s = l.getFluidState(p);
        if (s.getType() != EngineRegistry.WATER.get()) continue;
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
    if (AnimatedWaterRenderer.ownsWater()) return true;
    if (!com.shnewbs.deepprimal.settings.RendererOwnership.owned()
        || !com.shnewbs.deepprimal.settings.WaterVisualConfig.SMOOTH.get()) return false;
    boolean top = !water(l, p.above()) && !l.getBlockState(p.above()).isSolid();
    boolean exposed = top;
    for (var d : Direction.values())
      exposed |= !water(l, p.relative(d)) && !l.getBlockState(p.relative(d)).isSolid();
    if (!exposed) return true;
    var model = ((FluidModelAccess) renderer).deepprimal$models().get(state);
    var out = output.getBuilder(model.layer());
    var sprite = model.stillMaterial().sprite();
    int color =
        0xff000000
            | (net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(l, p) & 0xffffff);
    int light = LevelRenderer.getLightCoords(l, p.above());
    float x = p.getX() & 15, y = p.getY() & 15, z = p.getZ() & 15;
    double nw = corner(l, p),
        ne = corner(l, p.east()),
        sw = corner(l, p.south()),
        se = corner(l, p.east().south());
    double center = water(l, p.above()) ? 1 : state.getOwnHeight();
    int divisions=Math.abs(nw-ne-sw+se)<1e-5?1:SUB;
    if (top && divisions > 1) curvedCells++;
    if (top)
      for (int iz = 0; iz < divisions; iz++)
        for (int ix = 0; ix < divisions; ix++) {
          float u = ix / (float) divisions,
              v = iz / (float) divisions,
              uu = (ix + 1) / (float) divisions,
              vv = (iz + 1) / (float) divisions;
          face(
              out,
              sprite,
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
      {
        float x0 = ax, z0 = az, x1 = bx, z1 = bz;
        float h0 = (float) WaterTransport.surface(nw, ne, sw, se, center, x0, z0),
            h1 = (float) WaterTransport.surface(nw, ne, sw, se, center, x1, z1);
        face(
            out,
            sprite,
            x,
            y,
            z,
            x0,
            0,
            z0,
            x1,
            0,
            z1,
            x1,
            h1,
            z1,
            x0,
            h0,
            z0,
            color,
            light,
            d.getAxis() == Direction.Axis.X ? 1 : 2);
      }
    }
    if (!water(l, p.below()) && !l.getBlockState(p.below()).isSolid())
      face(out, sprite, x, y, z, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, color, light, 0);
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
    float ax = x1 - x0, ay = y1 - y0, az = z1 - z0, bx = x2 - x0, by = y2 - y0, bz = z2 - z0;
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
    float u = mode == 1 ? dz : dx, v = mode == 0 ? dz : 1 - dy;
    out.addVertex(
        x + dx,
        y + dy,
        z + dz,
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
