package com.shnewbs.deepprimal.core.lod;
import java.util.*;
public class ReductionBench {
 static volatile long blackhole;
 static long run(boolean before,ColumnarChunk c){long start=System.nanoTime();for(int i=0;i<3;i++){if(before)blackhole=BaselineLodMesher.build(c).bytes();else blackhole=LodMesher.build(c).bytes();}return (System.nanoTime()-start)/3;}
 static long pct(long[] x,double p){long[] y=x.clone();Arrays.sort(y);return y[(int)Math.ceil(y.length*p)-1];}
 public static void main(String[] a)throws Exception{
  var bean=(com.sun.management.ThreadMXBean)java.lang.management.ManagementFactory.getThreadMXBean();
  if(!bean.isThreadAllocatedMemoryEnabled())bean.setThreadAllocatedMemoryEnabled(true);
  for(int kind=0;kind<5;kind++){
   var c=LodReductionChecks.scene(kind,-64);
   for(int floor:new int[]{-64,320}){var scene=LodReductionChecks.scene(kind,floor);var b=BaselineLodMesher.build(scene);var n=LodMesher.build(scene);String bs=LodReductionChecks.signature(List.of(b.fine(),b.medium(),b.coarse()));String ns=LodReductionChecks.signature(List.of(n.fine(),n.medium(),n.coarse()));if(!bs.equals(ns))throw new AssertionError("Geometry differs");System.out.println("BASELINE_SIGNATURE "+kind+" "+floor+" "+bs);}
   for(int i=0;i<100;i++){run(true,c);run(false,c);}
   long[] old=new long[31],now=new long[31];long oldAlloc=0,newAlloc=0;
   for(int i=0;i<31;i++)for(boolean before:i%2==0?new boolean[]{true,false}:new boolean[]{false,true}){long allocated=bean.getThreadAllocatedBytes(Thread.currentThread().threadId());long t=run(before,c);allocated=bean.getThreadAllocatedBytes(Thread.currentThread().threadId())-allocated;if(before){old[i]=t;oldAlloc+=allocated;}else{now[i]=t;newAlloc+=allocated;}}
   System.out.println("BENCH kind="+kind+" beforeP50="+pct(old,.5)+" afterP50="+pct(now,.5)+" beforeP95="+pct(old,.95)+" afterP95="+pct(now,.95)+" beforeP99="+pct(old,.99)+" afterP99="+pct(now,.99)+" beforeAllocated="+oldAlloc/93+" afterAllocated="+newAlloc/93+" unit=ns_per_build allocation=bytes_per_build");
  }
 }
}
