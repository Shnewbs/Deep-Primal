package com.shnewbs.deepprimal.core;
import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import java.util.concurrent.*;

/** Regression checks for the integrated facade, finite query water and bounded CPU output. */
public final class EngineIntegrationTest {
  private static void check(boolean pass,String reason){if(!pass)throw new AssertionError(reason);}
  public static void main(String[] args)throws Exception{
    check(UpscalingPolicy.effective(UpscalingPolicy.Mode.SPATIAL,true,false)==UpscalingPolicy.Mode.SPATIAL,"spatial capability");
    for(var mode:new UpscalingPolicy.Mode[]{UpscalingPolicy.Mode.FSR,UpscalingPolicy.Mode.DLSS})check(UpscalingPolicy.effective(mode,true,false)==UpscalingPolicy.Mode.NONE,"vendor capability fallback");
    check(UpscalingPolicy.effective(UpscalingPolicy.Mode.SPATIAL,false,false)==UpscalingPolicy.Mode.NONE,"OpenGL untouched");
    check(UpscalingPolicy.effective(UpscalingPolicy.Mode.SPATIAL,true,true)==UpscalingPolicy.Mode.NONE,"frame generation fallback");
    check(UpscalingPolicy.dimension(1920,.75)==1440&&UpscalingPolicy.dimension(1,.5)==1,"resolution bounds");
    var cache=new ChunkCache(1,2);cache.put(1,"a",700000);cache.put(2,"b",700000);
    check(cache.size()==1&&cache.getMemoryUsage()==700000,"byte eviction");
    cache.put(3,"too large",Long.MAX_VALUE);check(cache.size()==1,"oversize admission");
    cache.put(2,"replace",8);check(cache.getMemoryUsage()==8,"replacement accounting");
    cache.put(4,"new",8);cache.get(2);cache.put(5,"newest",8);check(cache.get(4)==null&&cache.get(2)!=null,"true access-order eviction");
    var dimensionA=new ChunkMaterializer.ChunkKey(-1,2,"a");var dimensionB=new ChunkMaterializer.ChunkKey(-1,2,"b");
    cache.clear();cache.put(dimensionA,"A",1);cache.put(dimensionB,"B",1);check("A".equals(cache.get(dimensionA))&&"B".equals(cache.get(dimensionB)),"dimension-safe cache keys");
    var water=new WaterSimulation();water.setWaterLevel(0,0,1,true);long volume=water.getStats().totalWaterVolumeMl();
    for(int i=0;i<100;i++)water.tick(1,(x,z)->0);
    check(water.getStats().totalWaterVolumeMl()==volume,"closed water conservation with source metadata");
    check(water.getWaterLevel(0,0)<1,"source does not mint water");
    var fractional=new WaterSimulation();fractional.setWaterLevel(0,0,1,false);fractional.tick(.5,()->0);check(fractional.getWaterLevel(0,0)==1,"fractional tick");fractional.tick(.5,()->0);check(fractional.getWaterLevel(0,0)<1,"accumulated tick");
    var blocked=new WaterSimulation();blocked.setMaterial(15,15,(byte)2);
    try{blocked.addWater(15,15,1);throw new AssertionError("solid cell overwritten");}catch(IllegalStateException expected){}
    var buffer=java.nio.ByteBuffer.allocate(WaterSimulation.PACKET_BYTES+8);water.encodeToPacket(buffer,8,123);check(buffer.position()==0&&buffer.getInt(8)==0x44505753,"versioned packet");
    try{water.tick(Double.NaN,()->0);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
    var chunk=DensityChunk.sample((x,y,z)->x==0&&y==0&&z==0?1:-1,0,0,0,1);
    var mesh=AdvancedDensityField.mesh(chunk);check(mesh.positions().length==72&&mesh.indices().length==36,"six real voxel faces");
    for(int i:mesh.indices())check(i>=0&&i<24,"valid indices");
    for(int t=0;t<mesh.indices().length;t+=3){int a=mesh.indices()[t]*3,b=mesh.indices()[t+1]*3,c=mesh.indices()[t+2]*3;float[] p=mesh.positions();double ux=p[b]-p[a],uy=p[b+1]-p[a+1],uz=p[b+2]-p[a+2],vx=p[c]-p[a],vy=p[c+1]-p[a+1],vz=p[c+2]-p[a+2];check((uy*vz-uz*vy)*mesh.normals()[a]+(uz*vx-ux*vz)*mesh.normals()[a+1]+(ux*vy-uy*vx)*mesh.normals()[a+2]>0,"outward triangle winding");}
    float before=mesh.positions()[0];var mutable=mesh.positions();mutable[0]=999;check(mesh.positions()[0]==before,"immutable cached mesh");
    var queued=new ArrayList<Runnable>();
    try(var core=new EngineCore.Builder().revision(1).verticalRange(0,1).workerExecutor(queued::add).build()){
      core.start();var k=new ChunkMaterializer.ChunkKey(0,0,0);var first=core.generateChunkAsync(k);var shared=core.generateChunkAsync(k);check(queued.size()==1,"duplicate queries coalesce");first.cancel(false);queued.removeFirst().run();check(shared.get().mesh().indices().length>0,"caller cancellation isolated");
      for(int x=1;x<=64;x++)core.generateChunkAsync(new ChunkMaterializer.ChunkKey(x,0,0));
      check(core.generateChunkAsync(new ChunkMaterializer.ChunkKey(65,0,0)).isCompletedExceptionally(),"queue bounded");
      core.close();for(var task:queued)task.run();check(core.getDensityCache().size()==0&&core.getMeshCache().size()==0,"closed queues do not repopulate caches");
    }
    var worker=Executors.newSingleThreadExecutor();
    try(var core=new EngineCore.Builder().seed(123).revision(1).verticalRange(0,1).workerExecutor(worker).build()){
      core.start();var key=new ChunkMaterializer.ChunkKey(0,0,"overworld");var result=core.generateChunkAsync(key).get(60,TimeUnit.SECONDS);check(result.mesh().indices().length>0,"materialization has geometry");check(core.generateChunkAsync(key).get()==result,"query cache reuse");
      double top=Math.floor(core.getHeight(0,0))+1;
      check(core.getDensityField().sample(0,top,0)<=0&&core.getDensityField().sample(0,top-1,0)>0,"host surface quantization");
      var ray=core.raycastTerrain(0,1000,0,0,-1,0,2000);check(ray.hit()&&ray.ny()>0,"air-to-solid ray intersection");
      core.stop();check(!worker.isShutdown(),"caller executor retained");check(core.generateChunkAsync(key).isCompletedExceptionally(),"closed query rejected");
    }finally{worker.shutdownNow();}
    var neverStarted=new EngineCore.Builder().build();neverStarted.close();try{neverStarted.start();throw new AssertionError("closed restart accepted");}catch(IllegalStateException expected){}
    check(CoreValidation.validateSeed(123).passed(),"dry columns use the valid NaN water sentinel");
    EngineMetrics.reset();EngineMetrics.recordChunkMaterialize(2000);EngineMetrics.recordChunkMaterialize(4000);check(EngineMetrics.getAvgChunkMaterializeTimeUs()==3,"chunk timing mean");
    EngineBootstrap.initialize();EngineBootstrap.initialize();EngineBootstrap.shutdown();check(!EngineBootstrap.isInitialized(),"bootstrap cleanup");
    System.out.println("ENGINE_INTEGRATION_PASS cache water mesh ray lifecycle");
  }
}
