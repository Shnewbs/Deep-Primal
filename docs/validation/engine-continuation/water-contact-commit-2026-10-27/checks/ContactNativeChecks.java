package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterContactBudget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import java.nio.file.*;

/** Private diagnostic fixture; excluded from the mod and all user-facing archives. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class ContactNativeChecks {
 public ContactNativeChecks(){NeoForge.EVENT_BUS.register(this);}
 static void require(boolean x,String m){if(!x)throw new AssertionError(m);}
 @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception{
  String mode=System.getenv("DP_CONTACT_MODE");if(mode==null)return;
  require(mode.equals("fresh")||mode.equals("reopen"),"mode");
  var l=event.getServer().overworld();var a=new BlockPos(15,200,0);var b=new BlockPos(16,200,0);
  l.getChunk(0,0);l.getChunk(1,0);
  var data=HydrologyData.get(l);
  for(Class<?> c:new Class<?>[]{WaterAccount.class,WaterContactBudget.class,WaterContactRuntime.class,WaterRuntime.class,HydrologyRuntime.class,StreamSourceIndex.class}){
   try(var in=c.getResourceAsStream("/"+c.getName().replace('.','/')+".class")){
    String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(in.readAllBytes()));
    require(hash.equals(System.getenv("DP_HASH_"+c.getSimpleName())),"loaded candidate identity "+c.getName());
    System.out.println("CONTACT_NATIVE_CLASS "+c.getName()+" "+hash);
   }
  }
  if(mode.equals("fresh")){
   for(int x=14;x<=17;x++)for(int y=199;y<=201;y++)for(int z=-1;z<=1;z++)
    l.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2|16);
   HydrologyRuntime.write(l,a,EngineRegistry.WATER_BLOCK.get().defaultBlockState());
   HydrologyRuntime.write(l,b,EngineRegistry.WATER_BLOCK.get().defaultBlockState());
   data.composition.put(a.asLong(),new WaterAccount.Parcel(4096,512,0));
   data.composition.put(b.asLong(),new WaterAccount.Parcel(4096,0,512));data.setDirty();
   WaterContactRuntime.mix(l,a);
  }
  var expected=new WaterAccount.Parcel(4096,256,256);
  require(HydrologyRuntime.contents(l,a,l.getBlockState(a)).equals(expected),"left exact persisted parcel");
  require(HydrologyRuntime.contents(l,b,l.getBlockState(b)).equals(expected),"right exact persisted parcel");
  var sa=l.getBlockState(a);var sb=l.getBlockState(b);
  require(!HydrologyRuntime.commitContact(l,a,b,sa,sb,expected,expected,
      new WaterAccount.Parcel(4095,256,256),new WaterAccount.Parcel(4097,256,256)),"native volume shift rejected");
  require(!HydrologyRuntime.commitContact(l,a,b,sa,sb,expected,expected,
      new WaterAccount.Parcel(4096,257,256),expected),"native constituent creation rejected");
  require(!HydrologyRuntime.commitContact(l,a,b,sa,sb,expected,expected,
      new WaterAccount.Parcel(4096,256,255),expected),"native constituent destruction rejected");
  require(!HydrologyRuntime.commitContact(l,a,b.east(),sa,sb,expected,expected,
      new WaterAccount.Parcel(4096,255,256),new WaterAccount.Parcel(4096,257,256)),"native non-contact rejected");
  WaterContactRuntime.mix(l,b);WaterRuntime.tick(l,a);WaterRuntime.tick(l,b);
  require(HydrologyRuntime.contents(l,a,l.getBlockState(a)).equals(expected),"left replay/actual tick");
  require(HydrologyRuntime.contents(l,b,l.getBlockState(b)).equals(expected),"right replay/actual tick");
  // Repeated actual loaded-cell admissions cannot grow a tick's visit or contact residency.
  for(int n=0;n<1000;n++)WaterContactRuntime.mix(l,a);
  require(WaterContactRuntime.visits(l)==2048,"actual native contact visit cap");
  require(WaterContactRuntime.retainedEdges(l)<=2048,"actual native contact residency cap");
  require(HydrologyRuntime.contents(l,a,l.getBlockState(a)).equals(expected),"left after cap");
  require(HydrologyRuntime.contents(l,b,l.getBlockState(b)).equals(expected),"right after cap");
  var encoded=HydrologyData.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,data).getOrThrow();
  var decoded=HydrologyData.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();
  require(decoded.composition.get(a.asLong()).equals(expected)&&decoded.composition.get(b.asLong()).equals(expected),"real saved-data codec");
  String marker="CONTACT_NATIVE_PASS mode="+mode+" pageBoundary=true exactWaterSaltSediment=true actualSolverTick=true codec=true visits=2048 repeatedAdmissions=1000\n";
  Files.writeString(Path.of(System.getenv("DP_CONTACT_RECORD")),marker);System.out.print(marker);
 }
}
