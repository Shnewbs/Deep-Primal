package com.shnewbs.deepprimal.core;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.nio.*;

/** Closed 16x16 finite-volume query simulation using the production integer transport law.
 * Source flags are metadata: replenishment must be explicitly debited by the caller.
 * Live worlds continue to use WaterRuntime, including its saved constituent accounting. */
public final class WaterSimulation {
  public static final int CHUNK_SIZE=16,CELL_COUNT=256,PACKET_BYTES=16+CELL_COUNT*3;
  private final int[] units=new int[CELL_COUNT],ground=new int[CELL_COUNT];
  // Reused only while holding the tick monitor. Sample every height before committing
  // so an invalid callback cannot partially change the authoritative ground grid.
  private final int[] nextGround=new int[CELL_COUNT];
  // A height callback may synchronously reenter tick on the same instance.
  // Nested sampling needs its own scratch so it cannot overwrite the outer grid.
  private int groundSamplingDepth;
  private final double[] flowX=new double[CELL_COUNT],flowZ=new double[CELL_COUNT];
  private final boolean[] sources=new boolean[CELL_COUNT];private final byte[] materials=new byte[CELL_COUNT];
  private double fraction;
  public final AtomicLong simulationTimeNs=new AtomicLong(),totalWaterVolume=new AtomicLong(),activeCells=new AtomicLong();
  private static int index(int x,int z){if(x<0||x>=16||z<0||z>=16)throw new IllegalArgumentException("cell coordinates");return z*16+x;}
  private static int volume(double value){if(!Double.isFinite(value)||value<0||value>1||Math.abs(value*16-Math.rint(value*16))>1e-8)throw new IllegalArgumentException("volume must be 0..1 in 1/16 steps");return (int)Math.rint(value*16);}
  public synchronized void tick(double dt,DoubleSupplier height){tick(dt,(x,z)->height.getAsDouble());}
  public synchronized void tick(double dt,DoubleBinaryOperator height){
    if(!Double.isFinite(dt)||dt<0||dt>4)throw new IllegalArgumentException("tick delta 0..4");Objects.requireNonNull(height);
    int[] sampledGround=groundSamplingDepth==0?nextGround:new int[CELL_COUNT];
    groundSamplingDepth++;
    try {
      for(int z=0;z<16;z++)for(int x=0;x<16;x++){
        double h=height.applyAsDouble(x,z);
        if(!Double.isFinite(h)||Math.abs(h)>30_000_000)throw new IllegalArgumentException("ground height");
        sampledGround[index(x,z)]=(int)Math.floor(h);
      }
      System.arraycopy(sampledGround,0,ground,0,CELL_COUNT);
    } finally {
      groundSamplingDepth--;
    }
    long start=System.nanoTime();Arrays.fill(flowX,0);Arrays.fill(flowZ,0);fraction+=dt;
    while(fraction>=1){fraction--;for(int z=0;z<16;z++)for(int x=0;x<16;x++){int i=index(x,z);if(x<15)exchange(i,i+1,true);if(z<15)exchange(i,i+16,false);}}
    update();long elapsed=System.nanoTime()-start;simulationTimeNs.addAndGet(elapsed);EngineMetrics.recordWaterSim(elapsed);
  }
  private void exchange(int a,int b,boolean x){
    if(materials[a]>1||materials[b]>1)return;
    boolean reverse=(long)ground[a]*16+units[a]<(long)ground[b]*16+units[b];int from=reverse?b:a,to=reverse?a:b;
    var move=WaterTransport.pressure(ground[from],units[from],ground[to],units[to]);units[from]=move.from();units[to]=move.to();
    materials[from]=(byte)(units[from]>0?1:0);materials[to]=(byte)(units[to]>0?1:0);double v=move.moved()/16.0*(reverse?-1:1);double[] flow=x?flowX:flowZ;flow[a]+=v;flow[b]+=v;
  }
  /** Replacing water must not silently erase a solid cell or attach a source to one. */
  public synchronized void setWaterLevel(int x,int z,double level,boolean source){
    int i=index(x,z),next=volume(level);
    if(materials[i]==2){
      if(next!=0||source)throw new IllegalStateException("solid cell");
      return; // Zero-volume writes preserve the solid material.
    }
    units[i]=next;sources[i]=source;materials[i]=(byte)(next>0?1:0);update();
  }
  public synchronized void addWater(int x,int z,double amount){int i=index(x,z),add=volume(amount);if(materials[i]>1)throw new IllegalStateException("solid cell");if(add>16-units[i])throw new IllegalArgumentException("cell capacity");units[i]+=add;if(units[i]>0)materials[i]=1;update();}
  public synchronized double removeWater(int x,int z,double amount){int i=index(x,z),take=Math.min(units[i],volume(amount));units[i]-=take;if(units[i]==0&&materials[i]==1)materials[i]=0;update();return take/16.0;}
  public synchronized double getWaterLevel(int x,int z){return units[index(x,z)]/16.0;}
  public synchronized double getFlowX(int x,int z){return flowX[index(x,z)];}public synchronized double getFlowZ(int x,int z){return flowZ[index(x,z)];}
  public synchronized double getPressure(int x,int z){int i=index(x,z);return (ground[i]+units[i]/16.0)*9810;}
  public synchronized boolean isSource(int x,int z){return sources[index(x,z)];}
  public synchronized void setMaterial(int x,int z,byte material){
    if(material<0||material>2)throw new IllegalArgumentException("material");
    int i=index(x,z);
    if(units[i]>0&&material!=1)throw new IllegalStateException("drain water before replacing material");
    // Material 1 is a water-filled cell, never an independent dry marker.
    // Reject malformed imports rather than serializing phantom water materials.
    if(units[i]==0&&material==1)throw new IllegalStateException("dry cell cannot have water material");
    materials[i]=material;
    if(material==2)sources[i]=false; // A solid cannot remain a source after conversion.
  }
  public synchronized byte getMaterial(int x,int z){return materials[index(x,z)];}
  /** Independent versioned query snapshot, not WaterCornerPacket or world saved data. */
  public synchronized void encodeToPacket(ByteBuffer buffer,int offset,long timestamp){
    if(buffer.isReadOnly()||offset<0||offset>buffer.limit()-PACKET_BYTES)throw new IllegalArgumentException("packet buffer");var out=buffer.duplicate().order(ByteOrder.BIG_ENDIAN);out.position(offset);out.putInt(0x44505753).putInt(1).putLong(timestamp);for(int i=0;i<256;i++)out.put((byte)units[i]).put((byte)(sources[i]?1:0)).put(materials[i]);
  }
  public static WaterSimulation fromChunkData(double[] levels,boolean[] sources,byte[] materials){
    if(levels.length!=CELL_COUNT||sources.length!=CELL_COUNT||materials.length!=CELL_COUNT)
      throw new IllegalArgumentException("256 cells required");
    var result=new WaterSimulation();
    // A new instance has no observers. Populate each validated cell directly,
    // preserving setMaterial's dry/wet/solid rules and solid-source normalization.
    // Calling setWaterLevel here recalculated all 256 totals for every cell (O(n^2)).
    for(int i=0;i<CELL_COUNT;i++){
      int amount=volume(levels[i]);
      result.units[i]=amount;
      result.sources[i]=sources[i];
      result.materials[i]=(byte)(amount>0?1:0);
      result.setMaterial(i&15,i>>>4,materials[i]);
    }
    result.update();
    return result;
  }
  private void update(){long sum=0,active=0;for(int u:units){sum+=u;if(u>0)active++;}totalWaterVolume.set(sum*62500L);activeCells.set(active);}
  public record Stats(long activeCells,long totalWaterVolumeMl,long simulationTimeNs){}
  public synchronized Stats getStats(){update();return new Stats(activeCells.get(),totalWaterVolume.get(),simulationTimeNs.get());}
  public void resetStats(){simulationTimeNs.set(0);}
}
