package com.shnewbs.deepprimal;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.EngineRuntimeConfig;
import java.util.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Adapter owned by DeepPrimal's existing mod; it never registers a second mod or registry. */
public final class EngineCoreModule {
  private static final Map<String,EngineCore> worlds=new HashMap<>();
  @SubscribeEvent public void started(ServerStartedEvent event){
    shutdown();EngineBootstrap.initialize();
    try {for(var level:event.getServer().getAllLevels())if(level.getChunkSource().getGenerator() instanceof EngineChunkGenerator generator){
      var core=new EngineCore.Builder().seed(level.getSeed()).terrain(generator.geography())
          .verticalRange(generator.getMinY(),generator.getGenDepth())
          .densityCacheMB(EngineRuntimeConfig.DENSITY_MB.get()).densityCacheEntries(EngineRuntimeConfig.DENSITY_ENTRIES.get())
          .meshCacheMB(EngineRuntimeConfig.MESH_MB.get()).meshCacheEntries(EngineRuntimeConfig.MESH_ENTRIES.get()).build();core.start();
      synchronized(worlds){worlds.put(level.dimension().identifier().toString(),core);}
    }}catch(RuntimeException failure){shutdown();throw failure;}
  }
  @SubscribeEvent public void tick(ServerTickEvent.Post event){CoreValidation.tick();}
  @SubscribeEvent public void stopping(ServerStoppingEvent event){shutdown();}
  @SubscribeEvent public void stopped(ServerStoppedEvent event){shutdown();}
  public static EngineCore getEngineCore(){return getEngineCore("minecraft:overworld");}
  public static EngineCore getEngineCore(String dimension){synchronized(worlds){return worlds.get(dimension);}}
  private static void shutdown(){
    List<EngineCore> closing;
    synchronized(worlds){closing=new ArrayList<>(worlds.values());worlds.clear();}
    closing.forEach(EngineCore::close);EngineBootstrap.shutdown();
  }
}
