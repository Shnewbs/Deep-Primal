package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Private all-ore native witnesses and matched synchronous generation cost measurements. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class DepositCoverageChecks {
  public DepositCoverageChecks(){NeoForge.EVENT_BUS.register(this);}
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception {
    String output=System.getenv("DEEPPRIMAL_COVERAGE_RECORD");if(output==null)return;
    String productionHash;
    try(var in=DepositField.class.getResourceAsStream("DepositField.class")) {
      productionHash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes()));
    }
    if(!productionHash.equals(System.getenv("DEEPPRIMAL_EXPECT_DEPOSIT_CLASS_SHA")))
      throw new AssertionError("production class/source mismatch "+productionHash);
    var l=event.getServer().overworld();var ops=l.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var template=ChunkGenerator.CODEC.encodeStart(ops,l.getChunkSource().getGenerator()).getOrThrow().getAsJsonObject();
    var sites=JsonParser.parseString(Files.readString(Path.of(System.getenv("DEEPPRIMAL_COVERAGE_SITES")))).getAsJsonArray();
    var generators=new LinkedHashMap<Long,EngineChunkGenerator>();var hashes=new JsonObject();
    var seen=new EnumMap<Geology.Ore,Integer>(Geology.Ore.class);var hosts=new TreeSet<String>();
    var nativeCounts=new long[9];var footprints=new long[10];long columns=0,witnesses=0;
    var pos=new BlockPos.MutableBlockPos();
    for(var element:sites) {
      var site=element.getAsJsonObject();long seed=site.get("seed").getAsLong();
      var g=generators.computeIfAbsent(seed,s->DepositNativeChecks.generator(l,template,5,s));
      int cx=site.get("cx").getAsInt(),cz=site.get("cz").getAsInt();
      var c=DepositNativeChecks.chunk(l,g,new int[]{cx,cz});
      String key=seed+":"+cx+":"+cz;String digest=DepositNativeChecks.digest(c);
      if(hashes.has(key)&&!hashes.get(key).getAsString().equals(digest))throw new AssertionError("repeated chunk");
      hashes.addProperty(key,digest);
      int x=site.get("x").getAsInt(),y=site.get("y").getAsInt(),z=site.get("z").getAsInt();
      var s=g.geography().column(x,z);var host=new Geology(g.geography().noise).column(x,z,s);
      var expected=Geology.Ore.valueOf(site.get("ore").getAsString());
      if(DepositNativeChecks.oracle(g.geography().noise,host,x,y,z,(int)Math.floor(s.ground()))!=expected)
        throw new AssertionError("wider ore oracle "+site);
      if(!c.getBlockState(pos.set(x,y,z)).equals(DepositNativeChecks.material(expected,host.rock(y),y)))
        throw new AssertionError("native ore/host "+site+" "+c.getBlockState(pos));
      if(!g.getBaseColumn(x,z,l,l.getChunkSource().randomState()).getBlock(y).equals(c.getBlockState(pos)))
        throw new AssertionError("native witness column "+site);
      seen.merge(expected,1,Integer::sum);hosts.add(expected+":"+host.rock(y));witnesses++;
      var field=new DepositField(g.geography().noise);var geo=new Geology(g.geography().noise);
      var sample=new TerrainEngine.Sample(512,0,.6,.5,.2,.4,0,0,0,0);int active=0;
      for(int zz=cz*16;zz<cz*16+16;zz++)for(int xx=cx*16;xx<cx*16+16;xx++) {
        int count=field.column(xx,zz,geo.column(xx,zz,sample)).activeCandidateCount();footprints[count]++;
        if(count>0)active++;columns++;
      }
      if(active!=site.get("active_columns").getAsInt())throw new AssertionError("source footprint changed");
      for(var section:c.getSections())for(int yy=0;yy<16;yy++)for(int zz=0;zz<16;zz++)for(int xx=0;xx<16;xx++) {
        String name=BuiltInRegistries.BLOCK.getKey(section.getBlockState(xx,yy,zz).getBlock()).getPath();
        if(name.startsWith("deepslate_"))name=name.substring(10);
        if(name.endsWith("_ore"))nativeCounts[Geology.Ore.valueOf(name.substring(0,name.length()-4).toUpperCase(Locale.ROOT)).ordinal()]++;
      }
    }
    if(witnesses!=128||generators.size()!=16||seen.size()!=8||seen.values().stream().anyMatch(n->n!=16))
      throw new AssertionError("all-ore coverage "+seen);
    // Choose the same four dense-to-sparse chunk inputs for both variants.
    var ordered=new ArrayList<JsonObject>();sites.forEach(s->ordered.add(s.getAsJsonObject()));
    ordered.sort(Comparator.comparingInt(s->s.get("active_columns").getAsInt()));
    var profileSites=List.of(ordered.get(0),ordered.get(ordered.size()/3),ordered.get(ordered.size()*2/3),ordered.get(ordered.size()-1));
    var samples=new JsonArray();var factory=PalettedContainerFactory.create(l.registryAccess());
    var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);
    long tid=Thread.currentThread().threadId();
    for(int turn=-5;turn<12;turn++)for(int index=0;index<profileSites.size();index++) {
      int selected=turn%2==0?index:profileSites.size()-1-index;var site=profileSites.get(selected);
      long seed=site.get("seed").getAsLong();int cx=site.get("cx").getAsInt(),cz=site.get("cz").getAsInt();
      var g=generators.get(seed);long bytes=bean.getThreadAllocatedBytes(tid),began=System.nanoTime();
      var c=new ProtoChunk(new ChunkPos(cx,cz),UpgradeData.EMPTY,l,factory,null);
      g.fillFromNoise(Blender.empty(),l.getChunkSource().randomState(),l.structureManager(),c).join();
      long elapsed=System.nanoTime()-began,allocated=bean.getThreadAllocatedBytes(tid)-bytes;
      if(!DepositNativeChecks.digest(c).equals(hashes.get(seed+":"+cx+":"+cz).getAsString()))throw new AssertionError("profile changed output");
      if(turn>=0) {
        var sample=new JsonObject();sample.addProperty("turn",turn);sample.addProperty("site_index",selected);
        sample.addProperty("active_columns",site.get("active_columns").getAsInt());sample.addProperty("generation_ns",elapsed);
        sample.addProperty("thread_allocated_bytes",allocated);samples.add(sample);
      }
    }
    var result=new JsonObject();result.addProperty("fixture_schema",2);
    result.addProperty("deposit_class_sha256",productionHash);
    result.addProperty("pass",true);
    result.addProperty("witnesses",witnesses);result.addProperty("seeds",generators.size());result.add("ore_types",new Gson().toJsonTree(seen));
    result.add("ore_hosts",new Gson().toJsonTree(hosts));result.add("native_counts",new Gson().toJsonTree(nativeCounts));
    result.add("footprints",new Gson().toJsonTree(footprints));result.addProperty("columns",columns);result.add("hashes",hashes);
    result.add("profile_sites",new Gson().toJsonTree(profileSites));result.add("profile_samples",samples);
    result.addProperty("profile_scope","Server thread ProtoChunk construction plus synchronous full fill/decorate, warm regional caches; no lighting, biome pipeline, network, renderer, process peak or FPS acceptance");
    try(var in=DepositCoverageChecks.class.getResourceAsStream("DepositCoverageChecks.class")) {
      result.addProperty("fixture_class_sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));
    }
    Files.writeString(Path.of(output),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    System.out.println("DEPOSIT_COVERAGE_NATIVE_PASS witnesses="+witnesses+" seeds="+generators.size()+" oreTypes="+seen.size()+" profileSamples="+samples.size());
  }
}
