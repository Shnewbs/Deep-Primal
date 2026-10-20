package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.levelgen.Heightmap;

/** One admission budget for optional effects. Native particles share the game's existing renderer. */
public final class WaterEffects {
  public static void tick() {
    var mc=Minecraft.getInstance();var level=mc.level;
    if(level==null || mc.player==null || mc.isPaused())return;
    int remaining=WaterVisualConfig.PARTICLES.get();
    if(remaining==0)return;
    var random=level.getRandom();var center=mc.player.blockPosition();
    boolean particular=RendererOwnership.installed("particular");
    boolean wakes=RendererOwnership.installed("wakes") || RendererOwnership.installed("wakesreforged") || RendererOwnership.installed("wakes_reforged");
    if(WaterVisualConfig.WAKES.get() && !particular && !wakes) {
      int examined=0;
      for(var entity:level.entitiesForRendering()) {
        if(++examined>128 || remaining<=0)break;
        if(!(entity instanceof AbstractBoat) && !(entity instanceof LivingEntity))continue;
        if(entity.distanceToSqr(mc.player)>48*48 || !entity.isInWater() || entity.getDeltaMovement().horizontalDistanceSqr()<.001)continue;
        var v=entity.getDeltaMovement();
        level.addParticle(ParticleTypes.SPLASH,entity.getX()-v.x*2,entity.getY()+.1,entity.getZ()-v.z*2,-v.x*.2,.025,-v.z*.2);
        remaining--;
      }
    }
    for(int trial=0;trial<24 && remaining>0;trial++) {
      int x=center.getX()+random.nextInt(49)-24,z=center.getZ()+random.nextInt(49)-24;
      var probe=new BlockPos(x,center.getY(),z);if(!level.hasChunkAt(probe))continue;
      var top=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,probe);
      if(Math.abs(top.getY()-center.getY())>32)continue;
      if(WaterVisualConfig.SNOW.get() && level.isRaining() && level.getBiome(top).value().coldEnoughToSnow(top,level.getSeaLevel())) {
        level.addParticle(ParticleTypes.SNOWFLAKE,x+random.nextDouble(),top.getY()+2+random.nextDouble()*4,z+random.nextDouble(),.015,-.04,.005);
        remaining--;
      } else if(WaterVisualConfig.CASCADES.get() && !particular && level.getFluidState(top.below()).is(FluidTags.WATER)) {
        var p=top.below();
        for(Direction d:Direction.Plane.HORIZONTAL) {
          var side=p.relative(d);
          if(level.isEmptyBlock(side) && level.isEmptyBlock(side.below())) {
            level.addParticle(ParticleTypes.SPLASH,x+.5+d.getStepX()*.5,p.getY()+.8,z+.5+d.getStepZ()*.5,d.getStepX()*.03,.03,d.getStepZ()*.03);
            remaining--;break;
          }
        }
      }
    }
  }
  private WaterEffects() {}
}
