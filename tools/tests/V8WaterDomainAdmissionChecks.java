package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;

/** Isolated Java21 API-double source contract. NOT real Mojang, Minecraft or GPU. */
public final class V8WaterDomainAdmissionChecks {
  private static long checks, rejected, compared, masked;
  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  private static void reject(Runnable action, String name) {
    boolean failed = false;
    try { action.run(); } catch (IllegalArgumentException | IllegalStateException | NullPointerException expected) {
      failed = true;
    }
    check(failed,"failed to reject " + name);rejected++;
  }
  private static final long SEED=Long.MIN_VALUE+198;
  private static V8GeneratorSnapshot snapshot(int minY, int height) {
    var graph=new DensityGraph.Definition(1,"solid",List.of(
        DensityGraph.Node.constant("solid",1.0)));
    return new V8GeneratorSnapshot(1,SEED,minY,height,graph,
        new GroundSurfaceProfile("solid",5))
        .withBlendPolicy(new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW))
        .withSurfacePolicy(V8SurfacePolicy.FROZEN_R1)
        .withClimateBinding(V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,5));
  }
  private static V8EcotoneColumnPlan.ClimateSource climate() {
    return (worldX,topY,worldZ)->new V8EcotoneColumnPlan.ClimateDatum(
        new EcotoneBlend.Climate(topY,.55,.48,.22,.37),
        Math.floorMod(worldZ,7)==0?EcotoneBlend.Domain.OCEAN
          :Math.floorMod(worldX,5)==0?EcotoneBlend.Domain.RIVER
          :EcotoneBlend.Domain.LAND);
  }
  private static V8WaterDomainAdmission.WaterDatum water(int x,int z) {
    if(Math.floorMod(x,17)==0)return V8WaterDomainAdmission.WaterDatum.ocean();
    if(Math.floorMod(z,13)==0)return new V8WaterDomainAdmission.WaterDatum(
        V8WaterDomainAdmission.WaterKind.LAKE,98.25);
    if(Math.floorMod(x,11)==0)return new V8WaterDomainAdmission.WaterDatum(
        V8WaterDomainAdmission.WaterKind.RIVER,102.5);
    return V8WaterDomainAdmission.WaterDatum.dry();
  }
  private static V8WaterDomainAdmission.Authority authority(V8GeneratorSnapshot snapshot) {
    return new V8WaterDomainAdmission.Authority(snapshot.seed(),snapshot.density(),
        snapshot.climateBinding(),1,V8WaterDomainAdmissionChecks::water);
  }
  private static boolean dry(int x,int z,int topY) {
    return topY>=V8WaterDomainAdmission.SEA_PLANE
        && Math.floorMod(z,7)!=0 && Math.floorMod(x,5)!=0
        && water(x,z).kind()==V8WaterDomainAdmission.WaterKind.DRY;
  }
  private static final class Fake extends ChunkAccess {
    private final ChunkPos pos;
    private final int minY,height;
    private final BlockState[] data;
    private final Heightmap surface,floor;
    private int writes;
    Fake(int x,int z,int minY,int height) {
      this.pos=new ChunkPos(x,z);this.minY=minY;this.height=height;
      this.data=new BlockState[height*256];Arrays.fill(data,new BlockState("untouched"));
      this.surface=new Heightmap(minY);this.floor=new Heightmap(minY);
    }
    @Override public ChunkPos getPos(){return pos;}
    @Override public int getMinY(){return minY;}
    @Override public int getHeight(){return height;}
    @Override public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types kind){
      return kind==Heightmap.Types.WORLD_SURFACE_WG?surface:floor;
    }
    @Override public BlockState getBlockState(int x,int y,int z){return data[((y-minY)<<8)|(z<<4)|x];}
    @Override public void setBlockState(BlockPos where,BlockState state){
      int x=where.getX()-pos.getMinBlockX(),y=where.getY()-minY,z=where.getZ()-pos.getMinBlockZ();
      if(x<0||x>=16||y<0||y>=height||z<0||z>=16)throw new AssertionError("out of chunk write");
      data[(y<<8)|(z<<4)|x]=state;writes++;
    }
  }
  private static void dataContract() {
    var kind=V8WaterDomainAdmission.WaterKind.RIVER;
    reject(()->new V8WaterDomainAdmission.WaterDatum(kind,Double.NaN),"wet NaN stage");
    reject(()->new V8WaterDomainAdmission.WaterDatum(kind,Double.POSITIVE_INFINITY),"wet infinite stage");
    reject(()->new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.DRY,0),"dry stage");
    reject(()->new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.OCEAN,95),"ocean plane mismatch");
    reject(()->new V8WaterDomainAdmission.WaterDatum(null,96),"null domain");
    check(Double.isNaN(V8WaterDomainAdmission.WaterDatum.dry().physicalStage()),"dry sentinel");
    var s=snapshot(80,48);
    var p=s.prepare();
    var correct=authority(s);
    check(V8WaterDomainAdmission.bind(p,climate(),correct)!=null,"correct authority bound");
    reject(()->V8WaterDomainAdmission.bind(p,climate(),new V8WaterDomainAdmission.Authority(
        s.seed()+1,s.density(),s.climateBinding(),1,correct.source())),"wrong seed");
    var different=snapshot(64,48);
    reject(()->V8WaterDomainAdmission.bind(p,climate(),new V8WaterDomainAdmission.Authority(
        s.seed(),different.density(),V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,4),1,correct.source())),"wrong climate identity");
    var otherGraph=new DensityGraph.Definition(1,"solid",List.of(DensityGraph.Node.constant("solid",.75)));
    reject(()->V8WaterDomainAdmission.bind(p,climate(),new V8WaterDomainAdmission.Authority(
        s.seed(),otherGraph,s.climateBinding(),1,correct.source())),"wrong density identity");
    reject(()->new V8WaterDomainAdmission.Authority(s.seed(),s.density(),s.climateBinding(),2,
        correct.source()),"unsaved observation version");
    var r3=new V8GeneratorSnapshot(3,s.seed(),s.minY(),s.height(),s.density(),
        s.ground(),s.blendPolicy(),s.surfacePolicy());
    reject(()->V8WaterDomainAdmission.bind(r3.prepare(),climate(),correct),"r3 admission");
    // Existing r4 source entry point is unchanged and deliberately lacks admission.
    var plain=DensityChunkMaterializer.planSavedEcotone(p,0,0,80,48,16,climate());
    var strict=DensityChunkMaterializer.planSavedEcotoneAdmitted(p,0,0,80,48,16,climate(),correct);
    check(plain.columns().eligibleColumns()>strict.columns().eligibleColumns(),
        "admission must actually protect additional V8-water cells");
  }
  private static void atlas(int minY,int height,int chunkX,int chunkZ) {
    var snapshot=snapshot(minY,height);
    var prepared=snapshot.prepare();var mask=authority(snapshot);
    int wx=chunkX*16,wz=chunkZ*16;
    var first=DensityChunkMaterializer.planSavedEcotoneAdmitted(prepared,wx,wz,
        minY,height,16,climate(),mask);
    var second=DensityChunkMaterializer.planSavedEcotoneAdmitted(prepared,wx,wz,
        minY,height,16,climate(),mask);
    var chunk=new Fake(chunkX,chunkZ,minY,height);
    var written=DensityChunkMaterializer.fillSavedEcotoneAdmitted(
        prepared,chunk,16,climate(),mask);
    int eligible=0;
    for(int z=15;z>=0;z--)for(int x=15;x>=0;x--){
      int worldX=Math.addExact(wx,x),worldZ=Math.addExact(wz,z),topY=minY+height-1;
      boolean allowed=dry(worldX,worldZ,topY);
      var a=first.columns().column(x,z);
      var b=second.columns().column(x,z);
      check((a!=null)==allowed,"land admission mismatch x="+worldX+" z="+worldZ);
      check(Objects.equals(a,b),"repeat order gave different cover");
      check(Objects.equals(a,written.columns().column(x,z)),"prewrite/written mask mismatch");
      if(a!=null)eligible++;else masked++;
      var column=DensityChunkMaterializer.baseColumn(first,x,z);
      for(int y=minY;y<minY+height;y++){
        check(column.getBlock(y).equals(chunk.getBlockState(x,y,z)),
            "native-stub query/write voxel difference "+worldX+","+y+","+worldZ);
        compared++;
      }
    }
    check(first.columns().eligibleColumns()==eligible,"eligible count");
    check(written.columns().eligibleColumns()==eligible,"write eligible count");
  }
  private static void writeFailure() {
    var snapshot=snapshot(80,48);var prepared=snapshot.prepare();
    var authority=new V8WaterDomainAdmission.Authority(snapshot.seed(),snapshot.density(),
        snapshot.climateBinding(),1,(x,z)->{
          if(x==7&&z==7)throw new IllegalStateException("modeled V8 mask failure");
          return water(x,z);
        });
    var target=new Fake(0,0,80,48);
    reject(()->DensityChunkMaterializer.fillSavedEcotoneAdmitted(
        prepared,target,16,climate(),authority),"prewrite hydrology failure");
    check(target.writes==0,"hydrology failure mutated chunk before preflight");
    var badStage=new V8WaterDomainAdmission.Authority(snapshot.seed(),snapshot.density(),
        snapshot.climateBinding(),1,(x,z)->x==9&&z==3
            ?new V8WaterDomainAdmission.WaterDatum(V8WaterDomainAdmission.WaterKind.OCEAN,95)
            :water(x,z));
    reject(()->DensityChunkMaterializer.fillSavedEcotoneAdmitted(
        prepared,target,16,climate(),badStage),"invalid ocean callback");
    check(target.writes==0,"invalid ocean stage mutated native chunk");
  }
  private static void parallel() throws Exception {
    var snap=snapshot(80,48);var prep=snap.prepare();var authority=authority(snap);
    var pool=Executors.newFixedThreadPool(4);
    try {
      List<Callable<Integer>> jobs=new java.util.ArrayList<>();
      for(int i=0;i<16;i++){
        final int cx=i-8,cz=8-i;
        jobs.add(()->DensityChunkMaterializer.planSavedEcotoneAdmitted(prep,cx*16,
            cz*16,80,48,16,climate(),authority).columns().eligibleColumns());
      }
      var submitted=pool.invokeAll(jobs);
      for(int i=0;i<jobs.size();i++){
        int expected=jobs.get(i).call();
        check(submitted.get(i).get()==expected,"parallel/reverse-order domain replay");
      }
    } finally {pool.shutdownNow();}
  }
  public static void main(String[] args)throws Exception {
    dataContract();writeFailure();
    for(int cz=-2;cz<=1;cz++)for(int cx=-2;cx<=1;cx++)atlas(80,48,cx,cz);
    atlas(48,48,-1,-1); // entire modeled dry floor is below sea: deny all painting
    parallel();
    System.out.println("V8_WATER_DOMAIN_ADMISSION_PASS assertions="+checks+
        " voxelComparisons="+compared+" maskedColumns="+masked+
        " rejected="+rejected+" native=false");
  }
}
