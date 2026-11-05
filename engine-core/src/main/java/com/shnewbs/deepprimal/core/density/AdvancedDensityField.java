package com.shnewbs.deepprimal.core.density;

import com.shnewbs.deepprimal.core.*;
import java.util.*;

/** Bounded query/preview field using the saved terrain planner; never replaces saved geography. */
public final class AdvancedDensityField implements DensityField {
  private final TerrainEngine engine;
  private final int minY,height;
  private final ChunkCache cache;
  public AdvancedDensityField(TerrainEngine engine){this(engine,-128,768,new ChunkCache(16,128));}
  public AdvancedDensityField(TerrainEngine engine,int minY,int height,ChunkCache cache){
    this.engine=Objects.requireNonNull(engine);this.cache=Objects.requireNonNull(cache);
    if(height<1||height>DensityChunk.MAX_HEIGHT||minY < -DensityGraph.WORLD_LIMIT || (long)minY+height>DensityGraph.WORLD_LIMIT)throw new IllegalArgumentException("vertical range");
    this.minY=minY;this.height=height;
  }
  @Override public double sample(double x,double y,double z){return sampleFullRes(x,y,z);}
  public double sample(double x,double y,double z,int lod){
    if(lod<0||lod>3)throw new IllegalArgumentException("LOD 0..3");
    // Exact world-coordinate queries prevent collisions between LOD/cache keys and seams.
    return sampleFullRes(x,y,z);
  }
  public double sampleFullRes(double x,double y,double z){
    if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException("query cancelled");
    if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>DensityGraph.WORLD_LIMIT||Math.abs(z)>DensityGraph.WORLD_LIMIT||Math.abs(y)>DensityGraph.WORLD_LIMIT)throw new IllegalArgumentException("coordinates");
    synchronized(engine){var s=engine.column(x,z);double d=Math.floor(s.ground())+1-y;
      if(d>0 && engine.cave(x,y,z,s.ground(),minY))return -1;
      return d;
    }
  }
  public DensityChunk generateChunk(int x,int z){
    long key=((long)x<<32)|(z&0xffffffffL);
    return cache.getOrCompute(key,k->{
      int ox=Math.multiplyExact(x,16),oz=Math.multiplyExact(z,16);
      // DensityChunk visits one complete vertical column at a time. Plan that column once.
      DensityField sampler=new DensityField(){
        private double lastX=Double.NaN,lastZ=Double.NaN,ground;
        @Override public double sample(double wx,double y,double wz){
          if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException("query cancelled");
          synchronized(engine){
            if(wx!=lastX||wz!=lastZ){ground=engine.column(wx,wz).ground();lastX=wx;lastZ=wz;}
            double density=Math.floor(ground)+1-y;
            return density>0&&engine.cave(wx,y,wz,ground,minY)?-1:density;
          }
        }
      };
      return DensityChunk.sample(sampler,ox,minY,oz,height);
    },256L*height/8+1024);
  }
  public DensitySample sampleWithDerivatives(double x,double y,double z){double e=.125;return new DensitySample(sample(x,y,z),(sample(x+e,y,z)-sample(x-e,y,z))/(2*e),(sample(x,y+e,z)-sample(x,y-e,z))/(2*e),(sample(x,y,z+e)-sample(x,y,z-e))/(2*e));}
  public MeshData computeMesh(int x,int z){return mesh(generateChunk(x,z));}
  /** Exposed voxel faces, including the bounded query volume's edges. Not a host block-model mesh. */
  public static MeshData mesh(DensityChunk chunk){
    var builder=new MeshBuilder();
    int[][] direction={{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}};
    int[][][] corners={{{0,0,0},{0,0,1},{0,1,1},{0,1,0}},{{1,0,1},{1,0,0},{1,1,0},{1,1,1}},{{0,0,1},{0,0,0},{1,0,0},{1,0,1}},{{0,1,0},{0,1,1},{1,1,1},{1,1,0}},{{1,0,0},{0,0,0},{0,1,0},{1,1,0}},{{0,0,1},{1,0,1},{1,1,1},{0,1,1}}};
    chunk.forEachSolid((x,y,z)->{for(int f=0;f<6;f++){
      int nx=x+direction[f][0],ny=y+direction[f][1],nz=z+direction[f][2];
      if(nx>=0&&nx<16&&nz>=0&&nz<16&&ny>=chunk.minY()&&ny<chunk.maxYExclusive()&&chunk.solid(nx,ny,nz))continue;
      builder.face(x,y,z,corners[f],direction[f]);
    }});
    return builder.finish();
  }
  private static final class MeshBuilder {
    private static final int MAX_VERTICES=1_048_576;
    private static final int[] TRIANGLES={0,1,2,0,2,3};
    private float[] positions=new float[3072];
    private byte[] normals=new byte[3072];
    private int[] indices=new int[1536];
    private int vertices,indexCount;
    void face(int x,int y,int z,int[][] corners,int[] direction){
      if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException("mesh cancelled");
      if(vertices>MAX_VERTICES-4)throw new IllegalStateException("query mesh exceeds face budget");
      int required=(vertices+4)*3;
      if(required>positions.length){int capacity=Math.min(MAX_VERTICES*3,Math.max(required,positions.length*2));positions=Arrays.copyOf(positions,capacity);normals=Arrays.copyOf(normals,capacity);}
      if(indexCount+6>indices.length)indices=Arrays.copyOf(indices,Math.min(MAX_VERTICES/4*6,Math.max(indexCount+6,indices.length*2)));
      int base=vertices;
      for(int[] v:corners){int at=vertices++*3;positions[at]=x+v[0];positions[at+1]=y+v[1];positions[at+2]=z+v[2];for(int i=0;i<3;i++)normals[at+i]=(byte)(direction[i]*127);}
      for(int i:TRIANGLES)indices[indexCount++]=base+i;
    }
    MeshData finish(){return new MeshData(Arrays.copyOf(positions,vertices*3),Arrays.copyOf(indices,indexCount),Arrays.copyOf(normals,vertices*3),new short[vertices]);}
  }

  public SurfaceHit raycast(double ox,double oy,double oz,double dx,double dy,double dz,double maxDist){
    if(!Double.isFinite(maxDist)||maxDist<0||maxDist>4096)throw new IllegalArgumentException("ray distance 0..4096");
    double len=Math.hypot(Math.hypot(dx,dy),dz);if(!Double.isFinite(len)||len==0)throw new IllegalArgumentException("ray direction");dx/=len;dy/=len;dz/=len;
    double previous=sample(ox,oy,oz),last=0;
    for(double t=Math.min(.5,maxDist);t>last;t=Math.min(t+.5,maxDist)){
      double d=sample(ox+dx*t,oy+dy*t,oz+dz*t);
      if((previous>0)!=(d>0)){double lo=last,hi=t;boolean solid=previous>0;
        for(int i=0;i<16;i++){double mid=(lo+hi)/2;if((sample(ox+dx*mid,oy+dy*mid,oz+dz*mid)>0)==solid)lo=mid;else hi=mid;}
        double hit=(lo+hi)/2,x=ox+dx*hit,y=oy+dy*hit,z=oz+dz*hit;var n=sampleWithDerivatives(x,y,z).normalized();
        return new SurfaceHit(true,hit,x,y,z,-n.dx,-n.dy,-n.dz);
      } previous=d;last=t;
    }
    return new SurfaceHit(false,maxDist,0,0,0,0,0,0);
  }
  public void clearCaches(){cache.clear();}
  public record CacheStats(int chunkCount,int lodCount,long estimatedMemoryBytes){}
  public CacheStats getCacheStats(){return new CacheStats(cache.size(),0,cache.getMemoryUsage());}
  public record DensitySample(double density,double dx,double dy,double dz){
    public double nx(){return dx;}public double ny(){return dy;}public double nz(){return dz;}
    public double length(){return Math.hypot(Math.hypot(dx,dy),dz);}
    public DensitySample normalized(){double l=length();return l>0?new DensitySample(density,dx/l,dy/l,dz/l):this;}
  }
  public record SurfaceHit(boolean hit,double distance,double x,double y,double z,double nx,double ny,double nz){}
  public record MeshData(float[] positions,int[] indices,byte[] normals,short[] materialIds){
    public MeshData{if(positions.length%3!=0||indices.length%3!=0||normals.length!=positions.length||materialIds.length!=positions.length/3)throw new IllegalArgumentException("mesh layout");for(int i:indices)if(i<0||i>=positions.length/3)throw new IllegalArgumentException("mesh index");positions=positions.clone();indices=indices.clone();normals=normals.clone();materialIds=materialIds.clone();}
    @Override public float[] positions(){return positions.clone();}@Override public int[] indices(){return indices.clone();}@Override public byte[] normals(){return normals.clone();}@Override public short[] materialIds(){return materialIds.clone();}
    public long payloadBytes(){return 4L*positions.length+4L*indices.length+normals.length+2L*materialIds.length;}
  }
}
