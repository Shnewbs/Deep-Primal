package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.water.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;

/** Real world/weather/collision regressions. Isolated fixture columns, never included in the mod. */
final class WaterEnvironmentChecks {
  static void run(ServerLevel level) {
    var base=new BlockPos(36,500,8);level.getChunkAt(base);
    level.getServer().setWeatherParameters(0,6000,true,false);
    level.setRainLevel(1);
    level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"time set midnight");
    biome(level,"snowy_plains");
    clear(level,base);
    for(int i=0;i<100;i++)level.tickPrecipitation(base);
    require(snowCount(level,base)==24,"Native snow cap: "+snowCount(level,base));
    RiverConfig.SNOW_CAP.set(10);
    clear(level,base);
    for(int i=0;i<80;i++)level.tickPrecipitation(base);
    require(snowCount(level,base)==10,"Partial snow cap: "+snowCount(level,base));
    RiverConfig.SNOW_CAP.set(24);
    clear(level,base);
    var stand=EntityType.ARMOR_STAND.create(level,EntitySpawnReason.COMMAND);
    require(stand!=null,"Armor stand creation");
    stand.setPos(base.getX()+.5,501,base.getZ()+.5);level.addFreshEntity(stand);
    for(int i=0;i<6;i++)Precipitation.snow(level,base.above());
    require(stand.getY()>501.2,"Snow growth did not lift entity: "+stand.getY());
    double lift=stand.getY()-501;stand.discard();
    // Rain replenishes an exposed existing flow and never fills empty air.
    biome(level,"jungle");clear(level,base);
    level.setBlock(base.above(),Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,5),2|16);
    level.tickPrecipitation(base);
    require(level.getFluidState(base.above()).isSource(),"Rain did not refill exposed flow");
    level.setBlock(base.above(),Blocks.AIR.defaultBlockState(),2|16);level.tickPrecipitation(base);
    require(level.getBlockState(base.above()).isAir(),"Rain created water or snow from air in warm biome");
    for(int y=530;y<570;y++)level.setBlock(new BlockPos(40,y,8),Blocks.WATER.defaultBlockState(),2|16);
    level.setBlock(new BlockPos(40,570,8),Blocks.AIR.defaultBlockState(),2|16);
    require(WaterEnvironment.columnDepth(level,new BlockPos(40,530,8))==40,"Pressure column depth");
    level.setBlock(new BlockPos(40,550,8),Blocks.AIR.defaultBlockState(),2|16);
    require(WaterEnvironment.columnDepth(level,new BlockPos(40,530,8))==20,"Air pocket failed to stop pressure");
    var policy=new WaterEnvironment();RiverConfig.FINITE_SOURCES.set(true);
    var ordinary=new CreateFluidSourceEvent(level,base,Blocks.WATER.defaultBlockState());policy.sources(ordinary);
    require(!ordinary.canConvert(),"Finite source policy permitted ordinary regeneration");
    var natural=new CreateFluidSourceEvent(level,base,RiverWater.BLOCK.get().defaultBlockState());policy.sources(natural);
    require(natural.canConvert(),"Renewable river policy disabled native sources");RiverConfig.FINITE_SOURCES.set(false);
    clear(level,base);
    level.setBlock(base.above(),Blocks.WATER.defaultBlockState(),2|16);
    level.setBlock(base.above().east(),Blocks.FARMLAND.defaultBlockState(),2|16);
    var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
    player.setPos(36.5,502,8.5);
    var state=level.getBlockState(base.above());
    RiverConfig.FARM_WALKING.set(true);
    require(!state.getCollisionShape(level,base.above(),CollisionContext.of(player)).isEmpty(),"Farmland walking collision missing");
    RiverConfig.FARM_WALKING.set(false);
    require(state.getCollisionShape(level,base.above(),CollisionContext.of(player)).isEmpty(),"Farmland walking did not disable");
    var horse=EntityType.HORSE.create(level,EntitySpawnReason.COMMAND);
    require(horse!=null && !horse.dismountsUnderwater(),"Horse rider retention");
    DrainageServerChecks.record("WATER_ENVIRONMENT_PASS snowCap=24 partialCap=10 entityLift="+lift+" rainRefill=true dryAir=true pressure40=true airPocket20=true sourcePolicy=true farmCollision=true horseRetention=true");
  }
  private static void biome(ServerLevel level,String biome) {
    level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"fillbiome 32 490 0 47 580 15 minecraft:"+biome);
  }
  private static void clear(ServerLevel level,BlockPos base) {
    for(int y=base.getY()+1;y<level.getMaxY();y++)level.setBlock(base.atY(y),Blocks.AIR.defaultBlockState(),2|16);
    level.setBlock(base,Blocks.STONE.defaultBlockState(),2|16);
  }
  private static int snowCount(ServerLevel level,BlockPos base) {
    int count=0;
    for(int y=base.getY()+1;y<=base.getY()+12;y++) {
      var state=level.getBlockState(base.atY(y));
      if(state.is(Blocks.SNOW_BLOCK))count+=8;
      else if(state.is(Blocks.SNOW))count+=state.getValue(SnowLayerBlock.LAYERS);
    }
    return count;
  }
  private static void require(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
  private WaterEnvironmentChecks() {}
}
