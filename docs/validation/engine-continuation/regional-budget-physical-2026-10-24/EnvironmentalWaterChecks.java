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
  @SubscribeEvent public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String destination = System.getenv("DEEPPRIMAL_ENVIRONMENT_RECORD");
    if (destination == null) return;
    for(var type:java.util.List.of(HydrologyRuntime.class,EngineChunkGenerator.class,TerrainEngine.class,WaterAccount.class))
      System.out.println("ENVIRONMENT_CLASS_ORIGIN class="+type.getName()+" source="+type.getProtectionDomain().getCodeSource().getLocation());
    var server = event.getServer(); var level = server.overworld();
    var d = HydrologyData.get(level);
    if ("legacy3".equals(System.getenv("DEEPPRIMAL_ENVIRONMENT_MODE"))) {
      check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == 3, "Delivered V3 world preserves its saved generator");
      level.getChunk(0,0);
      var oldCell=level.getBlockState(new BlockPos(80,500,8));
      check(oldCell.is(EngineRegistry.WATER_BLOCK.get()) && oldCell.getValue(EngineFluid.UNITS)==7
          && oldCell.getValue(EngineFluid.FLOW)==3, "Delivered V3 saved edit/quantity retained");
      Files.writeString(Path.of(destination), "ENVIRONMENT_V3_COMPATIBILITY_PASS savedGenerator=continental_v3 noMigration=true savedEdit=true\n");
      server.halt(false); return;
    }
    var persisted = new BlockPos(20, 500, 20);
    boolean reopen = "reopen".equals(System.getenv("DEEPPRIMAL_ENVIRONMENT_MODE"));
    if (reopen) {
      check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == 4, "Saved V4 codec");
      var loaded = d.aquifers.get(HydrologyData.key(1000000, -1000000));
      check(loaded != null && loaded.ground.water() == 123456 && loaded.ground.snapshot().salt() == 789 && loaded.dynamicWetSteps() == 7,
          "Actual saved dimension groundwater survives restart");
      check(HydrologyRuntime.contents(level, persisted, level.getBlockState(persisted)).salt() == 12345,
          "Actual saved surface chemistry survives restart");
      check(level.getBlockState(persisted.west()).is(Blocks.GOLD_BLOCK), "Player edit survives restart");
      Files.writeString(Path.of(destination), "ENVIRONMENT_REOPEN_PASS aquifer=true chemistry=true savedV4=true edits=true\n");
      return;
    }
    check(((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography().revision == 4, "Default uses V4");
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
    var visit=HydrologyRuntime.class.getDeclaredMethod("visit",net.minecraft.server.level.ServerLevel.class,work.getClass(),int.class,int.class);
    visit.setAccessible(true);
    level.setRainLevel(1);
    long airBefore=d.atmosphere.water()+d.ocean.water();
    visit.invoke(null,level,work,8,8);
    long precipitation=rainStore.ground.water()+rainStore.snow.water()-1000;
    check(precipitation>0 && airBefore-d.atmosphere.water()-d.ocean.water()==precipitation,"Actual rain/snow recharge debits atmospheric water");
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
  }
}
