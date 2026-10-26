package com.shnewbs.deepprimal.validation;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.core.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
/** Private disposable-world source-index/quota persistence probe, never distributed. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class StreamIndexNativeChecks {
 public StreamIndexNativeChecks(){NeoForge.EVENT_BUS.register(this);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static Object make(String n)throws Exception{var c=Class.forName(HydrologyRuntime.class.getName()+"$"+n).getDeclaredConstructor();c.setAccessible(true);return c.newInstance();}
 static Method method(Class<?> c,String n,Class<?>...p)throws Exception{var m=c.getDeclaredMethod(n,p);m.setAccessible(true);return m;}
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static long[] amount(WaterAccount.Parcel p){return new long[]{p.water(),p.salt(),p.sediment()};}
 @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent e)throws Exception{
  String mode=System.getenv("DP_INDEX_MODE");if(mode==null)return;
  ServerLevel l=e.getServer().overworld();HydrologyConfig.ENABLED.set(false);
  require(l.getChunkSource().getGenerator() instanceof EngineChunkGenerator,"engine world required");
  var terrain=((EngineChunkGenerator)l.getChunkSource().getGenerator()).geography();
  var p=new BlockPos(2048,288,2048);var q=p.east(16);l.getChunkAt(p);l.getChunkAt(q);
  var data=HydrologyData.get(l);long regionKey=HydrologyData.key(p.getX(),p.getZ());
  Path receipt=Path.of(System.getenv("DP_INDEX_RECORD"));
  if(mode.equals("reopen")){
   long[] expected=Arrays.stream(Files.readString(receipt).trim().split(",")).mapToLong(Long::parseLong).toArray();
   var a=data.aquifers.get(regionKey);require(a!=null,"persisted aquifer missing");
   long[] actual=combine(amount(a.ground.snapshot()),amount(HydrologyRuntime.contents(l,p,l.getBlockState(p))),amount(HydrologyRuntime.contents(l,q,l.getBlockState(q))));
   require(Arrays.equals(actual,expected),"persisted exact water/constituents differ");
  } else {
   require(mode.equals("fresh"),"explicit mode required");
   for(var pos:List.of(p,q))for(var dir:Direction.values())require(HydrologyRuntime.write(l,pos.relative(dir),Blocks.STONE.defaultBlockState()),"bed/wall placement");
   for(int scenario=0;scenario<6;scenario++){
    for(var pos:List.of(p,q)){HydrologyRuntime.write(l,pos,Blocks.AIR.defaultBlockState());((Map<?,?>)field(HydrologyData.class,"composition").get(data)).remove(pos.asLong());}
    var initial=new WaterAccount.Parcel(Aquifer.CAPACITY*3/4,81917,163819);
    var aquifer=new Aquifer(initial,WaterAccount.Parcel.EMPTY,Aquifer.WET_STEPS,true,Long.MAX_VALUE);
    data.aquifers.put(regionKey,aquifer);
    Object work=make("Work"),first=make("Cursor"),second=make("Cursor");Class<?> wc=work.getClass(),cc=first.getClass();
    var chunks=(Map<Long,Object>)field(wc,"chunks").get(work);long pk=l.getChunkAt(p).getPos().pack(),qk=l.getChunkAt(q).getPos().pack();
    Object index=field(wc,"streamChunks").get(work);var begin=method(index.getClass(),"beginPass");
    var refresh=method(HydrologyRuntime.class,"refreshSource",wc,long.class,cc);
    for(Object c:List.of(first,second)){field(cc,"streamCount").setInt(c,1);((boolean[])field(cc,"streamCandidate").get(c))[0]=true;((double[])field(cc,"streamSurface").get(c))[0]=288.5;((int[])field(cc,"streamOutletY").get(c))[0]=288;}
    chunks.put(pk,first);chunks.put(qk,second);refresh.invoke(null,work,pk,first);refresh.invoke(null,work,qk,second);
    if(scenario==1||scenario==2){field(cc,"streamCount").setInt(first,0);refresh.invoke(null,work,pk,first);if(scenario==2)begin.invoke(index);field(cc,"streamCount").setInt(first,1);refresh.invoke(null,work,pk,first);}
    if(scenario==3){method(HydrologyRuntime.class,"forgetChunk",wc,long.class).invoke(null,work,pk);chunks.put(pk,first);refresh.invoke(null,work,pk,first);}
    if(scenario==4){((double[])field(cc,"streamSurface").get(second))[0]=289;refresh.invoke(null,work,qk,second);}
    Object regional=method(HydrologyRuntime.class,"region",wc,TerrainEngine.class,int.class,int.class).invoke(null,work,terrain,p.getX(),p.getZ());
    int quota=Aquifer.baseflowUnits(HydrologyConfig.RAIN_QUANTA.get());require(quota>0,"positive quota");field(regional.getClass(),"natural").setInt(regional,quota-1);
    long missingKey=net.minecraft.world.level.ChunkPos.pack(30000,30000);
    if(scenario==5){Object missing=make("Cursor");field(cc,"streamCount").setInt(missing,1);((double[])field(cc,"streamSurface").get(missing))[0]=1000;chunks.put(missingKey,missing);refresh.invoke(null,work,missingKey,missing);}
    for(Object c:List.of(first,second))Arrays.fill((long[])field(cc,"visited").get(c),Long.MAX_VALUE);
    var worlds=(Map<ServerLevel,Object>)field(HydrologyRuntime.class,"WORK").get(null);Object previous=worlds.put(l,work);int oldColumns=HydrologyConfig.COLUMNS.get();
    try{
     HydrologyConfig.COLUMNS.set(1);HydrologyConfig.ENABLED.set(true);
     for(int attempt=0;attempt<8;attempt++)new HydrologyRuntime().tick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,l));
    }finally{HydrologyConfig.ENABLED.set(false);HydrologyConfig.COLUMNS.set(oldColumns);if(previous==null)worlds.remove(l);else worlds.put(l,previous);}
    if(scenario==5)require(!chunks.containsKey(missingKey),"actual source pass must forget non-FULL chunk");
    var pa=HydrologyRuntime.contents(l,p,l.getBlockState(p));var pb=HydrologyRuntime.contents(l,q,l.getBlockState(q));var after=aquifer.ground.snapshot();
    require(pa.water()+pb.water()==WaterAccount.SCALE,"one regional unit accepted");
    require((scenario<2||scenario==5?pa:pb).water()==WaterAccount.SCALE,"finite quota delivered to wrong index priority scenario="+scenario);
    require(initial.water()==after.water()+pa.water()+pb.water()&&initial.salt()==after.salt()+pa.salt()+pb.salt()&&initial.sediment()==after.sediment()+pa.sediment()+pb.sediment(),"water/constituent conservation");
    data.setDirty();
    if(scenario==5)Files.writeString(receipt,Arrays.stream(combine(amount(after),amount(pa),amount(pb))).mapToObj(Long::toString).collect(java.util.stream.Collectors.joining(",")));
   }
  }
  for(String name:List.of(HydrologyRuntime.class.getName(),"com.shnewbs.deepprimal.engine.StreamSourceIndex")){
   Class<?> type=Class.forName(name);try(var in=type.getResourceAsStream("/"+name.replace('.','/')+".class")){System.out.println("INDEX_LOADED_CLASS "+name+" "+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));}
  }
  System.out.println("SOURCE_INDEX_NATIVE_PASS mode="+mode+" cases="+(mode.equals("fresh")?6:1)+" exactConservation=true quotaOrder=true persistence=true actualTick=true");
 }
 static long[] combine(long[]...a){return Arrays.stream(a).flatMapToLong(Arrays::stream).toArray();}
}
