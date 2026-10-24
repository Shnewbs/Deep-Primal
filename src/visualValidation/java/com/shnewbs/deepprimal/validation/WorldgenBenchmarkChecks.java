package com.shnewbs.deepprimal.validation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import java.nio.file.*;
import java.lang.management.ManagementFactory;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Native fill/decorate timing, not host lighting/status/load-to-ready latency. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class WorldgenBenchmarkChecks {
  public WorldgenBenchmarkChecks(){NeoForge.EVENT_BUS.register(this);}
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception {
    String output=System.getenv("DEEPPRIMAL_WORLDGEN_BENCHMARK");if(output==null)return;
    var level=event.getServer().overworld();var source=(EngineChunkGenerator)level.getChunkSource().getGenerator();
    var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    var template=ChunkGenerator.CODEC.encodeStart(ops,source).getOrThrow().getAsJsonObject();
    var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);
    long thread=Thread.currentThread().threadId();
    var records=new JsonArray();var hashes=new JsonObject();
    var classes=new JsonObject();
    for(Class<?> type:List.of(EngineChunkGenerator.class,com.shnewbs.deepprimal.core.TerrainEngine.class,WorldgenBenchmarkChecks.class))
      try(var in=type.getResourceAsStream(type.getSimpleName()+".class")){classes.addProperty(type.getName(),HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));}
    for(long seed:new long[]{0,1,73,20261020}) {
      var g=DepositNativeChecks.generator(level,template,7,seed);
      for(int i=0;i<33;i++) {
        // Cold first chunk, eight warmup chunks, then 24 unique nearby measured chunks.
        int cx=(i%6)-3,cz=(i/6)-3;
        var c=new ProtoChunk(new ChunkPos(cx,cz),UpgradeData.EMPTY,level,PalettedContainerFactory.create(level.registryAccess()),null);
        long before=bean.getThreadAllocatedBytes(thread),start=System.nanoTime();
        var future=g.fillFromNoise(Blender.empty(),level.getChunkSource().randomState(),level.structureManager(),c);
        long returned=System.nanoTime();boolean completed=future.isDone();future.join();long done=System.nanoTime();
        long allocated=bean.getThreadAllocatedBytes(thread)-before;
        var row=new JsonObject();row.addProperty("seed",seed);row.addProperty("chunk_x",cx);row.addProperty("chunk_z",cz);
        row.addProperty("phase",i==0?"cold":i<=8?"warmup":"warm_unique");row.addProperty("fill_ms",(done-start)*1e-6);
        row.addProperty("call_return_ms",(returned-start)*1e-6);row.addProperty("completed_at_return",completed);row.addProperty("thread_allocated_bytes",allocated);
        records.add(row);
        if(i==0||i==17||i==32) {
          String digest=DepositNativeChecks.digest(c);hashes.addProperty(seed+":"+cx+":"+cz,digest);
          var replay=DepositNativeChecks.chunk(level,g,new int[]{cx,cz});if(!DepositNativeChecks.digest(replay).equals(digest))throw new AssertionError("benchmark generation replay");
        }
      }
    }
    var out=new JsonObject();out.addProperty("scope","Native ProtoChunk fillFromNoise including authored decoration; excludes construction, biome population, lighting, IO and host status graph");
    out.addProperty("invocation_thread",Thread.currentThread().getName());out.addProperty("min_y",level.getMinY());out.addProperty("height",level.getHeight());
    out.addProperty("java",System.getProperty("java.runtime.version"));out.addProperty("os_arch",System.getProperty("os.arch"));out.addProperty("processors",Runtime.getRuntime().availableProcessors());
    out.add("loaded_classes",classes);out.add("samples",records);out.add("replay_hashes",hashes);out.addProperty("pass",true);
    Files.writeString(Path.of(output),new GsonBuilder().setPrettyPrinting().create().toJson(out));
    System.out.println("WORLDGEN_BENCHMARK_PASS rows="+records.size()+" replayHashes="+hashes.size());
  }
}
