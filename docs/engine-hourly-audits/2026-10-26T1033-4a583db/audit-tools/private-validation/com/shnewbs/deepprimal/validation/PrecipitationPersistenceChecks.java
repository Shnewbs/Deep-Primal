package com.shnewbs.deepprimal.validation;

import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Private disposable-world fixture. Never enable in an owner save. */
@Mod(value="deepprimal", dist=Dist.DEDICATED_SERVER)
public final class PrecipitationPersistenceChecks {
  public PrecipitationPersistenceChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void require(boolean ok, String why) { if (!ok) throw new AssertionError(why); }
  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String output=System.getenv("DEEPPRIMAL_PRECIPITATION_RECORD");
    if(output==null)return;
    String mode=System.getenv("DEEPPRIMAL_PRECIPITATION_MODE");
    require("fresh".equals(mode)||"reopen".equals(mode), "Explicit fresh/reopen mode");
    var level=event.getServer().overworld();
    HydrologyConfig.ENABLED.set(false); // Keep unrelated background hydrology out of this fixture.
    HydrologyConfig.RAIN_QUANTA.set(64);
    for(var type:java.util.List.of(WaterAccount.class,HydrologyRuntime.class))
      require(!type.getProtectionDomain().getCodeSource().getLocation().toString().contains("classes/java/main"),
          "Packaged runtime required: "+type.getName());
    var data=HydrologyData.get(level);
    long key=HydrologyData.key(-1048576,-1048576), cap=WaterAccount.MAX*1024;
    var saturated=new WaterAccount.Parcel(0,cap,cap);
    var supply=new WaterAccount.Parcel(1000000,1000000,1000000);
    if(mode.equals("fresh")) {
      require(!data.aquifers.containsKey(key), "Fresh isolated aquifer key");
      data.atmosphere.take(WaterAccount.MAX);
      require(data.atmosphere.add(supply), "Fixture atmosphere admission");
      data.aquifers.put(key,new Aquifer(saturated,saturated,0,false,Long.MAX_VALUE));
    }
    var aquifer=data.aquifers.get(key);
    require(aquifer!=null && aquifer.ground.snapshot().equals(saturated)
        && aquifer.snow.snapshot().equals(saturated) && data.atmosphere.snapshot().equals(supply),
        "Exact saved rejection state, including constituent-bearing atmosphere");
    int y=level.getMaxY()-8;
    for(int cz=0;cz<4;cz++)for(int cx=0;cx<4;cx++) {
      var p=new BlockPos(cx*16+8,y,cz*16+8);
      level.getChunkAt(p);
      for(int above=y+1;above<level.getMaxY();above++)level.setBlock(p.atY(above),Blocks.AIR.defaultBlockState(),2);
      level.setBlock(p,Blocks.DIRT.defaultBlockState(),2);
    }
    var method=HydrologyRuntime.class.getDeclaredMethod("precipitation",ServerLevel.class,
        TerrainEngine.class,int.class,int.class,HydrologyData.class,Aquifer.class);
    method.setAccessible(true);
    var terrain=new TerrainEngine(20261020,7);
    // A paired positive control prevents a dry/roofed/zero-demand scene from passing rejection vacuously.
    var control=new Aquifer(WaterAccount.Parcel.EMPTY,WaterAccount.Parcel.EMPTY,0,false,Long.MAX_VALUE);
    var controlData=new HydrologyData();
    controlData.atmosphere.take(WaterAccount.MAX);
    require(controlData.atmosphere.add(supply),"Control supply");
    method.invoke(null,level,terrain,0,0,controlData,control);
    long paid=control.ground.water()+control.snow.water();
    require(paid>0 && paid==supply.water()-controlData.atmosphere.water(), "Positive native precipitation demand");
    for(int repeat=0;repeat<3;repeat++)method.invoke(null,level,terrain,0,0,data,aquifer);
    require(aquifer.ground.snapshot().equals(saturated)&&aquifer.snow.snapshot().equals(saturated)
        && data.atmosphere.snapshot().equals(supply), "Repeated rejection retains exact parcels");
    var encoded=HydrologyData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();
    var decoded=HydrologyData.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
    require(decoded.atmosphere.snapshot().equals(supply)
        && decoded.aquifers.get(key).ground.snapshot().equals(saturated)
        && decoded.aquifers.get(key).snow.snapshot().equals(saturated), "Codec round trip");
    require(!level.hasChunk(-65536,-65536),"No remote aquifer chunk load");
    data.setDirty();
    String record="PRECIPITATION_PERSISTENCE_PASS mode="+mode+" positiveDemand="+paid
        +" repeatedRejections=3 exactWaterSaltSediment=true\n";
    Files.writeString(Path.of(output),record);System.out.print(record);
  }
}
