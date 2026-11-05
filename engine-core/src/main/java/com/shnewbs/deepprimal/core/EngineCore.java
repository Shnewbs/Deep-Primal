package com.shnewbs.deepprimal.core;
import com.shnewbs.deepprimal.core.density.AdvancedDensityField;
import java.util.*;
import java.util.concurrent.*;

/** World-scoped query facade over the saved engine; owns only executors it creates. */
public final class EngineCore implements AutoCloseable {
  private final TerrainEngine terrain;
  private final ChunkCache densityCache,meshCache;
  private final AdvancedDensityField field;
  private final ChunkMaterializer materializer;
  private final WorldGenPipeline pipeline;
  private final ExecutorService ownedWorker;
  private final WaterSimulation water=new WaterSimulation();
  private final long seed;
  private boolean running,closed;
  private EngineCore(Builder b){
    seed=b.seed;terrain=b.terrain!=null?b.terrain:new TerrainEngine(b.seed,b.revision,b.options,b.channelRevision);
    densityCache=new ChunkCache(b.densityMB,b.densityEntries);meshCache=new ChunkCache(b.meshMB,b.meshEntries);
    field=new AdvancedDensityField(terrain,b.minY,b.height,densityCache);
    ownedWorker=b.worker==null?new ThreadPoolExecutor(1,Math.max(1,Math.min(2,Runtime.getRuntime().availableProcessors()/2)),30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64),job->{var t=new Thread(job,"DeepPrimal-Query");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy()):null;
    materializer=new ChunkMaterializer(terrain,field,b.worker!=null?b.worker:ownedWorker);pipeline=new WorldGenPipeline(materializer,meshCache);
  }
  public synchronized void start(){if(closed)throw new IllegalStateException("engine closed");running=true;}
  public void stop(){
    synchronized(this){if(closed)return;running=false;closed=true;}
    pipeline.shutdown();densityCache.close();meshCache.close();if(ownedWorker!=null)ownedWorker.shutdownNow();
  }
  @Override public void close(){stop();}
  public synchronized boolean isRunning(){return running;}
  public CompletableFuture<ChunkMaterializer.MaterializedChunk> generateChunkAsync(ChunkMaterializer.ChunkKey key){if(!isRunning())return CompletableFuture.failedFuture(new IllegalStateException("engine stopped"));return pipeline.generateChunk(key);}
  public CompletableFuture<Void> generateChunks(Iterable<ChunkMaterializer.ChunkKey> keys,java.util.function.Consumer<ChunkMaterializer.MaterializedChunk> callback){if(!isRunning())return CompletableFuture.failedFuture(new IllegalStateException("engine stopped"));return pipeline.generateChunks(keys,callback);}
  public double getHeight(double x,double z){synchronized(terrain){return terrain.column(x,z).ground();}}
  public TerrainEngine.Sample sample(double x,double z){long start=System.nanoTime();try{synchronized(terrain){return terrain.sample(x,z);}}finally{EngineMetrics.recordTerrainSample(System.nanoTime()-start);}}
  public int getBiome(double x,double z){synchronized(terrain){return terrain.biome(x,z);}}
  public int getBiome(double x,double y,double z){synchronized(terrain){return terrain.biome(x,y,z);}}
  public boolean isOcean(double x,double z){return terrain.ocean(x,z);}
  public double getTemperature(double x,double z,double h){return terrain.temperature(x,z,h);}
  public double getRainfall(double x,double z){return terrain.rainfall(x,z);}
  public WatershedRegion getWatershed(double x,double z){return terrain.waterways(x,z);}
  public boolean hasCave(double x,double y,double z,double surface,int minY){synchronized(terrain){return terrain.cave(x,y,z,surface,minY);}}
  public boolean hasTunnel(double x,double z){synchronized(terrain){return terrain.tunnel(x,z);}}
  public double getSurfaceWithoutIce(double x,double z){synchronized(terrain){return terrain.surfaceWithoutIce(x,z);}}
  public void simulateWater(double dt,java.util.function.DoubleSupplier ground){water.tick(dt,ground);}
  public AdvancedDensityField.SurfaceHit raycastTerrain(double x,double y,double z,double dx,double dy,double dz,double distance){return field.raycast(x,y,z,dx,dy,dz,distance);}
  public AdvancedDensityField.MeshData computeChunkMesh(int x,int z){return field.computeMesh(x,z);}
  public TerrainEngine getTerrainEngine(){return terrain;}
  public ChunkMaterializer getMaterializer(){return materializer;}
  public ChunkCache getDensityCache(){return densityCache;}public ChunkCache getMeshCache(){return meshCache;}
  public WorldGenPipeline getWorldGenPipeline(){return pipeline;}public AdvancedDensityField getDensityField(){return field;}
  public WaterSimulation getWaterSimulation(){return water;}
  public long getWorldSeed(){return seed;}public int getTerrainRevision(){return terrain.revision;}public TerrainOptions getTerrainOptions(){return terrain.options;}
  public static final class Builder {
    private long seed;private int revision=7,channelRevision,minY=-128,height=768;
    private TerrainOptions options=TerrainOptions.DEFAULT;private TerrainEngine terrain;private Executor worker;
    private int densityMB=16,densityEntries=128,meshMB=32,meshEntries=64;
    public Builder seed(long value){seed=value;return this;}public Builder revision(int value){revision=value;return this;}
    public Builder channelRevision(int value){channelRevision=value;return this;}public Builder options(TerrainOptions value){options=Objects.requireNonNull(value);return this;}
    public Builder terrain(TerrainEngine value){terrain=Objects.requireNonNull(value);return this;}
    public Builder verticalRange(int min,int size){minY=min;height=size;return this;}
    public Builder workerExecutor(Executor value){worker=Objects.requireNonNull(value);return this;}
    public Builder densityCacheMB(int value){densityMB=value;return this;}public Builder densityCacheEntries(int value){densityEntries=value;return this;}
    public Builder meshCacheMB(int value){meshMB=value;return this;}public Builder meshCacheEntries(int value){meshEntries=value;return this;}
    public EngineCore build(){return new EngineCore(this);}
  }
}
