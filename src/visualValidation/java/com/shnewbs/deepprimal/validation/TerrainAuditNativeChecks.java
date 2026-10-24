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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Actual generator, saved-profile, seed binding, solid-column and biome/decorations audit. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class TerrainAuditNativeChecks {
  public TerrainAuditNativeChecks(){NeoForge.EVENT_BUS.register(this);}
  static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception {
    String output=System.getenv("DEEPPRIMAL_AUDIT_RECORD");if(output==null)return;
    int expected=Integer.parseInt(System.getenv("DEEPPRIMAL_AUDIT_REV"));
    var level=event.getServer().overworld();var own=(EngineChunkGenerator)level.getChunkSource().getGenerator();
    require(own.geography().revision==expected,"actual saved revision");
    var actualSeed=new TerrainEngine(level.getSeed(),expected,own.terrainProfile().options());
    require(own.geography().sample(341,-217).equals(actualSeed.sample(341,-217)),"actual saved-world seed binding");
    var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var encoded=ChunkGenerator.CODEC.encodeStart(ops,own).getOrThrow();
    var restored=(EngineChunkGenerator)ChunkGenerator.CODEC.parse(ops,encoded).getOrThrow();
    require(ChunkGenerator.CODEC.encodeStart(ops,restored).getOrThrow().equals(encoded),"saved codec/profile roundtrip");
    var result=new JsonObject();result.add("generator",encoded);var hashes=new JsonObject();var scenes=new JsonObject();
    var origins=new JsonArray();var identities=new JsonObject();
    for(Class<?> type:List.of(TerrainEngine.class,TerrainNoise.class,Landscape.class,WatershedRegion.class,EngineChunkGenerator.class,TerrainAuditNativeChecks.class))
      try(var in=type.getResourceAsStream(type.getSimpleName()+".class")){identities.addProperty(type.getName(),HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));}
    result.add("loaded_classes",identities);long blocks=0,logs=0,water=0;int chunks=0,biomeChecks=0;
    for(int revision:expected==7?new int[]{6,7}:new int[]{6})for(long seed:new long[]{0,1,73,20261020}) {
      var template=encoded.getAsJsonObject().deepCopy();template.addProperty("type","deepprimal:continental_v"+revision);
      var g=DepositNativeChecks.generator(level,template,revision,seed);var t=g.geography();
      var independent=new TerrainEngine(seed,revision,g.terrainProfile().options());
      require(t.sample(0,0).equals(independent.sample(0,0)),"native world seed binding");
      var origin=new JsonObject();origin.addProperty("seed",seed);origin.addProperty("revision",revision);origin.addProperty("height",t.raw(0,0));origin.addProperty("rain",t.rainfall(0,0));origins.add(origin);
      var sites=new LinkedHashSet<String>();Collections.addAll(sites,"0,0","-1,-1","1,0","0,1");
      var route=t.waterways(0,0).centerline();var broad=route.stream().max(Comparator.comparingDouble(WatershedRegion.Probe::radius)).orElseThrow();
      int rx=(int)Math.round(broad.x()),rz=(int)Math.round(broad.z());sites.add(Math.floorDiv(rx,16)+","+Math.floorDiv(rz,16));
      if(revision==7&&seed==20261020) {
        scenes.add("river",new Gson().toJsonTree(new int[]{rx,rz}));
        for(int z=-3072;z<=3072;z+=96)for(int x=-3072;x<=3072;x+=96){
          var s=t.sample(x,z);if(s.wet())continue;int b=t.biome(x,z);
          String key=b==2||b==9?"dry":Set.of(1,10,11,13,14,15).contains(b)&&s.slope()<.3?"forest":s.ground()>260?"mountain":b==21&&s.slope()<.3?"meadow":null;
          if(key!=null&&!scenes.has(key)){scenes.add(key,new Gson().toJsonTree(new int[]{x,z}));sites.add(Math.floorDiv(x,16)+","+Math.floorDiv(z,16));}
        }
      }
      for(String site:sites){String[] parts=site.split(",");int cx=Integer.parseInt(parts[0]),cz=Integer.parseInt(parts[1]);int[] at={cx,cz};
        var c=DepositNativeChecks.chunk(level,g,at);String hash=DepositNativeChecks.digest(c);hashes.addProperty(revision+":"+seed+":"+site,hash);
        require(hash.equals(DepositNativeChecks.digest(DepositNativeChecks.chunk(level,g,at))),"repeat chunk identity");chunks++;
        for(int z=0;z<16;z++)for(int x=0;x<16;x++){
          int wx=cx*16+x,wz=cz*16+z;var s=t.sample(wx,wz);int bed=(int)Math.floor(s.ground());
          var column=g.getBaseColumn(wx,wz,level,level.getChunkSource().randomState());
          for(int y=level.getMinY();y<=Math.min(639,Math.max(bed+32,s.wet()?(int)Math.ceil(s.water()):bed));y++){
            var actual=c.getBlockState(new BlockPos(wx,y,wz));
            if(y<=bed){var wanted=column.getBlock(y);require(actual.equals(wanted)||actual.is(Blocks.OAK_LOG)&&wanted.is(EngineRegistry.WATER_BLOCK.get()),"native base column "+wx+","+y+","+wz);blocks++;}
            if(actual.toString().contains("_log"))logs++;
            if(actual.is(EngineRegistry.WATER_BLOCK.get()))water++;
          }
          if((x&3)==0&&(z&3)==0){int yq=Math.floorDiv(bed+8,4);int id=t.biome(wx,yq*4,wz);
            var palette=template.getAsJsonObject("biome_source").getAsJsonArray("palette");
            String actual=g.getBiomeSource().getNoiseBiome(Math.floorDiv(wx,4),yq,Math.floorDiv(wz,4),level.getChunkSource().randomState().sampler()).unwrapKey().orElseThrow().identifier().toString();
            require(actual.equals(palette.get(id).getAsString()),"native biome palette binding");biomeChecks++;
          }
        }
      }
    }
    var edit=new BlockPos(9,500,9);boolean reopen="reopen".equals(System.getenv("DEEPPRIMAL_AUDIT_MODE"));
    if(reopen)require(level.getBlockState(edit).is(Blocks.GOLD_BLOCK),"saved player edit");
    level.setBlock(edit,Blocks.GOLD_BLOCK.defaultBlockState(),3);
    result.add("origins",origins);result.add("hashes",hashes);result.add("scenes",scenes);result.addProperty("blocks",blocks);result.addProperty("chunks",chunks);result.addProperty("logs",logs);result.addProperty("water",water);result.addProperty("biome_checks",biomeChecks);result.addProperty("pass",true);
    Files.writeString(Path.of(output),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    System.out.println("TERRAIN_AUDIT_NATIVE_PASS blocks="+blocks+" chunks="+chunks+" logs="+logs+" water="+water+" biomes="+biomeChecks+" reopen="+reopen);
  }
}
