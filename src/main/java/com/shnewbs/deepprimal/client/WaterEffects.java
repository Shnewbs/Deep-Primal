package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.EngineWaterBlock;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * One admission budget for optional effects. Native particles share the game's existing renderer.
 */
public final class WaterEffects {
  public static void tick() {
    var mc = Minecraft.getInstance();
    var level = mc.level;
    if (level == null || mc.player == null || mc.isPaused()) return;
    int remaining = WaterVisualConfig.PARTICLES.get();
    if (remaining == 0) return;
    var random = level.getRandom();
    var center = mc.player.blockPosition();
    boolean particular = RendererOwnership.installed("particular");
    boolean wakes =
        RendererOwnership.installed("wakes")
            || RendererOwnership.installed("wakesreforged")
            || RendererOwnership.installed("wakes_reforged");
    if (WaterVisualConfig.WAKES.get()
        && !particular
        && !wakes
        && !com.shnewbs.deepprimal.client.render.WakeRenderer.available()) {
      int examined = 0;
      for (var entity : level.entitiesForRendering()) {
        if (++examined > 128 || remaining <= 0) break;
        if (!(entity instanceof AbstractBoat) && !(entity instanceof LivingEntity)) continue;
        if (entity.distanceToSqr(mc.player) > 48 * 48
            || !entity.isInWater()
            || entity.getDeltaMovement().horizontalDistanceSqr() < .001) continue;
        var v = entity.getDeltaMovement();
        level.addParticle(
            ParticleTypes.SPLASH,
            entity.getX() - v.x * 2,
            entity.getY() + .1,
            entity.getZ() - v.z * 2,
            -v.x * .2,
            .025,
            -v.z * .2);
        remaining--;
      }
    }
    boolean ambience = level.getGameTime() % 89 == 0;
    for (int trial = 0; trial < 24 && remaining > 0; trial++) {
      int radius = trial < 8 ? 8 : 24;
      int x = center.getX() + random.nextInt(radius * 2 + 1) - radius;
      int z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
      var probe = new BlockPos(x, center.getY(), z);
      if (!level.hasChunkAt(probe)) continue;
      var top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, probe);
      if (Math.abs(top.getY() - center.getY()) > 32) continue;
      if (WaterVisualConfig.SNOW.get()
          && level.isRaining()
          && (com.shnewbs.deepprimal.water.RiverConfig.GLOBAL_WINTER.get()
              || level.getBiome(top).value().coldEnoughToSnow(top, level.getSeaLevel()))) {
        level.addParticle(
            ParticleTypes.SNOWFLAKE,
            x + random.nextDouble(),
            top.getY() + 2 + random.nextDouble() * 4,
            z + random.nextDouble(),
            .015,
            -.04,
            .005);
        remaining--;
      } else if (!particular && (WaterVisualConfig.CASCADES.get() || WaterVisualConfig.WAVES.get())) {
        var p = top.below();
        var state = level.getBlockState(p);
        if (!state.getFluidState().is(FluidTags.WATER) || !level.isEmptyBlock(top)) continue;
        double y = p.getY() + state.getFluidState().getOwnHeight();
        Direction edge = WaterVisualConfig.CASCADES.get() ? landingEdge(level, p) : null;
        if (edge != null) {
          double px = x + .32 + random.nextDouble() * .36 + edge.getStepX() * .28;
          double pz = z + .32 + random.nextDouble() * .36 + edge.getStepZ() * .28;
          level.addParticle(ParticleTypes.SPLASH, px, y + .035, pz,
              -edge.getStepX() * .08, .08 + random.nextDouble() * .08, -edge.getStepZ() * .08);
          if (--remaining > 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, px, y + .025, pz, 0, .025, 0);
            remaining--;
          }
          if (ambience) {
            level.playLocalSound(px, y, pz, SoundEvents.WATER_AMBIENT, SoundSource.AMBIENT, .43f, .78f, false);
            ambience = false;
          }
        } else if (state.is(EngineRegistry.WATER_BLOCK.get())) {
          if (WaterVisualConfig.WAVES.get() && state.getValue(EngineWaterBlock.OCEAN)) {
            Direction beach = shoreEdge(level, p);
            if (beach != null && random.nextInt(3) == 0) {
              double px = x + .5 + beach.getStepX() * .37, pz = z + .5 + beach.getStepZ() * .37;
              level.addParticle(ParticleTypes.SPLASH, px, y + .025, pz, beach.getStepX() * .045, .022, beach.getStepZ() * .045);
              remaining--;
            }
          } else if (WaterVisualConfig.CASCADES.get() && riffle(level, p, state) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, x + random.nextDouble(), y + .015,
                z + random.nextDouble(), 0, .015, 0);
            remaining--;
          }
        }
      }
    }
  }

  /** Called on a visible lower pool. A higher wet neighbour must actually touch its open edge. */
  static Direction landingEdge(LevelReader level, BlockPos pool) {
    if (pool.getY() >= level.getMaxY() || !level.hasChunkAt(pool) || !level.isEmptyBlock(pool.above())
        || !level.getFluidState(pool).is(FluidTags.WATER)) return null;
    for (Direction d : Direction.Plane.HORIZONTAL) {
      var side = pool.relative(d);
      if (!level.hasChunkAt(side) || !level.getFluidState(side.above()).is(FluidTags.WATER)) continue;
      if (level.getFluidState(side).is(FluidTags.WATER)) return d;
    }
    return null;
  }

  static Direction shoreEdge(LevelReader level, BlockPos water) {
    for (Direction d : Direction.Plane.HORIZONTAL) {
      var bank = water.relative(d);
      if (level.hasChunkAt(bank) && level.getBlockState(bank).isFaceSturdy(level, bank, d.getOpposite())
          && level.getFluidState(bank).isEmpty() && level.isEmptyBlock(bank.above())) return d;
    }
    return null;
  }

  static boolean riffle(LevelReader level, BlockPos p, BlockState water) {
    if (water.getValue(EngineWaterBlock.OCEAN) || water.getValue(EngineFluid.FLOW) == 16
        || water.getValue(EngineFluid.SPEED) < 4) return false;
    var bed = p.below();
    if (level.getFluidState(bed).is(FluidTags.WATER)) bed = bed.below();
    return level.getBlockState(bed).isFaceSturdy(level, bed, Direction.UP);
  }

  private WaterEffects() {}
}
