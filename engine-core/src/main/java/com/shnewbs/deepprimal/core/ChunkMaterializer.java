package com.shnewbs.deepprimal.core;
import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import java.util.concurrent.*;

/** CPU query materialization. GPU ownership stays with the host renderer and LOD module. */
public final class ChunkMaterializer {
  private final TerrainEngine engine;private final AdvancedDensityField field;private final Executor executor;
  public ChunkMaterializer(TerrainEngine engine,AdvancedDensityField field,Executor executor){this.engine=Objects.requireNonNull(engine);this.field=Objects.requireNonNull(field);this.executor=Objects.requireNonNull(executor);}
  public CompletableFuture<MaterializedChunk> materializeAsync(ChunkKey key){return materializeAsync(key,()->false);}
  CompletableFuture<MaterializedChunk> materializeAsync(ChunkKey key,java.util.function.BooleanSupplier cancelled){return CompletableFuture.supplyAsync(()->{if(cancelled.getAsBoolean())throw new CancellationException("query stopped");return materializeSync(key);},executor);}
  public MaterializedChunk materializeSync(ChunkKey key){long start=System.nanoTime();try{
    var density=field.generateChunk(key.chunkX,key.chunkZ);var mesh=AdvancedDensityField.mesh(density);
    float[] levels=new float[256],xFlow=new float[256],zFlow=new float[256];Arrays.fill(levels,Float.NaN);
    synchronized(engine){for(int z=0;z<16;z++)for(int x=0;x<16;x++){int i=z*16+x;var s=engine.column(key.chunkX*16+x,key.chunkZ*16+z);if(s.wet()){levels[i]=(float)s.water();xFlow[i]=(float)s.flowX();zFlow[i]=(float)s.flowZ();}}}
    return new MaterializedChunk(key,mesh,new WaterData(levels,xFlow,zFlow),density);
  }finally{EngineMetrics.recordChunkMaterialize(System.nanoTime()-start);}}
  public record ChunkKey(int chunkX,int chunkZ,String dimension){
    public ChunkKey{Objects.requireNonNull(dimension);if(dimension.isBlank()||dimension.length()>256||Math.abs((long)chunkX)>1_874_999||Math.abs((long)chunkZ)>1_874_999)throw new IllegalArgumentException("chunk key");}
    public ChunkKey(int x,int z,int dimension){this(x,z,Integer.toString(dimension));}
  }
  public record WaterData(float[] levels,float[] flowX,float[] flowZ){
    public WaterData{if(levels.length!=256||flowX.length!=256||flowZ.length!=256)throw new IllegalArgumentException("water columns");levels=levels.clone();flowX=flowX.clone();flowZ=flowZ.clone();}
    @Override public float[] levels(){return levels.clone();}@Override public float[] flowX(){return flowX.clone();}@Override public float[] flowZ(){return flowZ.clone();}
  }
  public record MaterializedChunk(ChunkKey key,AdvancedDensityField.MeshData mesh,WaterData water,DensityChunk density){public long payloadBytes(){return density.payloadBytes()+mesh.payloadBytes()+3L*256*Float.BYTES;}}
}
