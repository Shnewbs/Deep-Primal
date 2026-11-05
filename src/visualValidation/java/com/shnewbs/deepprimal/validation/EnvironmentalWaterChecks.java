package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class EnvironmentalWaterChecks {
  public EnvironmentalWaterChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void check(boolean ok, String what) { if (!ok) throw new AssertionError(what); }
  private static void mixedPrecipitation(net.minecraft.server.level.ServerLevel level,
      TerrainEngine terrain, java.lang.reflect.Method precipitation) throws Exception {
    var grassTop = new BlockPos(8, 500, 8);
    var sandTop = new BlockPos(24, 500, 8);
    String grassBiome = level.getBiome(grassTop).unwrapKey().orElseThrow().identifier().toString();
    String sandBiome = level.getBiome(sandTop).unwrapKey().orElseThrow().identifier().toString();
    int cases = 0;
    try {
      for (boolean snowOnGrass : new boolean[] {false, true}) {
        sampleBiome(level, grassTop, snowOnGrass ? "minecraft:snowy_plains" : "minecraft:jungle");
        sampleBiome(level, sandTop, snowOnGrass ? "minecraft:jungle" : "minecraft:snowy_plains");
        var rain = net.minecraft.world.level.biome.Biome.Precipitation.RAIN;
        var snow = net.minecraft.world.level.biome.Biome.Precipitation.SNOW;
        check(level.getBiome(grassTop).value().getPrecipitationAt(grassTop, level.getSeaLevel())
            == (snowOnGrass ? snow : rain), "Mixed grass precipitation fixture");
        check(level.getBiome(sandTop).value().getPrecipitationAt(sandTop, level.getSeaLevel())
            == (snowOnGrass ? rain : snow), "Mixed sand precipitation fixture");
        long grassDemand = (long) (HydrologyConfig.RAIN_QUANTA.get() * .5 * 256);
        long sandDemand = (long) (HydrologyConfig.RAIN_QUANTA.get() * .9 * 256);
        long rainDemand = snowOnGrass ? sandDemand : grassDemand;
        long snowDemand = snowOnGrass ? grassDemand : sandDemand;
        for (long available : new long[] {0, 1, 100, 100000})
          for (long room : new long[] {0, 10, Aquifer.CAPACITY}) {
            var data = new HydrologyData();
            data.atmosphere.take(WaterAccount.MAX);
            check(data.atmosphere.add(new WaterAccount.Parcel(available, 127, 251)), "Mixed atmosphere");
            var store = new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY - room, 17, 31),
                new WaterAccount.Parcel(0, 23, 43), 0, false, 0);
            long eligibleRain = Math.min(rainDemand, room);
            long paid = Math.min(available, eligibleRain + snowDemand);
            long expectedRain = paid == 0 ? 0 : paid * eligibleRain / (eligibleRain + snowDemand);
            precipitation.invoke(null, level, terrain, 8, 8, data, store);
            long actualRain = store.ground.water() - (Aquifer.CAPACITY - room);
            check(actualRain == expectedRain && store.snow.water() == paid - expectedRain,
                "Mixed precipitation allocation: available=" + available + " room=" + room
                    + " snowOnGrass=" + snowOnGrass + " expectedRain=" + expectedRain
                    + " actualRain=" + actualRain + " snow=" + store.snow.water());
            check(data.atmosphere.water() == available - paid
                && data.atmosphere.snapshot().salt() + store.ground.snapshot().salt()
                    + store.snow.snapshot().salt() == 167
                && data.atmosphere.snapshot().sediment() + store.ground.snapshot().sediment()
                    + store.snow.snapshot().sediment() == 325, "Mixed weather exact ledger");
            cases++;
          }
      }
    } finally {
      sampleBiome(level, grassTop, grassBiome);
      sampleBiome(level, sandTop, sandBiome);
    }
    System.out.println("ENVIRONMENT_MIXED_PRECIPITATION_PASS cases=" + cases
        + " swappedBiomes=true finiteAtmosphere=true capacityAware=true exactConstituents=true");
  }
  private static void sampleBiome(net.minecraft.server.level.ServerLevel level, BlockPos p, String biome) {
    level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),
        "fillbiome " + (p.getX() - 4) + " " + (p.getY() - 4) + " " + (p.getZ() - 4) + " "
            + (p.getX() + 3) + " " + (p.getY() + 3) + " " + (p.getZ() + 3) + " " + biome);
  }
  @SubscribeEvent public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String destination = System.getenv("DEEPPRIMAL_ENVIRONMENT_RECORD");
    if (destination == null) return;
    for(var type:java.util.List.of(HydrologyRuntime.class,EngineChunkGenerator.class,TerrainEngine.class,WaterAccount.class))
      System.out.println("ENVIRONMENT_CLASS_ORIGIN class="+type.getName()+" source="+type.getProtectionDomain().getCodeSource().getLocation());
    var server = event.getServer(); var level = server.overworld();
    var d = HydrologyData.get(level);
    String stormProfile = System.getenv("DEEPPRIMAL_STORM_PROFILE");
    if (stormProfile != null) {
      StormSamplingChecks.run(level, Path.of(stormProfile));
      server.halt(false);
      return;
    }
    if ("legacy3".equals(System.getenv("DEEPPRIMAL_ENVIRONMENT_MODE"))) {
      check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == 3, "Delivered V3 world preserves its saved generator");
      level.getChunk(0,0);
      var oldCell=level.getBlockState(new BlockPos(80,500,8));
      check(oldCell.is(EngineRegistry.WATER_BLOCK.get()) && oldCell.getValue(EngineFluid.UNITS)==7
          && oldCell.getValue(EngineFluid.FLOW)==3, "Delivered V3 saved edit/quantity retained");
      Files.writeString(Path.of(destination), "ENVIRONMENT_V3_COMPATIBILITY_PASS savedGenerator=continental_v3 noMigration=true savedEdit=true\n");
      server.halt(false); return;
    }
    int expectedRevision=Integer.parseInt(System.getenv().getOrDefault("DEEPPRIMAL_ENVIRONMENT_REVISION","4"));
    var persisted = new BlockPos(20, 500, 20);
    boolean reopen = "reopen".equals(System.getenv("DEEPPRIMAL_ENVIRONMENT_MODE"));
    if (reopen) {
      check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == expectedRevision, "Saved terrain codec");
      var loaded = d.aquifers.get(HydrologyData.key(1000000, -1000000));
      check(loaded != null && loaded.ground.water() == 123456 && loaded.ground.snapshot().salt() == 789 && loaded.dynamicWetSteps() == 7,
          "Actual saved dimension groundwater survives restart");
      check(HydrologyRuntime.contents(level, persisted, level.getBlockState(persisted)).salt() == 12345,
          "Actual saved surface chemistry survives restart");
      check(level.getBlockState(persisted.west()).is(Blocks.GOLD_BLOCK), "Player edit survives restart");
      Files.writeString(Path.of(destination), "ENVIRONMENT_REOPEN_PASS aquifer=true chemistry=true savedRevision=true edits=true\n");
      if (Boolean.parseBoolean(System.getenv("DEEPPRIMAL_ENVIRONMENT_STOP"))) server.halt(false);
      return;
    }
    check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == expectedRevision, "Default terrain revision");
    level.getChunk(1,1);
    for (int z=16;z<32;z++) for(int x=16;x<32;x++) for(int y=498;y<504;y++)
      level.setBlock(new BlockPos(x,y,z), Blocks.BEDROCK.defaultBlockState(), 2);
    var first = new BlockPos(22,500,22); var second = first.east();
    level.setBlock(first, Blocks.AIR.defaultBlockState(),2); level.setBlock(second, Blocks.AIR.defaultBlockState(),2);
    var aquifer = new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY/2,400000,12000), WaterAccount.Parcel.EMPTY,30,true,0);
    long initial = aquifer.ground.water();
    check(HydrologyRuntime.emit(level,first,aquifer,506,4),"Dynamic outlet accepts supply");
    check(HydrologyRuntime.emit(level,second,aquifer,506,4),"A second location can be an outlet");
    check(initial-aquifer.ground.water()==8*256 && WaterRuntime.units(level,first)+WaterRuntime.units(level,second)==8,
        "Two outlets debit one shared aquifer");
    var before = aquifer.ground.snapshot();
    check(!HydrologyRuntime.emit(level,first.north(),aquifer,506,4) && before.equals(aquifer.ground.snapshot()),"Blocked outlet does not consume or backlog");
    check(!HydrologyRuntime.emit(level,first,aquifer,490,4),"Backwater stops source");
    check(HydrologyRuntime.infiltrate(level,first,aquifer,4),"Surface water recharges same store");
    check(initial-aquifer.ground.water()==4*256,"Infiltration conserves volume");
    level.setBlock(first, Blocks.AIR.defaultBlockState(),2);
    var salty = new WaterAccount.Parcel(4096,12345,4321);
    check(HydrologyRuntime.place(level,first,salty),"Measured bucket placement");
    var bucket = EngineBucket.filled(HydrologyRuntime.pickup(level,first));
    check(EngineBucket.contents(bucket).equals(salty),"Bucket carries constituents");
    check(HydrologyRuntime.place(level,first,EngineBucket.contents(bucket)),"Bucket return");
    check(!HydrologyRuntime.boundary(level.getBlockState(first)),"Bucket never creates source marker");
    var waterBefore = HydrologyRuntime.contents(level,first,level.getBlockState(first));
    var neighborBefore = HydrologyRuntime.contents(level,second,level.getBlockState(second));
    WaterRuntime.tick(level,first);
    var waterAfter = HydrologyRuntime.contents(level,first,level.getBlockState(first));
    var neighborAfter = HydrologyRuntime.contents(level,second,level.getBlockState(second));
    check(waterBefore.water()+neighborBefore.water()==waterAfter.water()+neighborAfter.water()
        && waterBefore.salt()+neighborBefore.salt()==waterAfter.salt()+neighborAfter.salt()
        && waterBefore.sediment()+neighborBefore.sediment()==waterAfter.sediment()+neighborAfter.sediment(),"Native transport advects exact constituents");
    // Authored controlled ocean boundary; this fixture is intentionally not a generated lake.
    var sea = new BlockPos(28,95,28); var river = sea.above();
    for(var side:Direction.values()) level.setBlock(sea.relative(side), Blocks.BEDROCK.defaultBlockState(),2);
    var anchor=EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineWaterBlock.OCEAN,true);
    level.setBlock(sea,anchor,2); level.setBlock(river,Blocks.AIR.defaultBlockState(),2);
    HydrologyRuntime.place(level,river,new WaterAccount.Parcel(4096,0,0));
    long oceanBefore=d.ocean.water();
    check(HydrologyRuntime.oceanSink(level,river,sea),"River reaches ocean account");
    check(d.ocean.water()-oceanBefore==4*256 && WaterRuntime.units(level,river)==12 && WaterRuntime.units(level,sea)==16,"One ocean credit, fixed sea level");
    var oceanParcel=HydrologyRuntime.pickup(level,sea);
    check(oceanParcel.water()==4096 && WaterRuntime.units(level,sea)==16 && d.ocean.water()==oceanBefore+4*256-4096,"Ocean withdrawal is paid once");
    level.setBlock(sea,EngineRegistry.WATER_BLOCK.get().defaultBlockState(),2);
    check(!HydrologyRuntime.oceanSink(level,river,sea),"Inland lake at sea level is finite");
    // Run the real loaded-column weather path over an exposed, permeable fixture.
    var rainBed=new BlockPos(8,499,8); level.getChunk(0,0);
    for(int y=500;y<level.getMaxY();y++) level.setBlock(new BlockPos(8,y,8),Blocks.AIR.defaultBlockState(),2);
    level.setBlock(rainBed,Blocks.GRASS_BLOCK.defaultBlockState(),2);
    var rainStore=new Aquifer(new WaterAccount.Parcel(1000,0,0),WaterAccount.Parcel.EMPTY,0,false,level.getGameTime()-20);
    d.aquifers.put(HydrologyData.key(8,8),rainStore);
    var field=HydrologyRuntime.class.getDeclaredField("WORK"); field.setAccessible(true);
    var work=((java.util.Map<?,?>)field.get(null)).get(level);
    check(work != null, "Loaded chunks have environmental work");
    var cursorType=Class.forName(HydrologyRuntime.class.getName()+"$Cursor");
    var cursorConstructor=cursorType.getDeclaredConstructor(); cursorConstructor.setAccessible(true);
    var cursor=cursorConstructor.newInstance();
    var visit=HydrologyRuntime.class.getDeclaredMethod("visit",net.minecraft.server.level.ServerLevel.class,
        work.getClass(),cursorType,int.class,TerrainEngine.class,int.class,int.class);
    visit.setAccessible(true);
    var terrain=((EngineChunkGenerator)level.getChunkSource().getGenerator()).geography();
    level.setRainLevel(1);
    long airBefore=d.atmosphere.water()+d.ocean.water();
    visit.invoke(null,level,work,cursor,0,terrain,8,8);
    long precipitation=rainStore.ground.water()+rainStore.snow.water()-1000;
    check(precipitation>0 && airBefore-d.atmosphere.water()-d.ocean.water()==precipitation,"Actual rain/snow recharge debits atmospheric water");
    // Fixed chunk-center samples: seal every loaded center except grass and sand controls.
    for(int cz=0;cz<4;cz++) for(int cx=0;cx<4;cx++) {
      if(level.getChunkSource().getChunkNow(cx,cz)==null) continue;
      int sx=cx*16+8, sz=cz*16+8;
      for(int sy=500;sy<level.getMaxY();sy++)
        level.setBlock(new BlockPos(sx,sy,sz),Blocks.AIR.defaultBlockState(),2);
      level.setBlock(new BlockPos(sx,499,sz),Blocks.BEDROCK.defaultBlockState(),2);
    }
    level.getChunk(1,0);
    for(int sy=500;sy<level.getMaxY();sy++)
      level.setBlock(new BlockPos(24,sy,8),Blocks.AIR.defaultBlockState(),2);
    level.setBlock(rainBed,Blocks.GRASS_BLOCK.defaultBlockState(),2);
    level.setBlock(new BlockPos(24,499,8),Blocks.SAND.defaultBlockState(),2);
    var workConstructor=work.getClass().getDeclaredConstructor(); workConstructor.setAccessible(true);
    long[] orderRecharge=new long[2];
    for(int order=0;order<2;order++) {
      var orderedStore=new Aquifer(new WaterAccount.Parcel(1000,0,0),
          WaterAccount.Parcel.EMPTY,0,false,level.getGameTime()-20);
      d.aquifers.put(HydrologyData.key(8,8),orderedStore);
      var orderedWork=workConstructor.newInstance();
      long beforeWeather=d.atmosphere.water()+d.ocean.water();
      visit.invoke(null,level,orderedWork,cursor,0,terrain,order==0?8:24,8);
      orderRecharge[order]=orderedStore.ground.water()+orderedStore.snow.water()-1000;
      check(beforeWeather-d.atmosphere.water()-d.ocean.water()==orderRecharge[order],
          "Spatial recharge was not paid from atmospheric circulation");
    }
    check(orderRecharge[0]==orderRecharge[1],
        "Regional rain depends on first visited soil: grass="+orderRecharge[0]+" sand="+orderRecharge[1]);
    long expectedRain=(long)(HydrologyConfig.RAIN_QUANTA.get()*.5*256)
        +(long)(HydrologyConfig.RAIN_QUANTA.get()*.9*256);
    check(orderRecharge[0]==expectedRain,"Spatial rain did not match grass and sand sample weights");
    var precipitationMethod=HydrologyRuntime.class.getDeclaredMethod("precipitation",
        net.minecraft.server.level.ServerLevel.class,TerrainEngine.class,int.class,int.class,
        HydrologyData.class,Aquifer.class);
    precipitationMethod.setAccessible(true);
    var limitedData=new HydrologyData();
    limitedData.atmosphere.take(WaterAccount.MAX);
    check(limitedData.atmosphere.add(new WaterAccount.Parcel(100,0,0)),"Limited weather fixture");
    var limitedStore=new Aquifer(new WaterAccount.Parcel(1000,17,31),WaterAccount.Parcel.EMPTY,0,false,0);
    precipitationMethod.invoke(null,level,terrain,8,8,limitedData,limitedStore);
    check(limitedData.atmosphere.water()==0
        && limitedStore.ground.water()+limitedStore.snow.water()==1100
        && limitedStore.ground.snapshot().salt()==17 && limitedStore.ground.snapshot().sediment()==31,
        "Spatial rain exceeded the finite atmosphere or changed groundwater constituents");
    var unloadedStore=new Aquifer(new WaterAccount.Parcel(1000,17,31),WaterAccount.Parcel.EMPTY,0,false,0);
    long noLoadAir=d.atmosphere.water();
    check(!level.hasChunk(-62500,-62500),"Unloaded negative-region fixture was already loaded");
    precipitationMethod.invoke(null,level,terrain,-1000000,-1000000,d,unloadedStore);
    check(unloadedStore.ground.water()==1000 && unloadedStore.snow.water()==0
        && d.atmosphere.water()==noLoadAir && !level.hasChunk(-62500,-62500),
        "Regional precipitation loaded an absent chunk or credited an unloaded region");
    System.out.println("ENVIRONMENT_SPATIAL_RAIN_PASS orderIndependent=true samplesPerRegionAtMost=16"
        + " grassSandQuanta="+expectedRain+" sealedExcluded=true exactDebit=true finiteAtmosphere=true noForceLoad=true");
    mixedPrecipitation(level, terrain, precipitationMethod);
    level.setBlock(new BlockPos(24,499,8),Blocks.BEDROCK.defaultBlockState(),2);
    level.setBlock(rainBed,Blocks.BEDROCK.defaultBlockState(),2);
    var sealedStore=new Aquifer(new WaterAccount.Parcel(Aquifer.STOP,789,123),
        WaterAccount.Parcel.EMPTY,Aquifer.WET_STEPS,true,level.getGameTime()-20);
    d.aquifers.put(HydrologyData.key(8,8),sealedStore);
    work=workConstructor.newInstance();
    var sealedBefore=sealedStore.ground.snapshot();
    long sealedTotal=d.atmosphere.water()+d.ocean.water()+sealedStore.ground.water();
    visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(sealedStore.lastTick()==level.getGameTime() && !sealedStore.active(),
        "Impermeable surface froze the existing regional drought clock");
    check(sealedStore.ground.snapshot().equals(sealedBefore)
        && d.atmosphere.water()+d.ocean.water()+sealedStore.ground.water()==sealedTotal,
        "Impermeable surface admitted precipitation or lost accounted water");
    var airAfterSealed=d.atmosphere.snapshot();
    var oceanAfterSealed=d.ocean.snapshot();
    for(int attempt=0;attempt<16;attempt++) visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(d.atmosphere.snapshot().equals(airAfterSealed) && d.ocean.snapshot().equals(oceanAfterSealed),
        "Repeated impermeable visits duplicated a regional environmental step");
    level.setBlock(rainBed,Blocks.GRASS_BLOCK.defaultBlockState(),2);
    // A roof edit after sampling takes effect on the next interval, not a second scan this step.
    visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(sealedStore.ground.snapshot().equals(sealedBefore),
        "Opening a sampled surface granted a second regional rain scan in one interval");
    sealedStore=new Aquifer(sealedStore.ground.snapshot(),sealedStore.snow.snapshot(),
        sealedStore.wetSteps(),sealedStore.active(),level.getGameTime()-20,sealedStore.dynamicWetSteps());
    d.aquifers.put(HydrologyData.key(8,8),sealedStore);
    work=workConstructor.newInstance();
    visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(sealedStore.ground.water()+sealedStore.snow.water()>sealedBefore.water(),
        "Sealed first visit consumed the rain allowance of later permeable ground");
    var groundAfterRain=sealedStore.ground.snapshot();
    var snowAfterRain=sealedStore.snow.snapshot();
    var airAfterRain=d.atmosphere.snapshot();
    for(int attempt=0;attempt<16;attempt++) visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(sealedStore.ground.snapshot().equals(groundAfterRain)
        && sealedStore.snow.snapshot().equals(snowAfterRain) && d.atmosphere.snapshot().equals(airAfterRain),
        "Permeable visits duplicated the rain allowance after a sealed first visit");
    check(d.atmosphere.water()+d.ocean.water()+sealedStore.ground.water()+sealedStore.snow.water()==sealedTotal
        && sealedStore.ground.snapshot().salt()+sealedStore.snow.snapshot().salt()==sealedBefore.salt()
        && sealedStore.ground.snapshot().sediment()+sealedStore.snow.snapshot().sediment()==sealedBefore.sediment(),
        "Delayed precipitation lost water or groundwater constituents");
    var rebuiltWork=workConstructor.newInstance();
    visit.invoke(null,level,rebuiltWork,cursor,0,terrain,8,8);
    check(sealedStore.ground.snapshot().equals(groundAfterRain)
        && sealedStore.snow.snapshot().equals(snowAfterRain) && d.atmosphere.snapshot().equals(airAfterRain),
        "Rebuilt runtime cache granted another precipitation allowance");
    level.setBlock(rainBed,Blocks.BEDROCK.defaultBlockState(),2);
    long sealedInitialized=d.initialized;
    d.aquifers.remove(HydrologyData.key(8,8));
    visit.invoke(null,level,work,cursor,0,terrain,8,8);
    check(!d.aquifers.containsKey(HydrologyData.key(8,8)) && d.initialized==sealedInitialized,
        "Impermeable surface seeded a new groundwater reserve");
    d.aquifers.put(HydrologyData.key(8,8),sealedStore);
    System.out.println("ENVIRONMENT_SEALED_REGION_PASS droughtClock=true noRecharge=true"
        + " noSeed=true laterRainOnce=true cacheRebuild=true exactWaterSaltSediment=true repeatVisits=16");
    level.setRainLevel(0);
    long seeded=d.initialized;
    d.aquifers.remove(HydrologyData.key(-100,200));
    var existing=d.aquifer(-100,200,.7,10); long seededOnce=d.initialized;
    check(existing==d.aquifer(-100,200,.7,999999) && seededOnce==d.initialized && seededOnce>seeded,"Aquifer initialization is once per region");
    d.aquifers.put(HydrologyData.key(1000000,-1000000),new Aquifer(new WaterAccount.Parcel(123456,789,123),WaterAccount.Parcel.EMPTY,4,false,77,7));
    d.setDirty();
    var encoded=HydrologyData.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();
    var decoded=HydrologyData.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
    check(decoded.ocean.snapshot().equals(d.ocean.snapshot()) && decoded.atmosphere.snapshot().equals(d.atmosphere.snapshot())
        && decoded.aquifers.size()==d.aquifers.size()
        && decoded.aquifers.get(HydrologyData.key(1000000,-1000000)).dynamicWetSteps()==7,"Versioned ledger codec round trip");
    level.setBlock(persisted,Blocks.AIR.defaultBlockState(),2);
    HydrologyRuntime.place(level,persisted,salty); level.setBlock(persisted.west(),Blocks.GOLD_BLOCK.defaultBlockState(),2);
    check(!HydrologyRuntime.emit(level,new BlockPos(3000000,100,3000000),aquifer,506,4),"Unloaded outlet applies backpressure");
    check(!level.hasChunk(3000000>>4,3000000>>4),"Hydrology never force-loads outlet");
    server.saveAllChunks(false,true,true);
    Files.writeString(Path.of(destination),"ENVIRONMENT_NATIVE_PASS sharedAquifer=true dynamicOutlets=true headBlocked=true infiltration=true bucketChemistry=true advection=true oceanStage=true inlandFinite=true weatherDebit=true codec=true onceOnlyInitialization=true noForcedChunks=true\n");
    if (Boolean.parseBoolean(System.getenv("DEEPPRIMAL_ENVIRONMENT_STOP"))) server.halt(false);
  }
}
