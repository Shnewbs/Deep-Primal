package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Packaged native columns, saved creation profile, finite melt and player-edit regression. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class TerrainV6NativeChecks {
  public TerrainV6NativeChecks(){NeoForge.EVENT_BUS.register(this);}
  static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception {
    String output=System.getenv("DEEPPRIMAL_V6_RECORD");if(output==null)return;
    var l=event.getServer().overworld();var g=(EngineChunkGenerator)l.getChunkSource().getGenerator();
    require(g.geography().revision==6,"V6 selected by new world");var ops=l.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var encoded=ChunkGenerator.CODEC.encodeStart(ops,g).getOrThrow();
    var restored=(EngineChunkGenerator)ChunkGenerator.CODEC.parse(ops,encoded).getOrThrow();
    require(ChunkGenerator.CODEC.encodeStart(ops,restored).getOrThrow().equals(encoded),"creation profile roundtrip");
    require(g.terrainProfile().equals(restored.terrainProfile()),"all options and block contents saved");
    var result=new JsonObject();result.add("generator",encoded);var identities=new JsonObject();
    for(Class<?> type:List.of(TerrainEngine.class,Landscape.class,DepositPlan.class,CaveNetwork.class,EngineChunkGenerator.class,TerrainProfile.class,HydrologyRuntime.class,TerrainV6NativeChecks.class)){
      try(var in=type.getResourceAsStream(type.getSimpleName()+".class")){identities.addProperty(type.getName(),HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));}
    }result.add("loaded_classes",identities);
    var t=g.geography();var sites=new ArrayList<int[]>();
    Collections.addAll(sites,new int[]{0,0},new int[]{-1,-1},new int[]{-2,-1},new int[]{1,0},new int[]{0,1},new int[]{0,512},new int[]{0,1024});
    int[] glacier=null;
    outer:for(int z=14336;z<=18432;z+=128)for(int x=-2048;x<=2048;x+=128) {
      var sample=t.column(x,z);if(t.landscape.ice(x,z,sample)>=5){glacier=new int[]{x,z};break outer;}
    }
    require(glacier!=null,"glacier witness");sites.add(new int[]{Math.floorDiv(glacier[0],16),Math.floorDiv(glacier[1],16)});
    result.add("glacier",new Gson().toJsonTree(glacier));
    int[] tunnel=null;var route=t.waterways(0,0).centerline();
    for(int i=0;i<route.size();i+=4){int x=(int)Math.round(route.get(i).x()),z=(int)Math.round(route.get(i).z());var h=t.hydraulicColumn(x,z);
      if(t.tunnel(x,z)){tunnel=new int[]{x,z};break;}}
    require(tunnel!=null,"tunnel witness");result.add("tunnel",new Gson().toJsonTree(tunnel));
    var th=t.hydraulicColumn(tunnel[0],tunnel[1]);
    for(int side:new int[]{-1,1})for(int n=1;n<=8;n++) {
      int x=(int)Math.round(tunnel[0]-th.flowZ()*side*n),z=(int)Math.round(tunnel[1]+th.flowX()*side*n);
      var roof=g.getBaseColumn(x,z,l,l.getChunkSource().randomState()).getBlock((int)Math.ceil(th.water())+3);
      require(roof.isSolidRender(),"tunnel roof joins side rock "+x+","+z+" "+roof);
    }
    result.addProperty("lateral_roof_support_columns",16);
    for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)sites.add(new int[]{Math.floorDiv(tunnel[0],16)+dx,Math.floorDiv(tunnel[1],16)+dz});
    var oreSites=new TreeMap<Integer,int[]>();var plan=g.terrainProfile().plan(t.noise);
    outerOre:for(int z=-2048;z<=2048;z+=16)for(int x=-2048;x<=2048;x+=16) {
      var authored=plan.chunk(x,z,(a,b)->t.column(a,b).ground());if(authored.candidates()==0)continue;
      var sample=t.sample(x+8,z+8);var geology=new Geology(t.noise).column(x+8,z+8,sample);
      var oreColumn=authored.column(x+8,z+8,geology,(int)sample.ground());
      for(int y=l.getMinY()+12;y<sample.ground()-12;y+=2) {
        int ore=oreColumn.at(y);if(ore<0||oreSites.containsKey(ore))continue;
        if(g.getBaseColumn(x+8,z+8,l,l.getChunkSource().randomState()).getBlock(y).equals(g.terrainProfile().palette().get(ore)))oreSites.put(ore,new int[]{Math.floorDiv(x,16),Math.floorDiv(z,16)});
      }
      if(oreSites.size()==8)break outerOre;
    }
    require(oreSites.size()==8,"all default ore contents reachable "+oreSites.keySet());sites.addAll(oreSites.values());result.add("ore_sites",new Gson().toJsonTree(oreSites));
    var hashes=new JsonObject();long checked=0,water=0,cave=0,ice=0,ores=0,tunnels=0;var timings=new ArrayList<Double>();
    var pos=new BlockPos.MutableBlockPos();
    for(var site:sites){long start=System.nanoTime();var c=DepositNativeChecks.chunk(l,g,site);timings.add((System.nanoTime()-start)/1e6);
      hashes.addProperty(site[0]+","+site[1],DepositNativeChecks.digest(c));
      for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){
        int x=site[0]*16+dx,z=site[1]*16+dz;var s=t.sample(x,z);var h=t.hydraulicColumn(x,z);
        var column=g.getBaseColumn(x,z,l,l.getChunkSource().randomState());
        boolean hidden=t.tunnel(x,z);int bed=(int)Math.floor(s.ground());
        for(int y=l.getMinY();y<=bed;y++){
          var actual=c.getBlockState(pos.set(x,y,z));var expected=column.getBlock(y);
          // Worldgen debris can replace initial river water; underground and all solid layers must match exactly.
          require(actual.equals(expected)||actual.is(Blocks.OAK_LOG)&&expected.is(EngineRegistry.WATER_BLOCK.get()),"base column mismatch "+pos+" "+actual+" != "+expected);
          if(actual.is(EngineRegistry.WATER_BLOCK.get())){water++;require(!actual.getValue(EngineWaterBlock.OCEAN)||t.ocean(x,z),"finite inland water");}
          if(actual.isAir())cave++;
          if(actual.is(Blocks.PACKED_ICE)||actual.is(Blocks.BLUE_ICE))ice++;
          if(actual.toString().contains("_ore"))ores++;
          checked++;
        }
        if(hidden){tunnels++;int y=(int)Math.ceil(h.water());
          require(column.getBlock(y).isAir()&&column.getBlock(y+2).isAir(),"river has tunnel air passage");
          require(!column.getBlock(y+3).isAir(),"intact tunnel roof");
          require(column.getBlock((int)Math.floor(h.ground())+1).is(EngineRegistry.WATER_BLOCK.get()),"same finite water in tunnel");
        }
      }
    }
    require(ice>0&&cave>0&&water>0&&ores>0&&tunnels>0,"native feature coverage ice="+ice+" cave="+cave+" water="+water+" ore="+ores+" tunnel="+tunnels);
    // Regenerating independent protochunks in reverse order must reproduce every block.
    Collections.reverse(sites);for(var site:sites)require(hashes.get(site[0]+","+site[1]).getAsString().equals(DepositNativeChecks.digest(DepositNativeChecks.chunk(l,g,site))),"chunk order seam");
    var edit=new BlockPos(9,500,9);boolean reopen="reopen".equals(System.getenv("DEEPPRIMAL_V6_MODE"));
    if(reopen)require(l.getBlockState(edit).is(Blocks.GOLD_BLOCK),"player edit survives reopen");
    l.setBlock(edit,Blocks.GOLD_BLOCK.defaultBlockState(),3);
    var melt=new BlockPos(10,500,9);l.setBlock(melt,Blocks.PACKED_ICE.defaultBlockState(),3);
    require(!HydrologyRuntime.meltIce(l,melt,.1)&&l.getBlockState(melt).is(Blocks.PACKED_ICE),"cold glacier remains solid");
    l.setBlock(melt.above(),Blocks.STONE.defaultBlockState(),3);
    require(!HydrologyRuntime.meltIce(l,melt,.8),"roof blocks solar melt");l.setBlock(melt.above(),Blocks.AIR.defaultBlockState(),3);
    require(HydrologyRuntime.meltIce(l,melt,.8),"warm exposed physical melt");
    require(HydrologyRuntime.contents(l,melt,l.getBlockState(melt)).water()==4096,"one ice block pays exactly one water block");
    require(!HydrologyRuntime.meltIce(l,melt,.8),"cannot repeatedly refill melted ice");
    // Remove support in a controlled deep room. Construction supports and water must arrest a fall.
    var ceiling=new BlockPos(20,40,20);
    for(int dz=-5;dz<=5;dz++)for(int dx=-5;dx<=5;dx++)for(int dy=-5;dy<=2;dy++)
      l.setBlock(ceiling.offset(dx,dy,dz),dy==-5?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);
    l.setBlock(ceiling,Blocks.TUFF.defaultBlockState(),2|16);
    l.setBlock(ceiling.below(),EngineRegistry.WATER_BLOCK.get().defaultBlockState(),2|16);
    require(!StabilityRuntime.collapse(l,ceiling),"cave-in cannot consume water");
    l.setBlock(ceiling.below(),Blocks.AIR.defaultBlockState(),2|16);
    for(int dy=0;dy>=-2;dy--)l.setBlock(ceiling.offset(1,dy,0),Blocks.OAK_LOG.defaultBlockState(),2|16);
    require(!StabilityRuntime.collapse(l,ceiling),"constructed support prevents cave-in");
    for(int dy=0;dy>=-2;dy--)l.setBlock(ceiling.offset(1,dy,0),Blocks.AIR.defaultBlockState(),2|16);
    require(StabilityRuntime.collapse(l,ceiling)&&l.getBlockState(ceiling).isAir()&&l.getBlockState(ceiling.below(4)).is(Blocks.TUFF),"unsupported rock conserved at landing");
    result.addProperty("stability",true);
    result.add("protochunk_hashes",hashes);result.addProperty("columns",sites.size()*256);result.addProperty("block_comparisons",checked);
    result.addProperty("cave_air",cave);result.addProperty("ice_blocks",ice);result.addProperty("water_blocks",water);result.addProperty("ore_blocks",ores);result.addProperty("tunnel_columns",tunnels);
    result.add("generation_ms",new Gson().toJsonTree(timings));result.addProperty("reopen",reopen);result.addProperty("pass",true);
    Files.writeString(Path.of(output),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    System.out.println("TERRAIN_V6_NATIVE_PASS blocks="+checked+" tunnels="+tunnels+" ice="+ice+" ores="+ores+" reopen="+reopen);
  }
}
